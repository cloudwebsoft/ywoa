<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    // 原getInt的默认值为DISPLAY_MODE_SEARCH，360浏览器，可能会传过来有问题的值，导致进入了搜索，会致看到全部用户的待办
    int displayMode = ParamUtil.getInt(request, "displayMode", WorkflowMgr.DISPLAY_MODE_DOING); // 显示模式，0表示流程查询、1表示待办、2表示我参与的流程、3表示我发起的流程
    String op = StrUtil.getNullString(request.getParameter("op"));
    String action = ParamUtil.get(request, "action"); // sel 选择我的流程
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
    <style>
        #searchTable {
            margin-top: 5px;
            margin-left: 5px;
        }
        .condSpan {
            display: inline-block;
            float: left;
            width: 250px;
            min-height: 32px;
        }
        .condBtnSearch {
            display: inline-block;
            float: left;
            width: 50px;
        }
        .search-form input, select {
            vertical-align: middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
        .unreaded {
            font-weight:bold;
        }
    </style>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.toaster.flow.js"></script>
</head>
<body>
<%
    String toa = ParamUtil.get(request, "toa");
    String msg = ParamUtil.get(request, "msg");
    if (toa.equals("ok") && !msg.equals("")) {
%>
<script>
    $.toaster({priority: 'info', message: '<%=msg%>'});
</script>
<%
        msg = "";
    }
    if (!action.equals("sel")) {
        if (displayMode==WorkflowMgr.DISPLAY_MODE_ATTEND || displayMode==WorkflowMgr.DISPLAY_MODE_MINE) {
%>
<div class="tabs1Box">
    <%@ include file="../flow_inc_menu_top.jsp"%>
    <script>
        <%
        if (displayMode==WorkflowMgr.DISPLAY_MODE_ATTEND) {
        %>
        o("menu1").className="current";
        <%
        } else {
        %>
        o("menu2").className="current";
        <%
        }
        %>
    </script>
</div>
<%
        }
    }

    String typeCode = ParamUtil.get(request, "typeCode");
    String title = ParamUtil.get(request, "title");
    String userName = ParamUtil.get(request, "userName");

    String myname = ParamUtil.get(request, "userName");
    if (myname.equals("")) {
        myname = privilege.getUser(request);
    }

    JSONArray colProps = null;
    Leaf colLeaf = new Leaf();
    FormDb fd = new FormDb();
    if (!"".equals(typeCode)) {
        colLeaf = colLeaf.getLeaf(typeCode);
        fd = fd.getFormDb(colLeaf.getFormCode());
    }

    String handle = ParamUtil.get(request, "handle");
    if (handle.equals("resetColProps")) {
        if ("".equals(typeCode)) {
            colLeaf = colLeaf.getLeaf(Leaf.CODE_ROOT);
            colLeaf.setColProps("");
        }
        else {
            colLeaf.setColProps(com.redmoon.oa.flow.Leaf.getDefaultColProps(request, typeCode, displayMode).toString());
        }
        colLeaf.update();
        response.sendRedirect("flow_list.jsp?op=search&displayMode=" + displayMode + "&myname=" + StrUtil.UrlEncode(myname) + "&typeCode=" + StrUtil.UrlEncode(typeCode));
        return;
    }

    if (colLeaf.isLoaded() && !"".equals(colLeaf.getColProps())) {
        try {
            colProps = new JSONArray(colLeaf.getColProps());
        } catch (org.json.JSONException e) {
            DebugUtil.i(getClass(), "colLeaf", "colLeaf.getColProps()=" + colLeaf.getColProps());
            e.printStackTrace();
        }
    }
    if (colProps == null) {
        colProps = com.redmoon.oa.flow.Leaf.getDefaultColProps(request, typeCode, displayMode);
    }

    Leaf leaf = new Leaf();
    if (!"".equals(typeCode)) {
        leaf = leaf.getLeaf(typeCode);
        if (!leaf.isLoaded()) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
            out.println(cn.js.fan.web.SkinUtil.makeInfo(request, str));
            return;
        }
    }

    if (displayMode==WorkflowMgr.DISPLAY_MODE_SEARCH && "".equals(typeCode)) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
        out.println(cn.js.fan.web.SkinUtil.makeInfo(request, str));
        return;
    }

    if (displayMode == WorkflowMgr.DISPLAY_MODE_SEARCH) {
        LeafPriv leafPriv;

        // 如果是分类节点，且用户不是管理员权限
        if (leaf.getType() == Leaf.TYPE_NONE && !privilege.isUserPrivValid(myname, "admin")) {
            Vector v = new Vector();
            v = leaf.getAllChild(v, leaf);
            Iterator it = v.iterator();
            while (it.hasNext()) {
                Leaf sl = (Leaf) it.next();
                leafPriv = new LeafPriv(sl.getCode());
                // 如果分类节点的某个子节点无查询权限则退出
                if (!leafPriv.canUserQuery(myname)) {
                    String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
                    out.println(cn.js.fan.web.SkinUtil.makeInfo(request, str));
                    return;
                }
            }
        }

        leafPriv = new LeafPriv(typeCode);

        if (!leafPriv.canUserQuery(myname)) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
            out.println(cn.js.fan.web.SkinUtil.makeInfo(request, str));
            return;
        }

        if (!myname.equals(privilege.getUser(request)) || !myname.equals(userName)) {
            if (!leafPriv.canUserQuery(privilege.getUser(request))) {
                out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
                return;
            }
        }
    }
