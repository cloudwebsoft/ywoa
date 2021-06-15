<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网络硬盘发布</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="clouddisk.css" />
<script language=JavaScript src='showDialog/jquery.min.js'></script>
<script src="../js/jquery-alerts/jquery.alerts.js"	type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"  type="text/css" media="screen" />
<%
if (!privilege.isUserLogin(request)) {
	// out.print("对不起，请先登录！");
	// return;
}
int attId = ParamUtil.getInt(request, "attachId");
Attachment att = new Attachment();
att = att.getAttachment(attId);

LeafPriv lp = new LeafPriv(att.getDirCode());
if (privilege.isUserPrivValid(request, "admin")) {
}
else {
	if (!lp.canUserModify(privilege.getUser(request))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}	
}
Document doc = new Document();
doc = doc.getDocument(att.getDocId());
Leaf lf = new Leaf();
lf = lf.getLeaf(doc.getDirCode());

String privurl = ParamUtil.get(request, "privurl");

String op = ParamUtil.get(request, "op");
if (op.equals("share")) {
	String publicShareDir = ParamUtil.get(request, "dirCode");
	att.setPublicShareDir(publicShareDir);	
	boolean re = att.save();
	if (re) {
		String dirCode = ParamUtil.get(request, "dirCode");
		PublicAttachmentMgr pam = new PublicAttachmentMgr();
		boolean isLink = Boolean.parseBoolean(ParamUtil.get(request, "isLink"));
		try {
			if (pam.share(request, dirCode, attId, isLink)) {
				out.print(StrUtil.jAlert_Redirect("操作成功！","提示","netdisk_public_share.jsp?attachId=" + attId));
			}
			else {
				out.print(StrUtil.jAlert_Back("操作失败！","提示"));
			}
		}
		catch(ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("del")) {
	int publicAttId = ParamUtil.getInt(request, "publicAttId");
	PublicAttachment patt = new PublicAttachment();
	patt = patt.getPublicAttachment(publicAttId);
	if (patt.del()) {
		out.print(StrUtil.jAlert_Redirect("取消发布成功！","提示","netdisk_public_share.jsp?attachId=" + attId));
	}
	else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
%>
<script>
 function cancelShare(attId,amId){
	 jConfirm("您确定要取消发布么?","提示",function(r){
			if(!r){return;}
			else{
				window.location.href='netdisk_public_share.jsp?op=del&attachId='+attId+'&publicAttId='+amId;
			}
	 })

}
var oldDirCode = "";
//下拉框选择
function selDirCode(isLink){
	var code = '';
	if(isLink == 1){
		code = $(".linkResourcesSel option:selected").val();
	}else{
		code = $(".copyResourcesSel option:selected").val();
	}
	if(code == 'root'){
		jAlert("请选择相应的目录","提示");
		return false;
	}
	oldDirCode = code;
}
//表单提交
function form_onsubmit(isLink) {
	var res = true;
	if(isLink == 1){
		if($(".linkResourcesSel option:selected").val() == 'root'){
			res = false;
		}
	}else{
		if($(".copyResourcesSel option:selected").val() == 'root'){
			res = false;
		}
	}
	if(!res){
		jAlert("请选择相应的目录！","提示");
		return false;
	}
}
</script>
</head>
<body>
<div class="privBack">
	<% 
		int pageNo = ParamUtil.getInt(request,"pageNo",1);//1代表平铺 0代表 列表
		String url = "";
		if(pageNo == 1){
			url = "clouddisk_tiled.jsp?dir_code="+StrUtil.UrlEncode(lf.getCode());
		}else {
			url = "clouddisk_list.jsp?dir_code="+StrUtil.UrlEncode(lf.getCode());
		}
	%>
 	<a href='<%=url%>'>
		<img src="../netdisk/images/clouddisk/back.png"/>
	</a>
	<a href='<%=url%>'><%=lf.getName()%></a>
</div>
</br>
<%
//att -1 lzm添加 用于 判断是否发布链接
String sql = "select id from netdisk_public_attach where att_id=" + attId + " order by create_date desc";

PublicAttachment am = new PublicAttachment();
Iterator ir = am.listResult(sql, 1, 2000).getResult().iterator();
%>
<table width="80%" align="center" class="shareTable">
  <tr>
  	<th></th>
    <th>文件名</th>
    <th>共享目录</th>
    <th>发布者</th>
    <th>时间</th>
    <th>操作</th>
  </tr>
    <%
	long fileLength = -1;
	UserMgr um = new UserMgr();
	Directory dir = new Directory();
	PublicDirectory pdir = new PublicDirectory();
	DocumentMgr dm = new DocumentMgr();
	while (ir.hasNext()) {
	 	am = (PublicAttachment)ir.next();
		fileLength = (long)am.getSize()/1024; 
		if(fileLength == 0 && (long)am.getSize() > 0)
			fileLength = 1;
		PublicLeaf publf = pdir.getLeaf(am.getPublicDir());
	%>
	<tr class="btnTr">
	    <td><img src="../netdisk/images/sort/<%=Attachment.getIcon(am.getExt()) %>" width="32" height="32" /></td>
	    <td><%=am.getName()%></td>
	    <td><a href="clouddisk_pubilc_share.jsp?dir_code=<%=StrUtil.UrlEncode(publf.getCode())%>"><%=publf.getName()%></a></td>
	    <td>
	    <%
	  		UserDb ud = um.getUserDb(am.getUserName());
	  		%>
	  		<%=ud.getRealName()%>
	  	</td>
	    <td><%=DateUtil.format(am.getCreateDate(), "yy-MM-dd HH:mm")%></td>
	    <td><input type="button" value="取消发布"  class="btn" onclick="cancelShare('<%=attId%>','<%=am.getId()%>')" /></td>
  	</tr>
  	<%} %>
</table>
<br />
<form name=form1 action="?op=share" method=post onsubmit="return form_onsubmit(1)">
	<table  width="80%"  align="center" class="dirTable">
	  <tr>
	    <th>将<%=att.getName()%>发布到资源库</th>
	  </tr>
	  <tr class="btnTr">
	    <td>
	    	<span class="colTitle">目录名称</span>
	      <select  id="linkResources" name=dirCode onChange="selDirCode(1)" class="linkResourcesSel colSel">
	      	  <%
				PublicLeaf rootLeaf = new PublicLeaf();
				rootLeaf = rootLeaf.getLeaf(rootLeaf.ROOTCODE);
				PublicDirectoryView pdv = new PublicDirectoryView(rootLeaf);
				pdv.ShowDirectoryAsOptionsWithCode(request, out, rootLeaf, rootLeaf.getLayer());
				%>
	      </select>
	      <input type="hidden" name="attachId" value="<%=attId%>">
	 	  <input type="hidden" name="privurl" value="<%=privurl%>" />
	      <span class="colTitle">（只发布链接）</span>
	    </td>
	  </tr>
	   <tr class="btnTr">
	    <td>
	    	<input type="hidden" name="isLink" value="true" /> 
	    	<input type="submit" value="确定" class="sub"/>
		</td>
	    
	  </tr>
</table>

</form>
<form action="?op=share" method="post" name="form2" id="form2" onsubmit="return form_onsubmit(2)">
	<table  width="80%"  align="center" class="dirTable">
	  <tr>
	    <th>将<%=att.getName()%>复制至资源库</th>
	  </tr>
	  <tr class="btnTr">
	    <td>
	    	<span class="colTitle">目录名称</span>
	      <select  id="copyResources" name=dirCode onChange="selDirCode(2)" class="copyResourcesSel colSel">
	      	  <%
	      	pdv.ShowDirectoryAsOptionsWithCode(request, out, rootLeaf, rootLeaf.getLayer());
				%>
	      </select>
	      <input type="hidden" name="attachId" value="<%=attId%>">
	 	  <input type="hidden" name="privurl" value="<%=privurl%>" />
	      <span class="colTitle">（拷贝至资源库）</span>
	    </td>
	  </tr>
	   <tr class="btnTr">
	    <td>
	    <input type="hidden" name="isLink" value="false" /> 
	    	<input type="submit" value="确定" class="sub"/>
		</td>
	  </tr>
</table>
</form>
</body>
</html>