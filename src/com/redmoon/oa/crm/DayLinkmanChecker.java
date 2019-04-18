package com.redmoon.oa.crm;

import com.redmoon.oa.visual.IModuleChecker;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sale.SalePrivilege;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletResponse;
import com.redmoon.oa.person.PlanDb;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 工作计划中联系人的有效性验证</p>
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
public class DayLinkmanChecker implements IModuleChecker {
    public DayLinkmanChecker() {
        super();
    }

    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
        // System.out.println(getClass() + " validateUpdate");

        Privilege pvg = new Privilege();

        //long visitId = fdaoBeforeUpdate.getId();

        long linkmanId = StrUtil.toLong(fdaoBeforeUpdate.getFieldValue("lxr"));
        FormDb fdLinkman = new FormDb();
        fdLinkman = fdLinkman.getFormDb("sales_linkman");

        com.redmoon.oa.visual.FormDAO fdaoLinkman = new com.redmoon.oa.visual.FormDAO();
        fdaoLinkman = fdaoLinkman.getFormDAO(linkmanId, fdLinkman);

        String creator = fdaoBeforeUpdate.getCreator();
        if (!creator.equals(pvg.getUser(request))) {
            long customerId = StrUtil.toLong(fdaoLinkman.getFieldValue("customer"));
            if (!SalePrivilege.canUserManageCustomer(request, customerId)) {
                throw new ErrMsgException("权限非法：只有行动创建者、客户对应的销售员本人、部门管理者和销售总管理才能编辑！");
            }
        }

        if (true)
            return true;

        /*
        int parentId = StrUtil.toInt(fdaoBeforeUpdate.getCwsId());
        FormDb fd = new FormDb();
        fd = fd.getFormDb("day_work_plan");
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(parentId, fd);
        String curUser = fdao.getFieldValue("cur_user");
        // System.out.println(getClass() + " parentId=" + parentId + " curUser=" + curUser);
        java.util.Date mydate = DateUtil.parse(fdao.getFieldValue("mydate"), "yyyy-MM-dd");

        */

        if (pvg.getUser(request).equals(creator) || pvg.canAdminUser(request, creator))
            ;
        else
            throw new ErrMsgException("权限非法：只有本人和部门管理者才可以修改！");


        // 检查是否已超过限制修改时间
        /*
        CRMConfig cfg = CRMConfig.getInstance();
        String dayWorkPlanModifyTime = cfg.getProperty("dayWorkPlanModifyTime");
        if (!dayWorkPlanModifyTime.equals("") && !pvg.canAdminUser(request, curUser)) {
            dayWorkPlanModifyTime += ":00";

            String t = fdao.getFieldValue("mydate") + " " + dayWorkPlanModifyTime;
            java.util.Date dt = DateUtil.parse(t, "yyyy-MM-dd HH:mm:ss");

            if (DateUtil.compare(dt, new java.util.Date())==2) {
                throw new ErrMsgException("您只能在" + t + "之前进行编辑！");
            }
        }
        */

