package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.cache.jcs.ICacheMgr;

import java.util.Vector;

import com.cloudwebsoft.framework.template.TemplateLoader;
import com.redmoon.blog.Config;
import com.redmoon.forum.util.VisitTopicLogDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import cn.js.fan.db.PrimaryKey;

public class ForumCache extends ObjectCache implements ICacheMgr {
    static final String TOPFORUM = "TOP_FORUM";
    static final String AD_TOPIC_BOTTOM = "AD_TOPIC_BOTTOM";
    static final String NOTICE = "FORUM_NOTICE";
    static final String MONTH_MAX_VISITED_MSG = "MONTH_MAX_VISITED_MSG";
    static final String NEW_MSG = "NEW_MSG";
    
    public static Vector monthMaxVisitedMsgs;
    public static int monthMaxVisitedMsgCount = 0;
    
    public ForumCache(ForumDb forumDb) {
        super(forumDb);
    }

    public void refreshTopMsgs() {
        try {
            rmCache.remove(TOPFORUM, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void refreshSave(PrimaryKey primaryKey) {
        super.refreshSave(primaryKey);
        try {
            rmCache.remove(NOTICE, group);
            rmCache.remove(AD_TOPIC_BOTTOM, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Vector getAllNotice() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(NOTICE, group);
        } catch (Exception e) {
            logger.error("getAllNotice:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            String notices = ForumDb.getInstance().getNotices();
            notices = notices.replaceAll("，", ",");
            String[] ary = StrUtil.split(notices, ",");
            v = new Vector();
            if (ary!=null) {
                int len = ary.length;
                if (len > 0) {
                    try {
                        MsgMgr mm = new MsgMgr();
                        for (int i=0; i<len; i++) {
                            if (StrUtil.isNumeric(ary[i])) {
                                MsgDb md = mm.getMsgDb(Long.parseLong(ary[i]));
                                if (md.isLoaded())
                                    v.addElement(md);
                            }
                        }
                        rmCache.putInGroup(NOTICE, group, v);
                    } catch (Exception e) {
                        logger.error("getAllNotice:" + e.getMessage());
                    }
                }
            }
            return v;
        }
    }

    public Vector getAllAdTopicBottom() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(AD_TOPIC_BOTTOM, group);
        } catch (Exception e) {
            logger.error("getAdTopicBottoms:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            String ad = ForumDb.getInstance().getAdTopicBottom();
            ad = ad.replaceAll("，", ",");
            String[] ary = StrUtil.split(ad, ",");
            v = new Vector();
            if (ary!=null) {
                int len = ary.length;
                if (len > 0) {
                    try {
                        MsgMgr mm = new MsgMgr();
                        for (int i=0; i<len; i++) {
                            if (StrUtil.isNumeric(ary[i])) {
                                MsgDb md = mm.getMsgDb(Long.parseLong(ary[i]));
                                if (md.isLoaded())
                                    v.addElement(md);
                            }
                        }
                        rmCache.putInGroup(AD_TOPIC_BOTTOM, group, v);
                    } catch (Exception e) {
                        logger.error("getAdTopicBottoms:" + e.getMessage());
                    }
                }
            }
            return v;
        }
    }

    public long[] getTopMsgs() {
        long[] v = new long[0];
        try {
            v = (long[]) rmCache.getFromGroup(TOPFORUM, group);
        } catch (Exception e) {
            logger.error("getTopMsgs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            MsgDb md = new MsgDb();
            v = md.getTopMsgs();
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(TOPFORUM, group, v);
                } catch (Exception e) {
                    logger.error("getTopMsgs:" + e.getMessage());
                }
            }
            return v;
        }
    }
    
    public Vector getMonthMaxVisitedMsgs(int count) {
    	if (monthMaxVisitedMsgs==null) {
        	monthMaxVisitedMsgCount = count;    		
    		refreshMonthMaxVisitedMsgs();
    	}
    	return monthMaxVisitedMsgs; 
    }    

    /**
     * 取得最新发贴
     * @param count 条数
     * @return
     */
    public Vector getNewMsgs(int count) {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(NEW_MSG, group);
        } catch (Exception e) {
            logger.error("getMonthMaxVisitedMsgs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            MsgDb md = new MsgDb();
            v = md.getNewMsgs(count);
            
            // Add the thread count to cache
            if (v.size() > 0) {
                try {
                    rmCache.putInGroup(NEW_MSG, group, v);
                } catch (Exception e) {
                    logger.error("getNewMsgs:" + e.getMessage());
                }
            }
            return v;
        }
    }    
    
    public void setGroupCount() {
        COUNT_GROUP_NAME = "FORUM_COUNT_";
    }
    
    public void refresh() {
        try {
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            logger.error("refresh:" + StrUtil.trace(e));
        }
    }
    
    public void regist() {
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
    }
    
    public void refreshMonthMaxVisitedMsgs() {
        VisitTopicLogDb vtld = new VisitTopicLogDb();
        monthMaxVisitedMsgs = vtld.getMonthMaxVisited(monthMaxVisitedMsgCount);
    }

    /**
     * 定时刷新缓存
     */
    public void timer() {
        // 刷新全文检索
    	curForumRefreshLife--;
        if (curForumRefreshLife <= 0) {
            refresh();
            curForumRefreshLife = FORUM_REFRESH_INTERVAL;

            // System.out.println(getClass() + " has done on timer");
        }
        
        curVisitRefreshLife--;
        if (curVisitRefreshLife <= 0) {
            refreshMonthMaxVisitedMsgs();
            curVisitRefreshLife = VISIT_REFRESH_INTERVAL;
        }
    }
    
    static boolean isRegisted = false;

    public static long FORUM_REFRESH_INTERVAL = 5; // 五分钟
    private long curForumRefreshLife = FORUM_REFRESH_INTERVAL; // 五分钟    
    
    public static long VISIT_REFRESH_INTERVAL = 60*3; // 三小时
    private long curVisitRefreshLife = VISIT_REFRESH_INTERVAL; // 三小时       
}
