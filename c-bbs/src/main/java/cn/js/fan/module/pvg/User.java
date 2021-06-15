package cn.js.fan.module.pvg;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.module.cms.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class User {
    String name;
    String connname;
    String realName;
    String desc;
    String pwdMD5;

    public static final String ADMIN = "admin";

    transient Logger logger = Logger.getLogger(User.class.getName());

    final String GETGROUP =
            "select group_code from user_of_group where user_name=?";
    final String INSERT =
            "insert into users (name, realname, description, pwd, enter_last,is_foreground_user) values (?,?,?,?,?,?)";
    final String STORE =
            "update users set realname=?,description=?,enter_count=?,enter_last=?,is_foreground_user=? where name=?";
    final String LOAD =
            "select name,realname,description,enter_count,enter_last,pwd,is_foreground_user from users where name=?";
    final String STOREWITHPWD =
            "update users set realname=?,description=?,pwd=?,enter_count=? where name=?";

    public User() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("User:connname is empty.");
    }

    public User(String name) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:connname is empty.");
        this.name = name;
        load();
    }

    public String getName() {
        return name;
    }

    public String getPwdMD5() {
        return pwdMD5;
    }

    public void setPwdMD5(String p) {
        this.pwdMD5 = p;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return this.realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
        store();
    }

    public void setDesc(String d) {
        this.desc = d;
        store();
    }

    public void setEnterCount(int enterCount) {
        this.enterCount = enterCount;
    }

    public void setEnterLast(java.util.Date enterLast) {
        this.enterLast = enterLast;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setForegroundUser(boolean foregroundUser) {
        this.foregroundUser = foregroundUser;
    }

    public void setEnterLast() {
        enterLast = new java.util.Date();
    }

    public String getDesc() {
        return this.desc;
    }

    public int getEnterCount() {
        return enterCount;
    }

    public java.util.Date getEnterLast() {
        return enterLast;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isForegroundUser() {
        return foregroundUser;
    }

    public User getUser(String name) {
        return new User(name);
    }

    // 此函数中使用了rmconn的prepareStatement，好象会导致出现连接问题
    public UserGroup[] getGroup() {
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        UserGroup[] ug = null;
        PreparedStatement pstmt = null;

        try {
            //更新文件内容
            pstmt = rmconn.prepareStatement(GETGROUP);
            pstmt.setString(1, name);
            ri = rmconn.executePreQuery();

            if (ri != null) {
                ug = new UserGroup[ri.getRows()];
                int i = 0;
                String code;
                UserGroupMgr ugm = new UserGroupMgr();
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    code = rr.getString(1);
                    ug[i] = ugm.getUserGroup(code);
                    i++;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return ug;
    }

/*
    public Group[] getGroup() {
        Conn conn = new Conn(connname);
        Group[] ug = null;
        PreparedStatement pstmt = null;

        try {
            //更新文件内容
            pstmt = conn.prepareStatement(GETGROUP);
            pstmt.setString(1, name);
            ResultSet rs = conn.executePreQuery();

            if (rs != null) {
                ug = new Group[conn.getRows()];
                int i = 0;
                String code;
                UserGroupMgr ugm = new UserGroupMgr();
                while (rs.next()) {
                    code = rs.getString(1);
                    ug[i] = ugm.getUserGroup(code);
                    i++;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return ug;
    }
*/
    public boolean insert(String name, String realname, String desc,
                          String pwdMD5, boolean isForegroundUser) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setString(1, name);
            pstmt.setString(2, realname);
            pstmt.setString(3, desc);
            pstmt.setString(4, pwdMD5);
            pstmt.setString(5, DateUtil.toLongString(new java.util.Date()));
            pstmt.setInt(6, isForegroundUser?1:0);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean modifyUserName(String newName) {
        String sql = "update users set name=? where name=?";
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, newName);
             pstmt.setString(2, name);
             re = conn.executePreUpdate() == 1 ? true : false;
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
            PreparedStatement pstmt = conn.prepareStatement(STORE);
            pstmt.setString(1, realName);
            pstmt.setString(2, desc);
            pstmt.setInt(3, enterCount);
            pstmt.setString(4, DateUtil.toLongString(enterLast));
            pstmt.setInt(5, foregroundUser?1:0);
            pstmt.setString(6, name);

            // System.out.println("realName=" + realName);
            // System.out.println("desc=" + desc);
            // System.out.println("enterCount=" + enterCount);
            // System.out.println("enterLast=" + enterLast);
            // System.out.println("name=" + name);

            re = conn.executePreUpdate() == 1 ? true : false;
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

    public boolean storeWithPwd() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(STOREWITHPWD);
            pstmt.setString(1, realName);
            pstmt.setString(2, desc);
            pstmt.setString(3, pwdMD5);
            pstmt.setInt(4, enterCount);
            pstmt.setString(5, name);
            re = conn.executePreUpdate() == 1 ? true : false;
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
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    name = rs.getString(1);
                    realName = rs.getString(2);
                    desc = rs.getString(3);
                    enterCount = rs.getInt(4);
                    try {
                        enterLast = DateUtil.parse(rs.getString(5));
                    }
                    catch (Exception e) {
                    }
                    pwdMD5 = rs.getString(6);
                    foregroundUser = rs.getInt(7)==1;
                    loaded = true;
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            /*
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del(String name) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            conn.beginTrans();
            String sql = "delete from users where name=" + StrUtil.sqlstr(name);
            re = conn.executeUpdate(sql) == 1 ? true : false;
            sql = "delete from user_of_group where user_name=" + StrUtil.sqlstr(name);
            conn.executeUpdate(sql);
            conn.commit();
            // 删除其对目录的权限
            LeafPriv lp = new LeafPriv();
            lp.delPrivsOfUserOrGroup(name);
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean setGroup(HttpServletRequest request) throws ErrMsgException {
        String[] groups = request.getParameterValues("group_code");
        String name = ParamUtil.get(request, "name");
        if (name.equals(""))
            throw new ErrMsgException("用户名不能为空！");
        int len = 0;
        if (groups!=null)
            len = groups.length;
        String insertSql = "";

        Conn conn = new Conn(connname);
        try {
            String sql = "delete from user_of_group where user_name=" +
                         StrUtil.sqlstr(name);
            conn.beginTrans();
            conn.executeUpdate(sql);
            for (int i = 0; i < len; i++) {
                insertSql =
                        "insert into user_of_group (user_name,group_code) values (" +
                        StrUtil.sqlstr(name) +
                        ", " + StrUtil.sqlstr(StrUtil.UnicodeToUTF8(groups[i])) +
                        ")";
                conn.executeUpdate(insertSql);
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            logger.error("setGroup:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean Auth(String name, String pwd) {
        String sql = "select pwd,is_foreground_user from users where name=" + StrUtil.sqlstr(name);
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs!=null && rs.next()) {
                String pwdMD5 = rs.getString(1);
                String p = SecurityUtil.MD5(pwd);
                boolean isForegroundUser = rs.getInt(2)==1;
                if (isForegroundUser) {
                    // 前台用户
                    com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
                    ud = ud.getUserDbByNick(name);
                    if (ud==null || !ud.isLoaded())
                        return false;
                    else {
                        if (ud.getPwdMd5().equals(p))
                            return true;
                        else
                            return false;
                    }
                }

                // logger.info("pwdMD5=" + pwdMD5 + " p=" + p);
                if (pwdMD5.equals(p))
                    return true;
            }
        }
        catch (Exception e) {
            logger.error("Auth:" + e.getMessage());
        }
        finally {
            if (rs!=null) {
                try { rs.close(); } catch (Exception e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return false;
    }

    public String[] getPrivs() {
        String sql = "select priv from user_priv where username=" + StrUtil.sqlstr(name);
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        String[] pv = null;
        try {
            ri = rmconn.executeQuery(sql);
            int count = ri.getRows();
            if (count>0)
                pv = new String[count];
            if (ri != null) {
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    pv[i] = rr.getString(1);
                    i++;
                }
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return pv;
    }

    public boolean setPrivs(HttpServletRequest request) throws ErrMsgException{
        String[] privs = request.getParameterValues("priv");
        String insertSql = "";
        boolean clearall = false;
        int len = 0;
        if (privs==null)
            clearall = true;
        else {
            len = privs.length;
        }
        Conn conn = new Conn(connname);
        try {
            String sql = "delete from user_priv where username=" +
                         StrUtil.sqlstr(name);
            conn.beginTrans();
            conn.executeUpdate(sql);
            if (!clearall) {
                for (int i = 0; i < len; i++) {
                    insertSql = "insert into user_priv (username,priv) values (" +
                                StrUtil.sqlstr(name) +
                                ", " +
                                StrUtil.sqlstr(StrUtil.UnicodeToUTF8(privs[i])) +
                                ")";
                    conn.executeUpdate(insertSql);
                }
            }
            conn.commit();
        }
        catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库错误！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    private int enterCount = 0;
    private java.util.Date enterLast;
    private boolean loaded = false;
    private boolean foregroundUser = false;
}
