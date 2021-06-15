package com.redmoon.oa.ui.menu;

import cn.js.fan.web.Global;
import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.utils.SpringUtil;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import java.util.Iterator;

public class LeafChildrenCacheMgr {
    String parentCode;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
    }

    public Vector<Leaf> getChildren() {
        Leaf lf = new Leaf();
        MenuCache menuCache = SpringUtil.getBean(MenuCache.class);
        List<Menu> list = menuCache.getChildren(parentCode);
        Vector v = new Vector();
        for (Menu menu : list) {
            // System.out.println(getClass() + " menu=" + menu + " parentCode=" + parentCode);
            v.addElement(lf.getFromMenu(menu, new Leaf()));
        }
        return v;
    }
}
