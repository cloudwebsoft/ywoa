package com.redmoon.oa.sale;

import java.io.OutputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class ExcelResourceSaleChanceList {
    public ExcelResourceSaleChanceList() {
    }
	
    /**
     * 输出Excel
     *
     * @param os
     */
    public static void writeExcel(OutputStream os, String unitCode,String customer,String creator,String dept,int state,String status,String strBeginDate,String strEndDate,String tag,int cusid) {
        try { //创建工作薄
        	String sql = "";
        	if(!"".equals(tag)){
        		sql = "select * from form_table_sales_chance where unit_code=" + StrUtil.sqlstr(unitCode)+" and customer="+cusid;
        	}else{
	        	sql = "select * from form_table_sales_chance ch where ch.unit_code="+StrUtil.sqlstr(unitCode);
	        	if(!customer.equals("")){
	        		sql = "select ch.* from form_table_sales_chance ch, form_table_sales_customer c where c.unit_code="+StrUtil.sqlstr(unitCode)+" and ch.customer=c.id and c.customer like" +StrUtil.sqlstr("%"+customer+"%");
	        	}
	        	if(!creator.equals("")){
	    			sql += " and ch.cws_creator in ( select name from users where realName like "+ StrUtil.sqlstr("%"+creator+"%")+")";
	    		}
	    		if(!dept.equals("")){
	    			//sql += " and ch.cws_creator in ( select user_name from dept_user where dept_code in (select code from department where name like "+ StrUtil.sqlstr("%"+ dept +"%") +"))";
	    			sql += " and ch.cws_creator in ( select user_name from dept_user where dept_code in ("+ StrUtil.sqlstr(dept) +"))";
	    		}
	    		if (state != -1) {
	    			sql += " and ch.state=" + state;
	    		}
	    		if (!status.equals("")) {
	    			sql += " and ch.sjzt=" + status;
	    		}
	    		if (!strBeginDate.equals("")) {
	    			sql += " and ch.find_date>="
	    					+ SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	    		}
	    		if (!strEndDate.equals("")) {
	    			sql += " and ch.find_date<"
	    					+ SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	    		}
	    		sql += " order by ch.find_date desc";
        	}
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            //创建工作表
            WritableSheet ws = wwb.createSheet("销售商机汇总", 0);
            JdbcTemplate jt = new JdbcTemplate();
            
            Label labelA = new Label(0, 0, "客户名称");
            Label labelB = new Label(1, 0, "商机名称");
            Label labelD = new Label(2, 0, "主要产品或需求");
            Label labelE = new Label(3, 0, "预计我司金额");//
            Label labelF = new Label(4, 0, "可能性");//
            Label labelG = new Label(5, 0, "商机阶段");//
            Label labelH = new Label(6, 0, "商机状态");//
            Label labelI = new Label(7, 0, "客户经理");
            Label labelJ = new Label(8, 0, "所属部门");
            Label labelK = new Label(9, 0, "发现时间");

            ws.addCell(labelA);
            ws.addCell(labelB);
            ws.addCell(labelD);
            ws.addCell(labelE);
            ws.addCell(labelF);
            ws.addCell(labelG);
            ws.addCell(labelH);
            ws.addCell(labelI);
            ws.addCell(labelJ);
            ws.addCell(labelK);
            
            
    		
    		String sqlpro = "select product_name from form_table_sales_product_info where id = (select cpmc from form_table_sales_product_new where cws_id = ?)";
    		String sqlpri= "select sum(yjwsje) from form_table_sales_product_new where cws_id =?";
    		String sqlcus= "select customer from form_table_sales_customer where id=?";
    		JdbcTemplate jt2 = new JdbcTemplate();
    		ResultIterator ri2 = null;
    		ResultRecord rd2= null;
    		JdbcTemplate jt1 = new JdbcTemplate();
    		ResultIterator ri1 = null;
    		ResultRecord rd1= null;
    		
    		JdbcTemplate jt3 = new JdbcTemplate();
    		ResultIterator ri3 = null;
    		ResultRecord rd3= null;
    		
            
        	int j = 1;
        	if (!sql.equals("")) {
        		ResultIterator ri = jt.executeQuery(sql);
        		while(ri.hasNext()){
        			int sid = 0;
                    int possibility = 0;
                    String findDate = "";
                    int zt = 0 ;
                    int sjzt = 0;
                    String uName = "";
                    String chanceName ="";
                    String productName = "";
                    String cusName = "";
            		double sumprice = 0;
            		String manager = "";
            		String deptName = "";
            		String ztStr = "";
            		String sjztStr = "";
        			ResultRecord rd = (ResultRecord)ri.next();
        			sid = rd.getInt("id");
        			possibility = rd.getInt("possibility");
        			findDate = rd.getString("find_date");
        			zt = rd.getInt("state");
        			sjzt = rd.getInt("sjzt");
        			chanceName = rd.getString("chanceName");
        			uName = rd.getString("cws_creator");
        			UserMgr um = new UserMgr();
        			UserDb user = um.getUserDb(uName);
              		manager = user.getRealName();
              		SelectOptionDb sod = new SelectOptionDb();
              		ztStr = sod.getOptionName("sales_chance_state", zt+"");
        			sjztStr = sod.getOptionName("sales_chance_status", sjzt+"");
        			DeptUserDb dud = new DeptUserDb(uName);
        			deptName = dud.getDeptName();
        			ri1 = jt1.executeQuery(sqlpro, new Object[]{sid});
        			if(ri1.hasNext()){
        				rd1 = (ResultRecord)ri1.next();
        				productName = rd1.getString(1);
        			}
        			ri2 = jt2.executeQuery(sqlpri, new Object[]{sid});
        			if(ri2.hasNext()){
        				rd2 = (ResultRecord)ri2.next();
        				sumprice = rd2.getDouble(1);
        			}
        			ri3 = jt3.executeQuery(sqlcus, new Object[]{rd.getInt("customer")});
        			if(ri3.hasNext()){
        				rd3 = (ResultRecord)ri3.next();
        				cusName = rd3.getString(1);
        			}
        			
                    Label labelA1 = new Label(0, j, cusName);
                    Label labelB1 = new Label(1, j, chanceName);
                    Label labelD1 = new Label(2, j, productName);
                    Label labelE1 = new Label(3, j, sumprice+"");
                    Label labelF1 = new Label(4, j, possibility+"%");
                    Label labelG1 = new Label(5, j, ztStr);
                    Label labelH1 = new Label(6, j, sjztStr);
                    Label labelI1 = new Label(7, j, manager);
                    Label labelJ1 = new Label(8, j, deptName);
                    Label labelK1 = new Label(9, j, findDate);
                    
                    j++;
                    ws.addCell(labelA1);
                    ws.addCell(labelB1);
                    ws.addCell(labelD1);
                    ws.addCell(labelE1);
                    ws.addCell(labelF1);
                    ws.addCell(labelG1);
                    ws.addCell(labelH1);
                    ws.addCell(labelI1);
                    ws.addCell(labelJ1);
                    ws.addCell(labelK1);
        		}
        	}
        	
            wwb.write();
            wwb.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
