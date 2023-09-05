package com.redmoon.oa.person;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.alibaba.fastjson.annotation.JSONField;
import com.cloudwebsoft.framework.util.LogUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.redmoon.oa.message.*;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.DesktopUnit;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Title: </p>
 *
 * <p>Description: 开始时间myDate与结束时间endDate均不能为null，如果endDate未填写，则需置为myDate</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PlanDb extends ObjectDb implements IDesktopUnit {
	/**
	 * 销售回访
	 */
	public static final int ACTION_TYPE_SALES_VISIT = 1;
	
	/**
	 * 流程
	 */
	public static final int ACTION_TYPE_FLOW = 2;
	
	public static final int ACTION_TYPE_NORMAL = 0;
	
	public static final int ACTION_TYPE_PAPER_DISTRIBUTE = 3;
	
    private int id;
    
    public static final int DEFAULT_X = 30;
    public static final int DEFAULT_y = 30;
    
    private int x = DEFAULT_X;
    private int y = DEFAULT_y;

    public int getBefore() {
        return before;
    }

    private int before = 10;

    public PlanDb() {
        init();
    }

    public PlanDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    @Override
    public void initDB() {
        tableName = "user_plan";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new PlanCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into user_plan (title,content,myDate,userName,zdrq,isRemind,remindDate,remindCount,IS_REMIND_BY_SMS,endDate,action_type,action_data,is_notepaper,maker,x,y,is_shared) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set title=?,content=?,myDate=?,isRemind=?,remindDate=?,remindCount=?,IS_REMIND_BY_SMS=?,endDate=?,action_type=?,action_data=?,is_notepaper=?,is_closed=?,x=?,y=?,is_shared=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by mydate desc";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select title,content,myDate,userName,zdrq,isRemind,remindDate,remindCount,IS_REMIND_BY_SMS,endDate,action_type,action_data,is_notepaper,is_closed,maker,x,y,is_shared from " + tableName + " where id=?";
    }

    public PlanDb getPlanDb(int id) {
        return (PlanDb)getObjectDb(id);
    }

    public void makeRemindMsg() {
        PlanDb pd = null;
        String sql = "select id from " + getTableName() + " where isRemind=1 and remindDate<? and myDate>? and remindCount=0";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            ps.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
            rs = conn.executePreQuery();
            while (rs.next()) {
                pd = getPlanDb(rs.getInt(1));
                try {
                    pd.sendRemindMsg();
                }
                catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error("makeRemindMsg1:" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("makeRemindMsg2:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getPlanDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    
    public Vector listNotepaper(String userName) {
    	String sql = "select id from user_plan where userName="
    		+ StrUtil.sqlstr(userName) + " and is_notepaper=1 and is_closed=0";
    	sql += " order by mydate desc, enddate desc";
    	return list(sql);
    }

    public boolean sendRemindMsg() throws ErrMsgException {
        MessageDb md = new MessageDb();
        remindCount++;
        save();
        if (remindBySMS) {
            IMessage imsg = null;
            ProxyFactory proxyFactory = new ProxyFactory(
                    "com.redmoon.oa.message.MessageDb");
            Advisor adv = new Advisor();
            MobileAfterAdvice mba = new MobileAfterAdvice();
            adv.setAdvice(mba);
            adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
            proxyFactory.addAdvisor(adv);
            imsg = (IMessage) proxyFactory.getProxy();
            imsg.setAction("action=plan|id=" + id);
            return imsg.sendSysMsg(userName, "日程提醒：" + title, content);
        }
        else {
            md.setAction("action=plan|id=" + id);
        	return md.sendSysMsg(userName, "日程提醒：" + title, content);
        }
    }

    @Override
    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setTimestamp(3, new Timestamp(myDate.getTime()));
            ps.setString(4, userName);
            ps.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            ps.setInt(6, remind?1:0);
            if (remindDate==null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(7, new Timestamp(remindDate.getTime()));
            }
            ps.setInt(8, remindCount);
            ps.setInt(9, remindBySMS?1:0);
            if(this.getEndDate()!=null){
              ps.setTimestamp(10, new Timestamp(endDate.getTime()));
            }else{
              ps.setTimestamp(10, new Timestamp(myDate.getTime()));
            }
            
            ps.setInt(11, actionType);
            ps.setString(12, actionData);
            ps.setInt(13, notepaper?1:0);
            ps.setString(14, maker);
            ps.setInt(15, x);
            ps.setInt(16, y);
            ps.setInt(17, shared?1:0);
        	//ps.setInt(11, remindType);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
            	String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName) + " and mydate=" + SQLFilter.getDateStr(DateUtil.format(myDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " order by id desc";
            	ResultSet rs = conn.executeQuery(sql);
            	if (rs.next()) {
            		id = rs.getInt(1);
            	}
                PlanCache rc = new PlanCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }
    
    /**
     * 根据用户名、动作删除记录
     * @param userName 用户名
     * @param actionType 动作类型
     * @param actionData 动作ID
     * @return
     * @throws ErrMsgException 
     */
    public boolean del(String userName, int actionType, String actionData) throws ErrMsgException {
    	String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName) + " and action_type=" + actionType + " and action_data=" + StrUtil.sqlstr(actionData);
    	Iterator ir = list(sql).iterator();
    	if (ir.hasNext()) {
    		PlanDb pd = (PlanDb)ir.next();
    		return pd.del();
    	}
    	return false;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PlanCache rc = new PlanCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new PlanDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    @Override
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // QUERY_LOAD = "select title,content,mydate,userName,zdrq from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = rs.getString(2);
                myDate = rs.getTimestamp(3);
                userName = rs.getString(4);
                zdrq = rs.getDate(5);
                remind = rs.getInt(6) == 1;
                remindDate = rs.getTimestamp(7);
                remindCount = rs.getInt(8);
                remindBySMS = rs.getInt(9)==1;
                endDate = rs.getTimestamp(10);
                //this.remindType = rs.getInt(11);
                actionType = rs.getInt(11);
                actionData = StrUtil.getNullStr(rs.getString(12));
                notepaper = rs.getInt(13)==1;
                closed = rs.getInt(14)==1;
                maker = StrUtil.getNullStr(rs.getString(15));
                x = rs.getInt(16);
                y = rs.getInt(17);
                shared = rs.getInt(18)==1;
                loaded = true;
                primaryKey.setValue(id);

                // 计算提前量以返回给前端
                before = DateUtil.datediffMinute(myDate, remindDate);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            conn.close();
            conn = null;
        }
    }

    @Override
    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    @Override
    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        Privilege privilege = new Privilege();
        String sql = "select id from user_plan where userName=" +
                     StrUtil.sqlstr(privilege.getUser(request)) +
                     " and is_closed=0 order by mydate desc";
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();
        String str = "";
        try {
            ListResult lr = listResult(sql, 1, uds.getCount());
            Iterator ir = lr.getResult().iterator();
            if(ir.hasNext()) {
            	str += "<table class='article_table'>";
                while (ir.hasNext()) {
                    PlanDb pd = (PlanDb) ir.next();

                    String t = StrUtil.getLeft(pd.getTitle(), uds.getWordCount());

                    String mydate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd");

                    str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(pd.getTitle()) + "' href='" + url + "?id=" + pd.getId() + "'>" + " " +
                            StrUtil.toHtml(t) + "</a></td><td class='article_time'>[" +mydate + "]</td></tr>";
                }
                str += "</table>";
            }else{
            	str = "<div class='no_content'><img title='暂无日程安排' src='images/desktop/no_content.jpg'></div>";
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).info("display:" + e.getMessage());
        }
        return str;
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        QUERY_SAVE = "update " + tableName + " set title=?,content=?,myDate=?,isRemind=?,remindDate=?,remindCount=?,IS_REMIND_BY_SMS=?,endDate=?,is_notepaper=?,is_closed=?,x=?,y=?,is_shared=? where id=?";
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             // QUERY_SAVE = "update " + tableName + " set title=?,content=?,myDate=? where id=?";
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, title);
             ps.setString(2, content);
             ps.setTimestamp(3, new Timestamp(myDate.getTime()));
             ps.setInt(4, remind?1:0);
             
             if (remindDate==null)
             	ps.setTimestamp(5, null);
             else
             	ps.setTimestamp(5, new Timestamp(remindDate.getTime()));
             
             // ps.setTimestamp(5, new Timestamp(myDate.getTime()));
             ps.setInt(6, remindCount);
             ps.setInt(7, remindBySMS?1:0);
             if(this.getEndDate()!=null){
                 ps.setTimestamp(8, new Timestamp(endDate.getTime()));
               }else{
                 ps.setTimestamp(8, new Timestamp(remindDate.getTime()));
               }
           //  ps.setInt(9, remindType);
             ps.setInt(9, notepaper?1:0);
             ps.setInt(10, closed?1:0);
             ps.setInt(11, x);
             ps.setInt(12, y);
             ps.setInt(13, shared?1:0);
             ps.setInt(14, id);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
                 PlanCache rc = new PlanCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error(e);
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }
    
    /**
     * 用于流程节点处理完毕时,结束日程
     * @param userName
     * @param actionData 流程ID
     * @return
     */
    public PlanDb getPlanDb(String userName, int actionType, String actionData) {
    	String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName) + " and action_type=" + actionType + " and action_data=" + StrUtil.sqlstr(actionData);
    	Iterator ir = list(sql).iterator();
    	if (ir.hasNext()) {
    		return (PlanDb)ir.next();
    	}
    	return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setZdrq(java.util.Date zdrq) {
        this.zdrq = zdrq;
    }

    public void setMyDate(java.util.Date myDate) {
        this.myDate = myDate;
    }

    public void setRemind(boolean remind) {
        this.remind = remind;
    }

    public void setRemindDate(java.util.Date remindDate) {
        this.remindDate = remindDate;
    }

    public void setRemindCount(int remindCount) {
        this.remindCount = remindCount;
    }

    public void setRemindBySMS(boolean remindBySMS) {
        this.remindBySMS = remindBySMS;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public java.util.Date getZdrq() {
        return zdrq;
    }

    public java.util.Date getMyDate() {
        return myDate;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isRemind() {
        return remind;
    }

    public java.util.Date getRemindDate() {
        return remindDate;
    }

    public int getRemindCount() {
        return remindCount;
    }

    public boolean isRemindBySMS() {
        return remindBySMS;
    }

    /**
     * 取得用户发布的最后一条便笺
     * @param userName
     * @return
     */
    public PlanDb getLastNotepaper(String userName) {
		// x、y座标初始化
		String sql = "select id from user_plan where userName=" + StrUtil.sqlstr(userName) + " and is_notepaper=1 order by id desc";
		Iterator ir = list(sql, 0, 1).iterator();
		if (ir.hasNext()) {
			return (PlanDb)ir.next();
		}
		return null;
    }

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private java.util.Date endDate = null;
    private int remindType = 0;
    private String title;
    private String content;

    // JsonFormat为springboot注解，JSONField为fastjson注解，序列化时生效的是JSONField
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private java.util.Date zdrq;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private java.util.Date myDate;
    private String userName;
    private boolean remind = false;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private java.util.Date remindDate;
    private int remindCount = 0;
    private boolean remindBySMS = true;
    
    private int actionType = 0;
    private String actionData = "";
    
    private boolean shared = false;
    
	public boolean isShared() {
		return shared;
	}


	public void setShared(boolean shared) {
		this.shared = shared;
	}


	public java.util.Date getEndDate() {
		return endDate;
	}

	public void setEndDate(java.util.Date endDate) {
		this.endDate = endDate;
	}

	public int getRemindType() {
		return remindType;
	}

	public void setRemindType(int remindType) {
		this.remindType = remindType;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public int getActionType() {
		return actionType;
	}

	public void setActionData(String actionData) {
		this.actionData = actionData;
	}

	public String getActionData() {
		return actionData;
	}
	
	boolean notepaper = false;
	
	boolean closed = false;
	
	private String maker = "";
	
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isNotepaper() {
		return notepaper;
	}

	public void setNotepaper(boolean notepaper) {
		this.notepaper = notepaper;
	}

	public void setMaker(String maker) {
		this.maker = maker;
	}

	public String getMaker() {
		return maker;
	}

	/**
	 * @param left the left to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the left
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param top the top to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the top
	 */
	public int getY() {
		return y;
	}
}
