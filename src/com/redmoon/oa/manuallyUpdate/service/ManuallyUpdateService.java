package com.redmoon.oa.manuallyUpdate.service;

import java.io.File;

import cn.js.fan.util.ErrMsgException;
/**
 * 手动更新服务接口
 * @author Administrator
 *
 */
public interface ManuallyUpdateService {

	boolean manuallyUpdate(File file) throws ErrMsgException;
}
