package com.redmoon.oa.fileark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
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
/**
 * @author Administrator
 *
 */
public class DocAttachmentLogDb extends ObjectDb {
    private long id;
    private String userName;
    private int att_id;
    private String ip;
    private  Date logDate;
    private int doc_id;
    
    public DocAttachmentLogDb() {
        init();
    }

    public DocAttachmentLogDb(long id) {
        this.id = id;
        init();
        load();
    }

    public void setId(int id) {
		this.id = id;
	}

	public long getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    
    public int getAtt_id() {
		return att_id;
	}

	public void setAtt_id(int att_id) {
		this.att_id = att_id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	public int getDoc_id() {
		return doc_id;
	}

	public void setDoc_id(int doc_id) {
		this.doc_id = doc_id;
	}

	public void initDB() {
        tableName = "doc_attachment_log";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new DocAttachmentLogCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (user_name,att_id,ip,log_date,doc_id) values (?,?,?,?,?)";
        //QUERY_SAVE = "update " + tableName + " set userName=?,content=?,appraise=? where id=?";
        //QUERY_LIST =
                //"select id from " + tableName + " order by mydate desc";
        //QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select user_name,att_id,ip,log_date,doc_id from " + tableName + " where id=?";

    }

    public DocAttachmentLogDb getDocAttachmentLogDb(long id) {
        return (DocAttachmentLogDb)getObjectDb(new Long(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, userName);
            ps.setInt(2, att_id);
            ps.setString(3, ip);
            ps.setTimestamp(4, new Timestamp(logDate.getTime()));
            ps.setInt(5, doc_id);
            
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
            	DocAttachmentLogCache rc = new DocAttachmentLogCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
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
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    @Override
    public boolean del() throws ErrMsgException {
        String sql = "delete from " + tableName + " where id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
			jt.executeUpdate(sql, new Object[]{id});
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			return false;
		}
        
        // 更新缓存
        DocAttachmentLogCache dlc = new DocAttachmentLogCache();
        primaryKey.setValue(id);
        dlc.refreshDel(primaryKey);
        return true;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DocAttachmentLogDb(pk.getLongValue());
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
            ps.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                userName = rs.getString(1);
                att_id = rs.getInt(2);
                ip =rs.getString(3);
                logDate = rs.getTimestamp(4);
                doc_id = rs.getInt(5);
                loaded = true;
                primaryKey.setValue(new Long(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
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
         boolean re = false;
         return re;
    }

    @Override
    @SuppressWarnings("unchecked")
	public ListResult listResult(String listsql, int curPage, int pageSize)
			throws ErrMsgException {
		int total = 0;
		ResultSet rs = null;
		//创建Vector对象
		Vector result = new Vector();
		//创建ListResult对象
		ListResult lr = new ListResult();
		lr.setTotal(total);
		lr.setResult(result);
		//打开数据库连接
		Conn conn = new Conn(connname);
		try {
			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(listsql);
			//获取rs
			rs = conn.executeQuery(countsql);
			if (rs != null && rs.next()) {
				total = rs.getInt(1);
			}
			if (rs != null) {
				//关闭rs
				rs.close();
				//rs初始化
				rs = null;
			}

			if (total != 0)
				conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
			//获取rs
			rs = conn.executeQuery(listsql);
			if (rs == null) {
				return lr;
			} else {
				rs.setFetchSize(pageSize);
				int absoluteLocation = pageSize * (curPage - 1) + 1;
				if (rs.absolute(absoluteLocation) == false) {
					return lr;
				}
				do {
					DocAttachmentLogDb dad = getDocAttachmentLogDb(rs.getInt(1));
					result.addElement(dad);
				} while (rs.next());
			}
		} catch (SQLException e) {//捕获SQLException
			LogUtil.getLog(getClass()).error(e.getMessage());
			throw new ErrMsgException("数据库出错！");
		} finally {
			if (conn != null) {
				//关闭连接
				conn.close();
				//连接初始化
				conn = null;
			}
		}

		lr.setResult(result);
		lr.setTotal(total);
		return lr;
	}
    
    public void delOfDoc(int docId) throws ErrMsgException {
        String sql = "select id from " + tableName + " where doc_id=" + docId;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
        	DocAttachmentLogDb dld = (DocAttachmentLogDb)ir.next();
        	dld.del();
        }
    }
}
