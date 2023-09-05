<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%--
- 功能描述：明细表
- 访问规则：在宏控件中生成
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2015.2.22
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
--%>
<%
response.setHeader("X-Content-Type-Options", "nosniff");
response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
response.setContentType("text/javascript;charset=utf-8");

Privilege pvg = new Privilege();
int flowId = ParamUtil.getInt(request, "flowId", -1);
String nestFieldName = ParamUtil.get(request, "nestFieldName"); // 明细表控件字段名
long cwsId = ParamUtil.getLong(request, "cwsId", -1);
String pageType = ParamUtil.get(request, "pageType");
int workflowActionId = ParamUtil.getInt(request, "workflowActionId", -1);
//保存所有计算控件
List<FormField> calculators = new ArrayList<FormField>();

// System.out.println(getClass() + " " + ary + " desc=" + desc + " nestFieldName=" + nestFieldName + " flowId=" + flowId + " len=" + len);
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
FormField formField = fd.getFormField(nestFieldName);

boolean canAdd = true, canEdit = true, canImport = true, canExport=true, canDel = true, canSel = true;
boolean isAutoSel = false;

String parentFormCode = ParamUtil.get(request, "parentFormCode");
JSONObject json = null;
JSONArray mapAry = new JSONArray();
int queryId = -1;
if (!nestFieldName.equals("")) {
	FormDb parentFd = new FormDb();
	parentFd = parentFd.getFormDb(parentFormCode);
	FormField nestField = parentFd.getFormField(nestFieldName);
	if (nestField==null) {
		LogUtil.getLog(getClass()).warn("父表单（" + parentFormCode + "）中的嵌套表字段：" + nestFieldName + " 不存在");
		return;
	}
	try {
		String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
		json = new JSONObject(defaultVal);
		try {
			canAdd = "true".equals(json.getString("canAdd"));
			canEdit = "true".equals(json.getString("canEdit"));
			canImport = "true".equals(json.getString("canImport"));
			canDel = "true".equals(json.getString("canDel"));
			canSel = "true".equals(json.getString("canSel"));
			if (json.has("isAutoSel")) {
				isAutoSel = "1".equals(json.getString("isAutoSel"));
			}
			if (json.has("canExport")) {
				canExport = "true".equals(json.getString("canExport"));
			}
			// DebugUtil.i("macro_detaillist_ctl_js.jsp", "", json.toString());
		}
		catch (Exception e) {
		}		
		if (!json.isNull("maps"))
			mapAry = (JSONArray)json.get("maps");
		if (!json.isNull("queryId"))
			queryId = StrUtil.toInt((String)json.get("queryId"));
	} catch (JSONException e) {
		// e.printStackTrace();
	}	
}

boolean isEditable = ParamUtil.getBoolean(request, "isEditable", true);
if (!isEditable) {
	canAdd = false;
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);
Leaf lf = new Leaf();
lf = lf.getLeaf(wf.getTypeCode());

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
// String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

int len = 0;
if (fields!=null)
	len = fields.length;

boolean isWritable = true;
boolean isOptEnable = false;

MacroCtlMgr mm = new MacroCtlMgr();

Map fieldWriteMap = null;
Map fieldHideMap = null;

