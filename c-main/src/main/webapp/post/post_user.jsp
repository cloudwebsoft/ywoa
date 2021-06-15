<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.post.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理岗位关联的用户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.bgiframe.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.organize.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>

<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',800,600);
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
	if (users == "") {
		return;
	}
	addUser();
	//form1.submit();
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
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
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (!privilege.isUserPrivValid(request, "archive.user")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int postId = ParamUtil.getInt(request, "post_id", 0);
PostDb pdb = new PostDb();
pdb = pdb.getPostDb(postId);

if (!pdb.isLoaded()) {
	out.println(SkinUtil.makeErrMsg(request, "岗位不存在！"));
	return;
}

PostUserMgr puMgr = new PostUserMgr();
puMgr.setPostId(postId);
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String users = ParamUtil.get(request, "users");
	boolean re = false;
	try {
		re = puMgr.create(users);
	} catch (Exception e) {
	}
	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "post_user.jsp?post_id=" + postId));
	} else {
		out.print(StrUtil.jAlert("操作失败！", "提示"));
	}
	return;
}

%>
<%@ include file="post_op_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" width="100%" align="center" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td align="center">属于岗位&nbsp;<%=StrUtil.getNullStr(pdb.getString("name"))%>&nbsp;的用户</td>
    </tr>
  </tbody>
</table>
<table id="mainTable" width="98%" align="center" cellPadding="3" cellSpacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td width="4%" align="center" noWrap class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="6%" align="center" noWrap class="tabStyle_1_title" style="display:none">序号</td>
      <td width="16%" align="center" noWrap class="tabStyle_1_title">用户名</td>
      <td width="16%" align="center" noWrap class="tabStyle_1_title">真实姓名</td>
      <td width="8%" align="center" noWrap class="tabStyle_1_title">性别</td>
      <td width="20%" align="center" class="tabStyle_1_title">所属部门</td>
      <td width="13%" align="center" class="tabStyle_1_title">操作</td>
      
    </tr>
    <%
    Vector v = puMgr.listByPostId();
    Iterator it = v.iterator();
	int m = v.size();
	boolean isNeedSort = false;
	String userNames = "";
	String userRealNames = "";
	
	while (it.hasNext()) {
		PostUserDb post = (PostUserDb) it.next();
		int id = post.getInt("id");
		String userName = post.getString("user_name");
		UserDb ud = new UserDb(userName);
		String realName = ud.getRealName();
		if (userNames.equals("")) {
			userNames = userName;
			userRealNames = realName;
		} else {
			userNames += "," + userName;
			userRealNames += "," + realName;
		}
		
		String gender = (ud.getGender() == ConstUtil.GENDER_MAN ? "男" : "女");
		int orders = post.getInt("orders");
		
		/*if (!isNeedSort && m != orders) {
			isNeedSort = true;
		}
		m--;*/
	%>
    <tr id="tr_<%=id %>">
      <td align="center"><input type="checkbox" name="ids" value="<%=id%>" /></td>
      <td align="center" style="display:none">
	  	<%=orders %>
      <td>
      <a href="javascript:;" onclick="addTab('<%=realName%>', '<%=request.getContextPath()%>/admin/organize/user_edit.jsp?name=<%=StrUtil.UrlEncode(userName)%>')"><%=userName%></a>
      </td>
      <td><%=realName%></td>
      <td align="center"><%=gender%></td>
      <td align="left"><%
			DeptUserDb du = new DeptUserDb();
			Iterator ir = du.getDeptsOfUser(userName).iterator();
			while (ir.hasNext()) {
				DeptDb dd = (DeptDb) ir.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = new DeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				} else {
					deptName = dd.getName() + "&nbsp;&nbsp;";
				}
				if (ir.hasNext()) {
					out.print(deptName + "，");
				} else {
					out.print(deptName);
				}
			}
			%></td>
      <td align='center'><input class="btn" type="button" value="删除" onclick="delPostUser('<%=id%>')" /></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="253" align="center" class="percent80">
    <tr>
      <td colspan="7" align="left">
	  <input class="btn" title="选择并新增人员" onClick="openWinUsers()" type="button" value="新增">  
      &nbsp;&nbsp;    
      <input class="btn" type="button" value="删除" onclick="delBatch()" />
      <%if (isNeedSort) {%>
      &nbsp;&nbsp;<input class="btn" type="button" value="排序" onclick="sortUser()" />
      <%}%>
      </td>
    </tr>
