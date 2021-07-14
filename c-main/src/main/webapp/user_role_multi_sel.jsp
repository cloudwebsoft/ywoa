<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<base target="_self">
<TITLE><lt:Label res="res.flow.Flow" key="selectUserByRole"/></TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
  <script>
  var allUserOfDept="";
  var allUserRealNameOfDept = "";
  
  function setUsers() {
	// window.returnValue = users.innerText;
	if (window.opener)
		window.opener.setUsers(users.innerText, userRealNames.innerText);
	else
		dialogArguments.setUsers(users.innerText, userRealNames.innerText);
  	window.close();
  }

  function initUsers() {
	if (window.opener) {
		users.innerText = window.opener.getSelUserNames();
		userRealNames.innerText = window.opener.getSelUserRealNames();
	}
	else {
		users.innerText = dialogArguments.getSelUserNames();
		userRealNames.innerText = dialogArguments.getSelUserRealNames();
	}
	
   // 初始化可以选择的用户角色
   try {
	   var depts = window.opener.getValidUserRole();
	   if (depts!="") {
		   var ary = depts.split(",");
		   var isFinded = true;
	   	   isFinded = false;
		   var len = document.all.tags('A').length;
		   for(var i=0; i<len; i++) {
		   		try {
					var aObj = document.all.tags('A')[i];
					var canSel = false;
					for (var j=0; j<ary.length; j++) {
						if (aObj.outerHTML.indexOf(ary[j])!=-1) {
							canSel = true;
							// alert(canSel);
							break;
						}
					}
					if (!canSel && aObj.getAttribute("menu")=="true") {
						aObj.outerHTML = "<a><font color='#888888'>" + aObj.innerText + "</font></a>";
						// aObj.outerHTML = aObj.outerHTML.replace(/onClick/gi, "''");
					}
					isFinded = true;
				}
				catch (e) {}
		   }
	   }
   }
   catch (e) {}		
  }

  function selPerson(deptCode, deptName, userName, userRealName) {
	// 检查用户是否已被选择
	if (users.innerText.indexOf(userName)!=-1) {
		alert("<lt:Label res='res.flow.Flow' key='user'/>" + userRealName + "<lt:Label res='res.flow.Flow' key='hasBeenSelect'/>");
		return;
	}
	if (users.innerText=="") {
		users.innerText += userName
		userRealNames.innerText += userRealName;
	}
	else {
		users.innerText += "，" + userName;
		userRealNames.innerText += "，" + userRealName;
	}

  }
  
  function cancelSelPerson(deptCode, deptName, userName) {
	// 检查用户是否已被选择
	var strUsers = users.innerText;
	if (strUsers=="")
		return;
	if (strUsers.indexOf(userName)==-1) {
		return;
	}
	
	var strUserRealNames = userRealNames.innerText;
  	var ary = strUsers.split(",");
	var aryRealName = strUserRealNames.split(",");
	var len = ary.length;
	var ary1 = new Array();
	var aryRealName1 = new Array();
	var k = 0;
	for (i=0; i<len; i++) {
		if (ary[i]!=userName) {
			ary1[k] = ary[i];
			aryRealName1[k] = aryRealName[i];
			k++;
		}
	}
	var str = "";
	var str1 = "";
	for (i=0; i<k; i++) {
		if (str=="") {
			str = ary1[i];
			str1 = aryRealName1[i];
		}
		else {
			str += "," + ary1[i];
			str1 += "," + aryRealName1[i];
		}
	}
	users.innerText = str;
	userRealNames.innerText = str1;
  }
  
  function selAllUserOfDept() {
  	if (allUserOfDept=="")
		return;
	var allusers = users.innerText;
	var allUserRealNames = userRealNames.innerText;
	if (allusers=="") {
		allusers += allUserOfDept;
		allUserRealNames += allUserRealNameOfDept;
	}
	else {
		allusers += "," + allUserOfDept;
		allUserRealNames += "," + allUserRealNameOfDept;
	}
	// alert(allUserRealNames);
	var r = clearRepleatUser(allusers, allUserRealNames);
  	users.innerText = r[0];
	userRealNames.innerText = r[1];
  }
   
  function clearRepleatUser(strUsers, strUserRealNames) {
  	var ary = strUsers.split(",");
	var aryRealName = strUserRealNames.split(",");
	
	var len = ary.length;
	// 创建二维数组
	var ary1 = new Array();
	for (i=0; i<len; i++) {
		ary1[i] = new Array(2);
		ary1[i][0] = ary[i];
		ary1[i][1] = 0; // 1 表示重复
	}
	
	// 标记重复的用户
	for (i=0; i<len; i++) {
		var user = ary[i];
		for (j=i+1; j<len; j++) {
			if (ary1[j][1]==1)
				continue;
			if (ary[j]==user)
				ary1[j][1] = 1;
		}
	}

	// 重组为字符串
	var str = "";
	var str1 = "";
	for (i=0; i<len; i++) {
		if (ary1[i][1]==0) {
			u = ary1[i][0];
			if (str=="") {
				str = u;
				str1 = aryRealName[i];
			}
			else {
				str += "," + u;
				str1 += "," + aryRealName[i];
			}
		}
	}
	var retary = new Array();
	retary[0] = str;
	retary[1] = str1;
	return retary;
	
  }
