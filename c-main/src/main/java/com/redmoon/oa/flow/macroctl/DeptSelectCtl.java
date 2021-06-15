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

import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.sys.DebugUtil;
import org.json.*;
import java.util.Iterator;

/**
 * <p>Title:部门选择宏控件，可以与用户选择列表控件联动 </p>
 *
 * <p>Description:
 * 默认值为my，表示仅用户所在的部门（含兼职的部门）
 * 默认值为user_admin_dept，表示用户可管理的部门
 * 默认值为{user}，表示可以与用户选择列表控件联动，后者的字段名为user
 * 默认值为{$fieldName}，表示为字段fieldName的值所对应部门下的子部门（不包含对应部门）
 * 默认值为#val，表示为val所对应部门下的子部门（不包含该部门）
 * 默认值中包含 hideparent,不显示父级及以上层级部门. 如:#val,hideParent,表示为val所对应的部门下的子部门（不包含对应部门）不显示上级部门信息
 * 默认值中包含#val,onlyChildren,表示为val所对应部门下的子部门（不包含对应部门）, 而且子部门下级部门不做显示
 * 默认值为blank，表示默认置为空，否则默认为当前用户的部门
 *  </p>
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
        if (!deptCode.equals("") && !deptCode.equals("user_admin_dept") && !deptCode.startsWith("{")) {
            DeptMgr dm = new DeptMgr();

            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            if (!dd.isLoaded()) {
                return "";
            }

            String deptName = dd.getName();
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
                    deptName = dd.getName() + "\\" + deptName;
                    pCode = dd.getParentCode();
                }
            }
            return deptName;
        }
        else
            return "";
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
        Privilege pvg = new Privilege();

        String str = "";

        boolean isInclude = true; // 是否包含指定的部门
        boolean isOnlyChildren = false; // 执行部门下的子部门, 但是不显示子部门下的部门

        /*
         * 判断是否默认值中包含了隐藏上级部门信息 2018-8-1
         */
        String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        // String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if ("".equals(defaultValue)) {
            defaultValue = StrUtil.getNullStr(ff.getDescription());
        }

        DeptMgr dm = new DeptMgr();
        DeptDb lf = null;
        String strRelateUserCtl = "";

        String[] defaultArr = defaultValue.split(",");
        for(String defaultDept : defaultArr) {
            if(defaultDept.length() == 0) {
                continue;
            }else {
                if (defaultDept.equals("my")) {
                    DeptUserDb dud = new DeptUserDb();
                    Iterator ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
                    if (ir.hasNext()) {
                        lf = (DeptDb) ir.next();
                    }
                } else if (defaultDept.startsWith("{$") && defaultDept.indexOf("}") != -1) {
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
                }

                if (defaultDept.equalsIgnoreCase("onlyChildren")) {
                    isOnlyChildren = true;
                }
                else if (defaultDept.startsWith("#")) {
                    String defaultDeptSetCode = defaultDept.substring(1);
                    lf = dm.getDeptDb(defaultDeptSetCode);
                    if (lf == null || !lf.isLoaded()) {
                        lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                    } else {
                        isInclude = false;
                    }
                } else {
                    lf = dm.getDeptDb(pvg.getUserUnitCode(request));
                }

                if (defaultDept.startsWith("{") && !defaultDept.startsWith("{$")) {
                    if (defaultDept.indexOf("}")!=-1) {
                        String field = defaultDept.substring(1, defaultDept.indexOf("}"));
                        FormDb fd = new FormDb();
                        fd = fd.getFormDb(ff.getFormCode());
                        FormField userff = fd.getFormField(field);
                        if (userff.getMacroType().equals("macro_user_select")) {
                            if (request.getAttribute("isDeptSelectJS") == null) {
                                strRelateUserCtl = "<script src='" + request.getContextPath() + "/flow/macro/macro_js_dept_select.jsp?formCode=" + userff.getFormCode() + "&deptField=" + ff.getName() + "&userField=" +
                                        field + "'></script>";
                                request.setAttribute("isDeptSelectJS", "y");
                            }
                        }
                        else {
                            // str += "关联控件类型不匹配，不是用户列表选择控件";
                        }
                    }
                }
            }
        }

        if (ff.isReadonly()) {
            str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' style='background-color:#eeeeee' onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'>";
        }
        else {
            str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "'>";
        }

        if (lf==null) {
            lf = dm.getDeptDb(pvg.getUserUnitCode(request));
        }

        DeptView dv = new DeptView(lf);
        StringBuffer outStr = new StringBuffer(100);

        outStr.append("<option value=''>" + BasicSelectCtl.NONE + "</option>");

        if(isOnlyChildren){
            try {
                if (StrUtil.getNullStr(ff.getDefaultValueRaw()).equals("user_admin_dept")){
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
                if (StrUtil.getNullStr(ff.getDefaultValueRaw()).equals("user_admin_dept"))
                    dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer());
                else
                    dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }

            // DebugUtil.i(DeptSelectCtl.class, "convertToHTMLCtl", outStr.toString());

            str += outStr;
            str += "</select>";
        }

        str += strRelateUserCtl;

        return str;
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
        if (StrUtil.getNullStr(ff.getValue()).equals("") || StrUtil.getNullStr(ff.getValue()).equals("user_admin_dept")|| StrUtil.getNullStr(ff.getValue()).equalsIgnoreCase("hideParent") || ff.getValue().startsWith("{")) {
            if (ff.getValue()!=null && !"".equals(ff.getValue()) && !"user_admin_dept".equals(ff.getValue()) && !ff.getValue().equalsIgnoreCase("hideParent")) {
                ff.setValue(ff.getDefaultValue()); // 启用默认值
            }
            else {
                String deptCode = ParamUtil.get(request, "deptCode");
                if (deptCode.equals("")) {
                    String defaultValue = StrUtil.getNullStr(ff.getDefaultValueRaw());
                    if ("".equals(defaultValue)) {
                        defaultValue = StrUtil.getNullStr(ff.getDescription());
                    }
                    // 如果默认不是置为空
                    if (!VAL_BLANK.equals(defaultValue)) {
                        // 如果request中没有，则置为当前用户所在部门
                        Privilege privilege = new Privilege();
                        UserDb ud = new UserDb();
                        ud = ud.getUserDb(privilege.getUser(request));
                        DeptUserDb udd = new DeptUserDb();
                        Vector vdept = udd.getDeptsOfUser(ud.getName());
                        if (vdept != null && vdept.size() > 0) {
                            ff.setValue(((DeptDb) vdept.get(0)).getCode());
                        }
                    }
                }
                else {
                    ff.setValue(deptCode);
                }
            }
        }
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库
        String deptCode = StrUtil.getNullStr(ff.getValue());
        String deptName = "";
        if (!deptCode.equals("")) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            deptName = dd.getName();
        }
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "','" + deptName + "','" + ff.getValue() + "');\n";
        return str;
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
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
            if (defaultDept.equals("my")) {
                DeptUserDb dud = new DeptUserDb();
                Iterator ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
                if (ir.hasNext()) {
                    lf = (DeptDb)ir.next();
                }
            }
            else if (defaultDept.startsWith("{$") && defaultDept.indexOf("}")!=-1) {
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
                    if(tempStr.equalsIgnoreCase("onlyChildren")){
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
                if (StrUtil.getNullStr(ff.getDefaultValueRaw()).equals("user_admin_dept")){
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
                if (StrUtil.getNullStr(ff.getDefaultValueRaw()).equals("user_admin_dept"))
                    outStr = dv.getUserAdminDeptAsOptions(request, outStr, lf, lf.getLayer());
                else
                    outStr = dv.getDeptAsOptions(outStr, lf, lf.getLayer(), isInclude);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("convertToHTMLCtl:" + e.getMessage());
            }
            str += outStr;
            str += "</select>";
        }

        if (defaultDept.startsWith("{") && !defaultDept.startsWith("{$")) {
            if (defaultDept.indexOf("}")!=-1) {
                String field = defaultDept.substring(1, defaultDept.indexOf("}") - 1);
                FormDb fd = new FormDb();
                fd = fd.getFormDb(ff.getFormCode());
                FormField userff = fd.getFormField(field);
                if (userff==null) {
                    DebugUtil.e(getClass(), "convertToHTMLCtlForQuery", field + " 不存在");

                }
                else if (userff.getMacroType().equals("macro_user_select")) {
                    if (request.getAttribute("isDeptSelectJS") == null) {
                        str += "<script src='" + request.getContextPath() + "/flow/macro/macro_js_dept_select.jsp?deptField=" + ff.getName() + "&userField=" +
                                field + "'></script>";
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

    public String getReplaceCtlWithValueScript(FormField ff) {
        String deptName = "var deptName='';\n";
        if (ff.getValue()!=null && !ff.getValue().equals("")) {
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
    public JSONArray getDeptNameAsOptions(String deptCode,JSONArray childrens, boolean isInclude) throws ErrMsgException {
        if (isInclude) { // 是否包含该部门
            childrens.put(getDeptNameAsOptionValue(deptCode));
        }
        DeptMgr dm = new DeptMgr();
        /*
         * 获取该部门下的子部门
         */
        Vector children = dm.getChildren(deptCode);
        int size = children.size();
        if (size == 0)
            return childrens;
        /*
         * 遍历获取子节点的部门
         */
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            getDeptNameAsOptions(childlf.getCode(),childrens, true);
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
        DeptMgr dm = new DeptMgr();
        /*
         * 获取该部门下的子部门
         */
        Vector children = dm.getChildren(deptCode);
        if (children.size() == 0)
            return childrens;
        /*
         * 遍历获取子节点的部门
         */
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            childrens.put(getDeptNameAsOptionValue( childlf.getCode() ));
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
            ex.printStackTrace();
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
            if (defaultDept.equals("my")) {
                DeptUserDb dud = new DeptUserDb();
                Iterator ir = dud.getDeptsOfUser(userName).iterator();
                if (ir.hasNext()) {
                    DeptDb lf = (DeptDb)ir.next();
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
            res = "<option value=''>" + BasicSelectCtl.NONE + "</option>";

            if(onlyChildren){
                res = getDeptNameAsOptionsOnlyChildren(deptCode, childrens, isInclude).toString();
            }else{
                res = getDeptNameAsOptions(deptCode, childrens, isInclude).toString();
            }

            JSONArray ary = new JSONArray();
            JSONObject children = new JSONObject();
            children.put("name", BasicSelectCtl.NONE);
            children.put("value", "");
            ary.put(children);

            JSONArray jsonAry = new JSONArray(res);
            for (int i=0; i<jsonAry.length(); i++) {
                ary.put(jsonAry.get(i));
            }
            res = ary.toString();
        } catch (ErrMsgException ex1) {
            ex1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String getValueByName(FormField formField, String name) {
        String newDeptName[] = name.split("\\\\");
        if (newDeptName==null) {
            LogUtil.getLog(getClass()).error("部门:" + name + " 为空！");
            return "";
        }

        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        Department department = departmentService.getDepartmentByName(name);
        if (department!=null) {
            return department.getCode();
        }

        String deptCode = "";

        // 如果未找到，则可能导入时可能用的是全路径名：行政部\秘书科\秘书一组
        Map<String, String> map = departmentService.getFulleNameMap();

        String deptName = "";

        int depLevel = newDeptName.length;
        for (int j = 0; j < depLevel; j++) {
            deptName += deptName.equals("") ? newDeptName[j] : ("-" + newDeptName[j]);
        }
        deptCode = map.get(deptName);
        if (deptCode == null || deptCode.equals("")) {
            LogUtil.getLog(getClass()).error("部门：" + deptName + " 不存在！");
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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
