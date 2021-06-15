package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;

public interface IMsgService {

    boolean sendSysMsg(String receiver, String title, String content, String action) throws ErrMsgException;

    boolean sendSysMsg(String receiver, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException;

    boolean sendSysMsg(String[] receivers, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException;
}
