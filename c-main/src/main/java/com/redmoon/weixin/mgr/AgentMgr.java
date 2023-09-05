package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.util.HttpPostFileUtil;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

/**
 * Created by fgf on 2018/12/15.
 */
public class AgentMgr {
    public static String setAgent(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        JSONObject json = new JSONObject();
        Config cfg = Config.getInstance();

        FileUpload fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"png", "jpg"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        try {
            int ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(AgentMgr.class.getName()).error("doUpload:" + e.getMessage());
        }

        String oldAgentId = fileUpload.getFieldValue("oldAgentId");
        String agentId = fileUpload.getFieldValue("agentId");
        String secret = fileUpload.getFieldValue("secret");
        String agentName = fileUpload.getFieldValue("agentName");
        String homeUrl = fileUpload.getFieldValue("homeUrl");

        // 构造能够传递用户身份信息code的授权链接
        String home_url;
        String serverName = Config.getInstance().getProperty("serverName");
        if (!serverName.equals("qyapi.weixin.qq.com")) {
            home_url = "https://" + serverName + "/connect/oauth2/authorize?appid=CORPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
        }
        else {
            home_url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=CORPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
        }
        String corpId = cfg.getProperty("corpId");
        home_url = home_url.replace("CORPID", corpId).replace("REDIRECT_URI", StrUtil.UrlEncode(homeUrl));

        WXBaseMgr wm = new WXBaseMgr();
        String accessToken = wm.getToken(secret);
        String url = "https://" + serverName + "/cgi-bin/agent/set?access_token=" + accessToken;

        Vector v = fileUpload.getFiles();
        try {
            if (v.size() > 0) {
                FileInfo fi = (FileInfo) v.elementAt(0);
                // 置保存路径，保存图片
                String vpath = "upfile/";
                String filePath = Global.getRealPath() + vpath;
                fileUpload.setSavePath(filePath);
                // 使用随机名称写入磁盘
                fileUpload.writeFile(true);

                filePath += fi.getDiskName();
                String result = uploadMedia(secret, filePath);
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    if (jsonResult.has("media_id")) {
                        // 上传的媒体的ID
                        String mediaId = jsonResult.getString("media_id");
                        cfg.setAgent(oldAgentId, agentId, agentName, secret, mediaId, homeUrl);

                        JSONObject js = new JSONObject();
                        js.put("agentid", StrUtil.toLong(agentId));
                        js.put("report_location_flag", 0);
                        js.put("logo_mediaid", mediaId);
                        js.put("name", agentName);
                        js.put("description", agentName);
                        js.put("redirect_domain", Global.server);
                        js.put("isreportenter", 0);
                        js.put("home_url", home_url);
                        String r = HttpUtil.MethodPost(url, js.toString());
                        JSONObject jsonObj = new JSONObject(r);
                        if (jsonObj.getInt("errcode") == 0) {
                            json.put("ret", 1);
                            json.put("msg", "操作成功！");
                        } else {
                            json.put("ret", 0);
                            json.put("msg", jsonObj.getString("errmsg"));
                        }
                    } else {
                        DebugUtil.log(AgentMgr.class, "setAgent", result);
                        json.put("ret", 0);
                        json.put("msg", jsonResult.getString("errmsg"));
                        json.put("errCode", jsonResult.getInt("errcode"));
                    }
                } finally {
                    File f = new File(filePath);
                    f.delete();
                }
            } else {
                String logo = cfg.getAgentAttr(agentId, "logo");
                // 如果媒体文件未上传，则不能设置应用信息，因为logo_mediaid在获取应用时取不到
                if ("".equals(logo)) {
                    json.put("ret", 0);
                    json.put("msg", "请上传应用LOGO");
                } else {
                    cfg.setAgent(oldAgentId, agentId, agentName, secret, logo, homeUrl);

                    // 如果media文件已上传*/
                    JSONObject js = new JSONObject();
                    js.put("agentid", StrUtil.toLong(agentId));
                    js.put("report_location_flag", 0);
                    js.put("logo_mediaid", logo);
                    js.put("name", agentName);
                    js.put("description", agentName);
                    js.put("redirect_domain", Global.server);
                    js.put("isreportenter", 0);
                    js.put("home_url", home_url);
                    String r = HttpUtil.MethodPost(url, js.toString());
                    JSONObject jsonObj = new JSONObject(r);
                    if (jsonObj.getInt("errcode") == 0) {
                        json.put("ret", 1);
                        json.put("msg", "操作成功！");
                    } else {
                        json.put("ret", 0);
                        json.put("msg", jsonObj.getString("errmsg"));
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(AgentMgr.class).error(e);
        }
        cfg.reload();
        return json.toString();
    }

    public static String uploadMedia(String secret, String filePath) {
        Config cfg = Config.getInstance();
        String serverName = cfg.getProperty("serverName");

        // String mainAgentId = cfg.getProperty("mainAgentId");
        WXBaseMgr wm = new WXBaseMgr();
        String accessToken = wm.getToken(secret);

        // 根据接口规范上传临时图片
        String uploadUrl = "https://" + serverName + "/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=image";
        uploadUrl = uploadUrl.replace("ACCESS_TOKEN", accessToken);
        File file = new File(filePath);
        //此处修改为自己上传文件的地址
        HttpPostFileUtil post = null;
        try {
            post = new HttpPostFileUtil(uploadUrl);
            //此处参数类似 curl -F media=@test.jpg
            post.addParameter("media", file);
            return post.send();
        } catch (IOException e) {
            LogUtil.getLog(AgentMgr.class).error(e);
        }

        return "";

/*        if (jsonObject.has("media_id")) {
            // upload.setMedia_id(jsonObject.getString("media_id"));
            // upload.setCreated_at(jsonObject.getString("created_at"));

        } else {
            // result.setErrmsg(jsonObject.getString("errmsg"));
            // result.setErrcode(jsonObject.getString("errcode"));
        }*/
    }
}
