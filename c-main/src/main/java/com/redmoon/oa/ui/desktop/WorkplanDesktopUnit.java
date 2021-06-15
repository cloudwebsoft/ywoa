package com.redmoon.oa.ui.desktop;

import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.person.UserDesktopSetupDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.ui.IDesktopUnit;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.workplan.WorkPlanDb;
import cn.js.fan.db.ListResult;
import java.util.Iterator;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkplanDesktopUnit implements IDesktopUnit {
    public WorkplanDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String str = "";
        Privilege privilege = new Privilege();
        WorkPlanDb wpd = new WorkPlanDb();
        String sql = "select distinct p.id from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" +
                     StrUtil.sqlstr(privilege.getUser(request)) + " order by p.beginDate desc";
        try {
            ListResult lr = wpd.listResult(sql, 1, uds.getCount());
            Iterator ir = lr.getResult().iterator();
            if(ir.hasNext()){
	           	str += "<table class='article_table'>";
	            while (ir.hasNext()) {
	                wpd = (WorkPlanDb) ir.next();
	
	                String t = StrUtil.getLeft(wpd.getTitle(), uds.getWordCount());
	                str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(wpd.getTitle()) + "' href='" + du.getPageShow() + wpd.getId() + "'>" +
	                        t + "</a></td><td class='article_time'>[" +
	                        DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd") +
	                        "]</td></tr>";
	            }
	            str += "</table>";
            }else{
            	str = "<div class='no_content'><img title='暂无工作计划' src='images/desktop/no_content.jpg'></div>";
            }
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
       
        return str;
    }

}
