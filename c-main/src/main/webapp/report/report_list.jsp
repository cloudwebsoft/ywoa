<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="org.json.*" %>
<%@page import="com.redmoon.oa.report.ReportManageDb" %>
<%@page import="java.io.File" %>
<%@page import="cn.js.fan.db.ListResult" %>
<%@page import="cn.js.fan.db.Paginator" %>

<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "report.admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String userName = privilege.getUser(request);       //得到登陆用户名
    ReportManageDb rmb = new ReportManageDb();
    String op = ParamUtil.get(request, "op");
    String what = ParamUtil.get(request, "what");
    boolean isSearch = op.equals("search") && !"".equals(what);
    String cond = ParamUtil.get(request, "condition");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>报表管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/ajaxfileupload.js" type="text/javascript"></script>
    <script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/BootstrapMenu.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
    <style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
    <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
    <style>
        .search-form input,select {
            vertical-align:middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }

        #sortable {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }
        
        #sortable li {
            margin: 3px 3px 3px 0;
            padding: 1px;
            float: left;
            width: 100px;
            height: 50px;
            font-size: 10pt;
            text-align: center;
        }
        
        #sortable .ui-selecting {
            background: #FECA40;
        }
        
        #sortable .ui-selected {
            background: #F39814;
            color: white;
        }
    
    </style>


</head>
<body>

