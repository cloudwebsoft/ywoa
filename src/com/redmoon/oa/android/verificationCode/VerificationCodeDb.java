package com.redmoon.oa.android.verificationCode;

import com.cloudwebsoft.framework.base.QObjectDb;

public class VerificationCodeDb extends QObjectDb {

	public VerificationCodeDb() {
		super();
	}

	public VerificationCodeDb getVerificationCodeDb(int id) {
		return (VerificationCodeDb) getQObjectDb(new Integer(id));
	}
}
