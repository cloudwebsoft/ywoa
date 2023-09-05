package com.redmoon.oa.message;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCache extends ObjectCache {
    String prefix = "OA_Receiver_";

    public MessageCache(MessageDb messageDb) {
        super(messageDb);
    }

    @Override
    public void setGroup() {
        group = "OA_MESSAGE";
    }

    @Override
    public void setGroupCount() {
        COUNT_GROUP_NAME = "OA_MESSAGE_COUNT_";
    }

    public void refreshNewCountOfReceiver(String receiver) {
        try {
			rmCache.remove(prefix + receiver, group);
		} catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshNewCountOfReceiver: " + e.getMessage());
		}
    }
}
