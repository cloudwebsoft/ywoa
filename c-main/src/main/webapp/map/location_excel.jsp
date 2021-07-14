<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<%@page import="org.json.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="jxl.format.UnderlineStyle"%>
<%@page import="jxl.biff.DisplayFormat"%>
<%@page import="com.redmoon.oa.map.LocationDb"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}


String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String dept = ParamUtil.get(request, "dept");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "l.create_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String sql = "select l.id from oa_location l, dept_user du where l.user_name=du.user_name";

if (action.equals("search")) {
	if (kind.equals("user")) {
		if (!"".equals(what))
			sql += " and l.user_name like " + StrUtil.sqlstr("%" + what + "%");
	}
	else if (kind.equals("address")) {
		if (!"".equals(what))
			sql += " and l.address like " + StrUtil.sqlstr("%" + what + "%");
	}

	if (!beginDate.equals("")) {
		if (DateUtil.parse(beginDate, "yyyy-MM-dd")!=null)
			sql += " and l.create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
	}
	if (!endDate.equals("")) {
		java.util.Date d = DateUtil.parse(endDate, "yyyy-MM-dd");
		if (d!=null) {
			d = DateUtil.addDate(d, 1);
			sql += " and l.create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
		}
	}
	if (!dept.equals("")) {
		sql += " and du.dept_code=" + SQLFilter.sqlstr(dept);
	}		
}
sql += " order by " + orderBy + " " + sort;

LocationDb ld = new LocationDb();
Vector v = ld.list(sql);

// System.out.println(getClass() + " " + sql);
// if (true) return;
			
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("考勤记录导出") + ".xls");  
            
OutputStream os = response.getOutputStream();

try {
	
	File file = new File(Global.getAppPath() + "visual/template/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);
	UserMgr um = new UserMgr();

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
	WritableSheet ws = wwb.getSheet(0);
	
	/* 
	* WritableFont.createFont("宋体")：设置字体为宋体 
	* 10：设置字体大小 
	* WritableFont.NO_BOLD:设置字体非加粗（BOLD：加粗     NO_BOLD：不加粗） 
	* false：设置非斜体 
	* UnderlineStyle.NO_UNDERLINE：没有下划线 
	*/  
	WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 
                                         12, 
                                         WritableFont.BOLD);
	WritableCellFormat wcFormat = new WritableCellFormat(font);
	//水平居中对齐
	wcFormat.setAlignment(Alignment.CENTRE);
	//竖直方向居中对齐
	wcFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
    wcFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
    
    String title_A = "ID";
    Label title_A_label = new Label(0, 0, title_A, wcFormat);
	ws.addCell(title_A_label);
	String title_B = "用户";
    Label title_B_label = new Label(1, 0, title_B, wcFormat);
	ws.addCell(title_B_label);
	String title_C = "部门";
    Label title_C_label = new Label(2, 0, title_C, wcFormat);
	ws.addCell(title_C_label);
	String title_D = "信息";
    Label title_D_label = new Label(3, 0, title_D, wcFormat);
	ws.addCell(title_D_label);
	String title_E = "附近位置";
    Label title_E_label = new Label(4, 0, title_E, wcFormat);
	ws.addCell(title_E_label);
	String title_F = "日期";
    Label title_F_label = new Label(5, 0, title_F, wcFormat);
	ws.addCell(title_F_label);
	
	int i = 1;
	DeptUserDb dud = new DeptUserDb();
	
	if(v!=null){
		Iterator ir = v.iterator();
		ld = null;
		while(ir.hasNext()){
			ld = (LocationDb)ir.next();
			
			long id = ld.getLong("id");
			String realName = um.getUserDb(ld.getString("user_name")).getRealName();
			String depts = "";
			Iterator ir2 = dud.getDeptsOfUser(ld.getString("user_name")).iterator();
			int k = 0; 
			while(ir2.hasNext()){
				String deptName = "";
				DeptDb dd = (DeptDb) ir2.next();
      			if (!dd.getParentCode().equals(DeptDb.ROOTCODE)
      					&& !dd.getCode().equals(DeptDb.ROOTCODE)) {
      				DeptMgr dm = new DeptMgr();
      				deptName = dm.getDeptDb(dd.getParentCode()).getName()
      						+ "->"
      						+ dd.getName();
      			} else
      				deptName = dd.getName();
      			if(k==0){
      				depts = deptName; 
      			}else{
      				depts += ","+deptName;
      			}
      			k ++;
			}
			String remark = StrUtil.getNullStr(ld.getString("remark"));
			String address = StrUtil.getNullStr(ld.getString("address"));
			String date = DateUtil.format(ld.getDate("create_date"), "yyyy-MM-dd HH:mm:ss");
			
			wcFormat = setCellFormat(FormField.FIELD_TYPE_TEXT,i);
			Label A_label = new Label(0,i,id+"",wcFormat);
			ws.addCell(A_label);
			wcFormat = setCellFormat(FormField.FIELD_TYPE_TEXT,i);
			Label B_label = new Label(1,i,realName,wcFormat);
			ws.addCell(B_label);
			wcFormat = setCellFormat(FormField.FIELD_TYPE_TEXT,i);
			Label C_label = new Label(2,i,depts,wcFormat);
			ws.addCell(C_label);
			wcFormat = setCellFormat(FormField.FIELD_TYPE_TEXT,i);
			Label D_label = new Label(3,i,remark,wcFormat);
			ws.addCell(D_label);
			wcFormat = setCellFormat(FormField.FIELD_TYPE_TEXT,i);
			Label E_label = new Label(4,i,address,wcFormat);
			ws.addCell(E_label);
			wcFormat = setCellFormat(FormField.FIELD_TYPE_DATETIME,i);
			Label F_label = new Label(5,i,date,wcFormat);
			ws.addCell(F_label);
			
			i++;
		}
	}
	
	//设置列宽
	ws.setColumnView(0, 10);
	ws.setColumnView(1, 10);
	ws.setColumnView(2, 40);
	ws.setColumnView(3, 20);
	ws.setColumnView(4, 50);
	ws.setColumnView(5, 30);
    
	
	
	
	wwb.write();
	wwb.close();
	wb.close();
} catch (Exception e) {
	e.printStackTrace();
	out.println(e.toString());
}
finally {
	os.close();
}