String[] fieldWriteAry = null;
String[] fieldHideAry = null;
if ("flow".equals(pageType)) {
	WorkflowActionDb wad = new WorkflowActionDb();
	wad = wad.getWorkflowActionDb(workflowActionId);
	
    WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
    wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

    if (lf.getType()==Leaf.TYPE_FREE) {
    	fieldWriteAry = wfpd.getFieldsWriteOfUser(wf, pvg.getUser(request));
    }
    else {
		String fieldWrite = StrUtil.getNullString(wad.getFieldWrite()).trim();
		String fieldHide = StrUtil.getNullString(wad.getFieldHide()).trim();
					
		fieldWriteAry = StrUtil.split(fieldWrite, ",");
		fieldHideAry = StrUtil.split(fieldHide, ",");
    }
    
	fieldWriteMap = new HashMap();
	if (fieldWriteAry!=null) {
		boolean isFound = false;
		for (int i=0; i<fieldWriteAry.length; i++) {
			String f = fieldWriteAry[i];
			if (f.equals(nestFieldName)) {
				isFound = true;
			}
			if (f.startsWith("nest.")) {
				f = f.substring("nest.".length());
				fieldWriteMap.put(f, "");
			}			
		}
		if (!isFound) {
			isWritable = false;
		}
	}

	fieldHideMap = new HashMap();
	if (fieldHideAry!=null) {
		for (int i=0; i<fieldHideAry.length; i++) {
			String f = fieldHideAry[i];
			if (f.startsWith("nest.")) {
				f = f.substring("nest.".length());
				fieldHideMap.put(f, "");
			}			
		}
	}
}
%>

var mapCalculate_<%=nestFieldName%> = new Map();
<%
for (int i=0; i<len; i++) {
	String fName = fields[i];
	String title = "创建者";
	FormField ff = null;
	if (fName.startsWith("main:")) {
		String[] subFields = fName.split(":");
		if (subFields.length == 3) {
			// 特别处理明细表中的main,如果子表的关联表不是当前主表则不显示
			if (parentFormCode.equals(subFields[1])) {
				FormDb subfd = new FormDb(subFields[1]);
				title = subfd.getFieldTitle(subFields[2]);
				ff = subfd.getFormField(subFields[2]);
			}
		}
	} else if (fName.startsWith("other:")) {
		String[] otherFields = StrUtil.split(fName, ":");
		if (otherFields.length>=8) {
			FormDb oFormDb = new FormDb(otherFields[5]);
			title = oFormDb.getFieldTitle(otherFields[7]);
			ff = oFormDb.getFormField(otherFields[7]);
		} else {
			FormDb otherFormDb = new FormDb(otherFields[2]);
			title = otherFormDb.getFieldTitle(otherFields[4]);
			ff = otherFormDb.getFormField(otherFields[4]);
		}				
	} else if (!fName.equals("cws_creator")) {
		title = fd.getFieldTitle(fName);
		ff = fd.getFormField(fName);
	}
	
	if (ff==null) {
		continue;
	}
	// 记录明细表中的计算控件
	if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
		// String formula = "formula=\"" + ff.getDefaultValueRaw() + "\"";
		%>
		mapCalculate_<%=nestFieldName%>.put("<%=ff.getName()%>", "<%=ff.getDefaultValueRaw()%>");
		<%
	}	
}%>

function sel_<%=nestFieldName%>(parentId, isQuery) {
    if (isQuery) {
        openWin("<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?op=query&id=<%=queryId%>&mode=sel&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=detaillist&parentId=" + parentId, 800, 600);
    }
    else {
        openWin("<%=request.getContextPath()%>/visual/moduleListNestSel.do?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=detaillist&parentId=" + parentId, 800, 600);
    }
}

function importExcel(formCode, nestFieldName) {
	openWin("<%=request.getContextPath()%>/visual/nest_table_import_excel.jsp?formCode=" + formCode + "&nestType=detaillist&nestFieldName=" + nestFieldName, 480, 80);
}

