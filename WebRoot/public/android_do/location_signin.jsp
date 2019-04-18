<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.map.*"%>
<%@ page import="com.redmoon.kit.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<%
/*
- 功能描述：定位签到
- 访问规则：来自手机客户端
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-5
==================
- 修改者： lichao
- 修改时间：2015-06-26
- 修改原因： 
- 修改点：定位签到增加考勤功能，考勤规则跟oa中的在线考勤规律一样。
*/

// System.out.println(getClass() + " here");

/*
String address = ParamUtil.get(request, "address");
String latitude = ParamUtil.get(request, "latitude");
String lontitude = ParamUtil.get(request, "lontitude");
String remark = ParamUtil.get(request, "locationInfo");

System.out.println(getClass() + " address=" + address);
System.out.println(getClass() + " address2=" + request.getParameter("address"));
// System.out.println(getClass() + " latitude=" + latitude);
// System.out.println(getClass() + " lontitude=" + lontitude);

LocationDb ld = new LocationDb();
re = ld.create(new JdbcTemplate(), new Object[]{userName,new java.util.Date(),latitude,lontitude,address,remark});
*/
JSONObject json = new JSONObject();
FileUpload fu = new FileUpload();
try {
	fu.doUpload(application, request);
}
catch (Exception e) {
	e.printStackTrace();
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}

String skey = fu.getFieldValue("skey");
boolean re = privilege.Auth(skey);
if(re){
	try {
		json.put("res","-2");
		json.put("msg","时间过期");
		out.print(json.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

String userName = privilege.getUserName(skey);
if (userName.equals("")) {
	json.put("res", "0");
	json.put("msg", "请先登录！");
	out.print(json);
	return;
}

String address = fu.getFieldValue("address");
String latitude = fu.getFieldValue("latitude");
String lontitude = fu.getFieldValue("lontitude");
String remark = fu.getFieldValue("locationInfo");
String flagStr = StrUtil.getNullStr(fu.getFieldValue("flag"));
int flag = 0;
if(!flagStr.equals("")){
	flag = StrUtil.toInt(flagStr);
}

// System.out.println(getClass() + " address=" + address);

LocationDb ld = new LocationDb();

// 保存附件
Calendar cal = Calendar.getInstance();
String year = "" + (cal.get(Calendar.YEAR));
String month = "" + (cal.get(Calendar.MONTH) + 1);
String path = "location/" + year + "/" + month + "/";

long fileSize = 0;

if (fu.getRet() == FileUpload.RET_SUCCESS) {
	Vector v = fu.getFiles();
	com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	boolean isCaptureWhenLocation = "true".equals(cfg.getProperty("isCaptureWhenLocation"));
	if (isCaptureWhenLocation) {
		if (v.size()==0) {
			json.put("res", "-1");
			json.put("msg", "请先拍照！");
			out.print(json);
			return;
		}
	}
	
	if (v.size()>0) {
		String filepath = Global.getRealPath() + "upfile/" + path;
		fu.setSavePath(filepath);
		// 使用随机名称写入磁盘
		fu.writeFile(true);
		FileInfo fi = null;
		Iterator ir = v.iterator();
		if (ir.hasNext()) {
		   fi = (FileInfo) ir.next();
		   path += fi.getDiskName();
		   fileSize = fi.getSize();
		}
	}
	else {
		path = "";
	}
}


// add by lichao 2015-06-26 定位时同时考勤  ------start-----
java.util.Date kqDate;
String kqDateStr = ParamUtil.get(request, "kqDate");
if (!kqDateStr.equals("")) {
	kqDate = DateUtil.parse(kqDateStr, "yyyy-MM-dd");
}else {
	kqDate = new java.util.Date();
}


int kind =-1;
try {
	kind = KaoqinMgr.locationCanCheck( userName, kqDate);
} catch (ErrMsgException e){
	e.printStackTrace();
	json.put("msg", "操作失败！");
	out.print(json);
	return;
}

boolean res =false;
if(kind != -1){
	try {
		KaoqinMgr kq = new KaoqinMgr();
		res = kq.locationCreate(userName, kind ,kqDate,"");
	} catch (ErrMsgException e) {
		e.printStackTrace();
		json.put("msg", "操作失败！");
		out.print(json);
		return;
	}
}

//add by lichao 2015-06-26 定位时同时考勤  ------end-----

String client = StrUtil.getNullStr(fu.getFieldValue("client"));

double lat = StrUtil.toDouble(latitude, 0);
double lnt = StrUtil.toDouble(lontitude, 0);
re = ld.create(new JdbcTemplate(), new Object[]{userName,new java.util.Date(),new Double(lat),new Double(lnt),address,remark,path, fileSize, client,flag});

if (re) {
	if (client.equals("ios")) {
		json.put("res", "0");
	}
	else {
		json.put("res", "1");
	}
	json.put("msg", "操作成功！");
	json.put("address", address);
}
else {
	if (client.equals("ios")) {
		json.put("res", "-1");
	}
	else {
		json.put("res", "0");
	}
	json.put("msg", "操作失败！");
}
out.print(json);
%>