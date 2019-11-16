<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.base.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.post.*" %>
<%@ page import="com.redmoon.oa.sso.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.netdisk.UtilTools" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.video.VideoMgr" %>
<%@ page import="com.redmoon.weixin.Config" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.dept.DeptMgr"/>
<%
	String skincode = UserSet.getSkin(request);
	if (skincode == null || skincode.equals(""))
		skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	String unitCode = "";
	if (unitCode.equals(""))
		unitCode = privilege.getUserUnitCode(request);

	String searchType = ParamUtil.get(request, "searchType"); // realName-姓名、userName-帐户、account-工号、mobile-手机、Email
	if ("".equals(searchType)) {
		searchType = "realName";
	}
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>用户管理</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<style>
  .unit {
	  background-color:#CCC;
  }
</style>
<script language="JScript.Encode" src="<%=request.getContextPath()%>/js/browinfo.js"></script>				
<script language="JScript.Encode" src="<%=request.getContextPath()%>/js/rtxint.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/inc/common.js"></script>

<link href="<%=request.getContextPath()%>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  

<!--[if lte IE 9]>
	<script src="<%=request.getContextPath()%>/js/bootstrap/js/respond.min.js"></script>
	<script src="<%=request.getContextPath()%>/js/bootstrap/js/html5shiv.min.js"></script>
<![endif]-->

<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/flexigrid.js"></script>

<script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="../../js/jquery.editinplace.js"></script>

<script src="../../js/BootstrapMenu.min.js"></script>

<link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>

<script type="text/javascript" src="../../js/jquery.toaster.js"></script>

<body scroll="no">
<div id="d" style="border-left:1px solid #DDD;height:100%;width:100%;">
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
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src="<%=request.getContextPath() %>/images/loading.gif"></div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");

