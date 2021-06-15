package com.redmoon.oa.map;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: client字段中，如果为ios，则表示由IOS苹果端上传，坐标为WGS-84</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LocationDb extends QObjectDb {
	
	
    public LocationDb() {
    }

    public LocationDb getLocationDb(long id) {
        return (LocationDb)getQObjectDb(new Long(id));
    }
}
