package com.redmoon.oa.task;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.db.Conn;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import com.redmoon.oa.pvg.PrivDb;

public class TaskPrivilege {
    Logger logger = Logger.getLogger(TaskPrivilege.class.getName());

    Privilege privilege = null;
    public TaskPrivilege() {
        privilege = new Privilege();
    }

    /**
     * 能否编辑，只有任务发起人及总发起人才能编辑
     * @param request
     * @param taskid
     * @return
     */
    public boolean canEdit(HttpServletRequest request, int taskid) {
        if (privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN))
            return true;
        return isInitiator(request, taskid);
    }

    /**
     * 是否能汇报结果，只有任务承办人才可以汇报结果
     * @param request
     * @param taskid
     * @return
     */
    public boolean canAddResult(HttpServletRequest request, int taskid) {
        TaskDb td = new TaskDb();
        td = td.getTaskDb(taskid);
        if (td.getPerson().equals(privilege.getUser(request)))
            return true;
        else
            return false;
    }

    /**
     * 是否可以催办，只能由父任务的发起人催办
     * @param request
     * @param taskid
     * @return
     */
    public boolean canHurry(HttpServletRequest request, int taskid) {
        TaskDb td = new TaskDb();
        td = td.getTaskDb(taskid);
        return td.getInitiator().equals(privilege.getUser(request));
    }

    public boolean isInitiator(HttpServletRequest request, int taskid) {
        TaskDb td = new TaskDb();
        td = td.getTaskDb(taskid);
        return isInitiator(request, td);
    }

    public boolean isInitiator(HttpServletRequest request, TaskDb td) {
        boolean re = td.getInitiator().equals(privilege.getUser(request));
        if (re) {
            return true;
        }
        td = td.getTaskDb(td.getRootId());
        return td.getInitiator().equals(privilege.getUser(request));
    }

    /**
     * 能否改变任务状态
     * @param request HttpServletRequest
     * @param TaskDb td 可能为任务，也可能为子任务
     * @return boolean
     */
    public boolean canChangeStatus(HttpServletRequest request, TaskDb td) {
        if (privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN))
            return true;
        if (td.getType()==TaskDb.TYPE_TASK)
            return isInitiator(request, td.getId());
        else {
            if (td.getPerson().equals(privilege.getUser(request)))
                return true;
            else
                return false;
        }
    }

    /**
     * 是否能查看任务
     * @param request HttpServletRequest
     * @param td TaskDb
     * @return boolean
     */
    public boolean canUserSee(HttpServletRequest request, TaskDb td) {
        if (privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN))
            return true;
        if (!td.isSecret())
            return true;
        else {
            if (isInitiator(request, td)) {
                return true;
            }
            if (td.getPerson().equalsIgnoreCase(privilege.getUser(request)))
                return true;
            else
                return false;
        }
    }


}
