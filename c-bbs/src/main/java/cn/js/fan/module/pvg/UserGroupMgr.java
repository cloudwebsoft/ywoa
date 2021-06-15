package cn.js.fan.module.pvg;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;

public class UserGroupMgr {
    Logger logger = Logger.getLogger(UserGroupMgr.class.getName());
    String connname;

    public UserGroupMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Priv:connname is empty.");
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        UserGroupCheck ugc = new UserGroupCheck();
        ugc.checkAdd(request);

        UserGroup ug = new UserGroup();
        return ug.insert(ugc.getCode(), ugc.getDesc());
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        UserGroupCheck ugc = new UserGroupCheck();
        ugc.checkUpdate(request);

        UserGroup ug = new UserGroup();
        ug.setCode(ugc.getCode());
        ug.setDesc(ugc.getDesc());
        return ug.save();
    }

    public UserGroup getUserGroup(String code) {
        return new UserGroup(code);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        UserGroupCheck ugc = new UserGroupCheck();
        ugc.checkDel(request);

        UserGroup ug = getUserGroup(ugc.getCode());
        logger.info("del:" + ug.getDesc());
        return ug.del();
    }

    public UserGroup[] getAllUserGroup() {
        String sql = "select code from user_group";
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        UserGroup[] ug = null;
        try {
            ri = rmconn.executeQuery(sql);
            int count = ri.size();
            if (count>0)
                ug = new UserGroup[count];
            if (ri != null) {
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    String code = rr.getString(1);
                    ug[i] = getUserGroup(code);
                    i++;
                }
            }
        }
        catch (SQLException e) {
            logger.error("getAllUserGroup:" + e.getMessage());
        }
        return ug;
    }
}
