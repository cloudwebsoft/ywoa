package com.redmoon.oa.workplan;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

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
public class WorkPlanAnnexMgr {
    FileUpload fileUpload;

    public WorkPlanAnnexMgr() {
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        long annexId = ParamUtil.getLong(request, "annexId");
        WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
        wad = (WorkPlanAnnexDb) wad.getQObjectDb(new Long(annexId));

        com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
        if (pvg.canUserManageWorkPlan(request, wad.getInt("workplan_id")))
            // || pvg.getUser(request).equals(wad.getString("user_name")))
            ;
        else {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        boolean re = wad.del();
        if (re) {
            WorkPlanTaskDb wptd = new WorkPlanTaskDb();
            wptd = (WorkPlanTaskDb)wptd.getQObjectDb(wad.getInt("task_id"));
            // 恢复成原来的progress
            wptd.changeProgress(wad.getInt("old_progress"));
        }
        return re;
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws
            ErrMsgException {
        int workplanId = ParamUtil.getInt(request, "id");
        WorkPlanDb wpd = new WorkPlanDb();
        wpd = wpd.getWorkPlanDb(workplanId);

        com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
        if (!pvg.canUserManageWorkPlan(request, workplanId) && !pvg.getUser(request).equals(wpd.getAuthor())) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        fileUpload = new FileUpload();
        Config cfg = new Config();
        String exts = cfg.get("workplan_ext");
        String[] extAry = StrUtil.split(exts, ",");
        fileUpload.setValidExtname(extAry);
        fileUpload.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K

        int ret = -1;
        try {
            ret = fileUpload.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }
        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fileUpload.getErrMessage(request));
        }

        boolean re = false;

        WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
        String formCode = "workplan_annex_create";
        ParamConfig pc = new ParamConfig(wad.getTable().
                                         getFormValidatorFile());
        // System.out.println(getClass() + " validateFile=" + wad.getTable().
        //                                 getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request, fileUpload);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

       // 检查是否有重复的周报或月报
       int annexType = pck.getInt("annex_type");
       if (annexType!=0) {
           int annexYear = pck.getInt("annex_year");
           int annexItem = pck.getInt("annex_item");
           WorkPlanAnnexDb wpa = wad.getWorkPlanAnnexDb((int)pck.getLong("workplan_id"), annexYear, annexType, annexItem);
           if (wpa!=null) {
               throw new ErrMsgException("该项已存在！");
           }
       }

        try {
/*            if (pck.getInt("progress") == pck.getInt("old_progress"))
                pck.setValue("check_status", "", WorkPlanAnnexDb.CHECK_STATUS_NONE);*/
            // 20180108 fgf 未避免操作复杂，取消审核状态
            pck.setValue("check_status", "审核状态", WorkPlanAnnexDb.CHECK_STATUS_PASSED);

            WorkPlanTaskDb wptd = new WorkPlanTaskDb();
            wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(pck
                                                               .getLong("task_id")));

            JdbcTemplate jt = new JdbcTemplate();
            re = wad.create(jt, pck);
            if (re) {
                wad.writeAttachment(request, fileUpload, wad.getLong("id"));

                if (pck.getInt("check_status") == WorkPlanAnnexDb.CHECK_STATUS_PASSED) {
                    // LogUtil.getLog(getClass()).info("progress=" + pck.getInt("progress") + "--oldprogress=" + pck.getInt("old_progress"));
                    if (pck.getInt("progress") != pck.getInt("old_progress")) {
                        wptd.changeProgress(pck.getInt("progress"));
                    }
                }

               boolean isToMobile = SMSFactory.isUseSMS();
               IMessage imsg = null;
               String t = SkinUtil.LoadString(request,
                                              "res.module.workplan",
                                              "msg_workplan_annex_create_title");
               UserMgr um = new UserMgr();
               UserDb user = um.getUserDb(pvg.getUser(request));

               t = t.replaceFirst("\\$title", wptd.getString("name"));
               t = t.replaceFirst("\\$user", user.getRealName());
               String c = SkinUtil.LoadString(request,
                                              "res.module.workplan",
                                              "msg_workplan_annex_create_content");
               c = c.replaceFirst("\\$content", StrUtil.getAbstract(request, pck.getString("content"), 500));

               String[] principals = wpd.getPrincipals();

               if (isToMobile) {
                   ProxyFactory proxyFactory = new ProxyFactory(
                           "com.redmoon.oa.message.MessageDb");
                   imsg = (IMessage) proxyFactory.getProxy();
                   IMsgUtil imu = SMSFactory.getMsgUtil();
                   int len = principals.length;
                   for (int i = 0; i < len; i++) {
                       imsg.sendSysMsg(principals[i], t, c);
                       UserDb ud = um.getUserDb(principals[i]);
                       imu.send(ud, t, MessageDb.SENDER_SYSTEM);
                   }
               }
               else {
                   // 发送信息
                   MessageDb md = new MessageDb();
                   int len = principals.length;
                   String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
                   for (int i = 0; i < len; i++) {
                       md.sendSysMsg(principals[i], t, c, action);
                   }
               }
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(ServletContext application, HttpServletRequest request
                        ) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        fileUpload = new FileUpload();
        Config cfg = new Config();
        String exts = cfg.get("workplan_ext");
        String[] extAry = StrUtil.split(exts, ",");
        fileUpload.setValidExtname(extAry);
        fileUpload.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K

        int ret = -1;
        try {
            ret = fileUpload.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fileUpload.getErrMessage(request));
        }

        WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
        String formCode = "workplan_annex_save";

        ParamConfig pc = new ParamConfig(wad.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fileUpload);

        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        boolean re = false;

        long id = pck.getLong("id");
        wad = (WorkPlanAnnexDb)wad.getQObjectDb(new Long(id));
        int progress = wad.getInt("progress");

        String appraise = "";
        if (wad.getString("appraise") != null)
            appraise = wad.getString("appraise");
        pck.setValue("appraise", "评价", appraise);
        pck.setValue("checker", "审核者", StrUtil.getNullStr(wad.getString("checker")));
        pck.setValue("check_date", "审核时间", wad.getDate("check_date"));
        // 20180108 fgf 未避免操作复杂，取消审核状态
        pck.setValue("check_status", "审核状态", WorkPlanAnnexDb.CHECK_STATUS_PASSED);

        com.redmoon.oa.workplan.Privilege pvg = new com.redmoon.oa.workplan.Privilege();
        // 如果是进度
        if (pck.getInt("progress")!=0 && !pvg.canUserManageWorkPlan(request, wad.getInt("workplan_id"))) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = wad.save(jt, pck);
            if (re) {
                if (pck.getInt("check_status")==1) {
					if (pck.getInt("progress") != progress) {
		                WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		                wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wad.getLong("task_id")));
		                wptd.changeProgress(pck.getInt("progress"));
                	}
                }

                if (fileUpload.getFiles().size()>0) {
                    wad.writeAttachment(request, fileUpload, wad.getLong("id"));
                }

                /*
                WorkPlanDb wpd = new WorkPlanDb();
                wpd = wpd.getWorkPlanDb(wad.getInt("workplan_id"));
                wpd.setProgress(wad.getToalProgressFromDb());
                wpd.save();
                */

               // 因为只有计划负责人才能修改汇报的进展，所以不需要再发送消息
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }
}
