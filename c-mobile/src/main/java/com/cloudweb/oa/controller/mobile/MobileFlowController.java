package com.cloudweb.oa.controller.mobile;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.INestSheetCtl;
import com.cloudweb.oa.api.IWorkflowUtil;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.service.WorkflowService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.ThreadContext;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.stamp.StampDb;
import com.redmoon.oa.stamp.StampPriv;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/mini")
public class MobileFlowController {
    private static int RES_SUCCESS = 0;                      //成功
    private static int RES_FAIL = -1;                        //失败
    private static int RES_EXPIRED = -2;                     //SKEY过期

    private final static String OFFICE_EQUIPMENT = "office_equipment";
    private final static String RELATE_SELECT = "relate_select";//联动选择 办公用品 库存的

    @Autowired
    HttpServletRequest request;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ThreadContext threadContext;

    @Autowired
    private IWorkflowUtil workflowUtil;

    @ResponseBody
    @RequestMapping(value = "/flow/listToDo", produces = {"application/json;charset=UTF-8;"})
    public String listToDo(@RequestParam(defaultValue = "") String showyear,
                           @RequestParam(defaultValue = "") String showmonth,
                           @RequestParam(defaultValue = "") String op,
                           @RequestParam(defaultValue = "") String title,
                           Integer pagenum,
                           Integer pagesize) {
        JSONObject json = new JSONObject();
        String beginDate = "", endDate = "";
        if (!"".equals(showyear) && !"".equals(showmonth)) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, StrUtil.toInt(showyear));
            cal.set(Calendar.MONTH, StrUtil.toInt(showmonth) - 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            beginDate = DateUtil.format(cal, "yyyy-MM-dd");
            cal.add(Calendar.MONTH, 1);
            endDate = DateUtil.format(cal, "yyyy-MM-dd");
        }

        String myname = SpringUtil.getUserName();

        MyActionDb mad = new MyActionDb();
        WorkflowDb wfd = new WorkflowDb();

        Config cfg = Config.getInstance();
        boolean isFlowProxy = cfg.getBooleanProperty("isFlowProxy");

