package com.redmoon.forum.plugin.group.photo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.ErrMsgException;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import cn.js.fan.util.ResKeyException;

public class PhotoMgr {
    Logger logger = Logger.getLogger(PhotoMgr.class.getName());
    RMCache rmCache;

    public PhotoMgr() {
        rmCache = RMCache.getInstance();
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException,ResKeyException {
        PhotoForm lf = new PhotoForm();
        lf.checkAdd(application, request);
        groupId = lf.getGroupId();

        PhotoDb ld = new PhotoDb();
        ld.setTitle(lf.getTitle());
        ld.setGroupId(lf.getGroupId());
        ld.setUserName(lf.getUserName());
        boolean re = ld.create(lf.fileUpload);
        return re;
    }

    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException,ResKeyException {
        PhotoForm lf = new PhotoForm();
        lf.checkDel(request);

        PhotoDb ld = new PhotoDb();
        ld = ld.getPhotoDb(lf.getId());
        boolean re = ld.del(Global.getRealPath());
        return re;
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        PhotoForm lf = new PhotoForm();
        lf.checkModify(application, request);
        groupId = lf.getGroupId();

        PhotoDb ld = new PhotoDb();
        ld = ld.getPhotoDb(lf.getId());
        ld.setTitle(lf.getTitle());
        ld.setGroupId(lf.getGroupId());
        ld.setSort(lf.getSort());

        boolean re = ld.save(lf.getFileUpload());
        return re;
    }

    public PhotoDb getPhotoDb(int id) {
        PhotoDb ld = new PhotoDb();
        return ld.getPhotoDb(id);
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getGroupId() {
        return groupId;
    }

    long groupId;
}
