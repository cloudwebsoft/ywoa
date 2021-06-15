<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.flow.WorkflowActionDb"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.dept.DeptMgr"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int actionId = ParamUtil.getInt(request, "actionId");
// System.out.println(getClass() + " actionId=" + actionId);
WorkflowActionDb wad = new WorkflowActionDb();
wad = wad.getWorkflowActionDb(actionId); // 下一节点

boolean isCurActionXorFinish = false;
int curActionId = ParamUtil.getInt(request, "curActionId", -1);
if (curActionId != -1) {
	WorkflowActionDb curWa = wad.getWorkflowActionDb(curActionId);
	isCurActionXorFinish = curWa.isXorFinish();
}

boolean isXor = ParamUtil.get(request, "isXor").equals("true");

String depts = wad.getDept();

Vector vec = new Vector();
if (wad.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
	Vector v = privilege.getUserAdminDepts(request);
	Iterator it = v.iterator();
	while (it.hasNext()) {
		DeptDb dd = (DeptDb) it.next();
		vec.add(dd.getCode());
		Vector v1 = new Vector();
		v1 = dd.getAllChild(v1, dd);
		Iterator it1 = v1.iterator();
		while (it1.hasNext()) {
			DeptDb dd1 = (DeptDb) it1.next();
			vec.add(dd1.getCode());
		}
	}
} else {
	if (!depts.equals("")) {
		String[] ary = StrUtil.split(depts, ",");
		for (int i=0; i<ary.length; i++) {
			vec.add(ary[i]);
		}
	}
}

String userName = "";
String userRealName = "";
String path = request.getContextPath();
String mode = ParamUtil.get(request, "mode");//此参数用来判断是否单选模式,single表示单选模式
String parameterNum = ParamUtil.get(request, "parameterNum");//此参数用来设置选择用户之后，传递的参数个数
String windowSize = ParamUtil.get(request, "windowSize");
String isForm = ParamUtil.get(request, "isForm");
String name = privilege.getUser(request);
String unitCode = ParamUtil.get(request, "unitCode");
String root_code = ParamUtil.get(request, "root_code");
String from = ParamUtil.get(request,"from");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "unitCode", unitCode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "isXor", String.valueOf(isXor), getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "parameterNum", parameterNum, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "windowSize", windowSize, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "isForm", isForm, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", name, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "from", from, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

if (unitCode.equals("")) {
	unitCode = DeptDb.ROOTCODE;
}
/*
if (root_code.equals("")) {
	root_code = privilege.getUserUnitCode(request);
}
*/
root_code = DeptDb.ROOTCODE;

String sql = null;
JdbcTemplate rmconn = new JdbcTemplate();
ResultIterator ri = null;
ResultRecord rr = null;
String temp = "";

//获取最近选择的用户
String currentUsers = "<select name='webmenu2' id='webmenu2' style='width:300px;' size='24' multiple='true' ondblclick='notSelected(this);'>";
if (unitCode.equals(DeptDb.ROOTCODE)) {
	sql = "select u.name,u.realName,u.gender from users u,user_recently_selected a where a.userName=u.name and u.isValid=1 and a.name = " +StrUtil.sqlstr(name)+" order by a.times desc"; 
}else{
	sql = "select u.name,u.realName,u.gender from users u,user_recently_selected a where a.userName=u.name and u.isValid=1 and a.name = " +StrUtil.sqlstr(name)+" and u.unit_code = " + StrUtil.sqlstr(unitCode)+ " order by a.times desc"; 
}
int num = 0;
String deptName = "";
int gender =0;//0代表男性，1代表女性
String spaceSize = "";
String newName = null;
ri = rmconn.executeQuery(sql);
while (ri.hasNext()) {
	rr = (ResultRecord)ri.next();
	userName = rr.getString(1);
	userRealName = rr.getString(2);
	temp = userRealName;
	gender = rr.getInt(3);
	sql = "select d.name from dept_user du,department d where du.dept_code=d.code and du.user_name="+StrUtil.sqlstr(userName);
	ResultIterator ri1 = rmconn.executeQuery(sql);
	ResultRecord rr1 = null;
	while (ri1.hasNext()) {
		rr1 = (ResultRecord)ri1.next();
		deptName += rr1.getString(1)+(ri1.hasNext() ? "," : "");
	}
	newName = new String(userRealName.getBytes("gb2312"),"iso-8859-1");
	if(newName.length() >= 24){
		userRealName = userRealName.substring(0,10)+"...";
		newName = new String(userRealName.getBytes("gb2312"),"iso-8859-1");
	}
	for(int i=0;i<24-newName.length();i++){
		spaceSize += "&nbsp;";
	}
	userRealName = userRealName+spaceSize+deptName;
	if(num == 0){
			if( gender == 0){
				currentUsers += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";	
	 		}else{
	 			currentUsers += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' selected='selected'>"+userRealName+"</option>";	
	 		}
		}else{
			if( gender == 0){
				currentUsers += "<option value='"+userName+"' title='"+path+"/images/man.png' myattr='"+temp+"' >"+userRealName+"</option>";	
	 		}else{
	 			currentUsers += "<option value='"+userName+"' title='"+path+"/images/woman.png' myattr='"+temp+"' >"+userRealName+"</option>";	
	 		}
		}
	num++;
	if(num >= 12){
		break;
	}
	spaceSize = "";
	deptName = "";
}
currentUsers += "</select>";	

//获取所有角色
String roles = "<ul>";
String roleCode = null;
String roleDescription = null;
sql = "select code,description from user_role order by isSystem desc, unit_code asc, orders desc, description asc";
ri = rmconn.executeQuery(sql);
while (ri.hasNext()) {
	rr = (ResultRecord)ri.next();
	roleCode = rr.getString(1);
	roleDescription = rr.getString(2);
	roles += "<li onMouseOver='addLiClass(this);' onMouseOut='removeLiClass(this);' onclick='getLiRole(this);' id='"+roleCode+"' name='"+roleCode+"'>&nbsp;&nbsp;&nbsp;&nbsp;"+roleDescription+"</li>";
}
roles += "</ul>";	

