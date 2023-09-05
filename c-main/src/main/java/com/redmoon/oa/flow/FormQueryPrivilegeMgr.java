package com.redmoon.oa.flow;

import cn.js.fan.util.*;
import javax.servlet.http.*;
import java.util.Vector;
import java.util.Iterator;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.Privilege;

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
public class FormQueryPrivilegeMgr {

    public FormQueryPrivilegeMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        FormQueryPrivilegeDb aqpd = new FormQueryPrivilegeDb();
        boolean re = false;

        String users = ParamUtil.get(request, "users");
        int queryId = ParamUtil.getInt(request, "id");
        int type = ParamUtil.getInt(request, "type");

        re = del(queryId, type);

        String[] userNameArr = users.split(",");
        int i = 0;
        while (i < userNameArr.length) {
            if (!userNameArr[i].equals("")) {
                aqpd.setQueryId(queryId);
                aqpd.setUserName(userNameArr[i]);
                aqpd.setType(type);
                re = aqpd.create();
            }
            i++;
        }
        return re;
    }

    public FormQueryPrivilegeDb getFormQueryPrivilegeDb(int id) {
        FormQueryPrivilegeDb aqpd = new FormQueryPrivilegeDb();
        return aqpd.getFormQueryPrivilegeDb(id);
    }

    public boolean del(int queryId, int type) throws ErrMsgException {
        boolean re = false;
        FormQueryPrivilegeDb apd = new FormQueryPrivilegeDb();
        Vector vt = apd.list(FormSQLBuilder.getQueryPrivilege(queryId, type));
        Iterator ir = null;
        ir = vt.iterator();
        while (ir != null && ir.hasNext()) {
            apd = (FormQueryPrivilegeDb) ir.next();
            re = apd.del();
        }
        return re;
    }

    /**
     * 判断用户是否有执行某查询的权限
     * @param request HttpServletRequest
     * @param queryId int
     * @return boolean
     */
    public boolean canUserQuery(HttpServletRequest request, int queryId) {
        Privilege privilege = new Privilege();
        if (privilege.isUserPrivValid(request, "admin"))
            return true;

        UserDb user = new UserDb();
        String userName = privilege.getUser(request);

        FormQueryDb aqd = new FormQueryDb();
        aqd = aqd.getFormQueryDb(queryId);
        if (userName.equals(aqd.getUserName()))
            return true;

        user = user.getUserDb(userName);
        // 判断查询是否直接授权给了该用户
        String sql =
                "select id from FORM_QUERY_PRIVILEGE where query_id=" +
                queryId + " and priv_type=" +
                FormQueryPrivilegeDb.TYPE_USER + " and user_name=" +
                StrUtil.sqlstr(userName);
        FormQueryPrivilegeDb aqpd = new FormQueryPrivilegeDb();
        if (aqpd.list(sql).size() > 0)
            return true;

        // 判断用户拥有的角色是否被授权
        RoleDb[] rgs = user.getRoles();
        int len = rgs.length;
        if (len > 0) {
            String roles = "";
            for (int i = 0; i < len; i++) {
                if (roles.equals("")) {
                    roles = StrUtil.sqlstr(rgs[i].getCode());
                } else
                    roles += "," + StrUtil.sqlstr(rgs[i].getCode());
            }

            sql = "select id from FORM_QUERY_PRIVILEGE where query_id=" +
                  queryId + " and priv_type=" +
                  FormQueryPrivilegeDb.TYPE_ROLE +
                  " and user_name in (" + roles + ")";

            if (aqpd.list(sql).size() > 0)
                return true;
        }

        // 判断用户所在的部门是否被授权
        String depts = "";
        DeptUserDb dud = new DeptUserDb();
        Iterator ir = dud.getDeptsOfUser(userName).iterator();
        while (ir.hasNext()) {
        	DeptDb dd = (DeptDb)ir.next();
        	if ("".equals(depts)) {
        		depts = StrUtil.sqlstr(dd.getCode());
        	}
        	else {
        		depts += "," + StrUtil.sqlstr(dd.getCode());
        	}
        }
        
        if ("".equals(depts)) {
        	return false; // 尚未被分配部门
        }
        
        sql =
                "select id from FORM_QUERY_PRIVILEGE where query_id=" +
                queryId + " and priv_type=" +
                FormQueryPrivilegeDb.TYPE_DEPT + " and user_name in (" +
                depts + ")";
        if (aqpd.list(sql).size() > 0)
            return true;
        return false;
    }

}
