package com.redmoon.oa.sale;

import java.io.FileNotFoundException;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class SalesFahuodanDb extends QObjectDb {
	
	public SalesFahuodanDb getSalesFahuodanDb(String unitCode) {
		SalesFahuodanDb sfd = (SalesFahuodanDb)getQObjectDb(unitCode);
		if (sfd == null) {
			sfd = new SalesFahuodanDb();
			String filePath = Global.realPath + "sales/template/fahuodan.htm";
			String c = FileUtil.ReadFile(filePath, "gb2312");
			try {
				sfd.create(new JdbcTemplate(), new Object[]{unitCode, c});
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			sfd = (SalesFahuodanDb)getQObjectDb(unitCode);
		}
		return sfd;
	}

}
