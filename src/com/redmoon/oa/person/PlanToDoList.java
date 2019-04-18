package com.redmoon.oa.person;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.DesktopUnit;
import com.redmoon.oa.ui.IDesktopUnit;

public class PlanToDoList implements IDesktopUnit {

    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        Privilege privilege = new Privilege();
        String sql = "select id from user_plan where userName=" +
                     StrUtil.sqlstr(privilege.getUser(request)) +
                     " and is_closed=0 order by mydate desc";
        // System.out.println("PlanDb.java display sql=" + sql);
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();
        String str = "";
        try {
        	PlanDb pd = new PlanDb();
            ListResult lr = pd.listResult(sql, 1, uds.getCount());
            Iterator ir = lr.getResult().iterator();
            if(ir.hasNext()){
            	str += "<table class='article_table'>";
                while (ir.hasNext()) {
                    pd = (PlanDb) ir.next();

                    String t = StrUtil.getLeft(pd.getTitle(), uds.getWordCount());

                    String mydate = DateUtil.format(pd.getMyDate(),
                                                    "yyyy-MM-dd");

                    if (PlanDb.ACTION_TYPE_SALES_VISIT == pd.getActionType()) {
            			com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
            			String formCode = "day_lxr";
            			fd = fd.getFormDb(formCode);
            			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            			long id = StrUtil.toLong(pd.getActionData(), -1);
            			if (id==-1)
            				return "No data";
            			fdao = fdao.getFormDAO(id, fd);
            			String lxrId = fdao.getFieldValue("lxr");
            			fd = fd.getFormDb("sales_linkman");
            			// System.out.println(PlanMgr.class.getName() + " renderAction: pd.getActionData()=" + pd.getActionData() + " lxrId=" + lxrId);
            			fdao = fdao.getFormDAO(StrUtil.toLong(lxrId), fd);
            			// return "<a target='_blank' href='" + request.getContextPath() + "/visual/module_show.jsp?id=" + pd.getActionData() + "&action=&formCode=day_lxr&isShowNav=0'>点击查看</a>";			
            			str += "<tr><td class='article_content'><a href='javascript:;' onclick=\"addTab('行动', '" + request.getContextPath() + "/sales/customer_visit_list.jsp?customerId=" + fdao.getFieldValue("customer") + "')\">" + t + "</a></td><td class='article_time'>[" + mydate + "]</td></tr>";			
            		}
            		else if (PlanDb.ACTION_TYPE_FLOW == pd.getActionType()) {
            			MyActionDb mad = new MyActionDb();
            			mad = mad.getMyActionDb(StrUtil.toLong(pd.getActionData()));
            			WorkflowMgr wfm = new WorkflowMgr();
            			WorkflowDb wf = wfm.getWorkflowDb((int)mad.getFlowId());
            			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            			lf = lf.getLeaf(wf.getTypeCode());

    	  	            String cls = "class=\"readed\"";
    		        	if (!mad.isReaded()) {
    		        		cls = "class=\"unreaded\"";
    		        	}
            	  
            			if (lf!=null && lf.getType()==com.redmoon.oa.flow.Leaf.TYPE_LIST) {
            				str += "<tr><td class='article_content'><a " + cls + " href='javascript:;' onclick=\"addTab('处理流程', '" + request.getContextPath() + "/flow_dispose.jsp?myActionId=" + pd.getActionData() + "')\">" + t + "</a></td><td class='article_time'>[" + mydate + "]</td></tr>";
            			}
            			else {
            				str += "<tr><td class='article_content'><a " + cls + " href='javascript:;' onclick=\"addTab('处理流程', '" + request.getContextPath() + "/flow_dispose_free.jsp?myActionId=" + pd.getActionData() + "')\">" + t + "</a></td><td class='article_time'>[" + mydate + "]</td></tr>";
            			}
            		}	
            		else if (PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE == pd.getActionType()) {
            			str += "<tr><td class='article_content'><a href='javascript:;' onclick=\"addTab('收文', '" + request.getContextPath() + "/paper/paper_show.jsp?paperId=" + pd.getActionData() + "')\">" + t + "</a></td><td class='article_time'>[" + mydate + "]</td></tr>";			
            		}                
            		else {
    	                str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(pd.getTitle()) + "' href='javascript:;' onclick=\"addTab('日程安排', '" + url + "?id=" + pd.getId() + "')\">" + 
    	                        StrUtil.toHtml(t) + "</a></td><td class='article_time'>[" + mydate + "]</td></tr>";
            		}
                }
                str += "</table>";
            }else{
            	str = "<div class='no_content'><img title='暂无待办事项' src='images/desktop/no_content.jpg'></div>";
            }
            
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).info("display:" + e.getMessage());
        }
        return str;
    }

}
