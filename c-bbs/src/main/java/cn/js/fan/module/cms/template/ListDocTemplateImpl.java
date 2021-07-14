package cn.js.fan.module.cms.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.module.cms.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import cn.js.fan.web.Global;

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
public class ListDocTemplateImpl extends ListPart {
    String dirCode = "";
    String query = "";

    public ListDocTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);

        dirCode = StrUtil.getNullStr((String) props.get("dirCode"));
        String parentCode = StrUtil.getNullStr((String)props.get("parentCode"));
        boolean isListParent = false;
        if (!parentCode.equals("")) {
            isListParent = true;
            dirCode = parentCode;
        }

        // System.out.println(getClass() + " parentCode=" + parentCode + " " + isListParent);

        String dir = dirCode;

        if (dirCode.startsWith("request")) {
            int p = dirCode.indexOf(".");
            if (p != -1) {
                String param = dirCode.substring(p + 1);
                // LogUtil.getLog(getClass()).info("param=" + param + " request=" + request);
                dir = ParamUtil.get(request, param);
                if (dir.equals("")) {
                    dir = StrUtil.getNullStr((String)request.getAttribute(param));
                }
            }
        }

        if (dir.equals("")) {
            dir = (String) request.getAttribute("dirCode");
            if (dir == null) {
                // curDirCode用于循环体内，如栏目中，循环列出目录，同时列出目录中的文件
                dir = (String)request.getAttribute("curDirCode");
                if (dir==null) {
                    throw new IllegalArgumentException("ListDocTemplateImpl " +
                            getName() +
                            "'s dirCode is empty.");
                }
            }
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dir);
        if (lf == null) {
            return "目录" + dir + "不存在！";
        }

        // System.out.println(getClass() + " dir=" + dir);

        String s = (String) props.get("start");
        if (s != null && !StrUtil.isNumeric(s))
            throw new IllegalArgumentException("start:" + s +
                                               " is not a number");
        String e = (String) props.get("end");
        if (e != null && !StrUtil.isNumeric(e))
            throw new IllegalArgumentException("end:" + e + " is not a number");

        /*
        String ps = (String)props.get("pageSize");
        if (ps!=null && !StrUtil.isNumeric(ps))
            throw new IllegalArgumentException("pageSize:" + ps +
                                               " is not a number");
        */
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        int pageSize = cfg.getIntProperty("cms.listPageSize");

        int myStart = -1, myEnd = -1;
        int curPage = 1;

        boolean isCreateHtml = false;
        if (request==null)
            isCreateHtml = true;
        else
            isCreateHtml = StrUtil.getNullStr((String)request.getAttribute("isCreateHtml")).equals("true");

        query = StrUtil.getNullStr((String) props.get("query"));

        Document doc = new Document();
        Iterator ir = null;
        int total = 0;

        boolean isPreQuery = false;
        if (!query.equals("")) {
            isPreQuery = true;
            // @task:此处有问题
            total = doc.getDocCount(query);
            // doc.listResult(query, )
        } else {
            if (!isListParent)
                query = SQLBuilder.getDirDocListSql(dir);
            else
                query = SQLBuilder.getParentDirDocListSql(dir);

            // System.out.println(getClass() + " parentCode=" + parentCode + " query=" + query);

            total = doc.getDocCount(query);
        }

        if (request!=null)
            paginator = new Paginator(request, total, pageSize);

        // 如果没有设置start和end，则根据传递的参数自动获取
        if (s == null && e == null) {
            String cp = "";
            if (request!=null) {
                cp = ParamUtil.get(request, "CPages");
                if (cp.equals("")) {
                    cp = (String) request.getAttribute("CPages");
                }
            }
            curPage = StrUtil.toInt(cp, 1);
            myStart = (curPage - 1) * pageSize;
            myEnd = curPage * pageSize;

            if (isCreateHtml) {
                // 如果是第一页，则无需变化，第listPageHtmlCreateCount页以后要重新计算
                int listPageHtmlCreateCount = cfg.getIntProperty("cms.listPageHtmlCreateCount");
                if (curPage>listPageHtmlCreateCount) {
                    // 得到如果按动态分页方式，最后一页的条数可能<pageSize，取得不足的条数
                    int t = pageSize * paginator.getTotalPages() - total;
                    // 补足，使得最后一页是满的，从第二页以后都要补足，这样第一页和第二页之间可能会有重复，除非正好全部页都是满页
                    if (t>0) {
                        myStart -= t;
                        myEnd -= t;
                    }
                }
            }
        } else {
            myStart = Integer.parseInt(s);
            myEnd = Integer.parseInt(e);
        }
        if (isPreQuery) {
            ir = doc.list(query, myStart, myEnd).iterator();
        }
        else {
            if (!isListParent)
                ir = doc.getDocuments(query, dir, myStart, myEnd);
            else {
                // System.out.println(getClass() + " DocCacheMgr.getVisualDirWhileGetDocumentsFromParentDir(dir)=" + DocCacheMgr.getVisualDirWhileGetDocumentsFromParentDir(dir));
                ir = doc.getDocuments(query,
                        DocCacheMgr.
                     getVisualDirWhileGetDocumentsFromParentDir(dir), myStart,
                       myEnd);
            }
        }

        StringBuffer buf = new StringBuffer();

        boolean isHtml = cfg.getBooleanProperty("cms.html_doc");

        while (ir.hasNext()) {
            doc = (Document) ir.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                if (step instanceof PaginatorPart) {
                    PaginatorPart pp = (PaginatorPart) step;
                    // System.out.println(getClass() + " pp.writeCount=" + pp.writeCount);
                    String writeCount = (String) request.getAttribute(pp.
                            hashCode() + "_WriteCount_" + myStart);
                    // 第一条数据
                    if ((i == 0 || !ir.hasNext()) && writeCount == null) {
                        if (isCreateHtml) {
                            int pageNo = StrUtil.toInt(StrUtil.getNullStr((String)request.getAttribute("pageNo")), 1);
                            String pageStatics = "<script src=\"" + request.getContextPath() + "/inc/doc_list_page.jsp?op=statics&pageNo=" + pageNo + "&dirCode=" + StrUtil.UrlEncode(lf.getCode()) + "\"></script>";
                            String pageBlock = "<script src=\"" + request.getContextPath() + "/inc/doc_list_page.jsp?pageNo=" + pageNo + "&dirCode=" + StrUtil.UrlEncode(lf.getCode()) + "\"></script>";

                            buf.append(pp.write(request, this, pageStatics, pageBlock));
                            /*
                            ListDocPagniator paginator = new ListDocPagniator(
                                    request, total, pageSize);
                            buf.append(pp.write(request, this,
                                                paginator.
                                                getHtmlCurPageBlock(lf, curPage)));
                            */
                        } else {
                            pp.setUrl("?dirCode=" + StrUtil.UrlEncode(dir));
                            buf.append(pp.write(request, this));
                        }
                        request.setAttribute(pp.hashCode() + "_WriteCount_" + myStart, "y");
                    } else
                        continue;
                } else if (step instanceof FieldPart) {
                    // System.out.println(getClass() + " " + doc.getTitle());
                    FieldPart fp = (FieldPart) step;
                    if (fp.getName().equalsIgnoreCase("linkUrl")) {
                        if (doc.getType()==Document.TYPE_LINK) {
                            buf.append(doc.getSource());
                        }
                        else {
                            if (isHtml) {
                                if (Global.virtualPath.equals(""))
                                    buf.append("/" + doc.getDocHtmlName(1));
                                else
                                    buf.append("/" + Global.virtualPath + "/" +
                                               doc.getDocHtmlName(1));
                            } else {
                                buf.append(Global.getRootPath() +
                                           "/doc_view.jsp?id=" + doc.getId());
                            }
                        }
                    } else {
                        // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                        buf.append(((FieldPart) step).write(doc));
                    }
                } else if (step instanceof ListPart) {
                    ListPart listPart = (ListPart) step;
                    String dynName = listPart.getName();
                    buf.append("<!-- BEGIN:" + dynName + "-->\n");
                    buf.append(listPart.toString(request, null));
                    buf.append("<!-- END:" + dynName + "-->\n");
                } else { // StaticPart or VariablePart or IgnoredPart
                    buf.append(step.toString(request, null));
                }
            }
        }
        return buf.toString();
    }

}
