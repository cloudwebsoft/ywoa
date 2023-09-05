package com.redmoon.oa.pvg;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.cloudwebsoft.framework.db.JdbcTemplate;
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
public class UserGroupPrivDb extends ObjectDb {
    private String groupCode;

    public UserGroupPrivDb() {
        init();
    }

    public UserGroupPrivDb(String groupCode, String priv) {
        this.groupCode = groupCode;
        this.priv = priv;
        init();
        load();
    }

    public void initDB() {
        tableName = "user_group_priv";

        HashMap key = new HashMap();
        key.put("groupCode", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("priv", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);

        objectCache = new UserGroupPrivCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (groupCode, priv) values (?,?)";
        QUERY_SAVE = "update " + tableName + " set priv=? where groupCode=?";
        QUERY_LIST =
                "select groupCode from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where groupCode=? and priv=?";
        QUERY_LOAD =
                "select groupCode, priv from " + tableName + " where groupCode=? and priv=?";
    }

    /**
     * 用户组拥有的权限编码中是否有privPrefix开头的权限
     * @param groupCode String
     * @param privPrefix String
     * @return boolean
     */
    public boolean isUserGroupHasPrivStartWith(String groupCode, String privPrefix) {
        String sql =
                "select groupCode, priv from " + tableName +
                " where groupCode=?";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, groupCode);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    String priv = rs.getString(2);
                    if (priv.startsWith(privPrefix))
                        return true;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("isUserGroupHasPrivStartWith:" + e.getMessage());
        } finally {
            conn.close();
        }
        return false;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setString(1, groupCode);
            pstmt.setString(2, priv);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                UserGroupPrivCache rc = new UserGroupPrivCache(this);
                rc.refreshDel(primaryKey);
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
    
    /**
     * 删除权限时，同步删除相关记录
     * @param priv
     */
    public void delOfPriv(String priv) {
    	JdbcTemplate jt = new JdbcTemplate();
    	String sql = "delete from " + getTableName() + " where priv=?";
    	try {
			int re = jt.executeUpdate(sql, new Object[]{priv});
			if (re>0) {
                UserGroupPrivCache uc = new UserGroupPrivCache(this);				
				uc.refreshList();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error(e);
		}
    }    

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserGroupPrivDb(pk.getKeyStrValue("groupCode"), pk.getKeyStrValue("priv"));
    }

    public UserGroupPrivDb getUserGroupPrivDb(String roleCode, String priv) {
        primaryKey.setKeyValue("groupCode", roleCode);
        primaryKey.setKeyValue("priv", priv);
        return (UserGroupPrivDb)getObjectDb(primaryKey.getKeys());
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
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, groupCode);
            ps.setString(2, priv);
            primaryKey.setKeyValue("groupCode", groupCode);
            primaryKey.setKeyValue("priv", priv);
            rs = conn.executePreQuery();
            if (rs.next()) {
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        }
        finally {
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
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, priv);
            ps.setString(2, groupCode);
            rowcount = conn.executePreUpdate();
            UserGroupPrivCache uc = new UserGroupPrivCache(this);
            primaryKey.setKeyValue("groupCode", groupCode);
            primaryKey.setKeyValue("priv", priv);
            uc.refreshSave(primaryKey);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public void setPriv(String priv) {
        this.priv = priv;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getPriv() {
        return priv;
    }

    private String priv;
}
