package com.redmoon.oa.worklog;

import java.sql.*;
import java.util.*;

import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.workplan.WorkPlanAnnexDb;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;

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
public class WorkLogDb extends ObjectDb {
	public Vector attachs = new Vector();
    private int id;
    private int praiseCount;
    


	/**
	 * @return the praiseCount
	 */
	public int getPraiseCount() {
		return praiseCount;
	}

	/**
	 * @param praiseCount the praiseCount to set
	 */
	public void setPraiseCount(int praiseCount) {
		this.praiseCount = praiseCount;
	}

	public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;
    
    public static final int TYPE_NORMAL = 0;
    /**
     * 周报
     */
    public static final int TYPE_WEEK = 1;
    /**
     * 月报
     */
    public static final int TYPE_MONTH = 2;    

    public WorkLogDb() {
        init();
    }

    public WorkLogDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "work_log";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new WorkLogCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (id,userName,content,myDate,log_type,log_item,log_year,itemType) values (?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set userName=?,content=?,appraise=?,itemType=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by mydate desc";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select userName,content,myDate,appraise,log_type,log_item,log_year,itemType,review_count,praise_count from " + tableName + " where id=?";

    }

    public WorkLogDb getWorkLogDb(int id) {
        return (WorkLogDb)getObjectDb(new Integer(id));
    }

    /**
     * 用于手机端日报添加
     */
    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
        	id = (int) SequenceManager.nextID(SequenceManager.OA_WORK_LOG);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setString(2, userName);
            ps.setString(3, content);
            // 手机客户端提交时，myDate为null
            if (myDate==null)
            	myDate = new java.util.Date();
            ps.setTimestamp(4, new Timestamp(myDate.getTime()));
            ps.setInt(5, logType);
            ps.setInt(6, logItem);
            ps.setInt(7, logYear);
            ps.setString(8, itemType);
            
            
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                WorkLogCache rc = new WorkLogCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
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
    
