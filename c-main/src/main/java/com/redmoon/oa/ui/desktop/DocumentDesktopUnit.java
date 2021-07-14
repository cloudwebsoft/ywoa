package com.redmoon.oa.ui.desktop;

import com.redmoon.oa.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.fileark.Document;
import cn.js.fan.util.StrUtil;

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

    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
         DesktopMgr dm = new DesktopMgr();
         com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageList() + StrUtil.UrlEncode(uds.getModuleItem());
         return url;
     }

     public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
    	 String str = "";
         if (!StrUtil.isNumeric(uds.getModuleItem())){
        	 str = "<div class='no_content'><img title='暂无文章' src='images/desktop/no_content.jpg'></div>";
             return str;
         }
         DesktopMgr dm = new DesktopMgr();
         com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageShow();

         str += "<ul>";
         
         Document doc = new Document();
         int id = doc.getFirstIDByCode(uds.getModuleItem());
         if (id!=-1) {
	         doc = doc.getDocument(id);
	         str += "<a title='" + StrUtil.toHtml(doc.getTitle()) + "' href='" + url + id + "'>" + StrUtil.getAbstract(request, doc.getContent(1), uds.getCount()) + "</a>";
         }
         str += "</ul>";
         return str;
    }
}