$(function () {
    // Initialize appendGrid
    $('#detaillist_<%=nestFieldName%>').appendGrid({
        // caption: 'detaillist',
        i18n: {
            append: '添加新行',
            rowDrag: '拖动',
            removeLast: '删除最后一行',
            insert: '添加一行在上方',
            moveUp: '上移',
            moveDown: '下移',
            remove: '删除'
        },
        
        initRows: 1,
        rowDragging: true,
        columns: [
				<%        
                for (int i=0; i<len; i++) {
                    String fName = fields[i];
                    String title = "创建者";
                    FormField ff = null;
                	if (fName.startsWith("main:")) {
                		String[] subFields = fName.split(":");
                		if (subFields.length == 3) {
                			if (parentFormCode.equals(subFields[1])) {
                				FormDb subfd = new FormDb(subFields[1]);
                				title = subfd.getFieldTitle(subFields[2]);
                				ff = subfd.getFormField(subFields[2]);
                			}
                		}
                	} else if (fName.startsWith("other:")) {
                		String[] otherFields = StrUtil.split(fName, ":");
                		if (otherFields.length>=8) {
                			FormDb oFormDb = new FormDb(otherFields[5]);
                			title = oFormDb.getFieldTitle(otherFields[7]);
                			ff = oFormDb.getFormField(otherFields[7]);
                		} else {
                			FormDb otherFormDb = new FormDb(otherFields[2]);
                			title = otherFormDb.getFieldTitle(otherFields[4]);
                			ff = otherFormDb.getFormField(otherFields[4]);
                		}	
        			} else if (!fName.equals("cws_creator")) {
                		title = fd.getFieldTitle(fName);
                		ff = fd.getFormField(fName);
                	}
					
					if (ff==null) {
						continue;
					}

                    String macroType = "";
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        macroType = ff.getMacroType();
                    }
					
					// System.out.println(getClass() + " fName=" + fName);
					// System.out.println(getClass() + " fName=" + fName + " ff.getType()=" + ff.getType());
						
					String type = "text";
					String ctrlOptions = "";
					String defaultValue = ff.getDefaultValue();
					String uiOption = "";
					
					if (ff.getType().equals(FormField.TYPE_SELECT)) {
						type = "select";
						
						ctrlOptions = ", ctrlOptions: {";
						String[][] ary = FormParser.getOptionsArrayOfSelect(fd, ff);
						int optLen = ary.length;
						for (int j=0; j<optLen; j++) {
							if (j==0) {
								ctrlOptions += "'" + ary[j][1] + "':'" + ary[j][0] + "'";
							}
							else {
								ctrlOptions += ", '" + ary[j][1] + "':'" + ary[j][0] + "'";
							}
						}
						ctrlOptions += "}";
					}
					else if (ff.getType().equals(FormField.TYPE_MACRO)) {
						// type = "select";
												
						ctrlOptions = ", ctrlOptions: {";
						MacroCtlUnit mcu = mm.getMacroCtlUnit(ff.getMacroType());
						type = mcu.getIFormMacroCtl().getControlType();
						
						if (mcu.getVersion()==1) {
							defaultValue = "";
						}
						
						if ("select".equals(type)) {
							IFormMacroCtl imc = mcu.getIFormMacroCtl();
							String aryStr = imc.getControlOptions(pvg.getUser(request), ff);
							if ("".equals(aryStr)) {
								aryStr = "[]";
							}
							JSONArray jsonAry = new JSONArray(aryStr);
							for (int j=0; j<jsonAry.length(); j++) {
								JSONObject jsonObj = null;
								try {
									jsonObj = jsonAry.getJSONObject(j);
									String name = (String) jsonObj.get("name");
									String value = (String) jsonObj.get("value");
									if (j==0) {
										ctrlOptions += "'" + value + "':'" + name + "'";
									}
									else {
										ctrlOptions += ", '" + value + "':'" + name + "'";
									}
								} catch (JSONException ex) {
									ex.printStackTrace();
								}
							}						
							ctrlOptions += "}";
						}
						else if ("userSelect".equals(type)) {
							ctrlOptions += "}";
						}
						else {
							type = "text";
							ctrlOptions += "}";
						}
					}
					else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
						type = "checkbox";
						// System.out.println(getClass() + " " + ff.getDefaultValue());
						if ("1".equals(ff.getDefaultValue())) {
							defaultValue = "true";
						}
						else {
							defaultValue = ""; // false";
						}
					}
					else if (ff.getType().equals(FormField.TYPE_DATE)) {
						type = "datepicker";
						//uiOption = ", uiOption: { dateFormat: 'yy-mm-dd'}";
						if (ff.getDefaultValueRaw().equals("CURRENT")) {
							defaultValue = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
						}
					}
					else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
						type = "datetimepicker";
						//uiOption = ", uiOption: { dateFormat: 'yy-mm-dd'}";
						if (ff.getDefaultValueRaw().equals("CURRENT")) {
							defaultValue = DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss");
						}
					}
					
					String ctrlCss = "";
					if (!ff.getType().equals(FormField.TYPE_CHECKBOX)) {
						if (!fieldsWidth[i].equals("#")) {
							String w = fieldsWidth[i];
							ctrlCss = ", ctrlCss: { width: '" + w + "px'}";
						}
						else {
							ctrlCss = ", ctrlCss: { width: '100px' }"; // 或者也可以用100%
						}
					}
					
					// 是否可见
					String invisibleStr = "";
					if (fieldHideMap!=null && fieldHideMap.containsKey(ff.getName())) {
						invisibleStr = ", invisible: true";
					}
					
					// 是否可写
					String disabledStr = "";
					if (!canEdit || !isWritable || (fieldWriteMap!=null && !fieldWriteMap.containsKey(ff.getName()))) {
						disabledStr = ", ctrlAttr: { disabled: 'disabled' }"; // disabled后表单域不能被submit
					} else {
						if (!isOptEnable) {
							isOptEnable = true;
						}
					}
					
					String onchangeStr = "";
					if (!ff.getType().equals(FormField.TYPE_CALCULATOR)) {
						onchangeStr = ", onChange: handleChange_" + nestFieldName;
					}
					//记录计算控件
					if(ff.getType().equals(FormField.TYPE_CALCULATOR)){
						calculators.add(ff);
					}
					String valueStr = "";
					if (!"".equals(defaultValue)) {
						valueStr = ", value:'" + defaultValue + "'";
					}
                %>
                    { name: '<%=ff.getName()%>', resizable: true, display: '<%=ff.getTitle()%>', type: '<%=type%>' <%=ctrlOptions%> <%=ctrlCss%> <%=uiOption%> <%=invisibleStr%> <%=disabledStr%> <%=onchangeStr%> <%=valueStr%>}
                    <%if (i<len-1) {%>,<%}%>
                <%}%>
                ,{ name: 'id', display: 'ID', type:'text', invisible: true}
            ]
            <%if (!"".equals(formCode)) { // 非直接打开页面测试时
				String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + cwsId;
				sql += " order by cws_order";
				
				// System.out.println(getClass() + " sql=" + sql);
								
				com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
				Vector vt = fdao.list(formCode, sql);
				Iterator ir = vt.iterator();		
						
				JSONArray rowAry = new JSONArray();
				UserMgr um = new UserMgr();
				while (ir!=null && ir.hasNext()) {
					fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
					long id = fdao.getId();
					JSONObject jsonObj = new JSONObject();
					for (int i=0; i<len; i++) {
						String fName = fields[i];
						FormField ff = null;
						if (fName.startsWith("main:")) {
	                		String[] subFields = fName.split(":");
	                		if (subFields.length == 3) {
	                			if (parentFormCode.equals(subFields[1])) {
	                				FormDb subfd = new FormDb(subFields[1]);
	                				ff = subfd.getFormField(subFields[2]);
	                			}
	                		}
	                	} else if (fName.startsWith("other:")) {
	                		jsonObj.put(fName, com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName));
						} else {
							ff = fd.getFormField(fName);
							String val;
							if (!nestFieldName.equals("cws_creator")) {
								if (ff.getType().equals(FormField.TYPE_MACRO)) {
									val = fdao.getFieldValue(fName);
									IFormMacroCtl imc = mm.getMacroCtlUnit(ff.getMacroType()).getIFormMacroCtl();
									String valRealShow = imc.converToHtml(request, ff, fdao.getFieldValue(fName));
									jsonObj.put(fName + "_realshow", valRealShow);
								} else {
									val = fdao.getFieldValue(fName);
								}
							}else{
								val = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
							}
							jsonObj.put(fName, val);
	                	}
					}
					jsonObj.put("id", String.valueOf(id));
					rowAry.put(jsonObj);
                }
                %>
				<%if (rowAry.length()>0) {%>
                ,initData: <%=rowAry%>
                <%}%>
            <%}%>
            ,customFooterButtons: [
            <%
            boolean isToken = false;
            if (isOptEnable && canImport) {
            	isToken = true;
            %>
            {
                uiButton: { icons: { primary: 'import_icons' }},
                btnCss: { 'color': '#262626','height': '24px','font-size': '14px' },
                btnAttr: { title: '导入' },
                click: function (evt) {
                    importExcel("<%=formCode%>", "<%=nestFieldName%>");
                },
                atTheFront: false
            }
            <%}
			if (canExport) {
	            if (isToken) {
	            	out.print(",");
	            }			
				isToken = true;
			%>
            {
                uiButton: {icons: { primary: 'export_icons' }},
                btnCss: { 'color': '#262626','height': '24px','font-size': '14px' },
                btnAttr: { title: '导出' },
                click: function (evt) {
					openWin('<%=request.getContextPath()%>/visual/exportExcelRelate.do?parentId=<%=cwsId%>&formCode=<%=ParamUtil.get(request, "parentFormCode")%>&moduleCodeRelated=<%=formCode%>');
                },
                atTheFront: false
            }
            <%}
            %>
			<%if (canSel && queryId!=-1) {
	            if (isToken) {
	            	out.print(",");
	            }			
				isToken = true;
			%>
                {
                    uiButton: { icons: { primary: 'select_icons' }},
                     btnCss: { 'color': '#262626','height': '24px','font-size': '14px' },
                    btnAttr: { title: '选择' },
                    click: function (evt) {
                        sel_<%=nestFieldName%>(<%=cwsId%>, true);
                    }
                }        
            <%}
            %>
            <%if (canSel && mapAry.length()>0) {
                if (isToken) {
            		out.print(",");
            	}
				isToken = true;            	
            %>
                {
                    uiButton: {icons: { primary: 'select_icons' }},
                    btnCss: { 'color': '#262626','height': '24px','font-size': '14px' },
                    btnAttr: { title: '选择' },
                    click: function (evt) {
                        sel_<%=nestFieldName%>(<%=cwsId%>);
                    }
                }        
            <%}%>
        ],
        <%
		if (!isOptEnable) {
			%>
        hideButtons: {
        	<%if (!canAdd) { %>        
            append: true,
			<%} %>     
        	<%if (!canDel) { %>
        	remove: true,
        	removeLast: true,        	
        	<%} %>   			       
            insert: true,
            moveUp: true,
            moveDown: true,
            remove: true,
            removeLast: true
        },
			<%
		}
		else {
		%>
        hideButtons: {
        	<%if (!canAdd) { %>
        	append: true,
        	<%} %>
        	<%if (!canDel) { %>
        	remove: true,
        	removeLast: true,
        	<%} %>        	
            insert: true,
            moveUp: true,
            moveDown: true
        },
		<%
		}
		%>
            rowDataLoaded: function (caller, record, rowIndex, uniqueIndex) {
            	// var checkBoxCtl = $(this).appendGrid('getCellCtrl', 'dt', rowIndex);
                // alert(checkBoxCtl.tagName);
                // $(checkBoxCtl).attribute("checked", "checked");
            },
			afterRowRemoved:function(tbWhole, rowIndex){
                callDeleteCalculate_<%=nestFieldName%>(null, rowIndex);
            },
			dataLoaded:function(tbWhole, records) {
				// 更新对嵌套表的列进行sum操作的主表单中的控件
				$("input[kind='CALCULATOR']").each(function(){
					var calObj = $(this);
					var digit = calObj.attr('digit');
					var isRoundTo5 = calObj.attr('isRoundTo5');
					calculateSum($(this), '<%=nestFieldName%>');
				});
			}
    });
});

