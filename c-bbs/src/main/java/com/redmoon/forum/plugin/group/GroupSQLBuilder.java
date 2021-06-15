package com.redmoon.forum.plugin.group;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GroupSQLBuilder {
    public GroupSQLBuilder() {
    }

    public static String getListGroupPhotoSql(long groupId) {
        String sql = "select id from plugin_group_photo where group_id=" + groupId + " order by sort desc, id desc";;
        return sql;
    }

    public static String getListGroupSql(HttpServletRequest request) {
        String op = "";
        String catalogCode = "";

        // 生成博客首页静态页面时，request会为null
        if (request!=null) {
            op = ParamUtil.get(request, "op");
            catalogCode = ParamUtil.get(request, "catalogCode");
        }
        String sql = "";
        if (op.equals("search")) {
            String what = ParamUtil.get(request, "what");
            sql = "select id from plugin_group where name like " + StrUtil.sqlstr("%" + what + "%") + " order by recommand_point desc, id desc";
        }
        else {
            String listType = "";
            if (request!=null)
                listType = ParamUtil.get(request, "listType");
            if (listType.equals("")) {
                if (catalogCode.equals("")) {
                    sql = "select id from plugin_group order by recommand_point desc,id desc";
                } else {
                    GroupDb gd = new GroupDb();
                    sql = gd.getTable().getSql("listcatalog").replaceFirst(
                            "\\?",
                            StrUtil.sqlstr(catalogCode));
                }
            }
            else {
                if (listType.equals("member")) {
                    sql = "select id from plugin_group order by user_count desc";
                }
                else
                    sql = "select id from plugin_group order by msg_count desc";
            }
        }
        return sql;
    }
}
