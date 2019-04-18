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
import cn.js.fan.db.SQLFilter;

import java.util.Iterator;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptUserDb;
import java.util.Vector;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.notice.NoticeDb;

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
public class NoticeDesktopUnit implements IDesktopUnit {
    public NoticeDesktopUnit() {
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

        Privilege privilege = new Privilege();

        String userName = privilege.getUser(request);
        DeptUserDb deptUserDb = new DeptUserDb();
        Vector ud = new Vector();

        ud = deptUserDb.getDeptsOfUser(userName);
        Iterator ir = ud.iterator();
        
        String myUnitCode = privilege.getUserUnitCode(request);
        boolean isNoticeAll = privilege.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = privilege.isUserPrivValid(request, "notice.dept");
        String tableNameA = "oa_notice";
        String tableNameB = "oa_notice_dept";
        String sql = "";
//        if (isNoticeAll) { // 通知-总管理
//        	
//        		sql = "select distinct notice_id from " + tableNameB + " d, oa_notice n where n.id=d.notice_id";
//        	
//        } else if (isNoticeMgr) { // 通知-部门管理员
//        	
//        		sql = "select distinct notice_id from " + tableNameB + " d, oa_notice n where n.id=d.notice_id and (n.unit_code='-1' or dept_code in (" + StrUtil.sqlstr(myUnitCode);
//        		while(ir.hasNext()) {
//        			DeptDb deptDb = new DeptDb();
//        			deptDb = (DeptDb)ir.next();
//        			String deptCode = deptDb.getCode();
//        			sql += " , " + StrUtil.sqlstr(deptCode);
//        		}
//        		sql += "))";
//        	
//        }
//        else { // 普通员工
//        	
//        		String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
//        		sql = "select distinct d.notice_id from " + tableNameB + " d, " + tableNameA + " n where n.id=d.notice_id and n.begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (n.end_date is null or n.end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ") and (n.unit_code='-1' or d.dept_code in(" + StrUtil.sqlstr(myUnitCode);
//        		while(ir.hasNext()) {
//        			DeptDb deptDb = new DeptDb();
//        			deptDb = (DeptDb)ir.next();
//        			String deptCode = deptDb.getCode();
//        			sql += " , " + StrUtil.sqlstr(deptCode);
//        		}
//        		sql += "))";
//        }

/*        
        if(isNoticeAll) {
        	sql = "select id from oa_notice where 1=1";
        	if (!myUnitCode.equals(DeptDb.ROOTCODE)) {
        		DeptDb dd = new DeptDb(myUnitCode);
        		Vector v = new Vector();
        		try {
        			v = dd.getAllChild(v, dd);
        		} catch (ErrMsgException e) {
        			e.printStackTrace();
        		}
        		v.add(dd);
        		Iterator it = v.iterator();
        		int i = 0;
        		while (it.hasNext()) {
        			DeptDb dept = (DeptDb) it.next();
        			if (dept.getType() == DeptDb.TYPE_UNIT) {
        				sql += (i++ == 0 ? " and unit_code in (" : ",") + StrUtil.sqlstr(dept.getCode());
        			}
        		}
        		while (!dd.getCode().equals(DeptDb.ROOTCODE)) {
        			dd = dd.getDeptDb(dd.getParentCode());
        			if (dd.getType() == DeptDb.TYPE_UNIT) {
        				sql += (i++ == 0 ? " and unit_code in (" : ",") + StrUtil.sqlstr(dd.getCode());
        			}
        		}
        		if (i > 0) {
        			sql += ")";
        		}
        	}
        }else if(isNoticeMgr){
        	//sql = "select id from oa_notice where id in(select notice_id from oa_notice_reply where user_name = "+StrUtil.sqlstr(userName)+") or unit_code = "+StrUtil.sqlstr(myUnitCode);
        	DeptUserDb dud = new DeptUserDb(userName);
        	Vector v = dud.getDeptsOfUser(userName);  //得到部门code
        	String unitCode = "";
        	for( int i = 0 ; i<v.size() ; i++){
        		DeptDb duDb = (DeptDb) v.get(i);
        		if(v.size() == i+1){
        			unitCode += StrUtil.sqlstr(duDb.getCode());
        		}else{
        			unitCode += StrUtil.sqlstr(duDb.getCode()) + ",";
        		}
        	}
        	
        	String ads = dud.getAdminDepts();  //得到管理的部门code
        	String[] adss  = ads.split(",");
        	for( int i = 0 ; i < adss.length ;i++){
        		if(i == 0 ){
        			unitCode +=",";
        		}
        		if(adss.length == i+1){
        			unitCode += StrUtil.sqlstr(adss[i]);
        		}else{
        			unitCode += StrUtil.sqlstr(adss[i]) + ",";
        		}
        	}
        	sql = "select id from oa_notice where id in(select notice_id from oa_notice_reply where user_name in (select user_name from dept_user where dept_code in (" + unitCode + ")))";
        }else{
        	String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
        	sql = "select id from oa_notice where begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (end_date is null or end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ") and id in(select notice_id from oa_notice_reply where user_name = "+StrUtil.sqlstr(userName)+")";
        }

        sql += " order by id desc";	
*/
        
    	String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");        
    	sql = "select id from oa_notice where begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (end_date is null or end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ")";
    	sql += " and ((id in(select notice_id from oa_notice_reply where user_name = " + StrUtil.sqlstr(userName) + ")) or user_name=" + StrUtil.sqlstr(userName) + ")";
        sql += " order by id desc";
        NoticeDb noticeDb = new NoticeDb();

        String str = "";
        try {
            ListResult lr = noticeDb.listResult(sql,1,uds.getCount());
            ir = lr.getResult().iterator();
            if(ir.hasNext()){
            	str += "<table class='article_table'>";
            	while (ir.hasNext()) {
                    NoticeDb nd = (NoticeDb)ir.next();

                    String t = StrUtil.getLeft(nd.getTitle(), uds.getWordCount());

    				if (nd.isBold() || !nd.isUserReaded(userName))
    					t = "<b>" + t + "</b>";
    				if (!nd.getColor().equals(""))
    					t = "<font color='" + nd.getColor() + "'>" + t + "</font>";

                    str += "<tr><td class='article_content'><a title='" + nd.getTitle() + "' href='" + du.getPageShow() + nd.getId() + "&isShow=" + nd.getIsShow() + "'>" + t +  "</a></td><td class='article_time'>[" + DateUtil.format(nd.getBeginDate(), "yyyy-MM-dd") + "]</td></tr>";
                }
                str += "</table>";
            }else{
            	str = "<div class='no_content'><img title='暂无通知'  src='images/desktop/no_content.jpg'></div>";
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        
        return str;
    }

}
