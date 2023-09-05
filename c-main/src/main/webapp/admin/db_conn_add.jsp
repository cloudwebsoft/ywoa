<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 org.jdom.*,
				 org.jdom.input.*,
				 org.jdom.output.*,
				 org.json.*,
                 java.util.*,
				 java.net.*,
                 java.sql.*"
%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="com.cloudweb.oa.utils.ConfigUtil" %>
<%@ page import="org.xml.sax.InputSource" %>
<%@ page import="com.cloudweb.oa.base.IConfigUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ProxoolUtil" %>
<%
String oadb="", user="root", pwd="", ip="", port="3309", database="", url="", maximum_connection_count="50", odbcName = "", path="";

String op = ParamUtil.get(request, "op");
boolean isValid = true;
String className = "";
String msg = "";
if (op.equals("setup")) {
	String name = ParamUtil.get(request, "name");
	user = ParamUtil.get(request, "user");
	pwd = ParamUtil.get(request, "pwd");
	ip = ParamUtil.get(request, "ip");
	port = ParamUtil.get(request, "port");
	database = ParamUtil.get(request, "database");
	//url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
	oadb = ParamUtil.get(request, "oadb");
	odbcName = ParamUtil.get(request, "odbcName");
	path = ParamUtil.get(request, "path");

	int max_conn = ParamUtil.getInt(request, "max_conn", 50);

	if (oadb.equalsIgnoreCase("access")) {
		className = "sun.jdbc.odbc.JdbcOdbcDriver";
		url = "jdbc:odbc:driver={" + odbcName + "};DBQ=" + path;
	} else if (oadb.equalsIgnoreCase(Global.DB_ORACLE)) {
		className = "oracle.jdbc.driver.OracleDriver";
		url = "jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database;
	} else if (oadb.equalsIgnoreCase("mssql")) {
		className = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
		url = "jdbc:microsoft:sqlserver://" + ip + ":" + port + ";DatabaseName=" + database;
	} else if (oadb.equalsIgnoreCase("mssql_n")) {
		className = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		url = "jdbc:sqlserver://" + ip + ":" + port + ";DatabaseName=" + database;
	} else if (oadb.equalsIgnoreCase(Global.DB_MYSQL)) {
		className = "com.mysql.cj.jdbc.Driver";
		url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&serverTimezone=Asia/Shanghai";
	} else if (oadb.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
		className = "org.postgresql.Driver";
		url = "jdbc:postgresql://" + ip + ":" + port + "/" + database;
	}

    try {
        Class.forName(className);
    }
    catch (java.lang.ClassNotFoundException e) {
		isValid = false;
		msg = e.getMessage();
    }
    try {
        java.sql.Connection con = DriverManager.getConnection(url, user, pwd);
        con.close();
    }
    catch (SQLException e) {
		isValid = false;
		msg = e.getMessage();
    }

	JSONObject json = new JSONObject();
	if (isValid) {
        try {
			IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
			String xml = configUtil.getXml("config_sys");
        	SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(new InputSource(new StringReader(xml)));

            Element root = doc.getRootElement();
        	Element which = root.getChild("DataBase");
			Element edb = new Element("db");

			Element e = new Element("name");
			e.addContent(name);
			edb.addContent(e);

			e = new Element("Default");
			e.addContent("false");
			edb.addContent(e);

			e = new Element("UsePool");
			e.addContent("false");
			edb.addContent(e);

			e = new Element("PoolName");
			e.addContent("");
			edb.addContent(e);

			e = new Element("DBDriver");
			e.addContent("org.logicalcobwebs.proxool.ProxoolDriver");
			edb.addContent(e);

			e = new Element("ConnStr");
			e.addContent("proxool." + name);
			edb.addContent(e);

			which.addContent(edb);

			configUtil.putXml("config_sys", doc);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

		ProxoolUtil proxoolUtil = SpringUtil.getBean(ProxoolUtil.class);
		Document doc = proxoolUtil.getDoc();

		Element root = doc.getRootElement();
		Element edb = new Element("proxool");

		Element e = new Element("alias");
		e.addContent(name);
		edb.addContent(e);

		e = new Element("driver-url");
		e.addContent(url);
		edb.addContent(e);

		e = new Element("driver-class");
		e.addContent(className);
		edb.addContent(e);

		e = new Element("driver-properties");
		Element el = new Element("property");
		el.setAttribute("name", "user");
		el.setAttribute("value", user);
		e.addContent(el);
		el = new Element("property");
		el.setAttribute("name", "password");
		el.setAttribute("value", pwd);
		e.addContent(el);
		edb.addContent(e);

		e = new Element("maximum-connection-count");
		e.addContent(String.valueOf(max_conn));
		edb.addContent(e);

		e = new Element("house-keeping-test-sql");
		e.addContent("select 1");
		edb.addContent(e);

		root.addContent(edb);

		proxoolUtil.write();

		Global.getInstance().init();

		org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000); //
		org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(proxoolUtil.getCfgPath(), false);

		json.put("ret", "1");
		json.put("msg", "连接成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", msg);
	}
	out.print(json);
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>数据库连接驱动-添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>

<script>
$(function() {
	$("#oadb").find("option[value='<%=oadb%>']").attr("selected", true);
	changedb();
});

function changedb() {
	var index = $('#oadb').val();
	if (index == 'access') {
		$('#tr_ip').hide();
		$('#tr_port').hide();
		$('#tr_database').hide();
		$('#tr_conn').hide();
		$('#tr_path').show();
		$('#tr_drive').show();
	} else {
		$('#tr_ip').show();
		$('#tr_port').show();
		$('#tr_database').show();
		$('#tr_conn').show();
		$('#tr_path').hide();
		$('#tr_drive').hide();
	}
}
</script>
<body>
<div class="spacerH"></div>
<form id="form1" name=form1 action="?op=setup" method="post" onsubmit="return form1_onsubmit()">
<table id="t1" class="tabStyle_1 percent98" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td class="tabStyle_1_title" colspan=2>数据库配置</td>
        </tr>
      <tr>
        <td width="25%" height="24" align="right">&nbsp;</td>
        <td></td>
      </tr>
      <tr>
        <td height="24" align="right">名称</td>
        <td><input id="name" name="name" /></td>
      </tr>
      <tr>
        <td height="24" align="right">数据库类型</td>
        <td>
        <select id="oadb" name="oadb" value="<%=oadb %>" onchange='changedb()'>
		  <option value="">请选择数据库类型</option>
		  <option value="access">Access</option>
		  <option value="mysql">MySQL</option>
		  <option value="mssql">SQLServer2000/2003</option>
		  <option value="mssql_n">SQLServer2005+</option>
		  <option value="oracle">Oracle</option>
		  <option value="postgresql">PostGreSql</option>
	      </select>
        </td>
      </tr>
      <tr id="tr_drive">
        <td height="24" align="right">ODBC驱动程序</td>
        <td><input id="odbcName" name="odbcName" style="width:500px;" value="<%=odbcName%>"/></td>
      </tr>
      <tr id="tr_path">
        <td height="24" align="right">Access文件路径</td>
        <td><input id="path" name="path" style="width:500px;" value="<%=path%>"/></td>
      </tr>
      <tr id="tr_ip">
        <td height="24" align="right">主机名</td>
        <td><input id="ip" name="ip" value="<%=ip%>"/></td>
      </tr>
      <tr id="tr_port">
        <td height="24" align="right">端口号</td>
        <td><input id="port" name="port" value="<%=port%>"/></td>
      </tr>
      <tr id="tr_database">
        <td height="24" align="right">数据库名</td>
        <td><input id="database" name="database" value="<%=database%>"/></td>
      </tr>
      <tr id="tr_conn">
        <td height="24" align="right">最大连接数</td>
        <td><input id="max_conn" name="max_conn" value="<%=maximum_connection_count%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right">用户名</td>
        <td><input id="user" name="user" value="<%=user%>"/></td>
      </tr>
      <tr>
        <td height="24" align="right">密码</td>
        <td><input type="password" id="pwd" name="pwd" style="width:148px" value="<%=pwd%>"/></td>
      </tr>
      <tr>
      <td colspan=2 align=center>
		<input type="submit" class="btn" value="测试连接并保存" />
      </td>
      </tr>
    </table>
</form>
</body>
<script>
var connName = new LiveValidation('name');
connName.add(Validate.Presence);
var user = new LiveValidation('user');
user.add(Validate.Presence);
var pwd = new LiveValidation('pwd');
pwd.add(Validate.Presence);

function form1_onsubmit() {
	var params = $("#form1").serialize();
	$.ajax({
		type: "post",
		url: "db_conn_add.jsp?" + params,
		data : {
			op : "setup"
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//  ShowLoading();
		},
		success: function(data, status){
			var r = $.parseJSON(data);
			if (r.ret=="1") {
				jAlert(r.msg, "提示", function() {
					window.opener.location.reload();
					window.close();
				});
			}
			else {
				jAlert(r.msg, "提示");
			}
		},
		complete: function(XMLHttpRequest, status){
			// HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
	return false;
}
</script>
</html>