//获取所有用户组
String groups = "<ul>";
String groupCode = null;
String groupDescription = null;
sql = "select code,description from user_group order by isSystem desc, code asc";
ri = rmconn.executeQuery(sql);
while (ri.hasNext()) {
	rr = (ResultRecord)ri.next();
	groupCode = rr.getString(1);
	groupDescription = rr.getString(2);
	groups += "<li onMouseOver='addLiClass(this);' onMouseOut='removeLiClass(this);' onclick='getLiGroup(this);' id='"+groupCode+"' name='"+groupCode+"'>&nbsp;&nbsp;&nbsp;&nbsp;"+groupDescription+"</li>";
}
groups += "</ul>";
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title><lt:Label res='res.flow.Flow' key='selectUser'/></title>
	<meta http-equiv="pragma" content="no-cache"/>
	<meta http-equiv="Cache-Control" content="no-cache,must-revalidate"/>
	<meta http-equiv="expires" content="Wed, 26 Feb 1997 08:21:57 GMT"/>
	<script type="text/javascript" src="<%=path %>/js/jquery.my.js"></script>
	<script type="text/javascript" src="<%=path %>/js/jstree/jstree.js"></script>
	<script type="text/javascript" src="<%=path %>/js/msdropdown/uncompressed.jquery.dd.js"></script>
	<script type="text/javascript" src="<%=path %>/js/jquery-ui/jquery-ui.my.js"></script>
	<script type="text/javascript" src="<%=path %>/js/jquery.toaster.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=path %>/js/jquery-ui/css/jquery-ui.my.css"/>
	<link type="text/css" rel="stylesheet" href="<%=path %>/skin/common.css"/>
	<link type="text/css" rel="stylesheet" href="<%=path %>/skin/common/user_multi_sel.css"/>
	<link type="text/css" rel="stylesheet" href="<%=path %>/js/jstree/themes/default/style.css"/>
	<link type="text/css" rel="stylesheet" href="<%=path %>/js/msdropdown/dd.css"/>
	<style>
		html {
			zoom: 90%
		}

		.ui-widget-header {
			color: #1c94c4;
			border: 1px solid #d7d7d7;
			background: #e7f5f9;
		}

		.jstree-default .jstree-wholerow-clicked {
			background: #c4d7ec;
		}

		.dd .ddChild A IMG {
			width: 26px;
			height: 26px;
		}

		.toaster {
			left: 30% !important;
			right: 30% !important;
			bottom: 350px !important;
		}

		.alert-info {
			filter: alpha(opacity=60) !important;
			opacity: 0.6 !important;
		}
	</style>
	<script>
		if ("<%=windowSize %>" == "") {
			self.resizeTo(900, 730);
		}
		self.moveTo((screen.width - 900) / 2, (screen.height - 730) / 2);
	</script>
</head>
<body>
<h1 class="usermulti-top"><img src="<%=path %>/skin/images/user_multi_sel/user_top_icon.png" /><lt:Label res='res.flow.Flow' key='selectUser'/></h1>
<!--选人内容开始-->
<div class="usermulti-main">
  <div class="usermulti-main-box1">
<!--选人tab标签--> <ul>
      <li id="recently"><a href="javascript:;" onclick="changeCurrentMode(this,'recently')"><lt:Label res='res.flow.Flow' key='recently'/></a></li>
      <li id="department" class="usermulti-main-box1-sel"><a href="javascript:;" onclick="changeCurrentMode(this,'department')"><lt:Label res='res.flow.Flow' key='depart'/></a></li>
      <li id="role"><a href="javascript:;" onclick="changeCurrentMode(this,'role')"><lt:Label res='res.flow.Flow' key='role'/></a></li>
      <li id="group"><a href="javascript:;" onclick="changeCurrentMode(this,'group')"><lt:Label res='res.flow.Flow' key='userGroups'/></a></li>
    </ul>
  </div>
  <div class="usermulti-main-box2">
    <div class="usermulti-main-box2-cont">
      <div class="usermulti-cont-left">
<!--用户搜索--><div class="usermulti-search">
				<input name="userName" id="userName" type="text" value="<lt:Label res='res.flow.Flow' key='enterUser'/>" oninput="changeSearchResult();" onpropertychange="changeSearchResult();" />
				<div class="usermulti-searchimg" ><a href="javascript:;" onclick="searchUser();"><lt:Label res='res.flow.Flow' key='find'/></a></div>
			</div>
			<div class="user_search_close" id="user_search_close" onclick="closeSearchResult()" style="display:none"><img title="<lt:Label res='res.flow.Flow' key='close'/>" src="skin/images/user_multi_sel/user_search_close.png" /></div>
<!--搜索模块浮层--><div class="usermulti-search-mode" id="usermulti-search-mode" style="display:none">
					<div class="usermulti-search-result" id="usermulti-search-result" >
						<select name="webmenu5" id="webmenu5" style="width:300px;" size="24" multiple="true" ondblclick="notSelected(this);">
						 </select>
					</div>
				</div>		
<!--最近模块--><div class="urer-left-recently" id="urer-left-recently" style="display:none">
				<%=currentUsers %>
		  	 </div>
<!--左侧用户分类框上半部--><div class="usermulti-left-class" id="usermulti-left-class1">
<!--收起箭头1 --> <p class="usermulti-left-class-p1" id="usermulti-left-class-p1" onclick="p1showDepts()" style="cursor:pointer;display:none;"></p>              
                  <%
                  	DeptDb leaf = dm.getDeptDb(root_code);
					DeptView dv = new DeptView(leaf);
					String jsonData = dv.getJsonStringByDept(vec);
					List<String> list = dv.getAllUnit();
					
				%>
				<div id="departmentTree" class="departmentTree" ></div>
				<div class="allRoles" id="allRoles" style="display:none">
		          <%=roles %>
		        </div>
		        <div class="allGroups" id="allGroups" style="display:none">
		          <%=groups %>
		        </div>
<!--收起箭头2--><p class="usermulti-left-class-p2" id="usermulti-left-class-p2" onclick="p2hideDepts()" style="cursor:pointer;"></p>
			</div>
