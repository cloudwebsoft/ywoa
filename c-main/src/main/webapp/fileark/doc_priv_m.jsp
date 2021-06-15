<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
String dir_name = ParamUtil.get(request, "dir_name");
int doc_id = ParamUtil.getInt(request, "doc_id", -1);
String title = ParamUtil.get(request, "title");
//if (doc_id == -1) {
	//return;
//}
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");

String op = ParamUtil.get(request, "op");

String isAll = ParamUtil.get(request, "isAll");
//if (doc_id.equals("")) {
	//isAll = "y";
//}
 
//if (isAll.equals("y")) {
	//doc_id = Leaf.ROOTCODE;
	//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
		//out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
		//return;
	//}
//}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理文件权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "doc_priv_m.jsp?doc_id=<%=doc_id%>&isAll=<%=isAll%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<jsp:useBean id="docPriv" scope="page" class="com.redmoon.oa.fileark.DocPriv"/>
<%
//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
//	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
//	return;
//}

docPriv.setDocId(doc_id);

Document doc = new Document();
doc = doc.getDocument(doc_id);
//if (!(docPriv.canUserDel(privilege.getUser(request)) || docPriv.canUserExamine(privilege.getUser(request)))) {
	//out.print(StrUtil.Alert_Back(privilege.MSG_INVALID + " 用户需对该节点拥有删除或审核的权限！"));
	//return;
//}

//Leaf leaf = new Leaf();
//leaf = leaf.getLeaf(doc_id);

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	int docid = ParamUtil.getInt(request, "doc_id", -1);
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("名称不能为空！","提示"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	String[] names = name.split("\\,");
	boolean re = false;
	for (String um : names) {
		if (type == DocPriv.TYPE_USER) {
			UserDb user = new UserDb();
			user = user.getUserDb(um);
			if (!user.isLoaded()) {
				continue;
			}
		}
		try {
			re = docPriv.add(um, type, docid);
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "doc_priv_m.jsp?isAll=" + isAll + "&doc_id=" + doc_id+"&title="+ StrUtil.UrlEncode(title)));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败", "提示"));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		//String leafCode = ParamUtil.get(request, "dirCode");
		DocPriv lp = new DocPriv(doc_id);
		lp.setRoles(doc_id, roleCodes);
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "doc_priv_m.jsp?isAll=" + isAll + "&doc_id=" + doc_id+"&title="+ StrUtil.UrlEncode(title)));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
}
else if ("setDept".equals(op)) {
	try {
		String deptCodes = ParamUtil.get(request, "deptCodes");
		DocPriv lp = new DocPriv(doc_id);
		lp.setDepts(doc_id, deptCodes);
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "doc_priv_m.jsp?isAll=" + isAll + "&doc_id=" + doc_id+"&title="+ StrUtil.UrlEncode(title)));
	} catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, del=0, modify=0, examine=0;
	String strsee = ParamUtil.get(request, "see");
	if (StrUtil.isNumeric(strsee)) {
		see = Integer.parseInt(strsee);
	}
	
	String strmodify = ParamUtil.get(request, "modify");
	if (StrUtil.isNumeric(strmodify)) {
		modify = Integer.parseInt(strmodify);
	}
	
	int officeSee = ParamUtil.getInt(request, "officeSee", 0);
	int officePrint = ParamUtil.getInt(request, "officePrint", 0);
	
	docPriv.setId(id);
	docPriv.setDownload(modify);
	docPriv.setSee(see);
	docPriv.setOfficeSee(officeSee);
	docPriv.setOfficePrint(officePrint);
	if (docPriv.save()) {
		if (isAll.equals("y"))
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "doc_priv_m.jsp?isAll=" + isAll));
		else	
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "doc_priv_m.jsp?isAll=" + isAll + "&doc_id=" + doc_id+"&title=" + StrUtil.UrlEncode(title)));
	}
	else
		out.print(StrUtil.jAlert_Back("修改失败！","提示"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	DocPriv lp = new DocPriv();
	lp = lp.getDocPriv(id);
	if (lp.del())
		out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "doc_priv_m.jsp?isAll=" + isAll + "&doc_id=" + doc_id));
	else
		out.print(StrUtil.jAlert("删除失败！","提示"));
}


