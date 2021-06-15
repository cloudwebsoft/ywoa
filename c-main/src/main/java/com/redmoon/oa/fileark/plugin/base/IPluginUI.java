package com.redmoon.oa.fileark.plugin.base;

import cn.js.fan.base.ISkin;
import com.redmoon.oa.fileark.Document;

public interface IPluginUI {
    IPluginViewAddDocument getViewAddDocument(String dirCode);
    IPluginViewEditDocument getViewEditDocument(Document doc);
    ISkin getSkin();
    String getViewPage();
    IPluginRender getRender();
    
    String getListPage();
}