        // 当is_visited为“是”的时候，当前时间已经不是计划中的那一天时，visit_date需为修改当天的日期，除非具有对该用户的管理权限
        Iterator ir = fields.iterator();
        String isVisited = "否";
        java.util.Date visitDate = null;
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals("is_visited")) {
                isVisited = ff.getValue();
            }
            else if (ff.getName().equals("visit_date")) {
                visitDate = DateUtil.parse(ff.getValue(), "yyyy-MM-dd");
            }
        }
        if (isVisited.equals("否") && visitDate!=null) {
            throw new ErrMsgException("当未拜访时，拜访日期不用填写！");
        }
        /*
        if (isVisited.equals("是")) {
            // 比较，如果填写值与计划日期的值不一样
            if (DateUtil.compare(visitDate, mydate) != 0) {
                if (!pvg.canAdminUser(request, curUser)) {
                    java.util.Date curDate = DateUtil.parse(DateUtil.format(new java.
                            util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");

                    if (DateUtil.compare(visitDate, curDate) != 0) {
                        throw new ErrMsgException(
                                "填写日期必须与当天日期一致，否则请联系您的部门管理员修改！");
                    }
                }
            }
        }
        */
        return true;
    }
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {
        if (true)
            return true;

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
        // long visitId = fdao.getId();

        long linkmanId = StrUtil.toLong(fdao.getFieldValue("lxr"));
        FormDb fdLinkman = new FormDb();
        fdLinkman = fdLinkman.getFormDb("sales_linkman");

        com.redmoon.oa.visual.FormDAO fdaoLinkman = new com.redmoon.oa.visual.
                                                    FormDAO();
        fdaoLinkman = fdaoLinkman.getFormDAO(linkmanId, fdLinkman);

        long customerId = StrUtil.toLong(fdaoLinkman.getFieldValue("customer"));
        if (!SalePrivilege.canUserDel(request, customerId)) {
            throw new ErrMsgException("权限非法：只有部门管理者和销售总管理才能删除！");
        }

        if (true)
            return true;

        Privilege pvg = new Privilege();
        int parentId = StrUtil.toInt(fdao.getCwsId());
        FormDb fd = new FormDb();
        fd = fd.getFormDb("day_work_plan");

        FormDAO fdaoParent = new FormDAO();
        fdaoParent = fdaoParent.getFormDAO(parentId, fd);
        String curUser = fdaoParent.getFieldValue("cur_user");
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
        fdao.setCwsId(fdao.getFieldValue("customer"));
        fdao.save();

        LogUtil.getLog(getClass()).info("onCreate:" + fdao.getFieldValue("next_visit_time"));

        java.util.Date nextVisitDate = DateUtil.parse(fdao.getFieldValue("next_visit_time"), "yyyy-MM-dd HH:mm:ss");
        if (nextVisitDate==null)
            return true;

        long lxr = StrUtil.toLong(fdao.getFieldValue("lxr"));
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_linkman");
        FormDAO fdaoLinkman = new FormDAO();
        fdaoLinkman = fdaoLinkman.getFormDAO(lxr, fd);
        String linkmanName = fdaoLinkman.getFieldValue("linkmanName");

        long customerId = StrUtil.toInt(fdaoLinkman.getFieldValue("customer"));

        fd = fd.getFormDb("sales_customer");
        FormDAO fdaoCust = new FormDAO();
        fdaoCust = fdaoCust.getFormDAO(customerId, fd);
        String customerName = fdaoCust.getFieldValue("customer");
    	// 置客户表中的最后一次访问时间
    	fdaoCust.setFieldValue("last_visit_date", fdao.getFieldValue("visit_date"));
    	fdaoCust.save();        

    	// 在日程安排中写入记录
        PlanDb pd = new PlanDb();
        pd.setTitle("联系" + customerName + "的" + linkmanName);
        pd.setContent(customerName + "\r\n" + fdao.getFieldValue("next_visit_todo") + "\r\n提示：您上次联系的时间是：" + fdao.getFieldValue("visit_date"));
        pd.setMyDate(nextVisitDate);
        pd.setZdrq(new java.util.Date());
        pd.setActionType(PlanDb.ACTION_TYPE_SALES_VISIT);
        pd.setActionData("" + fdao.getId());

        Privilege privilege = new Privilege();
        pd.setUserName(privilege.getUser(request));

        int remindBefore = StrUtil.toInt(fdao.getFieldValue("remind_before"), 0);
        if (remindBefore==0)
            pd.setRemind(false);
        else {
            pd.setRemind(true);
            pd.setRemindBySMS(true);
            java.util.Date dt = DateUtil.addMinuteDate(nextVisitDate, -remindBefore);
            pd.setRemindDate(dt);
        }
    	return pd.create();
    }

    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
        return false;
    }

    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	// 如果联系时间已填，则将是否已联系字段置为“是”，以便于计划的对照
        String visit_date = StrUtil.getNullStr(fdao.getFieldValue("visit_date"));
        if (!"".equals(visit_date)) {
        	fdao.setFieldValue("is_visited", "是");
        	fdao.save();
        }
                
        String sign = fdao.getFieldValue("sign"); // 行动标志
        
        // 根据意向阶段写入赢率及客户阶段
        String formCode = "sales_stage_ratio";
        FormDAO fdaoStage = new FormDAO();
        String sql = "select id from " + FormDb.getTableName(formCode);
        Iterator ir = fdaoStage.list(formCode, sql).iterator();
        while (ir.hasNext()) {
        	fdaoStage = (FormDAO)ir.next();
        	String signStage = fdaoStage.getFieldValue("sign");
        	if (signStage.equals(sign)) {
        		String winRatio = fdaoStage.getFieldValue("win_ratio");
        		String custStage = fdaoStage.getFieldValue("customer_stage");
        		String intentStage = fdaoStage.getFieldValue("intent_stage"); // 意向阶段
        		
        		// 置客户阶段
        		String customer = fdao.getFieldValue("customer");
        		long customerId = StrUtil.toLong(customer, -1);
        		FormDAO fdaoCust = new FormDAO();
        		FormDb fdCust = new FormDb();
        		fdCust = fdCust.getFormDb("sales_customer");
        		fdaoCust = fdaoCust.getFormDAO(customerId, fdCust);
        		fdaoCust.setFieldValue("customer_type", custStage);
        		fdaoCust.save();
        		
        		// 找出对应的商机，置商机的意向阶段、赢率
        		// 这里找到的商机只是按生成时间排在最后的商机
        		sql = "select id from form_table_sales_chance where cws_id=" + customerId + " order by id desc";
        		FormDAO fdaoChance = new FormDAO();
        		ListResult lr = fdaoChance.listResult("sales_chance", sql, 1, 1);
        		Iterator irChange = lr.getResult().iterator();
        		if (irChange.hasNext()) {
        			fdaoChance = (FormDAO)irChange.next();
        			fdaoChance.setFieldValue("state", intentStage);
        			fdaoChance.setFieldValue("possibility", winRatio);
        			fdaoChance.save();
        		}
        		break;
        	}
        	
        }
        
    	return true;
    }
}
