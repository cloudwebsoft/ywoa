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
String tabIdOpener = ParamUtil.get(request, "tabIdOpener"); // 表示从哪个tab进入本页面，用于刷新角色菜单页面user_role_menu.jsp
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>智能模块设计 - 管理权限</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<script src="../js/layui/layui.js" charset="utf-8"></script>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if ("".equals(orderBy)) {
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
			
	window.location.href = "module_priv_list.jsp?code=<%=code%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
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
		if (rolesVector!=null) {
			rolesIterator = rolesVector.iterator();
		}
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
		if (setRoleCode!=null) {
			mpd.setRoles(formCode, setRoleCode.toString());
		}
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_priv_list.jsp?code=" + code + "&formCode=" + formCode + "&tabIdOpener=" + tabIdOpener));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		e.printStackTrace();
	}
	return;
}

boolean isNav = "".equals(tabIdOpener);
if (isNav) {
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<%
}

Vector result = mpd.list(code, orderBy, sort);
Iterator ir = result.iterator();
%>
<br/>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="left"><input class="btn" name="button" style="margin: 10px 0" type="button" onclick="window.location.href='module_priv_add.jsp?code=<%=code%>&formCode=<%=formCode%>&tabIdOpener=<%=tabIdOpener%>';" value="添加" width=80 height=20 />
	</td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="8%" style="cursor:pointer" onclick="doSort('name')">用户
        <%if ("name".equals(orderBy)) {
			if ("asc".equals(sort)) {
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			} else {
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
			}
		}%>	  
	  </td>
      <td class="tabStyle_1_title" width="4%" style="cursor:hand" align="center" onclick="doSort('priv_type')">类型
        <%if ("priv_type".equals(orderBy)) {
			if ("asc".equals(sort)) {
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			} else {
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
			}
		}%>	  
      </td>
      <td class="tabStyle_1_title" width="45%">权限</td>
      <td width="5%" class="tabStyle_1_title">可写<br/>字段</td>
		<td width="5%" class="tabStyle_1_title">隐藏<br/>字段</td>
		<td width="5%" class="tabStyle_1_title">可导<br/>字段</td>
      <td width="5%" class="tabStyle_1_title">过滤</td>
      <td width="8%" class="tabStyle_1_title">过滤条件</td>
      <td class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	mpd = (ModulePrivDb)ir.next();
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" method="post">
    <tr class="highlight" id="tr<%=mpd.getId()%>">
		<td>
			<%
				if (mpd.getType() == ModulePrivDb.TYPE_USER) {
					UserDb ud = new UserDb();
					ud = ud.getUserDb(mpd.getName());
					out.print(ud.getRealName());
				} else if (mpd.getType() == ModulePrivDb.TYPE_ROLE) {
					RoleDb rd = new RoleDb();
					rd = rd.getRoleDb(mpd.getName());
					out.print(rd.getDesc());
				} else if (mpd.getType() == ModulePrivDb.TYPE_USERGROUP) {
					UserGroupDb ug = new UserGroupDb();
					ug = ug.getUserGroupDb(mpd.getName());
					out.print(ug.getDesc());
				}
			%>
		</td>
      <td>
		  <%=mpd.getTypeDesc()%>
		  <input type="hidden" name="id" value="<%=mpd.getId()%>"/>
		  <input type="hidden" name="tabIdOpener" value="<%=tabIdOpener%>"/>
		  <input type="hidden" name="code" value="<%=code%>"/>
		  <input type="hidden" name="formCode" value="<%=formCode%>"/>
	  </td>
      <td>
	  <input name="see" type="checkbox" <%=mpd.getSee()==1?"checked":""%> value="1" title="浏览列表"/>
	  浏览&nbsp;
	  <input name="view" type="checkbox" <%=mpd.getView()==1?"checked":""%> value="1" title="查看详情及流程"/>
	  查看&nbsp;
	  <input name="append" type="checkbox" <%=mpd.getAppend()==1?"checked":""%> value="1" title="添加记录"/>
	  添加&nbsp;
	  <input name="modify" type="checkbox" <%=mpd.getModify()==1?"checked":""%> value="1" title="修改记录"/>
	  修改&nbsp;      
	  <input name="del" type="checkbox" <%=mpd.getDel()==1?"checked":""%> value="1" title="删除记录"/>
	  删除&nbsp;      
	  <input name="exportWord" type="checkbox" <%=mpd.getExportWord()==1?"checked":""%> value="1" title="生成Word文件"/>
	  生成&nbsp;
	  <input name="import" type="checkbox" <%=mpd.getImportXls()==1?"checked":""%> value="1" title="导入Excel文件"/>
	  导入&nbsp;
	  <input name="export" type="checkbox" <%=mpd.getExportXls()==1?"checked":""%> value="1" title="导出Excel文件"/>
	  按列导出&nbsp;
	  <input name="setList" type="checkbox" <%=mpd.getSetList()==1?"checked":""%> value="1" title="设置列表中的字段及排序"/>
	  列表&nbsp;
	  <br/>
	  <span style="display:none">
	  <input name="search" type="hidden" type=checkbox <%=mpd.getSearch()==1?"checked":""%> value="1" title="高级查询"/> 
	  高级查询 &nbsp;   
      </span>
	  <input name="reActive" type="checkbox" <%=mpd.getReActive()==1?"checked":""%> value="1" title="变更记录"/>
	  变更&nbsp;
	  <input name="log" type="checkbox" <%=mpd.getLog()==1?"checked":""%> value="1" title="管理日志"/>
      日志&nbsp;
	  <input name="zip" type="checkbox" <%=mpd.getZip()==1?"checked":""%> value="1" title="压缩下载文件"/>
	  压缩&nbsp;
	  <input name="copy" type="checkbox" <%=mpd.getCopy()==1?"checked":""%> value="1" title="复制记录"/>
	  复制&nbsp;
	  <input name="rollBack" type="checkbox" <%=mpd.getRollBack()==1?"checked":""%> value="1" title="回滚流程及记录"/>
	  回滚&nbsp;
	  <input name="manage" type="checkbox" <%=mpd.getManage()==1?"checked":""%> value="1" onclick="setManage(this, <%=mpd.getId()%>)" title="可添加、修改、删除记录、导入、导出、管理日志"/>
	  管理&nbsp;
	  <input name="data" type=checkbox <%=mpd.getData()==1?"checked":""%> value="1" title="数据维护，不受校验规则限制"/>
	  数据&nbsp;
	  <input name="exportXlsCol" type=checkbox <%=mpd.getExportXlsCol()==1?"checked":""%> value="1" title="选择列导出"/>
		选列导出&nbsp;
      <script>
		  function setManage(obj, id) {
			  if (obj.checked) {
				  var $tr = $('#tr' + id);
				  $tr.find("input[name=see]").prop('checked', true);
				  $tr.find("input[name=view]").prop('checked', true);
				  $tr.find("input[name=append]").prop('checked', true);
				  $tr.find("input[name=modify]").prop('checked', true);
				  $tr.find("input[name=del]").prop('checked', true);
				  $tr.find("input[name=import]").prop('checked', true);
				  $tr.find("input[name=export]").prop('checked', true);
				  $tr.find("input[name=search]").prop('checked', true);
				  $tr.find("input[name=reActive]").prop('checked', true);
				  $tr.find("input[name=log]").prop('checked', true);
				  $tr.find("input[name=exportWord]").prop('checked', true);
				  $tr.find("input[name=zip]").prop('checked', true);
				  $tr.find("input[name=copy]").prop('checked', true);
				  $tr.find("input[name=rollBack]").prop('checked', true);
				  $tr.find("input[name=exportXlsCol]").prop('checked', true);
				  $tr.find("input[name=setList]").prop('checked', true);
			  }
		  }
	  </script>
      </td>
      <td align="center">
		  <%
			  String clrWrite = "";
			  if (!StrUtil.isEmpty(mpd.getFieldWrite())) {
				  clrWrite = "#ff005c";
			  }
			  String clrHide = "";
			  if (!StrUtil.isEmpty(mpd.getFieldHide())) {
				  clrHide = "#ff005c";
			  }
			  String clrExport = "";
			  if (!StrUtil.isEmpty(mpd.getFieldExport())) {
				  clrExport = "#ff005c";
			  }
		  %>
		<a href="javascript:OpenFormFieldSelWin('fieldWrite<%=mpd.getId()%>', '<%=mpd.getId()%>');" style="color:<%=clrWrite%>">选择</a>
        <textarea id="fieldWrite<%=mpd.getId()%>" name="fieldWrite<%=mpd.getId()%>" style="display:none"><%=mpd.getFieldWrite()%></textarea> 
      </td>
      <td align="center">
        <a href="javascript:OpenFormFieldSelWin('fieldHide<%=mpd.getId()%>', '<%=mpd.getId()%>');" style="color:<%=clrHide%>">选择</a>
        <textarea id="fieldHide<%=mpd.getId()%>" name="fieldHide<%=mpd.getId()%>" style="display:none"><%=mpd.getFieldHide()%></textarea> 
      </td>
		<td align="center">
			<a href="javascript:OpenFormFieldSelWin('fieldExport<%=mpd.getId()%>', '<%=mpd.getId()%>');" style="color:<%=clrExport%>">选择</a>
			<textarea id="fieldExport<%=mpd.getId()%>" name="fieldExport<%=mpd.getId()%>" style="display:none"><%=mpd.getFieldExport()%></textarea>
		</td>
      <td align="center">
      	<input name="isFilter" value="1" type="checkbox" title="启用过滤条件" <%=mpd.isFilter()?"checked":""%>/>
      </td>
      <td align="center">
				<img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>
				<a href="javascript:" onclick="openFilterDlg(<%=mpd.getId()%>)">配置</a>
		  <%
			  String gouDis = "none";
			  if (!"".equals(mpd.getFilterCond().trim())) {
				  gouDis = "";
			  }
		  %>
				<span gouId="<%=mpd.getId()%>" style="margin:5px;display:<%=gouDis%>">
					<img src="../admin/images/gou.png" style="margin-bottom:-5px;width:10px;height:10px;"/>
				</span>
      </td>
      <td align="center">
	  <input class="btn" type="button" value="修改" onclick="updatePriv('<%=i%>')" />
		&nbsp;&nbsp;<input class="btn" type="button" onclick="delPriv('<%=mpd.getId()%>')" value="删除"/></td>
    </tr>
  </form>
