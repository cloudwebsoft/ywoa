package com.redmoon.oa.message;

// Imports

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.person.UserDb;

import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public interface IMessage {
    int getNewMsgCount(String receiver);

    void clearMsgOfUser(String userName);

    boolean AddMsg(ServletContext application, HttpServletRequest request, String sender) throws ErrMsgException;

    boolean delMsg(String[] ids) throws ErrMsgException;

    boolean isReaded();

    IMessage getMessageDb(int id);

    String getTitle();

    String getContent();

    String getRq();

    String getSender();

    String getReceiver();

    String getIp();

    int getType();

    int getId();

    Vector<Attachment> getAttachments();

    int getBox();

    boolean create(String toUser, FileUpload fu) throws ErrMsgException;

    boolean sendSysMsg(String receiver, String title, String content) throws ErrMsgException;

    boolean sendSysMsg(String receiver, String title, String content, String action) throws ErrMsgException;

    boolean sendSysMsg(String receiver, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException;

    boolean sendSysMsg(String[] receivers, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException;

    ObjectDb getObjectDb(Object primaryKeyValue);

    boolean del();

    int getObjectCount(String sql);

    int getMessageCount(String sql);

    Object[] getObjectBlock(String query, int startIndex);

    ObjectDb getObjectRaw(PrimaryKey pk);

    void setQueryCreate();

    void setQuerySave();

    void setQueryDel();

    void setQueryLoad();

    void setQueryList();

    boolean save();

    void setPrimaryKey();

    void load();

    void setReaded(boolean readed);

    void setAttachments(Vector<Attachment> attachments);

    void setBox(int box);

    Vector getNewMsgsOfUser(String userName);

    Attachment getAttachment(int attId);

    FileUpload getFileUpload();

    void setAction(String action);

    boolean TransmitMsg(ServletContext application,
                        HttpServletRequest request,
                        String sender, int msgId) throws ErrMsgException;

    String getSenderPortrait();

    String getSummary();
}
