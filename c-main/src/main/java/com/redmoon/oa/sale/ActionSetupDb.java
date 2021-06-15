package com.redmoon.oa.sale;

import cn.js.fan.db.PrimaryKey;

import com.cloudwebsoft.framework.base.QObjectDb;

public class ActionSetupDb extends QObjectDb {

    public ActionSetupDb getActionSetupDb(int customerType, String unitCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("customer_type", new Integer(customerType));
        pk.setKeyValue("unit_code", unitCode);
        return (ActionSetupDb)getQObjectDb(pk.getKeys());
    }
}
