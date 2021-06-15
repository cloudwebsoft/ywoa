package com.redmoon.sns.app.t;

import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.base.IAppUnit;

public class TUnit implements IAppUnit {
	public static final String code = "t";
	
	public IAction getAction() {
		return new TAction();
	}

}