%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">管理 <%=doc.getTitle()%> 权限</td>
    </tr>
  </tbody>
</table>
<%
String sql = "select id from doc_priv" + " order by " + orderBy + " " + sort;
Vector result = null;
if (isAll.equals("y")) {
	result = docPriv.list(sql);
}
else{
	sql = "select id from doc_priv" + " where doc_id="+ doc_id+" order by " + orderBy + " " + sort;
	result = docPriv.list(sql);
}
Iterator ir = result.iterator();
%>
<br>
<%if (!isAll.equals("y")) {%>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="right"><input class="btn" name="button" type="button" 
onclick="javascript:location.href='doc_priv_add.jsp?title=<%=StrUtil.UrlEncode(title) %>&id=<%=docPriv.getDocId()%>';" value="添加权限" width=80
height=20 />    </td>
  </tr>
</table>
<%}%>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="17%" style="cursor:pointer" onClick="doSort('name')">用户
        <%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="13%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
        <%if (orderBy.equals("priv_type")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
      </span></td>
      <td class="tabStyle_1_title" noWrap width="47%">权限</td>
      <td width="20%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
DeptMgr deptMgr = new DeptMgr();
//Directory dir = new Directory();
while (ir.hasNext()) {
 	DocPriv lp = (DocPriv)ir.next();
	//Leaf lf = dir.getLeaf(lp.getDirCode());	
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr id="tr<%=i%>" class="highlight">
      <td>
        <%
	  if (lp.getType()==DocPriv.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==DocPriv.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==DocPriv.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  else if (lp.getType()==DocPriv.TYPE_DEPT) {
	  	DeptDb dd = deptMgr.getDeptDb(lp.getName());
	  	if (dd!=null) {
	  		out.print(dd.getName());
		}
	  }
	  %>
	  <input type=hidden name="id" value="<%=lp.getId() %>">
      <input type=hidden name="doc_id" value="<%=lp.getDocId()%>">
	  <input type=hidden name="isAll" value="<%=isAll%>">
	  <input type=hidden name="title" value="<%=title %>">
      </td>
      <td><%
	  if (lp.getType()==DocPriv.TYPE_USER) {
	  	%>
        用户
        <%
	  }else if (lp.getType()==DocPriv.TYPE_ROLE) {
	  	%>
        角色
  <%
	  }
	  else if (lp.getType()==DocPriv.TYPE_USERGROUP) {
	  	%>
        用户组
  <%
	  }
	  else if (lp.getType()==DocPriv.TYPE_DEPT) {
	  	out.print("部门");
	  }
	  %></td>
      <td>
        <input name=see type=checkbox <%=lp.getSee()==1?"checked":""%> value="1">
        浏览文章&nbsp;
        <input name=modify type=checkbox <%=lp.canDownload()?"checked":""%> value="1" onclick="checkPrivDownload('tr<%=i%>')"> 
        下载附件 &nbsp;&nbsp;&nbsp;Office文件：
<input name="officeSee" type="checkbox" <%=lp.getOfficeSee()==1?"checked":""%> value="1" />
浏览&nbsp;
<input name="officePrint" type="checkbox" <%=lp.getOfficePrint()==1?"checked":""%> value="1" onclick="checkPrivPrint('tr<%=i%>')" /> 
打印
</td>
      <td align="center">
	  <input class="btn" type=submit value="修改">
&nbsp;<input class="btn" type=button onClick="sureToDel(<%=lp.getId()%>,<%=doc_id %>)" value="删除"/></td>
    </tr></form>
<%}%>
  </tbody>
</table>
<br>
<script>
function sureToDel(id,docid){
	jConfirm("您确定要删除吗?","提示",function(r){
		if(!r){return;}
		else{
			window.location.href="doc_priv_m.jsp?op=del&id="+id+"&doc_id="+docid;
		}
	})
}

function checkPrivDownload(trId) {
	var isChecked = $("#" + trId + " input[name='modify']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivPrint(trId) {
	var isChecked = $("#" + trId + " input[name='officePrint']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='officeSee']").attr("checked", true);
	}
}
</script>
</body>
</html>