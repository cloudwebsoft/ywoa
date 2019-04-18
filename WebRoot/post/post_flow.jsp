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
<%@page import="com.redmoon.oa.pointsys.PointSystemUtil"%>
<%@page import="com.redmoon.oa.pointsys.PointBean"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>岗位流程管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
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
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',600,480);
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
	if (users=="")
		return;
	form1.submit();
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
<div id='loading' class='loading'><img src='../images/loading.gif' /></div>
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

PostFlowDb pfDb = new PostFlowDb();
PostFlowMgr pfMgr = new PostFlowMgr();

String opts = "<option value='-1'>请选择</option>";
String sql = "select code,name from flow_directory where parent_code = 'performance'";
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
while(ri.hasNext()){
	ResultRecord rr = (ResultRecord)ri.next();
	String flowCode = rr.getString(1);
	String flowName = rr.getString(2);
	
	opts += "<option value='" + flowCode + "'>" + flowName + "</option>";
}
%>
<%@ include file="post_op_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" width="100%" align="center" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td align="center">属于岗位： <%=pdb.getString("name")%>&nbsp;的绩效考核流程</td>
    </tr>
  </tbody>
</table>

<table id="mainTable" width="98%" align="center" cellPadding="3" cellSpacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td width="10%" align="center" noWrap class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="35%" align="center" noWrap class="tabStyle_1_title">流程</td>
      <%
      PointBean pb = PointSystemUtil.getPointInit();
      if (pb.isScoreAssessInit()) {
      %>
      <td width="20%" align="center" noWrap class="tabStyle_1_title">关联积分制</td>
      <%} %>
      <td width="35%" align="center" noWrap class="tabStyle_1_title">操作</td>
	 </tr>
	   <tr>
    	<td></td>
    	<td>
			<select id="flow_code" name="flow_code">
				<%=opts %>
			</select>
		</td>
		<%
	      if (pb.isScoreAssessInit()) {
	      %>
		<td>
		</td>
		<%} %>
		<td style="text-align:center">
			<input class="btn" type="button" onclick="addPostFlow()" value="新增"/>
		</td>
    </tr>
     <%
  	 	int maxId = 0;
     	pfMgr.setPostId(postId);
     	Vector v = pfMgr.listByPostId();
     	Iterator it = v.iterator();
		while (it.hasNext()) {
		 	PostFlowDb postFlow = (PostFlowDb) it.next();
		 	int id = postFlow.getInt("id");
		 	String flowCode = postFlow.getString("flow_code");
		 	boolean isRelated = (postFlow.getInt("is_related") == 1);
		 	if (isRelated) {
		 		maxId = id;
		 	}
		%>
    <tr id="tr_<%=id %>">
      <td align="center"><input type="checkbox" name="ids" value="<%=id%>" /></td>
      <td>
      	<select id="flow_code_<%=id %>">
      		<%=opts %>
      	</select>
      	<script>
      		$(function() {
          		$('#flow_code_<%=id%>').val('<%=flowCode%>');
      		});
      	</script>
      </td>
      <%
      if (pb.isScoreAssessInit()) {
      %>
      <td align="center"><span id="is_related_<%=id %>"><%=isRelated ? "是" : "否" %></span></td>
      <%} %>
      <td align="center">
      	<input type="button" class="btn" value="修改" onclick="modifyPostFlow('<%=id %>')"/>
      &nbsp;&nbsp;
      	<input type="button" class="btn" value="删除" onclick="deletePostFlow('<%=id %>')" />
      	<%if (pb.isScoreAssessInit()) { %>
      	&nbsp;&nbsp;
      	<input id='rbtn_<%=id %>' type="button" class="btn" value="关联积分制" onclick="relateScore('<%=id %>')" <%=isRelated ? "style='display:none'" : "" %> />
      	<input id='cbtn_<%=id %>' type="button" class="btn" value="取消关联" onclick="cancelScore('<%=id %>')" <%=isRelated ? "" : "style='display:none'" %> />
      	<%} %>
      </td>
    </tr>
<%}%>
  </tbody>
</table>

