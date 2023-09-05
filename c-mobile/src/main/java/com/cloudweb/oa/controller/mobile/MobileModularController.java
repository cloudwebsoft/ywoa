package com.cloudweb.oa.controller.mobile;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.ICondUtil;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.INestSheetCtl;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.service.MobileModularService;
import com.cloudweb.oa.utils.ColorUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.Attachment;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.func.FuncMgr;
import com.redmoon.oa.visual.func.FuncUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/modular")
public class MobileModularController {
    public static final int RETURNCODE_SUCCESS = 0;       //请求成功
    public static final int RETURNCODE_FAIL = -1;       //请求失败
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
    ColorUtil colorUtil;

    @Autowired
    MobileModularService mobileModularService;

    @Autowired
    ICondUtil condUtil;

    @ResponseBody
    @RequestMapping(value = "/visit", produces = {"application/json;charset=UTF-8;"})
    public String visit(String moduleCode,
                       @RequestParam(required = true)Long id) throws JSONException {
        JSONObject jReturn = new JSONObject();

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);
        if (msd == null) {
            jReturn.put(RES, RESULT_MODULE_ERROR); //请求成功
            jReturn.put("msg", "模块不存在");
            return jReturn.toString();
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(msd.getString("form_code"));
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(id, fd);
        if (!fdao.isLoaded()) {
            jReturn.put(RES, RETURNCODE_FAIL);
            jReturn.put("msg", "记录：" + id + " 不存在");
        }
        else {
            fdao.setCwsVisited(true);
            boolean re = false;
            try {
                re = fdao.save();

                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
                com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
                fdm.runScriptOnSee(request, privilege, msd, fdao);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            if (re) {
                jReturn.put(RES, RETURNCODE_SUCCESS);
                jReturn.put("msg", "操作成功");
            }
        }
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/list", produces = {"application/json;charset=UTF-8;"})
    public String list(String moduleCode,
                       String op,
                       String orderBy,
                       String sort,
                       @RequestParam(defaultValue = "1")Integer pageNum,
                       @RequestParam(defaultValue = "20")Integer pageSize) throws JSONException {
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

        String userName = SpringUtil.getUserName();

        if ("".equals(orderBy)) {

            String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
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

            jReturn.put(RES, RETURNCODE_SUCCESS); // 请求成功
            jReturn.put("moduleName", msd.getString("name"));

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
                        jResult.put(RETURNCODE, RESULT_MODULE_ERROR);//模块不存在
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
                    DebugUtil.i(getClass(), "sql", sql);

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

                    // 页面配置
                    com.alibaba.fastjson.JSONArray pageSetup = new com.alibaba.fastjson.JSONArray();

                    String pageMobileList = StrUtil.getNullStr(msd.getString("page_mobile_list"));
                    com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(pageMobileList);
                    if (arr != null) {
                        int rowNum = 0;
                        com.alibaba.fastjson.JSONObject jsonRow = new com.alibaba.fastjson.JSONObject();
                        jsonRow.put("rowNum", rowNum);
                        com.alibaba.fastjson.JSONArray blocks = new com.alibaba.fastjson.JSONArray();
                        jsonRow.put("blocks", blocks);
                        pageSetup.add(jsonRow);

                        for (int i = 0; i < arr.size(); i++) {
                            com.alibaba.fastjson.JSONObject json = arr.getJSONObject(i);
                            int x = json.getIntValue("data_x");
                            int y = json.getIntValue("data_y");
                            if (y != rowNum) {
                                rowNum = y;
                                jsonRow = new com.alibaba.fastjson.JSONObject();
                                jsonRow.put("rowNum", rowNum);
                                blocks = new com.alibaba.fastjson.JSONArray();
                                jsonRow.put("blocks", blocks);
                                pageSetup.add(jsonRow);
                            }

                            com.alibaba.fastjson.JSONObject block = new com.alibaba.fastjson.JSONObject();
                            if (json.containsKey("blockId")) {
                                block.put("blockId", json.getString("blockId"));
                            }
                            block.put("bgImgUrl", json.getString("bgImgUrl"));
                            block.put("x", x);
                            block.put("width", json.getIntValue("data_width"));
                            block.put("fieldName", json.getString("fieldName"));
                            block.put("type", json.getString("type"));
                            block.put("imgUrl", json.getString("imgUrl"));
                            block.put("fontIcon", json.getString("fontIcon"));
                            block.put("label", json.getString("label"));
                            block.put("fontSize", json.getString("fontSize"));
                            if (json.containsKey("align")) {
                                block.put("align", json.getString("align"));
                            }
                            else {
                                block.put("align", "left");
                            }
                            block.put("isBold", json.getString("isBold"));

                            com.alibaba.fastjson.JSONObject jsonFontColor = com.alibaba.fastjson.JSONObject.parseObject(json.getString("fontColor"));
                            if (jsonFontColor != null) {
                                int r = jsonFontColor.getIntValue("r");
                                int g = jsonFontColor.getIntValue("g");
                                int b = jsonFontColor.getIntValue("b");
                                block.put("fontColor", colorUtil.convertRGBToHex(r, g, b));
                            } else {
                                block.put("fontColor", "");
                            }
                            com.alibaba.fastjson.JSONObject jsonBgColor = com.alibaba.fastjson.JSONObject.parseObject(json.getString("bgColor"));
                            if (jsonBgColor != null) {
                                int r = jsonBgColor.getIntValue("r");
                                int g = jsonBgColor.getIntValue("g");
                                int b = jsonBgColor.getIntValue("b");
                                block.put("bgColor", colorUtil.convertRGBToHex(r, g, b));
                            } else {
                                block.put("bgColor", "");
                            }
                            block.put("paddingTop", json.getString("paddingTop"));
                            block.put("paddingBottom", json.getString("paddingBottom"));
                            block.put("paddingLeft", json.getString("paddingLeft"));
                            block.put("paddingRight", json.getString("paddingRight"));
                            blocks.add(block);
                        }
                    }

                    jResult.put("pageSetup", pageSetup);

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

                            boolean isVisitedInCol = false;
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
                                    Object[] aryTitle = condUtil.getFieldTitle(fd, fieldName, "");
                                    String title = (String)aryTitle[0];
                                    isVisitedInCol = (Boolean)aryTitle[2];

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
                                            if (!fdao.getCwsId().equals(FormDAO.CWS_ID_NONE)) {
                                                FormDb subfd = new FormDb();
                                                subfd = subfd.getFormDb(subFields[1]);
                                                FormDAO subfdao = new FormDAO(subfd);
                                                FormField subff = subfd.getFormField(subFields[2]);
                                                String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
                                                JdbcTemplate jt = new JdbcTemplate();
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    ResultIterator ri = jt.executeQuery(subsql);
                                                    while (ri.hasNext()) {
                                                        ResultRecord rr = ri.next();
                                                        int subid = rr.getInt(1);
                                                        subfdao = new FormDAO(subid, subfd);
                                                        String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                                        if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                            mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                            if (mu != null) {
                                                                subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                                            }
                                                        }
                                                        sb.append(subFieldValue).append(ri.hasNext() ? "</br>" : "");
                                                    }
                                                } catch (Exception e) {
                                                    LogUtil.getLog(getClass()).error(e);
                                                }
                                                controlText = sb.toString();
                                            }
                                            else {
                                                DebugUtil.e(getClass(), "list", fieldName + " 关联字段cws_id为空");
                                                controlText = fieldName + " 关联字段cws_id为空";
                                            }
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
                                    }
                                    else if (fieldName.equals("cws_visited")) {
                                        controlText = fdao.isCwsVisited()?"已读":"未读";
                                        formFieldObj.put("value", fdao.isCwsVisited());
                                    }
                                    else {
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
                                                        // controlText = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                                        controlText = mu.getIFormMacroCtl().getControlText(userName, ff);
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

                                // 如果是否已读不在模块设置的列中，则加入
                                if (!isVisitedInCol) {
                                    JSONObject formFieldObj = new JSONObject();
                                    formFieldObj.put("name", "cws_visited");
                                    formFieldObj.put("title", "是否已读");
                                    formFieldObj.put("text", fdao.isCwsVisited()?"是":"否");
                                    fieldArr.put(formFieldObj);
                                }

                                // 加入cws_id，以便于二次开发
                                JSONObject fieldJson = new JSONObject();
                                fieldJson.put("name", "cws_id");
                                fieldJson.put("title", "cws_id");
                                fieldJson.put("value", fdao.getCwsId());
                                fieldJson.put("text", fdao.getCwsId());
                                fieldArr.put(fieldJson);

                                // 解析获取pageSetup中设置的comb组合型字段
                                for (Object jsonObject : pageSetup) {
                                    com.alibaba.fastjson.JSONObject row = (com.alibaba.fastjson.JSONObject)jsonObject;
                                    com.alibaba.fastjson.JSONArray blockArr = row.getJSONArray("blocks");
                                    for (Object blockObj : blockArr) {
                                        com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject)blockObj;
                                        if ("comb".equals(json.getString("type"))) {
                                            String blockId = json.getString("blockId");
                                            JSONObject formFieldObj = new JSONObject(); //Form表单对象
                                            formFieldObj.put("name", blockId);
                                            formFieldObj.put("type", "comb");
                                            formFieldObj.put("text", parseBlockComb(fdao, json.getString("label")));
                                            fieldArr.put(formFieldObj);
                                        }
                                    }
                                }

                                dataArr.put(rowObj);//组装json数组
                                i++;
                            }
                            jResult.put(DATAS, dataArr);
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