</table>

<form name="form1" style="display:none" action="post_user.jsp?op=add" method="post">
<table class="tabStyle_1 percent80" width="80%" border="0" align="center">
  <tr>
    <td align="center"><input name="users" id="users" type="hidden" value="">
	<input name="post_id" type="hidden" value="<%=postId%>">
        <textarea name="userRealNames" cols="50" rows="5" readonly wrap="yes" id="userRealNames"></textarea>
      </td>
  </tr>
</table>
</form>
</body>
<script>
function delBatch(){
    var ids = '';
    $('input:checkbox[name="ids"]:checked').each(function(i) {
    	if (ids == '') {
    		ids += $(this).val();
    	} else {
        	ids += ',' + $(this).val();
    	}
    });

	if (ids == ''){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除么？","提示",function(r){
		if (!r) {
			return;
		} else {
			$.ajax({
				type:"get",
				url:"post_do.jsp?op=delPostUserBatch&ids=" + ids,
				dataType:"html",
				beforeSend: function(XMLHttpRequest){
					showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data.trim());
					if (data.ret == 1) {
						$('input:checkbox[name="ids"]:checked').each(function(i) {
							$('#tr_' + $(this).val()).remove();
					    });
						$('#checkbox').removeAttr("checked");
						$.toaster({priority : 'info', message : '操作成功' });
					} else {
						$.toaster({priority : 'info', message : '操作失败' });
					}
				},
				complete: function(XMLHttpRequest, status){
					hideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function sortUser() {
	window.location.href = "post_user.jsp?op=sortUser&post_id=<%=postId%>";
}

function moveTo(userName, realName) {
	$("#dlg").dialog({
		title: "移动" + realName + "的位置",
		modal: true,
		// bgiframe:true,
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				if ($("#targetUser").val()=="") {
					jAlert("请选择用户！","提示");
				}
				else {
					if (userName==$("#targetUser").val()) {
						jAlert("请选择别的用户！","提示");
						return;
					}
					$("#userName").val(userName);
					$("#frmMove").submit();
					$(this).dialog("close");
				}
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true,
		width:300					
	});
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});

function showLoading() {
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
}

function hideLoading() {
	$(".loading").css({"display":"none"});
	$(".treeBackground").css({"display":"none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
}

function addUser() {
	$.ajax({
		type:"post",
		url:"post_do.jsp",
		data:{
			op: 'addPostUser',
			post_id: '<%=postId%>',
			users: $('#users').val()
		},
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if(data.ret == 1){
				var rownum = $("#mainTable tr").length - 1;
				$(data.data).each(function() {
					var row = "<tr id='tr_" + this.id + "'><td align='center'><input type='checkbox' name='ids' value='" + this.id + "' /></td>"
						+ "<td style='display:none'>" + this.orders + "</td>"
						+ "<td><a href='javascript:;' onclick=\"addTab('" + this.realName + "', '<%=request.getContextPath()%>/admin/organize/user_edit.jsp?name=" + encodeURI(this.userName) + "')\">" + this.userName + "</a></td>"
					    + "<td>" + this.realName + "</td>"
					    + "<td align='center'>" + this.gender + "</td>"
					    + "<td>" + this.dept + "</td>"
					    + "<td align='center'><input class='btn' type='button' value='删除' onclick=\"delPostUser('" + this.id + "')\" /></td>"
					    + "</tr>";
					$(row).insertAfter($("#mainTable tr:eq(" + rownum++ + ")"));
				});
				$('#users').val('');
				$('#userRealNames').val('');
				$.toaster({priority : 'info', message : '操作成功' });
			} else {
				$.toaster({priority : 'info', message : data.msg });
			}
		},
		complete: function(XMLHttpRequest, status){
			hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}


function delPostUser(id){
	jConfirm("您确定要删除么？", "提示", function(r) {
		if (!r) {
			return;
		} else {
			$.ajax({
				type:"get",
				url:"post_do.jsp?op=delPostUser&id=" + id,
				dataType:"html",
				beforeSend: function(XMLHttpRequest){
					showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data.trim());
					if(data.ret == 1){
						$('#tr_' + id).remove();
						$.toaster({priority : 'info', message : '操作成功' });
					} else {
						$.toaster({priority : 'info', message : data.msg });
					}
				},
				complete: function(XMLHttpRequest, status){
					hideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}
</script>
</html>