<!--左侧用户分类框下半部--><div class="usermulti-left-class" id="usermulti-left-class2">
<!--收起箭头3--><p class="usermulti-left-class-p3" id="usermulti-left-class-p3" onclick="p3hideDeptUsers()" style="cursor:pointer;"></p>
                 <div id="deptUsers" class="deptUsers" >
                 	<select name="webmenu" id="webmenu" style="width:298px;" size="11" multiple="true" ondblclick="notSelected(this);">
					 </select>
                 </div>
                 <div id="roleUsers" class="roleUsers" style="display:none">
                 	<select name="webmenu3" id="webmenu3" style="width:298px;" size="11" multiple="true" ondblclick="notSelected(this);">
					 </select>
                 </div>
                 <div id="groupUsers" class="groupUsers" style="display:none">
                 	<select name="webmenu4" id="webmenu4" style="width:298px;" size="11" multiple="true" ondblclick="notSelected(this);">
					 </select>
                 </div>
<!--收起箭头4 --> <p class="usermulti-left-class-p4" id="usermulti-left-class-p4" onclick="p4showDeptUsers()" style="cursor:pointer;display:none;"></p>
			</div>
</div>
<div class="usermulti-cont-right">
  <div class="usermulti-cont-right-title">
  	<span><img src="<%=path %>/skin/images/user_multi_sel/user_icon1.jpg" width="18" height="18"><lt:Label res='res.flow.Flow' key='selectStaff'/></span>
  	<div class="usermulti-right-title-div">
  		<%
  		//判断是否有用户管理的权限，可以经已选用户保存为组
  		String saveUserGroupShow = "";
  		if (!privilege.isUserPrivValid(request, "admin.user")) {
  			saveUserGroupShow = "style='display:none'";
  		}
  		%>
  		<span <%=saveUserGroupShow%>><img src="<%=path %>/skin/images/user_multi_sel/user_icon2.jpg" width="18" height="18"><a title="<lt:Label res='res.flow.Flow' key='saveGroupTitle'/>" href="javascript:;" onclick="addUserGroup();" id="addUserGroup"><lt:Label res='res.flow.Flow' key='saveGroup'/></a></span>
  		<span><img src="<%=path %>/skin/images/user_multi_sel/user_icon3.jpg" width="18" height="18"><a href="javascript:;" onclick="clearUsers();"><lt:Label res='res.flow.Flow' key='empty'/></a></span>
  	</div>
  </div> 
<!--保存为组的dialog --><div name="userGroupDialog" id="userGroupDialog" style="display:none">
						<!-- <div><label for="groupCode"><lt:Label res='res.flow.Flow' key='userGroupCode'/></label><input id="groupCode" name="groupCode" value="" style="width:140px;"/><font color="red">&nbsp;*</font></div> -->
						<div><label for="groupDescription"><lt:Label res='res.flow.Flow' key='userGroupDesc'/></label><input id="groupDescription" name="groupDescription" value="" style="width:140px;"/><font color="red">&nbsp;*</font></div>
 					  </div>
<!--右侧已选用户--><div class="usermulti-cont-right-user" id="usermulti-cont-right-user">
					<select name="websites" id="websites" style="width:300px;" size="24" multiple="true" ondblclick="hasSelected();">
					</select>
				</div>
</div>
<!--圆形全选图标--><div id="usermulti-cont-arrowall" class="usermulti-cont-arrowall" title="<lt:Label res='res.flow.Flow' key='moveAll'/>" onclick="notSelected(this);"></div>
<!--圆形右移图标--><div id="usermulti-cont-arrowright" class="usermulti-cont-arrowright" title="<lt:Label res='res.flow.Flow' key='moveRight'/>" onclick="notSelected(this);"></div>
<!--圆形左移图标--><div id="usermulti-cont-arrowleft" class="usermulti-cont-arrowleft" title="<lt:Label res='res.flow.Flow' key='delete'/>" onclick="hasSelected();"></div>
<!--圆形上移图标--><div id="usermulti-cont-arrowup" class="usermulti-cont-arrowup" title="<lt:Label res='res.flow.Flow' key='moveUp'/>" onclick="upSelected();"></div>
<!--圆形下移图标--><div id="usermulti-cont-arrowbottom" class="usermulti-cont-arrowbottom" title="<lt:Label res='res.flow.Flow' key='moveDown'/>" onclick="downSelected();"></div>
<!--圆形置顶图标--><div id="usermulti-cont-arrowstick" class="usermulti-cont-arrowstick" title="<lt:Label res='res.flow.Flow' key='moveTop'/>" onclick="topSelected();"></div>
<!--圆形置尾图标--><div id="usermulti-cont-arrowlower" class="usermulti-cont-arrowlower" title="<lt:Label res='res.flow.Flow' key='moveBottom'/>" onclick="bottomSelected();"></div>
    </div>
    <%if(from.equals("flow")){%>
    <div class="usermulti-btn-box" style="text-align:left">
    <input type="radio" name="selType" id="selTypeb" value="0"  checked="checked"/>会签&nbsp;&nbsp;
    <input type="radio" name="selType" id="selTypec" value="1" />串签
    </div>
    <%}%>
    <div class="usermulti-btn-box">
      <div class="usermulti-cont-btn1" onClick="setUsers()" id="setUsers"><lt:Label res='res.flow.Flow' key='sure'/></div>
      <div class="usermulti-cont-btn2" onClick="window.close()"><lt:Label res='res.flow.Flow' key='close'/></div>
    </div>
  </div>
