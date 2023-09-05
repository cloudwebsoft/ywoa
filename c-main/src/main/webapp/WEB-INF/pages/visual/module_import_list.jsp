<%@ page contentType="text/html;charset=utf-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.util.StrUtil"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="com.redmoon.oa.visual.ModulePrivDb"%>
<%@ page import="org.json.JSONObject"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="q" uri="http://www.yimihome.com/tags/QObject"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html>
<html>
<head>
<title>智能模块设计 - 导入设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals("")) {
	orderBy = "name";
}
String sort = ParamUtil.get(request, "sort");
%>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "module_import_list.jsp?code=<%=code%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="../../../visual/module_setup_inc_menu_top.jsp"%>
<script>
o("menu6").className="current"; 
</script>
<div class="spacerH"></div>
<table class="percent80" width="80%" align="center">
    <tr>
        <td align="left">
            <input class="btn" name="button" type="button"
                   onclick="javascript:location.href='module_import_add.jsp?code=${code}&formCode=${formCode}';"
                   value="添加" width=80 height=20/>
        </td>
    </tr>
</table>

<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tr>
		<td width="50%" class="tabStyle_1_title">名称</td>
		<td width="20%" class="tabStyle_1_title">批量</td>
		<td width="10%" class="tabStyle_1_title">默认</td>
        <td width="20%" class="tabStyle_1_title">操作</td>
    </tr>
    <q:forEach items="${items}" var="item" varStatus="status">
        <tr id="tr${item.id}">
			<td>${item.name}</td>
			<td style="text-align: center">
					${item.is_batch == 1?"是":"否" }
			</td>
			<td align="center">
				<input id="isDefault${item.id}" name="isDefault" defaultVal="${item.is_default}" class="default-item" value="${item.id}" type="checkbox"/>
			</td>
            <td align="center">
                <input class="btn btn-edit" type="button" value="修改" itemId="${item.id}" isBatch="${item.is_batch}"/>
                &nbsp;&nbsp;
                <input class="btn" type="button" onclick="del(${item.id})" value="删除"/></td>
        </tr>
    </q:forEach>
</table>
<br />
<br>
</body>
<script language="javascript">
	$('.default-item').click(function() {
		$.ajax({
			type: "post",
			url: "setDefaultImportTempalte.do",
			data: {
				id: $(this).val(),
				moduleCode: '${code}',
                isDefault: $(this).prop('checked')
			},
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				// $('#container').showLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				if (data.res == 0) {
				    jAlert(data.msg, "提示", function() {
				        window.location.reload();
                    })
                }
				else {
                    jAlert(data.msg, "提示");
                }
			},
			complete: function (XMLHttpRequest, status) {
				// $('#container').hideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	})

    $(function() {
        $('input[defaultVal=1]').prop('checked', true);

        $('.btn-edit').click(function() {
            var id = $(this).attr('itemId');
            var isBatch = $(this).attr('isBatch');
            if (isBatch == 0) {
                window.location.href='module_import_edit_cells.jsp?id=' + id + '&code=${code}&formCode=${formCode}'
            }
            else {
                window.location.href='module_import_edit.jsp?id=' + id + '&code=${code}&formCode=${formCode}'
            }
        })
    });

    function del(id) {
        jConfirm('您确定要删除吗?', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "get",
                    url: "module_import_del.do",
                    data: {
                        id: id
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        // $('#container').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == "0") {
                            jAlert(data.msg, "提示");
                        } else {
                            jAlert(data.msg, "提示");
                            $('#tr' + id).remove();
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        // $('#container').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }
</script>
</html>