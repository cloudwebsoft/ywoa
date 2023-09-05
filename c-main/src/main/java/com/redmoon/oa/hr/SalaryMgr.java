package com.redmoon.oa.hr;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IFormulaUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormulaResult;
import com.redmoon.oa.visual.func.CalculateFuncImpl;
import org.apache.commons.lang.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.SQLException;
import java.util.*;

public class SalaryMgr {
	/**
	 * 取得工资科目
	 * @param subjectCode
	 * @return
	 */
	public static FormDAO getSubject(String subjectCode) {
		String sql = "select id from ft_salary_subject where code=" + StrUtil.sqlstr(subjectCode);
		FormDAO fdao = new FormDAO();
		try {
			Iterator ir = fdao.list("salary_subject", sql).iterator();
			if (ir.hasNext()) {
				return (FormDAO)ir.next();
			}			
		} catch (ErrMsgException e) {
			LogUtil.getLog(SalaryMgr.class).error(e);
		}

		return null;
	}
	
	/**
	 * 取得工资科目
	 * @param subjectId
	 * @return
	 */	
	public static FormDAO getSubjectById(int subjectId) {
		String sql = "select id from ft_salary_subject where id=" + subjectId;
		FormDAO fdao = new FormDAO();
		try {
			Iterator ir = fdao.list("salary_subject", sql).iterator();
			if (ir.hasNext()) {
				return (FormDAO)ir.next();
			}
		} catch (ErrMsgException e) {
			LogUtil.getLog(SalaryMgr.class).error(e);
		}
		return null;
	}
	
	public static Vector getSubjectOfBook(int bookId) {
		// 根据顺序（因计算的需要）取得账套中的科目
		String sqlSubject = "select s.id from ft_salary_subject s, ft_salary_bk_subject bs where bs.cws_id='" + bookId + "' and s.id=bs.subject and s.status=1 order by s.orders";
		FormDAO fdao = new FormDAO();
		try {
			return fdao.list("salary_subject", sqlSubject);
		} catch (ErrMsgException e) {
			LogUtil.getLog(SalaryMgr.class).error(e);
		}	
		return null;
	}

