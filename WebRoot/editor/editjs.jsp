<%@ page contentType="text/html;charset=utf-8"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String editrootpath = request.getContextPath();
%>
document.writeln("<div id=\"cws_edit\"> ");
document.writeln("	<ul> <select language=\"javascript\" class=\"cws_TBGen\" id=\"FontSize\" onchange=\"FormatText('fontsize',this[this.selectedIndex].value);\">");
document.writeln("	<option class=\"heading\" selected><lt:Label res="res.label.editor.editjs" key="font_size"/> ");
document.writeln("	<option value=\"1\">1 ");
document.writeln("	<option value=\"2\">2 ");
document.writeln("	<option value=\"3\">3 ");
document.writeln("	<option value=\"4\">4 ");
document.writeln("	<option value=\"5\">5 ");
document.writeln("	<option value=\"6\">6 ");
document.writeln("	<option value=\"7\">7<\/option>");
document.writeln("	<\/select> ");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="bold"/>\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'bold\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/bold.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="italic"/>\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'italic\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/italic.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="underline"/>\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'underline\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/underline.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="cancel_fomat"/>\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'RemoveFormat\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/removeformat.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="align_left"/>\" NAME=\"Justify\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'justifyleft\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/aleft.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="align_center"/>\" NAME=\"Justify\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'justifycenter\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/center.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="align_right"/>\" NAME=\"Justify\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'justifyright\', \'\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/aright.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="emote"/>\" LANGUAGE=\"javascript\" onclick=\"cws_foremot()\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/smiley.gif\" \/> <\/li>");
document.writeln("	<li id=\"forecolor\" name=forecolor class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="font_color"/>\" LANGUAGE=\"javascript\" onclick=\"cws_foreColor();\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/fgcolor.gif\" \/> <\/li>");
document.writeln("	<li id=\"backcolor\" class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="font_bg_color"/>\" LANGUAGE=\"javascript\" onclick=\"cws_backColor();ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\';> ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/fbcolor.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="link"/>\" LANGUAGE=\"javascript\" onclick=\"cws_forlink();ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/wlink.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="cancel_link"/>\" LANGUAGE=\"javascript\" onclick=\"FormatText(\'Unlink\');ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\'; > ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/unlink.gif\" \/> <\/li>");
document.writeln("	<li class=\"cws_Btn\" TITLE=\"<lt:Label res="res.label.editor.editjs" key="clear_code"/>\" LANGUAGE=\"javascript\" onclick=\"cws_CleanCode();ondrag=\'return false;\'\" onmouseover=this.className=\'cws_BtnMouseOverUp\'; onmouseout=this.className=\'cws_Btn\';> ");
document.writeln("	<img class=\"cws_Ico\" src=\"<%=editrootpath%>\/editor\/images\/cleancode.gif\" \/><\/li>");
document.writeln("	<\/ul>");
document.writeln("	<ul style=\"height:100%\" id=\"PostiFrame\"> <iframe class=\"cws_Composition\" ID=\"cws_Composition\" MARGINHEIGHT=\"5\" MARGINWIDTH=\"5\" width=\"99%\" height=\"200\"><\/iframe> <\/ul>");
document.writeln("    <ul style=\"text-align:right;display:none\"><a href=\"javascript:cws_Size(-360)\"><img src=\"<%=editrootpath%>\/editor\/images\/minus.gif\" border=\'0\' height=\"20\" \/><\/a> <a href=\"javascript:cws_Size(330)\"><img src=\"<%=editrootpath%>\/editor\/images\/plus.gif\"  border=\'0\' height=\"20\" \/><\/a>");
document.writeln("	<li style=\"width:10px;\"><\/li>");
document.writeln("	<\/ul>");
document.writeln("<\/div>");