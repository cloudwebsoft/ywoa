<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="net.sf.json.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.privCenter.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>权限分配</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery.js"></script> 
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<div class="spacerH"></div>
<div style="width:70%;margin-right: auto;margin-left: auto;" >
<%
	PrivCenterMgr pcm = new PrivCenterMgr();	
	String parent_id = ParamUtil.get(request, "parent_id");
	String sql = (new StringBuilder("select code,name,module_code,privs,id from oa_privilege_center where parent_id=")).append(parent_id).toString();
 	JdbcTemplate rmconn = new JdbcTemplate();
	ResultIterator ri = rmconn.executeQuery(sql);
	ResultRecord rr = null;
	String code = null;
	String name = null;
	String module_code = null;
	String privs = null;
	int id = 0;
 	while (ri.hasNext()) {
 		rr = (ResultRecord)ri.next();
 		code = rr.getString(1);
 		name = rr.getString(2);
 		module_code = rr.getString(3);
 		privs = rr.getString(4);
 		id = rr.getInt(5);
 		if(module_code == null){
 			module_code = "";
 		}
 		if(privs == null){
 			privs = "";
 		}
%>
 		<table id="mainTable" width="60%" border="0" align="center" cellpadding="2" cellspacing="0" style="margin-bottom:50px" class="tabStyle_1">
          <thead>
	          <tr align="center">
	            <td colspan="2" class="tabStyle_1_title"><%=name %></td>
	          </tr>
           </thead>
           <tr>
            <td valign="top" width="50%">
            	<table width="100%" border="0" style="font-size: 14px;!important;color:#606060">
            		<tr>
            			<td align="center"><b>已授权用户</b></td>
            			<td align="right">
            				<a style="color: #0070e3" href="javascript:;" onClick="openUserWin('name<%=id%>','realName<%=id%>','../user_multi_sel.jsp','800','480','<%=code %>','<%=module_code %>','<%=privs %>')">选择</a>
            				&nbsp;&nbsp;&nbsp;<a style="color: #0070e3" href="javascript:;" onclick="clearAllUsers('<%=code %>','<%=module_code %>','<%=privs %>')">清空</a>
            			</td>
            		</tr>
            		<%
            			if((code==null || code.equals("")) && !module_code.equals("")){//表示只有模块权限没有普通权限,比如“订餐管理”
            				sql = (new StringBuilder("select a.name,a.realName from users a, visual_module_priv b where a.name = b.name and b.manage=1 and b.priv_type=")).append(LeafPriv.TYPE_USER).append(" and b.form_code = ").append(StrUtil.sqlstr(module_code)).toString();
            			}else if("root".equals(code)){//code=root表示的是文件柜跟目录，与其他普权限不一样，要判断，特殊处理
            				sql = (new StringBuilder("select a.name,a.realName from users a, dir_priv b where a.name = b.name and b.dir_code = 'root' and b.examine=1 and b.priv_type=")).append(LeafPriv.TYPE_USER).toString();
            			}else{
            				sql = (new StringBuilder("select a.name,a.realName from users a, user_priv b where a.name = b.username and b.priv = ")).append(StrUtil.sqlstr(code)).toString();
            			}
					  	String userName = null;
					  	String realName = null;
					  	ResultIterator ri1 = rmconn.executeQuery(sql);
					  	int usersCount = ri1.size();
					  	int userNum = 0;
					  	String selectedName = "";
					  	String selectedRealName = "";
						ResultRecord rr1 = null;
						while (ri1.hasNext()) {
							rr1 = (ResultRecord)ri1.next();
							userName = rr1.getString(1);
							selectedName += userName;
							realName = rr1.getString(2);
							selectedRealName += realName;
							if(++userNum < usersCount){
								selectedName += ",";
								selectedRealName += ",";
							}
							%>
							<tr>
							<td align="center"><%=realName %></td>
		            		<td align="right"><a href="javascript:;" onclick="deleteUser('<%=userName %>','<%=realName %>','<%=code %>','<%=module_code %>','<%=privs %>')">删除</a></td>
		            		</tr>
							<%
						}
					 %>
					 <input type="hidden" name="name<%=id%>" id="name<%=id%>" value="<%=selectedName%>">
					 <input type="hidden" name="realName<%=id%>" id="realName<%=id%>" value="<%=selectedRealName%>">
            	</table>
            </td>
            <td valign="top" width="50%">
            	<table width="100%" border="0" style="font-size: 14px;!important;color:#606060">
            		<tr>
            			<td align="center"><b>已授权角色</b></td>
            			<td align="right">
            				<a style="color: #0070e3" href="javascript:;" onClick="openRoleWin('role<%=id %>','640','480','<%=code %>','<%=module_code %>','<%=privs %>')">选择</a>
            				&nbsp;&nbsp;&nbsp;<a style="color: #0070e3" href="javascript:;" onclick="clearAllRoles('<%=code %>','<%=module_code %>','<%=privs %>')">清空</a>
            			</td>
            		</tr>
            		<%
					  	if((code==null || code.equals("")) && !module_code.equals("")){//表示只有模块权限没有普通权限,比如“订餐管理”
            				sql = (new StringBuilder("select a.code,a.description from user_role a, visual_module_priv b where a.code = b.name and b.manage=1 and b.priv_type=")).append(LeafPriv.TYPE_ROLE).append(" and b.form_code = ").append(StrUtil.sqlstr(module_code)).toString();
            			}else if("root".equals(code)){//code=root表示的是文件柜跟目录，与其他普权限不一样，要判断，特殊处理
            				sql = (new StringBuilder("select a.code,a.description from user_role a, dir_priv b where a.code = b.name and b.dir_code = 'root' and b.examine=1 and b.priv_type=")).append(LeafPriv.TYPE_ROLE).toString();
            			}else{
            				sql = (new StringBuilder("select a.code,a.description from user_role a, user_role_priv b where a.code = b.roleCode and b.priv = ")).append(StrUtil.sqlstr(code)).toString();
            			}
					  	String roleCode = null;
					  	String description = null;
					  	ResultIterator ri2 = rmconn.executeQuery(sql);
					  	int rolesCount = ri2.size();
					  	int roleNum = 0;
					  	String selectedCode = "";
						ResultRecord rr2 = null;
						while (ri2.hasNext()) {
							rr2 = (ResultRecord)ri2.next();
							roleCode = rr2.getString(1);
							selectedCode += roleCode;
							if(++roleNum < rolesCount){
								selectedCode += ",";
							}
							description = rr2.getString(2);
							%>
							<tr>
							<td align="center"><%=description %></td>
		            		<td align="right"><a href="javascript:;" onclick="deleteRole('<%=roleCode %>','<%=description %>','<%=code %>','<%=module_code %>','<%=privs %>')">删除</a></td>
		            		</tr>
							<%
						}
					 %>
					 <input type="hidden" name="role<%=id%>" id="role<%=id%>" value="<%=selectedCode%>">
            	</table>
            </td>
          </tr>         
		</table>
 		<%
 	}
