<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
String skey = ParamUtil.get(request, "skey");
JSONObject jsonRet = new JSONObject();
boolean re = privilege.Auth(skey);
if (re) {
	try {
		jsonRet.put("res", "-2");
		jsonRet.put("msg", "时间过期");
		out.print(jsonRet.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

String op = ParamUtil.get(request, "op");
String moduleCode = ParamUtil.get(request, "formCode");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(moduleCode);

FormDb fd = new FormDb();
fd = fd.getFormDb(msd.getString("form_code"));
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "表单不存在！");
	out.print(jsonRet.toString());
	return;
}

String userName = privilege.getUserName(skey);
ModulePrivDb mpd = new ModulePrivDb(moduleCode);
if (!mpd.canUserSee(userName)) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
	out.print(jsonRet.toString());
	return;
}

String byFieldName = ParamUtil.get(request, "byFieldName");
String showFieldName = ParamUtil.get(request, "showFieldName");

String openerFormCode = ParamUtil.get(request, "openerFormCode");
String openerFieldName = ParamUtil.get(request, "openerFieldName");

String filter = "";

FormDb openerFd = new FormDb();
openerFd = openerFd.getFormDb(openerFormCode);
FormField openerField = openerFd.getFormField(openerFieldName);
JSONArray mapAry = new JSONArray();
try {
	// System.out.println(getClass() + " openerField.getDescription()=" + openerField.getDescription());
	String desc = ModuleFieldSelectCtl.formatJSONStr(openerField.getDescription());
	JSONObject json = new JSONObject(desc);
	filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter")); 

	// 过滤条件
	// String filter = ParamUtil.get(request, "filter");
	if (filter.equals("none"))
		filter = "";	
	/*
	String sourceFormCode = json.getString("sourceFormCode");
	String byFieldName = json.getString("idField");
	String showFieldName = json.getString("showField");
	*/
	mapAry = (JSONArray)json.get("maps");
} catch (JSONException e) {
	// TODO Auto-generated catch block
	// "json 格式非法";
	e.printStackTrace();
}

String action = ParamUtil.get(request, "action");

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");

MacroCtlMgr mm = new MacroCtlMgr();
FormMgr fm = new FormMgr();

JSONArray conditions = new JSONArray();
int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			// System.out.println(getClass() + " " + btnScripts[i]);
			JSONObject jsonBtn = new JSONObject(btnScripts[i]);
			if (((String)jsonBtn.get("btnType")).equals("queryFields")) {
				isQuery = true;
				String condFields = (String)jsonBtn.get("fields");
				String[] fieldAry = StrUtil.split(condFields, ",");
				Iterator irKey = jsonBtn.keys();
				for (int j=0; j<fieldAry.length; j++) {
					String fieldName = fieldAry[j];
					String condType = (String)jsonBtn.get(fieldName);
					String queryValue = ParamUtil.get(request, fieldName);
					
					if ("cws_status".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}

						JSONObject jo = new JSONObject();
						jo.put("fieldName", fieldName);
						jo.put("fieldTitle", "状态");
						jo.put("fieldType", FormField.FIELD_TYPE_INT);
						jo.put("fieldCond", condType);
						
						String fieldOptions;
				        JSONArray ary = new JSONArray();
				        
				        JSONObject jsObj = new JSONObject();
				        jsObj.put("name", "不限");
		                jsObj.put("value", SQLBuilder.CWS_STATUS_NOT_LIMITED);
		                ary.put(jsObj);
		                
				        jsObj = new JSONObject();
				        jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT));
		                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DRAFT);
		                ary.put(jsObj);	
		                	
				        jsObj = new JSONObject();
				        jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT));
		                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_NOT);
		                ary.put(jsObj);			    
		                                            		                	
				        jsObj = new JSONObject();
				        jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE));
		                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DONE);
		                ary.put(jsObj);	
		                                            		                	
				        jsObj = new JSONObject();
				        jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED));
		                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_REFUSED);
		                ary.put(jsObj);			 
		                
		                jsObj = new JSONObject();
				        jsObj.put("name", com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD));
		                jsObj.put("value", com.redmoon.oa.flow.FormDAO.STATUS_DISCARD);
		                ary.put(jsObj);	
		                
						jo.put("fieldOptions", ary.toString());		

						int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);				
						if (queryValueCwsStatus!=-20000) {
							queryValue = String.valueOf(queryValueCwsStatus);
						} else {
							queryValue = String.valueOf(msd.getInt("cws_status"));
						}
							
						jo.put("fieldValue", queryValue);
						conditions.put(jo);
					}
					else if ("cws_flag".equals(fieldName)) {
						JSONObject jo = new JSONObject();
						jo.put("fieldName", fieldName);
						jo.put("fieldTitle", "冲抵状态");
						jo.put("fieldType", FormField.FIELD_TYPE_INT);
						jo.put("fieldCond", condType);
						
						String fieldOptions;
				        JSONArray ary = new JSONArray();
				        
				        JSONObject jsObj = new JSONObject();
				        jsObj.put("name", "不限");
		                jsObj.put("value", -1);
		                ary.put(jsObj);				
		                
				        jsObj = new JSONObject();
				        jsObj.put("name", "否");
		                jsObj.put("value", 0);
		                ary.put(jsObj);			
		                
		               	jsObj = new JSONObject();
				        jsObj.put("name", "是");
		                jsObj.put("value", 1);
		                ary.put(jsObj);	
		                
		               	jo.put("fieldOptions", ary.toString());
						jo.put("fieldValue", queryValue);
						conditions.put(jo);					
					}					
					else { 		
						String title = "";
						FormField ff = null;
						if (fieldName.startsWith("main:")) { // 关联的主表
							 String[] aryField = StrUtil.split(fieldName, ":");			
							 if (aryField.length==3) {
							  	FormDb mainFormDb = fm.getFormDb(aryField[1]);
							  	ff = mainFormDb.getFormField(aryField[2]);
								if (ff==null) {
									System.out.print(getClass() + " " + fieldName + "不存在");
								}							  	
							  	title = ff.getTitle();
							 }
							 else {
								System.out.print(getClass() + " " + fieldName + "不存在");
							 }
						}
						else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
							 String[] aryField = StrUtil.split(fieldName, ":");
							 if (aryField.length<5) {
							 	System.out.print(getClass() + " " + fieldName + "格式非法");							 	
							 }
							 else {
								FormDb otherFormDb = fm.getFormDb(aryField[2]);
								ff = otherFormDb.getFormField(aryField[4]);
								if (ff==null) {
									System.out.print(getClass() + " " + fieldName + "不存在");
								}								
								title = ff.getTitle();
							 }
						}			
						else {
							ff = fd.getFormField(fieldName);
						}
						
						if (ff==null) {
							continue;
						}
							
						JSONObject jo = new JSONObject();
						jo.put("fieldName", fieldName);
						jo.put("fieldTitle", ff.getTitle());
						jo.put("fieldType", ff.getFieldType());
						jo.put("type", ff.getType());
						jo.put("fieldCond", condType);
						jo.put("fieldValue", queryValue);
						
						String fieldOptions = "";
						if (ff.getType().equals(FormField.TYPE_MACRO)) {
							MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
							if (mu != null) {
								IFormMacroCtl imc = mu.getIFormMacroCtl();
								if ("select".equals(imc.getControlType())) {
									fieldOptions = imc.getControlOptions(userName, ff);
								}
							}
						}		
						jo.put("fieldOptions", fieldOptions);					
						
	               		if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
							if (condType.equals("0")) {
								String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
								String tDate  = ParamUtil.get(request, ff.getName() + "ToDate");
								jo.put("fromDate", fDate);
								jo.put("toDate", fDate);
							}
						}
						
						conditions.put(jo);
					}
				}
			}
		}
	}
}