/*jQuery-ui日历汉化*/
jQuery(function(){
	$.datepicker.regional['zh-CN']={
		closeText:'关闭',
		prevText:'&#x3C;上月',
		nextText:'下月&#x3E;',
		currentText:'今天',
		monthNames:['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort:['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
		dayNames:['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort:['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin:['日','一','二','三','四','五','六'],
		weekHeader:'周',
		dateFormat:'yy-mm-dd',
		firstDay:1,
		isRTL:false,
		showMonthAfterYear:true,
		yearSuffix:'年'
	};
	$.datepicker.setDefaults($.datepicker.regional['zh-CN']);
});

function handleChange_<%=nestFieldName%>(evt, rowIndex) {
    callCalculate_<%=nestFieldName%>(evt, rowIndex);
}

function callCalculate_<%=nestFieldName%>(evt, rowIndex) {
	 //alert('Selected Value = ' + evt.target.value);
    var elements = mapCalculate_<%=nestFieldName%>.getElements();
    for (i = 0; i < elements.length; i++) {
    	var colName = elements[i].key;
		var v = getCalculateCellValue_<%=nestFieldName%>("<%=nestFieldName%>", rowIndex, elements[i].value, colName);
        $('#detaillist_<%=nestFieldName%>').appendGrid('setCtrlValue', colName, rowIndex, v);
    }
    	
	// 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        var digit = calObj.attr('digit');
    	var isRoundTo5 = calObj.attr('isRoundTo5');
    	calculateSum($(this), '<%=nestFieldName%>');
	});
}

