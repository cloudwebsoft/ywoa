package cn.js.fan.module.pvg;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.security.SecurityUtil;
import org.apache.log4j.Logger;
import cn.js.fan.util.ParamUtil;

public class UserMgr {
    Logger logger = Logger.getLogger(UserMgr.class.getName());

    public UserMgr() {
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        if (!request.getMethod().equals("POST"))
            throw new ErrMsgException("Please use post method!");
        UserCheck uc = new UserCheck();
        uc.checkAdd(request);

        User user = new User();
        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(uc.getPwd());
        }
        catch (Exception e) {
            logger.error("add: " + e.getMessage());
        }

        // 如果为前台用户，则检查用户是否存在
        if (uc.isForegroundUser()) {
            com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
            ud = ud.getUserDbByNick(uc.getName());
            if (ud==null || !ud.isLoaded()) {
                throw new ErrMsgException("用户" + uc.getName() + "在前台不存在！");
            }
        }

        return user.insert(uc.getName(), uc.getRealName(), uc.getDesc(), pwdMD5, uc.isForegroundUser());
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        UserCheck uc = new UserCheck();
        uc.checkDel(request);

        User user = new User();
        return user.del(uc.getName());
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        String pwd = ParamUtil.get(request, "pwd");

        UserCheck uc = new UserCheck();

        if (!pwd.equals(""))
            uc.checkUpdateWithPwd(request);
        else
            uc.checkUpdate(request);

        User user = new User(uc.getName());
        user.setRealName(uc.getRealName());
        user.setDesc(uc.getDesc());
        user.setForegroundUser(uc.isForegroundUser());

        // 如果为前台用户，则检查用户是否存在
        if (uc.isForegroundUser()) {
            com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
            ud = ud.getUserDbByNick(uc.getName());
            if (ud==null || !ud.isLoaded()) {
                throw new ErrMsgException("用户" + uc.getName() + "在前台不存在！");
            }
        }

        if (!pwd.equals("")) {
            String str = "";
            try {
                str = SecurityUtil.MD5(uc.getPwd());
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
            user.setPwdMD5(str);
            return user.storeWithPwd();
        }
        else
            return user.store();
    }

    public User getUser(String name) {
        return new User(name);
    }

    public boolean Auth(String name, String pwd) {
        User user = new User();
        return user.Auth(name, pwd);
    }

}
