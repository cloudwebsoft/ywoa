<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.cloudweb.oa.utils.ResponseUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "admin.unit")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    DocTemplateMgr lm = new DocTemplateMgr();
    DocTemplateDb ld = new DocTemplateDb();
    String op = ParamUtil.get(request, "op");
    if ("del".equals(op)) {
        ResponseUtil responseUtil = SpringUtil.getBean(ResponseUtil.class);
        out.print(responseUtil.getResJson(lm.del(application, request)).toString());
        return;
    }
%>
<!DOCTYPE html>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>公文模板管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
<style>
    .unit {
        background-color: #CCC;
    }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
<script src="../js/layui/layui.js" charset="utf-8"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%
    if ("add".equals(op)) {
        try {
            if (lm.add(application, request)) {
                out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flow_doc_template_list.jsp"));
                return;
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        return;
    } else if ("edit".equals(op)) {
        try {
            if (lm.modify(application, request)) {
                out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flow_doc_template_list.jsp"));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        return;
    }
%>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">公文模板管理</td>
    </tr>
    </tbody>
</table>
<%
    String myUnitCode = privilege.getUserUnitCode(request);

    String action = ParamUtil.get(request, "action");
    String title = ParamUtil.get(request, "title");

    String sql;

    if ("search".equals(action) && !"".equals(title)) {
        sql = "select id from " + ld.getTableName() + " where title like " + StrUtil.sqlstr("%" + title + "%") + " and unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by sort asc";
        if (myUnitCode.equals(DeptDb.ROOTCODE)) {
            sql = "select id from " + ld.getTableName() + " where title like " + StrUtil.sqlstr("%" + title + "%") + " order by unit_code asc, sort asc";
        }
    } else {
        sql = "select id from " + ld.getTableName() + " where unit_code=" + StrUtil.sqlstr(myUnitCode) + " order by sort asc";
        if (myUnitCode.equals(DeptDb.ROOTCODE)) {
            sql = "select id from " + ld.getTableName() + " order by unit_code asc, sort asc";
        }
    }
    // System.out.println(getClass() + " sql=" +sql);

    int pagesize = 20;
    Paginator paginator = new Paginator(request);
    int curpage = paginator.getCurPage();
    ListResult lr = ld.listResult(sql, curpage, pagesize);
    long total1 = lr.getTotal();
    Vector v = lr.getResult();
    Iterator ir1 = null;
    if (v != null) {
        ir1 = v.iterator();
    }
    paginator.init(total1, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    String querystr = "action=" + action + "&title=" + StrUtil.UrlEncode(title);
%>
<br>
<table width="80%" border="0" align="center">
    <tr>
        <td width="100%" align="center">
            <form action="flow_doc_template_list.jsp" method="get">
                <label>名称：</label>
                <input id="title" name="title" value="<%=title%>" size="20"/>
                &nbsp;
                <input name="action" value="search" type="hidden"/>
                <input name="submit" type="submit" class="btn btn-default" value="搜索"/>
            </form>
        </td>
    </tr>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
    <tr>
        <td align="right">
            &nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %>
        </b> 条　每页显示 <b><%=paginator.getPageSize() %>
        </b> 条　页次 <b><%=curpage %>/<%=totalpages %>
        </b>
        </td>
    </tr>
</table>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1">
    <thead>
    <tr>
        <td width="3%" class="tabStyle_1_title">ID</td>
        <td width="3%" class="tabStyle_1_title">编号</td>
        <td width="10%" class="tabStyle_1_title">名称</td>
        <td width="14%" class="tabStyle_1_title">文件头</td>
        <td width="14%" class="tabStyle_1_title">文件尾</td>
        <td class="tabStyle_1_title">部门</td>
        <td width="10%" class="tabStyle_1_title">单位</td>
        <td class="tabStyle_1_title" width="30%">操作</td>
    </tr>
    </thead>
    <tbody>
    <%
        Iterator ir = lr.getResult().iterator();
        UserMgr um = new UserMgr();
        int i = 100;
        DeptDb dd = new DeptDb();
        while (ir.hasNext()) {
            ld = (DocTemplateDb) ir.next();
            i++;

            String depts = ld.getDepts();
            String deptNames = "";
            String[] ary = StrUtil.split(depts, ",");
            if (ary != null) {
                for (int k = 0; k < ary.length; k++) {
                    if (deptNames.equals("")) {
                        deptNames = dd.getDeptDb(ary[k]).getName();
                    } else {
                        deptNames += "," + dd.getDeptDb(ary[k]).getName();
                    }
                }
            }
    %>
    <form name="form<%=ld.getId()%>" action="?op=edit" method="post" enctype="MULTIPART/FORM-DATA">
        <tr id="tr<%=ld.getId()%>">
            <td align="center">
                <%=ld.getId()%>
            </td>
            <td align="center">
                <input name="sort" value="<%=ld.getSort()%>" size="3"/>
            </td>
            <td><input name=title value="<%=ld.getTitle()%>"></td>
            <td class="highlight">
                <input name="filename" type="file" style="width:150px" size="10" accept=".docx"></td>
            <td class="highlight">
                <input name="filenameTail" type="file" style="width:150px" size="10" accept=".docx"></td>
            <td class="highlight">
                <input type="hidden" name="depts" value="<%=depts%>">
                <textarea name="deptNames" style="width:150px; height:40px" readOnly wrap="yes" id="deptNames<%=i%>"><%=deptNames%></textarea>
                <input class="btn btn-default" style="vertical-align: bottom" title="选择部门" onClick="openWinDepts(<%=ld.getId()%>)" type="button" value="选择">
            </td>
            <td class="highlight">
                <%if (ld.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {%>
                公共模板
                <%
                    } else {
                        dd = dd.getDeptDb(ld.getUnitCode());
                        out.print(dd.getName());
                    }
                %>
            </td>
            <td>
                <button class="btn btn-default" onclick="form<%=ld.getId()%>.submit()">修改</button>
                &nbsp;&nbsp;
                <button class="btn btn-default btn-del" docId="<%=ld.getId()%>">删除</button>
                &nbsp;&nbsp;
                <%--<button class="btn btn-default btn-edit-templ" docId="<%=ld.getId()%>">编辑头</button>
                &nbsp;&nbsp;--%>
                <button class="btn btn-default btn-download" docId="<%=ld.getId()%>">下载头</button>
                <%
                    if (!StrUtil.isEmpty(ld.getFileNameTail())) {
                %>
                <button class="btn btn-default btn-download-tail" docId="<%=ld.getId()%>">下载尾</button>
                <%
                    }
                %>
                <input name="id" value="<%=ld.getId()%>" type="hidden"></td>
        </tr>
    </form>
    <%}%>
    <form id="form0" name="form0" action="flow_doc_template_list.jsp?op=add" method="post" enctype="multipart/form-data">
        <tr>
            <td align="center">
                &nbsp;
            </td>
            <td align="center">
                <input name="sort" value="0" size="3"/>
            </td>
            <td><input name="title" value=""></td>
            <td>
                <input type="file" name="filename" style="width:150px" size="10" accept=".docx">
            </td>
            <td>
                <input type="file" name="filenameTail" style="width:150px" size="10" accept=".docx">
            </td>
            <td>
                <input type="hidden" name="depts"/>
                <textarea name="deptNames" style="width:150px; height:40px" readonly wrap="yes" id="deptNamesAdd"></textarea>
                <input class="btn btn-default" style="vertical-align: bottom" title="选择部门" onclick="openWinDepts(0)" type="button" value="选择"/>
            </td>
            <td>
                <%
                    if (myUnitCode.equals(DeptDb.ROOTCODE)) {
                %>
                <select id="unitCode" name="unitCode" onchange="if (this.value=='') alert('请选择单位！');">
                    <option value="<%=Leaf.UNIT_CODE_PUBLIC%>">-公共模板-</option>
                    <%
                        DeptDb rootDept = new DeptDb();
                        rootDept = rootDept.getDeptDb(DeptDb.ROOTCODE);
                    %>
                    <option value="<%=DeptDb.ROOTCODE%>"><%=rootDept.getName()%>
                    </option>
                    <%
                        ir = rootDept.getChildren().iterator();
                        while (ir.hasNext()) {
                            dd = (DeptDb) ir.next();
                            String cls = "", val = "";
                            if (dd.getType() == DeptDb.TYPE_UNIT) {
                                cls = " class='unit' ";
                                val = dd.getCode();
                            }
                    %>
                    <option <%=cls%> value="<%=val%>">&nbsp;&nbsp;&nbsp;&nbsp;<%=dd.getName()%>
                    </option>
                    <%
                        Iterator ir2 = dd.getChildren().iterator();
                        while (ir2.hasNext()) {
                            DeptDb dd2 = (DeptDb) ir2.next();
                            String cls2 = "", val2 = "";
                            if (dd2.getType() == DeptDb.TYPE_UNIT) {
                                cls2 = " class='unit' ";
                                val2 = dd2.getCode();
                            }
                    %>
                    <option <%=cls2%> value="<%=val2%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd2.getName()%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>
                <%
                } else {
                %>
                <input name="unitCode" value="<%=myUnitCode%>" type="hidden"/>
                <%
                        dd = dd.getDeptDb(myUnitCode);
                        out.print(dd.getName());
                    }
                %>
            </td>
            <td>
                <input class="btn btn-default" type="submit" value="添加"/>
            </td>
        </tr>
    </form>
    </tbody>
</table>
<table width="98%" class="percent98">
    <tr>
        <td align="right">
            <%
                out.print(paginator.getCurPageBlock("flow_doc_template_list.jsp?" + querystr));
            %>
        </td>
    </tr>
</table>
<div style="margin-left: 20px">
    注：<br/>
    1、如果编辑时上传了文件，则会替换原来的文件<br/>
    2、如果部门为空，则表示对所有人都可见
</div>
<script>
    $(function () {
        $('.btn-del').click(function (e) {
            e.preventDefault();
            del($(this).attr('docId'));
        });

        $('.btn-edit-templ').click(function (e) {
            e.preventDefault();
            editTemplate($(this).attr('docId'));
        });

        $('.btn-download').click(function (e) {
            e.preventDefault();
            window.open('../flow/getTemplateFile?id=' + $(this).attr('docId'));
        });

        $('.btn-download-tail').click(function (e) {
            e.preventDefault();
            window.open('../flow/getTemplateFile?id=' + $(this).attr('docId') + "&type=tail");
        });
    })

    function del(id) {
        layer.confirm('您确定要删除么？', {icon: 3, title: '提示'}, function (index) {
            $.ajax({
                type: "post",
                url: "flow_doc_template_list.jsp",
                data: {
                    op: 'del',
                    id: id
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $("body").showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    layer.msg(data.msg, {
                        offset: '6px'
                    });
                    if (data.res == 0) {
                        $('#tr' + id).remove();
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $("body").hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
            layer.close(index);
        });
    }

    function editTemplate(id) {
        openWin("flow_doc_template_edit_ntko.jsp?id=" + id, 1100, 800);
    }

    var frmId;

    function openWinDepts(formId) {
        frmId = formId;
        openWin('../dept_multi_sel.jsp', 800, 600)
    }

    function setDepts(ret) {
        var frm = o("form" + frmId);
        frm.deptNames.value = "";
        frm.depts.value = "";
        for (var i = 0; i < ret.length; i++) {
            if (frm.deptNames.value == "") {
                frm.depts.value += ret[i][0];
                frm.deptNames.value += ret[i][1];
            } else {
                frm.depts.value += "," + ret[i][0];
                frm.deptNames.value += "," + ret[i][1];
            }
        }
    }

    function getDepts() {
        return o("form" + frmId).depts.value;
    }

    $(function () {
        $('input, select, textarea').each(function () {
            if (!$('body').hasClass('form-inline')) {
                $('body').addClass('form-inline');
            }
            // ffb-input 为flexbox的样式
            if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && !$(this).hasClass('tSearch') &&
                $(this).attr('type') != 'hidden' && $(this).attr('type') != 'file' && !$(this).hasClass('ffb-input')) {
                $(this).addClass('form-control');
                $(this).attr('autocomplete', 'off');
            }
        });
    })
</script>
</body>
</html>