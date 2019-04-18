package com.redmoon.oa.upgrade.service;

public interface IUpgradeService {

	void execute(boolean immediate);
	
	void sendFirstUseInfo(String isRegistered, String customerId, String version);

}