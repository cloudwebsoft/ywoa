package com.redmoon.oa.base;

import java.util.ArrayList;

import com.redmoon.oa.flow.FormDb;

import cn.js.fan.util.ErrMsgException;

public interface IFuncImpl {

	/**
	 * 执行函数
	 * @param fdao
	 * @param func 数据有两位，0为函数名，1为参数
	 * @return
	 */
    String func(IFormDAO fdao, String[] func) throws ErrMsgException;

    /**
     * 取得函数的参数中所用到的表单域，以逗号分隔
     * @param func
     * @return
     */
	ArrayList<String> getFieldsRelated(String[] func, FormDb fd);
}
