<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.stamp.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>图片印章</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="renderer" content="ie-stand">
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
UserDb user = new UserDb();
user = user.getUserDb(privilege.getUser(request));
%>
<script>
   function form1_onsubmit() {
    if ($("#stampId").val()==-1) {
		alert("请选择印章！");
		return false;
	}
	if ($("#pwd").val()==null||$("#pwd").val()=="") {
		alert("请填写密码！");
		return false;
	}
    $.ajax({
    	type : "post",
    	url:"<%=request.getContextPath()%>/flow/flow_ntko_stamp_choose_do.jsp",
    	data : { "op" :"getstamp",  "stampId": $("#stampId").val(), "pwd" : $("#pwd").val()},
        success : function (data,status) {
            data = $.parseJSON(data);
            if(data.ret==0){
				if (window.opener.AddPictureFromURL) {
					window.opener.AddPictureFromURL(data.link);
				}
				else {
					window.opener.insertSignImg($('#stampId').val(), data.link);
				}
            	window.close();
            } else if(data.ret==-1) {
            	alert("印章不存在");
            } else if(data.ret==-2) {
           		 alert("您没有使用印章的权限");
            } else {
            	alert("密码错误");
            }
        }
    });
  }
</script>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
  <tbody>
  <tr><td colspan="2" align="left" class="tabStyle_1_title">选择印章</td></tr>
    <tr >
      <td height="26" align="right">&nbsp;请选择印章:</td>
      <td align="left" >
	  <%
	    StampMgr lm = new StampMgr();
		StampDb ld = new StampDb();
		String kind = StampDb.KIND_DEFAULT;
		String sql = ld.getListSql(kind);
		
		StampPriv sp = new StampPriv();
		Vector v = sp.getStampsOfUser(privilege.getUser(request));
		
		Iterator ir = v.iterator();		
	  %>
	  <select id="stampId" name="stampId" style="width:100px;">
	  <option value="-1">无</option>
	  <%
	  	Map map = new HashMap();
	    while (ir.hasNext()) {
 	    ld = (StampDb)ir.next();
 	    if (ld != null && ld.isLoaded()) {
 	    if (map.containsKey(String.valueOf(ld.getId()))) {
 	    	continue;
 	    }
 	    map.put(String.valueOf(ld.getId()), "");
	  %>
	  <option value="<%=ld.getId()%>"><%=ld.getTitle()%></option>
	  <%
	    }}
	  %>
	  </select>
      </td>
    </tr>
    <tr>
      	<td height="26" align="right">&nbsp;请输入密码:</td>
 		<td align="left"   >
	  	<input name="pwd" type="password" id="pwd" autocomplete="off"/>
	  	</td></tr><tr height="30" colspan="2" align="center"><td></td><td height="30" colspan="2" align="left"><input class="btn" type="button" value="确定" onclick="form1_onsubmit()"></td></tr>
	  	</tbody></table>

</form>
</body>
</html>