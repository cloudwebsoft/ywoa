package com.redmoon.blog;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.blog.ui.TemplateDb;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.template.TemplateLoader;

/**
 * <p>Title: 用户自定义模板</p>
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
public class BlogUserTemplateDb extends QObjectDb {
    public BlogUserTemplateDb() {
    }

    public boolean init(long blogId) {
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        TemplateDb td = new TemplateDb();
        td = td.getTemplateDb(StrUtil.toInt(ucd.getSkin()));
        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {
                new Long(blogId), td.getString("main_content"), td.getString("sub_content"), td.getString("common_content")
            });
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }
        return re;
    }

    public boolean resumeContent(String contentType) throws ResKeyException {
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(getLong("blog_id"));
        TemplateDb td = new TemplateDb();
        td = td.getTemplateDb(StrUtil.toInt(ucd.getSkin()));

        set(contentType, td.getString(contentType));
        boolean re =save();
        if (re) {
            if (contentType.equals("main_content")) {
                TemplateLoader.refreshTemplate(BlogUserTemplateDb.
                                               getTemplateCacheKey(ucd,
                        TemplateDb.TEMPL_TYPE_MAIN));
            }
            else {
                TemplateLoader.refreshTemplate(BlogUserTemplateDb.
                                               getTemplateCacheKey(ucd,
                        TemplateDb.TEMPL_TYPE_SUB));
            }
        }
        return re;
    }

    public BlogUserTemplateDb getBlogUserTemplateDb(long blogId) {
        BlogUserTemplateDb up = (BlogUserTemplateDb)getQObjectDb(new Long(blogId));
        // 考虑到升级的需要，如此处理，并不在用户注册的时候，自动为其添加User Prop记录，而是当用到时自动创建
        if (up==null) {
            init(blogId);
            return (BlogUserTemplateDb)getQObjectDb(new Long(blogId));
        }
        else
            return up;
    }

    /**
     * 生成用于缓存ITemplate的键值
     * @param ucd UserConfigDb
     * @param templateType String
     * @return String
     */
    public static String getTemplateCacheKey(UserConfigDb ucd, String templateType) {
        return "blog_user_templ_" + templateType + "_" + ucd.getId();
    }

}
