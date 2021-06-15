package com.redmoon.oa.exam;
import java.sql.*;
import java.util.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.SequenceManager;

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
public class SubjectDb extends ObjectDb {
    private int id;

    /**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public SubjectDb() {
        init();
    }

    public SubjectDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_exam_subject";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new SubjectCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (id,subject,orders) values (?,?,?)";
        QUERY_SAVE = "update " + tableName + " set subject=?,orders=? where id=?";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select subject,orders from " + tableName + " where id=?";
    }

    public SubjectDb getSubjectDb(int id) {
        return (SubjectDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int)SequenceManager.nextID(SequenceManager.EXAM_SUBJECT);
            ps.setInt(1, id);
            ps.setString(2, subject);
            ps.setInt(3, orders);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                SubjectCache rc = new SubjectCache(this);
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
                SubjectCache rc = new SubjectCache(this);
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
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SubjectDb(pk.getIntValue());
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
                subject = rs.getString(1);
                orders = rs.getInt(2);
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
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, subject);
             ps.setInt(2, orders);
             ps.setInt(3, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 SubjectCache rc = new SubjectCache(this);
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
    public boolean isExist(String subjectName){
         ResultSet rs = null;
         Conn conn = new Conn(connname);
         try {
             rs = conn.executeQuery("select id from " + tableName + " where subject='"+subjectName+"'" );
             if(rs.next()) return true;
         } catch (SQLException e) {
             logger.error("list:" + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         return false ;
     }



    /**
     * 取出全部信息置于result中
     */
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
                    result.addElement(getSubjectDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }


    public String getSubject() {
        return subject;
    }

    public void setName(String subject) {
        this.subject = subject;
    }

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}


    private String subject;
    /**
     * 2017-10-26 添加orders属性 同时在增、删、改、查方法中添加orders字段
     * sht
     */
    private int orders;

}