%>
</div>
</br>
</body>
<script>
var myPriv = null;
var module_code = null;
var privs = null;
var userNameId = "";
var userRealNameId = "";

function deleteRole(roleCode,description,code,module_code,privs){
	jConfirm('您要删除角色"'+description+'"吗？','提示',function(r){
		if(!r){return;}
		else{
			$.ajax({
			url: "priv_center_do.jsp?op=delete",
			type: "post",
			data: {
				"flag":"2",
				"roleCode": roleCode,
				"myPriv": code,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				jAlert('删除角色"'+description+'"成功！','提示');
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert('删除角色"'+description+'"失败！','提示');
			}
			});
		}
	})
}

function deleteUser(user,realUser,code,module_code,privs){
	jConfirm('您要删除用户"'+realUser+'"吗？','提示',function(r){
		if(!r){return;}
		else{
			$.ajax({
			url: "priv_center_do.jsp?op=delete",
			type: "post",
			data: {
				"flag":"1",
				"user": user,
				"myPriv": code,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				jAlert('删除用户"'+realUser+'"成功！','提示');
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert('删除用户"'+realUser+'"失败！','提示');
			}
			});
		}
	})
}

function clearAllRoles(code,module_code,privs){
	jConfirm('您要清空所有角色吗？','提示',function(r){
		if(!r){return;}
		else{
			$.ajax({
			url: "priv_center_do.jsp?op=clear",
			type: "post",
			data: {
				"flag":"2",
				"myPriv": code,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				jAlert("清空角色成功！","提示");
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert("清空角色失败！","提示");
			}
			});	
		}
	})
}

function clearAllUsers(code,module_code,privs){
	jConfirm('您要清空所有用户吗？','提示',function(r){
		if(!r){return;}
		else{
			$.ajax({
			url: "priv_center_do.jsp?op=clear",
			type: "post",
			data: {
				"flag":"1",
				"myPriv": code,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				jAlert("清空用户成功！","提示");
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert("清空用户失败！","提示");
			}
			});	
		}
	})
}

function setRoles(str,strText){
	if(str != null && str !=""){
		$.ajax({
			url: "priv_center_do.jsp?op=add",
			type: "post",
			data: {
				"flag":"2",
				"roleCodes": str,
				"roleDescriptions": strText,
				"myPriv": myPriv,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert("赋予权限失败！","提示");
			}
		});	
	}
	
}

function setUsers(users, userRealNames) {
	if(users != null && users !=""){
		$.ajax({
			url: "priv_center_do.jsp?op=add",
			type: "post",
			data: {
				"flag":"1",
				"users": users,
				"userRealNames": userRealNames,
				"myPriv": myPriv,
				"module_code": module_code,
				"privs": privs
			},
			success: function(data, status){
				window.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				jAlert("赋予权限失败！","提示");
			}
		});	
	}
}

function openRoleWin(id,width,height,code,module,privList){
	window.open("../role_multi_sel.jsp?roleCodes="+$("#"+id).val(),"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
	myPriv = code;
	module_code = module;
	privs = privList;
}

function openUserWin(nameId,realNameId,url,width,height,code,module,privList){
	window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
	myPriv = code;
	module_code = module;
	privs = privList;
	userNameId = nameId;
	userRealNameId = realNameId;
}

function getSelUserNames(){
	return $("#"+userNameId).val();
}

function getSelUserRealNames(){
	return $("#"+userRealNameId).val();
}

</script>
</html>
