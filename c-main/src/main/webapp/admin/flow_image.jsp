<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="java.io.File" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();//从XML文件里取出文件存入路径
    boolean isGenerateFlowImage = cfg.getBooleanProperty("isGenerateFlowImage");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>生成流程图片</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/activebar2.js"></script>
    <script language=javascript>
        <!--
        function checkOfficeEditInstalled() {
            if (!isIE())
                return true;

            var bCtlLoaded = false;
            try {
                if (typeof (Designer.SetSelectedLinkProperty) == "undefined")
                    bCtlLoaded = false;
                if (typeof (Designer.SetSelectedLinkProperty) == "unknown") {
                    bCtlLoaded = true;
                }
            } catch (ex) {
            }
            return bCtlLoaded;
        }
        //-->
    </script>
</head>
<body>
<div class="spacerH"></div>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98"
       style="margin:0px">
    <tr>
      <td align="center" style="background-color: #fff" id="tdTitle">&nbsp;</td>
    </tr>
    <tr>
        <td align="center" style="background-color: #fff">
            <div id="designerDiv">
                <%
                    boolean isOem = License.getInstance().isOem();
                    String codeBase = "";
                    if (!isOem) {
                        codeBase = "codebase=\"../activex/cloudym.CAB#version=1,3,0,0\"";
                    }
                    String flowExpireUnit = cfg.get("flowExpireUnit");
                    if (flowExpireUnit.equals("day"))
                        flowExpireUnit = "天";
                    else
                        flowExpireUnit = "小时";
                    
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(privilege.getUser(request));
                %>
                <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" <%=codeBase%> width="80%" height="400">
                    <param name="Workflow" value=""/>
                    <param name="Mode" value="view"/>
                    <!--debug user initiate complete-->
                    <param name="CurrentUser" value="<%=ud.getName()%>"/>
                    <param name="CurrentUserRealName" value="<%=ud.getRealName()%>"/>
                    <param name="CurrentJobCode" value=""/>
                    <param name="CurrentJobName" value=""/>
                    <param name="ExpireUnit" value="<%=flowExpireUnit%>"/>
                    <%
                        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
                    %>
                    <param name="Organization" value="<%=license.getCompany()%>"/>
                    <param name="Key" value="<%=license.getKey()%>"/>
                    <param name="Company" value="<%=license.getName()%>"/>
                    <param name="LicenseType" value="<%=license.getType()%>"/>
                </object>
            </div>
        </td>
    </tr>
    <tr>
      <td align="center" style="background-color: #fff">
          <%
              if (isGenerateFlowImage) {
          %>
                <input id="btnStart" class="btn" type="button" value="开始" onclick="startRun()" />
          <%
              }
              else {
          %>
                系统已配置为不生成流程图片
          <%
              }
          %>
      </td>
    </tr>
</table>
</body>
<script>
    var isRun = false;
    function startRun() {
        isRun = !isRun;
        if (isRun) {
            $('#btnStart').val("暂停");
        }
        else {
            $('#btnStart').val("开始");
        }
    }
    
    function setFlowsRenewed(ids, visualPath) {
        $.ajax({
            type: "post",
            url: "../public/flow/setFlowsRenewed.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                ids: ids,
                visualPath: visualPath
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                // console.log("data.ret=" + data.ret);
            },
            complete: function (XMLHttpRequest, status) {
                // $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                // alert(XMLHttpRequest.responseText);
            }
        });
    }
    
    $(function () {
        checkOfficeEditInstalled();
        
        setInterval(function () {
            if (!isRun)
                return;
            
            $.ajax({
                type: "post",
                url: "../public/flow/getFlowsRenewed.do",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: {
                    count: 10
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    // $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    var c = data.count;
                    if (c>0) {
                        var ids = "";
                        var visualPath = data.visualPath;
                        var filePath = "<%=Global.getRealPath()%>" + visualPath;
                        var ary = data.rows;
                        for (var i=0; i<ary.length; i++) {
                            var json = ary[i];
                            var flowId = json.id;
                            var title = json.title;
                            var flowString = json.flowString;
                            // console.log(title + "--" + filePath + "/" + flowId + ".jpg");
                            $('#tdTitle').html("流程ID:" + flowId + "&nbsp;&nbsp;&nbsp;&nbsp;" + title);
                            Designer.Workflow = flowString;
                            Designer.SaveImage(filePath + "/" + flowId + ".jpg");

                            if (ids=="") {
                                ids = flowId;
                            }
                            else {
                                ids += "," + flowId;
                            }
                        }
                        setFlowsRenewed(ids, visualPath);
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    // $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    // alert(XMLHttpRequest.responseText);
                }
            });
        }, 3000);
    });
</script>
</html>
