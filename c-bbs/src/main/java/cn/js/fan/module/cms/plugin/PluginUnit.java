package cn.js.fan.module.cms.plugin;

import java.io.Serializable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResBundle;
import cn.js.fan.web.SkinUtil;
import org.apache.log4j.Logger;
import cn.js.fan.module.cms.plugin.base.IPluginUnit;
import cn.js.fan.module.cms.plugin.base.IPluginUI;
import cn.js.fan.module.cms.plugin.base.IPluginDocumentAction;

public class PluginUnit implements Serializable {
    public static final String DEFAULT = "default";
    public static final String IMAGE = "img";
    public static final String SOFTWARE = "software";

    public final String TYPE_DIR = "dir"; // 应用于版块型
    public final String TYPE_ALLDIR = "allDir";  // 应用于所有版块
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public PluginUnit() {
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
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

    public String getName(HttpServletRequest request) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        return rb.get("name");
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

    public IPluginUnit getUnit() {
        IPluginUnit ipu = null;
        try {
            ipu = (IPluginUnit) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            logger.error("getUnit:" + e.getMessage());
        }
        return ipu;
    }

    public IPluginUI getUI(HttpServletRequest request) {
        return getUnit().getUI(request);
    }

    public IPluginDocumentAction getDocumentAction() {
        return getUnit().getDocumentAction();
    }

    public String getAddPage() {
        return addPage;
    }

    public String getEditPage() {
        return editPage;
    }

    public String getViewPage() {
        return viewPage;
    }

    public void setAddPage(String addPage) {
        this.addPage = addPage;
    }

    public void setEditPage(String editPage) {
        this.editPage = editPage;
    }

    public void setViewPage(String viewPage) {
        this.viewPage = viewPage;
    }

    private String code;
    private String resource;
    private String adminEntrance;
    private String type;
    private String classUnit;
    private Vector skins;
    private String addPage;
    private String editPage;
    private String viewPage;
}
