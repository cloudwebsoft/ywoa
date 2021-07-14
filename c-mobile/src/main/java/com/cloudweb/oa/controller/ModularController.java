package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.INestSheetCtl;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
            String filter = StrUtil.getNullStr(msd.getString("filter")).trim();
            boolean isComb = filter.startsWith("<items>") || filter.equals("");
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

                if (formCode == null || formCode.trim().equals("")) {
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
                                    if (((String) jsonBtn.get("btnType")).equals("queryFields")) {
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

                        String[] ary = null;
                        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
                        ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
                        String sql = ary[0];

                        // 增加权限控制
                        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
                        boolean canAdd = mpd.canUserAppend(userName);
                        boolean canEdit = mpd.canUserModify(userName);
                        boolean canDel = mpd.canUserManage(userName);
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
                                        if (fieldName.equals("colOperate")) {
                                            continue;
                                        }
                                        JSONObject formFieldObj = new JSONObject(); //Form表单对象
                                        // System.out.println(ListAction.class.getName() + " fieldName=" + fieldName);
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
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
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
                                                    e.printStackTrace();
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
                                        } else if (fieldName.equals("flowId")) {
                                            controlText = String.valueOf(fdao.getFlowId());
                                        } else if (fieldName.equals("cws_status")) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                                        } else if (fieldName.equals("cws_flag")) {
                                            controlText = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                                        } else if (fieldName.equals("cws_create_date")) {
                                            controlText = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss");
                                        } else if (fieldName.equals("flow_begin_date")) {
                                            int flowId = fdao.getFlowId();
                                            if (flowId != -1) {
                                                wf = wf.getWorkflowDb(flowId);
                                                controlText = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                                            }
                                        } else if (fieldName.equals("flow_end_date")) {
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

            if (moduleCode != null && !moduleCode.trim().equals("")) {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String formCode = msd.getString("form_code");
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);

                Vector v = fd.getFields();
                Vector vWritable = new Vector(); // 可写表单域（去除了隐藏字段）

                // 置可写表单域
                String userName = privilege.getUserName(skey);
                ModulePrivDb mpd = new ModulePrivDb(formCode);
                String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
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

                if (!"".equals(fieldWrite)) {
                    String[] fds = StrUtil.split(fieldWrite, ",");
                    if (fds != null) {
                        int len = fds.length;

                        // 将不可写的域筛选出
                        ir = v.iterator();
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
                        ir = vWritable.iterator();
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
                ir = vWritable.iterator();
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
                            JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
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
                                e.printStackTrace();
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

                json.put("fields", fields);
                // 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());
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
                e.printStackTrace();
            }
        }

        try {
            FormDb fd = new FormDb();
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String formCode = msd.getString("form_code");
            fd = fd.getFormDb(formCode);
            FormDAO fdao = new FormDAO(fd);
            fdao = fdao.getFormDAO(id, fd);
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
                e.printStackTrace();
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
                fdao = fdao.getFormDAO(id, fd);
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
                            JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
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
                    String url = "/public/visual/visual_getfile.jsp?attachId=" + am.getId();
                    fileObj.put("url", url);
                    fileObj.put("id", am.getId());
                    fileObj.put("size", String.valueOf(am.getFileSize()));
                    filesArr.put(fileObj);
                }
                json.put("files", filesArr);

                // 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());
            } catch (JSONException e) {
                e.printStackTrace();
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
                e.printStackTrace();
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
            fdao = fdao.getFormDAO(id, fd);

            mm = new MacroCtlMgr();
            json.put("res", "0");
            json.put("id", id);

            JSONArray fields = new JSONArray();

            ModulePrivDb mpd = new ModulePrivDb(formCode);
            boolean isHideField = true;
            String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
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
            String[] fdsHide = StrUtil.split(fieldHide, ",");

            Vector v = fdao.getFields();
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();

                // 跳过隐藏域
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

                String val = StrUtil.getNullStr(ff.getValue());

                JSONObject field = new JSONObject();
                String macroCode = "";
                JSONArray jsonArr = null;
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        val = mu.getIFormMacroCtl().getControlText(privilege.getUserName(skey), ff);
                        macroCode = mu.getCode();
                        if (macroCode != null && !macroCode.equals("")) {
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
                                JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                                if (jsonObj != null) {
                                    field.put("desc", jsonObj);
                                } else {
                                    field.put("desc", ff.getDescription());
                                }
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
                if (ff.getType().equals("checkbox")) {
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
                String url = "/public/visual/visual_getfile.jsp?attachId=" + am.getId();
                fileObj.put("url", url);
                fileObj.put("id", am.getId());
                fileObj.put("size", String.valueOf(am.getFileSize()));
                filesArr.put(fileObj);
            }
            json.put("files", filesArr);

            // 关联模块
            FormDb fdRelated = new FormDb();
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
            json.put("formRelated", arrRelated);

            // 其它标签
            String[] subTags = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
            String[] subTagUrls = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
            int subLen = 0;
            if (subTags != null) {
                subLen = subTags.length;
            }
            JSONArray subArr = new JSONArray();
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
            json.put("subTags", subArr);
        } catch (JSONException e) {
            e.printStackTrace();
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
                e.printStackTrace();
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
                                    // System.out.println(getClass() + " " + btnScripts[i]);
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
                                boolean canDel = mpd.canUserManage(userName);
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
                                        // System.out.println(ListAction.class.getName() + " fieldName=" + fieldName);
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
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
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
                                                    e.printStackTrace();
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
                                                if (user != null)
                                                    realName = user.getRealName();
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
                            JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
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
                                e.printStackTrace();
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
                e.printStackTrace();
            }
        }

        String userName = privilege.getUserName(skey);
        if (userName == null || "".equals(userName)) {
            com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
            userName = pvg.getUser(request);
        } else {
            privilege.doLogin(request, skey);
        }

        if (moduleCode != null && !moduleCode.trim().equals("")) {
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
                fdao = fdao.getFormDAO(id, fd);
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
                            JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                            if (jsonObj != null) {
                                field.put("desc", jsonObj);
                            }
                        }
                    }
                    field.put("type", ff.getType());
                    if (ff.getType().equals("select")) {
                        String[][] optionsArray = FormParser
                                .getOptionsArrayOfSelect(fd, ff);
                        for (int i = 0; i < optionsArray.length; i++) {
                            String[] optionsItem = optionsArray[i];
                            if (optionsItem != null && optionsItem.length == 2) {
                                JSONObject option = new JSONObject();
                                option.put("value", optionsItem[0]);
                                option.put("name", optionsItem[1]);
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
                    String url = "/public/visual/visual_getfile.jsp?attachId=" + am.getId();
                    fileObj.put("url", url);
                    fileObj.put("id", am.getId());
                    fileObj.put("size", String.valueOf(am.getFileSize()));
                    filesArr.put(fileObj);
                }
                json.put("files", filesArr);
            } catch (JSONException e) {
                e.printStackTrace();
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
                    formDao = new FormDAO(id, formDb);
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
                    Vector v = formDb.getFields();

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
                    }
                }


                MacroCtlMgr macroCtrlMgr = new MacroCtlMgr();
                MacroCtlUnit macroCtrlUnit = null;
                Iterator it = formDao.getFields().iterator();
                JSONArray fieldsArr = new JSONArray();
                while (it.hasNext()) {
                    FormField ff = (FormField) it.next();
                    JSONObject field = new JSONObject();// json field
                    if (ff.isMobileDisplay() && ff.getHide() == FormField.HIDE_NONE) {
                        field.put("title", ff.getTitle());// 标题
                        field.put("isCanNull", ff.isCanNull());// 标题
                        field.put("code", ff.getName());// 名称
                        field.put("desc", ff.getDefaultValueRaw());
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
                        if (type.equals("macro")) {
                            String macroCode = ff.getMacroType();
                            field.put("macroCode", macroCode);// 宏控件的code
                            macroCtrlUnit = macroCtrlMgr
                                    .getMacroCtlUnit(macroCode);

                            metaData = macroCtrlUnit.getIFormMacroCtl().getMetaData(ff);

                            String macroType = macroCtrlUnit.getIFormMacroCtl()
                                    .getControlType();
                            field.put("macroType", macroType);// 宏控件类型
                            String controlText = StrUtil
                                    .getNullStr(macroCtrlUnit
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
                            if (macroType.equals("select")
                                    || macroType.equals("buttonSelect")) {
                                // 一般options只有在拉框中显示
                                String options = StrUtil
                                        .getNullStr(macroCtrlUnit
                                                .getIFormMacroCtl()
                                                .getControlOptions(
                                                        privilege
                                                                .getUserName(skey),
                                                        ff));
                                if (options != null && !options.trim().equals("")) {
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
                            if (value.equals("")) {
                                value = ff.getDefaultValue();
                            }
                            if (type.equals("DATE") || type.equals("DATE_TIME")) {
                                if (id != 0) {
                                    field.put("value", ff.getValue());
                                } else {
                                    field.put("value", ff.getDefaultValueRaw());
                                }

                            } else if (type.equals("select")) {// 解析普通控件中
                                // select控件
                                String[][] optionsArray = FormParser
                                        .getOptionsArrayOfSelect(formDb, ff);
                                if (optionsArray != null
                                        && optionsArray.length > 0) {
                                    JSONArray options = new JSONArray();
                                    for (String[] option : optionsArray) {
                                        JSONObject optionObj = new JSONObject();
                                        if (value.equals(option[0])) {
                                            field.put("text", option[1]);
                                            field.put("value", value);
                                        }
                                        optionObj.put("value", option[0]);
                                        optionObj.put("name", option[1]);
                                        options.put(optionObj);
                                    }
                                    field.put("options", options);
                                }
                            } else {
                                field.put("value", value);
                            }
                        }

                        // 可传SQL控件条件中的字段
                        field.put("metaData", metaData);
                    }

                    field.put("isEditable", ff.isEditable());

                    fieldsArr.put(field);
                }
                json.put("fields", fieldsArr);
                json.put("formCode", formCode);
                json.put("res", "0");
                json.put("msg", "操作成功");

                if (flowId != -1) {
                    FormDb flowFd = new FormDb();
                    flowFd = flowFd.getFormDb(parentFormCode);
                    com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
                    fdaoFlow = fdaoFlow.getFormDAO(flowId, flowFd);
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
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return json.toString();
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
                e.printStackTrace();
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
            String cwsId = "";
            boolean isNestSheet = true;
            try {
                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAO(id, fdRelated);
                cwsId = fdao.getCwsId();

                re = fdm.del(request, isNestSheet, formCodeRelated);
            } catch (ErrMsgException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
}
