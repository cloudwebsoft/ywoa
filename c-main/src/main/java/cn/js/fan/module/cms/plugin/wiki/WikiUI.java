package cn.js.fan.module.cms.plugin.wiki;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.ISkin;

import com.redmoon.oa.fileark.plugin.*;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.fileark.plugin.base.IPluginViewAddDocument;
import com.redmoon.oa.fileark.plugin.base.IPluginRender;
import com.redmoon.oa.fileark.plugin.base.IPluginViewEditDocument;
import com.redmoon.oa.fileark.Document;

public class WikiUI implements IPluginUI {
    HttpServletRequest request;

    public WikiUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewAddDocument getViewAddDocument(String dirCode) {
        return new WikiViewAddDocument(request, dirCode);
    }

    public IPluginViewEditDocument getViewEditDocument(Document doc) {
        return new WikiViewEditDocument(request, doc);
    }

    public ISkin getSkin() {
        return new WikiSkin();
    }

    public String getViewPage() {
    	PluginMgr pm = new PluginMgr();
    	PluginUnit pu = pm.getPluginUnit(WikiUnit.code);
        return pu.getViewPage();
    }

    public String getListPage() {
    	PluginMgr pm = new PluginMgr();
    	PluginUnit pu = pm.getPluginUnit(WikiUnit.code);
        return pu.getListPage();
    }
    
    public IPluginRender getRender() {
        return new WikiRender();
    }

}
