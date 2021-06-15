package com.redmoon.oa.sms;

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
public class SMSTemplateDb extends QObjectDb {
    public SMSTemplateDb() {
        super();
    }

    public SMSTemplateDb getMSTemplateDb(long id) {
        return (SMSTemplateDb) getQObjectDb(new Long(id));
    }
}
