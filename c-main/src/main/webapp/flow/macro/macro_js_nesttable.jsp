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
<%@ page import = "java.util.regex.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%--
- 功能描述：嵌套表格所需的js文件
- 访问规则：在宏控件中写入页面
- 过程描述：
- 注意事项：
- 创建者：
- 创建时间：
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

	String nestFieldName = ParamUtil.get(request, "fieldName");
	String nestFormCode = ParamUtil.get(request, "nestFormCode");

	int isTab = ParamUtil.getInt(request, "isTab", 0);
	int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);

	String op = ParamUtil.get(request, "op");
	if ("forRefresh".equals(op)) {
		String pageType = ParamUtil.get(request, "pageType");
        String path = ParamUtil.get(request, "path");
		%>

        function refreshNestTableCtl<%=nestFieldName%>(newIds) {
            var path = '<%=path %>';
            path = (path.indexOf("?")!=-1)? path += "&"+new Date().getTime() : path += "?"+new Date().getTime()

            var fieldName = '<%=nestFieldName %>';

            var strNewIds = "";
            if (newIds!=null) {
                strNewIds = newIds;
            }

            var ajaxData = {
                newIds: strNewIds
            };
            ajaxPost(path, ajaxData).then((data) => {
                console.log('data', data);
                $('#toolbar_' + fieldName).remove();
                $nestTableParent = $("#nestTable_" + fieldName).parent();
                $("#nestTable_" + fieldName).remove();

                // 清除原来的cws_span_***、cws_textarea_***
                <%--$("[id^='cws_span_']").remove();
                $("[id^='cws_textarea_']").remove();--%>

                <%
                    if (isTab==0) {
                %>
                // $("#nestTable_"+fieldName).html(data);
                $nestTableParent.append(data);
                <%
                    } else {
                %>
                // 流程中选项卡式显示方式
                $("#tabs-<%=nestFormCode %>").html(data);
                <%
                    }
                %>

                initEventOfNestTable('nestTable_<%=nestFieldName%>');
                callCalculateOnloadNestTable('nestTable_<%=nestFieldName%>');
                try {
                    onNestTableRefresh_<%=nestFieldName%>();
                } catch (e) {};
            });

            <%--$.ajax({
                type: "post",
                url: path,
                data : {
                    newIds: strNewIds
                },
                success: function(data, status) {
                    $('#toolbar_' + fieldName).remove();
                    $nestTableParent = $("#nestTable_" + fieldName).parent();
                    $("#nestTable_" + fieldName).remove();

                    // 清除原来的cws_span_***、cws_textarea_***
                    &lt;%&ndash;$("[id^='cws_span_']").remove();
                    $("[id^='cws_textarea_']").remove();&ndash;%&gt;

                    <%
                        if (isTab==0) {
                    %>
                    // $("#nestTable_"+fieldName).html(data);
                    $nestTableParent.append(data);
                    <%
                        } else {
                    %>
                    // 流程中选项卡式显示方式
                    $("#tabs-<%=nestFormCode %>").html(data);
                    <%
                        }
                    %>

                    initEventOfNestTable('nestTable_<%=nestFieldName%>');
                    callCalculateOnloadNestTable('nestTable_<%=nestFieldName%>');
                    try {
                        onNestTableRefresh_<%=nestFieldName%>();
                    } catch (e) {};
                },
                error: function(XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });--%>
        }
		<%
		boolean isAutoSel = false;
		String filter = "";
		String parentFormCode = ParamUtil.get(request, "parentFormCode");
		if (!"".equals(nestFieldName) && !"".equals(parentFormCode)) {
			FormDb parentFd = new FormDb();
			parentFd = parentFd.getFormDb(parentFormCode);
			FormField nestField = parentFd.getFormField(nestFieldName);
			if (nestField==null) {
				return;
			}
			try {
				String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
				JSONObject json = new JSONObject(defaultVal);
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
			}
		}

		if (isAutoSel) {
			// 为了获取主表单中的值，解析配置条件中的主表所配置的字段并获取签到表的表单编码用于刷新表格
			String mainFormFieldNames="";
			Pattern p = Pattern.compile("\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(filter);
            while (m.find()) {
                String fName = m.group(1);
                if ("cwsCurUser".equals(fName) || "curUser".equals(fName) || "curUserDept".equals(nestFieldName)
                        || "curUserRole".equals(nestFieldName) || "admin.dept".equals(nestFieldName)) {
                    continue;
                }
                // 当条件为包含时，fieldName以@开头
                if (fName.startsWith("@")) {
                    fName = fName.substring(1);
                }
                if (mainFormFieldNames.length() == 0) {
                    mainFormFieldNames = fName;
                } else {
                    mainFormFieldNames += "," + fName;
                }
            }
			String mainId = ParamUtil.get(request, "mainId");
		%>
			$(function() {
				// @ wumeng 自动拉单功能
				var isAutoSel = <%=isAutoSel%>;
				if(isAutoSel) {	// 如果是自动嵌套拉单，每隔两秒执行检查是否改刷新表格
					var fields = '<%=mainFormFieldNames%>';
					var fieldNames = fields.split(",");

					var fieldObj = findObj(fieldNames[0]);
					var formObj = $(fieldObj).closest('form')[0];

					var oldValueArr = getFormValue(fieldNames, formObj);
					var ajaxData = {
						'flowId':'<%=flowId%>',
						'parentFormCode':'<%=parentFormCode%>',
						'parentId':'<%=mainId%>',
						'nestFieldName':'<%=nestFieldName%>',
						'isFirst':true
					};
                    if (oldValueArr != null) {
                        for (var i in fieldNames) {
                            ajaxData[fieldNames[i]] = oldValueArr[i];
                        }
                    }

					<%
					// 如果是模块编辑，则不需要自动拉单，否则将只显示拉单结果，而无法显示原来的数据
					if (!"edit".equals(pageType)) {
					%>
                    ajaxPost('/nestsheetctl/autoSel.do', ajaxData).then((data) => {
                        console.log('data', data);
						if(data.ret == '1') {
							var newIds = data.newIds;
							if (newIds==null) {
								newIds = "";
							}

                            // 需用setTimeout，以免此js与loadNestCtl未完成时无refreshNestTableCtl方法带来冲突
                            setTimeout("refreshNestTableCtl<%=nestFieldName%>(" + newIds + ")", 1);

							<%
                                if (flowId == com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
                            %>
							if (newIds!="") {
								var idsAry = newIds.split(",");
								for (x in idsAry) {
									addTempCwsId("<%=nestFormCode%>", idsAry[x]);
								}
							}
							<%
							    }
                            %>
						}
                    }
					<%
					}
                    %>

					if (fields!='') { // 配置了条件,没有配置条件则不处理
                            var sint = window.setInterval(function(){ // 5秒进行自动检测一次
							// 从新获取获取的条件的值，如果变化了，
							var newValueArr = getFormValue(fieldNames, formObj);
                            if (newValueArr != null) {
                                if(JSON.stringify(oldValueArr) != JSON.stringify(newValueArr)) {
                                    oldValueArr = newValueArr;
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

                                    ajaxPost('/nestsheetctl/autoSel.do', ajaxData).then((data) => {
                                        console.log('data', data);
                                        if(data.ret == '1'){
                                            var newIds = data.newIds;
                                            if (newIds==null) {
                                                newIds = "";
                                            }
                                            // 需用setTimeout，以免此js与loadNestCtl未完成时无refreshNestTableCtl方法带来冲突
                                            setTimeout("refreshNestTableCtl<%=nestFieldName%>(" + newIds + ")", 1);

                                            <%if (flowId == com.redmoon.oa.visual.FormDAO.NONEFLOWID) {%>
                                            if (newIds!="") {
                                                var idsAry = newIds.split(",");
                                                for (x in idsAry) {
                                                    addTempCwsId("<%=nestFormCode%>", idsAry[x]);
                                                }
                                            }
                                            <%}%>
                                        }
                                    }
                                }
                            } else {
                                window.clearInterval(sint);
                            }
						},200);
					}
				}
			});

			// 获取主表单中的值并拼接成sql的where字句
			function getFormValue(fieldNames, formObj) {
				var arr = new Array();
				for (var i = 0;i < fieldNames.length;i++) {
					var obj = findObjInFormObj(formObj, fieldNames[i]);
                    if (obj) {
						arr[i] = obj.value;
					} else {
                        console.warn('getFormValue 表单中未找到: ' + fieldNames[i]);
                        return null;
                    }
				}
				return arr;
			}
		<%
		}

		return;
	}
%>
function callCalculateOnloadNestTable(nestTableId) {
	// 更新对嵌套表的列进行sum操作的控件
 	$("input[kind='CALCULATOR']").each(function() {
        var calObj = $(this);
        if ($(this).attr('formula')) {
           	var formula = $(this).attr('formula');
            var formCode = $(this).attr('formCode');
            // console.log('formCode=' + formCode);
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
               // console.log('isSum=' + isSum + ' nestTableId=' + nestTableId + ' field=' + field);
               if (field.indexOf("nest.")==0) {
               		var p = field.indexOf(".");
					// 取得列名
                    field = field.substring(p+1);
                    var sumField = $(this);
                    var v = 0;
                    // 遍历嵌套表格，对匹配到的列求和
                    var isFoundInNestTable = false;
                    var $nestTable;
                    if (formCode != null && formCode != '') {
                        $nestTable = $("[id='" + nestTableId + "'][formCode='" + formCode + "']");
                    }
                    else {
                        $nestTable = $("[id='" + nestTableId + "']");
                    }

                    $nestTable.find("[name^='nest_field_" + field + "']").each(function() {
                        // 去掉后缀为_realshow的控件
                        if ($(this).attr('id') && $(this).attr('id').lastIndexOf('_realshow')!=-1) {
                            return;
                        }
                        isFoundInNestTable = true;
                        var cellV = $(this).val();
                        if (cellV == "") {
                            return;
                        }
                        if (!isNaN(cellV)) {
                            v += eval(cellV);
                        }
                    });

                    // console.log("nestTableId=" + nestTableId + " formula=" + formula + " field=" + field + " v=" + v + " isFoundInNestTable=" + isFoundInNestTable);
                    // 如果在对应的嵌套表中找到了算式中相应的字段
                    if (isFoundInNestTable) {
                        try  {
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
                    }
                    else {
                        // 不能置为0，如果存在多个嵌套表格，有可能会把在其它嵌套表格中被算到的计算控件值给清0了
                        // sumField.val(0);
                    }
               }
            }
		}
	});
}

function initEventOfNestTable(nestTableId) {
 	$("input[kind='CALCULATOR']").each(function() {
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
                    // 取得字段名
                    field = field.substring(p+1);

                    // 遍历所有的嵌套表格，对匹配到的列绑定change事件
                    if (formCode != null) {
                        $("[id='" + nestTableId + "'][formCode='" + formCode + "']").each(function() {
                            bindChangeEvent(nestTableId, $(this), field);
                        });
                    }
                    else {
                        $("[id='" + nestTableId + "']").each(function() {
                            bindChangeEvent(nestTableId, $(this), field);
                        });
                    }
               }
            }
		}
	});
}

function bindChangeEvent(nestTableId, $obj, field) {
    $obj.find("[name^='nest_field_" + field + "']").each(function() {
        var ctlField = this;
        var oldValue = $(ctlField).val();
        setInterval(function() {
            if (oldValue != $(ctlField).val()) {
                oldValue = $(ctlField).val();
                callCalculateOnloadNestTable(nestTableId);
            }
        }, 200);
    });
}

function initNestTableCalculate(nestTableId) {
    // console.log("nestTableId=" + nestTableId);
    // 绑定change事件
    initEventOfNestTable(nestTableId);
    // 执行计算
    callCalculateOnloadNestTable(nestTableId);
}

function initTr(nestFieldName, $tr, rowId, isAdd) {
    $tr.find("input[kind='DATE']").each(function() {
        if ($(this).attr('readonly')==null) {
            try {
                $(this).datetimepicker({
                    lang: 'ch',
                    datepicker: true,
                    timepicker: false,
                    validateOnBlur: false,
                    format: 'Y-m-d'
                });
            } catch (e) {
            }
        }
    });

    $tr.find("input[kind='DATE_TIME']").each(function() {
        if ($(this).attr('readonly')==null) {
            try {
                $(this).datetimepicker({
                    lang: 'ch',
                    datepicker: true,
                    timepicker: true,
                    validateOnBlur: false,
                    format:'Y-m-d H:i:00'
                });
            } catch (e) {
            }
        }
    });

    if (isAdd) {
        // 将$nestTr中的控件的id、name值改为以nest_field_打头，以免与主表中的字段冲突
        $tr.find('input,select,textarea,span').each(function() {
            // 不改变行首的checkbox的name，并置其值为rowId，以便于在服务端根据rowId获取
            if ($(this).attr("name") == "chk" + nestFieldName) {
                $(this).val(rowId);
                return;
            }
            if ($(this).attr("name") == "rowId" + nestFieldName) {
                $(this).val(rowId);
                return;
            }

            var name = $(this).attr('name');
            if (typeof(name) != 'undefined') {
                // 如果名称为带有_realshow，则按规律转换
                var p = name.indexOf("_realshow");
                if (p!=-1) {
                    var tmpL = name.substring(0, p);
                    var tmpR = name.substring(p);
                    var newName = 'nest_field_' + tmpL + "_" + rowId + tmpR;
                    // console.log("tmpL=" + tmpL + " tmpR=" + tmpR + " newName=" + newName);
                    $(this).attr('name', newName);
                }
                else {
                    var p = name.indexOf("_show");
                    if (p!=-1) {
                        var tmpL = name.substring(0, p);
                        var tmpR = name.substring(p);
                        var newName = 'nest_field_' + tmpL + "_" + rowId + tmpR;
                        $(this).attr('name', newName);
                    }
                    else {
                        $(this).attr('name', 'nest_field_' + name + "_" + rowId);
                    }
                }
            }

            var id = $(this).attr('id');
            if (typeof(id) != 'undefined') {
                // 如果名称为带有_realshow，则按规律转换
                var p = id.indexOf("_realshow");
                if (p!=-1) {
                    var tmpL = id.substring(0, p);
                    var tmpR = id.substring(p);
                    var newId = 'nest_field_' + tmpL + "_" + rowId + tmpR;
                    $(this).attr('id', newId);
                }
                else {
                    var p = id.indexOf("_show");
                    if (p!=-1) {
                        var tmpL = id.substring(0, p);
                        var tmpR = id.substring(p);
                        var newId = 'nest_field_' + tmpL + "_" + rowId + tmpR;
                        $(this).attr('id', newId);
                    }
                    else {
                        $(this).attr('id', 'nest_field_' + id + "_" + rowId);
                    }
                }
            }
        });

        // 绑定$tr中计算控件所关联字段的change事件
        $("input[kind='CALCULATOR']").each(function() {
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
                        // 取得字段名
                        field = field.substring(p+1);

                        var nestTableId = 'nestTable_' + nestFieldName;
                        // 遍历所有的嵌套表格，对匹配到的列求和
                        if (formCode != null) {
                            $("[id='" + nestTableId + "'][formCode='" + formCode + "']").each(function() {
                                bindChangeEvent(nestTableId, $tr, field);
                            });
                        }
                        else {
                            $("[id='" + nestTableId + "']").each(function() {
                                bindChangeEvent(nestTableId, $tr, field);
                            });
                        }
                   }
                }
            }
        });
    }

    $tr.find('input,select,textarea').each(function() {
        // 重置nestTr中控件的宽度样式
        if ($(this).is("input") || $(this).is("textarea")) {
            var type = $(this).attr('type');
            if (type!=null) {
                if (type=='button' || type=='checkbox' || type=='radio') {
                    return;
                }
            }
            // 忽略含有_realshow的元素
            if ($(this).attr('name')) {
                if ($(this).attr('name').indexOf("_realshow")==-1) {
                    $(this).css('width', '96%');
                }
                else {
                    $(this).css('width', '80%');
                }
            }
            else {
                $(this).css('width', '96%');
            }
        }
        else if ($(this).is("select")) {
            $(this).css('width', '99%');
        }
    });

    $tr.find('input').each(function () {
        if ($(this).attr('kind') == 'DATE' || $(this).attr('kind') == 'DATE_TIME') {
          $(this).attr('autocomplete', 'off');
        }
    });

    // 替换按钮的样式
    var btns = $tr[0].getElementsByTagName('button');
    for (var i = 0; i < btns.length; i++) {
        btns[i].className = 'ant-btn ant-btn-primary ant-btn-sm';
    }

    initCalculatorFieldNameOfFormula($tr, rowId);
    initCalculatorInTr($tr);
}