<%
    String sql = "";
    if (isSearch) {
        if ("name".equals(cond)) {
            sql = "select id from report_manage where name like '%" + what + "%' order by report_date";
        }
        if ("description".equals(cond)) {
            sql = "select id from report_manage where description like '%" + what + "%' order by report_date";
        }
    } else {
        sql = "select id from report_manage order by report_date";
    }
    
    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
    int curpage = ParamUtil.getInt(request, "CPages", 1);
    ListResult lr = rmb.listResult(sql, curpage, pagesize);
    Iterator iterator = lr.getResult().iterator();
    long total = lr.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td width="48%" height="30" align="left">
            <form class="search-form" action="report_list.jsp" method="post">
                <input id="op" name="op" value="search" type="hidden"/>
                <select id="condition" name="condition">
                    <option value="name">名称</option>
                    <option value="description">描述</option>
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
        <th width="48" name="title">ID</th>
        <th width="234" name="title">名称</th>
        <th width="143" name="type">描述</th>
        <th width="108" name="create_date" style="display:none">发布日期</th>
        <th width="108" name="alter_date">修改日期</th>
        <th width="108" name="create_user">上传用户</th>
        <th width="111" name="show">操作</th>
    </tr>
    </thead>
    <tbody>
    <%
        while (iterator != null && iterator.hasNext()) {
            rmb = (ReportManageDb) iterator.next();
            int id = rmb.getInt("id");
            String name = rmb.getString("name");
            String upload_path = rmb.getString("upload_path");
            String description = rmb.getString("description");
            String username = rmb.getString("username");
            String priv_code = rmb.getString("priv_code");
            String priv_desc = rmb.getString("priv_desc");
            Date date = rmb.getDate("report_date");
            String report_date = DateUtil.format(date, "yyyy-MM-dd");
            date = rmb.getDate("alter_date");
            String alter_date = DateUtil.format(date, "yyyy-MM-dd");
    %>
    <tr id="<%=id %>">
        <td align="center"><%=id%>
        </td>
        <td><%=name %>
        </td>
        <td><%=description %>
        </td>
        <td style="display:none"><%=report_date%>
        </td>
        <td><%=alter_date%>
        </td>
        <td><%=new UserDb(username).getRealName() %>
        </td>
        <td>
            <a href="javascript:;" onclick="showView('<%=id%>')">查看</a>
            <%
                if (privilege.isUserPrivValid(request, "admin")) {
            %>
            &nbsp;&nbsp;
            <a href="javascript:;"
               onclick="addTab('<%=name%> 日志', '<%=request.getContextPath()%>/visual/module_list.jsp?op=search&code=module_log_read&read_type=1&module_id=<%=id%>')">日志</a>
            <%
                }
            %>
            <input name="privCode<%=id%>" type="hidden" value="<%=priv_code%>"/>
            <input name="privDesc<%=id%>" type="hidden" value="<%=priv_desc%>"/>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<div id="dlg" style="display:none">
    <div>上传文件<input id="upload" name="upload" type="file"/></div>
    <input id="priv_code" name="priv_code" type="hidden"/>
    使用权限&nbsp;<input id="priv_desc" name="priv_desc" type="text" style="margin-top:0.2cm" readonly="true"/>
    <input type="button" value="选择权限" onclick="openPrivPage()"/><br/>
    功能描述&nbsp;<textarea id="description" name="description" style="margin-top:0.2cm" rows="5" cols="30"></textarea><br/><br/>
    <table id="attach" style="display:none" border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td>
                &nbsp;&nbsp;&nbsp;&nbsp;<img src="../images/attach.gif"/>
                &nbsp;&nbsp;&nbsp; <a id="upload_file" href="javascript:void(0);" onclick="getFile(this)"></a>
            </td>
        </tr>
    </table>
</div>
</body>
<script>
    var flex;
    function changePage(newp) {
        if (newp)
            window.location.href = "report_list.jsp?CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "report_list.jsp?CPages=<%=curpage%>&pageSize=" + pageSize;
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
                    {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
                ],
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

                // onChangeSort: changeSort,

                onChangePage: changePage,
                onRpChange: rpChange,
                onReload: onReload,
                /*
                onRowDblclick: rowDbClick,
                onColSwitch: colSwitch,
                onColResize: colResize,
                onToggleCol: toggleCol,
                autoHeight: true,*/
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight - 84
            }
        );
    });

    function action(com, grid) {
        if (com == '添加') {
            $("#attach").css("display", "none");
            $("#dlg").dialog({
                title: "添加",
                modal: true,
                width: 500,
                height: 500,
                // bgiframe:true,
                buttons: {
                    "取消": function () {
                        $(this).dialog("close");
                    },
                    "确定": function () {
                        $.ajaxFileUpload({
                            url: '<%=request.getContextPath()%>/report/create',  //用于文件上传的服务器端请求地址
                            secureuri: false,//一般设置为false
                            fileElementId: 'upload',//文件上传空间的id属性  <input type="file" id="file" name="file" />
                            dataType: 'json',//返回值类型 一般设置为json
                            data: {
                                description: $("#description").val(),
                                privCode: $("#priv_code").val(),
                                privDesc: $("#priv_desc").val()
                            },
                            beforeSend: function (XMLHttpRequest) {
                                $('#dlg').showLoading();
                            },
                            success: function (data, status) {
                                $('#dlg').hideLoading();
                                jAlert(data.msg, '提示', function() {
                                    window.location.href = "report_list.jsp?toa=ok&msg=" + data.message;
                                });
                            },
                            complete: function (XMLHttpRequest, status) {

                            },
                            error: function (XMLHttpRequest, textStatus) {
                                // 请求出错处理
                                jAlert(XMLHttpRequest.responseText, '提示');
                            }
                        })
                        $(this).dialog("close");
                    }
                },
                closeOnEscape: true,
                draggable: true,
                resizable: true
            });
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
            $("#" + id).children().each(function (i) {
                if (2 == i) {
                    $("#upload_file").text($(this).text());
                }
                if (3 == i) {
                    $("#description").val($(this).text());
                }
                $("#priv_code").val($('#privCode' + id).val());
                $("#priv_desc").val($('#privDesc' + id).val());
                $("#attach").css("display", "inline");
                $("#dlg").dialog({
                    title: "修改",
                    modal: true,
                    width: 520,
                    height: 500,
                    // bgiframe:true,
                    buttons: {
                        "取消": function () {
                            $(this).dialog("close");
                        },
                        "确定": function () {
                            $.ajaxFileUpload({
                                url: '<%=request.getContextPath()%>/report/update',  //用于文件上传的服务器端请求地址
                                secureuri: false,//一般设置为false
                                fileElementId: 'upload',//文件上传空间的id属性  <input type="file" id="file" name="file" />
                                dataType: 'json',//返回值类型 一般设置为json
                                data: {
                                    description: $("#description").val(),
                                    privCode: $("#priv_code").val(),
                                    privDesc: $("#priv_desc").val(),
                                    id: id
                                },
                                beforeSend: function (XMLHttpRequest) {
                                    $('#dlg').showLoading();
                                },
                                success: function (data, status) {
                                    $('#dlg').hideLoading();
                                    jAlert(data.msg, '提示', function() {
                                        window.location.href = "report_list.jsp?toa=ok&msg=" + encodeURI(data.msg);
                                    });
                                },
                                complete: function (XMLHttpRequest, status) {
                                },
                                error: function (XMLHttpRequest, textStatus) {
                                    // 请求出错处理
                                    alert(XMLHttpRequest.responseText);
                                }
                            })
                            $(this).dialog("close");
                        }
                    },
                    closeOnEscape: true,
                    draggable: true,
                    resizable: true
                });
            })
        } else if (com == '删除') {
            selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
            if (selectedCount == 0) {
                jAlert('请选择至少一条记录!');
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
                    $.ajax({
                        type: "post",
                        url: "del",
                        data: {
                            ids: ids
                        },
                        dataType: "json",
                        beforeSend: function (XMLHttpRequest) {
                        },
                        success: function (data, status) {
                            var ary = ids.split(",");
                            for (i in ary) {
                                $('#' + ary[i]).remove();
                            }
                            $.toaster({priority: 'info', message: data.msg});
                        },
                        complete: function (XMLHttpRequest, status) {
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText, '提示');
                        }
                    });
                }
            })
        }
    }

    function showView(id) {
        addTab("报表展示", "<%=request.getContextPath()%>/reportJsp/showReport.jsp?id=" + id);
    }

    function openPrivPage() {
        var privilege = $("#priv_code").val();
        openWin("<%=request.getContextPath()%>/report/priv_list_sel.jsp?priv=" + privilege, 800, 600);
    }

    function setIntpuObjValue(code, desc) {
        $("#priv_code").val(code);
        $("#priv_desc").val(desc);
    }

    function getFile(obj) {
        var fileName = $(obj).text();
        window.location.href = "<%=request.getContextPath()%>/report/download.do?fileName=" + encodeURI(fileName);
    }
</script>
</html>