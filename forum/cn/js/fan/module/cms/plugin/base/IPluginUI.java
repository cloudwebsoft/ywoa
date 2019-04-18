package cn.js.fan.module.cms.plugin.base;

import cn.js.fan.base.ISkin;
import cn.js.fan.module.cms.Document;

public interface IPluginUI {
    IPluginViewAddDocument getViewAddDocument(String dirCode);
    IPluginViewEditDocument getViewEditDocument(Document doc);
    ISkin getSkin();
    String getViewPage();
    IPluginRender getRender();
}
