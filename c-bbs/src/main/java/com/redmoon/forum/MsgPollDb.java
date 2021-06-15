package com.redmoon.forum;

import com.cloudwebsoft.framework.base.QObjectDb;
import java.util.Vector;

/**
 * <p>Title:投票贴中的投标标题，是否单选多选等记录 </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MsgPollDb extends QObjectDb {
    public MsgPollDb() {
    }

    public Vector getOptions(long msgId) {
        MsgPollOptionDb mpod = new MsgPollOptionDb();
        return mpod.getOptions(msgId);
    }
}
