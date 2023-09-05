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
public class RolePrivDb extends ObjectDb {
    private String roleCode;
    public RolePrivDb() {
        init();
    }

    public RolePrivDb(String roleCode, String priv) {
        this.roleCode = roleCode;
        this.priv = priv;
        init();
        load();
    }

    public void initDB() {
        tableName = "user_role_priv";

        HashMap key = new HashMap();
        key.put("roleCode", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("priv", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);

        objectCache = new RolePrivCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (roleCode, priv) values (?,?)";
        QUERY_SAVE = "update " + tableName + " set priv=? where roleCode=?";
        QUERY_LIST =
                "select roleCode from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where roleCode=? and priv=?";
        QUERY_LOAD =
                "select roleCode, priv from " + tableName + " where roleCode=? and priv=?";
    }

    /**
     * 角色拥有的权限编码中是否有privPrefix开头的权限
     * @param roleCode String
     * @return boolean
     */
    public boolean isRoleHasPrivStartWith(String roleCode, String privPrefix) {
        String sql =
                "select roleCode, priv from " + tableName +
                " where roleCode=?";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roleCode);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    String priv = rs.getString(2);
                    if (priv.startsWith(privPrefix))
                        return true;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("isRolePrivStartWith:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
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
                RolePrivCache rc = new RolePrivCache(this);				
				rc.refreshList();
			}
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}
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
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setString(1, roleCode);
            pstmt.setString(2, priv);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                RolePrivCache rc = new RolePrivCache(this);
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
    
    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, roleCode);
            pstmt.setString(2, priv);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                RolePrivCache rc = new RolePrivCache(this);
                rc.refreshDel(primaryKey);
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

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new RolePrivDb(pk.getKeyStrValue("roleCode"), pk.getKeyStrValue("priv"));
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
            ps.setString(1, roleCode);
            ps.setString(2, priv);
            primaryKey.setKeyValue("roleCode", roleCode);
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
            ps.setString(2, roleCode);
            rowcount = conn.executePreUpdate();
            RolePrivCache uc = new RolePrivCache(this);
            primaryKey.setKeyValue("roleCode", roleCode);
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

    public RolePrivDb getRolePrivDb(String roleCode, String priv) {
        primaryKey.setKeyValue("roleCode", roleCode);
        primaryKey.setKeyValue("priv", priv);
        return (RolePrivDb)getObjectDb(primaryKey.getKeys());
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setPriv(String priv) {
        this.priv = priv;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getPriv() {
        return priv;
    }

    private String priv;
}
