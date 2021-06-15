package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.workplan.WorkPlanAnnexAttachment;
import com.redmoon.oa.workplan.WorkPlanAnnexDb;
import com.redmoon.oa.workplan.WorkPlanDb;
import com.redmoon.oa.workplan.WorkPlanTaskDb;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/public/workplan")
public class WorkplanController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 我参与的计划
     * @param skey
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listMine", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
    public String listMine(String skey) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        if(!re){
            try {
                json.put("res","-2");
                json.put("msg","权限非法");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            String op = ParamUtil.get(request, "op");
            String cond = ParamUtil.get(request, "cond");
            String what = request.getParameter("what");
            what = StrUtil.UnicodeToUTF8(what);
            WorkPlanDb wpd = new WorkPlanDb();
            String sql = "select distinct p.id from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(privilege.getUserName());

            if (op.equals("search")) {
                if (cond.equals("title")) {
                    sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
                }
            }

            sql += " order by id desc";

            int curpage = ParamUtil.getInt(request, "pagenum", 1);
            int pagesize = ParamUtil.getInt(request, "pagesize", 20);
            ListResult lr = wpd.listResult(sql, curpage, pagesize);
            Vector vt = lr.getResult();
            Iterator ri = vt.iterator();

            json.put("res","0");
            json.put("msg","操作成功");
            json.put("total",String.valueOf(lr.getTotal()));

            JSONObject result = new JSONObject();
            result.put("count",String.valueOf(pagesize));

            UserDb user = new UserDb();
            JSONArray workplans = new JSONArray();

            while (ri.hasNext()) {
                wpd = (WorkPlanDb) ri.next();
                JSONObject workPlan = new JSONObject();
                workPlan.put("id", String.valueOf(wpd.getId()));
                workPlan.put("title", wpd.getTitle());
                workPlan.put("realName", user.getUserDb(wpd.getAuthor()).getRealName());
                workPlan.put("progress", wpd.getProgress());
                workPlan.put("begindate", DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd"));
                workplans.put(workPlan);
            }
            result.put("workplans",workplans);
            json.put("result",result);
        } catch (JSONException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        } catch (ErrMsgException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
        return json.toString();
    }

    /**
     * 列出任务的某年某月的汇报
     * @param taskId
     * @param year
     * @param month
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listAnnexes", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
    public String listAnnexes(long taskId, int year, int month) {
        // month开始于1
        month -= 1;
        java.util.Date dtBegin = DateUtil.getDate(year, month, 1);
        java.util.Date dtEnd = DateUtil.getDate(year, month, DateUtil.getDayCount(year, month));
        // 往前往后各推7天
        dtBegin = DateUtil.addDate(dtBegin, -7);
        dtEnd = DateUtil.addDate(dtEnd, 7);

        String f = "yyyy-MM-dd";
        String strB = DateUtil.format(dtBegin, f);
        String strE = DateUtil.format(dtEnd, f);

        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (!pvg.auth(request)) {
            try {
                json.put("ret", 0);
                json.put("msg", "登录错误！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        String sql = "select id from work_plan_annex where task_id=" + taskId + " and add_date>=" + SQLFilter.getDateStr(strB, f) + " and add_date<=" + SQLFilter.getDateStr(strE, f) + " order by add_date asc";
        try {
            // DebugUtil.log(getClass(), "listAnnexes", sql);
            WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
            UserDb user = new UserDb();
            WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
            Vector v = wpad.list(sql);
            Iterator ir = v.iterator();
            JSONObject result = new JSONObject();
            while (ir.hasNext()) {
                wpad = (WorkPlanAnnexDb)ir.next();
                JSONObject plan = new JSONObject();
                plan.put("id", wpad.getLong("id"));
                plan.put("content", wpad.getString("content"));
                plan.put("oldProgress", wpad.getInt("old_progress"));
                plan.put("progress", wpad.getInt("progress"));
                plan.put("annexType", wpad.getInt("annex_type"));
                plan.put("checkStatus", wpad.getInt("check_status"));
                String userName = wpad.getString("user_name");
                user = user.getUserDb(userName);
                plan.put("realName", user.getRealName());
                plan.put("userName", wpad.getString("user_name"));
                String d = DateUtil.format(wpad.getDate("add_date"), "yyyy-M-d");
                if (result.has(d)){
                    JSONArray day = result.getJSONArray(d);
                    day.put(plan);
                }
                else {
                    JSONArray day = new JSONArray();
                    day.put(plan);
                    result.put(d, day);
                }

                JSONArray arr = new JSONArray();
                Vector wfaav = wfaa.getAttachments(wpad.getLong("id"));
                Iterator wfaair = wfaav.iterator();
                while (wfaair.hasNext()) {
                    WorkPlanAnnexAttachment att = (WorkPlanAnnexAttachment) wfaair.next();
                    JSONObject jo = new JSONObject();
                    jo.put("id", att.getId());
                    jo.put("name", att.getName());
                    jo.put("diskName", att.getDiskName());
                    jo.put("visualPath", att.getVisualPath());
                    jo.put("icon", com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(att.getDiskName())));
                    arr.put(jo);
                }
                plan.put("attachments", arr);
            }

            json.put("ret", 1);
            json.put("msg", "操作成功！");
            json.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addAnnex", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
    public String addAnnex(long taskId, String content, int progress, int annexType, String addDate, int annexYear, int annexItem, @RequestParam(value = "upload", required = false) MultipartFile[] files) {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (!pvg.auth(request)) {
            try {
                json.put("ret", 0);
                json.put("msg", "登录错误！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));
        int oldProgress = wptd.getInt("progress");

        int workplanId = wptd.getInt("work_plan_id");
        String userName = pvg.getUserName();

        com.redmoon.oa.db.SequenceManager seq = new com.redmoon.oa.db.SequenceManager();
        long id = seq.getNextId(SequenceManager.OA_WORKPLAN_ANNEX);

        try {
            int checkStatus;
            if (progress == oldProgress)
                checkStatus = WorkPlanAnnexDb.CHECK_STATUS_NONE; // 无需审核
            else {
                checkStatus = WorkPlanAnnexDb.CHECK_STATUS_PASSED;
            }
            boolean re = wad.create(new JdbcTemplate(), new Object[]{id, workplanId, content, userName, DateUtil.parse(addDate, "yyyy-MM-dd"), progress, 0, checkStatus, taskId, oldProgress, annexType, annexItem, annexYear});
            if (re) {
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_workplan") + "/" + year + "/" + month + "/";
                // 置保存路径
                String filepath = Global.getRealPath() + vpath;
                int len = 0;
                if (files!=null) {
                    len = files.length;
                }
                for (int i = 0; i < len; i++) {
                    MultipartFile file = files[i];
                    if (file.isEmpty()) {
                    } else {
                        String ext = StrUtil.getFileExt(file.getOriginalFilename());
                        String randName = FileUpload.getRandName();
                        WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
                        wfaa.setAnnexId(id);
                        String visualPath = year + "/" + month;
                        wfaa.setVisualPath(visualPath);
                        wfaa.setName(file.getOriginalFilename());
                        wfaa.setDiskName(randName + "." + ext);
                        wfaa.setOrders(0);
                        wfaa.setWorkplanId(workplanId);
                        wfaa.create();
                        file.transferTo(new File(filepath + randName + "." + ext));
                    }
                }

                if (progress != oldProgress) {
                    // 更改进度
                    wptd.changeProgress(progress);
                }

                json.put("ret", 1);
                json.put("id", id);
                json.put("checkStatus", checkStatus);
                json.put("msg", "操作成功！");

                WorkPlanDb wpd = new WorkPlanDb();
                wpd = wpd.getWorkPlanDb(workplanId);

                boolean isToMobile = SMSFactory.isUseSMS();
                IMessage imsg = null;
                String t = SkinUtil.LoadString(request,
                        "res.module.workplan",
                        "msg_workplan_annex_create_title");
                UserMgr um = new UserMgr();
                UserDb user = um.getUserDb(pvg.getUserName());

                t = t.replaceFirst("\\$title", wptd.getString("name"));
                t = t.replaceFirst("\\$user", user.getRealName());
                String c = SkinUtil.LoadString(request,
                        "res.module.workplan",
                        "msg_workplan_annex_create_content");
                c = c.replaceFirst("\\$content", StrUtil.getAbstract(request, content, 500));

                String[] principals = wpd.getPrincipals();

                if (isToMobile) {
                    ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
                    imsg = (IMessage) proxyFactory.getProxy();
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    len = principals.length;
                    for (int i = 0; i < len; i++) {
                        imsg.sendSysMsg(principals[i], t, c);
                        UserDb ud = um.getUserDb(principals[i]);
                        imu.send(ud, t, MessageDb.SENDER_SYSTEM);
                    }
                } else {
                    // 发送信息
                    MessageDb md = new MessageDb();
                    len = principals.length;
                    String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
                    for (int i = 0; i < len; i++) {
                        md.sendSysMsg(principals[i], t, c, action);
                    }
                }
            }
        } catch (ResKeyException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage(request));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delAnnex", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
    public String delAnnex(@RequestParam long id) {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (ParamUtil.isMobile(request)) {
            if (!pvg.auth(request)) {
                try {
                    json.put("ret", 0);
                    json.put("msg", "登录错误！");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json.toString();
            }
        }

        try {
            WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
            wad = (WorkPlanAnnexDb) wad.getQObjectDb(new Long(id));
            if (wad==null) {
                json.put("ret", 0);
                json.put("msg", "记录不存在！");
                return json.toString();
            }
            com.redmoon.oa.workplan.Privilege privilege = new com.redmoon.oa.workplan.Privilege();
            if (privilege.canUserManageWorkPlan(request, wad.getInt("workplan_id")))
                ;
            else {
                json.put("ret", 0);
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
            boolean re = wad.del();
            if (re) {
                WorkPlanTaskDb wptd = new WorkPlanTaskDb();
                wptd = (WorkPlanTaskDb)wptd.getQObjectDb(wad.getInt("task_id"));
                // 恢复成原来的progress
                wptd.changeProgress(wad.getInt("old_progress"));

                json.put("ret", 1);
                json.put("msg", "操作成功！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ResKeyException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/editAnnex", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
    public String editAnnex(long id, String content, int progress, @RequestParam(value = "upload", required = false) MultipartFile[] files) {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (!pvg.auth(request)) {
            try {
                json.put("ret", 0);
                json.put("msg", "登录错误！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        try {
            WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
            wad = (WorkPlanAnnexDb) wad.getQObjectDb(new Long(id));
            String userName = wad.getString("user_name");
            com.redmoon.oa.workplan.Privilege privilege = new com.redmoon.oa.workplan.Privilege();
            if (privilege.canUserManageWorkPlan(request, wad.getInt("workplan_id")) || userName.equals(pvg.getUserName()))
                ;
            else {
                json.put("ret", 0);
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }

            wad.set("progress", progress);
            wad.set("content", content);
            boolean re = wad.save();
            if (re) {
                int workplanId = wad.getInt("workplan_id");
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_workplan") + "/" + year + "/" + month + "/";
                // 置保存路径
                String filepath = Global.getRealPath() + vpath;
                int len = 0;
                if (files!=null) {
                    len = files.length;
                }
                for (int i = 0; i < len; i++) {
                    MultipartFile file = files[i];
                    if (file.isEmpty()) {
                    } else {
                        String ext = StrUtil.getFileExt(file.getOriginalFilename());
                        String randName = FileUpload.getRandName();
                        WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
                        wfaa.setAnnexId(id);
                        String visualPath = year + "/" + month;
                        wfaa.setVisualPath(visualPath);
                        wfaa.setName(file.getOriginalFilename());
                        wfaa.setDiskName(randName + "." + ext);
                        wfaa.setOrders(0);
                        wfaa.setWorkplanId(workplanId);
                        wfaa.create();
                        file.transferTo(new File(filepath + randName + "." + ext));
                    }
                }

                if (wad.getInt("old_progress") != progress) {
                    WorkPlanTaskDb wptd = new WorkPlanTaskDb();
                    wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(wad.getLong("task_id")));
                    wptd.changeProgress(progress);
                }

                json.put("ret", 1);
                json.put("msg", "操作成功！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ResKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delAnnexAttachment", method = {RequestMethod.POST, RequestMethod.GET}, produces={"text/html;","application/json;charset=UTF-8;"})
    public String delAnnexAttachment(@RequestParam long attId) {
        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (!pvg.auth(request)) {
            try {
                json.put("ret", 0);
                json.put("msg", "登录错误！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        try {
            WorkPlanAnnexAttachment att = new WorkPlanAnnexAttachment(attId);
            long workplanId = att.getWorkplanId();
            WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
            wad = (WorkPlanAnnexDb)wad.getQObjectDb(new Long(att.getAnnexId()));
            String userName = wad.getString("user_name");
            com.redmoon.oa.workplan.Privilege privilege = new com.redmoon.oa.workplan.Privilege();
            if (privilege.canUserManageWorkPlan(request, (int)workplanId) ||  userName.equals(pvg.getUserName()))
                ;
            else {
                json.put("ret", 0);
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
            boolean re = att.del();
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    /**
     * 列出任务的某年某月的汇报
     * @param id
     * @param year
     * @param month
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listAnnexesOfWorkplan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
    public String listAnnexesOfWorkplan(int id, int year, int month) {
        // month开始于1
        month -= 1;
        java.util.Date dtBegin = DateUtil.getDate(year, month, 1);
        java.util.Date dtEnd = DateUtil.getDate(year, month, DateUtil.getDayCount(year, month));
        // 往前往后各推7天
        dtBegin = DateUtil.addDate(dtBegin, -7);
        dtEnd = DateUtil.addDate(dtEnd, 7);

        String f = "yyyy-MM-dd";
        String strB = DateUtil.format(dtBegin, f);
        String strE = DateUtil.format(dtEnd, f);

        JSONObject json = new JSONObject();
        Privilege pvg = new Privilege();
        if (!pvg.auth(request)) {
            try {
                json.put("ret", 0);
                json.put("msg", "登录错误！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        String sql = "select id from work_plan_annex where workplan_id=" + id + " and add_date>=" + SQLFilter.getDateStr(strB, f) + " and add_date<=" + SQLFilter.getDateStr(strE, f) + " order by add_date asc";
        try {
            // DebugUtil.log(getClass(), "listAnnexes", sql);
            WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
            UserDb user = new UserDb();
            WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
            Vector v = wpad.list(sql);
            Iterator ir = v.iterator();
            JSONObject result = new JSONObject();
            while (ir.hasNext()) {
                wpad = (WorkPlanAnnexDb)ir.next();
                JSONObject plan = new JSONObject();
                plan.put("id", wpad.getLong("id"));
                plan.put("content", wpad.getString("content"));
                plan.put("oldProgress", wpad.getInt("old_progress"));
                plan.put("progress", wpad.getInt("progress"));
                plan.put("annexType", wpad.getInt("annex_type"));
                plan.put("checkStatus", wpad.getInt("check_status"));
                String userName = wpad.getString("user_name");
                user = user.getUserDb(userName);
                plan.put("realName", user.getRealName());
                plan.put("userName", wpad.getString("user_name"));
                String d = DateUtil.format(wpad.getDate("add_date"), "yyyy-M-d");
                if (result.has(d)){
                    JSONArray day = result.getJSONArray(d);
                    day.put(plan);
                }
                else {
                    JSONArray day = new JSONArray();
                    day.put(plan);
                    result.put(d, day);
                }

                JSONArray arr = new JSONArray();
                Vector wfaav = wfaa.getAttachments(wpad.getLong("id"));
                Iterator wfaair = wfaav.iterator();
                while (wfaair.hasNext()) {
                    WorkPlanAnnexAttachment att = (WorkPlanAnnexAttachment) wfaair.next();
                    JSONObject jo = new JSONObject();
                    jo.put("id", att.getId());
                    jo.put("name", att.getName());
                    jo.put("diskName", att.getDiskName());
                    jo.put("visualPath", att.getVisualPath());
                    jo.put("icon", com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(att.getDiskName())));
                    arr.put(jo);
                }
                plan.put("attachments", arr);
            }

            json.put("ret", 1);
            json.put("msg", "操作成功！");
            json.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
