<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="java.util.regex.*"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%--
- 功能描述：嵌套表2所需的js文件
- 访问规则：在宏控件中写入页面
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2013.8.1
- 修改原因：使支持隐藏字段
- 修改点：
--%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");
%>
// 支持分页功能后，此方法仅用于智能模块添加时
function callCalculateOnload() {
	// 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        if ($(this).attr('formula')) {
           	var formula = $(this).attr('formula');
			var formCode = $(this).attr('formCode');
			var digit = $(this).attr('digit');
			var isRoundTo5 = $(this).attr('isRoundTo5');

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
               var field = RegExp.$2;
               if (field.indexOf("nest.")==0) {
               		var p = field.indexOf(".");
					// 取得列名
                    field = field.substring(p+1);
                    var sumField = $(this);
                    var v = 0;
                    // 遍历所有的嵌套表格2，对匹配到的列求和
					var $nestTable;
					if (formCode != null) {
						$nestTable = $("[id^=cwsNestTable_]");
					}
					else {
						$nestTable = $("[id^=cwsNestTable_][formCode=" + formCode + "]");
					}
					$nestTable.each(function() {
                    	v = 0;
                        var obj = $(this)[0]; // table
                        var rows = obj.rows.length;
                        var colIndex = getCellIndexByFieldNameOfNestSheet(obj, field);
                        if (colIndex!=-1) {
                            // alert(colIndex + " " + field);
                            for (var i=1; i < rows; i++) {
                                var cellV = getCellValueOfNestSheet(obj, i, colIndex).trim();
                                if (cellV!="") {
                                    if (!isNaN(cellV))
                                        v += eval(cellV);
                                }
                            }
                        }
                        else {
                        	// 页面没有全部加载完成时，可能会报不存在
                        	// alert("算式" + formula + "中 " + field + " 不存在！");
                        	if (isIE9 || isIE10 || isIE11) {
                        		console.log("算式" + formula + "中 " + field + " 不存在！");
                            }
                            return; // 以免误算nesttable中的累加列
                        }    
                        try  {
							// v = v.toFixed(2);
							if (isRoundTo5 != null && isRoundTo5 == 1){
								var digitNum = parseFloat(digit);
								if (!isNaN(digitNum)){
									v = v.toFixed(digitNum);
								}
							}
							else if(isRoundTo5 != null && isRoundTo5 == 0){
								var digitNum = parseFloat(digit);
								if (!isNaN(digitNum)) {
									v = v.toFixed(digitNum + 1);
									v = changeTwoDecimal_f(v, digitNum);
								}
							}
                        }
                        catch (e) {}
                        sumField.val(v);
                    });
               }
           }
		}
	});
}
// i从1开始
<%
   
	String fieldName = ParamUtil.get(request,"fieldName");
	
	String nestFormCode = ParamUtil.get(request, "nestFormCode");
	
	int isTab = ParamUtil.getInt(request, "isTab", 0);
	int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);

	String op = ParamUtil.get(request, "op");
	if (op.equals("forRefresh")) {
		String pageType = ParamUtil.get(request, "pageType");
        String path = ParamUtil.get(request, "path");
		%>
		// 如果是智能模块添加页面，则重新加载newIds中的记录，否则刷新后显示第一页
        function refreshNestSheetCtl<%=nestFormCode%>(newIds){  
                var path = '<%=path %>';
				path = (path.indexOf("?")!=-1)? path += "&"+new Date().getTime() : path += "?"+new Date().getTime()

                var fieldName = '<%=fieldName %>';
                
                var strNewIds = "";
                if (newIds!=null) {
                	strNewIds = newIds;
                }
            
                 $.ajax({
                    type: "post",
                    url: path,
					data : {
						newIds: strNewIds
					},                    
                    success: function(data, status){
                    	<%if (isTab==0) { %>
                        $("#nestsheet_"+fieldName).html(data);
                        <%}else{ %>
                        // 流程中选项卡式显示方式
                        $("#tabs-<%=nestFormCode %>").html(data);                        
                        <%} %>
                        // callCalculateOnload();
						try {
							onNestSheetRefresh_<%=nestFormCode%>();
						} catch (e) {};
                    },
                    error: function(XMLHttpRequest, textStatus){
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });            
        }		
		<%
		boolean isAutoSel = false;
		String filter = "";
		String parentFormCode = ParamUtil.get(request, "parentFormCode");
		String nestFieldName = fieldName;	

		// 在DetailListCtl中也会引用此文件用于显示明细表内容（报表状态，只读），而此时未传parentFormCode，所以需判断一下
		if (!"".equals(nestFieldName) && !"".equals(parentFormCode)) {
			FormDb parentFd = new FormDb();
			parentFd = parentFd.getFormDb(parentFormCode);
			if (!parentFd.isLoaded()) {
				DebugUtil.e("macro_js_nestsheet.jsp", "表单", "不存在，编码：" + parentFormCode);
				return;
			}
			FormField nestField = parentFd.getFormField(nestFieldName);
			if (nestField==null) {
				return;
			}
			try {
				String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
				JSONObject json = new JSONObject(defaultVal);
				// System.out.println(getClass() + " json=" + json);
				try {
					if (json.has("isAutoSel")) {
						isAutoSel = "1".equals(json.getString("isAutoSel"));
					}					
					nestFormCode = json.getString("destForm");
					filter = json.getString("filter");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
			}	
		}		
		
		if (isAutoSel) {
			// wumeng,为了获取主表单中的值,解析配置条件中的主表所配置的字段并获取签到表的表单编码用于刷新表格
			String mainFormFieldNames="";
			Pattern p = Pattern.compile("\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
			Matcher m = p.matcher(filter); // fileter
			while (m.find()) {
				String fName = m.group(1);
				if ("cwsCurUser".equals(fName) || "curUser".equals(fName) || "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || fieldName.equals("admin.dept")) {
					continue;
				}
				// 当条件为包含时，fieldName以@开头
				if (fName.startsWith("@")){
					fName = fName.substring(1);
				}
				if(mainFormFieldNames.length()==0){
					mainFormFieldNames = fName;
				}else{
					mainFormFieldNames += ","+ fName;
				}
			}
			String mainId = ParamUtil.get(request, "mainId");
		%>
			$(function() {
				// 自动拉单功能
				var isAutoSel = <%=isAutoSel%>;
				if(isAutoSel){	//如果是自动嵌套拉单，每隔两秒执行检查是否改刷新表格
					var fields = '<%=mainFormFieldNames%>';
					var fieldNames = fields.split(",");
					var oldValueArr = getFormValue(fieldNames);
					var ajaxData = {
						'flowId':'<%=flowId%>',
						'parentFormCode':'<%=parentFormCode%>',
						'parentId':'<%=mainId%>',
						'nestFieldName':'<%=nestFieldName%>',
						'isFirst':true
					};
					for (var i in fieldNames) {
						ajaxData[fieldNames[i]] = oldValueArr[i];
					}
					
					<%
					// 如果是模块编辑，则不需要自动拉单，否则将只显示拉单结果，而无法显示原来的数据
					if (!"edit".equals(pageType)) {
					%>
					ajaxPost('<%=request.getContextPath()%>/nestsheetctl/autoSel.do',ajaxData,function(da){
						if (!isJson(da)) {
							da = $.parseJSON(da);
						}
						if(da.ret == '1') {
							var newIds = da.newIds;
							if (newIds==null) {
								newIds = "";
							}
							refreshNestSheetCtl<%=nestFormCode%>(newIds);
							<%if (flowId == com.redmoon.oa.visual.FormDAO.NONEFLOWID) {%>
							if (newIds!="") {
								var idsAry = newIds.split(",");
								for (x in idsAry) {
									addTempCwsId("<%=nestFormCode%>", idsAry[x]);									
								}								
							}
							<%}%>
						}
					},false);
					<%} %>
								
					if(fields!=''){ // 配置了条件,没有配置条件则不处理
						window.setInterval(function(){ // 5秒进行自动检测一次
							// 从新获取获取的条件的值，如果变化了，
							var newValueArr = getFormValue(fieldNames);
							if(JSON.stringify(oldValueArr) != JSON.stringify(newValueArr)){
								oldValueArr = getFormValue(fieldNames);
								var ajaxData = {
									'flowId':'<%=flowId%>',
									'parentFormCode':'<%=parentFormCode%>',
									'parentId':'<%=mainId%>',
									'nestFieldName':'<%=nestFieldName%>',
									'isFirst':false
								};
								for (var i in fieldNames) {
									ajaxData[fieldNames[i]] = newValueArr[i];
								}
								ajaxPost('<%=request.getContextPath()%>/nestsheetctl/autoSel.do',ajaxData,function(da){
									if (!isJson(da)) {
										da = $.parseJSON(da);
									}
									if(da.ret == '1'){
										var newIds = da.newIds;	
										if (newIds==null) {
											newIds = "";
										}
										refreshNestSheetCtl<%=nestFormCode%>(newIds);
										<%if (flowId == com.redmoon.oa.visual.FormDAO.NONEFLOWID) {%>
										if (newIds!="") {
											var idsAry = newIds.split(",");
											for (x in idsAry) {
												addTempCwsId("<%=nestFormCode%>", idsAry[x]);									
											}
										}
										<%}%>
									}
								},false);
							}
						},200);
					}
				}
			});
			
			// 获取主表单中的值并拼接成sql的where字句
			function getFormValue(fieldNames){
				var arr = new Array();
				for(var i = 0;i < fieldNames.length;i++){
					if (o(fieldNames[i])) {
						arr[i] =  o(fieldNames[i]).value;
					}
				}
				return arr;
			}
			// 写出公共方法用于调用
			function ajaxPost(path,parameter,func,asy){
				$.ajax({
					type: "post",
					url: path,
					data: parameter,
					async:asy,
					dataType: "html",
					contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
					beforeSend: function(XMLHttpRequest){
						if(!asy){
							$(document.body).showLoading();
						}
					},	
					success: function(data, status){
						func(data);
						if(!asy){
							$(document.body).hideLoading();
						}
					},
					error: function(XMLHttpRequest, textStatus){
						if(!asy){
							$(document.body).hideLoading();
						}
						alert(XMLHttpRequest.responseText);
					}
				});
			}			
		<%
		}
		
		return;
	}
%>
function getCellValueOfNestSheet(tableObj, i, j) {
	var cel = tableObj.rows.item(i).cells;
	var fieldType = tableObj.rows[0].cells[j].getAttribute("type");
	var macroType = tableObj.rows[0].cells[j].getAttribute("macroType");

	// 标值控件
	if (macroType=="macro_raty") {
		if(cel[j].children[0] && cel[j].children[0].tagName=="SPAN") {
			var ch = cel[j].children[0].children;
			for (var k=0; k < ch.length; k++) {
				if (ch[k].tagName=="INPUT") {
					return ch[k].value;
				}
			}
		}
	}
	// 在clear_color时，会置宏控件所在单元格的value属性为控件的值
	else if (cel[j].getAttribute("value")) {
		return cel[j].getAttribute("value");
	}
	else {
		if (cel[j].children.length>0) {
			var cellDiv = cel[j].children[0];
			return cellDiv.innerText.trim();
		}
		else {
			return cel[j].innerText.trim();
		}
	}
	return "";
}

// 区别于nest_table_view.jsp中的方法，以免重复
function getCellIndexOfNestSheet(td){   
    if (isIE()){   
        var cells=td.parentNode.cells;   
        for (var i=0,j=cells.length;i < j;i++ ){
            if (cells[i]===td){
                return i;
            }
        }
    }
    return td.cellIndex;
}

function getCellIndexByFieldNameOfNestSheet(tableObj, fieldName) {
	for(var i=0; i < tableObj.rows[0].cells.length; i++){
		if (tableObj.rows[0].cells[i].getAttribute("fieldName")==fieldName)
			return i;
	}
	return -1;
}

$(document).ready(function() {
	// 20210407 因为增加分页功能后，callCalculateOnload只计算当前页的数据，故注释掉，除去在添加页面会调用callCalculateOnload，其它场景因为调用了refreshNestSheetCtl，会自动刷新整个嵌套表，所以也无需调用此方法
	// setTimeout("callCalculateOnload()", 1000);
});

function hideNestSheetCol(fieldName) {
    $("[id^=cwsNestTable_]").each(function() {
        var obj = $(this)[0]; // table
        var rowsLen = obj.rows.length;
        var colIndex = getCellIndexByFieldNameOfNestSheet(obj, fieldName);
    	if (colIndex!=-1) {
            for (var i=0; i < rowsLen; i++) {
                obj.rows[i].cells[colIndex].style.display = "none";
            }
        }
    });
}

// 计算控件回调
function callByNestSheet(nestSheetSums, formCode) {
    if (nestSheetSums != null) {
        if (typeof (nestSheetSums) == 'object') {
            for (var o in nestSheetSums) {
				var $ctl = $("input[formula*='nest." + o + "'][formCode='" + formCode + "']");
				if (!$ctl[0]) {
					// 向下兼容，旧版的sum型计算控件中没有formCode
					$ctl = $("input[formula*='nest." + o + "']");
				}
                $ctl.val(nestSheetSums[o]);
            }
        }
    }
}