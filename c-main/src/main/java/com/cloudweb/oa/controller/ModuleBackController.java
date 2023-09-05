package com.cloudweb.oa.controller;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.service.ModuleBackService;
import com.cloudweb.oa.service.ModuleLogService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.dept.DeptChildrenCache;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.AttachmentLogDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.*;
import jxl.write.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Boolean;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/visual")
public class ModuleBackController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ModuleBackService moduleBackService;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    IFileService fileService;

    @Autowired
    AuthUtil authUtil;

    @ResponseBody
    @RequestMapping(value = "/moduleSetUnitCode", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String setUnitCode() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "模块：" + code + "不存在！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserManage(privilege.getUser(request))) {
            try {
                json.put("ret", 0);
                json.put("msg", cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String strIds = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strIds, ",");
        if (ids == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "请选择记录！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        String toUnitCode = ParamUtil.get(request, "toUnitCode");
        FormDAO fdao = new FormDAO();
        try {
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                int id = StrUtil.toInt(ids[i]);
                fdao = fdao.getFormDAO(id, fd);
                fdao.setUnitCode(toUnitCode);
                fdao.save();
            }
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } catch (ErrMsgException e) {
            try {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleBatchOp", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleBatchOp() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "模块：" + code + "不存在！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            re = fdm.batchOperate(request);
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
            return json.toString();
        }

        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleDel", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String moduleDel() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "模块：" + code + "不存在！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            //re = fdm.del(request);
            re = fdm.del(request, false, code);
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
            return json.toString();
        }

        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleDelRelate", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String moduleDelRelate() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "模块：" + code + "不存在！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            re = fdm.delRelate(request, msd);
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
            return json.toString();
        }

        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleList", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String moduleList() throws ErrMsgException {
        // String op = ParamUtil.get(request, "op");
        String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位

        String code = ParamUtil.get(request, "code");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "moduleCode");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            LogUtil.getLog(getClass()).error("模块：" + code + "不存在！");
            return "";
        }

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        long tPriv = System.currentTimeMillis();

        ModulePrivDb mpd = new ModulePrivDb(code);
        boolean canView = mpd.canUserView(userName);

        DebugUtil.i(getClass(), "list tPriv", String.valueOf((double)(System.currentTimeMillis() - tPriv)/1000));

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        String[] fieldsLink = msd.getColAry(false, "list_field_link");

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");

        if ("".equals(orderBy)) {
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

        // 用于传过滤条件
        request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);

        // ---------------module_list_sel.jsp 开始----------------------------
        boolean isModuleListSel = ParamUtil.getBoolean(request, "isModuleListSel", false);
        com.alibaba.fastjson.JSONObject moduleFieldSelectCtlDesc = null;
        FormField openerField = null;
        String showFieldName = "", openerFormCode = "", openerFieldName = "";
        String showValue = "";
        if (isModuleListSel) {
            FormDb openerFd = null;
            openerFormCode = ParamUtil.get(request, "openerFormCode");
            if ("".equals(openerFormCode)) {
                throw new ErrMsgException("openerFormCode不能为空");
            }
            openerFieldName = ParamUtil.get(request, "openerFieldName");
            openerFd = new FormDb();
            openerFd = openerFd.getFormDb(openerFormCode);
            if (!openerFd.isLoaded()) {
                throw new ErrMsgException("表单：" + openerFormCode + " 不存在");
            }
            openerField = openerFd.getFormField(openerFieldName);
            if (openerField == null) {
                throw new ErrMsgException("字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在");
            }
            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
            IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
            String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
            moduleFieldSelectCtlDesc = com.alibaba.fastjson.JSONObject.parseObject(desc);
            // String byFieldName = moduleFieldSelectCtlDesc.getString("idField");
            showFieldName = moduleFieldSelectCtlDesc.getString("showField");

            boolean isUseModuleFilter = true;
            if (moduleFieldSelectCtlDesc.containsKey("isUseModuleFilter")) {
                isUseModuleFilter = moduleFieldSelectCtlDesc.getBoolean("isUseModuleFilter");
            }
            String filter = com.redmoon.oa.visual.ModuleBackUtil.decodeFilter(moduleFieldSelectCtlDesc.getString("filter"));
            if ("none".equals(filter)) {
                filter = "";
            }
            request.setAttribute(ModuleBackUtil.NEST_SHEET_FILTER, filter);
            request.setAttribute(ModuleBackUtil.NEST_SHEET_FILTER_USE_MODULE, isUseModuleFilter);
        }
        // --------------------module_list_sel.jsp 结束-------------------------

        double t = System.currentTimeMillis();

        // --------------------module_list_sel_nest.jsp 开始-------------------------
        boolean isModuleListNestSel = ParamUtil.getBoolean(request, "isModuleListNestSel", false);
        if (isModuleListNestSel) {
            String nestFormCode = ParamUtil.get(request, "nestFormCode");
            String nestType = ParamUtil.get(request, "nestType");
            String parentFormCode = ParamUtil.get(request, "parentFormCode");
            String nestFieldName = ParamUtil.get(request, "nestFieldName");
			/*long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
			int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;*/

            FormDb pForm = new FormDb();
            pForm = pForm.getFormDb(parentFormCode);
            FormField nestField = pForm.getFormField(nestFieldName);

            com.alibaba.fastjson.JSONObject nestCtlDesc;
            String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
            nestCtlDesc = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);
            String sourceForm = nestCtlDesc.getString("sourceForm");

            // @task:需优化，之前已取msd，这儿又重复取
            msd = msd.getModuleSetupDb(sourceForm);
            fields = msd.getColAry(false, "list_field");
            fieldsLink = msd.getColAry(false, "list_field_link");

            nestFormCode = nestCtlDesc.getString("destForm");
            String filter = nestCtlDesc.getString("filter");
            request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);
            request.setAttribute(ModuleBackUtil.NEST_SHEET_FILTER, filter);
        }

        String[] ary = null;
        try {
            ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        } catch (ErrMsgException e) {
            DebugUtil.e(getClass(), "moduleList", "SQL：" + e.getMessage());
            return "";
        }

        String sql = ary[0];
        // 如果是日志表，则与需模块相关联，并能支持副模块
        if ("module_log".equals(formCode)) {
            sql = SQLBuilder.getListSqlForLogRelateModuleBack(request, sql);
        }
        DebugUtil.log(getClass(), "moduleList", "sql=" + sql);

        JSONObject jobject = new JSONObject();

        FormDAO fdao = new FormDAO();

        int pagesize = ParamUtil.getInt(request, "limit", -1);
        if (pagesize == -1) {
            pagesize = ParamUtil.getInt(request, "rp", 20);
        }
        int curpage = ParamUtil.getInt(request, "page", 1);

        ListResult lr = null;
        try {
            lr = fdao.listResult(formCode, sql, curpage, pagesize);
        } catch (ErrMsgException e1) {
            e1.printStackTrace();
            try {
                jobject.put("errCode", 0);
                jobject.put("page", 1);
                jobject.put("total", 0);
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }

            return jobject.toString();
        }

        DebugUtil.i(getClass(), "list listResult time", "" + (System.currentTimeMillis() - t)/1000);

        String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
        // String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
        String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!"".equals(promptField) && !"".equals(promptIcon)) {
            isPrompt = true;
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        UserMgr um = new UserMgr();
        WorkflowDb wf = new WorkflowDb();
        JSONArray rows = new JSONArray();
        try {
            jobject.put("errCode", 0); // 用于layui table
            jobject.put("rows", rows);
            jobject.put("page", curpage);
            jobject.put("total", lr.getTotal());

            int len = fields.length;

            int k = 0;
            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                fdao = (FormDAO) ir.next();

                RequestUtil.setFormDAO(request, fdao);

                k++;
                boolean isShowFieldFound = false;
                JSONObject jo = new JSONObject();

                // prompt 图标
                if (isPrompt) {
                    // 判断条件
                    if (ModuleBackUtil.isPrompt(request, msd, fdao)) {
                        jo.put("colPrompt", "<img src=\"" + request.getContextPath() + "/images/prompt/" + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />");
                    }
                }

                long id = fdao.getId();
                // id小写兼容flexigrid
                jo.put("id", String.valueOf(id));
                // layui table需对应大写ID
                jo.put("ID", String.valueOf(id));

/*				if (isPrompt) {
					// 判断条件
					if (ModuleBackUtil.isPrompt(request, msd, fdao)) {
					}
				}*/

                long tRow = System.currentTimeMillis();
                // DebugUtil.i(getClass(), ">>>>> list row start id", String.valueOf(id));

                for (int i = 0; i < len; i++) {
                    String fieldName = fields[i];

                    long tField = System.currentTimeMillis();

                    String val = ""; // fdao.getFieldValue(fieldName);

                    if (fieldName.startsWith("main:")) {
                        String[] subFields = fieldName.split(":");
                        if (subFields.length == 3) {
                            // 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
                            FormDb subfd = new FormDb();
                            subfd = subfd.getFormDb(subFields[1]);
                            com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                            FormField subff = subfd.getFormField(subFields[2]);
                            String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
                            StringBuilder sb = new StringBuilder();
                            try {
                                JdbcTemplate jt = new JdbcTemplate();
                                ResultIterator ri = jt.executeQuery(subsql);
                                while (ri.hasNext()) {
                                    ResultRecord rr = (ResultRecord) ri.next();
                                    int subid = rr.getInt(1);
                                    subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                    String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                    if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                        MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
                                        if (mu != null) {
                                            subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                        }
                                    }
                                    sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                                }
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                            val += sb.toString();
                        }
                    } else if (fieldName.startsWith("other:")) {
                        // 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
                        String fName = fieldName;
                        int logType = ParamUtil.getInt(request, "log_type", 0);
                        if (logType == FormDAOLog.LOG_TYPE_DEL) {
                            if ("module_log".equals(formCode)) {
                                if (fName.contains("module_id:")) {
                                    int p = fName.indexOf(":");
                                    p = fName.indexOf(":", p + 1);
                                    String prefix = fName.substring(0, p);
                                    fName = fName.substring(p + 1);
                                    p = fName.indexOf(":");
                                    String endStr = fName.substring(p);
                                    if (endStr.startsWith(":id:")) {
                                        // 将id替换为***_log表中的cws_log_id
                                        endStr = ":cws_log_id" + endStr.substring(3);
                                    }
                                    fName = fName.substring(0, p);
                                    fName += "_log";
                                    fName = prefix + ":" + fName + endStr;
                                }
                            }
                        }
                        val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
                    } else if ("ID".equals(fieldName)) {
                        fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
                        val = String.valueOf(fdao.getId());
                    } else if ("cws_progress".equals(fieldName)) {
                        val = String.valueOf(fdao.getCwsProgress());
                    } else if ("cws_flag".equals(fieldName)) {
                        val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                    } else if ("cws_creator".equals(fieldName)) {
                        String realName = "";
                        if (fdao.getCreator() != null) {
                            UserDb user = um.getUserDb(fdao.getCreator());
                            if (user != null) {
                                realName = user.getRealName();
                            }
                        }
                        val = realName;
                    } else if ("flowId".equals(fieldName)) {
                        val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
                    } else if ("cws_status".equals(fieldName)) {
                        val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                    } else if ("cws_create_date".equals(fieldName)) {
                        val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
                    } else if ("flow_begin_date".equals(fieldName)) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                        }
                    } else if ("flow_end_date".equals(fieldName)) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                        }
                    } else if ("cws_cur_handler".equals(fieldName)) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            MyActionDb mad = new MyActionDb();
                            for (MyActionDb madCur : mad.getMyActionDbDoingOfFlow(wf.getId())) {
                                if (!"".equals(val)) {
                                    val += "、";
                                }
                                val += um.getUserDb(madCur.getUserName()).getRealName();
                            }
                        }
                    } else if ("cws_id".equals(fieldName)) {
                        val = fdao.getCwsId();
                    }
                    else if ("cws_visited".equals(fieldName)) {
                        val = fdao.isCwsVisited() ? "是" : "否";
                    }
                    else {
                        FormField ff = fdao.getFormField(fieldName);
                        if (ff == null) {
                            val += "不存在！";
                        } else {
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                }
                            } else {
                                val = FuncUtil.renderFieldValue(fdao, ff);
                            }
                            if (!isShowFieldFound && ff.getName().equals(showFieldName)) {
                                isShowFieldFound = true;
                                showValue = val;
                            }
                        }
                    }

                    DebugUtil.i(getClass(), "list field end", fieldName + " " + (double)(System.currentTimeMillis() - tField)/1000);

                    if (!"#".equals(fieldsLink[i]) && !"&".equals(fieldsLink[i]) && !fieldsLink[i].startsWith("$")) {
                        String link = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);
                        if (!link.startsWith("http")) {
                            link = request.getContextPath() + "/" + link;
                        }
                        val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + link + "')\">" + val + "</a>";
                    } else if (((i == 0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
                        // 在第一列或者fieldsLink[i]为&的列上，生成查看链接
                        if (msd.getInt("btn_display_show") == 1) {
                            String visitKey = SecurityUtil.makeVisitKey(id);
                            if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                                String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                                if (urlShow.contains("?")) {
                                    val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "&parentId=" + id + "&id=" + id + "&code=" + code + "&visitKey=" + visitKey + "')\">" + val + "</a>";
                                }
                                else {
                                    val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "?parentId=" + id + "&id=" + id + "&code=" + code + "&visitKey=" + visitKey + "')\">" + val + "</a>";
                                }

                            } else {
                                val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + id + "&id=" + id + "&code=" + code + "&visitKey=" + visitKey + "')\">" + val + "</a>";
                            }
                        }
                    }

                    jo.put(fieldName, val);
                }

                long tBtn = System.currentTimeMillis();

                if (isModuleListSel) {
                    jo.put("colOperate", moduleBackService.getColOperateForListSel(request, moduleFieldSelectCtlDesc, k, openerFormCode, openerFieldName, showValue, isShowFieldFound, fdao));
                }
                else {
                    jo.put("colOperate", moduleBackService.getColOperate(request, userName, msd, fd, fdao, mpd, canView));
                }
                DebugUtil.i(getClass(), "list operateBtn", id + " " + (double)(System.currentTimeMillis() - tBtn)/1000);

                rows.put(jo);

                DebugUtil.i(getClass(), "-----list row end id", id + " " + (double)(System.currentTimeMillis() - tRow)/1000);
            }

            if (rows.length() > 0) {
                String propStat = msd.getString("prop_stat");
                if (StringUtils.isNotEmpty(propStat)) {
                    if ("".equals(propStat)) {
                        propStat = "{}";
                    }
                    JSONObject json = new JSONObject(propStat);
                    JSONObject jo = new JSONObject();
                    Iterator ir3 = json.keys();
                    int n = 0;
                    while (ir3.hasNext()) {
                        String fieldName = (String) ir3.next();
                        String modeStat = json.getString(fieldName);

                        FormField ff = fd.getFormField(fieldName);
                        if (ff == null) {
                            DebugUtil.e(getClass(), "moduleList", "field:" + fieldName + " is not exist");
                        }
                        int fieldType = ff.getFieldType();

                        double sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
                        if ("0".equals(modeStat)) {
                            if (fieldType == FormField.FIELD_TYPE_INT
                                    || fieldType == FormField.FIELD_TYPE_LONG) {
                                jo.put(fieldName, "合计：" + (long) sumVal);
                            } else {
                                jo.put(fieldName, "合计：" + NumberUtil.round(sumVal, 2));
                            }
                        } else if ("1".equals(modeStat)) {
                            jo.put(fieldName, "平均：" + NumberUtil.round(sumVal / lr.getTotal(), 2));
                        }
                        n++;
                    }
                    if (n > 0) {
                        jo.put("id", String.valueOf(ConstUtil.MODULE_ID_STAT));
                        rows.put(jo);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        DebugUtil.i(getClass(), "list time", "" + (double)(System.currentTimeMillis() - t)/1000);
        // LogUtil.getLog(getClass()).info(getClass() + " " + jobject.toString());

        return jobject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleListRelate", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String moduleListRelate() {
        JSONObject jobject = new JSONObject();
        long parentId = ParamUtil.getLong(request, "parentId", -1);

        String formCode = ParamUtil.get(request, "formCode");
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        String menuItem = ParamUtil.get(request, "menuItem");
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
        formCode = parentMsd.getString("form_code");

        String mode = ParamUtil.get(request, "mode");
        String tagName = ParamUtil.get(request, "tagName");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            LogUtil.getLog(getClass()).error("模块：" + moduleCodeRelated + "不存在！");
            return "";
        }

        String formCodeRelated = msd.getString("form_code");
        boolean isEditInplace = msd.getInt("is_edit_inplace") == 1;

        // 通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);

        if (isSubTagRelated) {
            String tagUrl = ModuleBackUtil.getModuleSubTagUrl(moduleCode, tagName);
            try {
                JSONObject json = new JSONObject(tagUrl);
                if (json.has("viewList")) {
                    int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
                    if (viewList == ModuleSetupDb.VIEW_LIST_GANTT) {
                        LogUtil.getLog(getClass()).error("模块：" + moduleCodeRelated + "需用甘特图视图显示！");
                        jobject.put("page", 1);
                        jobject.put("total", 0);
                        jobject.put("msg", "模块：" + moduleCodeRelated + "需用甘特图视图显示！");
                        return jobject.toString();
                    }
                }
                if (!json.isNull("formRelated")) {
                    moduleCodeRelated = json.getString("formRelated");
                    msd = msd.getModuleSetupDb(moduleCodeRelated);
                    formCodeRelated = msd.getString("form_code");
                    isEditInplace = msd.getInt("is_edit_inplace") == 1;
                } else {
                    LogUtil.getLog(getClass()).error("关联模块：" + moduleCodeRelated + "选项卡关联配置不正确！");
                    jobject.put("page", 1);
                    jobject.put("total", 0);
                    jobject.put("msg", "选项卡关联配置不正确！");
                    return jobject.toString();
                }
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        String relateFieldValue = "";
        if (parentId == -1) {
            try {
                jobject.put("page", 1);
                jobject.put("total", 0);
                jobject.put("msg", "缺少父模块记录的ID！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return jobject.toString();
        } else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue == null) {
                    // 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                    // out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
                    // return;
                }
            }
        }

        // String op = ParamUtil.get(request, "op");
        String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        String[] fieldsLink = msd.getColAry(false, "list_field_link");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCodeRelated);

        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(orderBy)) {
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

        // 用于传过滤条件
        request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);
        String[] ary = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);

        String sql = ary[0];

        DebugUtil.log(getClass(), "moduleListRelate", "sql=" + sql);

        FormDAO fdao = new FormDAO();

        // layui 的默认值为10
        int pagesize = ParamUtil.getInt(request, "limit", -1);
        if (pagesize == -1) {
            pagesize = ParamUtil.getInt(request, "rp", 20);
        }
        int curpage = ParamUtil.getInt(request, "page", 1);

        ListResult lr = null;
        try {
            lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
        } catch (ErrMsgException e1) {
            LogUtil.getLog(getClass()).error(e1);
            try {
                jobject.put("errCode", 0);
                jobject.put("page", 1);
                jobject.put("total", 0);
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }

            return jobject.toString();
        }

        String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
        String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
        String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!promptField.equals("") && !promptIcon.equals("")) {
            isPrompt = true;
        }

        boolean canView = mpd.canUserView(userName);
        boolean canLog = mpd.canUserLog(userName);
        boolean canManage = mpd.canUserManage(userName);
        boolean canModify = mpd.canUserModify(userName);
        boolean canDel = mpd.canUserDel(userName);

        Config cfg = new Config();
        boolean isModuleHistory = cfg.getBooleanProperty("isModuleHistory");
        boolean isModuleLogRead = cfg.getBooleanProperty("isModuleLogRead");
        boolean isModuleLogModify = cfg.getBooleanProperty("isModuleLogModify");

        String strProps = msd.getString("props");
        boolean isColOperateTextShow = false;
        if (!StrUtil.isEmpty(strProps)) {
            com.alibaba.fastjson.JSONObject props = com.alibaba.fastjson.JSONObject.parseObject(strProps);
            if (props != null) {
                if (props.containsKey("isColOperateTextShow")) {
                    isColOperateTextShow = props.getBoolean("isColOperateTextShow");
                }
            }
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        UserMgr um = new UserMgr();
        WorkflowDb wf = new WorkflowDb();
        JSONArray rows = new JSONArray();
        try {
            jobject.put("errCode", 0);
            jobject.put("rows", rows);
            jobject.put("page", curpage);
            jobject.put("total", lr.getTotal());

            int len = fields.length;

            int k = 0;
            Iterator ir = lr.getResult().iterator();
            while (ir != null && ir.hasNext()) {
                fdao = (FormDAO) ir.next();

                RequestUtil.setFormDAO(request, fdao);

                k++;
                JSONObject jo = new JSONObject();

                // prompt 图标
                if (isPrompt) {
                    // 判断条件
                    if (ModuleBackUtil.isPrompt(request, msd, fdao)) {
                        jo.put("colPrompt", "<img src=\"" + SkinMgr.getSkinPath(request) + "/icons/prompt/" + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />");
                    }
                }

                long id = fdao.getId();
                // id小写兼容flexigrid
                jo.put("id", String.valueOf(id));
                // layui table需对应大写ID
                jo.put("ID", String.valueOf(id));

/*				if (isPrompt) {
					// 判断条件
					if (ModuleBackUtil.isPrompt(request, msd, fdao)) {
					}
				}*/

                for (int i = 0; i < len; i++) {
                    String fieldName = fields[i];

                    String val = ""; // fdao.getFieldValue(fieldName);

                    if (fieldName.startsWith("main:")) {
                        String[] subFields = fieldName.split(":");
                        if (subFields.length == 3) {
                            // 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
                            FormDb subfd = new FormDb(subFields[1]);
                            FormDAO subfdao = new FormDAO(subfd);
                            FormField subff = subfd.getFormField(subFields[2]);
                            String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
                            StringBuilder sb = new StringBuilder();
                            try {
                                JdbcTemplate jt = new JdbcTemplate();
                                ResultIterator ri = jt.executeQuery(subsql);
                                while (ri.hasNext()) {
                                    ResultRecord rr = (ResultRecord) ri.next();
                                    int subid = rr.getInt(1);
                                    subfdao = new FormDAO(subid, subfd);
                                    String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                    if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                        MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
                                        if (mu != null) {
                                            subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                        }
                                    }
                                    sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                                }
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                            val += sb.toString();
                        }
                    } else if (fieldName.startsWith("other:")) {
                        // 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
                        String fName = fieldName;
                        int logType = ParamUtil.getInt(request, "log_type", 0);
                        if (logType == FormDAOLog.LOG_TYPE_DEL) {
                            if (formCode.equals("module_log")) {
                                if (fName.indexOf("module_id:") != -1) {
                                    int p = fName.indexOf(":");
                                    p = fName.indexOf(":", p + 1);
                                    String prefix = fName.substring(0, p);
                                    fName = fName.substring(p + 1);
                                    p = fName.indexOf(":");
                                    String endStr = fName.substring(p);
                                    if (endStr.startsWith(":id:")) {
                                        // 将id替换为***_log表中的cws_log_id
                                        endStr = ":cws_log_id" + endStr.substring(3);
                                    }
                                    fName = fName.substring(0, p);
                                    fName += "_log";
                                    fName = prefix + ":" + fName + endStr;
                                }
                            }
                        }
                        val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
                    } else if (fieldName.equals("ID")) {
                        fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
                        val = String.valueOf(fdao.getId());
                    } else if (fieldName.equals("cws_progress")) {
                        val = String.valueOf(fdao.getCwsProgress());
                    } else if (fieldName.equals("cws_flag")) {
                        val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                    } else if (fieldName.equals("cws_creator")) {
                        String realName = "";
                        if (fdao.getCreator() != null) {
                            UserDb user = um.getUserDb(fdao.getCreator());
                            if (user != null) {
                                realName = user.getRealName();
                            }
                        }
                        val = realName;
                    } else if (fieldName.equals("flowId")) {
                        val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
                    } else if (fieldName.equals("cws_status")) {
                        val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                    } else if (fieldName.equals("cws_create_date")) {
                        val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
                    } else if (fieldName.equals("flow_begin_date")) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                        }
                    } else if (fieldName.equals("flow_end_date")) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                        }
                    } else if (fieldName.equals("cws_id")) {
                        val = fdao.getCwsId();
                    }
                    else if ("cws_visited".equals(fieldName)) {
                        val = fdao.isCwsVisited() ? "是" : "否";
                    }
                    else {
                        FormField ff = fdao.getFormField(fieldName);
                        if (ff == null) {
                            val += "不存在！";
                        } else {
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

                    // DebugUtil.i(getClass(), "moduleList", fieldName + " link=" + fieldsLink[i]);

                    if (!"#".equals(fieldsLink[i]) && !"&".equals(fieldsLink[i]) && !fieldsLink[i].startsWith("$")) {
                        String link = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);
                        if (!link.startsWith("http:")) {
                            link = request.getContextPath() + "/" + link;
                        }
                        val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + link + "')\">" + val + "</a>";
                    } else if (((i == 0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
                        // 在第一列或者fieldsLink[i]为&的列上，生成查看链接
                        if (msd.getInt("btn_display_show") == 1) {
                            if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                                String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                                if (urlShow.contains("?")) {
                                    val = "<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "')\">" + val + "</a>";
                                }
                                else {
                                    val = "<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "?mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "')\">" + val + "</a>";
                                }
                            } else {
                                val = "<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleShowRelatePage.do?menuItem=" + menuItem + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "')\">" + val + "</a>";
                            }
                        }
                    }

                    jo.put(fieldName, val);
                }

                StringBuilder sb = new StringBuilder();
                if (msd.getInt("btn_display_show") == 1 && canView) {
                    String showName = "<i class=\"fa fa-file-text-o link-icon link-icon-show\" />";
                    if (isColOperateTextShow) {
                        showName += "查看";
                    }
                    if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                        String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                        if (urlShow.contains("?")) {
                            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&code=" + moduleCodeRelated + "&menuItem=" + menuItem + "&isNav=0')\">" + showName + "</a>");
                        }
                        else {
                            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "?mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&code=" + moduleCodeRelated + "&menuItem=" + menuItem + "&isNav=0')\">" + showName + "</a>");
                        }
                    } else {
                        sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleShowRelatePage.do?menuItem=" + menuItem + "&mode=" + mode + "&parentId=" + parentId + "&id=" + id + "&code=" + moduleCode + "&moduleCodeRelated=" + moduleCodeRelated + "&menuItem=" + menuItem + "&isNav=0')\">" + showName + "</a>");
                    }
                }

                boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName);
                if (isBtnEditShow) {
                    String editName = "<i class=\"fa fa-edit link-icon link-icon-edit\" />";
                    if (isColOperateTextShow) {
                        editName += "修改";
                    }
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"修改\" onclick=\"edit(" + id + ")\">" + editName + "</a>");
                }

                boolean isBtnDelShow = msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage);
                if (isBtnDelShow) {
                    String delName = "<i class=\"fa fa-trash-o link-icon link-icon-del\" />";
                    if (isColOperateTextShow) {
                        delName += "删除";
                    }
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"修改\" onclick=\"del('" + id + "')\">" + delName + "</a>");
                }

                if (msd.getInt("btn_flow_show") == 1) {
                    if (fd.isFlow() && fdao.getFlowId() != -1) {
                        String btnFlowName = "<i class=\"fa fa-check-circle-o link-icon link-icon-flow\" />";
                        if (isColOperateTextShow) {
                            btnFlowName += i18nUtil.get("btn_flow");
                        }
                        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
                        String desKey = ssoCfg.get("key");
                        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
                        sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看流程\" onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "&visitKey=" + visitKey + "')\">" + btnFlowName + "</a>");
                    }
                }
                if (canLog || canManage) {
                    if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
                        String btnHisName = "<i class=\"fa fa-history link-icon link-icon-history\" />";
                        if (isColOperateTextShow) {
                            btnHisName += "历史";
                        }
                        if (isModuleHistory) {
                            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"历史记录\" onclick=\"addTab('历史记录', '" + request.getContextPath() + "/visual/module_his_list.jsp?op=search&code=" + moduleCodeRelated + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnHisName + "</a>");
                        }
                        String btnLogModifyName = "<i class=\"fa fa-pencil link-icon link-icon-log-edit\" />";
                        if (isColOperateTextShow) {
                            btnLogModifyName += "修改日志";
                        }
                        if (isModuleLogModify) {
                            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"修改日志\" onclick=\"addTab('修改日志', '" + request.getContextPath() + "/visual/module_log_list.jsp?op=search&code=" + moduleCodeRelated + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnLogModifyName + "</a>");
                        }
                        String btnLogReadName = "<i class=\"fa fa-tv link-icon link-icon-log-read\" />";
                        if (isColOperateTextShow) {
                            btnLogReadName += "浏览日志";
                        }
                        if (isModuleLogRead) {
                            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"浏览日志\" onclick=\"addTab('浏览日志', '" + request.getContextPath() + "/visual/moduleListPage.do?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + moduleCodeRelated + "&module_id=" + id + "&form_code=" + formCode + "')\">" + btnLogReadName + "</a>");
                        }
                    }
                }

                String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
                String[] linkNames = StrUtil.split(op_link_name, ",");
                String op_link_href = StrUtil.getNullStr(msd.getString("op_link_url"));
                String[] linkHrefs = StrUtil.split(op_link_href, ",");

                String op_link_field = StrUtil.getNullStr(msd.getString("op_link_field"));
                String[] linkFields = StrUtil.split(op_link_field, ",");
                String op_link_cond = StrUtil.getNullStr(msd.getString("op_link_cond"));
                String[] linkConds = StrUtil.split(op_link_cond, ",");
                String op_link_value = StrUtil.getNullStr(msd.getString("op_link_value"));
                String[] linkValues = StrUtil.split(op_link_value, ",");
                String op_link_event = StrUtil.getNullStr(msd.getString("op_link_event"));
                String[] linkEvents = StrUtil.split(op_link_event, ",");
                String op_link_role = StrUtil.getNullStr(msd.getString("op_link_role"));
                String[] linkRoles = StrUtil.split(op_link_role, "#");
                String op_link_icon = StrUtil.getNullStr(msd.getString("op_link_icon"));
                String[] linkIcons = StrUtil.split(op_link_icon, ",");
                String op_link_color = StrUtil.getNullStr(msd.getString("op_link_color"));
                String[] linkColors = StrUtil.split(op_link_color, ",");

                // 为兼容以前的版本，初始化tRole
                if (linkNames != null) {
                    if (linkRoles == null || linkRoles.length != linkNames.length) {
                        linkRoles = new String[linkNames.length];
                        for (int i = 0; i < linkNames.length; i++) {
                            linkRoles[i] = "";
                        }
                    }
                    if (linkIcons == null || linkIcons.length != linkNames.length) {
                        linkIcons = new String[linkNames.length];
                        for (int i = 0; i < linkNames.length; i++) {
                            linkIcons[i] = "";
                        }
                    }
                    if (linkColors == null || linkColors.length != linkNames.length) {
                        linkColors = new String[linkNames.length];
                        for (int i = 0; i < linkNames.length; i++) {
                            linkColors[i] = "";
                        }
                    }
                }

                if (linkNames != null) {
                    for (int i = 0; i < linkNames.length; i++) {
                        String linkName = linkNames[i];
                        String linkBtnName = "";
                        if (!"".equals(linkIcons[i])) {
                            linkBtnName = "<i class=\"fa " + linkIcons[i] + " link-icon\" style=\"color:" + linkColors[i] + "\" />";
                        }
                        if (isColOperateTextShow) {
                            linkBtnName += linkNames[i];
                        }

                        String linkField = linkFields[i];
                        String linkCond = linkConds[i];
                        String linkValue = linkValues[i];
                        String linkEvent = linkEvents[i];
                        String linkRole = linkRoles[i];

                        // 检查是否拥有权限
                        if (!privilege.isUserPrivValid(request, "admin")) {
                            boolean canSeeLink = false;
                            if (!"".equals(linkRole)) {
                                String[] codeAry = StrUtil.split(linkRole, ",");
                                if (codeAry != null) {
                                    UserDb user = new UserDb();
                                    user = user.getUserDb(privilege.getUser(request));
                                    RoleDb[] rdAry = user.getRoles();
                                    if (rdAry != null) {
                                        for (RoleDb rd : rdAry) {
                                            String roleCode = rd.getCode();
                                            for (String codeAllowed : codeAry) {
                                                if (roleCode.equals(codeAllowed)) {
                                                    canSeeLink = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    canSeeLink = true;
                                }
                            } else {
                                canSeeLink = true;
                            }

                            if (!canSeeLink) {
                                continue;
                            }
                        }

                        if ("#".equals(linkField)) {
                            linkField = "";
                        }
                        if ("#".equals(linkCond)) {
                            linkCond = "";
                        }
                        if ("#".equals(linkValue)) {
                            linkValue = "";
                        }
                        if ("#".equals(linkEvent)) {
                            linkEvent = "";
                        }
                        if ("".equals(linkField) || ModuleBackUtil.isLinkShow(request, msd, fdao, linkField, linkCond, linkValue)) {
                            if ("click".equals(linkEvent)) {
                                sb.append("<a href=\"javascript:;\" title=\"" + linkName + "\" class=\"link-btn\" onclick=\"" + ModuleBackUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated) + "\">" + linkBtnName + "</a>");
                            } else {
                                sb.append("<a href=\"javascript:;\" title=\"" + linkName + "\" class=\"link-btn\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleBackUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated) + "')\">" + linkBtnName + "</a>");
                            }
                        }
                    }
                }

                if (mpd.canUserReActive(privilege.getUser(request))) {
                    MyActionDb mad = new MyActionDb();
                    long flowId = fdao.getFlowId();
                    if (flowId != 0 && flowId != -1) {
                        wf = wf.getWorkflowDb((int) flowId);
                        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
                        boolean isReactive = false;
                        if (wpd != null) {
                            isReactive = wpd.isReactive();
                        }

                        if (isReactive) {
                            mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
                            if (mad != null) {
                                String btnAltName = "<i class=\"fa fa-leaf link-icon link-icon-alt\" />";
                                if (isColOperateTextShow) {
                                    btnAltName += "变更";
                                }
                                sb.append("<a href=\"javascript:;\" title=\"变更\" class=\"link-btn\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flowDispose.do?myActionId=" + mad.getId() + "')\">" + btnAltName + "</a>");
                            }
                        }
                    }
                }

                jo.put("colOperate", sb.toString());
                rows.put(jo);
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return jobject.toString();
    }

    /**
     * 恢复记录
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/moduleRestore", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String moduleRestore(@RequestParam(value = "id", required = true) long id) {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            String moduleLog = "module_log";
            FormDb fd = new FormDb();
            fd = fd.getFormDb(moduleLog);

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(id, fd);

            String formCode = fdao.getFieldValue("form_code");
            long moduleId = StrUtil.toLong(fdao.getFieldValue("module_id"));
            long logId = StrUtil.toLong(fdao.getFieldValue("log_id"), -1);
            String fieldName = fdao.getFieldValue("field_name");
            int logType = StrUtil.toInt(fdao.getFieldValue("log_type"));

            // LogUtil.getLog(getClass()).info("logType=" + logType + " formCode=" + formCode + " moduleId=" + moduleId + " logId=" + logId);

            // 找到之前的日志记录，恢复至上一条
            if (logType == FormDAOLog.LOG_TYPE_EDIT) {
                long privLogId = -1;
                String sql = "select id from " + FormDb.getTableNameForLog(formCode) + " where cws_log_id = '" + moduleId + "' and id < " + logId + " order by id desc";
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, 1, 1);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    privLogId = rr.getLong(1);
                }

                if (privLogId == -1) {
                    json.put("ret", "0");
                    json.put("msg", "恢复失败，之前不存在日志记录！");
                }

                fd = fd.getFormDb(formCode);
                FormDAOLog fdaoLog = new FormDAOLog(fd);
                fdaoLog = fdaoLog.getFormDAOLog(privLogId);

                fdao = new com.redmoon.oa.visual.FormDAO();
                fdao = fdao.getFormDAO(moduleId, fd);
                if (fdao.isLoaded()) {
                    fdao.setFieldValue(fieldName, fdaoLog.getFieldValue(fieldName));
                    re = fdao.save();
                } else {
                    json.put("ret", "0");
                    json.put("msg", "记录已被删除，请先恢复被删除的记录");
                    return json.toString();
                }
            } else if (logType == FormDAOLog.LOG_TYPE_DEL) {
                fd = fd.getFormDb(formCode);
                FormDAOLog fdaoLog = new FormDAOLog(fd);
                fdaoLog = fdaoLog.getFormDAOLog(logId);

                fdao = new com.redmoon.oa.visual.FormDAO(fd);
                Vector v = fd.getFields();
                Iterator<FormField> ir = v.iterator();
                while (ir.hasNext()) {
                    FormField ff = ir.next();
                    fdao.setFieldValue(ff.getName(), fdaoLog.getFieldValue(ff.getName()));
                }
                fdao.setFlowId(fdaoLog.getFlowId());
                fdao.setCreator(fdaoLog.getCreator());
                fdao.setCwsId(fdaoLog.getCwsId());
                fdao.setCwsOrder(fdaoLog.getCwsOrder());
                fdao.setFlowTypeCode(fdaoLog.getFlowTypeCode());
                fdao.setUnitCode(fdaoLog.getUnitCode());
                re = fdao.create();
            }

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    /**
     * 在位编辑
     *
     * @param id
     * @param code
     * @param colName
     * @param original_value
     * @param update_value
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/moduleEditInPlace", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String moduleEditInPlace(@RequestParam(value = "id", required = true) long id, String code, String colName, String original_value, String update_value) {
        JSONObject json = new JSONObject();
        if (update_value.equals(original_value)) {
            try {
                json.put("ret", "-1");
                json.put("msg", "值未更改！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
            try {
                json.put("ret", 0);
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            try {
                json.put("ret", 0);
                json.put("msg", "模块：" + code + "不存在！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        boolean re = false;
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        try {
            fdao = fdao.getFormDAO(id, fd);
            fdao.setFieldValue(colName, update_value);
            re = fdao.save();
            // 如果需要记录历史
            if (re && fd.isLog()) {
                FormDAO.log(privilege.getUser(request), FormDAOLog.LOG_TYPE_EDIT, fdao);
            }

            // 相关的脚本事件暂不考虑调用，因为涉及到FileUpload，而此时不存在此变量
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
            return json.toString();
        }

        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }


    /**
     * 删除附件日志
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/moduleDelLog", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delLog(String ids) {
        JSONObject json = new JSONObject();

        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择记录！");
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
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
                        Attachment att = new Attachment((int) ald.getLong("att_id"));
                        String formCode = att.getFormCode();
                        ModulePrivDb mpd = new ModulePrivDb(formCode);
                        if (mpd.canUserManage(pvg.getUser(request))) {
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
            LogUtil.getLog(getClass()).error(e);
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }


    /**
     * 列出下载日志
     *
     * @param moduleId
     * @param attId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/moduleListAttLog", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String listAttLog(long moduleId, long attId) {
        AttachmentLogDb ald = new AttachmentLogDb();
        String sql = ald.getQuery(request, moduleId, attId);
        DebugUtil.i(getClass(), "listAttLog", sql);
        int pageSize = ParamUtil.getInt(request, "limit", -1);
        if (pageSize == -1) {
            // 支持flexigrid
            pageSize = ParamUtil.getInt(request, "rp", 20);
        }
        int curPage = ParamUtil.getInt(request, "page", 1);
        ListResult lr = null;
        try {
            lr = ald.listResult(sql, curPage, pageSize);
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        JSONArray rows = new JSONArray();
        JSONObject jobject = new JSONObject();

        try {
            jobject.put("rows", rows);
            jobject.put("page", curPage);
            jobject.put("total", lr.getTotal());
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
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
                LogUtil.getLog(getClass()).error(e);
            }

            rows.put(jo);
        }

        return jobject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleCreate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleCreate(HttpServletRequest request) throws ErrMsgException {
        return moduleBackService.create(request);
    }

    @ResponseBody
    @RequestMapping(value = "/moduleUpdate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleUpdate(HttpServletRequest request) throws ErrMsgException {
        return moduleBackService.update(request);
    }

    @ResponseBody
    @RequestMapping(value = "/moduleDelAttach", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleDelAttach(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "code");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在！");
            return json.toString();
        }

        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        int attachId = ParamUtil.getInt(request, "attachId", -1);
        com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
        boolean re = att.del();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moduleCreateRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleCreateRelate(HttpServletRequest request) throws ErrMsgException {
        return moduleBackService.createRelate(request);
    }

    @ResponseBody
    @RequestMapping(value = "/moduleUpdateRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moduleUpdateRelate(HttpServletRequest request) throws ErrMsgException {
        return moduleBackService.updateRelate(request);
    }

    @ResponseBody
    @RequestMapping(value = "/moduleDelAttachRelate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delAttachRelate(HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();

        // 取从模块编码
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        if ("".equals(moduleCodeRelated)) {
            json.put("ret", "0");
            json.put("msg", "缺少关联模块编码");
            return json.toString();
        }

        // 取主模块编码
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在");
            return json.toString();
        }
        String formCodeRelated = msd.getString("form_code");

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserModify(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        int id = ParamUtil.getInt(request, "id", -1);
        if (id == -1) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json.toString();
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(moduleCode);
        if (parentMsd == null) {
            json.put("ret", "0");
            json.put("msg", "父模块不存在");
            return json.toString();
        }
        String parentFormCode = parentMsd.getString("form_code");
        String mode = ParamUtil.get(request, "mode");
        // 是否通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId == -1) {
            json.put("ret", "0");
            json.put("msg", "缺少父模块记录的ID");
            return json.toString();
        } else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue == null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }
        if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            json.put("ret", "0");
            json.put("msg", i18nUtil.get("info_access_data_fail"));
            return json.toString();
        }

        int attachId = ParamUtil.getInt(request, "attachId", -1);
        com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
        boolean re = false;
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

    @ResponseBody
    @RequestMapping(value = "/moduleListCalendar", produces = {"application/json;charset=UTF-8;"})
    public String moduleListCalendar(@RequestParam(value = "code", required = true) String code, String start, String end) {
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            LogUtil.getLog(getClass()).error("模块：" + code + "不存在！");
            return "";
        }

        com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserSee(userName)) {
            return arr.toString();
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        String orderBy = "id";
        String sort = "desc";

        // 用于传过滤条件
        request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);
        String[] ary = null;
        try {
            String op = "";
            ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        } catch (ErrMsgException e) {
            DebugUtil.i(getClass(), "moduleListCalendar", "SQL：" + e.getMessage());
            return "";
        }

        String sql = ary[0];
        // sql拼接入时间段
        String fieldBeginDate = msd.getString("field_begin_date");
        String fieldEndDate = msd.getString("field_end_date");
        int p = sql.lastIndexOf(" order ");
        String tmp = " ((" + fieldBeginDate + " between " + StrUtil.sqlstr(start) + " and " + StrUtil.sqlstr(end) + ") or (" + fieldEndDate + " between " + StrUtil.sqlstr(start) + " and " + StrUtil.sqlstr(end) + "))";
        if (p == -1) {
            sql += " and " + tmp;
        } else {
            String sqlPrefix = sql.substring(0, p);
            String sqlSuffix = sql.substring(p);
            sql = sqlPrefix + " and " + tmp + sqlSuffix;
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        String fieldName = msd.getString("field_name");
        String fieldDesc = msd.getString("field_desc");
        String fieldLabel = msd.getString("field_label");
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        try {
            Vector v = fdao.list(formCode, sql);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                fdao = (FormDAO) ir.next();

                String bd = fdao.getFieldValue(fieldBeginDate);
                String ed = fdao.getFieldValue(fieldEndDate);
                com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

                json.put("start", bd);
                json.put("end", ed);

                String name = "";
                if (!StringUtils.isEmpty(fieldName)) {
                    FormField ff = fdao.getFormField(fieldName);
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            name = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
                        }
                    } else {
                        name = FuncUtil.renderFieldValue(fdao, ff);
                    }
                }

                String desc = "";
                if (!StringUtils.isEmpty(fieldDesc)) {
                    FormField ff = fdao.getFormField(fieldDesc);
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            desc = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
                        }
                    } else {
                        desc = FuncUtil.renderFieldValue(fdao, ff);
                    }
                }

                String label = "";
                if (!StringUtils.isEmpty(fieldLabel)) {
                    FormField ff = fdao.getFormField(fieldLabel);
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            label = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
                        }
                    } else {
                        label = FuncUtil.renderFieldValue(fdao, ff);
                    }
                }

                String t = name;
                if (!"".equals(desc)) {
                    t += " " + desc;
                }
                if (!"".equals(label)) {
                    t += " " + label;
                }
                json.put("title", t);
                json.put("id", fdao.getId());
                arr.add(json);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return arr.toString();
    }

    /**
     * 设置单元格格式
     * 一个WritableCellFormat不能被重复引用多次，否则会报错
     * 优化了setCellFormat，使其从map中复用取值，但是map不能置于中作为本页的全局变量，而必须作为一个参数来传
     *
     * @param fieldType
     * @param row
     * @param map
     * @return
     */
    private WritableCellFormat setCellFormat(int fieldType, int row, Map map) {
        WritableCellFormat wcf = null;
        boolean isFirst = false;
        try {
            // 单元格格式
            switch (fieldType) {
                case FormField.FIELD_TYPE_DOUBLE:
                case FormField.FIELD_TYPE_FLOAT:
                case FormField.FIELD_TYPE_PRICE:
                    if (map.get("double") != null) {
                        wcf = (WritableCellFormat) map.get("double");
                    } else {
                        NumberFormat nf1 = new NumberFormat("0.00");
                        wcf = new WritableCellFormat(nf1);
                        map.put("double", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_INT:
                case FormField.FIELD_TYPE_LONG:
                    if (map.get("long") != null) {
                        wcf = (WritableCellFormat) map.get("long");
                    } else {
                        NumberFormat nf2 = new NumberFormat("0");
                        wcf = new WritableCellFormat(nf2);
                        map.put("long", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_DATE:
                    if (map.get("date") != null) {
                        wcf = (WritableCellFormat) map.get("date");
                    } else {
                        jxl.write.DateFormat df1 = new jxl.write.DateFormat("yyyy-MM-dd");
                        wcf = new jxl.write.WritableCellFormat(df1);
                        map.put("date", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_DATETIME:
                    if (map.get("datetime") != null) {
                        wcf = (WritableCellFormat) map.get("datetime");
                    } else {
                        jxl.write.DateFormat df2 = new jxl.write.DateFormat("yyyy-MM-dd HH:mm:ss");
                        wcf = new jxl.write.WritableCellFormat(df2);
                        map.put("datetime", wcf);
                        isFirst = true;
                    }
                    break;
                default:
                    if (map.get("str") != null) {
                        wcf = (WritableCellFormat) map.get("str");
                    } else {
                        wcf = new WritableCellFormat();
                        map.put("str", wcf);
                        isFirst = true;
                    }

                    break;
            }

            if (isFirst) {
                // 不能修改已指向的format， jxl.write.biff.JxlWriteException: Attempt to modify a referenced format
                // 对齐方式
                wcf.setAlignment(Alignment.CENTRE);
                wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
                // 边框
                wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
                //自动换行
                wcf.setWrap(true);

                // 背景色
	        /*
	        if (row % 2 == 0) {
	        	wcf.setBackground(jxl.format.Colour.ICE_BLUE);
			} else {
				wcf.setBackground(jxl.format.Colour.WHITE);
			}
			*/
            }

        } catch (WriteException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return wcf;
    }

    // 创建单元格
    private WritableCell createWritableCell(int fieldType, int column, int row, String data, WritableCellFormat wcf) {
        WritableCell wc = null;
        if (data == null || "".equals(data)) {
            wc = new Label(column, row, "", wcf);
        } else {
            switch (fieldType) {
                case FormField.FIELD_TYPE_TEXT:
                case FormField.FIELD_TYPE_VARCHAR:
                    wc = new Label(column, row, data, wcf);
                    break;
                case FormField.FIELD_TYPE_DOUBLE:
                case FormField.FIELD_TYPE_FLOAT:
                case FormField.FIELD_TYPE_PRICE:
                    wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
                    break;
                case FormField.FIELD_TYPE_INT:
                case FormField.FIELD_TYPE_LONG:
                    wc = new jxl.write.Number(column, row, StrUtil.toLong(data), wcf);
                    break;
                case FormField.FIELD_TYPE_DATE:
                    wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd"), wcf);
                    break;
                case FormField.FIELD_TYPE_DATETIME:
                    wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd HH:mm:ss"), wcf);
                    break;
                default:
                    wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
                    break;
            }
        }
        return wc;
    }

    // 过滤超链接、隐藏输入框
    String filterString(String content) {
        // content = "asdfasdf<input type=\"hidden\" name=\"checkItemsSel\" id=\"checkItemsSel\" value=\"\" />asdfasdf";
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        replacementStr = "";
        patternStr = "<a .*?style=['|\"]?display:none['|\"]?>(.*?)</div>";
        pattern = Pattern.compile(patternStr,
                Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);

        patternStr = "<input .*?type=['|\"]?hidden['|\"]? .*?>";
        pattern = Pattern.compile(patternStr,
                Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);

        // 注意来自于嵌套表nest_table_view.jsp中的数据，如果不过滤style就会出现乱码
        // 采用以下方式导入的css文件，不会出现乱码：@import url("...");
        // 可能是因为生成word后，丢失了css，所以不会出现乱码

        // 过滤javascript
        String regExScript = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或]*?>[\\s\\S]*?<\\/script> }
        //String regExScript = "<script[^>]*>.*</script[^>]*>"; // 此行过滤不了，@task:AntiXSS.stripScriptTag中可能存在同样问题

        Pattern pat = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
        Matcher m = pat.matcher(content);
        content = m.replaceAll("");

        // 过滤style
        String regExStyle = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或]*?>[\\s\\S]*?<\\/style> }
        pat = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
        m = pat.matcher(content);
        content = m.replaceAll("");

        // 过滤html
        // String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
        // LogUtil.getLog(getClass()).info(getClass() + cont);

        return content;
    }

	/*@RequestMapping("/moduleEdit")
	public String userResetPwd(Model model) {
		model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
		return "module_edit";
	}*/

    public static boolean isXlsxRowEmpty(XSSFRow row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            XSSFCell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public static boolean isXlsRowEmpty(HSSFRow row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            HSSFCell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public JSONArray importData(String userName, String formCode,String unitCode,String path, boolean isAll,String cws_id, int templateId) throws ErrMsgException, IOException{
        JSONArray rowAry = new JSONArray();
        // LogUtil.getLog(getClass()).info("userName = " + userName + "formCode = "+ formCode + "unitCode = "+unitCode + "path = "+ path + "isall = "+ isAll);
        InputStream in = null;
        try {
            // LogUtil.getLog(getClass()).info(getClass()+"::::"+formCode);
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDbOrInit(formCode);

            // String listField = StrUtil.getNullStr(msd.getString("list_field"));
            String[] fields = msd.getColAry(false, "list_field");

            JSONArray arr = null;
            if (templateId!=-1) {
                ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
                mid = mid.getModuleImportTemplateDb(templateId);

                String rules = mid.getString("rules");
                try {
                    arr = new JSONArray(rules);
                    if (arr.length()>0) {
                        fields = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject json = (JSONObject) arr.get(i);
                            fields[i] = json.getString("name");
                        }
                    }
                }
                catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                    throw new ErrMsgException(e.getMessage());
                }
            }
			/*
			 for(int i = 0 ; i < fields.length; i ++){
			 */
            // LogUtil.getLog(getClass()).info(getClass()+"::path="+path);
            FormDb fd = new FormDb(formCode);
            // LogUtil.getLog(getClass()).info(getClass() + " isAll2=" + isAll);
            if (isAll) {
                Vector vt = fd.getFields();
                fields = new String[vt.size()];
                Iterator ir = vt.iterator();
                int i=0;
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    fields[i] = ff.getName();
                    i++;
                }
            }

            MacroCtlMgr mm = new MacroCtlMgr();

            in = new FileInputStream(path);
            String pa = StrUtil.getFileExt(path);
            if (pa.equals("xls")) {
                // 读取xls格式的excel文档
                HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
                // 获取sheet
                int rows = w.getNumberOfSheets();
                rows = 1; // 只取第1张sheet
                for (int i = 0; i < rows; i++) {
                    HSSFSheet sheet = w.getSheetAt(i);
                    if (sheet != null) {
                        // 获取行数
                        int rowcount = sheet.getLastRowNum();
                        HSSFCell cell = null;

                        // 取得第0行，检查表头是否相符
                        HSSFRow rowHeader = sheet.getRow(0);
                        if (rowHeader != null) {
                            int colcount = rowHeader.getLastCellNum();
                            if (templateId!=-1 && colcount != arr.length()) {
                                throw new ErrMsgException("表头数量为" + colcount + "，与模板文件中的数量" + arr.length() + "不同");
                            }
                            // 获取每一单元格
                            for (int m = 0; m < colcount; m++) {
                                cell = rowHeader.getCell(m);
                                if (cell==null) {
                                    continue;
                                }
                                cell.setCellType(CellType.STRING);
                                String colTitle = cell.getStringCellValue();
                                if (templateId!=-1) {
                                    JSONObject json = (JSONObject) arr.get(m);
                                    String title = json.getString("title");
                                    if (!title.equals(colTitle)) {
                                        throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
                                    }
                                }
                            }
                        }

                        // 获取每一行
                        for (int k = 1; k <= rowcount; k++) {
                            HSSFRow row = sheet.getRow(k);
                            if (row != null) {
                                if (isXlsRowEmpty(row)) {
                                    continue;
                                }
                                int colcount = row.getLastCellNum();
                                if (colcount > fields.length) {
                                    colcount = fields.length;
                                }
                                JSONObject jo = new JSONObject();

                                // 获取每一单元格
                                for (int m = 0; m < colcount; m++) {
                                    cell = row.getCell(m);

                                    String colName = fields[m];

                                    if (cell==null) {
                                        jo.put(colName, "");
                                        continue;
                                    }

                                    // 为空表示不需要导入
                                    if ("".equals(fields[m])) {
                                        jo.put(colName, "");
                                        continue;
                                    }
                                    // LogUtil.getLog(getClass()).info(getClass() + " m=" + m + " fields[m]=" + fields[m]);
                                    if (fields[m].equals("cws_creator")) {
                                        jo.put(colName, userName);
                                    }
                                    else {
                                        if (CellType.NUMERIC == cell.getCellType() && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                            Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue());
                                            jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
                                        }
                                        else {
                                            cell.setCellType(CellType.STRING);
                                            String val = cell.getStringCellValue().trim();
                                            jo.put(colName, val);
                                        }
                                    }
                                }
                                rowAry.put(jo);
                            }
                        }
                    }
                }
            } else if (pa.equals("xlsx")) {
                XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
                int rows = w.getNumberOfSheets();
                rows = 1; // 只取第1张sheet
                for (int i = 0; i < rows; i++) {
                    XSSFSheet sheet = w.getSheetAt(i);
                    if (sheet != null) {
                        int rowcount = sheet.getLastRowNum();
                        XSSFCell cell = null;
                        // 取得第0行，检查表头是否相符
                        XSSFRow rowHeader = sheet.getRow(0);
                        if (rowHeader != null) {
                            int colcount = rowHeader.getLastCellNum();
                            if (templateId!=-1 && colcount != arr.length()) {
                                throw new ErrMsgException("表头数量为" + colcount + "，与模板文件中的数量" + arr.length() + "不同");
                            }
                            // 获取每一单元格
                            for (int m = 0; m < colcount; m++) {
                                cell = rowHeader.getCell(m);
                                if (cell==null) {
                                    continue;
                                }
                                cell.setCellType(CellType.STRING);
                                String colTitle = cell.getStringCellValue();
                                if (templateId!=-1) {
                                    JSONObject json = (JSONObject) arr.get(m);
                                    String title = json.getString("title");
                                    if (!title.equals(colTitle)) {
                                        throw new ErrMsgException("表头“" + colTitle + "”与模板文件中的“" + title + "”不相符");
                                    }
                                }
                            }
                        }
                        // FormDAO fdao = new FormDAO();
                        for (int k = 1; k <= rowcount; k++) {
                            XSSFRow row = sheet.getRow(k);
                            if (row != null) {
                                // 如果是空行则跳过
                                if (isXlsxRowEmpty(row)) {
                                    continue;
                                }
                                int colcount = row.getLastCellNum();
                                if (colcount > fields.length) {
                                    colcount = fields.length;
                                }

                                JSONObject jo = new JSONObject();
                                for (int m = 0; m < colcount; m++) {
                                    cell = row.getCell(m);

                                    String colName = fields[m];

                                    if (cell==null) {
                                        jo.put(colName, "");
                                        continue;
                                    }
                                    // 为空表示不需要导入
                                    if ("".equals(fields[m])) {
                                        jo.put(colName, "");
                                        continue;
                                    }

                                    if (fields[m].equals("cws_creator")) {
                                        jo.put(colName, userName);
                                    }
                                    else {
                                        if (CellType.NUMERIC == cell.getCellType() && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                            Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue());
                                            jo.put(colName, DateUtil.format(date, "yyyy-MM-dd"));
                                        }
                                        else {
                                            cell.setCellType(CellType.STRING);
                                            String val = cell.getStringCellValue().trim();
                                            jo.put(colName, val);
                                        }
                                    }
                                }
                                rowAry.put(jo);
                            }
                        }
                    }
                }
            }
        }
        catch (ErrMsgException e) {
            throw e;
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rowAry;
    }

    /**
     * 导入单条记录，如资产负债表
     * @param userName
     * @param code
     * @param formCode
     * @param moduleCodeRelated
     * @param unitCode
     * @param path
     * @param cws_id
     * @param mid
     * @return
     * @throws ErrMsgException
     * @throws IOException
     */
    public boolean importDataCells(String userName, String code, String formCode, String moduleCodeRelated, String unitCode, String path, String cws_id, ModuleImportTemplateDb mid) throws ErrMsgException, IOException{
        InputStream in = null;
        try {
            List<List> lists = new ArrayList<List>();
            JSONArray aryCleans = null;
            String strJson = StrUtil.getNullStr(mid.getString("cleans"));
            if (!"".equals(strJson)) {
                aryCleans = new JSONArray(strJson);
            }

            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            MacroCtlMgr mm = new MacroCtlMgr();
            in = new FileInputStream(path);
            String pa = StrUtil.getFileExt(path);
            if ("xls".equals(pa)) {
                // 读取xls格式的excel文档
                HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
                // 获取sheet
                int rows = w.getNumberOfSheets();
                rows = 1; // 只取第1张sheet
                for (int i = 0; i < rows; i++) {
                    HSSFSheet sheet = w.getSheetAt(i);
                    if (sheet != null) {
                        // 获取行数
                        int rowcount = sheet.getLastRowNum();
                        HSSFCell cell = null;
                        // 获取每一行
                        for (int k = 0; k < rowcount; k++) {
                            List<String> list = new ArrayList<String>();
                            HSSFRow row = sheet.getRow(k);
                            if (row != null) {
                                if (isXlsRowEmpty(row)) {
                                    continue;
                                }
                                int colcount = row.getLastCellNum();
                                JSONObject jo = new JSONObject();

                                // 获取每一单元格
                                for (int m = 0; m < colcount; m++) {
                                    cell = row.getCell(m);

                                    if (cell==null) {
                                        continue;
                                    }

                                    if (CellType.NUMERIC == cell.getCellType() && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue());
                                        list.add(DateUtil.format(date, "yyyy-MM-dd"));
                                    }
                                    else {
                                        cell.setCellType(CellType.STRING);
                                        String val = cell.getStringCellValue().trim();
                                        list.add(val);
                                    }
                                }
                                lists.add(list);
                            }
                        }
                    }
                }
            } else if ("xlsx".equals(pa)) {
                XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
                int rows = w.getNumberOfSheets();
                rows = 1; // 只取第1张sheet
                for (int i = 0; i < rows; i++) {
                    XSSFSheet sheet = w.getSheetAt(i);
                    if (sheet != null) {
                        int rowcount = sheet.getLastRowNum();
                        XSSFCell cell = null;
                        // FormDAO fdao = new FormDAO();
                        for (int k = 0; k < rowcount; k++) {
                            List<String> list = new ArrayList<String>();
                            XSSFRow row = sheet.getRow(k);
                            if (row != null) {
                                // 如果是空行则跳过
                                if (isXlsxRowEmpty(row)) {
                                    continue;
                                }
                                int colcount = row.getLastCellNum();
                                for (int m = 0; m < colcount; m++) {
                                    cell = row.getCell(m);

                                    if (cell==null) {
                                        continue;
                                    }

                                    if (CellType.NUMERIC == cell.getCellType() && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue());
                                        list.add(DateUtil.format(date, "yyyy-MM-dd"));
                                    }
                                    else {
                                        cell.setCellType(CellType.STRING);
                                        String val = cell.getStringCellValue().trim();
                                        DebugUtil.i(getClass(), "val", val);
                                        list.add(val);
                                    }
                                }
                                lists.add(list);
                            }
                        }
                    }
                }
            }

            long parentId = StrUtil.toLong(cws_id, -1);

            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDbOrInit(formCode);
            // 执行验证脚本
            String errMsg = "";
            String script = vsd.getScript("import_validate");
            if (script != null && !script.equals("")) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuilder sb = new StringBuilder();

                    // 赋值用户
                    sb.append("userName=\"" + userName + "\";");
                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.set("importRecords", lists);
                    bsh.set("request", request);
                    bsh.set("parentId", parentId);
                    bsh.set("moduleCodeRelated", moduleCodeRelated);
                    bsh.set("code", code);

                    bsh.eval(script);
                    Object obj = bsh.get("ret");
                    if (obj != null) {
                        boolean ret = ((Boolean) obj).booleanValue();
                        if (!ret) {
                            errMsg = (String) bsh.get("errMsg");
                            throw new ErrMsgException(errMsg);
                        }
                    } else {
                        // 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
                        // throw new ErrMsgException("该节点脚本中未配置ret=...");
                    }
                } catch (EvalError e) {
                    LogUtil.getLog(getClass()).error(e);
                    throw new ErrMsgException(e.getMessage());
                }
            }

            String rules = mid.getString("rules");
            FormDAO fdao = new FormDAO(fd);
            DebugUtil.i(getClass(), "rules", rules);
            com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(rules);
            if (arr != null) {
                for (Object obj : arr) {
                    com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject)obj;
                    int row = json.getIntValue("r");
                    int col = json.getIntValue("c");
                    String fieldName = json.getString("field");

                    int r = 0;
                    for (List<String> list : lists) {
                        int c = 0;
                        for (String cell : list) {
                            if (row == r && col == c) {
                                DebugUtil.i(getClass(), "lists", "r=" + r + " c=" + c + " " + list.toString());
                                FormField ff = fd.getFormField(fieldName);
                                if (ff == null) {
                                    LogUtil.getLog(getClass()).error("字段：" + fieldName + " 已不存在");
                                    continue;
                                }
                                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                    if (mu != null && !"macro_raty".equals(mu.getCode())) {
                                        // 如果是基础数据宏控件
                                        boolean isClean = false;
                                        if ("macro_flow_select".equals(mu.getCode())) {
                                            JSONObject jsonObj = null;
                                            if (aryCleans != null) {
                                                for (int i = 0; i < aryCleans.length(); i++) {
                                                    jsonObj = aryCleans.getJSONObject(i);
                                                    if (ff.getName().equals(jsonObj.get("fieldName"))) {
                                                        isClean = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            // 如果需清洗数据
                                            if (isClean) {
                                                cell = jsonObj.getString(cell);
                                            }
                                        }
                                        if (!isClean) {
                                            cell = mu.getIFormMacroCtl().getValueByName(ff, cell);
                                        }
                                    }
                                }
                                fdao.setFieldValue(fieldName, cell);
                                break;
                            }
                            c++;
                        }
                        if (row == r && col == c) {
                            break;
                        }
                        r++;
                    }
                }
            }

            fdao.setCreator(userName);
            fdao.setUnitCode(unitCode);
            if (parentId != -1) {
                fdao.setCwsId(String.valueOf(parentId));
            }
            fdao.create();
            // 如果需要记录历史
            if (fd.isLog()) {
                FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
            }

            // 导入后事件
            script = vsd.getScript("import_create");
            if (script != null && !"".equals(script)) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuilder sb = new StringBuilder();

                    // 赋值用户
                    sb.append("userName=\"").append(userName).append("\";");
                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.set("records", lists);
                    bsh.set("request", request);

                    bsh.eval(script);
                } catch (EvalError e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        catch (Exception e) {
            //LogUtil.getLog(SignMgr.class).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    @RequestMapping(value = "/moduleListSelPage")
    public String moduleListSelPage(Model model) {
        String op = ParamUtil.get(request, "op");
        String moduleCode = ParamUtil.get(request, "formCode");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(msd.getString("form_code"));
        if (!fd.isLoaded()) {
            model.addAttribute("msg", "表单不存在");
            return "th/error/info";
        }

        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean isModuleFieldSelectCtlCheckPrivilege = cfg.getBooleanProperty("isModuleFieldSelectCtlCheckPrivilege");
        if (isModuleFieldSelectCtlCheckPrivilege) {
            Privilege privilege = new Privilege();
            if (!mpd.canUserSee(privilege.getUser(request))) {
                model.addAttribute("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
                return "th/error/error";
            }
        }

        String openerFormCode = ParamUtil.get(request, "openerFormCode");
        if ("".equals(openerFormCode)) {
            model.addAttribute("msg", "openerFormCode不能为空");
            return "th/error/error";
        }
        String openerFieldName = ParamUtil.get(request, "openerFieldName");

        int mode = 1; // 默认为选择窗体

        FormDb openerFd = new FormDb();
        openerFd = openerFd.getFormDb(openerFormCode);
        if (!openerFd.isLoaded()) {
            model.addAttribute("msg", "表单：" + openerFormCode + " 不存在");
            return "th/error/error";
        }
        FormField openerField = openerFd.getFormField(openerFieldName);
        if (openerField == null) {
            model.addAttribute("msg", "字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在");
            return "th/error/error";
        }

        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
        String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(desc);
        String filter = com.redmoon.oa.visual.ModuleBackUtil.decodeFilter(json.getString("filter"));
        if ("none".equals(filter)) {
            filter = "";
        }
        String byFieldName = json.getString("idField");
        String showFieldName = json.getString("showField");

        if (json.containsKey("mode")) {
            mode = json.getIntValue("mode");
        }

        String queryStr = "pageType=moduleListSel&op=" + op + "&formCode=" + moduleCode + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

        // 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值{$fieldName}
        com.alibaba.fastjson.JSONArray colProps = null;
        try {
            colProps = ModuleBackUtil.getColProps(msd, true);
        } catch (ErrMsgException e) {
            model.addAttribute("msg", e.getMessage());
            return "th/error/error";
        }

        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
        String condsHtml = ModuleBackUtil.getConditionHtml(request, msd, dateFieldNamelist);
        boolean isQuery = !"".equals(condsHtml);

        model.addAttribute("dateFieldNamelist", dateFieldNamelist);
        model.addAttribute("condsHtml", condsHtml);
        model.addAttribute("isQuery", isQuery);
        model.addAttribute("colProps", colProps.toString());
        model.addAttribute("mode", mode);
        model.addAttribute("openerFormCode", openerFormCode);
        model.addAttribute("openerFieldName", openerFieldName);
        model.addAttribute("byFieldName", byFieldName);
        model.addAttribute("showFieldName", showFieldName);
        model.addAttribute("queryStr", queryStr);
        model.addAttribute("filter", filter);
        model.addAttribute("moduleCode", moduleCode);
        model.addAttribute("op", "search");
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("conds", ModuleBackUtil.getModuleListSelCondScriptFromWinOpener(filter));
        model.addAttribute("moduleListCss", msd.getCss(ConstUtil.PAGE_TYPE_LIST));
        model.addAttribute("formName", fd.getName());

        int modulePageSize = cfg.getInt("modulePageSize");
        model.addAttribute("pageSize", modulePageSize);
        return "th/visual/module_list_sel";
    }

    @RequestMapping(value = "/moduleListNestSel")
    public String moduleListNestSel(Model model) {
        String op = ParamUtil.get(request, "op");
        String nestFormCode = "";
        String nestType = ParamUtil.get(request, "nestType");
        String parentFormCode = ParamUtil.get(request, "parentFormCode");
        String nestFieldName = ParamUtil.get(request, "nestFieldName");
        long parentId = ParamUtil.getLong(request, "parentId", -1);
        int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;

        FormDb pForm = new FormDb();
        pForm = pForm.getFormDb(parentFormCode);
        FormField nestField = pForm.getFormField(nestFieldName);

        if (parentId != -1) {
            com.redmoon.oa.visual.FormDAO fdaoPar = new com.redmoon.oa.visual.FormDAO();
            fdaoPar = fdaoPar.getFormDAO(parentId, pForm);
            flowId = fdaoPar.getFlowId();
        }

        String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);

        String moduleCode = json.getString("sourceForm");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);
        String formCode = msd.getString("form_code");
        String filter = json.getString("filter");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            model.addAttribute("msg", "表单不存在");
            return "th/error/error";
        }

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        if (isNestSheetCheckPrivilege) {
            if (!mpd.canUserSee(privilege.getUser(request))) {
                model.addAttribute("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
                return "th/error/error";
            }
        }

        String action = ParamUtil.get(request, "action");
        long mainId = ParamUtil.getLong(request, "mainId", -1);
        String queryStr = "op=" + op + "&action=" + action + "&formCode=" + formCode + "&parentFormCode=" + parentFormCode + "&nestFieldName=" + nestFieldName + "&nestType=" + nestType + "&parentId=" + parentId + "&mainId=" + mainId;

        try {
            com.alibaba.fastjson.JSONArray colProps = ModuleBackUtil.getColProps(msd, false);
            model.addAttribute("colProps", colProps.toString());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            model.addAttribute("msg", e.getMessage());
            return "th/error/error";
        }

        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
        String condsHtml = ModuleBackUtil.getConditionHtml(request, msd, dateFieldNamelist);
        boolean isQuery = !"".equals(condsHtml);

        model.addAttribute("dateFieldNamelist", dateFieldNamelist);
        model.addAttribute("condsHtml", condsHtml);
        model.addAttribute("isQuery", isQuery);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("queryStr", queryStr);
        model.addAttribute("flowId", flowId);
        model.addAttribute("formName", fd.getName());
        model.addAttribute("moduleListCss", msd.getCss(ConstUtil.PAGE_TYPE_LIST));
        model.addAttribute("conds", ModuleBackUtil.getModuleListNestSelCondScriptFromWinOpener(filter));
        int modulePageSize = cfg.getInt("modulePageSize");
        model.addAttribute("pageSize", modulePageSize);
        model.addAttribute("nestFormCode", nestFormCode);
        model.addAttribute("parentFormCode",parentFormCode);
        model.addAttribute("nestFieldName", nestFieldName);
        model.addAttribute("parentId", parentId);
        model.addAttribute("nestType", nestType);
        model.addAttribute("moduleCode", moduleCode);
        return "th/visual/module_list_nest_sel";
    }

    @ResponseBody
    @RequestMapping(value = "/moduleSelBatchForNest", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String selBatchForNest(int flowId, String parentFormCode, long parentId, String nestFieldName, String nestType) {
        FormDb pForm = new FormDb();
        pForm = pForm.getFormDb(parentFormCode);
        FormField nestField = pForm.getFormField(nestFieldName);

        String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);
        String nestFormCode = json.getString("destForm");
        String sourceForm = json.getString("sourceForm");
        com.alibaba.fastjson.JSONArray mapAry = json.getJSONArray("maps");

        String script = "";
        ModuleSetupDb msdSource = new ModuleSetupDb();
        msdSource = msdSource.getModuleSetupDbOrInit(sourceForm);
        try {
            script = moduleBackService.getSelBatchForNestScript(request, flowId, parentFormCode, parentId, msdSource, nestFormCode, nestFieldName, nestType, mapAry);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            return responseUtil.getResultJson(false, e.getMessage()).toString();
        }

        com.alibaba.fastjson.JSONObject result = responseUtil.getResultJson(true);
        result.put("script", script);
        return result.toString();
    }

    @RequestMapping(value = "/moduleListPage")
    public String moduleListPage(Model model) {
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        String op = ParamUtil.get(request, "op");
        String action = ParamUtil.get(request, "action");

        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "code");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }
        if ("".equals(code)) {
            model.addAttribute("msg", SkinUtil.ERR_ID);
            return "th/error/error";
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            model.addAttribute("msg", "模块不存在");
            return "th/error/error";
        }

        if (msd.getInt("is_use") != 1) {
            model.addAttribute("msg", "模块未启用");
            return "th/error/error";
        }

        boolean isEditInplace = msd.getInt("is_edit_inplace") == 1;
        String formCode = msd.getString("form_code");

        if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT) {
            return "forward:" + "module_list_gantt.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
        } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR) {
            return "forward:" + "module_list_calendar.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
        } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_TREE) {
            boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
            if (!isInFrame) {
                return "forward:" + "module_list_frame.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
            }
        } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_MODULE_TREE) {
            boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
            if (!isInFrame) {
                return "forward:" + "module_basic_tree_frame.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
            }
        } else if (msd.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CUSTOM) {
            String moduleUrlList = StrUtil.getNullStr(msd.getString("url_list"));
            if (!"".equals(moduleUrlList)) {
                return "redirect:" + request.getContextPath() + "/" + moduleUrlList + "?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
            }
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserSee(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        Config cfg = Config.getInstance();

        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");

        if ("".equals(orderBy)) {
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

        request.setAttribute("moduleCode", code);

        // 置嵌套表需要用到的页面类型
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_LIST);

        String unitCode = ParamUtil.get(request, "unitCode");

        boolean isAutoHeight = msd.getInt("is_auto_height")==1;

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("pageCss", StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_LIST)));
        model.addAttribute("formCode", formCode);
        model.addAttribute("code", code);

        StringBuffer params = new StringBuffer();
        com.alibaba.fastjson.JSONArray aryParam = new com.alibaba.fastjson.JSONArray();
        Enumeration enu = request.getParameterNames();
        while(enu.hasMoreElements()) {
            String paramName = (String) enu.nextElement();
            if ("code".equals(paramName) || "formCode".equals(paramName)) {
                continue;
            }
            String paramVal = ParamUtil.get(request, paramName);
            StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramVal));
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("paramName", paramName);
            json.put("paramVal", paramVal);
            aryParam.add(json);
        }
        model.addAttribute("params", params);
        model.addAttribute("op", op);

        model.addAttribute("random", Math.random());

        String querystr = "op=" + op + "&code=" + code + "&orderBy=" + orderBy + "&sort=" + sort + "&unitCode=" + unitCode;

        // 将过滤配置中request中其它参数也传至url中，这样分页时可传入参数
        String requestParams = "";
        String requestParamInputs = "";

        Map map = ModuleBackUtil.getFilterParams(request, msd);
        Iterator irMap = map.keySet().iterator();
        while (irMap.hasNext()) {
            String key = (String) irMap.next();
            String val = (String) map.get(key);
            requestParams += "&" + key + "=" + StrUtil.UrlEncode(val);
            requestParamInputs += "<input type='hidden' name='" + key + "' value='" + val + "' />";
        }
        querystr += requestParams;

        // 用于传过滤条件
        request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);
        String[] ary = null;
        try {
            ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        } catch (ErrMsgException e) {
            model.addAttribute("msg", e.getMessage());
            return "th/error/error";
        }

        String sqlUrlStr = ary[1];
        if (!"".equals(sqlUrlStr)) {
            querystr += "&" + sqlUrlStr;
        }

        // 加上二开传入的参数
        querystr += "&" + params.toString();

        int defaultPageSize = cfg.getInt("modulePageSize");
        int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);

        String[] fields = msd.getColAry(false, "list_field");
        if (fields == null || fields.length == 0) {
            model.addAttribute("msg", "显示列未配置");
            return "th/error/error";
        }

        model.addAttribute("querystr", querystr);

        MacroCtlMgr mm = new MacroCtlMgr();

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
        String[] btnBclasses = StrUtil.split(btnBclass, ",");
        String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
        String[] btnRoles = StrUtil.split(btnRole, "#");

        boolean isToolbar = true;
        if (btnNames != null) {
            int len = btnNames.length;
            for (int i = 0; i < len; i++) {
                if (btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if ("queryFields".equals(json.get("btnType"))) {
                        if (json.containsKey("isToolbar")) {
                            isToolbar = json.getIntValue("isToolbar") == 1;
                        }
                        break;
                    }
                }
            }
        }

        boolean isButtonsShow = false;
        isButtonsShow = (msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(privilege.getUser(request))) ||
                (msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(privilege.getUser(request))) ||
                mpd.canUserDel(privilege.getUser(request)) ||
                mpd.canUserManage(privilege.getUser(request)) ||
                mpd.canUserImport(privilege.getUser(request)) ||
                mpd.canUserExport(privilege.getUser(request)) ||
                (btnNames != null && isToolbar);

        boolean isShowUnitCode = false;
        Vector<DeptDb> vtUnit = new Vector<>();
        DeptDb dd = new DeptDb();
        String myUnitCode = "";

        if (msd.getInt("is_unit_show") == 1) {
            myUnitCode = privilege.getUserUnitCode(request);
            dd = dd.getDeptDb(myUnitCode);

            vtUnit.addElement(dd);

            // 向下找两级单位
            DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
            java.util.Vector<DeptDb> vt = dl.getDirList();
            for (DeptDb db : vt) {
                dd = db;
                if (dd.getType() == DeptDb.TYPE_UNIT) {
                    vtUnit.addElement(dd);
                    DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
                    for (DeptDb deptDb : dl2.getDirList()) {
                        dd = deptDb;
                        if (dd.getType() == DeptDb.TYPE_UNIT) {
                            vtUnit.addElement(dd);
                        }

                        DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
                        for (DeptDb value : dl3.getDirList()) {
                            dd = value;
                            if (dd.getType() == DeptDb.TYPE_UNIT) {
                                vtUnit.addElement(dd);
                            }
                        }
                    }
                }
            }

            // 如果是集团单位，且能够管理模块
            if (vtUnit.size() > 1 && mpd.canUserManage(privilege.getUser(request))) {
                isShowUnitCode = true;
            }
        }

        com.alibaba.fastjson.JSONArray colProps = new com.alibaba.fastjson.JSONArray();
        try {
            colProps = ModuleBackUtil.getColProps(msd, false);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("colProps", colProps);

        model.addAttribute("isShowUnitCode", isShowUnitCode);
        model.addAttribute("requestParamInputs", requestParamInputs);

        boolean isCurCuserUnitCodeRoot = false;
        if (privilege.getUserUnitCode(request).equals(DeptDb.ROOTCODE)) {
            isCurCuserUnitCodeRoot = true;
        }
        model.addAttribute("isCurCuserUnitCodeRoot", isCurCuserUnitCodeRoot);

        com.alibaba.fastjson.JSONArray aryUnit = new com.alibaba.fastjson.JSONArray();
        for (DeptDb deptDb : vtUnit) {
            dd = deptDb;
            int layer = dd.getLayer();
            String layerStr = "";
            for (int i = 2; i < layer; i++) {
                layerStr += "&nbsp;&nbsp;";
            }
            if (layer > 1) {
                layerStr += "├";
            }
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("deptCode", dd.getCode());
            json.put("deptName", dd.getName());
            json.put("layer", layerStr);
            aryUnit.add(json);
        }
        model.addAttribute("aryUnit", aryUnit);

        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
        int len = 0;

        String condsHtml = ModuleBackUtil.getConditionHtml(request, msd, dateFieldNamelist);
        boolean isQuery = !"".equals(condsHtml);

        model.addAttribute("isQuery", isQuery);
        model.addAttribute("condsHtml", condsHtml);
        model.addAttribute("isShowUnitCode", isShowUnitCode);
        model.addAttribute("dateFieldNamelist", dateFieldNamelist);

        model.addAttribute("mainCode", ParamUtil.get(request, "mainCode"));
        int menuItem = ParamUtil.getInt(request, "menuItem", 1);
        model.addAttribute("menuItem", menuItem);

        model.addAttribute("MODULE_EXPORT_WORD_VIEW_FORM", ConstUtil.MODULE_EXPORT_WORD_VIEW_FORM);
        com.alibaba.fastjson.JSONArray aryFormView = new com.alibaba.fastjson.JSONArray();
        FormViewDb formViewDb = new FormViewDb();
        boolean hasView = false;
        Vector vtView = formViewDb.getViews(formCode);
        if (vtView.size() > 0) {
            hasView = true;
            Iterator irView = vtView.iterator();
            while (irView.hasNext()) {
                formViewDb = (FormViewDb)irView.next();
                com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                json.put("id", formViewDb.getLong("id"));
                json.put("name", formViewDb.getString("name"));
                aryFormView.add(json);
            }
        }
        model.addAttribute("hasView", hasView);
        model.addAttribute("aryFormView", aryFormView);

        boolean canManage =  mpd.canUserManage(userName);
        boolean isBtnAddShow = msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(userName);
        boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName);
        boolean isBtnDelShow = msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage);
        boolean isBtnMoveShow = canManage && isShowUnitCode;
        boolean isBtnImportShow = mpd.canUserImport(userName);
        boolean isBtnExportShow = mpd.canUserExport(userName);
        boolean isBtnExportWordShow = mpd.canUserExportWord(userName);
        model.addAttribute("isBtnAddShow", isBtnAddShow);
        model.addAttribute("isBtnEditShow", isBtnEditShow);
        model.addAttribute("isBtnDelShow", isBtnDelShow);
        model.addAttribute("isBtnMoveShow", isBtnMoveShow);
        model.addAttribute("isBtnImportShow", isBtnImportShow);
        model.addAttribute("isBtnExportShow", isBtnExportShow);
        model.addAttribute("isBtnExportWordShow", isBtnExportWordShow);

        com.alibaba.fastjson.JSONArray aryBtn = new com.alibaba.fastjson.JSONArray();
        if (btnNames != null && btnBclasses != null) {
            len = btnNames.length;
            for (int i = 0; i < len; i++) {
                boolean isToolBtn = false;
                if (!btnScripts[i].startsWith("{")) {
                    isToolBtn = true;
                } else {
                    com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    String btnType = json.getString("btnType");
                    if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
                        isToolBtn = true;
                    }
                }
                if (isToolBtn) {
                    // 检查是否拥有权限
                    boolean canSeeBtn = false;
                    if (!privilege.isUserPrivValid(request, "admin")) {
                        if (btnRoles != null && btnRoles.length > 0) {
                            String roles = btnRoles[i];
                            String[] codeAry = StrUtil.split(roles, ",");
                            // 如果codeAry为null，则表示所有人都能看到
                            if (codeAry == null) {
                                canSeeBtn = true;
                            } else {
                                UserDb user = new UserDb();
                                user = user.getUserDb(privilege.getUser(request));
                                RoleDb[] rdAry = user.getRoles();
                                if (rdAry != null) {
                                    for (RoleDb roleDb : rdAry) {
                                        String roleCode = roleDb.getCode();
                                        for (String codeAllowed : codeAry) {
                                            if (roleCode.equals(codeAllowed)) {
                                                canSeeBtn = true;
                                                break;
                                            }
                                        }
                                        if (canSeeBtn) {
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            canSeeBtn = true;
                        }

                        if (!canSeeBtn) {
                            continue;
                        }
                    }
                    else {
                        canSeeBtn = true;
                    }
                    if (canSeeBtn) {
                        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                        json.put("name", btnNames[i]);
                        json.put("class", btnBclasses[i]);
                        aryBtn.add(json);
                    }
                }
            }
        }
        model.addAttribute("aryBtn", aryBtn);
        model.addAttribute("isAdmin", privilege.isUserPrivValid(request, "admin"));

        model.addAttribute("isEditInplace", isEditInplace);
        // 可在位编辑的字段相关属性
        com.alibaba.fastjson.JSONArray aryEditable = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONArray aryEditableOpt = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONArray aryChkPresent = new com.alibaba.fastjson.JSONArray();
        if (isEditInplace) {
            moduleBackService.getEditInplaceProps(aryEditable, aryEditableOpt, aryChkPresent, mpd, fd);
        }
        model.addAttribute("aryEditable", aryEditable);
        model.addAttribute("aryEditableOpt", aryEditableOpt);
        model.addAttribute("aryChkPresent", aryChkPresent);

        model.addAttribute("isAutoHeight", isAutoHeight);
        model.addAttribute("pageSize", pagesize);

        model.addAttribute("isViewEditCustom", msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM);
        model.addAttribute("moduleName", msd.getString("name"));
        model.addAttribute("urlEdit", msd.getString("url_edit"));

        String expUrl = "";
        // 检查是否设置有模板
        Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msd.getString("form_code"));
        if (vt.size()>0) {
            String querystrTempl = "code=" + code;
            expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp?" + querystrTempl;
        }
        else {
            expUrl = request.getContextPath() + "/visual/exportExcel.do";
        }
        model.addAttribute("expUrl", expUrl);

        // 自选视图
        boolean isExportWordViewSelect = false;
        int exportWordView = msd.getInt("export_word_view");
        if (exportWordView == ConstUtil.MODULE_EXPORT_WORD_VIEW_SELECT) {
            isExportWordViewSelect = true;
        }
        model.addAttribute("isExportWordViewSelect", isExportWordViewSelect);
        model.addAttribute("exportWordView", exportWordView);

        com.alibaba.fastjson.JSONArray aryBtnEvent = new com.alibaba.fastjson.JSONArray();
        if (btnNames!=null) {
            len = btnNames.length;
            for (int i = 0; i < len; i++) {
                if (!btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                    json.put("type", "script");
                    json.put("script", ModuleBackUtil.renderScript(request, btnScripts[i]));
                    aryBtnEvent.add(json);
                } else {
                    com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                    com.alibaba.fastjson.JSONObject jsonBtn = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if ("batchBtn".equals(jsonBtn.get("btnType"))) {
                        json.put("type", "batchBtn");
                        String batchField = jsonBtn.getString("batchField");
                        String batchValue = jsonBtn.getString("batchValue");
                        json.put("batchField", batchField);
                        json.put("batchValue", batchValue);
                        json.put("name", btnNames[i]);
                        aryBtnEvent.add(json);
                    }
                    else if ("flowBtn".equals(jsonBtn.get("btnType"))) {
                        json.put("type", "flowBtn");
                        String flowTypeCode = jsonBtn.getString("flowTypeCode");
                        Leaf lf = new Leaf();
                        lf = lf.getLeaf(flowTypeCode);
                        if (lf == null) {
                            DebugUtil.e(getClass(), "流程型按钮 flowTypeCode", flowTypeCode + " 不存在");
                            json.put("flowName", "");
                            json.put("flowTypeCode", flowTypeCode);
                        }
                        else {
                            json.put("flowName", lf.getName());
                            json.put("flowTypeCode", flowTypeCode);
                        }
                        aryBtnEvent.add(json);
                    }
                }
            }
        }
        model.addAttribute("aryBtnEvent", aryBtnEvent);

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        return "th/visual/module_list";
    }

    @RequestMapping(value = "/moduleAddPage")
    public String moduleAddPage(Model model) {
        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "code");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }
        if ("".equals(code)) {
            model.addAttribute("msg", i18nUtil.get(SkinUtil.ERR_ID));
            return "th/error/error";
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            model.addAttribute("msg", "模块不存在");
            return "th/error/error";
        }

        String formCode = msd.getString("form_code");
        if (formCode.equals("")) {
            model.addAttribute("msg", "编码不能为空");
            return "th/error/error";
        }

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserAppend(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));
        if (modUrlList.equals("")) {
            String privurl = ParamUtil.get(request, "privurl");
            if (privurl.equals("")) {
                modUrlList = request.getContextPath() + "/" + "visual/moduleListPage.do?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode);
            }
            else {
                modUrlList = privurl;
            }
        } else {
            modUrlList = request.getContextPath() + "/" + modUrlList;
        }

        request.setAttribute("modUrlList", modUrlList);

        // 置嵌套表需要用到的pageType
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_ADD);
        // 置NestSheetCtl需要用到的formCode
        request.setAttribute("formCode", formCode);

        if (fd == null || !fd.isLoaded()) {
            model.addAttribute("msg", "表单不存在");
            return "th/error/error";
        }

        model.addAttribute("formCode", formCode);
        model.addAttribute("code", code);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("privurl", StrUtil.UrlEncode(ParamUtil.get(request, "privurl")));
        model.addAttribute("nameTempCwsId", com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS);

        Map map = new HashMap();
        Enumeration reqParamNames = request.getParameterNames();
        while (reqParamNames.hasMoreElements()) {
            String paramName = (String) reqParamNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = ParamUtil.getParam(request, paramName);
                // 过滤掉formCode等
                if (paramName.equals("code")
                        || paramName.equals("formCode")
                        || paramName.equals("moduleCode")
                        || paramName.equals("mainCode")
                        || paramName.equals("menuItem")
                        || paramName.equals("parentId")
                        || paramName.equals("moduleCodeRelated")
                        || paramName.equals("formCodeRelated")
                        || paramName.equals("mode") // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
                        || paramName.equals("tagName")
                        || paramName.equals("id")
                ) {
                    ;
                }
                else {
                    map.put(paramName, paramValue);
                }
            }
        }
        model.addAttribute("map", map);
        model.addAttribute("isHasAttachment", fd.isHasAttachment());

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
        model.addAttribute("rend", rd.rendForAdd(msd));

        com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_ADD, null, 1);
        model.addAttribute("buttons", buttons);

        model.addAttribute("pageCss", StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_ADD)));
        model.addAttribute("pageType", ConstUtil.PAGE_TYPE_ADD);
        model.addAttribute("random", Math.random());

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            // 在renderTabs中将reqParams置于了request中
            model.addAttribute("reqParams", request.getAttribute("reqParams"));
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        boolean isPageStyleLight = false;
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
            isPageStyleLight = true;
        }
        model.addAttribute("isPageStyleLight", isPageStyleLight);

        return "th/visual/module_add.html";
    }

    @RequestMapping(value = "/moduleAttListPage")
    public String moduleAttListPage(Long id, Model model) {
        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "code");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }

        boolean isShowPage = ParamUtil.getBoolean(request, "isShowPage", false);

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!isShowPage) {
            if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
                model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
                return "th/error/error";
            }
        }

        if (!ModulePrivMgr.canAccessData(request, msd, id)) {
            model.addAttribute("msg", i18nUtil.get("info_access_data_fail"));
            return "th/error/error";
        }

        String formCode = msd.getString("form_code");

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);

        Vector vAttach = fdao.getAttachments();
        model.addAttribute("hasAttach", vAttach.size()>0);
        model.addAttribute("vAttach", vAttach);
        model.addAttribute("canUserLog", mpd.canUserLog(authUtil.getUserName()));
        model.addAttribute("isShowPage", isShowPage);

        return "th/visual/module_att_list";
    }

    @RequestMapping(value = "/moduleEditPage")
    public String moduleEditPage(Model model) {
        Privilege privilege = new Privilege();

        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "code");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }
        if ("".equals(code)) {
            model.addAttribute("msg", i18nUtil.get(SkinUtil.ERR_ID));
            return "th/error/error";
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd==null) {
            model.addAttribute("msg", "模块不存在");
            return "th/error/error";
        }

        if (!msd.isEditPageTabStyleHor()) {
            return "forward:module_edit_v.jsp";
        }

        String op = ParamUtil.get(request, "op");
        int parentId = ParamUtil.getInt(request, "parentId", -1);
        int id = ParamUtil.getInt(request, "id", -1);
        String formCode = msd.getString("form_code");

        if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
            return "redirect:" + request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + parentId + "&id=" + id + "&code=" + code + "&formCode=" + formCode;
        }

        if ("".equals(formCode)) {
            model.addAttribute("msg", "表单编码不能为空");
            return "th/error/error";
        }

        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        if (!ModulePrivMgr.canAccessData(request, msd, id)) {
            model.addAttribute("msg", i18nUtil.get("info_access_data_fail"));
            return "th/error/error";
        }

        boolean canUserData = mpd.canUserData(privilege.getUser(request));
        model.addAttribute("canUserData", canUserData);

        request.setAttribute("moduleCode", code);

        // 置嵌套表需要用到的cwsId
        request.setAttribute("cwsId", "" + id);
        // 置嵌套表需要用到的页面类型
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_EDIT);
        // 置NestSheetCtl需要用到的formCode
        request.setAttribute("formCode", formCode);

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
        if (!fdao.isLoaded()) {
            model.addAttribute("msg", "记录不存在");
            return "th/error/error";
        }

        model.addAttribute("formCode", formCode);
        model.addAttribute("code", code);
        model.addAttribute("id", id);
        model.addAttribute("parentId", parentId);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("isHasAttachment", fd.isHasAttachment());
        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);
        model.addAttribute("isNav", isNav);

        StringBuffer requestParamBuf = new StringBuffer();
        Enumeration reqParamNames = request.getParameterNames();
        while (reqParamNames.hasMoreElements()) {
            String paramName = (String) reqParamNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = ParamUtil.getParam(request, paramName);
                // 过滤掉formCode等
                if ("id".equals(paramName)) {
                    ;
                }
                else {
                    StrUtil.concat(requestParamBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
                }
            }
        }
        model.addAttribute("requestParams", requestParamBuf.toString());

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
        model.addAttribute("rend", rd.rend(msd));

        com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_EDIT, fdao, isNav?1:0);
        model.addAttribute("buttons", buttons);

        model.addAttribute("pageCss", StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_EDIT)));
        model.addAttribute("pageType", ConstUtil.PAGE_TYPE_EDIT);

        model.addAttribute("random", Math.random());

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        boolean isPageStyleLight = false;
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
            isPageStyleLight = true;
        }
        model.addAttribute("isPageStyleLight", isPageStyleLight);

        model.addAttribute("isReloadAfterUpdate", msd.isReloadAfterUpdate());
        model.addAttribute("tabIdOpener", ParamUtil.get(request, "tabIdOpener"));

        return "th/visual/module_edit";
    }

    @RequestMapping(value = "/moduleShowPage")
    public String moduleShowPage(Model model) {
        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "code");
            if ("".equals(code)) {
                code = ParamUtil.get(request, "formCode");
            }
        }
        if ("".equals(code)) {
            model.addAttribute("msg", i18nUtil.get(SkinUtil.ERR_ID));
            return "th/error/error/";
        }
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            model.addAttribute("msg", "模块不存在！");
            return "th/error/error/";
        }

        long id = ParamUtil.getLong(request, "id", -1);
        if (id == -1) {
            model.addAttribute("msg", i18nUtil.get(SkinUtil.ERR_ID));
            return "th/error/error/";
        }

        Privilege privilege = new Privilege();
        boolean canUserView = true;
        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserView(privilege.getUser(request))) {
            canUserView = false;
            // 原嵌套表访问时带入的visitKey，现已改为通过Security.makeVisitKey生成
			/*boolean canShow = false;
			if (!"".equals(visitKey)) {
				String fId = String.valueOf(id);
				com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
				String desKey = ssoconfig.get("key");
				visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
				if (visitKey.equals(fId)) {
					canShow = true;
				}
			}
			if (!canShow) {
				...
			}*/
            // out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            // return;
        }

        // @task: 其实应在有权限的同时，还得有visitKey，以免超出其数据权限范围
        if (!canUserView) {
            String visitKey = ParamUtil.get(request, "visitKey");
            if (!"".equals(visitKey)) {
                int r = SecurityUtil.validateVisitKey(visitKey, String.valueOf(id));
                if (r != 1) {
                    model.addAttribute("msg", SecurityUtil.getValidateVisitKeyErrMsg(r));
                    return "th/error/error/";
                }
            }
            else {
                model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
                return "th/error/error/";
            }
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        // 20211215去掉，耗时且当模块过滤条件中含有request传入的动态参数，会通过不了
		/*if (!ModulePrivMgr.canAccessData(request, msd, id)) {
			I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, i18nUtil.get("info_access_data_fail")));
			return;
		}*/

        String formCode = msd.getString("form_code");
        if ("".equals(formCode)) {
            model.addAttribute("msg", "编码不能为空");
            return "th/error/error/";
        }

        if (!msd.isShowPageTabStyleHor()) {
            return "forward:module_show_v.jsp";
        }

        if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_TREE) {
            boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
            if (!isInFrame) {
                return "redirect:" + request.getContextPath() + "/" + "visual/module_show_frame.jsp?id=" + id + "&code=" + code;
            }
        }

        String userName = privilege.getUser(request);
        Config cfg = Config.getInstance();
        // 创建浏览日志
        if (cfg.getBooleanProperty("isModuleLogRead") && !formCode.equals(ConstUtil.MODULE_CODE_LOG) && !formCode.equals(ConstUtil.MODULE_CODE_LOG_READ) && !formCode.equals(ConstUtil.FORM_FORMULA)) {
            ModuleLogService moduleLogService = SpringUtil.getBean(ModuleLogService.class);
            moduleLogService.logRead(formCode, code, id, userName, privilege.getUserUnitCode(request));
        }

        request.setAttribute("moduleCode", code);

        // 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
        request.setAttribute("cwsId", String.valueOf(id));
        // 置嵌套表需要用到的页面类型
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_SHOW);
        // 置NestSheetCtl需要用到的formCode
        request.setAttribute("formCode", formCode);

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);

        model.addAttribute("id", id);
        model.addAttribute("code", code);
        model.addAttribute("isNav", isNav);
        model.addAttribute("isHasAttachment", fd.isHasAttachment());
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
        if (!fdao.isLoaded()) {
            model.addAttribute("msg", "记录不存在");
            return "th/error/error/";
        }

        // 置为已读
        if (!fdao.isCwsVisited()) {
            fdao.setCwsVisited(true);
            try {
                fdao.save();
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        fdm.runScriptOnSee(request, privilege, msd, fdao);

        Vector<IAttachment> vAttach = fdao.getAttachments();

        model.addAttribute("vAttach", vAttach);
        model.addAttribute("canUserLog", mpd.canUserLog(privilege.getUser(request)));

        model.addAttribute("btn_print_display", msd.getInt("btn_print_display") == 1);
        // request.setAttribute("btn_edit_display", msd.getInt("btn_edit_display") == 1 && (mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request))));
        model.addAttribute("btn_edit_display", mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request)));
        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
        model.addAttribute("rend", rd.report(msd));

        com.alibaba.fastjson.JSONArray buttons = msd.getButtons(request, ConstUtil.PAGE_TYPE_SHOW, fdao, isNav?1:0);
        model.addAttribute("buttons", buttons);

        model.addAttribute("pageCss", msd.getCss(ConstUtil.PAGE_TYPE_SHOW));

        model.addAttribute("random", Math.random());

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);
        boolean isPageStyleLight = false;
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
            isPageStyleLight = true;
        }
        model.addAttribute("isPageStyleLight", isPageStyleLight);

        return "th/visual/module_show";
    }

    @RequestMapping(value = "/moduleListRelatePage")
    public String moduleListRelatePage(Model model) {
        String formCode = ParamUtil.get(request, "formCode");
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        String menuItem = ParamUtil.get(request, "menuItem");
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
        formCode = parentMsd.getString("form_code");

        String mode = ParamUtil.get(request, "mode");
        String tagName = ParamUtil.get(request, "tagName");
        long parentId = ParamUtil.getLong(request, "parentId", -1);

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        String formCodeRelated = msd.getString("form_code");
        boolean isEditInplace = msd.getInt("is_edit_inplace")==1;
        boolean isAutoHeight = msd.getInt("is_auto_height")==1;

        // 通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        if (isSubTagRelated) {
            String tagUrl = ModuleBackUtil.getModuleSubTagUrl(moduleCode, tagName);
            try {
                JSONObject json = new JSONObject(tagUrl);
                if (json.has("viewList")) {
                    int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
                    if (viewList==ModuleSetupDb.VIEW_LIST_GANTT) {
                        return "redirect:module_list_relate_gantt.jsp?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName) + "&parentId=" + parentId + "&code=" + formCode + "&menuItem=" + menuItem;
                    }
                    else if (viewList==ModuleSetupDb.VIEW_LIST_CALENDAR) {
                        return "redirect:module_list_relate_calendar.jsp?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName) + "&parentId=" + parentId + "&code=" + formCode + "&menuItem=" + menuItem;
                    }
                }
                if (!json.isNull("formRelated")) {
                    // formCodeRelated = json.getString("formRelated");
                    moduleCodeRelated = json.getString("formRelated");
                    msd = msd.getModuleSetupDb(moduleCodeRelated);
                    if (msd == null) {
                        model.addAttribute("msg", "关联模块：" + moduleCodeRelated + " 不存在");
                        return "th/error/error";
                    }
                    formCodeRelated = msd.getString("form_code");
                    isEditInplace = msd.getInt("is_edit_inplace")==1;
                }
                else {
                    model.addAttribute("msg", "选项卡关联配置不正确");
                    return "th/error/error";
                }
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        // 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
        request.setAttribute("cwsId", "" + parentId);
        // 用于传过滤条件
        request.setAttribute(ModuleBackUtil.MODULE_SETUP, msd);

        FormDb fd = new FormDb();

        model.addAttribute("moduleName", msd.getName());
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("pageCss", msd.getCss(ConstUtil.PAGE_TYPE_LIST));

        StringBuffer params = new StringBuffer();
        com.alibaba.fastjson.JSONArray aryParam = new com.alibaba.fastjson.JSONArray();
        Enumeration enu = request.getParameterNames();
        while(enu.hasMoreElements()) {
            String paramName = (String) enu.nextElement();
            if ("code".equals(paramName) || "formCode".equals(paramName)) {
                continue;
            }
            String paramVal = ParamUtil.get(request, paramName);
            StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramVal));
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("paramName", paramName);
            json.put("paramVal", paramVal);
            aryParam.add(json);
        }
        model.addAttribute("params", params);

        model.addAttribute("formCodeRelated", formCodeRelated);
        model.addAttribute("parentId", parentId);
        model.addAttribute("formCode", formCode);
        model.addAttribute("formCodeRelated", formCodeRelated);
        model.addAttribute("moduleCodeRelated", moduleCodeRelated);
        model.addAttribute("pageType", ConstUtil.PAGE_TYPE_LIST_RELATE);
        model.addAttribute("random", Math.random());

        fd = fd.getFormDb(formCodeRelated);
        if (!fd.isLoaded()) {
            model.addAttribute("msg", "表单不存在");
            return "th/error/error/";
        }

        // 置页面类型
        request.setAttribute("pageType", ConstUtil.PAGE_TYPE_LIST_RELATE);

        String relateFieldValue = "";
        if (parentId==-1) {
            model.addAttribute("msg", "缺少父模块记录的ID");
            return "th/error/error/";
        }
        else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue==null) {
                    // 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                    // out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
                    // return;
                }
            }
        }

        Privilege privilege = new Privilege();
        String op = ParamUtil.get(request, "op");
        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserSee(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error/";
        }

        String parentPageType = ParamUtil.get(request, "parentPageType");
        ModuleRelateDb mrd = new ModuleRelateDb();
        Iterator ir = mrd.getModulesRelated(moduleCode).iterator();
        while (ir.hasNext()) {
            mrd = (ModuleRelateDb)ir.next();
            String code = mrd.getString("relate_code");
            if (code.equals(formCodeRelated)) {
                if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
                    // 获取与formCode关联的表单型（单条记录）的ID
                    com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                    try {
                        fdao = fdao.getFormDAOOfRelate(fd, relateFieldValue);
                        if (fdao==null) {
                            fdao = new com.redmoon.oa.visual.FormDAO(fd);
                            fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis()));
                            fdao.setCwsId(relateFieldValue); // 关联的模块的ID
                            fdao.setCreator(privilege.getUser(request)); // 参数为用户名（创建记录者），必填
                            fdao.setUnitCode(privilege.getUserUnitCode(request)); // 置单位编码，必填
                            fdao.setCwsParentForm(formCode); // 如为嵌套表，则置主表单的编码，且必填，否则选填
                            fdao.create();
                        }
                    } catch (ErrMsgException e) {
                        LogUtil.getLog(getClass()).error(e);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    long id = fdao.getId();
                    if ("edit".equals(parentPageType)) {
                        return "redirect:moduleEditRelatePage.do?menuItem=" + menuItem + "&id=" + id + "&parentId=" + parentId + "&moduleCodeRelated=" + moduleCodeRelated + "&code=" + moduleCode + "&isNav=1";
                    }
                    else {
                        return "redirect:moduleShowRelatePage.do?menuItem=" + menuItem + "&id=" + id + "&parentId=" + parentId + "&moduleCodeRelated=" + moduleCodeRelated + "&code=" + moduleCode;
                    }
                }
            }
        }

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        String userName = privilege.getUser(request);
        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(orderBy)) {
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

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        int defaultPageSize = cfg.getInt("modulePageSize");
        int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);

        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);
        model.addAttribute("isNav", isNav);
        model.addAttribute("pageSize", pagesize);

        String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
        String sqlUrlStr = arySQL[1];

        String querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + moduleCode + "&menuItem=" + menuItem + "&formCode=" + formCode + "&moduleCodeRelated=" + moduleCodeRelated + "&formCodeRelated=" + moduleCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isNav=" + isNav;
        if (!"".equals(sqlUrlStr)) {
            if (!sqlUrlStr.startsWith("&")) {
                querystr += "&" + sqlUrlStr;
            }
            else {
                querystr += sqlUrlStr;
            }
        }

        model.addAttribute("mode", mode);
        model.addAttribute("parentPageType", parentPageType);
        model.addAttribute("isAutoHeight", isAutoHeight);
        model.addAttribute("isEditInplace", isEditInplace);
        // 可在位编辑的字段相关属性
        com.alibaba.fastjson.JSONArray aryEditable = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONArray aryEditableOpt = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONArray aryChkPresent = new com.alibaba.fastjson.JSONArray();
        if (isEditInplace) {
            moduleBackService.getEditInplaceProps(aryEditable, aryEditableOpt, aryChkPresent, mpd, fd);
        }
        model.addAttribute("aryEditable", aryEditable);
        model.addAttribute("aryEditableOpt", aryEditableOpt);
        model.addAttribute("aryChkPresent", aryChkPresent);

        model.addAttribute("orderBy", orderBy);
        model.addAttribute("sort", sort);
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("moduleCode", moduleCode);

        // 将过滤配置中request中其它参数也传至url中，这样分页时可传入参数
        String requestParams = "";
        String requestParamInputs = "";

        Map map = ModuleBackUtil.getFilterParams(request, msd);
        Iterator irMap = map.keySet().iterator();
        while (irMap.hasNext()) {
            String key = (String)irMap.next();
            String val = (String)map.get(key);
            requestParams += "&" + key + "=" + val;
            requestParamInputs += "<input type='hidden' name='" + key + "' value='" + val + "' />";
        }
        querystr += requestParams;

        // 加上二开传入的参数
        querystr += "&" + params.toString();
        model.addAttribute("queryStr", querystr);

        String[] fields = msd.getColAry(false, "list_field");
        String[] fieldsWidth = msd.getColAry(false, "list_field_width");
        String[] fieldsShow = msd.getColAry(false, "list_field_show");
        String[] fieldsTitle = msd.getColAry(false, "list_field_title");
        String[] fieldsAlign = msd.getColAry(false, "list_field_align");

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
        String[] btnBclasses = StrUtil.split(btnBclass, ",");
        String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
        String[] btnRoles = StrUtil.split(btnRole, "#");

        boolean canView = mpd.canUserView(userName);
        boolean canLog = mpd.canUserLog(userName);
        boolean canManage = mpd.canUserManage(userName);

        com.alibaba.fastjson.JSONArray colProps = new com.alibaba.fastjson.JSONArray();
        try {
            colProps = ModuleBackUtil.getColProps(msd, false);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("colProps", colProps);

        boolean isButtonsShow = false;
        isButtonsShow = mpd.canUserAppend(privilege.getUser(request)) ||
                mpd.canUserModify(privilege.getUser(request)) ||
                mpd.canUserManage(privilege.getUser(request)) ||
                mpd.canUserImport(privilege.getUser(request)) ||
                mpd.canUserExport(privilege.getUser(request)) ||
                btnNames!=null;
        model.addAttribute("isButtonsShow", isButtonsShow);

        boolean isToolbar = true;
        if (btnNames!=null) {
            int len = btnNames.length;
            for (int i=0; i<len; i++) {
                if (btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if (json.get("btnType").equals("queryFields")) {
                        if (json.containsKey("isToolbar")) {
                            isToolbar = json.getIntValue("isToolbar")==1;
                        }
                        break;
                    }
                }
            }
        }
        model.addAttribute("isToolbar", isToolbar);

        ArrayList<String> dateFieldNamelist = new ArrayList<String>();
        String condsHtml = ModuleBackUtil.getConditionHtml(request, msd, dateFieldNamelist);
        boolean isQuery = !"".equals(condsHtml);
        model.addAttribute("condsHtml", condsHtml);
        model.addAttribute("isQuery", isQuery);
        model.addAttribute("requestParamInputs", requestParamInputs);

        boolean isBtnAddShow = msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(userName);
        boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName);
        boolean isBtnDelShow = msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage);
        boolean isBtnImportShow = mpd.canUserImport(userName);
        boolean isBtnExportShow = mpd.canUserExport(userName);
        boolean isBtnExportWordShow = mpd.canUserExportWord(userName);
        model.addAttribute("isBtnAddShow", isBtnAddShow);
        model.addAttribute("isBtnEditShow", isBtnEditShow);
        model.addAttribute("isBtnDelShow", isBtnDelShow);
        model.addAttribute("isBtnImportShow", isBtnImportShow);
        model.addAttribute("isBtnExportShow", isBtnExportShow);
        model.addAttribute("isBtnExportWordShow", isBtnExportWordShow);

        com.alibaba.fastjson.JSONArray aryBtn = new com.alibaba.fastjson.JSONArray();
        if (btnNames != null && btnBclasses != null) {
            int len = btnNames.length;
            for (int i = 0; i < len; i++) {
                boolean isToolBtn = false;
                if (!btnScripts[i].startsWith("{")) {
                    isToolBtn = true;
                } else {
                    com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    String btnType = json.getString("btnType");
                    if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
                        isToolBtn = true;
                    }
                }
                if (isToolBtn) {
                    // 检查是否拥有权限
                    if (!privilege.isUserPrivValid(request, "admin")) {
                        boolean canSeeBtn = false;
                        if (btnRoles != null && btnRoles.length > 0) {
                            String roles = btnRoles[i];
                            String[] codeAry = StrUtil.split(roles, ",");
                            // 如果codeAry为null，则表示所有人都能看到
                            if (codeAry == null) {
                                canSeeBtn = true;
                            } else {
                                UserDb user = new UserDb();
                                user = user.getUserDb(privilege.getUser(request));
                                RoleDb[] rdAry = user.getRoles();
                                if (rdAry != null) {
                                    for (RoleDb roleDb : rdAry) {
                                        String roleCode = roleDb.getCode();
                                        for (String codeAllowed : codeAry) {
                                            if (roleCode.equals(codeAllowed)) {
                                                canSeeBtn = true;
                                                break;
                                            }
                                        }
                                        if (canSeeBtn) {
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            canSeeBtn = true;
                        }

                        if (!canSeeBtn) {
                            continue;
                        }

                        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                        json.put("name", btnNames[i]);
                        json.put("class", btnBclasses[i]);
                        aryBtn.add(json);
                    }
                }
            }
        }
        model.addAttribute("aryBtn", aryBtn);
        model.addAttribute("isAdmin", privilege.isUserPrivValid(request, "admin"));

        // 检查是否设置有模板
        Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msd.getString("form_code"));
        String expUrl = "";
        // 检查是否设置有模板
        if (vt.size()>0) {
            expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp?mode=" + mode + "&isRelate=true";
        }
        else {
            expUrl = request.getContextPath() + "/visual/exportExcelRelate.do";
        }
        model.addAttribute("expUrl", expUrl);

        com.alibaba.fastjson.JSONArray aryBtnEvent = new com.alibaba.fastjson.JSONArray();
        if (btnNames!=null) {
            int len = btnNames.length;
            for (int i = 0; i < len; i++) {
                if (!btnScripts[i].startsWith("{")) {
                    com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                    json.put("type", "script");
                    json.put("script", ModuleBackUtil.renderScript(request, btnScripts[i]));
                    aryBtnEvent.add(json);
                } else {
                    com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
                    com.alibaba.fastjson.JSONObject jsonBtn = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
                    if ((jsonBtn.get("btnType")).equals("batchBtn")) {
                        json.put("type", "batchBtn");
                        String batchField = jsonBtn.getString("batchField");
                        String batchValue = jsonBtn.getString("batchValue");
                        json.put("batchField", batchField);
                        json.put("batchValue", batchValue);
                        json.put("name", btnNames[i]);
                        aryBtnEvent.add(json);
                    }
                    else if ("flowBtn".equals(json.get("btnType"))) {
                        json.put("type", "flowBtn");
                        String flowTypeCode = json.getString("flowTypeCode");
                        Leaf lf = new Leaf();
                        lf = lf.getLeaf(flowTypeCode);
                        if (lf == null) {
                            DebugUtil.e(getClass(), "流程型按钮 flowTypeCode", flowTypeCode + " 不存在");
                        }
                        else {
                            json.put("flowName", lf.getName());
                            json.put("flowTypeCode", flowTypeCode);
                        }
                        aryBtnEvent.add(json);
                    }
                }
            }
        }
        model.addAttribute("aryBtnEvent", aryBtnEvent);
        model.addAttribute("op", op);

        return "th/visual/module_list_relate";
    }

    @RequestMapping(value = "/moduleAddRelatePage")
    public String moduleAddRelatePage(Model model) {
        String formCode = ParamUtil.get(request, "formCode");
        if ("".equals(formCode)) {
            model.addAttribute("msg", i18nUtil.get("err_id"));
            return "th/error/error";
        }

        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        ModuleSetupDb msdRelated = new ModuleSetupDb();
        msdRelated = msdRelated.getModuleSetupDb(moduleCodeRelated);
        String formCodeRelated = msdRelated.getString("form_code");

        Privilege privilege = new Privilege();
        String menuItem = ParamUtil.get(request, "menuItem");
        try {
            com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "menuItem", menuItem, getClass().getName());
        } catch (ErrMsgException e) {
            model.addAttribute("msg", e.getMessage());
            return "th/error/error";
        }

        String moduleCode = ParamUtil.get(request, "code");

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        if (fd == null || !fd.isLoaded()) {
            model.addAttribute("msg", "表单不存在");
            return "th/error/error";
        }

        String relateFieldValue = "";
        long parentId = ParamUtil.getLong(request, "parentId", -1); // 父模块的ID
        if (parentId == -1) {
            model.addAttribute("msg", "缺少父模块记录的ID");
            return "th/error/error";
        }

        String parentPageType = ParamUtil.get(request, "parentPageType");
        boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
        request.setAttribute("isTabStyleHor", isTabStyleHor);

        // 用于表单域选择窗体宏控件及查询选择宏控件
        request.setAttribute("formCodeRelated", formCodeRelated);
        // 置嵌套表需要用到的pageType
        request.setAttribute("pageType", "add");

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);

        if (!mpd.canUserAppend(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        model.addAttribute("menuItem", menuItem);
        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);
        model.addAttribute("isNav", isNav);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("formCode", formCode);
        model.addAttribute("formCodeRelated", formCodeRelated);
        model.addAttribute("parentId", parentId);
        model.addAttribute("formCodeRelated", formCodeRelated);
        model.addAttribute("moduleCodeRelated", moduleCodeRelated);
        model.addAttribute("random", Math.random());
        model.addAttribute("parentPageType", parentPageType);
        model.addAttribute("moduleCode", moduleCode);

        com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(formCode);
        relateFieldValue = fdmMain.getRelateFieldValue(parentId, moduleCodeRelated);
        if (relateFieldValue == null) {
            model.addAttribute("msg", "请检查模块是否相关联");
            return "th/error/error";
        }

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
        model.addAttribute("rend", rd.rendForAdd());

        model.addAttribute("isHasAttachment", fd.isHasAttachment());
        model.addAttribute("relateFieldValue", relateFieldValue);
        model.addAttribute("isTabStyleHor", isTabStyleHor);
        model.addAttribute("isPageStyleLight", msdRelated.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT);
        model.addAttribute("NAME_TEMP_CWS_IDS", com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS);

        return "th/visual/module_add_relate";
    }

    @RequestMapping(value = "/moduleEditRelatePage")
    public String moduleEditRelatePage(Model model) {
        // 取从模块编码
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        if ("".equals(moduleCodeRelated)) {
            model.addAttribute("msg", "缺少关联模块编码");
            return "th/error/error";
        }
        String menuItem = ParamUtil.get(request, "menuItem");
        // 取主模块编码
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            model.addAttribute("msg", "模块不存在");
            return "th/error/error";
        }
        String formCodeRelated = msd.getString("form_code");
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserModify(privilege.getUser(request))) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        int id = ParamUtil.getInt(request, "id", -1);
        if (id==-1) {
            model.addAttribute("msg", i18nUtil.get("err_id"));
            return "th/error/error";
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(moduleCode);
        if (parentMsd==null) {
            model.addAttribute("msg", "父模块不存在");
            return "th/error/error";
        }
        String parentFormCode = parentMsd.getString("form_code");
        String mode = ParamUtil.get(request, "mode");
        // 是否通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId==-1) {
            model.addAttribute("msg", "缺少父模块记录的ID");
            return "th/error/error";
        }
        else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue==null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }
        if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            model.addAttribute("msg", i18nUtil.get("info_access_data_fail"));
            return "th/error/error";
        }

        boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
        model.addAttribute("isTabStyleHor", isTabStyleHor);

        model.addAttribute("moduleCode", moduleCode);

        // 置嵌套表需要用到的cwsId
        request.setAttribute("cwsId", "" + id);
        // 置页面类型
        request.setAttribute("pageType", "edit");
        // 用于表单域选择窗体宏控件
        request.setAttribute("formCode", formCodeRelated); // 这里是为了使嵌套表在getNestSheet方法中，传递给当前编辑的表单中的嵌套表

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        model.addAttribute("isNav", ParamUtil.getBoolean(request, "isNav", true));
        model.addAttribute("formCodeRelated", formCodeRelated);
        String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
        model.addAttribute("tabIdOpener", tabIdOpener);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("parentId", parentId);
        model.addAttribute("id", id);
        model.addAttribute("parentFormCode", parentFormCode);
        model.addAttribute("moduleCodeRelated", moduleCodeRelated);
        model.addAttribute("random", Math.random());
        model.addAttribute("menuItem", menuItem);

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
        model.addAttribute("rend", rd.rend(msd));
        model.addAttribute("isHasAttachment", fd.isHasAttachment());
        model.addAttribute("relateFieldValue", relateFieldValue);

        boolean isPageStyleLight = false;
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
            isPageStyleLight = true;
        }
        model.addAttribute("isPageStyleLight", isPageStyleLight);
        model.addAttribute("mode", mode);

        return "th/visual/module_edit_relate";
    }

    @RequestMapping(value = "/moduleShowRelatePage")
    public String moduleShowRelatePage(Model model) {
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCodeRelated);
        if (msd == null) {
            model.addAttribute("msg", "模块不存在");
            return "th/error/error";
        }

        long parentId = ParamUtil.getLong(request, "parentId", -1);
        if (parentId==-1) {
            model.addAttribute("msg", "缺少父模块ID");
            return "th/error/error";
        }
        int id = ParamUtil.getInt(request, "id", -1);
        if (id==-1) {
            model.addAttribute("msg", i18nUtil.get("err_id"));
            return "th/error/error";
        }

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserView(privilege.getUser(request))) {
            boolean canShow = false;
            // 从嵌套表格查看时访问
            String visitKey = ParamUtil.get(request, "visitKey");
            if (!"".equals(visitKey)) {
                String fId = String.valueOf(id);
                com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
                String desKey = ssoconfig.get("key");
                visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
                if (visitKey.equals(fId)) {
                    canShow = true;
                }
            }
            if (!canShow) {
                model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
                return "th/error/error";
            }
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        String code = ParamUtil.get(request, "code");
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(code);
        if (parentMsd==null) {
            model.addAttribute("msg", "父模块不存在");
            return "th/error/error";
        }
        String parentFormCode = parentMsd.getString("form_code");

        String mode = ParamUtil.get(request, "mode");
        // 通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        if (parentId==-1) {
            model.addAttribute("msg", "缺少父模块记录的ID");
            return "th/error/error";
        }
        else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue==null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }
        if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            model.addAttribute("msg", i18nUtil.get("info_access_data_fail"));
            return "th/error/error";
        }

        String formCode = msd.getString("form_code");
        if (formCode.equals("")) {
            model.addAttribute("msg", "编码不能为空");
            return "th/error/error";
        }

        if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_TREE) {
            boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
            if (!isInFrame) {
                return "redirect:module_show_frame.jsp?id=" + id + "&code=" + code;
            }
        }

        Config cfg = Config.getInstance();
        // 创建浏览日志
        if (cfg.getBooleanProperty("isModuleLogRead") && !formCode.equals("module_log") && !formCode.equals("module_log_read") && !formCode.equals("formula")) {
            FormDb fdModule = new FormDb(formCode);
            FormDb fd = new FormDb("module_log_read");
            FormDAO fdao = new FormDAO(fd);
            fdao.setFieldValue("read_type", String.valueOf(FormDAOLog.READ_TYPE_MODULE));
            fdao.setFieldValue("log_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            fdao.setFieldValue("module_code", code);
            fdao.setFieldValue("form_code", formCode);
            fdao.setFieldValue("module_id", String.valueOf(id));
            fdao.setFieldValue("form_name", fdModule.getName());
            fdao.setFieldValue("user_name", privilege.getUser(request));
            fdao.setCreator(privilege.getUser(request)); // 参数为用户名（创建记录者）
            fdao.setUnitCode(privilege.getUserUnitCode(request)); // 置单位编码
            fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis())); // 置冗余字段“流程编码”，可用于取出刚插入的记录，也可以为空
            try {
                fdao.create();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
        request.setAttribute("cwsId", "" + id);
        // 置嵌套表需要用到的页面类型
        request.setAttribute("pageType", "show");
        // 置NestSheetCtl需要用到的formCode
        request.setAttribute("formCode", formCode);

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);

        model.addAttribute("moduleCode", code);
        model.addAttribute("id", id);
        model.addAttribute("parentId", parentId);
        model.addAttribute("code", code);
        model.addAttribute("formCode", formCode);
        model.addAttribute("isNav", isNav);
        model.addAttribute("isHasAttachment", fd.isHasAttachment());
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("moduleCodeRelated", moduleCodeRelated);
        model.addAttribute("random", Math.random());
        model.addAttribute("pageCss", msd.getCss(ConstUtil.PAGE_TYPE_SHOW));

        model.addAttribute("btn_print_display", msd.getInt("btn_print_display")==1);
        model.addAttribute("btn_edit_display", msd.getInt("btn_edit_display") == 1 && (mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request))));

        com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
        model.addAttribute("rend", rd.report(msd));

        boolean hasTab = false;
        com.alibaba.fastjson.JSONArray aryTab = null;
        try {
            aryTab = ModuleBackUtil.renderTabs(request);
            if (aryTab.size() > 1) {
                hasTab = true;
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        model.addAttribute("hasTab", hasTab);
        model.addAttribute("aryTab", aryTab);

        String menuItem = ParamUtil.get(request, "menuItem");
        model.addAttribute("menuItem", menuItem);

        boolean isPageStyleLight = false;
        if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
            isPageStyleLight = true;
        }
        model.addAttribute("isPageStyleLight", isPageStyleLight);
        return "th/visual/module_show_relate";
    }
}