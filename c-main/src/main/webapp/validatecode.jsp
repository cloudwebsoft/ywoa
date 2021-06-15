<%@ page contentType="image/jpeg" import="java.awt.*,java.awt.image.*,java.util.*,javax.imageio.*,cn.js.fan.util.*" %><%
int width = 60;
int charNum = 4;
try {
	String num = request.getParameter("charNum");
	if (num!=null) {
		charNum = Integer.parseInt(num);	
		width = charNum * 15;
	}
}
catch (Exception e) {
}

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
String chs = cfg.getProperty("forum.validateCodeChars");
String[] chars = StrUtil.split(chs, ",");
cn.js.fan.security.ValidateCodeCreator vcc = new cn.js.fan.security.ValidateCodeCreator();
// out.clear();
vcc.create(request, response, width, 20, charNum, chars);
%>