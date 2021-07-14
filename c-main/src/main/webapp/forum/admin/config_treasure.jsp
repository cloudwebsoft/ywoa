<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title></title>
<%@ include file="../inc/nocache.jsp" %>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<link rel="stylesheet" href="../../common.css">
<script language="JavaScript">
<!--
function validate()
{
	if  (document.addform.name.value=="")
	{
		alert('<lt:Label res="res.label.cms.config" key="type_cannot_null"/>');
		document.addform.name.focus();
		return false ;
	}
}

function checkdel(frm)
{
 if(!confirm('<lt:Label res="res.label.cms.config" key="is_confirm_del"/>'))
	 return;
 frm.op.value="del";
 frm.submit();
}
//-->
</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style><body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head>灌水宝贝</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<%
String desc = "", code = "", image = "";	  
String op = ParamUtil.get(request, "op");	
String name="",value = "";
int num = 0, day = 0, count;
TreasureMgr tm = new TreasureMgr();
code = ParamUtil.get(request, "code");
if (op.equals("edit")) {	
	try {
		Enumeration e = request.getParameterNames();
		while(e.hasMoreElements()) {
			String fieldName = (String)e.nextElement();
			if(!fieldName.equals("code") && !fieldName.equals("edit") && !fieldName.equals("op")) {
				String value1 = ParamUtil.get(request, fieldName);
				tm.setTreasure(code, fieldName, value1);
				tm.writemodify();
			}
		}
		out.print(StrUtil.Alert_Redirect("操作成功！", "config_treasure.jsp"));
	} catch (Exception e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		e.printStackTrace();
	}
}	
else {
	if (op.equals("add")) {
		try {
			String moneyCode = ParamUtil.get(request, "moneyCode");
			String price = ParamUtil.get(request, "price");	
			if (!StrUtil.isNumeric(price)) {
					out.print(StrUtil.Alert_Redirect("请输入数字！", "config_treasure.jsp"));
					return;
				}
			else {	
				tm.addPrice(code, moneyCode, price);
				tm.writemodify();
				out.print(StrUtil.Alert_Redirect("操作成功！", "config_treasure.jsp"));
			}	
		} catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Redirect("操作失败！" + e, "config_treasure.jsp"));;
		}	
	}				
}

int k = 0;
Iterator ir = tm.getAllTreasure().iterator();
int countnum = 0;
while (ir!=null && ir.hasNext()) {
	TreasureUnit tu = (TreasureUnit)ir.next();
	count = tu.getCount();
	code = tu.getCode();
	name = tu.getName();	
	image = tu.getImage();
	desc = tu.getDesc();
	day = tu.getDay();
%>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td height="23" colspan="4" class="thead">&nbsp;灌水宝贝</td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa"> 
    <td colspan="4" valign="top" bgcolor="#FFFFFF">
      <br>
      <table width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_treasure.jsp?op=edit&code=<%=code%>'>
          <tr>
            <td bgcolor=#F6F6F6>名称
          <td colspan="2" bgcolor=#F6F6F6><input type="text" value="<%=name%>" name="name" />          </tr>		
          <tr>
            <td bgcolor=#F6F6F6>描述
          <td colspan="2" bgcolor=#F6F6F6><input type="text" value="<%=desc%>" name="desc" />          </tr>
          <tr> 
            <td bgcolor=#F6F6F6 width='29%'>库存
            <td colspan="2" bgcolor=#F6F6F6>
            <input type="text" value="<%=count%>" name="count" />
		              </tr>
		  <tr>
            <td bgcolor=#F6F6F6>图片          
          <td colspan="2" bgcolor=#F6F6F6><input type="text" value="<%=image%>" name="image" size="35"/>            </tr>
          <tr>
            <td bgcolor=#F6F6F6>天数          
          <td colspan="2" bgcolor=#F6F6F6><input type="text" value="<%=day%>" name="day" />            </tr>
          <tr>
            <td bgcolor=#F6F6F6>可交易币种
            <td width="35%" bgcolor=#F6F6F6>  
<%
	Iterator pir = tu.getPrice().iterator();
	ScoreMgr sm = new ScoreMgr();
	while (pir.hasNext()) {
		TreasurePrice tp = (TreasurePrice)pir.next();
		ScoreUnit su = sm.getScoreUnit(tp.getScoreCode());
%>
		 <%=su.getName()%>&nbsp;&nbsp;
		 <input type="text" value="<%=tp.getValue()%>" name="<%=tp.getScoreCode()%>" /><br>
<%		
	}
	
%>                      
            <td width="36%" bgcolor=#F6F6F6>            
          </tr>
          <tr>
            <td bgcolor=#F6F6F6>          
            <td colspan="2" bgcolor=#F6F6F6>          
          <input type=submit value='<lt:Label res="res.label.cms.config" key="modify"/>'>            </tr>
        </FORM>
      </table>
<br></td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
<form name="formadd" action="?op=add&code=<%=code%>" method="post">  
    <td width="17%" valign="top" bgcolor="#FFFFFF" class="thead">增加可交易币种 </td>
    <td width="13%" valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;
<%
Vector v = sm.getAllScore();
Iterator iterator = v.iterator();
%>	
	<select name="moneyCode">
<%	  
while (iterator.hasNext()) {
      ScoreUnit su = (ScoreUnit) iterator.next();
      if (su.isExchange()) {
%>
				<option value="<%=su.getCode()%>"><%=su.getName(request)%></option>
<%}
}%>				
	</select>	</td>
    <td width="8%" valign="top" bgcolor="#FFFFFF" class="thead">价格</td>
    <td width="62%" valign="top" bgcolor="#FFFFFF" class="thead"><input name="price" maxlength="20" > &nbsp;&nbsp;&nbsp;<input type="submit" value="增加" /></td>
</form>  
  </tr>
</table> 
<p><br>
  <%
  k++;
}
%>
</p>
<p>
</p>
</body>                                        
</html>                            
  