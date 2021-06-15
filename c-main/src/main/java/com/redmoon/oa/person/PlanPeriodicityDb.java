package com.redmoon.oa.person;

import com.cloudwebsoft.framework.base.*;

public class PlanPeriodicityDb extends QObjectDb{
    public static int REMIND_TYPE_DAY = 1;
    public static int REMIND_TYPE_WEEK = 2;
    public static int REMIND_TYPE_MONTH = 3;
    public static int REMIND_TYPE_YEAR = 4;

    public PlanPeriodicityDb() {
        super();
    }

    public PlanPeriodicityDb getPlanPeriodicityDb(int id) {
            return (PlanPeriodicityDb) getQObjectDb(new Integer(id));
    }
}