String op = StrUtil.getNullString(request.getParameter("op"));
String type = ParamUtil.get(request, "type");
int curpage	= ParamUtil.getInt(request, "CPages", 1);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
VideoMgr vmgr = new VideoMgr();                             
String content = ParamUtil.get(request,"content");
content = content.replaceAll("，", ",");
String condition = ParamUtil.get(request,"condition");
int isValid = ParamUtil.getInt(request,"isValid",1);    //1:在职  0：离职
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "regDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc"; 
String deptCode = ParamUtil.get(request,"deptCode");
%>
<script>
</script>
<div class="spacerH"></div>
	  <table align="center">
	  	<tr>
	  		<td>
  				<form id="formSearch" name="formSearch" method="post" class="form-inline" role="form" action="user_list.jsp" onsubmit="return searchFormOnSubmit()">
					<span style="margin-right: 20px">部门编码：<%=deptCode%></span>
	  			<%
	  			com.redmoon.oa.sso.Config ssocfg = new com.redmoon.oa.sso.Config();
	  			com.redmoon.oa.tigase.Config tigasecfg = new com.redmoon.oa.tigase.Config();
	  			if ((cfg.getBooleanProperty("isLarkUsed") && ssocfg.getBooleanProperty("isUse")) || tigasecfg.getBooleanProperty("isUse")) { %>
	  			<input type="button" class="btn btn-default" value="同步" title="同步至精灵" onclick="syncToLark()" />
	  			<%
	  			}
	  			Config weixinCfg = Config.getInstance();
	  			boolean _isUseWx = weixinCfg.getBooleanProperty("isUse");
	  			boolean _isSyncWxToOA = weixinCfg.getBooleanProperty("isSyncWxToOA");
				com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
	  			boolean _isUseDingDing = dingdingCfg.getBooleanProperty("isUse");
	  			boolean _isSyncDingDingToOA = dingdingCfg.getBooleanProperty("isSyncDingDingToOA");

	  			if(_isUseWx){
					if(_isSyncWxToOA){
				%>
	  				<input type="button" class="btn btn-default" value="微信同步" title="同步微信企业号账户至OA" onclick="syncToWeixin(0)" />
	  			<%
					}else{
				%>
	  				<input type="button" class="btn btn-default" value="微信同步" title="同步OA账户至微信，无邮箱及手机的将不能被同步" onclick="syncToWeixin(1)" />
	  			<%
					}
	  			}
	  			if(_isUseDingDing){
	  			    if(_isSyncDingDingToOA){
				%>
	  				<input type="button" class="btn btn-default" value="钉钉同步" title="同步钉钉至OA" onclick="syncToDingDing(0)" />
	  			<%
	  			    }else{ %>
	 				 <input type="button" class="btn btn-default" value="钉钉同步" title="同步OA至钉钉" onclick="syncToDingDing(1)" />
				<%
					}
	  			}
	  			if (isValid==1){ %>
					<button type="button" class="btn btn-default" title="调出部门" onclick="changeDept()">调出</button>	  			
					<button type="button" class="btn btn-default" title="调入部门" onclick="openWin('../../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>','800','480')">调入</button>

	  			<%}%>
				<button id="btnLeaveOffice" type="button" class="btn btn-default" title="停用账号" onclick="leaveOffice()">停用</button>
	  			<input id="btnEnable" style="display: none" type="button" class="btn btn-default" value="启用" title="启用账号" onclick="enableBatch()" />
	  			<%if (isValid==1){ %>
	  			<input type="button" class="btn btn-default" value="权限" title="用户权限" onclick="changePriv()" />
	  			<% }
	  			if (isValid==1 && is_bind_mobile){ %>
	  			<input type="button" class="btn btn-default" value="绑定" title="绑定手机端硬件号" onclick="bindBatch()" />
	  			<input type="button" class="btn btn-default" value="解绑" title="解绑手机端硬件号" onclick="unbindBatch()" />
	  			<%} %>
	  			<input type="button" class="btn btn-default" value="导出" title="导出全部用户" onclick="exportAll()"/>
	  			<%
	  			com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
	  			boolean canDelUser = oacfg.getBooleanProperty("canDelUser");
	  			if (canDelUser) {
	  			%>
	  			<input type="button" class="btn btn-default" value="删除" title="删除" onclick="delBatch()"/>
	  			<%} %>
					<div class="btn-group">
						<button id="btnValidTxt" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
							启用<span class="caret"></span>
						</button>
						<ul class="dropdown-menu" role="menu" id="btnValidItems">
							<li type="realName" val="1"><a href="#">启用</a></li>
							<li type="userName" val="0"><a href="#">停用</a></li>
						</ul>
					</div>
					<script>
					$('#btnValidItems li').on('click', function() {
					    $('#btnValidTxt').html($(this).text() + "<span class=\"caret\"></span>");
					    var v = $(this).text()=="启用"?1:0;
					    $('#isValid').val(v);
					});
					
					$(function() {
						$('#btnValidItems li').each(function() {
							if ($(this).attr("val")=="<%=isValid%>") {
								$('#btnValidTxt').html($(this).text() + "<span class=\"caret\"></span>");
							}
						});
					});
					</script>
		  			

				<div class="input-group" style="width: auto;">
					<div class="input-group-btn">
						<button style="width:80px" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
							<span id="spanSearchType">请选择 </span>
							<span class="caret"></span>
						</button>
						<ul class="dropdown-menu" role="menu" id="btnSearchTypeItems">
							<li type="realName"><a href="#">姓名</a></li>
							<li type="userName"><a href="#">帐户</a></li>
							<li type="account"><a href="#">工号</a></li>
							<li type="mobile"><a href="#">手机</a></li>
							<li type="email"><a href="#">邮箱</a></li>
						</ul>
					</div><!-- /btn-group -->
					<input id="condition" name="condition" value="<%=condition%>" type="text" class="form-control" style="width:150px; height:26px" />
				</div><!-- /input-group -->							
					
					<input id="searchType" name="searchType" type="hidden" value="<%=searchType%>" />
					<input type="hidden" name="deptCode" value="<%=deptCode%>" />
		  			<input type="hidden" id="isValid" name="isValid" value="<%=isValid%>"/>					
					<script>
					$('#btnSearchTypeItems li').on('click', function(){
					    $('#spanSearchType').html($(this).text());
					    $('#searchType').val($(this).attr("type"));
					});
					
					$(function() {
						$('#btnSearchTypeItems li').each(function() {
							if ($(this).attr("type")=="<%=searchType%>") {
								$('#spanSearchType').html($(this).text());
							}
						});
					});
					</script>
	  			<input name="op" type="hidden" value="search"/>
	  			<input type="submit" value="" class="oranize-cx-button" />
