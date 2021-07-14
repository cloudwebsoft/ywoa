package com.redmoon.oa.fileark.plugin.base;

import javax.servlet.http.HttpServletRequest;

public interface IPluginUnit {
    IPluginUI getUI(HttpServletRequest request);
    IPluginDocumentAction getDocumentAction();
    boolean isPluginDir(String dirCode);
}
