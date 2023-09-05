package com.redmoon.oa.flow;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
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
public class WorkflowRuler {
    String errMsg = "";

    public WorkflowRuler() {
    }

    public boolean canMonitor(HttpServletRequest request, WorkflowDb wfd) {
        // 检查用户是否有权限，当用户为监控者或者为发起人时能够添加删除监控人员
        Privilege pvg = new Privilege();

        String curUserName = pvg.getUser(request);
        if (curUserName.equals(wfd.getUserName()))
            return true;
        if (wfd.isMonitor(curUserName))
            return true;
        return false;
    }

    /**
     * 在出现节点所设用户的同时，能否选择用户
     * @param request
     * @param wa
     * @return
     */
    public boolean canUserSelUser(HttpServletRequest request,
            WorkflowActionDb wa) {
        String flag = wa.getFlag();
        int len = flag.length();
        if (len >= 2) {
            if ("0".equals(flag.substring(1, 2))) {
                return false;
            }
        }
        return true;
    }

    public boolean canUserOpenActionPropertyDialog(HttpServletRequest request,
            WorkflowActionDb wa) {
        String flag = wa.getFlag();
        int len = flag.length();
        if (len>=1) {
            if (flag.substring(0, 1).equals("0")) {
                return false;
            }
        }
        return true;
    }

    public boolean canUserStartFlow(HttpServletRequest request, WorkflowDb wfd) {
        if (wfd.getStatus()==wfd.STATUS_DISCARDED)
            return false;
        // 检查用户是否为流程的发起者或者具有流程归档的权限
        /*
        // 前加签，也可能会发起流程
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        boolean re = isUserFlowStarter(userName, wfd);
        if (!re) {
            errMsg = userName + "不是流程的发起者！";
            return false;
        }
        */
        if (wfd.isStarted()) {
            errMsg = "流程已开始！";
            return false;
        }
        return true;
    }

    /**
     * 检查用户是否为流程的发起者
     * @param userName String
     * @param wd WorkflowDb
     * @return boolean
     */
    public boolean isUserFlowStarter(String userName, WorkflowDb wd) {
        if (wd.getUserName().equals(userName))
            return true;
        else
            return false;
    }

    public boolean canUserModifyFlow(HttpServletRequest request, WorkflowDb wfd) {
        if (wfd.getStatus()==wfd.STATUS_DISCARDED)
            return false;
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        if (canMonitor(request, wfd))
            return true;

        errMsg = userName + SkinUtil.LoadString(request, "pvg_invalid");
        return false;
        // 如果流程已开始，则不能修改
        // if (wd.isStarted()) {
        //     errMsg = "流程已开始，不能被修改";
        //     return false;
        //}
    }

    /**
     * 判断用户是否能催办
     * @return boolean
     */
    public boolean canUserHurry(HttpServletRequest request, WorkflowDb wfd) {
        if (wfd.getStatus()==wfd.STATUS_DISCARDED)
            return false;
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        if (!isUserFlowStarter(userName, wfd)) {
            errMsg = userName + "不是流程的发起者！";
            return false;
        }
        if (wfd.isStarted()) {
            if (canMonitor(request, wfd))
                return true;

            errMsg = userName + SkinUtil.LoadString(request, "pvg_invalid");
        }
        return false;
    }

    public boolean canUserDelFlow(HttpServletRequest request, WorkflowDb wfd) {
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        if (canMonitor(request, wfd))
            return true;
        errMsg = userName + "不是流程的发起者！";

        return false;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
