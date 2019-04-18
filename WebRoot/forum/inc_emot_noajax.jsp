<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.ErrMsgException"
import = "cn.js.fan.web.SkinUtil"
import = "com.redmoon.forum.ui.*"
%>
<%
com.redmoon.forum.Config cfgEmot = com.redmoon.forum.Config.getInstance();
if (cfgEmot.getBooleanProperty("forum.isCustomEmotShow")) {
%>
<fieldset id="smiliebox" style="width:200px; height:150px;border: 1px solid #E3E3E3;">
<legend><lt:Label res="res.label.forum.showtopic" key="emote"/></legend>
<div style="width:200px; height:150px;overflow-y:scroll;padding: 3px">
		<%
		int q = 0;
		String[] emots = EmotMgr.getEmots("1");
		int esize = emots.length;
		int countPerRow = 5;
		int row = 0;
		for (q=0; q<esize; q++) {%>
			<a href="javascript:cws_InsertSymbol('<img src=../<%=emots[q]%> border=0>');/*delayhidemenu()*/"><img border="0" src="../<%=emots[q]%>"></a>&nbsp;
		<%
			row ++;
			if (row==3)
				out.print("<BR>");
		}
		%>
</div>
<div align="right" style="padding:3px;">
[<a href="#" onClick="return clickreturnvalue()" onMouseover="dropdownmenu(this, event, 'anylinkmenu1')"><lt:Label res="res.label.forum.showtopic" key="emote_more"/></a>]</div>
</fieldset>
<script type="text/javascript" src="inc/anylinkvertical.js"></script>		
<script type="text/javascript" src="inc/tabcontent.js"></script>
<div id="anylinkmenu1" class="anylinkcss">
	<div class="shadetabs_bar"">
	<ul id="maintab" class="shadetabs">
	<li class="selected"><a href="#" rel="tcontent1"><lt:Label res="res.label.forum.showtopic" key="emote1"/></a></li>
	<li><a href="#" rel="tcontent2"><lt:Label res="res.label.forum.showtopic" key="emote2"/></a></li>
	<li><a href="#" rel="tcontent3"><lt:Label res="res.label.forum.showtopic" key="emote_default"/></a></li>
	</ul>
	</div>
	<div class="tabcontentstyle">
		<div id="tcontent1" class="tabcontent">
		<%
		row = 0;
		for (q=0; q<esize; q++) {%>
			<a href="javascript:cws_InsertSymbol('<img src=../<%=emots[q]%> border=0>');/*delayhidemenu()*/"><img border="0" src="../<%=emots[q]%>"></a>&nbsp;
		<%
			row ++;
			if (row==countPerRow)
				out.print("<BR>");	
		}
		%>
		</div>
		<div id="tcontent2" class="tabcontent">
		<%
		emots = EmotMgr.getEmots("2");
		esize = emots.length;		
		row = 0;
		for (q=0; q<esize; q++) {%>
			<a href="javascript:cws_InsertSymbol('<img src=../<%=emots[q]%> border=0>');/*delayhidemenu()*/"><img border="0" src="../<%=emots[q]%>"></a>&nbsp;
		<%
			row ++;
			if (row==countPerRow)
				out.print("<BR>");	
		}
		%>
		</div>
		<div id="tcontent3" class="tabcontent">
		<%
		emots = EmotMgr.getEmots("0");
		esize = emots.length;
		row = 0;
		for (q=0; q<esize; q++) {%>
			<a href="javascript:cws_InsertSymbol('<img src=../<%=emots[q]%> border=0>');/*delayhidemenu()*/"><img border="0" src="../<%=emots[q]%>"></a>&nbsp;
		<%
			row ++;
			if (row==10)
				out.print("<BR>");
		}
		%>		
		</div>
	</div>
	<script type="text/javascript">
	initializetabcontent("maintab");
	</script>
</div>
<%}%>