function callDeleteCalculate_<%=nestFieldName%>(evt, rowIndex) {
	// 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        var digit = calObj.attr('digit');
    	var isRoundTo5 = calObj.attr('isRoundTo5');
    	calculateSum($(this), '<%=nestFieldName%>');
	});
}

function calculateSum($obj, nestFieldName) {
        if ($obj.attr('formula')) {
           var formula = $obj.attr('formula');    
           var isSum = false;
           var regStr = /(sum\(([\w|\.]+)\))/gi;
           var mactches = formula.match(regStr)
           var len = 0;
           if (mactches) {
                len = mactches.length;
                isSum = true;
           }
           if (isSum) {
               // 累加列 
               // alert(mactches[0]);
               var field = RegExp.$2;
               if (field.indexOf("nest.")==0) {
                    var p = field.indexOf(".");
                    // 取得列名
                    field = field.substring(p+1);
                    // 当数据列有更新时，更新计算控件的值
                    // 计算
                    var rowCount = $('#detaillist_' + nestFieldName).appendGrid('getRowCount');                    
                    var v = 0;
                    for (var i=0; i < rowCount; i++) {
                        var cellV = $('#detaillist_' + nestFieldName).appendGrid('getCtrlValue', field, i);
                        if(cellV == null){
                        	return;
                        }
                        if (cellV!="") {
                            if (!isNaN(cellV))
                                v += eval(cellV);
                        }
                    }
                    var digit = $obj.attr('digit');
    				var isRoundTo5 = $obj.attr('isRoundTo5');
                    try {
                        //判断是否四舍五入 1：是 0：否    
                        if (isRoundTo5 != null && isRoundTo5 == 1){
                            var digitNum = parseFloat(digit);
                            if (!isNaN(digitNum))
                            {
                                v = v.toFixed(digitNum);
                            }
                            
                        }
                        else if(isRoundTo5 != null && isRoundTo5 == 0){
                            var digitNum = parseFloat(digit);
                            if (!isNaN(digitNum))
                            {
                            	v = v.toFixed(digitNum + 1);
                                v = changeTwoDecimal_f(v,digitNum);
                            }
                        } else {
                        	v = v.toFixed(2);
                        }
                       } catch (e) {
                       	v = v.toFixed(2);
                       	}   
                    $obj.val(v);
               }
           }
        }
}

