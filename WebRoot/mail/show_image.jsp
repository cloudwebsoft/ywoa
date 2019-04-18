<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@page import="cn.js.fan.web.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.emailpop3.Attachment"%>
<%@page import="com.redmoon.oa.emailpop3.MailMsgDb"%>
<%
  int id = ParamUtil.getInt(request, "id", -1);
  int attId = ParamUtil.getInt(request, "attachId");
  com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
  

  Attachment att = null;
  MailMsgDb mailMsgDb = new MailMsgDb();
  if (id==-1) {
	  att = new Attachment(attId);
	  mailMsgDb = mailMsgDb.getMailMsgDb(att.geteEailId());
	
  }
  else {
	  mailMsgDb = mailMsgDb.getMailMsgDb(id);
	  att = mailMsgDb.getAttachment(attId);
  }
  
  String uName = privilege.getUser(request);
  java.util.Date logDate = new java.util.Date();
  
  String s = att.getVisualPath() + "/" + att.getDiskName();
  
  if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
  	%>
  	<img src="<%=request.getContextPath() %>/img_show.jsp?path=<%=s%>">
	<%
	return;
  }

  java.io.File f = new java.io.File(s);
  java.io.FileInputStream fis = new java.io.FileInputStream(f);

  response.reset();

  response.setHeader("Accept-Ranges", "bytes");

  long p = 0;
  long l = 0;
  l = f.length();


  if (request.getHeader("Range") != null) 
  {
   response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);//206
   p = Long.parseLong(request.getHeader("Range").replaceAll("bytes=","").replaceAll("-",""));
  }

 
  response.setHeader("Content-Length", new Long(l - p).toString()); 

  if (p != 0)
  {
   response.setHeader("Content-Range","bytes " + new Long(p).toString() + "-" + new Long(l -1).toString() + "/" + new Long(l).toString());
  }


  response.setContentType("application/octet-stream");

  response.setHeader("Content-Disposition", "attachment;filename=\"" + StrUtil.GBToUnicode(att.getName()) + "\"");

  fis.skip(p);

  byte[] b = new byte[1024]; 
  int i;

  while ( (i = fis.read(b)) != -1 )
  {
   response.getOutputStream().write(b,0,i);
  }
  fis.close();
  
  out.clear();
  out = pageContext.pushBody();  
%>