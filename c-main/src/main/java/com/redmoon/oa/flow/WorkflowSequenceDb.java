package com.redmoon.oa.flow;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

public class WorkflowSequenceDb extends ObjectDb {
	/**
	 * 数值型
	 */
    public static final int TYPE_NUMBER = 0;
    /**
     * 组合型
     */
    public static final int TYPE_COMPOUND = 1;

    public WorkflowSequenceDb() {
        init();
    }

    public WorkflowSequenceDb(int id) {
        init();
        this.id = id;
        load();
    }

    public void initDB() {
        tableName = "flow_sequence";
        primaryKey = new PrimaryKey("ID", PrimaryKey.TYPE_INT);
        objectCache = new WorkflowSequenceCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (ID, NAME, BEGIN_INDEX, CUR_INDEX, LENGTH, seq_type, template, cur_value, item_separator, year_digit) values (?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set NAME=?,BEGIN_INDEX=?,CUR_INDEX=?,LENGTH=?, template=?,cur_value=?,item_separator=?,year_digit=? where ID=?";
        QUERY_LOAD =
                "select NAME,BEGIN_INDEX,CUR_INDEX,LENGTH,seq_type,template,cur_value,item_separator, year_digit from " + tableName + " where ID=?";
        QUERY_DEL = "delete from " + tableName + " where ID=?";
        QUERY_LIST = "select ID from " + tableName + " order by NAME ASC";
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WorkflowSequenceDb(pk.getIntValue());
    }

    public WorkflowSequenceDb getWorkflowSequenceDb(int id) {
        WorkflowSequenceDb log = (WorkflowSequenceDb)getObjectDb(new Integer(id));
        return log;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBeginIndex(long beginIndex) {
        this.beginIndex = beginIndex;
    }

    public void setCurIndex(long curIndex) {
        this.curIndex = curIndex;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public long getBeginIndex() {
        return beginIndex;
    }

    public long getCurIndex() {
        return curIndex;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        // QUERY_CREATE = "insert into " + tableName + " (ID, NAME, BEGIN_INDEX, CUR_INDEX, LENGTH) values (?,?,?,?,?)";
        id = (int)SequenceManager.nextID(SequenceManager.OA_WORKFLOW_SEQUENCE);
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setLong(3, beginIndex);
            pstmt.setLong(4, curIndex);
            pstmt.setLong(5, length);
            pstmt.setInt(6, type);
            pstmt.setString(7, template);
            pstmt.setString(8, curValue);
            pstmt.setString(9, itemSeparator);
            pstmt.setInt(10, yearDigit);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                WorkflowSequenceCache rc = new WorkflowSequenceCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public synchronized boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        // QUERY_SAVE = "update " + tableName + " set NAME=?,BEGIN_INDEX=?,CUR_INDEX=?,LENGTH=? where ID=?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, name);
            pstmt.setLong(2, beginIndex);
            pstmt.setLong(3, curIndex);
            pstmt.setInt(4, length);
            pstmt.setString(5, template);
            pstmt.setString(6, curValue);
            pstmt.setString(7, itemSeparator);
            pstmt.setInt(8, yearDigit);
            pstmt.setInt(9, id);
            re = conn.executePreUpdate()==1?true:false;
            LogUtil.getLog(getClass()).info("save curIndex=" + curIndex);
            if (re) {
                WorkflowSequenceCache rc = new WorkflowSequenceCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public long getNextId() {
        String sql = "select CUR_INDEX from " + tableName + " where id=?";
        long idx = -2;
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    idx = rs.getLong(1);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getNextId:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return idx + 1;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            LogUtil.getLog(getClass()).info("load: id=" + id + " " + QUERY_LOAD);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                // "select NAME,BEGIN_INDEX,CUR_INDEX,LENGTH from " + tableName + " where ID=?";
                    name = StrUtil.getNullStr(rs.getString(1));
                    beginIndex = rs.getLong(2);
                    curIndex = rs.getLong(3);
                    length = rs.getInt(4);
                    type = rs.getInt(5);
                    template = StrUtil.getNullStr(rs.getString(6));
                    curValue = StrUtil.getNullStr(rs.getString(7));
                    itemSeparator = StrUtil.getNullStr(rs.getString(8));
                    yearDigit = rs.getInt(9);
                    loaded = true;
                    primaryKey.setValue(new Integer(id));
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;

            if (re) {
                re = conn.executePreUpdate() >= 0 ? true : false;
                WorkflowSequenceCache rc = new WorkflowSequenceCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setCurValue(String curValue) {
		this.curValue = curValue;
	}

	public String getCurValue() {
		return curValue;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setItemSeparator(String itemSeparator) {
		this.itemSeparator = itemSeparator;
	}

	public String getItemSeparator() {
		return itemSeparator;
	}

	public void setYearDigit(int yearDigit) {
		this.yearDigit = yearDigit;
	}

	public int getYearDigit() {
		return yearDigit;
	}

	private int id;
    private long beginIndex = 1;
    private long curIndex = 1;
    private int length = 0; // 为0的时候，表示不用填充
    private String name;
    private int type;
    private String curValue;
    private String template;
    
    private String itemSeparator = "-";
    private int yearDigit = 4;
}
