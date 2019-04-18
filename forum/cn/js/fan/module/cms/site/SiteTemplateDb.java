package cn.js.fan.module.cms.site;

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
public class SiteTemplateDb extends QObjectDb {
    // public static final String basePath = "upfile/cms/template";

    // 因为main和sub模板需要解析为ITemplate，而通用模板只需替换，不需解析，所以不需设置常量
    public static String TEMPL_TYPE_MAIN = "main"; // 主模板
    public static String TEMPL_TYPE_HOME = "home"; // 首页模板
    public static String TEMPL_TYPE_LIST = "list"; // 列表页模板
    public static String TEMPL_TYPE_DOC = "doc"; // 文章页模板

    public SiteTemplateDb() {
    }

    public SiteTemplateDb getSiteTemplateDb(int id) {
        return (SiteTemplateDb)getQObjectDb(new Integer(id));
    }

    /**
     * 生成当根据模板字符串，由TemplateLoader初始化模板时，其构造函数中传入的参数cacheKey
     * @return String
     */
    public String getCacheKey(String templateType) {
        return "cms_site_sys_templ_" + templateType + "_" + getInt("id");
    }

    /**
     * 根据顺序号，排序为第一的为默认模板
     * @return TemplateDb
     */
    public SiteTemplateDb getDefaultSiteTemplateDb() {
        String sql = "select id from " + table.getName() + " order by orders";
        QObjectBlockIterator qbi = (QObjectBlockIterator)getQObjects(sql, 0, 1);
        if (qbi.hasNext()) {
            return (SiteTemplateDb)qbi.next();
        }
        return null;
    }

}
