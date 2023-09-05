package com.redmoon.oa.basic;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import javax.servlet.http.HttpServletRequest;

public class SelectKindMgr {

    public SelectKindMgr() {
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        String errmsg = "";

        int id = ParamUtil.getInt(request, "id");
        String name = ParamUtil.get(request, "name");
        if ("".equals(name)) {
            errmsg += "名称不能为空";
        }
        if (!"".equals(errmsg)) {
            throw new ErrMsgException(errmsg);
        }

        int orders = ParamUtil.getInt(request, "orders", 0);

        SelectKindDb wptd = getSelectKindDb(id);
        wptd.setName(name);
        wptd.setOrders(orders);
        return wptd.save();
    }

    public SelectKindDb getSelectKindDb(int id) {
        SelectKindDb addr = new SelectKindDb();
        return addr.getSelectKindDb(id);
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        boolean re;

        String errmsg = "";
        String name = ParamUtil.get(request, "name");
        if ("".equals(name)) {
            errmsg += "名称不能为空";
        }
        int orders = ParamUtil.getInt(request, "orders", 0);

        if (!"".equals(errmsg)) {
            throw new ErrMsgException(errmsg);
        }

        SelectKindDb wptd = new SelectKindDb();
        wptd.setName(name);
        wptd.setOrders(orders);
        re = wptd.create();
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        SelectKindDb selectKindDb = getSelectKindDb(id);
        if (selectKindDb == null || !selectKindDb.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }

        return selectKindDb.del();
    }
}
