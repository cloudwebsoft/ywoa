<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.ErrMsgException"
import = "cn.js.fan.web.SkinUtil"
import = "com.redmoon.forum.ui.*"
%>
<%
int q = 0;		
String[] emots = EmotMgr.getEmots("2");
int esize = emots.length;
int countPerRow = 5;
int row = 0;
row = 0;
for (q=0; q<esize; q++) {%>
	<a href="javascript:cws_InsertSymbol('<img src=../<%=emots[q]%> border=0>')"><img border="0" src="../<%=emots[q]%>"></a>&nbsp;
<%
	row ++;
	if (row==countPerRow)
		out.print("<BR>");	
}
%>