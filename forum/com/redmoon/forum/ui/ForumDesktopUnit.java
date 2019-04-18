package com.redmoon.forum.ui;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.MsgDb;
import cn.js.fan.module.cms.ui.DesktopItemDb;
import cn.js.fan.module.cms.ui.DesktopMgr;
import cn.js.fan.module.cms.ui.DesktopUnit;
import cn.js.fan.module.cms.ui.IDesktopUnit;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.ThreadBlockIterator;
import com.redmoon.forum.ForumDb;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.forum.ForumSkin;
import com.redmoon.forum.MsgMgr;
import com.redmoon.forum.Leaf;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.base.ObjectBlockIterator;
import java.util.HashMap;
import cn.js.fan.web.Global;

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

    public String getPageList(HttpServletRequest request, DesktopItemDb uds) {
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageList();

         String boardcode = uds.getModuleItem();
         url += "boardcode=" + StrUtil.UrlEncode(boardcode);
         return url;
     }

     public String displayOnlineRank(HttpServletRequest request, DesktopItemDb di) {
         String str = "<ul>";
         UserDb ud = new UserDb();
         ObjectBlockIterator oi = ud.listUserRank("online_time", di.getCount());
         String rootPath = request.getContextPath();
         while (oi.hasNext()) {
             ud = (UserDb) oi.next();
             str += "<li>";
             str += "<a href='" + rootPath + "/userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>";
             str += "</li>";
         }
         str += "</ul>";
         return str;
     }

     public String displayExperienceRank(HttpServletRequest request, DesktopItemDb di) {
         String str = "<ul>";
         UserDb ud = new UserDb();
         ObjectBlockIterator oi = ud.listUserRank("experience", di.getCount());
         String rootPath = request.getContextPath();
         while (oi.hasNext()) {
             ud = (UserDb) oi.next();
             str += "<li>";
             str += "<a  href='" + rootPath + "/userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>";
             str += "</li>";
         }
         str += "</ul>";
         return str;
     }

     public String displayBoardRank(HttpServletRequest request, DesktopItemDb di) {
         String str = "<ul>";
         Leaf lf = new Leaf();
         Iterator bir = lf.getBoardsByTodayPost(di.getCount()).iterator();
         int i = 1;
         String rootPath = Global.getRootPath();
         if (request!=null)
             rootPath = request.getContextPath();
         while (bir.hasNext()) {
             lf = (Leaf) bir.next();
             str += "<li>";
             str += i + ".『 <a href='" + rootPath + "/forum/listtopic.jsp?boardcode=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a> 』" + lf.getTodayCount();
             str += "</li>";
             i++;
         }
         str += "</ul>";
         return str;
     }

     public String displayHot(HttpServletRequest request, DesktopItemDb di) {
         String str = "<ul>";
         MsgMgr mm = new MsgMgr();
         MsgDb md = null;
         Home home = Home.getInstance();
         int[] v = home.getHotIds();
         int hotlen = v.length;
         if (hotlen != 0) {
             HashMap props = di.getProps();
             boolean isDateShow = false;
             String dateFormat = "";
             String dt = (String)props.get("date");
             if (dt!=null) {
                 isDateShow = dt.equals("true") || dt.equals("yes");
                 dateFormat = (String)props.get("dateFormat");
                 if (dateFormat==null) {
                     dateFormat = "yy-MM-dd";
                 }
             }

             String rootPath = Global.getRootPath();
             if (request!=null)
             rootPath = request.getContextPath();

             for (int k = 0; k < hotlen; k++) {
                 md = mm.getMsgDb(v[k]);
                 if (md.isLoaded()) {
                     str += "<li><a href='" + rootPath +
                             "/forum/showtopic.jsp?rootid=" + md.getId() + "' title='" + md.getTitle() + "'>";
                     if (!md.getColor().equals(""))
                         str += "<font color='" + md.getColor() + "'>";
                     if (md.isBold())
                         str += "<b>";
                     str += StrUtil.getLeft(md.getTitle(), di.getTitleLen());
                     if (md.isBold())
                         str += "</b>";
                     if (!md.getColor().equals(""))
                         str += "</font>";
                     str += "</a>&nbsp;";
                     if (isDateShow) {
                         str += " [" +
                                 DateUtil.format(md.getAddDate(), dateFormat) +
                                 "]";
                     }
                     str += "</li>";
                 }
             }
         }
         str += "</ul>";
         return str;
     }

     public String displayNotice(HttpServletRequest request, DesktopItemDb uds) {
         String str = "";
         ForumDb fd = ForumDb.getInstance();
         Vector v = fd.getAllNotice();
         Iterator ir = v.iterator();
         String rootPath = Global.getRootPath();
         if (request!=null)
             rootPath = request.getContextPath();
         while (ir.hasNext()) {
             MsgDb md = (MsgDb) ir.next();
             str += "<a href='" + rootPath +
                     "/forum/showtopic.jsp?rootid=" +
                     md.getId() + "' target='_blank'>";
             if (!md.getColor().equals(""))
                 str += "<font color='" + md.getColor() + "'>";
             if (md.isBold())
                 str += "<b>";
             str += md.getTitle();
             if (md.isBold())
                 str += "</b>";
             if (!md.getColor().equals(""))
                 str += "</font>";
             str += " [" + DateUtil.format(md.getAddDate(), "yyyy-MM-dd") +
                     "]</a>&nbsp;&nbsp;&nbsp;";

         }
         return str;
     }

     public String display(HttpServletRequest request, DesktopItemDb di) {
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
         String str = "";
         str += "<ul>";
         if (di.getModuleItem().equals("newTopic")) {
             HashMap props = di.getProps();
             boolean isDateShow = false;
             String dateFormat = "";
             String dt = (String)props.get("date");
             if (dt!=null) {
                 isDateShow = dt.equals("true") || dt.equals("yes");
                 dateFormat = (String)props.get("dateFormat");
                 if (dateFormat==null) {
                     dateFormat = "yy-MM-dd";
                 }
             }

             String rootPath = request.getContextPath();

             MsgDb md = new MsgDb();
             String topmsgsql = "select id from sq_thread order by lydate desc";
             long[] newMsgs = md.getNewMsgs(topmsgsql, di.getCount());
             int newMsgsLen = newMsgs.length;
             for (int i = 0; i < newMsgsLen; i++) {
                 md = md.getMsgDb((int) newMsgs[i]);
                 str += "<li><a href='" + rootPath + "/" + du.getPageShow() + "?rootid=" +
                         md.getId() + "' target='_blank' title='" + StrUtil.toHtml(md.getTitle()) + "'>" + StrUtil.toHtml(StrUtil.getLeft(md.getTitle(),di.getTitleLen()));
                 if (isDateShow) {
                     str += " [" +
                             DateUtil.format(md.getAddDate(), dateFormat) +
                             "]";
                 }
                 str += "</a></li>";
             }
         }
         else if (di.getModuleItem().equals("notice")) {
             return displayNotice(request, di);
         }
         else if (di.getModuleItem().equals("hot")) {
             return displayHot(request, di);
         } else if (di.getModuleItem().equals("boardRank")) {
             return displayBoardRank(request, di);
         } else if (di.getModuleItem().equals("onlineTimeRank")) {
             return displayOnlineRank(request, di);
         } else if (di.getModuleItem().equals("experienceRank")) {
             return displayExperienceRank(request, di);
         } else if (di.getModuleItem().equals("flashImages")) {
             return dispalyFlashImages();
         }
         else {
             String boardcode = di.getModuleItem();

             HashMap props = di.getProps();
             boolean isDateShow = false;
             String dateFormat = "";
             String dt = (String)props.get("date");
             if (dt!=null) {
                 isDateShow = dt.equals("true") || dt.equals("yes");
                 dateFormat = (String)props.get("dateFormat");
                 if (dateFormat==null) {
                     dateFormat = "yy-MM-dd";
                 }
             }

             String rootPath = request.getContextPath();

             String sql = "select id from sq_thread where boardcode=" +
                   StrUtil.sqlstr(boardcode) + " and check_status=" +
                   MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                   MsgDb.LEVEL_TOP_BOARD +
                          " ORDER BY msg_level desc,redate desc";
             MsgDb md = new MsgDb();
             ThreadBlockIterator irmsg = md.getThreads(sql, boardcode, 0, di.getCount());
             while (irmsg.hasNext()) {
                 md = (MsgDb) irmsg.next();
                 str += "<li><a href='" + rootPath + "/" + du.getPageShow() + "?rootid=" +
                         md.getId() + "' target='_blank' title='" + StrUtil.toHtml(md.getTitle()) + "'>";
                 if (!md.getColor().equals(""))
                     str += "<font color='" + md.getColor() + "'>";
                 if (md.isBold())
                     str += "<b>";
                 str += StrUtil.toHtml(StrUtil.getLeft(md.getTitle(), di.getTitleLen()));
                 if (md.isBold())
                     str += "</b>";
                 if (!md.getColor().equals(""))
                     str += "</font>";
                 if (isDateShow) {
                     str += " [" +
                             DateUtil.format(md.getAddDate(), dateFormat) +
                             "]";
                 }
                 str += "</a></li>";
             }
         }
         str += "</ul>";

         return str;
    }

    public String dispalyFlashImages() {
        Home home = Home.getInstance();
        String str = "<script>";
        for (int i = 1; i <= 5; i++) {
            str += "imgUrl" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "url")) + "\";\n";
            str += "imgtext" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "text")) + "\";\n";
            str += "imgLink" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "link")) + "\";\n";
        }
        str += "</script>";
        return str;
    }
}
