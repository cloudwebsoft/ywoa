<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "org.jdom.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作报告 - 添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script src="../inc/upload.js"></script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
.workDiv p {
margin:0px;
line-height:1.5;
}
</style>
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

WorkLogDb wld = new WorkLogDb();
wld = wld.getWorkLogDbOfToday(privilege.getUser(request));
/*
if (wld!=null) {
	response.sendRedirect("mywork_edit.jsp?id=" + wld.getId() + "&userName=" + StrUtil.UrlEncode(privilege.getUser(request)));
	return;
}
*/

String op = ParamUtil.get(request, "op");

int logYear = ParamUtil.getInt(request, "logYear", 0);
int logItem = ParamUtil.getInt(request, "logItem", 0);
int logType = ParamUtil.getInt(request, "logType", WorkLogDb.TYPE_NORMAL);
if (op.equals("add")) {
	boolean re = false;
	try {
		WorkLogMgr wlm = new WorkLogMgr();
		re = wlm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re) {
		if (logType==WorkLogDb.TYPE_WEEK) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mywork_list_week.jsp"));
		}
		else if (logType==WorkLogDb.TYPE_MONTH) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mywork_list_month.jsp"));
		}
		else
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "mywork.jsp"));
	}
	return;
}
%>
<%@ include file="mywork_nav.jsp"%>
<script>
o("menu4").className="current";
</script>
<br />
<br>
<br>
<form name="form1" action="?op=add&logYear=<%=logYear %>&logItem=<%=logItem %>&logType=<%=logType %>" method="post" onSubmit="return form1_onsubmit()" enctype="multipart/form-data">
<table width="98%" class="tabStyle_1 percent80">
  <tr>
    <td class="tabStyle_1_title"><a name="#write"></a>
    <input type="hidden" name="logType" value="<%=logType%>" />
    <input type="hidden" name="logItem" value="<%=logItem%>" />
    <input type="hidden" name="logYear" value="<%=logYear%>" />
    <%
	java.util.Date curDate = new java.util.Date();
	String strDate = DateUtil.format(curDate, "yyyy-MM-dd");
	curDate = DateUtil.parse(strDate, "yyyy-MM-dd");
    com.redmoon.oa.worklog.Config cfg = com.redmoon.oa.worklog.Config.getInstance();

	int canWriteDays = 0;
	%>
    <%if (logType==WorkLogDb.TYPE_NORMAL) {%>
    工作日报
    &nbsp;&nbsp;
    <select id="myDate" name="myDate">
		<%if (wld==null) {
            canWriteDays++;
          %>
          <option value="<%=strDate%>"><%=strDate%></option>
        <%}%>
        <%
        int dayLimit = cfg.getIntProperty("dayLimit");
        OACalendarDb oacdb = new OACalendarDb();
        wld = new WorkLogDb();
        int k = 1;
        for (int i=0; i<dayLimit; ) {
            java.util.Date d = DateUtil.addDate(curDate, -k);
                    
            oacdb = (OACalendarDb) oacdb.getQObjectDb(d);
            k++;
            if (oacdb==null)
                break;
            strDate = DateUtil.format(d, "yyyy-MM-dd");
            String bkclr = "#cccccc";
            if (oacdb.getInt("date_type")==OACalendarDb.DATE_TYPE_WORK) {
                i++;
                bkclr = "";
            }
            
            wld = wld.getWorkLogDb(privilege.getUser(request), d);
            if (wld==null) {
                canWriteDays++;
            %>
            <option style="background-color:<%=bkclr%>" value="<%=strDate%>"><%=strDate%></option>
            <%
                wld = new WorkLogDb();
            }
        }
        %>
    </select>
    <%
    if (canWriteDays==0) {
        if (dayLimit>0)
            out.print(StrUtil.jAlert_Back("您已填写进" + dayLimit + "天的记录！","提示"));
        else
            out.print(StrUtil.jAlert_Back("您已填写当天的记录！","提示"));
        return;
    }
    %>    
    <%}else if (logType==WorkLogDb.TYPE_WEEK) {%>
    周报
    &nbsp;&nbsp;第<%=logItem%>周
	<%}else{%>
    月报
    &nbsp;&nbsp;<%=logItem%>月
    <%}%>
    </td>
  </tr>
    <tr>
      <td>
      <%
	  List list = cfg.getRoot().getChild("items").getChildren();
	  if (list != null) {
		  Iterator ir = list.iterator();
		  int i=0;
		  while (ir.hasNext()) {
			  Element e = (Element) ir.next();
			  String title = e.getChildText("title");
			  boolean canNull = e.getChildText("canNull").equals("true");
			  int wordCount = StrUtil.toInt(e.getChildText("wordCount"), -1);
			  %>
              <div style="padding:5px"><%=title%>：<%=canNull?"":"<font color=red>*</font>"%><%=wordCount!=-1?"&nbsp;(" + wordCount+"字以上)":""%></div>
                <textarea id="content<%=i%>" name="content" style="display:none"></textarea>
                <script>
                CKEDITOR.replace('content<%=i%>',
                {
                // skin : 'kama',
                toolbar : 'Middle',
				height : 200
                });
                </script>
			  <%
			  i++;
		  }
	  }
	  %>
      </td>
    </tr>
    <tr>
    	<td>
    		<script>initUpload()</script>
    	</td>
    </tr>
    <tr>
      <td align="center"><input id="btn" class="btn" name="submit" type=submit value="确定"></td>
    </tr>
</table>
</form>
</body>
<script>
function form1_onsubmit() {
	o("btn").disabled = true;
	<%
	  list = cfg.getRoot().getChild("items").getChildren();
	  if (list != null) {
		  Iterator ir = list.iterator();
		  int i=0;
		  while (ir.hasNext()) {
			  Element e = (Element) ir.next();
			  String title = e.getChildText("title");			  
			  boolean canNull = e.getChildText("canNull").equals("true");
			  int wordCount = StrUtil.toInt(e.getChildText("wordCount"), -1);
			  if (!canNull) {
			  %>
				if (CKEDITOR.instances.content<%=i%>.getData().trim()=="") {
					jAlert("请填写<%=title%>！","提示");
					o("btn").disabled = false;
					return false;
				}
			  <%
			  }
			  
			  if (wordCount!=-1) {
			  %>
			  	var txt = CKEDITOR.instances.content<%=i%>.document.getBody().getText();	
				if (strlen(txt) < <%=wordCount%>) {
					jAlert("<%=title%>不能少于<%=wordCount%>字！","提示");
					o("btn").disabled = false;
					return false;
				}
			  <%
			  }
			  i++;
		  }
	  }
	%>
	
}

function strlen(str) {
	//<summary>获得字符串实际长度，中文2，英文1</summary>    
	//<param name="str">要获得长度的字符串</param>
	var regExp = new RegExp(" ","g");
	str = str.replace(regExp , "");
	str = str.replace(/\r\n/g,"");
	var realLength = 0, len = str.length, charCode = -1;
	for (var i = 0; i < len; i++) {
		charCode = str.charCodeAt(i);
		if (charCode >= 0 && charCode <= 128)
			realLength += 1;
		else realLength += 2;
	}
	return realLength;
}   
</script>
</html>
