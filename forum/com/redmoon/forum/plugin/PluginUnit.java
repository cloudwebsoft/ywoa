package com.redmoon.forum.plugin;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.plugin.base.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 插件单元</p>
 *
 * <p>Description: </p>
 * board型的插件应用于版块中，pluginCode不会被记录到sq_message的pluginCode字段中
 * topic型的插件应用在贴子上，将会被记录于sq_message的pluginCode字段中，并且该字段中只会记录一个插件
 * forum型的插件应用于整个论坛的所有贴子
 * 每一个插件单元对应于plugin.xml中<plugin>...</plugin>
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PluginUnit implements Serializable {
    public static final String TYPE_BOARD = "board"; // 应用于版块型
    public static final String TYPE_FORUM = "forum";  // 应用于所有版块
    public static final String TYPE_TOPIC = "topic"; // 应用于贴子

    public PluginUnit() {
    }

    public PluginUnit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setAdminEntrance(String adminEntrance) {
        this.adminEntrance = adminEntrance;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setClassUnit(String classUnit) {
        this.classUnit = classUnit;
    }

    public void setSkins(Vector skins) {
        this.skins = skins;
    }

    public void setAddTopicPage(String addTopicPage) {
        this.addTopicPage = addTopicPage;
    }

    public void setEditTopicPage(String editTopicPage) {
        this.editTopicPage = editTopicPage;
    }

    public void setAddReplyPage(String addReplyPage) {
        this.addReplyPage = addReplyPage;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public void setShowTopicPage(String showTopicPage) {
        this.showTopicPage = showTopicPage;
    }

    public void setRenderCode(String renderCode) {
        this.renderCode = renderCode;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public void setUserCenterPage(String userCenterPage) {
        this.userCenterPage = userCenterPage;
    }

    public void setUserInfoPage(String userInfoPage) {
        this.userInfoPage = userInfoPage;
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, resource, "name");
    }

    public String LoadString(HttpServletRequest request, String key) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        if (rb == null)
            return "";
        else
            return rb.get(key);
    }

    public String getDesc(HttpServletRequest request) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        return rb.get("desc");
    }

    public String getCode() {
        return code;
    }

    public String getResource() {
        return resource;
    }

    public String getAdminEntrance() {
        return adminEntrance;
    }

    public String getType() {
        return type;
    }

    public String getClassUnit() {
        return classUnit;
    }

    public Vector getSkins() {
        return skins;
    }

    public String getAddTopicPage() {
        return addTopicPage;
    }

    public String getEditTopicPage() {
        return editTopicPage;
    }

    public String getAddReplyPage() {
        return addReplyPage;
    }

    public String getButton() {
        return button;
    }

    public String getShowTopicPage() {
        return showTopicPage;
    }

    public String getClassRender() {
        return renderCode;
    }

    /**
     * 取得插件unit类的实例，如InfoUnit
     * @return IPluginUnit
     */
    public IPluginUnit getUnit() {
        IPluginUnit ipu = null;
        try {
            ipu = (IPluginUnit) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return ipu;
    }

    public String getRenderCode() {
        return renderCode;
    }

    public boolean isShowName() {
        return showName;
    }

    public String getUserCenterPage() {
        return userCenterPage;
    }

    public String getUserInfoPage() {
        return userInfoPage;
    }

    public IPluginUI getUI(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return getUnit().getUI(request, response, out);
    }

    public IPluginPrivilege getPrivilege() {
        return getUnit().getPrivilege();
    }

    public IPluginMsgAction getMsgAction() {
        return getUnit().getMsgAction();
    }

    private String code;
    private String resource;
    private String adminEntrance;
    private String type;
    private String classUnit;
    private Vector skins;
    private String addTopicPage;
    private String editTopicPage;
    private String addReplyPage;
    private String button;
    private String showTopicPage;
    private String renderCode;
    private boolean showName = true;
    private String userCenterPage;
    private String userInfoPage;

}