</form>	  			
	  		</td>
	  	</tr>
	  </table>
    <table id="mainTable">
    </table>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="deptCode" value="<%=deptCode%>" type="hidden" />
<input name="CPages" value="<%=curpage%>" type="hidden" />
<input name="pageSize" value="<%=pagesize%>" type="hidden" />
<input name="ids" type="hidden" />
</form>

</div>
<%
	boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
	StringBuffer colProps = new StringBuffer();
	if (showByDeptSort && !"".equals(deptCode) && !DeptDb.ROOTCODE.equals(deptCode)) {
		colProps.append("{display: '序号', name : 'deptOrder', width : 50, sortable : true, align: 'center', hide: false, process:editCol}");
		colProps.append(",{display: '帐号', name : 'name', width : 100, sortable : true, align: 'center', hide: false}");
	}
	else {
		colProps.append("{display: '帐号', name : 'name', width : 100, sortable : true, align: 'center', hide: false}");
	}
	colProps.append(",{display: '姓名', name : 'realName', width : 150, sortable : true, align: 'center', hide: false}");
	colProps.append(",{display: '性别', name : 'sex', width : 50, sortable : true, align: 'center', hide: false}");
	if (isUseAccount) {
		colProps.append(",{display: '工号', name : 'account', width : 100, sortable : true, align: 'center', hide: false}");
	}
	colProps.append(",{display: '所属部门', name : 'deptNames', width : 200, sortable : true, align: 'center', hide: false}");
	colProps.append(",{display: '角色', name : 'roleName', width : 180, sortable : true, align: 'center', hide: false}");
	colProps.append(",{display: '手机号', name : 'mobile', width : 100, sortable : true, align: 'center', hide: false}");
	colProps.append(",{display: '状态', name : 'status', width : 50, sortable : true, align: 'center', hide: false}");
	if (is_bind_mobile) {
		colProps.append(",{display: '手机绑定', name : 'isBindMobile', width : 100, sortable : true, align: 'center', hide: false}");
	}
