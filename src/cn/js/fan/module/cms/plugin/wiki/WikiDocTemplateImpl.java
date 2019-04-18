package cn.js.fan.module.cms.plugin.wiki;

import java.util.*;

import cn.js.fan.module.cms.*;
import cn.js.fan.util.*;
import com.redmoon.oa.fileark.Document;
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
public class WikiDocTemplateImpl extends VarPart {
    public WikiDocTemplateImpl() {

    }

    public String write(HttpServletRequest request, WikiDocumentDb doc) {
        BeanUtil bu = new BeanUtil();
        Object obj = null;

        if (field.equalsIgnoreCase("content")) {
            String pageNum = (String) ParamUtil.get(request, "pageNum");
            if (!StrUtil.isNumeric(pageNum)) {
                pageNum = (String) request.getAttribute("pageNum");
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
            WikiDocumentDb doc = new WikiDocumentDb();
            doc = doc.getWikiDocumentDb(id);
            // LogUtil.getLog(getClass()).info("toString:id=" + id + " doc=" + doc);
            return write(request, doc);
        } else if (keyName.equalsIgnoreCase("dirCode")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(keyValue);
            int id = lf.getDocID();
            WikiDocumentDb doc = new WikiDocumentDb();
            doc = doc.getWikiDocumentDb(id);
            return write(request, doc);
        } else
            return "Key " + keyName + " is invalid!";
    }

}
