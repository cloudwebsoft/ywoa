package com.redmoon.oa.hr;

import com.cloudwebsoft.framework.base.QObjectDb;

public class TrainNoticeDb extends QObjectDb {
	public TrainNoticeDb(){
		super();
	}
	public TrainNoticeDb getTrainNoticeDb(int id){
		 return (TrainNoticeDb)getQObjectDb(Integer.valueOf(id));
	}
}
