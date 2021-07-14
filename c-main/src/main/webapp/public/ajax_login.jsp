<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<table width="88%" align="center" class="tableCommon80">
    <thead>
    </thead>
    <tr>
      <td width="23%" height="30" align="left"><lt:Label res="res.label.door" key="user_name"/></td>
            <td width="77%" height="22"><input id="name" name="name" style="width:120"></td>
  </tr>
    <tr>
      <td height="30" align="left"><lt:Label res="res.label.door" key="pwd"/></td>
            <td height="22"><input id="pwd" name="pwd" type=password style="width:120">
	</td>
  </tr>
</table>
