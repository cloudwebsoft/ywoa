package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.*;
import com.redmoon.forum.MsgDb;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.person.*;

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
public class ListBlogUserTemplateImpl extends ListPart {
    String query = "";

    public ListBlogUserTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        String searchType = ParamUtil.get(request, "searchType");
        String keyword = ParamUtil.get(request, "keyword");
        if (!searchType.equals("") || !keyword.equals("")) {
            if (!SQLFilter.isValidSqlParam(keyword)) {
                return SkinUtil.makeErrMsg(request,
                                           SkinUtil.LoadString(request,
                        SkinUtil.ERR_SQL));
            }
        }
        String sql = "";
        if (searchType.equals("userNick")) {
            if (!keyword.equals("")) {
                UserMgr um = new UserMgr();
                UserDb ud = um.getUserDbByNick(keyword);
                if (ud == null) {
                    return "用户 " + keyword + " 不存在！";
                }
                sql =
                        "select id from blog_user_config where isValid=1 and userName=" +
                        StrUtil.sqlstr(ud.getName());
            }
            else {
                sql = "select id from blog_user_config where isValid=1";
            }
        }
        else {
            sql = "select id from blog_user_config where isValid=1";
        }

        sql += " order by addDate desc";

        String ps = (String) props.get("pageSize");
        if (ps != null && !StrUtil.isNumeric(ps))
            throw new IllegalArgumentException("pageSize:" + ps +
                                               " is not a number");

        UserConfigDb ucd = new UserConfigDb();
        int pagesize = 10;
        paginator = new Paginator(request);

        int total = ucd.getObjectCount(sql);
        paginator.init(total, pagesize);
        int curpage = paginator.getCurPage();
        //设置当前页数和总页数
        int totalpages = paginator.getTotalPages();
        if (totalpages == 0) {
            curpage = 1;
            totalpages = 1;
        }

        ListResult lr = null;
        Vector v = ucd.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);

        // LogUtil.getLog(getClass()).info("query=" + query);
        StringBuffer buf = new StringBuffer();
        Iterator ir = v.iterator();

        while (ir.hasNext()) {
            ucd = (UserConfigDb) ir.next();

            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                if (step instanceof PaginatorPart) {
                    PaginatorPart pp = (PaginatorPart) step;
                    pp.setUrl("?searchType=" + searchType + "&keyword=" +
                              StrUtil.UrlEncode(keyword));
                    // System.out.println(getClass() + " pp.writeCount=" + pp.writeCount);
                    String writeCount = (String) request.getAttribute(pp.
                            hashCode() + "_WriteCount");
                    // 第一条数据
                    if ((i == 0 || !ir.hasNext()) && writeCount == null) {
                        buf.append(pp.write(request, this));
                        request.setAttribute(pp.hashCode() + "_WriteCount", "y");
                    } else
                        continue;
                } else if (step instanceof FieldPart) {
                    // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                    buf.append(((FieldPart) step).write(ucd));
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
