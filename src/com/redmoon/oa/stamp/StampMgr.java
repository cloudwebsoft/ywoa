package com.redmoon.oa.stamp;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public class StampMgr {
    public StampMgr() {
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        StampForm lf = new StampForm();
        lf.checkAdd(application, request);
        userNames = lf.getUserNames();

        StampDb ld = new StampDb();
        ld.setTitle(lf.getTitle());
        ld.setKind(lf.getKind());
        ld.setUserNames(lf.getUserNames());
        ld.setPwd(lf.getPwd());
        ld.setRoleCodes(lf.getRoleCodes());
        boolean re = ld.create(lf.fileUpload);
        if (re) {
        	String[] ary = StrUtil.split(lf.getUserNames(), ",");
        	if (ary!=null) {
            	StampPriv sp = new StampPriv();
        		for (int i=0; i<ary.length; i++) {
        			sp.add(ary[i], StampPriv.TYPE_USER, ld.getId());
        		}
        	}
        	ary = StrUtil.split(lf.getRoleCodes(), ",");
        	if (ary!=null) {
            	StampPriv sp = new StampPriv();
        		for (int i=0; i<ary.length; i++) {
        			sp.add(ary[i], StampPriv.TYPE_ROLE, ld.getId());
        		}
        	}        	
        }
        return re;
    }

    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        StampForm lf = new StampForm();
        lf.checkDel(request);

        StampDb ld = new StampDb();
        ld = ld.getStampDb(lf.getId());
        boolean re = ld.del(new JdbcTemplate());
        
        if (re) {
        	// 删除对应的权限表
        	StampPriv sp = new StampPriv();
        	sp.delPrivsOfStamp(lf.getId());
        }
        return re;
    }

    public boolean move(HttpServletRequest request) throws ErrMsgException {
        StampForm lf = new StampForm();
        lf.checkMove(request);

        StampDb ld = new StampDb();
        ld = ld.getStampDb(lf.getId());
        boolean re = ld.move(lf.getDirection());
        return re;
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        StampForm lf = new StampForm();
        lf.checkModify(application, request);
        userNames = lf.getUserNames();

        StampDb ld = new StampDb();
        ld = ld.getStampDb(lf.getId());
        ld.setTitle(lf.getTitle());
        ld.setKind(lf.getKind());
        ld.setUserNames(lf.getUserNames());
        ld.setPwd(lf.getPwd());
        ld.setRoleCodes(lf.getRoleCodes());

        boolean re = ld.save(lf.getFileUpload());
        if (re) {
        	StampPriv sp = new StampPriv();
        	sp.delPrivsOfStamp(lf.getId());
        	String[] ary = StrUtil.split(lf.getUserNames(), ",");
        	if (ary!=null) {
        		for (int i=0; i<ary.length; i++) {
        			sp.add(ary[i], StampPriv.TYPE_USER, lf.getId());
        		}
        	}
        	ary = StrUtil.split(lf.getRoleCodes(), ",");
        	if (ary!=null) {
        		for (int i=0; i<ary.length; i++) {
        			sp.add(ary[i], StampPriv.TYPE_ROLE, lf.getId());
        		}
        	}           	
        }
        return re;
    }

    public StampDb getStampDb(int id) {
        StampDb ld = new StampDb();
        return ld.getStampDb(id);
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getUserNames() {
        return userNames;
    }

    private String userNames;

}
