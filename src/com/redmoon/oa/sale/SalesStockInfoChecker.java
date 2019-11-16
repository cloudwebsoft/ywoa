package com.redmoon.oa.sale;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.servlet.ServletResponseWrapperInclude;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;
import com.redmoon.oa.visual.ModuleSetupDb;

/**
 * <p>Title: 订单的有效性验证</p>
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
public class SalesStockInfoChecker implements IModuleChecker {
    public SalesStockInfoChecker() {
        super();
    }

    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

        throw new ErrMsgException("记录不允许编辑!");

    }
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {
    	// 检查出库时，产品在库中数量是否足够
    	
		int opType = StrUtil.toInt(fu.getFieldValue("op_type"), 0);
		
		if (opType==1)
			return true;
		try
		{
			long stockId = Long.valueOf(fu.getFieldValue("stock"));
		

			String cws_cell_rows = fu.getFieldValue("cws_cell_rows");
			int rows = StrUtil.toInt(cws_cell_rows, 0);
	
			String formCode = "sales_stock_detail";
	
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(formCode);
	
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
	
			// String listField = StrUtil.getNullStr(msd.getString("list_field"));
			String[] fieldsAry = msd.getColAry(false, "list_field");

			if (fieldsAry == null) {
				LogUtil.getLog(getClass()).error(
						"createForNestCtl:The fields is null, please set "
								+ formCode + " module's list");
				return false;
			}
			int cols = fieldsAry.length;
	
			// 有效性验证
			SalesStockProductDb sspd2 = new SalesStockProductDb();
	
			SalesStockProductDb sspd;
	
			FormDb fdStock = new FormDb();
			fdStock = fdStock.getFormDb("sales_stock");
			FormDb fdProduct = new FormDb();
			fdProduct = fdProduct.getFormDb("sales_product_info");
	
			FormDAO fdao = new FormDAO();
			FormDAO fProduct;
			long productId = -1;
			int num = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					LogUtil.getLog(getClass()).info(
							"fields[" + j + "]=" + fieldsAry[j]);
					FormField ff = fd.getFormField(fieldsAry[j]);
					if (ff.getName().equals("product")) {
						productId = StrUtil.toLong(fu
								.getFieldValue("cws_cell_" + i + "_" + j), -1);
					} else if (ff.getName().equals("num")) {
						num = StrUtil.toInt(fu
								.getFieldValue("cws_cell_" + i + "_" + j), 0);
					}
				}
				
				sspd = sspd2.getSalesStockProductDb(stockId, productId);
				if (sspd == null) {
					LogUtil.getLog(getClass()).info("productId=" + productId);				
					fProduct = fdao.getFormDAO(productId, fdProduct);
					throw new ErrMsgException("仓库中没有产品："
							+ fProduct.getFieldValue("product_name"));
				}
				else {
					int curNumOfStock = sspd.getInt("num");
					if (curNumOfStock<num) {					
						fProduct = fdao.getFormDAO(productId, fdProduct);
						throw new ErrMsgException("产品" + fProduct.getFieldValue("product_name") + "库存不足，需出库" + num + fProduct.getFieldValue("measure_unit") + "，现仅余" + curNumOfStock + fProduct.getFieldValue("measure_unit"));
					}
				}			
			}
		}
		catch(NumberFormatException e)
		{
			throw new  ErrMsgException("请选择仓库！");
		}
        return true;
    }

    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	return true;
    }

    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	long id = fdao.getId();
    	// 添加库存记录
    	// 从入库单中得到嵌套表中的产品记录
    	String sql = "select id from form_table_sales_stock_detail where cws_id=" + id;
    	Iterator ir = fdao.list("sales_stock_detail", sql).iterator();
    	SalesStockProductDb sspd = new SalesStockProductDb();
		int opType = StrUtil.toInt(fdao.getFieldValue("op_type"));
		long stockId = StrUtil.toLong(fdao.getFieldValue("stock"), -1);
		if (stockId==-1) {
			throw new ErrMsgException("请选择仓库！");
		}
		SalesStockProductDb sspd2 = new SalesStockProductDb();
		
    	while (ir.hasNext()) {
    		FormDAO fdao2 = (FormDAO)ir.next();
    		long productId = Long.valueOf(fdao2.getFieldValue("product"));
    		int num = StrUtil.toInt(fdao2.getFieldValue("num"), 0);
    		if (opType==1) {
    			// 入库
    			// 检查产品在库中是否已存在
    			sspd = sspd2.getSalesStockProductDb(stockId, productId);
    			if (sspd==null) {
		    		try {
						sspd2.create(new JdbcTemplate(), new Object[]{new Long(stockId), new Long(productId), new Integer(num), fdao.getUnitCode()});
					} catch (ResKeyException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					}
    			}
    			else {
					try {
						sspd.updateNum(num);
					} catch (ResKeyException e) {
						throw new ErrMsgException(e.getMessage(request));
					}
    			}
    		}
    		else {
    			// 出库
    			sspd = sspd2.getSalesStockProductDb(stockId, productId);
    			
    			try {
					sspd.updateNum(-num);
				} catch (ResKeyException e) {
					throw new ErrMsgException(e.getMessage(request));
				}
    		}
    	}
        return true;
    }
    
    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	 long orderId = ParamUtil.getLong(request, "orderId", -1);
    	 if (orderId==-1)
    		 return false;
    	 String relativePath = "../sales/sales_order_stock_info_nesttable.jsp";
         try {
             RequestDispatcher rd = request.getRequestDispatcher(
                     relativePath);
             rd.include(request,
                        new ServletResponseWrapperInclude(response, out));
         }
         catch (Exception e) {
             LogUtil.getLog(getClass()).error("render:" + e.getMessage());
         }
         return true;
    }

    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }
}
