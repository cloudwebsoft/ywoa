package com.redmoon.sns.app.blog;

import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.base.IAppUnit;

public class BlogUnit implements IAppUnit {
	
	public static final String code = "blog";

	public IAction getAction() {
		return new BlogAction();
	}
	
}
