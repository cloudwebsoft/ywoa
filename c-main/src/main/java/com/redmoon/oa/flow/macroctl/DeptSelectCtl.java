package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import cn.js.fan.web.Global;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.sys.DebugUtil;
import org.json.*;
import java.util.Iterator;

/**
 * <p>Title:部门选择宏控件，默认取当前用户的所在”单位“的部门，可以与用户选择列表控件联动 </p>
 *
 * <p>Description:
 * 描述为my，表示仅用户所在的部门及其子部门
 * 描述为user_admin_dept，表示用户可管理的部门
 * 描述为{user}，表示可以与用户选择列表控件联动，后者的字段名为user，选择部门后，user字段中列出该部门的人员，如果配以参数allUserInChildren则表示取该部门下的所有用户（含子部门）
 * 描述为{$fieldName}，表示为字段fieldName的值所对应部门下的子部门（不包含对应部门）
 * 描述为#val，表示为val所对应部门下的子部门（不包含该部门）
 * 描述中包含#val,onlyChildren,表示为val所对应部门下的子部门（不包含对应部门）, 而且子部门下级部门不做显示
 * 描述中包含 hideparent,不显示父级及以上层级部门. 如:#val,hideParent,表示为val所对应的部门下的子部门（不包含对应部门）不显示上级部门信息
 * 描述为blank，表示默认置为空，否则默认为当前用户的部门
 * 描述为@user，表示与user字段联动，显示user字段中用户对应的部门，如果有兼职，则只显示其排序为第一的部门
 * 描述为unit，表示仅取子单位
 * 描述为all，表示取所有的部门
 * 描述为&dept，表示与dept字段联动，dept显示为当前字段的上级部门
 * 描述为%4，表示显示当前用户部门的层级为4的父节点，如果当前用户部门的层级为4则显示其父部门，否则显示其所在部门
 * </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DeptSelectCtl extends AbstractMacroCtl {
    public static final String VAL_BLANK = "blank";

    public DeptSelectCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String deptCode = StrUtil.getNullStr(fieldValue);
        if (!"".equals(deptCode) && !"user_admin_dept".equals(deptCode) && !deptCode.startsWith("{")) {
            DeptMgr dm = new DeptMgr();

            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            if (!dd.isLoaded()) {
                return "";
            }

            String deptName = dd.getName();
            /*
             * 判断是否描述中包含了隐藏上级部门信息
             */
            String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if ("".equals(defaultValue)) {
                defaultValue = StrUtil.getNullStr(ff.getDescription());
            }
            boolean isShowParent = true;
            String[] defaultArr = defaultValue.split(",");
            for(String str : defaultArr){
                if(str.trim().length() != 0) {
                    if ("hideparent".equalsIgnoreCase(str)) {
                        // 隐藏上级部门信息
                        isShowParent = false;
                        break;
                    }
                }
            }
            if(isShowParent){
                String pCode = dd.getParentCode();
                while (!pCode.equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {
                    dd = dm.getDeptDb(pCode);
                    if (!dd.isLoaded()) {
                        break;
                    }
                    deptName = dd.getName() + "\\" + deptName;
                    pCode = dd.getParentCode();
                }
            }
            return deptName;
        }
        else {
            return "";
        }
    }

    /**
     *
     * @Description: 用于智能表单显示控件
     * @param request
     * @param ff
     * @return
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        long t = System.currentTimeMillis();

        Privilege pvg = new Privilege();

        String str = "";

        boolean isInclude = true; // 是否包含指定的部门
        boolean isOnlyChildren = false; // 执行部门下的子部门, 但是不显示子部门下的部门

        /*
         * 判断是否描述中包含了隐藏上级部门信息 2018-8-1
         */
        String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        // String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if ("".equals(defaultValue)) {
            defaultValue = StrUtil.getNullStr(ff.getDescription());
        }

        DeptMgr dm = new DeptMgr();
        DeptDb lf = null;
        String strRelateUserCtl = "";
        String formName = ParamUtil.get(request, "cwsFormName");

        boolean isAll = false;
        String[] defaultArr = defaultValue.split(",");
        for(String defaultDept : defaultArr) {
            if(defaultDept.length() != 0) {
                if ("my".equals(defaultDept)) {
                    DeptUserDb dud = new DeptUserDb();
                    Iterator<DeptDb> ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
                    if (ir.hasNext()) {
                        lf = ir.next();
                    }
                }
                else if ("all".equals(defaultDept)) {
                    isAll = true;
                }
                else if (defaultDept.startsWith("{$") && defaultDept.contains("}")) {
                    // 解析出对应的字段名
                    String fieldName = defaultDept.substring(2, defaultDept.indexOf("}"));
                    if (iFormDAO != null) {
                        String deptCode = iFormDAO.getFieldValue(fieldName);
                        lf = dm.getDeptDb(deptCode);
                        if (lf == null || !lf.isLoaded()) {
                            lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                        }
                    } else {
                        lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                    }
                } else if ("onlyChildren".equalsIgnoreCase(defaultDept)) {
                    isOnlyChildren = true;
                } else if ("hideParent".equalsIgnoreCase(defaultDept)) {
                    isInclude = false;
                }
                else if (defaultDept.startsWith("#")) {
                    String defaultDeptSetCode = defaultDept.substring(1);
                    lf = dm.getDeptDb(defaultDeptSetCode);
                    if (lf == null || !lf.isLoaded()) {
                        lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                    } else {
                        isInclude = false;
                    }
                }
                else if ("allUserInChildren".equals(defaultDept)) {
                    continue;
                }
                else {
                    lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                }

                if (defaultDept.startsWith("{") && !defaultDept.startsWith("{$")) {
                    if (defaultDept.contains("}")) {
                        String field = defaultDept.substring(1, defaultDept.indexOf("}"));
                        FormDb fd = new FormDb();
                        fd = fd.getFormDb(ff.getFormCode());
                        FormField userff = fd.getFormField(field);
                        if ("macro_user_select".equals(userff.getMacroType())) {
                            strRelateUserCtl += "<script>ajaxGetJS(\"/flow/macro/macro_js_dept_select.jsp?cwsFormName=" + formName + "&formCode=" + userff.getFormCode() + "&deptField=" + ff.getName() + "&userField=" +
                                    field + "\", {})</script>\n";
                        }
                        else {
                            // str += "关联控件类型不匹配，不是用户列表选择控件";
                        }
                    }
                }
                else if (defaultDept.startsWith("&")) {
                    String field = defaultDept.substring(1);
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(ff.getFormCode());
                    FormField userff = fd.getFormField(field);
                    if (userff == null) {
                        DebugUtil.e(getClass(), "关联的上级部门字段", field + " 不存在");
                    }
                    else {
                        if (ff.isEditable()) {
                            strRelateUserCtl += "<script>ajaxGetJS(\"/flow/macro/macro_js_dept_select.jsp?cwsFormName=" + formName + "&formCode=" + userff.getFormCode() + "&deptField=" + ff.getName() + "&parentDeptField=" +
                                    field + "\", {})</script>\n";
                        }
                    }
                }
                else if (defaultDept.startsWith("@")) {
                    String field = defaultDept.substring(1);
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(ff.getFormCode());
                    FormField userff = fd.getFormField(field);
                    if (userff == null) {
                        DebugUtil.e(getClass(), "关联的字段", field + " 不存在");
                    }
                    else {
                        strRelateUserCtl += "<script>ajaxGetJS(\"/flow/macro/macro_js_dept_select.jsp?cwsFormName=" + formName + "&formCode=" + userff.getFormCode() + "&deptField=" + ff.getName() + "&atUserField=" +
                                field + "\", {})</script>\n";
                    }
                }
            }
        }

        // 为了提高显示效率
        if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
            return "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' />";
        }

        String style = "";
        if (!"".equals(ff.getCssWidth())) {
            style = "style='width:" + ff.getCssWidth() + "'";
        }
        else {
            style = "style='width:150px'";
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ff.getFormCode());
        FormParser fp = new FormParser();
        String tip = StrUtil.getNullStr(fp.getFieldAttribute(fd, ff, "tip"));
        style += " tip='" + tip + "'";

        if (ff.isReadonly()) {
            String dCode = ff.getValue();
            String pageType = (String)request.getAttribute("pageType");
            if (!ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType) && !ConstUtil.PAGE_TYPE_SHOW.equals(pageType)) {
                str += "<span id='" + ff.getName() + "_show'>";
                if (StrUtil.isEmpty(dCode) || "user_admin_dept".equals(StrUtil.getNullStr(ff.getValue()))
                        || "hideParent".equalsIgnoreCase(StrUtil.getNullStr(ff.getValue())) || ff.getValue().startsWith("{")) {
                    dCode = getDeptCodeByValue(request, ff);
                    if (!StrUtil.isEmpty(dCode)) {
                        str += dm.getDeptDb(dCode).getName();
                    }
                } else {
                    str += dm.getDeptDb(ff.getValue()).getName();
                }
                str += "</span>";
            }
            str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' type='hidden' value='" + dCode + "' class='readonly'/>";
            return str;
            // 为提高效率，当只读时，不再生成整个部门树
            // str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' readonly class='readonly' " + style + " onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'>";
        }
        else {
            str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' " + style + ">";
        }

        if (lf==null) {
            if (isAll) {
                lf = dm.getDeptDb(DeptDb.ROOTCODE);
            }
            else {
                lf = dm.getDeptDb(pvg.getUserUnitCode(request));
            }
        }

        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("DeptSelectCtl getDeptDb " + ff.getTitle() + " " + ff.getName() + " take " + (System.currentTimeMillis() - t) + " ms");
        }

        DeptView dv = new DeptView(lf);
        StringBuffer outStr = new StringBuffer(100);

        outStr.append("<option value=''>" + ConstUtil.NONE + "</option>");

        if(isOnlyChildren){
            try {
                if ("user_admin_dept".equals(StrUtil.getNullStr(ff.getDefaultValueRaw()))){
                    dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer(), isOnlyChildren);
                } else{
                    dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude, isOnlyChildren);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }

            str += outStr;
            str += "</select>";
        }
        else{
            try {
                if ("user_admin_dept".equals(defaultValue)) {
                    dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer());
                } else if ("unit".equals(defaultValue)) {
                    dv.getDeptAsOptionsOnlyUnit(outStr, lf, lf.getLayer());
                } else {
                    dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }

            // DebugUtil.i(DeptSelectCtl.class, "convertToHTMLCtl", outStr.toString());

            str += outStr;
            str += "</select>";
        }

        str += strRelateUserCtl;
        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("DeptSelectCtl " + ff.getTitle() + " " + ff.getName() + " take " + (System.currentTimeMillis() - t) + " ms");
        }
        return str;
    }

    public String getDeptCodeByValue(HttpServletRequest request, FormField ff) {
        String code = "";
        if (ff.getValue()!=null && !"".equals(ff.getValue()) && !"user_admin_dept".equals(ff.getValue()) && !ff.getValue().equalsIgnoreCase("hideParent")) {
            code = ff.getDefaultValue(); // 启用默认值
        }
        else {
            String deptCode = ParamUtil.get(request, ff.getName());
            if ("".equals(deptCode)) {
                String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
                if ("".equals(defaultValue)) {
                    defaultValue = StrUtil.getNullStr(ff.getDescription());
                }

                int defaultLayer = -1;
                boolean isBlank = false;
                String[] defaultArr = defaultValue.split(",");
                for(String str : defaultArr){
                    str = str.trim();
                    if (VAL_BLANK.equals(str)) {
                        isBlank = true;
                    } else if (str.startsWith("%")) {
                        defaultLayer = StrUtil.toInt(str.substring(1), -1);
                        if (defaultLayer < 1) {
                            defaultLayer = -1;
                        }
                    }
                }
                // 如果默认不是置为空
                if (!isBlank) {
                    // 如果request中没有，则置为当前用户所在部门
                    Privilege privilege = new Privilege();
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(privilege.getUser(request));
                    DeptUserDb udd = new DeptUserDb();
                    Vector<DeptDb> vdept = udd.getDeptsOfUser(ud.getName());

                    if (vdept.size() > 0) {
                        code = vdept.get(0).getCode();
                        if (defaultLayer != -1) {
                            DeptDb dd = vdept.get(0);
                            int layer = dd.getLayer();
                            while (layer > defaultLayer) {
                                dd = dd.getDeptDb(dd.getParentCode());
                                layer = dd.getLayer();
                                // 根节点
                                if (layer == 1) {
                                    break;
                                }
                            }
                            code = dd.getCode();
                        }
                    }
                }
            }
            else {
                code = deptCode;
            }
        }
        return code;
    }

    /**
     * 取得用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request,
            FormField ff) {
        // 如果是部门选择宏控件，则检查如果没有赋值就赋予其当前用户的部门
        if (StrUtil.isEmpty(ff.getValue()) || "user_admin_dept".equals(StrUtil.getNullStr(ff.getValue()))
                || StrUtil.getNullStr(ff.getValue()).equalsIgnoreCase("hideParent") || ff.getValue().startsWith("{")) {
            ff.setValue(getDeptCodeByValue(request, ff));
        }
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库
        String deptCode = StrUtil.getNullStr(ff.getValue());
        String deptName = "";
        if (!"".equals(deptCode)) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            deptName = dd.getName();
        }
        return "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "','" + deptName + "','" + ff.getValue() + "');\n";
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "</select>";

        String userField = "";
        String defaultDept = StrUtil.getNullStr(ff.getDefaultValueRaw());
        // String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if ("".equals(defaultDept)) {
            defaultDept = StrUtil.getNullStr(ff.getDescription());
        }
        if (defaultDept.startsWith("{") && !defaultDept.startsWith("{$")) {
            if (defaultDept.contains("}")) {
                String field = defaultDept.substring(1, defaultDept.indexOf("}") - 1);
                FormDb fd = new FormDb();
                fd = fd.getFormDb(ff.getFormCode());
                FormField userff = fd.getFormField(field);
                if (userff==null) {
                    DebugUtil.e(getClass(), "convertToHTMLCtlForQuery", field + " 不存在");
                }
                else if ("macro_user_select".equals(userff.getMacroType())) {
                    userField = userff.getName();
                }
            }
        }

        if (request.getAttribute("isDeptSelectJS") == null) {
            str += "<script>ajaxGetJS(\"/flow/macro/macro_js_dept_select.jsp?op=forQuery&formCode=" + ff.getFormCode() + "&deptField=" + ff.getName() + "&userField=" +
                    userField + "\", {})</script>\n";
            request.setAttribute("isDeptSelectJS", "y");
        }
        return str;
    }

    // @Override
    public String convertToHTMLCtlForQueryXXX(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();
        String str = "";

        boolean isInclude = true; // 是否包含指定的部门
        boolean isOnlyChildren = false; // 执行部门下的子部门, 但是不显示子部门下的部门

        /*
         * 判断是否默认值中包含了隐藏上级部门信息 2018-8-1
         */
        String defaultDept = StrUtil.getNullStr(ff.getDefaultValueRaw());
        // String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if ("".equals(defaultDept)) {
            defaultDept = StrUtil.getNullStr(ff.getDescription());
        }

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";

        DeptMgr dm = new DeptMgr();
        DeptDb lf = null;
        if (!"".equals(defaultDept)) {
            if ("my".equals(defaultDept)) {
                DeptUserDb dud = new DeptUserDb();
                Iterator ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
                if (ir.hasNext()) {
                    lf = (DeptDb)ir.next();
                }
            }
            else if (defaultDept.startsWith("{$") && defaultDept.contains("}")) {
                // 解析出对应的字段名
                String fieldName = defaultDept.substring(2, defaultDept.indexOf("}"));
                if (iFormDAO!=null) {
                    String deptCode = iFormDAO.getFieldValue(fieldName);
                    lf = dm.getDeptDb(deptCode);
                    if (lf==null || !lf.isLoaded()) {
                        lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                    }
                }
                else {
                    lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                }
            }
            else if (defaultDept.startsWith("#")){
                /*
                 * 由于加入了是否仅仅显示子节点, 是否隐藏父节点, 要进行判断 2018-08-01 wm
                 */
                String defaultDeptSetCode = "";
                String[] defaultArr = defaultDept.split(",");
                for(String tempStr : defaultArr){
                    if("onlyChildren".equalsIgnoreCase(tempStr)){
                        isOnlyChildren = true;
                        break;
                    }
                }
                for(String tempStr : defaultArr){
                    if(tempStr.startsWith("#")){
                        defaultDeptSetCode = tempStr.substring(1);
                    }
                }
                lf = dm.getDeptDb( defaultDeptSetCode );
                if (lf==null || !lf.isLoaded()) {
                    lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                }
                else {
                    isInclude = false;
                }
            }
            else {
                lf = dm.getDeptDb(pvg.getUserUnitCode(request));
            }
        }else{
            lf = dm.getDeptDb(pvg.getUserUnitCode(request));
        }
        DeptView dv = new DeptView(lf);
        StringBuffer outStr = new StringBuffer(100);

        if(isOnlyChildren){
            try {
                if ("user_admin_dept".equals(StrUtil.getNullStr(ff.getDefaultValueRaw()))){
                    outStr = dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer(), isOnlyChildren);
                } else{
                    outStr = dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude, isOnlyChildren);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }

            str += outStr;
            str += "</select>";
        }
        else{
            try {
                if ("user_admin_dept".equals(StrUtil.getNullStr(ff.getDefaultValueRaw()))) {
                    outStr = dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer());
                } else {
                    outStr = dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }
            str += outStr;
            str += "</select>";
        }

        if (defaultDept.startsWith("{") && !defaultDept.startsWith("{$")) {
            if (defaultDept.contains("}")) {
                String field = defaultDept.substring(1, defaultDept.indexOf("}") - 1);
                FormDb fd = new FormDb();
                fd = fd.getFormDb(ff.getFormCode());
                FormField userff = fd.getFormField(field);
                if (userff==null) {
                    DebugUtil.e(getClass(), "convertToHTMLCtlForQuery", field + " 不存在");

                }
                else if ("macro_user_select".equals(userff.getMacroType())) {
                    if (request.getAttribute("isDeptSelectJS") == null) {
                        /*str += "<script src='" + request.getContextPath() + "/flow/macro/macro_js_dept_select.jsp?deptField=" + ff.getName() + "&userField=" +
                                field + "'></script>";*/
                        str += "<script>ajaxGetJS(\"/flow/macro/macro_js_dept_select.jsp?macro_js_dept_select.jsp?deptField=" + ff.getName() + "&userField=" +
                                field + "\", {})</script>\n";
                        request.setAttribute("isDeptSelectJS", "y");
                    }
                }
                else {
                    // str += "关联控件类型不匹配，不是用户列表选择控件";
                }
            }
        }

        return str;
    }

    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String deptName = "var deptName='';\n";
        if (ff.getValue()!=null && !"".equals(ff.getValue())) {
            DeptMgr dm = new DeptMgr();
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(ff.getValue());
            if (!dd.isLoaded()) {
                return "";
            }

            deptName = dd.getName();
            /*
             * 判断是否默认值中包含了隐藏上级部门信息
             */
            String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if ("".equals(defaultValue)) {
                defaultValue = StrUtil.getNullStr(ff.getDescription());
            }
            boolean isShowParent = true;
            String[] defaultArr = defaultValue.split(",");
            for(String str : defaultArr){
                if(str.length() == 0){
                    continue;
                }else if(str.equalsIgnoreCase("hideparent")){
                    // 隐藏上级部门信息
                    isShowParent = false;
                    break;
                }
            }
            if(isShowParent){
                String pCode = dd.getParentCode();
                while (!pCode.equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {
                    dd = dm.getDeptDb(pCode);
                    if (!dd.isLoaded()) {
                        break;
                    }
                    deptName = dd.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + deptName;
                    pCode = dd.getParentCode();
                }
            }
            deptName = "var deptName=\"" + deptName + "\";\n";
        }
        else {
            deptName = " var deptName='';\n";
        }
        return deptName + "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
                + "', deptName);\n";
    }

    @Override
    public String getControlType() {
        return "select";
    }

    /**
     * 手机端获取值
     * @Description:
     * @param userName
     * @param ff
     * @return
     */
    @Override
    public String getControlValue(String userName, FormField ff) {
        String deptCode ="";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            deptCode = ff.getValue();
        } else {
            // Privilege privilege = new Privilege();
            String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if ("".equals(defaultValue)) {
                defaultValue = StrUtil.getNullStr(ff.getDescription());
            }
            // 如果默认不是置为空值
            if (!VAL_BLANK.equals(defaultValue)) {
                UserDb ud = new UserDb();
                ud = ud.getUserDb(userName);
                DeptUserDb udd = new DeptUserDb();
                Vector vdept = udd.getDeptsOfUser(ud.getName());
                if (vdept != null && vdept.size() > 0) {
                    deptCode = ((DeptDb) vdept.get(0)).getCode();
                }
            }
        }
        return deptCode;
    }

    /**
     * 手机端获取显示
     * @Description:
     * @param userName
     * @param ff
     * @return
     */
    @Override
    public String getControlText(String userName, FormField ff) {
        String deptName = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            String deptCode = ff.getValue();
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            deptName = dd.getName();
        } else {
            // Privilege privilege = new Privilege();
            String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
            if ("".equals(defaultValue)) {
                defaultValue = StrUtil.getNullStr(ff.getDescription());
            }
            // 如果默认不是置为空值
            if (!VAL_BLANK.equals(defaultValue)) {
                // Privilege privilege = new Privilege();
                UserDb ud = new UserDb();
                ud = ud.getUserDb(userName);
                DeptUserDb udd = new DeptUserDb();
                Vector vdept = udd.getDeptsOfUser(ud.getName());
                if (vdept != null && vdept.size() > 0) {
                    String deptCode = ((DeptDb) vdept.get(0)).getCode();
                    DeptDb dd = new DeptDb();
                    dd = dd.getDeptDb(deptCode);
                    deptName = dd.getName();
                }
            }
        }
        return deptName;
    }

    /**
     *
     * @Description:
     * @param deptCode
     * @param childrens
     * @param isInclude 包含本身
     * @return
     * @throws ErrMsgException
     */
    public JSONArray getDeptNameAsOptions(String deptCode, JSONArray childrens, boolean isInclude) throws ErrMsgException {
        if (isInclude) { // 是否包含该部门
            childrens.put(getDeptNameAsOptionValue(deptCode));
        }
        /*
         * 获取该部门下的子部门
         */
        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(deptCode);
        Vector<DeptDb> children = deptChildrenCache.getDirList();
        int size = children.size();
        if (size == 0) {
            return childrens;
        }
        /*
         * 遍历获取子节点的部门
         */
        for (DeptDb childlf : children) {
            getDeptNameAsOptions(childlf.getCode(), childrens, true);
        }
        return childrens;
    }

    /**
     *
     * @Description:
     * @param deptCode
     * @param childrens
     * @param isInclude 包含本身
     * @return
     * @throws ErrMsgException
     */
    public JSONArray getDeptNameAsOptionsOnlyChildren(String deptCode,JSONArray childrens, boolean isInclude) throws ErrMsgException {
        if (isInclude) { // 是否包含该部门
            childrens.put(getDeptNameAsOptionValue(deptCode));
        }
        /*
         * 获取该部门下的子部门
         */
        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(deptCode);
        Vector<DeptDb> children = deptChildrenCache.getDirList();
        if (children.size() == 0) {
            return childrens;
        }
        /*
         * 遍历获取子节点的部门
         */
        for (DeptDb childlf : children) {
            childrens.put(getDeptNameAsOptionValue(childlf.getCode()));
        }
        return childrens;
    }

    public JSONObject getDeptNameAsOptionValue(String deptCode) {
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
        /*
        String code = dd.getCode();
        String name = dd.getName();
        String parentCode = dd.getParentCode();
        */
        JSONObject children = new JSONObject();
        try {
            String deptName = "";
            int layer = dd.getLayer();
            String blank = "";
            int d = layer-1;
            for (int i=0; i<d; i++) {
                blank += "　";
            }
            if (dd.getChildCount()>0) {
                deptName = blank + "╋ " + dd.getName();
            }
            else {
                deptName += blank + "├『" + dd.getName();
            }

            children.put("deptName", dd.getName());
            children.put("deptCode", dd.getCode());
            children.put("name", deptName);
            children.put("value", dd.getCode());
            children.put("parentCode", dd.getParentCode());
        } catch (JSONException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return children;
    }

    /**
     * 获取手机端
     * @Description:
     * @param userName
     * @param ff
     * @return
     */
    @Override
    public String getControlOptions(String userName, FormField ff) {
        String res = "";
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        String unitcode = ud.getUnitCode();
        // 可能是用admin账户进行测试
        if (unitcode==null) {
            unitcode = DeptDb.ROOTCODE;
        }
        String deptCode = unitcode;
        boolean isInclude = true; // 是否包含指定的部门
        String defaultDept = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if ("".equals(defaultDept)) {
            defaultDept = StrUtil.getNullString(ff.getDescription());
        }
        if (!"".equals(defaultDept)) {
            if ("my".equals(defaultDept)) {
                DeptUserDb dud = new DeptUserDb();
                Iterator<DeptDb> ir = dud.getDeptsOfUser(userName).iterator();
                if (ir.hasNext()) {
                    DeptDb lf = ir.next();
                    deptCode = lf.getCode();
                }
            }
            else if (defaultDept.startsWith("{$")) {
                // 解析出对应的字段名
                String fieldName = "";
                String[] arr = defaultDept.split(",");
                for(String str : arr){
                    if(str.startsWith("{$")){
                        fieldName = str.substring(2, str.length()-1);
                    }
                }
                if (iFormDAO!=null) {
                    deptCode = iFormDAO.getFieldValue(fieldName);
                }
            }
            else if (defaultDept.startsWith("#")){
                String[] arr = defaultDept.split(",");
                for(String str : arr){
                    if(str.startsWith("#")){
                        deptCode = str.substring(1);
                    }
                }
                isInclude = false;
            }
        }
        // 由于新增了仅仅显示本部(不显示父节点的信息), 不显示子部门, 要进行判断
        // boolean hideParent = false;  // 手机端好像不显示父节点信息,不用处理
        boolean onlyChildren = false;
        String[] arr = defaultDept.split(",");
        for(String str : arr){
            if(str.equalsIgnoreCase("onlyChildren")){
                onlyChildren = true;
            }
    		/*
    		if(str.equalsIgnoreCase("hideParent")){
    			hideParent = true;
    		}*/
        }

        JSONArray childrens = new JSONArray();
        try {
            res = "<option value=''>" + ConstUtil.NONE + "</option>";

            if(onlyChildren){
                res = getDeptNameAsOptionsOnlyChildren(deptCode, childrens, isInclude).toString();
            }else{
                res = getDeptNameAsOptions(deptCode, childrens, isInclude).toString();
            }

            JSONArray ary = new JSONArray();
            JSONObject children = new JSONObject();
            children.put("name", ConstUtil.NONE);
            children.put("value", "");
            ary.put(children);

            JSONArray jsonAry = new JSONArray(res);
            for (int i=0; i<jsonAry.length(); i++) {
                ary.put(jsonAry.get(i));
            }
            res = ary.toString();
        } catch (ErrMsgException | JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return res;
    }

    @Override
    public String getValueByName(FormField formField, String name) {
        /*String[] newDeptName = StrUtil.split(name, "\\\\");
        if (newDeptName==null) {
            LogUtil.getLog(getClass()).error("部门:" + name + " 为空！");
            return "";
        }*/

        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        Department department = departmentService.getDepartmentByName(name);
        if (department!=null) {
            return department.getCode();
        }

        // 如果未找到，则可能导入时可能用的是全路径名：行政部\秘书科\秘书一组
        Map<String, String> map = departmentService.getFulleNameMap();

        /*String deptName = "";

        int depLevel = newDeptName.length;
        for (int j = 0; j < depLevel; j++) {
            deptName += "".equals(deptName) ? newDeptName[j] : ("-" + newDeptName[j]);
        }*/
        String deptCode = map.get(name);
        if (deptCode == null || "".equals(deptCode)) {
            LogUtil.getLog(getClass()).error("部门：" + name + " 不存在！");
            return "";
        }

        return deptCode;
    }


    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     * @param request
     * @param ff 当前被查询的字段
     * @param value
     * @param isBlur 是否模糊查询
     * @return
     */
    @Override
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
        // 包含时，取出本部门及所有子部门
        if ("".equals(value)){
            return "";
        }
        if (isBlur) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(value);
            Vector vt = new Vector();
            try {
                dd.getAllChild(vt, dd);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            StringBuffer sb = new StringBuffer();
            sb.append(StrUtil.sqlstr(value));
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                dd = (DeptDb)ir.next();
                StrUtil.concat(sb, ",", StrUtil.sqlstr(dd.getCode()));
            }
            return sb.toString();

/*			return "select code from department where code=" +
          		StrUtil.sqlstr(value) + " or parentCode=" + StrUtil.sqlstr(value);*/
        }
        else {
            return "";
        }
    }
}
