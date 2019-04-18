<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="flowConfig" scope="page" class="com.redmoon.oa.flow.FlowConfig"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>流程按钮配置</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<body>
<%@ include file="config_m_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
	<thead>
    <tr>
      <td class="tabStyle_1_title" noWrap width="20%" >按钮</td>
      <td class="tabStyle_1_title" noWrap width="10%" >启用</td>
      <td class="tabStyle_1_title" noWrap width="30%" >名称</td>
      <td class="tabStyle_1_title" noWrap width="30%" >提示信息</td>
      <td class="tabStyle_1_title" noWrap width="10%" >操作</td>
    </tr>
    </thead>
    <tbody >
 <%
	Element root = flowConfig.getRootElement();
	String name="";
	String isDisplay = "";
	String value = "";
	String title = "";
	
	name = ParamUtil.get(request, "name");
	value = ParamUtil.get(request, "value");
	title = ParamUtil.get(request, "title");
	
	if (name!=null && !name.equals(""))
	{
		if(value == null || value.equals("")){
			out.print(StrUtil.jAlert_Back("名称不能为空！","提示"));
			return;
		}
		if(!flowConfig.checkChar(value)){
			out.print(StrUtil.jAlert_Back("名称中不能含有”字符！","提示"));
			return;
		}
		if(!flowConfig.checkChar(title)){
			out.print(StrUtil.jAlert_Back("提示信息中不能含有“字符！","提示"));
			return;
		}
		flowConfig.modify(name,request);
		out.println(fchar.jAlert_Redirect("更改成功！","提示", "config_flow_button.jsp"));
		return;
	}
	
	int k = 0;
	Iterator ir = root.getChildren().iterator();
	while (ir.hasNext()) {
	  Element e = (Element)ir.next();
	  name = e.getName();  
	  if (!name.equals("FLOW_BUTTON_ATTENTION") && !name.equals("FLOW_BUTTON_NETDISKFILE") && !name.equals("FLOW_BUTTON_TRANSFER") && !name.equals("FLOW_BUTTON_PLUS") && !name.equals("FLOW_BUTTON_DOC") && !name.equals("FLOW_BUTTON_SUSPEND") && !name.equals("FLOW_BUTTON_DISCARD") && !name.equals("FLOW_BUTTON_LINKPROJECT")){
	  	continue;
	  }
	  value = StrUtil.getNullStr(e.getValue());
	  title = StrUtil.getNullStr(e.getAttributeValue("title"));
	  isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
%>
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_flow_button.jsp'>
          <tr class="highlight" align="center">
            <td><input type="hidden" name="name" value="<%=name%>"><%=value%></td>
            <td> 
				<select name="isDisplay" width=10>
				<option value="true">是</option>
				<option value="false">否</option>
				</select>
				<script>
					form<%=k%>.isDisplay.value = "<%=isDisplay%>";
				</script>
			</td>
			<td><input type="text" name="value" value="<%=value%>"></td>
			<td><input type="text" name="title" value="<%=title%>"></td>
			<td><input class="btn" TYPE=submit name='edit' value='修改'></td>
          </tr>
        </FORM>
<%
  k++;
}
%>
</tbody>
</table>
<br />
</body>                                        
</html>                            
  