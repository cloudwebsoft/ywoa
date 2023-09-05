package com.cloudweb.oa.utils;

import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;

@Component
public class SysUtil {

    @Autowired
    SysProperties sysProperties;

    public JSONObject getServerInfo() {
        JSONObject json = new JSONObject();
        String rootPath = getRootPath();
        json.put("url", rootPath);
        json.put("id", sysProperties.getId());
        json.put("showId", sysProperties.isShowId());
        json.put("rootPath", getRootPath());
        return json;
    }

    /**
     * 访问后端的路径，用于ueditor及预览文件
     * @return
     */
    public String getRootPath() {
        String rootPath = sysProperties.getRootPath();
        if (StrUtil.isEmpty(rootPath)) {
            // 如果配置文件中为空，则取完整的后端访问路径
            return Global.getFullRootPath(SpringUtil.getRequest());
        }
        else {
            return rootPath;
        }
    }

    /**
     * 取得上传文件的路径
     * @return
     */
    public String getUploadPath() {
        String uploadPath = sysProperties.getUploadPath();
        if (uploadPath.endsWith("\\")) {
            uploadPath = uploadPath.substring(0, uploadPath.length() - 1 ) + "/";
        }
        else {
            // 在末尾自动加上 /
            if (!uploadPath.endsWith("/")) {
                uploadPath += "/";
            }
        }
        return uploadPath;
    }

    /**
     * 取得nginx中前端的虚拟路径，如：http://***:8080/oaf，则虚拟路径应置为oaf，如果为空则返回空，如果有值则返回/publiPath
     * @return
     */
    public String getPublicPath() {
        String publicPath = sysProperties.getPublicPath();
        // 去掉末尾可能存在的/
        if (!StrUtil.isEmpty(publicPath)) {
            // 如果以/开头
            if (publicPath.startsWith("/")) {
                // 如果publicPath等于/
               if (!"/".equals(publicPath)) {
                   return publicPath;
               }
               else {
                   publicPath = "";
               }
            }
            else {
                publicPath = "/" + publicPath;
            }
            if (publicPath.endsWith("/")) {
                publicPath = publicPath.substring(0, publicPath.length() - 1);
            }
        }
        return publicPath;
    }

    public String getFrontPath() {
        String path = sysProperties.getFrontPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }
}
