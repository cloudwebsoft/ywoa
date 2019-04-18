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
  public int getNewMsgCount(String receiver);
  public void clearMsgOfUser(String userName);
  public boolean AddMsg(ServletContext application, HttpServletRequest request, String sender) throws ErrMsgException;
  public boolean delMsg(String[] ids) throws ErrMsgException;
  public boolean isReaded();
  public IMessage getMessageDb(int id);
  public String getTitle();
  public String getContent();
  public String getRq();
  public String getSender();
  public String getReceiver();
  public String getIp();
  public int getType();
  public int getId();
  public Vector getAttachments();
  public int getBox();
  public boolean create(String toUser, FileUpload fu) throws ErrMsgException;
  public boolean sendSysMsg(String receiver, String title, String content) throws ErrMsgException;
  public boolean sendSysMsg(String receiver, String title, String content, String action) throws ErrMsgException;
  public boolean sendSysMsg(String receiver, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException;
  public ObjectDb getObjectDb(Object primaryKeyValue);
  public boolean del();
  public int getObjectCount(String sql);
  public int getMessageCount(String sql);
  public Object[] getObjectBlock(String query, int startIndex);
  public ObjectDb getObjectRaw(PrimaryKey pk);
  public void setQueryCreate();
  public void setQuerySave();
  public void setQueryDel();
  public void setQueryLoad();
  public void setQueryList();
  public boolean save();
  public void setPrimaryKey();
  public void load();
  public void setReaded(boolean readed);
  public void setAttachments(Vector attachments);
  public void setBox(int box);
  public Vector getNewMsgsOfUser(String userName);
  public Attachment getAttachment(int attId);

  public FileUpload getFileUpload();
  
  public void setAction(String action);
  
  public boolean TransmitMsg(ServletContext application,
          HttpServletRequest request,
          String sender, int msgId) throws ErrMsgException;
  
  public String getSenderPortrait();
  
  public String getSummary();
}
