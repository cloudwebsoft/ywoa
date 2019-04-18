<%@ page contentType="text/html; charset=utf-8"%>
<div id="emoteBox">
	<div class="emoteCnt">
		<div class="emoteClose" onclick="closeTipEmote()">×</div>
		<div class="emoteIcon">
		<%
			for (int i=1; i<=40; i++) {
				out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"insertEmote('[em"+i+"]')\" style=\"CURSOR: pointer\">");
			}
		%>
        </div>
	</div>
</div>
<script src="<%=request.getContextPath()%>/inc/tip_emote.js"></script>