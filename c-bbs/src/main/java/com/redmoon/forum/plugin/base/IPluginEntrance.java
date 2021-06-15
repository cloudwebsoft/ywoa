package com.redmoon.forum.plugin.base;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;

public interface IPluginEntrance {
    public boolean canEnter(HttpServletRequest request, String boardCode) throws ErrMsgException;
    public boolean isPluginBoard(String boardCode);
    public boolean canAddReply(HttpServletRequest request, String boardCode, long rootid) throws ErrMsgException;
    public boolean canAddNew(HttpServletRequest request, String boardCode) throws ErrMsgException;
    public boolean canVote(HttpServletRequest request, String boardCode) throws ErrMsgException;

    // public boolean canUploadAttach() throws ErrMsgException;
    // public boolean canDownloadAttach() throws ErrMsgException;
    // public boolean canVote() throws ErrMsgException;
    // public int getUploadAttachDayCount();
    // public int getUploadAttachMaxSize();
}
