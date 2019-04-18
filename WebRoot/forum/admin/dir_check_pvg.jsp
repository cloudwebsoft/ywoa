<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.forum_m" key="filter_info"/></title>
<%@ include file="../inc/nocache.jsp" %>
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><%out.print(SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","check_pvg"));%></td>
  </tr>
</table>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<br>
<%
		String cloudHome = cn.js.fan.web.Global.realPath;
		String CacheTemp = "";
		boolean propError = false;
		String errorMessage = "";
		String rightMessage = "";
		PropertiesUtil pu = new PropertiesUtil(cn.js.fan.web.Global.realPath + "WEB-INF" + File.separator + "log4j.properties");
        String logpath = pu.getValue("log4j.appender.R.File");
		java.net.URL cfgURL = getClass().getClassLoader().getResource("cache.ccf");
		PropertiesUtil pucache = new PropertiesUtil(java.net.URLDecoder.decode(cfgURL.getFile()));
		String cachepath = pucache.getValue("jcs.auxiliary.DC.attributes.DiskPath");
		String FileUploadTmp = cloudHome + "FileUploadTmp" + File.separator;
		String doc = cloudHome + "doc" + File.separator;
		String forumupfiledir = cloudHome + "forum" + File.separator + "upfile" + File.separator;
		String upfiledir = cloudHome + "upfile" + File.separator;
		String classesdir = cloudHome + "WEB-INF"  + File.separator + "classes" + File.separator;
		String bakdir = cloudHome + "bak" + File.separator;
		String themedir = cloudHome + "forum" + File.separator + "images" + File.separator + "theme" + File.separator;
		File filelog = new File(logpath);	
		File cachefile = new File(cachepath);
		File fileUpload = new File(FileUploadTmp);
		File docfile = new File(doc);
		File upfile = new File(upfiledir);
		File forumupfile = new File(forumupfiledir);
		File classesfile = new File(classesdir);
		File bakfile = new File(bakdir);
		File themefile = new File(themedir);
		try {
			if (cloudHome != null) {
                try {
                    File file = new File(cloudHome);
                    if (!file.exists()) {
                        propError = true;
                        errorMessage = "" + cloudHome + "</tt> " +
                            SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","no_exsist") + "<br>";
                    }
                }
                catch (Exception e) {}
                if (!propError) {
					boolean readable = (new File(cloudHome)).canRead();
    				if (!readable) {
    					propError = true;
    					errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + cloudHome +
    					SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
    				}
					boolean writable = false;
					if (readable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + cloudHome + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}
					// 检查Log目录是否有读写权限						
					readable = filelog.getParentFile().canRead();					
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + filelog.getParentFile() +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = filelog.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + filelog.getParentFile() +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + filelog.getParentFile() + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}
					
					// 检查CacheTemp是否有读写权限					
					readable = cachefile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + cachepath +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = cachefile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + cachepath +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + cachepath + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}					
						
					// 检查FileUploadTmp目录上传是否有读写权限					
					readable = fileUpload.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + FileUploadTmp +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = fileUpload.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + FileUploadTmp +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}	
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + FileUploadTmp + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}															
					// 检查doc目录上传是否有读写权限					
					readable = docfile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + doc +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = docfile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + doc +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + doc + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}						
					// 检查forum/upfile目录上传是否有读写权限					
					readable = forumupfile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + forumupfiledir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = forumupfile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + forumupfiledir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + forumupfiledir + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}												
					// 检查upfile目录上传是否有读写权限					
					readable = upfile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + upfiledir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = upfile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + upfiledir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + upfiledir + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}	
					// 检查WEB-INF/classes/目录上传是否有读写权限					
					readable = classesfile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + classesdir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = classesfile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + classesdir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + classesdir + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}	
					// 检查bak目录上传是否有读写权限					
					readable = bakfile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + bakdir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = bakfile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + bakdir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}	
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + bakdir + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}	
					// 检查themefile目录是否有读写权限
					readable = themefile.canRead();
					if (!readable) {
						propError = true;
						errorMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + themedir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_read") + "<br>";
					}
					writable = themefile.canWrite();
					if (!writable) {
						propError = true;
						errorMessage +=  SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + themedir +
									SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","can_not_write") + "<br>";
					}	
					if (readable && writable) {
					    rightMessage += SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir") + themedir + "   " + SkinUtil.LoadString(request, "res.label.forum.admin.dir_check_pvg", "read_and_write_priv_right") + "<br>";
					}						
                }
	    	}
        	else {
           		propError = true;
           		errorMessage = "<tt>" + cloudHome + "</tt>" + SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir_not_right") + "<br>";
        	}
		}
		catch (Exception e) {
			e.printStackTrace();
			out.print(StrUtil.toHtml(StrUtil.trace(e)));
			propError = true;
           	errorMessage = SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","check") + cloudHome + "</tt>" + SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","dir_exception") +
				SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","check_cwbbs") + "<br>";
		}
%>
<TABLE class="frame_gray" cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD colspan="2" valign="top" bgcolor="#FFFBFF" class="thead"><%out.print(SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","file_dir_attr"));%></TD>
    </TR>
    <TR>
      <TD width="51%" height=200 valign="top" bgcolor="#FFFBFF"><br>
        <ul><%
		if(errorMessage.equals("")) {
		%><%out.print(SkinUtil.LoadString(request,"res.label.forum.admin.dir_check_pvg","file_dir_all_right") + "<br>");%>
		<%}%>
		<%out.print(rightMessage);%></ul>	
	  </TD>
      <TD width="49%" valign="top" bgcolor="#FFFBFF"><br>
	  <%
	  if(!errorMessage.equals("")) {
	  %>
	  <ul><font color="#FF0000"><%out.print(errorMessage);%></font></ul>
	  <%}%>
	  </TD>
    </TR>
  </TBODY>
</TABLE>
<br>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>                                      
</html>                            
  