<%}%>
  </tbody>
</table>
<br />
<br>
</body>
<script language="javascript">
<!--
function showGouImg(id, isShow) {
	if (isShow) {
		$('span[gouId=' + id + ']').show();
	}
	else {
		$('span[gouId=' + id + ']').hide();
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
	else if (curFields.indexOf("fieldExport") == 0) {
		op = "setFieldExport";
	}
	else {
		op = "setFieldHide";
	}
	
	$.ajax({
		type: "post",
		url: op,
		data : {
			id: curId,
			fields: fieldsSeted
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest) {
			$('body').showLoading();
		},
		success: function(data, status) {
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
		},
		complete: function(XMLHttpRequest, status){
			$('body').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert("error:" + XMLHttpRequest.responseText);
		}
	});		
}
/*
$(function() {
	if (window.top.mainFrame) {
		window.top.mainFrame.reloadTabById("<%=tabIdOpener%>");
	}
	else {
		if (window.top.o("content-main")) {
			window.top.reloadTabFrame("<%=tabIdOpener%>");
		}
	}
});*/

function delPriv(id) {
	jConfirm('您确定要删除吗?', '提示', function (r) {
		if (!r) {
			return;
		} else {
			$.ajax({
				type: "post",
				url: "delPriv",
				data : {
					id: id,
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest) {
					$('body').showLoading();
				},
				success: function(data, status) {
					data = $.parseJSON(data);
					if (data.ret==1) {
						$('#tr' + id).remove();
					}

					layer.msg(data.msg, {
						offset: '6px'
					});
				},
				complete: function(XMLHttpRequest, status){
					$('body').hideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert("error:" + XMLHttpRequest.responseText);
				}
			});
		}
	});
}

$.fn.serializeJsonObject = function () {
	var json = {};
	var form = this.serializeArray();
	$.each(form, function () {
		if (json[this.name]) {
			if (!json[this.name].push) {
				json[this.name] = [json[this.name]];
			}
			json[this.name].push();
		} else {
			json[this.name] = this.value || '';
		}
	});
	return json;
};

function updatePriv(index) {
	var data = $('#form' + index).serializeJsonObject();
	$.ajax({
		type: "post",
		url: "updatePriv",
		data: data,
		dataType: "html",
		beforeSend: function(XMLHttpRequest) {
			$('body').showLoading();
		},
		success: function(data, status) {
			data = $.parseJSON(data);
			layer.msg(data.msg, {
				offset: '6px'
			});
		},
		complete: function(XMLHttpRequest, status){
			$('body').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert("error:" + XMLHttpRequest.responseText);
		}
	});
}

function openFilterDlg(id) {
	openWin("module_priv_filter.jsp?code=<%=code%>&id=" + id, 850, 400);
}
//-->
</script>
</html>