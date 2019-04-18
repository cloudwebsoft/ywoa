package cn.js.fan.module.cms.site;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.pvg.Privilege;
import com.redmoon.forum.person.UserDb;

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
public class SitePrivilege {
    public SitePrivilege() {
    }

    /**
     * 能否管理站点
     * @param request HttpServletRequest
     * @param sd SiteDb
     * @return boolean
     */
    public static boolean canManage(HttpServletRequest request, SiteDb sd) {
        if (sd == null)
            return false;
        Privilege privilege = new Privilege();
        if (privilege.isUserPrivValid(request, "admin"))
            return true;
        if (!privilege.isUserLogin(request))
            return false;
        String nick = privilege.getUser(request);
        UserDb ud = new UserDb();
        ud = ud.getUserDbByNick(nick);
        if (ud==null)
            return false;
        boolean re = sd.getString("owner").equals(ud.getName());
        if (re)
        	return true;
        SiteManagerDb smd = new SiteManagerDb();
        Iterator ir = smd.getSiteManagerDbs(sd.getString("code")).iterator();
        while (ir.hasNext()) {
        	smd = (SiteManagerDb)ir.next();
        	if (smd.getString("user_name").equals(ud.getName())) {
        		return true;
        	}
        }
        return false;

    }

}
