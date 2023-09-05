<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="org.apache.http.client.utils.URIBuilder" %>
<%@ page import="com.cloudweb.oa.api.ICloudUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudwebsoft.framework.util.IPUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    String isFlow = ParamUtil.get(request, "isFlow");
    String op = ParamUtil.get(request, "op");

    FormDb restoreFormDb = null;
    String code = "";
    String name = "";
    String content = "";

    ICloudUtil cloudUtil = SpringUtil.getBean(ICloudUtil.class);
    String userSecret = cloudUtil.getUserSecret();
    String ip = IPUtil.getRemoteAddr(request);
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>创建表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/bootstrap/js/bootstrap.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/json2.js"></script>
<script type="text/javascript" src="../js/formpost.js"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<script language="JavaScript" type="text/JavaScript">
<!--
function setFormContent(htmlCode) {
	divContent.innerHTML = htmlCode;
}

function getFormContent() {
	return divContent.innerHTML;
}

function myFormAdd_onsubmit() {
    if ($('#isFlow').prop('checked') && $('#flowTypeCode').val()=="-1") {
        jAlert("请选择流程类型", "提示");
        return false;
    }

	var formContent = getFormContent();
	if (formContent.indexOf("<form ")!=-1) {
		jAlert("表单中不能含有form", "提示");
		return false;
	}
	
	$('#content').val(formContent);

    getFieldsOnSubmit();
}

// 当有重复字段返回时，恢复显示编辑的表单
function restoreContent() {
	if ($("#temp_content").html()!="")
		$("#divContent").html($("#temp_content").html());
}
//-->
</script>
</head>
<body onload="restoreContent()">
<%
if ("add".equals(op)) {
	FormMgr ftm = new FormMgr();
	boolean re = false;
	try {
		re = ftm.create(request);
		if (re) {
			if (!flowTypeCode.equals("-1") || isFlow.equals("") || isFlow.equals("1")) {
				if (flowTypeCode.equals("-1")) {
                    flowTypeCode = "";
                }
				out.print(StrUtil.jAlert_Redirect("创建成功！","提示", "form_m.jsp?flowTypeCode=" + StrUtil.UrlEncode(flowTypeCode)));
			}
			else {
				out.print(StrUtil.jAlert_Redirect("创建成功！","提示", "form_m.jsp?isFlow=0"));
			}
		}
		else {
			out.print(StrUtil.jAlert_Back("创建失败！","提示"));
		}
		return;
	}
	catch (ErrMsgException e) {
	    e.printStackTrace();
		FormForm fc = new FormForm();
		try {
			fc.checkCreate(request);
		}
		catch (ErrMsgException e2) {
		}

        restoreFormDb = fc.getFormDb();
		
		code = restoreFormDb.getCode();
		name = restoreFormDb.getName();
		content = restoreFormDb.getContent();
		out.print(StrUtil.jAlert(e.getMessage() + " 请检查是否有重复的编码或者编码使用了数据库的关键字！","提示"));
	}
}
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td class="tdStyle_1">表单管理</td>
  </tr>
