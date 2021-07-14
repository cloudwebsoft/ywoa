<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if (strcurpage.equals("")) {
        strcurpage = "1";
    }
    if (!StrUtil.isNumeric(strcurpage)) {
        out.print(StrUtil.makeErrMsg("标识非法！"));
        return;
    }
    int pagesize = ParamUtil.getInt(request, "pageSize", 10);
    int curpage = Integer.parseInt(strcurpage);

    String op = ParamUtil.get(request, "op");
    String by = ParamUtil.get(request, "by");
    String what = ParamUtil.get(request, "what");

    String action = ParamUtil.get(request, "action");

    WorkflowSequenceDb wf = new WorkflowSequenceDb();

    String sql = "select id from flow_sequence order by name asc";
    if (op.equals("search")) {
        if (by.equals("name")) {
            sql = "select id from flow_sequence where name like " + StrUtil.sqlstr("%" + what + "%") + " order by name asc";
        }
    }

    ListResult lr = wf.listResult(sql, curpage, pagesize);
    long total = lr.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null) {
        ir = v.iterator();
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>工作流序列号管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <style>
        #searchTable {
            margin-top: 5px;
            margin-left: 5px;
        }
        .search-form input, select {
            vertical-align: middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
    </style>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%
    if (action.equals("del")) {
        try {
            String ids = ParamUtil.get(request, "id");
            String[] ary = StrUtil.split(ids, ",");
            for (int i = 0; i < ary.length; i++) {
                int id = StrUtil.toInt(ary[i]);
                WorkflowSequenceDb wsd = new WorkflowSequenceDb();
                wsd = wsd.getWorkflowSequenceDb(id);
                wsd.del();
            }
            out.print(StrUtil.jAlert_Redirect("删除完毕！", "提示", "?op=" + op + "&by=" + by + "&what=" + StrUtil.UrlEncode(what)));
        } catch (Exception e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
    } else if (action.equals("add")) {
        String name = ParamUtil.get(request, "name").trim();
        long beginIndex = ParamUtil.getLong(request, "beginIndex");
        String curIndex = ParamUtil.get(request, "curIndex").trim();
        String curValue = ParamUtil.get(request, "curValue").trim();
        String len = ParamUtil.get(request, "length").trim();
        String template = ParamUtil.get(request, "template").trim();
        if (template.length() > 45) {
            out.print(StrUtil.jAlert_Back("规则过长！", "提示"));
        }
        //判断序列名称是否为空
        if (name.equals("")) {
            out.print(StrUtil.jAlert_Back("请填写序列名称！", "提示"));
            return;
        }
        //判断“当前值”是否与“规则”匹配
        if (template.equals("{num}")) {//数值型
            //判断“当前值”是否为数字
            if (!curIndex.matches("[0-9]+")) {
                out.print(StrUtil.jAlert_Back("“当前值”必须是数字", "提示"));
                return;
            }
        } else {//组合型
            curIndex = "0";
        }
        //判断“补齐位数”是否为数字
        if (!len.matches("[0-9]+")) {
            out.print(StrUtil.jAlert_Back("“补齐位数”必须是数字", "提示"));
            return;
        }

        String itemSeparator = ParamUtil.get(request, "itemSeparator");
        int yearDigit = ParamUtil.getInt(request, "yearDigit", 4);

        WorkflowSequenceDb wsd = new WorkflowSequenceDb();
        wsd.setBeginIndex(beginIndex);
        wsd.setCurIndex(Long.parseLong(curIndex));
        wsd.setCurValue(curValue);
        wsd.setLength(Integer.parseInt(len));
        wsd.setName(name);
        wsd.setTemplate(template);

        wsd.setItemSeparator(itemSeparator);
        wsd.setYearDigit(yearDigit);

        if (template.trim().equals("{num}")) {
            wsd.setType(WorkflowSequenceDb.TYPE_NUMBER);
        } else {
            wsd.setType(WorkflowSequenceDb.TYPE_COMPOUND);
        }
        if (wsd.create()) {
            out.print(StrUtil.jAlert_Redirect("添加成功！", "提示", "flow_sequence_list.jsp"));
        } else {
            out.print(StrUtil.jAlert_Back("添加失败！", "提示"));
        }
        return;
    } else if (action.equals("modify")) {
        String name = ParamUtil.get(request, "name").trim();
        long beginIndex = ParamUtil.getLong(request, "beginIndex");
        String curIndex = ParamUtil.get(request, "curIndex").trim();
        String len = ParamUtil.get(request, "length").trim();
        int id = ParamUtil.getInt(request, "id");
        String template = ParamUtil.get(request, "template");
        String curValue = ParamUtil.get(request, "curValue");
        if (template.length() > 45) {
            out.print(StrUtil.jAlert_Back("规则过长！", "提示"));
        }
        if (name.equals("")) {
            out.print(StrUtil.jAlert_Back("请填写序列名称！", "提示"));
        }
        //判断“当前值”是否与“规则”匹配
        if (template.equals("{num}")) {//数值型
            //判断“当前值”是否为数字
            if (!curIndex.matches("[0-9]+")) {
                out.print(StrUtil.jAlert_Back("“当前值”必须是数字", "提示"));
                return;
            }
        } else {//组合型
            curIndex = "0";
        }
        //判断“补齐位数”是否为数字
        if (!len.matches("[0-9]+")) {
            out.print(StrUtil.jAlert_Back("“补齐位数”必须是数字", "提示"));
            return;
        }

        String itemSeparator = ParamUtil.get(request, "itemSeparator");
        int yearDigit = ParamUtil.getInt(request, "yearDigit", 4);

        WorkflowSequenceDb wsd = new WorkflowSequenceDb();
        wsd = wsd.getWorkflowSequenceDb(id);
        wsd.setBeginIndex(beginIndex);
        wsd.setCurIndex(Long.parseLong(curIndex));
        wsd.setLength(Integer.parseInt(len));
        wsd.setName(name);
        wsd.setTemplate(template);
        wsd.setCurValue(curValue);
        wsd.setItemSeparator(itemSeparator);
        wsd.setYearDigit(yearDigit);
        if (wsd.save()) {
            out.print(StrUtil.jAlert_Redirect("修改成功！", "提示", "flow_sequence_list.jsp"));
        } else {
            out.print(StrUtil.jAlert_Back("修改失败！", "提示"));
        }
    }
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td width="48%" height="30" align="left">
            <form class="search-form" id="form1" name="form1" action="?op=search" method="post">
                <select id="by" name="by">
                    <option value="name">名称</option>
                </select>
                <input id="what" name="what" size="15" value="<%=what%>"/>
                <input class="tSearch" value="搜索" type="submit"/>
            </form>
        </td>
    </tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
    <thead>
    <tr>
        <th width="80" name="title">ID</th>
        <th width="122" name="creator">名称</th>
        <th width="80" name="type">类型</th>
        <th width="300" name="validate_date">规则</th>
        <th width="450" name="create_date">当前值</th>
        <th width="80" name="create_date" title="数字长度位数，不足将在左侧补0，置为0表示不需要补位">补齐位数</th>
    </tr>
    </thead>
    <tbody>
    <%
        Leaf ft = new Leaf();
        UserMgr um = new UserMgr();
        int i = 0;
        while (ir.hasNext()) {
            i++;
            WorkflowSequenceDb wsd = (WorkflowSequenceDb) ir.next();
    %>
    <tr id="<%=wsd.getId()%>">
        <td><%=wsd.getId()%>
        </td>
        <td><%=wsd.getName()%>
        </td>
        <td>
            <%if (wsd.getType() == WorkflowSequenceDb.TYPE_NUMBER) {%>
            数值型
            <%} else {%>
            组合型
            <%} %>
        </td>
        <td><%=StrUtil.HtmlEncode(wsd.getTemplate())%>
        </td>
        <td>
            <%if (wsd.getType() == WorkflowSequenceDb.TYPE_COMPOUND) {%>
            <%=StrUtil.HtmlEncode(wsd.getCurValue())%>
            <%} else {%>
            <%=wsd.getCurIndex()%>
            <%}%>
        </td>
        <td><%=wsd.getLength()%>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>
<%
    String querystr = "op=" + op + "&by=" + by + "&what=" + StrUtil.UrlEncode(what);
%>
</body>
<script language="javascript">
    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "flow_sequence_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
    }

    function changePage(newp) {
        if (newp) {
            window.location.href = "flow_sequence_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
        }
    }

    function rpChange(pageSize) {
        //alert(pageSize);
        window.location.href = "flow_sequence_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '修改', bclass: 'edit', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    {separator: true},
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
                /*
                searchitems : [
                    {display: 'ISO', name : 'iso'},
                    {display: 'Name', name : 'name', isdefault: true}
                    ],
                sortname: "iso",
                sortorder: "asc",
                */
                url: false,
                usepager: true,
                checkbox: true,
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pagesize%>,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

                onChangeSort: changeSort,

                onChangePage: changePage,
                onRpChange: rpChange,
                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                */
                autoHeight: true,
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );

        <%if (op.equals("search")) {%>
        o("by").value = "<%=by%>";
        <%}%>

    });

    function action(com, grid) {
        if (com == '添加') {
            window.location.href = "flow_sequence_add.jsp?op=add";
        } else if (com == '修改') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择一条记录!', '提示');
                return;
            }
            if (selectedCount > 1) {
                jAlert('只能选择一条记录!', '提示');
                return;
            }

            var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
            window.location.href = "flow_sequence_add.jsp?op=modify&id=" + id;
        } else if (com == '删除') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择一条记录!', '提示');
                return;
            }
            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    var ids = "";
                    $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                        if (ids == "")
                            ids = $(this).val();
                        else
                            ids += "," + $(this).val();
                    });
                    window.location.href = 'flow_sequence_list.jsp?action=del&CPages=<%=curpage%>&id=' + ids;
                }
            })
        }
    }
</script>
</html>