jsonRet.put("res", 0);
       	
JSONObject result = new JSONObject();
result.put("op", op);
result.put("action", action);        	        	
result.put("filter", filter);
result.put("conditions", conditions);
        	
// 取得过滤条件中的父窗口的字段        	
boolean isFound = false;
StringBuffer parentFields = new StringBuffer();
if (!"".equals(filter)) {
	Pattern p = Pattern.compile(
			"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	Matcher m = p.matcher(filter);
    while (m.find()) {
        String fieldName = m.group(1);
		// 当条件为包含时，fieldName以@开头
		if (fieldName.startsWith("@"))
			fieldName = fieldName.substring(1);
		else if (fieldName.equals("cwsCurUser")) { // 当前用户
			isFound = true;
			continue;
		}
		
		StrUtil.concat(parentFields, ",", fieldName);
	   	isFound = true;
    }
}

result.put("parentFields", parentFields.toString());
jsonRet.put("result", result);

// 如果未从父窗口中取值，则返回，客户端需取值后再提交
if (isFound) {
	if (!"afterGetClientValue".equals(action)) {
		out.print(jsonRet);
		return;
	}
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
// 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "search", orderBy, sort);
String sql = ary[0];

// System.out.print(getClass() + " " + sql);

int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

// System.out.println(getClass() + " sql=" + sql);
ListResult lr = fdao.listResult(msd.getString("form_code"), sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
result.put("totalCount", total);

len = 0;
if (fields!=null)
	len = fields.length;
JSONArray datas = new JSONArray();	
jsonRet.put("datas", datas);

int k = 0;
UserMgr um = new UserMgr();
while (ir!=null && ir.hasNext()) {
	fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
	k++;
	
	long id = fdao.getId();

	JSONObject row = new JSONObject();
	datas.put(row);
	JSONArray fieldAry = new JSONArray();
	row.put("rId", id);
	row.put("fields", fieldAry);
		
	String showValue = "";
	boolean isShowFieldFound = false;
	for (int i=0; i<len; i++) {
		JSONObject fjo = new JSONObject();
		String fieldName = fields[i];
		if (fieldName.equals("cws_creator")) {
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
			
			fjo.put("title", "创建者");
			fjo.put("name", "cws_creator");
			fjo.put("value", fdao.getCreator());
			fjo.put("text", realName);
		}
		else if (fieldName.equals("cws_status")) {
			fjo.put("title", "状态");
			fjo.put("name", "cws_status");
			fjo.put("value", fdao.getCwsStatus());
			fjo.put("text", com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));		
		}				
		else if (fieldName.equals("cws_flag")) {
			fjo.put("title", "冲抵状态");
			fjo.put("name", "cws_flag");
			fjo.put("value", fdao.getCwsFlag());
			fjo.put("text", fdao.getCwsFlag());
		}
		else if (fieldName.equals("cws_progress")) {
			fjo.put("title", "进度");
			fjo.put("name", "cws_progress");
			fjo.put("value", fdao.getCwsProgress());
			fjo.put("text", fdao.getCwsProgress());		
		} 
		else if (fieldName.equals("ID")) {
			fjo.put("title", "ID");
			fjo.put("name", "ID");
			fjo.put("value", fdao.getId());
			fjo.put("text", fdao.getId());			
		}			
		else {
			if (fieldName.startsWith("main:")) {
				String[] aryMain = StrUtil.split(fieldName, ":");
				FormDb mainFormDb = fm.getFormDb(aryMain[1]);
				long parentId = StrUtil.toLong(fdao.getCwsId(), -1);				
				com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
				com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(parentId);
				FormField ff = mainFormDb.getFormField(aryMain[2]);
				String val = "", text = "";
				if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						val = fdaoMain.getFieldValue(aryMain[2]);
						text = mu.getIFormMacroCtl().converToHtml(request, ff, val);
					}
				} else {
					val = fdmMain.getFieldValueOfMain(parentId, aryMain[2]);
					text = val;
				}
				
				fjo.put("title", ff.getTitle());
				fjo.put("name", ff.getName());
				fjo.put("value", val);
				fjo.put("text", text);				
			}
			else if (fieldName.startsWith("other:")) {
				// 一级
				String title = "";
				String[] aryField = StrUtil.split(fieldName, ":");
				if (aryField.length<5) {
					System.out.print(getClass() + " " + fieldName + "格式非法");		
					continue;					 	
				}
				else {
					FormDb otherFormDb = fm.getFormDb(aryField[2]);
					FormField ff = otherFormDb.getFormField(aryField[4]);
					if (ff==null) {
						System.out.print(getClass() + " " + fieldName + "不存在");
						continue;
					}								
					title = ff.getTitle();
				}
				
				String text = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);	
							 			
				fjo.put("title", title);
				fjo.put("name", fieldName);
				fjo.put("value", text);
				fjo.put("text", text);		
				// String[] ary = StrUtil.split(fieldName, ":");
				
				// FormDb otherFormDb = fm.getFormDb(ary[2]);
				// com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
				// out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
			}	
			else {	
				FormField ff = fd.getFormField(fieldName);
				if (ff!=null) {
					String tempValue = "";
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							tempValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
						}
					} else {
						tempValue = fdao.getFieldValue(fieldName);
					}
					
					fjo.put("title", ff.getTitle());
					fjo.put("name", ff.getName());
					fjo.put("value", fdao.getFieldValue(fieldName));
					fjo.put("text", tempValue);
				}			
			}
		}
		
		fieldAry.put(fjo);		
	}

	String byValue = "";
	if (byFieldName.equals("id")) {
		byValue = "" + id;
	}
	else {
		byValue = fdao.getFieldValue(byFieldName);
	}
	// @task:id在设置宏控件时，还不能被配置为被显示
	if (showFieldName.equals("id")) {//yonghu bdyhxz
		showValue = "" + id;
	}
	else {
		FormField ff = fd.getFormField(showFieldName);
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
			if (mu != null) {
				showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
			}
		} else {
			showValue = fdao.getFieldValue(showFieldName);
		}
	}
	
	row.put("byValue", byValue);
	row.put("showValue", showValue);
	
	JSONArray fieldMapAry = new JSONArray();
	row.put("parentFieldMaps", fieldMapAry);
	
	for (int i=0; i<mapAry.length(); i++) {
		JSONObject json = (JSONObject)mapAry.get(i);
		String destF = (String)json.get("destField");	// 父页面
		String sourceF = (String)json.get("sourceField");	// module_list_sel.jsp页面
		Vector vector = openerFd.getFields();
		Iterator it = vector.iterator();
		FormField tempFf = null;
		while (it.hasNext()) {
			tempFf = (FormField) it.next();
			if (tempFf.getName().equals(destF)) {
				break;
			}
		}
		boolean isMacro = false;
		// setValue为module_list_sel.jsp页面中所选择的值
		String setValue = fdao.getFieldValue(sourceF);
		// 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
		if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
			tempFf.setValue(setValue);
			isMacro = true;
			setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
		}

		JSONObject jo = new JSONObject();
		jo.put("name", destF);
		jo.put("value", fdao.getFieldValue(sourceF));
		jo.put("text", setValue);
		jo.put("isMacro", isMacro);
		
		fieldMapAry.put(jo);
	}	
}

out.print(jsonRet);
%>