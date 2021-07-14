package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.api.IMsgService;
import com.redmoon.oa.message.MessageDb;
import org.springframework.stereotype.Service;

@Service
public class MsgService implements IMsgService {
    @Override
    public boolean sendSysMsg(String receiver, String title, String content, String action) throws ErrMsgException {
        MessageDb md = new MessageDb();
        return md.sendSysMsg(receiver, title, content, action);
    }

    @Override
    public boolean sendSysMsg(String receiver, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException {
        MessageDb md = new MessageDb();
        return md.sendSysMsg(receiver, title, content, actionType, actionSubType, action);
    }

    @Override
    public boolean sendSysMsg(String[] receivers, String title, String content, String actionType, String actionSubType, String action) throws ErrMsgException {
        MessageDb md = new MessageDb();
        return md.sendSysMsg(receivers, title, content, actionType, actionSubType, action);
    }
}
