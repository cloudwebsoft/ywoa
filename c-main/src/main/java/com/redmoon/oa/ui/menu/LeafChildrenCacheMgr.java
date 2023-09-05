package com.redmoon.oa.ui.menu;

import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.utils.SpringUtil;

import java.util.List;
import java.util.Vector;

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
            v.addElement(lf.getFromMenu(menu, new Leaf()));
        }
        return v;
    }
}
