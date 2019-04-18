<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.pvg.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.Config"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.person.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.setup_user_level" key="setup_user_level"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
}
if (op.equals("modify")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success"))); // "操作成功！"));
}
if (op.equals("del")) {
	boolean re = false;
	UserLevelMgr ulm = new UserLevelMgr();
	try {
		re = ulm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success"))); // "操作成功！"));
}
Config cfg = Config.getInstance();
Element root = cfg.getRootElement();
Element e = root.getChild("forum").getChild("userLevel");
String level = e.getText();
if (level.equals("levelCredit")) {
   level = "信用值";
}
if (level.equals("levelExperience")) {
   level = "经验值";
}
if (level.equals("levelGold")) {
   level = "金币";
}
if (level.equals("levelTopticCount")) {
   level = "最少发贴";
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.setup_user_level" key="setup_user_level"/></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.setup_user_level" key="level"/></td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="91%"  border="0" align="center" cellpadding="3" cellspacing="0" bgcolor="#FFFFFF" class="tableframe_gray" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
      <tr align="center" bgcolor="#F8F7F9">
        <td width="16%" height="24" align="left" bgcolor="#ECE9D8"><strong><%=level%></strong></td>
        <td width="32%" height="24" align="left" bgcolor="#ECE9D8"><strong>
          <span class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15"></span>
          <lt:Label res="res.label.forum.admin.setup_user_level" key="desc"/>
        </strong></td>
        <td width="17%" align="left" bgcolor="#ECE9D8"><strong><span class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15"></span>图片</strong></td>
        <td width="16%" align="left" bgcolor="#ECE9D8"><strong><span class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15"></span>所属用户组</strong></td>
        <td width="19%" align="left" bgcolor="#ECE9D8"><strong>
        <span class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15"></span>
        <lt:Label key="op"/>
      </strong></td>
      </tr>
	<%
	UserLevelDb uld = new UserLevelDb();
	Vector v = uld.getAllLevel();
	Iterator ir = v.iterator();
	int i = 0;
	while (ir.hasNext()) {
		i ++;
		uld = (UserLevelDb)ir.next();
	%>
      <tr align="center">
	  <form name="form<%=i%>" action="?op=modify" method=post>
        <td height="24"><input name=newLevel value="<%=uld.getLevel()%>"><input type=hidden name=level value="<%=uld.getLevel()%>"></td>
        <td height="24"><input name=desc value="<%=uld.getDesc()%>"></td>
        <td>&nbsp;<input name=levelPicPath value="<%=uld.getLevelPicPath()%>"></td>
        <td>
			<select name="groupCode">
                <%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator ir2 = result.iterator();
			String opts = "";
			while (ir2.hasNext()) {
				ugroup = (UserGroupDb) ir2.next();
				opts += "<option value='" + ugroup.getCode() + "'>" + ugroup.getDesc() + "</option>";
			}
			%>
				<option value=""><lt:Label res="res.label.forum.admin.user_m" key="none"/></option>
                <%=opts%>
              </select>
			  <script>
			  form<%=i%>.groupCode.value = "<%=uld.getGroupCode()%>";
			  </script>		</td>
        <td height="24" align="left">
		<input type="submit" value="<lt:Label key="ok"/>">
		&nbsp;
		<input name="submit2" type="button" value="<lt:Label key="op_del"/>" onClick="window.location.href='?op=del&level=<%=uld.getLevel()%>'">	  
		<%if (!uld.getGroupCode().equals("")) {%>
		&nbsp;<input type="button" value="权限" onClick="window.open('user_group_priv_frame.jsp?groupCode=<%=StrUtil.UrlEncode(uld.getGroupCode())%>')">
		<%}%>
		</td>
	  </form>
      </tr>
	<%}%>
      <tr align="center">
	  <form action="?op=add" method=post>
        <td height="24"><input name="level" type="text" id="level"></td>
        <td height="24"><input name="desc" type="text" id="desc"></td>
        <td><input name="levelPicPath" type="text" id="levelPicPath"></td>
        <td><select name="groupCode">
          <%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator ir2 = result.iterator();
			String opts = "";
			while (ir2.hasNext()) {
				ugroup = (UserGroupDb) ir2.next();
				opts += "<option value='" + ugroup.getCode() + "'>" + ugroup.getDesc() + "</option>";
			}
			%>
          <option value="">
            <lt:Label res="res.label.forum.admin.user_m" key="none"/>
            </option>
          <%=opts%>
        </select></td>
        <td height="24" align="left"><input name="submit" type="submit" value="<lt:Label key="op_add"/>"></td>
		</form>
      </tr>
    </table>
      <br>
      <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe_gray">
        <tr>
          <td align="center">&nbsp;注意：图片请以level + &quot;1-9&quot;+&quot;.gif&quot; 为等级名图片 ，如：level1.gif。&nbsp;&nbsp;</td>
        </tr>
      </table>
      <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
&nbsp;
</body>                                        
</html>                            
  