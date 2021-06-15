<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.miniplugin.index.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt"%>
<%
String tskincode = UserSet.getSkin(request);
if (tskincode.equals(""))
	tskincode = UserSet.defaultSkin;
SkinMgr tskm = new SkinMgr();
Skin tskin = tskm.getSkin(tskincode);
if (tskin==null)
	tskin = tskm.getSkin(UserSet.defaultSkin);
%>
<style>
#newelitetop td{
padding:0 8px;
}
#newelitetop td p {
margin:0px;
padding:0px;
}
#newelitetop td ul {
margin:0px;
padding:0px;
list-style:none;
line-height:1.0;
}
#newelitetop td ul li {
list-style-type: none;
margin:0px;
padding:2px 5px;
border-bottom:1px solid dotted #cccccc;
}
#newelitetop {
margin-bottom:5px;
}
</style>
<%
Index home = Index.getInstance();
%>
<table id="newelitetop" width="100%" class="tableCommon" style="clear:both;margin-top:8px">
    <thead>
      <tr>
        <td width="28%" align="center">≡
        <lt:Label res="res.label.forum.miniplugin.newelitetop" key="recommandimg"/>≡</td>
        <td width="24%" align="center">≡<%=home.getColumnTitle(request, 2)%>≡</td>
        <td width="24%" align="center">≡<%=home.getColumnTitle(request, 3)%>≡</td>
        <td width="24%" align="center">≡<%=home.getColumnTitle(request, 4)%>≡</td>
      </tr>
    </thead>
	<tbody>
      <tr>
        <td colspan="4" align="left">
		<%
		String userContent = StrUtil.getNullStr(home.getProperty("userDefine.content")).trim();
		if (!userContent.equals(""))
			out.print(userContent);
		%></td>
      </tr>
	</tbody>
    <TBODY>
      <tr>
        <td align="left">
<%		
String str = "<script>";
for (int i = 1; i <= 5; i++) {
	str += "var imgUrl" + i + "=\"" +
			StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
			"url")) + "\";\n";
	str += "var imgtext" + i + "=\"" +
			StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
			"text")) + "\";\n";
	str += "var imgLink" + i + "=\"" +
			StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
			"link")) + "\";\n";
}
str += "</script>";
out.print(str);
%>
<script>
NS6 = (document.getElementById&&!document.all);
IE = (document.all);
var focus_width,focus_height,focus_height;
if (IE){
focus_width=270
focus_height=160
text_height=18
}
if (NS6){
focus_width=270
focus_height=172
text_height=30
}
var swf_height = focus_height+text_height
var pics=imgUrl1+"|"+imgUrl2+"|"+imgUrl3+"|"+imgUrl4+"|"+imgUrl5
var links=imgLink1+"|"+imgLink2+"|"+imgLink3+"|"+imgLink4+"|"+imgLink5
var texts=imgtext1+"|"+imgtext2+"|"+imgtext3+"|"+imgtext4+"|"+imgtext5
document.open();
document.write('<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" style="width:'+ focus_width +'px;height:'+ swf_height +'px">');
document.write('<param name="allowScriptAccess" value="sameDomain"><param name="movie" value="<%=request.getContextPath()%>/images/home/focus.swf"><param name="quality" value="high"><param name="bgcolor" value="#dfdfdf">');
document.write('<param name="menu" value="false"><param name=wmode value="opaque">');
document.write("<param name='FlashVars' value='pics="+pics+"&links="+links+"&texts="+texts+"&borderwidth="+focus_width+"&borderheight="+focus_height+"&textheight="+text_height+"'>");
document.write("<embed src='<%=request.getContextPath()%>/images/home/focus.swf' wmode='opaque' FlashVars='pics="+pics+"&links="+links+"&texts="+texts+"&borderwidth="+focus_width+"&borderheight="+focus_height+"&textheight="+text_height+"' menu='false' bgcolor='#dfdfdf' quality='high' style='width:"+ focus_width +"px;height:"+ focus_height +"px' allowScriptAccess='sameDomain' type='application/x-shockwave-flash' pluginspage='http://www.macromedia.com/go/getflashplayer' />");
document.write('</object>');
document.close();
</script></td>
        <td align="left" valign="top"><%=home.getColumnContent(request, 2, 10, 26)%></td>
        <td align="left" valign="top"><%=home.getColumnContent(request, 3, 10, 26)%></td>
        <td align="left" valign="top"><%=home.getColumnContent(request, 4, 10, 26)%></td>
      </tr>
    </TBODY>
</table>