function getCalculateCellValue_<%=nestFieldName%>(nestFieldName, rowIndex, formula,colName) {          
    var ary = getSymbolsWithBracket(formula);
    for (var i=0; i < ary.length; i++) {
        if (!isOperator(ary[i])) {
            var v = $('#detaillist_' + nestFieldName).appendGrid('getCtrlValue', ary[i], rowIndex);
                        
            if (v=="")
                ary[i] = 0;
            else if (isNaN(v))
                ary[i] = 0;
            else
                ary[i] = v;
        }
    }
    formula = "";
    for (var i=0; i < ary.length; i++) {
        formula += ary[i];
    }
	// 需四舍五入，否则数字看起来会比较乱，如出现：60*21.76=1305.6000000000001的情况
    // return eval(formula);
    return roundNum_<%=nestFieldName%>(eval(formula),colName);
}

function roundNum_<%=nestFieldName%>(num,colName) {
	var v= 0 ;
	<%
		for(FormField ff : calculators){
			
	%>
		if (colName == '<%=ff.getName() %>'){
			<%
				FormParser fp = new FormParser();
				String cal_digit = fp.getFieldAttribute(fd,ff,"digit");
				String cal_isRoundTo5 = fp.getFieldAttribute(fd,ff,"isRoundTo5");
			%>
			var digit = '<%=cal_digit %>';
			var isRoundTo5 = '<%=cal_isRoundTo5 %>';
			
			 try {
	                    //判断是否四舍五入 1：是 0：否    
				    	if (isRoundTo5 != null && isRoundTo5 == 1){
					    	var digitNum = parseFloat(digit);
					    	if (!isNaN(digitNum))
							{
								v = num.toFixed(digitNum);
							}
					    	
					    }
					    else if(isRoundTo5 != null && isRoundTo5 == 0){
					    	var digitNum = parseFloat(digit);
					    	var isNum = parseFloat(num);
					    	if (!isNaN(digitNum) && !isNaN(isNum))
							{
								num = num.toFixed(digitNum + 1);
								v = changeTwoDecimal_f(num,digitNum);
							}
					    }
				} catch (e) {}
			}  	
				
			
	<%	}
	
	%>
    return v;
    //var vv = Math.pow(10,v);
    //return Math.round(num*vv)/vv;
}

