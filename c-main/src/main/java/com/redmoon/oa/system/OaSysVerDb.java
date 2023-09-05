package com.redmoon.oa.system;

import com.cloudwebsoft.framework.base.QObjectDb;

public class OaSysVerDb extends QObjectDb {
    public OaSysVerDb() {
        super();
    }

    public OaSysVerDb getOaSysVerDb(int id) {
        return (OaSysVerDb) getQObjectDb(id);
    }

}