<table width="253" align="center" class="percent80">
    <tr>
      <td colspan="7" align="left">
      <input class="btn" type="button" value="删除" onclick="delBatch()" />
      </td>
    </tr>
</table>
</body>
<script>
var maxId = <%=maxId%>;
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
				url:"post_do.jsp?op=delPostFlowBatch&ids=" + ids,
				dataType:"html",
				beforeSend: function(XMLHttpRequest){
					showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data.trim());
					if(data.ret == 1){
						$('input:checkbox[name="ids"]:checked').each(function(i) {
							$('#tr_' + $(this).val()).remove();
					    });
						$('#checkbox').removeAttr("checked");
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

function addPostFlow(){
	if ($('#flow_code').val() == '-1') {
		$.toaster({priority : 'info', message : '请选择关联流程！' });
		return;
	}
	$.ajax({
		type:"get",
		url:"post_do.jsp?op=addPostFlow&post_id=<%=postId%>&flow_code=" + $('#flow_code').val(),
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if (data.ret == 1) {
				var rownum = $("#mainTable tr").length - 1;
				var row = "<tr id='tr_" + data.id + "'><td align='center'><input type='checkbox' name='ids' value='" + data.id + "' /></td>"
			    	+ "<td><select id='flow_code_" + data.id + "'><%=opts%></select><script>$(function() {$('#flow_code_" + data.id + "').val('" + $('#flow_code').val() + "');});<\/script></td>"
					+ "<td align='center'><span id='is_related_" + data.id + "'>否</span></td>"
			    	+ "<td align='center'><input type='button' class='btn' value='修改' onclick=\"modifyPostFlow('" + data.id + "')\"/>&nbsp;&nbsp;&nbsp;&nbsp;<input type='button' class='btn' value='删除' onclick=\"deletePostFlow('" + data.id + "')\" />"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;<input id='rbtn_" + data.id + "' type='button' class='btn' value='关联积分制' onclick='relateScore(\"" + data.id + "\")'  />"
			      	+ "<input id='cbtn_" + data.id + "' type='button' class='btn' value='取消关联' onclick='cancelScore(\"" + data.id + "\")' style='display:none' />"
			    	+ "</td></tr>";
					$(row).insertAfter($("#mainTable tr:eq(" + rownum + ")"));
				$('#flow_code').val('');
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

function modifyPostFlow(id){
	if ($('#flow_code_' + id).val() == '-1') {
		$.toaster({priority : 'info', message : '请选择关联流程！' });
		return;
	}
	$.ajax({
		type:"get",
		url:"post_do.jsp?op=editPostFlow&id=" + id + "&post_id=<%=postId%>&flow_code=" + $('#flow_code_' + id).val(),
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if(data.ret == 1){
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

function deletePostFlow(id){
	jConfirm("您确定要删除“" + $('#flow_code_' + id).find("option:selected").text() + "”么？", "提示", function(r) {
		if (!r) {
			return;
		} else {
			$.ajax({
				type:"get",
				url:"post_do.jsp?op=delPostFlow&id=" + id,
				dataType:"html",
				beforeSend: function(XMLHttpRequest){
					showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data.trim());
					if(data.ret == 1){
						if (maxId == id) {
							maxId = 0;
						}
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

function relateScore(id){
	$.ajax({
		type:"get",
		url:"post_do.jsp?op=relateScore&id=" + id + "&maxId=" + maxId,
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if(data.ret == 1){
				$('#is_related_' + id).text('是');
				$('#cbtn_' + id).show();
				$('#rbtn_' + id).hide();
				if (maxId > 0) {
					$('#is_related_' + maxId).text('否');
					$('#cbtn_' + maxId).hide();
					$('#rbtn_' + maxId).show();
				}
				$('#tr_' + id).fadeOut().fadeIn();
				$('#tr_' + id).insertAfter($("#mainTable tr:eq(1)"));
				maxId = id;
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

function cancelScore(id){
	$.ajax({
		type:"get",
		url:"post_do.jsp?op=cancelScore&id=" + id,
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if(data.ret == 1){
				$('#is_related_' + id).text('否');
				$('#cbtn_' + id).hide();
				$('#rbtn_' + id).show();
				maxId = 0;
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
</script>
</html>