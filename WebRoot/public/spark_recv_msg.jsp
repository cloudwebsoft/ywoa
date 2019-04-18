<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.lark.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Properties"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.util.regex.Matcher"%>
<%@ page import="com.redmoon.oa.xmpp.MessageUtil"%>
<%!
static String lastQuestionThread = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>接收消息</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
			<script type="text/javascript" src="inc/common.js"></script>
	</head>
	<body>
		<%
/*
- 功能描述：记录spark消息
- 访问规则：当用户接收到别人发来的消息时，由sparkpost至本页面
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/
		
			String toUserName = ParamUtil.get(request, "toUserName");
			int p = toUserName.indexOf("@");
			if (p != -1)
				toUserName = toUserName.substring(0, p);
			String fromUserName = ParamUtil.get(request, "fromUserName");
			p = fromUserName.indexOf("@");
			if (p != -1)
				fromUserName = fromUserName.substring(0, p);
			System.out.println(getClass() + " fromUserName=" + fromUserName);
			System.out.println(getClass() + " toUserName=" + toUserName);

			String msg = ParamUtil.get(request, "msg");
			String thread = ParamUtil.get(request, "thread");
			String questionThread = ParamUtil.get(request, "questionThread");
			String msgType = ParamUtil.get(request, "msgType"); // 个人聊天为空，群聊为groupchat
			
			System.out.println(getClass() + " questionThread=" + questionThread);
			System.out.println(getClass() + " thread=" + thread);
			System.out.println(getClass() + " msgType=" + msgType);

			boolean isNewQuestion = false;
			// 如果是提问过程中的消息
			if (!questionThread.equals("")) {
				// 提问过程中的消息的thread不等于最后一个提问thread，则说明是新问题
				if (!questionThread.equals(lastQuestionThread)) {
					lastQuestionThread = questionThread;
					isNewQuestion = true;
				}
			}
			

			// System.out.println(getClass() + " " + toUserName);
			// System.out.println(getClass() + " " + fromUserName);
			/*
			 System.out.println(getClass() + " " + msg);
			 System.out.println(getClass() + " " + thread);
			 */

			LarkMsgDb lmd = new LarkMsgDb();
			boolean re = lmd.create(new JdbcTemplate(),
					new Object[] { fromUserName, toUserName, msg, thread,
							new java.util.Date() });

		%>
	</body>
	</html>