</script>
</HEAD>
<BODY onLoad="initUsers()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="536" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
<thead>
  <tr> 
    <td height="24" colspan="3" align="center"><span><lt:Label res='res.flow.Flow' key='role'/></span></td>
  </tr>
</thead>
  <tr> 
    <td width="165" height="87" valign="top">
  <%
String showCode = ParamUtil.get(request, "showCode");
String code;
String desc;
RoleDb urole = new RoleDb();
Iterator userir = null;
if (!showCode.equals("")) {
	urole = urole.getRoleDb(showCode);
	userir = urole.getAllUserOfRole().iterator();
}
else
	userir = (new Vector()).iterator();
String unitCode = privilege.getUserUnitCode(request);	
Vector result = urole.getRolesOfUnit(unitCode);
Iterator ir = result.iterator();
%>
      <br>
      <table width="95%" align="center" class="tabStyle_1_sub">
        <tbody>
  <%
while (ir.hasNext()) {
 	RoleDb ug = (RoleDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
%>
          <tr class="row">
            <td width="31%">
              <a href="?showCode=<%=StrUtil.UrlEncode(code)%>" menu="true"><%=desc%></a>
            </td>
          </tr>
  <%}%>
        </tbody>
      </table>	</td>
    <td width="223" align="center" valign="top">
	<div id="resultTable">
	  <table width="100%" border="0" cellpadding="4" cellspacing="0" class="tabStyle_1_subTab">
        <tr>
          <td width="98" align="left" class="tabStyle_1_subTab_title"><lt:Label res='res.flow.Flow' key='depart'/></td>
          <td width="91" align="left" class="tabStyle_1_subTab_title"><lt:Label res='res.flow.Flow' key='fullName'/></td>
          <td width="74" align="left" class="tabStyle_1_subTab_title">&nbsp;</td>
        </tr>
      <tbody id="postsbody">
	  <%
	  DeptUserDb dud = new DeptUserDb();
	  while (userir.hasNext()) {
	  	UserDb ud = (UserDb)userir.next();
		String deptName = "";
		Iterator dir = dud.getDeptsOfUser(ud.getName()).iterator();
		if (dir.hasNext()) {
			DeptDb dd = (DeptDb)dir.next();
			deptName = dd.getName();
		}
	  %>
	  <script>
	  if (allUserOfDept=="") {
	  	allUserOfDept = "<%=ud.getName()%>";
		allUserRealNameOfDept = "<%=ud.getRealName()%>";
	  }
	  else {
	  	allUserOfDept += "," + "<%=ud.getName()%>";
		allUserRealNameOfDept += "," + "<%=ud.getRealName()%>";
	  }
	  </script>
	  <tr>
	    <td>
		<%
		// out.print(com.redmoon.oa.basic.RankDb.getRankName(ud.getRankCode()));
		%>
        <%=deptName%>
        </td>
	    <td><a onClick="selPerson('', '', '<%=ud.getName()%>', '<%=ud.getRealName()%>')" href="javascript:;"><%=ud.getRealName()%></a></td>
	    <td>[<a onClick="cancelSelPerson('', '', '<%=ud.getName()%>')" href="javascript:;"><lt:Label res='res.flow.Flow' key='deselect'/></a>]</td>
	  </tr>
	  <%}%>
      </tbody>
    </table>
	</div>
  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="tabStyle_1_sub">
  <tr>
    <td height="30" align="center"><input class="btn" name="button" type="button" onClick="selAllUserOfDept()" value="<lt:Label res='res.flow.Flow' key='selectRoleAllUser'/>"></td>
  </tr>
</table>
	</td>
    <td width="148" align="center" valign="top">
	  <div id="users" name="users" style="display:none"></div>
	  <div id="userRealNames" name="userRealNames" style="margin-top:10px"></div>    
    </td>
  </tr>
  <tr align="center">
    <td height="28" colspan="3">
<input class="btn" type="button" name="okbtn" value="<lt:Label res='res.flow.Flow' key='sure'/>" onClick="setUsers()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
      <input class="btn" type="button" name="cancelbtn" value="<lt:Label res='res.flow.Flow' key='cancel'/>" onClick="window.close()">    </td>
  </tr>
</table>
</BODY></HTML>