    public boolean create(FileUpload fu) throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
        	id = (int) SequenceManager.nextID(SequenceManager.OA_WORK_LOG);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setString(2, userName);
            ps.setString(3, content);
            // 手机客户端提交时，myDate为null
            if (myDate==null)
            	myDate = new java.util.Date();
            ps.setTimestamp(4, new Timestamp(myDate.getTime()));
            ps.setInt(5, logType);
            ps.setInt(6, logItem);
            ps.setInt(7, logYear);
            ps.setString(8, itemType);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                WorkLogCache rc = new WorkLogCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        
        
     
		Vector v = fu.getFiles();
		Iterator ir = v.iterator();
		int k = 0;
		Calendar cal = Calendar.getInstance();
		// 置保存路径
		String year = "" + (cal.get(cal.YEAR));
		String month = "" + (cal.get(cal.MONTH) + 1);
		String vpath = "";
		WorkLogAttachmentDb workLogAttachmentDb = new WorkLogAttachmentDb();
		while (ir.hasNext()) {
			FileInfo fi = (FileInfo) ir.next();
			vpath = "upfile/workLog/" + year + "/" + month + "/";
			String filepath = Global.getRealPath() + "/" + vpath;
			// 使用随机名称写入磁盘
			fi.write(filepath, true);
			workLogAttachmentDb.setVisualPath(vpath);
			workLogAttachmentDb.setWorkLogId(id);
			workLogAttachmentDb.setName(fi.getName());
			workLogAttachmentDb.setDiskName(fi.getDiskName());
			workLogAttachmentDb.setOrders(k);
			workLogAttachmentDb.setSize(fi.getSize());
			re = workLogAttachmentDb.create();
			k++;
		}
        
        
        return re;
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
                WorkLogCache rc = new WorkLogCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }
    /**
     * 删除上传附件及缓存
     * @param attachId
     * @return
     */
    public boolean delAttachment(int attachId){
    	WorkLogAttachmentDb workLogAttachmentDb = new WorkLogAttachmentDb(attachId);
    	if(workLogAttachmentDb == null){
    		return false;
    	}
    	boolean re = workLogAttachmentDb.del();
    	WorkLogCache workLogCache = new WorkLogCache(this);
    	primaryKey.setValue(Integer.valueOf(id));
    	workLogCache.refreshSave(primaryKey);
    	return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WorkLogDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
        // QUERY_LOAD = "select name,reason,direction,type,myDate from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                userName = rs.getString(1);
                content = rs.getString(2);
                myDate = rs.getTimestamp(3);
                appraise = StrUtil.getNullStr(rs.getString(4));
                logType = rs.getInt(5);
                logItem = rs.getInt(6);
                logYear = rs.getInt(7);
                itemType = rs.getString(8);
                reviewCount = rs.getInt(9);
                praiseCount = rs.getInt(10);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        
        WorkLogAttachmentDb workLogAttachmentDb = new WorkLogAttachmentDb();
        attachs = workLogAttachmentDb.getAttachsOfMyWork(id);
        
        
    }
    
    /**
     * 取得当天的工作记事
     * @param userName
     * @return
     */
    public WorkLogDb getWorkLogDbOfToday(String userName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select id from " + tableName + " where myDate>=? and myDate<=? and userName=? and log_type=?";

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, 23);
            cal2.set(Calendar.MINUTE, 59);
            cal2.set(Calendar.SECOND, 59);

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(cal.getTime().getTime()));
            ps.setTimestamp(2, new Timestamp(cal2.getTime().getTime()));
            ps.setString(3, userName);
            ps.setInt(4, TYPE_NORMAL);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                return getWorkLogDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("getWorkLogDbOfToday: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }    	
        return null;
    }

    /**
     * 取得某天的日报
     * @param userName
     * @param date
     * @return
     */
    public WorkLogDb getWorkLogDb(String userName, java.util.Date date) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select id from " + tableName + " where myDate>=? and myDate<=? and userName=? and log_type=?";

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            cal2.set(Calendar.HOUR_OF_DAY, 23);
            cal2.set(Calendar.MINUTE, 59);
            cal2.set(Calendar.SECOND, 59);

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(cal.getTime().getTime()));
            ps.setTimestamp(2, new Timestamp(cal2.getTime().getTime()));
            ps.setString(3, userName);
            ps.setInt(4, TYPE_NORMAL);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                return getWorkLogDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("getWorkLogDb: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }    	
        return null;
    }    

    /**
     * 判断当天的工作记事是否已写
     * @param userName
     * @return
     */
    public boolean isWorkLogTodayWritten(String userName) {
        if (getWorkLogDbOfToday(userName)==null)
        	return false;
        else
        	return true;
    }
    
    public boolean isWorkLogWritten(String userName, java.util.Date date) {
        if (getWorkLogDb(userName, date)==null)
        	return false;
        else
        	return true;
    }    

    /**
     * save
     * 手机端日报数据更新
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, userName);
             ps.setString(2, content);
             ps.setString(3, appraise);
             ps.setString(4, itemType);
             ps.setInt(5, id);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
                 WorkLogCache rc = new WorkLogCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }
    
    public boolean save(FileUpload fu) throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, userName);
             ps.setString(2, content);
             ps.setString(3, appraise);
             ps.setString(4, itemType);
             ps.setInt(5, id);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
                 WorkLogCache rc = new WorkLogCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         
         Vector v = fu.getFiles();
 		Iterator ir = v.iterator();
 		int k = 0;
 		Calendar cal = Calendar.getInstance();
 		// 置保存路径
 		String year = "" + (cal.get(cal.YEAR));
 		String month = "" + (cal.get(cal.MONTH) + 1);
 		String vpath = "";
 		WorkLogAttachmentDb workLogAttachmentDb = new WorkLogAttachmentDb();
 		while (ir.hasNext()) {
 			FileInfo fi = (FileInfo) ir.next();
 			vpath = "upfile/workLog/" + year + "/" + month + "/";
 			String filepath = Global.getRealPath() + "/" + vpath;
 			// 使用随机名称写入磁盘
 			fi.write(filepath, true);
 			workLogAttachmentDb.setVisualPath(vpath);
 			workLogAttachmentDb.setWorkLogId(id);
 			workLogAttachmentDb.setName(fi.getName());
 			workLogAttachmentDb.setDiskName(fi.getDiskName());
 			workLogAttachmentDb.setOrders(k);
 			workLogAttachmentDb.setSize(fi.getSize());
 			re = workLogAttachmentDb.create();
 			k++;
 		}
         
        return re;
    }
    
    public WorkLogDb getWorkLogDb(String userName, int type, int year, int item) {
        String sql = "select id from work_log where userName=" + StrUtil.sqlstr(userName) + " and log_year=" + year + " and log_type=" + type + " and log_item=" + item;
        Iterator ir = list(sql).iterator();
        if (ir.hasNext()) {
            WorkLogDb wld = (WorkLogDb)ir.next();
            return wld;
        }
        return null;
    }    
    
    /**
     * 取得最后一个日报、周报或月报
     * @param userName
     * @param type
     * @return
     */
    public WorkLogDb getLastWorkLogDb(String userName, int type) {
        String sql = "select id from work_log where userName=" + StrUtil.sqlstr(userName) + " and log_type=" + type + " order by myDate desc";
        Iterator ir = list(sql).iterator();
        if (ir.hasNext()) {
            WorkLogDb wld = (WorkLogDb)ir.next();
            return wld;
        }
        return null;
    } 
    
    public java.util.Date getMyDate() {
        return myDate;
    }

    public String getContent() {
        return content;
    }

    public String getUserName() {
        return userName;
    }

    public String getAppraise() {
        return appraise;
    }

    public void setMyDate(java.util.Date myDate) {
        this.myDate = myDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAppraise(String appraise) {
        this.appraise = appraise;
    }

    public void setLogType(int logType) {
		this.logType = logType;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogItem(int logItem) {
		this.logItem = logItem;
	}

	public int getLogItem() {
		return logItem;
	}

	public void setLogYear(int logYear) {
		this.logYear = logYear;
	}

	public int getLogYear() {
		return logYear;
	}

	private java.util.Date myDate;
    private String content;
    private String userName;

    public static final int MAX_LEN2 = 3000;
    private String appraise;
    
    private int logType = TYPE_NORMAL;
    private int logItem = 0;
    
    private int logYear = 0;
    private String itemType;

	public Vector getAttachs() {
		return attachs;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	private int reviewCount;

	/**
	 * @return the reviewCount
	 */
	public int getReviewCount() {
		return reviewCount;
	}

	/**
	 * @param reviewCount the reviewCount to set
	 */
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}


	


}