// 将行中的计算控件中的字段改为nest_field_***_rowid
function initCalculatorFieldNameOfFormula($tr, rowId) {
    $tr.find("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        var formula = $(this).attr('formula');
        if (formula != null) {
            if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1){
                // 对日期绑定事件
                // 时间相减方法subdate(d1, d2)
                var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
                formula = formula.replace(pat, function(p1, date1, date2){
                    var isSelect = false;
                    date1 = 'nest_field_' + date1 + '_' + rowId;
                    date2 = 'nest_field_' + date2 + '_' + rowId;
                    return "subdate(" + date1 + "," + date2 + ")";
                });

                // 时间相加方法addDate(d1, d2)
                var pat = /adddate\(([a-z0-9_-]+),([0-9-]+)\)/ig;
                formula = formula.replace(pat, function(p1, date1, date2){
                    var isSelect = false;
                    date1 = 'nest_field_' + date1 + '_' + rowId;
                    date2 = 'nest_field_' + date2 + '_' + rowId;
                    return "adddate(" + date1 + "," + date2 + ")";
                });

                $(this).attr('formula', formula);
            }else{
                var ary = getSymbolsWithBracket(formula);
                for (var i=0; i < ary.length; i++) {
                    // ary[i]可能为0.2这样的系数
                    if (!isOperator(ary[i]) && !isNumeric(ary[i])) {
                        if (o('nest_field_' + ary[i] + '_' + rowId)) {
                            ary[i] = 'nest_field_' + ary[i] + '_' + rowId;
                        }
                    }
                }
                formula = "";
                for (var i=0; i < ary.length; i++) {
                    formula += ary[i];
                }
                $(this).attr('formula', formula);
            }
        }
    });
}

