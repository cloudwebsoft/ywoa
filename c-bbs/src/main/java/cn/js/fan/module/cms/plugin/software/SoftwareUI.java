package cn.js.fan.module.cms.plugin.software;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.base.ISkin;
import cn.js.fan.module.cms.plugin.base.IPluginUI;
import cn.js.fan.module.cms.plugin.base.IPluginViewAddDocument;
import cn.js.fan.module.cms.plugin.base.IPluginRender;
import cn.js.fan.module.cms.plugin.base.IPluginViewEditDocument;
import cn.js.fan.module.cms.Document;

public class SoftwareUI implements IPluginUI {
    HttpServletRequest request;

    public SoftwareUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewAddDocument getViewAddDocument(String dirCode) {
        return new SoftwareViewAddDocument(request, dirCode);
    }

    public IPluginViewEditDocument getViewEditDocument(Document doc) {
        return new SoftwareViewEditDocument(request, doc);
    }

    public ISkin getSkin() {
        return new SoftwareSkin();
    }

    public String getViewPage() {
        return "";
    }

    public IPluginRender getRender() {
        return new SoftwareRender();
    }

}
