package com.redmoon.oa.visual;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.db.PrimaryKey;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.flow.FormMgr;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ModuleRelateDb extends QObjectDb {
    public static int TYPE_MULTI = 0;
    public static int TYPE_SINGLE = 1;

    public ModuleRelateDb() {
        super();
    }

    /**
     * 取得关联的模块
     * @param code
     * @param relateCode 模块编码
     * @return
     */
    public ModuleRelateDb getModuleRelateDb(String code, String relateCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("code", code);
        pk.setKeyValue("relate_code", relateCode);
        return (ModuleRelateDb)getQObjectDb(pk.getKeys());
    }

    /**
     * 取得关联的子模块
     * @param mainFormCode String
     * @return Vector
     */
    public Vector<ModuleRelateDb> getModulesRelated(String mainFormCode) {
        String sql = "select code, relate_code from " + getTable().getName() + " where code=? order by relate_order";
        return list(sql, new Object[]{mainFormCode});
    }

    /**
     * 取得反向关联的主模块
     * @param relateCode String
     * @return Vector
     */
    public Vector getModuleReverseRelated(String relateCode) {
        String sql = "select code, relate_code from " + getTable().getName() + " where relate_code=? order by relate_order";
        return list(sql, new Object[]{relateCode});
    }

    public Vector getFormsRelatedWith(String relateCode) {
        String sql = "select code, relate_code from " + getTable().getName() + " where relate_code=? order by relate_order";
        Iterator ir = list(sql, new Object[]{relateCode}).iterator();
        FormMgr fm = new FormMgr();
        Vector v = new Vector();
        while (ir.hasNext()) {
            ModuleRelateDb mrd = (ModuleRelateDb)ir.next();
           	v.addElement(fm.getFormDb(mrd.getString("code")));
        }
        return v;
    }
}
