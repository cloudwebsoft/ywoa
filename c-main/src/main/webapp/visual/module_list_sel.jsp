<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudweb.oa.api.ISQLCtl" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.IModuleFieldSelectCtl" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    /*
    - 功能描述：表单域选择框
    - 访问规则：如果传入了条件conds，则从父窗口中取值，传入sql语句中的{$fieldName}或{@fieldName}其中@表示like条件
    - 过程描述：
    - 注意事项：
    - 创建者：fgf
    - 创建时间：
    ==================
    - 修改者：fgf
    - 修改时间：2013.09.15
    - 修改原因：
    - 修改点：

    - 修改者：fgf
    - 修改时间：2016.02.17
    - 修改原因：
    增加{$cwsCurUser}表示当前用户
    可以在条件中增加cws_status字段
    - 修改点：
    */
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");

    String moduleCode = ParamUtil.get(request, "formCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(moduleCode);

    FormDb fd = new FormDb();
    fd = fd.getFormDb(msd.getString("form_code"));
    if (!fd.isLoaded()) {
        out.print(StrUtil.Alert_Back("表单不存在！"));
        return;
    }

    ModulePrivDb mpd = new ModulePrivDb(moduleCode);
    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
    boolean isModuleFieldSelectCtlCheckPrivilege = cfg.getBooleanProperty("isModuleFieldSelectCtlCheckPrivilege");
    if (isModuleFieldSelectCtlCheckPrivilege) {
        if (!mpd.canUserSee(privilege.getUser(request))) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    // 用于传过滤条件
    request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

    String orderBy = ParamUtil.get(request, "orderBy");
    if ("".equals(orderBy)) {
        orderBy = "id";
    }
    String sort = ParamUtil.get(request, "sort");
    if ("".equals(sort)) {
        sort = "desc";
    }

    String byFieldName = ParamUtil.get(request, "byFieldName");
    String showFieldName = ParamUtil.get(request, "showFieldName");

    String openerFormCode = ParamUtil.get(request, "openerFormCode");
    if ("".equals(openerFormCode)) {
        out.print(SkinUtil.makeErrMsg(request, "openerFormCode不能为空"));
        return;
    }
    String openerFieldName = ParamUtil.get(request, "openerFieldName");

    int mode = 1; // 默认选择窗体

    FormDb openerFd = new FormDb();
    openerFd = openerFd.getFormDb(openerFormCode);
    if (!openerFd.isLoaded()) {
        out.print(SkinUtil.makeErrMsg(request, "表单：" + openerFormCode + " 不存在"));
        return;
    }
    FormField openerField = openerFd.getFormField(openerFieldName);
    if (openerField == null) {
        out.print(SkinUtil.makeErrMsg(request, "字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在"));
        return;
    }
    JSONObject json = null;
    JSONArray mapAry = new JSONArray();
    String filter = "";
    try {
        // System.out.println(getClass() + " openerField.getDescription()=" + openerField.getDescription());
        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
        String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
        json = new JSONObject(desc);
        filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));

        if ("none".equals(filter)) {
            filter = "";
        }
        request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);

        /*
        String sourceFormCode = json.getString("sourceFormCode");
        String byFieldName = json.getString("idField");
        String showFieldName = json.getString("showField");
        */
        mapAry = (JSONArray) json.get("maps");

        if (json.has("mode")) {
            mode = json.getInt("mode");
        }
    } catch (JSONException e) {
        // "json 格式非法";
        e.printStackTrace();
    }

    String querystr = "";

    int pagesize = 20;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    // 过滤条件
    String conds = filter;

    String action = ParamUtil.get(request, "action");

    querystr = "op=" + op + "&action=" + action + "&formCode=" + moduleCode + "&orderBy=" + orderBy + "&sort=" + sort + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

    String querystrForSort = "op=" + op + "&formCode=" + moduleCode + "&filter=" + StrUtil.UrlEncode(conds) + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

    // String sql = "select distinct form.id from " + fd.getTableNameByForm() + " form, flow f ";
    String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";

    String urlStrFilter = "";

    // 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值，再作为参数重定向回本页面传入sql条件参数中
    if (!"".equals(conds)) {
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        boolean isFound = false;
        Matcher m = p.matcher(conds);
        // out.print(conds + "<BR>");
        // 如果尚未取值，则从其父窗口中取值，然后重定向回来赋值给条件中的{$fieldName}
        if (!"afterGetClientValue".equals(action)) {
            StringBuffer urlStrBuf = new StringBuffer();
            if ("search".equals(op)) {
                for (FormField ff : fd.getFields()) {
                    String value = ParamUtil.get(request, ff.getName());
                    String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
                    if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "_cond=" + name_cond);
                        if ("0".equals(name_cond)) {
                            // 时间段
                            String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                            String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                            if (!"".equals(fDate)) {
                                urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "FromDate=" + fDate);
                            }
                            if (!"".equals(tDate)) {
                                urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "ToDate=" + fDate);
                            }
                        } else {
                            // 时间点
                            String d = ParamUtil.get(request, ff.getName());
                            if (!"".equals(d)) {
                                urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(d));
                            }
                        }
                    } else if (ff.getType().equals(FormField.TYPE_SELECT)) {
                        String[] ary = ParamUtil.getParameters(request, ff.getName());
                        if (ary != null) {
                            for (String s : ary) {
                                if (!"".equals(s)) {
                                    urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(s));
                                    urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "_cond=" + StrUtil.UrlEncode(name_cond));
                                }
                            }
                        }
                    } else {
                        if (!"".equals(value)) {
                            urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(value));
                            urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "_cond=" + StrUtil.UrlEncode(name_cond));
                        }
                    }
                }

                int cws_flag = ParamUtil.getInt(request, "cws_flag", -1);
                String cws_flag_cond = ParamUtil.get(request, "cws_flag_cond");
                StrUtil.concat(urlStrBuf, "&", "cws_flag_cond=" + cws_flag_cond);
                StrUtil.concat(urlStrBuf, "&", "cws_flag=" + cws_flag);
                int cws_status = ParamUtil.getInt(request, "cws_status", -10000);
                String cws_status_cond = ParamUtil.get(request, "cws_status_cond");
                StrUtil.concat(urlStrBuf, "&", "cws_status=" + cws_status);
                StrUtil.concat(urlStrBuf, "&", "cws_status_cond=" + cws_status_cond);
            }
