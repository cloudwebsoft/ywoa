package cn.js.fan.module.pvg;

import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class Priv {
    String priv;
    String connname;
    String desc;

    static String[] privs = {"admin", "user", "backup", "email", "fourm", "forum.message", "chat"};

    public static final String PRIV_ADMIN = privs[0];
    public static final String PRIV_USER = privs[1];
    public static final String PRIV_BACKUP = privs[2];
    public static final String PRIV_EMAIL = privs[3];
    public static final String PRIV_FORUM = privs[4];
    public static final String PRIV_FORUM_MESSAGE = privs[5];
    public static final String PRIV_CHAT = privs[6];

    Logger logger = Logger.getLogger(Priv.class.getName());

    final String INSERT = "insert into privilege (priv, description) values (?,?)";
    final String STORE = "update privilege set description=? where priv=?";
    final String LOAD = "select priv,description,isSystem from privilege where priv=?";
    final String DEL = "delete from privilege where priv=?";

    public Priv() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Priv:connname is empty.");
    }

    public Priv(String priv) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Priv:connname is empty.");
        this.priv = priv;
        load();
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

    public String getDesc() {
        return this.desc;
    }

    public int getIsSystem() {
        return isSystem;
    }

    public boolean insert(String priv, String desc) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setString(1, priv);
            pstmt.setString(2, desc);
            re = conn.executePreUpdate()==1?true:false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean store() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(STORE);
            pstmt.setString(1, desc);
            pstmt.setString(2, priv);
            re = conn.executePreUpdate()==1?true:false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(LOAD);
            pstmt.setString(1, priv);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    priv = rs.getString(1);
                    desc = rs.getString(2);
                    isSystem = rs.getInt(3);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del(String priv) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(DEL);
            pstmt.setString(1, priv);
            re = conn.executePreUpdate()==1?true:false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    private int isSystem = 0;
}
