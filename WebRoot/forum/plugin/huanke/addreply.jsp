<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.huanke.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<script language="javascript">
function changeReplyType(){
	if(frmAnnounce.replyType.value == "<%=HuankeReplyDb.REPLY_TYPE_EXCHANGE%>"){
		exchangeTable.style.display = "";
	}else{
		exchangeTable.style.display = "none";
	}
}
</script>
<%
	String replyType = ParamUtil.get(request, "replytype");	
	String replyId = ParamUtil.get(request, "replyid");	
	String rootId = ParamUtil.get(request, "rootid");	
    String userName = privilege.getUser(request);		
	
	HuankeGoodsDb hgd = new HuankeGoodsDb();
	if(!replyId.equals("")){
    	hgd = hgd.getHuankeGoodsDb(Long.parseLong(replyId));
	}else{
		hgd = hgd.getHuankeGoodsDb(Long.parseLong(rootId));
	}
%>
<table width="98%">
  <tr>
    <td width="20%">选择回复类型：</td>
    <td width="80%"><select name="replyType" onchange="changeReplyType()">
<%
	if(!userName.equals(hgd.getUserName())){
		if(replyType.equals("")){
%>
      <option value="<%=HuankeReplyDb.REPLY_TYPE_EXCHANGE%>">我要交换</option>
      <option value="<%=HuankeReplyDb.REPLY_TYPE_COMMUNICATION%>" selected>交流</option>
      <%
		}else{	
			if(Integer.parseInt(replyType) == HuankeReplyDb.REPLY_TYPE_EXCHANGE){
%>
      <option value="<%=HuankeReplyDb.REPLY_TYPE_EXCHANGE%>">我要交换</option>
      <%
			}else{
%>
      <option value="<%=HuankeReplyDb.REPLY_TYPE_COMMUNICATION%>">交流</option>
      <%			
			}
		}
	}else{	  
%>
		<option value="<%=HuankeReplyDb.REPLY_TYPE_COMMUNICATION%>">交流</option>
<%
	}	  
%>
    </select>
      <input type="hidden" name="pluginCode" value="<%=HuankeUnit.code%>" /></td>
  </tr>
</table>
<table width="98%" id="exchangeTable">
	<!--<tr>
		<td width="20%" align="left" bgcolor="#F9FAF3">物品类别：</td>
		<td height=23 align="left" bgcolor="#F9FAF3">
		<select name="exchangeCatalogCode">
		<%
			/*
			com.redmoon.forum.plugin.huanke.Directory dir = new com.redmoon.forum.plugin.huanke.Directory();
			com.redmoon.forum.plugin.huanke.Leaf lf = dir.getLeaf("root");
			com.redmoon.forum.plugin.huanke.DirectoryView dv = new com.redmoon.forum.plugin.huanke.DirectoryView(lf);
			StringBuffer sb = new StringBuffer();
			dv.ShowDirectoryAsOptionsToString(sb, lf, lf.getLayer());
			out.print(sb);
			*/
		%>
		</select></td>
	</tr>
	-->	
	<tr>
		<td width="20%" align="left" bgcolor="#F9FAF3">物品名称：</td> 
	  <td width="80%" height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="exchangeGoods"/></td>
	</tr>
	<tr>
		<td width="20%" align="left" bgcolor="#F9FAF3">新旧程度：</td> 
	  <td width="80%" height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="exchangeDepreciation"/></td>
	</tr>
	<tr>
		<td width="20%" align="left" bgcolor="#F9FAF3">交换地点：</td> 
	  <td width="80%" height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="exchangeProvince"/></td>
	</tr>
	<tr>
		<td width="20%" align="left" bgcolor="#F9FAF3">联系方式：</td> 
	  <td width="80%" height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="contact"/></td>
	</tr>
</table>
<script language="javascript">
if(frmAnnounce.replyType.value == "<%=HuankeReplyDb.REPLY_TYPE_COMMUNICATION%>"){
	exchangeTable.style.display = "none";
}else{
	exchangeTable.style.display = "";
}
</script>

