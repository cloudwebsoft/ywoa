<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
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
    
    <title>短信模板添加</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
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
public String convertToHTMLCtl(HttpServletRequest request, String code,String id) {
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
    <form name="form1" action="sms_template_add_do.jsp?op=add" method="post" >
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">短信模版</td>
      </tr>
  </tbody>
</table>
<TABLE width="71%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60">
<thead>
<TR>
  <TD colspan="2" align=center>模板</TD>
  </TR>
</thead>
<TBODY>
<TR>
<TD width="19%" align=right>短信类型：</TD>
<TD width="81%" align=left>&nbsp;<%out.print(convertToHTMLCtl(request,"sms_type"));%></TD>
</TR>
<tr>
<td align=right>短信内容：</td>
<td align=left ><textarea id="content" name="content" rows="5" cols="60"></textarea>
<script>
var content = new LiveValidation('content');
content.add(Validate.Presence, { failureMessage:'请填写内容'} );
</script>
</td>
</tr>
<tr>
  <td colspan="2" align=center><input type="submit" value="添加" class="btn" /></td>
  </tr>

</TBODY>
</TABLE>&nbsp;
</form>
  </body>
</html>
