package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.*;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.DefaultRender;

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
public class ListBlogMsgTemplateImpl extends ListPart {
    String query = "";

    public ListBlogMsgTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);

        String searchType = ParamUtil.get(request, "searchType");
        String keyword = ParamUtil.get(request, "keyword");
        String sql;

        String dirCode = StrUtil.getNullStr((String) props.get("dirCode"));

        if (searchType.equals("msgTitle")) {
            sql =
            "select id from sq_message where isBlog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and replyid=-1 and title like '%" +
                    keyword + "%' ORDER BY lydate desc";
        }
        else if (searchType.equals("msgContent")) {
            sql =
                    "select id from sq_message where isBlog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and replyid=-1 and content like '%" +
                    keyword + "%' ORDER BY lydate desc";
        }
        else {
            if (dirCode != null && dirCode.startsWith("request")) {
                int p = dirCode.indexOf(".");
                if (p != -1) {
                    String param = dirCode.substring(p + 1);
                    // LogUtil.getLog(getClass()).info("param=" + param + " request=" + request);
                    dirCode = ParamUtil.get(request, param);
                }
            }

            sql = StrUtil.getNullStr((String) props.get("query"));
            if (dirCode.equals(""))
                sql =
                        "select id from sq_thread where isBlog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " ORDER BY lydate desc";
            else
                sql = "select id from sq_thread where isBlog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and blog_dir_code=" +
                      StrUtil.sqlstr(dirCode) + " ORDER BY lydate desc";
        }

        String ps = (String)props.get("pageSize");
        if (ps!=null && !StrUtil.isNumeric(ps))
            throw new IllegalArgumentException("pageSize:" + ps +
                                               " is not a number");
        int pageSize = StrUtil.toInt(ps, 10);

        // 如果没有设置start和end，则根据传递的参数自动获取
        int curPage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

        ListResult lr = null;
        MsgDb md = new MsgDb();
        try {
            lr = md.list(sql, curPage, pageSize);
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("toString:" + e.getMessage());
            return "";
        }

        paginator = new Paginator(request, lr.getTotal(), pageSize);

        // LogUtil.getLog(getClass()).info("query=" + query);

        StringBuffer buf = new StringBuffer();
        Iterator ir = lr.getResult().iterator();

        while (ir.hasNext()) {
            md = (MsgDb) ir.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                if (step instanceof PaginatorPart) {
                    PaginatorPart pp = (PaginatorPart)step;
                    pp.setUrl("?dirCode=" + StrUtil.UrlEncode(dirCode) + "&searchType=" + searchType + "&keyword=" + StrUtil.UrlEncode(keyword));
                    // System.out.println(getClass() + " pp.writeCount=" + pp.writeCount);
                    String writeCount = (String)request.getAttribute(pp.hashCode() + "_WriteCount");
                    // 第一条数据
                    if ((i==0 || !ir.hasNext()) && writeCount==null) {
                        buf.append(pp.write(request, this));
                        request.setAttribute(pp.hashCode() + "_WriteCount", "y");
                    }
                    else
                        continue;
                }
                else if (step instanceof FieldPart) {
                    // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                    FieldPart fp = (FieldPart)step;
                    if (fp.getName().equals("title")) {
                        buf.append(DefaultRender.RenderFullTitle(request, md));
                    } else
                        buf.append(((FieldPart) step).write(md));
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