        String sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname);
        if (isFlowProxy) {
            sql += " or m.proxy=" + StrUtil.sqlstr(myname);
        }
        sql += ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " + WorkflowDb.STATUS_DELETED + " and (is_checked=0 or is_checked=2) and sub_my_action_id=0";
        if (op.equals("search")) {
            sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<>" + WorkflowDb.STATUS_DELETED + " and f.status<>" + WorkflowDb.STATUS_DISCARDED + " and (m.user_name=" + StrUtil.sqlstr(myname);
            if (isFlowProxy) {
                sql += " or m.proxy=" + StrUtil.sqlstr(myname);
            }
            sql += ") and (is_checked=0 or is_checked=2) and sub_my_action_id=0";
            if (!title.equals("")) {
                if (StrUtil.isNumeric(title)) {
                    sql += " and f.id=" + title;
                } else {
                    sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
                }
            }
            if (!beginDate.equals("")) {
                sql += " and f.mydate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
            }
            if (!endDate.equals("")) {
                sql += " and f.mydate<" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
            }
        }

        sql += " order by receive_date desc";
        //LogUtil.getLog(getClass()).info("sql = " + sql);
        int curpage = pagenum; // 第几页
        ListResult lr;
        try {
            lr = mad.listResult(sql, curpage, pagesize);
            long total = lr.getTotal();

            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("total", String.valueOf(total));

            Vector v = lr.getResult();
            Iterator ir = null;
            if (v != null) {
                ir = v.iterator();
            }
            JSONObject result = new JSONObject();
            result.put("count", String.valueOf(pagesize));

            Leaf lf = new Leaf();

            JSONArray flows = new JSONArray();
            while (ir.hasNext()) {
                mad = (MyActionDb) ir.next();

                wfd = wfd.getWorkflowDb((int) mad.getFlowId());

                lf = lf.getLeaf(wfd.getTypeCode());
                if (lf == null) {
                    lf = new Leaf();
                    continue;
                }

                JSONObject flow = new JSONObject();
                flow.put("myActionId", String.valueOf(mad.getId()));
                flow.put("flowId", String.valueOf(mad.getFlowId()));
                flow.put("name", StringEscapeUtils.unescapeHtml3(wfd.getTitle()));
                flow.put("status", WorkflowActionDb.getStatusName(mad.getActionStatus()));
                flow.put("beginDate", DateUtil.format(wfd.getBeginDate(), "MM-dd HH:mm"));
                flow.put("type", String.valueOf(lf.getType()));
                flow.put("typeName", lf.getName());

                flows.put(flow);
            }
            result.put("flows", flows);
            json.put("result", result);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/listDone", produces = {"application/json;charset=UTF-8;"})
    public String listDone(String op,
                           String title,
                           Integer pagenum,
                           Integer pagesize) {
        JSONObject json = new JSONObject();

        String myname = SpringUtil.getUserName();

        WorkflowDb wf = new WorkflowDb();
        Config cfg = Config.getInstance();
        boolean isFlowProxy = cfg.getBooleanProperty("isFlowProxy");

        String sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname);
        if (isFlowProxy) {
            sql += " or proxy=" + StrUtil.sqlstr(myname);
        }
        sql += ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " + WorkflowDb.STATUS_DELETED;

        if ("search".equals(op)) {
            sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name=" + StrUtil.sqlstr(myname);
            if (isFlowProxy) {
                sql += " or m.proxy=" + StrUtil.sqlstr(myname);
            }
            sql += ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " + WorkflowDb.STATUS_DELETED;
            if (!"".equals(title) && title != null) {
                if (StrUtil.isNumeric(title)) {
                    sql += " and f.id=" + title;
                } else {
                    sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
                }
            }
        }
        sql += " order by flow_id desc";
        //LogUtil.getLog(getClass()).info("sql = " + sql);
        int curpage = pagenum;   //第几页
        try {
            ListResult lr = wf.listResult(sql, curpage, pagesize);
            long total = lr.getTotal();
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("total", String.valueOf(total));
            Vector v = lr.getResult();
            Iterator ir = null;
            if (v != null)
                ir = v.iterator();
            JSONObject result = new JSONObject();
            result.put("count", String.valueOf(pagesize));
            MyActionDb mad = new MyActionDb();
            UserMgr um = new UserMgr();
            Leaf lf = new Leaf();
            JSONArray flows = new JSONArray();
            while (ir.hasNext()) {
                WorkflowDb wfd = (WorkflowDb) ir.next();

                lf = lf.getLeaf(wfd.getTypeCode());
                if (lf == null) {
                    lf = new Leaf();
                }

                JSONObject flow = new JSONObject();
                flow.put("flowId", String.valueOf(wfd.getId()));
                flow.put("name", StringEscapeUtils.unescapeHtml3(wfd.getTitle()));
                flow.put("status", wfd.getStatusDesc());
                flow.put("beginDate", DateUtil.format(wfd.getBeginDate(), "MM-dd HH:mm"));
                flow.put("type", lf.getCode());
                flow.put("typeName", lf.getName());

                String lastUser = "";

                sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " and is_checked<>" + MyActionDb.CHECK_STATUS_CHECKED + " order by id desc";

                Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
                if (ir2.hasNext()) {
                    mad = (MyActionDb) ir2.next();
                    lastUser = um.getUserDb(mad.getUserName()).getRealName();
                }

                flow.put("lastUser", lastUser);

                flows.put(flow);
            }
            result.put("flows", flows);
            json.put("result", result);

        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    public JSONArray getCwsWorkflowResultDetail(long flowId, long myActionId, String username) {
        JSONArray jsonArr = new JSONArray();
        UserDb userDb = new UserDb();
        String processListSql = "select id from flow_my_action where flow_id="
                + flowId + " and id <>" + myActionId
                + " order by receive_date asc";
        MyActionDb mad = new MyActionDb();
        Vector vProcess = mad.list(processListSql);
        Iterator ir = vProcess.iterator();
        WorkflowAnnexMgr workFlowAnnex = new WorkflowAnnexMgr();
        while (ir.hasNext()) {
            MyActionDb child_mad = (MyActionDb) ir.next();
            JSONObject obj = new JSONObject();
            try {
                userDb = userDb.getUserDb(child_mad.getUserName());
                String content = MyActionMgr.renderResultForMobile(child_mad);
                obj.put("result", StringEscapeUtils.unescapeHtml3(content));
                obj.put("photo", StrUtil.getNullStr(userDb.getPhoto()));
                obj.put("readDate", DateUtil.format(child_mad.getReadDate(),
                        "MM-dd HH:mm"));
                obj.put("userName", userDb.getRealName());
                obj.put("gender", userDb.getGender());
                obj.put("myActionId", child_mad.getId());
                obj.put("annexs", workFlowAnnex.getFlowAnnex(child_mad.getId(), username, 0));
                jsonArr.put(obj);
            } catch (JSONException e) {
                log.error("@详情列表" + e.getMessage());
            }
        }
        return jsonArr;
    }

    @ResponseBody
    @RequestMapping(value = "/flow/dispose", produces = {"application/json;charset=UTF-8;"})
    public String dispose(Long myActionId) {
        JSONObject json = new JSONObject();
        JSONArray fields = new JSONArray();
        JSONObject result = new JSONObject();
        JSONArray users = new JSONArray();
        JSONArray files = new JSONArray();
        Privilege privilege = new Privilege();
        boolean hasAttach = true;
        String username = SpringUtil.getUserName();
        boolean isProgress = false;
        WorkflowAnnexMgr workflowAnnexMgr = new WorkflowAnnexMgr();
        int progress = 0;
        String res = "0";
        String msg = "操作成功";
        try {
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            if (!mad.isLoaded()) {
                res = "-1";
                msg = "出现错误，请检查预设流程是否存在！";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS) {
                res = "-1";
                msg = "流程节点已由其他人员处理，不需要再处理";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
                res = "-1";
                msg = "待办流程已因被返回而忽略，不需要再处理！";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_TRANSFER) {
                res = "-1";
                msg = "	流程已转办，不需要再处理!";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            }

            if (!mad.isReaded()) {
                mad.setReadDate(new Date());
                mad.setReaded(true);
                mad.save();
            }
            long flowId = mad.getFlowId();
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb((int) flowId);
            // 锁定流程
            WorkflowMgr wfm = new WorkflowMgr();
            wfm.lock(wf, SpringUtil.getUserName());
            com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            if (lf == null) {
                res = "-1";
                msg = "流程目录不存在";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            }

            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
            boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
            MacroCtlMgr mm = new MacroCtlMgr();

            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());
            hasAttach = fd.isHasAttachment();
            isProgress = fd.isProgress();

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(wf.getId(), fd);

            progress = fdao.getCwsProgress();
            Vector<FormField> v = fdao.getFields();

            // 表单域选择控件实时映射
            boolean isRealTime = false;
            for (FormField ff : v) {
                if ("macro".equals(ff.getType())) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    mu.getIFormMacroCtl().setMyActionId(myActionId);
                    String macroCode = mu.getCode();

                    if ("module_field_select".equals(macroCode)) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                        if (jsonObj != null) {
                            if (jsonObj.getBoolean("isRealTime")) {
                                // 自动映射
                                if (!StringUtils.isEmpty(ff.getValue())) {
                                    isRealTime = true;
                                    moduleFieldSelectCtl.autoMap(request, (int) flowId, ff.getValue(), ff);
                                }
                            }
                        }
                    }
                }
            }
            if (isRealTime) {
                fdao = fdao.getFormDAO(wf.getId(), fd);
            }

            UserMgr um = new UserMgr();

            // 这段用来验证字段是否可写
            WorkflowActionDb wa = new WorkflowActionDb();
            int actionId = (int) mad.getActionId();
            wa = wa.getWorkflowActionDb(actionId);
            if (wa == null || !wa.isLoaded()) {
                res = "-1";
                msg = "流程中的相应动作不存在";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            }
            String userName = SpringUtil.getUserName();
            WorkflowPredefineDb wfp = new WorkflowPredefineDb();
            wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
            if (wa.getStatus() == WorkflowActionDb.STATE_DOING || wa.getStatus() == WorkflowActionDb.STATE_RETURN) {
                ;
            } else {
                // 有可能会是重激活的情况，或者是异或聚合的情况
                if (!wfp.isReactive() && !wa.isXorAggregate()) {
                    mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
                    mad.setCheckDate(new java.util.Date());
                    mad.save();
                    res = "-1";
                    msg = "流程节点已处理";
                    json.put("res", res);
                    json.put("msg", msg);
                    return json.toString();
                }
            }

            String strIsShowNextUsers = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isShowNextUsers");
            boolean isShowNextUsers = !"0".equals(strIsShowNextUsers);

            String strIsBtnSaveShow = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isBtnSaveShow");
            boolean isBtnSaveShow = !"0".equals(strIsBtnSaveShow);

            String btnAgreeName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnAgreeName");
            String btnRefuseName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnRefuseName");

            // 取可写表单域
            String fieldWrite = StrUtil.getNullString(wa.getFieldWrite()).trim();
            String[] fds = fieldWrite.split(",");
            int len = fds.length;

            // 自由流程根据用户所属的角色，得到可写表单域
            if (lf.getType() == Leaf.TYPE_FREE) {
                fds = wfp.getFieldsWriteOfUser(wf, userName);
                len = fds.length;
            }

            String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();
            // 将不显示的字段加入fieldHide
            Iterator<FormField> ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
                    if ("".equals(fieldHide)) {
                        fieldHide = ff.getName();
                    } else {
                        fieldHide += "," + ff.getName();
                    }
                }
            }
            String[] fdsHide = fieldHide.split(",");
            int lenHide = fdsHide.length;

            MacroCtlUnit mu;
            ir = v.iterator();
            json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
            // json.put("cwsWorkflowResult",mad.getResult());
            json.put("cwsWorkflowTitle", wf.getTitle());
            json.put("status", wf.getStatusDesc());
            json.put("actionId", String.valueOf(actionId));
            json.put("myActionId", String.valueOf(myActionId));
            json.put("flowId", String.valueOf(flowId));
            json.put("formCode", fdao.getFormCode());
            json.put("isShowNextUsers", isShowNextUsers);
            json.put("isBtnSaveShow", isBtnSaveShow);
            json.put("btnAgreeName", btnAgreeName);
            json.put("btnRefuseName", btnRefuseName);

            String flowTypeName = lf.getName();
            json.put("flowTypeName", flowTypeName);
            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());

            boolean isLight = wfd.isLight();
            json.put("isLight", isLight);// 判断时候是@liuchen
            if (isLight) {
                JSONArray jsonArr = getCwsWorkflowResultDetail(flowId,
                        myActionId, username);
                json.put("lightDetail", jsonArr);
            } else {
                result.put("annexs", workflowAnnexMgr.getFlowAnnex(0, username, flowId));
            }

            json.put("isFree", lf.getType() == Leaf.TYPE_FREE);

            com.redmoon.oa.pvg.Privilege p = new com.redmoon.oa.pvg.Privilege();
            if (p.isUserPrivValid(request, "sms")) {
                json.put("isSms", "true");
            } else {
                json.put("isSms", "false");
            }
            // 能否拒绝
            json.put("canDecline", String.valueOf(wa.canDecline()));

            Vector<WorkflowActionDb> returnv = wa.getLinkReturnActions();
            boolean canReturn;
            // 如果当前为发起节点则不能退回
            if (wf.getStartActionId() == mad.getActionId()) {
                canReturn = false;
            } else {
                canReturn = returnv.size() > 0
                        || wfp.getReturnStyle() == WorkflowPredefineDb.RETURN_STYLE_FREE;
            }

            json.put("canReturn", String.valueOf(canReturn));

            json.put("url", "public/flow_dispose_do.jsp"); // 似乎已无用 20210706

            // fgf 20170519
            json.put("isProgress", fd.isProgress());
            json.put("isReply", wfd.isReply());

            // fgf 20180415
            json.put("progress", progress);

            // fgf 20180814 显示控制
            String viewJs = workflowUtil.doGetViewJSMobile(request, fd, fdao, userName, false);
            json.put("viewJs", viewJs);

            // 判断删除按钮是否显示
            boolean isReadOnly = false;
            if (wa.getKind() == WorkflowActionDb.KIND_READ) {
                isReadOnly = true;
            }
            boolean canDel = false;
            com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();
            if (conf.getIsDisplay("FLOW_BUTTON_DEL")) {
                if (WorkflowMgr.canDelFlowOnAction(request, wf, wa, mad)) {
                    canDel = true;
                }
            }
            json.put("canDel", canDel);

            // 结束流程标志
            boolean canFinishAgree = false;
            if (conf.getIsDisplay("FLOW_BUTTON_FINISH")) {
                String flag = wa.getFlag();
                if (wf.isStarted() && flag.length() >= 12 && "1".equals(flag.substring(11, 12))) {
                    if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND) {
                        canFinishAgree = true;
                    }
                }
            }
            json.put("canFinishAgree", canFinishAgree);

            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                String val = fdao.getFieldValue(ff.getName());
                boolean finded = false;
                for (int i = 0; i < len; ++i) {
                    if (ff.getName().equals(fds[i])) {
                        finded = true;
                        break;
                    }
                }
                if (!(finded)) {
                    ff.setEditable(false);
                }

                // 如果不是自由流程
                if (lf.getType() != Leaf.TYPE_FREE) {
                    // 置隐藏表单域
                    finded = false;
                    for (String s : fdsHide) {
                        if (ff.getName().equals(s)) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        ff.setHidden(true);
                    }
                }

                JSONObject field = new JSONObject();
                field.put("title", ff.getTitle());
                field.put("code", ff.getName());
                field.put("desc", StrUtil.getNullStr(ff.getDescription()));

                field.put("present", StrUtil.getNullStr(ff.getPresent()));
                // 如果是计算控件，则取出精度和四舍五入属性
                if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                    FormParser fp = new FormParser();
                    String isroundto5 = fp.getFieldAttribute(fd, ff, "isroundto5");
                    String digit = fp.getFieldAttribute(fd, ff, "digit");
                    field.put("formula", ff.getDefaultValueRaw());
                    field.put("isroundto5", isroundto5);
                    field.put("digit", digit);
                }

                String options = "";
                String macroType = "";
                String controlText = "";
                String macroCode = "";
                JSONArray js = new JSONArray();
                JSONArray opinionArr = null;
                JSONObject opinionVal = null;

                String metaData = "";
                if ("macro".equals(ff.getType())) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu == null) {
                        log.error("MactoCtl " + ff.getTitle() + "：" + ff.getMacroType() + " is not exist.");
                        continue;
                    }

                    macroType = mu.getIFormMacroCtl().getControlType();
                    mu.getIFormMacroCtl().setMyActionId(myActionId);
                    macroCode = mu.getCode();

                    IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                    ifmc.setIFormDAO(fdao);

                    // 如果值为null，则在json中put的时候，是无效的，不会被记录至json中
                    controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
                            .getControlText(userName, ff));
                    val = StrUtil.getNullStr(mu.getIFormMacroCtl()
                            .getControlValue(userName, ff));
                    // 须放在此位置，因为在当前用户宏控件的getContextValue中更改了ff的值
                    metaData = mu.getIFormMacroCtl().getMetaData(ff);

                    options = ifmc.getControlOptions(userName, ff);
                    if (options != null && !"".equals(options)) {
                        // options = options.replaceAll("\\\"", "");
                        js = new JSONArray(options);
                    }
                }
                // 判断是否为意见输入框
                if (macroCode != null && !"".equals(macroCode)) {
                    if ("macro_opinion".equals(macroCode) || "macro_opinionex".equals(macroCode)) {
                        if (controlText != null
                                && !"".equals(controlText.trim())) {
                            opinionArr = new JSONArray(controlText);
                        }
                        if (val != null && !"".equals(val.trim())) {
                            opinionVal = new JSONObject(val);
                        }
                    }

                    if ("nest_sheet".equals(macroCode) || "nest_table".equals(macroCode) || "macro_detaillist_ctl".equals(macroCode)) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        INestSheetCtl ntc = macroCtlService.getNestSheetCtl();
                        JSONObject jsonObj = ntc.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    } else if ("module_field_select".equals(macroCode)) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    }
                }
                field.put("type", ff.getType());
                if ("select".equals(ff.getType())) {
                    String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        String[] optionsItem = optionsArray[i];
                        if (optionsItem != null && optionsItem.length == 2) {
                            JSONObject option = new JSONObject();
                            option.put("value", optionsItem[0]);
                            option.put("name", optionsItem[1]);
                            js.put(option);
                        }
                    }
                } else if ("radio".equals(ff.getType())) {
                    FormParser fp = new FormParser();
                    // options = fp.getOptionsOfSelect(fd, ff);
                    String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        JSONObject option = new JSONObject();
                        option.put("value", optionsArray[i]);
                        option.put("name", optionsArray[i]);
                        js.put(option);
                    }
                }
                String level = "";
                if ("checkbox".equals(ff.getType())) {
                    // level = "个人兴趣";
                    level = ff.getTitle();
                }
                field.put("options", js);
                if (opinionVal != null) {
                    field.put("value", opinionVal);
                } else {
                    field.put("value", val);
                }
                if (opinionArr != null && opinionArr.length() > 0) {
                    field.put("text", opinionArr);
                } else {
                    field.put("text", controlText);
                }
                // log.info(ff.getTitle() +
                // " controlText=" + controlText);
                field.put("level", level);
                field.put("macroType", macroType);
                field.put("editable", String.valueOf(ff.isEditable()));
                field.put("isHidden", String.valueOf(ff.isHidden()));
                field.put("isNull", String.valueOf(ff.isCanNull()));
                field.put("fieldType", ff.getFieldTypeDesc());
                field.put("macroCode", macroCode);
                // 可传SQL控件条件中的字段
                field.put("metaData", metaData);
                field.put("isReadonly", ff.isReadonly());
                fields.put(field);
            }

            String downPath = "";
            int doc_id = wf.getDocId();
            DocumentMgr dm = new DocumentMgr();
            Document doc = dm.getDocument(doc_id);
            if (doc != null) {
                java.util.Vector<IAttachment> attachments = doc.getAttachments(1);
                Iterator<IAttachment> irAtt = attachments.iterator();
                while (irAtt.hasNext()) {
                    IAttachment am = irAtt.next();
                    JSONObject file = new JSONObject();
                    boolean isPreview = false;
                    boolean isHtml = false;
                    String ext = StrUtil.getFileExt(am.getDiskName());
                    if (canOfficeFilePreview) {
                        if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
                            isPreview = true;
                            isHtml = true;
                        }
                    }
                    if (canPdfFilePreview && ext.equals("pdf")) {
                        isPreview = true;
                        isHtml = true;
                    }
                    if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)) {
                        isPreview = true;
                    }

                    if (isPreview) {
                        if (isHtml) {
                            String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
                            String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
                            java.io.File fileExist = new java.io.File(htmlfile);
                            if (fileExist.exists()) {
                                file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
                            }
                        } else {
                            file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
                        }
                    }

                    file.put("id", am.getId());

                    file.put("canDel", wa.canDelAttachment());

                    file.put("name", am.getName());
                    // downPath = "public/flow_getfile.jsp?" + "flowId=" + flowId + "&attachId=" + am.getId();
                    downPath = "public/android/getFile.do?" + "flowId=" + flowId + "&attachId=" + am.getId();

                    file.put("url", downPath);
                    file.put("size", String.valueOf(am.getSize()));
                    files.put(file);
                }
            }

            // 置异或发散
            StringBuffer condBuf = new StringBuffer();
            boolean flagXorRadiate = wa.isXorRadiate();
            Vector vMatched = null;
            if (flagXorRadiate) {
                vMatched = WorkflowRouter.matchNextBranch(wa, userName, condBuf, myActionId);
                String conds = condBuf.toString();
                boolean hasCond = !conds.equals(""); // 是否含有条件
                flagXorRadiate = hasCond;
            }
            json.put("flagXorRadiate", String.valueOf(flagXorRadiate));
            WorkflowRuler wr = new WorkflowRuler();

            // 取得下一步提交的用户
            Iterator<UserDb> userir = null;
            Vector<WorkflowActionDb> vto = wa.getLinkToActions();
            Iterator<WorkflowActionDb> toir = vto.iterator();
            while (toir.hasNext()) {
                WorkflowActionDb towa = (WorkflowActionDb) toir.next();
                if (towa.getJobCode().equals(
                        WorkflowActionDb.PRE_TYPE_USER_SELECT)
                        || towa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
                    boolean isStragegyGoDown = towa.isStrategyGoDown(); // 是否为下达

                    JSONObject user = new JSONObject();
                    user.put("actionTitle", towa.getTitle());
                    user.put("roleName", towa.getJobName());
                    user.put("internalname", towa.getInternalName());
                    user.put("name", "WorkflowAction_" + towa.getId());
                    // 手机客户端还不能区分是否在所管理的部门范围内
                    user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
                    user.put("realName", "自选用户");
                    user.put("isSelectable", "true");

                    // 如果节点上曾经选过人，则在手机客户端默认选中
                    user.put("actionUserName", towa.getUserName());
                    user.put("actionUserRealName", towa.getUserRealName());

                    // 标志位，能否选择用户
                    boolean canSelUser = wr.canUserSelUser(request, towa);
                    // towa.getUserRealName() + " canSelUser=" + canSelUser);
                    user.put("canSelUser", String.valueOf(canSelUser));
                    user.put("isGoDown", String.valueOf(isStragegyGoDown));

                    users.put(user);
                } else {
                    boolean isStrategySelectable = towa.isStrategySelectable();
                    boolean isStrategySelected = towa.isStrategySelected();
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    Vector<UserDb> vuser = workflowRouter.matchActionUser(request, towa, wa, false, null);
                    userir = vuser.iterator();
                    while (userir.hasNext()) {
                        UserDb ud = (UserDb) userir.next();
                        JSONObject user = new JSONObject();
                        user.put("actionTitle", towa.getTitle());
                        user.put("roleName", towa.getJobName());
                        user.put("internalname", towa.getInternalName());
                        user.put("name", "WorkflowAction_" + towa.getId());
                        user.put("value", ud.getName());
                        user.put("realName", ud.getRealName());
                        user.put("isSelectable", String.valueOf(isStrategySelectable));
                        user.put("isSelected", String.valueOf(isStrategySelected));

                        // 标志位，能否选择用户
                        boolean canSelUser = wr.canUserSelUser(request, towa);
                        user.put("canSelUser", String.valueOf(canSelUser));
                        users.put(user);
                    }
                }
            }
        } catch (JSONException e) {
            res = "-1";
            msg = "JSON解析异常";
            log.error(e.getMessage());
        } catch (ErrMsgException e1) {
            res = "-1";
            msg = e1.getMessage();
            log.error(e1.getMessage());
        } catch (MatchUserException e2) {
            res = "0";
            // 手机端在选择后将会post至FlowMultiDeptAction再次匹配人员
            msg = "手机端兼职处理";
            String userName = SpringUtil.getUserName();
            DeptUserDb dud = new DeptUserDb();
            Vector<DeptDb> vu = dud.getDeptsOfUser(userName);
            Iterator<DeptDb> irdu = vu.iterator();
            JSONArray deptArr = new JSONArray();
            try {
                while (irdu.hasNext()) {
                    DeptDb dept = irdu.next();
                    if (dept.isHide()) {
                        continue;
                    }
                    String code = dept.getCode();
                    String name = dept.getName();

                    if (!dept.getParentCode().equals(DeptDb.ROOTCODE) && !dept.getCode().equals(DeptDb.ROOTCODE)) {
                        name = dept.getDeptDb(dept.getParentCode()).getName() + "->" + dept.getName();
                    }
                    JSONObject deptObj = new JSONObject();
                    deptObj.put("name", name);
                    deptObj.put("code", code);
                    deptArr.put(deptObj);
                }
                result.put("multiDepts", deptArr);
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            LogUtil.getLog(getClass()).error(e2);
        } finally {
            try {
                result.put("users", users);
                result.put("fields", fields);
                result.put("files", files);
                json.put("result", result);
                json.put("res", res);
                json.put("msg", msg);
                // 是否允许上传附件
                json.put("hasAttach", hasAttach);
            } catch (final JSONException e) {
                log.error(e.getMessage());
            }
        }
        return json.toString();
    }

    public JSONArray getCwsWorkflowResultDetail(long flowId, String userName) {
        WorkflowAnnexMgr wfam = new WorkflowAnnexMgr();
        JSONArray jsonArr = new JSONArray();
        UserMgr um = new UserMgr();
        UserDb userDb = new UserDb();
        String processListSql = "select id from flow_my_action where flow_id="
                + flowId + " order by receive_date asc";
        MyActionDb mad = new MyActionDb();
        Vector vProcess = mad.list(processListSql);
        Iterator ir = vProcess.iterator();
        while (ir.hasNext()) {
            MyActionDb pmad = (MyActionDb) ir.next();
            JSONObject obj = new JSONObject();
            try {
                userDb = userDb.getUserDb(pmad.getUserName());

                String content = MyActionMgr.renderResultForMobile(pmad);
                obj.put("result", StringEscapeUtils.unescapeHtml3(content));
                obj.put("readDate", DateUtil.format(pmad.getReadDate(), "MM-dd HH:mm"));
                obj.put("userName", userDb.getRealName());
                obj.put("photo", StrUtil.getNullStr(userDb.getPhoto()));
                obj.put("gender", userDb.getGender());
                obj.put("myActionId", pmad.getId());
                obj.put("annexs", wfam.getFlowAnnex(pmad.getId(), userName, 0));

                jsonArr.put(obj);
            } catch (JSONException e) {
                log.error("@详情列表" + e.getMessage());
            }
        }
        return jsonArr;
    }

    @ResponseBody
    @RequestMapping(value = "/flow/show", produces = {"application/json;charset=UTF-8;"})
    public String show(@RequestParam(required = true) Integer flowId) {
        JSONObject json = new JSONObject();
        JSONObject result = new JSONObject();
        int progress = 0;
        WorkflowAnnexMgr wfam = new WorkflowAnnexMgr();
        MacroCtlUnit mu;
        MacroCtlMgr mm = new MacroCtlMgr();
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        String userName = SpringUtil.getUserName();

        //原来语句中有一个and canList=1 ，这参数不知道啥用
        String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList from form_field where formCode=? ";
        sql += " order by orders desc";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            if (lf == null) {
                try {
                    json.put("res", "-1");
                    json.put("msg", "流程目录不存在");
                    return json.toString();
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());

            boolean isProgress = fd.isProgress();

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(wf.getId(), fd);

            // 表单域选择控件实时映射
            boolean isRealTime = false;
            for (FormField ff : fdao.getFields()) {
                if ("macro".equals(ff.getType())) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if ("module_field_select".equals(mu.getCode())) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                        if (jsonObj != null) {
                            if (jsonObj.getBoolean("isRealTime")) {
                                // 自动映射
                                if (!StringUtils.isEmpty(ff.getValue())) {
                                    isRealTime = true;
                                    moduleFieldSelectCtl.autoMap(request, (int) flowId, ff.getValue(), ff);
                                }
                            }
                        }
                    }
                }
            }
            if (isRealTime) {
                fdao = fdao.getFormDAO(wf.getId(), fd);
            }

            progress = fdao.getCwsProgress();

            UserMgr um = new UserMgr();
            mm = new MacroCtlMgr();
            ri = jt.executeQuery(sql, new Object[]{lf.getFormCode()});
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
            json.put("createDate", DateUtil.format(wf.getMydate(), "yyyy-MM-dd HH:mm:ss"));
            json.put("status", wf.getStatusDesc());
            json.put("flowTypeName", lf.getName());

            // fgf 20170519
            json.put("isProgress", fd.isProgress());

            // fgf 20180415
            json.put("progress", progress);

            // fgf 20180814 显示控制
            String viewJs = workflowUtil.doGetViewJSMobile(request, fd, fdao, userName, true);
            json.put("viewJs", viewJs);

            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());

            json.put("isReply", wfd.isReply());

            boolean isLight = wfd.isLight();
            json.put("isLight", isLight);//判断时候是@liuchen
            if (isLight) {
                if (flowId != null) {
                    JSONArray jsonArr = getCwsWorkflowResultDetail(flowId, userName);
                    json.put("lightDetail", jsonArr);
                }
            } else {
                result.put("annexs", wfam.getFlowAnnex(0, userName, flowId));
            }

            JSONArray fields = new JSONArray();

            boolean isHideField = true;
            String fieldHide = "";
            String[] fdsHide = null;
            // 将不显示的字段加入fieldHide
            Iterator ir = fd.getFields().iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getHide() == FormField.HIDE_ALWAYS) {
                    if ("".equals(fieldHide)) {
                        fieldHide = ff.getName();
                    } else {
                        fieldHide += "," + ff.getName();
                    }
                }
            }

            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDbOfFlow(wf.getId(), userName);
            // 管理员查看时，其本人可能并未参与流程，则mad将为null
            if (mad != null) {
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb((int) mad.getActionId());

                String fHide = StrUtil.getNullString(wad.getFieldHide()).trim();
                if ("".equals(fieldHide)) {
                    fieldHide = fHide;
                } else {
                    fieldHide += "," + fHide;
                }
            }

            fdsHide = StrUtil.split(fieldHide, ",");

            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();

                // FormField ff = fd.getFormField(rr.getString("name"));

                FormField ff = fdao.getFormField(rr.getString("name"));
                if (isHideField) {
                    boolean isShow = true;
                    if (fdsHide != null) {
                        for (int i = 0; i < fdsHide.length; i++) {
                            if (!fdsHide[i].startsWith("nest.")) {
                                if (fdsHide[i].equals(ff.getName())) {
                                    isShow = false;
                                    break;
                                }
                            } else {
                                isShow = false;
                                break;
                            }
                        }

                        if (!isShow) {
                            continue;
                        }
                    }
                }

                // String val = ff.getValue();
                String val = ff.convertToHtml();
                // 用于当前用户宏控件判断如果为空，则不显示当前用户
                ff.setEditable(false);

                JSONObject field = new JSONObject();

                String macroCode = "";
                JSONArray jsonArr = null;
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        val = mu.getIFormMacroCtl().getControlText(userName, ff);
                        macroCode = mu.getCode();
                        if (macroCode != null && !macroCode.equals("")) {
                            if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
                                if (!val.equals("")) {
                                    jsonArr = new JSONArray(val);
                                } else {
                                    jsonArr = new JSONArray();
                                }
                            }

                            if ("nest_sheet".equals(macroCode) || "nest_table".equals(macroCode) || "macro_detaillist_ctl".equals(macroCode)) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                INestSheetCtl ntc = macroCtlService.getNestSheetCtl();
                                JSONObject jsonObj = ntc.getCtlDescription(ff);
                                if (jsonObj != null) {
                                    field.put("desc", jsonObj);
                                } else {
                                    field.put("desc", ff.getDescription());
                                }
                            } else if (macroCode.equals("module_field_select")) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                                com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                                if (jsonObj != null) {
                                    field.put("desc", jsonObj);
                                } else {
                                    field.put("desc", ff.getDescription());
                                }
                            }
                        }
                    }
                }
                field.put("title", rr.getString("title"));
                field.put("code", ff.getName());
                if (jsonArr != null) {
                    field.put("value", jsonArr);
                } else {
                    field.put("value", val);
                }
                field.put("type", rr.getString("type"));
                String level = "";
                if (ff.getType().equals("checkbox")) {
                    // level = "个人兴趣";
                    level = ff.getTitle();
                }
                field.put("level", level);
                field.put("macroCode", macroCode);
                fields.put(field);
            }

            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
            boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");

            // 文件附件
            JSONArray files = new JSONArray();
            String downPath = "";
            int doc_id = wf.getDocId();
            DocumentMgr dm = new DocumentMgr();
            Document doc = dm.getDocument(doc_id);
            if (doc != null) {
                java.util.Vector attachments = doc.getAttachments(1);
                ir = attachments.iterator();
                while (ir.hasNext()) {
                    Attachment am = (Attachment) ir.next();
                    JSONObject file = new JSONObject();

                    boolean isPreview = false;
                    boolean isHtml = false;
                    String ext = StrUtil.getFileExt(am.getDiskName());
                    log.info(" ext=" + ext + " canOfficeFilePreview=" + canOfficeFilePreview + " isHtml=" + isHtml);
                    if (canOfficeFilePreview) {
                        if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
                            isPreview = true;
                            isHtml = true;
                        }
                    }
                    if (canPdfFilePreview && ext.equalsIgnoreCase("pdf")) {
                        isPreview = true;
                        isHtml = true;
                    }
                    if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)) {
                        isPreview = true;
                    }

                    if (isPreview) {
                        if (isHtml) {
                            String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
                            String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
                            java.io.File fileExist = new java.io.File(htmlfile);

                            log.info("fileExist.exists()=" + fileExist.exists());
                            if (fileExist.exists()) {
                                file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
                            }
                        } else {
                            file.put("preview", "public/flow_att_preview.jsp?attachId=" + am.getId());
                        }
                    }

                    file.put("name", am.getName());
                    // downPath = "public/flow_getfile.jsp?"+"flowId="+flowId+"&attachId="+am.getId();
                    downPath = "public/android/getFile.do?" + "flowId=" + flowId + "&attachId=" + am.getId();
                    file.put("url", downPath);
                    file.put("size", String.valueOf(am.getSize()));
                    files.put(file);
                }
            }
            result.put("fields", fields);
            result.put("files", files);
            json.put("result", result);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/getlist", produces = {"application/json;charset=UTF-8;"})
    public String getlist(@RequestParam(required = true) String skey) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);

        Directory dir = new Directory();
        Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
        DirectoryView dv = new DirectoryView(rootlf);
        Vector children;
        try {
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("root", rootlf.getName());
            children = dir.getChildren(Leaf.CODE_ROOT);
            Iterator ri = children.iterator();
            JSONObject result = new JSONObject();
            JSONArray parentNames = new JSONArray();
            while (ri.hasNext()) {
                Leaf childlf = (Leaf) ri.next();

                JSONArray childNames = new JSONArray();

                if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
                    Iterator ir = dir.getChildren(childlf.getCode()).iterator();
                    while (ir.hasNext()) {
                        Leaf chlf = (Leaf) ir.next();
                        if (chlf.isOpen() && chlf.isMobileStart() && dv.canUserSeeWhenInitFlow(request, chlf)) {
                            JSONObject parentName = new JSONObject();
                            parentName.put("parentName", chlf.getName());
                            if (chlf.getType() != Leaf.TYPE_NONE) {
                                parentName.put("parentCode", chlf.getCode());
                                parentName.put("parentType", chlf.getType());
                            }
                            parentName.put("childNames", childNames);
                            parentNames.put(parentName);
                        }
                    }
                }
            }
            result.put("parentNames", parentNames);
            json.put("result", result);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/init", produces = {"application/json;charset=UTF-8;"})
    public String init(String code, Integer type, String mutilDept) {
        JSONObject json = new JSONObject();
        JSONArray fields = new JSONArray();
        JSONObject result = new JSONObject();
        Privilege privilege = new Privilege();
        String res = "0";
        String msg = "操作成功";
        long startActionId = 0;
        boolean hasAttach = true;
        try {
            String userName = SpringUtil.getUserName();
            // 加入对默认标题的处理 fgf 2015-1-2
            com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            lf = lf.getLeaf(code);
            com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
            String flowTitle = WorkflowMgr.makeTitle(request, pvg, lf);
            MacroCtlMgr mm = new MacroCtlMgr();

            WorkflowMgr wm = new WorkflowMgr();
            if (type == Leaf.TYPE_FREE) {
                startActionId = wm.initWorkflowFree(userName, code, flowTitle, -1, 0);
            } else {
                startActionId = wm.initWorkflow(userName, code, flowTitle, -1, 0);
            }
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(startActionId);

            long flowId = mad.getFlowId();
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb((int) flowId);

            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());
            hasAttach = fd.isHasAttachment();

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(wf.getId(), fd);

            // 先自动映射
            Vector<FormField> v = fdao.getFields();
            Iterator<FormField> ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();
                if ("macro".equals(ff.getType())) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu == null) {
                        log.error(
                                "MactoCtl " + ff.getTitle() + "："
                                        + ff.getMacroType() + " is not exist.");
                        continue;
                    }

                    String macroCode = mu.getCode();
                    if ("module_field_select".equals(macroCode)) {
                        String strDesc = StrUtil.getNullStr(ff.getDescription());
                        // 向下兼容
                        if ("".equals(strDesc)) {
                            strDesc = ff.getDefaultValueRaw();
                        }

                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        strDesc = moduleFieldSelectCtl.formatJSONString(strDesc);
                        try {
                            JSONObject jsonField = new JSONObject(strDesc);
                            String value = StrUtil.getNullStr(ff.getValue());
                            if ("".equals(value) || value.equals(ff.getDefaultValueRaw()) || value.equals(ff.getDescription())) {
                                if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
                                    // 来自于指定的参数名称
                                    value = ParamUtil.get(request, json.getString("requestParam"));
                                } else {
                                    // 默认以字段名作为参数从request中获取
                                    value = ParamUtil.get(request, ff.getName());
                                }
                                // 如果value中存在值，则说明需自动映射
                                if (!"".equals(value)) {
                                    moduleFieldSelectCtl.autoMap(request, (int) flowId, value, ff);
                                }
                            }
                        } catch (JSONException e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                    }
                }
            }

            // 重新再载入一次，以免缓存问题
            fdao.load();

            UserMgr um = new UserMgr();
            // 这段用来验证字段是否可写
            WorkflowActionDb wa = new WorkflowActionDb();
            int actionId = (int) mad.getActionId();
            wa = wa.getWorkflowActionDb(actionId);
            if (wa == null || !wa.isLoaded()) {
                res = "-1";
                msg = "流程中的相应动作不存在";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            }

            WorkflowPredefineDb wfp = new WorkflowPredefineDb();
            wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

            String strIsShowNextUsers = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isShowNextUsers");
            boolean isShowNextUsers = !"0".equals(strIsShowNextUsers);

            String strIsBtnSaveShow = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isBtnSaveShow");
            boolean isBtnSaveShow = !"0".equals(strIsBtnSaveShow);

            String btnAgreeName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnAgreeName");
            String btnRefuseName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnRefuseName");


            // 取可写表单域
            String fieldWrite = StrUtil.getNullString(wa.getFieldWrite()).trim();
            String[] fds = fieldWrite.split(",");
            int len = fds.length;

            // 自由流程根据用户所属的角色，得到可写表单域
            if (lf.getType() == Leaf.TYPE_FREE) {
                WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
                wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

                fds = wfpd.getFieldsWriteOfUser(wf, userName);
                len = fds.length;
            }

            String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();
            // 将不显示的字段加入fieldHide
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
                    if ("".equals(fieldHide)) {
                        fieldHide = ff.getName();
                    } else {
                        fieldHide += "," + ff.getName();
                    }
                }
            }
            String[] fdsHide = fieldHide.split(",");
            int lenHide = fdsHide.length;

            MacroCtlUnit mu;
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
            json.put("cwsWorkflowTitle", wf.getTitle());
            json.put("actionId", String.valueOf(actionId));
            json.put("myActionId", String.valueOf(startActionId));
            json.put("flowId", String.valueOf(flowId));
            json.put("url", "public/flow_dispose_do.jsp");
            json.put("formCode", fdao.getFormCode());
            json.put("isShowNextUsers", isShowNextUsers);
            json.put("isBtnSaveShow", isBtnSaveShow);
            json.put("btnAgreeName", btnAgreeName);
            json.put("btnRefuseName", btnRefuseName);

            // fgf 20180814 显示控制
            String viewJs = workflowUtil.doGetViewJSMobile(request, fd, fdao, userName, false);
            json.put("viewJs", viewJs);

            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
            boolean isLight = wfd.isLight();
            json.put("isLight", isLight);// 判断时候是@liuchen
            json.put("isFree", lf.getType() == Leaf.TYPE_FREE);

            boolean canDel = false;
            com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();
            if (conf.getIsDisplay("FLOW_BUTTON_DEL")) {
                if (WorkflowMgr.canDelFlowOnAction(request, wf, wa, mad)) {
                    canDel = true;
                }
            }
            json.put("canDel", canDel);

            // 结束流程标志
            boolean canFinishAgree = false;
            if (conf.getIsDisplay("FLOW_BUTTON_FINISH")) {
                String flag = wa.getFlag();
                if (wf.isStarted() && flag.length() >= 12 && "1".equals(flag.substring(11, 12))) {
                    if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND) {
                        canFinishAgree = true;
                    }
                }
            }
            json.put("canFinishAgree", canFinishAgree);

            // 遍历表单字段-------------------------------------------------
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();

                // 置可写表单域
                boolean finded = false;
                for (int i = 0; i < len; ++i) {
                    if (ff.getName().equals(fds[i])) {
                        finded = true;
                        break;
                    }
                }
                if (!(finded)) {
                    ff.setEditable(false);
                }

                // 如果不是自由流程
                if (lf.getType() != Leaf.TYPE_FREE) {
                    // 置隐藏表单域
                    finded = false;
                    for (String s : fdsHide) {
                        if (ff.getName().equals(s)) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        log.info(
                                "field:" + ff.getTitle() + " is hidden.");
                        ff.setHidden(true);
                    }
                }

                JSONObject field = new JSONObject();
                String desc = StrUtil.getNullStr(ff.getDescription());
                field.put("title", ff.getTitle());
                field.put("code", ff.getName());
                field.put("desc", desc);

                field.put("present", StrUtil.getNullStr(ff.getPresent()));
                // 如果是计算控件，则取出精度和四舍五入属性
                if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                    FormParser fp = new FormParser();
                    String isroundto5 = fp.getFieldAttribute(fd, ff, "isroundto5");
                    String digit = fp.getFieldAttribute(fd, ff, "digit");
                    field.put("formula", ff.getDefaultValueRaw());
                    field.put("isroundto5", isroundto5);
                    field.put("digit", digit);
                }
                String options = "";
                String macroType = "";
                String controlText = "";
                String controlValue = "";
                JSONArray opinionArr = null;
                JSONObject opinionVal = null;

                String macroCode = "";

                String metaData = "";

                JSONArray js = new JSONArray();
                if ("macro".equals(ff.getType())) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu == null) {
                        log.error("MactoCtl " + ff.getTitle() + "：" + ff.getMacroType() + " is not exist.");
                        continue;
                    }

                    IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                    ifmc.setIFormDAO(fdao);

                    macroCode = mu.getCode();

                    macroType = mu.getIFormMacroCtl().getControlType();
                    controlText = mu.getIFormMacroCtl().getControlText(userName, ff);
                    controlValue = mu.getIFormMacroCtl().getControlValue(userName, ff);

                    // 须放在此位置，因为在当前用户宏控件的getContextValue中更改了ff的值
                    metaData = mu.getIFormMacroCtl().getMetaData(ff);

                    options = ifmc.getControlOptions(userName, ff);
                    // options = options.replaceAll("\\\"", "");
                    if (options != null && !"".equals(options)) {
                        // options = options.replaceAll("\\\"", "");
                        try {
                            js = new JSONArray(options);
                        } catch (JSONException e) {
                            DebugUtil.e(getClass(), "execute", ff.getTitle() + " macro ctl's options cann't convert to JSONArray," + options);
                        }
                    }
                } else {
                    String fieldType = ff.getType();
                    if (fieldType != null && !"".equals(fieldType)) {
                        if (FormField.TYPE_DATE.equals(fieldType) || FormField.TYPE_DATE_TIME.equals(fieldType)) {
                            // 有可能已经通过表单域选择宏控件传值映射带过来了值
                            if (ff.getValue() != null && !"".equals(ff.getValue())) {
                                controlValue = ff.getValue();
                            } else {
                                controlValue = ff.getDefaultValueRaw();
                            }
                        } else {
                            if (ff.getValue() != null && !"".equals(ff.getValue())) {
                                controlValue = ff.getValue();
                            } else {
                                controlValue = ff.getDefaultValue();
                            }
                        }
                    } else {
                        controlValue = ff.getDefaultValue();
                    }
                }
                // 判断是否为意见输入框
                if (macroCode != null && !"".equals(macroCode)) {
                    if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
                        if (controlText != null
                                && !controlText.trim().equals("")) {
                            opinionArr = new JSONArray(controlText);
                        }
                        if (controlValue != null
                                && !controlValue.trim().equals("")) {
                            opinionVal = new JSONObject(controlValue);
                        }
                    }

                    if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        INestSheetCtl ntc = macroCtlService.getNestSheetCtl();
                        JSONObject jsonObj = ntc.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    } else if (macroCode.equals("module_field_select")) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    }
                }
                field.put("type", ff.getType());
                if (ff.getType().equals("select")) {
                    // options = fp.getOptionsOfSelect(fd, ff);
                    String[][] optionsArray = FormParser
                            .getOptionsArrayOfSelect(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        String[] optionsItem = optionsArray[i];
                        JSONObject option = new JSONObject();
                        try {
                            option.put("value", optionsItem[0]);
                            option.put("name", optionsItem[1]);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                        js.put(option);
                    }
                } else if (ff.getType().equals("radio")) {
                    // options = fp.getOptionsOfSelect(fd, ff);
                    String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        JSONObject option = new JSONObject();
                        option.put("value", optionsArray[i]);
                        option.put("name", optionsArray[i]);
                        js.put(option);
                    }
                }
                field.put("options", js);
                field.put("text", controlText);
                String level = "";
                if (ff.getType().equals("checkbox")) {
                    // level = "个人兴趣";
                    level = ff.getTitle();
                }
                field.put("level", level);
                if (macroType.equals("select") && desc.equals(OFFICE_EQUIPMENT)) {
                    field.put("macroType", RELATE_SELECT);
                } else {
                    field.put("macroType", macroType);
                }
                field.put("editable", String.valueOf(ff.isEditable()));
                field.put("isHidden", String.valueOf(ff.isHidden()));
                field.put("isNull", String.valueOf(ff.isCanNull()));
                field.put("fieldType", ff.getFieldTypeDesc());
                if (opinionVal != null) {
                    field.put("value", opinionVal);
                } else {
                    field.put("value", controlValue);
                }
                if (opinionArr != null && opinionArr.length() > 0) {
                    field.put("text", opinionArr);
                } else {
                    field.put("text", controlText);
                }
                field.put("macroCode", macroCode);

                // 可传SQL控件条件中的字段
                field.put("metaData", metaData);
                field.put("isReadonly", ff.isReadonly());

                fields.put(field);
            }
            // 遍历表单字段---------------------------------------------------------

            // 置异或发散
            StringBuffer condBuf = new StringBuffer();
            boolean flagXorRadiate = wa.isXorRadiate();
            Vector vMatched = null;
            if (flagXorRadiate) {
                vMatched = WorkflowRouter.matchNextBranch(wa, userName, condBuf, startActionId);
                String conds = condBuf.toString();
                boolean hasCond = conds.equals("") ? false : true; // 是否含有条件
                if (hasCond) {
                    flagXorRadiate = true;
                } else {
                    flagXorRadiate = false;
                }

            }
            json.put("flagXorRadiate", String.valueOf(flagXorRadiate));
            WorkflowRuler wr = new WorkflowRuler();
            // 取得下一步提交的用户--------------------------------------------------
            JSONArray users = new JSONArray();
            Vector vto = wa.getLinkToActions();
            Iterator toir = vto.iterator();
            Iterator userir = null;
            while (toir.hasNext()) {
                WorkflowActionDb towa = (WorkflowActionDb) toir.next();
                if (towa.getJobCode().equals(
                        WorkflowActionDb.PRE_TYPE_USER_SELECT)) {
                    JSONObject user = new JSONObject();
                    user.put("actionTitle", towa.getTitle());
                    user.put("roleName", towa.getJobName());
                    user.put("internalname", towa.getInternalName());
                    user.put("name", "WorkflowAction_" + towa.getId());
                    user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
                    user.put("realName", "自选用户");
                    user.put("isSelectable", "true");
                    // 标志位，能否选择用户
                    boolean canSelUser = wr.canUserSelUser(request, towa);
                    user.put("canSelUser", String.valueOf(canSelUser));

                    boolean isStragegyGoDown = towa.isStrategyGoDown(); // 是否为下达
                    user.put("isGoDown", String.valueOf(isStragegyGoDown));

                    users.put(user);
                } else {
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    Vector vuser = workflowRouter.matchActionUser(request, towa, wa, false, mutilDept);
                    userir = vuser.iterator();
                    boolean isStrategySelectable = towa.isStrategySelectable();
                    boolean isStrategySelected = towa.isStrategySelected();

                    while (userir != null && userir.hasNext()) {
                        UserDb ud = (UserDb) userir.next();
                        JSONObject user = new JSONObject();
                        user.put("actionTitle", towa.getTitle());
                        user.put("roleName", towa.getJobName());
                        user.put("internalname", towa.getInternalName());
                        user.put("name", "WorkflowAction_" + towa.getId());
                        user.put("value", ud.getName());
                        user.put("realName", ud.getRealName());
                        user.put("isSelectable", String.valueOf(isStrategySelectable));
                        user.put("isSelected", String.valueOf(isStrategySelected));

                        // 标志位，能否选择用户
                        boolean canSelUser = wr.canUserSelUser(request, towa);
                        user.put("canSelUser", String.valueOf(canSelUser));
                        // " 3 actionUserRealName=" + towa.getUserRealName() +
                        // " canSelUser=" + canSelUser);
                        users.put(user);
                    }
                }
            }
            result.put("users", users);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
            res = "-1";
            msg = "JSON解析异常";
        } catch (ErrMsgException e1) {
            res = "-1";
            msg = e1.getMessage();
            LogUtil.getLog(getClass()).error(e1.getMessage());
        } catch (MatchUserException e2) {
            res = "0";
            msg = "手机端兼职处理";
            String userName = SpringUtil.getUserName();
            DeptUserDb dud = new DeptUserDb();
            Vector vu = dud.getDeptsOfUser(userName);
            Iterator irdu = vu.iterator();
            JSONArray deptArr = new JSONArray();
            try {
                while (irdu.hasNext()) {
                    DeptDb dept = (DeptDb) irdu.next();
                    if (dept.isHide()) {
                        continue;
                    }
                    String deptCode = dept.getCode();
                    String name = dept.getName();
                    if (!dept.getParentCode().equals(DeptDb.ROOTCODE) && !dept.getCode().equals(DeptDb.ROOTCODE)) {
                        name = dept.getDeptDb(dept.getParentCode()).getName() + "->" + dept.getName();
                    }
                    JSONObject deptObj = new JSONObject();
                    deptObj.put("name", name);
                    deptObj.put("code", deptCode);
                    deptArr.put(deptObj);
                }
                result.put("multiDepts", deptArr);
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            LogUtil.getLog(getClass()).error(e2);
        } finally {
            try {
                json.put("res", res);
                json.put("msg", msg);
                result.put("fields", fields);
                json.put("result", result);
                json.put("hasAttach", hasAttach);

            } catch (final JSONException e) {
                log.error(e.getMessage());
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/getReturnUsers", produces = {"application/json;charset=UTF-8;"})
    public String getReturnUsers(int flowId, long myActionId) {
        JSONObject json = new JSONObject();

        long actionId = myActionId;
        WorkflowMgr wfm = new WorkflowMgr();
        WorkflowDb wf = wfm.getWorkflowDb(flowId);

        WorkflowPredefineDb wfp = new WorkflowPredefineDb();
        wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

        try {
            json.put("res", "0");
            json.put("msg", "操作成功");
            JSONArray users = new JSONArray();
            if (wfp.getReturnStyle() == WorkflowPredefineDb.RETURN_STYLE_FREE) {

                String sql = "select id from flow_my_action where flow_id="
                        + flowId + " and is_checked<>" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date asc";
                MyActionDb mad = new MyActionDb();
                Vector v = mad.list(sql);
                Iterator ir = v.iterator();
                Map map = new HashMap();
                WorkflowActionDb wa = new WorkflowActionDb();
                while (ir.hasNext()) {
                    mad = (MyActionDb) ir.next();

                    // 防止用户重复
                    if (map.get(mad.getUserName()) == null) {
                        map.put(mad.getUserName(), mad.getUserName());
                    } else {
                        continue;
                    }

                    long aId = mad.getActionId();
                    if (map.get("" + aId) != null) {
                        continue;
                    }
                    map.put("" + aId, "" + aId);
                    wa = wa.getWorkflowActionDb((int) aId);
                    // 去除本节点
                    if (actionId == aId) {
                        continue;
                    }

                    JSONObject user = new JSONObject();
                    user.put("id", String.valueOf(wa.getId()));
                    user.put("name", wa.getUserRealName());
                    user.put("actionTitle", wa.getTitle());
                    users.put(user);
                }
            } else {
                WorkflowActionDb wa = new WorkflowActionDb();
                MyActionDb mad = new MyActionDb();
                mad = mad.getMyActionDb(actionId);
                wa = wa.getWorkflowActionDb((int) mad.getActionId());

                Vector returnv = wa.getLinkReturnActions();

                Iterator returnir = returnv.iterator();
                while (returnir.hasNext()) {
                    WorkflowActionDb returnwa = (WorkflowActionDb) returnir
                            .next();
                    if (returnwa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
                        JSONObject user = new JSONObject();
                        user.put("id", String.valueOf(returnwa.getId()));
                        user.put("name", returnwa.getUserRealName());
                        user.put("actionTitle", returnwa.getTitle());
                        users.put(user);
                    }
                }
            }
            json.put("users", users);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/multiDept", produces = {"application/json;charset=UTF-8;"})
    public String multiDept(@RequestParam(required = true) String skey, String deptCode, Long myActionId) {
        JSONObject json = new JSONObject();
        String res = "0";
        String msg = "操作成功";
        Privilege privilege = new Privilege();
        JSONObject result = new JSONObject();
        boolean re = privilege.Auth(skey);
        try {
            if (re) {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            }

            privilege.doLogin(request, skey);

            // 这段用来验证字段是否可写
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            WorkflowActionDb wa = new WorkflowActionDb();
            int actionId = (int) mad.getActionId();
            wa = wa.getWorkflowActionDb(actionId);
            if (wa == null || !wa.isLoaded()) {
                res = "-1";
                msg = "流程中的相应动作不存在";
                json.put("res", res);
                json.put("msg", msg);
                return json.toString();
            }
            // 取得下一步提交的用户
            JSONArray users = new JSONArray();
            Vector vto = wa.getLinkToActions();
            Iterator toir = vto.iterator();
            Iterator userir = null;
            WorkflowRuler wr = new WorkflowRuler();
            while (toir.hasNext()) {
                WorkflowActionDb towa = (WorkflowActionDb) toir.next();
                if (towa.getJobCode().equals(
                        WorkflowActionDb.PRE_TYPE_USER_SELECT)
                        || towa
                        .getJobCode()
                        .equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
                    JSONObject user = new JSONObject();
                    user.put("actionTitle", towa.getTitle());
                    user.put("roleName", towa.getJobName());
                    user.put("internalname", towa.getInternalName());
                    user.put("name", "WorkflowAction_" + towa.getId());
                    // 手机客户端还不能区分是否在所管理的部门范围内
                    user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
                    user.put("realName", "自选用户");
                    user.put("isSelectable", "true");

                    // 如果节点上曾经选过人，则在手机客户端默认选中
                    user.put("actionUserName", towa.getUserName());
                    user.put("actionUserRealName", towa.getUserRealName());

                    // 标志位，能否选择用户
                    boolean canSelUser = wr.canUserSelUser(request, towa);
                    // towa.getUserRealName() + " canSelUser=" + canSelUser);
                    user.put("canSelUser", String.valueOf(canSelUser));

                    users.put(user);
                } else {
                    boolean isStrategySelectable = towa.isStrategySelectable();
                    if (deptCode != null) {
                        deptCode = deptCode.trim(); // @android存在bug，多了一个空格
                    }
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    Vector vuser = workflowRouter.matchActionUser(request, towa, wa, false, deptCode);
                    userir = vuser.iterator();
                    while (userir != null && userir.hasNext()) {
                        UserDb ud = (UserDb) userir.next();
                        JSONObject user = new JSONObject();
                        user.put("actionTitle", towa.getTitle());
                        user.put("roleName", towa.getJobName());
                        user.put("internalname", towa.getInternalName());
                        user.put("name", "WorkflowAction_" + towa.getId());
                        user.put("value", ud.getName());
                        user.put("realName", ud.getRealName());
                        user.put("isSelectable", String
                                .valueOf(isStrategySelectable));

                        // 标志位，能否选择用户
                        boolean canSelUser = wr.canUserSelUser(request, towa);
                        user.put("canSelUser", String.valueOf(canSelUser));
                        users.put(user);
                    }
                }
            }
            result.put("users", users);
        } catch (ErrMsgException e) {
            res = "-1";
            msg = "服务器端异常";
            log.error(e.getMessage());
        } catch (JSONException e) {
            res = "-1";
            msg = "JSON解析异常";
            log.error(e.getMessage());
        } catch (MatchUserException e) {
            res = "-1";
            msg = "下一步处理用户获取失败";
            log.error(e.getMessage());
        } finally {
            try {
                json.put("result", result);
                json.put("res", res);
                json.put("msg", msg);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/flow/addReply", produces = {"application/json;charset=UTF-8;"})
    public String addReply(@RequestParam(required = true) String skey, Integer flow_id, Long myActionId, String content, Integer is_secret, Integer progress) {
        boolean flag = true;
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if (re) {
            try {
                jReturn.put("res", RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        WorkflowAnnexMgr workflowAnnexMgr = new WorkflowAnnexMgr();
        String name = privilege.getUserName(skey);
        try {
            String sql = "insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret) values(?,?,?,?,?,?,?,?)";
            long id = SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
            if (myActionId != 0) {
                //@流程
                MyActionDb mad = new MyActionDb(myActionId);
                flow_id = (int) mad.getFlowId();
                long action_id = mad.getActionId();
                String reply_name = mad.getUserName();
                re = workflowAnnexMgr.create(sql, new Object[]{id, flow_id, content, name, reply_name, new Date(), action_id, is_secret});
            } else {
                //普通流程
                sql = "insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(?,?,?,?,?,?,?,?,?,?)";
                re = workflowAnnexMgr.create(sql, new Object[]{id, flow_id, content, name, name, new Date(), 0, 0, -1, progress});

                if (re) {
                    WorkflowDb wf = new WorkflowDb(flow_id);

                    // 写入进度
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(wf.getTypeCode());
                    String formCode = lf.getFormCode();
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(formCode);
                    // 进度为0的时候不更新
                    if (fd.isProgress() && progress > 0) {
                        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                        fdao = fdao.getFormDAO(flow_id, fd);
                        fdao.setCwsProgress(progress);
                        fdao.save();
                    }
                }
            }
            if (re) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RES_SUCCESS);
                UserDb userDb = new UserDb(name);
                jResult.put("annexName", userDb.getRealName());
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            flag = false;
            log.error(StrUtil.trace(e));
        } catch (Exception e) {
            flag = false;
            log.error(StrUtil.trace(e));
        } finally {
            if (!flag) {
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }

        return jReturn.toString();
    }

    @RequestMapping("/getFile")
    public void getFile(HttpServletResponse response) throws IOException, ErrMsgException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());

            String op = ParamUtil.get(request, "op");
            //下载全路径 ，下载标题
            String url = "";
            String title = "";
            String skey = ParamUtil.get(request, "skey");
            com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
            String userName = pri.getUserName(skey);
            if ("".equals(userName)) {
                response.setContentType("text/html;charset=utf-8");
                bos.write("skey不存在".getBytes(StandardCharsets.UTF_8));
                return;
            }
            if (!"".equals(op)) {
                if ("stamp".equals(op)) {
                    String opinionName = ParamUtil.get(request, "opinionName");
                    if (opinionName != null && !"".equals(opinionName.trim())) {
                        StampPriv sp = new StampPriv();
                        StampDb sd = sp.getPersonalStamp(opinionName);
                        if (sd != null) {
                            title = sd.getImage();
                            url = cn.js.fan.web.Global.getRealPath() + "/upfile/stamp/" + title;

                        }
                    }
                }
            } else {
                int flowId = ParamUtil.getInt(request, "flowId");
                int attId = ParamUtil.getInt(request, "attachId");
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Document doc = new Document();
                doc = doc.getDocument(wf.getDocId());
                Attachment att = doc.getAttachment(1, attId);
                title = att.getName();
                url = cn.js.fan.web.Global.getRealPath() + "/" + att.getVisualPath() + "/" + att.getDiskName();

                // 判断是否超出下载次数限制
                AttachmentLogMgr alm = new AttachmentLogMgr();
                if (!alm.canDownload(userName, flowId, attId)) {
                    throw new ErrMsgException(alm.getErrMsg(request));
                }
                // 下载记录存至日志
                AttachmentLogMgr.log(userName, flowId, attId, AttachmentLogDb.TYPE_DOWNLOAD);
            }

            // 用下句会使IE在本窗口中打开文件
            // response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
            // 使客户端直接下载，上句会使IE在本窗口中打开文件，下句也一样
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(title));
            // 多番尝试，UTF8ToUnicode在mui.plus打开时也还是乱码
            // response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.Unicode2GB(title));
            bis = new BufferedInputStream(new FileInputStream(url));

            byte[] buff = new byte[2048];
            int bytesRead;

            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (final IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/flow/finishAction", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String finishAction(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            // 将场景置为流程
            threadContext.setSceneFlow();
            json = workflowService.finishActionByMobile(request, new com.redmoon.oa.pvg.Privilege());
        }
        catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            DebugUtil.i(getClass(), "finishAction", StrUtil.trace(e));
            // 置为异常状态
            threadContext.setAbnormal(true);
            json.put("res", "-1");
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
            json = workflowService.finishActionFreeByMobile(request, new com.redmoon.oa.pvg.Privilege());
        } catch (ErrMsgException | ClassCastException | NullPointerException | IllegalArgumentException e) {
            // 置为异常状态
            threadContext.setAbnormal(true);
            json.put("res", "-1");
            json.put("msg", e.getMessage());
            json.put("op", "");
            LogUtil.getLog(getClass()).error(e);
        } finally {
            // 清缓存
            threadContext.remove();
        }
        return json.toString();
    }
}
