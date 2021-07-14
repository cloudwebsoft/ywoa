<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<style>
#loading{
position:absolute;
top:50%;
left:50%;
margin-top:30px;
margin-left:-300px;
width:600px;
height:400px;
text-align: center;
vertical-align: middle;
display:table-cell;
}
#loading table {
_margin-top:expression((400-this.height)/2-30);
}
</style>
<div id="loading" style="display:none">
	<table border="0" cellpadding="0" cellspacing="0" id="loadingImg" style="width:120px;height:30px;background-image:url(<%=SkinMgr.getSkinPath(request)%>/images/loading_bg.gif)">
	<tr><td>
	<img src="<%=SkinMgr.getSkinPath(request)%>/images/loading1.gif" width="110px" />
  </td></tr></table>
</div>
<script>
function displayLoading(isShow) {
	if (isShow)
		document.getElementById("loading").style.display = "";
	else
		document.getElementById("loading").style.display = "none";
}
</script>