</div>
</BODY>
<script LANGUAGE="JavaScript">
var currentMode = "department";
var selectedLiRoleCode = "";
var selectedLiGroupCode = "";
var oHandler1;
$(document).ready(function(){
	$('#departmentTree').jstree({
				    	"core" : {
				            "data" :  <%=jsonData%>,
				            "themes" : {
							   "theme" : "default" ,
							   "dots" : true,  
							   "icons" : true  
							},
							"check_callback" : true,	
				 		},
				 		"ui" : {"initially_select" : [ "root" ]  },
				 		"plugins" : ["wholerow", "themes", "ui", ,"types","state"],
					}).bind('select_node.jstree', function (e, data) {//绑定选中事件
					     //alert(data.node.id);
					     ajaxGetDeptUsers(data.node.id);
   					});

   	try {
   		//是否有限定部门
		var depts = "<%=depts%>";
	 	if (depts != "") {
	 		$("#recently").hide();//隐藏“最近”选人模块
	 		$("#role").hide();//隐藏“角色”选人模块
	 		$("#group").hide();//隐藏“用户组”选人模块
	 	}else{
	 		$("#urer-left-recently select").msDropDown();//初始化最近选择用户select
	 		$("#roleUsers select").msDropDown();//初始化角色用户的select
	 		$("#groupUsers select").msDropDown();//初始化用户组用户的select
	 	}
   		
		$("#deptUsers select").msDropDown();//初始化部门用户的select
		$("#usermulti-search-result select").msDropDown().data("dd");//初始化搜素用户的select

		initUsers();//初始化传进来的所有用户
	} catch(e) {
		alert(e.message);
	}
	   	
   	//搜索框获取焦点后，绑定Enter快捷键事件,会点击查询按钮
   	document.getElementById("userName").onfocus = function () {
   		if(this.value =='<lt:Label res='res.flow.Flow' key='enterUser'/>'){
   			this.value ='';
   		}
	   	document.onkeypress=function(event){
	   		var e = event || window.event; 
			if(e.keyCode==13){         
				searchUser();
			}        
		}; 
	};
	//搜索框获取焦点后，接触Enter快捷键事件,不然会一直点击“查询”按钮
	document.getElementById("userName").onblur = function () {
   		if(this.value ==""){
   			this.value ='<lt:Label res='res.flow.Flow' key='enterUser'/>';
   		}
   		document.onkeypress="";
	};
});

var userNamesSelected = ""; // 记录之前已选择的用户
function initUsers() {
	var win = window.opener ? window.opener : dialogArguments;

	curId = win.curUserSelectActionId;

	<%if("single".equals(mode)){%>
		oHandler1 = $("#websites").msDropDown().data("dd");//初始化已选用户的select
		$("#websites_child").css("height","444px");
		return;
	<%}%>
	
	var userNames = "";
	var userRealNames = "";
	
	var objs = win.document.getElementsByName("WorkflowAction_" + curId);
	var spanobjs = win.document.getElementsByName("WorkflowAction_span_" + curId);
	if (spanobjs.length == 0) {
		var temp = win.document.getElementsByTagName("span");
		var j = 0;
		for (i = 0; i < temp.length; i++) {
			if (temp[i].name == "WorkflowAction_span_" + curId) {
				spanobjs[j++] = temp[i];
			}
		}
	}
	try {
		for (i = 0; i < objs.length; i++) {
			if (userNames.indexOf(objs[i].value) != -1) {
				continue;
			}
			if (userNames == "") {
				userNames = objs[i].value;
				userRealNames = spanobjs[i].innerText;
			} else {
				userNames += "," + objs[i].value;
				userRealNames += "," + spanobjs[i].innerText
			}
		}
		// userName.value = userNames;
	} catch(e) {
		userNames = "";
		userRealNames = "";
	}
	
	// 记录之前已选择的用户
	userNamesSelected = userNames;
	
	<%
	// 如果是下达模式，则不从父窗口中取已被他人选取的用户，以免重复
	if (wad.isStrategyGoDown() || isCurActionXorFinish) {
	%>
		oHandler1 = $("#websites").msDropDown().data("dd");//初始化已选用户的select
		$("#websites_child").css("height","444px");
		return;
	<%
	}
	%>

	if(userNames != ""){
		$.ajax({
			type: "post",
			url: "<%=path%>/user_multi_sel_ajax.jsp",
			data : {
			    "op": "initUsers",
	        	"userNames" : encodeURI(userNames),
	        	"userRealNames" : encodeURI(userRealNames)
	        },
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			complete: function(XMLHttpRequest, status){
			},
			success: function(data, status){
				var re = $.parseJSON(data);
				if (re.ret=="1") {
					document.getElementById("usermulti-cont-right-user").innerHTML = re.result;
					oHandler1 = $("#websites").msDropDown().data("dd");//初始化已选用户的select
					$("#websites_child").css("height","444px");
				}	
			},
			error: function(){
			}
		});
	}
	oHandler1 = $("#websites").msDropDown().data("dd");//初始化已选用户的select
	$("#websites_child").css("height","444px");
	
}

var curId = "";

function setUsers() {
	var win = window.opener ? window.opener : dialogArguments;

	var uns = "," + userNamesSelected + ",";
	var str = "";
	var uNames = $("#websites option");
	if (typeof(uNames) != 'undefined' && uNames.length>0) {
		for (var i=0; i<uNames.length; i++) {
			var userName = uNames.eq(i).val();
			<%
            if (wad.isStrategyGoDown() || isCurActionXorFinish) {
            %>
			// 过滤掉已被选择的用户
			if (uns.indexOf("," + userName + ",") !=-1 ) {
				continue;
			}
			<%}%>
			
			var userRealName = uNames.eq(i).html();
			<%if (isXor) {%>
				try {
					// 发散的情况必须先执行一次checkXOR
					// var xorObj = win.document.getElementById("XOR<%=actionId%>");
					// xorObj.checked = true; // 无效，虽然属性为true，但看不到打勾（把XOR<%=actionId%>的display置为空）
					// $("#XOR<%=actionId%>").attr("checked","checked"); // 无效，alert时，属性checked仍为undefined
                    // $(xorObj).prop('checked',true);
                    // IE11下必须要调用下句才生效				
					win.setXORActionChecked(<%=actionId%>);
				} catch (e) {}
				str += "<span name=\"WorkflowAction_span_" + curId + "\" class='checkerUser'><input onclick=\"checkXOR(this,'<%=actionId%>')\" name=\"WorkflowAction_" + curId + "\" value=\"" + userName + "\" type=\"checkbox\" checked=\"checked\">" + userRealName + "</span>";
			<%}else{%>
				str += "<span name=\"WorkflowAction_span_" + curId + "\" class='checkerUser'><input name=\"WorkflowAction_" + curId + "\" value=\"" + userName + "\" checked type=\"checkbox\" checked=\"checked\">" + userRealName + "</span></span>";
			<%}%>
		}
	}

	var temp = win.document.getElementById("dlg").innerHTML;
	if (temp != '') {
		/*
		// jquery-ui无法自动刷新,使用如下方法在ie8下会依然有问题,而且这个方法还会重复添加用户,故舍弃
		var sep = '<span id=\"checker\">';
		var pos = temp.indexOf(sep);
		if (pos != -1) {
			pos += sep.length;
			temp = temp.substring(0, pos) + str + temp.substring(pos);
			win.document.getElementById("dlg").innerHTML = temp;
		}*/
		win.document.getElementById("spanNextUser").innerHTML = temp;
		win.document.getElementById("spanUsers_" + curId).innerHTML = str;
		win.document.getElementById("dlg").innerHTML = win.document.getElementById("spanNextUser").innerHTML;
	} else {
		<%
		if (wad.isStrategyGoDown() || isCurActionXorFinish) {
		%>
			// 过滤掉已选择的用户，仅添加新选择的用户
			$(win.document.getElementById("spanUsers_" + curId)).append(str);
		<%
		}
		else {
		%>
			win.document.getElementById("spanUsers_" + curId).innerHTML = str;
		<%}%>
	}
	window.close();
}

