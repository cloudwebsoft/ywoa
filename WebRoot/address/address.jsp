<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudweb.oa.bean.Address" %>
<%@ page import="com.cloudweb.oa.service.AddressService" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>通讯录</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
    <%@ include file="../inc/nocache.jsp" %>
    <style>
        .search-form input, select {
            vertical-align: middle;
        }

        .menutitle {
            cursor: pointer;
            margin-bottom: 5px;
            background-color: #ECECFF;
            color: #000000;
            width: 140px;
            padding: 2px;
            text-align: center;
            font-weight: bold;
            border: 1px solid #000000;
        }

        .submenu {
            margin-bottom: 0.1em;
        }

        .loading {
            display: none;
            position: fixed;
            z-index: 1801;
            top: 45%;
            left: 45%;
            width: 100%;
            margin: auto;
            height: 100%;
        }

        .SD_overlayBG2 {
            background: #FFFFFF;
            filter: alpha(opacity=20);
            -moz-opacity: 0.20;
            opacity: 0.20;
            z-index: 1500;
        }

        .treeBackground {
            display: none;
            position: absolute;
            top: -2%;
            left: 0%;
            width: 100%;
            margin: auto;
            height: 200%;
            background-color: #EEEEEE;
            z-index: 1800;
            -moz-opacity: 0.8;
            opacity: .80;
            filter: alpha(opacity=80);
        }
    </style>
</head>
<body>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    String mode = ParamUtil.get(request, "mode");
    int type = ParamUtil.getInt(request, "type", AddressService.TYPE_USER);
    if (type == AddressService.TYPE_PUBLIC) {
        if (!"show".equals(mode)) {
            if (!privilege.isUserPrivValid(request, "admin.address.public")) {
                out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
                return;
            }
        }
    }
    
    String who = privilege.getUser(request);
    String op = ParamUtil.get(request, "op");
    String typeId = ParamUtil.get(request, "dir_code");
    String person = ParamUtil.get(request, "person");
    String company = ParamUtil.get(request, "company");
    String mobile = ParamUtil.get(request, "mobile");
%>
<table id="searchTable" width="98%" align="center">
    <tr>
        <td colspan="2" align="center" nowrap="nowrap">
            <form action="address.jsp" class="search-form" method="get" name="form1" id="form1">
                分组
                <input name="op" value="search" type="hidden"/>
                <%
                    if (type == AddressService.TYPE_PUBLIC)
                        who = Leaf.USER_NAME_PUBLIC;
                %>
                <select name="dir_code" id="dir_code">
                    <%
                        Leaf lf = new Leaf();
                        lf = lf.getLeaf(who);
                        DirectoryView dv = new DirectoryView(lf);
                        int rootlayer = 1;
                        dv.ShowDirectoryAsOptionsWithCode(out, lf, rootlayer);
                    %>
                </select>
                <script>
                    $(function () {
                        o("dir_code").value = "<%=typeId%>";
                    })
                </script>
                <input name="type" value="<%=type%>" type="hidden"/>
                姓名&nbsp;
                <input type="text" name="person" size="10" value="<%=person%>"/>
                手机&nbsp;
                <input type="text" name="mobile" size="10" value="<%=mobile%>"/>
                单位&nbsp;
                <input type="text" name="company" size="10" value="<%=company%>"/>
                <input type="hidden" name="mode" value="<%=mode%>"/>
                &nbsp;<input class="tSearch" type="submit" value="搜索"/>
            </form>
        </td>
    </tr>
