<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>分配管理员</title>
<link rel="stylesheet" href="../../common.css">
<script language="JavaScript">
<!--
function validate()
{
	if  (document.addform.name.value=="")
	{
		alert("新加管理员名称空");
		document.addform.name.focus();
		return false ;
	}
}

function checkdel(frm)
{
 if(!confirm("你是否确认删除该管理员？"))
	 return;
 frm.op.value="del";
 frm.submit();
}
//-->
</script>
<link href="../common.css" rel="stylesheet" type="text/css">
<LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理员</td>
  </tr>
</table>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<br>
<TABLE class="frame_gray"  
cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF" class="thead">分配管理员 </TD>
    </TR>
    <TR>
      <TD height=200 valign="top" bgcolor="#FFFBFF"><table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
        <tr>
          <td height=20 align="left">&nbsp;</td>
        </tr>
        <tr>
          <td valign="top"><table width="100%" border='0' cellspacing='0' cellpadding='0'>
              <tr >
                <td width="100%" class="stable">
<%
String op = ParamUtil.get(request, "op");
String name,desc;
int sort;
MasterDb md = new MasterDb();

if (!op.equals(""))
{
	name = ParamUtil.get(request, "name");
	desc = ParamUtil.get(request, "desc");
	sort = ParamUtil.getInt(request, "sort");
	
	if (op.equals("add"))
	{
		md.setName(name);
		md.setDesc(desc);
		md.setSort(sort);
		if (!md.create())
			out.println(StrUtil.Alert("创建管理员未成功，请检查用户是否存在，或是否有重复！"));	
	}
	if (op.equals("edit"))
	{
		md = md.getMasterDb(name);
		md.setDesc(desc);
		md.setSort(sort);
		if (!md.save())
			out.println(StrUtil.Alert("修改管理员信息未成功！"));	
	}
	if (op.equals("del"))
	{
		md = md.getMasterDb(name);
		if (!md.del())
			out.println(StrUtil.Alert("删除管理员未成功！"));	
	}
}
Vector v = md.list();
Iterator ir = v.iterator();
int i = 0;
%>
                    <table width="98%" align="center">
	<% while (ir.hasNext()){
			md = (MasterDb) ir.next();
			name = md.getName();
			sort = md.getSort();
			desc = md.getDesc();
			i++;
		%>
                      <FORM METHOD=POST id="form<%=i%>" name="form<%=i%>" ACTION='master_m.jsp'>
                        <tr>
                          <td width='60%'>                            呢称
                              <INPUT TYPE=text value="<%=name%>" name="name" style='border:1pt solid #636563;font-size:9pt' size=10>
                    描述
                    <INPUT TYPE=text value="<%=desc%>" name="desc" style='border:1pt solid #636563;font-size:9pt' size=20>
                    <input type=hidden name=op value="edit"></td>
                          <td width='22%'>序号：
                              <input name="sort" type=text class="singleboarder" value="<%=sort%>" size=3></td>
                          <td width="8%" align=left><INPUT TYPE=submit name='edit' value='修改' style='border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;'>
                          </td>
                          <td width="10%" align=center><INPUT TYPE=button value='删除' name='del' style='border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;' onclick='checkdel(form<%=i%>)'>
                          </td>
                        </tr>
                      </FORM>
                      <%
		}%>
                    </table>
                    <tr>
                      <FORM METHOD=POST ACTION="master_m.jsp" name="addform">
                        <input type=hidden name=op value="add">
                        <td height="23" colspan=3 align="center" class="stable"><table width="98%">
                          <tr>
                            <td width="60%">呢称
                              <input type=text value="" name="name" style='border:1pt solid #636563;font-size:9pt' size=10>
描述
<input type=text value="" name="desc" style='border:1pt solid #636563;font-size:9pt' size=20></td>
                            <td width="22%">&nbsp;序号：
                              <input name="sort" type="text" class="singleboarder" size=3>                              </td>
                            <td width="8%"><input type="submit" name="add" value="增加" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" onClick="return  validate()"></td>
                            <td width="10%">&nbsp;</td>
                          </tr>
                        </table>
                        </td>
                      </FORM>
                    </tr>
          </TABLE></td>
        </tr>
      </table></TD>
    </TR>
  </TBODY>
</TABLE>
<br>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>                                        
</html>                            
  