<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormMgr" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.redmoon.oa.flow.FormParser" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    /*
    - 功能描述：为扩散选择功能而创建
    - 访问规则：
    - 过程描述：
    - 注意事项：
    - 创建者：
    - 创建时间：20200319
    ==================
    - 修改者：
    - 修改时间：
    - 修改原因：
    - 修改点：
    */
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    String op = ParamUtil.get(request, "op");
    
    
    String moduleCode = ParamUtil.get(request, "moduleCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDbOrInit(moduleCode);
    
    String formCode = msd.getString("form_code");
    
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(StrUtil.Alert_Back("表单不存在！"));
        return;
    }
    
    ModulePrivDb mpd = new ModulePrivDb(moduleCode);
    if (!mpd.canUserSee(privilege.getUser(request))) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "id";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";
    
    String querystr = "";
    
    int pagesize = 20;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    
    
    String action = ParamUtil.get(request, "action");
    
    querystr = "op=" + op + "&action=" + action + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&moduleCode=" + StrUtil.UrlEncode(moduleCode);
    
    String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";
    String urlStrFilter = "";
    
    MacroCtlMgr mm = new MacroCtlMgr();
    
    // 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
    request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
    
    String query = ParamUtil.get(request, "query");
    String keywords = "";
    String urlStr = "";
    
    if (!query.equals("")) {
        sql = query;
    } else if (op.equals("insearch")) {
        Iterator ir = fd.getFields().iterator();
        String cond = "";
        keywords = ParamUtil.get(request, "keywords");
        
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                continue;
            } else if (ff.getType().equals(FormField.TYPE_SELECT)) {
                continue;
            } else {
                if (!keywords.equals("")) {
                    if (cond.equals("")) {
                        cond += ff.getName() + " like " + StrUtil.sqlstr("%" + keywords + "%");
                    } else {
                        cond += " or " + ff.getName() + " like " + StrUtil.sqlstr("%" + keywords + "%");
                    }
                }
            }
        }
        if (!cond.equals(""))
            sql = sql + (sql.contains(" where ") ? " and " : " where ") + "(" + cond + ")";
        
        if (sql.toLowerCase().indexOf("cws_status") == -1) {
            if (sql.indexOf(" where ") == -1) {
                sql += " where cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            } else {
                sql += " and cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }
        }
        
        // System.out.print(getClass() + " sql=" + sql);
        
        sql += " order by " + orderBy + " " + sort;
    } else {
        op = "search";
        String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        sql = ary[0];
        urlStr = ary[1];
    }
    
    // out.print(sql);
    
    if ("".equals(urlStr)) {
        urlStr = urlStrFilter;
    } else {
        urlStr += "&" + urlStrFilter;
    }
    
    if (!urlStr.equals(""))
        querystr += "&" + urlStr;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .tSearch {
            background: url(../images/search.png) no-repeat left;
            border:0px;
            padding-left:12px;
            cursor:pointer;
            margin-top:3px;
            height:21px;
            width:21px;
        }
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]):not([type="submit"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
        .condSpan {
            display:inline-block;
            float:left;
            width:330px;
            height:32px;
            text-align: left;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery.bgiframe.js"></script>

    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    
    <script>
        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";

        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";

            window.location.href = "module_list_for_sel.jsp?op=<%=op%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystr%>&keywords=<%=keywords%>";
        }

        function sel(id, sth) {
            window.opener.setIntpuObjValue(id, sth);
            window.close();
        }
    </script>
</head>
<body>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr>
        <td height="23" align="center">
            <form id="searchForm" class="search-form" action="module_list_for_sel.jsp" method="get">
                &nbsp;
                <%
                    boolean isShowUnitCode = false;
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
                    String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
                    String[] btnBclasses = StrUtil.split(btnBclass, ",");
                    
                    ArrayList<String> list = new ArrayList<String>();
                    
                    FormMgr fm = new FormMgr();
                    
                    int len = 0;
                    boolean isQuery = false;
                    if (btnNames != null) {
                        len = btnNames.length;
                        for (int i = 0; i < len; i++) {
                            if (btnScripts[i].startsWith("{")) {
                                Map<String, String> checkboxGroupMap = new HashMap<String, String>();
                                JSONObject jsonBtn = new JSONObject(btnScripts[i]);
                                if (((String) jsonBtn.get("btnType")).equals("queryFields")) {
                                    isQuery = true;
                                    String condFields = (String) jsonBtn.get("fields");
                                    String[] fieldAry = StrUtil.split(condFields, ",");
                                    Iterator irKey = jsonBtn.keys();
                                    for (int j = 0; j < fieldAry.length; j++) {
                                        String fieldName = fieldAry[j];
                                        FormField ff = fd.getFormField(fieldName);
                                        String title;
                                        String condType = (String) jsonBtn.get(fieldName);
                                        String queryValue = ParamUtil.get(request, fieldName);
                                        if ("cws_status".equals(fieldName)) {
                                            title = "状态";
                                        } else if ("cws_flag".equals(fieldName)) {
                                            title = "冲抵状态";
                                        }
                                        else if ("ID".equals(fieldName)) {
                                            title = "ID";
                                        }
                                        else if ("flowId".equals(fieldName)) {
                                            title = "流程号";
                                        }
                                        else if ("flow:begin_date".equals(fieldName)) {
                                            title = "流程开始时间";
                                        }
                                        else if ("flow:end_date".equals(fieldName)) {
                                            title = "流程结束时间";
                                        }
                                        else {
                                            if (fieldName.startsWith("main:")) { // 关联的主表
                                                String[] aryField = StrUtil.split(fieldName, ":");
                                                String field = fieldName.substring(5);
                                                if (aryField.length == 3) {
                                                    FormDb mainFormDb = fm.getFormDb(aryField[1]);
                                                    ff = mainFormDb.getFormField(aryField[2]);
                                                    if (ff == null) {
                                                        out.print(fieldName + "不存在");
                                                        continue;
                                                    }
                                                    title = ff.getTitle();
                                                } else {
                                                    title = field + " 不存在";
                                                }
                                            } else if (fieldName.startsWith("other")) { // 映射的字段，多重映射不支持
                                                String[] aryField = StrUtil.split(fieldName, ":");
                                                if (aryField.length < 5) {
                                                    title = "<font color='red'>格式非法</font>";
                                                } else {
                                                    FormDb otherFormDb = fm.getFormDb(aryField[2]);
                                                    ff = otherFormDb.getFormField(aryField[4]);
                                                    if (ff == null) {
                                                        out.print(fieldName + "不存在");
                                                        continue;
                                                    }
                                                    title = ff.getTitle();
                                                }
                                            } else {
                                                // ff = fd.getFormField(fieldName);
                                                if (ff == null) {
                                                    out.print(fieldName + "不存在");
                                                    continue;
                                                }
                                                if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                                                    String desc = StrUtil.getNullStr(ff.getDescription());
                                                    if (!"".equals(desc)) {
                                                        title = desc;
                                                    } else {
                                                        title = ff.getTitle();
                                                    }
                                                    String chkGroup = StrUtil.getNullStr(ff.getDescription());
                                                    if (!"".equals(chkGroup)) {
                                                        if (!checkboxGroupMap.containsKey(chkGroup)) {
                                                            checkboxGroupMap.put(chkGroup, "");
                                                        } else {
                                                            continue;
                                                        }
                                                    }
                                                } else {
                                                    title = ff.getTitle();
                                                }
                                            }
                                            // 用于给convertToHTMLCtlForQuery辅助传值
                                            // ff.setCondType(condType);
                                        }
                %>
                <span class="condSpan">
                <%=title%>
                <%
                    if ("cws_status".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
                        if ("".equals(nameCond)) {
                            nameCond = condType;
                        }
                        int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);
                %>
                <select name="<%=fieldName%>_cond" style="display:none">
                    <option value="=" selected="selected">等于</option>
                </select>
                <select name='<%=fieldName%>'>
                    <option value='<%=SQLBuilder.CWS_STATUS_NOT_LIMITED%>'>不限</option>
                    <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%>
                    </option>
                    <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%>
                    </option>
                    <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'
                            selected><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%>
                    </option>
                    <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%>
                    </option>
                    <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%>
                    </option>
                </select>
                <script>
                    $(function () {
                        o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                        <%if (queryValueCwsStatus!=-20000) {%>
                        o("<%=fieldName%>").value = "<%=queryValueCwsStatus%>";
                        <%}	else {%>
                        o("<%=fieldName%>").value = "<%=msd.getInt("cws_status")%>";
                        <%}%>
                    });
                </script>
                <%
                } else if ("cws_flag".equals(fieldName)) {
                    String nameCond = ParamUtil.get(request, fieldName + "_cond");
                    if ("".equals(nameCond)) {
                        nameCond = condType;
                    }
                    int queryValueCwsFlag = ParamUtil.getInt(request, "cws_flag", -1);
                %>
                <select name="<%=fieldName%>_cond" style="display:none">
                    <option value="=" selected="selected">等于</option>
                </select>
                <select name='<%=fieldName%>'>
                    <option value='-1'>不限</option>
                    <option value='0'>否</option>
                    <option value='1'>是</option>
                </select>
                <script>
                    $(function () {
                        o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                        o("<%=fieldName%>").value = "<%=queryValueCwsFlag%>";
                    });
                </script>
                <%
                }
                else if ("ID".equals(fieldName)) {
                    String nameCond = ParamUtil.get(request, fieldName + "_cond");
                    if ("".equals(nameCond)) {
                        nameCond = condType;
                    }
                    String queryValueID = ParamUtil.get(request, "ID");
                %>
				          <select name="ID_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="ID" size="5" />
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueID%>";
                        });
						</script>
						<%
                        }
                        else if ("flowId".equals(fieldName)) {
                            String nameCond = ParamUtil.get(request, fieldName + "_cond");
                            if ("".equals(nameCond)) {
                                nameCond = condType;
                            }
                            String queryValueID = ParamUtil.get(request, "flowId");
                        %>
				          <select name="flowId_cond">
				            <option value="=" selected="selected">=</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
                              <option value="&lt;="><=</option>
				          </select>
	                      <input name="flowId" size="5" />
						<script>
						$(function() {
                            o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                            o("<%=fieldName%>").value = "<%=queryValueID%>";
                        });
						</script>
						<%
                        }
                        else if ("flow:begin_date".equals(fieldName) || "flow:end_date".equals(fieldName) || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        %>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
               			<%
                            String idPrefix = fieldName.replaceAll(":", "_");
                            if (condType.equals("0")) {
                                String fDate = ParamUtil.get(request, fieldName + "FromDate");
                                String tDate  = ParamUtil.get(request, fieldName + "ToDate");
                                list.add(idPrefix + "FromDate");
                                list.add(idPrefix + "ToDate");
                        %>
                              从
                              <input id="<%=idPrefix%>FromDate" name="<%=fieldName%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                              至
                              <input id="<%=idPrefix%>ToDate" name="<%=fieldName%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
	  					<%
                        }
                        else {
                            list.add(idPrefix);
                        %>
                              <input id="<%=idPrefix%>" name="<%=fieldName%>" size="15" value = "<%=queryValue%>" />
						<%
                            }
                 } else if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <%
                    if (condType.equals("0")) {
                        String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                        String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                        list.add(ff.getName() + "FromDate");
                        list.add(ff.getName() + "ToDate");
                %>
                大于
                <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" value="<%=fDate%>"/>
                    <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
                小于
                <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" value="<%=tDate%>"/>
                    <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>ToDate', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
                    <!-- <script>
							$(document).ready(function() {
							o("<%=ff.getName()%>FromDate").value = "<%=fDate%>";
							o("<%=ff.getName()%>ToDate").value = "<%=tDate%>";
							});
							</script> -->
                <%
                } else {
                    list.add(ff.getName());
                %>
                <input id="<%=ff.getName()%>" name="<%=ff.getName()%>" size="15" value="<%=queryValue%>"/>
                    <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
                    <!-- <script>
							  $(document).ready(function() {							  
                              o("<%=fieldName%>").value = "<%=queryValue%>";
							  });
                              </script> -->
                <%
                    }
                } else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
                        out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <script>
                    $(document).ready(function () {
                        o("<%=fieldName%>").value = "<%=queryValue%>";
                        try {
                            o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
                        } catch (e) {
                        }
                    });
                </script>
                <%
                    }
                } else if (ff.getFieldType() == FormField.FIELD_TYPE_INT || ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE || ff.getFieldType() == FormField.FIELD_TYPE_FLOAT || ff.getFieldType() == FormField.FIELD_TYPE_LONG || ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
                    String nameCond = ParamUtil.get(request, fieldName + "_cond");
                    if ("".equals(nameCond)) {
                        nameCond = condType;
                    }
                %>
                <select name="<%=fieldName%>_cond">
                    <option value="=" selected="selected">等于</option>
                    <option value=">">></option>
                    <option value="&lt;"><</option>
                    <option value=">=">>=</option>
                    </option>
                    <option value="&lt;="><=</option>
                </select>
                <input name="<%=fieldName%>" size="5"/>
                <script>
                    $(document).ready(function () {
                        o("<%=fieldName%>_cond").value = "<%=nameCond%>";
                        o("<%=fieldName%>").value = "<%=queryValue%>";
                    });
                </script>
                <%
                } else {
                    boolean isSpecial = false;
                    if (condType.equals(SQLBuilder.COND_TYPE_NORMAL)) {
                        if (ff.getType().equals(FormField.TYPE_SELECT)) {
                            isSpecial = true;
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <select id="<%=fieldName %>" name="<%=fieldName %>">
                    <%=FormParser.getOptionsOfSelect(fd, ff) %>
                </select>
                <script>
                    $(document).ready(function () {
                        o("<%=fieldName%>").value = "<%=queryValue%>";
                    });
                </script>
                <%
                } else if (ff.getType().equals(FormField.TYPE_RADIO)) {
                    isSpecial = true;
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <%
                    String[][] aryRadio = FormParser.getOptionsArrayOfRadio(fd, ff);
                    for (int k = 0; k < aryRadio.length; k++) {
                        String val = aryRadio[k][0];
                        String text = aryRadio[k][1];
                %>
                <input type="radio" id="<%=fieldName %>" name="<%=fieldName %>" value="<%=val %>"/><%=text %>
                <%
                    }
                } else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                    isSpecial = true;
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <%
                    String[][] aryChk = FormParser.getOptionsArrayOfCheckbox(fd, ff);
                    for (int k = 0; k < aryChk.length; k++) {
                        String val = aryChk[k][0];
                        String fName = aryChk[k][1];
                        String text = aryChk[k][2];
                        queryValue = ParamUtil.get(request, fName);
                %>
                <input type="checkbox" id="<%=fName %>" name="<%=fName %>" value="<%=val %>"
                       style="<%=aryChk.length>1?"width:20px":""%>"/>
                <script>
                    $(function () {
                        o('<%=fName%>').checked = <%=queryValue.equals(val)?"true":"false"%>;
                    })
                </script>
                <%if (aryChk.length > 1) { %>
                <%=text %>
                <%} %>
                <%
                            }
                        }
                    }
                    if (!isSpecial) {
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <input name="<%=fieldName%>" size="5"/>
                <script>
                    $(document).ready(function () {
                        o("<%=fieldName%>").value = "<%=queryValue%>";
                    });
                </script>
                <%
                                        }
                                    }
                %>
                </span>
                        <%
                                }
                            }
                        }
                    }
                    
                    if (isQuery) {
                %>
                <input type="hidden" name="op" value="search"/>
                <input type="hidden" name="moduleCode" value="<%=moduleCode%>"/>
                <input type="hidden" name="orderBy" value="<%=orderBy %>"/>
                <input type="hidden" name="sort" value="<%=sort %>"/>
                <span class="condSpan">
                <input class="tSearch" type="submit" value=" 搜索 "/>
                </span>
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
    ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
    int total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null)
        ir = v.iterator();
    paginator.init(total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
    String[] fields = msd.getColAry(false, "list_field");
    String[] fieldsWidth = msd.getColAry(false, "list_field_width");
%>
<table class="percent98 p9" width="98%" border="0" align="center" cellpadding="3" cellspacing="1">
    <tr>
        <td align="left" width="30%" height="23">
            <input type="button" class="btn" value="选择" onClick="selBatch()"/></td>
        <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %>
        </b> 条　每页显示 <b><%=paginator.getPageSize() %>
        </b> 条　页次 <b><%=curpage %>/<%=totalpages %>
        </b></td>
    </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
    <tr align="center">
        <td class="tabStyle_1_title" width="30px" style="cursor:hand">
            <input name="checkbox" type="checkbox"
                   onClick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"/>
        </td>
        <%
            // 取得当前用户的隐藏字段
            String fieldHide = mpd.getUserFieldsHasPriv(privilege.getUser(request), "hide");
            String[] fdsHide = StrUtil.split(fieldHide, ",");
            len = 0;
            if (fields != null)
                len = fields.length;
            for (int i = 0; i < len; i++) {
                String fieldName = fields[i];
                
                if (fdsHide != null) {
                    boolean isHide = false;
                    for (String hideField : fdsHide) {
                        if (hideField.equals(fieldName)) {
                            isHide = true;
                            break;
                        }
                    }
                    if (isHide) {
                        continue;
                    }
                }
                
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
                } else if (fieldName.equalsIgnoreCase("id")) {
                    title = "ID";
                } else if (fieldName.equals("cws_progress")) {
                    title = "进度";
                } else if (fieldName.equals("cws_flag")) {
                    title = "冲抵状态";
                } else {
                    title = fd.getFieldTitle(fieldName);
                }
        %>
        <td class="tabStyle_1_title" width="<%=fieldsWidth[i]%>" style="cursor:hand" onClick="doSort('<%=fieldName%>')">
            <%=title%>
            <%
                if (orderBy.equals(fieldName)) {
                    if (sort.equals("asc"))
                        out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                    else
                        out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                }%>
        </td>
        <%}%>
    </tr>
    <%
        int k = 0;
        UserMgr um = new UserMgr();
        while (ir != null && ir.hasNext()) {
            fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
            RequestUtil.setFormDAO(request, fdao);
            k++;
            long id = fdao.getId();
    %>
    <tr align="center" class="highlight">
        <td align="center"><input type="checkbox" id="ids" name="ids" value="<%=id%>"/></td>
        <%
            for (int i = 0; i < len; i++) {
                String fieldName = fields[i];
                if (fdsHide != null) {
                    boolean isHide = false;
                    for (String hideField : fdsHide) {
                        if (hideField.equals(fieldName)) {
                            isHide = true;
                            break;
                        }
                    }
                    if (isHide) {
                        continue;
                    }
                }
        %>
        <td align="left">
            <%if (i == 0) {%>
            <a href="moduleShowPage.do?parentId=<%=id%>&id=<%=id%>&code=<%=moduleCode%>&isShowNav=0" target="_blank">
                <%
                    }
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
                            if (user != null)
                                realName = user.getRealName();
                        }
                        out.print(realName);
                    } else if (fieldName.equals("cws_progress")) {
                        out.print(fdao.getCwsProgress());
                    } else if (fieldName.equals("cws_flag")) {
                        out.print(fdao.getCwsFlag());
                    } else if (fieldName.equalsIgnoreCase("id")) {
                        out.print(fdao.getId());
                    } else {
                        FormField ff = fdao.getFormField(fieldName);
                        if (ff != null) {
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null) {
                                    out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
                                }
                            } else {%>
                <%=FuncUtil.renderFieldValue(fdao, ff)%>
                <%
                    }
                } else {
                %>
                <%=fieldName%>不存在
                <%
                    }%>
                <%
                    }
                    if (i == 0) {
                %>
            </a>
            <%}%>
        </td>
        <%}%>
    </tr>
    <%
        }
    %>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
    <tr>
        <td width="50%" height="23" align="left">
            <input type="button" class="btn" value="选择" onClick="selBatch()"/>
        </td>
        <td width="50%" align="right"><%
            out.print(paginator.getCurPageBlock("module_list_for_sel.jsp?" + querystr + "&keywords=" + keywords));
        %></td>
    </tr>
