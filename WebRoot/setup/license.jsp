<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.io.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 cn.js.fan.util.file.*,
				 com.redmoon.oa.fileark.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.kit.util.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.Config,
				 com.redmoon.oa.SpConfig"
				 
%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
String licFileName = "";
if ("upload".equals(op)) {
	FileUpload fileUpload = new FileUpload();
	fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
	String[] extnames = {"dat"};
	fileUpload.setValidExtname(extnames); //设置可上传的文件类型
	int ret = 0;
	try {
		ret = fileUpload.doUpload(application, request);
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fileUpload.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                String filepath = Global.getRealPath() + FileUpload.TEMP_PATH + "/";
               	fileUpload.setSavePath(filepath);
               	fileUpload.writeFile(true);
                // 使用随机名称写入磁盘
                // fi.writeToPath(filepath);
                licFileName = filepath + fi.getDiskName();
            }
        }	
}
else if ("change".equals(op)) {
	licFileName = ParamUtil.get(request, "licFileName");
	FileUtil.CopyFile(licFileName, Global.getRealPath() + "WEB-INF/license.dat");
	License.getInstance().init();
	out.print(StrUtil.Alert("操作成功！"));
}

License.getInstance().init();
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv))
{
    // out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>
<form name="addform" action="license.jsp?op=upload" method="post" enctype="MULTIPART/FORM-DATA">
<table border="0" width="100%">
  <tr>
    <td align="center">许可证文件
      <input type="file" name="attachment0" />
      <input type="submit" value="上传" />
      </td>
  </tr>
</table>
</form>
<%if ("".equals(licFileName)) {%>
<table width="53%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
      <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;许可证信息 </td>
      </tr>
      <tr>
        <td width="17%" align="left">授权单位</td>
        <td width="83%" align="left"><%
		Config oaCfg = new Config();
		SpConfig spCfg = new SpConfig();
		String version = StrUtil.getNullStr(oaCfg.get("version"));
		String spVersion = StrUtil.getNullStr(spCfg.get("version"));        
	  	License license = License.getInstance();
	  	%>
        <%=license.getCompany()%>
        <%
        // LicenseUtil.setLicenseFilePath("d:/license11.dat");
        // LicenseUtil lu = LicenseUtil.getInstance();
        // out.print(lu.getCompany());
        // out.print(lu.getName());
        // out.print(lu.getUserCount());
        // out.print(DateUtil.format(lu.getExpiresDate(), "yyyy-MM-dd"));
        // out.print(lu.getDomain());
        %>
        </td>
      </tr>
      <tr>
        <td width="17%" align="left">名称</td>
        <td width="83%" align="left">
        <%=license.getName()%></td>
      </tr>
      <tr>
        <td align="left">企业号</td>
        <td align="left"><%=license.getEnterpriseNum() != null ? license.getEnterpriseNum() : license.getName()%></td>
      </tr>
      <tr>
        <td align="left">用户数</td>
        <td align="left"><%=license.getUserCount()%></td>
      </tr>
      <tr>
        <td align="left">类型</td>
        <td align="left"><%=license.getType().equals(license.TYPE_COMMERICAL) ? "免费版" : license.getType()%></td>
      </tr>
      <tr>
        <td align="left">到期时间</td>
        <td align="left"><%=DateUtil.format(license.getExpiresDate(), "yyyy-MM-dd")%></td>
      </tr>
      <tr>
        <td align="left">域名</td>
        <td align="left"><%=license.getDomain()%></td>
      </tr>
      <tr>
        <td align="left">许可证版本</td>
        <td align="left"><%=license.getVersion()%></td>
      </tr>      
      <tr>
        <td align="left">系统版本</td>
        <td align="left"><%=version%></td>
      </tr>
      <tr>
        <td align="left">系统补丁版本</td>
        <td align="left"><%=spVersion%></td>
      </tr>
      <tr>
        <td colspan="2" align="center">
		<input type="button" onclick="window.history.back()" value="返回"/>
		</td>
      </tr>
    </tbody>
</table>
<%}else{%>
<table width="53%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
      <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;新许可证信息 </td>
      </tr>
      <tr>
        <td width="17%" align="left">授权单位</td>
        <td width="83%" align="left"><%
        LicenseUtil.setLicenseFilePath(licFileName);
        LicenseUtil lu = LicenseUtil.getInstance();
        lu.init();
        %>
        <%=lu.getCompany()%>
        </td>
      </tr>
      <tr>
        <td width="17%" align="left">使用单位</td>
        <td width="83%" align="left">
        <%=lu.getName()%></td>
      </tr>
      <tr>
        <td align="left">企业号</td>
        <td align="left"><%=lu.getEnterpriseNum()%></td>
      </tr>
      <tr>
        <td align="left">用户数</td>
        <td align="left"><%=lu.getUserCount()%></td>
      </tr>
      <tr>
        <td align="left">类型</td>
        <td align="left"><%=lu.getType().equals(License.TYPE_COMMERICAL) ? "免费版" : lu.getType()%></td>
      </tr>
      <tr>
        <td align="left">到期时间</td>
        <td align="left"><%=DateUtil.format(lu.getExpiresDate(), "yyyy-MM-dd")%></td>
      </tr>
      <tr>
        <td align="left">域名</td>
        <td align="left"><%=lu.getDomain()%></td>
      </tr>
      <tr>
        <td colspan="2" align="center">
        <input id="btnChange" type="button" value="替换" />
        &nbsp;&nbsp;
        <input id="btnLogin" type="button" value="登录" />     
<%
if (!"".equals(op)) {
%>        
        &nbsp;&nbsp;
        <input id="btnCurLic" type="button" value="当前许可证" />
		<script>
        $('#btnCurLic').click(function() {
        	window.location.href = "license.jsp";
        });  
        </script>		
<%}%>        
        <script>
        $('#btnChange').click(function() {
        	window.location.href = "license.jsp?op=change&licFileName=<%=licFileName%>";
        });
        $('#btnLogin').click(function() {
        	window.location.href = "../index.jsp";
        });    
        </script>
        &nbsp;&nbsp;
		<input type="button" onclick="window.location.href='setup.jsp'" value="返回"/>           
        </td>
      </tr>
    </tbody>
</table>
<%}%>
</body>
</html>