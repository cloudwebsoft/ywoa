package cn.js.fan.module.cms.plugin.img;

import java.util.*;

import cn.js.fan.module.cms.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import cn.js.fan.module.cms.template.DocPagniator;
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
public class ImgDocTemplateImpl extends VarPart {
    public ImgDocTemplateImpl() {

    }

    public String write(HttpServletRequest request, ImgDocumentDb doc) {
        BeanUtil bu = new BeanUtil();
        Object obj = null;

        if (field.equalsIgnoreCase("content")) {
            String pageNum = (String) ParamUtil.get(request, "CPages");
            if (!StrUtil.isNumeric(pageNum)) {
                pageNum = (String) request.getAttribute("CPages");
                if (pageNum == null)
                    pageNum = "1";
            }
            int pNum = Integer.parseInt(pageNum);
            Document dc = new Document();
            dc = dc.getDocument(doc.getDocId());
            if (pNum>dc.getPageCount())
                pNum = 1;
            obj = dc.getContent(pNum);
            return format(obj, props);
        }
        else if (field.equalsIgnoreCase("smallImg")) {
            String rootPath = "";
            if (!Global.virtualPath.equals(""))
            rootPath = "/" + Global.virtualPath;

            return "<img src='" + rootPath + "/" + doc.getSmallImg() + "'>";
        }
        else if (field.equalsIgnoreCase("image")) {
            String rootPath = "";
            if (!Global.virtualPath.equals(""))
                rootPath = "/" + Global.virtualPath;

            String[][] ary = doc.getImageAry();
            String str = "";
            if (doc.getPageType()==ImgDocumentDb.PAGE_TYPE_MULTI) {
                String pageNum = (String) ParamUtil.get(request, "CPages");
                if (!StrUtil.isNumeric(pageNum)) {
                    pageNum = (String) request.getAttribute("CPages");
                }
                int num = StrUtil.toInt(pageNum, 1);
                if (num>doc.getPageCount())
                    num = 1;

                String onClick = "";
                Document dc = new Document();
                dc = dc.getDocument(doc.getDocId());
                String isCreateHtml = StrUtil.getNullStr((String) request.
                        getAttribute("isCreateHtml"));
                if (isCreateHtml.equals("true")) {
                    if (num < ary.length) {
                        onClick = " title='下一页 " + ary[num][1] + "' onClick=\"window.location.href='" + rootPath +
                                  "/" + dc.getDocHtmlName(num + 1) +
                                  "'\"";
                    } else
                        onClick = " title='第一页 " + ary[0][1] + "' onClick=\"window.location.href='" + rootPath +
                                  "/" + dc.getDocHtmlName(1) +
                                  "'\"";
                } else {
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(dc.getDirCode());
                    if (num < ary.length) {
                        onClick =
                                " title='下一页 " + ary[num][1] + "' onClick=\"window.location.href='doc_view.jsp?id=" +
                                dc.getId() + "&CPages=" + (num + 1) +
                                "&dirCode=" + StrUtil.UrlEncode(dc.getDirCode()) +
                                "'\"";
                    } else
                        onClick =
                                " title='第一页 " + ary[0][1] + "' onClick=\"window.location.href='doc_view.jsp?id=" +
                                dc.getId() + "&dirCode=" + StrUtil.UrlEncode(dc.getDirCode()) +
                                "'\"";
                }
                str = "<div><img src='" + rootPath + "/" + ary[num - 1][0] +
                             "' style='cursor:hand'  " + onClick + "></div>";

                str += "<BR><div>" + ary[num - 1][1] + "</div>";
            }
            else {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    str += "<div><img src='" + rootPath + "/" + ary[i][0] +
                          "' style='cursor:hand'><div>";
                    str += "<BR><div align=left>" + ary[i][1] + "</div>";
                }
            }
            return str;
        }
        else {
            obj = bu.getProperty(doc, field);
            return format(obj, props);
        }
    }

    public String toString(HttpServletRequest request, List param) {
        if (keyName.equalsIgnoreCase("id")) {
            // LogUtil.getLog(getClass()).info("toString:keyValue=" + keyValue);
            String kValue = parseKeyValueFromRequest(request);

            if (kValue.equals("")) {
                kValue = (String) request.getAttribute("id"); // 当添加修改文章自动生成静态页面时
            }

            if (kValue==null)
                kValue = keyValue;

            int id = Integer.parseInt(kValue);
            ImgDocumentDb doc = new ImgDocumentDb();
            doc = doc.getImgDocumentDb(id);
            // LogUtil.getLog(getClass()).info("toString:id=" + id + " doc=" + doc);
            return write(request, doc);
        } else if (keyName.equalsIgnoreCase("dirCode")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(keyValue);
            int id = lf.getDocID();
            ImgDocumentDb doc = new ImgDocumentDb();
            doc = doc.getImgDocumentDb(id);
            return write(request, doc);
        } else
            return "Key " + keyName + " is invalid!";
    }

}
