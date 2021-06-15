package com.redmoon.oa.flow.macroctl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import javax.servlet.ServletContext;
import java.io.IOException;

import com.redmoon.kit.util.FileInfo;
import com.redmoon.oa.person.UserDb;
import jxl.read.biff.BiffException;
import jxl.read.biff.WorkbookParser;
import jxl.Workbook;
import jxl.Cell;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.*;

import java.io.File;

import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: 嵌套表格，对应于nest_table_view.jsp，注意一个表单中不能同时出现两个嵌套表格，会致JS及<div id="dragDiv" style="display:none"></div>重复出现</p>
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
public class NestTableCtl extends AbstractMacroCtl {
    JSONArray cellJsonArray;

    public JSONArray getCellJsonArray() {
        return cellJsonArray;
    }

    public NestTableCtl() {
        super();
    }

    /**
     * 通过NetUtil.gather方法取得控件的HTML代码
     *
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    public String getNestTable(HttpServletRequest request, FormField ff) {
        String op = "add";
        String cwsId = (String) request.getAttribute("cwsId");
        long workflowActionId = StrUtil.toLong((String) request.getAttribute("workflowActionId"), -1);

        long mainId = -1;
        // 数据库中为空
        if (cwsId != null) {
            String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
            if (pageType.equals("show")) { // module_show.jsp
                op = "show&cwsId=" + cwsId;
            } else if ("flowShow".equals(pageType)) { // flow_modify.jsp
                int flowId = StrUtil.toInt(cwsId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wf.getTypeCode());

                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(lf.getFormCode());
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO(flowId, flowFd);
                long fdaoId = fdao.getId();
                op = "show&cwsId=" + fdaoId;
            } else if (pageType.equals("archive")) {
                op = "show&action=archive&cwsId=" + cwsId;
            } else if ("flow".equals(pageType)) { // flow_dispose.jsp中pageType=flow
                int flowId = StrUtil.toInt(cwsId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(flowId);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wf.getTypeCode());

                FormDb flowFd = new FormDb();
                flowFd = flowFd.getFormDb(lf.getFormCode());
                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                fdao = fdao.getFormDAO(flowId, flowFd);
                long fdaoId = fdao.getId();
                mainId = fdao.getId();

                if (ff.isEditable()) {
                    op = "edit&cwsId=" + fdaoId + "&actionId=" + workflowActionId + "&mainId=" + fdaoId;
                }
                else {
                    op = "show&cwsId=" + fdaoId + "&actionId=" + workflowActionId + "&mainId=" + fdaoId;
                }
            } else { // module_edit.jsp中pageType=edit
                op = "edit&cwsId=" + cwsId;
            }
            if (workflowActionId != -1) {
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb((int) workflowActionId);
                // 审阅
                if (wad.getKind() == WorkflowActionDb.KIND_READ) {
                    op = "show&cwsId=" + cwsId;
                }
            }
        }

        if (ff.isEditable()) {
            // 主表中嵌套表字段不可写，则不允许添加、删除行，只可以根据权限编辑
            op += "&isNestFormFieldEditable=true";
        }

        // 将request中其它参数也传至url中
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                // 过滤掉formCode
                if (paramName.equals("formCode")) {
                    op += "&parentFormCode=" + StrUtil.UrlEncode(paramValue);
                } else {
                    op += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
                }
            }
        }

        // 获取父页面中处理的formCode
        String parentFormCode = StrUtil.getNullStr((String) request.getAttribute("formCode"));
        if (!op.contains("parentFormCode=")) {
            // 当在流程的flow_dispose.jsp页面处理时，传递formCode，以便于导出嵌套表中的内容至EXCEL
            op += "&parentFormCode=" + parentFormCode;
        }

        int isTab = 0;
        String nestFieldName = ff.getName();
        String nestFormCode = ff.getDescription();
        try {
            // 20131123 fgf 添加
            String defaultVal = StrUtil.decodeJSON(ff.getDescription());
            JSONObject json = new JSONObject(defaultVal);
            nestFormCode = json.getString("destForm");
            if (json.has("isTab")) {
                isTab = json.getInt("isTab");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));

        String url = "/visual/nest_table_view.jsp?formCode=" +
                StrUtil.UrlEncode(nestFormCode) + "&op=" + op + "&nestFieldName=" + StrUtil.UrlEncode(nestFieldName) + "&pageType=" + pageType;

        String path = Global.getFullRootPath(request) + url;
        // LogUtil.getLog(getClass()).info("path=" + path);
        if ("archive".equals(pageType)) {
            String str = NetUtil.gather(request, "utf-8", path);
            str ="<div id='nestsheet_"+ff.getName()+"'>"+str+"</div>";
            return str;
        }

        String ajaxPath = request.getContextPath() + url;

        String str = "<div id='nestsheet_" + ff.getName() + "'></div>";

        if (isTab == 1) {
            // 加入判断tabs-***是否存在，是因为在模块查看界面中并没有选项卡
            str += "\n<script>$(function() { if (o('tabs-" + nestFormCode + "')) loadNestCtl('" + ajaxPath + "','tabs-" + nestFormCode + "'); });</script>\n";
        } else {
            str += "\n<script>loadNestCtl('" + ajaxPath + "','nestsheet_" + ff.getName() + "')</script>\n";
        }

        if (request.getAttribute("isNestTableCtlJS") == null) {
            str = "\n<script src='" + request.getContextPath()
                    + "/flow/macro/macro_js_nesttable.jsp?isTab=" + isTab + "&pageType=" + pageType + "&fieldName=" + ff.getName() + "&nestFormCode=" + nestFormCode + "&flowId=" + cwsId
                    + "'></script>\n" + str;
            request.setAttribute("isNestTableCtlJS", "y");
        }

        // 因为每个表单的重新载入调用的方法是不一样的，所以在这里需要分别为每个嵌套表生成方法
        // 当自动拉单后，需调用forRefresh
        if (!"show".equals(pageType) && !"flowShow".equals(pageType)) {
            // 有可能会有多个嵌套表格，所以不能只引入一次
            // if (request.getAttribute("isNestTableCtlJS_forRefresh") == null) {
                str = "\n<script src='" + request.getContextPath()
                        + "/flow/macro/macro_js_nesttable.jsp?op=forRefresh&pageType=" + pageType + "&isTab=" + isTab + "&fieldName=" + ff.getName() + "&parentFormCode=" + parentFormCode + "&nestFormCode=" + nestFormCode + "&path=" + StrUtil.UrlEncode(ajaxPath) + "&flowId=" + cwsId + "&mainId=" + mainId
                        + "'></script>\n" + str;
                request.setAttribute("isNestTableCtlJS_forRefresh", "y");
            // }
        }
        return str;
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = getNestTable(request, ff);
        // 转换javascript脚本中的\为\\
        str = str.replaceAll("\\\\", "\\\\\\\\");

        return str;
    }

    /**
     * 当创建父记录时，同步创建嵌套表单的记录（用于visual模块，流程中用不到，因为流程中事先生成了空的表单）
     *
     * @param macroField FormField
     * @param cwsId      String
     * @param creator    String
     * @param fu         FileUpload
     * @return int
     * @throws ErrMsgException
     */
    @Override
    public int createForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                                String creator,
                                FileUpload fu) throws ErrMsgException {
        String[] rowIds = fu.getFieldValues("rowId" + macroField.getName());
        // 手机端没有post过来cws_cell_rows，在此不作处理，直接返回0，以免数据被清空
        if (rowIds == null) {
            return 0;
        }

        int rows = rowIds.length;

        int formViewId = -1;
        String formCode = macroField.getDescription();
        try {
            String defaultVal = StrUtil.decodeJSON(formCode);
            JSONObject json = new JSONObject(defaultVal);
            formCode = json.getString("destForm");
            if (!json.isNull("formViewId")) {
                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(formCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        String viewContent = "";
        if (formViewId!=-1) {
            FormViewDb formViewDb = new FormViewDb();
            formViewDb = formViewDb.getFormViewDb(formViewId);
            viewContent = formViewDb.getString("content");
        }
        else {
            viewContent = FormViewMgr.makeViewContent(msd);
        }

        // 解析出行中的字段，按td从左至右顺序
        Vector<FormField> fields = NestTableCtl.parseFieldsFromView(fd, viewContent);

        // 有效性验证
        ParamChecker pck = new ParamChecker(request, fu);
        for (int i = 0; i < rows; i++) {
            int rowId = StrUtil.toInt(rowIds[i], -1);
            if (rowId == -1) {
                LogUtil.getLog(getClass()).error("行rowId " + rowIds[i] + " 错误");
                continue;
            }

            Iterator irField = fields.iterator();
            while (irField.hasNext()) {
                FormField ff = (FormField) irField.next();
                try {
                    // LogUtil.getLog(getClass()).info("ruleStr=" + ruleStr);
                    FormDAOMgr.checkField(request, fu, pck, ff, "nest_field_" + ff.getName() + "_" + rowId, null);
                } catch (CheckErrException e) {
                    // 如果onError=exit，则会抛出异常
                    throw new ErrMsgException(e.getMessage());
                }
            }
        }
        if (pck.getMsgs().size() != 0) {
            throw new ErrMsgException(pck.getMessage(false));
        }

        try {
            checkUnique(request, fields, fu, rowIds);
        } catch (ResKeyException e) {
            e.printStackTrace();
        }

        String fds = "";
        String str = "";
        for (FormField ff : fields) {
            if ("".equals(fds)) {
                fds = ff.getName();
                str = "?";
            } else {
                fds += "," + ff.getName();
                str += ",?";
            }
        }
        String sql = "insert into " + fd.getTableNameByForm() +
                " (flowId, cws_creator, cws_id, " + fds +
                ",flowTypeCode,cws_status,cws_create_date,cws_parent_form,unit_code) values (?,?,?," + str + ",?,?,?,?,?)";

        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(creator).getCode();

        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            for (int i = 0; i < rows; i++) {
                int rowId = StrUtil.toInt(rowIds[i], -1);

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, FormDAO.NONEFLOWID);
                ps.setString(2, creator);
                ps.setString(3, cwsId);

                // 赋值
                Iterator<FormField> irField = fields.iterator();
                while (irField.hasNext()) {
                    FormField ff = (FormField) irField.next();
                    // 取得表单域的值
                    ff.setValue(StrUtil.getNullStr(fu.getFieldValue("nest_field_" + ff.getName() + "_" + rowId)));
                    // 因为在计算控件中，将根据下列语句获得表单域的字段值
                    // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                    // 所以在此需赋值给fileUpload中对应的属性
                    fu.setFieldValue(ff.getName(), ff.getValue());
                }

                int k = 4;
                irField = fields.iterator();
                while (irField.hasNext()) {
                    FormField ff = (FormField) irField.next();
                    ff.createDAOVisual(ps, k, fu, fd);
                    k++;
                }

                String curTime = "" + System.currentTimeMillis();
                ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                ps.setInt(k + 1, com.redmoon.oa.flow.FormDAO.STATUS_DONE);
                ps.setTimestamp(k + 2, new Timestamp(System.currentTimeMillis()));
                ps.setString(k + 3, macroField.getFormCode());
                ps.setString(k + 4, unitCode);
                if (conn.executePreUpdate() == 1) {
                    ret++;
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            throw new ErrMsgException("数据库错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return ret;
    }

    /**
     * 唯一性检查
     * @param request
     * @param fieldsWritable
     * @param fu
     * @param rowIds
     * @throws ResKeyException
     */
    public void checkUnique(HttpServletRequest request, Vector<FormField> fieldsWritable, FileUpload fu, String[] rowIds) throws ResKeyException {
        // 如果是需检查全局唯一，则需先检查在嵌套表中唯一
        boolean isCheckUniqueGloal = false;
        boolean isCheckUniqueNest = false;
        for (FormField ff : fieldsWritable) {
            if (ff.isUnique()) {
                isCheckUniqueNest = true;
                isCheckUniqueGloal = true;
                break;
            }
            if (ff.isUniqueNest()) {
                isCheckUniqueNest = true;
            }
        }

        // 判断是否在嵌套表中唯一
        if (isCheckUniqueNest) {
            checkFieldIsUniqueNestInNestTableRow(request, fieldsWritable, fu, rowIds);
        }

        // 判断是否全局唯一
        if (isCheckUniqueGloal) {
            FormDAOMgr.checkFieldIsUniqueInNestTable(fieldsWritable, fu, rowIds);
        }
    }

    /**
     * 保存嵌套表单中的记录，智能模块与流程中共用本方法
     *
     * @param macroField FormField
     * @param cwsId      String 父记录的ID，用于与父记录关联
     * @param creator    String 当运用于流程时，creator为空，因为无法记录是具体哪个人修改了嵌套表格
     * @param fu         FileUpload
     * @return int
     * @throws ErrMsgException
     */
    @Override
    public int saveForNestCtl(HttpServletRequest request, FormField macroField, String cwsId,
                              String creator,
                              FileUpload fu) throws ErrMsgException {
        if (!macroField.isEditable()) {
            return 0;
        }
        String skey = fu.getFieldValue("skey");
        // 手机端在此不作处理，直接返回0，以免数据被清空
        if (skey != null && !"".equals(skey.trim())) {
            return 0;
        }
        String[] rowIds = fu.getFieldValues("rowId" + macroField.getName());
        // if (rowIds == null) {
            // 不能直接返回，因为有可能删除了全部的记录
            // return 0;
        // }

        int rows = 0;
        if (rowIds!=null) {
            rows = rowIds.length;
        }

        int formViewId = -1;
        String nestFormCode = macroField.getDescription();
        try {
            String defaultVal = StrUtil.decodeJSON(nestFormCode);
            JSONObject json = new JSONObject(defaultVal);
            nestFormCode = json.getString("destForm");
            if (!json.isNull("formViewId")) {
                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(nestFormCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(nestFormCode);

        String viewContent = "";
        if (formViewId!=-1) {
            FormViewDb formViewDb = new FormViewDb();
            formViewDb = formViewDb.getFormViewDb(formViewId);
            viewContent = formViewDb.getString("content");
        }
        else {
            viewContent = FormViewMgr.makeViewContent(msd);
        }

        // 解析出行中的字段，按td从左至右顺序
        Vector<FormField> fields = NestTableCtl.parseFieldsFromView(fd, viewContent);
        long fdaoId = StrUtil.toLong(cwsId);

        // 在flow.FormDAOMgr的update方法中，置了request的属性action
        WorkflowActionDb action = (WorkflowActionDb) request.getAttribute("action");
        // 判断是否来自于流程处理表单
        Vector<FormField> fieldsWritable;
        int flowId = FormDAO.NONEFLOWID;
        boolean isFlow = false;

        if (action != null) {
            isFlow = true;
            fieldsWritable = new Vector<>();
            flowId = StrUtil.toInt(cwsId);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(wf.getTypeCode());

            Vector<FormField> flds = fd.getFields();

            FormDb flowFd = new FormDb();
            flowFd = flowFd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
            fdao = fdao.getFormDAO(flowId, flowFd);
            fdaoId = fdao.getId();

            if (lf.getType() == Leaf.TYPE_FREE) {
                // 自由流程根据用户所属的角色，得到可写表单域
                Privilege pvg = new Privilege();
                String userName = pvg.getUser(request);
                WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
                wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

                String[] fds = wfpd.getFieldsWriteOfUser(wf, userName);
                // 将可写的域筛选出
                for (Object fld : flds) {
                    FormField ff = (FormField) fld;
                    for (String s : fds) {
                        if (ff.getName().equals(s)) {
                            fieldsWritable.addElement(ff);
                            break;
                        }
                    }
                }
            } else {
                // 预设流程根据动作中的设定得到可写表单域
                String fieldWrite = StrUtil.getNullString(action.getFieldWrite()).trim();
                String[] fds = fieldWrite.split(",");
                // 将可写的域筛选出
                for (Object fld : flds) {
                    FormField ff = (FormField) fld;
                    for (String s : fds) {
                        if (s.startsWith("nest.")) {
                            String fName = s.substring("nest.".length());
                            if (ff.getName().equals(fName)) {
                                fieldsWritable.addElement(ff);
                                break;
                            }
                        }
                    }
                }
            }
            // 如果没有可写字段，则返回，以免生成空行
            if (fieldsWritable.size() == 0) {
                return 0;
            }
        }
        else {
            fieldsWritable = fields;
        }

        if (rowIds==null) {
            String sql = "delete from " + fd.getTableNameByForm() + " where cws_id=? and cws_parent_form=?";
            JdbcTemplate jt = new JdbcTemplate();
            try {
                return jt.executeUpdate(sql, new Object[]{fdaoId, macroField.getFormCode()});
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 有效性验证
        ParamChecker pck = new ParamChecker(request, fu);
        for (int i = 0; i < rows; i++) {
            int rowId = StrUtil.toInt(rowIds[i], -1);
            if (rowId == -1) {
                LogUtil.getLog(getClass()).error("行rowId " + rowIds[i] + " 错误");
                continue;
            }

            for (FormField ff : fields) {
                // 判断是否在流程中，且是否为可写字段，如果是，则进行有效性验证
                boolean isCheck = false;
                if (action != null) {
                    for (Object o : fieldsWritable) {
                        FormField f = (FormField) o;
                        if (f.getName().equals(ff.getName())) {
                            isCheck = true;
                            break;
                        }
                    }
                } else {
                    isCheck = true;
                }

                if (isCheck) {
                    try {
                        FormDAOMgr.checkField(request, fu, pck, ff, "nest_field_" + ff.getName() + "_" + rowId, null);
                    } catch (CheckErrException e) {
                        throw new ErrMsgException(e.getMessage());
                    }
                }
            }
        }

        if (pck.getMsgs().size() != 0) {
            throw new ErrMsgException(pck.getMessage(false));
        }

        try {
            checkUnique(request, fieldsWritable, fu, rowIds);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }

        StringBuilder fds = new StringBuilder();
        StringBuilder str = new StringBuilder();
        for (FormField ff : fieldsWritable) {
            if ("".equals(fds.toString())) {
                fds = new StringBuilder(ff.getName());
                str = new StringBuilder("?");
            } else {
                fds.append(",").append(ff.getName());
                str.append(",?");
            }
        }

        String sql = "select id from " + fd.getTableNameByForm() +
                " where cws_id=" + StrUtil.sqlstr(String.valueOf(fdaoId)) + " and cws_parent_form=" + StrUtil.sqlstr(macroField.getFormCode()) +
                " order by cws_order";
        FormDAO fdao = new FormDAO();
        Vector<FormDAO> v = fdao.list(nestFormCode, sql);
        long[] ids = new long[v.size()]; // 数据库中已有记录的ID
        Iterator<FormDAO> irDao = v.iterator();
        int i = 0;
        while (irDao.hasNext()) {
            fdao = irDao.next();
            ids[i] = fdao.getId();
            i++;
        }

        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(creator).getCode();

        int ret = 0;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            sql = "insert into " + fd.getTableNameByForm() +
                    " (flowId, cws_creator, cws_id, cws_order, " + fds +
                    ",flowTypeCode, cws_status,cws_parent_form,unit_code,cws_create_date) values (?,?,?,?," + str + ",?,?,?,?,?)";
            int cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_NOT;
            if (action == null) {
                cwsStatus = com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }
            // 插入新增行
            for (i = 0; i < rows; i++) {
                int rowId = StrUtil.toInt(rowIds[i], -1);
                int dataId = StrUtil.toInt(fu.getFieldValue("nest_field_dataId" + macroField.getName() + "_" + rowId));
                if (dataId == -1) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, flowId);
                    ps.setString(2, creator);
                    ps.setString(3, String.valueOf(fdaoId));
                    ps.setInt(4, i);

                    int k = 5;
                    for (Object o : fieldsWritable) {
                        FormField ff = (FormField) o;

                        // 取得表单域的值
                        ff.setValue(StrUtil.getNullStr(fu.getFieldValue("nest_field_" + ff.getName() + "_" + rowId)));
                        // String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // f为计算控件中涉及的表单域的字段
                        // 所以在此需赋值给fileUpload中对应的属性
                        fu.setFieldValue(ff.getName(), ff.getValue());

                        ff.createDAOVisual(ps, k, fu, fd);
                        k++;
                    }
                    String curTime = "" + System.currentTimeMillis();
                    ps.setString(k, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取
                    ps.setInt(k + 1, cwsStatus);
                    ps.setString(k + 2, macroField.getFormCode());
                    ps.setString(k + 3, unitCode);
                    ps.setTimestamp(k + 4, new Timestamp(System.currentTimeMillis()));
                    if (conn.executePreUpdate() == 1) {
                        ret++;
                    }
                    ps.close();
                }
            }

            sql = "delete from " + fd.getTableNameByForm() + " where id=?";
            // 检查是否有被删除项
            for (i = 0; i < ids.length; i++) {
                boolean isFound = false;
                for (String rowId : rowIds) {
                    int dataId = StrUtil.toInt(fu.getFieldValue("nest_field_dataId" + macroField.getName() + "_" + rowId));
                    if (dataId == ids[i]) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setLong(1, ids[i]);
                    conn.executePreUpdate();
                }
            }

            fds = new StringBuilder();
            for (Object o : fieldsWritable) {
                FormField ff = (FormField) o;
                if ("".equals(fds.toString())) {
                    fds = new StringBuilder(ff.getName() + "=?");
                } else {
                    fds.append(",").append(ff.getName()).append("=?");
                }
            }

            sql = "update " + fd.getTableNameByForm() + " set " + fds + ",cws_creator=?,cws_order=?,flowTypeCode=? where id=?";
            for (i = 0; i < rowIds.length; i++) {
                int rowId = StrUtil.toInt(rowIds[i], -1);
                int dataId = StrUtil.toInt(fu.getFieldValue("nest_field_dataId" + macroField.getName() + "_" + rowId));

                irDao = v.iterator();
                while (irDao.hasNext()) {
                    fdao = irDao.next();
                    if (fdao.getId() == dataId) {
                        PreparedStatement ps = conn.prepareStatement(sql);

                        int k = 1;

                        for (Object o : fieldsWritable) {
                            FormField ff = (FormField) o;

                            ff.setValue(StrUtil.getNullStr(fu.getFieldValue("nest_field_" + ff.getName() + "_" + rowId)));

                            LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue());
                            // 因为在计算控件中，将根据下列语句获得表单域的字段值
                            // String s = fDao.getFieldValue(f); // f为计算控件中涉及的表单域的字段
                            // 所以在此需赋值给fileUpload中对应的属性
                            fdao.setFieldValue(ff.getName(), ff.getValue());

                            ff.saveDAOVisual(fdao, ps, k, fdao.getId(), fd, fu);
                            k++;
                        }

                        ps.setString(k, creator);
                        ps.setInt(k + 1, i);
                        String curTime = "" + System.currentTimeMillis();
                        ps.setString(k + 2, "" + curTime); // 用flowTypeCode记录修改时间，同时作为create时用来作为标识，以便在插入后获取

                        ps.setInt(k + 3, dataId);
                        conn.executePreUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ErrMsgException("数据库错误！");
        } finally {
            conn.close();
        }
        return ret;
    }

    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = super.getDisableCtlScript(ff, formElementId);
        str += "\ntry {tableOperate.style.display='none';} catch(e){}";
        str += "\ncanTdEditable=false;\n";
        return str;
    }

    @Override
    public int onDelNestCtlParent(FormField macroField, String cwsId) {
        String formCode = macroField.getDescription();
        try {
            String defaultVal = StrUtil.decodeJSON(formCode);
            JSONObject json = new JSONObject(defaultVal);
            formCode = json.getString("destForm");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FormDAO fdao = new FormDAO();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        int r = 0;
        String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=? and cws_parent_form=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{cwsId, macroField.getFormCode()});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                int fdaoId = rr.getInt(1);
                fdao = fdao.getFormDAO(fdaoId, fd);
                if (!fdao.isLoaded()) {
                    continue;
                }
                if (fdao.del()) {
                    r++;
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return r;
    }

    public int uploadExcel(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        int r = 0;
        FileUpload fileUpload = doUpload(application, request);
        String upFile = writeExcel(fileUpload);
        String excelFile = Global.getRealPath() + upFile;
        String nestType = ParamUtil.get(request, "nestType");
        String formCode = fileUpload.getFieldValue("formCode");
        try {
            if (!"".equals(upFile)) {
                if ("detaillist".equals(nestType)) {
                    cellJsonArray = read(excelFile, formCode);
                    r = cellJsonArray.length();
                }
                else {
                    int flowId = ParamUtil.getInt(request, "flowId", -1);
                    long parentId = ParamUtil.getLong(request, "parentId", -1);
                    String nestFieldName = ParamUtil.get(request, "nestFieldName");
                    String parentFormCode = ParamUtil.get(request, "parentFormCode");
                    String nestFormCode = "";
                    JSONObject json = null;
                    int formViewId = -1;
                    FormField nestField = null;
                    if (!"".equals(nestFieldName)) {
                        FormDb parentFd = new FormDb();
                        parentFd = parentFd.getFormDb(parentFormCode);
                        nestField = parentFd.getFormField(nestFieldName);
                        if (nestField == null) {
                            throw new ErrMsgException("父表单（" + parentFormCode + "）中的嵌套表字段：" + nestFieldName + " 不存在");
                        }
                        try {
                            String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
                            json = new JSONObject(defaultVal);
                            nestFormCode = json.getString("destForm");
                            if (!json.isNull("formViewId")) {
                                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
                            }
                            else {
                                throw new ErrMsgException("嵌套表格未视定视图");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        nestFormCode = formCode;
                    }

                    String viewContent = "";
                    if (formViewId!=-1) {
                        FormViewDb formViewDb = new FormViewDb();
                        formViewDb = formViewDb.getFormViewDb(formViewId);
                        viewContent = formViewDb.getString("content");
                    }
                    else {
                        ModuleSetupDb msd = new ModuleSetupDb();
                        msd = msd.getModuleSetupDb(nestFormCode);
                        viewContent = FormViewMgr.makeViewContent(msd);
                    }

                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(nestFormCode);
                    Vector fields = NestTableCtl.parseFieldsFromView(fd, viewContent);

                    r = read(excelFile, formCode, fields, parentFormCode, parentId, flowId, request);
                }
            } else {
                throw new ErrMsgException("文件不能为空！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            File file = new File(excelFile);
            file.delete();
        }
        return r;
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"xls", "xlsx"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String writeExcel(FileUpload fu) {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                vpath = "upfile/" + fi.getExt() + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + vpath;
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return vpath + fi.getDiskName();
            }
        }
        return "";
    }

    public JSONArray read(String xlspath, String formCode) throws ErrMsgException,
            IndexOutOfBoundsException {
        JSONArray rows = new JSONArray();
        try {
            Workbook book = WorkbookParser.getWorkbook(new java.io.File(xlspath));
            // 获取sheet表的总行数、总列数
            jxl.Sheet rs = book.getSheet(0);
            int rsRows = rs.getRows();
            int rsColumns = rs.getColumns();

            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDbOrInit(formCode);

            // String listField = StrUtil.getNullStr(msd.getString("list_field"));
            String[] fieldCodes = msd.getColAry(false, "list_field");

            // System.out.println(getClass() + " " + formCode + " listField=" + listField);

            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            if (rsColumns > fieldCodes.length) {
                rsColumns = fieldCodes.length;
            }

            // 从第二行开始
            for (int i = 1; i < rsRows; i++) {
                JSONObject cell = new JSONObject();

                for (int j = 0; j < rsColumns; j++) {
                    Cell cc = rs.getCell(j, i);
                    try {
                        String c = cc.getContents();
                        if (fd.getFormField(fieldCodes[j]).getFieldType()==FormField.FIELD_TYPE_DATE) {
                            java.util.Date d = DateUtil.parse(c, "dd/MM/yyyy");
                            if (d!=null)
                                c = DateUtil.format(DateUtil.parse(c, "dd/MM/yyyy"), "yyyy-MM-dd");
                        }
                        cell.put(fieldCodes[j], c);
                    } catch (JSONException ex1) {
                    }
                }
                rows.put(cell);
            }
        } catch (BiffException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return rows;

    }

    /**
     * import excel
     * @param xlspath
     * @param parentId
     * @param flowId
     * @param request
     * @return
     * @throws IOException
     */
    public int read(String xlspath, String formCode, Vector fields, String parentFormCode, long parentId, int flowId, HttpServletRequest request) throws IOException {
        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        InputStream in = null;
        int rowcount = 0;
        try {
            FormDb fd = new FormDb(formCode);
            FormDAO fdao = new FormDAO(fd);

            in = new FileInputStream(xlspath);
            String pa = StrUtil.getFileExt(xlspath);
            if (pa.equals("xls")) {
                try {
                    // 读取xls格式的excel文档
                    HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
                    // 获取sheet
                    for (int i = 0; i < w.getNumberOfSheets() && i<1; i++) {
                        HSSFSheet sheet = w.getSheetAt(i);
                        if (sheet != null) {
                            // 获取行数
                            rowcount = sheet.getLastRowNum();
                            HSSFCell cell = null;

                            FormField ff = null;
                            Vector<FormField> vfields = null;
                            // 获取每一行
                            for (int k = 1; k <= rowcount; k++) {
                                vfields = new Vector<FormField>();
                                HSSFRow row = sheet.getRow(k);
                                if (row != null) {
                                    // 获取每一单元格
                                    Iterator ir = fields.iterator();
                                    int m = 0;
                                    while (ir.hasNext()) {
                                        ff = (FormField)ir.next();
                                        cell = row.getCell(m);
                                        // 如果单元格中的值为空，则cell可能为null
                                        if (cell!=null) {
                                            if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell))
                                            {
                                                Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                                                ff.setValue(DateUtil.format(date, "yyyy-MM-dd"));
                                            }
                                            else
                                            {
                                                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                                ff.setValue(cell.getStringCellValue());
                                            }
                                        }

                                        vfields.add(ff);
                                        m++;
                                    }
                                    fdao.setFields(vfields);
                                    fdao.setUnitCode(unitCode);
                                    fdao.setFlowId(flowId);
                                    fdao.setCwsId(String.valueOf(parentId));
                                    fdao.setCreator(pvg.getUser(request));
                                    fdao.setCwsParentForm(parentFormCode);
                                    fdao.create();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("xlsx".equals(pa)) {
                XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
                for (int i = 0; i < w.getNumberOfSheets() && i<1; i++) {
                    XSSFSheet sheet = w.getSheetAt(i);
                    if (sheet != null) {
                        rowcount = sheet.getLastRowNum();
                        XSSFCell cell = null;
                        FormField ff = null;
                        Vector<FormField> vfields = null;
                        for (int k = 1; k <= rowcount; k++) {
                            vfields = new Vector<FormField>();
                            XSSFRow row = sheet.getRow(k);
                            if (row != null) {
                                int m = 0;
                                Iterator ir = fields.iterator();
                                while (ir.hasNext()) {
                                    ff = (FormField)ir.next();
                                    cell = row.getCell(m);
                                    if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell))
                                    {
                                        Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                                        ff.setValue(DateUtil.format(date, "yyyy-MM-dd"));
                                    }
                                    else
                                    {
                                        cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                        ff.setValue(cell.getStringCellValue());
                                    }
                                    vfields.add(ff);
                                    m++;
                                }
                                fdao.setFields(vfields);
                                fdao.setUnitCode(unitCode);
                                fdao.setFlowId(flowId);
                                fdao.setCwsId(String.valueOf(parentId));
                                fdao.setCreator(pvg.getUser(request));
                                fdao.setCwsParentForm(parentFormCode);
                                fdao.create();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rowcount;
    }

    @Override
    public String getControlType() {
        return "text";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    /**
     * 将textarea转为input，清除cws_span_***
     * cws_textarea_***不能清除，因为生成的脚本中可能含有：setCtlValue('dd', 'text', o('cws_textarea_dd').value);
     * cws_span_***不能清除，因为生成的脚本中可能含有：DisableCtl('dd', 'text',cws_span_dd.innerHTML, o('cws_textarea_dd').value);清除掉后会导致DisableCtl时报错
     * @param html
     * @return
     */
    public static String convertTextareaToInputAndClearCwsSpan(String html) {
        boolean isFound = false;
        Parser parser;
        TagNameFilter filter = new TagNameFilter("textarea");
        try {
            do {
                isFound = false;
                parser = new Parser(html);
                parser.setEncoding("utf-8");

                NodeList nodes = parser.parse(filter);
                if (nodes == null || nodes.size() == 0) {
                    ;
                } else {
                    for (int i = 0; i < nodes.size(); i++) {
                        TextareaTag node = (TextareaTag) nodes.elementAt(i);
                        String nodeName = node.getAttribute("name");
                        if (!nodeName.startsWith("cws_textarea_")) {
                            int s = node.getStartPosition();
                            int e = node.getEndTag().getEndPosition();
                            String c = html.substring(0, s);
                            c += "<input id='" + nodeName + "' name='" + nodeName + "'/>";
                            c += html.substring(e);
                            html = c;
                            isFound = true;
                            break;
                        }
                    }
                }
            } while (isFound);

            /*
            // 清除cws_span_***
            filter = new TagNameFilter("span");
            do {
                isFound = false;
                parser = new Parser(html);
                parser.setEncoding("utf-8");
                NodeList nodes = parser.parse(filter);
                if (nodes == null || nodes.size() == 0) {
                    ;
                } else {
                    for (int i=0; i < nodes.size(); i++) {
                        Span node = (Span) nodes.elementAt(i);
                        if (node.getAttribute("id")!=null && node.getAttribute("id").startsWith("cws_span_")) {
                            int s = node.getStartPosition();
                            int e = node.getEndTag().getEndPosition();
                            String c = html.substring(0, s);
                            c += html.substring(e);
                            html = c;

                            isFound = true;
                            break;
                        }
                    }
                }
            } while (isFound);
            */
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return html;
    }

    /**
     * 从视图中解析出表单域，按照td从左到右的顺序
     * @param fd
     * @param viewContent
     * @return
     */
    public static Vector<FormField> parseFieldsFromView(FormDb fd, String viewContent) {
        String ieVersion = "11";
        Vector<FormField> fields = new Vector<>();
        // 取出视图中最后一行tr
        NodeFilter filter = new CssSelectorNodeFilter("tr"); // .className tr
        filter = new AndFilter(filter, new NotFilter(new HasChildFilter(new CssSelectorNodeFilter("tr"))));
        Parser parser;
        try {
            parser = new Parser(viewContent);
            NodeList list = parser.extractAllNodesThatMatch(filter);

            int lastTrIndex = list.size() - 1;
            if (lastTrIndex < 0) {
                // out.print("视图中不存在表格行");
                return null;
            }
            Node tr = list.elementAt(lastTrIndex);
            String trHtml = tr.toHtml();

            // 解析出行中的字段，按td从左至右顺序
            FormParser fp = new FormParser();
            parser = new Parser(trHtml);
            NodeList tds = parser.extractAllNodesThatMatch(new CssSelectorNodeFilter("td"));
            for (int j = 0; j < tds.size(); j++) {
                fields.addAll(fp.parseCtlFromView(tds.elementAt(j).toHtml(), ieVersion, fd));
            }
        }
        catch(ParserException e) {
            e.printStackTrace();
        }
        return fields;
    }


    /**
     * 如果在流程中，嵌套表格被置为必填，在验证前置表单域的值，以便于有效性检查能通过
     * @param request
     * @param fu
     * @param ff
     */
    @Override
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) {
        String[] rowIds = fu.getFieldValues("rowId" + ff.getName());
        int rows = 0;
        if (rowIds!=null) {
            rows = rowIds.length;
        }

        if (rows > 0) {
            fu.setFieldValue(ff.getName(), "cws");
        }
    }

    /**
     * 判断嵌套表格宏控件中是否存在重复记录
     * @param fields
     * @param fu
     */
    public static void checkFieldIsUniqueNestInNestTableRow(HttpServletRequest request, Vector<FormField> fields, FileUpload fu, String[] rowIds) throws ResKeyException {
        StringBuffer sbFields = new StringBuffer();
        for (FormField ff : fields) {
            if (ff.isUniqueNest()) {
                StrUtil.concat(sbFields, "+", ff.getTitle());
            }
        }

        Map<String, String> map = new HashMap<>();
        for (String strRowId : rowIds) {
            int rowId = StrUtil.toInt(strRowId, -1);
            StringBuilder sb = new StringBuilder();
            for (FormField ff : fields) {
                if (ff.isUniqueNest()) {
                    StrUtil.concat(sb, "#_cws_&", fu.getFieldValue("nest_field_" + ff.getName() + "_" + rowId));
                }
            }
            if (map.get(sb.toString()) == null) {
                map.put(sb.toString(), "");
            } else {
                throw new ResKeyException("res.module.flow", "err_is_not_unique", new Object[]{sbFields.toString()});
            }
        }
    }
}