	/**
	 * 为用户生成单个科目的值
	 * @param personId
	 * @param vtSubjectOfBook
	 * @param bookId
	 * @param year
	 * @param month
	 * @param fdaoSubject
	 * @param fdaoFormula
	 * @param fdFormula
	 * @return
     * @throws ErrMsgException
     */
	public static double makeSingleSubjectForUser(String personId, Vector vtSubjectOfBook, int bookId, int year, int month, FormDAO fdaoSubject, FormDAO fdaoFormula, FormDb fdFormula, Map<String, Double> mapSubjectVal) throws ErrMsgException {
		DebugUtil.i(SalaryMgr.class, "makeSingleSubjectForUser", "personId:" + personId + " 科目：" + fdaoSubject.getFieldValue("name"));

		String colName = fdaoSubject.getFieldValue("code");
		if (mapSubjectVal.containsKey(colName)) {
			return mapSubjectVal.get(colName);
		}
		String kind = fdaoSubject.getFieldValue("kind");
		String subjectName = fdaoSubject.getFieldValue("name");

		double val = 0;
		// 手动输入型，从工资表中获取输入的值
		if ("1".equals(kind)) {
			String sql = "select " + colName + " from salary_payroll where person_id=? and book_id=? and year=? and month=?";
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri;
			try {
				ri = jt.executeQuery(sql, new Object[]{personId, bookId, year, month});
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					val = rr.getDouble(1);
				}
				else {
					// 取不到则取默认值
					String defaultVal = fdaoSubject.getFieldValue("default_value");
					if (defaultVal!=null && !"".equals(defaultVal)) {
						val = StrUtil.toDouble(defaultVal, 0);
					}
				}
			} catch (SQLException e) {
				// 有可能payroll表尚未生成，其中的字段还不存在
				LogUtil.getLog(SalaryMgr.class).error(e);
			}
		}
		// 函数型
		else if ("0".equals(kind)) {
			String strFormula = fdaoSubject.getFieldValue("formula");
			long formulaId = StrUtil.toLong(strFormula, -1);
			fdaoFormula = fdaoFormula.getFormDAO(formulaId, fdFormula);
			String subjectParams = StrUtil.getNullStr(fdaoSubject.getFieldValue("subject_params"));

			String code = fdaoFormula.getFieldValue("code");
			if (code==null || "".equals(code)) {
				DebugUtil.log(SalaryMgr.class, "makeSingleSubjectForUser", "科目：" + colName + " 中的函数未选择！");
				return 0;
			}

			String[] paramAry = null;
			int paramAryLen = 0;
			// 科目中的函数，参数中需包含personId,bookId,year,month的子集，且参数名需一致
			// 取函数的形参
			String params = StrUtil.getNullStr(fdaoFormula.getFieldValue("params"));
			paramAry = StrUtil.split(params, ",");
			if (paramAry!=null) {
				paramAryLen = paramAry.length;
			}

			// 取科目中的形参
			String[] subjectParamsAry = StrUtil.split(subjectParams, ",");
			// 如果科目中的形参不为空，则以科目中的形参为准，如果为空，则表示以函数中的参数为准
			if (subjectParamsAry!=null) {
				if (paramAryLen != subjectParamsAry.length) {
					throw new ErrMsgException("科目" + subjectName + "中的参数" + subjectParams + "与函数中的参数" + params + "的数量不一致！");
				}

				paramAry = subjectParamsAry;
				paramAryLen = paramAry.length;
			}

			// 将形参替换为实参
			if (paramAryLen > 0) {
				for (int i = 0; i < paramAryLen; i++) {
					if ("personId".equalsIgnoreCase(paramAry[i])) {
						paramAry[i] = personId;
					}
					else if ("bookId".equalsIgnoreCase(paramAry[i])) {
						paramAry[i] = String.valueOf(bookId);
					}
					else if ("year".equalsIgnoreCase(paramAry[i])) {
						paramAry[i] = String.valueOf(year);
					}
					else if ("month".equalsIgnoreCase(paramAry[i])) {
						paramAry[i] = String.valueOf(month);
					}
					else {
						// 判断是否为科目，如果是，则计算科目
						String subjCode = paramAry[i];
						// 如果参数名不等于当前正在计算的科目名（以免引起死循环）
						if (!subjCode.equals(colName)) {
							FormDAO fdaoSubj = getSubject(subjCode);
							if (fdaoSubj != null) {
								double subjVal = 0;
								if (mapSubjectVal.containsKey(subjCode)) {
									subjVal = mapSubjectVal.get(subjCode).doubleValue();
								} else {                    // 为科目型参数生成值
									subjVal = makeSingleSubjectForUser(String.valueOf(personId), vtSubjectOfBook, bookId, year, month, fdaoSubj, fdaoFormula, fdFormula, mapSubjectVal);
									mapSubjectVal.put(subjCode, subjVal);
								}
								paramAry[i] = String.valueOf(subjVal);
							}
							else {
								// 如果不是科目，则说明参数paramAry[i]可能未赋值，当然也可能为常量
								// 例：科目 餐补/天 是用的函数#getSalarySet，其函数参数为personId,item，而如果科目参数如果为空，则此处的paramAry[i]未能被替换，仍为item
								// 如果paramAry[i]既不是数字型也不是字符串型，则说明未被赋值
								if (!StrUtil.isDouble(paramAry[i]) && !paramAry[i].startsWith("\"")) {
									throw new ErrMsgException("科目 " + subjectName + " 调用函数" + code + "时参数" + paramAry[i] + "非法，不是personId、bookId、year、month，也不是数字、字符串常量，或其它科目");
								}
							}
						}
					}
				}
				params = StringUtils.join(paramAry, ",");
			}

			String formula = "#" + code + "(" + params + ")";

			DebugUtil.i(SalaryMgr.class, "makeSingleSubjectForUser", formula);
			IFormulaUtil formulaUtil = SpringUtil.getBean(IFormulaUtil.class);
			FormulaResult fr = formulaUtil.render(formula);
			val = StrUtil.toDouble(fr.getValue(), 0);
		}
		// 计算型
		else if ("2".equals(kind)) {
			String equation = StrUtil.getNullStr(fdaoSubject.getFieldValue("equation"));
			if ("".equals(equation)) {
				DebugUtil.log(SalaryMgr.class, "makeSingleSubjectForUser", "科目：" + colName + " 算式为空！");
				return 0;
			}

			val = eval(subjectName, equation, personId, vtSubjectOfBook, bookId, year, month, fdaoFormula, fdFormula, mapSubjectVal);
		}

