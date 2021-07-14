package com.redmoon.forum.person;

import com.cloudwebsoft.framework.base.*;
import com.redmoon.forum.Config;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.DateUtil;
import java.util.Date;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
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
public class UserPrivDb extends QObjectDb {
    public static String querySave;

    public UserPrivDb() {
    }

    public boolean init(String userName) {
        Config cfg = Config.getInstance();
        // 发表贴子 回复贴子
        String priv = "11";
        String strAttachDayCount = cfg.getProperty("forum.maxAttachDayCount");
        int maxAttachDayCount = Integer.parseInt(strAttachDayCount);
        String strMaxAttachmentSize = cfg.getProperty("forum.maxAttachmentSize");
        int maxAttachmentSize = Integer.parseInt(strMaxAttachmentSize);

        String attach_upload = cfg.getProperty("forum.canUserUploadAttach").
                               equals("true") ? "1" : "0";
        String attach_download = cfg.getProperty("forum.canUserDownloadAttach").
                                 equals("true") ? "1" : "0";
        String add_topic = cfg.getProperty("forum.canUserAddTopic").equals(
                "true") ? "1" : "0";
        String reply_topic = cfg.getProperty("forum.canUserReplyTopic").equals(
                "true") ? "1" : "0";
        String vote = cfg.getProperty("forum.canUserVote").equals("true") ? "1" :
                      "0";
        String search = cfg.getProperty("forum.canUserSearch").equals("true")?"1":"0";

        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {
                userName, priv, new Integer(maxAttachDayCount),
                        new Integer(maxAttachmentSize), attach_upload,
                        attach_download, add_topic, reply_topic, vote, search
            });
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    public UserPrivDb getUserPrivDb(String userName) {
        UserPrivDb up = (UserPrivDb)getQObjectDb(userName);
        if (up==null) {
            init(userName);
            return (UserPrivDb)getQObjectDb(userName);
        }
        else
            return up;
    }

    public int getAttachTodayUploadCount() {
        String sToday = resultRecord.getString("attach_today");
        if (sToday!=null) {
            Date d = DateUtil.parse(sToday);
            if (DateUtil.isSameDay(d, new java.util.Date()))
                return resultRecord.getInt("attach_today_upload_count");
        }
        // 如果记录的日期与今日不是同一天
        resultRecord.set("attach_today", "" + new java.util.Date().getTime());
        resultRecord.set("attach_today_upload_count", new Integer(0));
        try {
            save();
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("getAttachTodayUploadCount:" + e.getMessage());
        }
        return 0;
    }

    public boolean addAttachTodayUploadCount(int count) {
        resultRecord.set("attach_today_upload_count", new Integer(resultRecord.getInt("attach_today_upload_count") + count));
        boolean re = false;
        try {
            re = save();
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("addAttachTodayUploadCount:" + e.getMessage());
        }
        return re;
    }

}
