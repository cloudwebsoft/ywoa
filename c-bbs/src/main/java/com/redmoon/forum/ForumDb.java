package com.redmoon.forum;

import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

/**
 *
 * <p>Title: 论坛全局信息管理</p>
 *
 * <p>Description: 公告、置项贴、注册发贴过滤、用户数、贴子数、最高在线、今日发贴等</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ForumDb extends ObjectDb {
    public static final int ID = 1;

    public static int STATUS_NORMAL = 1;
    public static int STATUS_STOP = 0;

    private int id = ID;
    private java.util.Date createDate;
    private int todayCount = 0;
    private int userCount = 0;
    private int topicCount = 0;
    private int postCount = 0;
    private int yestodayCount = 0;
    private Date maxDate;
    private int maxCount = 0;

    public String[] filterUserNameAry;
    public String[] filterMsgAry;
    
    private Map filterMsgMap;

    static ForumDb fd = null;

    public ForumDb() {
        id = ID;
        load();
    }

    public ForumDb(int id) {
        this.id = id;
        init();
        load();
        initMapFilter();
    }

    public void initMapFilter() {
        filterUserNameAry = StrUtil.split(filterUserName, "\\|");
        if (filterUserNameAry==null)
            filterUserNameAry = new String[0];
        int len = filterUserNameAry.length;
        for (int i = 0; i < len; i++) {
            // 规则中有*字符
            filterUserNameAry[i] = filterUserNameAry[i].replaceAll("\\*", ".*?");
        }
        filterMsgAry = StrUtil.split(filterMsg, "\\|");
        if (filterMsgAry==null)
            filterMsgAry = new String[0];
        
        filterMsgMap = new HashMap();
        
        len = filterMsgAry.length;
        for (int i = 0; i < len; i++) {
        	// 规则中有*字符
            filterMsgAry[i] = filterMsgAry[i].replaceAll("\\*", ".*?");
        	// 检查其中是否含有=号
        	String rule = filterMsgAry[i];
        	int p = rule.indexOf("=");
        	if (p!=-1) {
        		String sub = rule.substring(0, p);
        		filterMsgAry[i] = sub;
        		if (p==rule.length()-1)
        			filterMsgMap.put(sub, "");
        		else
        			filterMsgMap.put(sub, rule.substring(p+1));
        	}
        }
     }
    
    public Map getFilterMsgMap() {
    	return filterMsgMap;
    }

    /**
     * 取得论坛的置顶贴
     * @return long[]
     */
    public long[] getTopMsgs() {
        ForumCache fc = new ForumCache(this);
        return fc.getTopMsgs();
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        ForumCache fc = new ForumCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (ForumDb)fc.getObjectDb(pk);
    }

    public boolean del() {
        return true;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ForumDb(pk.getIntValue());
    }

    public void setQueryCreate() {
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update sq_forum set createDate=?, todayCount=?, userCount=?, topicCount=?, postCount=?, yestodayCount=?, maxDate=?, maxCount=?, todayDate=?, userNew=?, maxOnlineCount=?, maxOnlineDate=?, notices=?, filterUserName=?, filterMsg=?, isShowLink=?, status=?, reason=?, GUEST_ACTION=?, ad_topic_bottom=?, stars=? where id=?";
    }

    public void setQueryDel() {

    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select createDate, todayCount, userCount, topicCount, postCount, yestodayCount, maxDate, maxCount, todayDate, userNew, maxOnlineCount, maxOnlineDate, notices, filterUserName, filterMsg, isShowLink, status, reason, GUEST_ACTION, ad_topic_bottom, stars from sq_forum where id=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, DateUtil.toLongString(createDate));
            ps.setInt(2, todayCount);
            ps.setInt(3, userCount);
            ps.setInt(4, topicCount);
            ps.setInt(5, postCount);
            ps.setInt(6, yestodayCount);
            ps.setString(7, DateUtil.toLongString(maxDate));
            ps.setInt(8, maxCount);
            ps.setString(9, DateUtil.toLongString(todayDate));
            ps.setString(10, userNew);
            ps.setInt(11, maxOnlineCount);
            ps.setString(12, DateUtil.toLongString(maxOnlineDate));
            ps.setString(13, notices);
            ps.setString(14, filterUserName);
            ps.setString(15, filterMsg);
            ps.setInt(16, showLink?1:0);
            ps.setInt(17, status);
            ps.setString(18, reason);
            ps.setString(19, guestAction);
            ps.setString(20, adTopicBottom);
            ps.setString(21, stars);
            ps.setInt(22, id);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            fd = null;
            ForumCache uc = new ForumCache(this);
            primaryKey.setValue(new Integer(this.id));
            uc.refreshSave(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, id);
            primaryKey.setValue(new Integer(id));
            rs = conn.executePreQuery();
            if (rs.next()) {
                createDate = DateUtil.parse(rs.getString(1));

                todayCount = rs.getInt(2);
                userCount = rs.getInt(3);
                topicCount = rs.getInt(4);
                postCount = rs.getInt(5);
                yestodayCount = rs.getInt(6);
                maxDate = DateUtil.parse(rs.getString(7));
                maxCount = rs.getInt(8);
                todayDate = DateUtil.parse(rs.getString(9));

                userNew = StrUtil.getNullString(rs.getString(10));
                maxOnlineCount = rs.getInt(11);
                maxOnlineDate = DateUtil.parse(rs.getString(12));

                notices = StrUtil.getNullString(rs.getString(13));
                filterUserName = StrUtil.getNullString(rs.getString(14));
                filterMsg = StrUtil.getNullString(rs.getString(15));
                showLink = rs.getInt(16)==1?true:false;
                status = rs.getInt(17);
                reason = StrUtil.getNullStr(rs.getString(18)).trim();
                guestAction = StrUtil.getNullStr(rs.getString(19));
                if (guestAction.length()>0)
                    guestSeeTopic = guestAction.substring(0, 1).equals("1");
                if (guestAction.length()>1)
                    guestSeeAttachment = guestAction.substring(1, 2).equals("1");
                if (guestAction.length()>2)
                    guestEnterIntoForum = guestAction.substring(2, 3).equals("1");
                adTopicBottom = StrUtil.getNullString(rs.getString(20));
                stars = StrUtil.getNullStr(rs.getString(21));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public static ForumDb getInstance() {
        if (fd==null) {
            fd = new ForumDb();
            fd = fd.getForumDb();
        }
        return fd;
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setTodayCount(int todayCount) {
        this.todayCount = todayCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setYestodayCount(int yestodayCount) {
        this.yestodayCount = yestodayCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public void setTodayDate(Date todayDate) {
        this.todayDate = todayDate;
    }

    public void setUserNew(String userNew) {
        this.userNew = userNew;
    }

    public void setMaxOnlineCount(int maxOnlineCount) {
        this.maxOnlineCount = maxOnlineCount;
    }

    public void setMaxOnlineDate(Date maxOnlineDate) {
        this.maxOnlineDate = maxOnlineDate;
    }

    public void setNotices(String notices) {
        this.notices = notices;
    }

    public void setFilterUserName(String filterUserName) {
        this.filterUserName = filterUserName;
    }

    public void setFilterMsg(String filterMsg) {
        this.filterMsg = filterMsg;
    }

    public void setShowLink(boolean showLink) {
        this.showLink = showLink;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setGuestAction(String guestAction) {
        this.guestAction = guestAction;
    }

    public void setAdTopicBottom(String adTopicBottom) {
        this.adTopicBottom = adTopicBottom;
    }

    public int getId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getYestodayCount() {
        return yestodayCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public Date getTodayDate() {
        return todayDate;
    }

    public String getUserNew() {
        return userNew;
    }

    public int getMaxOnlineCount() {
        return maxOnlineCount;
    }

    public Date getMaxOnlineDate() {
        return maxOnlineDate;
    }

    public String getNotices() {
        return notices;
    }

    public String getFilterUserName() {
        return filterUserName;
    }

    public String getFilterMsg() {
        return filterMsg;
    }

    public boolean isShowLink() {
        return showLink;
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getGuestAction() {
        return guestAction;
    }

    public String getAdTopicBottom() {
        return adTopicBottom;
    }
    
    public String getStars() {
    	return stars;
    }
    
    public void setStars(String stars) {
    	this.stars = stars;
    }

    /**
     * 更新今日、昨日发贴数及贴子总数的统计信息
     * @param isAddNew boolean 是否为发新贴而不是回贴
     */
    public void setStatics(boolean isAddNew) {
        // 从数据库中取出今天日期
        Calendar todaydb = Calendar.getInstance();
        if (todayDate==null)
            todayDate = new java.util.Date();
        todaydb.setTime(todayDate);
        Calendar today = Calendar.getInstance();
        // 如果today_date字段中为当前日期，则today_count加1
        if (DateUtil.isSameDay(todaydb, today)) {
            setTodayCount(todayCount + 1);
        } else { // 如果字段日期与本日不一致，则说明是本日第一贴
            setYestodayCount(todayCount);
            if (maxCount < todayCount) {
                maxCount = todayCount;
                maxDate = todayDate;
            }
            todayCount = 1;
            //
            todayDate = today.getTime();
        }
        if (isAddNew)
            setTopicCount(topicCount + 1);
        setPostCount(postCount + 1);
        save();
    }

    public ForumDb getForumDb() {
        return (ForumDb)getObjectDb(new Integer(ID));
    }

    public Vector getAllAdTopicBottom() {
        ForumCache fc = new ForumCache(this);
        return fc.getAllAdTopicBottom();
    }

    public Vector getAllNotice() {
        ForumCache fc = new ForumCache(this);
        return fc.getAllNotice();
    }

    // 重新载入
    public void reload() {
        if (fd!=null)
            fd = null;
        ForumCache uc = new ForumCache(this);
        primaryKey.setValue(new Integer(this.id));
        uc.refreshSave(primaryKey);
    }

    public boolean canGuestSeeTopic() {
        return guestSeeTopic;
    }

    public boolean canGuestSeeAttachment() {
        return guestSeeAttachment;
    }

    public boolean canGuestEnterIntoForum() {
        return guestEnterIntoForum;
    }
    
    /**
     * 取得月访问量最大的贴子
     * @param count 条数
     * @return
     */
    public Vector getMonthMaxVisitedMsgs(int count) {
        ForumCache fc = new ForumCache(this);
        return fc.getMonthMaxVisitedMsgs(count);
    }
    
    /**
     * 取得最新发贴
     * @param count 条数
     * @return
     */
    public Vector getNewMsgs(int count) {
        ForumCache fc = new ForumCache(this);
        return fc.getNewMsgs(count);
    }    

    private boolean guestSeeTopic = true;
    private boolean guestSeeAttachment = true;
    private boolean guestEnterIntoForum = true;

    private java.util.Date todayDate;
    private String userNew;
    private int maxOnlineCount = 0;
    private Date maxOnlineDate;
    private String notices;
    private String filterUserName;
    private String filterMsg;
    private boolean showLink = true;
    private int status;
    private String reason;
    private String guestAction;
    private String adTopicBottom;
    private String stars;

}
