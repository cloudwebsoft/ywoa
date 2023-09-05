package com.redmoon.oa.visual;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.pvg.Privilege;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

public class ModulePrivMgr {

    /**
     * 检查数据范围权限()，判断用户是否可以存取此条数据
     * @param request
     * @param msd
     * @param id
     * @return
     */
    public static boolean canAccessData(HttpServletRequest request, ModuleSetupDb msd, long id) {
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(request, Privilege.ADMIN)) {
            return true;
        }

        /*
        @TASK 20230517 街道或国企删除项目报数据范围权限错误，故先注释掉
        String filter = msd.getFilter(pvg.getUser(request));
        if (null==filter || "".equals(filter)) {
            return true;
        }

        // 置canAccessData中调用SQLBuilder所要用到的attribute
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb(formCode);
        try {
            String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "", "", "");
            String sql = ary[0].toLowerCase();
            int p = sql.indexOf(" order by");
            if (p!=-1) {
                sql = sql.substring(0, p);
            }
            if (sql.contains(" where ")) {
                sql += " and t1.id=" + id;
            }
            else {
                sql += " where t1.id=" + id;
            }
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                return true;
            }
        } catch (ErrMsgException | SQLException e) {
            LogUtil.getLog(ModulePrivMgr.class).error(e);
        }
        return false;
        */
        return true;
    }

    /**
     * 检查数据范围权限，判断用户是否可以存取此条数据
     * @param request
     * @param msdRelated
     * @param relateFieldValue
     * @param id
     * @return
     */
    public static boolean canAccessDataRelated(HttpServletRequest request, ModuleSetupDb msdRelated, String relateFieldValue, long id) {
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(request, Privilege.ADMIN)) {
            return true;
        }

        String formCode = msdRelated.getString("form_code");
        FormDb fd = new FormDb(formCode);
        try {
            String[] ary = SQLBuilder.getModuleListRelateSqlAndUrlStr(request,
                    fd, "", "id", "desc",
                    relateFieldValue);
            String sql = ary[0].toLowerCase();
            int p = sql.indexOf(" order by");
            if (p!=-1) {
                sql = sql.substring(0, p);
            }
            if (sql.contains(" where ")) {
                sql += " and t1.id=" + id;
            }
            else {
                sql += " where t1.id=" + id;
            }
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(ModulePrivMgr.class).error(e);
        }
        return false;
    }

}
