package com.redmoon.oa.fileark.plugin.base;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.fileark.CMSMultiFileUploadBean;
import com.redmoon.oa.fileark.Document;

public interface IPluginDocumentAction {
    boolean create(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException;

    boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException;
    
    public boolean del(HttpServletRequest request, Document doc, boolean isToDustbin) throws ErrMsgException;
    
}
