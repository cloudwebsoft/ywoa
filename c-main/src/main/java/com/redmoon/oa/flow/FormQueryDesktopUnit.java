package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.IDesktopUnit;

public class FormQueryDesktopUnit implements IDesktopUnit {
    public FormQueryDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        int id = StrUtil.toInt(uds.getModuleItem(), -1);
        url += id;
        return url;
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        StringBuffer sb = new StringBuffer();
        
        int id = StrUtil.toInt(uds.getModuleItem(), -1);
		FormQueryDb aqd = new FormQueryDb();
		aqd = aqd.getFormQueryDb(id);
		
		if (aqd.isScript()) {
			ResultIterator ri = null;
			QueryScriptUtil	qsu = new QueryScriptUtil();		
			try {
				ri = qsu.executeQuery(request, aqd);
				HashMap mapIndex = qsu.getMapIndex();
				int len = mapIndex.keySet().size();
				int k = 1;
				if(ri.hasNext()){
					sb.append("<ul>");
					while(ri.hasNext()){
						ResultRecord rr = (ResultRecord)ri.next();
		                sb.append("<li>");
		                
		                for (int i=0; i<len; i++) {
		                	if (i==0)
		                		sb.append("<span class='col'>" + rr.getString(i+1) + "</span>");
		                	else
		                		sb.append("<span class='col'>&nbsp;&nbsp;&nbsp;&nbsp;" + rr.getString(i+1) + "</span>");	                		
		                }
		                
		                sb.append("</li>");
		                k++;
					}
					sb.append("</ul>");
				}else{
					sb.append("<div class='no_content'><img title='暂无自由查询项' src='images/desktop/no_content.jpg'></div>");
				}
			} catch (ErrMsgException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}else{
			sb.append("<div class='no_content'><img title='暂无自由查询项' src='images/desktop/no_content.jpg'></div>");
		}
        return sb.toString();
    }
}