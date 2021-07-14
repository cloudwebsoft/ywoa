package com.redmoon.oa.workplan;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.base.AbstractModulePrivilege;
import com.redmoon.oa.pvg.PrivDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Privilege extends AbstractModulePrivilege {

    public Privilege() {
        CODE = PrivDb.PRIV_WORKPLAN;
    }
    
    /**
     * 判断是否为计划总管理员，如果是，则可以将计划置为未审核状态
     * @Description: 
     * @param request
     * @return
     */
    public boolean isWorkPlanMaster(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
    	
        if (pvg.isUserPrivValid(request, "admin"))
            return true;

        if (pvg.isUserPrivValid(request, "admin.workplan"))
            return true;
        // 预留 20130413 fgf
        if (pvg.isUserPrivValid(request, "workplan.admin"))
            return true;
        
        return false;
    }

    /**
     * 判断是否有管理计划的权限，只有负责人才能修改计划及进度
     * @param request
     * @param id
     * @return
     */
    public boolean canUserManageWorkPlan(HttpServletRequest request, int id) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (pvg.isUserPrivValid(request, "admin"))
            return true;

        if (pvg.isUserPrivValid(request, "admin.workplan"))
            return true;
        // 预留 20130413 fgf
        if (pvg.isUserPrivValid(request, "workplan.admin"))
            return true;

        WorkPlanDb wpd = new WorkPlanDb();
        wpd = wpd.getWorkPlanDb(id);
        // 检查用户是否有发起工作计划的权限
        // if (!isUserPrivValid(request, CODE))
        //    return false;
        String userName = getUser(request);
        // 检查用户是否是计划的撰写者
        // if (wpd.getAuthor().equals(userName))
        //     return true;

        // 检查用户是否是计划的负责人
        // String[] managers = wpd.getPrincipal().split(",");
        String[] managers = wpd.getPrincipals();
        int len = managers.length;
        for (int i = 0; i < len; i++) {
            if (managers[i].equals(userName))
                return true;
        }
        return false;
    }

    public boolean canUserSeeWorkPlan(HttpServletRequest request, int id) {
        if (canUserManageWorkPlan(request, id))
            return true;
        WorkPlanDb wpd = new WorkPlanDb();
        wpd = wpd.getWorkPlanDb(id);
        String userName = getUser(request);
        // 检查用户是否是计划的参与者
        String[] users = wpd.getUsers();
        int len = users.length;
        for (int i = 0; i < len; i++) {
            if (users[i].equals(userName))
                return true;
        }

        String[] managers = wpd.getPrincipals();
        len = managers.length;
        for (int i = 0; i < len; i++) {
            if (managers[i].equals(userName))
                return true;
        }
        // System.out.println("Privilege.java canUserSeeWorkPlan:" + id);
        return false;
    }
}