		mapSubjectVal.put(colName, val);
		return val;
	}
	
	/**
	 * 为某个用户生成工资中各项科目的值
	 * @param personId
	 * @param vtSubjectOfBook
	 * @param bookId
	 * @param year
	 * @param month
	 * @param fdaoFormula
	 * @param fdFormula
	 * @return
	 * @throws ErrMsgException
	 */
	public static Vector makeSubjectForPerson(String personId, Vector vtSubjectOfBook, int bookId, int year, int month, FormDAO fdaoFormula, FormDb fdFormula) throws ErrMsgException {
		// 将已计算的科目值存在map中，以免重复计算，因为可能会碰到递归调用或者相互调用计算各科目值的情况
		Map<String, Double> mapSubjectVal = new HashMap<String, Double>();

		Vector<SalaryColumnProp> vcp = new Vector<SalaryColumnProp>();
		Iterator ir = vtSubjectOfBook.iterator();
		while (ir.hasNext()) {
			FormDAO fdao = (FormDAO)ir.next();
			String colName = fdao.getFieldValue("code");
			String name = fdao.getFieldValue("name");
			String kind = fdao.getFieldValue("kind");
			String inOrDe = fdao.getFieldValue("in_de_item");

			// 手动输入型
			if ("1".equals(kind)) {
				double val = makeSingleSubjectForUser(personId, vtSubjectOfBook, bookId, year, month, fdao, fdaoFormula, fdFormula, mapSubjectVal);

				SalaryColumnProp cp = new SalaryColumnProp();
				cp.name = colName;
				cp.value = val;
				cp.inOrDe = "1".equals(inOrDe) ? 1:0;							
				vcp.addElement(cp);				
			}
			// 函数型
			else if ("0".equals(kind)) {
				double val = makeSingleSubjectForUser(personId, vtSubjectOfBook, bookId, year, month, fdao, fdaoFormula, fdFormula, mapSubjectVal);

				SalaryColumnProp cp = new SalaryColumnProp();
				cp.name = colName;
				cp.value = val;
				cp.inOrDe = "1".equals(inOrDe) ? 1:0;
				vcp.addElement(cp);
			}
			// 计算型
			else if ("2".equals(kind)) {
				String equation = StrUtil.getNullStr(fdao.getFieldValue("equation"));
				if ("".equals(equation)) {
					DebugUtil.log(SalaryMgr.class, "makeSubjectForPerson", "科目：" + name + " 算式为空！");
					continue;
				}
				double val = makeSingleSubjectForUser(personId, vtSubjectOfBook, bookId, year, month, fdao, fdaoFormula, fdFormula, mapSubjectVal);
				// 解析四则运算算式
				SalaryColumnProp cp = new SalaryColumnProp();
				cp.name = colName;
				cp.value = val;
				cp.inOrDe = "1".equals(inOrDe) ? 1:0;							
				vcp.addElement(cp);
			}
		}
		
		return vcp;
	}
	
	public static double eval(String subjectName, String equation, String personId, Vector vtSubjectOfBook, int bookId, int year, int month, FormDAO fdaoFormula, FormDb fdFormula, Map<String, Double>mapSubjectVal) throws ErrMsgException {
		ArrayList<String> aryList = CalculateFuncImpl.getSymbolsWithBracket(equation);
	    Object[] ary = aryList.toArray();
		for (int i=0; i<ary.length; i++) {
			String el = (String)ary[i];
	        if (!CalculateFuncImpl.isOperator(el)) {
	            // ary[i]可能为0.2这样的系数
			    if (!StrUtil.isDouble(el)) {
					// 如果el不是字段，由可能出现了书写错误，也可能是其它的科目
					FormDAO fdaoSubject = new FormDAO();
					String sql = "select id from ft_salary_subject where code=" + StrUtil.sqlstr(el);
					Vector v = fdaoSubject.list("salary_subject", sql);
					if (v.size()>0) {
						fdaoSubject = (FormDAO)v.elementAt(0);
						double val = 0;
						if (mapSubjectVal.containsKey(el)) {
							val = mapSubjectVal.get(el).doubleValue();
						}
						else {
							val = makeSingleSubjectForUser(personId, vtSubjectOfBook, bookId, year, month, fdaoSubject, fdaoFormula, fdFormula, mapSubjectVal);
						}
						// 递归调用
						ary[i] = val;
					}
					else {
						ary[i] = "0";
						DebugUtil.e(SalaryMgr.class, "eval", el + "不是字段！");
						throw new ErrMsgException("科目" + subjectName + "的算式" + equation + "中的" + el + "不是字段！");
					}
				}
	        }
	    }
		
	    String formula2 = "";
	    for (int i=0; i < ary.length; i++) {
	    	formula2 += ary[i];
	    }

		ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
        	Double ret = (Double)engine.eval(formula2);
        	if (ret!=null) {
        		return ret.doubleValue();
        	}
        	else {
				LogUtil.getLog(SalaryMgr.class).info(" eval: ret=null, equation=" + equation);
				return -1;
        	}
        }
        catch (ScriptException ex) {
        	LogUtil.getLog(SalaryMgr.class).error(StrUtil.trace(ex));
        	throw new ErrMsgException(ex.getMessage());
        }		
        catch (ClassCastException e) {
			LogUtil.getLog(SalaryMgr.class).error(e);
        	throw new ErrMsgException(e.getMessage());
        }
	}
}
