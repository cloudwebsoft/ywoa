<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    PersonGroupTypeDb pgtd = new PersonGroupTypeDb();
    Vector typeV = pgtd.listOfUser(privilege.getUser(request));
    if (typeV.size() == 0) {
        response.sendRedirect("persongroup_type_list.jsp");
        return;
    }

    int groupId = ParamUtil.getInt(request, "groupId", -1);
    if (groupId == -1) {
        groupId = ((PersonGroupTypeDb) typeV.elementAt(0)).getId();
    }

%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>个人用户组管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function openWinUsers() {
            // showModalDialog('../user_multi_sel.jsp', window.self, 'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
            openWin('../user_multi_sel.jsp', window.self, 'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
        }

        function setUsers(users, userRealNames) {
            o("users").value = users;
            o("userRealNames").value = userRealNames;
            if (o("users").value != "") {
                o("form2").submit();
            }
        }

        function getSelUserNames() {
            return o("users").value;
        }

        function getSelUserRealNames() {
            return o("userRealNames").value;
        }
    </script>
</head>
<%
    String op = ParamUtil.get(request, "op");
    if (op.equals("sel")) {
        String userNames = ParamUtil.get(request, "users");
        String[] ary = StrUtil.split(userNames, ",");
        if (ary == null) {
            out.print(StrUtil.jAlert_Back("请选择人员！", "提示"));
            return;
        }
        boolean re = false;
        boolean isErr = false;
        for (int i = 0; i < ary.length; i++) {
            try {
                PersonGroupUserDb pgud = new PersonGroupUserDb();
                re = pgud.create(new JdbcTemplate(), new Object[]{new Integer(groupId), ary[i], new Integer(0)});
            } catch (ResKeyException e) {
                isErr = true;
            }
        }
        if (isErr) {
            out.print(StrUtil.jAlert_Redirect("请检查是否已存在相同记录！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
        } else if (re)
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
        return;
    } else if (op.equals("del")) {
        PersonGroupUserDb pgud = new PersonGroupUserDb();

        String[] ary = StrUtil.split(ParamUtil.get(request, "ids"), ","); // StrUtil.split(ids, ",");
        if (ary == null) {
            out.print("请选择记录！");
            return;
        }

        boolean re = false;
        for (int i = 0; i < ary.length; i++) {
            pgud = (PersonGroupUserDb) pgud.getQObjectDb(new Integer(StrUtil.toInt(ary[i])));
            if (pgud!=null) {
                re = pgud.del();
            }
            else {
                DebugUtil.e(getClass(), "del", ary[i] + " 不存在");
            }
        }
        if (re)
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
        return;
    }
%>
<body>
<%@ include file="persongroup_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
<table id="searchTable" width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td align="center">
            <form action="persongroup_user_list.jsp?op=sel" class="search-form" method="post" id="form2" name="form2">
                用户组&nbsp;<select id="groupId" name="groupId" onchange="window.location.href='persongroup_user_list.jsp?groupId=' + this.value">
                    <%
                        Iterator ir = typeV.iterator();
                        while (ir.hasNext()) {
                            pgtd = (PersonGroupTypeDb) ir.next();
                    %>
                    <option value="<%=pgtd.getId()%>"><%=pgtd.getName()%>
                    </option>
                    <%}%>
                </select>
                <script>
                    o("groupId").value = "<%=groupId%>";
                </script>
                <input type="hidden" id="users" name="users"/>
                <input type="hidden" id="userRealNames" name="userRealNames"/>
            </form>
        </td>
    </tr>
</table>
<form action="persongroup_user_list.jsp?op=del" method="post" id="form1" name="form1" onSubmit="return form_onsubmit()">
    <table id="grid" cellSpacing="0" cellPadding="0" width="1028">
        <thead>
        <tr>
            <th width="300" style="cursor:pointer">部门</th>
            <th width="300" style="cursor:pointer">用户</th>
            <th width="300" style="cursor:pointer">手机</th>
            <th width="300" style="cursor:pointer">操作</th>
        </tr>
        </thead>
        <tbody>
        <%
            UserMgr um = new UserMgr();
            DeptMgr dm = new DeptMgr();
            DeptUserDb du = new DeptUserDb();

            PersonGroupUserDb pgud = new PersonGroupUserDb();
            String sql = "select id from " + pgud.getTable().getName() + " where group_id=? order by orders";
            ir = pgud.list(sql, new Object[]{groupId}).iterator();
            while (ir.hasNext()) {
                pgud = (PersonGroupUserDb) ir.next();%>
        <tr id="tr_<%=pgud.getInt("id")%>">
            <td>
                <%
                    UserDb user = um.getUserDb(pgud.getString("user_name"));
                    Iterator ir2 = du.getDeptsOfUser(pgud.getString("user_name")).iterator();
                    int k = 0;
                    while (ir2.hasNext()) {
                        DeptDb dd = (DeptDb) ir2.next();
                        String deptName = "";
                        if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {
                            deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
                        } else
                            deptName = dd.getName();
                        if (k == 0) {
                            out.print(deptName);
                        } else {
                            out.print("，&nbsp;" + deptName);
                        }
                        k++;
                    }
                %>
            </td>
            <td align="center">
                <%if (user.isLoaded()) {%>
                <a href="javascript:;" onclick="addTab('<%=user.getRealName() %>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%>
                </a>
                <%} else {%>
                用户不存在
                <%}%>
            </td>
            <td align="center"><%=user.getMobile()%>
            </td>
            <td align="center"><a href="javascript:;" onclick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return;} else {location.href='?op=del&ids=<%=pgud.getInt("id")%>'}})">删除</a>
                &nbsp;&nbsp;
                <a href="javascript:;" onclick="addTab('短消息', '<%=request.getContextPath()%>/message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(pgud.getString("user_name"))%>')">短消息</a>
                <%
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms") && !StrUtil.getNullStr(user.getMobile()).equals("")) {
                %>
                &nbsp;&nbsp;
                <a href="javascript:;" onclick="addTab('短信', '<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=<%=StrUtil.UrlEncode(user.getName())%>')">短信</a>
                <%}%>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
