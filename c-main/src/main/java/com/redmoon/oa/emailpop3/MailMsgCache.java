package com.redmoon.oa.emailpop3;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MailMsgCache extends ObjectCache {
    public MailMsgCache(MailMsgDb messageDb) {
        super(messageDb);
    }

    public void setGroup() {
        group = "OA_MAIL_MSG";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "OA_MAIL_MSG_COUNT_";
    }
}
