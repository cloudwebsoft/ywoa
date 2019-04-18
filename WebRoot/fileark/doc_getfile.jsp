<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.*"%><%@ page import="com.redmoon.oa.pvg.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.fileark.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><%@page import="com.cloudwebsoft.framework.util.*"%><%
  int id = ParamUtil.getInt(request, "id", -1);
  int attId = ParamUtil.getInt(request, "attachId");
  String dirCode = ParamUtil.get(request, "dir_code");
  com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
  
  String op = ParamUtil.get(request, "op");
  
  int pageNum = 1;
  String pn = ParamUtil.get(request, "pageNum");
  if (StrUtil.isNumeric(pn))
  	pageNum = Integer.parseInt(pn);

  Attachment att = null;
  Document mmd = new Document();
  if (id==-1) {
	  att = new Attachment(attId);
	  mmd = mmd.getDocument(att.getDocId());
  }
  else {
	  mmd = mmd.getDocument(id);
	  att = mmd.getAttachment(pageNum, attId);
  }
  
  dirCode = mmd.getDirCode();
  String uName = privilege.getUser(request);
  String ip = IPUtil.getRemoteAddr(request);
  
  Leaf lf = new Leaf();
  lf = lf.getLeaf(mmd.getDirCode());
  
  try {
  	Directory.onDownloadValidate(request, String.valueOf(attId), uName, false);
  }
  catch (ErrMsgException e) {
  	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));  
  	return;
  }
  java.util.Date logDate = new java.util.Date();
  DocLogDb dld = new DocLogDb();
  DocAttachmentLogDb dad = new DocAttachmentLogDb();
  // 记录访问日志
  if(lf.isLog()){
	  dad.setUserName(uName);
	  dad.setAtt_id(attId);
	  dad.setIp(ip);
	  dad.setLogDate(logDate);
	  dad.setDoc_id(id);
	  dad.create();
	  
	  dld.setUserName(uName);
	  dld.setDoc_id(id);
	  dld.setIp(ip);
	  dld.setLogDate(logDate);
	  dld.create();
  }
    
  att.setDownloadCount(att.getDownloadCount() + 1);
  att.save();
  
  String s = att.getVisualPath() + "/" + att.getDiskName();
  
  if (!"download".equals(op) && StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
  	%>
  	<img src="<%=request.getContextPath() %>/img_show.jsp?path=<%=s%>">
	<%
	return;
  }
  
  s = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
  
  //String s = "e:\\tree.mdb"; 
  // RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  //java.io.RandomAccessFile raf = new java.io.RandomAccessFile(s,"r");

  java.io.File f = new java.io.File(s);
  java.io.FileInputStream fis = null;
  
  try {
  	fis = new java.io.FileInputStream(f);
  }
  catch (java.io.FileNotFoundException e) {
  	e.printStackTrace();
  	out.print(SkinUtil.makeErrMsg(request, "文件不存在！"));
  	return;
  }

  response.reset();

  //response.setHeader("Server", "playyuer@Microshaoft.com");

  //Accept-Ranges: bytes
  response.setHeader("Accept-Ranges", "bytes");

  long p = 0;
  long l = 0;
  //l = raf.length();
  l = f.length();


  //HTTP/1.1 200 OK

  if (request.getHeader("Range") != null) //ͻصļĿʼֽ
  {
   //HTTP/1.1 206 Partial Content
   response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);//206

   //Range: bytes=[ļĿʼֽ]-
   p = Long.parseLong(request.getHeader("Range").replaceAll("bytes=","").replaceAll("-",""));
  }

  //Content-Length: [ļܴС] - [ͻصļĿʼֽ]
  response.setHeader("Content-Length", new Long(l - p).toString()); 

  if (p != 0)
  {
   //Content-Range: bytes [ļĿʼֽ]-[ļܴС - 1]/[ļܴС]
   response.setHeader("Content-Range","bytes " + new Long(p).toString() + "-" + new Long(l -1).toString() + "/" + new Long(l).toString());
  }

  //response.setHeader("Connection", "Close"); //д˾仰 IE ֱ

  //ʹͻֱ
  //Ӧĸʽ:
  //Content-Type: application/octet-stream
  response.setContentType("application/octet-stream");

  //ΪͻָĬϵļ
  //Ӧĸʽ:
  //Content-Disposition: attachment;filename="[ļ]"
  //response.setHeader("Content-Disposition", "attachment;filename=\"" + s.substring(s.lastIndexOf("\\") + 1) + "\""); // RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  response.setHeader("Content-Disposition", "attachment;filename=\"" + StrUtil.GBToUnicode(att.getName()) + "\"");

  //raf.seek(p);
  fis.skip(p);

  byte[] b = new byte[1024]; 
  int i;

  //while ( (i = raf.read(b)) != -1 ) // RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  while ( (i = fis.read(b)) != -1 ) {
  	response.getOutputStream().write(b,0,i);
  }
  //raf.close();// RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  fis.close();
  
  try {
  	Directory.onDownload(request, String.valueOf(attId), uName, false);
  }
  catch (ErrMsgException e) {
  	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));  
  	return;
  }  
  
  out.clear();
  out = pageContext.pushBody();  
%>