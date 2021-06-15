package com.redmoon.blog.ui;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.base.QObjectBlockIterator;

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
public class TemplateDb extends QObjectDb {
    // public static final String basePath = "upfile/blog/template";

    // 因为main和sub模板需要解析为ITemplate，而通用模板只需替换，不需解析，所以不需设置常量
    public static String TEMPL_TYPE_MAIN = "main";
    public static String TEMPL_TYPE_SUB = "sub";

    public TemplateDb() {
    }

    public TemplateDb getTemplateDb(int id) {
        return (TemplateDb)getQObjectDb(new Integer(id));
    }

    /**
     * 生成当根据模板字符串，由TemplateLoader初始化模板时，其构造函数中传入的参数cacheKey
     * @return String
     */
    public String getCacheKey(String templateType) {
        return "blog_sys_templ_" + templateType + "_" + getInt("id");
    }

    /**
     * 根据顺序号，排序为第一的为默认模板
     * @return TemplateDb
     */
    public TemplateDb getDefaultTemplateDb() {
        String sql = "select id from " + table.getName() + " order by orders";
        QObjectBlockIterator qbi = (QObjectBlockIterator)getQObjects(sql, 0, 1);
        if (qbi.hasNext()) {
            return (TemplateDb)qbi.next();
        }
        return null;
    }
}
