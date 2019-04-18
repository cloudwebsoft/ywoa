package com.redmoon.oa.flow;

import com.redmoon.oa.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.pvg.Privilege;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import com.redmoon.oa.ui.DesktopUnit;
import cn.js.fan.util.DateUtil;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class WorkflowAttendedDesktopUnit implements IDesktopUnit {
	public WorkflowAttendedDesktopUnit() {
	}

	public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
		Privilege privilege = new Privilege();
		DesktopMgr dm = new DesktopMgr();
		DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
		String url = du.getPageShow();

		String sql = "select distinct m.flow_id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name="
				+ StrUtil.sqlstr(privilege.getUser(request))
				+ " or m.proxy="
				+ StrUtil.sqlstr(privilege.getUser(request))
				+ ") and f.status<>"
				+ WorkflowDb.STATUS_NONE
				+ " and f.status<>" + WorkflowDb.STATUS_DELETED
				+ " and m.is_checked<>" + MyActionDb.CHECK_STATUS_WAITING_TO_DO // 等待前一节点结束
				+ " order by flow_id desc";

		String str = "";
		try {
			WorkflowPredefineDb wfp = new WorkflowPredefineDb();
			WorkflowDb wfd = new WorkflowDb();
			ListResult wflr = wfd.listResult(sql, 1, uds.getCount());
			Iterator wfir = wflr.getResult().iterator();
			Directory dir = new Directory();
			if (wfir.hasNext()) {
				str += "<table class='article_table'>";
				while (wfir.hasNext()) {
					wfd = (WorkflowDb) wfir.next();
					Leaf lf = dir.getLeaf(wfd.getTypeCode());
					if (lf == null) {
						continue;
					}

					wfp = wfp.getPredefineFlowOfFree(wfd.getTypeCode());
					String t = wfp.isLight() ? MyActionMgr
							.renderTitle(request, wfd)
							: StrUtil.toHtml(StrUtil.getLeft(wfd.getTitle(),
									uds.getWordCount()));
					str += "<tr><td class='article_content'><a title='"
							+ wfd.getTitle()
							+ "' href='"
							+ (wfp.isLight() ? "flow_dispose_light_show.jsp?flowId="
									: url) + wfd.getId() + "'>"
							+ t
							+ "</a></td><td class='article_time'>["
							+ DateUtil.format(wfd.getMydate(), "yyyy-MM-dd")
							+ "]</td></tr>";
				}
				str += "</table>";
			} else {
				str = "<div class='no_content'><img title='暂无我参与的流程' src='images/desktop/no_content.jpg'></div>";
			}
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error("display:" + e.getMessage());
		}
		return str;
	}

	public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
		DesktopMgr dm = new DesktopMgr();
		DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
		String url = du.getPageList();
		return url;
	}
}
