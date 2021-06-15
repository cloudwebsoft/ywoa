package com.redmoon.oa.workplan;

import com.redmoon.oa.basic.SelectMgr;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.ParamConfig;
import com.redmoon.oa.oacalendar.OACalendarDb;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamChecker;
import com.redmoon.oa.basic.SelectDb;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.CheckErrException;
import org.json.JSONException;
import java.util.Iterator;
import cn.js.fan.util.ParamUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkPlanTaskUserMgr {
    public WorkPlanTaskUserMgr() {
    }


    /**
     * 创建责任人
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean create(HttpServletRequest request) throws ErrMsgException {
        long taskId = ParamUtil.getLong(request, "task_id");
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));

        Privilege pvg = new Privilege();
        if (!pvg.canUserManageWorkPlan(request, wptd.getInt("work_plan_id")))
            throw new ErrMsgException("权限非法！");

        boolean re = false;
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();

        // 判断是否已存在
        if (wptud.isTaskUserExist(taskId, ParamUtil.get(request, "user_name")))
            throw new ErrMsgException("用户已存在于该任务中！");

        ParamConfig pc = new ParamConfig(wptud.getTable().getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule("workplan_task_user_create"));

            String mode = ParamUtil.get(request, "mode");
            if (mode.equals("byPercent")) {
                int percent = ParamUtil.getInt(request, "percent");
                double duration = percent * wptd.getDouble("duration") / 100;
                pck.setValue("duration", "工作日", new Double(duration));
            }
            else {
                double duration = ParamUtil.getDouble(request, "duration");
                int percent = (int)((duration / wptd.getDouble("duration")) * 100);
                pck.setValue("percent", "使用率", new Integer(percent));
            }
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            re = wptud.create(pck);
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }

        return re;
    }

    /**
     * 删除责任人
     * @param request
     * @param taskId
     * @return
     * @throws ErrMsgException
     */
    public boolean del(HttpServletRequest request) throws ErrMsgException {
        long id = ParamUtil.getLong(request, "id");
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
        wptud = (WorkPlanTaskUserDb) wptud.getQObjectDb(new Long(id));

        long taskId = wptud.getLong("task_id");
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));

        Privilege pvg = new Privilege();
        if (!pvg.canUserManageWorkPlan(request, wptd.getInt("work_plan_id")))
            throw new ErrMsgException("权限非法！");

        boolean re = false;
        try {
            re = wptud.del();
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage(request));
        }

        if (re) {
            // 如果不是根节点，则需要将位于其后的用户的orders - 1
            String sql = "select id from " + wptud.getTable().getName() + " where task_id=? and orders>" +
                         wptud.getInt("orders");
            Iterator ir = wptd.list(sql, new Object[] {new Integer(wptud.getInt("task_id"))}).iterator();
            while (ir.hasNext()) {
                wptud = (WorkPlanTaskUserDb) ir.next();
                wptud.set("orders", new Integer(wptd.getInt("orders") - 1));
                try {
                    wptud.save();
                } catch (ResKeyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return re;
    }

    /**
     * 编辑任务责任人
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean edit(HttpServletRequest request) throws ErrMsgException {
        long id = ParamUtil.getLong(request, "id");
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
        wptud = (WorkPlanTaskUserDb) wptud.getQObjectDb(new Long(id));

        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(wptud.getLong("task_id")));

        Privilege pvg = new Privilege();
        if (!pvg.canUserManageWorkPlan(request, wptd.getInt("work_plan_id")))
            throw new ErrMsgException("权限非法！");

        boolean re = false;
        ParamConfig pc = new ParamConfig(wptd.getTable().getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request);

        try {
            pck.doCheck(pc.getFormRule("workplan_task_user_edit"));

            String mode = ParamUtil.get(request, "mode");
            if (mode.equals("byPercent")) {
                int percent = ParamUtil.getInt(request, "percent");
                double duration = percent * wptd.getDouble("duration") / 100;
                pck.setValue("duration", "工作日", new Double(duration));
            }
            else {
                double duration = ParamUtil.getDouble(request, "duration");
                int percent = (int)((duration / wptd.getDouble("duration")) * 100);
                pck.setValue("percent", "使用率", new Integer(percent));
            }
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            re = wptud.save(pck);

        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }

        return re;
    }

    /**
     * 上移或下移
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean move(HttpServletRequest request) throws ErrMsgException {
        long id = ParamUtil.getLong(request, "id");
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
        wptud = (WorkPlanTaskUserDb) wptud.getQObjectDb(new Long(id));

        boolean isUp = ParamUtil.get(request, "direction").equals("true");

        int orders = wptud.getInt("orders");

        // 如果level与上一个或下一个节点不相等，则不允许移动
        if (isUp) {
            // 如果orders为0，则不允许上移
            if (orders == 0)
                throw new ErrMsgException("不能上移！");

            WorkPlanTaskUserDb taskUser = wptud.getTaskUserByOrders(wptud.getInt("task_id"), orders - 1);
            if (taskUser == null)
                throw new ErrMsgException("不能上移！");

            try {
                taskUser.set("orders", new Integer(orders));
                taskUser.save();

                wptud.set("orders", orders - 1);
                wptud.save();
            } catch (ResKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new ErrMsgException(e.getMessage(request));
            }

        } else {
            // 如果没有下一节点，则不允许下移
            WorkPlanTaskUserDb taskUser = wptud.getTaskUserByOrders(wptud.getInt("task_id"), orders + 1);
            if (taskUser == null)
                throw new ErrMsgException("不能下移！");

            try {
                taskUser.set("orders", new Integer(orders));
                taskUser.save();

                wptud.set("orders", orders + 1);
                wptud.save();
            } catch (ResKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        return true;
    }
}
