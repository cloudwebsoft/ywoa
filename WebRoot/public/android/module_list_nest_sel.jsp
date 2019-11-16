<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
/*
- 功能描述：手机端从嵌套表格点击“选择”按钮出的模块选择窗体，拉单
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：20170212
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/
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

privilege.doLogin(request, skey);

String op = ParamUtil.get(request, "op");

String nestFormCode = ParamUtil.get(request, "nestFormCode");

String nestType = ParamUtil.get(request, "nestType");
String parentFormCode = ParamUtil.get(request, "parentFormCode");
String nestFieldName = ParamUtil.get(request, "nestFieldName");
long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);

FormDb pForm = new FormDb();
pForm = pForm.getFormDb(parentFormCode);
FormField nestField = pForm.getFormField(nestFieldName);

JSONObject json = null;
JSONArray mapAry = new JSONArray();
String filter = "";
try {
	// 20131123 fgf 添加
	String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
	json = new JSONObject(defaultVal);
	nestFormCode = json.getString("destForm");
	filter = json.getString("filter");
	mapAry = (JSONArray)json.get("maps");
} catch (JSONException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
	return;
}

String formCode = json.getString("sourceForm");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", "表单不存在！");
	out.print(jsonRet.toString());
	return;
}

String userName = privilege.getUserName(skey);

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(userName)) {
	jsonRet.put("res", "-1");
	jsonRet.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
	out.print(jsonRet.toString());
	return;
}

if (op.equals("selBatch")) {
    DeptUserDb dud = new DeptUserDb();
    String unitCode = dud.getUnitOfUser(userName).getCode();
        
	FormDb nestFd = new FormDb();
	nestFd = nestFd.getFormDb(nestFormCode);
	
	int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
	
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary!=null) {
		// 取出待插入的数据
		for (int i=0; i<ary.length; i++) {
			long id = StrUtil.toLong(ary[i]);
			fdao = fdao.getFormDAO(id, fd);
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(nestFormCode);
			// String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fields = msd.getColAry(false, "list_field");
			
			int len = 0;
			if (fields!=null)
				len = fields.length;
						
			// 根据映射关系赋值
			JSONObject jsonObj2 = new JSONObject();
			for (int k=0; k<mapAry.length(); k++) {
				try {
					JSONObject jsonObj = mapAry.getJSONObject(k);
					String sfield = (String) jsonObj.get("sourceField");
					String dfield = (String) jsonObj.get("destField");
					
					String fieldValue = fdao.getFieldValue(sfield);
					if (sfield.equals(com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID) || sfield.equals("FormDAO_ID")) {
						fieldValue = String.valueOf(fdao.getId());
					}
					
					fdaoNest.setFieldValue(dfield, fieldValue);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			
            fdaoNest.setFlowId(flowId);			
			fdaoNest.setCwsId(String.valueOf(parentId));
			fdaoNest.setCreator(userName);

			fdaoNest.setUnitCode(unitCode);
			fdaoNest.setCwsQuoteId((int)id);
			fdaoNest.setCwsParentForm(parentFormCode);
			re = fdaoNest.create();
		}
		
		jsonRet.put("res", "0");
		
		String cwsId = String.valueOf(parentId);				
		jsonRet.put("sums", FormUtil.getSums(nestFd, pForm, cwsId));		
	}
	else {
		jsonRet.put("res", "0");
		jsonRet.put("msg", "请选择记录！");
	}
	out.print(jsonRet);
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

// 过滤条件
// System.out.println(getClass() + " filter=" + filter);
String conds = filter; // ParamUtil.get(request, "filter");
if (conds.equals("none"))
	conds = "";

String action = ParamUtil.get(request, "action");
		
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

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
        if (fieldName.equals("cwsCurUser") || fieldName.equals("curUser") 
         	|| fieldName.equals("curUserDept") || fieldName.equals("curUserRole") || fieldName.equals("admin.dept")) {
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

// String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";

// 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, "search", orderBy, sort);
String sql = ary[0];

// out.print(sql);

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");

result.put("totalCount", total);
result.put("filter", filter);

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
}

out.print(jsonRet);
%>
