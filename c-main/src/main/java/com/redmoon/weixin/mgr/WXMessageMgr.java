package com.redmoon.weixin.mgr;

import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @author:
 * @Date: 2016-8-8下午03:34:57
 */
public class WXMessageMgr extends WXBaseMgr {
    public static enum MESSAGE_TYPE_ENMU {
        NOTICE, SYSMSG, MSG, FLOW
    }

    public final static String TEXT_TYPE = "text";

    public int sendTextMessage(String userName, String content, String action, MESSAGE_TYPE_ENMU message_enum) {
        Config config = Config.getInstance();
        if (!config.getBooleanProperty("isUse")) {
            return 0;
        }

        // 转换其中的<p>...</p>
        content = StrUtil.getAbstract(null, content, 3000, "\r\n");

        String userId = userName;
        if (config.isUserIdUseEmail()) {
            UserDb userDb = new UserDb();
            userDb = userDb.getUserDb(userName);
            userId = userDb.getWeixin();
        } else if (config.isUserIdUseAccount()) {
            AccountDb accountDb = new AccountDb();
            accountDb = accountDb.getUserAccount(userName);
            userId = accountDb.getName();
        } else if (config.isUserIdUseMobile()) {
            UserDb userDb = new UserDb();
            userDb = userDb.getUserDb(userName);
            userId = userDb.getWeixin();
        }

        String link = "";

        String agentId = config.getDefaultAgentId();

        // message_enum = MESSAGE_TYPE_ENMU.SYSMSG;

        switch (message_enum) {
/*            case NOTICE:
            case SYSMSG:
            case MSG:
                // agentId = config.getIntProperty("sysMsgAgentMenuId");
                // agentId = config.getIntProperty("noticeAgentMenuId");
                // agentId = config.getIntProperty("msgAgentMenuId");
                break;*/
            case FLOW:
                // agentId = config.getProperty("sysMsgAgentMenuId");
                // agentId = config.getIntProperty("flowAgentMenuId");
                // action=flow_dispose|myActionId=975
                String[] ary = StrUtil.split(action, "\\|");
                if (ary != null) {
                    String actionType = ary[0];
                    if ("action=flow_dispose".equals(actionType)) {
                        String[] arr = StrUtil.split(ary[1], "=");
                        if ("myActionId".equals(arr[0])) {
                            long myActionId = StrUtil.toLong(arr[1], -1);
                            if (myActionId != -1) {
                                MyActionDb mad = new MyActionDb();
                                mad = mad.getMyActionDb(myActionId);
                                com.redmoon.oa.android.Privilege pvg = new com.redmoon.oa.android.Privilege();
                                String skey = pvg.getSkey(userName);
                                boolean isSSL = Global.getInternetFlag().equals(Global.INTERNET_FLAG_SECURE);

                                String serverDomain = Global.server;
                                String serverPort = Global.port;

                                String rootPath;
                                String virtualPath = Global.virtualPath;
                                if (!virtualPath.equals("")) {
                                    if (isSSL) {
                                        rootPath = "https://" + serverDomain + ":" + serverPort + "/" + Global.virtualPath; // "http://www.zjrj.cn";
                                    } else {
                                        if (serverPort.equals("80"))
                                            rootPath = "http://" + serverDomain + "/" + Global.virtualPath; // "http://www.zjrj.cn";
                                        else {
                                            rootPath = "http://" + serverDomain + ":" + serverPort + "/"
                                                    + Global.virtualPath; // "http://www.zjrj.cn";
                                        }
                                    }
                                } else {
                                    if (isSSL) {
                                        rootPath = "https://" + serverDomain + ":" + serverPort;
                                    } else {
                                        if (serverPort.equals("80")) {
                                            rootPath = "http://" + serverDomain;
                                        } else {
                                            rootPath = "http://" + serverDomain + ":" + serverPort; // "http://www.zjrj.cn";
                                        }
                                    }
                                }

                                link = rootPath + "/weixin/flow/flow_dispose.jsp?skey=" + skey + "&flowId=" + mad.getFlowId() + "&myActionId=" + myActionId;
                                content = "<a href='" + link + "'>" + content + "</a>";
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }

        LogUtil.getLog(WXMessageMgr.class).info("agentId = " + agentId + " userName=" + userName + " content=" + content);

        String accessToken = getToken(config.getSecretOfAgent(String.valueOf(agentId)));
        String url = Constant.MESSAGE_SEND + accessToken + "&agentid=" + accessToken;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("touser", userId);
        jsonObj.put("msgtype", TEXT_TYPE);
        jsonObj.put("agentid", agentId);
        JSONObject textObj = new JSONObject();
        textObj.put("content", content);
        jsonObj.put("text", textObj);
        jsonObj.put("safe", 0);
        return baseRequestWxAdd(url, jsonObj.toString(), Constant.REQUEST_METHOD_POST);
    }


}
