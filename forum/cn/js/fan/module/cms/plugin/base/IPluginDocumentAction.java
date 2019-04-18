package cn.js.fan.module.cms.plugin.base;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import cn.js.fan.module.cms.CMSMultiFileUploadBean;
import cn.js.fan.module.cms.Document;
import javax.servlet.http.HttpServletRequest;

public interface IPluginDocumentAction {
    boolean create(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException;

    boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException;
    /**
     * 用于文章型节点中，当初始化目录节点对应的文章时创建空的插件文章
     * @param doc Document
     * @return boolean
     */
    public boolean create(Document doc);
}
