<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.util.ExchangeRateDb"%>
<%@page import="cn.js.fan.db.Paginator"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@page import="com.redmoon.oa.basic.SelectMgr"%>
<%@page import="com.redmoon.oa.basic.SelectDb"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.basic.SelectOptionDb"%>
<%@page import="com.redmoon.oa.util.ExchangeRateMgr"%>
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.basic.TreeSelectView"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>汇率信息添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>

<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<script src="../inc/livevalidation_standalone.js"></script>
<%!
	public String convertToHTMLCtl(HttpServletRequest request, String code) {
        StringBuffer str = new StringBuffer();
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect(code);
        str.append("<select name='" + sd.getCode() + "' id = '"+sd.getCode()+"'>");
        if (sd.getType()==SelectDb.TYPE_LIST) {
            Vector v = sd.getOptions(new JdbcTemplate());
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) ir.next();
                String selected = "";
                if (sod.isDefault())
                    selected = "selected";
                str.append("<option value='" + sod.getValue() + "' " + selected +
                        ">" +
                        sod.getName() +
                        "</option>");
            }
        }
        else {
            TreeSelectDb tsd = new TreeSelectDb();
            tsd = tsd.getTreeSelectDb(sd.getCode());
            TreeSelectView tsv = new TreeSelectView(tsd);
            StringBuffer sb = new StringBuffer();
            try {
                str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
            }
            catch (ErrMsgException e) {
                e.printStackTrace();
            }
        }
        str.append("</select>");
        return str.toString();
    }
%>
<%! 
public String convertToHTMLCtl(HttpServletRequest request, String code, String id) {
        StringBuffer str = new StringBuffer();
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect(code);
		if(id.equals("")){
        	str.append("<select name='" + sd.getCode()+"_"+id + "' id = '"+sd.getCode()+"_"+id+"'>");
        }else{
        	str.append("<select name='" + sd.getCode()+"_"+id + "' id = '"+sd.getCode()+"_"+id+"'>");
        }
        if (sd.getType()==SelectDb.TYPE_LIST) {
            Vector v = sd.getOptions(new JdbcTemplate());
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) ir.next();
                String selected = "";
                if (sod.isDefault())
                    selected = "selected";
                str.append("<option value='" + sod.getValue() + "' " + selected +
                        ">" +
                        sod.getName() +
                        "</option>");
            }
        }
        else {
            TreeSelectDb tsd = new TreeSelectDb();
            tsd = tsd.getTreeSelectDb(sd.getCode());
            TreeSelectView tsv = new TreeSelectView(tsd);
            StringBuffer sb = new StringBuffer();
            try {
                str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
            }
            catch (ErrMsgException e) {
                e.printStackTrace();
            }
        }
        str.append("</select>");
        return str.toString();
    }
%>
<script>		
	var curObjId;
	function selectNode(code, name) {
		$(curObjId).value = code;
		$(curObjId + "Desc").value = name;
	}
</script>	
</head>
<body>
<form name="form1" action="exchange_rate_do.jsp?op=add" method="post" >
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      	<td class="tdStyle_1">汇率信息</td>
   	  </tr>
  </tbody>
</table>
<TABLE class=tabStyle_1 cellSpacing=0 cellPadding=0 width="98%">
<TBODY>
		<TR>
			<TD class=tabStyle_1_title colSpan=4>汇率信息添加</TD>
		</TR>
	</TBODY>
		<TR>
			<TD align=right>币&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;种：</TD>
			<TD align=left width="15%">&nbsp;<%out.print(convertToHTMLCtl(request,"bz"));%>&nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
			<TD align=right>汇&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;率：</TD>
			<TD align=left>&nbsp;<INPUT title=汇率 name=rate id="rate" >
				<script>
					var rate = new LiveValidation('rate');
					rate.add(Validate.Presence);		
				</script>
			</TD>
		</TR>
</TABLE>
<table width="98%" align="center">
<tr><td width="100%" align="center">
<input type="submit" value="确定" class="btn" /></td></tr>
</table>
</form>
</body>
</html>
