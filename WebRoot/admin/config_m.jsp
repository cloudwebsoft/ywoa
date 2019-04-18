<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.util.TwoDimensionCode"%>
<%@page import="com.redmoon.oa.kernel.License"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>配置管理</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="JavaScript">
<!--
function validate() {
	if  (document.addform.name.value=="")
	{
		jAlert("新加类别不能为空","提示");
		document.addform.name.focus();
		return false ;
	}
}

function checkdel(frm){
	jConfirm("你是否确认删除该类别？","提示",function(r){
		if(!r){return;}
		else{
			frm.op.value="del";
 			frm.submit();
		}
	})
}
//-->
</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="myconfig" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("setDefaultSkin")) {
	com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
	String defaultSkinCode = ParamUtil.get(request, "defaultSkinCode");
	sm.setDefaultSkin(defaultSkinCode);
	out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "config_m.jsp"));
	return;
}
else if (op.equals("setSms")) {
	com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
	boolean isUsed = ParamUtil.get(request, "isUsed").equals("true");
	cfg.setIsUsed(isUsed);
	com.redmoon.oa.sms.SMSFactory.init();
	com.redmoon.oa.sms.SMSFactory.getMsgUtil();
	out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "config_m.jsp"));
	return;
}
%>
<%@ include file="config_m_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<table id="mainTable" width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
  <tr> 
    <td class="tabStyle_1_title" colspan="3">配置管理</td>
  </tr>
        <FORM METHOD=POST id="form_sms" name="form_sms" ACTION='config_m.jsp?op=setSms'>
          <tr class="highlight">
            <td>&nbsp;短信启用
            <td>
            <select name="isUsed">
			<option value="true">是</option>
			<option value="false">否</option>
			</select>
            </td>      
            <td align="center"><input class="btn" type=submit value='<lt:Label key="op_modify"/>' /></td>        
          </tr>
		  </form>
		  <script>
		  form_sms.isUsed.value = "<%=com.redmoon.oa.sms.SMSFactory.isUseSMS()?"true":"false"%>";
		  </script>
        <FORM METHOD=POST id="form_skin" name="form_skin" ACTION='config_m.jsp?op=setDefaultSkin'>
          <tr class="highlight">
            <td>
            &nbsp;经典版默认皮肤
            </td>
            <td><%
			  String opt = "";
			  com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
			  Iterator irskin = sm.getAllSkin().iterator();
			  while (irskin.hasNext()) {
			  	com.redmoon.oa.ui.Skin sk = (com.redmoon.oa.ui.Skin)irskin.next();
				String d = "";
				if (sk.isDefaultSkin())
					d = "selected";
				opt += "<option value=" + sk.getCode() + " " + d + ">" + sk.getName() + "</option>";
			  }
			  %>
                <select name="defaultSkinCode">
                  <%=opt%>
                </select></td>           
            <td align="center"><input class="btn" type=submit value='<lt:Label key="op_modify"/>' />      
            </td>      
          </tr>
        </FORM>                  
      <%
Element root = myconfig.getRootElement();

String name="",value = "";

name = request.getParameter("name");
if (name!=null && !name.equals(""))
{
	value = ParamUtil.get(request, "value");

	myconfig.put(name,value);
	if(name.equals("enterprise")){
		TwoDimensionCode.generate2DCodeByMobileClient();
		XMLConfig cfg = new XMLConfig("config_cws.xml", false, "utf-8");
		cfg.set("Application.name", value);
		cfg.writemodify();
		Global.init();
	}
	
	out.println(fchar.jAlert_Redirect("更改成功！","提示", "config_m.jsp?flag="+flag));
	return;
}

int k = 0;
Iterator ir = root.getChild("oa").getChildren().iterator();
String desc = "";
License lic = License.getInstance();
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
  if (isDisplay.equals("false")) {
  	continue;
  }  
  desc = e.getAttributeValue("desc");
  if (lic.isOem() && desc.indexOf("一米") > -1) {
	  desc = desc.replaceAll("一米", "");
  }
  String type = StrUtil.getNullStr(e.getAttributeValue("type"));
  name = e.getName();
  value = e.getValue(); 
  
  // System.out.println(name);
%>
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_m.jsp?flag=<%=flag %>'>
          <tr class="highlight">
            <td width='37%'> <INPUT TYPE=hidden name=name value="<%=name%>"> 
              &nbsp;<%=myconfig.getDescription(name)%>
            <td width='55%'> 
			<%
			if (name.equals("flowExpireUnit")) {%>
				<select name="value" width=10>
				<option value="day">天</option>
				<option value="hour">小时</option>
				</select>
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>				
			<%}
			else if (value.equals("true") || value.equals("false")) {%>
				<select name="value" width=10>
				<option value="true">是</option>
				<option value="false">否</option>
				</select>
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
			<%}else{
				if (type.equals("textarea")) {
					%>
					<textarea name="value" cols="50" rows="5"><%=value%></textarea>
					<%
				}
				else {
					if("morningbegin".equals(name) && "introduction".equals(flag)){
						%>
							<input id ="morningbegin" type=text value="<%=value%>" name="value" size=30>
							<script>
				        		jQuery(document).ready(function(){
							    	var tour = {
											id : "hopscotch",
											steps : [ {
												title : "提示",
												content : "点击右侧“修改”按钮，可以修改上下班时间，用于初始化工作日历。点击“下一步”可以进入“工作日历”！",
												target : "morningbegin",
												placement : "right",
												width : "290px",
												showNextButton : false,
												showCTAButton : true,
												ctaLabel : "下一步",
												onCTA : function(){
													addTab('工作日历', 'admin/oa_calendar.jsp?type=1&flag=introduction');
												}
											}]
										};
									hopscotch.startTour(tour);
								});
							</script>
		                <%
					}else{
						String opts = StrUtil.getNullStr(e.getAttributeValue("options"));
						if ("".equals(opts)) {
						%>
							<input type=text value="<%=value%>" name="value" size=30>
		                <%
		                }
		                else {
		                %>
							<select id="att<%=k%>" name="value">
								<%
								String[] ary = StrUtil.split(opts, ",");
									for (String item : ary) {
										String[] aryOpts = StrUtil.split(item, "\\|");
										if (aryOpts!=null && aryOpts.length==2) {
											%>
											<option value="<%=aryOpts[0]%>"><%=aryOpts[1]%></option>
											<%
										}
									}
								%>
							</select>
							<script>
								$(function() {
									$('#att<%=k%>').val('<%=value%>');
								})
							</script>
						<%
		                }
					}
				}%>
            <%}%>
			<td width="8%" align=center> <INPUT class="btn" TYPE=submit name='edit' value='修改'>            </td>
          </tr>
        </FORM>
<%
  k++;
}
%>
</table>
<br />
</body>            
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>                            
</html>                            
  