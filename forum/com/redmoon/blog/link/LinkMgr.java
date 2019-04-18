package com.redmoon.blog.link;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
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
        blogId = lf.getBlogId();

        LinkDb ld = new LinkDb();
        ld.setUrl(lf.getUrl());
        ld.setTitle(lf.getTitle());
        ld.setBlogId(lf.getBlogId());
        boolean re = ld.create(lf.fileUpload);
        return re;
    }

    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        LinkForm lf = new LinkForm();
        lf.checkDel(request);

        LinkDb ld = new LinkDb();
        ld = ld.getLinkDb(lf.getId());
        boolean re = ld.del();
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
        blogId = lf.getBlogId();

        LinkDb ld = new LinkDb();
        ld = ld.getLinkDb(lf.getId());
        ld.setUrl(lf.getUrl());
        ld.setTitle(lf.getTitle());
        ld.setBlogId(lf.getBlogId());

        boolean re = ld.save(lf.getFileUpload());
        return re;
    }

    public LinkDb getLinkDb(int id) {
        LinkDb ld = new LinkDb();
        return ld.getLinkDb(id);
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public long getBlogId() {
        return blogId;
    }

    private long blogId;

}