</table>
<br />
<form id="myFormAdd" name="myFormAdd" action="form_add.jsp?op=add" method=post onSubmit="return myFormAdd_onsubmit()">
<table width="100%"  border="0" cellpadding="0" cellspacing="0" class="tableframe">
      <tr>
        <td height="100" align="center" class="p14"><table class="tabStyle_1 percent80" width="98%"  border="0" cellpadding="5" cellspacing="0">
          <tr>
            <td class="tabStyle_1_title" colspan="2" >创建<%=isFlow.equals("0")?"模块":"流程"%>表单</td>
          </tr>
          <tr>
            <td width="20%" >表单编码</td>
            <td width="80%" align="left" ><input type="text" name="code" maxlength="27" value="<%=code%>"></td>
          </tr>
          <tr>
            <td >表单名称</td>
            <td align="left" ><input type="text" name="name" value="<%=name%>" maxlength="50"><input name="isFlow" type="hidden" value="<%=isFlow%>" /></td>
          </tr>
          <tr style="display: <%=License.getInstance().isPlatformSrc()?"":"none"%>">
            <td >历史记录</td>
            <td align="left"><input type="checkbox" id="isLog" name="isLog" value="1" />
              保留 </td>
          </tr>
          <tr>
            <td >带有进度</td>
            <td align="left" >
            <select name="isProgress">
              <option value="0" selected="selected">否</option>
              <option value="1">是</option>
            </select></td>
          </tr>
          <tr>
            <td >带有附件</td>
            <td align="left" >
			<select id="hasAttachment" name="hasAttachment">
			<option value="1" selected>是</option>
			<option value="0">否</option>
			</select>
            <span id="spanIsOnlyCamera">
            <input id="isOnlyCamera" name="isOnlyCamera" type="checkbox" value="true" />
            手机端只允许拍照，不能选择照片上传（仅支持安卓端）
            </span>
            <script>
            $(function() {
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
                    <input type="radio" id="isFlow" name="isFlow" value="1" <%="1".equals(isFlow)?"checked":""%> />
                    流程型
                    <input type="radio" id="isModule" name="isFlow" value="0" <%="0".equals(isFlow)?"checked":""%> />
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
          <tr id="trFlowType" style="<%=!isFlow.equals("0")?"":"display:none"%>">
            <td >流程类型</td>
            <td align="left" >
			<%
			// -1表示非流程所用的表单
			if (!flowTypeCode.equals("") && !flowTypeCode.equals("-1")) {
				Leaf lf = new Leaf();
				lf = lf.getLeaf(flowTypeCode);
			%>
				<%=lf.getName(request)%><input name="flowTypeCode" type="hidden" value="<%=flowTypeCode%>">
			<%} else {
			%>
			<select id="flowTypeCode" name="flowTypeCode" onChange="if(this.options[this.selectedIndex].value=='root'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示'); return false;}">
			<option value="-1">无</option>
                <%
				Leaf rootlf = new Leaf();
				rootlf = rootlf.getLeaf("root");
				DirectoryView dv = new DirectoryView(rootlf);
				dv.ShowFlowTypeAsOptionsWithCode(request, out, rootlf, rootlf.getLayer());
				%>
              </select>
			<%}%>
            <input type="hidden" id="content" name="content" />
            <span id="temp_content" style="display:none"><%=content %></span>
            </td>
    </tr>
    <tr>
      <td >单位</td>
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
              %>
              <option <%=cls%> value="<%=val%>">&nbsp;&nbsp;&nbsp;&nbsp;<%=dd.getName()%></option>
              <%
              }
              %>
              <!--
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
                      <%
                      }
                }%>
                -->
            <%}
            %>
            </select>
        <%}else{%>
        	<input name="unitCode" value="<%=myUnitCode%>" type="hidden" />
        <%
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(myUnitCode);
			out.print(dd.getName());
		}%>
      </td>
    </tr>          
          
          </table>
        </td>
      </tr>
      <tr>
        <td align="center" style="height: 50px">
        <input class="btn btn-default" type="button" value="设计" onclick="openFormDesigner()" />
	    &nbsp;&nbsp;
        <input id="ieVersion" name="ieVersion" type="hidden" />
        <input class="btn btn-default" type="submit" value="确定"/>
        </td>
      </tr>
  </table>
    <input id="fieldsAry" name="fieldsAry" type="hidden"/>
</form>
  <table width="100%">
      <tr>
        <td align="center"><table width="100%" align="center">
          <tr>
            <td align="left" class="tabStyle_1_title">表单内容</td>
          </tr>
          <tr>
            <td><div id="divContent" name="divContent"><%=content%></div></td>
          </tr>
        </table></td>
      </tr>
