<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.fileark.*"%><%@page import="org.json.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><%@page import="com.redmoon.oa.map.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %><%
/*
- 功能描述：移动手机端使用
- 访问规则：手机端口文件柜
- 过程描述：用于手机端文件柜的附件下载
- 注意事项：
- 创建者：fgf 
- 创建时间：
*/ 
  JSONObject result = new JSONObject(); 
  String skey = ParamUtil.get(request,"skey");
  com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
  String userName = pri.getUserName(skey);

    if (userName.equals("")) {
        try {
            result.put("res", "-1");
            result.put("msg", "skey不存在");
            out.println(result.toString());
            return;
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

  int id = ParamUtil.getInt(request, "id");
  LocationDb ld = new LocationDb();
  ld = (LocationDb)ld.getQObjectDb(new Long(id));
  
  if (ld==null) {
	try {
			result.put("res","-1");
			result.put("msg","文件不存在");
			out.println(result.toString());
			return;
		} catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
		}
  }
  
  
  String s = Global.getRealPath() + "upfile/" + ld.getString("file_path");
  
  java.io.File f = new java.io.File(s);
  
  if (!f.exists()) {
	  return;
  }
  
  java.io.FileInputStream fis = new java.io.FileInputStream(f);

  response.reset();

  //response.setHeader("Server", "playyuer@Microshaoft.com");

  //߿ͻϵ߳
  //Ӧĸʽ:
  //Accept-Ranges: bytes
  response.setHeader("Accept-Ranges", "bytes");

  long p = 0;
  long l = 0;
  //l = raf.length();
  l = f.length();

  //ǵһ,ûжϵ,״̬Ĭϵ 200,ʽ
  //Ӧĸʽ:
  //HTTP/1.1 200 OK

  if (request.getHeader("Range") != null) //ͻصļĿʼֽ
  {
   //ļķΧȫ,ͻֲ֧ʼļ
   //Ҫ״̬
   //Ӧĸʽ:
   //HTTP/1.1 206 Partial Content
   response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);//206

   //еõʼֽ
   //ĸʽ:
   //Range: bytes=[ļĿʼֽ]-
   p = Long.parseLong(request.getHeader("Range").replaceAll("bytes=","").replaceAll("-",""));
  }

  //صļ()
  //Ӧĸʽ:
  //Content-Length: [ļܴС] - [ͻصļĿʼֽ]
  response.setHeader("Content-Length", new Long(l - p).toString()); 

  if (p != 0)
  {
   //Ǵʼ,
   //Ӧĸʽ:
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
  response.setHeader("Content-Disposition", "attachment;filename=\"" + StrUtil.GBToUnicode("定位文件.jpg") + "\"");

  //raf.seek(p);
  fis.skip(p);

  byte[] b = new byte[1024]; 
  int i;

  //while ( (i = raf.read(b)) != -1 ) // RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  while ( (i = fis.read(b)) != -1 )
  {
   response.getOutputStream().write(b,0,i);
  }
  //raf.close();// RandomAccessFile Ҳʵ,Ȥɽעȥ,ע͵ FileInputStream 汾
  fis.close();
  
out.clear();
out = pageContext.pushBody();
  
%>