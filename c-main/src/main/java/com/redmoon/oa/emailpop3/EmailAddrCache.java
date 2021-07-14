package com.redmoon.oa.emailpop3;

import cn.js.fan.base.ObjectCache;

public class EmailAddrCache extends ObjectCache {

	public EmailAddrCache() {
	}

	public EmailAddrCache(EmailAddrDb epd) {
		super(epd);
	}
}
