package cn.js.fan.module.cms;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ParamUtil;
import javax.servlet.http.HttpServletRequest;

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
public class SQLBuilder {
    public SQLBuilder() {
    }

    public static String getDocRelateSql(String keywords) {
        keywords = keywords.replaceAll("，", ",");
        String[] allkeys = StrUtil.split(keywords, ",");
        int len = allkeys.length;
        String keys = "";
        String sql = "select id from document";
        if (len==0)
            return sql;
        sql += " where";
        for (int i = 0; i < len; i++) {
            if (keys.equals(""))
                sql += " keywords like " +  StrUtil.sqlstr("%" + allkeys[i] + "%");
            else
                sql += " or keywords like " + StrUtil.sqlstr("%" + allkeys[i] + "%");
        }
        return sql;
    }

    public static String getDirDocListSql(String dirCode) {
        if (dirCode.equals("")) {
            return "select id from document where examine=" + Document.EXAMINE_PASS +
                    " order by doc_level desc, createDate desc";
        }
        else {
            return "select id from document where class1=" +
                    StrUtil.sqlstr(dirCode) +
                    " and examine=" + Document.EXAMINE_PASS +
                    " order by doc_level desc, createDate desc";
        }
    }

    /**
     * 列出父目录parentCode下面的子目录中的文章
     * @param parentCode String
     * @return String
     */
    public static String getParentDirDocListSql(String parentCode) {
        return "select id from document where parent_code=" +
                    StrUtil.sqlstr(parentCode) +
                    " and examine=" + Document.EXAMINE_PASS +
                    " order by doc_level desc, createDate desc";

    }

    /**
     * 用于JS提取
     * @param dirCode String
     * @return String
     */
    public static String getJSSql(HttpServletRequest request) {
        String sql = "";
        String dirCode = ParamUtil.get(request, "dircode");
	boolean ishot = ParamUtil.get(request, "ishot").equals("y");
        if (dirCode.equals("")) {
            if (ishot) {
                sql = "select id from document where examine=" +
                      Document.EXAMINE_PASS +
                      " order by doc_level desc, isHome desc, hit desc";
            }
            else {
                sql = "select id from document where examine=" +
                      Document.EXAMINE_PASS +
                      " order by doc_level desc, isHome desc, createDate desc";
            }
        } else {
            if (ishot) {
                sql = "select id from document where class1=" +
                      StrUtil.sqlstr(dirCode) +
                      " and examine=" + Document.EXAMINE_PASS +
                      " order by doc_level desc, isHome desc, hit desc";
            }
            else {
                sql = "select id from document where class1=" +
                      StrUtil.sqlstr(dirCode) +
                      " and examine=" + Document.EXAMINE_PASS +
                      " order by doc_level desc, isHome desc, createDate desc";
            }
        }
        return sql;
    }

    public static String getSubjectDocListSql(String subjectCode) {
        String sql="select s.doc_id from cws_cms_subject_doc s,document d where s.doc_id=d.id and s.code=" + StrUtil.sqlstr(subjectCode) + " and d.examine=" + Document.EXAMINE_PASS + " order by s.doc_level desc, s.create_date desc";
        return sql;
    }
}
