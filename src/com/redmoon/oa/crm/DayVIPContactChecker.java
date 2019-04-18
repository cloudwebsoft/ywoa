package com.redmoon.oa.crm;

import com.redmoon.oa.visual.IModuleChecker;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.flow.FormDb;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletResponse;

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
public class DayVIPContactChecker implements IModuleChecker {
    public DayVIPContactChecker() {
        super();
    }

    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
        Privilege pvg = new Privilege();
        int parentId = StrUtil.toInt(fdaoBeforeUpdate.getCwsId());
        FormDb fd = new FormDb();
        fd = fd.getFormDb("day_work_plan");
        FormDAO fdaoParent = new FormDAO();
        fdaoParent = fdaoParent.getFormDAO(parentId, fd);
        String curUser = fdaoParent.getFieldValue("cur_user");
        // System.out.println(getClass() + " parentId=" + parentId + " curUser=" + curUser);
        if (pvg.getUser(request).equals(curUser) || pvg.canAdminUser(request, curUser))
            ;
        else
            throw new ErrMsgException("权限非法：只有本人和部门管理者才可以修改！");

        java.util.Date mydate = DateUtil.parse(fdaoParent.getFieldValue("mydate"), "yyyy-MM-dd");

        // 检查是否已超过限制修改时间
        CRMConfig cfg = CRMConfig.getInstance();
        String dayWorkPlanModifyTime = cfg.getProperty("dayWorkPlanModifyTime");
        if (!dayWorkPlanModifyTime.equals("") && !pvg.canAdminUser(request, curUser)) {
            dayWorkPlanModifyTime += ":00";

            String t = fdaoParent.getFieldValue("mydate") + " " + dayWorkPlanModifyTime;
            java.util.Date dt = DateUtil.parse(t, "yyyy-MM-dd HH:mm:ss");

            if (DateUtil.compare(dt, new java.util.Date())==2) {
                throw new ErrMsgException("您只能在" + t + "之前进行编辑！");
            }
        }

        // 当is_visited为“是”的时候，当前时间已经不是计划中的那一天时，visit_date需为修改当天的日期，除非具有对该用户的管理权限
        Iterator ir = fields.iterator();
        String is_contact = "否";
        java.util.Date visitDate = null;
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals("is_contact")) {
                is_contact = ff.getValue();
            }
            else if (ff.getName().equals("contact_date")) {
                visitDate = DateUtil.parse(ff.getValue(), "yyyy-MM-dd");
            }
        }
        if (is_contact.equals("否") && visitDate!=null) {
            throw new ErrMsgException("当未拜访时，拜访日期不用填写！");
        }
        if (is_contact.equals("是") && visitDate==null) {
            throw new ErrMsgException("请填写拜访日期！");
        }

        if (is_contact.equals("是")) {
            // 比较，如果填写值与计划日期的值不一样
            if (DateUtil.compare(visitDate, mydate) != 0) {
                if (!pvg.canAdminUser(request, curUser)) {
                    java.util.Date curDate = DateUtil.parse(DateUtil.format(new java.
                            util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
                    if (DateUtil.compare(visitDate, curDate) != 0) {
                        throw new ErrMsgException("您的填写日期必须与当天日期一致，否则请联系您的部门管理员修改！");
                    }
                }
            }
        }
        return true;
    }

    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals("is_visited")) {
                if (ff.getValue().equals("是")) {
                    throw new ErrMsgException("是否拜访不能为“是”");
                }
            }
            else if (ff.getName().equals("visit_date")) {
                if (!ff.getValue().equals("")) {
                    throw new ErrMsgException("拜访时间必须为空！");
                }
            }
        }
        return true;
    }

    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        Privilege pvg = new Privilege();
        int parentId = StrUtil.toInt(fdao.getCwsId());
        FormDb fd = new FormDb();
        fd = fd.getFormDb("day_work_plan");

        FormDAO fdaoParent = new FormDAO();
        fdaoParent = fdaoParent.getFormDAO(parentId, fd);
        String curUser = fdaoParent.getFieldValue("cur_user");
        LogUtil.getLog(getClass()).info("curUser=" + curUser + " pvgUser=" + pvg.getUser(request));
        if (pvg.getUser(request).equals(curUser) ||
            pvg.canAdminUser(request, curUser))
            ;
        else
            throw new ErrMsgException("权限非法：只有本人和部门管理者才可以删除！");

        // 计划日期
        java.util.Date mydate = DateUtil.parse(fdaoParent.getFieldValue("mydate"), "yyyy-MM-dd");
        java.util.Date curDate = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
        if (DateUtil.compare(mydate, curDate)==2) {
            if (!pvg.canAdminUser(request, curUser)) {
                throw new ErrMsgException("您的计划已过期，请联系部门管理者删除！");
            }
        }

        return true;
    }

    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return false;
    }

    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }
}
