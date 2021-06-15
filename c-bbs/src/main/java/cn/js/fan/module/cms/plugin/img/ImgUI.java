package cn.js.fan.module.cms.plugin.img;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.base.ISkin;
import cn.js.fan.module.cms.plugin.base.IPluginUI;
import cn.js.fan.module.cms.plugin.base.IPluginViewAddDocument;
import cn.js.fan.module.cms.plugin.base.IPluginRender;
import cn.js.fan.module.cms.plugin.base.IPluginViewEditDocument;
import cn.js.fan.module.cms.Document;

public class ImgUI implements IPluginUI {
    HttpServletRequest request;

    public ImgUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewAddDocument getViewAddDocument(String dirCode) {
        return new ImgViewAddDocument(request, dirCode);
    }

    public IPluginViewEditDocument getViewEditDocument(Document doc) {
        return new ImgViewEditDocument(request, doc);
    }

    public ISkin getSkin() {
        return new ImgSkin();
    }

    public String getViewPage() {
        return "";
    }

    public IPluginRender getRender() {
        return new ImgRender();
    }

}
