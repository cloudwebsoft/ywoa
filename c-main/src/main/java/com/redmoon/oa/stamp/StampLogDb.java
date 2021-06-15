package com.redmoon.oa.stamp;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class StampLogDb extends QObjectDb {
    public StampLogDb() {
    }

    public StampLogDb getStampLogDb(int id) {
        return (StampLogDb)getQObjectDb(new Long(id));
    }
}