%>
<script>
    var condStr = "";
</script>
<%
    while (m.find()) {
        String fieldName = m.group(1);

        if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
                || "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName)) {
            isFound = true;
            continue;
        }
        // 当条件为包含时，fieldName以@开头
        if (fieldName.startsWith("@")) {
            fieldName = fieldName.substring(1);
        }

        String fieldNameReal = fieldName;
        if ("parentId".equals(fieldName) || "mainId".equals(fieldName)) {
            fieldNameReal = "cws_id";
        }
%>
<script>
    if (window.opener.o("<%=fieldNameReal%>") == null) {
        console.error("条件字段：<%=fieldName%>在表单中不存在！");
    } else {
        if (condStr == "")
            condStr = "<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldNameReal%>").value);
        else
            condStr += "&<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldNameReal%>").value);
    }
</script>
<%
        isFound = true;
    }

    if (isFound) {
%>
<script>
    window.location.href = "module_list_sel.jsp?action=afterGetClientValue&<%=querystr%>&<%=urlStrBuf%>&CPages=<%=curpage%>&" + condStr;
</script>
<%
                return;
            } else {
                String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), conds);
                if (ary[0] != null) {
                    conds = ary[0];
                }
            }
        } else {
            String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), conds);
            if (ary[0] != null) {
                // conds = ary[0];
                urlStrFilter = ary[1];
            }
        }
    }

    // 表单域选择控件中可能有传过来的filter，所以还不能用SQLBuiler中的getModuleListSqlAndUrlStr方法
    String urlStr = "";
    String query = ParamUtil.get(request, "query");
    if (!"".equals(query)) {
        sql = query;
    } else {
        op = "search";
        String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        sql = ary[0];
        urlStr = ary[1];
    }

    if ("".equals(urlStr)) {
        urlStr = urlStrFilter;
    } else {
        urlStr += "&" + urlStrFilter;
    }

    if (!"".equals(urlStr)) {
        querystr += "&" + urlStr;
        querystrForSort += "&" + urlStr;
    }

    String[] fields = msd.getColAry(false, "list_field");
    String[] fieldsWidth = msd.getColAry(false, "list_field_width");
    String[] fieldsTitle = msd.getColAry(false, "list_field_title");
	DebugUtil.i(getClass(), "sql", sql);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <style>
        .main-content {
            margin: 5px 20px;
        }
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
        .cond-span {
            display: inline-block;
            float: left;
            text-align: left;
            width: 300px;
            height: 32px;
        }
        .condBtnSearch {
            display: inline-block;
            float: left;
        }
        .cond-title {
            margin: 0 5px;
        }

        <%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script>
        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";

        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";

            window.location.href = "module_list_sel.jsp?op=<%=op%>&formCode=<%=msd.getString("form_code")%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystrForSort%>";
        }

        function sel(id, sth) {
            if ("<%=mode%>" == "1") {
                window.opener.setIntpuObjValue(id, sth);
            } else {
                // var obj = window.opener.o("<%=openerFieldName%>");
                window.opener.$("#<%=openerFieldName%>").empty().append("<option id='" + id + "' value='" + id + "'>" + sth + "</option>").trigger('change');
            }
            // 在doFuns中已实现，且为SQL宏控件调用了事件方法，另外还生成了校验的JS
            // window.opener.onSelect<%=openerFieldName %>(id, sth);
            window.close();
        }

        // 不放在页尾，因为服务器卡的时候，如果页面加载还没完成就点了“选择”，会导致JS出错：setOpenerFieldValue is not defined
        function setOpenerFieldValue(openerField, val, isMacro, sourceValue, checkJs) {
            var obj = window.opener.o('helper_' + openerField);
            if (obj) {
                obj.parentNode.removeChild(obj);
            }

            if (isMacro) {
                window.opener.replaceValue(openerField, val, sourceValue, checkJs);
            } else {
                var obj = window.opener.o(openerField);
                if (obj) {
                    if (obj.getAttribute("type") == "radio") {
                        window.opener.setRadioValue(obj.name, val);
                    } else if (obj.getAttribute("type") == "checkbox") {
                        window.opener.setCheckboxChecked(obj.name, val);
                    } else {
                        // obj.value = val;
                        if (obj.tagName != 'SPAN') {
                            obj.value = val;
                        }
                        else {
                            // 不可写字段
                            obj.innerHTML = val;
                        }
                    }
                } else {
                    if (isIE()) {
                        console.log("字段：" + openerField + " 不存在！")
                    }
                }

                var objShow = window.opener.o(openerField + "_show");
                if (objShow) {
                    $(objShow).html(val);
                }
            }
        }
    </script>
