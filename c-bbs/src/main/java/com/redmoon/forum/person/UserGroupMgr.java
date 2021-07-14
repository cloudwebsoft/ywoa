package com.redmoon.forum.person;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.IPUtil;

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

        UserGroupDb ug = new UserGroupDb();
        ug.setCode(ugc.getCode());
        ug.setDesc(ugc.getDesc());
        ug.setDisplayOrder(ugc.getDisplayOrder());
        ug.setIpBegin(IPUtil.ip2long(ugc.getIpBegin()));
        ug.setIpEnd(IPUtil.ip2long(ugc.getIpEnd()));
        ug.setGuest(ugc.isGuest());
        boolean re = false;
        try {
            re = ug.create(new JdbcTemplate());
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        UserGroupCheck ugc = new UserGroupCheck();
        ugc.checkUpdate(request);

        UserGroupDb ug = new UserGroupDb();
        ug.setCode(ugc.getCode());
        ug.setDesc(ugc.getDesc());
        ug.setDisplayOrder(ugc.getDisplayOrder());
        ug.setIpBegin(IPUtil.ip2long(ugc.getIpBegin()));
        ug.setIpEnd(IPUtil.ip2long(ugc.getIpEnd()));
        ug.setGuest(ugc.isGuest());
        boolean re = false;
        try {
            re = ug.save(new JdbcTemplate());
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public UserGroupDb getUserGroupDb(String code) {
        UserGroupDb ugd = new UserGroupDb();
        return ugd.getUserGroupDb(code);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        UserGroupCheck ugc = new UserGroupCheck();
        ugc.checkDel(request);

        UserGroupDb ug = getUserGroupDb(ugc.getCode());
        boolean re = false;
        try {
            re = ug.del(new JdbcTemplate());
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public UserGroupDb[] getAllUserGroup() {
        String sql = "select code from sq_user_group";
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        UserGroupDb[] ug = null;
        try {
            ri = rmconn.executeQuery(sql);
            int count = ri.size();
            if (count>0)
                ug = new UserGroupDb[count];
            if (ri != null) {
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    String code = rr.getString(1);
                    ug[i] = getUserGroupDb(code);
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
