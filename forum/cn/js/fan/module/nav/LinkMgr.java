package cn.js.fan.module.nav;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import org.apache.log4j.*;

public class LinkMgr {
    Logger logger = Logger.getLogger(LinkMgr.class.getName());
    RMCache rmCache;

    public LinkMgr() {
        rmCache = RMCache.getInstance();
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        LinkForm lf = new LinkForm();
        lf.checkAdd(application, request);
        userName = lf.getUserName();

        LinkDb ld = new LinkDb();
        ld.setUrl(lf.getUrl());
        ld.setTitle(lf.getTitle());
        ld.setKind(lf.getKind());
        ld.setUserName(lf.getUserName());
        boolean re = ld.create(lf.fileUpload);
        return re;
    }

    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        LinkForm lf = new LinkForm();
        lf.checkDel(request);

        LinkDb ld = new LinkDb();
        ld = ld.getLinkDb(lf.getId());
        boolean re = ld.del(new JdbcTemplate());
        return re;
    }

    public boolean move(HttpServletRequest request) throws ErrMsgException {
        LinkForm lf = new LinkForm();
        lf.checkMove(request);

        LinkDb ld = new LinkDb();
        ld = ld.getLinkDb(lf.getId());
        boolean re = ld.move(lf.getDirection());
        return re;
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        LinkForm lf = new LinkForm();
        lf.checkModify(application, request);
        userName = lf.getUserName();

        LinkDb ld = new LinkDb();
        ld = ld.getLinkDb(lf.getId());
        ld.setUrl(lf.getUrl());
        ld.setTitle(lf.getTitle());
        ld.setKind(lf.getKind());
        ld.setUserName(lf.getUserName());

        boolean re = ld.save(lf.getFileUpload());
        return re;
    }

    public LinkDb getLinkDb(int id) {
        LinkDb ld = new LinkDb();
        return ld.getLinkDb(id);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    private String userName;

}