</head>
<body>
<%@ include file="module_sel_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<div class="spacerH"></div>
<div class="main-content">
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr>
        <td height="23" align="center">
            <form id="searchForm" class="search-form" action="module_list_sel.jsp" method="get">
                &nbsp;
                <%
                    boolean isShowUnitCode = false;
					/*
					Vector vt = privilege.getUserAdminUnits(request);
					if (vt.size()>0) {
						isShowUnitCode = true;
						if (vt.size()==1) {
							DeptDb dd = (DeptDb)vt.elementAt(0);
							// 只有一个单位，且是本单位节点
							if (dd.getCode().equals(privilege.getUserUnitCode(request))) {
								isShowUnitCode = false;
							}
						}
					}
					*/

                    String myUnitCode = privilege.getUserUnitCode(request);
                    DeptDb dd = new DeptDb();
                    dd = dd.getDeptDb(myUnitCode);

                    Vector vtUnit = new Vector();
                    vtUnit.addElement(dd);

                    // 向下找两级单位
                    DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
                    java.util.Vector vt = dl.getDirList();
                    Iterator irDept = vt.iterator();
                    while (irDept.hasNext()) {
                        dd = (DeptDb) irDept.next();
                        if (dd.getType() == DeptDb.TYPE_UNIT) {
                            vtUnit.addElement(dd);
                            DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
                            Iterator ir2 = dl2.getDirList().iterator();
                            while (ir2.hasNext()) {
                                dd = (DeptDb) ir2.next();
                                if (dd.getType() == DeptDb.TYPE_UNIT) {
                                    vtUnit.addElement(dd);
                                }

                                DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
                                Iterator ir3 = dl3.getDirList().iterator();
                                while (ir3.hasNext()) {
                                    dd = (DeptDb) ir3.next();
                                    if (dd.getType() == DeptDb.TYPE_UNIT) {
                                        vtUnit.addElement(dd);
                                    }
                                }
                            }
                        }
                    }

                    String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
                    String[] btnNames = StrUtil.split(btnName, ",");
                    String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
                    String[] btnScripts = StrUtil.split(btnScript, "#");

                    ArrayList<String> dateFieldNamelist = new ArrayList<String>();
                    MacroCtlMgr mm = new MacroCtlMgr();
                    int len = 0;
                    boolean isQuery = false;
                    if (btnNames != null) {
                        len = btnNames.length;
                        for (int i = 0; i < len; i++) {
                            if (btnScripts[i].startsWith("{")) {
                                Map<String, String> checkboxGroupMap = new HashMap<String, String>();
                                JSONObject jsonBtn = new JSONObject(btnScripts[i]);
                                if (jsonBtn.get("btnType").equals("queryFields")) {
                                    String condFields = (String) jsonBtn.get("fields");
                                    String condTitles = "";
                                    if (jsonBtn.has("titles")) {
                                        condTitles = (String) jsonBtn.get("titles");
                                    }
                                    String[] fieldAry = StrUtil.split(condFields, ",");
                                    if (fieldAry.length > 0) {
                                        isQuery = true;
                                    }
                                    String[] titleAry = StrUtil.split(condTitles, ",");
                                    for (int j = 0; j < fieldAry.length; j++) {
                                        String fieldName = fieldAry[j];
                                        String fieldTitle = "#";
                                        if (titleAry != null) {
                                            fieldTitle = titleAry[j];
                                            if ("".equals(fieldTitle)) {
                                                fieldTitle = "#";
                                            }
                                        }

                                        if (!jsonBtn.has(fieldName)) {
                                            continue;
                                        }
                                        String condType = (String) jsonBtn.get(fieldName);
                                        CondUnit condUnit = CondUtil.getCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist);
                                        out.print("<span class=\"cond-span\">");
                                        out.print("<span class=\"cond-title\">");
                                        out.print(condUnit.getFieldTitle());
                                        out.print("</span>");
                                        out.print(condUnit.getHtml());
                                        out.print("</span>");
                                        out.print("<script>");
                                        out.print(condUnit.getScript());
                                        out.print("</script>");
                                    }
                                }
                            }
                        }

                        if (isQuery) {
                %>
                <input type="hidden" name="op" value="search"/>
                <input type="hidden" name="formCode" value="<%=moduleCode%>"/>
                <input type="hidden" name="orderBy" value="<%=orderBy %>"/>
                <input type="hidden" name="sort" value="<%=sort %>"/>
                <input type="hidden" name="filter" value="<%=StrUtil.HtmlEncode(conds) %>"/>
                <input type="hidden" name="byFieldName" value="<%=byFieldName %>"/>
                <input type="hidden" name="showFieldName" value="<%=showFieldName %>"/>
                <input type="hidden" name="openerFormCode" value="<%=openerFormCode %>"/>
                <input type="hidden" name="openerFieldName" value="<%=openerFieldName %>"/>
                <span class="cond-span"><input class="btn btn-default" type="submit" value="搜索"/></span>
                <%
                        }
                    }
                %>
            </form>
        </td>
    </tr>