function clearDetailList(nestFieldName) {
		// $('#detaillist_' + nestFieldName).appendGrid('removeEmptyRows');
	    var rowCount = $('#detaillist_' + nestFieldName).appendGrid('getRowCount');
        for (i=0; i < rowCount; i++) {
            $('#detaillist_' + nestFieldName).appendGrid('removeRow', i);
        }
}

// 或从module_list_nest_sel.jsp选择数据后调用
function insertRow(formCode, jsonAry, nestFieldName) {
	// 如果jsonAry直接传过来的是json数组格式， 在ie8、9下无法插入
	jsonAry = eval("(" + jsonAry + ")");
    $('#detaillist_' + nestFieldName).appendGrid('appendRow', jsonAry);
    
    // 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        var digit = calObj.attr('digit');
    	var isRoundTo5 = calObj.attr('isRoundTo5');
    	calculateSum($(this), nestFieldName);
	});
}

function SelectNewDate(ObjName,FormatDate) {
	var scFormat = false;
	try {
		if (FormatDate == "yyyy-MM-dd") {
			$('#' + ObjName).datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:false,
				format:'Y-m-d'
			});
		} else {
			scFormat = true;
			$('#' + ObjName).datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:true,
				format:'Y-m-d H:i:00',
				step:10
			});
		}
	} catch (e) {
		
	}
}