// 初始化行中的计算控件
function initCalculatorInTr($tr) {
    $tr.find("input[kind='CALCULATOR']").each(function() {
        var calObj = $(this);
        if ($(this).attr('formula')) {
            var formula = $(this).attr('formula');
            if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1){
                // 对日期绑定事件
                formula = initFuncFieldEvent(formula, calObj);
            }else{
                var ary = getSymbolsWithBracket(formula);
                for (var i=0; i < ary.length; i++) {
                    // ary[i]可能为0.2这样的系数
                    if (!isOperator(ary[i]) && !isNumeric(ary[i])) {
                        var isSelect = false;
                        var o = $("input[name='" + ary[i] + "']")[0];
                        if(!o) {
                            o = $("select[name='" + ary[i] + "']")[0];
                            if (!o) {
                                // 因为字段可能为不可写状态，所以o可能为null，所以此处把提示注释掉，而且表单设计时是选择字段，所以字段名不会错
                                // alert("计算控件" + calObj.attr("name") + "，算式" + formula + "中的字段：" + ary[i] + " 不存在！");
                            }
                            else {
                                isSelect = true;
                            }
                        }
                        if (o) {
                            bindEvent(o, calObj, isSelect);
                        }
                    }
                }
            }

        }
    });

    $tr.find("input[kind='CALCULATOR']").each(function(){
        if ($(this).attr('formula')) {
            doCalculate($(this));
        }
    });
}