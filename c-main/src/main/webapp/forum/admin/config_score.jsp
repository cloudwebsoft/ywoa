<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.forum.*,
				 org.jdom.*,
                 java.util.*"
%>
<title>Score Manage</title>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<LINK href="images/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
<body bgcolor="#FFFFFF">
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head> <lt:Label res="res.label.forum.admin.config_score" key="score_mgr"/></TD>
    </TR>
  </TBODY>
</TABLE>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
<tr>
<td width="1%" valign="top"></td>
<td width="99%" align="center" valign="top">
<%
int k = 0;
String code="", name = "", regist = "", login = "", add = "",reply = "", elite = "", del = "",className="", type="", exchange="", desc="", advertiseLink="", attachment="", attachmentDel="", ratio="", isDisplay="";
ScoreConfig sc = new ScoreConfig();
Element root = sc.getRootElement();
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	code = ParamUtil.get(request, "code");
	name = ParamUtil.get(request, "name");
	regist = ParamUtil.get(request, "regist");
	login = ParamUtil.get(request, "login");
	add = ParamUtil.get(request, "add");
	reply = ParamUtil.get(request, "reply");
	elite = ParamUtil.get(request, "elite");
	del = ParamUtil.get(request, "del");
	className = ParamUtil.get(request, "className");
	type = ParamUtil.get(request, "type");
	exchange = ParamUtil.get(request, "exchange");
	desc = ParamUtil.get(request, "desc");
	advertiseLink = ParamUtil.get(request, "advertiseLink");
	attachment = ParamUtil.get(request, "attachment_add");
	attachmentDel = ParamUtil.get(request, "attachment_del");
	ratio = ParamUtil.get(request, "ratio");
	isDisplay = ParamUtil.get(request, "isDisplay");
	
	sc.set(code, "name", name);	
	sc.set(code, "regist", regist);	
	sc.set(code, "type", type);
	sc.set(code, "login", login);	
	sc.set(code, "add", add);	
	sc.set(code, "exchange", exchange);
	sc.set(code, "reply", reply);	
	sc.set(code, "elite", elite);	
	sc.set(code, "del", del);	
	sc.set(code, "className", className);
	sc.set(code, "type", type);	
	sc.set(code, "desc", desc);	
	sc.set(code, "advertiseLink", advertiseLink);
	sc.set(code, "attachment_add", attachment);
	sc.set(code, "attachment_del", attachmentDel);
	sc.set(code, "ratio", ratio);
	sc.set(code, "isDisplay", isDisplay);
	sc.writemodify();
	ScoreMgr sm = new ScoreMgr();
	sm.reload();
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_score.jsp"));
}
List list = root.getChildren();
if (list != null) {
Iterator ir = list.iterator();
while (ir.hasNext()) {
      	 Element child = (Element) ir.next();
		 code = child.getAttributeValue("code");
	     name =  child.getChildText("name");
		 className = child.getChildText("className");
		 type = child.getChildText("type");
		 exchange = child.getChildText("exchange");
		 desc = child.getChildText("desc");
		 regist =  child.getChildText("regist");
	     login =  child.getChildText("login");
	     add =  child.getChildText("add");
		 reply =  child.getChildText("reply");
		 elite =  child.getChildText("elite");
	     del =  child.getChildText("del");
		 advertiseLink = child.getChildText("advertiseLink");
		 attachment = child.getChildText("attachment_add");
		 attachmentDel = child.getChildText("attachment_del");
		 ratio = child.getChildText("ratio");
		 isDisplay = child.getChildText("isDisplay");
%>
<table width="98%" border="0" align="center" cellpadding="3" cellspacing="1">
  <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='?op=modify'>
  <tr>
    <td colspan="4" class="thead"><%=name%><input type="hidden" name="code" value="<%=code%>"/></td>
    </tr>
  <tr >
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="name"/></td>
    <td bgcolor="#F6F6F6"><input type="input" name="name" value="<%=name%>"></td>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="className"/></td>
    <td bgcolor="#F6F6F6"><input type="input" name="className" value="<%=className%>"></td>
  </tr>
  <tr>
    <td width="15%" bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="desc"/></td>
    <td width="35%" bgcolor="#F6F6F6"><input type="input" name="desc" value="<%=desc%>"></td>
    <td width="15%" bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="exchange"/></td>
    <td width="35%" bgcolor="#F6F6F6"><select name="exchange">
      <option value="1" selected>
        <lt:Label res="res.label.forum.admin.config_score" key="yes"/>
        </option>
      <option value="0">
        <lt:Label res="res.label.forum.admin.config_score" key="no"/>
        </option>
    </select>
	<script language="javascript">
			<!--
				form<%=k%>.exchange.value = "<%=exchange%>"
			//-->
		</script>	</td>
  </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="ratio"/></td>
    <td bgcolor="#F6F6F6"><input name="ratio" type="input" value="<%=ratio%>"></td>
    <td bgcolor="#F6F6F6">前台显示</td>
    <td bgcolor="#F6F6F6"><select name="isDisplay">
      <option value="1" selected>
      <lt:Label res="res.label.forum.admin.config_score" key="yes"/>
      </option>
      <option value="0">
      <lt:Label res="res.label.forum.admin.config_score" key="no"/>
      </option>
    </select>
		<script language="javascript">
			<!--
				form<%=k%>.isDisplay.value = "<%=isDisplay%>"
			//-->
		</script>	</td>
  </tr>
  <!--
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="type"/></td>
    <td bgcolor="#F6F6F6"></td>
  </tr>
  -->
  <input type="hidden" name="type" value="<%=type%>">
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="regist"/></td>
    <td bgcolor="#F6F6F6"><input type="input" name="regist" value="<%=regist%>"></td>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="login"/></td>
    <td bgcolor="#F6F6F6"><input name="login" type="input" value="<%=login%>"></td>
  </tr>
  
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="add"/></td>
    <td bgcolor="#F6F6F6"><input name="add" type="input" value="<%=add%>"></td>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="elite"/></td>
    <td bgcolor="#F6F6F6"><input name="elite" type="input" value="<%=elite%>"></td>
  </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="reply"/></td>
    <td bgcolor="#F6F6F6"><input name="reply" type="input" value="<%=reply%>"></td>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="del"/></td>
    <td bgcolor="#F6F6F6"><input name="del" type="input" value="<%=del%>"></td>
  </tr>
  
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="attachment_add"/></td>
    <td bgcolor="#F6F6F6"><input name="attachment_add" type="input" value="<%=attachment%>"></td>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="advertiseLink"/></td>
    <td bgcolor="#F6F6F6"><input name="advertiseLink" type="input" value="<%=advertiseLink%>"></td>
  </tr>
  <tr>
    <td bgcolor="#F6F6F6"><lt:Label res="res.label.forum.admin.config_score" key="attachment_del"/></td>
    <td bgcolor="#F6F6F6"><input name="attachment_del" type="input" value="<%=attachmentDel%>"></td>
    <td bgcolor="#F6F6F6">&nbsp;</td>
    <td bgcolor="#F6F6F6">&nbsp;</td>
  </tr>
    
    <tr>
    <td></td>
    <td><div align="center">
      <INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>
    </div></td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    </tr>
</form>  
</table>
	<%	
	k++;	 
   }   
 } // end if
%>
  <hr size="0">
 </td>
</tr>
</table>	
</body>
</html>