</table>
<%
    com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

    ListResult lr;
    try {
        lr = fdao.listResult(msd.getString("form_code"), sql, curpage, pagesize);
    }
    catch (ErrMsgException e) {
        out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
        e.printStackTrace();
        return;
    }
    long total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null) {
        ir = v.iterator();
    }
    paginator.init(total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table class="percent98 p9" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="left"><input type="button" class="btn btn-default" value="清空" onclick="window.opener.setIntpuObjValue('', ' ');window.close()" style="margin-bottom:5px"/></td>
        <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %>
        </b> 条　每页显示 <b><%=paginator.getPageSize() %>
        </b> 条　页次 <b><%=curpage %>/<%=totalpages %>
        </b></td>
    </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
    <thead>
    <tr align="center">
        <%
            len = 0;
            if (fields != null) {
                len = fields.length;
            }
            for (int i = 0; i < len; i++) {
                String fieldName = fields[i];
                if ("colOperate".equals(fieldName)) {
                    continue;
                }
                String fieldTitle = fieldsTitle[i];

                Object[] aryTitle = CondUtil.getFieldTitle(fd, fieldName, fieldTitle);
                String title = (String) aryTitle[0];
                boolean sortable = (Boolean) aryTitle[1];

                String doSort;
                if (!sortable) {
                    doSort = "";
                }
                else {
                    doSort = "doSort('" + fieldName + "')";
                }
        %>
        <td class="tabStyle_1_title" nowrap width="<%=fieldsWidth[i]%>" style="cursor:hand" onclick="<%=doSort%>">
            <%=title%>
            <%
                if (orderBy.equals(fieldName)) {
                    if (sort.equals("asc")) {
                        out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                    } else {
                        out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                    }
                }
            %>
        </td>
        <%}%>
        <td class="tabStyle_1_title" nowrap title="按时间排序" style="cursor:hand" onClick="doSort('id')">操作
            <%
                if (orderBy.equals("id")) {
                    if (sort.equals("asc")) {
                        out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                    } else {
                        out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                    }
                }
            %>
        </td>
    </tr>
    </thead>
    <%
        WorkflowDb wf = new WorkflowDb();
        int k = 0;
        UserMgr um = new UserMgr();
        while (ir != null && ir.hasNext()) {
            fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
            RequestUtil.setFormDAO(request, fdao);
            k++;
            long id = fdao.getId();
    %>
    <tr align="center" class="highlight">
        <%
            String showValue = "";
            boolean isShowFieldFound = false;
            for (int i = 0; i < len; i++) {
                String fieldName = fields[i];
                if ("colOperate".equals(fieldName)) {
                    continue;
                }
        %>
        <td align="left">
            <a href="module_show.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=moduleCode%>&formCode=<%=msd.getString("form_code")%>&isShowNav=0" target="_blank">
                <%
                    if (fieldName.startsWith("main:")) {
                        String[] subFields = fieldName.split(":");
                        if (subFields.length == 3) {
                            FormDb subfd = new FormDb(subFields[1]);
                            com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                            FormField subff = subfd.getFormField(subFields[2]);
                            String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
                            StringBuilder sb = new StringBuilder();
                            JdbcTemplate jt = new JdbcTemplate();
                            try {
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
                                e.printStackTrace();
                            }
                            out.print(sb.toString());
                        }
                    } else if (fieldName.startsWith("other:")) {
                        out.print(StrUtil.getNullStr(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName)));
                    } else if (fieldName.equals("cws_creator")) {
                        String realName = "";
                        if (fdao.getCreator() != null) {
                            UserDb user = um.getUserDb(fdao.getCreator());
                            if (user != null) {
                                realName = user.getRealName();
                            }
                        }
                        out.print(realName);
                    } else if (fieldName.equals("cws_progress")) {
                        out.print(fdao.getCwsProgress());
                    } else if (fieldName.equals("cws_status")) {
                        out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
                    } else if (fieldName.equals("cws_flag")) {
                        out.print(fdao.getCwsFlag());
                    } else if (fieldName.equals("ID")) {
                        out.print(fdao.getId());
                    } else if (fieldName.equals("cws_id")) {
                        out.print(StrUtil.getNullStr(fdao.getCwsId()));
                    } else if (fieldName.equals("cws_create_date")) {
                        out.print(DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd"));
                    } else if (fieldName.equals("flow_begin_date")) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            out.print(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd"));
                        }
                    } else if (fieldName.equals("flow_end_date")) {
                        int flowId = fdao.getFlowId();
                        if (flowId != -1) {
                            wf = wf.getWorkflowDb(flowId);
                            out.print(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd"));
                        }
                    } else {
                        FormField ff = fdao.getFormField(fieldName);
                        if (ff != null) {
                            String tempValue = "";
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    tempValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                                }
                            } else {
                                tempValue = FuncUtil.renderFieldValue(fdao, ff);
                            }
                            out.print(tempValue);
                            if (!isShowFieldFound && ff.getName().equals(showFieldName)) {
                                isShowFieldFound = true;
                                showValue = tempValue;
                            }
                        } else {
                %>
                <%=fieldName%>不存在
                <%
                        }
                    }
                %>
            </a>
        </td>
        <%}%>
        <td width="50px">
            <%
                String byValue = "";
                if (byFieldName.equals("id")) {
                    byValue = "" + id;
                } else {
                    byValue = fdao.getFieldValue(byFieldName);
                }
                // @task:id在设置宏控件时，还不能被配置为被显示
                if (showFieldName.equals("id")) {
                    showValue = "" + id;
                    isShowFieldFound = true;
                }

                if (!isShowFieldFound) {
                    FormField ff = fdao.getFormField(showFieldName);
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
                        }
                    } else {
                        showValue = fdao.getFieldValue(showFieldName);
                    }
                }

                ParamChecker pck = new ParamChecker(request);
                String funs = "";
                for (int i = 0; i < mapAry.length(); i++) {
                    json = (JSONObject) mapAry.get(i);
                    String destF = (String) json.get("destField");    // 父页面
                    String sourceF = (String) json.get("sourceField");    // module_list_sel.jsp页面
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
                    String setValue = "";
                    if ("id".equals(sourceF)) {
                        setValue = String.valueOf(fdao.getId());
                    } else if ("cws_id".equals(sourceF)) {
                        setValue = fdao.getCwsId();
                    } else {
                        setValue = fdao.getFieldValue(sourceF);
                    }
                    String checkJs = com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, tempFf);
                    IFormMacroCtl ifmc = null;
                    // 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
                    if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                        tempFf.setValue(setValue);
                        isMacro = true;
                        request.setAttribute("cwsMapSourceFormField", fdao.getFormField(sourceF));
                        // setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
                        ifmc = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl();
                        setValue = ifmc.convertToHTMLCtl(request, tempFf);
                    }
                    // 增加辅助表单域，以免算式中出现引号问题，helper为outerHTML
            %>
            <textarea id="helper<%=k%>_<%=i%>" style="display:none"><%=setValue %></textarea>
            <textarea id="helperSource<%=k%>_<%=i%>" style="display:none"><%=fdao.getFieldValue(sourceF) %></textarea>
            <textarea id="helperJs<%=k%>_<%=i%>" style="display:none"><%=checkJs%></textarea>
            <%
                    // System.out.println(getClass() + " " + destF + "-" + setValue);
                    funs += "setOpenerFieldValue('" + destF + "', o('helper" + k + "_" + i + "').value," + isMacro + ", o('helperSource" + k + "_" + i + "').value, o('helperJs" + k + "_" + i + "').value);\n";
                    if (ifmc instanceof ISQLCtl) {
                        // 调用onSQLCtlRelateFieldChange_，以使得控件被映射时能够生成
                        // 因为当convertToHtmlCtl时，生成的是input，且已经有value，
                        // 而在macro_sql_ctl_js.jsp是用setInterval来检测变化的，而值在映射过来后并不变化，所以不这样处理，无法生成控件，而只能看到一个带有字段值的文本框
                        funs += "window.opener.onSQLCtlRelateFieldChange_" + destF + "();\n";
                    }
                }
            %>
            <script>
                function doFuns<%=k%>() {
                    // 似乎不生效
                    $('body').showLoading();

                    <%=funs%>
                }
            </script>
            <textarea id="helperJsShowValue<%=k%>" style="display:none"><%=showValue%></textarea>
            <a href="javascript:sel('<%=byValue%>', o('helperJsShowValue<%=k%>').value);doFuns<%=k%>();doAtferSel();">选择</a></td>
    </tr>
    <%
        }
    %>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
    <tr>
        <td width="50%" height="23" align="left">&nbsp;</td>
        <td width="50%" align="right"><%
            out.print(paginator.getCurPageBlock("module_list_sel.jsp?" + querystr));
        %></td>
    </tr>
</table>
</div>
</body>
<script>
    function initCalendar() {
        <%for (String ffname : dateFieldNamelist) {%>
        $('#<%=ffname%>').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });

        $('#<%=ffname%>').attr('autocomplete', 'off');
        <%}%>
    }

    $(function () {
        initCalendar();
    });

    function doAtferSel() {
        // 应用显示规则
        var isDoViewJS = true;
        try {
            isDoViewJS = window.opener.isDoViewJSOnModuleListSel();
        } catch (e) {
        }

        if (isDoViewJS) {
            try {
                window.opener.doViewJS();
            } catch (e) {
                consoleLog(e);
            }
        }
    }

    $('.tabStyle_1').find('.tabStyle_1_title').removeClass('tabStyle_1_title');
    $('.tabStyle_1').addClass('layui-table').removeClass('tabStyle_1');
</script>
</html>
