<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.video.*"%>
<%@ page import="org.json.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
Config cfg = new Config();
String isUse = cfg.get("isUse");
String siteName = cfg.get("siteName");
String sequence = cfg.get("sequence");
String op = ParamUtil.get(request,"op");
if ("edit".equals(op)){
	isUse = ParamUtil.get(request,"isUse");
	siteName = ParamUtil.get(request,"siteName");
	sequence = ParamUtil.get(request,"sequence");
	cfg.put("isUse",isUse);
	cfg.put("siteName",siteName);
	cfg.put("sequence",sequence);
	cfg.writemodify();
	JSONObject json = new JSONObject();	
	json.put("msg","操作成功！");
	out.print(json.toString());
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>视频会议配置</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
<script src="../js/jquery-ui/jquery-ui.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<%@ include file="config_m_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<form id="form1" name="form1" action="config_video.jsp?op=edit" method="post" >
<table width="" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98" style="width:800px;">
	<thead>
    <tr>
      <td class="tabStyle_1_title"  colSpan="2">视频会议配置</td>
    </tr>
    </thead>
    <tbody >
 	<tr class="highlight">
 		<td>
 		   视频会议启用
 		 </td><td>
 		    <select id="isUse" name="isUse" >
				<option value="true">是</option>
				<option value="false">否</option>
			</select>
 		</td>
 	</tr>
 	<tr class="highlight">
 		<td>
 		   站点名称
 		 </td>
 		 <td>
 		   <input id="siteName" name="siteName" type="text" size="40" value="<%=siteName %>"/>
 		</td>
 	</tr>
 	<tr class="highlight">
 		<td>
 		   序列
 		 </td>
 		 <td>
 		   <input id="sequence" name="sequence" type="text" size="40" value="<%=sequence %>"/>
 		</td>
 	</tr>
 	<tr class="highlight" >
 		<td style="text-align:center;" colspan="2">
 		 <input type="button" value="确定" style="margin-right:50px;" onclick="commit()"/>
 		  <input type="button" value="取消" style="margin-left:50px;" onclick="cancel()"/>
 		</td>
 	</tr>
	</tbody>
</table>
</form>
<br />
</body>    
<script type="text/javascript">
	$("#isUse").val("<%=isUse%>");
	function commit(){
		var isUse = $("#isUse").val();
		var siteName = $("#siteName").val();
		var sequence = $("#sequence").val();
		$.ajax({
						type: "post",
						url: "config_video.jsp",
						data: {
							op: "edit",
				  		 isUse:isUse,
				  		 siteName:siteName,
				  		 sequence:sequence
						},
						dataType: "json",
						beforeSend: function(XMLHttpRequest){
						},
						success: function(data, status){
							jAlert(data.msg, "提示")
						},
						complete: function(XMLHttpRequest, status){
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});	
	}
	function cancel(){
		window.location.reload();
	}
</script>                                    
</html>                            
  