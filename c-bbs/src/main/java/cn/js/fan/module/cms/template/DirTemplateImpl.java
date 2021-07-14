package cn.js.fan.module.cms.template;

import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import cn.js.fan.module.cms.Document;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.LeafChildrenCacheMgr;
import cn.js.fan.module.cms.Leaf;

/**
 * <p>Title:目录列表模板 </p>
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
public class DirTemplateImpl extends ListPart {
    String dirCode = "";

    public DirTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);

        dirCode = StrUtil.getNullStr((String) props.get("dirCode"));
        int row = StrUtil.toInt((String)props.get("row"), 10);

        String dir = dirCode;

        if (dirCode!=null) {
           if (dirCode.startsWith("request")) {
               int p = dirCode.indexOf(".");
               if (p != -1) {
                   String param = dirCode.substring(p + 1);
                   // LogUtil.getLog(getClass()).info("param=" + param + " request=" + request);
                   dir = ParamUtil.get(request, param);
               }
           }
           else if (dirCode.equals("auto")) {
               // 根据接收的文章id，取其所在目录的父目录，列出该父目录中的子目录，即列出与id的父目录平级的目录
               int id = ParamUtil.getInt(request, "id", -1);
               if (id==-1) {
                   id = StrUtil.toInt((String)request.getAttribute("id"), -1); // 生成静态页面时
               }

               if (id == -1) {
                   dir = ParamUtil.get(request, "dirCode");
               } else {
                   Document doc = new Document();
                   doc = doc.getDocument(id);
                   dir = doc.getDirCode();
                   Leaf lf = new Leaf();
                   lf = lf.getLeaf(dir);
                   // 取得当前文章的父目录
                   dir = lf.getParentCode();
               }
           }
        }

        // System.out.println(getClass() + " dir=" + dir);
        if (dir.equals("")) {
            dir = (String)request.getAttribute("dirCode");
        }

        if (dir==null || dir.equals(""))
            throw new IllegalArgumentException("DirTemplateImpl " + getName() + "'s Leaf of dirCode=" + dirCode + " is null or empty.");

        // 是否为显示父节点下的孩子节点
        String parent = (String)props.get("parent");
        if (parent!=null) {
            if (parent.equalsIgnoreCase("y") || parent.equalsIgnoreCase("true")) {
                Leaf lf = new Leaf();
                lf = lf.getLeaf(dir);
                if (lf==null)
                    throw new IllegalArgumentException("DirTemplateImpl " + getName() + "'s Leaf of dirCode=" + dirCode + " is null.");

                dir = lf.getParentCode();
            }
        }

        // System.out.println(getClass() + " parent=" + parent + " dir=" + dir);

        LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(dir);
        Vector v = lccm.getDirList();
        Iterator ir = v.iterator();

        // LogUtil.getLog(getClass()).info("query=" + query);

        StringBuffer buf = new StringBuffer();

        // System.out.println(getClass() + " dir=" + dir);

        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
        int k=0;
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            if (lf.getIsHome()) {
                int nSteps = steps.size();
                // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

                for (int i = 0; i < nSteps; i++) {
                    ITemplate step = (ITemplate) steps.get(i);
                    // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                    if (step instanceof FieldPart) {
                        // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                        FieldPart fp = (FieldPart)step;
                        // System.out.println(getClass() + " isHtml=" + isHtml);
                        if (fp.getName().equalsIgnoreCase("linkUrl")) {
                            // @task 文章型节点的处理
                            if (isHtml) {
                                if (lf.getType() == Leaf.TYPE_DOCUMENT) {
                                    Document doc = new Document();
                                    doc = doc.getDocumentByDirCode(lf.getCode());
                                    if (Global.virtualPath.equals(""))
                                        buf.append("/" +
                                                doc.getDocHtmlName(1));
                                    else
                                        buf.append("/" + Global.virtualPath +
                                                "/" + doc.getDocHtmlName(1));
                                }
                                else {
                                    if (Global.virtualPath.equals(""))
                                        buf.append("/" +
                                                lf.
                                                getListHtmlNameByPageNum(request,
                                                1));
                                    else
                                        buf.append("/" + Global.virtualPath +
                                                "/" +
                                                lf.
                                                getListHtmlNameByPageNum(request,
                                                1));
                                }
                            }
                            else {
                                if (lf.getType()==Leaf.TYPE_DOCUMENT) {
                                    buf.append(request.getContextPath() +
                                               "/doc_view.jsp?dirCode=" +
                                               StrUtil.UrlEncode(lf.getCode()));
                                }
                                else {
                                    buf.append(request.getContextPath() +
                                               "/doc_list_view.jsp?dirCode=" +
                                               StrUtil.UrlEncode(lf.getCode()));
                                }
                            }
                        }
                        else if (fp.getName().equalsIgnoreCase("logo")) {
                            if (!lf.getLogo().equals("")) {
                                // System.out.println(getClass() + " lf.getListHtmlNameByPageNum(request, 1)=" + lf.getListHtmlNameByPageNum(request, 1));
                                // @task 文章型节点的处理
                                buf.append("<a href='");
                                if (isHtml) {
                                    if (Global.virtualPath.equals(""))
                                        buf.append(
                                                "/" +
                                                lf.
                                                getListHtmlNameByPageNum(request,
                                                1));
                                    else
                                        buf.append(
                                                "/" + Global.virtualPath + "/" +
                                                lf.getListHtmlNameByPageNum(
                                                request, 1));
                                }
                                else {
                                    buf.append(request.getContextPath() + "/doc_list_view.jsp?dirCode=" + StrUtil.UrlEncode(lf.getCode()));
                                }
                                buf.append("'>");
                                buf.append("<img border=0 src='" +
                                           request.getContextPath() + "/" +
                                           lf.getLogo() + "'/>");
                                buf.append("</a>");
                            }
                        }
                        else {
                            // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                            buf.append(((FieldPart) step).write(lf));
                        }
                    } else if (step instanceof ListPart) {

                        request.setAttribute("curDirCode", lf.getCode());

                        ListPart listPart = (ListPart) step;
                        String dynName = listPart.getName();
                        buf.append("<!-- BEGIN:" + dynName + "-->\n");
                        buf.append(listPart.toString(request, null));
                        buf.append("<!-- END:" + dynName + "-->\n");
                    } else { // StaticPart or VariablePart or IgnoredPart
                        buf.append(step.toString(request, null));
                    }
                }
                k++;
                if (k>=row)
                    break;
            }
        }
        return buf.toString();
    }


}
