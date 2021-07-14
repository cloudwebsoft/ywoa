package com.redmoon.oa.util;


import com.cloudwebsoft.framework.base.QObjectDb;

public class ExchangeRateDb  extends QObjectDb{
	public ExchangeRateDb(){
		super();
	}

	public ExchangeRateDb getExchangeRateDb(int id){
		return (ExchangeRateDb)getQObjectDb(new Integer(id));
	}
}