function getSelUserNames() {
	if (userName.value=="<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>") {
		return "";
	}
	if (nodeMode.value=="<%=WorkflowActionDb.NODE_MODE_USER%>") {
		if (userName.value=="$self")
			return "";
		else
			return userName.value;
	}
	else
		return "";
}

function getSelUserRealNames() {
	if (userName.value=="<%=WorkflowActionDb.PRE_TYPE_USER_SELECT%>") {
		return "";
	}
	if (nodeMode.value=="<%=WorkflowActionDb.NODE_MODE_USER%>") {
		if (userName.value=="$self")
			return "";
		else
			return userRealName.value;
	}
	else
		return "";
}

function changeCurrentMode(obj,flag){
	if(currentMode == flag){
		return;
	}
	$("#"+currentMode).removeClass("usermulti-main-box1-sel");//给旧的模块去掉样式
	$("#"+flag).addClass("usermulti-main-box1-sel");//给新的模块加样式
	//将搜索隐藏
	$("#user_search_close").hide();
	$("#usermulti-search-mode").hide();
	$("#userName").val("<lt:Label res='res.flow.Flow' key='enterUser'/>");
	currentMode = flag;
	if(flag == "recently"){
		$("#urer-left-recently").show();
		$("#webmenu2_child").css("height","440px");
		$("#usermulti-left-class1").hide();
		$("#usermulti-left-class2").hide();
	}else if(flag == "department"){
		$("#urer-left-recently").hide();
		$("#usermulti-left-class1").show();
		$("#usermulti-left-class2").show();
		$("#usermulti-left-class1").css("height","217px");
		$("#usermulti-left-class2").css("height","217px");
		$("#usermulti-left-class-p1").hide();	
		$("#usermulti-left-class-p2").show();
		$("#usermulti-left-class-p3").show();
		$("#usermulti-left-class-p4").hide();	
		$("#departmentTree").show();
		$("#departmentTree").css("height","194px");
		$("#deptUsers").show();
		$("#webmenu_child").css("height","203px");
		$("#allRoles").hide();
		$("#roleUsers").hide();
		$("#allGroups").hide();
		$("#groupUsers").hide();
	}else if(flag == "role"){
		$("#urer-left-recently").hide();
		$("#usermulti-left-class1").show();
		$("#usermulti-left-class2").show();
		$("#usermulti-left-class1").css("height","217px");
		$("#usermulti-left-class2").css("height","217px");
		$("#usermulti-left-class-p1").hide();	
		$("#usermulti-left-class-p2").show();
		$("#usermulti-left-class-p3").show();
		$("#usermulti-left-class-p4").hide();	
		$("#departmentTree").hide();
		$("#deptUsers").hide();
		$("#allRoles").show();
		$("#allRoles").css("height","194px");
		$("#roleUsers").show();
		$("#webmenu3_child").css("height","203px");
		$("#allGroups").hide();
		$("#groupUsers").hide();
		if(selectedLiRoleCode == ""){
			selectedLiRoleCode = "member";
			getLiRole(document.getElementById("member"));
		}
	}else if(flag == "group"){
		$("#urer-left-recently").hide();
		$("#usermulti-left-class1").show();
		$("#usermulti-left-class2").show();
		$("#usermulti-left-class1").css("height","217px");
		$("#usermulti-left-class2").css("height","217px");
		$("#usermulti-left-class-p1").hide();	
		$("#usermulti-left-class-p2").show();
		$("#usermulti-left-class-p3").show();
		$("#usermulti-left-class-p4").hide();	
		$("#departmentTree").hide();
		$("#deptUsers").hide();
		$("#allRoles").hide();
		$("#roleUsers").hide();
		$("#allGroups").show();
		$("#allGroups").css("height","194px");
		$("#groupUsers").show();
		$("#webmenu4_child").css("height","203px");
		if(selectedLiGroupCode == ""){
			selectedLiGroupCode == "Everyone";
			getLiGroup(document.getElementById("Everyone"));
		}
	}
}

function p1showDepts(){
	$("#usermulti-left-class-p1").hide();
	$("#usermulti-left-class-p2").show();
	$("#usermulti-left-class-p3").hide();
	$("#usermulti-left-class-p4").show();
	$("#usermulti-left-class1").css("height","424px");
	$("#usermulti-left-class2").css("height","12px");
	if(currentMode == "department"){
		$("#departmentTree").show();
		$("#departmentTree").css("height","412px");
		$("#deptUsers").hide();
	}else if(currentMode == "role"){
		$("#allRoles").show();
		$("#allRoles").css("height","412px");
		$("#roleUsers").hide();
	}else if(currentMode == "group"){
		$("#allGroups").show();
		$("#allGroups").css("height","412px");
		$("#groupUsers").hide();
	}
}

