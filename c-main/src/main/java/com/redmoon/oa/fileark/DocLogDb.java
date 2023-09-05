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
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
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
public class DocLogDb extends ObjectDb {
    private long id;
    private String userName;
    private int doc_id;
    private String ip;
    private Date logDate; 
    
    public DocLogDb() {
        init();
    }

    public DocLogDb(long id) {
        this.id = id;
        init();
        load();
    }

    public void setId(long id) {
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
    
    public int getDoc_id() {
		return doc_id;
	}

	public void setDoc_id(int doc_id) {
		this.doc_id = doc_id;
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

	public void initDB() {
        tableName = "doc_log";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new DocLogCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (user_name,doc_id,ip,log_date) values (?,?,?,?)";
        //QUERY_SAVE = "update " + tableName + " set userName=?,content=?,appraise=? where id=?";
        //QUERY_LIST =
                //"select id from " + tableName + " order by mydate desc";
        //QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select user_Name,ip,log_date,doc_id from " + tableName + " where id=?";

    }

    public DocLogDb getDocLogDb(long id) {
        return (DocLogDb)getObjectDb(new Long(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, userName);
            ps.setInt(2, doc_id);
            ps.setString(3, ip);
            ps.setTimestamp(4, new Timestamp(logDate.getTime()));
            
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                DocLogCache rc = new DocLogCache(this);
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
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        String sql = "delete from " + tableName + " where id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
			jt.executeUpdate(sql, new Object[]{new Long(id)});
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			return false;
		}
        
        // 更新缓存
        DocLogCache dlc = new DocLogCache();
        primaryKey.setValue(new Long(id));
        dlc.refreshDel(primaryKey);
        return true;
    }
    
    public void delOfDoc(int docId) throws ErrMsgException {
        String sql = "select id from doc_log where doc_id=" + docId;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
        	DocLogDb dld = (DocLogDb)ir.next();
        	dld.del();
        }
    }    

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DocLogDb(pk.getLongValue());
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
                ip = rs.getString(2);
                logDate = rs.getTimestamp(3);
                doc_id = rs.getInt(4);
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
					DocLogDb dld = getDocLogDb(rs.getInt(1));
					result.addElement(dld);
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
	
	/**
	 * @Description: 用户是否已读文章
	 * @param userName
	 * @param docId
	 * @return
	 */
	public boolean isUserReaded(String userName, int docId) {
		Document doc = new Document(docId);
		if (doc == null || !doc.isLoaded()) {
			return true;
		}
		Leaf lf = new Leaf(doc.getDirCode());
		if (lf == null || !lf.isLoaded()) {
			return true;
		}
		if (!lf.isLog()) {
			return true;
		}

		String sql = "select id from doc_log where user_name="
				+ StrUtil.sqlstr(userName) + " and doc_id=" + docId;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (!ri.hasNext()) {
				return false;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		} finally {
			jt.close();
		}

		return true;
	}

}
