package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import com.redmoon.oa.hr.SalaryColumnProp;
import com.redmoon.oa.hr.SalaryMgr;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

@Controller
@RequestMapping("/salary")
public class SalaryController {
	@Autowired
	private HttpServletRequest request;

	/**
	 * 同步工资表，使其字段与工资表保持一致
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/synPayrollField", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;", "application/json;" })
	public String synPayrollField() {
		boolean re = true;
		// 取得表salary_payroll中的所有字段
		String tableName = "salary_payroll";
		Vector<SalaryColumnProp> vCol = new Vector<SalaryColumnProp>();
		Vector vFdao = new Vector();
		com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(
				Global.getDefaultDB());
		try {
			String sql = "select * from salary_payroll";
			conn.setMaxRows(1); // 尽量减少内存的使用
			ResultSet rs = conn.executeQuery(sql);
			ResultSetMetaData rm = rs.getMetaData();
			int colCount = rm.getColumnCount();
			for (int i = 1; i <= colCount; i++) {
				String colName = rm.getColumnName(i);
				int colType = QueryScriptUtil.getFieldTypeOfDBType(rm.getColumnType(i));
				SalaryColumnProp cp = new SalaryColumnProp();
				cp.name = colName;
				cp.type = colType;
				vCol.addElement(cp);
			}

			// 取得表form_table_salary_subject中的所有的记录
			sql = "select id from form_table_salary_subject where status=1";
			String formCode = "salary_subject";
			FormDAO fdao = new FormDAO();
			vFdao = fdao.list(formCode, sql);
			Iterator<FormDAO> ir = vFdao.iterator();
			while (ir.hasNext()) {
				fdao = ir.next();
				String code = fdao.getFieldValue("code");
				String name = fdao.getFieldValue("name");
				// 检查记录在表salary_payroll中如不存在，则新增
				boolean isFound = false;
				Iterator<SalaryColumnProp> irCol = vCol.iterator();
				while (irCol.hasNext()) {
					SalaryColumnProp cp = irCol.next();
					if (code.equals(cp.name)) {
						isFound = true;
						break;
					}
				}
				// 新增字段
				if (!isFound) {
					String str = "ALTER TABLE `" + tableName + "`";
					str += " ADD COLUMN `" + code + "` DOUBLE default '0' COMMENT " + StrUtil.sqlstr(name);
					conn.executeUpdate(str);
				}
			}

			Map<String, String> keepCols = new HashMap<String, String>();
			keepCols.put("id", "");
			keepCols.put("person_id", "");
			keepCols.put("create_date", "");
			keepCols.put("book_id", "");
			keepCols.put("year", "");
			keepCols.put("month", "");

			// 检查表salary_payroll中的字段，如果在form_table_salary_subject中不存在，则删除
			Iterator<SalaryColumnProp> irCol = vCol.iterator();
			while (irCol.hasNext()) {
				SalaryColumnProp cp = irCol.next();
				if (keepCols.containsKey(cp.name)) {
					continue;
				}

				boolean isFound = false;
				ir = vFdao.iterator();
				while (ir.hasNext()) {
					fdao = ir.next();
					String code = fdao.getFieldValue("code");
					if (code.equals(cp.name)) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					String str = "ALTER TABLE `" + tableName + "`";
					str += " DROP COLUMN `" + cp.name + "`";
					conn.executeUpdate(str);
				}
			}
		} catch (ErrMsgException e1) {
			re = false;
			e1.printStackTrace();
		} catch (SQLException e) {
			re = false;
			e.printStackTrace();
		} finally {
			conn.close();
		}

		JSONObject json = new JSONObject();
		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 生成工资
	 * @param bookIds
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/generateSalary", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
	public String generateSalary(@RequestParam(value = "bookIds", required = true) String bookIds, int year, int month) {
		JSONObject json = new JSONObject();
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);		
		try {
			boolean re = true;
			String[] aryBook = StrUtil.split(bookIds, ",");
			if (aryBook == null) {
				json.put("ret", "0");
				json.put("msg", "请选择帐套！");
				return json.toString();
			}

			// 检查工资帐套是否已生成
			String sql = "select id from salary_book_subject where book_id=? and year=? and month=?";
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);
				ResultIterator ri = jt.executeQuery(sql, new Object[] { bookId, year, month });
				if (ri.hasNext()) {
					json.put("ret", "0");
					json.put("msg", "帐套工资已被生成！");
					return json.toString();
				}
			}

			// 取出帐套对应的科目
			Map<String, Vector> map = new HashMap<String, Vector>();
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);			
				Vector vtSubjectOfBook = SalaryMgr.getSubjectOfBook(bookId);
				map.put(String.valueOf(bookId), vtSubjectOfBook);
			}
			
			FormDb fdFormula = new FormDb();
			fdFormula = fdFormula.getFormDb("formula");
			FormDAO fdaoFormula = new FormDAO(fdFormula);
			
			// 取得帐套中的用户，为其生成工资
			sql = "select person_id from form_table_salary_bk_person where cws_id=?";
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);
				ResultIterator ri = jt.executeQuery(sql, new Object[] { bookId });
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					String personId = rr.getString(1);
					Vector vtSubjectOfBook = (Vector)map.get(String.valueOf(bookId));
					generateForPerson(jt, personId, vtSubjectOfBook, bookId, year, month, fdaoFormula, fdFormula);
				}
			}
			
			// 记录生成的工资帐套中包含的科目信息salary_book_subject，用以判断是否已生成及显示salary_payroll_list表头
			sql = "insert into salary_book_subject (book_id, subject, year, month) values (?,?,?,?)";
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);
				Vector vtSubjectOfBook = (Vector)map.get(String.valueOf(bookId));
				Iterator ir = vtSubjectOfBook.iterator();
				while (ir.hasNext()) {
					FormDAO fdao = (FormDAO)ir.next();
					String subject = fdao.getFieldValue("code");
					jt.executeUpdate(sql, new Object[]{bookId, subject, year, month});
				}
			}			

			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (ErrMsgException e) {
			// e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			jt.close();
		}
		return json.toString();
	}
	
	
	/**
	 * 删除所有生成的工资
	 * @param bookIds
	 * @param year
	 * @param month
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delSalary", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
	public String delSalary(@RequestParam(value = "bookIds", required = true) String bookIds, int year, int month) {
		JSONObject json = new JSONObject();
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		boolean re = false;
		try {
			String[] aryBook = StrUtil.split(bookIds, ",");
			if (aryBook == null) {
				try {
					json.put("ret", "0");
					json.put("msg", "请选择帐套！");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return json.toString();
			}
			
			// 从salary_book_subject中删除
			String sql = "delete from salary_book_subject where book_id=? and year=? and month=?";
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);			
				jt.executeUpdate(sql, new Object[]{bookId, year, month});
			}
			
			// 从salary_payroll中删除
			sql = "delete from salary_payroll where book_id=? and year=? and month=?";
			for (String strBookId : aryBook) {
				int bookId = StrUtil.toInt(strBookId, -1);			
				jt.executeUpdate(sql, new Object[]{bookId, year, month});
			}			
			re = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally  {
			jt.close();
		}
		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/listPayroll", method = RequestMethod.POST, produces={"text/html;", "application/json;charset=UTF-8;"})	
	public String listPayroll(int bookId, int year, int month) {
		JSONObject jobject = new JSONObject();
		StringBuffer sb = new StringBuffer();
		String sql = "select subject from salary_book_subject where book_id=? and year=? and month=? order by id asc";
		String realName = ParamUtil.get(request, "realName");

		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		ResultIterator riSubject = null;
		try {
			JSONArray rows = new JSONArray();	
			int pageSize = ParamUtil.getInt(request, "rp", 20);
			int curPage = ParamUtil.getInt(request, "page", 1);
			
			riSubject = jt.executeQuery(sql, new Object[]{bookId, year, month});
			// 为0表示还没有生成工资
			if (riSubject.size()==0) {
				jobject.put("rows", rows);
				jobject.put("page", curPage);
				jobject.put("total", 0);
				return jobject.toString();
			}
			while (riSubject.hasNext()) {
				ResultRecord rr = (ResultRecord)riSubject.next();
				StrUtil.concat(sb, ",", rr.getString(1));
			}
			
			String cols = sb.toString();
			ResultIterator ri = null;
			sql = "select id,person_id," + cols + " from salary_payroll where book_id=? and year=? and month=?";
			if (!"".equals(realName)) {
				sql = "select s.id,person_id," + cols + " from salary_payroll s, form_table_personbasic p where s.person_id=p.id and p.realname like " + StrUtil.sqlstr("%" + realName + "%") + " and book_id=? and year=? and month=?";
			}			
			// DebugUtil.i(getClass(), "listPayroll", cols);
			try {
				ri = jt.executeQuery(sql, new Object[]{bookId, year, month}, curPage, pageSize);
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			
			jobject.put("rows", rows);
			jobject.put("page", curPage);
			jobject.put("total", ri.getTotal());

			Vector vtSubjectOfBook = SalaryMgr.getSubjectOfBook(bookId);
			Map<String, Integer> mapDecimals = new HashMap<String, Integer>();
			Iterator ir = vtSubjectOfBook.iterator();
			while (ir.hasNext()) {
				FormDAO fdao = (FormDAO)ir.next();
				mapDecimals.put(fdao.getFieldValue("code"), StrUtil.toInt(fdao.getFieldValue("decimals"), 2));
			}

			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				JSONObject jo = new JSONObject();
				try {
					jo.put("id", String.valueOf(rr.getLong("id")));
					int personId = rr.getInt("person_id");
					
					realName = "";
					sql = "select realName from form_table_personbasic where id=?";
					ResultIterator riPerson = jt.executeQuery(sql, new Object[]{personId});
					if (riPerson.hasNext()) {
						ResultRecord rrPerson = (ResultRecord)riPerson.next();
						realName = rrPerson.getString(1);
					}
					
					jo.put("realName", realName);
					
					riSubject.beforeFirst();
					while (riSubject.hasNext()) {
						ResultRecord rrSubject = (ResultRecord)riSubject.next();
						String colName = rrSubject.getString(1);
						// DebugUtil.i(getClass(), "listPayroll", "colName=" + colName);

						jo.put(colName, NumberUtil.round(rr.getDouble(colName), mapDecimals.get(colName).intValue()));
					}
				}catch (JSONException e){
					e.printStackTrace();
				}
				
				rows.put(jo);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}
		
		return jobject.toString();		
	}		

	/**
	 * 编辑科目中的手工输入型字段
	 * @param id
	 * @param colName
	 * @param original_value
	 * @param update_value
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/editSalary", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
	public String editSalary(@RequestParam(value = "id", required = true) long id, String colName, String original_value, String update_value) {
		JSONObject json = new JSONObject();
		if (update_value.equals(original_value)) {
			try {
				json.put("ret", "-1");
				json.put("msg", "值未更改！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = false;
		try {
			String sql = "update salary_payroll set " + colName + "=? where id=?";
			re = jt.executeUpdate(sql, new Object[]{update_value, id})==1;
			
			sql = "select person_id, year, month, book_id from salary_payroll where id=?";
			ResultIterator ri = jt.executeQuery(sql, new Object[]{id});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				String personId = rr.getString(1);
				int year = rr.getInt(2);
				int month = rr.getInt(3);
				int bookId = rr.getInt(4);
				
				// 根据顺序（因计算的需要）取得账套中的科目
				Vector vtSubjectOfBook = SalaryMgr.getSubjectOfBook(bookId);
				
				FormDb fdFormula = new FormDb();
				fdFormula = fdFormula.getFormDb("formula");
				FormDAO fdaoFormula = new FormDAO(fdFormula);
				
				refreshSalaryForPerson(jt, personId, bookId, year, month, vtSubjectOfBook, fdaoFormula, fdFormula);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}

		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return json.toString();
	}	
	
	/**
	 * 删除某些用户的工资
	 * @param ids
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delSalaryForUsers", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
	public String delSalaryForUsers(@RequestParam(value = "ids", required = true) String ids) {
		JSONObject json = new JSONObject();
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		boolean re = false;
		try {
			String[] ary = StrUtil.split(ids, ",");
			if (ary==null) {
				try {
					json.put("ret", "0");
					json.put("msg", "请选择记录！");
				}catch (JSONException e){
					e.printStackTrace();
				}
				return json.toString();
			}
			// 从salary_payroll中删除
			String sql = "delete from salary_payroll where id=?";
			for (String strId : ary) {
				int id = StrUtil.toInt(strId, -1);			
				jt.executeUpdate(sql, new Object[]{id});
			}			
			re = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally  {
			jt.close();
		}
		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return json.toString();
	}	
	
	@ResponseBody
	@RequestMapping(value = "/generateSalaryForUsers", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
	public String generateSalaryForUsers(@RequestParam(value = "ids", required = true) String ids, int bookId, int year, int month) {
		JSONObject json = new JSONObject();
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);		
		try {
			String[] ary = StrUtil.split(ids, ",");
			if (ary == null) {
				json.put("ret", "0");
				json.put("msg", "请选择人员！");
				return json.toString();
			}
			
			String sql = "select id from salary_book_subject where book_id=? and year=? and month=?";
			ResultIterator ri = jt.executeQuery(sql, new Object[] { bookId, year, month });
			if (!ri.hasNext()) {
				json.put("ret", "0");
				json.put("msg", "请先在工资帐套中生成工资！");
				return json.toString();
			}
			
			// 检查用户是否已被生成过，以免重复生成
			sql = "select id from salary_payroll where book_id=? and person_id=? and year=? and month=?";
			for (String strId : ary) {
				int id = StrUtil.toInt(strId, -1);
				
				String personId = "";
				String sqlPerson = "select person_id from form_table_salary_bk_person where cws_id=" + bookId + " and id=" + id;
				ri = jt.executeQuery(sqlPerson);
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					personId = rr.getString(1);
				}
				
				ri = jt.executeQuery(sql, new Object[] { bookId, personId, year, month });
				if (ri.hasNext()) {
					String realName = "";
					sql = "select realName from form_table_personbasic where id=?";
					ResultIterator riPerson = jt.executeQuery(sql, new Object[]{personId});
					if (riPerson.hasNext()) {
						ResultRecord rrPerson = (ResultRecord)riPerson.next();
						realName = rrPerson.getString(1);
					}
					
					json.put("ret", "0");
					json.put("msg", realName + " 的工资已被生成过！");
					return json.toString();
				}
			}
			
			// 根据顺序（因计算的需要）取得账套中的科目
			Vector vtSubjectOfBook = SalaryMgr.getSubjectOfBook(bookId);
			
			FormDb fdFormula = new FormDb();
			fdFormula = fdFormula.getFormDb("formula");
			FormDAO fdaoFormula = new FormDAO(fdFormula);
			
			// 取得帐套中的用户，为其生成工资
			sql = "select person_id from form_table_salary_bk_person where id=?";
			for (String strId : ary) {
				int id = StrUtil.toInt(strId, -1);
				ri = jt.executeQuery(sql, new Object[] { id });
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					String personId = rr.getString(1);
					
					generateForPerson(jt, personId, vtSubjectOfBook, bookId, year, month, fdaoFormula, fdFormula);
				}				
			}

			json.put("ret", "1");
			json.put("msg", "操作成功！");

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (ErrMsgException e) {
			e.printStackTrace();
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			jt.close();
		}
		return json.toString();
	}

	/**
	 * 为某用户生成工资
	 * @param jt
	 * @param personId
	 * @param vtSubjectOfBook
	 * @param bookId
	 * @param year
	 * @param month
	 * @param fdaoFormula
	 * @param fdFormula
	 * @return
	 */
	public boolean generateForPerson(JdbcTemplate jt, String personId, Vector vtSubjectOfBook, int bookId, int year, int month, FormDAO fdaoFormula, FormDb fdFormula) throws ErrMsgException {
		Vector vcp = SalaryMgr.makeSubjectForPerson(personId, vtSubjectOfBook, bookId, year, month, fdaoFormula, fdFormula);
		// 创建工资记录
		StringBuffer cols = new StringBuffer();
		StringBuffer vals = new StringBuffer();
		Iterator ircp = vcp.iterator();
		while (ircp.hasNext()) {
			SalaryColumnProp cp = (SalaryColumnProp)ircp.next();
			StrUtil.concat(cols, ",", cp.name);
			StrUtil.concat(vals, ",", String.valueOf(cp.value));
		}
		String sqlPayroll = "insert into salary_payroll (person_id, book_id, create_date, year, month, " + cols.toString() + ") values (" + StrUtil.sqlstr(personId)
							+ "," + bookId + "," + SQLFilter.getDateStr(DateUtil.format(new java.util.Date(),  "yyyy-MM-dd"), "yyyy-MM-dd") + "," + year + "," + month + "," + vals.toString() + ")";
		// DebugUtil.log(getClass(), "generateSalary", sqlPayroll);
		try {
			return jt.executeUpdate(sqlPayroll)==1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 刷新某用户的工资
	 * @param jt
	 * @param personId
	 * @param bookId
	 * @param year
	 * @param month
	 * @param vtSubjectOfBook
	 * @param fdaoFormula
	 * @param fdFormula
     * @return
     */
	public boolean refreshSalaryForPerson(JdbcTemplate jt, String personId, int bookId, int year, int month, Vector vtSubjectOfBook, FormDAO fdaoFormula, FormDb fdFormula) throws ErrMsgException {
		Vector vcp = SalaryMgr.makeSubjectForPerson(personId, vtSubjectOfBook, bookId, year, month, fdaoFormula, fdFormula);
		
		// 刷新工资记录
		StringBuffer cols = new StringBuffer();
		Iterator ircp = vcp.iterator();
		while (ircp.hasNext()) {
			SalaryColumnProp cp = (SalaryColumnProp)ircp.next();
			StrUtil.concat(cols, ",", cp.name + "=" + cp.value);
		}
		String sqlPayroll = "update salary_payroll set " + cols.toString() + " where person_id=" + personId + " and book_id=" + bookId + " and year=" + year + " and month=" + month;
		// DebugUtil.log(getClass(), "generateSalary", sqlPayroll);
		try {
			return jt.executeUpdate(sqlPayroll)==1;
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return false;		
	}
	
	@RequestMapping("exportPayroll")
    public void exportPayroll(int bookId, int year, int month, HttpServletResponse response) throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();

        StringBuffer sb = new StringBuffer();
		String sql = "select subject from salary_book_subject where book_id=? and year=? and month=? order by id asc";
		String realName = ParamUtil.get(request, "realName");

		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		ResultIterator riSubject = null;
		try {
			riSubject = jt.executeQuery(sql, new Object[]{bookId, year, month});
			// 为0表示还没有生成工资
			if (riSubject.size()==0) {
				DebugUtil.i(getClass(), "exportPayroll", year + "年" + month + "月工资尚未生成！");
			}
			else {
				// 生成表头
				int rowNum = 0;
	            Row row = sh.createRow(rowNum);
	            int cellnum = 0;
	            
                Cell cell = row.createCell(cellnum);
                CellReference cr = new CellReference(cell);
                // String address = cr.formatAsString();
                cell.setCellValue("姓名");
                
	            while (riSubject.hasNext()) {
					ResultRecord rr = (ResultRecord)riSubject.next();
					String subjectCode = rr.getString(1);
					StrUtil.concat(sb, ",", rr.getString(1));
					
					FormDAO fdao = SalaryMgr.getSubject(subjectCode);
					
					cellnum ++;
	                cell = row.createCell(cellnum);
	                cr = new CellReference(cell);
	                // String address = cr.formatAsString();
	                cell.setCellValue(fdao.getFieldValue("name"));		                
				}
				
				String cols = sb.toString();
				ResultIterator ri = null;
				sql = "select id,person_id," + cols + " from salary_payroll where book_id=? and year=? and month=?";
				if (!"".equals(realName)) {
					sql = "select s.id,person_id," + cols + " from salary_payroll s, form_table_personbasic p where s.person_id=p.id and p.realname like " + StrUtil.sqlstr("%" + realName + "%") + " and book_id=? and year=? and month=?";
				}			
				// DebugUtil.i(getClass(), "listPayroll", cols);
				try {
					ri = jt.executeQuery(sql, new Object[]{bookId, year, month});
				} catch (SQLException e) {
					e.printStackTrace();
				}		
								
                CellStyle cellStyle = wb.createCellStyle();
                HSSFDataFormat format= (HSSFDataFormat) wb.createDataFormat();
                cellStyle.setDataFormat(format.getFormat("#,#0.00"));
                
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					JSONObject jo = new JSONObject();
					try {
						jo.put("id", String.valueOf(rr.getLong("id")));
						int personId = rr.getInt("person_id");
		                
						realName = "";
						sql = "select realName from form_table_personbasic where id=?";
						ResultIterator riPerson = jt.executeQuery(sql, new Object[]{personId});
						if (riPerson.hasNext()) {
							ResultRecord rrPerson = (ResultRecord)riPerson.next();
							realName = rrPerson.getString(1);
						}
						
						jo.put("realName", realName);
						
						rowNum ++;
						cellnum = 0;
			            row = sh.createRow(rowNum);
		                cell = row.createCell(cellnum);
		                cr = new CellReference(cell);
		                cell.setCellValue(realName);							
						
						riSubject.beforeFirst();
						while (riSubject.hasNext()) {
							ResultRecord rrSubject = (ResultRecord)riSubject.next();
							String colName = rrSubject.getString(1);
							// DebugUtil.i(getClass(), "listPayroll", "colName=" + colName);
	
							jo.put(colName, NumberUtil.round(rr.getDouble(colName), 2));
							
							cellnum ++;
			                cell = row.createCell(cellnum, Cell.CELL_TYPE_NUMERIC);
			                cell.setCellStyle(cellStyle);	
			                cell.setCellValue(rr.getDouble(colName)); // 如果此处用rr.getString，则无论怎么设置CELL_TYPE_NUMERIC，导出的都是字符型单元格		                
						}
					}catch (JSONException e){
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			jt.close();
		}        
 
        response.setCharacterEncoding("UTF-8");
		response.setContentType("application/x-download");

		// String fileName = new String(("工资_" + year + "_" + month + ".xls").getBytes("UTF-8"), "iso-8859-1");//为了解决中文名称乱码问题

		String fileName = "工资_" + year + "_" + month + ".xls";
		fileName = URLEncoder.encode(fileName, "UTF-8");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

		try {
			OutputStream out = response.getOutputStream();
			wb.write(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	@ResponseBody
	@RequestMapping(value = "/regenerateForUsers", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })	
	public String regenerateForUsers(@RequestParam(value = "ids", required = true) String ids, int bookId, int year, int month) {
		JSONObject json = new JSONObject();
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		boolean re = false;
		try {
			String[] ary = StrUtil.split(ids, ",");
			if (ary==null) {
				try {
					json.put("ret", "0");
					json.put("msg", "请选择记录！");
				}catch (JSONException e){
					e.printStackTrace();
				}
				return json.toString();
			}
			String sql = "select person_id, year, month, book_id from salary_payroll where id=?";
			// 根据顺序（因计算的需要）取得账套中的科目
			Vector vtSubjectOfBook = SalaryMgr.getSubjectOfBook(bookId);
			
			FormDb fdFormula = new FormDb();
			fdFormula = fdFormula.getFormDb("formula");
			FormDAO fdaoFormula = new FormDAO(fdFormula);
			// 从salary_payroll中删除
			for (String strId : ary) {
				int id = StrUtil.toInt(strId, -1);			
				ResultIterator ri = jt.executeQuery(sql, new Object[]{id});
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					String personId = rr.getString(1);
					
					refreshSalaryForPerson(jt, personId, bookId, year, month, vtSubjectOfBook, fdaoFormula, fdFormula);
				}
			}			
			re = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}
		finally  {
			jt.close();
		}
		try {
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return json.toString();		
	}
}
