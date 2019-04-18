package com.redmoon.oa.post;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostFlowDb extends QObjectDb {
	public PostFlowDb() {
		super();
	}

	public PostFlowDb getPostFlowDb(int id) {
		return (PostFlowDb) getQObjectDb(new Integer(id));
	}
}
