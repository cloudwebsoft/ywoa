<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Vector"%>
<%@page import="cn.js.fan.db.Paginator"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.web.SkinUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@ page import="com.redmoon.oa.Config" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE>组织机构图</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<META content="Microsoft FrontPage 4.0" name=GENERATOR>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	font-size: 12pt;
	font-weight: bold;
}
-->
</style>
<script src="inc/common.js"></script>
  <script src='dwr/interface/DeptUserDb.js'></script>
  <script src='dwr/engine.js'></script>
  <script src='dwr/util.js'></script>
  <script>
  function updateResults(deptCode) {
    DWRUtil.removeAllRows("postsbody");
    DeptUserDb.list2DWR(fillTable, deptCode);
    o("resultTable").style.display = '';
  }
  
  var getCode = function(unit) { return unit.deptCode };
  var getName = function(unit) { return unit.deptName };
  var getUserName = function(unit) {
  	  var u = unit.userRealName;
	  if (u!=null && u!="")
		// return "<a href=# onClick=\"setPerson('" + unit.deptCode + "', '" + unit.deptName + "', '" + unit.userName + "')\">" + u + "</a>" 
		return u; 
	  else
	  	return "无";
  };
  
  var getDuty = function(unit) {
  	  var u = unit.duty;
	  if (u!=null && u!="")
		return u; 
	  else
	  	return "";
  };
  
  
  var getParty = function(unit) {
  	  var u = unit.party;
	  if (u!=null && u!="")
		return u; 
	  else
	  	return "";
  };  
  
  var getOperate = function(unit) {
  	  var u = unit.userName;
	  var uUrl = unit.userNameUrlEncoded;
	  if (u!=null && u!="")
		// return "<a href=# onClick=\"showWork('" + u + "')\">工作</a>&nbsp;&nbsp;<a href=# onClick=\"showKaoqin('" + u + "')\">考勤</a>&nbsp;&nbsp;<a href=# onClick=\"showDiskShare('" + u + "')\">共享</a>" 
		// return "<a href=# title='待办流程' onClick=\"showFlowDoing('" + u + "')\">待办</a>&nbsp;&nbsp;<a title='已办流程' href=# onClick=\"showFlowDone('" + u + "')\">已办</a>&nbsp;&nbsp;<a title='工作计划' href=# onClick=\"showWorkplan('" + u + "')\">计划</a>&nbsp;&nbsp;<a href=# title='工作记事' onClick=\"showWork('" + u + "')\">记事</a>&nbsp;&nbsp;<a href=# onClick=\"showKaoqin('" + u + "')\">考勤</a>&nbsp;&nbsp;<a href=# title='网络硬盘共享' onClick=\"showDiskShare('" + u + "')\">共享</a>&nbsp;<a title='用户信息' href=# onClick=\"showInfo('" + u + "')\">信息</a>" 
		return "<a href=# title='网络硬盘共享' onClick=\"showDiskShare('" + uUrl + "')\">网盘共享</a>&nbsp;&nbsp;&nbsp;&nbsp;<a title='用户信息' href=# onClick=\"showInfo('" + uUrl + "')\">用户信息</a>" 
	  else
	  	return "无";
  };
  
  var getAdminSymbol = function(unit) {
  	var deptCode = unit.deptCode;
	var depts = unit.adminDepts;
	var ary = depts.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==deptCode) {
			return "<img alt='部门管理员' src='<%=SkinMgr.getSkinPath(request)%>/images/dept_admin.gif'>";
		}
	}
	return "";
  }
  
  function fillTable(apartment) {
    <%if (License.getInstance().isGov()) {%>
    DWRUtil.addRows("postsbody", apartment, [ getAdminSymbol, getName, getUserName, getDuty, getParty, getOperate ]);
	<%}else{%>
    DWRUtil.addRows("postsbody", apartment, [ getAdminSymbol, getName, getUserName, getOperate ]);
	<%}%>
  }

  function setPerson(deptCode, deptName, userName) {
	// dialogArguments.setPerson(deptCode, deptName, userName);
	// window.close();	
  }
  
  function showWork(userName) {
  	form1.userName.value = userName;
  	form1.action = "mywork/mywork.jsp";
	form1.submit();
  }
  
  function showWorkplan(userName) {
  	form1.userName.value = userName;
  	form1.action = "workplan/workplan_list.jsp";
	form1.submit();
  }
  
  function showFlowDoing(userName) {
  	form1.userName.value = userName;
  	form1.action = "flow/flow_list.jsp?displayMode=1";
	form1.submit();
  }
  
  function showFlowDone(userName) {
  	form1.userName.value = userName;
  	form1.action = "flow_list_done.jsp";
	form1.submit();
  }
  
  function showKaoqin(userName) {
  	form1.userName.value = userName;
  	form1.action = "kaoqin.jsp";
	form1.submit();
  }
  
  function showDiskShare(userName) {
	/*
  	form1.userName.value = userName;
  	form1.action = "netdisk/netdisk_frame.jsp?op=showDirShare";
	form1.submit();
	*/
	addTab("网盘共享", "<%=request.getContextPath()%>/netdisk/netdisk_frame.jsp?op=showDirShare&userName=" + userName);	
  }
  
  function showBlog(userName) {
  	form1.userName.value = userName;
  	form1.action = "blog/myblog.jsp";
	form1.submit();
  }
  
  function showInfo(userName) {
	/*
  	form1.userName.value = userName;
  	form1.action = "user_info.jsp";
	form1.submit();
	*/
	addTab("用户信息", "<%=request.getContextPath()%>/user_info.jsp?userName=" + userName);
	
  }
