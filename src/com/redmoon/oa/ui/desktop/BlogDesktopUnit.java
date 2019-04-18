package com.redmoon.oa.ui.desktop;

import com.redmoon.oa.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.ui.DesktopMgr;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.MsgDb;
import com.redmoon.blog.BlogDb;
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
public class BlogDesktopUnit implements IDesktopUnit{
    public BlogDesktopUnit() {
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
         long[] newMsgs = null;
         BlogDb bd = new BlogDb();
         bd = bd.getBlogDb();
         newMsgs = bd.getNewBlogMsgs(uds.getCount());
         if (newMsgs == null)
             return "<div class='no_content'><img title='暂无博客' src='images/desktop/no_content.jpg'></div>";
         int newMsgsLen = newMsgs.length;
         if (newMsgsLen>uds.getCount())
                 newMsgsLen = uds.getCount();
         for (int i=0; i<newMsgsLen; i++) {
                 msd = msd.getMsgDb((int)newMsgs[i]);
                 String t = StrUtil.getLeft(msd.getTitle(), uds.getWordCount());
                 str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(msd.getTitle()) + "' href='" + du.getPageShow() + "?fromWhere=oa&toWhere=blog&rootid=" + msd.getId() + "' target='_blank'>" + t + "</a></td><td class='article_time'>[" + DateUtil.format(msd.getAddDate(), "yyyy-MM-dd") + "]</a></li>";
         }
         str += "</table>";
         return str;
    }
}
