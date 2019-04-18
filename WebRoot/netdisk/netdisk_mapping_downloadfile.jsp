<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.dept.*"%><%@page import="com.redmoon.oa.*"%><%@page import="com.redmoon.oa.netdisk.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
  //你可以使用你服务器上的文件及其路径  
  String mappingAddress = ParamUtil.get(request, "mappingAddress");
  java.io.File f = new java.io.File(mappingAddress);
  String fileName = f.getName();
  //String s = "e:\\tree.mdb"; 
  //经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
  //java.io.RandomAccessFile raf = new java.io.RandomAccessFile(s,"r");


  java.io.FileInputStream fis = new java.io.FileInputStream(f);

  response.reset();

  //response.setHeader("Server", "playyuer@Microshaoft.com");

  //告诉客户端允许断点续传多线程连接下载
  //响应的格式是:
  //Accept-Ranges: bytes
  response.setHeader("Accept-Ranges", "bytes");

  long p = 0;
  long l = 0;
  //l = raf.length();
  l = f.length();

  //如果是第一次下,还没有断点续传,状态是默认的 200,无需显式设置
  //响应的格式是:
  //HTTP/1.1 200 OK

  if (request.getHeader("Range") != null) //客户端请求的下载的文件块的开始字节
  {
   //如果是下载文件的范围而不是全部,向客户端声明支持并开始文件块下载
   //要设置状态
   //响应的格式是:
   //HTTP/1.1 206 Partial Content
   response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);//206

   //从请求中得到开始的字节
   //请求的格式是:
   //Range: bytes=[文件块的开始字节]-
   p = Long.parseLong(request.getHeader("Range").replaceAll("bytes=","").replaceAll("-",""));
  }

  //下载的文件(或块)长度
  //响应的格式是:
  //Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
  response.setHeader("Content-Length", new Long(l - p).toString()); 

  if (p != 0)
  {
   //不是从最开始下载,
   //响应的格式是:
   //Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
   response.setHeader("Content-Range","bytes " + new Long(p).toString() + "-" + new Long(l -1).toString() + "/" + new Long(l).toString());
  }

  //response.setHeader("Connection", "Close"); //如果有此句话不能用 IE 直接下载

  //使客户端直接下载
  //响应的格式是:
  //Content-Type: application/octet-stream
  response.setContentType("application/octet-stream");

  //为客户端下载指定默认的下载文件名称
  //响应的格式是:
  //Content-Disposition: attachment;filename="[文件名]"
  //response.setHeader("Content-Disposition", "attachment;filename=\"" + s.substring(s.lastIndexOf("\\") + 1) + "\""); //经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
  response.setHeader("Content-Disposition", "attachment;filename=\"" + StrUtil.GBToUnicode(fileName) + "\"");

  //raf.seek(p);
  fis.skip(p);

  byte[] b = new byte[1024]; 
  int i;

  //while ( (i = raf.read(b)) != -1 ) //经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
  while ( (i = fis.read(b)) != -1 )
  {
   response.getOutputStream().write(b,0,i);
  }
  //raf.close();//经测试 RandomAccessFile 也可以实现,有兴趣可将注释去掉,并注释掉 FileInputStream 版本的语句
  fis.close();
%>