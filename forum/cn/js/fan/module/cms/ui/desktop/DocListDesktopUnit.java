package cn.js.fan.module.cms.ui.desktop;

import cn.js.fan.module.cms.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.ui.DesktopItemDb;
import cn.js.fan.module.cms.ui.DesktopMgr;
import cn.js.fan.module.cms.ui.DesktopUnit;
import cn.js.fan.util.StrUtil;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.module.cms.LeafPriv;
import cn.js.fan.module.cms.Document;
import java.util.Iterator;
import cn.js.fan.util.DateUtil;
import java.util.HashMap;
import cn.js.fan.web.Global;
import cn.js.fan.module.cms.Config;
import cn.js.fan.module.cms.SQLBuilder;
import cn.js.fan.db.Paginator;
import cn.js.fan.module.cms.template.ListDocPagniator;

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
public class DocListDesktopUnit implements IDesktopUnit  {
    public DocListDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, DesktopItemDb di) {
        // 静态页面
        Config cfg = new Config();
        boolean html_doc = cfg.getBooleanProperty("cms.html_doc");
        if (html_doc) {
            String dir_code = di.getModuleItem();
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dir_code);
            if (lf==null)
                return "";

            String sql = SQLBuilder.getDirDocListSql(dir_code);
            Document doc = new Document();
            int total = doc.getDocCount(sql);
            int pageSize = cfg.getIntProperty("cms.listPageSize");
            ListDocPagniator paginator = new ListDocPagniator(request, total, pageSize);

            int pageNo = paginator.pageNum2No(1);

            // System.out.println(getClass() + " getPageList: pageNo=" + pageNo);

            return lf.getListHtmlNameByPageNo(pageNo);
        }
        else {
            DesktopMgr dm = new DesktopMgr();
            DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
            String url = du.getPageList() + di.getModuleItem();
            return url;
        }
     }

     public String display(HttpServletRequest request, DesktopItemDb di) {
         String dir_code = di.getModuleItem();
         Leaf lf = new Leaf();
         lf = lf.getLeaf(dir_code);
         if (lf==null)
             return "";

         Document doc = new Document();
         int count = di.getCount();
         String sql = "select id from document where class1=" + StrUtil.sqlstr(dir_code) + " and examine=" + Document.EXAMINE_PASS + " order by doc_level desc, modifiedDate desc";
         Iterator ir = doc.list(sql, count).iterator();
         String str = "<ul>";
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());

         cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                           Config();
         boolean isHtml = cfg.getBooleanProperty("cms.html_doc");

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

         String url = "";

         String rootPath = Global.getRootPath();
         if (request!=null)
             rootPath = request.getContextPath();

         while (ir.hasNext()) {
             doc = (Document) ir.next();
             if (doc.getType()==Document.TYPE_LINK) {
                 url = doc.getSource();
             }
             else {
                 if (!isHtml)
                     url = "doc_view.jsp?id=" + doc.getId();
                 else
                     url = doc.getDocHtmlName(1);
             }
             if (DateUtil.compare(new java.util.Date(), doc.getExpireDate()) == 2) {
                 str += "<li>";
                 if (isDateShow) {
                     str += "<span style='float:right'>" +
                             DateUtil.format(doc.getModifiedDate(), dateFormat) + "</span>&nbsp;";
                 }
                 str += "<a href='" + url + "'>";
                 if (doc.isBold())
                     str += "<B>";
                 if (!doc.getColor().equals("")) {
                     str += "<font color='" + doc.getColor() + "'>";
                 }
                 str += StrUtil.toHtml(StrUtil.getLeft(doc.getTitle(), di.getTitleLen()));
                 if (!doc.getColor().equals(""))
                     str += "</font>";
                 if (doc.isBold())
                     str += "</B>";

                 str += "</a>";

                 if (doc.getIsNew()==1) {
                     str += "&nbsp;<img border=0 src='" + rootPath + "/images/i_new.gif'>";
                 }

                 str += "</li>";
             } else {
                 str += "<li>";
                 if (isDateShow) {
                     str += "<span style='float:right'>" +
                             DateUtil.format(doc.getModifiedDate(), dateFormat) +
                             "</span>&nbsp;";
                 }
                 str += "<a href='" + url + "'>" +
                         StrUtil.toHtml(StrUtil.getLeft(doc.getTitle(), di.getTitleLen()));
                 str += "</a>";
                 str += "</li>";
             }
         }
         str += "</ul>";
         return str;
    }
}
