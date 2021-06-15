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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>接收消息-cmd</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
			<script type="text/javascript" src="inc/common.js"></script>
	</head>
	<body>
		<%
/*
- 功能描述：解析spark命令
- 访问规则：当一用户通过spark向另一用户发消息时，如果含有命令，则post至本页面
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

			String msg = ParamUtil.get(request, "msg");
			String thread = ParamUtil.get(request, "thread");

			// System.out.println(getClass() + " " + toUserName);
			// System.out.println(getClass() + " " + fromUserName);
			/*
			 System.out.println(getClass() + " " + msg);
			 System.out.println(getClass() + " " + thread);
			 */


			if (true) {
				// 如果是自己发给自己
				if (fromUserName.equals(toUserName)) {
					String cmd = "";
					String msgContent = "";
					String patStr = "([a-zA-Z]{1,2})[:|：](.*)";
					Pattern pa = Pattern.compile(patStr, Pattern.DOTALL
							| Pattern.CASE_INSENSITIVE);
					Matcher ma = pa.matcher(msg.trim());
					if (ma.find()) {
						cmd = ma.group(1).toLowerCase();
						msgContent = ma.group(2);
					} else
						return;

					// System.out.println(getClass() + " cmd=" + cmd);

					if (cmd.equals("c")) {
						String cmdContent = "";
						// [br]表示换行，在BroadcastPlugin中被替换为换行\n，在这里直接写\n无效，在进入BroadcastPlugin前会被替换为空字符串，原因待查
						cmdContent += "[br]n:表示写便笺";
						cmdContent += "[br]p:表示写日程安排，当对别人发话时，如果是对方的部门领导，则可以给对方制定日程安排";
						cmdContent += "[br]ln:表示列出未完成的便笺";
						cmdContent += "[br]c:表示列出命令";
						cmdContent += "[br]注意命令后面要跟冒号";
						// mu.send(MessageUtil.USER_SYSTEM, fromUserName, cmdContent);
						MessageUtil mu = new MessageUtil();
						mu.send(fromUserName, fromUserName, cmdContent);
					}

					// 如果是便笺指令
					boolean isPlan = false;
					boolean isNotepaper = false;

					if (cmd.equals("n") || cmd.equals("p")) {
						if (cmd.equals("n"))
							isNotepaper = true;

						String title = msgContent;
						if (title.length() > 30)
							title = title.substring(0, 30);
						boolean isRemind = false;
						int before = 10;

						boolean isRemindBySMS = false;

						boolean isClosed = false;

						java.util.Date d = new java.util.Date();
						java.util.Date end = new java.util.Date();

						PlanDb pd = new PlanDb();
						pd.setTitle(title);
						pd.setContent(msgContent);
						pd.setMyDate(d);
						pd.setEndDate(end);
						pd.setUserName(fromUserName);
						if (isRemind == true) {
							pd.setRemind(isRemind);
							java.util.Date dt = DateUtil.addMinuteDate(d,
									-before);
							pd.setRemindDate(dt);
						} else {
							pd.setRemind(isRemind);
						}
						pd.setRemindBySMS(isRemindBySMS);
						pd.setNotepaper(isNotepaper);
						pd.setClosed(isClosed);
						pd.create();
						
						MessageUtil mu = new MessageUtil();
						mu.send(fromUserName, fromUserName, "命令操作成功！");						
					} else if (cmd.equals("ln")) {
						// 列出便笺
						String sql = "select id from user_plan where username="
								+ StrUtil.sqlstr(fromUserName) + " and is_closed=0 and is_notepaper=1 order by myDate desc";
						PlanDb pd = new PlanDb();
						String t = "[br]未完成便笺：";
						Iterator ir = pd.list(sql).iterator();
						while (ir.hasNext()) {
							pd = (PlanDb) ir.next();
							t += "[br]" + DateUtil.format(pd.getMyDate(), "yy-MM-dd HH:mm") + " " + pd.getTitle();
						}

						MessageUtil mu = new MessageUtil();
						mu.send(fromUserName, fromUserName, t);
					}
				} else {
					String cmd = "";
					String msgContent = "";
					String patStr = "([a-zA-Z]{1,2})[:|：](.*)";
					Pattern pa = Pattern.compile(patStr, Pattern.DOTALL
							| Pattern.CASE_INSENSITIVE);
					Matcher ma = pa.matcher(msg);
					if (ma.find()) {
						cmd = ma.group(1).toLowerCase();
						msgContent = ma.group(2);
					} else
						return;

					// System.out.println(getClass() + " cmd=" + cmd + " msg=" + msg);

					UserDb user = new UserDb();
					user = user.getUserDb(fromUserName);

					Privilege pvg = new Privilege();
					Privilege.doLoginSession(request, user.getName(), user
							.getPwdMD5());

					// 如果是便笺指令
					boolean isPlan = false;
					boolean isNotepaper = false;

					if (cmd.equals("n")) {
						isNotepaper = true;
						isPlan = true;
					}
					if (cmd.equals("p")) {
						isPlan = true;
					}
					if (isPlan) {
						if (!pvg.canAdminUser(request, toUserName)) {
							String mymsg = "您没有权限安排" + toUserName + "的日程！";
							MessageUtil mu = new MessageUtil();
							mu.send(fromUserName, fromUserName, mymsg);
							return;
						}

						String title = msgContent;
						if (title.length() > 30)
							title = title.substring(0, 30);
						boolean isRemind = false;
						int before = 10;

						boolean isRemindBySMS = false;

						boolean isClosed = false;

						java.util.Date d = new java.util.Date();
						java.util.Date end = new java.util.Date();

						PlanDb pd = new PlanDb();
						pd.setTitle(title);
						pd.setContent(msgContent);
						pd.setMyDate(d);
						pd.setEndDate(end);
						pd.setUserName(toUserName);
						if (isRemind == true) {
							pd.setRemind(isRemind);
							java.util.Date dt = DateUtil.addMinuteDate(d,
									-before);
							pd.setRemindDate(dt);
						} else {
							pd.setRemind(isRemind);
						}
						pd.setRemindBySMS(isRemindBySMS);
						pd.setNotepaper(isNotepaper);
						pd.setClosed(isClosed);
						pd.setMaker(fromUserName);
						pd.create();
						
						MessageUtil mu = new MessageUtil();
						mu.send(fromUserName, fromUserName, "命令操作成功！");						
					}

				}

			}
		%>
</body>
</html>