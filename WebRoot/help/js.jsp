<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.help.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="java.io.*"%>
<%
int id = ParamUtil.getInt(request, "id", -1);
if (id==-1)
	return;
String type = ParamUtil.get(request, "type");
int size = ParamUtil.getInt(request, "size", 0);
%>
$("#tip<%=id%>").qtip({
  position: {
      my: 'top center',
      at: 'bottom center'
  },
  content: {
      text: "加载中...",
      ajax: {
        url: "<%=request.getContextPath()%>/help/tip.jsp?id=<%=id%>&type=<%=type%>&size=<%=size%>"
      }
  },
  show: {
      effect: function() {
          // $(this).slideDown();
          $(this).fadeTo(500, 1);
      }
  },		
  // show: 'click',
  style: {
          // classes: 'qtip-jtools'  
          classes: 'qtip-tipsy qtip-rounded qtip-shadow'  
      },

  // hide: 'click' // false
  hide: 'unfocus'
});
