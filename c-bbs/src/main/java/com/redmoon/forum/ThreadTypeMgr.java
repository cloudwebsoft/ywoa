package com.redmoon.forum;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import org.apache.log4j.Logger;

/**
 *
 * <p>Title: 版块子类别的代理类</p>
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
public class ThreadTypeMgr {
    Logger logger = Logger.getLogger(ThreadTypeMgr.class.getName());

    public ThreadTypeMgr() {
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        ThreadTypeCheck uc = new ThreadTypeCheck();
        uc.checkAdd(request);

        ThreadTypeDb nav = new ThreadTypeDb();
        boolean re = nav.create(uc.getName(), uc.getBoardCode(), uc.getColor(), uc.getOrders());
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        ThreadTypeCheck uc = new ThreadTypeCheck();
        uc.checkDel(request);

        ThreadTypeDb ttd = new ThreadTypeDb();
        ttd = ttd.getThreadTypeDb(uc.getId());
        boolean re = ttd.del();
        return re;
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        ThreadTypeCheck uc = new ThreadTypeCheck();
        uc.checkUpdate(request);

        ThreadTypeDb ttd = new ThreadTypeDb();
        ttd = ttd.getThreadTypeDb(uc.getId());
        ttd.setName(uc.getName());
        ttd.setColor(uc.getColor());
        ttd.setDisplayOrder(uc.getOrders());
        boolean re = ttd.save();
        return re;
    }

    public ThreadTypeDb getThreadTypeDb(int id) {
        ThreadTypeDb ttd = new ThreadTypeDb();
        return ttd.getThreadTypeDb(id);
    }

}