function p2hideDepts(){
	if(currentMode == "department"){
		if($("#usermulti-left-class1").css("height") == "217px"){
			$("#usermulti-left-class-p1").show();
			$("#usermulti-left-class-p2").hide();
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","12px");
			$("#usermulti-left-class2").css("height","424px");
			$("#departmentTree").hide();
			$("#webmenu_child").css("height","410px");
		}else{
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#departmentTree").css("height","194px");
			$("#deptUsers").show();
			$("#webmenu_child").css("height","203px");
		};
	}else if(currentMode == "role"){
		if($("#usermulti-left-class1").css("height") == "217px"){
			$("#usermulti-left-class-p1").show();
			$("#usermulti-left-class-p2").hide();
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","12px");
			$("#usermulti-left-class2").css("height","424px");
			$("#allRoles").hide();
			$("#webmenu3_child").css("height","410px");
		}else{
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#allRoles").css("height","194px");
			$("#roleUsers").show();
			$("#webmenu3_child").css("height","203px");
		};
	}else if(currentMode == "group"){
		if($("#usermulti-left-class1").css("height") == "217px"){
			$("#usermulti-left-class-p1").show();
			$("#usermulti-left-class-p2").hide();
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","12px");
			$("#usermulti-left-class2").css("height","424px");
			$("#allGroups").hide();
			$("#webmenu4_child").css("height","410px");
		}else{
			$("#usermulti-left-class-p3").show();
			$("#usermulti-left-class-p4").hide();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#allGroups").css("height","194px");
			$("#groupUsers").show();
			$("#webmenu4_child").css("height","203px");
		};
	}
}

function p3hideDeptUsers(){
	if(currentMode == "department"){
		if($("#usermulti-left-class2").css("height") == "217px"){
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class-p3").hide();
			$("#usermulti-left-class-p4").show();
			$("#usermulti-left-class1").css("height","424px");
			$("#usermulti-left-class2").css("height","12px");
			$("#deptUsers").hide();
			$("#departmentTree").css("height","412px");
		}else{
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#departmentTree").show();
			$("#departmentTree").css("height","194px");
			$("#webmenu_child").css("height","203px");
		};
	}else if(currentMode == "role"){
		if($("#usermulti-left-class2").css("height") == "217px"){
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class-p3").hide();
			$("#usermulti-left-class-p4").show();
			$("#usermulti-left-class1").css("height","424px");
			$("#usermulti-left-class2").css("height","12px");
			$("#roleUsers").hide();
			$("#allRoles").css("height","412px");
		}else{
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#allRoles").show();
			$("#allRoles").css("height","194px");
			$("#webmenu3_child").css("height","203px");
		};
	}else if(currentMode == "group"){
		if($("#usermulti-left-class2").css("height") == "217px"){
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class-p3").hide();
			$("#usermulti-left-class-p4").show();
			$("#usermulti-left-class1").css("height","424px");
			$("#usermulti-left-class2").css("height","12px");
			$("#groupUsers").hide();
			$("#allGroups").css("height","412px");
		}else{
			$("#usermulti-left-class-p1").hide();
			$("#usermulti-left-class-p2").show();
			$("#usermulti-left-class1").css("height","217px");
			$("#usermulti-left-class2").css("height","217px");
			$("#allGroups").show();
			$("#allGroups").css("height","194px");
			$("#webmenu4_child").css("height","203px");
		};
	}
}

function p4showDeptUsers(){
	$("#usermulti-left-class-p1").show();
	$("#usermulti-left-class-p2").hide();
	$("#usermulti-left-class-p3").show();
	$("#usermulti-left-class-p4").hide();
	$("#usermulti-left-class1").css("height","12px");
	$("#usermulti-left-class2").css("height","424px");
	if(currentMode == "department"){
		$("#departmentTree").hide();
		$("#deptUsers").show();
		$("#webmenu_child").css("height","412px");
	}else if(currentMode == "role"){
		$("#allRoles").hide();
		$("#roleUsers").show();
		$("#webmenu3_child").css("height","412px");
	}else if(currentMode == "group"){
		$("#allGroups").hide();
		$("#groupUsers").show();
		$("#webmenu4_child").css("height","412px");
	}
}

//重置已选人员的select
function resetSelected(){
	var options = $("#websites option"); 
	if(options.length > 0){
		var optionsSelected = $("#websites option:selected");
		if(optionsSelected.length == 0){
			options.eq(0).attr("selected",true); 
		}
	}
	oHandler1 = $("#websites").msDropDown().data("dd");
	$("#websites_child").css("height","444px");
}

function userIsExist(obj){
	var isExist = false;
	var options = $("#websites option");//option集合
	for(var i=0;i<options.length;i++){
		if(obj.value == options.eq(i).get(0).value){
			isExist = true;
			break;
		}
	}
	return isExist;
}

function notSelected(obj){
	var selected;
	if(document.getElementById("usermulti-search-mode").style.display == "none"){
		if(obj.id == "usermulti-cont-arrowall"){
			if(currentMode == "recently"){
				selected = $("#webmenu2 option");
			}else if(currentMode == "department"){
				selected = $("#webmenu option");
			}else if(currentMode == "role"){
				selected = $("#webmenu3 option");
			}else if(currentMode == "group"){
				selected = $("#webmenu4 option");
			}
		}else{
			if(currentMode == "recently"){
				selected = $("#webmenu2 option:selected");
			}else if(currentMode == "department"){
				selected = $("#webmenu option:selected");
			}else if(currentMode == "role"){
				selected = $("#webmenu3 option:selected");
			}else if(currentMode == "group"){
				selected = $("#webmenu4 option:selected");
			}
		}
	}else{
		if(obj.id == "usermulti-cont-arrowall"){
			selected = $("#webmenu5 option");
		}else{
			selected = $("#webmenu5 option:selected");
		}
	}
	
	if(selected.length == 0){
		return;
	}
	
	if("<%=mode%>" == "single"){
		if($("#websites option").length > 0){
			alert("<lt:Label res='res.flow.Flow' key='onlyoneUser'/>");
			return;
		}
		if(selected.length > 1){
			alert("<lt:Label res='res.flow.Flow' key='onlyoneUser'/>");
			return;		
		}
	}
	
	//先重置，再添加
	resetSelected();
	
	var obj;
	var newOption;//用自定义标签获取值
	for(var i=0;i<selected.length;i++){
		obj = selected.eq(i).get(0);
		newOption = document.createElement("option");
		newOption.value = obj.value;
		newOption.text = obj.getAttribute("myattr");
		newOption.setAttribute("title",obj.getAttribute("title"));
		if(!userIsExist(obj)){
			oHandler1.add(newOption);
		}	
	}
}


function hasSelected(){
	var selected = $("#websites option:selected");//option集合
	for(var i=selected.length-1;i>=0;i--){
		oHandler1.remove(selected.eq(i).index());
	}
}

