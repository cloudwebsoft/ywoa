package com.redmoon.oa.pvg;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.License;

import cn.js.fan.base.*;
import cn.js.fan.db.*;

public class PrivDb extends ObjectDb {
    String priv;
    String desc;

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    int orders;

    static String[] privs = {"admin", "admin.user", "admin.backup", "email",
                            "read", "workplan"};

    public static final String PRIV_ADMIN = privs[0];
    public static final String PRIV_USER = privs[1];
    public static final String PRIV_BACKUP = privs[2];
    public static final String PRIV_EMAIL = privs[3];
    public static final String PRIV_READ = privs[4];
    public static final String PRIV_WORKPLAN = privs[5];
    
    /**
     * 默认型
     */
    public static final int KIND_DEFAULT = 0;
    /**
     * 政府型
     */
    public static final int KIND_GOV = 1;
    
    /**
     * 企业型
     */
    public static final int KIND_COM = 2;

    public static final int LAYER_GROUP = 1;

    public static final int LAYER_PRV = 2;

    public PrivDb() {
        init();
    }

    public PrivDb(String priv) {
        init();
        this.priv = priv;
        load();
    }

    public void initDB() {
        tableName = "privilege";
        primaryKey = new PrimaryKey("priv", PrimaryKey.TYPE_STRING);
        objectCache = new PrivCache(this);
        this.isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName +
                       " (priv, description, layer) values (?,?,?)";
        QUERY_SAVE = "update " + tableName + " set description=?,layer=?,orders=? where priv=?";
        QUERY_LOAD =
                "select priv,description,isSystem,is_admin,kind,layer,orders from " + tableName +
                " where priv=?";
        QUERY_DEL = "delete from " + tableName + " where priv=?";
        QUERY_LIST = "select priv from " + tableName +
                     " order by orders asc";
        
        License license = License.getInstance();
        if (license.isCom()) {
            QUERY_LIST = "select priv from " + tableName +
            	" where kind=" + PrivDb.KIND_DEFAULT + " or kind=" + PrivDb.KIND_COM + " order by orders asc";
        }
        else if (license.isGov()) {
            QUERY_LIST = "select priv from " + tableName +
            	" where kind=" + PrivDb.KIND_DEFAULT + " or kind=" + PrivDb.KIND_GOV + " order by orders asc";
        }
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new PrivDb(pk.getStrValue());
    }

    public PrivDb getPrivDb(String priv) {
        return (PrivDb) getObjectDb(priv);
    }

    public String getPriv() {
        return priv;
    }

    public void setPriv(String priv) {
        this.priv = priv;
    }

    public void setDesc(String d) {
        this.desc = d;
    }

    public void setIsSystem(int isSystem) {
        this.isSystem = isSystem;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getIsSystem() {
        return isSystem;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean create(String priv, String desc, int layer) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, priv);
            pstmt.setString(2, desc);
            pstmt.setInt(3, layer);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PrivCache rc = new PrivCache(this);
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

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, desc);
            pstmt.setInt(2, layer);
            pstmt.setInt(3, orders);
            pstmt.setString(4, priv);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PrivCache rc = new PrivCache(this);
                primaryKey.setValue(priv);
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    @Override
    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, priv);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    priv = rs.getString(1);
                    desc = rs.getString(2);
                    isSystem = rs.getInt(3);
                    admin = rs.getInt(4)==1;
                    kind = rs.getInt(5);
                    layer = rs.getInt(6);
                    orders = rs.getInt(7);
                    loaded = true;
                    primaryKey.setValue(priv);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * 删除权限
     * @return boolean
     */
    @Override
    public boolean del() {
        return false;
    }

    /**
     * 取得拥有某权限的全部用户，如：用于合同到期时发送短消息
     * @param priv String
     * @return Vector
     */
    public static Vector getUsersHavePriv(String priv) {
        return Privilege.getUsersHavePriv(priv);
    }

    /**
     * 是否为系统权限，系统权限不能被删除
     */
    private int isSystem = 0;

    /**
     * 该权限是否仅可以由拥有admin权限的人员来赋予
     */
    private boolean admin = false;
    
    /**
     * 权限类型（企业型-0或政府型-1）
     */
    private int kind = KIND_COM;
    
    private int layer = 1;

	/**
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}

	public String getSqlList() {
        License license = License.getInstance();
        String sql = "select priv,description,isSystem,layer from privilege";
        if (license.isGov()) {
            sql = "select priv,description,isSystem,layer from privilege where kind=" + PrivDb.KIND_DEFAULT + " or kind=" + PrivDb.KIND_GOV;
        }
        else if (license.isGov()) {
            sql = "select priv,description,isSystem,layer from privilege where kind=" + PrivDb.KIND_DEFAULT + " or kind=" + PrivDb.KIND_COM;
        }

        // sql += " order by isSystem desc, priv asc";
        sql += " order by orders asc";
        return sql;
    }
}
