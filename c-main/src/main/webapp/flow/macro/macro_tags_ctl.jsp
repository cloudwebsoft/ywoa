<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

	String fieldName = ParamUtil.get(request, "fieldName");
	String relateField = ParamUtil.get(request, "relateField");
	String code = "keywords";
	SelectMgr sm = new SelectMgr();
    SelectDb sd = sm.getSelect(code);
	Vector v = sd.getOptions(new JdbcTemplate());
    Iterator ir = v.iterator();
	String tags = "";
    while (ir.hasNext()) {
	   SelectOptionDb sod = (SelectOptionDb) ir.next();
	   if(tags.equals("")){
		  tags = "'"+sod.getName()+"'";
	   }else{
		  tags += ","+"'"+sod.getName()+"'";
	   }
	}
%>

var keys = new Array(<%=tags%>);

$(document).ready(function(){
  // $("#<%=fieldName%>").css({width:"250px"});
  if (document.getElementById("<%=relateField%>")) {
      document.getElementById("<%=relateField%>").onkeyup = function(){
          var keyStr = "";
          for (var i=0; i < keys.length; i++) {
              if (o("<%=relateField%>").value.indexOf(keys[i])!=-1) {
                if (keyStr=="")
                    keyStr = keys[i];
                else
                    keyStr += "，" + keys[i];
              }
          }
          o("<%=fieldName%>").value = keyStr;
      };
  }
})



