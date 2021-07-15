package com.cloudweb.oa.controller;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.mail.SendMail;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.service.WorkflowService;
import com.cloudweb.oa.utils.ThreadContext;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FuncUtil;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.weixin.mgr.FlowDoMgr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

@Controller
public class WorkflowController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ThreadContext threadContext;

    @ResponseBody
    @RequestMapping(value = "/public/flow/addReply", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String addReply(String skey) {
        Privilege pvg = new Privilege();
        boolean re = pvg.auth(request);
        JSONObject json = new JSONObject();
        if (!re) {
            try {
                json.put("ret", "0");
                json.put("msg", "权限非法！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
        int flowId = ParamUtil.getInt(request, "flowId", -1);//当前流程id
        long actionId = ParamUtil.getLong(request, "actionId", -1);//当前流程action的id
        String content = request.getParameter("content");//“评论”的内容
        int parentId = ParamUtil.getInt(request, "parentId", -1);

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        String replyName = wf.getUserName(); // 被回复的用户
        UserMgr um = new UserMgr();
        UserDb ud = um.getUserDb(pvg.getUserName());

        String partakeUsers = "";
        int isSecret = ParamUtil.getInt(request, "isSecret", 0);//此“评论”是否隐藏
        // 将数据插入flow_annex附言表中
        long annexId = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);

        int progress = ParamUtil.getInt(request, "progress", 0);

        // id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress
        WorkflowAnnexDb wad = new WorkflowAnnexDb();
        JdbcTemplate jt = new JdbcTemplate();
        wad.create(jt, new Object[]{annexId, flowId, content, pvg.getUserName(), replyName, new java.util.Date(), actionId, isSecret, parentId, progress});

        // 不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
        // 写入进度
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        String formCode = lf.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        try {
            // 进度为0的时候不更新
            if (fd.isProgress() && progress > 0) {
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO((int) flowId, fd);
                fdao.setCwsProgress(progress);
                fdao.save();
            }

            MessageDb md = new MessageDb();
            String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
            MyActionDb mad = new MyActionDb();
            if (!myActionId.equals("")) {
                mad = mad.getMyActionDb(Long.parseLong(myActionId));
            }

            if (isSecret == 1) { // 如果是隐藏“评论”，只提醒发起“意见”的人
                if (!replyName.equals(pvg.getUserName())) {// 如果发起“意见”的人不是自己，就提醒
                    if (!myActionId.equals("")) {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), ud.getRealName() + "对意见：" + mad.getResult() + "发表了评论：<p>" + content + "</p>", myAction);
                    } else {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), ud.getRealName() + "发表了评论：<p>" + content + "</p>", myAction);
                    }
                }
            } else {
                // 如果不是隐藏“评论”，提醒所有参与流程的人
                // 解析得到参与流程的所有人
                String allUserListSql = "select distinct user_name from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
                ResultIterator ri1 = jt.executeQuery(allUserListSql);
                ResultRecord rr1 = null;
                while (ri1.hasNext()) {
                    rr1 = (ResultRecord) ri1.next();
                    partakeUsers += rr1.getString(1) + ",";
                }
                if (!partakeUsers.equals("")) {
                    partakeUsers = partakeUsers.substring(0, partakeUsers.length() - 1);
                }
                String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
                for (String user : partakeUsersArr) {
                    // 如果不是自己就提醒
                    if (!user.equals(pvg.getUserName())) {
                        if (!myActionId.equals("")) {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), ud.getRealName() + "对意见：" + mad.getResult() + "发表了评论：<p>" + content + "</p>", myAction);
                        } else {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), ud.getRealName() + "发表了评论：<p>" + content + "</p>", myAction);
                        }
                    }
                }
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
            try {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return json.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return json.toString();
        }

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/saveSearchColProps", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String saveSearchColProps(String typeCode, String colProps) {
        Privilege pvg = new Privilege();
        boolean re = pvg.auth(request);
        JSONObject json = new JSONObject();
        if (!re) {
            try {
                json.put("ret", "0");
                json.put("msg", "权限非法！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        // System.out.println(colProps);

        Leaf lf = new Leaf();
        if ("".equals(typeCode)) {
            typeCode = Leaf.CODE_ROOT;
        }
        lf = lf.getLeaf(typeCode);
        lf.setColProps(colProps);
        re = lf.update();

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 置待办记录的状态
     *
     * @param myActionId
     * @param checkStatus
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/setMyActionStatus", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setMyActionStatus(long myActionId, int checkStatus) {
        JSONObject json = new JSONObject();
        WorkflowMgr wm = new WorkflowMgr();
        boolean re = false;
        try {
            int actionStatus = wm.setMyActionStatus(request, myActionId, checkStatus);
            json.put("actionStatus", actionStatus);
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
            try {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 清除节点上的用户
     *
     * @param actionId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/clearActionUser", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String clearActionUser(int actionId) {
        boolean re = false;
        JSONObject json = new JSONObject();
        try {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb(actionId);
            wa.setUserName("");
            wa.setUserRealName("");
            re = wa.save();
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/public/flow/finishBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String finishBatch() {
        JSONObject json = new JSONObject();
        try {
            String noteStr = LocalUtil.LoadString(request, "res.flow.Flow", "prompt");
            WorkflowMgr wm = new WorkflowMgr();
            int count = 0;
            try {
                count = wm.FinishActionBatch(request);
            } catch (ErrMsgException e) {
                String alertStr = e.getMessage();
                alertStr = alertStr.replace("\r\n", "");
                json.put("ret", 0);
                json.put("msg", alertStr);
                return json.toString();
            }

            json.put("ret", 1);
            json.put("msg", LocalUtil.LoadString(request, "res.common", "info_op_success"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 列出下载日志
     *
     * @param flowId
     * @param attId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/listAttLog", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String listAttLog(long flowId, long attId) {
        AttachmentLogDb ald = new AttachmentLogDb();
        String sql = ald.getQuery(request, flowId, attId);
        int pageSize = ParamUtil.getInt(request, "rp", 20);
        int curPage = ParamUtil.getInt(request, "page", 1);
        ListResult lr = null;
        try {
            lr = ald.listResult(sql, curPage, pageSize);
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONArray rows = new JSONArray();
        JSONObject jobject = new JSONObject();

        try {
            jobject.put("rows", rows);
            jobject.put("page", curPage);
            jobject.put("total", lr.getTotal());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UserDb user = new UserDb();
        Iterator ir = lr.getResult().iterator();
        while (ir.hasNext()) {
            ald = (AttachmentLogDb) ir.next();
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", String.valueOf(ald.getLong("id")));
                jo.put("logTime", DateUtil.format(ald.getDate("log_time"), "yyyy-MM-dd HH:mm:ss"));

                user = user.getUserDb(ald.getString("user_name"));
                jo.put("realName", user.getRealName());

                Attachment att = new Attachment((int) ald.getLong("att_id"));
                jo.put("attName", att.getName());

                jo.put("logType", AttachmentLogDb.getTypeDesc(ald.getInt("log_type")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            rows.put(jo);
        }

        return jobject.toString();
    }

    /**
     * 删除附件日志
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/delLog", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delLog(String ids) {
        JSONObject json = new JSONObject();

        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择记录！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        try {
            boolean re = false;
            for (String strId : ary) {
                long id = StrUtil.toLong(strId, -1);
                if (id != -1) {
                    AttachmentLogDb ald = new AttachmentLogDb();
                    ald = (AttachmentLogDb) ald.getQObjectDb(id);

                    boolean isValid = false;
                    com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                    if (pvg.isUserPrivValid(request, "admin")) {
                        isValid = true;
                    } else {
                        long flowId = ald.getLong("flow_id");
                        WorkflowDb wf = new WorkflowDb();
                        wf = wf.getWorkflowDb((int) flowId);
                        if (wf != null) {
                            LeafPriv lp = new LeafPriv(wf.getTypeCode());
                            if (pvg.isUserPrivValid(request, "admin.flow")) {
                                if (lp.canUserExamine(pvg.getUser(request))) {
                                    isValid = true;
                                }
                            }
                        } else {
                            // 对应的流程如不存在，则允许删除
                            isValid = true;
                        }
                    }

                    if (!isValid) {
                        json.put("ret", "0");
                        json.put("msg", "权限非法！");
                        return json.toString();
                    }

                    if (isValid) {
                        re = ald.del();
                    }
                } else {
                    json.put("ret", "0");
                    json.put("msg", "标识非法！");
                    return json.toString();
                }
            }

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * flow_list.jsp中列出查询结果
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String list() {
        // 默认值用DISPLAY_MODE_DOING，而原先是DISPLAY_MODE_SEARCH，防止360浏览器传上来的参数异常，致直接进入查询，而导致看到所有人的待办流程
        int displayMode = ParamUtil.getInt(request, "displayMode", WorkflowMgr.DISPLAY_MODE_DOING);
        String op = ParamUtil.get(request, "op");
        String typeCode;
        if ("search".equals(op)) {
            typeCode = ParamUtil.get(request, "f.typeCode");
        } else {
            typeCode = ParamUtil.get(request, "typeCode");
        }

        String action = ParamUtil.get(request, "action"); // sel 选择我的流程
        String tabIdOpener = ParamUtil.get(request, "tabIdOpener");

        MyActionDb mad = new MyActionDb();
        MacroCtlMgr mm = new MacroCtlMgr();
        FormDb fd = new FormDb();
        FormDAO fdao = new FormDAO();
        UserMgr um = new UserMgr();
        UserDb user;

        WorkflowDb wf = new WorkflowDb();
        JSONObject jobject = new JSONObject();
        int pageSize = ParamUtil.getInt(request, "rp", 20);
        int curPage = ParamUtil.getInt(request, "page", 1);

        // 防止在“我的流程”界面，如果session过期，生成的sql可以查到全部的流程信息
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request)) {
            JSONArray rows = new JSONArray();
            try {
                jobject.put("rows", rows);
                jobject.put("page", curPage);
                jobject.put("total", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jobject.toString();
        }

        Leaf leaf = new Leaf();
        if (!"".equals(typeCode)) {
            leaf = leaf.getLeaf(typeCode);
            if (leaf == null) {
                JSONArray rows = new JSONArray();
                try {
                    jobject.put("rows", rows);
                    jobject.put("page", curPage);
                    jobject.put("total", 0);
                    jobject.put("msg", "流程类型：" + typeCode + "不存在");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jobject.toString();
            }
            fd = fd.getFormDb(leaf.getFormCode());
        }

        JSONArray colProps = null;
        if (leaf.isLoaded() && !"".equals(leaf.getColProps())) {
            try {
                colProps = new JSONArray(leaf.getColProps());
            } catch (org.json.JSONException e) {
                System.out.println(getClass() + " colLeaf.getColProps()=" + leaf.getColProps());
                e.printStackTrace();
            }
        }
        if (colProps == null) {
            colProps = com.redmoon.oa.flow.Leaf.getDefaultColProps(request, typeCode, displayMode);
        }
        String userRealName = "";

        // 显示模式，0表示流程查询、1表示待办、2表示我参与的流程、3表示我发起的流程、4表示我关注的流程
        if (displayMode == WorkflowMgr.DISPLAY_MODE_DOING) {
            String sql = wf.getSqlDoing(request);
            ListResult lr = null;
            try {
                lr = mad.listResult(sql, curPage, pageSize);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }
            JSONArray rows = new JSONArray();
            try {
                jobject.put("rows", rows);
                jobject.put("page", curPage);
                jobject.put("total", lr.getTotal());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            WorkflowDb wfd = new WorkflowDb();
            java.util.Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                mad = (MyActionDb) ir.next();
                wfd = wfd.getWorkflowDb((int) mad.getFlowId());
                String userName = wfd.getUserName();
                if (userName != null) {
                    user = um.getUserDb(wfd.getUserName());
                    userRealName = user.getRealName();
                }
                if (!typeCode.equals(wfd.getTypeCode())) { // 流程查询时，点击根节点，会显示所有的流程，此时typeCode可能与wfd.getTypeCode不一致
                    Leaf lf = leaf.getLeaf(wfd.getTypeCode());
                    if (lf == null) {
                        DebugUtil.e(getClass(), "list", "流程：" + wfd.getId() + "、" + wfd.getTitle() + " 类型：" + wfd.getTypeCode() + " 已不存在！");
                        continue;
                    } else {
                        fd = fd.getFormDb(lf.getFormCode());
                    }
                }
                fdao = fdao.getFormDAO(wfd.getId(), fd);
                JSONObject jo = getRow(wfd, fdao, colProps, um, userRealName, mad, mm, leaf, displayMode, action, tabIdOpener);
                rows.put(jo);
            }
        } else {
            String sql;
            if (displayMode == WorkflowMgr.DISPLAY_MODE_ATTEND) {
                sql = wf.getSqlAttend(request);
            } else if (displayMode == WorkflowMgr.DISPLAY_MODE_MINE) {
                sql = wf.getSqlMine(request);
            } else if (displayMode == WorkflowMgr.DISPLAY_MODE_FAVORIATE) {
                sql = wf.getSqlFavorite(request);
            } else {
                // 判断是否有权限，以防止非法操作
                LeafPriv lp = new LeafPriv(typeCode);
                if (!lp.canUserQuery(pvg.getUser(request))) {
                    // 如果用户没有权限，则返回我的流程所用的sql
                    sql = wf.getSqlAttend(request);
                } else {
                    sql = wf.getSqlSearch(request);
                }
            }

            DebugUtil.i(getClass(), "list sql=", sql);

            long t = new java.util.Date().getTime();

            ListResult lr = null;
            try {
                lr = wf.listResult(sql, curPage, pageSize);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }

            // DebugUtil.i(getClass(), "list", "listResult 时长：" + (new Date().getTime() - t) + "毫秒 sql=" + sql);

            JSONArray rows = new JSONArray();
            try {
                jobject.put("rows", rows);
                jobject.put("page", curPage);
                jobject.put("total", lr.getTotal());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                WorkflowDb wfd = (WorkflowDb) ir.next();
                if (!typeCode.equals(wfd.getTypeCode())) { // 流程查询时，点击根节点，会显示所有的流程，此时typeCode可能与wfd.getTypeCode不一致
                    Leaf lf = leaf.getLeaf(wfd.getTypeCode());
                    if (lf == null) {
                        DebugUtil.e(getClass(), "list", "流程：" + wfd.getId() + " ，其类型：" + wfd.getTypeCode() + " 已不存在！");
                        continue;
                    }
                    fd = fd.getFormDb(lf.getFormCode());
                } else {
                    fd = fd.getFormDb(leaf.getFormCode());
                }
                fdao = fdao.getFormDAO(wfd.getId(), fd);

                user = um.getUserDb(wfd.getUserName());
                if (user.isLoaded()) {
                    userRealName = user.getRealName();
                }

                JSONObject jo = getRow(wfd, fdao, colProps, um, userRealName, mad, mm, leaf, displayMode, action, tabIdOpener);
                rows.put(jo);
            }
        }

        return jobject.toString();
    }

    public JSONObject getRow(WorkflowDb wfd, FormDAO fdao, JSONArray colProps, UserMgr um, String userRealName, MyActionDb mad, MacroCtlMgr mm, Leaf leaf, int displayMode, String action, String tabIdOpener) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("id", String.valueOf(mad.getId()));

            Leaf lf = leaf.getLeaf(wfd.getTypeCode());
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            String rootPath = request.getContextPath();
            String cls = "class=\"readed\"";
            if (!mad.isReaded()) {
                cls = "class=\"unreaded\"";
            }

            RequestUtil.setFormDAO(request, fdao);

            for (int i = 0; i < colProps.length(); i++) {
                JSONObject json = (JSONObject) colProps.get(i);
/*					if (json.getBoolean("hide")) {
						continue;
					}*/
                String fieldName = json.getString("name");
                String val = "";
                if (fieldName.startsWith("f.")) {
                    fieldName = fieldName.substring(2);
                    if ("id".equalsIgnoreCase(fieldName)) {
                        val = String.valueOf(wfd.getId());
                    } else if ("flow_level".equalsIgnoreCase(fieldName)) {
                        val = WorkflowMgr.getLevelImg(request, wfd);
                    } else if ("title".equalsIgnoreCase(fieldName)) {
                        if (displayMode == WorkflowMgr.DISPLAY_MODE_DOING) {
                            if (lf.getType() == Leaf.TYPE_LIST) {
                                val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + mad.getId() + "&tabIdOpener=" + tabIdOpener + "')\" title=\"" + wfd.getTitle() + "\" " + cls + ">" + wfd.getTitle() + "</a>";
                            } else {
                                wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                                if (wpd.isLight()) {
                                    val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" " + cls + " link=\"flow_dispose_light.jsp?myActionId=" + mad.getId() + "\"";
                                    val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + rootPath + "/flow_dispose_light.jsp?myActionId=" + mad.getId() + "')\">" + wfd.getTitle() + "</a>";
                                } else {
                                    val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" " + cls + " link=\"flow_dispose_free.jsp?myActionId=" + mad.getId() + "\"";
                                    val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + rootPath + "/flow_dispose_free.jsp?myActionId=" + mad.getId() + "')\">" + wfd.getTitle() + "</a>";
                                }
                            }
                        } else {
                            wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                            if (wpd.isLight()) {
                                val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + wfd.getTitle() + "</a>";
                            } else {
                                val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + wfd.getTitle() + "</a>";
                            }
                        }
                    } else if ("type_code".equalsIgnoreCase(fieldName)) {
                        if (lf != null) {
                            val = "<a href=\"flow_list.jsp?op=search&displayMode=" + displayMode + "&typeCode=" + StrUtil.UrlEncode(lf.getCode()) + "\">" + lf.getName(request) + "</a>";
                        } else {
                            val = "";
                        }
                    } else if ("begin_date".equals(fieldName) || "mydate".equals(fieldName)) { // 保留mydate条件，是为了向下兼容，以免需重置后，才能显示时间
                        val = DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm");
                    } else if ("userName".equals(fieldName)) {
                        val = userRealName;
                    } else if ("finallyApply".equals(fieldName)) {
                        // 取得最后一个已办理的人员
                        MyActionDb lastMad = mad.getLastMyActionDbDoneOfFlow(wfd.getId());
                        if (lastMad != null) {
                            val = um.getUserDb(lastMad.getUserName()).getRealName();
                        }
                    } else if ("currentHandle".equals(fieldName)) {
                        Iterator ir2 = mad.getMyActionDbDoingOfFlow(wfd.getId()).iterator();
                        while (ir2.hasNext()) {
                            MyActionDb madCur = (MyActionDb) ir2.next();
                            if (!val.equals("")) {
                                val += "、";
                            }
                            val += um.getUserDb(madCur.getUserName()).getRealName();
                        }
                    } else if ("status".equals(fieldName)) {
                        if (displayMode != WorkflowMgr.DISPLAY_MODE_DOING) {
                            val = wfd.getStatusDesc();
                        } else {
                            val = WorkflowActionDb.getStatusName(mad.getActionStatus());
                        }
                    } else if ("remainTime".equals(fieldName)) {
                        String remainDateStr = "";
                        if (mad.getExpireDate() != null && DateUtil.compare(new java.util.Date(), mad.getExpireDate()) == 2) {
                            int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
                            String str_day = LocalUtil.LoadString(request, "res.flow.Flow", "day");
                            String str_hour = LocalUtil.LoadString(request, "res.flow.Flow", "h_hour");
                            String str_minute = LocalUtil.LoadString(request, "res.flow.Flow", "minute");
                            remainDateStr = ary[0] + " " + str_day + ary[1] + " " + str_hour + ary[2] + " " + str_minute;
                            val = remainDateStr;
                        }
                    } else if ("end_date".equals(fieldName)) {
                        val = DateUtil.format(wfd.getEndDate(), "yy-MM-dd HH:mm");
                    }
                } else if (fieldName.equals("operate")) {
                    if (displayMode == WorkflowMgr.DISPLAY_MODE_SEARCH) {
                        val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + rootPath + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                    } else if (displayMode == WorkflowMgr.DISPLAY_MODE_DOING) {
                        String suspend = "";
                        if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_SUSPEND) {
                            suspend = mad.getCheckStatusName();
                        }
                        if (lf.getType() == Leaf.TYPE_LIST) {
                            val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + rootPath + "/flow_dispose.jsp?myActionId=" + mad.getId() + "&tabIdOpener=" + tabIdOpener + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + suspend + "</a>";
                        } else {
                            wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                            if (wpd.isLight()) {
                                val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" " + cls + " link=\"flow_dispose_light.jsp?myActionId=" + mad.getId() + "\"";
                                val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + rootPath + "/flow_dispose_light.jsp?myActionId=" + mad.getId() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + "</a>";
                            } else {
                                val = "<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "processingFlow") + "：" + wfd.getTitle() + "\" " + cls + " link=\"flow_dispose_free.jsp?myActionId=" + mad.getId() + "\"";
                                val += " onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + rootPath + "/flow_dispose_free.jsp?myActionId=" + mad.getId() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "chandle") + "</a>";
                            }
                        }
                        val += "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:;\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "focusProcess") + "/>\" onclick=\"favorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "attention") + "</a>";
                    } else if (displayMode == WorkflowMgr.DISPLAY_MODE_ATTEND || displayMode == WorkflowMgr.DISPLAY_MODE_MINE) {
                        wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                        if (wpd.isLight()) {
                            val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                        } else {
                            val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                        }
                        // 当action为sel时，显示选择链接，否则显示关注链接
                        if ("sel".equals(action)) {
                            val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"selFlow('" + wfd.getId() + "', '" + wfd.getTitle() + "')\">" + SkinUtil.LoadString(request, "res.flow.Flow", "choose") + "</a>";
                        } else {
                            val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"favorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "attention") + "</a>";
                        }
                    } else if (displayMode == WorkflowMgr.DISPLAY_MODE_FAVORIATE) {
                        wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                        if (wpd.isLight()) {
                            val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("<br>", "").replace("&#039;", "\\&#039;") + "', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wfd.getId() + "')\" title=\"" + wfd.getTitle() + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                        } else {
                            val = "<a href=\"javascript:;\" onclick=\"addTab('" + StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "") + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wfd.getId() + "')\" title=\"" + SkinUtil.LoadString(request, "res.flow.Flow", "viewProcess") + "\">" + SkinUtil.LoadString(request, "res.flow.Flow", "show") + "</a>";
                        }
                        val += "&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"unfavorite(" + wfd.getId() + ")\">" + SkinUtil.LoadString(request, "res.flow.Flow", "cancelAttention") + "</a>";
                    }
                } else {
                    FormField ff = fdao.getFormField(fieldName);
                    if (ff != null) {
                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                            }
                        } else {
                            val = FuncUtil.renderFieldValue(fdao, ff);
                        }
                    }
                }
                jo.put(json.getString("name"), val);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    /**
     * 取得被renew的流程
     *
     * @param count int 数量
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/getFlowsRenewed", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getFlowsRenewed(int count) {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();//从XML文件里取出文件存入路径
        String flowImagePath = cfg.get("flowImagePath");
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String vpath = flowImagePath + "/" + year + "/" + month;
        File f = new File(Global.getRealPath() + vpath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        JSONArray rows = new JSONArray();
        JSONObject jobject = new JSONObject();
        String sql = "select id from flow where is_renewed=1 order by id desc";
        WorkflowDb wf = new WorkflowDb();
        try {
            ListResult lr = wf.listResult(sql, 1, count);
            try {
                jobject.put("visualPath", vpath);
                jobject.put("rows", rows);
                jobject.put("count", lr.getResult().size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                wf = (WorkflowDb) ir.next();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("id", String.valueOf(wf.getId()));
                    jo.put("title", wf.getTitle());
                    jo.put("flowString", wf.getFlowString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                rows.put(jo);
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return jobject.toString();
    }

    /**
     * 删除附件日志
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/setFlowsRenewed", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setFlowsRenewed(String ids, String visualPath) {
        JSONObject json = new JSONObject();
        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择记录！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        try {
            boolean re = false;
            for (String strId : ary) {
                int id = StrUtil.toInt(strId, -1);
                if (id != -1) {
                    WorkflowDb wf = new WorkflowDb();
                    wf = wf.getWorkflowDb(id);
                    wf.setRenewed(false);
                    wf.setImgVisualPath(visualPath);
                    re = wf.save();
                } else {
                    json.put("ret", "0");
                    json.put("msg", "标识非法！");
                    return json.toString();
                }
            }

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 关联项目
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/linkProject", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String linkProject(HttpServletRequest request) {
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        long projectId = ParamUtil.getLong(request, "projectId", -1);
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        wf.setProjectId(projectId);
        boolean re = wf.save();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            json.put("ret", "1");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str);
        } else {
            json.put("ret", "0");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str);
        }
        return json.toString();
    }

    /**
     * 取消关联项目
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/unlinkProject", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String unlinkProject(HttpServletRequest request) {
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        wf.setProjectId(-1);
        boolean re = wf.save();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            json.put("ret", "1");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str);
        } else {
            json.put("ret", "0");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/distribute", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String distribute(HttpServletRequest request) throws ResKeyException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String myname = privilege.getUser(request);

        UserDb user = new UserDb();
        user = user.getUserDb(myname);

        String depts = ParamUtil.get(request, "depts");
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        String paperTitle = ParamUtil.get(request, "title");
        int isFlowDisplay = ParamUtil.getInt(request, "isFlowDisplay", 0);
        String[] ary = StrUtil.split(depts, ",");
        int len = 0;
        if (ary != null) {
            len = ary.length;
        }

        //发送邮件提醒
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        SendMail sendmail = WorkflowDb.getSendMail();
        UserDb formUserDb = new UserDb();
        formUserDb = formUserDb.getUserDb(privilege.getUser(request));
        String fromNick = "";
        try {
            fromNick = MimeUtility.encodeText(formUserDb.getRealName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fromEmail = Global.getEmail();
        fromNick = fromNick + "<" + fromEmail + ">";

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

        boolean re = false;
        DeptDb dd = new DeptDb();
        String fromUnit = user.getUnitCode();
        dd = dd.getDeptDb(fromUnit);
        String unitName = dd.getName();
        PaperConfig pc = PaperConfig.getInstance();
        // 从配置文件中得到收文角色
        String swRoles = pc.getProperty("swRoles");
        String[] aryRole = StrUtil.split(swRoles, ",");
        int aryRoleLen = 0;
        if (aryRole != null) {
            aryRoleLen = aryRole.length;
        }
        RoleDb[] aryR = new RoleDb[aryRoleLen];
        RoleDb rd = new RoleDb();
        // 取出收文角色
        for (int i = 0; i < aryRoleLen; i++) {
            aryR[i] = rd.getRoleDb(aryRole[i]);
        }
        for (int i = 0; i < len; i++) {
            PaperDistributeDb pdd = new PaperDistributeDb();
            String toUnit = ary[i];
            java.util.Date disDate = new java.util.Date();
            int isReaded = 0;
            int kind = PaperDistributeDb.KIND_UNIT;
            long paperId = SequenceManager.nextID(SequenceManager.OA_FLOW_PAPER_DISTRIBUTE);

            re = pdd.create(new JdbcTemplate(), new Object[]{new Long(paperId), paperTitle, new Integer(flowId), fromUnit, toUnit, disDate, myname, new Integer(isFlowDisplay), new Integer(isReaded), new Integer(kind)});
            if (re) {
                for (int j = 0; j < aryRoleLen; j++) {
                    // 取出角色中的全部用户
                    java.util.Iterator ir = aryR[j].getAllUserOfRole().iterator();
                    while (ir.hasNext()) {
                        user = (UserDb) ir.next();
                        // 如果用户属于收文单位
                        if (user.getUnitCode().equals(toUnit)) {
                            // 消息提醒
                            String action;
                            if (isFlowDisplay == 0) {
                                action = "action=" + MessageDb.ACTION_PAPER_DISTRIBUTE + "|paperId=" + paperId;
                            } else {
                                action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
                            }

                            try {
                                String swNoticeTitle = pc.getProperty("swNoticeTitle");
                                swNoticeTitle = StrUtil.format(swNoticeTitle, new Object[]{unitName, paperTitle});
                                String swNoticeContent = pc.getProperty("swNoticeContent");
                                swNoticeContent = StrUtil.format(swNoticeContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});
                                MessageDb md = new MessageDb();
                                md.sendSysMsg(user.getName(), swNoticeTitle, swNoticeContent, action);

                                if (flowNotifyByEmail) {
                                    if (isFlowDisplay == 1) {
                                        String actionFlow = "op=show|userName=" + user.getName() + "|" +
                                                "flowId=" + flowId;
                                        actionFlow = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), actionFlow);
                                        UserSetupDb usd = new UserSetupDb(user.getName());
                                        swNoticeContent += "<BR />>>&nbsp;<a href='" +
                                                Global.getFullRootPath(request) +
                                                "/public/flow_dispose.jsp?action=" + actionFlow +
                                                "' target='_blank'>" +
                                                (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
                                    }
                                    sendmail.initMsg(user.getEmail(), fromNick, swNoticeTitle, swNoticeContent, true);
                                    sendmail.send();
                                    sendmail.clear();
                                }

                                String swPlanTitle = pc.getProperty("swPlanTitle");
                                swPlanTitle = StrUtil.format(swPlanTitle, new Object[]{paperTitle});
                                String swPlanContent = pc.getProperty("swPlanContent");
                                swPlanContent = StrUtil.format(swPlanContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});

                                // 创建日程安排
                                PlanDb pd = new PlanDb();
                                pd.setTitle(swPlanTitle);
                                pd.setContent(swPlanContent);
                                pd.setMyDate(new java.util.Date());
                                pd.setEndDate(new java.util.Date());
                                pd.setActionData(String.valueOf(paperId));
                                pd.setActionType(PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE);
                                pd.setUserName(user.getName());
                                pd.setRemind(false);
                                pd.setRemindBySMS(false);
                                pd.setRemindDate(new java.util.Date());
                                pd.create();
                            } catch (ErrMsgException ex2) {
                                ex2.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        UserDb toUserDb = new UserDb();
        String users = ParamUtil.get(request, "users");
        ary = StrUtil.split(users, ",");
        len = 0;
        if (ary != null) {
            len = ary.length;
        }
        for (int i = 0; i < len; i++) {
            PaperDistributeDb pdd = new PaperDistributeDb();
            java.util.Date disDate = new java.util.Date();
            int isReaded = 0;
            int kind = PaperDistributeDb.KIND_USER;
            long paperId = SequenceManager.nextID(SequenceManager.OA_FLOW_PAPER_DISTRIBUTE);
            re = pdd.create(new JdbcTemplate(), new Object[]{new Long(paperId), paperTitle, new Integer(flowId), fromUnit, ary[i], disDate, myname, new Integer(isFlowDisplay), new Integer(isReaded), new Integer(kind)});
            if (re) {
                // 消息提醒
                String action;
                if (isFlowDisplay == 0) {
                    action = "action=" + MessageDb.ACTION_PAPER_DISTRIBUTE + "|paperId=" + paperId;
                } else {
                    action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
                }
                try {
                    String swNoticeTitle = pc.getProperty("swNoticeTitle");
                    swNoticeTitle = StrUtil.format(swNoticeTitle, new Object[]{unitName, paperTitle});
                    String swNoticeContent = pc.getProperty("swNoticeContent");
                    swNoticeContent = StrUtil.format(swNoticeContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});
                    MessageDb md = new MessageDb();
                    md.sendSysMsg(ary[i], swNoticeTitle, swNoticeContent, action);

                    if (flowNotifyByEmail) {
                        toUserDb = toUserDb.getUserDb(ary[i]);
                        if (isFlowDisplay == 1) {
                            String actionFlow = "op=show|userName=" + toUserDb.getName() + "|" +
                                    "flowId=" + flowId;
                            actionFlow = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), actionFlow);
                            UserSetupDb usd = new UserSetupDb(toUserDb.getName());
                            swNoticeContent += "<BR />>>&nbsp;<a href='" +
                                    Global.getFullRootPath(request) +
                                    "/public/flow_dispose.jsp?action=" + actionFlow +
                                    "' target='_blank'>" +
                                    (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
                        }
                        sendmail.initMsg(toUserDb.getEmail(), fromNick, swNoticeTitle, swNoticeContent, true);
                        sendmail.send();
                        sendmail.clear();
                    }

                    String swPlanTitle = pc.getProperty("swPlanTitle");
                    swPlanTitle = StrUtil.format(swPlanTitle, new Object[]{paperTitle});
                    String swPlanContent = pc.getProperty("swPlanContent");
                    swPlanContent = StrUtil.format(swPlanContent, new Object[]{unitName, paperTitle, DateUtil.format(disDate, "yyyy-MM-dd")});

                    // 创建日程安排
                    PlanDb pd = new PlanDb();
                    pd.setTitle(swPlanTitle);
                    pd.setContent(swPlanContent);
                    pd.setMyDate(new java.util.Date());
                    pd.setEndDate(new java.util.Date());
                    pd.setActionData(String.valueOf(paperId));
                    pd.setActionType(PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE);
                    pd.setUserName(ary[i]);
                    pd.setRemind(false);
                    pd.setRemindBySMS(false);
                    pd.setRemindDate(new java.util.Date());
                    pd.create();
                } catch (ErrMsgException ex2) {
                    ex2.printStackTrace();
                }
            }
        }

        // 生成PDF
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        int doc_id = wf.getDocId();
        com.redmoon.oa.flow.DocumentMgr dm = new com.redmoon.oa.flow.DocumentMgr();
        com.redmoon.oa.flow.Document doc = dm.getDocument(doc_id);
        java.util.Vector attachments = doc.getAttachments(1);
        java.util.Iterator ir = attachments.iterator();
        while (ir.hasNext()) {
            com.redmoon.oa.flow.Attachment att = (com.redmoon.oa.flow.Attachment) ir.next();
            String ext = StrUtil.getFileExt(att.getName());
            if (ext.equals("doc") || ext.equals("docx")) {
                ;
            } else {
                continue;
            }

            String fileName = att.getDiskName();
            String fName = fileName.substring(0, fileName.lastIndexOf("."));
            fileName = fName + ".pdf";

            String fileDiskPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
            String pdfPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + fileName;
            File f = new File(pdfPath);
            try {
                com.redmoon.oa.util.PDFConverter.convert2PDF(fileDiskPath, pdfPath);
            } catch (Exception e) {
                // UnsatisfiedLinkError: no jacob in java.library.path
                e.printStackTrace();
            }
        }

        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("op", "distribute");
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "distributionSuccess");
            json.put("msg", str);
        } else {
            json.put("ret", "0");
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "distributionFaile");
            json.put("msg", str);
            json.put("op", "distribute");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/clearLocker", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String clearLocker(HttpServletRequest request) {
        int fileId = ParamUtil.getInt(request, "fileId", -1);
        boolean re = true;
        com.redmoon.oa.flow.Attachment at = new com.redmoon.oa.flow.Attachment(fileId);
        if (!"".equals(at.getLockUser())) {
            at.setLockUser("");
            re = at.save();
        }
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            json.put("ret", "1");
        } else {
            json.put("ret", "0");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/recall", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String recall(HttpServletRequest request) {
        WorkflowMgr wfm = new WorkflowMgr();
        long myActionId = ParamUtil.getLong(request, "action_id", -1);
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = wfm.recallMyAction(request, myActionId);
        } catch (ErrMsgException e) {
            e.printStackTrace();
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("ret", "1");
            json.put("msg", str);
        } else {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "noNodeCanBeWithdrawn");
            json.put("ret", "0");
            json.put("msg", str);
        }
        return json.toString();
    }

    /**
     * 如果本节点是异或聚合，办理完毕，但不转交
     *
     * @param request
     * @return
     * @throws ErrMsgException
     */
    @ResponseBody
    @RequestMapping(value = "/flow/setFinishAndNotDelive", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setFinishAndNotDelive(HttpServletRequest request) throws ErrMsgException {
        boolean re = false;
        long myActionId = ParamUtil.getLong(request, "myActionId");
        long actionId = ParamUtil.getLong(request, "actionId");
        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int) actionId);
        wa.setStatus(WorkflowActionDb.STATE_FINISHED);
        re = wa.save();
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        re = mad.save();
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(wa.getFlowId());
        // 检查流程中的节点是否都已完成
        if (wfd.checkStatusFinished()) {
            re = wfd.changeStatus(request, WorkflowDb.STATUS_FINISHED, wa);
        }
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("ret", "1");
            json.put("msg", str);
        } else {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("ret", "0");
            json.put("msg", str);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/renameAtt", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String renameAtt(HttpServletRequest request) {
        boolean re = false;
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        int attId = ParamUtil.getInt(request, "attId", -1);
        String newName = ParamUtil.get(request, "newName");
        String str1 = LocalUtil.LoadString(request, "res.common", "info_op_success");
        String str_faile = LocalUtil.LoadString(request, "res.common", "info_op_fail");
        com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment(attId);
        String name = att.getName();
        if (name.equals("")) {
            json.put("ret", "0");
            json.put("msg", LocalUtil.LoadString(request, "res.flow.Flow", "nameNotBeEmpty"));
            return json.toString();
        }
        if (!name.equals(newName)) {
            att.setName(newName);
            re = att.save();
        } else {
            json.put("ret", "0");
            json.put("msg", "名称相同");
            return json.toString();
        }
        if (re) {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("ret", "1");
            json.put("msg", str);
        } else {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("ret", "0");
            json.put("msg", str);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/deliver", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String deliver(HttpServletRequest request) {
        WorkflowMgr wfm = new WorkflowMgr();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        try {
            re = wfm.deliverFree(request, flowId);
        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            e.printStackTrace();
            return json.toString();
        }
        if (re) {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("ret", "1");
            json.put("msg", str);
        } else {
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("ret", "0");
            json.put("msg", str);
        }
        return json.toString();
    }

    /**
     * 提交“评论”
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/addReply", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addReply(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        // flow_dispose.jsp回复
        String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
        long flowId = ParamUtil.getLong(request, "flow_id", -1);//当前流程id
        long actionId = ParamUtil.getLong(request, "action_id", -1);//当前流程action的id
        String replyContent = request.getParameter("content");//“评论”的内容
        String userRealName = request.getParameter("userRealName");
        String userName = request.getParameter("user_name");
        String replyName = request.getParameter("reply_name");
        int parentId = ParamUtil.getInt(request, "parent_id", -1);

        UserMgr um = new UserMgr();
        UserDb oldUser = um.getUserDb(userName);
        UserDb replyUser = um.getUserDb(replyName);

        String partakeUsers = "";
        int isSecret = ParamUtil.getInt(request, "isSecret", 0);//此“评论”是否隐藏
        //将数据插入flow_annex附言表中
        long id = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String myDate = currentDate;

        int progress = ParamUtil.getInt(request, "cwsProgress", 0);
        StringBuilder sql = new StringBuilder("insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(");
        sql.append(id).append(",").append(flowId).append(",").append(StrUtil.sqlstr(replyContent))
                .append(",").append(StrUtil.sqlstr(userName)).append(",").append(StrUtil.sqlstr(replyName))
                .append(",").append(StrUtil.sqlstr(myDate)).append(",").append(actionId).append(",").append(isSecret).append(",").append(parentId).append(",").append(progress).append(")");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            jt.executeUpdate(sql.toString());

            //不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
            WorkflowDb wf = new WorkflowDb((int) flowId);

            // 写入进度
            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            String formCode = lf.getFormCode();
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            // 进度为0的时候不更新
            if (fd.isProgress() && progress > 0) {
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO((int) flowId, fd);
                fdao.setCwsProgress(progress);
                fdao.save();
            }

            MessageDb md = new MessageDb();
            String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
            MyActionDb mad = new MyActionDb();
            if (!myActionId.equals("")) {
                mad = mad.getMyActionDb(Long.parseLong(myActionId));
            }
            if (isSecret != 0) {//如果是隐藏“评论”，只提醒发起“意见”的人
                if (!replyName.equals(userName)) {//如果发起“意见”的人不是自己，就提醒
                    if (!myActionId.equals("")) {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), userRealName + "对意见：" + mad.getResult() + "发表了评论：<p>" + replyContent + "</p>", myAction);
                    } else {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), userRealName + "发表了评论：<p>" + replyContent + "</p>", myAction);
                    }
                }
            } else {
                //如果不是隐藏“评论”，提醒所有参与流程的人
                //解析得到参与流程的所有人
                String allUserListSql = "select distinct user_name from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
                ResultIterator ri1 = jt.executeQuery(allUserListSql);
                ResultRecord rr1 = null;
                while (ri1.hasNext()) {
                    rr1 = (ResultRecord) ri1.next();
                    partakeUsers += rr1.getString(1) + ",";
                }
                if (!partakeUsers.equals("")) {
                    partakeUsers = partakeUsers.substring(0, partakeUsers.length() - 1);
                }
                String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
                for (String user : partakeUsersArr) {
                    //如果不是自己就提醒
                    if (!user.equals(userName)) {
                        if (!myActionId.equals("")) {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), userRealName + "对意见：" + mad.getResult() + "发表了评论：<p>" + replyContent + "</p>", myAction);
                        } else {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), userRealName + "发表了评论：<p>" + replyContent + "</p>", myAction);
                        }
                    }
                }
            }

            json.put("ret", "1");
            json.put("myDate", currentDate);

            StringBuilder sr = new StringBuilder();
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());

            String othersHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "othersHidden");
            String needHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "needHidden");
            String replyTo = SkinUtil.LoadStr(request, "res.flow.Flow", "replyTo");
            String sure = SkinUtil.LoadStr(request, "res.flow.Flow", "sure");
            if (wpd.isLight()) {
                if (parentId == -1) {
                    sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
                            .append(replyUser.getRealName()).append(":</td>")
                            .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
                            .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                            .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
                            // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
                            .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>");

                    sr.append("<tr><td colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                            .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
                            .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                            .append("<input type=hidden name=myActionId value= >")
                            .append("<input type=hidden name=discussId value=").append(id).append(" >")
                            .append("<input type=hidden name=flow_id value=").append(flowId).append(" >")
                            .append("<input type=hidden name=action_id value=").append(actionId).append(" >")
                            .append("<input type=hidden name=user_name value=").append(userName).append(" >")
                            .append("<input type=hidden name=userRealName value=").append(userRealName).append(" >")
                            .append("<input type=hidden name=reply_name value=").append(replyName).append(" >")
                            .append("<input type=hidden name=parent_id value=").append(id).append(" >")
                            .append("<input type=hidden name=discussId value=").append(parentId).append(" >")
                            .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                            .append(id).append(",").append(id).append(") />")
                            .append("</form></div></td></tr>");
                } else {
                    sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append(replyUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(oldUser.getRealName()).append(":</td>	")
                            .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
                            .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                            .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");

                    sr.append("<tr><td colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                            .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
                            .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                            .append("<input type=hidden name=myActionId value= >")
                            .append("<input type=hidden name=discussId value=").append(id).append(" >")
                            .append("<input type=hidden name=flow_id value=").append(flowId).append(" >")
                            .append("<input type=hidden name=action_id value=").append(actionId).append(" >")
                            .append("<input type=hidden name=user_name value=").append(replyName).append(" >")
                            .append("<input type=hidden name=userRealName value=").append(userRealName).append(" >")
                            .append("<input type=hidden name=reply_name value=").append(replyName).append(" >")
                            .append("<input type=hidden name=parent_id value=").append(parentId).append(" >")
                            .append("<input type=hidden name=discussId value=").append(parentId).append(" >")
                            .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                            .append(id).append(",").append(parentId).append(") />")
                            .append("</form></div></td></tr>");
                }
            } else {
                if (parentId == -1) {
                    sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
                            .append(replyUser.getRealName()).append(":</td>")
                            .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
                            .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                            .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
                            // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
                            .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>");

                    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                            .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
                            .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                            .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret0\" name=\"isSecret0\" value=\"0\"/></span>")
                            .append("<input type=hidden id=myActionId").append(id).append(" value= >")
                            .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")
                            .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
                            .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
                            .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
                            .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
                            .append("<input type=hidden id=reply_name").append(id).append(" value=").append(replyName).append(" >")
                            .append("<input type=hidden id=parent_id").append(id).append(" value=").append(id).append(" >")
                            .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
                            .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
                            .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                            .append(id).append(",").append(id).append(") />")
                            .append("</form></div></td></tr>");
                } else {
                    sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append(oldUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(replyUser.getRealName()).append(":</td>	")
                            .append("<td style=\"text-align:left;\">").append(replyContent).append("</td>")
                            .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                            .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                            .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");

                    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                            .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
                            .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                            .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret").append(id).append("\" name=\"isSecret").append(id).append("\" value=\"0\"/></span>")
                            .append("<input type=hidden id=myActionId").append(id).append(" value= >")
                            .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")
                            .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
                            .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
                            .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
                            .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
                            .append("<input type=hidden id=reply_name").append(id).append(" value=").append(userName).append(" >")
                            .append("<input type=hidden id=parent_id").append(id).append(" value=").append(parentId).append(" >")
                            .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
                            .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
                            .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                            .append(id).append(",").append(parentId).append(") />")
                            .append("</form></div></td></tr>");
                }
            }

            json.put("result", sr.toString());
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 在flow_modify.jsp、flow_dispose_free.jsp中回复
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/addReplyDispose", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addReplyDispose(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
        long flowId = ParamUtil.getLong(request, "flow_id", -1);//当前流程id
        long actionId = ParamUtil.getLong(request, "action_id", -1);//当前流程action的id
        String replyContent = ParamUtil.get(request, "content");//“评论”的内容
        String userRealName = ParamUtil.get(request, "userRealName");//发起“评论”人真实姓名
        String userName = ParamUtil.get(request, "user_name");
        String replyName = ParamUtil.get(request, "reply_name");
        int parentId = ParamUtil.getInt(request, "parent_id", -1);

        UserMgr um = new UserMgr();
        UserDb oldUser = um.getUserDb(userName);
        UserDb replyUser = um.getUserDb(replyName);

        String partakeUsers = "";
        int isSecret = ParamUtil.getInt(request, "isSecret", 0);//此“评论”是否隐藏
        //将数据插入flow_annex附言表中
        long id = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String myDate = currentDate;

        int progress = ParamUtil.getInt(request, "cwsProgress", 0);

        StringBuilder sql = new StringBuilder("insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(");
        sql.append(id).append(",").append(flowId).append(",").append(StrUtil.sqlstr(replyContent))
                .append(",").append(StrUtil.sqlstr(userName)).append(",").append(StrUtil.sqlstr(replyName))
                .append(",").append(StrUtil.sqlstr(myDate)).append(",").append(actionId).append(",").append(isSecret).append(",").append(parentId).append(",").append(progress).append(")");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            jt.executeUpdate(sql.toString());

            //不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
            WorkflowDb wf = new WorkflowDb((int) flowId);

            // 写入进度
            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            String formCode = lf.getFormCode();
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            // 进度为0的时候不更新
            if (fd.isProgress() && progress > 0) {
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO((int) flowId, fd);
                fdao.setCwsProgress(progress);
                fdao.save();
            }

            MessageDb md = new MessageDb();
            String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
            MyActionDb mad = new MyActionDb();
            if (!myActionId.equals("")) {
                mad = mad.getMyActionDb(Long.parseLong(myActionId));
            }
            if (isSecret != 0) {//如果是隐藏“评论”，只提醒发起“意见”的人
                if (!replyName.equals(userName)) {//如果发起“意见”的人不是自己，就提醒
                    if (!myActionId.equals("")) {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), userRealName + "对意见：" + mad.getResult() + "发表了评论：<p>" + replyContent + "</p>", myAction);
                    } else {
                        md.sendSysMsg(replyName, "请注意查看我的流程：" + wf.getTitle(), userRealName + "发表了评论：<p>" + replyContent + "</p>", myAction);
                    }
                }
            } else {
                //如果不是隐藏“评论”，提醒所有参与流程的人
                //解析得到参与流程的所有人
                String allUserListSql = "select distinct user_name from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
                ResultIterator ri1 = jt.executeQuery(allUserListSql);
                ResultRecord rr1 = null;
                while (ri1.hasNext()) {
                    rr1 = (ResultRecord) ri1.next();
                    partakeUsers += rr1.getString(1) + ",";
                }
                if (!partakeUsers.equals("")) {
                    partakeUsers = partakeUsers.substring(0, partakeUsers.length() - 1);
                }
                String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
                for (String user : partakeUsersArr) {
                    //如果不是自己就提醒
                    if (!user.equals(userName)) {
                        if (!myActionId.equals("")) {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), userRealName + "对意见：" + mad.getResult() + "发表了评论：<p>" + replyContent + "</p>", myAction);
                        } else {
                            md.sendSysMsg(user, "请注意查看我的流程：" + wf.getTitle(), userRealName + "发表了评论：<p>" + replyContent + "</p>", myAction);
                        }
                    }
                }
            }

            json.put("ret", "1");
            json.put("myDate", currentDate);

            String othersHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "othersHidden");
            String needHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "needHidden");
            String replyTo = SkinUtil.LoadStr(request, "res.flow.Flow", "replyTo");
            String sure = SkinUtil.LoadStr(request, "res.flow.Flow", "sure");
            StringBuilder sr = new StringBuilder();
            if (parentId == -1) {
                sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
                        .append(replyUser.getRealName()).append(":</td>")
                        .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
                        .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                        .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
                        // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
                        .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>");

                sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                        .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                        .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret0\" name=\"isSecret0\" value=\"0\"/></span>")
                        .append("<input type=hidden id=myActionId").append(id).append(" value= >")
                        .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")
                        .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
                        .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
                        .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
                        .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
                        .append("<input type=hidden id=reply_name").append(id).append(" value=").append(replyName).append(" >")
                        .append("<input type=hidden id=parent_id").append(id).append(" value=").append(id).append(" >")
                        .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
                        .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
                        .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                        .append(id).append(",").append(id).append(") />")
                        .append("</div></td></tr>");
            } else {
                sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(oldUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(replyUser.getRealName()).append(":</td>	")
                        .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
                        .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
                        .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");

                sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
                        .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
                        .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret").append(id).append("\" name=\"isSecret").append(id).append("\" value=\"0\"/></span>")
                        .append("<input type=hidden id=myActionId").append(id).append(" value= >")
                        .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")
                        .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
                        .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
                        .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
                        .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
                        .append("<input type=hidden id=reply_name").append(id).append(" value=").append(userName).append(" >")
                        .append("<input type=hidden id=parent_id").append(id).append(" value=").append(parentId).append(" >")
                        .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
                        .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
                        .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
                        .append(id).append(",").append(parentId).append(") />")
                        .append("</div></td></tr>");
            }

            json.put("result", sr.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/finishAction", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String finishAction(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            // 将场景置为流程
            threadContext.setSceneFlow();
            json = workflowService.finishAction(request, new com.redmoon.oa.pvg.Privilege());
        }
        catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            DebugUtil.i(getClass(), "finishAction", StrUtil.trace(e));
            // 置为异常状态
            threadContext.setAbnormal(true);
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            json.put("op", "Exception occured in finishAction");
        }
        finally {
            // 清缓存
            threadContext.remove();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/finishActionFree", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String finishActionFree(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            // 将场景置为流程
            threadContext.setSceneFlow();
            json = workflowService.finishActionFree(request, new com.redmoon.oa.pvg.Privilege());
        } catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // 置为异常状态
            threadContext.setAbnormal(true);
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            json.put("op", "");
            e.printStackTrace();
        } finally {
            // 清缓存
            threadContext.remove();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/favorite", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String favorite(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        boolean re;
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        WorkflowFavoriteDb wfd = new WorkflowFavoriteDb();
        try {
            long flowId = ParamUtil.getLong(request, "flowId");
            if (wfd.isExist(privilege.getUser(request), flowId)) {
                String str = LocalUtil.LoadString(request, "res.flow.Flow", "processBeConcerned");
                throw new ErrMsgException(str);
            }
            re = wfd.create(new JdbcTemplate(), new Object[]{new Long(flowId), privilege.getUser(request), new java.util.Date(), new Integer(0)});
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            return json.toString();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str);
        } else {
            json.put("ret", "0");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/unfavorite", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String unfavorite(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        WorkflowFavoriteDb wfd = new WorkflowFavoriteDb();
        try {
            long flowId = ParamUtil.getLong(request, "flowId");
            wfd = wfd.getWorkflowFavoriteDb(privilege.getUser(request), new Long(flowId));
            if (wfd != null) {
                re = wfd.del();
            } else {
                String str = LocalUtil.LoadString(request, "res.flow.Flow", "notAlreadyExist");
                throw new ErrMsgException(str);
            }

        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            // e.printStackTrace();
            return json.toString();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            // e.printStackTrace();
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str);
        } else {
            json.put("ret", "0");
            String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/refreshFlow", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String refreshFlow(HttpServletRequest request) {
        boolean re = false;
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        try {
            wf.refreshFlow();
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            return json.toString();
        }

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/refreshFlowBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String refreshFlowBatch(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String ids = ParamUtil.get(request, "ids");
        String[] ary = StrUtil.split(ids, ",");
        for (int i = 0; i < ary.length; i++) {
            int flowId = StrUtil.toInt(ary[i], -1);

            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            try {
                wf.refreshFlow();
            } catch (ErrMsgException e) {
                e.printStackTrace();
                json.put("ret", "0");
                json.put("msg", e.getMessage().replace("\\r", "<BR />"));
                return json.toString();
            }
        }

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/discardFlowBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String discardFlowBatch(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String ids = ParamUtil.get(request, "ids");
        String[] ary = StrUtil.split(ids, ",");
        String userName = privilege.getUser(request);
        for (int i = 0; i < ary.length; i++) {
            int flowId = StrUtil.toInt(ary[i], -1);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            try {
                wf.discard(userName);
            } catch (ErrMsgException e) {
                e.printStackTrace();
                json.put("ret", "0");
                json.put("msg", e.getMessage().replace("\\r", "<BR />"));
                return json.toString();
            }
        }

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    /**
     * 调试模块中配置应用属性
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/applyProps", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String applyProps(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String fieldWrite = ParamUtil.get(request, "fieldWrite");
        String fieldHide = ParamUtil.get(request, "fieldHide");
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        int actionId = ParamUtil.getInt(request, "actionId", -1);

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        WorkflowActionDb wad = new WorkflowActionDb();
        wad = wad.getWorkflowActionDb(actionId);

        WorkflowDb wfDefault = new WorkflowDb();
        wfDefault.setFlowString(wpd.getFlowString());
        wfDefault.setFlowJson(wpd.getFlowJson());

        WorkflowActionDb waDefault = null;

        try {
            java.util.Vector v = wfDefault.getActionsFromString(wfDefault.getFlowString());
            java.util.Iterator ir = v.iterator();
            while (ir.hasNext()) {
                WorkflowActionDb wa = (WorkflowActionDb) ir.next();
                if (wa.getInternalName().equals(wad.getInternalName())) {
                    wa.setFieldWrite(fieldWrite);
                    wa.setFieldHide(fieldHide);

                    String item2 = wa.generateItem2();
                    wa.setItem2(item2);

                    waDefault = wa;
                    break;
                }
            }

            if (waDefault != null) {
                wfDefault.renewWorkflowString(waDefault, false);

                wpd.setFlowString(wfDefault.getFlowString());
                DebugUtil.i(getClass(), "applyProps", wfDefault.getFlowJson());
                wpd.setFlowJson(wfDefault.getFlowJson());
                wpd.save();
            }
            wf.refreshFlow();
        } catch (ErrMsgException e) {
            e.printStackTrace();
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            return json.toString();
        }

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/runValidateScript", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String runValidateScript(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        int actionId = ParamUtil.getInt(request, "actionId", -1);
        FormDAOMgr fdm = new FormDAOMgr();
        BSHShell shell = null;
        try {
            shell = fdm.runValidateScript(request, flowId, actionId);
            json.put("ret", "1");
            if (shell == null) {
                json.put("msg", "请检查脚本是否存在！");
            } else {
                String errDesc = shell.getConsole().getLogDesc();
                json.put("msg", StrUtil.toHtml(errDesc));
            }
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            shell = fdm.getBshShell();
            String errDesc = "";
            if (shell != null) {
                errDesc = shell.getConsole().getLogDesc();
            }
            if (!"".equals(errDesc)) {
                errDesc += "\r\n" + e.getMessage();
            } else {
                errDesc = e.getMessage();
            }
            json.put("msg", StrUtil.toHtml(errDesc));
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/runFinishScript", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String runFinishScript(HttpServletRequest request, int flowId, int actionId) throws ErrMsgException {
        return workflowService.runFinishScript(request, flowId, actionId).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/runDeliverScript", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String runDeliverScript(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        long myActionId = ParamUtil.getInt(request, "myActionId", -1);

        BSHShell shell = null;

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());

        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(flowId, fd);

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int) mad.getActionId());

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
        String script = wpm.getActionFinishScript(wpd.getScripts(), wa.getInternalName());

        if (script != null && !"".equals(script.trim())) {
            WorkflowMgr wm = new WorkflowMgr();
            shell = wm.runDeliverScript(request, privilege.getUser(request), wf, fdao, mad, script, true);
        }

        json.put("ret", "1");
        if (shell == null) {
            json.put("msg", "请检查脚本是否存在！");
        } else {
            String errDesc = shell.getConsole().getLogDesc().trim();
            json.put("msg", StrUtil.toHtml(errDesc));
        }

        return json.toString();
    }

    /**
     * 恢复流程
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/recover", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String recover(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int flowId = ParamUtil.getInt(request, "flowId", -1);

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        wf.setStatus(WorkflowDb.STATUS_STARTED);
        wf.save();

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/delAnnex", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delAnnex(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        long annexId = ParamUtil.getLong(request, "annexId", -1);
        WorkflowAnnexDb wad = new WorkflowAnnexDb();
        wad = (WorkflowAnnexDb) wad.getQObjectDb(new Long(annexId));
        try {
            wad.del();
        } catch (ResKeyException e) {
            e.printStackTrace();
            json.put("ret", "0");
            json.put("msg", e.getMessage().replace("\\r", "<BR />"));
            return json.toString();
        }

        json.put("ret", "1");
        String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/getTree", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getTree(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Leaf lf = new Leaf();
        lf = lf.getLeaf("root");
        DirectoryView dv = new DirectoryView(lf);
        StringBuffer opts = new StringBuffer();
        try {
            dv.getDirectoryAsOptions(request, lf, lf.getLayer(), opts);
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return opts.toString();
    }

    /**
     * 所有用户 字母A-Z排序
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/initUserList", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String initUserList() {
        net.sf.json.JSONObject json = new net.sf.json.JSONObject();
        FlowDoMgr flowDoMgr = new FlowDoMgr();
        json = flowDoMgr.usersInitList();
        return json.toString();
    }

    /**
     * 发起流程列表初始化 listview 字母 A-Z排序
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/flow/initFlowTypeList", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String initFlowTypeList(String skey) {
        Privilege pvg = new Privilege();
        boolean re = pvg.auth(request);
        net.sf.json.JSONObject json = new net.sf.json.JSONObject();
        if (!re) {
            json.put("ret", "0");
            json.put("msg", "权限非法！");
            return json.toString();
        }
        FlowDoMgr flowDoMgr = new FlowDoMgr();
        json = flowDoMgr.flowInitList(request, pvg.getUserName());
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/createNestSheetRelated", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String createNestSheetRelated() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            // 将场景置为流程
            threadContext.setSceneFlow();
            json = workflowService.createNestSheetRelated(request);
        }
        catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // 置为异常状态
            threadContext.setAbnormal(true);
            e.printStackTrace();
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            json.put("op", "");
        }
        finally {
            // 清缓存
            threadContext.remove();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/updateNestSheetRelated", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String updateNestSheetRelated() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            // 将场景置为流程
            threadContext.setSceneFlow();
            json = workflowService.updateNestSheetRelated(request);
        }
        catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // 置为异常状态
            threadContext.setAbnormal(true);
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        finally {
            // 清缓存
            threadContext.remove();
        }

        String str = json.toJSONString();

        return json.toJSONString();
    }


    /**
     * 删除嵌套表中的附件
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/delNestSheetRelated", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delNestSheetRelated() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String formCodeRelated = ParamUtil.get(request, "formCodeRelated");

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserManage(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        String moduleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(moduleCode)) {
            moduleCode = formCodeRelated;
        }

        FormMgr fm = new FormMgr();
        FormDb fdRelated = fm.getFormDb(formCodeRelated);
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
        try {
            boolean isNestSheet = true;
            if (fdm.del(request, isNestSheet, moduleCode)) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            // e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 删除嵌套表中的附件
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/flow/delAttachForNestSheetRelated", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delAttachForNestSheetRelated() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
        if ("".equals(formCode)) {
            json.put("ret", "0");
            json.put("msg", "编码不能为空！");
            return json.toString();
        }

        String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserManage(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        long attachId = ParamUtil.getLong(request, "attachId", -1);
        if (attachId == -1) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json.toString();
        }

        boolean re = false;
        com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
        if (att.isLoaded()) {
            re = att.del();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }
}