</script>
<script>
function ShowChild(imgobj, name) {
	var tableobj = o("childof"+name);
	if (tableobj==null) {
		document.frames.ifrmGetChildren.location.href = "admin/dept_ajax_getchildren.jsp?op=func&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "images/i_minus.gif";
		}
		else
			imgobj.src = "images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}	
}

function insertAdjacentHTML(objId,code,isStart){ 
	var obj = document.getElementById(objId);
	if(isIE()) 
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code); 
	else{ 
		var range=obj.ownerDocument.createRange(); 
		range.setStartBefore(obj); 
		var fragment = range.createContextualFragment(code); 
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild); 
		else 
			obj.appendChild(fragment); 
	}
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String deptCode = ParamUtil.get(request, "deptCode");

if (deptCode.equals("")) {
	out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
	return;
}
//if (!privilege.canUserAdminDept(request, deptCode)) {
	// out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
//	out.print(SkinUtil.makeErrMsg(request, "请选择具有管理权限的部门！"));		
//	return;
//}

DeptMgr dm = new DeptMgr();
DeptUserDb du = new DeptUserDb();

DeptDb deptDb = new DeptDb();
deptDb = deptDb.getDeptDb(deptCode);
Vector dv = new Vector();
deptDb.getAllChild(dv, deptDb);
String depts = StrUtil.sqlstr(deptCode);
Iterator ird = dv.iterator();
while (ird.hasNext()) {
	deptDb = (DeptDb)ird.next();
	depts += "," + StrUtil.sqlstr(deptDb.getCode());
}

DeptUserDb jd = new DeptUserDb();
UserDb ud = new UserDb();

Config cfg = new Config();
boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
String orderField = showByDeptSort ? "du.orders" : "u.orders";

String sql = "select du.ID,u.id from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";

int curpage = ParamUtil.getInt(request, "CPages", 1);
int pagesize = ParamUtil.getInt(request, "pageSize", 10);

ListResult lr = jd.listResult(sql,curpage,pagesize);
Iterator iterator = lr.getResult().iterator();
int total = lr.getTotal();	
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr><td>&nbsp;</td></tr>
  <tr> 
    
    <td width="95%" align="center" valign="top">
	<div id="resultTable">
	  <table class="tabStyle_1 percent98" width="100%" cellpadding="4" cellspacing="0">
      <thead>
        <tr>
          <!-- <td width="8%" class="tabStyle_1_title">&nbsp;</td>-->
          <td width="11%" class="tabStyle_1_title">部门</td>
          <td width="14%" class="tabStyle_1_title">人员</td>
          <td width="7%" class="tabStyle_1_title">性别</td>
          <td width="22%" class="tabStyle_1_title">角色</td>
          <%if (false && License.getInstance().isGov()) {%>
          <td width="11%" class="tabStyle_1_title">职务</td>
          <td width="11%" class="tabStyle_1_title">政治面貌</td>
          <%}%>
          <td width="24%" class="tabStyle_1_title">操作</td>
        </tr>
      </thead>
      <tbody id="postsbody">
      	<%
			Vector v = lr.getResult();
			
			Iterator ir = v.iterator();
			DeptDb dd = new DeptDb();
			while (ir.hasNext()) {
				DeptUserDb pu = (DeptUserDb)ir.next();
				if (!pu.getUserName().equals(""))
					ud = ud.getUserDb(pu.getUserName());
				dd = deptDb.getDeptDb(pu.getDeptCode());
		%>
	  <tr class="highlight">
	    <!-- <td colspan="<%=License.getInstance().isGov()?6:4%>">请选择部门</td> -->
	    <td align="center"><%=dd.getName() %></td>
	    <td align="center"><%=ud.getRealName() %></td>
	    <td align="center"><%=ud.getGender()==0?"男":"女"%></td>
	    <td align="center">
<%
com.redmoon.oa.pvg.RoleDb[] rld = ud.getRoles();
int rolelen = 0;
if (rld!=null)
	rolelen = rld.length;
String roleDescs = "";
for (int m=0; m<rolelen; m++) {
	if (rld[m]==null)
		continue;
	if (rld[m].getCode().equals(com.redmoon.oa.pvg.RoleDb.CODE_MEMBER)) {
		continue;
	}

	if (roleDescs.equals("")) {
		roleDescs = StrUtil.getNullStr(rld[m].getDesc());
	}
	else {
		roleDescs += "，" + StrUtil.getNullStr(rld[m].getDesc());
	}
}
out.print(roleDescs);
%></td>
	    <%if (false && License.getInstance().isGov()) {%>
	    <td><%=pu.getDuty() %></td>
	    <td><%=pu.getParty() %></td>
	    <%} %>
	    <td align="center">
	    <!--  
	    <a href="javascript:;" onclick="addTab('网盘共享','netdisk/netdisk_frame.jsp?op=showDirShare&userName=<%=pu.getUserName() %>')">网盘共享</a>-->
	    	<a href="javascript:;" onclick="addTab('用户信息','user_info.jsp?userName=<%=pu.getUserName() %>')">用户信息</a>&nbsp;&nbsp;&nbsp;
	    </td>
	  </tr><%} %>
      </tbody>
    </table>
    <table class="percent98" width="92%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="right"><%
	String querystr = "deptCode=" + StrUtil.UrlEncode(deptCode);
    out.print(paginator.getCurPageBlock("organize_list.jsp?"+querystr));
%></td>
  </tr>
</table>
	</div>	</td>
  </tr>
  <form name="form1" action="" method="post" target="_blank">
  <tr align="center">
    <td height="28" colspan="2">&nbsp;<input name="userName" type="hidden"></td>
  </tr>
  </form>
</table>
</BODY></HTML>