%>
</body>
<script>
function delBatch() {
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择用户！","提示");
		return;
	}

	jConfirm('您确定要删除么？', '提示', function(r) {
		if (!r) {
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/delUsers.do",
			data: {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}

// 调出至部门
function selectNode(codes, name) {
	var ids = getFlexgridCheckboxValue();
	var deptCodes = '' + codes;
	jConfirm('您确定要选择 ' + name + ' 么？', '提示', function(r) {
		if (!r)
			return;
		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/changeDepts.do",
			data: {
				deptCodes: deptCodes,
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}
// lzm 将所有用户同步至微信
function syncToWeixin(type){
    var op = type == 1?"syncWeixin":"syncWeixinToOA";
    var prompt = type == 1?"您确定要将所有的用户同步至微信么？\n导入数据可能会花费较长时间，请耐心等候 ！":"您确定要将所有微信企业号用户导入至OA？\n导入数据可能会花费较长时间，请耐心等候";
	jConfirm(prompt,"提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
				type: "post",
				url: "../sync_all_do.jsp?op="+op,
				beforeSend: function(XMLHttpRequest){
					$(".treeBackground").addClass("SD_overlayBG2");
					$(".treeBackground").css({"display":"block"});
					$(".loading").css({"display":"block"});
				},
				complete: function(XMLHttpRequest, status){
					$(".loading").css({"display":"none"});
					$(".treeBackground").css({"display":"none"});
					$(".treeBackground").removeClass("SD_overlayBG2");
				},
				success: function(data, status){
					parent.parent.setToaster(data);
				},
				error: function(){
					jAlert("操作失败！","提示");
				}
			});
		}
	})
}
function syncToDingDing(type){
    var op = type == 1?"syncOAToDingding":"syncDingdingToOA";
    var prompt = type == 1?"您确定要将所有的用户同步至钉钉么？\n导入数据可能会花费较长时间，请耐心等候 ！":"您确定要将所有钉钉用户导入至OA？\n导入数据可能会花费较长时间，请耐心等候";
    jConfirm(prompt,"提示",function(r){
        if(!r){return;}
        else{
            $.ajax({
                type: "post",
                url: "../sync_all_do.jsp?op="+op,
                beforeSend: function(XMLHttpRequest){
                    $(".treeBackground").addClass("SD_overlayBG2");
                    $(".treeBackground").css({"display":"block"});
                    $(".loading").css({"display":"block"});
                },
                complete: function(XMLHttpRequest, status){
                    $(".loading").css({"display":"none"});
                    $(".treeBackground").css({"display":"none"});
                    $(".treeBackground").removeClass("SD_overlayBG2");
                },
                success: function(data, status){
                    parent.parent.setToaster(data);
                },
                error: function(){
                    jAlert("操作失败！","提示");
                }
            });
        }
    })
}
function syncToLark() {
	jConfirm("您确定要将所有用户信息同步至精灵么？\n导入数据可能会花费较长时间，请耐心等候！","提示",function(r){
		if(!r){return;}
		else{
			$.ajax({
				type: "post",
				url: "../sync_all_do.jsp?op=sync",
				beforeSend: function(XMLHttpRequest){
					$(".treeBackground").addClass("SD_overlayBG2");
					$(".treeBackground").css({"display":"block"});
					$(".loading").css({"display":"block"});
				},
				complete: function(XMLHttpRequest, status){
					$(".loading").css({"display":"none"});
					$(".treeBackground").css({"display":"none"});
					$(".treeBackground").removeClass("SD_overlayBG2");
				},
				success: function(data, status){
					parent.parent.setToaster(data);
				},
				error: function(){
					jAlert("操作失败！","提示");
				}
			});
		}
	})
}

function changeDept() {
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择要调动的用户！","提示");
		return;
	}
	openWin("<%=request.getContextPath() %>/admin/organize/organize_dept_sel.jsp", 450, 400, "yes");
}

function leaveOffice() {
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择要停用的用户！","提示");
		return;
	}

	jConfirm('您确定要操作么，停用将删除用户相关的权限！', '提示', function(r) {
		if (!r) {
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/leaveOffBatch.do",
			data: {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
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

function exportAll() {
	window.open("<%=request.getContextPath() %>/admin/user_excel.jsp?isValid=<%=isValid%>");
}
// 权限
function changePriv() {
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请选择用户！","提示");
		return;
	}
	var idsArr = ids.split(",");
	if (idsArr.length>1){
		jAlert("只能选择一个用户！","提示");
		return;
	}
	addTab('用户权限', '<%=request.getContextPath() %>/admin/organize/user_privilege.jsp?userId='+ids);
}

var flex;

function changeSort(sortname, sortorder) {
	if (!sortorder)
		sortorder = "desc";

	var params = $("#formSearch").serialize();
	// console.log(params);
	var urlStr = "<%=request.getContextPath()%>/admin/organize/user_list.jsp?" + params;
	urlStr += "&pageSize=" + $("#maintable").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();
}

function onReload() {
	doQuery();
}

$(document).ready(function() {
	var requestParams = [];
	requestParams.push({name:'deptCode', value:'<%=deptCode%>'});
	var colModel = [<%=colProps%>];

	flex = $("#mainTable").flexigrid({
		url: "<%=request.getContextPath()%>/user/list.do",
		params: requestParams,
		dataType: 'json',
		colModel : colModel,

		sortname: "<%=orderBy%>",
		sortorder: "<%=sort%>",	
		usepager: true,
		checkbox: true,
		useRp: true,
		rp: <%=pagesize%>,
		
		// title: "通知",
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		
		onChangeSort: changeSort,
		
		// onChangePage: changePage,
		// onRpChange: rpChange,
		onLoad: onLoad,
		onReload: onReload,
		/*
		onRowDblclick: rowDbClick,
		onColSwitch: colSwitch,
		onColResize: colResize,
		onToggleCol: toggleCol,*/
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
});

function onLoad() {
	try {
		onFlexiGridLoaded();
	}
	catch(e) {}
}

function getFlexgridCheckboxValue(){
	var ids = "";
	$(".cth input[type='checkbox'][value!='on']", mainTable.bDiv).each(function(i) {
		if($(this).is(":checked")) {
			if (ids=="")
				ids = $(this).val().substring(3);
			else
				ids += "," + $(this).val().substring(3);
		}
	});	
	return ids;	
}

//启用
function enableBatch(){
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择要启用的用户！","提示");
		return;
	}

	jConfirm('您确定要启用么？', '提示', function(r) {
		if (!r) {
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/enableBatch.do",
			data: {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}
//绑定
function bindBatch(){
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择要绑定的用户！","提示");
		return;
	}
	jConfirm('您确定要绑定么？', '提示', function(r) {
		if (!r) {
			return;
		}
		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/bindBatch.do",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
			data: {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}
//解绑
function unbindBatch(){
	var ids = getFlexgridCheckboxValue();
	if (ids=="") {
		jAlert("请先选择要解绑的用户！","提示");
		return;
	}
	jConfirm('您确定要解绑么？', '提示', function(r) {
		if (!r) {
			return;
		}
		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/unbindBatch.do",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
			data: {
				ids: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}

function setUsers(users, userRealNames) {
	jConfirm('您确定要调入么？', '提示', function(r) {
		if (!r) {
			return;
		}

		if (users=="") {
			jAlert("请选择调入人员");
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/transferUsers.do",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
			data: {
				deptCode: "<%=deptCode%>",
				userName: users
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}

function getSelUserNames() {
	return "";
}

function getSelUserRealNames() {
	return "";
}

function openWin(url,width,height) {
  	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function editCol(celDiv, id, colName) {
	$( celDiv ).click(function() {
		// 该插件会上传值：original_value、update_value
		$(celDiv).editInPlace({
			url: "<%=request.getContextPath()%>/user/editOrder.do",
			params: "colName=" + colName + "&id=" + id + "&deptCode=<%=deptCode%>",
			error:function(obj){
				alert(JSON.stringify(obj));
			},
			success:function(data){
				data = $.parseJSON(data);
				if (data.ret==-1) { // 值未更改
					return;
				}
				else {
					$.toaster({
						"priority" : "info",
						"message" : data.msg
					});
					$("#mainTable").flexReload();
				}
			}
		});
	});
}

function doQuery() {
	var v = $('#isValid').val();
	if (v==0) {
		$('#btnEnable').show();
		$('#btnLeaveOffice').hide();
	}
	else {
		$('#btnEnable').hide();
		$('#btnLeaveOffice').show();
	}

	var params = $("#formSearch").serialize();
	var urlStr = "<%=request.getContextPath()%>/user/list.do?" + params;
	$("#mainTable").flexOptions({url : urlStr});
	$("#mainTable").flexReload();
}

function searchFormOnSubmit() {
	doQuery();
	return false;
}
</script>
</html>