function topSelected(){
	//第一步：将select排序
	var selectedA = $("#websites option:selected");//option集合
	for(var i=selectedA.length-1;i>=0;i--){
		var obj = selectedA.eq(i);
		obj.prependTo($('#websites'));
	}
	//第二步:重置select，使前台页面显示实时更新
	resetSelected();
}

function bottomSelected(){
	//第一步：将select排序
	var selectedA = $("#websites option:selected");//option集合
	for(var i=0;i<selectedA.length;i++){
		var obj = selectedA.eq(i);
		obj.appendTo($('#websites'));
	}
	//第二步:重置select，使前台页面显示实时更新
	resetSelected();
}

function upSelected(){
	//第一步：将select排序
	var selected = $("#usermulti-cont-right-user a");
	var optionsAsA = selectedOptionsAsA(selected);
	for(var i=0;i<optionsAsA.length;i++){
		var obj = optionsAsA[i];
		var optionIndex = obj.get(0).index;
		if(optionIndex > 0){ 
			obj.insertBefore(obj.prev('option')); 
			selected.eq(optionIndex).insertBefore(selected.eq(optionIndex-1));
		}else{
			obj.appendTo($('#websites'));
			selected = $("#usermulti-cont-right-user a");
			selected.eq(optionIndex).appendTo($(selected.eq(optionIndex).get(0).parentNode));
		} 
	}
	//第二步:重置select，使前台页面显示实时更新
	//resetSelected();
}

function downSelected(){
	//第一步：将select排序
	var selected = $("#usermulti-cont-right-user a");
	var optionsAsA = selectedOptionsAsA(selected);
	for(var i=optionsAsA.length-1;i>=0;i--){
		var obj = optionsAsA[i];
		var optionIndex = obj.get(0).index;
		if(optionIndex < $("#websites option").size()-1){ 
			obj.insertAfter(obj.next('option')); 
			selected.eq(optionIndex).insertAfter(selected.eq(optionIndex+1));
		}else{
			obj.prependTo($('#websites'));
			selected = $("#usermulti-cont-right-user a");
			selected.eq(optionIndex).prependTo($(selected.eq(optionIndex).get(0).parentNode));
		} 
	}
	//第二步:重置select，使前台页面显示实时更新
	//resetSelected();
}

//根据所有选中a元素的位置，获取对应位置的option，并顺序放到数组中，因为直接$("#websites option:selected");会出现不同的已选option
function selectedOptionsAsA(selected){
	var selectedA = $("#websites option");//option集合
	var num = 0;
	var options = new Array();
	for(var i=0;i<selected.length;i++){
		if(selected.eq(i).hasClass("selected")){
			options[num] = selectedA.eq(i);
			num++;
		}
	}
	return options;
}

//js在节点后面加入新的节点
function insertAfter(newElement, targetElement) {
	//得到父节点
	var parent = targetElement.parentNode;
	//如果最后一个子节点是当前元素，那么直接添加即可
	if (parent.lastChild === targetElement) {
		parent.appendChild(newElement);
	} else {
		//否则，在当前节点的下一个节点之前添加
		parent.insertBefore(newElement, targetElement.nextSibling);
	}
}

function clearUsers(){
	var usersLength = $("#websites option").length;
	if(usersLength > 0){
		if (confirm("<lt:Label res='res.flow.Flow' key='clearUsersSure'/>")){
			for(var i=usersLength-1;i>=0;i--){
				oHandler1.remove(i);
			}
		}
	}
}

function addUserGroup(){
	var allUsers = $("#websites option");
	if(allUsers.length <=0){
		alert("<lt:Label res='res.flow.Flow' key='selectUserSaveGroup'/>");
	}else{
		//弹出dialog对话框，输入用户组的code、description
		$("#userGroupDialog").dialog({
			modal: true,
			bgiframe:true,
			title : "<lt:Label res='res.flow.Flow' key='addUserGroup'/>",
			closeText : "<lt:Label res='res.flow.Flow' key='close'/>",
			buttons: {
				"<lt:Label res='res.flow.Flow' key='sure'/>": function() {
					//用户组的编码和描述不能为空
					if ($("#groupDescription").val() != '') {
						var pattern = new RegExp("'"); 
						if(pattern.test($("#groupDescription").val())){
							alert("用户组描述不能包含‘字符");
							return;
						}
						//取出所有用户，中间以逗号隔开，放入users变量中
						var users = "";
						for(var i=0;i<allUsers.length;i++){
							users += allUsers.eq(i).get(0).value+",";
						}
						users = users.substring(0,users.length-1);
						$.ajax({
							url: "<%=path%>/user_multi_sel_ajax.jsp",
							type: "post",
							dataType: "html",
							data : {
							    op: "addUserGroup",
					        	selectedUsers : users,
					        	desc : encodeURI($("#groupDescription").val())
					        },
							success: function(data, status){
								var re = $.parseJSON(data);
								if (re.ret=="2") {
									alert(re.result);
								}else if(re.ret=="1") {
									$("#groupDescription").val("");
									//刷新用户组模块
									refreshUserGroup();
								}
							},
							error: function(XMLHttpRequest, textStatus){
								alert("<lt:Label res='res.flow.Flow' key='addUserGroupWrong'/>");
							}
						});
						$(this).dialog("close");
					}else{
						alert("<lt:Label res='res.flow.Flow' key='codeAndDescNotNull'/>");
					}
				},
				"<lt:Label res='res.flow.Flow' key='cancel'/>": function() {
					$(this).dialog("close");
				}
			}
		});
		$("#setUsers").focus();
	}
}

function ajaxGetDeptUsers(code){
	//是否有限定部门
	var depts = "<%=depts%>";
	$.ajax({
		type: "post",
		url: "<%=path%>/user_multi_sel_ajax.jsp",
		data : {
		    op: "getDeptUsers",
        	deptCode : code,
        	limitDepts : depts
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				// 当人数较多时，msDropDown的效率很低
				$("#deptUsers select").msDropDown();
				$("#usermulti-left-class-p3").show();
				$("#usermulti-left-class-p4").hide();
				$("#usermulti-left-class1").css("height","217px");
				$("#usermulti-left-class2").css("height","217px");
				$("#departmentTree").css("height","194px");
				$("#deptUsers").show();
				$("#webmenu_child").css("height","203px");

				if (re.tip!="") {
					$.toaster({
						"priority" : "info",
						"message" : re.tip
					});
				}
			}		
		},
		error: function(){
			//alert("<lt:Label res='res.flow.Flow' key='getUsersWrong'/>！");
			$.toaster({
				"priority" : "info", 
				"message" : "<lt:Label res='res.flow.Flow' key='getUsersWrong'/>"
			});
		}
	});
}


