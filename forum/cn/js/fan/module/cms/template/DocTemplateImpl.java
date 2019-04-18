package cn.js.fan.module.cms.template;

import java.util.*;

import cn.js.fan.module.cms.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * $doc.dirCode(code).summary 提取文章的摘要
 * $doc.id(id).title 提取文章的标题
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DocTemplateImpl extends VarPart {
    public DocTemplateImpl() {

    }

    public String write(HttpServletRequest request, Document doc) {
        BeanUtil bu = new BeanUtil();
        Object obj = null;
        if (field.equalsIgnoreCase("content")) {
            String pageNum = (String) ParamUtil.get(request, "CPages");
            if (!StrUtil.isNumeric(pageNum)) {
                pageNum = (String) request.getAttribute("CPages");
                if (pageNum == null)
                    pageNum = "1";
            }
            obj = doc.getContent(Integer.parseInt(pageNum));
            return format(obj, props);
        } else if (field.equals("vote")) { // 投票
            if (doc.getType() == 1) {
                DocPollDb mpd = new DocPollDb();
                mpd = (DocPollDb)mpd.getQObjectDb(new Integer(doc.getId()));
                if (mpd!=null) {
                    String ctlType = "radio";
                    if (mpd.getInt("max_choice") > 1)
                        ctlType = "checkbox";
                    Vector options = mpd.getOptions(doc.getId());
                    int len = options.size();

                    int[] re = new int[len];
                    int[] bfb = new int[len];
                    int total = 0;
                    int k = 0;
                    for (k = 0; k < len; k++) {
                        DocPollOptionDb opt = (DocPollOptionDb) options.
                                              elementAt(k);
                        re[k] = opt.getInt("vote_count");
                        total += re[k];
                    }
                    if (total != 0) {
                        for (k = 0; k < len; k++) {
                            bfb[k] = (int) Math.round((double) re[k] / total *
                                    100);
                        }
                    }

                    String str = "";
                    str += "<table>";
                    str += "<form action='" + request.getContextPath() +
                            "/doc_vote.jsp?op=vote&id=" + doc.getId() +
                            "' name=formvote method='post'>";
                    str += "<tr><td colspan='2'>";
                    java.util.Date epDate = mpd.getDate("expire_date");
                    if (epDate != null) {
                        str += "到期时间：" + DateUtil.format(epDate, "yyyy-MM-dd");
                    }
                    str += "</td><tr>";
                    for (k = 0; k < len; k++) {
                        DocPollOptionDb opt = (DocPollOptionDb) options.
                                              elementAt(k);

                        str += "<tr>";
                        str += "<td width=26>" + (k + 1) + "、</td>";
                        str +=
                                "<td width=720><input class='n' type=" +
                                ctlType + " name=votesel value='" +
                                k + "'>";
                        str += opt.getString("content") + "</td>";
                        str += "</tr>";
                    }
                    str += "<tr>";
                    str +=
                            "<td colspan='2' align=center><input type='submit' value=' 投  票 '>";
                    str += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    str +=
                            "<input name='btn' type='button' value='查看结果' onClick=\"window.location.href='" +
                            request.getContextPath() + "/doc_vote.jsp?id=" +
                            doc.getId() + "&op=view'\"></td>";
                    str += "</tr>";
                    str += "</form>";
                    str += "</table>";
                    return str;
                }
                else
                    return "";
            } else
                return "";
        } else if (field.equalsIgnoreCase("attachments")) {
            String pageNum = (String) ParamUtil.get(request, "CPages");
            if (!StrUtil.isNumeric(pageNum)) {
                pageNum = (String) request.getAttribute("CPages");
                if (pageNum == null)
                    pageNum = "1";
            }
            java.util.Vector attachments = doc.getAttachments(Integer.parseInt(
                    pageNum));
            java.util.Iterator ir = attachments.iterator();
            String str = "";
            cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
            boolean is_att_image_show = cfg.getBooleanProperty("cms.is_att_image_show");
            while (ir.hasNext()) {
                Attachment am = (Attachment) ir.next();
                // 如果图片附件不显示（当普通编辑方式时，以免与内容重复）
                if (!is_att_image_show) {
                    String ext = StrUtil.getFileExt(am.getDiskName());
                    if (ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp"))
                        continue;
                }
                str +=
                        "<table width='569'  border='0' cellspacing=0 cellpadding=0>";
                str += "<tr>";
                str +=
                        "<td width=91 height=26 align='right'><img src=" +
                        request.getContextPath() + "/images/attach.gif></td>";
                str +=
                        "<td>&nbsp; <a target=_blank href='" +
                        request.getContextPath() + "/doc_getfile.jsp?pageNum=" +
                        pageNum + "&id=" + doc.getId() + "&attachId=" +
                        am.getId() + "'>" + am.getName() +
                        "</a> &nbsp;下载次数&nbsp;<script src='/" +
                        Global.virtualPath +
                        "/inc/doc_att_down_count.jsp?pageNum=" + pageNum +
                        "&docId=" + doc.getId() + "&attachId=" + +am.getId() +
                        "'></script>";
                str += "</td>";
                str += "</tr></table>";
            }
            return str;
        } else if (field.equals("pageBlock")) {
            int pagesize = 1;
            int total = doc.getPageCountPlugin();
            DocPagniator paginator = new DocPagniator(request, total, pagesize);
            String isCreateHtml = StrUtil.getNullStr((String) request.
                    getAttribute("isCreateHtml"));
            if (isCreateHtml.equals("true")) {
                String pageNum = (String) ParamUtil.get(request, "CPages");
                if (!StrUtil.isNumeric(pageNum)) {
                    pageNum = (String) request.getAttribute("CPages");
                    if (pageNum == null)
                        pageNum = "1";
                }
                return paginator.getHtmlCurPageBlock(doc,
                        Integer.parseInt(pageNum));
            } else
                return paginator.getCurPageBlock("?id=" +
                                                 ParamUtil.get(request, "id") +
                                                 "&dirCode=" +
                                                 StrUtil.
                                                 UrlEncode(ParamUtil.
                        get(request, "dirCode")));
        } else if (field.equals("relatedDoc")) {
            String keywords = StrUtil.getNullStr(doc.getKeywords());
            StringBuffer str = new StringBuffer();
            if (!keywords.equals("")) {
                String sql = SQLBuilder.getDocRelateSql(keywords);
                cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                                  Config();
                boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
                Iterator ir = doc.getDocuments(sql, DocCacheMgr.FULLTEXT, 0, 10);
                str.append("<ul>");
                while (ir.hasNext()) {
                    Document document = (Document) ir.next();
                    str.append("<li>");
                    // 系统设置是否静态化
                    if (!isHtml) {
                        str.append("<a href='" +
                                request.getContextPath() + "/doc_view.jsp?id=" +
                                document.getId() + "'>" + document.getTitle() +
                                "</a>&nbsp;&nbsp;&nbsp;&nbsp;[" +
                                DateUtil.format(document.getCreateDate(),
                                                "yyyy-MM-dd") +
                                "]");
                    } else {
                        str.append("<a href='" +
                                request.getContextPath() + "/" +
                                document.getDocHtmlName(1) + "'>" +
                                document.getTitle() +
                                "</a>&nbsp;&nbsp;&nbsp;&nbsp;[" +
                                DateUtil.format(document.getCreateDate(),
                                                "yyyy-MM-dd") +
                                "]");
                    }
                    str.append("</li>");
                }
                str.append("</ul>");
            }
            return str.toString();
        } else if (field.equals("postComment")) {
            // System.out.println(getClass() + " doc=" + doc);
            cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
            boolean isDocRemarkable = cfg.getBooleanProperty("cms.isDocRemarkable");
            if (isDocRemarkable && doc.isCanComment()) {
                String str = "<a href='" + request.getContextPath() + "/doc_comment.jsp?id=" + doc.getId() + "'>【发表评论】";
                return str;
            }
            else
                return "";
        } else {
            obj = bu.getProperty(doc, field);
            return format(obj, props);
        }
    }

    public String toString(HttpServletRequest request, List params) {
        if (keyName.equalsIgnoreCase("id")) {
            // LogUtil.getLog(getClass()).info("toString:keyValue=" + keyValue);
            String kValue = parseKeyValueFromRequest(request);

            if (kValue.equals("")) {
                kValue = (String) request.getAttribute("id"); // 当添加修改文章自动生成静态页面时
            }

            if (kValue==null)
                kValue = keyValue;

            int id = Integer.parseInt(kValue);
            Document doc = new Document();
            doc = doc.getDocument(id);
            // LogUtil.getLog(getClass()).info("toString:id=" + id + " doc=" + doc);
            return write(request, doc);
        } else if (keyName.equalsIgnoreCase("dirCode")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(keyValue);
            int id = lf.getDocID();
            Document doc = new Document();
            doc = doc.getDocument(id);
            return write(request, doc);
        } else
            return "Key " + keyName + " is invalid!";
    }

}
