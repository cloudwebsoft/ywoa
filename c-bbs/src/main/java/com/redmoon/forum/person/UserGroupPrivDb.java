package com.redmoon.forum.person;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.Config;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.StrUtil;

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
public class UserGroupPrivDb extends QObjectDb {
    public static final String ALLBOARD = "allboard";

    public static final String OTHERS = "-1";

    public UserGroupPrivDb() {
    }

    public boolean init(String groupCode, String boardCode) {
        Config cfg = Config.getInstance();
        // 发表贴子 回复贴子
        String priv = "11";

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
        String search = cfg.getProperty("forum.canUserSearch").equals("true") ? "1" :
                      "0";
        String view_topic = "1";
        String view_userinfo = "1";

        long diskSpaceAllowed = StrUtil.toLong(cfg.getProperty("forum.defaultDiskSpaceAllowed"));

        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {
                groupCode, boardCode, priv,
                        attach_upload,
                        attach_download, add_topic, reply_topic, vote, search, view_topic, view_userinfo, new Long(diskSpaceAllowed)
            });
        }
        catch (ResKeyException e) {
            // LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new IllegalAccessError(e.getMessage());
        }
        return re;
    }

    public UserGroupPrivDb getUserGroupPrivDb(String groupCode, String boardCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("group_code", groupCode);
        pk.setKeyValue("board_code", boardCode);
        UserGroupPrivDb ugp = (UserGroupPrivDb)getQObjectDb(pk.getKeys());
        // LogUtil.getLog(getClass()).info("getUserGroupPrivDb:" + primaryKey);
        if (ugp==null) {
            init(groupCode, boardCode);
            return (UserGroupPrivDb)getQObjectDb(pk.getKeys());
        }
        else
            return ugp;
    }

}
