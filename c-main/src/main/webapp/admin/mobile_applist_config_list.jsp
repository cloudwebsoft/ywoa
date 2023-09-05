<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.android.system.*" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.db.ListResult" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>手机端应用</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <style>
        .search-form input, select {
            vertical-align: middle;
        }
        .search-form input:not([type="radio"]):not([type="button"]) {
            width: 80px;
            line-height: 20px;
        }
        .icon {
            width: 1em;
            height: 1em;
            vertical-align: -0.15em;
            fill: currentColor;
            overflow: hidden;
            font-size: 32px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <script src="../js/flexigrid.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../fonts/mobile/iconfont.js"></script>
</head>
<body>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int pagesize = ParamUtil.getInt(request, "pageSize", 25);
    String CPages = ParamUtil.get(request, "CPages");

    String op = ParamUtil.get(request, "op");
    String what = ParamUtil.get(request, "what");
    int typeInt = ParamUtil.getInt(request, "type", -1);
    String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&type=" + typeInt;

    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    MobileAppIconConfigDb mb = new MobileAppIconConfigDb();
    String sql = mb.getListSql(op, typeInt, what);
    ListResult lr = mb.listResult(sql, curpage, pagesize);
    long total = lr.getTotal();
    Iterator ir = lr.getResult().iterator();
    paginator.init(total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="left">
            <form class="search-form" action="mobile_applist_config_list.jsp" method="post">
                <input name="op" value="search" type="hidden"/>
                &nbsp;&nbsp;名称&nbsp;
                <input name="what" size="15" value="<%=what%>"/>
                &nbsp;&nbsp;类型&nbsp;
                <select id="type" name="type">
                    <option id="type_all" value=-1>全部</option>
                    <option id="type_menu" value=1>菜单项</option>
                    <option id="type_flow" value=2>流程项</option>
                    <option id="type_module" value=3>智能表单项</option>
                </select>
                <input name="submit" type="submit" class="tSearch" value="搜索"/>
            </form>
        </td>
    </tr>
</table>
<table border="0" id="grid">
    <thead>
    <tr>
        <th width="37"><input name="checkbox" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')"></th>
        <th width="46" abbr="money_type">编号</th>
        <th width="136">名称</th>
        <th width="91">类型</th>
        <th width="80">图片型图标</th>
	    <th width="80" style="display: none">矢量型图标</th>
        <th width="163">上传时间</th>
        <th width="69">增加</th>
        <th width="75">发起</th>
        <th width="75">顺序</th>
    </tr>
    </thead>
    <tbody>
    <%
        int n = 1;
        String name = "";
        String typeName = "";
        String imgUrl = "";
        String setTime = "";

        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (ir.hasNext()) {
            mb = (MobileAppIconConfigDb) ir.next();

            int id = mb.getInt("id");
            name = mb.getString("name");
            int type = mb.getInt("type");
            if (type == 1) {
                typeName = "菜单";
            } else if (type == 2) {
                typeName = "流程";
            } else if (type == 3) {
                typeName = "模块";
            } else {
                typeName = "链接";
            }

            imgUrl = mb.getString("imgUrl");
            setTime = f.format(mb.getDate("setTime"));
            int isAdd = mb.getInt("is_add");
            int isMobileStart = mb.getInt("isMobileStart");
            String icon = mb.getString("icon");
    %>
    <tr align="center" id="tr<%=id%>">
        <td><input type="checkbox" id="ids" name="ids" value="<%=id%>"/></td>
        <td><%=n%>
        </td>
        <td><%=name%>
        </td>
        <td><%=typeName%>
        </td>
        <td><img width="20" height="20" src='<%=request.getContextPath()%>/static/<%=imgUrl%>'></td>
        <td style="display: none">
            <svg class="icon svg-icon" aria-hidden="true">
                <use id="useFontIcon" xlink:href="<%=icon%>"></use>
            </svg>
        </td>
        <td><%=setTime%>
        </td>
        <td>
            <%
                if (type == 3) {
                    out.print(isAdd == 1 ? "是" : "否");
                }
            %>
        </td>
        <td><%=isMobileStart == 1 ? "是" : "否"%>
        </td>
        <td><%=mb.getInt("orders")%>
        </td>
    </tr>
    <%
            n++;
        }
    %>
    </tbody>
</table>
</body>
<script>
    function doOnToolbarInited() {
    }

    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "mobile_applist_config_list.jsp?pageSize=" + flex.getOptions().rp;
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "mobile_applist_config_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
    }

    function rpChange(pageSize) {
        window.location.href = "mobile_applist_config_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
    }

    function onReload() {
        window.location.reload();
    }

    flex = $("#grid").flexigrid
    (
        {
            buttons: [
                {name: '添加', bclass: 'add', onpress: action},
                {name: '修改', bclass: 'edit', onpress : action},
                {name: '删除', bclass: 'delete', onpress: action},
                {separator: true},
                {name: '条件', bclass: 'fbutton', type: 'include', id: 'searchTable'}
            ],
            /*
            searchitems : [
                {display: 'ISO', name : 'iso'},
                {display: 'Name', name : 'name', isdefault: true}
                ],
            */
            url: false,
            usepager: true,
            checkbox: false,
            page: <%=curpage%>,
            total: <%=total%>,
            useRp: true,
            rp: <%=pagesize%>,

            //title: "通知",
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
            onToolbarInited: doOnToolbarInited,
            autoHeight: true,
            width: document.documentElement.clientWidth,
            height: document.documentElement.clientHeight - 84
        }
    );

    function action(com, grid) {
        if (com == "删除") {
            doDel();
        } else if (com == "添加") {
            doAdd();
        } else if (com == "修改") {
            edit();
        }
    }

    function edit() {
        var id = getIds();
        if (id=='') {
            jAlert('请选择一条记录!', '提示');
            return;
        }
        if (id.indexOf(",")!=-1) {
            jAlert('只能选择一条记录!', '提示');
            return;
        }
        var tabIdOpener = getActiveTabId();
        addTab('修改应用', '<%=request.getContextPath()%>/admin/mobile_applist_config_edit.jsp?id=' + id + "&tabIdOpener=" + tabIdOpener);
    }

    function doDel() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert("请选择记录！", "提示");
            return;
        }
        jConfirm('您确定要删除么？', '提示', function(r) {
            if (!r) {
                return;
            }

            $.ajax({
                type: "post",
                url: "delMobileAppIcon.do",
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
                    jAlert(data.msg, "提示");
                    if (data.ret == 1) {
                        var ary = ids.split(",");
                        for (i in ary) {
                            $('#tr' + ary[i]).remove();
                        }
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
        });
    }

    function doAdd() {
        window.location.href = "mobile_applist_config_add.jsp";
    }

    function del(id) {
        var pageSize = <%=pagesize%>;
        var CPages = <%=curpage%>;

        jConfirm('您确定要删除么？', '提示', function(r) {
            if (!r) {
                return;
            }
            window.location.href = "mobile_applist_config_list.jsp?op=del&id=" + id + "&pageSize=" + pageSize + "&CPages=" + CPages;
        });
    }

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

    function getIds() {
        var checkedboxs = 0;
        var checkboxboxs = document.getElementsByName("ids");
        var id = "";
        if (checkboxboxs != null) {
            // 如果只有一个元素
            if (checkboxboxs.length == null) {
                if (checkboxboxs.checked) {
                    checkedboxs = 1;
                    id = checkboxboxs.value;
                }
            }
            for (i = 0; i < checkboxboxs.length; i++) {
                if (checkboxboxs[i].checked) {
                    checkedboxs = 1;
                    if (id == "")
                        id = checkboxboxs[i].value;
                    else
                        id += "," + checkboxboxs[i].value;
                }
            }
        }
        return id;
    }
</script>
</html>