package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.Paginator;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.*;
import com.cloudweb.oa.obs.ObsServiceFactory;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.qcloud.cos.model.COSObjectInputStream;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.util.TwoDimensionCode;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.AttachmentLogDb;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/public/android")
public class ModularController {
    public static final int RETURNCODE_SUCCESS = 0;       //登录成功
    public static final String RES = "res";
    public static final int RESULT_TIME_OUT = -2;//时间过期
    public static final String RETURNCODE = "returnCode";
    public static final int RESULT_FORMCODE_ERROR = -3;//未传formcode
    public static final int RESULT_MODULE_ERROR = -8;//未传moduleCode
    public static final int RESULT_NO_DATA = -1;//列表无数据 
    public static final int RESULT_SUCCESS = 0;//请求成功
    public static final String RESULT = "result";
    public static final String DATAS = "datas";

    @Autowired
    HttpServletRequest request;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    IModuleUtil moduleUtilService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    SysProperties sysProperties;

    @Autowired
    IObsServiceFactory obsServiceFactory;

    @ResponseBody
    @RequestMapping(value = "/module/list", produces = {"application/json;charset=UTF-8;"})
    public String list(@RequestParam(defaultValue = "", required = true) String skey,
                       String moduleCode,
                       String op,
                       String orderBy,
                       String sort,
                       Integer pageNum,
                       Integer pageSize) throws JSONException {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        if ("".equals(moduleCode)) {
            jReturn.put(RES, RESULT_MODULE_ERROR); //请求成功
            return jReturn.toString();
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);

        if (orderBy == null) {
            orderBy = ParamUtil.get(request, "orderBy");
        }
        if (sort == null) {
            sort = ParamUtil.get(request, "sort");
        }

        if ("".equals(orderBy)) {
            Privilege privilege = new Privilege();
            String userName = privilege.getUserName(skey);

            String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
            boolean isComb = filter.startsWith("<items>") || "".equals(filter);
            // 如果是组合条件，则赋予后台设置的排序字段
            if (isComb) {
                orderBy = StrUtil.getNullStr(msd.getString("orderby"));
                sort = StrUtil.getNullStr(msd.getString("sort"));
            }
            if ("".equals(orderBy)) {
                orderBy = "id";
            }
        }

        if ("".equals(sort)) {
            sort = "desc";
        }

        String formCode = msd.getString("form_code");
        try {
            UserMgr um = new UserMgr();
            Privilege privilege = new Privilege();

            boolean re = privilege.Auth(skey);
            String userName = privilege.getUserName(skey);

            jReturn.put(RES, RETURNCODE_SUCCESS); //请求成功
            if (re) {
                jResult.put(RETURNCODE, RESULT_TIME_OUT); //登录超时
            } else {
                privilege.doLogin(request, skey);

                if (formCode == null || "".equals(formCode.trim())) {
                    jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR); //表单为空
                } else {
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(formCode);
                    if (!fd.isLoaded()) { //表单不存在
                        jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR);//表单不存在
                        return jResult.toString();
                    } else {
                        MacroCtlUnit mu;
                        MacroCtlMgr mm = new MacroCtlMgr();

                        FormDAO fdao = new FormDAO();
                        if (msd == null) {
                            jResult.put(RETURNCODE, RESULT_MODULE_ERROR);//表单不存在
                            return jResult.toString();
                        }

                        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
                        String[] btnNames = StrUtil.split(btnName, ",");
                        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
                        String[] btnScripts = StrUtil.split(btnScript, "#");

                        // 取得条件
                        JSONArray conditions = new JSONArray();
                        int len = 0;
                        if (btnNames != null) {
                            len = btnNames.length;
                            for (int i = 0; i < len; i++) {
                                if (btnScripts[i].startsWith("{")) {
                                    JSONObject jsonBtn = new JSONObject(btnScripts[i]);
                                    if ("queryFields".equals(jsonBtn.getString("btnType"))) {
                                        String condFields = (String) jsonBtn.get("fields");
                                        String[] fieldAry = StrUtil.split(condFields, ",");
                                        for (int j = 0; j < fieldAry.length; j++) {
                                            String fieldName = fieldAry[j];
                                            FormField ff = fd.getFormField(fieldName);
                                            if (ff == null) {
                                                continue;
                                            }
                                            String condType = (String) jsonBtn.get(fieldName);
                                            String queryValue = ParamUtil.get(request, fieldName);

                                            queryValue = new String(queryValue.getBytes("ISO-8859-1"), "UTF-8");

                                            JSONObject jo = new JSONObject();

                                            jo.put("type", ff.getType());
                                            jo.put("fieldName", ff.getName());
                                            jo.put("fieldTitle", ff.getTitle());
                                            jo.put("fieldType", ff.getFieldType());
                                            jo.put("fieldCond", condType); // 点时间、时间段或模糊、准确查询
                                            jo.put("fieldOptions", "");

                                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                                mu = mm.getMacroCtlUnit(ff.getMacroType());
                                                if (mu != null) {
                                                    IFormMacroCtl ifm = mu.getIFormMacroCtl();
                                                    if (ifm.getControlType().equals(FormField.TYPE_SELECT)) {
                                                        jo.put("controlType", FormField.TYPE_SELECT);
                                                        jo.put("fieldOptions", ifm.getControlOptions(userName, ff));
                                                    }
                                                }
                                            }

                                            jo.put("fieldValue", queryValue);

                                            if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                                if ("0".equals(condType)) {
                                                    String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                                                    String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                                                    jo.put("fromDate", fDate);
                                                    jo.put("toDate", tDate);
                                                }
                                            }
                                            conditions.put(jo);
                                        }
                                    }
                                }
                            }
                        }
                        jResult.put("conditions", conditions);

                        String[] ary = null;
                        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
                        ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
                        String sql = ary[0];

                        // 增加权限控制
                        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
                        boolean canAdd = mpd.canUserAppend(userName);
                        boolean canEdit = mpd.canUserModify(userName);
                        boolean canDel = mpd.canUserDel(userName);
                        jResult.put("canAdd", canAdd);
                        jResult.put("canEdit", canEdit);
                        jResult.put("canDel", canDel);

                        // 列名
                        JSONArray cols = new JSONArray();
                        jResult.put("cols", cols);

                        int i = 0;
                        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
                        String[] formFieldArr = msd.getColAry(false, "list_field");

                        if (formFieldArr != null && formFieldArr.length > 0) {
                            ListResult lr = fdao.listResult(formCode, sql, pageNum, pageSize);
                            long total = lr.getTotal();
                            if (total == 0) {
                                jResult.put("total", total); //总页数
                                jResult.put(RETURNCODE, RESULT_NO_DATA); //没有数据
                            } else {
                                jResult.put("total", total); //总页数
                                jResult.put(RETURNCODE, RESULT_SUCCESS); //请求成功
                                Iterator ir = null;
                                Vector v = lr.getResult();
                                JSONArray dataArr = new JSONArray();
                                if (v != null) {
                                    ir = v.iterator();
                                }

                                // 将不显示的字段加入fieldHide
                                String fieldHide = "";
                                Iterator irField = fd.getFields().iterator();
                                while (irField.hasNext()) {
                                    FormField ff = (FormField) irField.next();
                                    if (ff.getHide() == FormField.HIDE_ALWAYS) {
                                        if ("".equals(fieldHide)) {
                                            fieldHide = ff.getName();
                                        } else {
                                            fieldHide += "," + ff.getName();
                                        }
                                    }
                                }
                                String[] fdsHide = StrUtil.split(fieldHide, ",");
                                List<String> listHide = null;
                                if (fdsHide != null) {
                                    listHide = Arrays.asList(ary);
                                }

                                WorkflowDb wf = new WorkflowDb();
                                HashMap<String, FormField> map = getFormFieldsByFromCode(formCode);
                                while (ir != null && ir.hasNext()) {
                                    fdao = (FormDAO) ir.next();

                                    RequestUtil.setFormDAO(request, fdao);

                                    long id = fdao.getId();
                                    JSONObject rowObj = new JSONObject();
                                    rowObj.put("id", id);
                                    rowObj.put("creator", um.getUserDb(fdao.getCreator()).getRealName());

                                    JSONArray fieldArr = new JSONArray();
                                    rowObj.put("fields", fieldArr);
                                    for (String fieldName : formFieldArr) {
                                        // 不需要显示操作列
                                        if ("colOperate".equals(fieldName)) {
                                            continue;
                                        }
                                        JSONObject formFieldObj = new JSONObject(); //Form表单对象
                                        String title = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = StrUtil.split(fieldName, ":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                title = subfd.getFieldTitle(subFields[2]);
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            String[] otherFields = StrUtil.split(fieldName, ":");
                                            if (otherFields.length == 5) {
                                                FormDb otherFormDb = new FormDb(otherFields[2]);
                                                title = otherFormDb.getFieldTitle(otherFields[4]);
                                            }
                                        } else if (fieldName.equals("cws_creator")) {
                                            title = "创建者";
                                        } else if (fieldName.equals("ID")) {
                                            title = "ID";
                                        } else if (fieldName.equals("cws_progress")) {
                                            title = "进度";
                                        } else if (fieldName.equals("flowId")) {
                                            title = "流程号";
                                        } else if (fieldName.equals("cws_status")) {
                                            title = "状态";
                                        } else if (fieldName.equals("cws_flag")) {
                                            title = "冲抵状态";
                                        } else if (fieldName.equals("cws_create_date")) {
                                            title = "创建时间";
                                        } else if (fieldName.equals("flow_begin_date")) {
                                            title = "流程开始时间";
                                        } else if (fieldName.equals("flow_end_date")) {
                                            title = "流程结束时间";
                                        } else {
                                            title = fd.getFieldTitle(fieldName);
                                        }

                                        formFieldObj.put("name", fieldName);
                                        formFieldObj.put("title", title);

                                        if (i == 0) {
                                            JSONObject jo = new JSONObject();
                                            jo.put("name", fieldName);
                                            jo.put("title", title);
                                            cols.put(jo);
                                        }

                                        if (map.containsKey(fieldName)) {
                                            FormField ff = map.get(fieldName);
                                            String value = fdao.getFieldValue(fieldName); //表单值
                                            formFieldObj.put("value", value);
                                            String type = ff.getType();// 类型描述
                                            formFieldObj.put("type", type);
                                        }

                                        String controlText = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = fieldName.split(":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                                                FormField subff = subfd.getFormField(subFields[2]);
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id='" + id + "' order by cws_order";
                                                JdbcTemplate jt = new JdbcTemplate();
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    ResultIterator ri = jt.executeQuery(subsql);
                                                    while (ri.hasNext()) {
                                                        ResultRecord rr = (ResultRecord) ri.next();
                                                        int subid = rr.getInt(1);
                                                        subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                                        String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                                        if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                            mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                            if (mu != null) {
                                                                subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                                            }
                                                        }
                                                        sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                                                    }
                                                } catch (Exception e) {
                                                    LogUtil.getLog(getClass()).error(e);
                                                }
                                                controlText = sb.toString();
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            controlText = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
                                        } else if ("ID".equals(fieldName)) {
                                            controlText = String.valueOf(fdao.getId());
                                        } else if ("cws_progress".equals(fieldName)) {
                                            controlText = String.valueOf(fdao.getCwsProgress());
                                        } else if ("cws_creator".equals(fieldName)) {
                                            String realName = "";
                                            if (fdao.getCreator() != null) {
                                                UserDb user = um.getUserDb(fdao.getCreator());
                                                if (user != null) {
                                                    realName = user.getRealName();
                                                }
                                            }
                                            controlText = realName;
                                        } else if ("flowId".equals(fieldName)) {
                                            controlText = String.valueOf(fdao.getFlowId());
                                        } else if ("cws_status".equals(fieldName)) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                                        } else if ("cws_flag".equals(fieldName)) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                                        } else if ("cws_create_date".equals(fieldName)) {
                                            controlText = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss");
                                        } else if ("flow_begin_date".equals(fieldName)) {
                                            int flowId = fdao.getFlowId();
                                            if (flowId != -1) {
                                                wf = wf.getWorkflowDb(flowId);
                                                controlText = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                                            }
                                        } else if ("flow_end_date".equals(fieldName)) {
                                            int flowId = fdao.getFlowId();
                                            if (flowId != -1) {
                                                wf = wf.getWorkflowDb(flowId);
                                                controlText = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                                            }
                                        } else {
                                            FormField ff = fdao.getFormField(fieldName);
                                            if (ff == null) {
                                                controlText = fieldName + " 已不存在！";
                                            } else {
                                                // 隐藏
                                                if (listHide != null && listHide.contains(ff.getName())) {
                                                    continue;
                                                }
                                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                                                    if (mu != null) {
                                                        String controlType = mu.getIFormMacroCtl().getControlType();
                                                        formFieldObj.put("controlType", controlType);
                                                        if ("img".equals(controlType)) {
                                                            controlText = mu.getIFormMacroCtl().getControlText(userName, ff);
                                                        }
                                                        else {
                                                            controlText = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                                        }
                                                    }
                                                } else {
                                                    controlText = FuncUtil.renderFieldValue(fdao, ff);
                                                }
                                            }
                                        }