function searchUser(){
	if($("#userName").val()=="<lt:Label res='res.flow.Flow' key='enterUser'/>" || $("#userName").val()==""){
		alert("<lt:Label res='res.flow.Flow' key='enterUser'/>");
		return;
	}
	//是否有限定部门
	var depts = "<%=depts%>";
		
	$.ajax({
		type: "post",
		url: "<%=path%>/user_multi_sel_ajax.jsp",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
		data : {
		    op: "searchUsers",
        	userName : $("#userName").val(),
        	limitDepts : depts
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				if(re.result.indexOf("<option") != -1){
					$("#user_search_close").show();
					$("#usermulti-search-mode").show();
					document.getElementById("usermulti-search-result").innerHTML = re.result;
					$("#usermulti-search-result select").msDropDown().data("dd");
					$("#webmenu5_child").css("height","442px");
				}else{
					alert("<lt:Label res='res.flow.Flow' key='noUsers'/>");
				}
				
			}		
		},
		error: function(){
			//alert("<lt:Label res='res.flow.Flow' key='searchUsersWrong'/>");
			$.toaster({
				"priority" : "info", 
				"message" : "<lt:Label res='res.flow.Flow' key='searchUsersWrong'/>"
			});
		}
	});
}

function closeSearchResult(){
	$("#user_search_close").hide();
	$("#usermulti-search-mode").hide();
	$("#userName").val("<lt:Label res='res.flow.Flow' key='enterUser'/>");
}

function changeSearchResult(){
	if($("#userName").val() == ""){
		//将搜索隐藏
		$("#user_search_close").hide();
		$("#usermulti-search-mode").hide();
	}
}

function addLiClass(obj){
	if ($(obj).hasClass('liClick')) {
	}else{
		if ($(obj).hasClass('liMouseOver')) {
		}else{
			$(obj).addClass('liMouseOver');
		}
	}
}

function removeLiClass(obj){
	if ($(obj).hasClass('liClick')) {
	}else{
		if ($(obj).hasClass('liMouseOver')) {
			$(obj).removeClass('liMouseOver');
		}
	}
}

function getLiRole(obj){
	var allChildNode = obj.parentNode.childNodes
	for(var i=0;i<allChildNode.length;i++){
		if(allChildNode[i] == obj){
			if ($(allChildNode[i]).hasClass('liClick')) {
			}else{
				$(allChildNode[i]).addClass('liClick');
			}
			if ($(allChildNode[i]).hasClass('liMouseOver')) {
				$(allChildNode[i]).removeClass('liMouseOver');
			}
		}else{
			if ($(allChildNode[i]).hasClass('liClick')) {
				$(allChildNode[i]).removeClass('liClick');
			}
		}
	}
	selectedLiRoleCode = $(obj).attr("name");
	$.ajax({
		type: "post",
		url: "<%=path%>/user_multi_sel_ajax.jsp",
		data : {
		    op: "getRoleUsers",
        	roleCode : encodeURI($(obj).attr("name"))
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("roleUsers").innerHTML = re.result;
				$("#roleUsers select").msDropDown();
				$("#usermulti-left-class-p3").show();
				$("#usermulti-left-class-p4").hide();
				$("#usermulti-left-class1").css("height","217px");
				$("#usermulti-left-class2").css("height","217px");
				$("#roleUsers").show();
				$("#webmenu3_child").css("height","203px");
			}		
		},
		error: function(){
			//alert("<lt:Label res='res.flow.Flow' key='getUsersWrong'/>！");
			$.toaster({
				"priority" : "info", 
				"message" : "<lt:Label res='res.flow.Flow' key='getUsersWrong'/>"
			});
		}
	});
}

function getLiGroup(obj){
	var allChildNode = obj.parentNode.childNodes;
	for(var i=0;i<allChildNode.length;i++){
		if(allChildNode[i] == obj){
			if ($(allChildNode[i]).hasClass('liClick')) {
			}else{
				$(allChildNode[i]).addClass('liClick');
			}
			if ($(allChildNode[i]).hasClass('liMouseOver')) {
				$(allChildNode[i]).removeClass('liMouseOver');
			}
		}else{
			if ($(allChildNode[i]).hasClass('liClick')) {
				$(allChildNode[i]).removeClass('liClick');
			}
		}
	}
	selectedLiGroupCode = $(obj).attr("name");
	$.ajax({
		type: "post",
		url: "<%=path%>/user_multi_sel_ajax.jsp",
		data : {
		    op: "getGroupUsers",
        	groupCode : encodeURI($(obj).attr("name"))
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("groupUsers").innerHTML = re.result;
				$("#groupUsers select").msDropDown();
				$("#usermulti-left-class-p3").show();
				$("#usermulti-left-class-p4").hide();
				$("#usermulti-left-class1").css("height","217px");
				$("#usermulti-left-class2").css("height","217px");
				$("#groupUsers").show();
				$("#webmenu4_child").css("height","203px");
			}		
		},
		error: function(){
			//alert("<lt:Label res='res.flow.Flow' key='getUsersWrong'/>！");
			$.toaster({
				"priority" : "info", 
				"message" : "<lt:Label res='res.flow.Flow' key='getUsersWrong'/>"
			});
		}
	});
}

function refreshUserGroup(){
	$.ajax({
		type: "post",
		url: "<%=path%>/user_multi_sel_ajax.jsp",
		data : {
		    op: "refreshUserGroup"
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("allGroups").innerHTML = re.result;
				$("#"+selectedLiGroupCode).addClass('liClick');//刷新之后还是要让之前点击的那个用户组还原被点击的样式
			}		
		},
		error: function(){
		}
	});
}
</script>
</HTML>
