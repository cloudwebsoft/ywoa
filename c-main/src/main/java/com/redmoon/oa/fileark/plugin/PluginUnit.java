package com.redmoon.oa.fileark.plugin;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.fileark.plugin.base.IPluginUnit;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;

public class PluginUnit implements Serializable {
    public static final String DEFAULT = "default";

    public final String TYPE_DIR = "dir"; // 应用于分类型
    public final String TYPE_ALLDIR = "allDir";  // 应用于所有类别

    public PluginUnit() {
    }

    public void renew() {
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

    public void setAddPage(String addPage) {
        this.addPage = addPage;
    }

    public void setEditPage(String editPage) {
        this.editPage = editPage;
    }

    public void setViewPage(String viewPage) {
        this.viewPage = viewPage;
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

    public String getAddPage() {
        return addPage;
    }

    public String getEditPage() {
        return editPage;
    }

    public String getViewPage() {
        return viewPage;
    }

    public IPluginUnit getUnit() {
        IPluginUnit ipu = null;
        try {
            ipu = (IPluginUnit) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getUnit:" + e.getMessage());
        }
        return ipu;
    }

    public IPluginUI getUI(HttpServletRequest request) {
        return getUnit().getUI(request);
    }

    public IPluginDocumentAction getDocumentAction() {
        return getUnit().getDocumentAction();
    }
    
	public String getListPage() {
		return listPage;
	}

	public void setListPage(String listPage) {
		this.listPage = listPage;
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
    private String listPage;
    

}
