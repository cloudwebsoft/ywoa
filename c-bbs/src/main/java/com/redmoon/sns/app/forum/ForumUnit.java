package com.redmoon.sns.app.forum;

import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.base.IAppUnit;

public class ForumUnit implements IAppUnit {
	public static final String code = "forum";

	public IAction getAction() {
		return new ForumAction();
	}
}