</table>
<br>
<%
    License license = License.getInstance();
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String url = cfg.get("cloudUrl");
    URIBuilder uriBuilder = new URIBuilder(url);
    String host = uriBuilder.getHost();
    int port = uriBuilder.getPort();
    if (port==-1) {
        port = 80;
    }
    String path = uriBuilder.getPath();
    if (path.startsWith("/")) {
        path = path.substring(1);
    }

    boolean isServerConnectWithCloud = cfg.getBooleanProperty("isServerConnectWithCloud");
    if (!isServerConnectWithCloud) {
%>
<TABLE align="center" class="tabStyle_1 percent60" style="margin-top: 20px;">
    <TR>
        <TD align="left" class="tabStyle_1_title">上传助手</TD>
    </TR>
    <TR>
        <td align="center">
            <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,3,0,0" width=450 height=86 align="middle" id="webedit">
                <param name="Encode" value="utf-8">
                <param name="MaxSize" value="<%=Global.MaxSize%>">
                <!--上传字节-->
                <param name="ForeColor" value="(255,255,255)">
                <param name="BgColor" value="(107,154,206)">
                <param name="ForeColorBar" value="(255,255,255)">
                <param name="BgColorBar" value="(0,0,255)">
                <param name="ForeColorBarPre" value="(0,0,0)">
                <param name="BgColorBarPre" value="(200,200,200)">
                <param name="FilePath" value="">
                <param name="Relative" value="2">
                <!--上传后的文件需放在服务器上的路径-->
                <param name="Server" value="<%=host%>">
                <param name="Port" value="<%=port%>">
                <param name="VirtualPath" value="">
                <param name="PostScript" value="">
                <param name="PostScriptDdxc" value="">
                <param name="SegmentLen" value="204800">
                <param name="BasePath" value="">
                <param name="InternetFlag" value="">
                <param name="Organization" value="<%=license.getCompany()%>" />
                <param name="Key" value="<%=license.getKey()%>" />
            </object>
        </TD>
    </TR>
</table>
<%
    }
%>
</body>
<script>
function openFormDesigner() {
	//var preWin=window.open('<%=request.getContextPath()%>/ueditor/form_designer.jsp?op=add','','left=0,top=0,width=' + (screen.width-6) + ',height=' + (screen.height-78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');
	var preWin=window.open('<%=request.getContextPath()%>/ueditor/form_designer.jsp?op=edit','','left=0,top=0,width=' + (screen.width-6) + ',height=' + (screen.height-78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');	
}

function getFieldsOnSubmit() {
    <%
    if (isServerConnectWithCloud) {
    %>
    $.ajax({
        async: false,
        type: "post",
        url: "../form/parseForm.do",
        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
        data:  {
            content: getFormContent()
        },
        dataType: "html",
        beforeSend: function (XMLHttpRequest) {
            $('body').showLoading();
        },
        success: function (data, status) {
            data = $.parseJSON(data);
            if (data.ret=="1") {
                // jAlert(data.msg, "提示");
                $('#fieldsAry').val(JSON.stringify(data.fields));
            }
            else {
                alert(data.msg);
            }
        },
        complete: function (XMLHttpRequest, status) {
            $('body').hideLoading();
        },
        error: function (XMLHttpRequest, textStatus) {
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });
    <%
    }else {
    %>
    var we = o("webedit");
    we.PostScript = "<%=path%>/public/module/parseForm.do";

    loadDataToWebeditCtrl(o("myFormAdd"), we);
    we.AddField("content", getFormContent());
    we.AddField("cwsVersion", "<%=cfg.get("version")%>");
    we.AddField("userSecret", "<%=userSecret%>");
    we.AddField("ip", "<%=ip%>");
    we.UploadToCloud();

    var data = $.parseJSON(we.ReturnMessage);
    if (data.ret=="1") {
        $('#fieldsAry').val(JSON.stringify(data.fields));
    }
    <%
    }
    %>
}
</script>
</html>
