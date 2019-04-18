<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加角色</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
  .unit {
	  background-color:#CCC;
  }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function form1_onsubmit() {
	if (getRadioValue("isDefault")=="0") {
		if ($('#diskQuota').val()=="" || $('#diskQuota').val()<0 || !isNumeric($('#diskQuota').val())) {
			jAlert("云盘配额请输入大于0的数字！","提示");
			$('#diskQuota').focus();
			return false;
		}
	}
	if (getRadioValue("isMsgDefault")=="0") {
		if ($('#msgSpaceQuota').val()=="" || $('#msgSpaceQuota').val()<0 || !isNumeric($('#msgSpaceQuota').val())) {
			jAlert("内部邮箱配额请输入大于0的数字！","提示");
			$('#msgSpaceQuota').focus();
			return false;
		}
	}
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String op = ParamUtil.get(request, "op");
RoleDb rd = new RoleDb();
boolean isEdit = false;

if (op.equals("edit")) {
	isEdit = true;
	String code = ParamUtil.get(request, "code");
	if (code.equals("")) {
		out.print(StrUtil.jAlert_Back("编码不能为空！","提示"));
		return;
	}
	rd = rd.getRoleDb(code);
	String sql = "select u.name from users u, user_of_role r where r.roleCode=" + StrUtil.sqlstr(code) + " and r.userName=u.name";
	JdbcTemplate jt = new JdbcTemplate();
	try {
		ResultIterator ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			String userName = rr.getString(1);
			UserDb ud = new UserDb(userName);
			ud.setDuty(rd.getOrders() + "");
			ud.save();
		}
	} catch (Exception e) {
	} finally {
		jt.close();
	}
}
else if (op.equals("editdo")) {
	isEdit = true;
	RoleMgr roleMgr = new RoleMgr();
	try {
		if (roleMgr.update(request)) {
			String code = ParamUtil.get(request, "code");
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "user_role_op.jsp?op=edit&code=" + StrUtil.UrlEncode(code)));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}	
	return;
}
%>
<%if (op.equals("edit")) {%>
<%@ include file="user_role_op_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<%} else {%>
<%@ include file="user_role_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%} %>
<div class="spacerH"></div>
<form name="form1" method="post" onsubmit="return form1_onsubmit()" action="<%=isEdit?"user_role_op.jsp?op=editdo":"user_role_m.jsp?op=add"%>">
<table class="tabStyle_1 percent80" width="65%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td height="31" colspan="2" align="center" class="tabStyle_1_title">
        <%if (isEdit) {%>
        修改角色
        <%}else{%>
        添加角色
        <%}%>
      </td>
    </tr>
    <tr style="display:none">
      <td width="127" height="31" align="center">编码</td>
      <td align="left"><input name="code" value="<%=isEdit?rd.getCode():RandomSecquenceCreator.getId(20)%>" <%=isEdit?"readonly":""%> onfocus="this.select()"></td>
    </tr>
    <tr>
      <td height="32" align="center">名称</td>
      <td align="left"><input name="desc" value="<%=isEdit?rd.getDesc():""%>"></td>
    </tr>
    <%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
	String strDis = "";
	if (!isNetdiskUsed) {
		strDis = "display:none";
	}
	%>
    <tr style="<%=strDis%>">
      <td height="32" align="center">云盘配额</td>
      <td align="left">
      <span id="spanRadio">
      <input type="radio" name="isDefault" value="1" checked="checked" />
      不指定
      <input type="radio" name="isDefault" value="0" />
      指定
      </span>
      <span id="spanQuota" style="display:none">
      <input id="diskQuota" name="diskQuota" value="<%=isEdit?""+rd.getDiskQuota():"-1"%>" />
      &nbsp;字节
      </span>
      <script>
	  $(function() {
		  <%
		  if (isEdit) {
			int v = -1;
			if (rd.getDiskQuota()==-1) {
				v = 1;
			}
			else {
				v = 0;
				%>
				$('#spanQuota').show();
				<%
			}
		  	%>
			setRadioValue("isDefault", <%=v%>);
			<%if (rd.getCode().equals(RoleDb.CODE_MEMBER)) {%>
				$('#spanRadio').hide();
			<%}%>
			<%
		  }
		  %>
		  
		  $('input[name=isDefault]').click(function() {
			  if(this.value=="0") {
				  $('#spanQuota').show();
				  $('#diskQuota').val("");
			  }
			  else {
				  $('#diskQuota').val("-1");
				  $('#spanQuota').hide();
			  }
		  });
		  
	  });
	  </script>
      </td>
    </tr>
    <tr>
      <td height="32" align="center">邮箱配额</td>
      <td align="left">
      <span id="spanMsgRadio">
      <input type="radio" name="isMsgDefault" value="1" checked="checked" />
      不指定
      <input type="radio" name="isMsgDefault" value="0" />
      指定
      </span>
      <span id="spanMsgQuota" style="display:none">
      <input id="msgSpaceQuota" name="msgSpaceQuota" value="<%=isEdit?""+rd.getMsgSpaceQuota():"-1"%>" />
      &nbsp;字节
      </span>
      <script>
	  $(function() {
		  <%
		  if (isEdit) {
			int v = -1;
			if (rd.getMsgSpaceQuota()==-1) {
				v = 1;
			}
			else {
				v = 0;
				%>
				$('#spanMsgQuota').show();
				<%
			}
		  	%>
			setRadioValue("isMsgDefault", <%=v%>);
			<%if (rd.getCode().equals(RoleDb.CODE_MEMBER)) {%>
				$('#spanMsgRadio').hide();
			<%}%>			
			<%
		  }
		  %>
		  
		  $('input[name=isMsgDefault]').click(function() {
			  if(this.value=="0") {
				  $('#spanMsgQuota').show();
				  $('#msgSpaceQuota').val("");
			  }
			  else {
				  $('#msgSpaceQuota').val("-1");
				  $('#spanMsgQuota').hide();
			  }
		  });
	  });
	  </script>      
      </td>
    </tr>
    <%
	String dis = "";
	if (op.equals("edit")) {
		if (rd.getCode().equals(RoleDb.CODE_MEMBER)) {
			dis = "display:none";
		}
	}
	%>    
    <tr style="<%=dis%>">
      <td height="32" align="center">序号</td>
      <td align="left"><input name="orders" value="<%=isEdit?""+rd.getOrders():"0"%>">
      &nbsp;（序号越大，表示角色级别越高，用于流程中比较角色大小）
      </td>
    </tr>
    <tr style="display:none">
      <td height="32" align="center">类型</td>
      <td align="left">
      <select id="type" name="type" title="特定角色在流程中不能被跳过">
        <option value="<%=RoleDb.TYPE_NORMAL%>">普通</option> 
        <option value="<%=RoleDb.TYPE_SPECIAL%>">特定</option> 
      </select>
	  <%if (op.equals("edit")) {%>
      <script>
      form1.type.value = "<%=rd.getType()%>";
      </script>
      <%}%>      
      </td>
    </tr>
    <tr style="<%=dis%>">
      <td height="32" align="center">管理本部门</td>
      <td align="left">
      <select id="isDeptManager" name="isDeptManager">
      <option value="1">是</option>
      <option value="0">否</option>
      </select>
       <%if (op.equals("edit")) {%>
      <script>
      form1.isDeptManager.value = "<%=rd. isDeptManager()?"1" : "0"%>";
      </script>
      <%}%>  
      （能够查看“部门工作”）</td>
    </tr>
    <%if (privilege.isUserPrivValid(request, "admin")) {%>     
    <tr style="<%=dis%>">
      <td height="32" align="center">系统</td>
      <td align="left">
      <input type="checkbox" id="isSystem" name="isSystem" value="1" />
      (系统角色对于集团中的子单位管理员可见)
      <%if (isEdit && rd.isSystem()) {%>
      <script>
	  o("isSystem").checked = true;
	  </script>
      <%}%>
      </td>
    </tr>
    <%}%>
    <tr style="<%=dis%>">
      <td height="32" align="center">单位</td>
      <td align="left">
          <%if (License.getInstance().isGroup() || License.getInstance().isPlatform()) {%>
              <select id="unitCode" name="unitCode" <%=isEdit?"disabled":""%>>
              <%
			  DeptDb dd = new DeptDb();
			  DeptView dv = new DeptView(request, dd);
			  StringBuffer sb = new StringBuffer();
			  dd = dd.getDeptDb(privilege.getUserUnitCode(request));
			  %>
			  <%=dv.getUnitAsOptions(sb, dd, dd.getLayer())%>
              </select>          
			  <%if (op.equals("edit")) {%>
              <script>
              form1.unitCode.value = "<%=rd.getUnitCode()%>";
              </script>
              <input name="unitCode" value="<%=rd.getUnitCode()%>" type="hidden" />
              <%}%>
		  <%}else{
		  	DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(DeptDb.ROOTCODE);
		  	%>
			<%=dd.getName()%>
            <input type="hidden" name="unitCode" value="<%=DeptDb.ROOTCODE%>" />
			<%
		  }%>
	  </td>
    </tr>
    <tr>
      <td height="43" colspan="2" align="center"><input class="btn" name="Submit" type="submit" value="确定">
        &nbsp;&nbsp;&nbsp;
        <input name="Submit" type="button" onclick="window.location.href='user_role_m.jsp'" class="btn" value="返回">
        </td>
    </tr>
</table>
</form>
</body>
</html>