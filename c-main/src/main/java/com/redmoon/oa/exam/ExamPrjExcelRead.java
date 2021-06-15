package com.redmoon.oa.exam;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * @Description:
 * @author:
 * @Date: 2018-1-25下午05:50:10
 */
public class ExamPrjExcelRead {

	Workbook book = null;

	public ExamPrjExcelRead() {
	}

	public void ExamExcelPrjhad(String xlspath, int isPrj)
			throws ErrMsgException, IndexOutOfBoundsException, SQLException,
			ParseException, ResKeyException {
		boolean re = true;
		String realName, msg = "", prjId = "", subject = "", idCard = "", paperId = "", testtime = "", score = "", mobile = "";
		try {
			book = Workbook.getWorkbook(new java.io.File(xlspath));
			// 获取sheet表的总行数、总列数
			jxl.Sheet rs = book.getSheet(0);
			int rsRows = rs.getRows();
			int rsColumns = rs.getColumns();
			Cell cc;
			int columns;
			String fieldsValue;
			int fields[] = new int[rsColumns];
			String strc[] = new String[rsColumns];
			int k = 0;
			for (int m = 0; m < rsColumns; m++) {
				cc = rs.getCell(m, 0);
				fieldsValue = cc.getContents();
				if (fieldsValue.equals("姓名")) {
					fields[0] = m;
					k = 3;
				}
				LogUtil.getLog(getClass()).info(
						"fieldsValue=" + fieldsValue + ":" + m);
				if (fieldsValue.equals("试卷编号"))
					fields[1] = m;
				if (fieldsValue.equals("考试时间"))
					fields[2] = m;
				if (fieldsValue.equals("分数"))
					fields[3] = m;
				if (fieldsValue.equals("手机"))
					fields[4] = m;
				if (fieldsValue.equals("身份证号"))
					fields[5] = m;
				if (fieldsValue.equals("科目"))
					fields[6] = m;
			}
			if (k < 2)
				throw new ErrMsgException("导入Excel格式不正确，请检查！");
			for (int i = 1; i < rsRows; i++) {
				for (int j = 0; j <= 6; j++) {
					columns = fields[j];
					cc = rs.getCell(columns, i);
					strc[j] = cc.getContents();
				}
				// 写入数据库
				realName = strc[0];
				paperId = strc[1];
				testtime = strc[2];
				score = strc[3];
				mobile = strc[4];
				idCard = strc[5];
				subject = strc[6];
				ScoreDb sc = new ScoreDb();
				sc.setUserName(realName);
				sc.setPaperId(Integer.parseInt(paperId));
//				SimpleDateFormat sdf = new SimpleDateFormat(
//						" yyyy-MM-dd HH:mm:ss ");
//				Date d = sdf.parse(testtime);
//				sc.setEndtime(d);
				sc.setScore(Integer.parseInt(score));
				sc.setIsprj(isPrj);
				sc.setMobile(mobile);
				re = sc.create();
				if (re) {
					String sql = "select prj_id,id_number,contractor from form_table_contractor_prj_qkj where name ="
							+ StrUtil.sqlstr(realName)
							+ " order by id desc limit 1";
					// System.out.println(this.getClass()+"sql语句  " +sql);
					JdbcTemplate jt = new JdbcTemplate();
					ResultIterator ri = jt.executeQuery(sql);
					if (ri.hasNext()) {
						ResultRecord rd = (ResultRecord) ri.next();
						prjId = rd.getString("prj_id");
						String idNumber = rd.getString("id_number");
						String contractor = rd.getString("contractor");
						// System.out.println(this.getClass()+"姓名  "
						// +userName+" 项目ID "+prjId+
						// " 身份证号 "+idNumber+" 承包商名称"+contractor+" 结果ID"+std.getId());
						ScorePrjDb spd = new ScorePrjDb();
						Object[] obj = new Object[] { realName, idNumber,
								prjId, contractor, sc.getId() };
						// 外部人员信息写入oa_exam_score_prj表
						re = spd.create(jt, obj);
					}
					int totle = Integer.parseInt(score);
					if (totle >= 60) {
						sql = "select orders from oa_exam_subject where subject="
								+ StrUtil.sqlstr(subject);
						System.out.println(sql);
						ri = jt.executeQuery(sql);
						if (ri.hasNext()) {
							ResultRecord rd1 = (ResultRecord) ri.next();
							int orders = rd1.getInt("orders");
							System.out.println(this.getClass() + "考试的科目的顺序号是："
									+ orders);
							switch (orders) {
							case 1:
								sql = "update form_table_contractor_prj_qkj set sjaqjy='厂级' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							case 2:
								sql = "update form_table_contractor_prj_qkj set sjaqjy='部门级' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							case 3:
								sql = "update form_table_contractor_prj_qkj set sjaqjy='通过' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							case 4:
								sql = "update form_table_contractor_prj_qkj set gzpszr='负责人' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							case 5:
								sql = "update form_table_contractor_prj_qkj set gzpszr='签发人' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							case 6:
								sql = "update form_table_contractor_prj_qkj set gzpszr='许可人' where prj_id="
										+ prjId
										+ " and name="
										+ StrUtil.sqlstr(realName);
								break;
							default:
								break;
							}
							// System.out.println(this.getClass()+"更新项目全口径人员表的sql"+sql);
							jt.executeUpdate(sql);
						}

					}
				}
			}
			if (msg != "") {
				throw new ErrMsgException(msg + "的成绩上传失败");
			}
		} catch (BiffException ex) {
			throw new ErrMsgException("请上传.xls格式的文件！");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