                                        formFieldObj.put("text", controlText);
                                        fieldArr.put(formFieldObj);
                                    }
                                    dataArr.put(rowObj);//组装json数组

                                    i++;
                                }

                                jResult.put(DATAS, dataArr);
                            }
                        }
                    }
                }
            }
            jReturn.put(RESULT, jResult);
        } catch (JSONException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (UnsupportedEncodingException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return jReturn.toString();
    }

    /**
     * 获得所有字段信息
     *
     * @return
     */
    public HashMap<String, FormField> getFormFieldsByFromCode(String formCode) {
        HashMap<String, FormField> map = new HashMap<String, FormField>();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(fd);//获得所有表单元素
        Iterator fdaoIr = fdao.getFields().iterator();
        while (fdaoIr != null && fdaoIr.hasNext()) {
            FormField ff = (FormField) fdaoIr.next();
            map.put(ff.getName(), ff);
        }
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/module/add", produces = {"application/json;charset=UTF-8;"})
    public String add(@RequestParam(defaultValue = "", required = true) String skey, String moduleCode) {
        // 手机客户端 —— 新增 判断 需要显示的列
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        try {
            boolean re = privilege.Auth(skey);
            if (re) {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            }

            privilege.doLogin(request, skey);

            if (moduleCode != null && !"".equals(moduleCode.trim())) {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String formCode = msd.getString("form_code");
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);

                Vector<FormField> v = fd.getFields();
                Vector<FormField> vWritable = new Vector<>(); // 可写表单域（去除了隐藏字段）

                // 扫码传值
                boolean isByScan = false;
                String scanActionType = ParamUtil.get(request, "scanActionType");
                if (ConstUtil.SCAN_ACTION_TYPE_CREATE.equals(scanActionType)) {
                    String scanTargetField = ParamUtil.get(request, "scanTargetField");
                    if (!StrUtil.isEmpty(scanTargetField)) {
                        String scanId = ParamUtil.get(request, "scanId");
                        FormField ff = fd.getFormField(scanTargetField);
                        ff.setValue(String.valueOf(scanId));
                        isByScan = true;
                        DebugUtil.w(getClass(), "add", "scanId=" + scanId);
                    } else {
                        DebugUtil.w(getClass(), "add", "scanTargetField 不存在");
                    }
                }

                // 置可写表单域
                String userName = privilege.getUserName(skey);
                ModulePrivDb mpd = new ModulePrivDb(formCode);
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
                String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
                // 将不显示的字段加入fieldHide
                Iterator<FormField> ir = v.iterator();
                while (ir.hasNext()) {
                    FormField ff = ir.next();
                    if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
                        if ("".equals(fieldHide)) {
                            fieldHide = ff.getName();
                        } else {
                            fieldHide += "," + ff.getName();
                        }
                    }
                }
                String[] fdsHide = StrUtil.split(fieldHide, ",");

                if (!StringUtils.isEmpty(fieldWrite)) {
                    String[] fds = StrUtil.split(fieldWrite, ",");
                    if (fds != null) {
                        // 将不可写的域筛选出
                        ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = ir.next();

                            boolean finded = false;
                            for (String s : fds) {
                                if (ff.getName().equals(s)) {
                                    finded = true;
                                    break;
                                }
                            }

                            if (finded) {
                                vWritable.addElement(ff);
                                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                                ff.setEditable(false);
                            }
                        }
                    }

                    // 从可写字段中去掉隐藏字段
                    if (fdsHide != null) {
                        ir = vWritable.iterator();
                        while (ir.hasNext()) {
                            FormField ff = ir.next();
                            for (String s : fdsHide) {
                                if (ff.getName().equals(s)) {
                                    ir.remove();
                                }
                            }
                        }
                    }
                } else {
                    // 全部可写
                    vWritable = v;
                }

                // 为扫码传值的表单域选择宏控件自动映射
                MacroCtlMgr mm = new MacroCtlMgr();
                if (isByScan) {
                    ir = vWritable.iterator();
                    while (ir.hasNext()) {
                        FormField ff = ir.next();
                        if ("macro".equals(ff.getType())) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu == null) {
                                log.error("MactoCtl " + ff.getTitle() + "：" + ff.getMacroType() + " is not exist.");
                                continue;
                            }

                            String macroCode = mu.getCode();
                            if ("module_field_select".equals(macroCode)) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                                try {
                                    String value = StrUtil.getNullStr(ff.getValue());
                                    if ("".equals(value) || value.equals(ff.getDefaultValueRaw()) || value.equals(ff.getDescription())) {
                                        if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
                                            // 来自于指定的参数名称
                                            value = ParamUtil.get(request, json.getString("requestParam"));
                                        } else {
                                            // 默认以字段名作为参数从request中获取
                                            value = ParamUtil.get(request, ff.getName());
                                        }
                                    }
                                    // 如果value中存在值，则说明需自动映射
                                    if (!"".equals(value)) {
                                        moduleFieldSelectCtl.autoMapOnAdd(request, v, value, ff);
                                    }
                                } catch (JSONException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        }
                    }
                }

                JSONArray fields = new JSONArray();
                MacroCtlUnit mu;
                ir = vWritable.iterator();
                json.put("res", "0");
                // 遍历表单字段-------------------------------------------------
                while (ir.hasNext()) {
                    FormField ff = ir.next();

                    if (!ff.isMobileDisplay()) {
                        continue;
                    }

                    JSONObject field = new JSONObject();
                    String desc = StrUtil.getNullStr(ff.getDescription());
                    field.put("title", ff.getTitle());
                    field.put("code", ff.getName());
                    field.put("desc", desc);

                    // 如果是计算控件，则取出精度和四舍五入属性
                    if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                        FormParser fp = new FormParser();
                        String isroundto5 = fp.getFieldAttribute(fd, ff,
                                "isroundto5");
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
                            LogUtil.getLog(getClass()).error(
                                    "MactoCtl " + ff.getTitle() + "："
                                            + ff.getMacroType() + " is not exist.");
                            continue;
                        }

                        metaData = mu.getIFormMacroCtl().getMetaData(ff);

                        macroCode = mu.getCode();

                        macroType = mu.getIFormMacroCtl().getControlType();
                        controlText = mu.getIFormMacroCtl().getControlText(
                                privilege.getUserName(skey), ff);
                        controlValue = mu.getIFormMacroCtl().getControlValue(
                                privilege.getUserName(skey), ff);
                        options = mu.getIFormMacroCtl().getControlOptions(
                                privilege.getUserName(skey), ff);
                        // options = options.replaceAll("\\\"", "");
                        if (options != null && !"".equals(options)) {
                            // options = options.replaceAll("\\\"", "");
                            js = new JSONArray(options);
                        }
                    } else {
                        String type = ff.getType();
                        if (type != null && !"".equals(type)) {
                            if ("DATE".equals(type) || "DATE_TIME".equals(type)) {
                                controlValue = ff.getDefaultValueRaw();
                            } else {
                                // 有可能扫码传值时已被表单域选择宏控件映射
                                if (isByScan && !StrUtil.isEmpty(ff.getValue())) {
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
                        if ("macro_opinion".equals(macroCode) || "macro_opinionex".equals(macroCode)) {
                            if (controlText != null
                                    && !"".equals(controlText.trim())) {
                                opinionArr = new JSONArray(controlText);
                            }
                            if (controlValue != null
                                    && !"".equals(controlValue.trim())) {
                                opinionVal = new JSONObject(controlValue);
                            }
                        }

                        if ("nest_sheet".equals(macroCode) || "nest_table".equals(macroCode) || "macro_detaillist_ctl".equals(macroCode)) {
                            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                            INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                            JSONObject jsonObj = nestSheetCtl.getCtlDescription(ff);
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
                        // options = fp.getOptionsOfSelect(fd, ff);
                        String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                        for (String[] optionsItem : optionsArray) {
                            JSONObject option = new JSONObject();
                            try {
                                option.put("value", optionsItem[1]);
                                option.put("name", optionsItem[0]);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                            js.put(option);
                        }
                    } else if ("radio".equals(ff.getType())) {
                        // options = fp.getOptionsOfSelect(fd, ff);
                        String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
                        for (String s : optionsArray) {
                            JSONObject option = new JSONObject();
                            option.put("value", s);
                            option.put("name", s);
                            js.put(option);
                        }
                    }
                    field.put("options", js);
                    field.put("text", controlText);
                    String level = "";
                    if ("checkbox".equals(ff.getType())) {
                        // level = "个人兴趣";
                        level = ff.getTitle();
                    }
                    field.put("level", level);
                    field.put("macroType", macroType);
                    field.put("editable", String.valueOf(ff.isEditable()));
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

                String viewJs = moduleUtilService.doGetViewJSMobile(request, fd, null, authUtil.getUserName(), false);
                json.put("viewJs", viewJs);

                json.put("fields", fields);
                // 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());

                json.put("formCode", fd.getCode());
                // 算式相关的字段
                json.put("funcRelatedOnChangeFields", FuncUtil.doGetFieldsRelatedOnChangeMobile(fd));

                return json.toString();
            } else {
                json.put("res", "-1");
                json.put("msg", "表单编码为空！");
                return json.toString();
            }

        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/del", produces = {"application/json;charset=UTF-8;"})
    public String del(@RequestParam(defaultValue = "", required = true) String skey,
                      @RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
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

        try {
            FormDb fd = new FormDb();
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String formCode = msd.getString("form_code");
            fd = fd.getFormDb(formCode);
            FormDAO fdao = new FormDAO(fd);
            fdao = fdao.getFormDAOByCache(id, fd);
            re = fdao.del();
            if (re) {
                json.put("res", "0");
                json.put("msg", "操作成功");
            } else {
                json.put("res", "-1");
                json.put("msg", "删除失败");
            }
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/edit", produces = {"application/json;charset=UTF-8;"})
    public String edit(@RequestParam(defaultValue = "", required = true) String skey,
                       @RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
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

        if (moduleCode != null && !moduleCode.trim().equals("")) {
            JSONArray fields = new JSONArray();
            MacroCtlUnit mu;
            MacroCtlMgr mm = new MacroCtlMgr();
            try {
                json.put("res", "0");

                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String formCode = msd.getString("form_code");
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                Vector v = fd.getFields();

                // 置可写表单域
                String userName = privilege.getUserName(skey);
                ModulePrivDb mpd = new ModulePrivDb(formCode);
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
                if (!"".equals(fieldWrite)) {
                    String[] fds = StrUtil.split(fieldWrite, ",");
                    if (fds != null) {
                        int len = fds.length;

                        // 将不可写的域筛选出
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();

                            boolean finded = false;
                            for (int i = 0; i < len; i++) {
                                if (ff.getName().equals(fds[i])) {
                                    finded = true;
                                    break;
                                }
                            }

                            if (finded) {
                                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                                ff.setEditable(false);
                            }
                        }
                    }

                    // 去掉隐藏字段
                    String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
                    // 将不显示的字段加入fieldHide
                    Iterator ir = v.iterator();
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
                    String[] fdsHide = StrUtil.split(fieldHide, ",");
                    if (fdsHide != null) {
                        ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();
                            for (int k = 0; k < fdsHide.length; k++) {
                                if (ff.getName().equals(fdsHide[k])) {
                                    ir.remove();
                                }
                            }
                        }
                    }
                }

                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAOByCache(id, fd);
                Iterator ir = fdao.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();

                    if (!ff.isMobileDisplay()) {
                        continue;
                    }

                    String val = fdao.getFieldValue(ff.getName());

                    JSONObject field = new JSONObject();
                    field.put("title", ff.getTitle());
                    field.put("code", ff.getName());
                    field.put("desc", StrUtil.getNullStr(ff.getDescription()));

                    // 如果是计算控件，则取出精度和四舍五入属性
                    if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                        FormParser fp = new FormParser();
                        String isroundto5 = fp.getFieldAttribute(fd, ff,
                                "isroundto5");
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
                    if (ff.getType().equals("macro")) {
                        mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu == null) {
                            LogUtil.getLog(getClass()).error(
                                    "MactoCtl " + ff.getTitle() + "："
                                            + ff.getMacroType() + " is not exist.");
                            continue;
                        }

                        IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                        ifmc.setIFormDAO(fdao);

                        macroType = mu.getIFormMacroCtl().getControlType();
                        metaData = mu.getIFormMacroCtl().getMetaData(ff);
                        macroCode = mu.getCode();

                        // 如果值为null，则在json中put的时候，是无效的，不会被记录至json中
                        controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
                                .getControlText(privilege.getUserName(skey),
                                        ff));
                        val = StrUtil.getNullStr(mu.getIFormMacroCtl()
                                .getControlValue(privilege.getUserName(skey),
                                        ff));
                        options = mu.getIFormMacroCtl().getControlOptions(
                                privilege.getUserName(skey), ff);
                        if (options != null && !"".equals(options)) {
                            // options = options.replaceAll("\\\"", "");
                            js = new JSONArray(options);
                        }
                    }
                    // 判断是否为意见输入框
                    if (macroCode != null && !"".equals(macroCode)) {
                        if ("macro_opinion".equals(macroCode) || "macro_opinionex".equals(macroCode)) {
                            if (controlText != null
                                    && !controlText.trim().equals("")) {
                                opinionArr = new JSONArray(controlText);
                            }
                            if (val != null && !val.trim().equals("")) {
                                opinionVal = new JSONObject(val);
                            }
                        }

                        if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
                            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                            INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                            JSONObject jsonObj = nestSheetCtl.getCtlDescription(ff);
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
                    if ("select".equals(ff.getType())) {
                        String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                        for (int i = 0; i < optionsArray.length; i++) {
                            String[] optionsItem = optionsArray[i];
                            if (optionsItem != null && optionsItem.length == 2) {
                                JSONObject option = new JSONObject();
                                option.put("value", optionsItem[1]);
                                option.put("name", optionsItem[0]);
                                js.put(option);
                            }
                        }
                    } else if (ff.getType().equals("radio")) {
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
                    if (ff.getType().equals("checkbox")) {
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
                    // LogUtil.getLog(getClass()).info(ff.getTitle() +
                    // " controlText=" + controlText);
                    field.put("level", level);
                    field.put("macroType", macroType);
                    field.put("editable", String.valueOf(ff.isEditable()));
                    // field.put("isHidden", String.valueOf(ff.isHidden())); // 之前已被去除
                    field.put("isNull", String.valueOf(ff.isCanNull()));
                    field.put("fieldType", ff.getFieldTypeDesc());

                    field.put("macroCode", macroCode);

                    // 传SQL控件条件中的字段
                    field.put("metaData", metaData);
                    field.put("isReadonly", ff.isReadonly());

                    fields.put(field);
                }

                json.put("fields", fields);

                Iterator itFiles = fdao.getAttachments().iterator();
                JSONArray filesArr = new JSONArray();
                while (itFiles.hasNext()) {
                    com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
                            .next();
                    JSONObject fileObj = new JSONObject();
                    String name = am.getName();
                    fileObj.put("name", name);
                    String url = "/public/android/download.do?attachId=" + am.getId() + "&visitKey=" + SecurityUtil.makeVisitKey(am.getId());
                    fileObj.put("url", url);
                    fileObj.put("id", am.getId());
                    fileObj.put("size", String.valueOf(am.getFileSize()));
                    filesArr.put(fileObj);
                }
                json.put("files", filesArr);

                String viewJs = moduleUtilService.doGetViewJSMobile(request, fd, fdao, authUtil.getUserName(), false);
                json.put("viewJs", viewJs);

                // 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());

                json.put("formCode", fd.getCode());
                // 算式相关的字段
                json.put("funcRelatedOnChangeFields", FuncUtil.doGetFieldsRelatedOnChangeMobile(fd));

            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/show", produces = {"application/json;charset=UTF-8;"})
    public String show(@RequestParam(defaultValue = "", required = true) String skey,
                       @RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
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

        String userName = privilege.getUserName(skey);
        if (userName == null || "".equals(userName)) {
            com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
            userName = pvg.getUser(request);
        }

        MacroCtlUnit mu;
        MacroCtlMgr mm = new MacroCtlMgr();
        try {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String formCode = msd.getString("form_code");
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAOByCache(id, fd);
            Vector<FormField> v = fdao.getFields();

            int viewShow = msd.getInt("view_show");
            // 如果采用了视图
            if (!(viewShow==ModuleSetupDb.VIEW_DEFAULT || viewShow==ModuleSetupDb.VIEW_SHOW_CUSTOM)) {
                FormViewDb fvd = new FormViewDb();
                fvd = fvd.getFormViewDb(viewShow);
                if (fvd == null) {
                    LogUtil.getLog(getClass()).error("视图ID=" + viewShow + "不存在");
                } else {
                    String ieVersion = fvd.getString("ie_version");
                    FormParser fp = new FormParser();
                    Vector<FormField> fields = fp.parseCtlFromView(fvd.getString("content"), ieVersion, fd);
                    // 将fields改为已fdao中已取得值的FormField
                    Vector<FormField> vt = new Vector<>();
                    for (FormField ff : fields) {
                        vt.addElement(fdao.getFormField(ff.getName()));
                    }
                    v = vt;
                }
            }

            json.put("res", "0");
            json.put("id", id);

            JSONArray fields = new JSONArray();

            ModulePrivDb mpd = new ModulePrivDb(formCode);
            boolean isHideField = true;
            String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
            // 将不显示的字段加入fieldHide
            Iterator<FormField> ir = fd.getFields().iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();
                if (ff.getHide() == FormField.HIDE_ALWAYS) {
                    if ("".equals(fieldHide)) {
                        fieldHide = ff.getName();
                    } else {
                        fieldHide += "," + ff.getName();
                    }
                }
            }
            String[] fdsHide = StrUtil.split(fieldHide, ",");

            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();

                // 跳过隐藏域
                if (isHideField) {
                    boolean isShow = true;
                    if (fdsHide != null) {
                        for (String s : fdsHide) {
                            if (!s.startsWith("nest.")) {
                                if (s.equals(ff.getName())) {
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

                // String val = StrUtil.getNullStr(ff.getValue());
                String val = ff.convertToHtml();

                JSONObject field = new JSONObject();
                String macroCode = "";
                JSONArray jsonArr = null;
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        IFormMacroCtl iFormMacroCtl = mu.getIFormMacroCtl();
                        iFormMacroCtl.setIFormDAO(fdao);
                        val = iFormMacroCtl.getControlText(privilege.getUserName(skey), ff);
                        macroCode = mu.getCode();
                        if (macroCode != null && !"".equals(macroCode)) {
                            if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
                                if (!val.equals("")) {
                                    jsonArr = new JSONArray(val);
                                } else {
                                    jsonArr = new JSONArray();
                                }
                            }

                            if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                                JSONObject jsonObj = nestSheetCtl.getCtlDescription(ff);
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
                            } else if ("macro_barcode".equals(macroCode)) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                IBarcodeCtl barcodeCtl = macroCtlService.getBarcodeCtl();
                                field.put("image", barcodeCtl.getBracodeSteamBase64(barcodeCtl.getBarcodeStr(StrUtil.toInt(ff.getValue(), 0))));
                            } else if ("macro_qrcode".equals(macroCode)) {
                                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                                IQrcodeCtl qrcodeCtl = macroCtlService.getQrcodeCtl();
                                field.put("image", qrcodeCtl.getQrcodeSteamBase64(qrcodeCtl.getQrcodeStr(ff), 150, 150));
                            }
                        }
                    }
                }
                field.put("title", ff.getTitle());
                field.put("code", ff.getName());
                if (jsonArr != null) {
                    field.put("value", jsonArr);
                } else {
                    field.put("value", val);
                }
                field.put("type", ff.getType());
                String level = "";
                if ("checkbox".equals(ff.getType())) {
                    level = ff.getTitle();
                }
                field.put("level", level);
                field.put("macroCode", macroCode);
                fields.put(field);
            }
            json.put("fields", fields);

            Iterator itFiles = fdao.getAttachments().iterator();
            JSONArray filesArr = new JSONArray();
            while (itFiles.hasNext()) {
                com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
                        .next();
                JSONObject fileObj = new JSONObject();
                String name = am.getName();
                fileObj.put("name", name);
                String url = "/public/android/download.do?attachId=" + am.getId() + "&visitKey=" + SecurityUtil.makeVisitKey(am.getId());
                fileObj.put("url", url);
                fileObj.put("id", am.getId());
                fileObj.put("size", String.valueOf(am.getFileSize()));
                filesArr.put(fileObj);
            }
            json.put("files", filesArr);

            boolean isTab = ParamUtil.getBoolean(request, "isTab", true);
            JSONArray arrRelated = new JSONArray();

            // 关联模块
            if (isTab) {
                FormDb fdRelated = new FormDb();
                ModuleRelateDb mrdTop = new ModuleRelateDb();
                java.util.Iterator irTop = mrdTop.getModulesRelated(formCode).iterator();
                while (irTop.hasNext()) {
                    mrdTop = (ModuleRelateDb) irTop.next();
                    // 有查看权限才能看到从模块选项卡
                    ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
                    if (mpdTop.canUserSee(userName)) {
                        String name = fdRelated.getFormDb(mrdTop.getString("relate_code")).getName();
                        JSONObject jsonRelated = new JSONObject();
                        jsonRelated.put("name", name);
                        jsonRelated.put("formCodeRelated", mrdTop.getString("relate_code"));
                        arrRelated.put(jsonRelated);
                    }
                }
            }
            json.put("formRelated", arrRelated);

            JSONArray subArr = new JSONArray();
            if (isTab) {
                // 其它标签
                String[] subTags = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
                String[] subTagUrls = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
                int subLen = 0;
                if (subTags != null) {
                    subLen = subTags.length;
                }
                for (int i = 0; i < subLen; i++) {
                    // String uri = ModuleUtil.filterViewEditTagUrl(request, codeTop, subTagsTop[i]);
                    String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, subTags[i]);
                    if (tagUrl.startsWith("{")) {
                        JSONObject jsonTag = new JSONObject(tagUrl);
                        if (!jsonTag.isNull("fieldSource")) {
                            JSONObject jsonRelated = new JSONObject();
                            jsonRelated.put("tagName", subTags[i]);
                            jsonRelated.put("subTagIndex", i); // 因为传tagName用于得到配置信息时，从RelateListAction取出来时乱码（因为是中文），所以用该键值来传递信息
                            subArr.put(jsonRelated);
                            continue;
                        }
                    }
                }
            }
            json.put("subTags", subArr);

            String viewJs = moduleUtilService.doGetViewJSMobile(request, fd, fdao, authUtil.getUserName(), false);
            json.put("viewJs", viewJs);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/attDel", produces = {"application/json;charset=UTF-8;"})
    public String attDel(@RequestParam(defaultValue = "", required = true) String skey,
                         @RequestParam(required = true) boolean isFlow, @RequestParam(required = true) int id) {
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

        try {
            if (isFlow) {
                com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment(id);
                re = att.del();
            } else {
                com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(id);
                re = att.del();
            }
            if (re) {
                json.put("res", "0");
                json.put("msg", "操作成功");
            } else {
                json.put("res", "-1");
                json.put("msg", "删除失败");
            }
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/listRelate", produces = {"application/json;charset=UTF-8;"})
    public String listRelate(@RequestParam(defaultValue = "", required = true) String skey,
                             @RequestParam(required = true) String moduleCode,
                             Long parentId,
                             String formCodeRelated,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "15") Integer pageSize,
                             @RequestParam(defaultValue = "") String op,
                             @RequestParam(defaultValue = "id") String orderBy,
                             @RequestParam(defaultValue = "desc") String sort) throws JSONException {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        if ("".equals(moduleCode)) {
            jResult.put(RETURNCODE, RESULT_MODULE_ERROR);
            return jResult.toString();
        }

        try {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            if (msd == null) {
                jResult.put(RETURNCODE, RESULT_MODULE_ERROR);
                return jResult.toString();
            }

            String formCode = msd.getString("form_code");

            UserMgr um = new UserMgr();
            Privilege privilege = new Privilege();
            boolean re = privilege.Auth(skey);
            jReturn.put(RES, RETURNCODE_SUCCESS); //请求成功
            if (re) {
                jResult.put(RETURNCODE, RESULT_TIME_OUT); //登录超时
            } else {
                String userName = privilege.getUserName(skey);
                if (userName == null || "".equals(userName)) {
                    com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                    userName = pvg.getUser(request);
                }

                if (formCode == null || formCode.trim().equals("")) {
                    jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR); //表单为空
                } else {
                    FormDb fdMain = new FormDb();
                    fdMain = fdMain.getFormDb(formCode);
                    if (!fdMain.isLoaded()) { //表单不存在
                        jResult.put(RETURNCODE, RESULT_FORMCODE_ERROR);//表单不存在
                        return jResult.toString();
                    } else {
                        FormDb fdRelated = new FormDb();

                        try {
                            request.setCharacterEncoding("utf-8");
                        } catch (UnsupportedEncodingException e1) {
                            e1.printStackTrace();
                        }

                        // 按选项卡设置中的顺序排列，从0开始
                        int subTagIndex = ParamUtil.getInt(request, "subTagIndex", -1);

                        if (subTagIndex != -1) {
                            String[] subTags = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
                            int subLen = 0;
                            if (subTags != null) {
                                subLen = subTags.length;
                            }
                            String tagName = "";
                            for (int i = 0; i < subLen; i++) {
                                if (i == subTagIndex) {
                                    tagName = subTags[i];
                                    break;
                                }
                            }

                            String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
                            if (tagUrl.equals("")) {
                                com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(tagName + " 不存在！");
                            } else {
                                if (tagUrl.startsWith("{")) {
                                    JSONObject json = new JSONObject(tagUrl);
                                    if (!json.isNull("formRelated")) {
                                        // formCodeRelated = json.getString("formRelated");
                                        msd = msd.getModuleSetupDb(json.getString("formRelated"));
                                        // 用于SQLBuilder中调用ModuleUtil.parseFilter时
                                        request.setAttribute("MODULE_SETUP", msd);
                                        formCodeRelated = msd.getString("form_code");
                                    }
                                }
                            }
                        }

                        fdRelated = fdRelated.getFormDb(formCodeRelated);

                        MacroCtlUnit mu;
                        MacroCtlMgr mm = new MacroCtlMgr();

                        FormDAO fdao = new FormDAO();

                        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
                        String relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
                        if (relateFieldValue == null) {
                            relateFieldValue = SQLBuilder.IS_NOT_RELATED;

/*							jReturn.put(RES,-3);
							jReturn.put("msg", "请检查模块是否相关联！");
							setResult(jReturn.toString());
							return;			*/
                        }

                        jReturn.put(RES, 0);
                        jReturn.put("parentId", parentId);
                        jReturn.put("moduleCode", moduleCode);
                        jReturn.put("formCodeRelated", formCodeRelated);

                        // 关联模块
                        JSONArray arrRelated = new JSONArray();
                        ModuleRelateDb mrdTop = new ModuleRelateDb();
                        java.util.Iterator irTop = mrdTop.getModulesRelated(formCode).iterator();
                        while (irTop.hasNext()) {
                            mrdTop = (ModuleRelateDb) irTop.next();
                            // 有查看权限才能看到从模块选项卡
                            ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
                            if (mpdTop.canUserSee(userName)) {
                                String name = fdRelated.getFormDb(mrdTop.getString("relate_code")).getName();
                                JSONObject jsonRelated = new JSONObject();
                                jsonRelated.put("name", name);
                                jsonRelated.put("formCodeRelated", mrdTop.getString("relate_code"));
                                arrRelated.put(jsonRelated);
                            }
                        }
                        jReturn.put("formRelated", arrRelated);

                        ModuleSetupDb msdRelated = new ModuleSetupDb();
                        msdRelated = msdRelated.getModuleSetupDb(formCodeRelated);
                        String btnName = StrUtil.getNullStr(msdRelated.getString("btn_name"));
                        String[] btnNames = StrUtil.split(btnName, ",");
                        String btnScript = StrUtil.getNullStr(msdRelated.getString("btn_script"));
                        String[] btnScripts = StrUtil.split(btnScript, "#");

                        // 取得条件
                        JSONArray conditions = new JSONArray();
                        int len = 0;
                        if (btnNames != null) {
                            len = btnNames.length;
                            for (int i = 0; i < len; i++) {
                                if (btnScripts[i].startsWith("{")) {
                                    JSONObject jsonBtn = new JSONObject(btnScripts[i]);
                                    if (((String) jsonBtn.get("btnType")).equals("queryFields")) {
                                        String condFields = (String) jsonBtn.get("fields");
                                        String[] fieldAry = StrUtil.split(condFields, ",");
                                        for (int j = 0; j < fieldAry.length; j++) {
                                            String fieldName = fieldAry[j];
                                            FormField ff = fdRelated.getFormField(fieldName);
                                            if (ff == null) {
                                                continue;
                                            }
                                            String condType = (String) jsonBtn.get(fieldName);
                                            String queryValue = ParamUtil.get(request, fieldName);

                                            JSONObject jo = new JSONObject();

                                            jo.put("fieldName", ff.getName());
                                            jo.put("fieldTitle", ff.getTitle());
                                            jo.put("fieldType", ff.getFieldType());
                                            jo.put("fieldCond", condType); // 点时间、时间段或模糊、准确查询
                                            jo.put("fieldOptions", "");
                                            jo.put("fieldValue", queryValue);

                                            if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                                if (condType.equals("0")) {
                                                    String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                                                    String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                                                    jo.put("fromDate", fDate);
                                                    jo.put("toDate", tDate);
                                                }
                                            }
                                            conditions.put(jo);
                                        }
                                    }
                                }
                            }
                        }
                        jResult.put("conditions", conditions);

                        request.setAttribute("cwsId", String.valueOf(parentId));
                        // String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
                        String[] ary = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fdRelated, op, orderBy, sort, relateFieldValue);
                        String sql = ary[0];

                        String listField = StrUtil.getNullStr(msdRelated.getString("list_field"));
                        String[] formFieldArr = StrUtil.split(listField, ",");
                        if (formFieldArr != null && formFieldArr.length > 0) {
                            ListResult lr = fdao.listResult(formCodeRelated, sql, pageNum, pageSize);
                            long total = lr.getTotal();
                            if (total == 0) {
                                jResult.put(RETURNCODE, RESULT_NO_DATA); //没有数据
                            } else {
                                jResult.put("total", total); //总页数
                                jResult.put(RETURNCODE, RESULT_SUCCESS); //请求成功
                                Iterator ir = null;
                                Vector v = lr.getResult();
                                JSONArray dataArr = new JSONArray();
                                if (v != null) {
                                    ir = v.iterator();
                                }

                                // 增加权限控制
                                ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
                                boolean canAdd = mpd.canUserAppend(userName);
                                boolean canEdit = mpd.canUserModify(userName);
                                boolean canDel = mpd.canUserDel(userName);
                                jResult.put("canAdd", canAdd);
                                jResult.put("canEdit", canEdit);
                                jResult.put("canDel", canDel);

                                // 列名
                                JSONArray cols = new JSONArray();
                                jResult.put("cols", cols);

                                int i = 0;
                                HashMap<String, FormField> map = getFormFieldsByFromCode(formCode);
                                while (ir != null && ir.hasNext()) {
                                    fdao = (FormDAO) ir.next();
                                    long id = fdao.getId();
                                    JSONObject rowObj = new JSONObject();
                                    rowObj.put("id", id);
                                    rowObj.put("creator", um.getUserDb(fdao.getCreator()).getRealName());

                                    JSONArray fieldArr = new JSONArray();
                                    rowObj.put("fields", fieldArr);
                                    for (String fieldName : formFieldArr) {
                                        JSONObject formFieldObj = new JSONObject(); //Form表单对象
                                        String title = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = StrUtil.split(fieldName, ":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                title = subfd.getFieldTitle(subFields[2]);
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            String[] otherFields = StrUtil.split(fieldName, ":");
                                            if (otherFields.length == 5) {
                                                FormDb otherFormDb = new FormDb(otherFields[2]);
                                                title = otherFormDb.getFieldTitle(otherFields[4]);
                                            }
                                        } else if (fieldName.equals("cws_creator")) {
                                            title = "创建者";
                                        } else if (fieldName.equals("ID")) {
                                            title = "ID";
                                        } else if (fieldName.equals("cws_progress")) {
                                            title = "进度";
                                        } else {
                                            title = fdRelated.getFieldTitle(fieldName);
                                        }

                                        formFieldObj.put("name", fieldName);
                                        formFieldObj.put("title", title);

                                        if (i == 0) {
                                            JSONObject jo = new JSONObject();
                                            jo.put("name", fieldName);
                                            jo.put("title", title);
                                            cols.put(jo);
                                        }

                                        if (map.containsKey(fieldName)) {
                                            FormField ff = map.get(fieldName);
                                            String value = fdao.getFieldValue(fieldName); //表单值
                                            formFieldObj.put("value", value);
                                            String type = ff.getType();// 类型描述
                                            formFieldObj.put("type", type);
                                        }

                                        String controlText = "";
                                        if (fieldName.startsWith("main:")) {
                                            String[] subFields = fieldName.split(":");
                                            if (subFields.length == 3) {
                                                FormDb subfd = new FormDb(subFields[1]);
                                                com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                                                FormField subff = subfd.getFormField(subFields[2]);
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id='" + id + "' order by cws_order";
                                                JdbcTemplate jt = new JdbcTemplate();
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    ResultIterator ri = jt.executeQuery(subsql);
                                                    while (ri.hasNext()) {
                                                        ResultRecord rr = (ResultRecord) ri.next();
                                                        int subid = rr.getInt(1);
                                                        subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                                        String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                                        if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                            mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                            if (mu != null) {
                                                                subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                                            }
                                                        }
                                                        sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                                                    }
                                                } catch (Exception e) {
                                                    LogUtil.getLog(getClass()).error(e);
                                                }
                                                controlText = sb.toString();
                                            }
                                        } else if (fieldName.startsWith("other:")) {
                                            controlText = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
                                        } else if (fieldName.equals("ID")) {
                                            controlText = String.valueOf(fdao.getId());
                                        } else if (fieldName.equals("cws_progress")) {
                                            controlText = String.valueOf(fdao.getCwsProgress());
                                        } else if (fieldName.equals("cws_creator")) {
                                            String realName = "";
                                            if (fdao.getCreator() != null) {
                                                UserDb user = um.getUserDb(fdao.getCreator());
                                                if (user != null) {
                                                    realName = user.getRealName();
                                                }
                                            }
                                            controlText = realName;
                                        } else {
                                            FormField ff = fdao.getFormField(fieldName);
                                            if (ff == null) {
                                                controlText = fieldName + " 已不存在！";
                                            } else {
                                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                                                    if (mu != null) {
                                                        controlText = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                                    }
                                                } else {
                                                    controlText = FuncUtil.renderFieldValue(fdao, ff);
                                                }
                                            }
                                        }

                                        formFieldObj.put("text", controlText);

                                        fieldArr.put(formFieldObj);
                                    }
                                    dataArr.put(rowObj);//组装json数组

                                    i++;
                                }

                                jResult.put(DATAS, dataArr);
                            }
                        }
                    }
                }
            }
            jReturn.put(RESULT, jResult);
        } catch (JSONException e) {
            log.error(e.getMessage());
        } catch (ErrMsgException e) {
            log.error(e.getMessage());
        }
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/addRelate", produces = {"application/json;charset=UTF-8;"})
    public String addRelate(@RequestParam(defaultValue = "", required = true) String skey,
                            @RequestParam(required = true) String moduleCode, String formCodeRelated, long parentId) {
        // 手机客户端 —— 新增 判断 需要显示的列
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        try {
            boolean re = privilege.Auth(skey);
            if (re) {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            }

            privilege.doLogin(request, skey);

            if (moduleCode != null && !moduleCode.trim().equals("")) {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String formCode = msd.getString("form_code");
                FormDb fdMain = new FormDb();
                fdMain = fdMain.getFormDb(formCode);

                com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(formCode);
                String relateFieldValue = fdmMain.getRelateFieldValue(parentId, formCodeRelated);
                if (relateFieldValue == null) {
                    json.put("res", "-3");
                    json.put("msg", "请检查模块是否相关联！");
                    return json.toString();
                }

                json.put("res", "0");
                json.put("cws_id", relateFieldValue); // 关联值
                json.put("formCode", formCode); // 主表单编码

                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCodeRelated);
                Vector v = fd.getFields();
                Vector vWritable = new Vector(); // 可写表单域（去除了隐藏字段）

                // 置可写表单域
                String userName = privilege.getUserName(skey);
                ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
                String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
                String[] fdsHide = StrUtil.split(fieldHide, ",");

                if (!"".equals(fieldWrite)) {
                    String[] fds = StrUtil.split(fieldWrite, ",");
                    if (fds != null) {
                        int len = fds.length;

                        // 将不可写的域筛选出
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();

                            boolean finded = false;
                            for (int i = 0; i < len; i++) {
                                if (ff.getName().equals(fds[i])) {
                                    finded = true;
                                    break;
                                }
                            }

                            if (finded) {
                                vWritable.addElement(ff);
                                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                                ff.setEditable(false);
                            }
                        }
                    }

                    // 从可写字段中去掉隐藏字段
                    if (fdsHide != null) {
                        Iterator ir = vWritable.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();
                            for (int k = 0; k < fdsHide.length; k++) {
                                if (ff.getName().equals(fdsHide[k])) {
                                    ir.remove();
                                }
                            }
                        }
                    }
                } else {
                    // 全部可写
                    vWritable = v;
                }

                JSONArray fields = new JSONArray();
                MacroCtlUnit mu;
                MacroCtlMgr mm = new MacroCtlMgr();
                Iterator ir = vWritable.iterator();
                json.put("res", "0");
                // 遍历表单字段-------------------------------------------------
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();

                    if (!ff.isMobileDisplay()) {
                        continue;
                    }

                    JSONObject field = new JSONObject();
                    String desc = StrUtil.getNullStr(ff.getDescription());
                    field.put("title", ff.getTitle());
                    field.put("code", ff.getName());
                    field.put("desc", desc);

                    // 如果是计算控件，则取出精度和四舍五入属性
                    if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                        FormParser fp = new FormParser();
                        String isroundto5 = fp.getFieldAttribute(fd, ff,
                                "isroundto5");
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
                    if (ff.getType().equals("macro")) {
                        mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu == null) {
                            LogUtil.getLog(getClass()).error(
                                    "MactoCtl " + ff.getTitle() + "："
                                            + ff.getMacroType() + " is not exist.");
                            continue;
                        }

                        metaData = mu.getIFormMacroCtl().getMetaData(ff);

                        macroCode = mu.getCode();

                        macroType = mu.getIFormMacroCtl().getControlType();
                        controlText = mu.getIFormMacroCtl().getControlText(
                                privilege.getUserName(skey), ff);
                        controlValue = mu.getIFormMacroCtl().getControlValue(
                                privilege.getUserName(skey), ff);
                        options = mu.getIFormMacroCtl().getControlOptions(
                                privilege.getUserName(skey), ff);
                        // options = options.replaceAll("\\\"", "");
                        if (options != null && !options.equals("")) {
                            // options = options.replaceAll("\\\"", "");
                            js = new JSONArray(options);
                        }
                    } else {
                        String type = ff.getType();
                        if (type != null && !type.equals("")) {
                            if (type.equals("DATE") || type.equals("DATE_TIME")) {
                                controlValue = ff.getDefaultValueRaw();
                            } else {
                                controlValue = ff.getDefaultValue();
                            }
                        } else {
                            controlValue = ff.getDefaultValue();
                        }
                    }
                    // 判断是否为意见输入框
                    if (macroCode != null && !macroCode.equals("")) {
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
                            INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                            JSONObject jsonObj = nestSheetCtl.getCtlDescription(ff);
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
                        String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                        for (int i = 0; i < optionsArray.length; i++) {
                            String[] optionsItem = optionsArray[i];
                            JSONObject option = new JSONObject();
                            try {
                                option.put("value", optionsItem[1]);
                                option.put("name", optionsItem[0]);
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
                    field.put("macroType", macroType);
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

                    fields.put(field);
                }

                json.put("fields", fields);
                // 判断 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());

                json.put("formCode", fd.getCode());
                // 算式相关的字段
                json.put("funcRelatedOnChangeFields", FuncUtil.doGetFieldsRelatedOnChangeMobile(fd));

                return json.toString();
            } else {
                json.put("res", "-1");
                json.put("msg", "表单编码为空！");
                return json.toString();
            }
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/editRelate", produces = {"application/json;charset=UTF-8;"})
    public String editRelate(@RequestParam(defaultValue = "", required = true) String skey,
                             @RequestParam(required = true) String moduleCode, String formCodeRelated, Long parentId, Long id) {
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

        String userName = privilege.getUserName(skey);
        if (userName == null || "".equals(userName)) {
            com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
            userName = pvg.getUser(request);
        } else {
            privilege.doLogin(request, skey);
        }

        if (moduleCode != null && !"".equals(moduleCode.trim())) {
            JSONArray fields = new JSONArray();
            MacroCtlUnit mu;
            MacroCtlMgr mm = new MacroCtlMgr();
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCodeRelated);

                ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
                if (!mpd.canUserModify(userName)) {
                    json.put("res", "-3");
                    json.put("msg", "权限非法！");
                    return json.toString();
                }

                json.put("res", "0");
                json.put("id", id);
                json.put("parentId", parentId);

                Vector v = fd.getFields();

                // 置可写表单域
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
                if (!"".equals(fieldWrite)) {
                    String[] fds = StrUtil.split(fieldWrite, ",");
                    if (fds != null) {
                        int len = fds.length;

                        // 将不可写的域筛选出
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();

                            boolean finded = false;
                            for (int i = 0; i < len; i++) {
                                if (ff.getName().equals(fds[i])) {
                                    finded = true;
                                    break;
                                }
                            }

                            if (finded) {
                                // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                                ff.setEditable(false);
                            }
                        }
                    }

                    // 去掉隐藏字段
                    String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
                    String[] fdsHide = StrUtil.split(fieldHide, ",");
                    if (fdsHide != null) {
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();
                            for (int k = 0; k < fdsHide.length; k++) {
                                if (ff.getName().equals(fdsHide[k])) {
                                    ir.remove();
                                }
                            }
                        }
                    }
                }

                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAOByCache(id, fd);
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();

                    if (!ff.isMobileDisplay()) {
                        continue;
                    }

                    String val = fdao.getFieldValue(ff.getName());

                    JSONObject field = new JSONObject();
                    field.put("title", ff.getTitle());
                    field.put("code", ff.getName());
                    field.put("desc", StrUtil.getNullStr(ff.getDescription()));

                    // 如果是计算控件，则取出精度和四舍五入属性
                    if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                        FormParser fp = new FormParser();
                        String isroundto5 = fp.getFieldAttribute(fd, ff,
                                "isroundto5");
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
                    if (ff.getType().equals("macro")) {
                        mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu == null) {
                            LogUtil.getLog(getClass()).error(
                                    "MactoCtl " + ff.getTitle() + "："
                                            + ff.getMacroType() + " is not exist.");
                            continue;
                        }

                        macroType = mu.getIFormMacroCtl().getControlType();
                        metaData = mu.getIFormMacroCtl().getMetaData(ff);
                        macroCode = mu.getCode();

                        // 如果值为null，则在json中put的时候，是无效的，不会被记录至json中
                        controlText = StrUtil.getNullStr(mu.getIFormMacroCtl()
                                .getControlText(privilege.getUserName(skey),
                                        ff));
                        val = StrUtil.getNullStr(mu.getIFormMacroCtl()
                                .getControlValue(privilege.getUserName(skey),
                                        ff));
                        options = mu.getIFormMacroCtl().getControlOptions(
                                privilege.getUserName(skey), ff);
                        if (options != null && !options.equals("")) {
                            // options = options.replaceAll("\\\"", "");
                            js = new JSONArray(options);
                        }
                    }
                    // 判断是否为意见输入框
                    if (macroCode != null && !macroCode.equals("")) {
                        if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
                            if (controlText != null
                                    && !controlText.trim().equals("")) {
                                opinionArr = new JSONArray(controlText);
                            }
                            if (val != null && !val.trim().equals("")) {
                                opinionVal = new JSONObject(val);
                            }
                        }

                        if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
                            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                            INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                            JSONObject jsonObj = nestSheetCtl.getCtlDescription(ff);
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
                        String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                        for (int i = 0; i < optionsArray.length; i++) {
                            String[] optionsItem = optionsArray[i];
                            if (optionsItem != null && optionsItem.length == 2) {
                                JSONObject option = new JSONObject();
                                option.put("value", optionsItem[1]);
                                option.put("name", optionsItem[0]);
                                js.put(option);
                            }
                        }
                    } else if (ff.getType().equals("radio")) {
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
                    if (ff.getType().equals("checkbox")) {
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
                    // LogUtil.getLog(getClass()).info(ff.getTitle() +
                    // " controlText=" + controlText);
                    field.put("level", level);
                    field.put("macroType", macroType);
                    field.put("editable", String.valueOf(ff.isEditable()));
                    field.put("isHidden", String.valueOf(ff.isHidden()));
                    field.put("isNull", String.valueOf(ff.isCanNull()));
                    field.put("fieldType", ff.getFieldTypeDesc());

                    field.put("macroCode", macroCode);

                    // 传SQL控件条件中的字段
                    field.put("metaData", metaData);

                    fields.put(field);
                }

                json.put("fields", fields);

                Iterator itFiles = fdao.getAttachments().iterator();
                JSONArray filesArr = new JSONArray();
                while (itFiles.hasNext()) {
                    com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
                            .next();
                    JSONObject fileObj = new JSONObject();
                    String name = am.getName();
                    fileObj.put("name", name);
                    String url = "/public/android/download.do?attachId=" + am.getId() + "&visitKey=" + SecurityUtil.makeVisitKey(am.getId());
                    fileObj.put("url", url);
                    fileObj.put("id", am.getId());
                    fileObj.put("size", String.valueOf(am.getFileSize()));
                    filesArr.put(fileObj);
                }
                json.put("files", filesArr);

                json.put("formCode", fd.getCode());
                // 算式相关的字段
                json.put("funcRelatedOnChangeFields", FuncUtil.doGetFieldsRelatedOnChangeMobile(fd));
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/initNestSheet", produces = {"application/json;charset=UTF-8;"})
    public String initNestSheet(@RequestParam(defaultValue = "", required = true) String skey,
                                @RequestParam(required = true) String formCode, String parentFormCode, Long parentId, Integer flowId, Integer actionId, Long id) {
        // 手机客户端 —— 新增 判断 需要显示的列
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        try {
            boolean re = privilege.Auth(skey);
            if (re) {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            }

            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(formCode);
            if (msd == null || !msd.isLoaded()) {
                json.put("res", "-1");
                json.put("msg", "模块不存在！");
                return json.toString();
            } else {
                formCode = msd.getString("form_code");
            }

            privilege.doLogin(request, skey);

            FormDb formDb = new FormDb(formCode);
            if (formCode != null && !formCode.trim().equals("")) {
                FormDAO formDao = null;
                if (id != 0) {
                    formDao = new FormDAO();
                    formDao = formDao.getFormDAOByCache(id, formDb);
                } else {
                    formDao = new FormDAO(formDb);
                }

                if (flowId != -1) {
                    WorkflowActionDb wfa = new WorkflowActionDb();
                    wfa = wfa.getWorkflowActionDb(actionId);
                    String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

                    String[] fds = fieldWrite.split(",");
                    int len = fds.length;

                    int nestLen = "nest.".length();
                    // 将嵌套表中不可写的域筛选出
                    Iterator ir = formDao.getFields().iterator();
                    while (ir.hasNext()) {
                        FormField ff = (FormField) ir.next();

                        boolean finded = false;
                        for (int i = 0; i < len; i++) {
                            // 如果不是嵌套表格2的可写表单域
                            if (!fds[i].startsWith("nest.")) {
                                continue;
                            }
                            String fName = fds[i].substring(nestLen);
                            if (ff.getName().equals(fName)) {
                                finded = true;
                                break;
                            }
                        }

                        if (!finded) {
                            // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                            ff.setEditable(false);
                        }
                    }
                } else {
                    Vector<FormField> v = formDb.getFields();

                    // 置可写表单域
                    String userName = privilege.getUserName(skey);
                    ModulePrivDb mpd = new ModulePrivDb(formCode);
                    String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");

                    if (!"".equals(fieldWrite)) {
                        String[] fds = StrUtil.split(fieldWrite, ",");
                        if (fds != null) {
                            int len = fds.length;
                            // 将不可写的域筛选出
                            for (FormField ff : v) {
                                boolean finded = false;
                                for (String fd : fds) {
                                    if (ff.getName().equals(fd)) {
                                        finded = true;
                                        break;
                                    }
                                }

                                if (finded) {
                                    // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
                                    ff.setEditable(false);
                                }
                            }
                        }
                    }
                }

                MacroCtlMgr macroCtrlMgr = new MacroCtlMgr();
                MacroCtlUnit macroCtrlUnit = null;
                JSONArray fieldsArr = new JSONArray();
                Iterator<FormField> it = formDao.getFields().iterator();
                while (it.hasNext()) {
                    FormField ff = (FormField) it.next();
                    JSONObject field = new JSONObject();// json field
                    if (ff.isMobileDisplay() && ff.getHide() == FormField.HIDE_NONE) {
                        field.put("title", ff.getTitle());// 标题
                        field.put("isCanNull", ff.isCanNull());// 标题
                        field.put("code", ff.getName());// 名称
                        field.put("desc", StrUtil.getNullStr(ff.getDescription()));
                        // field.put("editable",true);//控件是否显示
                        // field.put("isHidden",false);//是否隐藏
                        String type = ff.getType();// 类型描述
                        field.put("type", type);

                        // 如果是计算控件，则取出精度和四舍五入属性
                        if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                            FormParser fp = new FormParser();
                            String isroundto5 = fp.getFieldAttribute(formDao.getFormDb(), ff, "isroundto5");
                            String digit = fp.getFieldAttribute(formDao.getFormDb(), ff, "digit");
                            field.put("formula", ff.getDefaultValueRaw());
                            field.put("isroundto5", isroundto5);
                            field.put("digit", digit);
                        }

                        String metaData = "";

                        // 判断是否是宏控件类型
                        if ("macro".equals(type)) {
                            String macroCode = ff.getMacroType();
                            field.put("macroCode", macroCode);// 宏控件的code
                            macroCtrlUnit = macroCtrlMgr.getMacroCtlUnit(macroCode);
                            metaData = macroCtrlUnit.getIFormMacroCtl().getMetaData(ff);
                            String macroType = macroCtrlUnit.getIFormMacroCtl().getControlType();
                            field.put("macroType", macroType);// 宏控件类型
                            String controlText = StrUtil.getNullStr(macroCtrlUnit
                                            .getIFormMacroCtl()
                                            .getControlText(
                                                    privilege
                                                            .getUserName(skey),
                                                    ff));
                            String controlValue = StrUtil
                                    .getNullStr(macroCtrlUnit
                                            .getIFormMacroCtl()
                                            .getControlValue(
                                                    privilege
                                                            .getUserName(skey),
                                                    ff));
                            field.put("text", controlText);// 文本
                            field.put("value", controlValue);// 显示的值
                            if ("select".equals(macroType) || "buttonSelect".equals(macroType)) {
                                // 一般options只有在拉框中显示
                                String options = StrUtil.getNullStr(macroCtrlUnit.getIFormMacroCtl()
                                                .getControlOptions(
                                                        privilege.getUserName(skey),
                                                        ff));
                                if (!"".equals(options.trim())) {
                                    JSONArray opinionArr = new JSONArray(
                                            options);
                                    field.put("options", opinionArr);
                                }
                            } else {
                                JSONArray opinionArr = new JSONArray();
                                field.put("options", opinionArr);
                            }
                        } else {
                            String value = StrUtil.getNullStr(ff.getValue());
                            if ("".equals(value)) {
                                value = ff.getDefaultValue();
                            }
                            if ("DATE".equals(type) || "DATE_TIME".equals(type)) {
                                if (id != 0) {
                                    field.put("value", ff.getValue());
                                } else {
                                    field.put("value", ff.getDefaultValueRaw());
                                }

                            } else if ("select".equals(type)) {// 解析普通控件中
                                // select控件
                                String[][] optionsArray = FormParser.getOptionsArrayOfSelect(formDb, ff);
                                if (optionsArray.length > 0) {
                                    JSONArray options = new JSONArray();
                                    for (String[] option : optionsArray) {
                                        JSONObject optionObj = new JSONObject();
                                        if (value.equals(option[1])) {
                                            field.put("text", option[0]);
                                            field.put("value", value);
                                        }
                                        optionObj.put("value", option[1]);
                                        optionObj.put("name", option[0]);
                                        options.put(optionObj);
                                    }
                                    field.put("options", options);
                                }
                            }
                            else if ("radio".equals(ff.getType())) {
                                JSONArray options = new JSONArray();
                                String[] optionsArray = FormParser.getValuesOfInput(formDao.getFormDb(), ff);
                                for (String s : optionsArray) {
                                    JSONObject option = new JSONObject();
                                    option.put("value", s);
                                    option.put("name", s);
                                    options.put(option);
                                }
                                field.put("options", options);
                                field.put("value", value);
                            }
                            else {
                                field.put("value", value);
                            }
                        }

                        field.put("fieldType", ff.getFieldType());
                        field.put("isReadonly", ff.isReadonly());

                        // 可传SQL控件条件中的字段
                        field.put("metaData", metaData);
                        field.put("isEditable", ff.isEditable());
                        fieldsArr.put(field);
                    }
                }
                json.put("fields", fieldsArr);
                json.put("formCode", formCode);
                json.put("res", "0");
                json.put("msg", "操作成功");

                String viewJs = moduleUtilService.doGetViewJSMobile(request, formDb, formDao, authUtil.getUserName(), false);
                json.put("viewJs", viewJs);

                if (flowId != -1) {
                    FormDb flowFd = new FormDb();
                    flowFd = flowFd.getFormDb(parentFormCode);
                    com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
                    fdaoFlow = fdaoFlow.getFormDAOByCache(flowId, flowFd);
                    parentId = fdaoFlow.getId();

                    com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                    String relateFieldValue = fdm.getRelateFieldValue(parentId, formCode);

                    json.put("cws_id", relateFieldValue);
                } else {
                    json.put("cws_id", String.valueOf(parentId));
                }
                return json.toString();
            } else {
                json.put("res", "-1");
                json.put("msg", "表单编码为空！");
                return json.toString();
            }

        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/listNestSheet", produces = {"application/json;charset=UTF-8;"})
    public String listNestSheet(@RequestParam(defaultValue = "", required = true) String skey) {
        com.alibaba.fastjson.JSONObject jsonRet = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            jsonRet.put("res", "-2");
            jsonRet.put("msg", "时间过期");
            return jsonRet.toString();
        }

        String pageType = ParamUtil.get(request, "pageType");
        String userName = privilege.getUserName(skey);
        String formCode = ParamUtil.get(request, "formCode");
        String moduleCode = "";
        // 传过来的formCode有可能是模块编码
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(formCode);
            if (msd == null || !msd.isLoaded()) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "模块不存在！");
                return jsonRet.toString();
            }
            else {
                moduleCode = formCode;
                formCode = msd.getString("form_code");
                fd = fd.getFormDb(formCode);
            }
        }
        else {
            moduleCode = formCode;
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");

        int len = 0;
        if (fields!=null) {
            len = fields.length;
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();
        FormDAO fdao = new FormDAO();
        // 注意此处不同于nest_table_view控件，需取得相关联的父表单字段的值，nest_table_view控件只会关联cwsId即父表单记录的id
        String parentFormCode = ParamUtil.get(request, "parentFormCode");
        if ("".equals(parentFormCode)) {
            // 从智能模块中传参
            String parentModuleCode = ParamUtil.get(request, "parentModuleCode");
            ModuleSetupDb msdParent = new ModuleSetupDb();
            msdParent = msdParent.getModuleSetupDb(parentModuleCode);
            parentFormCode = msdParent.getString("form_code");
        }

        int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);

        if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());

            if (lf==null) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "流程类型不存在！");
                return jsonRet.toString();
            }
            parentFormCode = lf.getFormCode();
        }

        // 20131123 fgf 添加nestFieldName，因为其中存储了“选择”按钮需要的配置信息
        String nestFieldName = ParamUtil.get(request, "nestFieldName");

        jsonRet.put("res", 0);
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        jsonRet.put("result", result);

        com.alibaba.fastjson.JSONObject json = null;
        com.alibaba.fastjson.JSONArray mapAry = new com.alibaba.fastjson.JSONArray();
        int queryId = -1;
        boolean canAdd = false, canEdit = false, canImport = false, canExport=false, canDel = false, canSel = false;
        boolean isAutoSel = false;
        FormField nestField = null;
        if (!"".equals(nestFieldName)) {
            FormDb parentFd = new FormDb();
            parentFd = parentFd.getFormDb(parentFormCode);
            nestField = parentFd.getFormField(nestFieldName);
            if (nestField==null) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "父表单（" + parentFormCode + "中的嵌套表字段：" + nestFieldName + "不存在！");
                return jsonRet.toString();
            }

            // 20200205 使智能模块中也根据嵌套表格的配置显示相应按钮
            if (false && !"".equals(moduleCode) && !"flow".equals(pageType)) { // pageType.equals("add") || pageType.equals("edit")) {
                ModulePrivDb mpd = new ModulePrivDb(moduleCode);
                canAdd = mpd.canUserAppend(userName);
                canEdit = mpd.canUserModify(userName);
                canDel = mpd.canUserManage(userName);

                canSel = canEdit;

                result.put("canAdd", canAdd);
                result.put("canEdit", canEdit);
                result.put("canDel", canDel);
                result.put("canSel", canSel);
            }
            else {
                try {
                    // 20131123 fgf 添加
                    String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
                    json = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);
                    if (json.containsKey("canAdd")) {
                        canAdd = "true".equals(json.getString("canAdd"));
                    } else {
                        // 向下兼容
                        canAdd = true;
                        canEdit = true;
                        canImport = true;
                        canExport = true;
                        canDel = true;
                    }
                    if (json.containsKey("canEdit")) {
                        canEdit = "true".equals(json.getString("canEdit"));
                    }
                    if (json.containsKey("canImport")) {
                        canImport = "true".equals(json.getString("canImport"));
                    }
                    if (json.containsKey("canDel")) {
                        canDel = "true".equals(json.getString("canDel"));
                    }
                    if (json.containsKey("canSel")) {
                        canSel = "true".equals(json.getString("canSel"));
                    }
                    if (json.containsKey("isAutoSel")) {
                        isAutoSel = "1".equals(json.getString("isAutoSel"));
                    }
                    if (json.containsKey("canExport")) {
                        canExport = "true".equals(json.getString("canExport"));
                    }

                    result.put("canAdd", canAdd);
                    result.put("canEdit", canEdit);
                    result.put("canImport", canImport);
                    result.put("canDel", canDel);
                    result.put("canSel", canSel);
                    result.put("isAutoSel", isAutoSel);
                    result.put("canExport", canExport);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                if (json.containsKey("maps")) {
                    mapAry = (com.alibaba.fastjson.JSONArray) json.get("maps");
                }
                if (json.containsKey("queryId")) {
                    queryId = StrUtil.toInt((String) json.get("queryId"));
                }
            }
        }

        String op;
        boolean isEditable = ParamUtil.getBoolean(request, "isEditable", true);
        if (isEditable) {
            op = "edit";
        }
        else {
            op = "view";
        }

        // 流程中或者智能模块编辑时，或者查看时
        if ("edit".equals(op) || "view".equals(op)) {
            // cwsId为fdao的id
            String cwsId = "";
            if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(parentFormCode);
                com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
                fdaoFlow = fdaoFlow.getFormDAOByCache(flowId, flowFd);
                cwsId = String.valueOf(fdaoFlow.getId());
            }
            else {
                cwsId = ParamUtil.get(request, "cwsId");
            }
            if ("".equals(parentFormCode)) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "嵌套表参数：父模块编码为空！");
                return jsonRet.toString();
            }

            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
            String relateFieldValue = fdm.getRelateFieldValue(StrUtil.toInt(cwsId), moduleCode);
            if (relateFieldValue==null) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "请检查模块" + fd.getName() + "（编码：" + formCode + "）是否相关联");
                return jsonRet.toString();
            }

            String sql = "select id from " + fd.getTableNameByForm() + " where cws_id='" + relateFieldValue + "'";
            sql += " and cws_parent_form=" + StrUtil.sqlstr(parentFormCode);
            sql += " order by cws_order";
            DebugUtil.i(getClass(), "sql", sql);

            Vector fdaoV = fdao.list(formCode, sql);

            if (isAutoSel) {
                if (fdaoV.size()==0) {
                    // 如果嵌套表中没有记录，则说明是正在发起流程
                    if ("edit".equals(op)) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        INestSheetCtl nestSheetCtl = macroCtlService.getNestSheetCtl();
                        re = nestSheetCtl.autoSelect(request, StrUtil.toLong(cwsId, -1), nestField);
                        if (re) {
                            fdaoV = fdao.list(formCode, sql);
                        }
                    }
                }
            }

            result.put("totalCount", fdaoV.size());
            result.put("parentId", cwsId);

            Iterator ir = fdaoV.iterator();

            int k = 0;
            UserMgr um = new UserMgr();
            WorkflowDb wf = new WorkflowDb();
            com.alibaba.fastjson.JSONArray datas = new com.alibaba.fastjson.JSONArray();
            result.put("datas", datas);

            while (ir.hasNext()) {
                fdao = (FormDAO)ir.next();
                k++;
                long id = fdao.getId();

                com.alibaba.fastjson.JSONObject row = new com.alibaba.fastjson.JSONObject();
                datas.add(row);
                com.alibaba.fastjson.JSONArray fieldAry = new com.alibaba.fastjson.JSONArray();
                row.put("rId", id);
                row.put("fields", fieldAry);

                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];

                    com.alibaba.fastjson.JSONObject fjo = new com.alibaba.fastjson.JSONObject();

                    String title = "创建者";
                    if (!"cws_creator".equals(fieldName)) {
                        if (fieldName.startsWith("main")) {
                            String[] ary = StrUtil.split(fieldName, ":");
                            FormDb mainFormDb = fm.getFormDb(ary[1]);
                            title = mainFormDb.getFieldTitle(ary[2]);
                        }
                        else if (fieldName.startsWith("other")) {
                            String[] ary = StrUtil.split(fieldName, ":");
                            if (ary.length>=8) {
                                FormDb oFormDb = fm.getFormDb(ary[5]);
                                title = oFormDb.getFieldTitle(ary[7]);
                            }
                            else {
                                FormDb otherFormDb = fm.getFormDb(ary[2]);
                                title = otherFormDb.getFieldTitle(ary[4]);
                            }
                        }
                        else if ("cws_creator".equals(fieldName)) {
                            title = "创建者";
                        } else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
                            title = "ID";
                        } else if ("cws_status".equals(fieldName)) {
                            title = "状态";
                        } else if ("cws_flag".equals(fieldName)) {
                            title = "冲抵状态";
                        } else if ("flowId".equalsIgnoreCase(fieldName)) {
                            title = "流程号";
                        } else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
                            title = "流程开始时间";
                        } else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
                            title = "流程结束时间";
                        } else if ("cws_id".equals(fieldName)) {
                            title = "关联ID";
                        }
                        else if ("cws_visited".equals(fieldName)) {
                            title = "是否已读";
                        }
                        else if ("colPrompt".equals(fieldName)) {
                            title = "colPrompt"; //
                        }
                        else if ("cws_create_date".equals(fieldName)) {
                            title = "创建时间";
                        } else if ("cws_modify_date".equals(fieldName)) {
                            title = "修改时间";
                        }
                        else {
                            title = fd.getFieldTitle(fieldName);
                        }
                    }

                    fjo.put("title", title);

                    if (!"cws_creator".equals(fieldName)) {
                        if (fieldName.startsWith("main")) {
                            String[] ary = StrUtil.split(fieldName, ":");
                            FormDb mainFormDb = fm.getFormDb(ary[1]);
                            com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
                            FormDAO fdaoMain = new FormDAO();
                            fdaoMain = fdaoMain.getFormDAOByCache(StrUtil.toLong(cwsId), mainFormDb);
                            FormField ff = mainFormDb.getFormField(ary[2]);
                            String val = "", text = "";
                            if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    val = fdaoMain.getFieldValue(ary[2]);
                                    RequestUtil.setFormDAO(request, fdaoMain);
                                    text = mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2]));
                                }
                            } else {
                                val = fdmMain.getFieldValueOfMain(StrUtil.toInt(cwsId), ary[2]);
                                text = val;
                            }

                            fjo.put("name", ff.getName());
                            fjo.put("value", val);
                            fjo.put("text", text);
                        }
                        else if (fieldName.startsWith("other:")) {
                            String val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
                            String val = String.valueOf(fdao.getId());
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("cws_flag".equals(fieldName)) {
                            String val = String.valueOf(fdao.getCwsFlag());
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("cws_creator".equals(fieldName)) {
                            String val = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("cws_status".equals(fieldName)) {
                            String val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("flowId".equalsIgnoreCase(fieldName)) {
                            String val = String.valueOf(fdao.getFlowId());
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
                            String val = "";
                            if (fdao.getFlowId() != -1) {
                                wf = wf.getWorkflowDb(fdao.getFlowId());
                                val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                            }
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
                            String val = "";
                            if (fdao.getFlowId() != -1) {
                                wf = wf.getWorkflowDb(fdao.getFlowId());
                                val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                            }
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("cws_id".equals(fieldName)) {
                            String val = fdao.getCwsId();
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        }
                        else if ("cws_visited".equals(fieldName)) {
                            String val = fdao.isCwsVisited()?"是":"否";
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        }
                        else if ("colPrompt".equals(fieldName)) {
                            continue;
                        } else if ("cws_create_date".equals(fieldName)) {
                            String val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss");
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        } else if ("cws_modify_date".equals(fieldName)) {
                            String val = DateUtil.format(fdao.getCwsModifyDate(), "yyyy-MM-dd HH:mm:ss");
                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", val);
                        }
                        else{
                            FormField ff = fd.getFormField(fieldName);
                            String val = "", text = "";
                            if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    val = fdao.getFieldValue(fieldName);
                                    RequestUtil.setFormDAO(request, fdao);
                                    text = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                }
                            }else{
                                val = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
                                text = val;
                            }

                            fjo.put("name", fieldName);
                            fjo.put("value", val);
                            fjo.put("text", text);
                        }
                    }else{
                        fjo.put("name", "cws_creator");
                        fjo.put("value", fdao.getCreator());
                        fjo.put("text", um.getUserDb(fdao.getCreator()).getRealName());
                    }

                    fieldAry.add(fjo);
                }
            }
        }
        return jsonRet.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/addNestSheet", produces = {"application/json;charset=UTF-8;"})
    public String addNestSheet(@RequestParam(defaultValue = "", required = true) String skey) {
        com.alibaba.fastjson.JSONObject jsonRet = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            jsonRet.put("res", "-2");
            jsonRet.put("msg", "时间过期");
            return jsonRet.toString();
        }

        String userName = privilege.getUserName(skey);
        privilege.doLogin(request, skey);

        String op = ParamUtil.get(request, "op");

        String formCode = ParamUtil.get(request, "formCode");
        if ("".equals(formCode)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "编码不能为空！");
            return jsonRet.toString();
        }

        String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
        String menuItem = ParamUtil.get(request, "menuItem");

        request.setAttribute("formCode", formCodeRelated);

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);
        if (fd==null || !fd.isLoaded()) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "表单不存在！");
            return jsonRet.toString();
        }

        FormDb flowFd = new FormDb();
        flowFd = flowFd.getFormDb(formCode);
        long actionId = ParamUtil.getLong(request, "actionId", -1);

        String relateFieldValue = "" + com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID;
        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId==-1) {
            if (actionId!=-1 && actionId!=0) {
                WorkflowActionDb wa = new WorkflowActionDb();
                wa = wa.getWorkflowActionDb((int)actionId);
                com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
                fdaoFlow = fdaoFlow.getFormDAOByCache(wa.getFlowId(), flowFd);
                parentId = (int)fdaoFlow.getId();
            }

            ModuleRelateDb mrd = new ModuleRelateDb();
            mrd = mrd.getModuleRelateDb(formCode, formCodeRelated);
            if (mrd==null) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "请检查模块是否相关联！");
                return jsonRet.toString();
            }
        }
        else {
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
            relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
            if (relateFieldValue==null) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "请检查模块是否相关联！");
                return jsonRet.toString();
            }
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        String moduleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(moduleCode)) {
            moduleCode = formCodeRelated;
        }
        msd = msd.getModuleSetupDbOrInit(moduleCode);
        if (msd==null) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "模块不存在！");
            return jsonRet.toString();
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserAppend(userName)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return jsonRet.toString();
        }

        // 用于com.redmoon.oa.visual.Render
        request.setAttribute("pageKind", "nest_sheet_relate");
        request.setAttribute("actionId", String.valueOf(actionId));

        if ("saveformvalue".equals(op)) {
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
            try {
                ServletContext application = request.getSession().getServletContext();
                re = fdm.create(application, request, msd);
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
                jsonRet.put("res", "-1");
                jsonRet.put("msg", e.getMessage());
                return jsonRet.toString();
            }
            if (re) {
                jsonRet.put("res", "0");
                jsonRet.put("msg", "操作成功！");

                String cwsId = String.valueOf(parentId);
                jsonRet.put("sums", FormUtil.getSums(fd, flowFd, cwsId));
            }
            else {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "操作失败！");
            }
        }
        return jsonRet.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/editNestSheet", produces = {"application/json;charset=UTF-8;"})
    public String editNestSheet(@RequestParam(defaultValue = "", required = true) String skey) {
        com.alibaba.fastjson.JSONObject jsonRet = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            jsonRet.put("res", "-2");
            jsonRet.put("msg", "时间过期");
            return jsonRet.toString();
        }

        privilege.doLogin(request, skey);

        String myname = privilege.getUserName( skey );
        String op = ParamUtil.get(request, "op");

        String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
        if ("".equals(formCode)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "编码不能为空！");
            return jsonRet.toString();
        }

        String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserManage(myname)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return jsonRet.toString();
        }

        int id = ParamUtil.getInt(request, "id");
        // 置嵌套表需要用到的cwsId
        request.setAttribute("cwsId", "" + id);
        // 置页面类型
        request.setAttribute("pageType", "edit");

        // 这里是为了使嵌套表格2表单中又存在嵌套表格2宏控件时，在getNestSheet方法中，传递给当前编辑的表单中的嵌套表格2宏控件
        // 同时也用于查询选择宏控件
        request.setAttribute("formCode", formCodeRelated);

        long actionId = ParamUtil.getLong(request, "actionId", -1);
        // 用于com.redmoon.oa.visual.Render
        request.setAttribute("pageKind", "nest_sheet_relate");
        request.setAttribute("actionId", String.valueOf(actionId));

        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID，仅用于导航，如果导航不显示，则不用传递该参数，用例：module_show_realte.jsp编辑按钮

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        String moduleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(moduleCode)) {
            moduleCode = formCodeRelated;
        }
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);

        if ("saveformvalue".equals(op)) {
            re = false;
            try {
                ServletContext application = request.getSession().getServletContext();
                re = fdm.update(application, request, msd);
            }
            catch (ErrMsgException e) {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", e.getMessage());
                return jsonRet.toString();
            }
            if (re) {
                jsonRet.put("res", "0");
                jsonRet.put("msg", "操作成功！");

                FormDb pForm = new FormDb();
                pForm = pForm.getFormDb(formCode);
                jsonRet.put("sums", com.alibaba.fastjson.JSONObject.parseObject(FormUtil.getSums(fd, pForm, String.valueOf(parentId)).toString()));
            }
            else {
                jsonRet.put("res", "-1");
                jsonRet.put("msg", "操作失败！");
            }
        }
        return jsonRet.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/delNestSheet", produces = {"application/json;charset=UTF-8;"})
    public String delNestSheet(@RequestParam(defaultValue = "", required = true) String skey,
                               @RequestParam(required = true) String formCode,
                               String formCodeRelated,
                               @RequestParam(required = true) Long id) {
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

        try {
            if (id == -1) {
                json.put("res", "-1");
                json.put("msg", "缺少ID！");
                return json.toString();
            }

            FormMgr fm = new FormMgr();
            FormDb fdRelated = fm.getFormDb(formCodeRelated);
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
            String cwsId = FormDAO.TEMP_CWS_ID;
            boolean isNestSheet = true;
            try {
                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAOByCache(id, fdRelated);
                cwsId = fdao.getCwsId();

                re = fdm.del(request, isNestSheet, formCodeRelated);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
                json.put("res", "-1");
                json.put("msg", e.getMessage());
                return json.toString();
            }

            if (re) {
                json.put("res", "0");
                json.put("msg", "操作成功！");

                FormDb pfd = new FormDb();
                pfd = pfd.getFormDb(formCode);

                json.put("sums", FormUtil.getSums(fdRelated, pfd, cwsId));
            } else {
                json.put("res", "-1");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return json.toString();
    }
    
    @RequestMapping("/download")
    public void download(HttpServletResponse response, Long attachId, String visitKey) throws IOException, ErrMsgException {
        Attachment att = new Attachment(attachId);

        boolean canUserView = false;
        String errMsg = i18nUtil.get("err_visit_invalid");
        if (!"".equals(visitKey)) {
            int r = SecurityUtil.validateVisitKey(visitKey, String.valueOf(attachId));
            if (r == 1) {
                canUserView = true;
            }
            else {
                errMsg = SecurityUtil.getValidateVisitKeyErrMsg(r);
            }
        }

        if (!canUserView) {
            String moduleCode = ParamUtil.get(request, "moduleCode");
            if (!"".equals(moduleCode)) {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                if (msd != null) {
                    // 检查数据权限，判断用户是否可以存取此条数据
                    if (!ModulePrivMgr.canAccessData(request, msd, attachId)) {
                        errMsg = i18nUtil.get("info_access_data_fail");
                    } else {
                        canUserView = true;
                    }
                } else {
                    errMsg = i18nUtil.get("err_visit_invalid_module");
                }
            }
            else {
                errMsg = i18nUtil.get("err_visit_need_module");
            }
        }
        if (!canUserView) {
            throw new ErrMsgException(errMsg);
        }

        // 如果是ntko控件在添加页面临时生成的记录，则不需要记录日志
        if (att.getVisualId() != -1) {
            AttachmentLogDb attLogDb = new AttachmentLogDb();
            attLogDb.log(SpringUtil.getUserName(), att.getVisualId(), att.getId(), AttachmentLogDb.TYPE_DOWNLOAD);
        }

        response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getName())));
        response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(att.getName()));

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            if (sysProperties.isObjStoreEnabled()) {
                InputStream is = obsServiceFactory.getInstance().getInputStream(att.getVisualPath() + "/" + att.getDiskName());
                bis = new BufferedInputStream(is);
            }
            else {
                bis = new BufferedInputStream(new FileInputStream(Global.realPath + att.getVisualPath() + "/" + att.getDiskName()));
            }
            bos = new BufferedOutputStream(response.getOutputStream());

            byte[] buff = new byte[2048];
            int bytesRead;

            while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff,0,bytesRead);
            }
        } catch(final IOException e) {
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

    /**
     * 嵌套表格拉单
     * @param skey
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/module/listNestSel", produces = {"application/json;charset=UTF-8;"})
    public String listNestSel(@RequestParam(defaultValue = "", required = true) String skey) {
        com.alibaba.fastjson.JSONObject jsonRet = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            jsonRet.put("res", "-2");
            jsonRet.put("msg", "时间过期");
            return jsonRet.toString();
        }

        privilege.doLogin(request, skey);

        String op = ParamUtil.get(request, "op");

        String nestFormCode = ParamUtil.get(request, "nestFormCode");

        String nestType = ParamUtil.get(request, "nestType");
        String parentFormCode = ParamUtil.get(request, "parentFormCode");
        String nestFieldName = ParamUtil.get(request, "nestFieldName");
        long parentId = ParamUtil.getLong(request, "parentId", -1);

        FormDb pForm = new FormDb();
        pForm = pForm.getFormDb(parentFormCode);
        FormField nestField = pForm.getFormField(nestFieldName);

        com.alibaba.fastjson.JSONObject json = null;
        com.alibaba.fastjson.JSONArray mapAry = new com.alibaba.fastjson.JSONArray();
        String filter = "";
        // 20131123 fgf 添加
        String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
        json = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);
        nestFormCode = json.getString("destForm");
        filter = json.getString("filter");
        mapAry = (com.alibaba.fastjson.JSONArray)json.get("maps");

        String moduleCode = json.getString("sourceForm");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);
        String formCode = msd.getString("form_code");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "表单不存在！");
            return jsonRet.toString();
        }

        String userName = privilege.getUserName(skey);

        ModulePrivDb mpd = new ModulePrivDb(formCode);
        if (!mpd.canUserSee(userName)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return jsonRet.toString();
        }

        if ("selBatch".equals(op)) {
            DeptUserDb dud = new DeptUserDb();
            String unitCode = dud.getUnitOfUser(userName).getCode();

            FormDb nestFd = new FormDb();
            nestFd = nestFd.getFormDb(nestFormCode);

            int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);

            com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
            com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
            String ids = ParamUtil.get(request, "ids");
            String[] ary = StrUtil.split(ids, ",");
            if (ary!=null) {
                // 取出待插入的数据
                for (String s : ary) {
                    long id = StrUtil.toLong(s);
                    fdao = fdao.getFormDAOByCache(id, fd);

                    ModuleSetupDb msdNest = new ModuleSetupDb();
                    msdNest = msdNest.getModuleSetupDbOrInit(nestFormCode);
                    // String listField = StrUtil.getNullStr(msdNest.getString("list_field"));
                    String[] fields = msdNest.getColAry(false, "list_field");

                    int len = 0;
                    if (fields != null) {
                        len = fields.length;
                    }

                    // 根据映射关系赋值
                    com.alibaba.fastjson.JSONObject jsonObj2 = new com.alibaba.fastjson.JSONObject();
                    for (int k = 0; k < mapAry.size(); k++) {
                        com.alibaba.fastjson.JSONObject jsonObj = mapAry.getJSONObject(k);
                        String sfield = (String) jsonObj.get("sourceField");
                        String dfield = (String) jsonObj.get("destField");

                        String fieldValue = fdao.getFieldValue(sfield);
                        if (sfield.equals(FormDAO.FormDAO_NEW_ID) || "FormDAO_ID".equals(sfield)) {
                            fieldValue = String.valueOf(fdao.getId());
                        }

                        fdaoNest.setFieldValue(dfield, fieldValue);
                    }

                    fdaoNest.setFlowId(flowId);
                    fdaoNest.setCwsId(String.valueOf(parentId));
                    fdaoNest.setCreator(userName);

                    fdaoNest.setUnitCode(unitCode);
                    fdaoNest.setCwsQuoteId((int) id);
                    fdaoNest.setCwsParentForm(parentFormCode);
                    fdaoNest.setCwsQuoteForm(formCode);
                    try {
                        re = fdaoNest.create();
                    } catch (SQLException ex) {
                        LogUtil.getLog(getClass()).error(ex);
                        jsonRet.put("res", "-1");
                        jsonRet.put("msg", ex.getMessage());
                        return jsonRet.toString();
                    }
                }

                jsonRet.put("res", "0");

                jsonRet.put("nestFormCode", nestFormCode);
                jsonRet.put("sums", FormUtil.getSums(nestFd, pForm, String.valueOf(parentId)));
            }
            else {
                jsonRet.put("res", "0");
                jsonRet.put("msg", "请选择记录！");
            }
            return jsonRet.toString();
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        String querystr = "";

        int pagesize = 20;
        Paginator paginator = new Paginator(request);
        int curpage = paginator.getCurPage();

        // 过滤条件
        String conds = filter; // ParamUtil.get(request, "filter");
        if ("none".equals(conds)) {
            conds = "";
        }

        String action = ParamUtil.get(request, "action");

        msd = msd.getModuleSetupDbOrInit(formCode);

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
        String[] btnBclasses = StrUtil.split(btnBclass, ",");

        MacroCtlMgr mm = new MacroCtlMgr();
        FormMgr fm = new FormMgr();

        com.alibaba.fastjson.JSONArray conditions = new com.alibaba.fastjson.JSONArray();
        int len = 0;
        boolean isQuery = false;
        if (btnNames!=null) {
            len = btnNames.length;
            for (int i=0; i<len; i++) {
                if (btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject jsonBtn = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if ("queryFields".equals(jsonBtn.get("btnType"))) {
                        isQuery = true;
                        String condFields = (String)jsonBtn.get("fields");
                        String[] fieldAry = StrUtil.split(condFields, ",");
                        for (String fieldName : fieldAry) {
                            String condType = (String) jsonBtn.get(fieldName);
                            String queryValue = ParamUtil.get(request, fieldName);

                            if ("cws_status".equals(fieldName)) {
                                String nameCond = ParamUtil.get(request, fieldName + "_cond");
                                if ("".equals(nameCond)) {
                                    nameCond = condType;
                                }

                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", "状态");
                                jo.put("fieldType", FormField.FIELD_TYPE_INT);
                                jo.put("fieldCond", condType);

                                String fieldOptions;
                                com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();

                                com.alibaba.fastjson.JSONObject jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "不限");
                                jsObj.put("value", SQLBuilder.CWS_STATUS_NOT_LIMITED);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DRAFT);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_NOT);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DONE);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_REFUSED);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DISCARD);
                                ary.add(jsObj);

                                jo.put("fieldOptions", ary.toString());

                                int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);
                                if (queryValueCwsStatus != -20000) {
                                    queryValue = String.valueOf(queryValueCwsStatus);
                                } else {
                                    queryValue = String.valueOf(msd.getInt("cws_status"));
                                }

                                jo.put("fieldValue", queryValue);
                                conditions.add(jo);
                            } else if ("cws_flag".equals(fieldName)) {
                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", "冲抵状态");
                                jo.put("fieldType", FormField.FIELD_TYPE_INT);
                                jo.put("fieldCond", condType);

                                String fieldOptions;
                                com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();

                                com.alibaba.fastjson.JSONObject jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "不限");
                                jsObj.put("value", -1);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "否");
                                jsObj.put("value", 0);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "是");
                                jsObj.put("value", 1);
                                ary.add(jsObj);

                                jo.put("fieldOptions", ary.toString());
                                jo.put("fieldValue", queryValue);
                                conditions.add(jo);
                            } else {
                                String title = "";
                                FormField ff = null;
                                if (fieldName.startsWith("main:")) { // 关联的主表
                                    String[] aryField = StrUtil.split(fieldName, ":");
                                    if (aryField.length == 3) {
                                        FormDb mainFormDb = fm.getFormDb(aryField[1]);
                                        ff = mainFormDb.getFormField(aryField[2]);
                                        if (ff == null) {
                                            LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                        }
                                        title = ff.getTitle();
                                    } else {
                                        LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                    }
                                } else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
                                    String[] aryField = StrUtil.split(fieldName, ":");
                                    if (aryField.length < 5) {
                                        LogUtil.getLog(getClass()).warn(fieldName + "格式非法");
                                    } else {
                                        FormDb otherFormDb = fm.getFormDb(aryField[2]);
                                        ff = otherFormDb.getFormField(aryField[4]);
                                        if (ff == null) {
                                            LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                        }
                                        title = ff.getTitle();
                                    }
                                } else {
                                    ff = fd.getFormField(fieldName);
                                }

                                if (ff == null) {
                                    continue;
                                }

                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", ff.getTitle());
                                jo.put("fieldType", ff.getFieldType());
                                jo.put("fieldCond", condType);
                                jo.put("fieldValue", queryValue);

                                String fieldOptions = "";
                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                    if (mu != null) {
                                        IFormMacroCtl imc = mu.getIFormMacroCtl();
                                        if ("select".equals(imc.getControlType())) {
                                            fieldOptions = imc.getControlOptions(userName, ff);
                                        }
                                    }
                                }
                                jo.put("fieldOptions", fieldOptions);

                                if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                    if ("0".equals(condType)) {
                                        String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                                        String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                                        jo.put("fromDate", fDate);
                                        jo.put("toDate", fDate);
                                    }
                                }

                                conditions.add(jo);
                            }
                        }
                    }
                }
            }
        }

        jsonRet.put("res", 0);

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        result.put("op", op);
        result.put("action", action);
        result.put("conditions", conditions);

        // 取得过滤条件中的父窗口的字段
        boolean isFound = false;
        StringBuffer parentFields = new StringBuffer();
        if (!"".equals(filter)) {
            Pattern p = Pattern.compile(
                    "\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(filter);
            while (m.find()) {
                String fieldName = m.group(1);
                if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
                        || "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName)) {
                    isFound = true;
                    continue;
                }

                StrUtil.concat(parentFields, ",", fieldName);
                isFound = true;
            }
        }

        result.put("parentFields", parentFields.toString());
        jsonRet.put("result", result);

        // 如果未从父窗口中取值，则返回，客户端需取值后再提交
        if (isFound) {
            if (!"afterGetClientValue".equals(action)) {
                return jsonRet.toString();
            }
        }

        // String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";

        // 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
        request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
        String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "search", orderBy, sort);
        String sql = ary[0];

        DebugUtil.i(getClass(), "sql", sql);

        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
        long total = lr.getTotal();
        Vector v = lr.getResult();
        Iterator ir = null;
        if (v!=null) {
            ir = v.iterator();
        }
        paginator.init(total, pagesize);
        // 设置当前页数和总页数
        int totalpages = paginator.getTotalPages();
        if (totalpages==0) {
            curpage = 1;
            totalpages = 1;
        }

        String[] fields = msd.getColAry(false, "list_field");

        result.put("totalCount", total);
        result.put("filter", filter);

        len = 0;
        if (fields!=null) {
            len = fields.length;
        }
        com.alibaba.fastjson.JSONArray datas = new com.alibaba.fastjson.JSONArray();
        jsonRet.put("datas", datas);

        int k = 0;
        UserMgr um = new UserMgr();
        while (ir!=null && ir.hasNext()) {
            fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
            k++;
            long id = fdao.getId();

            com.alibaba.fastjson.JSONObject row = new com.alibaba.fastjson.JSONObject();
            datas.add(row);
            com.alibaba.fastjson.JSONArray fieldAry = new com.alibaba.fastjson.JSONArray();
            row.put("rId", id);
            row.put("fields", fieldAry);

            for (int i=0; i<len; i++) {
                com.alibaba.fastjson.JSONObject fjo = new com.alibaba.fastjson.JSONObject();
                String fieldName = fields[i];
                if ("cws_creator".equals(fieldName)) {
                    String realName = "";
                    if (fdao.getCreator()!=null) {
                        UserDb user = um.getUserDb(fdao.getCreator());
                        if (user!=null) {
                            realName = user.getRealName();
                        }
                    }

                    fjo.put("title", "创建者");
                    fjo.put("name", "cws_creator");
                    fjo.put("value", fdao.getCreator());
                    fjo.put("text", realName);
                }
                else if ("cws_status".equals(fieldName)) {
                    fjo.put("title", "状态");
                    fjo.put("name", "cws_status");
                    fjo.put("value", fdao.getCwsStatus());
                    fjo.put("text", com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
                }
                else if ("cws_flag".equals(fieldName)) {
                    fjo.put("title", "冲抵状态");
                    fjo.put("name", "cws_flag");
                    fjo.put("value", fdao.getCwsFlag());
                    fjo.put("text", fdao.getCwsFlag());
                }
                else if ("cws_progress".equals(fieldName)) {
                    fjo.put("title", "进度");
                    fjo.put("name", "cws_progress");
                    fjo.put("value", fdao.getCwsProgress());
                    fjo.put("text", fdao.getCwsProgress());
                }
                else if ("ID".equals(fieldName)) {
                    fjo.put("title", "ID");
                    fjo.put("name", "ID");
                    fjo.put("value", fdao.getId());
                    fjo.put("text", fdao.getId());
                }
                else {
                    if (fieldName.startsWith("main:")) {
                        String[] aryMain = StrUtil.split(fieldName, ":");
                        FormDb mainFormDb = fm.getFormDb(aryMain[1]);
                        com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
                        com.redmoon.oa.visual.FormDAO fdaoMain = new FormDAO();
                        fdaoMain = fdaoMain.getFormDAOByCache(parentId, mainFormDb);
                        FormField ff = mainFormDb.getFormField(aryMain[2]);
                        String val = "", text = "";
                        if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                val = fdaoMain.getFieldValue(aryMain[2]);
                                text = mu.getIFormMacroCtl().converToHtml(request, ff, val);
                            }
                        } else {
                            val = fdmMain.getFieldValueOfMain(parentId, aryMain[2]);
                            text = val;
                        }

                        fjo.put("title", ff.getTitle());
                        fjo.put("name", ff.getName());
                        fjo.put("value", val);
                        fjo.put("text", text);
                    }
                    else if (fieldName.startsWith("other:")) {
                        // 一级
                        String title = "";
                        String[] aryField = StrUtil.split(fieldName, ":");
                        if (aryField.length<5) {
                            LogUtil.getLog(getClass()).warn(fieldName + "格式非法");
                            continue;
                        }
                        else {
                            FormDb otherFormDb = fm.getFormDb(aryField[2]);
                            FormField ff = otherFormDb.getFormField(aryField[4]);
                            if (ff==null) {
                                LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                continue;
                            }
                            title = ff.getTitle();
                        }

                        String text = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);

                        fjo.put("title", title);
                        fjo.put("name", fieldName);
                        fjo.put("value", text);
                        fjo.put("text", text);
                        // String[] ary = StrUtil.split(fieldName, ":");

                        // FormDb otherFormDb = fm.getFormDb(ary[2]);
                        // com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
                        // out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
                    }
                    else {
                        FormField ff = fd.getFormField(fieldName);
                        if (ff!=null) {
                            String tempValue = "";
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    tempValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                }
                            } else {
                                tempValue = fdao.getFieldValue(fieldName);
                            }

                            fjo.put("title", ff.getTitle());
                            fjo.put("name", ff.getName());
                            fjo.put("value", fdao.getFieldValue(fieldName));
                            fjo.put("text", tempValue);
                        }
                    }
                }

                fieldAry.add(fjo);
            }
        }
        return jsonRet.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/module/moduleListSel", produces = {"application/json;charset=UTF-8;"})
    public String moduleListSel(@RequestParam(defaultValue = "", required = true) String skey) {
        com.alibaba.fastjson.JSONObject jsonRet = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            jsonRet.put("res", "-2");
            jsonRet.put("msg", "时间过期");
            return jsonRet.toString();
        }

        String op = ParamUtil.get(request, "op");
        String moduleCode = ParamUtil.get(request, "formCode");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(msd.getString("form_code"));
        if (!fd.isLoaded()) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", "表单不存在！");
            return jsonRet.toString();
        }

        String userName = privilege.getUserName(skey);
        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        if (!mpd.canUserSee(userName)) {
            jsonRet.put("res", "-1");
            jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return jsonRet.toString();
        }

        String byFieldName = ParamUtil.get(request, "byFieldName");
        String showFieldName = ParamUtil.get(request, "showFieldName");

        String openerFormCode = ParamUtil.get(request, "openerFormCode");
        String openerFieldName = ParamUtil.get(request, "openerFieldName");

        String filter = "";

        FormDb openerFd = new FormDb();
        openerFd = openerFd.getFormDb(openerFormCode);
        FormField openerField = openerFd.getFormField(openerFieldName);
        com.alibaba.fastjson.JSONArray mapAry = new com.alibaba.fastjson.JSONArray();

        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
        String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(desc);
        filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));

        // 过滤条件
        // String filter = ParamUtil.get(request, "filter");
        if (filter.equals("none")) {
            filter = "";
        }
        /*
        String sourceFormCode = json.getString("sourceFormCode");
        String byFieldName = json.getString("idField");
        String showFieldName = json.getString("showField");
        */
        mapAry = (com.alibaba.fastjson.JSONArray)json.get("maps");

        String action = ParamUtil.get(request, "action");

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
        String[] btnBclasses = StrUtil.split(btnBclass, ",");

        MacroCtlMgr mm = new MacroCtlMgr();
        FormMgr fm = new FormMgr();

        com.alibaba.fastjson.JSONArray conditions = new com.alibaba.fastjson.JSONArray();
        int len = 0;
        boolean isQuery = false;
        if (btnNames!=null) {
            len = btnNames.length;
            for (int i=0; i<len; i++) {
                if (btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject jsonBtn = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if (((String)jsonBtn.get("btnType")).equals("queryFields")) {
                        isQuery = true;
                        String condFields = (String)jsonBtn.get("fields");
                        String[] fieldAry = StrUtil.split(condFields, ",");
                        for (int j=0; j<fieldAry.length; j++) {
                            String fieldName = fieldAry[j];
                            String condType = (String)jsonBtn.get(fieldName);
                            String queryValue = ParamUtil.get(request, fieldName);

                            if ("cws_status".equals(fieldName)) {
                                String nameCond = ParamUtil.get(request, fieldName + "_cond");
                                if ("".equals(nameCond)) {
                                    nameCond = condType;
                                }

                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", "状态");
                                jo.put("fieldType", FormField.FIELD_TYPE_INT);
                                jo.put("fieldCond", condType);

                                String fieldOptions;
                                com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();

                                com.alibaba.fastjson.JSONObject jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "不限");
                                jsObj.put("value", SQLBuilder.CWS_STATUS_NOT_LIMITED);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DRAFT);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_NOT);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DONE);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_REFUSED);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD));
                                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DISCARD);
                                ary.add(jsObj);

                                jo.put("fieldOptions", ary.toString());

                                int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);
                                if (queryValueCwsStatus!=-20000) {
                                    queryValue = String.valueOf(queryValueCwsStatus);
                                } else {
                                    queryValue = String.valueOf(msd.getInt("cws_status"));
                                }

                                jo.put("fieldValue", queryValue);
                                conditions.add(jo);
                            }
                            else if ("cws_flag".equals(fieldName)) {
                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", "冲抵状态");
                                jo.put("fieldType", FormField.FIELD_TYPE_INT);
                                jo.put("fieldCond", condType);

                                String fieldOptions;
                                com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();

                                com.alibaba.fastjson.JSONObject jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "不限");
                                jsObj.put("value", -1);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "否");
                                jsObj.put("value", 0);
                                ary.add(jsObj);

                                jsObj = new com.alibaba.fastjson.JSONObject();
                                jsObj.put("name", "是");
                                jsObj.put("value", 1);
                                ary.add(jsObj);

                                jo.put("fieldOptions", ary.toString());
                                jo.put("fieldValue", queryValue);
                                conditions.add(jo);
                            }
                            else {
                                String title = "";
                                FormField ff = null;
                                if (fieldName.startsWith("main:")) { // 关联的主表
                                    String[] aryField = StrUtil.split(fieldName, ":");
                                    if (aryField.length==3) {
                                        FormDb mainFormDb = fm.getFormDb(aryField[1]);
                                        ff = mainFormDb.getFormField(aryField[2]);
                                        if (ff==null) {
                                            LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                        }
                                        title = ff.getTitle();
                                    }
                                    else {
                                        LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                    }
                                }
                                else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
                                    String[] aryField = StrUtil.split(fieldName, ":");
                                    if (aryField.length<5) {
                                        LogUtil.getLog(getClass()).warn(fieldName + "格式非法");
                                    }
                                    else {
                                        FormDb otherFormDb = fm.getFormDb(aryField[2]);
                                        ff = otherFormDb.getFormField(aryField[4]);
                                        if (ff==null) {
                                            LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                        }
                                        title = ff.getTitle();
                                    }
                                }
                                else {
                                    ff = fd.getFormField(fieldName);
                                }

                                if (ff==null) {
                                    continue;
                                }

                                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                                jo.put("fieldName", fieldName);
                                jo.put("fieldTitle", ff.getTitle());
                                jo.put("fieldType", ff.getFieldType());
                                jo.put("type", ff.getType());
                                jo.put("fieldCond", condType);
                                jo.put("fieldValue", queryValue);

                                String fieldOptions = "";
                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                    if (mu != null) {
                                        IFormMacroCtl imc = mu.getIFormMacroCtl();
                                        if ("select".equals(imc.getControlType())) {
                                            fieldOptions = imc.getControlOptions(userName, ff);
                                        }
                                    }
                                }
                                jo.put("fieldOptions", fieldOptions);

                                if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                    if (condType.equals("0")) {
                                        String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                                        String tDate  = ParamUtil.get(request, ff.getName() + "ToDate");
                                        jo.put("fromDate", fDate);
                                        jo.put("toDate", fDate);
                                    }
                                }

                                conditions.add(jo);
                            }
                        }
                    }
                }
            }
        }

        jsonRet.put("res", 0);

        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        result.put("op", op);
        result.put("action", action);
        result.put("filter", filter);
        result.put("conditions", conditions);

        // 取得过滤条件中的父窗口的字段
        boolean isFound = false;
        StringBuffer parentFields = new StringBuffer();
        if (!"".equals(filter)) {
            Pattern p = Pattern.compile(
                    "\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(filter);
            while (m.find()) {
                String fieldName = m.group(1);
                // 当条件为包含时，fieldName以@开头
                if (fieldName.startsWith("@")) {
                    fieldName = fieldName.substring(1);
                } else if (fieldName.equals("cwsCurUser")) { // 当前用户
                    isFound = true;
                    continue;
                }

                StrUtil.concat(parentFields, ",", fieldName);
                isFound = true;
            }
        }

        result.put("parentFields", parentFields.toString());
        jsonRet.put("result", result);

        // 如果未从父窗口中取值，则返回，客户端需取值后再提交
        if (isFound) {
            if (!"afterGetClientValue".equals(action)) {
                return jsonRet.toString();
            }
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }
        // 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
        request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
        String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "search", orderBy, sort);
        String sql = ary[0];

        int pagesize = 10;
        Paginator paginator = new Paginator(request);
        int curpage = paginator.getCurPage();

        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

        ListResult lr = fdao.listResult(msd.getString("form_code"), sql, curpage, pagesize);
        long total = lr.getTotal();
        Vector v = lr.getResult();
        Iterator ir = null;
        if (v!=null) {
            ir = v.iterator();
        }
        paginator.init(total, pagesize);
        // 设置当前页数和总页数
        int totalpages = paginator.getTotalPages();
        if (totalpages==0)
        {
            curpage = 1;
            totalpages = 1;
        }

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        result.put("totalCount", total);

        len = 0;
        if (fields!=null) {
            len = fields.length;
        }
        com.alibaba.fastjson.JSONArray datas = new com.alibaba.fastjson.JSONArray();
        jsonRet.put("datas", datas);

        int k = 0;
        UserMgr um = new UserMgr();
        while (ir!=null && ir.hasNext()) {
            fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
            k++;

            long id = fdao.getId();

            com.alibaba.fastjson.JSONObject row = new com.alibaba.fastjson.JSONObject();
            datas.add(row);
            com.alibaba.fastjson.JSONArray fieldAry = new com.alibaba.fastjson.JSONArray();
            row.put("rId", id);
            row.put("fields", fieldAry);

            String showValue = "";
            boolean isShowFieldFound = false;
            for (int i=0; i<len; i++) {
                com.alibaba.fastjson.JSONObject fjo = new com.alibaba.fastjson.JSONObject();
                String fieldName = fields[i];
                if ("cws_creator".equals(fieldName)) {
                    String realName = "";
                    if (fdao.getCreator()!=null) {
                        UserDb user = um.getUserDb(fdao.getCreator());
                        if (user!=null) {
                            realName = user.getRealName();
                        }
                    }

                    fjo.put("title", "创建者");
                    fjo.put("name", "cws_creator");
                    fjo.put("value", fdao.getCreator());
                    fjo.put("text", realName);
                }
                else if ("cws_status".equals(fieldName)) {
                    fjo.put("title", "状态");
                    fjo.put("name", "cws_status");
                    fjo.put("value", fdao.getCwsStatus());
                    fjo.put("text", com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
                }
                else if (fieldName.equals("cws_flag")) {
                    fjo.put("title", "冲抵状态");
                    fjo.put("name", "cws_flag");
                    fjo.put("value", fdao.getCwsFlag());
                    fjo.put("text", fdao.getCwsFlag());
                }
                else if (fieldName.equals("cws_progress")) {
                    fjo.put("title", "进度");
                    fjo.put("name", "cws_progress");
                    fjo.put("value", fdao.getCwsProgress());
                    fjo.put("text", fdao.getCwsProgress());
                }
                else if (fieldName.equals("ID")) {
                    fjo.put("title", "ID");
                    fjo.put("name", "ID");
                    fjo.put("value", fdao.getId());
                    fjo.put("text", fdao.getId());
                }
                else {
                    if (fieldName.startsWith("main:")) {
                        String[] aryMain = StrUtil.split(fieldName, ":");
                        FormDb mainFormDb = fm.getFormDb(aryMain[1]);
                        long parentId = StrUtil.toLong(fdao.getCwsId(), -1);
                        com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
                        FormDAO fdaoMain = new FormDAO();
                        fdaoMain = fdaoMain.getFormDAOByCache(parentId, mainFormDb);
                        FormField ff = mainFormDb.getFormField(aryMain[2]);
                        String val = "", text = "";
                        if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                val = fdaoMain.getFieldValue(aryMain[2]);
                                text = mu.getIFormMacroCtl().converToHtml(request, ff, val);
                            }
                        } else {
                            val = fdmMain.getFieldValueOfMain(parentId, aryMain[2]);
                            text = val;
                        }

                        fjo.put("title", ff.getTitle());
                        fjo.put("name", ff.getName());
                        fjo.put("value", val);
                        fjo.put("text", text);
                    }
                    else if (fieldName.startsWith("other:")) {
                        // 一级
                        String title = "";
                        String[] aryField = StrUtil.split(fieldName, ":");
                        if (aryField.length<5) {
                            LogUtil.getLog(getClass()).warn(fieldName + "格式非法");
                            continue;
                        }
                        else {
                            FormDb otherFormDb = fm.getFormDb(aryField[2]);
                            FormField ff = otherFormDb.getFormField(aryField[4]);
                            if (ff==null) {
                                LogUtil.getLog(getClass()).warn(fieldName + "不存在");
                                continue;
                            }
                            title = ff.getTitle();
                        }

                        String text = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);

                        fjo.put("title", title);
                        fjo.put("name", fieldName);
                        fjo.put("value", text);
                        fjo.put("text", text);
                        // String[] ary = StrUtil.split(fieldName, ":");

                        // FormDb otherFormDb = fm.getFormDb(ary[2]);
                        // com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
                        // out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
                    }
                    else {
                        FormField ff = fd.getFormField(fieldName);
                        if (ff!=null) {
                            String tempValue = "";
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    tempValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                }
                            } else {
                                tempValue = fdao.getFieldValue(fieldName);
                            }

                            fjo.put("title", ff.getTitle());
                            fjo.put("name", ff.getName());
                            fjo.put("value", fdao.getFieldValue(fieldName));
                            fjo.put("text", tempValue);
                        }
                    }
                }

                fieldAry.add(fjo);
            }

            String byValue = "";
            if (byFieldName.equals("id")) {
                byValue = "" + id;
            }
            else {
                byValue = fdao.getFieldValue(byFieldName);
            }
            // @task:id在设置宏控件时，还不能被配置为被显示
            if (showFieldName.equals("id")) {//yonghu bdyhxz
                showValue = "" + id;
            }
            else {
                FormField ff = fd.getFormField(showFieldName);
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
                    }
                } else {
                    showValue = fdao.getFieldValue(showFieldName);
                }
            }

            row.put("byValue", byValue);
            row.put("showValue", showValue);

            com.alibaba.fastjson.JSONArray fieldMapAry = new com.alibaba.fastjson.JSONArray();
            row.put("parentFieldMaps", fieldMapAry);

            for (int i=0; i<mapAry.size(); i++) {
                com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject)mapAry.get(i);
                String destF = (String)jsonObject.get("destField");	// 父页面
                String sourceF = (String)jsonObject.get("sourceField");	// module_list_sel.jsp页面
                Vector vector = openerFd.getFields();
                Iterator it = vector.iterator();
                FormField tempFf = null;
                while (it.hasNext()) {
                    tempFf = (FormField) it.next();
                    if (tempFf.getName().equals(destF)) {
                        break;
                    }
                }
                boolean isMacro = false;
                // setValue为module_list_sel.jsp页面中所选择的值
                String setValue = fdao.getFieldValue(sourceF);
                if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                    tempFf.setValue(setValue);
                    isMacro = true;
                    setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().converToHtml(request, tempFf, setValue).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
                }

                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                jo.put("name", destF);
                jo.put("value", fdao.getFieldValue(sourceF));
                jo.put("text", setValue);
                jo.put("isMacro", isMacro);

                fieldMapAry.add(jo);
            }
        }
        return jsonRet.toString();
    }
}
