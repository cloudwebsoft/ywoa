package com.redmoon.oa.fileark;

import com.cloudwebsoft.framework.base.QObjectDb;
import java.util.Vector;

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
public class DocPollDb extends QObjectDb {
    public DocPollDb() {
    }

    public Vector getOptions(int docId) {
        DocPollOptionDb mpod = new DocPollOptionDb();
        return mpod.getOptions(docId);
    }
}
