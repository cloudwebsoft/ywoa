package cn.js.fan.module.cms.site;

import com.cloudwebsoft.framework.base.QObjectDb;
import java.util.Vector;
import com.cloudwebsoft.framework.base.QCache;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.SQLException;
import cn.js.fan.util.ResKeyException;

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
public class SiteScrollImgDb extends QObjectDb {
	public static String KIND_SCROLL = "scroll";
	public static String KIND_SWITCH = "switch";
	
    public SiteScrollImgDb() {
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        boolean re = super.save(jt, params);
        if (re) {
            try {
                QCache.getInstance().remove(getString("site_code") +
                        "_scrollImg", cacheGroup);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = super.del(jt);
        if (re) {
            try {
                QCache.getInstance().remove(getString("site_code") +
                        "_scrollImg", cacheGroup);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return re;
    }

    public Vector listOfSite(String siteCode, String kind) {
        Vector v = null;
        try {
            v = (Vector) QCache.getInstance().getFromGroup(siteCode +
                    "_scrollImg", cacheGroup);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (v==null) {
            String sql = "select id from " + getTable().getName() +
                         " where site_code=? and kind=? order by orders desc";
            v = list(sql, new Object[] {siteCode, kind});
            try {
                QCache.getInstance().putInGroup(siteCode + "_scrolImg",
                                                cacheGroup, v);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return v;
    }
}
