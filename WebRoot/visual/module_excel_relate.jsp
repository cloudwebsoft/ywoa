<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import = "java.io.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "org.json.*"%>
<%@ page import = "jxl.*"%>
<%@ page import = "jxl.write.*"%>
<%@ page import = "java.awt.Color"%>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
String formCodeRelated = msd.getString("form_code");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCodeRelated);
if (!fd.isLoaded()) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单不存在！"));
	return;
}
String op = ParamUtil.get(request, "op");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String moduleCode = ParamUtil.get(request, "code");
String mode = ParamUtil.get(request, "mode");
String tagName = ParamUtil.get(request, "tagName");

// 通过选项卡标签关联
boolean isSubTagRelated = "subTagRelated".equals(mode);

if (isSubTagRelated) {
   	String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
	try {
		JSONObject json = new JSONObject(tagUrl);
		if (!json.isNull("formRelated")) {
			// formCodeRelated = json.getString("formRelated");
			moduleCodeRelated = json.getString("formRelated");
			msd = msd.getModuleSetupDb(moduleCodeRelated);
			formCodeRelated = msd.getString("form_code");		
		}
		else {
			out.print(StrUtil.Alert_Back("选项卡关联配置不正确！"));			
			return;
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}
}

String relateFieldValue = "";
int parentId = ParamUtil.getInt(request, "parentId", -1);
if (parentId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	return;
}
else {
	if (!isSubTagRelated) {
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
		relateFieldValue = fdm.getRelateFieldValue(parentId, moduleCodeRelated);
		if (relateFieldValue==null) {
			// 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
			relateFieldValue = SQLBuilder.IS_NOT_RELATED;
		}
	}
}

request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sql = arySQL[0];

FormDAO fdao = new FormDAO();
Vector v = fdao.list(formCodeRelated, sql);

// System.out.print(sql);

String fileName = fd.getName();
long templateId = ParamUtil.getLong(request, "templateId", -1);
ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
if (templateId!=-1) {
	metd = metd.getModuleExportTemplateDb(templateId);
	fileName = metd.getString("name");	
}

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String cols = ParamUtil.get(request, "cols");
if (!"".equals(cols)) {
	listField = cols;
}
String[] fields = StrUtil.split(listField, ",");
			
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fileName) + ".xls");  
            
OutputStream os = response.getOutputStream();

