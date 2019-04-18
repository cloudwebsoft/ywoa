<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.point_sel" key="sel_point"/> - <%=Global.AppName%></title>
<script src="../inc/common.js"></script>
<script>
function selMoneyCode() {
   	var ary = new Array();
	ary[0] = getRadioValue("moneyCode");
	ary[1] = sum.value;
	if (ary[0]==null) {
		alert('<lt:Label res="res.label.forum.point_sel" key="sel_one_point"/>');
		return;
	}
	else {
		if (!isNumeric(sum.value)) {
			alert('<lt:Label res="res.label.forum.point_sel" key="err_num"/>');
			return;
		}
	}

	window.returnValue = ary;
	window.close();
}
</script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<TABLE class="tableCommon" width="100%" border=0 align=center cellPadding=2 cellSpacing=1>
<thead>
  <TR> 
	<TD height=23 colspan="2" align="center"><lt:Label res="res.label.forum.point_sel" key="select"/></TD>
  </TR>
</thead>
<TBODY>
<%	  
        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        String str = "";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            if (su.isExchange()) {
%>
      <TR> 
        <TD width="2%" height=23 align="center"><input name="moneyCode" type="radio" value="<%=su.getCode()%>"></TD>
        <TD width="98%" align="left"><%=su.getName(request)%></TD>
      </TR>
<%	  
          }
      }
%>      
<TR>
  <TD height=23 colspan="2" align="center"><lt:Label res="res.label.forum.point_sel" key="sum"/>
    <input name="sum" size=6 value=""></TD>
</TR>
<TR>
        <TD height=23 colspan="2" align="center">
		<input type="button" value="<lt:Label key="ok"/>" onClick="selMoneyCode()">
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input name="button" type="button" onClick="window.close()" value="<lt:Label key="cancel"/>"></TD>
      </TR>    </TBODY>
  </TABLE>
</body>
</html>
