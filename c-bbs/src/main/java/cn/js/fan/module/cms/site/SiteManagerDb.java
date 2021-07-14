package cn.js.fan.module.cms.site;

import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.module.cms.LeafPriv;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.person.UserDb;

public class SiteManagerDb extends QObjectDb {
	/**
	 * 取得子站点的所有管理员
	 * @param siteCode
	 * @return
	 */
	public Vector getSiteManagerDbs(String siteCode) {
		String sql = "select code, user_name from " + getTable().getName() + " where code=? order by orders";
		return list(sql, new Object[]{siteCode});
	}
	
	/**
	 * 取得用户被赋予为管理员的站点
	 * @param userName
	 * @return
	 */
	public Vector getSiteDbsManageredByUser(String userName) {
		String sql = "select code, user_name from " + getTable().getName() + " where user_name=? order by orders";
		Vector v = new Vector();
		Iterator ir = list(sql, new Object[]{userName}).iterator();
		SiteDb sd = new SiteDb();
		while (ir.hasNext()) {
			SiteManagerDb smd = (SiteManagerDb)ir.next();
			SiteDb sd2 = sd.getSiteDb(smd.getString("code"));
			if (sd2!=null)
				v.addElement(sd2);
		}
		return v;
	}
	
	/**
	 * 删除站点管理员
	 * @param siteCode
	 */
	public void delManagersOfSite(String siteCode) {
		Iterator ir = getSiteManagerDbs(siteCode).iterator();
		LeafPriv lp = new LeafPriv(siteCode);
		UserDb ud = new UserDb();
		while (ir.hasNext()) {
			SiteManagerDb smd = (SiteManagerDb)ir.next();
			try {
				// 删除管理员对于子站点内所有目录的管理权限
				ud = ud.getUser(smd.getString("user_name"));
				// lp.delUserPrivsOfSite(siteCode, ud.getNick());
				lp.delPrivsOfDir();
				smd.del();
			}
			catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
	}
}