    public String parseBlockComb(FormDAO fdao, String label) {
        label = label.trim();
        // 如果是函数
        if (label.startsWith("$") && label.endsWith(")")) {
            String funcStr = label;
            // 调用函数
            boolean isMatched = false;
            do {
                isMatched = false;
                // 找到原子算式，因为算式有可能是嵌套的
                Pattern p = Pattern.compile(
                        "\\$([A-Z0-9a-z-_]+)\\(([^\\$]*?)\\)",
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(funcStr);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    isMatched = true;

                    String[] myfunc = new String[2];
                    myfunc[0] = m.group(1); // 方法名
                    myfunc[1] = m.group(2); // 参数

                    String val = "1";
                    FuncMgr fm = new FuncMgr();
                    FuncUnit fu = fm.getFuncUnit(myfunc[0]);
                    if (fu!=null) {
                        IFuncImpl ifil = fu.getIFuncImpl();
                        try {
                            val = StrUtil.getNullStr(ifil.func(fdao, myfunc));
                        }
                        catch (ErrMsgException e) {
                            LogUtil.getLog(getClass()).error(e);
                            return m.group() + " " + e.getMessage();
                        }
                    }
                    else {
                        LogUtil.getLog(FuncUtil.class).error(myfunc[0] + "不存在！");
                        return myfunc[0] + "不存在！";
                    }

                    m.appendReplacement(sb, val);
                }
                m.appendTail(sb);

                funcStr = sb.toString();
            }
            while (isMatched);

            return funcStr;
        }
        else {
            Pattern p = Pattern.compile(
                    "\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(label);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String fieldName = m.group(1);
                m.appendReplacement(sb, StrUtil.getNullStr(fdao.getFieldValue(fieldName)));
            }
            m.appendTail(sb);

            return sb.toString();
        }
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
    @RequestMapping(value = "/add", produces = {"application/json;charset=UTF-8;"})
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
                    String label = "";
                    if (ff.getType().equals("checkbox")) {
                        label = ff.getTitle();
                    }
                    field.put("label", label);
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
    @RequestMapping(value = "/del", produces = {"application/json;charset=UTF-8;"})
    public String del(@RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
        JSONObject json = new JSONObject();
        try {
            FormDb fd = new FormDb();
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String formCode = msd.getString("form_code");
            fd = fd.getFormDb(formCode);
            FormDAO fdao = new FormDAO(fd);
            fdao = fdao.getFormDAO(id, fd);
            boolean re = fdao.del();
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
    @RequestMapping(value = "/edit", produces = {"application/json;charset=UTF-8;"})
    public String edit(@RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
        JSONObject json = new JSONObject();
        if (moduleCode != null && !"".equals(moduleCode.trim())) {
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
                com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                String userName = pvg.getUser(request);
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
                                .getControlText(userName, ff));
                        val = StrUtil.getNullStr(mu.getIFormMacroCtl()
                                .getControlValue(userName, ff));
                        options = mu.getIFormMacroCtl().getControlOptions(
                                userName, ff);
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
                    String label = "";
                    if (ff.getType().equals("checkbox")) {
                        // label = "个人兴趣";
                        label = ff.getTitle();
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
                    field.put("label", label);
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

                // 是否允许上传附件
                json.put("hasAttach", fd.isHasAttachment());
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/show", produces = {"application/json;charset=UTF-8;"})
    public String show(@RequestParam(required = true) String moduleCode, @RequestParam(required = true) long id) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String userName = privilege.getUser(request);

        MacroCtlUnit mu;
        MacroCtlMgr mm = new MacroCtlMgr();
        try {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            if (msd == null) {
                json.put("res", "0");
                json.put("msg", "模块" + moduleCode + "不存在");
                return json.toString();
            }

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

                // String val = StrUtil.getNullStr(ff.getValue());
                String val = ff.convertToHtml();

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
                String label = "";
                if (ff.getType().equals("checkbox")) {
                    label = ff.getTitle();
                }
                field.put("label", label);
                field.put("macroCode", macroCode);
                fields.put(field);
            }

            // 加入cws_id，以便于二次开发
            JSONObject field = new JSONObject();
            field.put("code", "cws_id");
            field.put("value", fdao.getCwsId());
            field.put("type", FormField.TYPE_TEXTFIELD);
            field.put("label", "");
            field.put("macroCode", "");
            fields.put(field);

            json.put("fields", fields);

            int flowId = fdao.getFlowId();
            json.put("flowId", flowId);
            if (flowId == -1) {
                Iterator itFiles = fdao.getAttachments().iterator();
                JSONArray filesArr = new JSONArray();
                while (itFiles.hasNext()) {
                    com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles.next();
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
            }
            else {
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
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
                                    file.put("preview", "/public/flow_att_preview.jsp?attachId=" + am.getId());
                                }
                            } else {
                                file.put("preview", "/public/flow_att_preview.jsp?attachId=" + am.getId());
                            }
                        }

                        file.put("name", am.getName());
                        // downPath = "public/flow_getfile.jsp?"+"flowId="+flowId+"&attachId="+am.getId();
                        downPath = "/public/android/getFile.do?" + "flowId=" + flowId + "&attachId=" + am.getId();
                        file.put("url", downPath);
                        file.put("id", am.getId());
                        file.put("size", String.valueOf(am.getSize()));
                        files.put(file);
                    }
                    json.put("files", files);
                }
            }

            // 关联模块
            FormDb fdRelated = new FormDb();
            JSONArray arrRelated = new JSONArray();
            ModuleRelateDb mrdTop = new ModuleRelateDb();
            Iterator irTop = mrdTop.getModulesRelated(formCode).iterator();
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

            // 按钮
            com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_SHOW, fdao, 0);
            JSONArray ary = new JSONArray(buttons.toString());
            json.put("buttons", ary);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/attDel", produces = {"application/json;charset=UTF-8;"})
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
    @RequestMapping(value = "/listRelate", produces = {"application/json;charset=UTF-8;"})
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
                                LogUtil.getLog(getClass()).error(tagName + " 不存在！");
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
                        Iterator irTop = mrdTop.getModulesRelated(formCode).iterator();
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
                                                FormDAO subfdao = new FormDAO(subfd);
                                                FormField subff = subfd.getFormField(subFields[2]);
                                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id='" + id + "' order by cws_order";
                                                JdbcTemplate jt = new JdbcTemplate();
                                                StringBuilder sb = new StringBuilder();
                                                try {
                                                    ResultIterator ri = jt.executeQuery(subsql);
                                                    while (ri.hasNext()) {
                                                        ResultRecord rr = ri.next();
                                                        int subid = rr.getInt(1);
                                                        subfdao = new FormDAO(subid, subfd);
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
    @RequestMapping(value = "/addRelate", produces = {"application/json;charset=UTF-8;"})
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
                    String label = "";
                    if (ff.getType().equals("checkbox")) {
                        // label = "个人兴趣";
                        label = ff.getTitle();
                    }
                    field.put("label", label);
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
    @RequestMapping(value = "/editRelate", produces = {"application/json;charset=UTF-8;"})
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
                            com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
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
                    String label = "";
                    if (ff.getType().equals("checkbox")) {
                        label = ff.getTitle();
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
                    field.put("label", label);
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
                    com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles.next();
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
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/initNestSheet", produces = {"application/json;charset=UTF-8;"})
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
                            } else if ("radio".equals(ff.getType())) {
                                JSONArray options = new JSONArray();
                                String[] optionsArray = FormParser.getValuesOfInput(formDao.getFormDb(), ff);
                                for (int i = 0; i < optionsArray.length; i++) {
                                    JSONObject option = new JSONObject();
                                    option.put("value", optionsArray[i]);
                                    option.put("name", optionsArray[i]);
                                    options.put(option);
                                }
                                field.put("options", options);
                                field.put("value", value);
                            } else {
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
            LogUtil.getLog(getClass()).error(e);
            log.error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delNestSheet", produces = {"application/json;charset=UTF-8;"})
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
                fdao = fdao.getFormDAO(id, fdRelated);
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

    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String update(HttpServletRequest request) throws ErrMsgException {
        return mobileModularService.update(request);
    }

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String create(HttpServletRequest request) throws ErrMsgException {
        return mobileModularService.create(request);
    }
}
