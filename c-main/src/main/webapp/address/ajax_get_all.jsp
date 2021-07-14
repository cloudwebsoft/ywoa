<%@ page import = "java.util.*"%><%@ page import = "cn.js.fan.db.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.cloudwebsoft.framework.db.*"%><%@ page import = "com.redmoon.oa.dept.*"%><%@ page import = "com.redmoon.oa.basic.*"%><%@ page import = "com.redmoon.oa.ui.*"%><%@ page import = "com.redmoon.oa.person.*"%><%@ page import = "com.redmoon.oa.visual.*"%><%@ page import = "com.redmoon.oa.flow.FormDb"%><%@ page import = "com.redmoon.oa.address.*"%><%@ page import = "org.json.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
response.reset();

int type = ParamUtil.getInt(request, "type", AddressDb.TYPE_USER);
String sql = "select id from address where type=" + type;
String myname = privilege.getUser(request);
String group = ParamUtil.get(request, "dir_code");
String op = ParamUtil.get(request, "op");

if (op.equals("search")){
   String person = ParamUtil.get(request, "person");
   String nickname = ParamUtil.get(request, "nickname");
   String company = ParamUtil.get(request, "company");
   String address = ParamUtil.get(request, "address");
   String street = ParamUtil.get(request, "street");
   String QQ = ParamUtil.get(request, "QQ");
   String MSN = ParamUtil.get(request, "MSN");
   String typeId = ParamUtil.get(request, "typeId");	
				  
   if (type==AddressDb.TYPE_USER)
	   sql = "select id from address where userName=" + StrUtil.sqlstr(privilege.getUser(request)) + " and type=" + AddressDb.TYPE_USER;
   else {
		sql = "select id from address where type=" + type;
   }
   if (!person.equals("")){
		sql += " and person like " + StrUtil.sqlstr("%" + person + "%");
   }
   if (!nickname.equals("")){
		sql += " and nickname like " + StrUtil.sqlstr("%" + nickname + "%");
   }
   if (!company.equals("")){
		sql += " and company like " + StrUtil.sqlstr("%" + company + "%");
   }
   if (!address.equals("")){
		sql += " and address like " + StrUtil.sqlstr("%" + address + "%");
   }
   if (!street.equals("")){
		sql += " and street like " + StrUtil.sqlstr("%" + street + "%");
   }
   if (!MSN.equals("")){
		sql += " and MSN like " + StrUtil.sqlstr("%" + MSN + "%");
   }
   if (!QQ.equals("")){
		sql += " and QQ like " + StrUtil.sqlstr("%" + QQ + "%");
   }
   if (!typeId.equals("")){
	   sql += " and typeId = " + StrUtil.sqlstr(typeId);				   
   }
}
else {
	if (!group.equals(""))
		sql += " and typeId = " + StrUtil.sqlstr(group);
	if (type!=AddressDb.TYPE_PUBLIC)	 
		sql += " and userName=" + 	StrUtil.sqlstr(privilege.getUser(request)); 
}

if (type==AddressDb.TYPE_PUBLIC) {
	sql += " and unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request));
}

// System.out.println(getClass() + " sql=" + sql);

AddressDb addr = new AddressDb();
Iterator ir = addr.list(sql).iterator();

// 定义rows，存放数据
JSONArray rows = new JSONArray();              

while (ir.hasNext()) {
	addr = (AddressDb)ir.next();

	// 存放一条记录的对象
	JSONObject cell = new JSONObject();   
	cell.put("person", addr.getPerson());
	cell.put("mobile", addr.getMobile());
	// 将该记录放入rows中
	rows.put(cell);
}

// response.setContentType("application/x-json");
response.setCharacterEncoding("UTF-8");
out.print(rows);
%>
