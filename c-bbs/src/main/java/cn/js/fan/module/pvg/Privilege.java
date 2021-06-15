package cn.js.fan.module.pvg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import cn.js.fan.db.Conn;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.base.IPrivilege;
import cn.js.fan.security.SecurityUtil;
import com.redmoon.forum.security.IPMonitor;

public class Privilege implements IPrivilege {
    public static final String NAME = Global.AppName + "_NAME";
    public static final String PWDMD5 = Global.AppName + "_PWDMD5";

    final String ISGROUPPRIVVALID = "select priv from user_group_priv where group_code=? and priv=?";
    String connname;
    Logger logger = Logger.getLogger(Privilege.class.getName());

    public Privilege() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Privilege:默认数据库名不能为空");
    }

    public String getUser(HttpServletRequest request) {
      HttpSession session = request.getSession(true);
      return (String)session.getAttribute(NAME);
    }

    public boolean isUserLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String name = (String)session.getAttribute(NAME);
        if (name == null)
            return false;
        else
            return true;
    }

    public boolean isUserPrivValid(HttpServletRequest request, String priv) {
        if (!isUserLogin(request))
            return false;
        if (getUser(request).equals(User.ADMIN)) // admin 享有所有权限
            return true;
        // 查用户本人是否拥有权限
        User user = new User(getUser(request));
        String[] privs = user.getPrivs();
        if (privs!=null) {
            int len = privs.length;
            for (int i=0; i<len; i++) {
                // 拥有管理员权限
                if (privs[i].equals(Priv.PRIV_ADMIN))
                    return true;
                if (privs[i].equals(priv))
                    return true;
            }
        }
        UserGroup[] ug = user.getGroup();
        // 根据组权限来判断是否有相应权限
        if (ug==null) {
            return false;
        }
        int k = ug.length;
        for (int i=0; i<k; i++) {
            if (isGroupPrivValid(ug[i], priv))
                return true;
        }
        return false;
    }

    public boolean isGroupPrivValid(UserGroup group, String priv) {
        // 管理员组拥有全部权限
        if (group.getCode().equals(group.Administrators))
            return true;
        SecurityUtil fs = new SecurityUtil();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        UserGroup[] ug = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(ISGROUPPRIVVALID);
            pstmt.setString(1, group.getCode());
            pstmt.setString(2, priv);
            rs = conn.executePreQuery();
            if (rs!=null) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("isGroupPrivValid:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public boolean setGroupPriv(HttpServletRequest request) throws ErrMsgException{
        String[] privs = request.getParameterValues("priv");
        String group_code = ParamUtil.get(request, "group_code");
        if (privs==null)
            return false;
        if (group_code.equals(""))
            throw new ErrMsgException("用户组编码不能为空！");
        int len = privs.length;
        String insertSql = "";

        Conn conn = new Conn(connname);
        try {
            String sql = "delete from user_group_priv where group_code=" +
                         StrUtil.sqlstr(group_code);
            conn.beginTrans();
            conn.executeUpdate(sql);
            for (int i = 0; i < len; i++) {
                insertSql = "insert into user_group_priv (group_code,priv) values (" +
                        StrUtil.sqlstr(group_code) +
                        ", " + StrUtil.sqlstr(StrUtil.UnicodeToUTF8(privs[i])) +
                        ")";
                conn.executeUpdate(insertSql);
            }
            conn.commit();
        }
        catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
            return false;
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean login(HttpServletRequest request, String username,
                         String pwd, String validateCode) throws
            ErrMsgException {
        HttpSession session = request.getSession(true);
        String vcode = StrUtil.getNullStr((String) session.getAttribute(
                "validateCode"));
        if (!vcode.equals(validateCode))
            throw new ErrMsgException("请输入正确的验证码！");
        String ip = request.getRemoteAddr();
        IPMonitor ipm = new IPMonitor();
        boolean re = ipm.isIPCanAdmin(ip);
        if (!re) {
            throw new ErrMsgException("IP地址非法！");
        }

        UserMgr um = new UserMgr();
        re = um.Auth(username, pwd);
        if (re) {
            User user = um.getUser(username);
            user.setEnterCount(user.getEnterCount() + 1);
            user.setEnterLast();
            user.store();

            session.setAttribute(NAME, username);
            try {
                session.setAttribute(PWDMD5, SecurityUtil.MD5(pwd));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;

    }

    public void doLogin(HttpServletRequest request, String username, String pwdMD5) {
        HttpSession session = request.getSession(true);
        session.setAttribute(NAME, username);
        session.setAttribute(PWDMD5, pwdMD5);
    }

    public boolean login(HttpServletRequest request, String username,
                         String pwd) throws
            ErrMsgException {
        String ip = request.getRemoteAddr();
        IPMonitor ipm = new IPMonitor();
        boolean re = ipm.isIPCanAdmin(ip);
        if (!re) {
            throw new ErrMsgException("IP地址非法！");
        }
        UserMgr um = new UserMgr();
        re = um.Auth(username, pwd);
        if (re) {
            User user = um.getUser(username);
            user.setEnterCount(user.getEnterCount() + 1);
            user.setEnterLast();
            user.store();

            try {
                doLogin(request, username, SecurityUtil.MD5(pwd));
            } catch (Exception e) {
                logger.error("login:" + e.getMessage());
            }
        }
        return re;
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        session.invalidate();
    }

    public boolean isValid(HttpServletRequest request, String priv) {
        return isUserPrivValid(request, priv);
    }

}