</form>
</body>
<script>
    function sendMessage() {
        var ids = "";
        var checkboxboxs = document.getElementsByName("ids");
        if (checkboxboxs != null) {
            for (i = 0; i < checkboxboxs.length; i++) {
                if (checkboxboxs[i].checked) {
                    if (ids == "")
                        ids = checkboxboxs[i].getAttribute("userName");
                    else
                        ids += "," + checkboxboxs[i].getAttribute("userName");
                }
            }
            addTab("短消息", "<%=request.getContextPath()%>/message_oa/message_frame.jsp?op=send&receiver=" + ids);
        }
    }

    function sendSMS() {
        var ids = "";
        var checkboxboxs = document.getElementsByName("ids");
        if (checkboxboxs != null) {
            for (i = 0; i < checkboxboxs.length; i++) {
                if (checkboxboxs[i].checked) {
                    if (ids == "")
                        ids = checkboxboxs[i].getAttribute("userName");
                    else
                        ids += "," + checkboxboxs[i].getAttribute("userName");
                }
            }
            addTab("短消息", "<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=" + ids);
        }
    }

    $(document).ready(function () {
        flex = $("#grid").flexigrid
        (
            {
                buttons: [
                    {name: '添加', bclass: 'add', onpress: action},
                    {name: '删除', bclass: 'delete', onpress: action},
                    <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
                    {name: '短信', bclass: 'SMS', onpress: actions},
                    <%}%>
                    {separator: true},
                    {name: '', bclass: '', type: 'include', id: 'searchTable'}
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
                usepager: false,
                <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
                checkbox: true,
                <%}else{%>
                checkbox: true,
                <%}%>

                useRp: false,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: true,

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
    });

    function onReload() {
        window.location.reload();
    }

    // 用于工具条自定义按钮的调用
    function getIdsSelected(onlyOne) {
        var ids = "";
        $(".cth input[type='checkbox'][value!='on']", grid.bDiv).each(function(i) {
            if($(this).is(":checked")) {
                if (ids=="")
                    ids = $(this).val().substring(3);
                else
                    ids += "," + $(this).val().substring(3);
            }
        });

        var selectedCount = 0;
        var ary = ids.split(",");
        if (ids!="") {
            selectedCount = ary.length;
        }
        if (selectedCount == 0) {
            return "";
        }

        if (selectedCount > 1 && onlyOne) {
            return "";
        }
        return ids;
    }

    function del(ids) {
        window.location.href = "?op=del&ids=" + ids;
    }

    function action(com, grid) {
        if(com=='添加'){
            openWinUsers();
        }
        else if (com=='删除'){
            var ids = getIdsSelected();
            if (ids=='') {
                jAlert('请选择记录！','提示');
                return;
            }
            del(ids);
        }
        else if (com=='短信') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择记录!', '提示');
                return;
            }

            if (selectedCount > 0) {
                $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function (i) {
                    if (ids == "")
                        ids = $(this).val();
                    else
                        ids += "," + $(this).val();
                });
                addTab("短信", "<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=" + ids);
            }
        }
    }
</script>
</html>