</table>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();

    String searchStr = "";

    searchStr += "&person=" + StrUtil.UrlEncode(person);
    searchStr += "&company=" + StrUtil.UrlEncode(company);
    searchStr += "&typeId=" + typeId;
    searchStr += "&mobile=" + mobile;

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "id";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";

    String userName = privilege.getUser(request);
    int pageSize = ParamUtil.getInt(request, "pageSize", 20);
    Address addr = new Address();
    AddressService addressService = new AddressService();
    String sql = addressService.getSql(op, userName, type, typeId, person, company, mobile, orderBy, sort, privilege.getUserUnitCode(request));
    ListResult lr = addressService.listResult(sql, curpage, pageSize);
    int total = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir = null;
    if (v != null)
        ir = v.iterator();
    paginator.init(total, pageSize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<form name="form1" action="../message_oa/sms_send.jsp" method="post">
    <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
        <thead>
        <tr>
            <th width="72" style="cursor:pointer">姓名</th>
            <th width="72" style="cursor:pointer">职务</th>
            <th width="72" style="cursor:pointer">单位</th>
            <th width="100" style="cursor:pointer">手机</th>
            <th width="100" style="cursor:pointer">电话</th>
            <th width="72" style="cursor:pointer">短号</th>
            <th width="150" style="cursor:pointer">邮箱</th>
            <th width="125" style="cursor:pointer">操作</th>
        </tr>
        </thead>
        <tbody>
        <%
            while (ir != null && ir.hasNext()) {
                addr = (Address) ir.next();
                int id = addr.getId();
        %>
        <tr id="row<%=addr.getId()%>">
            <td align="left"><a href="javascript:;"
                                onclick="addTab('<%=addr.getPerson()%>', '<%=request.getContextPath()%>/address/address_show.jsp?id=<%=id%>&mode=show')"><%=addr.getPerson()%>
            </a></td>
            <td><%=addr.getJob()%>
            </td>
            <td><%=addr.getCompany()%>
            </td>
            <td><%=addr.getMobile()%>
            </td>
            <td><%=addr.getTel()%>
            </td>
            <td><%=addr.getMSN()%>
            </td>
            <td><%=addr.getEmail()%>
            </td>
            <td>
                <a href="javascript:;"
                   onclick="addTab('<%=addr.getPerson()%>', '<%=request.getContextPath()%>/address/address_show.jsp?id=<%=id%>&mode=show')">查看</a>
                <%
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                %>
                &nbsp;&nbsp;<a href="../message_oa/sms_send.jsp?mobile=<%=addr.getMobile()%>">短信</a>
                <%}%>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
</form>
</body>
<script>
    function openExcel() {
        <%
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String sql3des = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg.getKey(), sql);
        %>
        var sql = "<%=sql3des%>";
        window.open("address_excel.jsp?sql=" + sql);
    }

    function openWin(url, width, height) {
        var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
    }

    function importExcel() {
        var url = "import_excel.jsp?type=" + "<%=type%>" + "&group=" + "<%=typeId%>";
        openWin(url, 360, 50);
    }

    function add() {
        window.location.href = "address_add.jsp?type=<%=type%>&typeId=<%=typeId%>";
    }

    function sendSms() {
        var ids = "";
        $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
            var id = $(this).val();
            if (ids=="")
                ids = id;
            else
                ids += "," + id;
        });
        if (ids == "") {
            jAlert("请选择人员！", "提示");
            return;
        }
        var mobiles = "";
        var ary = ids.split(",");
        for (i in ary) {
            var id = ary[i];
            if ($('#' + id).attr('mobile')!='') {
                if (mobiles=="") {
                    mobiles = $('#' + id).attr('mobile');
                }
                else {
                    mobiles += "," + $('#' + id).attr('mobile');
                }
            }
        }
        if (mobiles=="") {
            jAlert("请选择有手机号的人员！", "提示");
            return;
        }
        window.location.href = "../message_oa/sms_send.jsp?mobile=" + mobiles;
    }

    $(function () {
        flex = $("#grid").flexigrid({
                buttons: [
                    <%if (type==AddressService.TYPE_USER || (type==AddressService.TYPE_PUBLIC && privilege.isUserPrivValid(request, "admin.address.public"))) {%>
                    {name: '添加', bclass: 'add', onpress: actions},
                    <%}%>
                    <%if (type==AddressService.TYPE_USER || (type==AddressService.TYPE_PUBLIC && privilege.isUserPrivValid(request, "admin.address.public"))) {%>
                    {name: '修改', bclass: 'edit', onpress: actions},
                    <%}%>
                    <%if (type==AddressService.TYPE_USER || (type==AddressService.TYPE_PUBLIC && privilege.isUserPrivValid(request, "admin.address.public"))) {%>
                    {name: '删除', bclass: 'delete', onpress: actions},
                    <%}%>
                    <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
                    {name: '短信', bclass: 'sms', onpress: actions},
                    <%}%>
                    <%if (type==AddressService.TYPE_USER || (type==AddressService.TYPE_PUBLIC && privilege.isUserPrivValid(request, "admin.address.public"))) {%>
                    {name: '导入', bclass: 'import1', onpress: actions},
                    <%}%>
                    {name: '导出', bclass: 'export', onpress: actions},
                    {separator: true},
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
                /*
                 searchitems : [
                 {display: 'ISO', name : 'iso'},
                 {display: 'Name', name : 'name', isdefault: true}
                 ],
                 */
                width: 'auto',
                sortname: "<%=orderBy%>",
                sortorder: "<%=sort%>",
                url: false,
                usepager: true,
                checkbox: true,
                page: <%=curpage%>,
                total: <%=total%>,
                useRp: true,
                rp: <%=pageSize%>,

                // title: "通知",
                singleSelect: true,
                resizable: false,
                showTableToggleBtn: true,
                showToggleBtn: false,
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
                autoHeight: true
                // width: document.documentElement.clientWidth,
                // height: document.documentElement.clientHeight - 84
            }
        );
    });

    function changeSort(sortname, sortorder) {
        window.location.href = "address.jsp?pageSize=" + flex.getOptions().rp + "&dir_code=<%=typeId%>&mode=<%=mode%>&orderBy=" + sortname + "&sort=" + sortorder + "<%=searchStr%>";
    }

    function changePage(newp) {
        if (newp) {
            window.location.href = "address.jsp?CPages=" + newp + "&type=<%=type%>&dir_code=<%=typeId%>&mode=<%=mode%>&pageSize=" + flex.getOptions().rp + "<%=searchStr%>";
        }
    }

    function rpChange(pageSize) {
        window.location.href = "address.jsp?CPages=<%=curpage%>&type=<%=type%>&dir_code=<%=typeId%>&mode=<%=mode%>&pageSize=" + pageSize + "<%=searchStr%>";
    }

    function onReload() {
        window.location.reload();
    }

    function actions(com, grid) {
        if (com == '短信') {
            sendSms();
        } else if (com == '修改') {
            selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择记录!', '提示');
                return;
            } else if (selectedCount > 1) {
                jAlert('请选择一条记录!', '提示');
                return;
            }

            var tabId = getActiveTabId();

            var id = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function (i) {
                id = $(this).val().substring(3);
            });
            addTab('修改通讯录', '<%=request.getContextPath()%>/address/address_modify.jsp?type=<%=addr.getType()%>&id=' + id + '&tabIdOpener=' + tabId);
        } else if (com == '导入') {
            importExcel();
        } else if (com == '导出') {
            openExcel();
        } else if (com == '删除') {
            var ids = "";
            // value!='on' 过滤掉复选框按钮
            $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function (i) {
                if (ids == "")
                    ids = $(this).val().substring(3);
                else
                    ids += "," + $(this).val().substring(3);
            });
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }
            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    $.ajax({
                        type: "post",
                        url: "delBatch.do",
                        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                        data: {
                            ids: ids
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                $.toaster({priority: 'info', message: data.msg});
                                var ary = ids.split(",");
                                for (var i = 0; i < ary.length; i++) {
                                    $('#row' + ary[i]).remove();
                                }
                            } else {
                                $.toaster({priority: 'info', message: data.msg});
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('body').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        } else if (com == '添加') {
            add();
        }
    }

    $(function () {
        $(".bDiv").css({"height": "448px"});
    })
</script>
</html>