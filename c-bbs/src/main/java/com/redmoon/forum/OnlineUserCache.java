package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.util.StrUtil;
import java.util.Calendar;

public class OnlineUserCache extends ObjectCache {
    String ALLCOUNTSQL = "select count(*) from sq_online";
    String ALLUSERCOUNTSQL = "select count(*) from sq_online where isguest=0";
    String BOARDCOUNTSQL = "select count(*) from sq_online where boardcode=";
    String BOARDUSERCOUNTSQL =
                "select count(*) from sq_online where isguest=0 and boardcode=";

    public OnlineUserCache(OnlineUserDb ou) {
        super(ou);
    }

    public void setGroup() {
        group = "OL_USER_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "OL_USER_COUNT_";
    }

    public int getAllCount() {
        int count = getObjectCount(ALLCOUNTSQL);
        return count;
    }

    public int getAllUserCount() {
        int count = getObjectCount(ALLUSERCOUNTSQL);
        return count;
    }

    public int getBoardCount(String boardcode) {
        BOARDCOUNTSQL += StrUtil.sqlstr(boardcode);
        int count = getObjectCount(BOARDCOUNTSQL);
        return count;
    }

    /**
     * 取得对应于boardcode版面的注册用户人数
     * @param boardcode String
     * @return int
     */
    public int getBoardUserCount(String boardcode) {
        BOARDUSERCOUNTSQL += StrUtil.sqlstr(boardcode);
        int count = getObjectCount(BOARDUSERCOUNTSQL);
        return count;
    }

    /**
     * 刷新在线人数，刷新该类型的总人数与注册用户人数
     * @param counttype String 人数类型，包括总人数和各版人数
     */
    public void refreshBoardCount(String countsql, String boardcode) {
        try {
            rmCache.remove(countsql + StrUtil.sqlstr(boardcode), COUNT_GROUP_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 刷新所有的在线记录，包括总在线及各个论坛版块的在线
     */
    public void refreshAll() {
        try {
            rmCache.invalidateGroup(COUNT_GROUP_NAME);

            // 记录最高在线人数
            int allcount = getAllCount();
            ForumDb forum = new ForumDb();
            if (allcount > forum.getMaxOnlineCount()) {
                forum.setMaxOnlineCount(allcount);
                Calendar cal = Calendar.getInstance();
                forum.setMaxOnlineDate(cal.getTime());
                forum.save();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