</table>
</body>
<script>
    function initCalendar() {
        <%for (String ffname : list) {%>
        $('#<%=ffname%>').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        <%}%>
    }

    $(function () {
        initCalendar();
    });

    function selAllCheckBox(checkboxname) {
        var checkboxboxs = document.getElementsByName(checkboxname);
        if (checkboxboxs != null) {
            // 如果只有一个元素
            if (checkboxboxs.length == null) {
                checkboxboxs.checked = true;
            }
            for (i = 0; i < checkboxboxs.length; i++) {
                checkboxboxs[i].checked = true;
            }
        }
    }

    function deSelAllCheckBox(checkboxname) {
        var checkboxboxs = document.getElementsByName(checkboxname);
        if (checkboxboxs != null) {
            if (checkboxboxs.length == null) {
                checkboxboxs.checked = false;
            }
            for (i = 0; i < checkboxboxs.length; i++) {
                checkboxboxs[i].checked = false;
            }
        }
    }

    function selBatch() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert("请先选择记录！", "提示");
            return;
        }

        jConfirm("您确定要选择么？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                if (typeof (window.opener.onSelect) == "function") {
                    window.opener.onSelect(ids);
                } else {
                    jAlert("请在父窗口中加入方法：onSelect");
                }
                window.close();
            }
        });
    }
</script>
</html>