%>
<table id="searchTable" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            <form name="formSearch" class="search-form" action="flow_list.jsp" method="get" onsubmit="return false">
                <span class="span-cond">
                    <%
                        if (displayMode!=WorkflowMgr.DISPLAY_MODE_SEARCH) {
                    %>
                    <span class="condSpan">
                        类型
                <select id="typeCode" name="f.typeCode" onchange="onTypeCodeChange(this)" style="width:170px;">
                    <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
                </select>
                        </span>
                    <%
                        }
                        else {
                    %>
                    <input id="typeCode" name="f.typeCode" value="<%=typeCode%>" type="hidden"/>
                    <%
                        }
                    %>
                    <span class="condSpan">
                    
                <select id="by" name="f.by">
                    <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
                    <option value="flowId"><lt:Label res="res.flow.Flow" key="number"/></option>
                </select>
                <input id="title" name="f.title" value="<%=title%>"/>
                    </span>
                    <span class="condSpan">
                <lt:Label res="res.flow.Flow" key="organ"/>
                <input id="starter" name="f.starter" value="<%=userName%>"/>
                    </span>
                    <span class="condSpan">
                    待办状态
                    <select id="actionStatus" name="actionStatus">
                        <option value="-1">不限</option>
                        <option value="<%=WorkflowActionDb.STATE_RETURN%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_RETURN)%></option>
                        <option value="<%=WorkflowActionDb.STATE_DOING%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_DOING)%></option>
                    </select>
                    </span>
                <span class="condSpan">
                <lt:Label res="res.flow.Flow" key="state"/>
                <select id="status" name="f.status">
                    <option value="1000" selected><lt:Label res="res.flow.Flow" key="limited"/></option>
                    <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%>
                    </option>
                    <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%>
                    </option>
                    <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%>
                    </option>
                    <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%>
                    </option>
                    <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%>
                    </option>
                </select>
                </span>
                <span class="condSpan">
                从
                <input id="fromDate" name="f.fromDate"/>
                至
                <input id="toDate" name="f.toDate"/>
                </span>
                <%
                    if (leaf.isLoaded() && !"".equals(leaf.getCondProps())) {
                        JSONObject json = new JSONObject(leaf.getCondProps());
                        ArrayList<String> list = new ArrayList<String>();
                        MacroCtlMgr mm = new MacroCtlMgr();
                        Map<String, String> checkboxGroupMap = new HashMap<String, String>();

                        String condFields = (String) json.get("fields");
                        String[] fieldAry = StrUtil.split(condFields, ",");
                        for (int j = 0; j < fieldAry.length; j++) {
                            boolean isSub = false;
                            FormDb subFormDb = null;
                            String fieldName = fieldAry[j];
                            FormField ff = null;
                            String fieldTitle;
                            String condType = (String) json.get(fieldName);
                            String queryValue = ParamUtil.get(request, fieldName);

                            ff = fd.getFormField(fieldName);
                            if (ff == null) {
                                out.print(fieldName + "不存在");
                                continue;
                            }
                            if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                                String desc = StrUtil.getNullStr(ff.getDescription());
                                if (!"".equals(desc)) {
                                    fieldTitle = desc;
                                } else {
                                    fieldTitle = ff.getTitle();
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
                                fieldTitle = ff.getTitle();
                            }

                            // 用于给convertToHTMLCtlForQuery辅助传值
                            ff.setCondType(condType);
                %>
                    <span class="condSpan">
                <%=fieldTitle%>
                <%
                    if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <%
                    if (condType.equals("0")) {
                        String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
                        String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
                        list.add(ff.getName() + "FromDate");
                        list.add(ff.getName() + "ToDate");
                %>
                从
                <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" value="<%=fDate%>"/>
                至
                <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" value="<%=tDate%>"/>
                <%
                } else {
                    list.add(ff.getName());
                %>
                <input id="<%=ff.getName()%>" name="<%=ff.getName()%>" value="<%=queryValue%>"/>
                <%
                    }
                } else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
                        // 用main及other映射字段的描述替换其name，以使得生成的查询控件的id及name中带有main及other
                        FormField ffQuery = (FormField) ff.clone();
                        ffQuery.setName(fieldName);
                        IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                        out.print(ifmc.convertToHTMLCtlForQuery(request, ffQuery));
                %>
                <input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden"/>
                <a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>
                <script>
                    $(document).ready(function () {
                        o("<%=fieldName%>").value = "<%=queryValue%>";
                        try {
                            o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
                        } catch (e) {
                        }

                        <%
                        if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
                        %>
                        // 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
                        $("input[name='<%=fieldName%>']").focus(function () {
                            if ($(this).val() == '<%=SQLBuilder.IS_EMPTY%>' || $(this).val() == '<%=SQLBuilder.IS_NOT_EMPTY%>') {
                                this.select();
                            }
                        });

                        var menu = new BootstrapMenu('#arrow<%=j%>', {
                            menuEvent: 'click',
                            actions: [{
                                name: '等于空',
                                onClick: function () {
                                    o('<%=fieldName%>').value = '<%=SQLBuilder.IS_EMPTY%>';
                                }
                            }, {
                                name: '不等于空',
                                onClick: function () {
                                    o('<%=fieldName%>').value = '<%=SQLBuilder.IS_NOT_EMPTY%>';
                                }
                            }]
                        });
                        <%}%>
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
                    <option value="=" selected="selected">=</option>
                    <option value=">">></option>
                    <option value="&lt;"><</option>
                    <option value=">=">>=</option>
                    </option>
                    <option value="&lt;="><=</option>
                </select>
                <input name="<%=fieldName%>" style="width: 60px"/>
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
                    String[][] aryChk = null;
                    if (isSub) {
                        aryChk = FormParser.getOptionsArrayOfCheckbox(subFormDb, ff);
                    } else {
                        aryChk = FormParser.getOptionsArrayOfCheckbox(fd, ff);
                    }
                    for (int k = 0; k < aryChk.length; k++) {
                        String val = aryChk[k][0];
                        String fName = aryChk[k][1];
                        if (isSub) {
                            fName = "sub:" + subFormDb.getCode() + ":" + fName;
                        }
                        String text = aryChk[k][2];
                        queryValue = ParamUtil.get(request, fName);
                %>
                <input name="<%=fName%>_cond" value="<%=condType%>" type="hidden"/>
                <input type="checkbox" id="<%=fName %>" name="<%=fName %>" value="<%=val %>" style="<%=aryChk.length>1?"width:20px":""%>"/>
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
                <input id="field<%=j%>" name="<%=fieldName%>" style="width: 60px"/>
                <a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>
                <script>
                    $(function () {
                        o("<%=fieldName%>").value = "<%=queryValue%>";

                        // 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
                        $("#field<%=j%>").focus(function () {
                            if ($(this).val() == '<%=SQLBuilder.IS_EMPTY%>' || $(this).val() == '<%=SQLBuilder.IS_NOT_EMPTY%>') {
                                this.select();
                            }
                        });

                        var menu = new BootstrapMenu('#arrow<%=j%>', {
                            menuEvent: 'click',
                            actions: [{
                                name: '等于空',
                                onClick: function () {
                                    $('#field<%=j%>').val('<%=SQLBuilder.IS_EMPTY%>');
                                }
                            }, {
                                name: '不等于空',
                                onClick: function () {
                                    $('#field<%=j%>').val('<%=SQLBuilder.IS_NOT_EMPTY%>');
                                }
                            }]
                        });
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
                %>
                <input name="op" value="search" type="hidden"/>
                <input name="action" value="<%=action%>" type="hidden"/>
                <input name="myname" value="<%=myname %>" type="hidden"/>
                </span>
                <input type="submit" value='<lt:Label res="res.flow.Flow" key="search"/>' class="tSearch" onclick="doQuery()" />
            </form>
        </td>
    </tr>
</table>
<table id="grid" style="display:none"></table>
</body>
<script>
    var flex;

    function changeSort(sortname, sortorder) {
        if (!sortorder)
            sortorder = "desc";

        var params = $("form").serialize();
        var urlStr = "<%=request.getContextPath()%>/public/flow/list.do?" + params;
        urlStr += "&pageSize=" + $("#grid").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
        $("#grid").flexOptions({url: urlStr});
        $("#grid").flexReload();
    }

    function onReload() {
        doQuery();
    }

    var requestParams = [];
    requestParams.push({name: 'displayMode', value: '<%=displayMode%>'});

    var colModel = <%=colProps%>;

    $(document).ready(function () {
        // 将tabIdOpener传至flow_dispose.jsp，以便于在流程处理后刷新待办列表
        var tabIdOpener = getActiveTabId();
        requestParams.push({name: 'tabIdOpener', value: tabIdOpener});
        
        flex = $("#grid").flexigrid
        (
            {
                url: '../public/flow/list.do?typeCode=<%=typeCode%>&action=<%=action%>',
                params: requestParams,
                dataType: 'json',
                colModel: colModel,
                buttons: [
                    <%
                    boolean canDisposeBatch = false;
                    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                    if (displayMode==WorkflowMgr.DISPLAY_MODE_DOING && cfg.getBooleanProperty("canFlowDisposeBatch")) {
                        canDisposeBatch = true;
                    %>
                    {name: '<lt:Label res="res.flow.Flow" key="agree"/>', bclass: 'pass', onpress: action},
                    <%}%>
                    <%
                    if (displayMode==WorkflowMgr.DISPLAY_MODE_SEARCH) {
                        if (leaf != null && leaf.isLoaded() && leaf.getType() != Leaf.TYPE_NONE) {
                    %>
                    {name: '<lt:Label res="res.flow.Flow" key="export"/>', bclass: 'export', onpress: action},
                    <%
                        }
                    }
                        if (privilege.isUserPrivValid(request, "admin")) {
                    %>
                    {name: '<lt:Label res="res.flow.Flow" key="resetColProps"/>', bclass: 'resetCol', onpress: action},
                    <%
                            if (!"".equals(typeCode)) {
                    %>
                    {name: '<lt:Label res="res.flow.Flow" key="conds"/>', bclass: 'query', onpress: action},
                    <%
                            }
                        }
                    %>
                    {separator: true}
                    // {name: '<lt:Label res="res.flow.Flow" key="search"/>', bclass: '', type: 'include', id: 'searchTable'}
                ],
                usepager: true,
                checkbox: <%=canDisposeBatch%>,
                useRp: true,
                rp: 20,

                //title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

                onChangeSort: changeSort,

                // onChangePage: changePage,
                // onRpChange: rpChange,
                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                */
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,

                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );

        $('#fromDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $('#toDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $("[name$='FromDate']").datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
        $("[name$='ToDate']").datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d'
        });
    });

    function getIdsSelected(onlyOne) {
        var selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
        if (selectedCount == 0) {
            return "";
        }

        if (selectedCount > 1 && onlyOne) {
            return "";
        }

        var ids = "";
        // value!='on' 过滤掉复选框按钮
        $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
            var id = $(this).val().substring(3); // 去掉前面的row
            if (ids=="")
                ids = id;
            else
                ids += "," + id;
        });
        return ids;
    }
    
    function action(com, grid) {
        if (com == '<lt:Label res="res.flow.Flow" key="agree"/>') {
            var selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('<lt:Label res="res.flow.Flow" key="selectRecord"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
                return;
            }
            //if (!confirm('<lt:Label res="res.flow.Flow" key="isArgee"/>'))
            //return;
            jConfirm('<lt:Label res="res.flow.Flow" key="isArgee"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
                if (!r) {
                    return;
                } else {
                    var ids = getIdsSelected();
                    $.ajax({
                        type: "POST",
                        url: "../public/flow/finishBatch.do",
                        data: {
                            action: "finishBatch",
                            ids: ids
                        },
                        beforeSend: function(XMLHttpRequest){
                            $('body').showLoading();
                        },
                        success: function (html) {
                            var json = jQuery.parseJSON(html);
                            json.msg = json.msg.replace(/\\r/ig, "<BR/>");
                            if (json.ret == "1") {
                                jAlert(json.msg, "提示", function() {
                                    doQuery();
                                })
                            } else {
                                jAlert(json.msg, "提示");
                            }
                        },
                        complete: function(XMLHttpRequest, status){
                            $('body').hideLoading();
                        },
                        error: function(XMLHttpRequest, textStatus){
                            // 请求出错处理
                            alert("error:" + XMLHttpRequest.responseText);
                        }
                    });
                }
            });

        }
        else if (com == '<lt:Label res="res.flow.Flow" key="export"/>') {
            window.location.href = "flow_query_result_export.jsp?" + $('form').serialize() + "&typeCode=<%=typeCode%>";
        } else if (com == '<lt:Label res="res.flow.Flow" key="resetColProps"/>') {
            jConfirm('<lt:Label res="res.flow.Flow" key="isResetColProps"/>', "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    window.location.href = "flow_list.jsp?op=search&displayMode=<%=displayMode%>&myname=<%=myname%>&typeCode=<%=typeCode%>&handle=resetColProps";
                }
            })
        } else if (com == '<lt:Label res="res.flow.Flow" key="conds"/>') {
            addTab('条件', 'flow/flow_field_conds.jsp?typeCode=<%=typeCode%>');
        }
    }

    function getNameOfCol(display) {
        for (var i = 0; i < colM.length; i++) {
            if (colM[i].display == display)
                return colM[i].name;
        }
        return "";
    }

    var colM = <%=colProps.toString()%>;

    // 保存显示列配置
    function saveColProps() {
        var str = '';
        $('th', $(".hDiv")).each(function (i) {
            if ($(this).attr("name") == "op") {
                return;
            }
            var hide = $(this).css("display") == "none" ? true : false;
            var sortable = true;
            var align = $(this).attr("align");
            if (!align) {
                align = "";
            }
            var title = $.trim($(this).text()); // 去掉可能的回车符
            var name = getNameOfCol(title); // 标题不能重复，否则会致使name重复
            if (name=="") {
                // 如果name为空，可能是有些浏览器获取了多余的列
                return;
            }

            // 最后处理、操作、当前处理、剩余时间，这些列不能排序
            if (name=="f.finallyApply" || name=="operate" || name=="f.currentHandle" || name=="f.remainTime") {
                sortable = false;
            }

            if (str == '') {
                str = "{display: '" + title + "', name : '" + name + "', width : " + $(this).width() + ", sortable : " + sortable + ", align: '" + align + "', hide: " + hide + "}";
            } else {
                str += ",{display: '" + title + "', name : '" + name + "', width : " + $(this).width() + ", sortable : " + sortable + ", align: '" + align + "', hide: " + hide + "}";
            }
        });
        str = "[" + str + "]";

        $.ajax({
            type: "POST",
            url: "../public/flow/saveSearchColProps.do",
            data: "colProps=" + str + "&typeCode=<%=typeCode%>",
            beforeSend: function(XMLHttpRequest){
                $('body').showLoading();
            },
            success: function (html) {
                var json = jQuery.parseJSON(html);
                if (json.ret == "1") {
                    // alert("保存成功！");
                } else {
                    jAlert("保存列调整失败！", "提示");
                }
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert("error:" + XMLHttpRequest.responseText);
            }
        });
    }

    var colSwitch = function (i, j) {
        saveColProps();
    }

    var colResize = function () {
        saveColProps();
    }

    var toggleCol = function (cid, visible) {
        if (visible)
            $("#" + cid).width(100);
        saveColProps();
    }

    function onLoad() {
        try {
            onFlexiGridLoaded();
        } catch (e) {
        }
    }

    // 刷新
    function doQuery() {
        var params = $("form").serialize();
        var urlStr = "<%=request.getContextPath()%>/public/flow/list.do?" + params;
        $("#grid").flexOptions({url: urlStr});
        $("#grid").flexReload();
    }

    <%
    if (displayMode!=WorkflowMgr.DISPLAY_MODE_SEARCH) {
    %>
    function onTypeCodeChange(obj) {
        if(obj.options[obj.selectedIndex].value=='not'){
            jAlert(obj.options[obj.selectedIndex].text+' <lt:Label res="res.flow.Flow" key="notBeSelect"/>','<lt:Label res="res.flow.Flow" key="prompt"/>');
            return false;
        }
        window.location.href = "flow_list.jsp?op=search&action=<%=action%>&displayMode=<%=displayMode%>&typeCode=" + obj.options[obj.selectedIndex].value;
    }

    // ajax取得流程目录树，在流程类型较多的时候可以提升加载体验
    $(function() {
        $.ajax({
            type: "post",
            url: "flow_do.jsp",
            data: {
                op: "getTree",
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest) {
            },
            success: function(data, status) {
                $("#typeCode").empty();
                data = "<option value=''><lt:Label res="res.flow.Flow" key="limited"/></option>" + data;
                $("#typeCode").append(data);
                o("f.typeCode").value = "<%=typeCode%>";
            },
            complete: function(XMLHttpRequest, status) {
            },
            error: function(XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    });
    <%
    }

    if (displayMode==WorkflowMgr.DISPLAY_MODE_DOING) {
    %>
    $(function () {
        <%if (myname.equals(privilege.getUser(request))) {%>
        setActiveTabTitle("待办流程");
        <%} else {%>
        setActiveTabTitle('<%=new UserDb(myname).getRealName()%>的待办流程');
        <%}%>
    });
    <%
    }

    if (displayMode==WorkflowMgr.DISPLAY_MODE_DOING || displayMode==WorkflowMgr.DISPLAY_MODE_ATTEND || displayMode==WorkflowMgr.DISPLAY_MODE_MINE) {
    %>
    // 关注流程
    function favorite(id) {
        $.ajax({
            type: "post",
            url: "flow_do.jsp",
            data: {
                op: "favorite",
                flowId: id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('#grid').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                } else {
                    jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('#grid').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                jAlert(XMLHttpRequest.responseText, '<lt:Label res="res.flow.Flow" key="prompt"/>');
            }
        });
    }
    <%
    }
    else if (displayMode==WorkflowMgr.DISPLAY_MODE_FAVORIATE) {
    %>
    // 取消关注
    function unfavorite(id) {
        jConfirm('<lt:Label res="res.flow.Flow" key="toCancelAttention"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function(r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "flow_do.jsp",
                    data: {
                        op: "unfavorite",
                        flowId: id
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                        $('#grid').showLoading();
                    },
                    success: function(data, status){
                        data = $.parseJSON(data);
                        if (data.ret=="0") {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }
                        else {
                            jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
                            doQuery();
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('#grid').hideLoading();
                    },
                    error: function(XMLHttpRequest, textStatus){
                        jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
                    }
                });
            }
        });
    }
    <%
    }

    if ("sel".equals(action)) {
    %>
    function selFlow(id, title) {
        var dlg = window.opener ? window.opener : dialogArguments;
        dlg.setIntpuObjValue(id, "<a href='<%=request.getContextPath()%>/flow_modify.jsp?flowId=" + id + "' target='_blank'>" + title + "</a>");
        window.close();
    }
    <%
    }
    %>
</script>
</html>