try {
	File file = new File(Global.getAppPath(request) + "visual/template/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);
	UserMgr um = new UserMgr();

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
	WritableSheet ws = wwb.getSheet(0);

	int len = 0;
	if (fields!=null)
		len = fields.length;
		
	
	/* 
	* WritableFont.createFont("宋体")：设置字体为宋体 
	* 10：设置字体大小 
	* WritableFont.NO_BOLD:设置字体非加粗（BOLD：加粗     NO_BOLD：不加粗） 
	* false：设置非斜体 
	* UnderlineStyle.NO_UNDERLINE：没有下划线 
	*/  
	boolean isBar = false;
	int rowHeader = 0;	
	Map mapWidth = new HashMap();
	WritableFont font;
	String backColor = "", foreColor = "";
	if (templateId!=-1) {
		String barName = StrUtil.getNullStr(metd.getString("bar_name"));
		if (!"".equals(barName)) {
			isBar = true;
		}

		String fontFamily = metd.getString("font_family");
		int fontSize = metd.getInt("font_size");
		backColor = metd.getString("back_color");
		foreColor = metd.getString("fore_color");
		boolean isBold = metd.getInt("is_bold") == 1;
		if (isBold) {
			font = new WritableFont(WritableFont.createFont(fontFamily), 
                                         fontSize, 
                                         WritableFont.BOLD);		
		}
		else {
			font = new WritableFont(WritableFont.createFont(fontFamily), 
                                         fontSize, 
                                         WritableFont.NO_BOLD);		
		}

		if (!"".equals(foreColor)) {
			Color color = Color.decode(foreColor); // 自定义的颜色
			wwb.setColourRGB(Colour.BLUE, color.getRed(), color.getGreen(), color.getBlue());
			font.setColour(Colour.BLUE);
		}
	    
	    String columns = metd.getString("cols");

	    boolean isSerialNo = metd.getString("is_serial_no").equals("1");
	    if (isSerialNo) {
	        columns = columns.substring(1); // [{}, {},...]去掉[
			columns = "[{\"field\":\"serialNoForExp\",\"title\":\"序号\",\"link\":\"#\",\"width\":80,\"name\":\"serialNoForExp\"}," + columns;
		}

		JSONArray arr = new JSONArray(columns);
		StringBuffer colsSb = new StringBuffer();
	    for (int i=0; i<arr.length(); i++) {
	    	JSONObject json = arr.getJSONObject(i);
	    	
	    	// System.out.println(getClass() + " " + i + " " + json.getInt("width"));
	    	ws.setColumnView(i, (int)(json.getInt("width") * 0.09 * 0.94)); // 设置列的宽度 ，单位是自己根据实际的像素值推算出来的
	    	
	    	StrUtil.concat(colsSb, ",", json.getString("field"));
	    	mapWidth.put(json.getString("field"), json.getInt("width"));
	    }
	    
		listField = colsSb.toString();
		fields = StrUtil.split(listField, ",");
		len = fields.length;	    
		
		if (isBar) {
			WritableFont barFont;
			String barBackColor = metd.getString("bar_back_color");
			String barForeColor = metd.getString("bar_fore_color");
			String barFontFamily = metd.getString("bar_font_family");
			int barFontSize = metd.getInt("bar_font_size");
			boolean isBarbBold = metd.getInt("bar_is_bold")==1;
			if (isBarbBold) {
				barFont = new WritableFont(WritableFont.createFont(barFontFamily), 
	                                         barFontSize,
	                                         WritableFont.BOLD);
			}
			else {
				barFont = new WritableFont(WritableFont.createFont(barFontFamily), 
	                                         barFontSize, 
	                                         WritableFont.NO_BOLD);		
			}

			if (!"".equals(barForeColor)) {
				Color color = Color.decode(barForeColor); // 自定义的颜色
				wwb.setColourRGB(Colour.RED, color.getRed(), color.getGreen(), color.getBlue());
				barFont.setColour(Colour.RED);
			}
		    
			WritableCellFormat barFormat = new WritableCellFormat(barFont);
			// 水平居中对齐
			barFormat.setAlignment(Alignment.CENTRE);
			// 竖直方向居中对齐
			barFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		    barFormat.setBorder(Border.ALL, BorderLineStyle.THIN);		 

		    if (!"".equals(barBackColor)) {
				Color bClr = Color.decode(barBackColor); // 自定义的颜色
				wwb.setColourRGB(Colour.GREEN, bClr.getRed(), bClr.getGreen(), bClr.getBlue());
				barFormat.setBackground(Colour.GREEN);
			}
		    
		    Label a = new Label(0, 0, barName, barFormat);
			ws.addCell(a);
			
			ws.mergeCells(0, 0, len-1, 0);			
			
	   		ws.setRowView(0, metd.getInt("bar_line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素	    

	    	rowHeader = 1;
	    }
   		ws.setRowView(rowHeader, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
	}
	else {
		font = new WritableFont(WritableFont.createFont("宋体"), 
                                         12, 
                                         WritableFont.BOLD);		
	}
                                         
	WritableCellFormat wcFormat = new WritableCellFormat(font);
	//水平居中对齐
	wcFormat.setAlignment(Alignment.CENTRE);
	//竖直方向居中对齐
	wcFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
    wcFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
    
    if (templateId!=-1) {
        if (!"".equals(backColor)) {
			Color color = Color.decode(backColor); // 自定义的颜色
			wwb.setColourRGB(Colour.ORANGE, color.getRed(), color.getGreen(), color.getBlue());
			wcFormat.setBackground(Colour.ORANGE);
		}
    }		
    
	FormMgr fm = new FormMgr();    
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title;
		if (fieldName.equals("serialNoForExp")) {
		    title = "序号";
		}
		else if (fieldName.equals("cws_creator")) {
			title = "创建者";
		}
		else if (fieldName.equals("ID")) {
			title = "ID";
		}
		else if (fieldName.equals("cws_status")) {
			title = "状态";
		}		
		else if (fieldName.equals("cws_flag")) {
			title = "冲抵状态";
		}			
		else {
			if (fieldName.startsWith("main")) {
				String[] ary = StrUtil.split(fieldName, ":");
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				title = mainFormDb.getFieldTitle(ary[2]);
			}
			else if (fieldName.startsWith("other")) {
				String[] ary = StrUtil.split(fieldName, ":");
				FormDb otherFormDb = fm.getFormDb(ary[2]);
				title = otherFormDb.getFieldTitle(ary[4]);
			}
			else {
				title = fd.getFieldTitle(fieldName);
			}
			if ("".equals(title)) {
				title = fieldName + "不存在";
			}
		}
				
		Label a = new Label(i, rowHeader, title, wcFormat);
		ws.addCell(a);
	}

	Iterator ir = v.iterator();

	int j = rowHeader + 1;
	int k = 0;
	
	MacroCtlMgr mm = new MacroCtlMgr();
	while (ir.hasNext()) {
		fdao = (FormDAO)ir.next();
		// 置SQL宏控件中需要用到的fdao		
		RequestUtil.setFormDAO(request, fdao);
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			String fieldValue = "";
			if (fieldName.equals("serialNoForExp")) {
			    fieldValue = String.valueOf(++k);
			}
			else if (fieldName.equals("cws_creator")) {
				fieldValue = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
			}
			else if (fieldName.equals("cws_progress")) {
				fieldValue = String.valueOf(fdao.getCwsProgress());
			}
			else if (fieldName.equals("ID")) {
				fieldValue = String.valueOf(fdao.getId());
			}					
			else if (fieldName.equals("cws_status")) {
				fieldValue = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
			}	
			else if (fieldName.equals("cws_flag")) {
				fieldValue = String.valueOf(fdao.getCwsFlag());
			}				
			else {
				if (fieldName.startsWith("main")) {
					String[] ary = StrUtil.split(fieldName, ":");
					FormDb mainFormDb = fm.getFormDb(ary[1]);
					com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
					com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(parentId);
					FormField ff = mainFormDb.getFormField(ary[2]);
					if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2]));
						}
					} else {
						fieldValue = fdmMain.getFieldValueOfMain(parentId, ary[2]);
					}
				}
				else if (fieldName.startsWith("other:")) {
					fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
				}
				else{			
					FormField ff = fd.getFormField(fieldName);
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null && !mu.getCode().equals("macro_raty")) {
							fieldValue = StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)), 1000, "");
							// fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
						}
						else {
							fieldValue = fdao.getFieldValue(fieldName);
						}
					}
					else {				
						fieldValue = fdao.getFieldValue(fieldName);
					}
				}
			}
			
			Label a = new Label(i, j, fieldValue);
			ws.addCell(a);
		}
			
		j++;
	}
	wwb.write();
	wwb.close();
	wb.close();
} catch (Exception e) {
	// System.out.println(e.toString());
	e.printStackTrace();
}
finally {
	os.close();
}
out.clear();
out = pageContext.pushBody();
%>