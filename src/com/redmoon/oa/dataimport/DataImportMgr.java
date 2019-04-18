package com.redmoon.oa.dataimport;

import java.io.Serializable;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dataimport.bean.DataImportBean;
import com.redmoon.oa.dataimport.service.IDataImport;

public class DataImportMgr implements Serializable {

	private DataImportBean dataImportBean;

	public DataImportMgr(DataImportBean dataImportBean) {
		this.dataImportBean = dataImportBean;
	}

	public IDataImport getIDataImport() {
		IDataImport idi = null;
		try {
			idi = (IDataImport) Class.forName(dataImportBean.getClassName())
					.newInstance();
			idi.setDataImportBean(dataImportBean);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(
					"getIDataImport: code=" + dataImportBean.getCode()
							+ " className=" + dataImportBean.getClassName()
							+ " " + StrUtil.trace(e));
		}
		return idi;
	}
}
