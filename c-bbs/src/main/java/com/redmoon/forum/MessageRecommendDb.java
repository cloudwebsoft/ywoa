package com.redmoon.forum;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 贴子举报管理</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MessageRecommendDb extends QObjectDb{
    public MessageRecommendDb() {
    }

    public MessageRecommendDb getMessageRecommendDb(long id) {
        return (MessageRecommendDb) getQObjectDb(new Long(id));
    }

    public boolean pass() {
        resultRecord.set("check_state", new Integer(1));
        try {
            return save();
        } catch (ResKeyException ex) {
            LogUtil.getLog(getClass()).error("pass:" + ex.getMessage());
            return false;
        }
    }


    public boolean setOrders(int orders) {
        resultRecord.set("orders", new Integer(orders));
        try {
            return save();
        } catch (ResKeyException ex) {
            LogUtil.getLog(getClass()).error("setOrders:" + ex.getMessage());
            return false;
        }
    }

}
