package com.redmoon.oa.ui.menu;

import com.cloudwebsoft.framework.base.*;
import java.util.Vector;
import java.util.Iterator;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

public class MostRecentlyUsedMenuDb extends QObjectDb {
    public static final int MAXIMUM = 10;

    public MostRecentlyUsedMenuDb() {
    }
 
    public Vector<MostRecentlyUsedMenuDb> getMenuBeingStored(String userName) {
        MostRecentlyUsedMenuDb mrum = new MostRecentlyUsedMenuDb();
        String sql = "select id from " + mrum.getTable().getName() + " where user_name=? order by times";
        return mrum.list(sql, new Object[]{userName});
    }

    public boolean isMenuBeingStored(String menuCode, String userName) {
        MostRecentlyUsedMenuDb mrum = new MostRecentlyUsedMenuDb();
        String sql = "select id from " + mrum.getTable().getName() + " where menu_code=? and user_name=?";
        return !mrum.list(sql, new Object[]{menuCode, userName}).isEmpty();
    }

    public void storeMenu(String menuCode, String userName) {
        MostRecentlyUsedMenuDb mrum = null;
        //判断菜单是否已经被保存
        boolean isMenuBeingStored = isMenuBeingStored(menuCode, userName);
        Vector<MostRecentlyUsedMenuDb> v = getMenuBeingStored(userName);
        if(isMenuBeingStored) {
            //如果菜单已经被保存，此菜单的times归零，该用户其他保存的菜单times + 1
            Iterator<MostRecentlyUsedMenuDb> iterator = v.iterator();
            while (iterator.hasNext()) {
                mrum = iterator.next();
                int times = menuCode.equals(mrum.getString("menu_code")) ? 0 : mrum.getInt("times") + 1;
                mrum.set("times", new Integer(times));
                try {
                    mrum.save();
                } catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error(e.getMessage());
                }
            }
        } else {
            //如果菜单没有已经被保存，先保存菜单并置其times为零
            mrum = new MostRecentlyUsedMenuDb();
            long id = SequenceManager.nextID(SequenceManager.OA_MENU_MOST_RECENTLY_USED);
            try {
                mrum.create(new JdbcTemplate(), new Object[] {
                    new Long(id),
                    menuCode,
                    new Integer(0),
                    userName
                });
            } catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            if (v.size() == MAXIMUM) {
                //如果保存的菜单数量已经没达到限定值，删除该用户保存的菜单中times值最大的菜单
                mrum = v.lastElement();
                v.removeElement(mrum);
                try {
                    mrum.del();
                } catch(ResKeyException e) {
                    LogUtil.getLog(getClass()).error(e.getMessage());
                }
            }
            //该用户其他保存的菜单的times值 + 1
            Iterator<MostRecentlyUsedMenuDb> iterator = v.iterator();
            while (iterator.hasNext()) {
                mrum = iterator.next();
                mrum.set("times", new Integer(mrum.getInt("times") + 1));
                try {
                    mrum.save();
                } catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error(e.getMessage());
                }
            }
        }
    }
}
