package com.redmoon.oa.sale;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.IModuleChecker;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sale.SalePrivilege;

/**
 * <p>Title: 采购单的有效性验证</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SalesPurchaseChecker implements IModuleChecker {
    public SalesPurchaseChecker() {
        super();
    }

    @Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

        // throw new ErrMsgException("记录不允许编辑!");
    	return true;

    }
    @Override
	public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {

        return true;
    }

    @Override
	public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {


        return true;
    }

    @Override
	public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    @Override
	public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	long id = fdao.getId();
    	// 添加库存记录
    	// 从入库单中得到嵌套表中的产品记录
    	String sql = "select id from form_table_sales_purch_detail where cws_id=" + id;
    	Iterator ir = fdao.list("sales_purch_detail", sql).iterator();
    	SalesStockProductDb sspd = new SalesStockProductDb();
		long stockId = Long.valueOf(fdao.getFieldValue("stock"));
		SalesStockProductDb sspd2 = new SalesStockProductDb();
		
    	while (ir.hasNext()) {
    		FormDAO fdao2 = (FormDAO)ir.next();
    		long productId = Long.valueOf(fdao2.getFieldValue("product"));
			int num = StrUtil.toInt(fdao2.getFieldValue("num"), 0);
			// 入库
			// 检查产品在库中是否已存在
			sspd = sspd2.getSalesStockProductDb(stockId, productId);
			if (sspd == null) {
				try {
					sspd2.create(new JdbcTemplate(), new Object[] {
							new Long(stockId), new Long(productId),
							new Integer(num) });
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			} else {
				try {
					sspd.updateNum(num);
				} catch (ResKeyException e) {
					throw new ErrMsgException(e.getMessage(request));
				}
			}
    		
    	}
        return true;
    }
    
    @Override
	public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    }     

    @Override
	public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }
}

