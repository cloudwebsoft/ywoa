package cn.js.fan.module.cms.plugin.base;

import javax.servlet.http.HttpServletRequest;

public interface IPluginUnit {
    IPluginUI getUI(HttpServletRequest request);
    IPluginDocumentAction getDocumentAction();
    boolean isPluginDir(String dirCode);
    IPluginDocument getDocument(int docId);
}
