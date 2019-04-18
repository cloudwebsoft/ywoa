<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%
String rootpath = request.getContextPath();
String code = ParamUtil.get(request, "code");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>编辑表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script>
function getFormContent() {
	return o("divContent").innerHTML;
}

function setFormContent(c) {
	o("divContent").innerHTML = c;
	$("#content").val(c);
}

function myFormEdit_onsubmit() {
	if ($('#flowTypeCode').val()=="") {
		jAlert("请选择流程类型！","提示");
		return false;
	}
	
	var ver = "6";
	if (isIE7)
		ver = "7";
	else if (isIE8)
		ver = "8";
	else if (isIE9)
		ver = "9";
	else if (isIE10)
		ver = "10";
	else if (isIE11)
		ver = "11";

	$('#ieVersion').val(ver);
	
	$('#content').val(getFormContent());
}

//当有重复字段返回时，恢复显示编辑的表单
function restoreContent() {
	if ($("#content").val()!="")
		$("#divContent").html($("#content").val());
}

function openFormWin() {
	var preWin=window.open('../editor_full/flow_form.jsp?op=edit&formCode=<%=StrUtil.UrlEncode(code)%>','','left=0,top=0,width=' + (screen.width-6) + ',height=' + (screen.height-78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body onload="restoreContent()">
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="form_edit_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
FormDb fd = new FormDb();
fd = fd.getFormDb(code);
if (fd==null || !fd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "类型" + code + "不存在！"));
	return;
}

String flowTypeCode = fd.getFlowTypeCode();
if (!flowTypeCode.equals("") && !flowTypeCode.equals("-1")) {
	LeafPriv lp = new LeafPriv(flowTypeCode);
	if (!lp.canUserSee(privilege.getUser(request))) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
else {
	// 对于智能表单设计的表必须具有admin权限才可以管理
	if (!privilege.isUserPrivValid(request, "admin.flow")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
	
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	FormMgr ftm = new FormMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = ftm.modify(request);
		fd = fd.getFormDb(code);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re) {
			out.print(StrUtil.jAlert_Redirect("编辑成功！","提示", "form_edit.jsp?code=" + StrUtil.UrlEncode(code)));
		}
		else {
			out.print(StrUtil.jAlert_Back("编辑失败！请检查是否有重复的编码或者编码使用了数据库的关键字！","提示"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Redirect(e.getMessage(), "提示", "form_edit.jsp?code=" + StrUtil.UrlEncode(code)));
	}
	return;
}		
%>
<form action="form_edit_compare.jsp" id="myFormEdit" name="myFormEdit" method="post" onsubmit="return myFormEdit_onsubmit()">
  <table width="98%"  border="0" cellpadding="5" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td colspan="2" class="tabStyle_1_title" >编辑表单</td>
    </tr>
    <tr>
      <td width="16%" >表单编码 </td>
      <td width="84%" ><input type="text" name="code" value="<%=fd.getCode()%>" readonly="readonly" /></td>
    </tr>
    <tr>
      <td >表单名称</td>
      <td >
      <input type="text" name="name" value="<%=fd.getName()%>" />
      <input type="hidden" id="content" name="content" />
      <input type="hidden" id="ieVersion" name="ieVersion" />
      </td>
    </tr>
    <tr>
      <td >历史记录</td>
      <td >
      <input type="checkbox" id="isLog" name="isLog" value="1" <%=fd.isLog()?"checked":""%> />
      保留
      </td>
    </tr>    
	<tr>
	  <td >带有进度</td>
	  <td align="left" >
      	<select id="isProgress" name="isProgress">
	    <option value="0" selected="selected">否</option>
	    <option value="1">是</option>
	    </select>
      </td>
    </tr>
	<tr>
	<td >带有附件</td>
	<td align="left" >
	<select id="hasAttachment" name="hasAttachment">
	<option value="1" selected>是</option>
	<option value="0">否</option>
	</select>
    <span id="spanIsOnlyCamera">
	<input id="isOnlyCamera" name="isOnlyCamera" type="checkbox" value="true" <%=fd.isOnlyCamera()?"checked":""%> />
	手机端只允许拍照，不能选择照片上传
    </span>
	<script>
	$(function() {
        $('#isProgress').val("<%=fd.isProgress()?"1":"0"%>");
        $('#hasAttachment').val("<%=fd.isHasAttachment()?"1":"0"%>");

		<%if (!fd.isHasAttachment()) {%>
			$('#spanIsOnlyCamera').hide();
		<%}%>
		
		$('#hasAttachment').change(function() {
			if ($('#hasAttachment').val()=="1") {
				$('#spanIsOnlyCamera').show();
			}
			else {
				$('#spanIsOnlyCamera').hide();
			}
		});
	});
	</script>
	</td>
	</tr>	
    <tr>
      <td >表单类型</td>
      <td >
      <input type="radio" id="isFlow" name="isFlow" value="1" <%=fd.isFlow()?"checked":""%> />
      流程型
      <input type="radio" id="isModule" name="isFlow" value="0" <%=!fd.isFlow()?"checked":""%> />      
      模块型
      <script>
      $(function() {
      	$('#isFlow').click(function() {
    		$('#trFlowType').show();
      	})
      	$('#isModule').click(function() {
    		$('#trFlowType').hide();      		
      	})      	
      });
      </script>
      </td>
    </tr>	
    <tr id="trFlowType" style="<%=fd.isFlow()?"":"display:none"%>">
      <td >流程类型</td>
      <td ><select id="flowTypeCode" name="flowTypeCode" onchange="if(this.options[this.selectedIndex].value=='root'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示'); return false;}">
        <option value="-1">无</option>
        <!---1表示非流程所用的表单-->
        <%
				Leaf rootlf = new Leaf();
				rootlf = rootlf.getLeaf("root");
				DirectoryView dv = new DirectoryView(rootlf);
				dv.ShowFlowTypeAsOptionsWithCode(request, out, rootlf, rootlf.getLayer());
		%>
      </select>
          <script>
		  <%if (fd.isFlow()) {%>
				myFormEdit.flowTypeCode.value = "<%=fd.getFlowTypeCode()%>";
		  <%}else{%>
				myFormEdit.flowTypeCode.value = "-1";
		  <%}%>
		  </script></td>
    </tr>
    
<tr>
      <td align="left">单位</td>
      <td colspan="2" align="left">
		<%
		String myUnitCode = privilege.getUserUnitCode(request);
		
		if (myUnitCode.equals(DeptDb.ROOTCODE)) {%>
            <select id="unitCode" name="unitCode" onchange="if (this.value=='') jAlert('请选择单位！','提示');">
            <%
            DeptDb rootDept = new DeptDb();
            rootDept = rootDept.getDeptDb(DeptDb.ROOTCODE);
            %>
            <option value="<%=DeptDb.ROOTCODE%>"><%=rootDept.getName()%></option>
            <%
            // Iterator ir = privilege.getUserAdminUnits(request).iterator();
            Iterator ir = rootDept.getChildren().iterator();
            while (ir.hasNext()) {
              DeptDb dd = (DeptDb)ir.next();
              String cls = "", val = "";
              if (dd.getType()==DeptDb.TYPE_UNIT) {
                  cls = " class='unit' ";
                  val = dd.getCode();
              }
              %>
              <option <%=cls%> value="<%=val%>">&nbsp;&nbsp;&nbsp;&nbsp;<%=dd.getName()%></option>
                <%
                Iterator ir2 = dd.getChildren().iterator();
                while (ir2.hasNext()) {
                    DeptDb dd2 = (DeptDb)ir2.next();
                    String cls2 = "", val2 = "";
                    if (dd2.getType()==DeptDb.TYPE_UNIT) {
                        cls2 = " class='unit' ";
                        val2 = dd2.getCode();
                    }
                    %>
                      <option <%=cls2%> value="<%=val2%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd2.getName()%></option>
<%
                      Iterator ir3 = dd2.getChildren().iterator();
                      while (ir3.hasNext()) {
                          DeptDb dd3 = (DeptDb)ir3.next();
                          String cls3 = "", val3 = "";
                          if (dd3.getType()==DeptDb.TYPE_UNIT) {
                              cls3 = " class='unit' ";
                              val3 = dd3.getCode();
                          }
                          %>
                          <!--
                            <option <%=cls3%> value="<%=val3%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd3.getName()%></option>
                              <%
                              Iterator ir4 = dd3.getChildren().iterator();
                              while (ir4.hasNext()) {
                                  DeptDb dd4 = (DeptDb)ir4.next();
                                  String cls4 = "", val4 = "";
                                  if (dd4.getType()==DeptDb.TYPE_UNIT) {
                                      cls4 = " class='unit' ";
                                      val4 = dd4.getCode();
                                  }
                                  %>
                                    <option <%=cls4%> value="<%=val4%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd4.getName()%></option>
                                      <%
                                      Iterator ir5 = dd4.getChildren().iterator();
                                      while (ir5.hasNext()) {
                                          DeptDb dd5 = (DeptDb)ir5.next();
                                          String cls5 = "", val5 = "";
                                          if (dd5.getType()==DeptDb.TYPE_UNIT) {
                                            cls5 = " class='unit' ";
                                            val5 = dd5.getCode();
                                          }
                                          %>
                                            <option <%=cls5%> value="<%=val5%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dd5.getName()%></option>
                                      <%}                                
                              }
                              %>
                           -->
                      <%
                      }
                }
            }
            %>
            </select>
            <script>
			o("unitCode").value = "<%=fd.getUnitCode()%>";
			</script>
        <%}else{%>
        	<input name="unitCode" value="<%=fd.getUnitCode()%>" type="hidden" />
        <%
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(fd.getUnitCode());
			out.print(dd.getName());
		}%>
      </td>
    </tr>    
    
    <tr>
      <td colspan="2" align="center" >
      <!-- 判断是否为新表单，若是则打开新设计器 -->
      	<%
      		String cnt = fd.getContent();
      		if (cnt.toLowerCase().indexOf("cwsplugins") > 0){ 
      	%>
      	<input class="btn" type="button" name="next2" value="设计" onclick="openFormDesigner()"/>
      	<%
      		}else{
      	%>
      	<input class="btn" type="button" name="next2" value="设计" onclick="openFormWin()"/>
      	<%
      		}
      	%>
        &nbsp;&nbsp;        
        <input class="btn" type="submit" name="next" value="确定" />
        &nbsp;&nbsp;        
        <input class="btn" type="button" value="重置" onclick="window.location.reload()" />        
        <%if (com.redmoon.oa.kernel.License.getInstance().getVersionType().equalsIgnoreCase(com.redmoon.oa.kernel.License.VERSION_ENTERPRISE)) {%>
        &nbsp;&nbsp;
      	<input class="btn" type="button" name="next2" value="模块" onclick="top.mainFrame.addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=fd.getCode()%>')" />
        <%}%>
        &nbsp;&nbsp;
      	<input class="btn" type="button" name="next2" value="导出" onclick="openWin('form_export.jsp?formCode=<%=fd.getCode()%>')" />        
          </td>
    </tr>
  </table>
</form>
<br>
<table width="90%" align="center" class="percent98">
  <tr>
    <td class="tabStyle_1_title">表单预览 
    </td>
  </tr>
  <tr>
    <td>
    <span id="infoSpan" style="color:red"></span>    
	<script>
	$(function() {
		if (!isIE()) {
			// $('#infoSpan').html("设计器只能在IE内核浏览器使用!");
		}
	});	
	</script>    
    <div id="divContent" name="divContent"><%
	out.print(cnt);
	%></div></td>
  </tr>
</table>
<br />
</body>
<script>
function openFormDesigner() {
	// var preWin=window.open('<%=request.getContextPath()%>/ueditor/form_designer.jsp?op=edit&formCode=<%=fd.getCode()%>','','left=0,top=0,width=' + (screen.width-6) + ',height=' + (screen.height-78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');
	openWinMax('<%=request.getContextPath()%>/ueditor/form_designer.jsp?op=edit&formCode=<%=fd.getCode()%>');
}
</script>
</html>