out.clear();
out = pageContext.pushBody();
%>

<%!
// 设置单元格格式
private WritableCellFormat setCellFormat(int fieldType, int row) {
    WritableCellFormat wcf = null;
    try {
    	// 单元格格式
    	switch (fieldType) {
		case FormField.FIELD_TYPE_DOUBLE:
		case FormField.FIELD_TYPE_FLOAT:
		case FormField.FIELD_TYPE_PRICE:
			NumberFormat nf1 = new NumberFormat("0.00");
			wcf = new WritableCellFormat(nf1);
			break;
		case FormField.FIELD_TYPE_INT:
		case FormField.FIELD_TYPE_LONG:
			NumberFormat nf2 = new NumberFormat("#");
			wcf = new WritableCellFormat(nf2);
			break;
		case FormField.FIELD_TYPE_DATE:
			jxl.write.DateFormat df1 = new jxl.write.DateFormat("yyyy-MM-dd");
			wcf = new jxl.write.WritableCellFormat(df1);
			break;
		case FormField.FIELD_TYPE_DATETIME:
			jxl.write.DateFormat df2 = new jxl.write.DateFormat("yyyy-MM-dd HH:mm:ss");
			wcf = new jxl.write.WritableCellFormat(df2);
			break;
		default:
			wcf = new WritableCellFormat();
			break;
		}
        // 对齐方式
        wcf.setAlignment(Alignment.CENTRE);
        wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
        // 边框
        wcf.setBorder(Border.ALL,BorderLineStyle.THIN);
        // 背景色
        if (row % 2 == 0) {
        	wcf.setBackground(jxl.format.Colour.ICE_BLUE);
		} else {
			wcf.setBackground(jxl.format.Colour.WHITE);
		}
		//自动换行
        wcf.setWrap(true);
    } catch (WriteException e) {
		e.printStackTrace();
    }    
    return wcf;
}

// 创建单元格
private WritableCell createWritableCell(int fieldType, int column, int row, String data, WritableCellFormat wcf) {
	WritableCell wc = null;
	if (data == null || data.equals("")) {
		wc = new Label(column, row, "", wcf);
	} else {
		switch (fieldType) {
		case FormField.FIELD_TYPE_TEXT:
		case FormField.FIELD_TYPE_VARCHAR:
			wc = new Label(column, row, data, wcf);
			break;
		case FormField.FIELD_TYPE_DOUBLE:
		case FormField.FIELD_TYPE_FLOAT:
		case FormField.FIELD_TYPE_PRICE:
			wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
			break;
		case FormField.FIELD_TYPE_INT:
		case FormField.FIELD_TYPE_LONG:
			wc = new jxl.write.Number(column, row, StrUtil.toLong(data), wcf);
			break;
		case FormField.FIELD_TYPE_DATE:
			wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd"), wcf);
			break;
		case FormField.FIELD_TYPE_DATETIME:
			wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd HH:mm:ss"), wcf);
			break;
		default:
			wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
			break;
		}
	}
	return wc;
}
%>
