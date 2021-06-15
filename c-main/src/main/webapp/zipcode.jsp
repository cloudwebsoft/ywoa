<%@ page contentType="text/html;charset=gb2312"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>邮编区号</title>
<link href="common.css" rel="stylesheet" type="text/css">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="inc/nocache.jsp"%>
</head>
<body leftmargin="0" topmargin="3">
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" >
  <tr>
    <td>
        <table width="100%" border="0" cellpadding="0" cellspacing="0" align="center" class="percent98" >
        <form name="form1" method="post" action="zipcode.jsp"> 
          <tr> 
            <td height="26" class="stable">关键字：
<input name="keyword" type="text" size=12 id="keyword"> 
              选项： 
              <select name="searchmode" id="searchmode">
                <option value="1" selected>查找城市邮编</option>
                <option value="2">邮编查找城市</option>
                <option value="1">查找城市区号</option>
                <option value="3">区号查找城市</option>
              </select> <input class="btn" type="submit" name="Submit" value="查 找"></td>
          </tr></form>
      </table>
    </td>
  </tr>
  <tr>
    <td><table width="100%" border="1" cellspacing="0" cellpadding="0" align="center" class="tabStyle_1 percent98">
      <tr>
        <td width="22%" class="tabStyle_1_title">
          <div align="center" ><font color="#000000">省洲名称</font></div>
        </td>
        <td width="30%" class="tabStyle_1_title">
          <div align="center"><font color="#000000">地区名称</font></div>
        </td>
        <td width="26%" class="tabStyle_1_title">
          <div align="center"><font color="#000000">邮政编码</font></div>
        </td>
        <td width="22%" class="tabStyle_1_title">
          <div align="center"><font color="#000000">电话区号</font></div>
        </td>
      </tr>
<%
String keyword = StrUtil.Unicode2GB(request.getParameter("keyword"));
try {
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "keyword", keyword, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

if (keyword==null || keyword.trim().equals(""))
	;
else {
	Conn conn = new Conn(Global.getDefaultDB());
	try {
	 	  int searchmode = Integer.parseInt(request.getParameter("searchmode"));
		  
		  String province = "";
		  String city = "";
		  String zip = "";
		  String yb = "";
		  String sql = "";
		  switch (searchmode)
		  {
		  	case 1:sql = "select province,city,postcode,qh from postcode where city="+StrUtil.sqlstr(keyword);
					break;
			case 2:sql = "select province,city,postcode,qh from postcode where postcode="+StrUtil.sqlstr(keyword);
					break;
			case 3:sql = "select province,city,postcode,qh from postcode where qh="+StrUtil.sqlstr(keyword);
					break;
			case 4:sql = "select province,city,postcode,qh from postcode where province="+StrUtil.sqlstr(keyword);
					break;
		  }
		  ResultSet rs = conn.executeQuery(sql);
		  if (rs!=null)
		  {
		  	while (rs.next())
			{ 
				province = rs.getString(1);
				city = rs.getString(2);
				zip = rs.getString(3);
				yb = rs.getString(4);
%>
      <tr>
        <td width='22%' align="center"><%=province%></td>
        <td width='30%' align="center"><%=city%></td>
        <td width='26%' align="center"><%=zip%></td>
        <td width='22%' align="center" ><%=yb%></td>
      </tr>
<% 			}
			rs.close();
			rs = null;
		  }
		}
		catch (Exception e) {
		  	out.print(e.getMessage());
		}
		finally {
			if (conn!=null) {
				conn.close();
				conn = null;
			}		  
		}
}

%>
    </table></td>
  </tr>
  <tr>
    <td align="center"><img src="images/chinamap.gif" width="469" height="367" border="0" usemap="#Map"></td>
  </tr>
</table>

<map name="Map">
<area shape="poly" coords="68,80" href="#">
<area shape="rect" coords="74,117,114,133" href="?searchmode=4&keyword=%D0%C2%BD%AE">
<area shape="rect" coords="76,217,117,232" href="?searchmode=4&keyword=%CE%F7%B2%D8">
<area shape="rect" coords="153,181,192,197" href="?searchmode=4&keyword=%C7%E0%BA%A3">
<area shape="rect" coords="224,135,288,152" href="?searchmode=4&keyword=%C4%DA%C3%C9%B9%C5">
<area shape="rect" coords="390,88,420,102" href="?searchmode=4&keyword=%BC%AA%C1%D6">
<area shape="rect" coords="368,112,396,128" href="?searchmode=4&keyword=%C1%C9%C4%FE">
<area shape="rect" coords="334,127,363,141" href="?searchmode=4&keyword=%B1%B1%BE%A9">
<area shape="rect" coords="318,131,332,160" href="?searchmode=4&keyword=%BA%D3%B1%B1">
<area shape="rect" coords="334,143,364,155" href="?searchmode=4&keyword=%CC%EC%BD%F2">
<area shape="rect" coords="293,154,309,182" href="?searchmode=4&keyword=%C9%BD%CE%F7">
<area shape="rect" coords="332,166,363,183" href="?searchmode=4&keyword=%C9%BD%B6%AB">
<area shape="rect" coords="245,159,264,188" href="?searchmode=4&keyword=%C4%FE%CF%C4">
<area shape="rect" coords="227,176,244,207" href="?searchmode=4&keyword=%B8%CA%CB%E0">
<area shape="rect" coords="205,227,243,245" href="?searchmode=4&keyword=%CB%C4%B4%A8">
<area shape="rect" coords="218,245,248,259" href="?searchmode=4&keyword=%D6%D8%C7%EC">
<area shape="rect" coords="270,192,286,222" href="?searchmode=4&keyword=%C9%C2%CE%F7">
<area shape="rect" coords="301,194,331,214" href="?searchmode=4&keyword=%BA%D3%C4%CF">
<area shape="poly" coords="339,210,349,203,364,236,351,237" href="?searchmode=4&keyword=%B0%B2%BB%D5">
<area shape="poly" coords="354,196,364,188,387,217,375,227" href="?searchmode=4&keyword=%BD%AD%CB%D5">
<area shape="rect" coords="292,225,323,242" href="?searchmode=4&keyword=%BA%FE%B1%B1">
<area shape="rect" coords="197,294,235,311" href="?searchmode=4&keyword=%D4%C6%C4%CF">
<area shape="rect" coords="250,271,279,286" href="?searchmode=4&keyword=%B9%F3%D6%DD">
<area shape="rect" coords="296,252,315,283" href="?searchmode=4&keyword=%BA%FE%C4%CF">
<area shape="rect" coords="331,250,347,280" href="?searchmode=4&keyword=%BD%AD%CE%F7">
<area shape="rect" coords="357,262,372,292" href="?searchmode=4&keyword=%B8%A3%BD%A8">
<area shape="rect" coords="266,300,298,318" href="?searchmode=4&keyword=%B9%E3%CE%F7">
<area shape="rect" coords="311,298,343,315" href="?searchmode=4&keyword=%B9%E3%B6%AB">
<area shape="rect" coords="306,347,337,363" href="?searchmode=4&keyword=%BA%A3%C4%CF">
<area shape="rect" coords="403,280,418,313" href="?searchmode=4&keyword=%CC%A8%CD%E5">
<area shape="rect" coords="370,233,396,249" href="?searchmode=4&keyword=%D5%E3%BD%AD">
<area shape="rect" coords="392,214,419,228" href="?searchmode=4&keyword=%C9%CF%BA%A3">
<area shape="rect" coords="384,47,424,62" href="?searchmode=4&keyword=%BA%DA%C1%FA%BD%AD">
</map>
</body>
</html>
