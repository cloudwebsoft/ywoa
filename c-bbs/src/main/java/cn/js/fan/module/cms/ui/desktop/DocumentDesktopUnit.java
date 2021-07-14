package cn.js.fan.module.cms.ui.desktop;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.Document;
import cn.js.fan.util.StrUtil;
import cn.js.fan.module.cms.ui.DesktopItemDb;
import cn.js.fan.module.cms.ui.DesktopMgr;
import cn.js.fan.module.cms.ui.DesktopUnit;
import cn.js.fan.module.cms.ui.IDesktopUnit;

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
public class DocumentDesktopUnit implements IDesktopUnit{
    public DocumentDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, DesktopItemDb di) {
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
         String url = du.getPageList() + di.getModuleItem();
         return url;
     }

     public String display(HttpServletRequest request, DesktopItemDb di) {
         if (!StrUtil.isNumeric(di.getModuleItem()))
             return "标识非法";
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
         String url = du.getPageShow();
         String str = "";

         str += "<ul>";
         Document doc = new Document();
         doc = doc.getDocument(Integer.parseInt(di.getModuleItem()));
         str += "<a href='" + url + di.getModuleItem() + "'>" + StrUtil.getLeft(doc.getContent(1), di.getTitleLen()) + "</a>";
         str += "</ul>";
         return str;
    }
}
