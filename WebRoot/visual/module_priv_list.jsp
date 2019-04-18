<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
// 表示从哪个tab进入本页面，用于刷新角色菜单页面user_role_menu.jsp 
if ("setFieldWrite".equals(op)) {
	int id = ParamUtil.getInt(request, "id");
	String fields = ParamUtil.get(request, "fields");
	ModulePrivDb mpd = new ModulePrivDb();
	mpd = mpd.getModulePrivDb(id);
	mpd.setFieldWrite(fields);
	boolean re = mpd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	out.print(json);
	return;
}
else if ("setFieldHide".equals(op)) {
	int id = ParamUtil.getInt(request, "id");
	String fields = ParamUtil.get(request, "fields");
	ModulePrivDb mpd = new ModulePrivDb();
	mpd = mpd.getModulePrivDb(id);
	mpd.setFieldHide(fields);
	boolean re = mpd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	out.print(json);
	return;
}
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计 - 管理权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
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
			
	window.location.href = "module_priv_list.jsp?code=<%=code%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

ModulePrivDb mpd = new ModulePrivDb();

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("名称不能为空！","提示"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	String[] names = name.split("\\,");
	boolean re = false;
	for (String um : names) {
		if (type == ModulePrivDb.TYPE_USER) {
			UserDb user = new UserDb();
			user = user.getUserDb(um);
			if (!user.isLoaded()) {
				continue;
			}
		}
		try {
			mpd.setFormCode(code);
			re = mpd.create(um, type);
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_priv_list.jsp?code=" + code + "&formCode=" + formCode + "&tabIdOpener=" + tabIdOpener));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败", "提示"));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		String[] roleCodesArr = roleCodes.split(",");
		ArrayList<String> roleCodeList = new ArrayList<String>();
		StringBuilder setRoleCode = null;                            //保存待设置的角色
		mpd.setFormCode(code);
		Vector rolesVector = mpd.getRolesOfModule(code);
		Iterator rolesIterator = null;
		if (rolesVector!=null)
			rolesIterator = rolesVector.iterator();
		while(rolesIterator!=null&&rolesIterator.hasNext()){
			RoleDb roleDb = (RoleDb)rolesIterator.next();
			String roleCode = roleDb.getCode();
			roleCodeList.add(roleCode);                       //将已经存在的角色添加到集合
		}
		for(int i=0;i<roleCodesArr.length;i++){
			if (roleCodeList.contains(roleCodesArr[i])){    //判断指定角色是否已经存在
				continue;
			} else {
				if (setRoleCode==null){
					setRoleCode = new StringBuilder();
					setRoleCode.append(roleCodesArr[i]);
				} else {
					setRoleCode.append(",").append(roleCodesArr[i]);
				}
			}
		}
		mpd.setRoles(formCode, setRoleCode.toString());
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_priv_list.jsp?code=" + code + "&formCode=" + formCode + "&tabIdOpener=" + tabIdOpener));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, manage=0;
	String strsee = ParamUtil.get(request, "see");
	if (StrUtil.isNumeric(strsee)) {
		see = Integer.parseInt(strsee);
	}
	String strappend = ParamUtil.get(request, "append");
	if (StrUtil.isNumeric(strappend)) {
		append = Integer.parseInt(strappend);
	}
	String strmanage = ParamUtil.get(request, "manage");
	if (StrUtil.isNumeric(strmanage)) {
		manage = Integer.parseInt(strmanage);
	}
	
	int modify = ParamUtil.getInt(request, "modify", 0);
	
	int view = ParamUtil.getInt(request, "view", 0);
	int search = ParamUtil.getInt(request, "search", 0);
	int reActive = ParamUtil.getInt(request, "reActive", 0);

	int importXls = ParamUtil.getInt(request, "import", 0);
	int exportXls = ParamUtil.getInt(request, "export", 0);
	int del = ParamUtil.getInt(request, "del", 0);
	int log = ParamUtil.getInt(request, "log", 0);

	if (manage==1) {
		append = 1;
		manage = 1;
		see = 1;
		modify = 1;
		view = 1;
		search = 1;
		reActive = 1;
		importXls = 1;
		exportXls = 1;
		log = 1;
	}
	
	mpd = mpd.getModulePrivDb(id);
	mpd.setAppend(append);
	mpd.setManage(manage);
	mpd.setSee(see);
	mpd.setModify(modify);
	mpd.setView(view);
	mpd.setSearch(search);
	mpd.setReActive(reActive);
	mpd.setImportXls(importXls);
	mpd.setExportXls(exportXls);

	mpd.setDel(del);
	mpd.setLog(log);
	
	if (mpd.save()) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_priv_list.jsp?code=" + code + "&formCode=" + formCode + "&tabIdOpener=" + tabIdOpener));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	mpd = mpd.getModulePrivDb(id);
	if (mpd.del())
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_priv_list.jsp?code=" + code + "&formCode=" + formCode + "&tabIdOpener=" + tabIdOpener));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

boolean isNav = tabIdOpener.equals("")?true:false;
if (isNav) {
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<%
}
String sql = "select id from visual_module_priv where form_code=" + StrUtil.sqlstr(code) + " order by " + orderBy + " " + sort;
Vector result = mpd.list(sql);
Iterator ir = result.iterator();
%>
<br/>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="left"><input class="btn" name="button" type="button" onclick="javascript:location.href='module_priv_add.jsp?code=<%=code%>&formCode=<%=formCode%>&tabIdOpener=<%=tabIdOpener%>';" value="添加" width=80 height=20 />
	</td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="10%" style="cursor:hand" onclick="doSort('name')">用户
        <%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" width="7%" style="cursor:hand" onclick="doSort('priv_type')">类型
        <%if (orderBy.equals("priv_type")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
      </td>
      <td class="tabStyle_1_title" width="55%">权限</td>
      <td width="6%" class="tabStyle_1_title">可写字段</td>
      <td width="6%" class="tabStyle_1_title">隐藏字段</td>
      <td width="16%" class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	mpd = (ModulePrivDb)ir.next();
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr class="highlight" id="tr<%=mpd.getId()%>">
      <td>
        <%
	  if (mpd.getType()==ModulePrivDb.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(mpd.getName());
		out.print(ud.getRealName());
	  }else if (mpd.getType()==ModulePrivDb.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(mpd.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (mpd.getType()==ModulePrivDb.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(mpd.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
	  <input type=hidden name="id" value="<%=mpd.getId()%>"/>
      <input type=hidden name="tabIdOpener" value="<%=tabIdOpener%>"/>
      <input type=hidden name="code" value="<%=code%>"/>
      <input type=hidden name="formCode" value="<%=formCode%>"/>
      <td><%=mpd.getTypeDesc()%></td>
      <td>
	  <input name="see" type=checkbox <%=mpd.getSee()==1?"checked":""%> value="1" title="浏览列表"/>
	  浏览列表&nbsp;
	  <input name="view" type=checkbox <%=mpd.getView()==1?"checked":""%> value="1" title="查看详情及流程"/>
	  查看详情&nbsp;
	  <input name="append" type=checkbox <%=mpd.getAppend()==1?"checked":""%> value="1" title="添加记录"/> 
	  添加 &nbsp;
	  <input name="modify" type=checkbox <%=mpd.getModify()==1?"checked":""%> value="1" title="修改记录"/> 
	  修改 &nbsp;      
	  <input name="del" type=checkbox <%=mpd.getDel()==1?"checked":""%> value="1" title="删除记录"/> 
	  删除 &nbsp;      
	  <input name="import" type=checkbox <%=mpd.getImportXls()==1?"checked":""%> value="1" title="导入"/> 
	  导入 &nbsp;      
	  <input name="export" type=checkbox <%=mpd.getExportXls()==1?"checked":""%> value="1" title="导出"/> 
	  导出 &nbsp;   
      <span style="display:none">   
	  <input name="search" type="hidden" type=checkbox <%=mpd.getSearch()==1?"checked":""%> value="1" title="高级查询"/> 
	  高级查询 &nbsp;   
      </span>
	  <input name="reActive" type=checkbox <%=mpd.getReActive()==1?"checked":""%> value="1" title="变更记录"/> 
	  变更&nbsp;
	  <input name="log" type=checkbox <%=mpd.getLog()==1?"checked":""%> value="1" title="管理日志"/> 
      日志&nbsp; 	     
	  <input name="manage" type=checkbox <%=mpd.getManage()==1?"checked":""%> value="1" onclick="setManage(this, <%=mpd.getId()%>)" title="可添加、修改、删除记录、导入、导出、管理日志"/>
	  管理
      <script>
	  function setManage(obj, id) {
		  if (obj.checked) {
			  $('#tr' + id).find("input[name=see]").prop('checked', true);
			  $('#tr' + id).find("input[name=view]").prop('checked', true);
			  $('#tr' + id).find("input[name=append]").prop('checked', true);
			  $('#tr' + id).find("input[name=modify]").prop('checked', true);
			  $('#tr' + id).find("input[name=del]").prop('checked', true);
			  $('#tr' + id).find("input[name=import]").prop('checked', true);
			  $('#tr' + id).find("input[name=export]").prop('checked', true);
			  $('#tr' + id).find("input[name=search]").prop('checked', true);
			  $('#tr' + id).find("input[name=reActive]").prop('checked', true);
			  $('#tr' + id).find("input[name=log]").prop('checked', true);
		  }
	  }
	  </script>
      </td>
      <td align="center">
		<a href="javascript:OpenFormFieldSelWin('fieldWrite<%=mpd.getId()%>', '<%=mpd.getId()%>');">选择</a>
        <textarea id="fieldWrite<%=mpd.getId()%>" name="fieldWrite<%=mpd.getId()%>" style="display:none"><%=mpd.getFieldWrite()%></textarea> 
      </td>
      <td align="center">
        <a href="javascript:OpenFormFieldSelWin('fieldHide<%=mpd.getId()%>', '<%=mpd.getId()%>');">选择</a>      
        <textarea id="fieldHide<%=mpd.getId()%>" name="fieldHide<%=mpd.getId()%>" style="display:none"><%=mpd.getFieldHide()%></textarea> 
      </td>
      <td align="center">
	  <input class="btn" type=submit value="修改"/>
&nbsp;&nbsp;<input class="btn" type=button onClick="jConfirm('您确定要删除吗?','提示',function(r){ if(!r){return;}else{ window.location.href='module_priv_list.jsp?op=del&code=<%=code%>&formCode=<%=formCode%>&id=<%=mpd.getId()%>&tabIdOpener=<%=tabIdOpener%>'}}) " value="删除"/></td>
    </tr></form>
<%}%>
  </tbody>
</table>
<br />
<br>
</body>
<script language="javascript">
<!--
function form1_onsubmit() {
	errmsg = "";
	if (form1.pwd.value!=form1.pwd_confirm.value)
		errmsg += "密码与确认密码不致，请检查！\n"
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}
}

var curFields, curId;
function OpenFormFieldSelWin(fields, id) {
	curFields = fields;
	curId = id;
	openPostWindow("form_field_sel.jsp");
}

function openWin(url,width,height) {
	var newwin=window.open(url,"fieldWin","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
	return newwin;
}

function openPostWindow(url) {
	openWin("form_field_sel.jsp",600,515); 
	var tempForm = document.createElement("form");  
	tempForm.id="tempForm1";  
	tempForm.method="post";
	tempForm.action=url;  
    
	var hideInput = document.createElement("input");  
	hideInput.type="hidden";
    hideInput.name= "formCode";
    hideInput.value= "<%=formCode%>";
    tempForm.appendChild(hideInput);
    		  
	hideInput = document.createElement("input");
    hideInput.type="hidden";
    hideInput.name="fields";
    hideInput.value=o(curFields).value;
    tempForm.appendChild(hideInput);

	document.body.appendChild(tempForm);
	tempForm.target="fieldWin";
    tempForm.submit();
    document.body.removeChild(tempForm);
}

function setFields(fieldsSeted) {
	o(curFields).value = fieldsSeted;
	var op;
	if (curFields.indexOf("fieldWrite")==0) {
		op = "setFieldWrite";
	}
	else {
		op = "setFieldHide";
	}
	
	$.ajax({
		type: "post",
		url: "module_priv_list.jsp",
		data : {
			op: op,
			id: curId,
			formCode: "<%=formCode%>",
			fields: fieldsSeted
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			// ShowLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
		},
		complete: function(XMLHttpRequest, status){
			// HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert("error:" + XMLHttpRequest.responseText);
		}
	});		
}

$(function() {
	if (window.top.mainFrame) {
		window.top.mainFrame.reloadTabById("<%=tabIdOpener%>");
	}
	else {
		if (window.top.o("content-main")) {
			window.top.reloadTabFrame("<%=tabIdOpener%>");
		}
	}
});
//-->
</script>
</html>