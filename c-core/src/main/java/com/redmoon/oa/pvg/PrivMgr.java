package com.redmoon.oa.pvg;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.utils.SpringUtil;
import org.apache.log4j.Logger;

import com.redmoon.oa.person.UserDb;

import java.util.Vector;
import java.util.Iterator;

public class PrivMgr {
    String connname;
    Logger logger = Logger.getLogger(PrivMgr.class.getName());

    public PrivMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            logger.info("PrivDb:默认数据库名为空！");
        }
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        PrivCheck pc = new PrivCheck();
        pc.checkAdd(request);

        PrivDb pv = new PrivDb();
        boolean re = pv.create(pc.getPriv(), pc.getDesc(), pc.getLayer());
        if (re) {
            PrivCache privCache = new PrivCache();
            privCache.refreshAllPrivs();
        }
        return re;
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        PrivCheck pc = new PrivCheck();
        pc.checkUpdate(request);

        PrivDb pv = new PrivDb();
        pv.setPriv(pc.getPriv());
        pv.setDesc(pc.getDesc());
        pv.setLayer(pc.getLayer());
        boolean re = pv.save();
        if (re) {
            PrivCache privCache = new PrivCache();
            privCache.refreshAllPrivs();
        }
        return re;
    }

    public PrivDb getPriv(String priv) {
        PrivDb pd = new PrivDb();
        return pd.getPrivDb(priv);
    }

    /*
    public PrivDb[] getAllPriv(HttpServletRequest request) {
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(request, "admin"))
            return getAllPriv();
        PrivDb pd = new PrivDb();
        // is_admin=0表示除去管理员才能赋予的权限
        String sql = "select priv from privilege where is_admin=0 order by priv asc";
        Vector v = pd.list(sql);
        PrivDb[] p = null;
        int count = v.size();
        if (count > 0) {
            p = new PrivDb[count];
            Iterator ir = v.iterator();
            int i = 0;
            while (ir.hasNext()) {
                pd = (PrivDb) ir.next();
                p[i] = pd;
                i++;
            }
            return p;
        } else {
            return null;
        }
    }
    */

    public PrivDb[] getAllPriv() {
        PrivCache pc = new PrivCache();
        return pc.getAllPrivs();
    }
}
