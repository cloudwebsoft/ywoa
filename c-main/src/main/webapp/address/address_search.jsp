<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>通讯录查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" style="margin-bottom: 10px">
  <tr>
    <td width="100%" height="23" valign="middle" class="tdStyle_1"> 通讯录 </td>
  </tr>
</table>
<%
String action = "address.jsp";
String flag = ParamUtil.get(request, "flag");
if (flag.equals("sel")) {
	action = "address_list_sel.jsp";
}
%>
<table class="tabStyle_1 percent80">
	 <form action="<%=action%>" name="form1" method="get">
    <tr>
      <td colspan="2" nowrap class="tabStyle_1_title">查询条件</td>
    </tr>
    <tr>
    <td nowrap>分组
	<input name="op" value="search" type="hidden" />
	：</td>
    <td nowrap>
      &nbsp;    
	 <%
	  	int type = ParamUtil.getInt(request, "type");

        String who = privilege.getUser(request);
        if (type==AddressDb.TYPE_PUBLIC)
          who = Leaf.USER_NAME_PUBLIC;
      %>
         <select name="typeId" id="typeId" >
           <option value="">-----请选择-----</option>
           <%
           Leaf lf = new Leaf();
           lf = lf.getLeaf(who);
           DirectoryView dv = new DirectoryView(lf);
           int rootlayer = 1;
           dv.ShowDirectoryAsOptionsWithCode(out, lf, rootlayer);
           %>
       </select>      
<input name="type" value="<%=type%>" type="hidden">	  </td>
   </tr>
    <tr>
      <td nowrap>姓名：</td>
      <td>&nbsp;
      <input type="text" name="person" size="25"></td>
    </tr>
    <tr>
      <td nowrap> 昵称：</td>
      <td>
      &nbsp;
      <input type="text" name="nickname" size="25" ><input name=mode value="show" type=hidden>      </td>
    </tr>
    <tr>
      <td nowrap>手机：</td>
      <td>&nbsp;
      <input type="text" name="mobile" size="25" /></td>
    </tr>
    <tr>
      <td nowrap>QQ：</td>
      <td>&nbsp;
      <input type="text" name="QQ" size="25" ></td>
    </tr>
    <tr>
      <td nowrap> 单位名称：</td>
      <td>
      &nbsp;
      <input type="text" name="company" size="25" >      </td>
    </tr>
    <tr>
      <td nowrap>地址：</td>
      <td>
      &nbsp;
      <input type="text" name="address" size="25" class="BigInput">      </td>
    </tr>
    <tr>
      <td nowrap>住宅所在地：</td>
      <td>
      &nbsp;
      <input type="text" name="street" size="25">      </td>
    </tr>
    <tr>
      <td colspan="2" align="center" nowrap>
        <input class="btn" type="submit" value="查 询" name="button"></td>
    </tr>
    </form>
</table>
</body>
</html>