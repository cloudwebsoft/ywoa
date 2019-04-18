package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.MsgDb;
import com.redmoon.kit.util.FileUpload;

public interface IPluginPrivilege {
    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fu) throws ErrMsgException;
    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException;
    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException;
    public boolean canAddQuickReply(HttpServletRequest request, MsgDb md) throws ErrMsgException;
    public boolean canManage(HttpServletRequest request, long msgId) throws ErrMsgException;
}
