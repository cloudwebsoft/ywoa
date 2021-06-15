package com.redmoon.oa.ui.desktop;

import com.redmoon.oa.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.ui.DesktopMgr;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.ThreadBlockIterator;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ForumDesktopUnit implements IDesktopUnit{
    public ForumDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
         DesktopMgr dm = new DesktopMgr();
         com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageList();
         return url;
     }

     public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
         DesktopMgr dm = new DesktopMgr();
         com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String str = "";

         str += "<table class='article_table'>";
         MsgDb msd = new MsgDb();
         // String topmsgsql = "select id from sq_thread order by lydate desc limit 0," + uds.getCount();
         String sql = "select id from sq_thread where check_status <> 10 order by lydate desc";
         if (!uds.getModuleItem().equals("")) {
        	 sql = "select id from sq_thread where boardcode=" + StrUtil.sqlstr(uds.getModuleItem()) + " and check_status <> 10 order by lydate desc";
         }
         
         /*
          *因为有缓存的关系，在OA中我的办公桌上不能及时看到
         ThreadBlockIterator ir = msd.getThreads(topmsgsql, "", 0, uds.getCount());
         while (ir.hasNext()) {
             msd = (MsgDb)ir.next();
             String t = StrUtil.getLeft(msd.getTitle(), uds.getWordCount());
             str += "<li><a title='" + StrUtil.toHtml(msd.getTitle()) + "' href='" + du.getPageShow() + "?fromWhere=oa&toWhere=forum&rootid=" + msd.getId() + "' target='_blank'>" + t + " [" + DateUtil.format(msd.getAddDate(), "yyyy-MM-dd") + "]</a></li>";
         }
         */
         long[] newMsgs = null;
         newMsgs = msd.getNewMsgs(sql, uds.getCount());
         if (newMsgs == null){
        	 return "<div class='no_content'><img title='论坛无内容' src='images/desktop/no_content.jpg'></div>";
         }
         int newMsgsLen = newMsgs.length;
         for (int i=0; i<newMsgsLen; i++) {
             msd = msd.getMsgDb((int)newMsgs[i]);
             String t = StrUtil.getLeft(msd.getTitle(), uds.getWordCount());
             str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(msd.getTitle()) + "' href='" + du.getPageShow() + "?fromWhere=oa&toWhere=forum&rootid=" + msd.getId() + "' target='_blank'>" + t + "</a></td><td class='article_time'>[" + DateUtil.format(msd.getAddDate(), "yyyy-MM-dd") + "]</td></tr>";
         }
 
         str += "</table>";
         return str;
    }
}
