<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectView"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "officeequip";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String strTypeId = ParamUtil.get(request, "typeId");
String strEquipId =ParamUtil.get(request, "equipId");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "typeId", strTypeId, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "equipId", strEquipId, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用品领用查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<script>
o("menu8").className="current";
</script>
<div class="spacerH"></div>
<form action="officeequip_return_list.jsp?op=search" name="form1" method="post">
<table class="tabStyle_1 percent80">
<tr>
  <td colspan="2" class="tabStyle_1_title">办公用品借用查询</td>
</tr>
   
	<tr>
      <td>用品名称：</td>
      <td><%
	  int total =0 ;

		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
        <select name="equipId" id="equipId">
        <%=sb%>
      </select></td>
	</tr>
	<tr>
      <td>借用人：</td>
      <td><input name="person" type="text" id="person" size="20"></td>
    </tr>	
    <tr align="left">
      <td><script>
	  </script>
	     开始时间：</td>
      <td><input id="beginDate" name="beginDate" type="text"size="20">
      <script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
        </script>
        </td>
    </tr>
	 <tr>
	  <td>	  
       结束时间：</td>
      <td><input id="endDate" name="endDate" type="text" id="endDate" size="20">
        <script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
        </script>
        </td>
	</td>
    </tr>
	<tr>
	  <td>借用状态：</td>
      <td><select name="opType" id="opType">
        <option value="" selected>不限</option>
        <option value="<%=OfficeOpDb.TYPE_BORROW%>">借用</option>
        <option value="<%=OfficeOpDb.TYPE_RETURN%>">已还</option>
      </select></td>
  </tr>
 <tr> 
  <td colspan="2" align="center">
    <input name="submit" type="submit" class="btn"  value="确定" ></td>
</tr>
</table>
</form>
</body>
</html>
