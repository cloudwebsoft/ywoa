package com.redmoon.oa.exam;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * @Description:
 * @author:
 * @Date: 2018-1-25上午10:12:09
 */
public class ExamExcelRead {

	Workbook book = null;

	public ExamExcelRead() {
	}

	public void ExamExcelhad(String xlspath, int isPrj) throws ErrMsgException,
			IndexOutOfBoundsException, SQLException, ParseException {

		boolean re = true;
		String realName, userName = "", msg = "", idCard, paperId = "", testtime = "", score = "", mobile = "";
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
			}
			if (k < 2)
				throw new ErrMsgException("导入Excel格式不正确，请检查！");
			for (int i = 1; i < rsRows; i++) {
				for (int j = 0; j <= 5; j++) {
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
				String sql = "select name from users where realName = "
						+ StrUtil.sqlstr(realName) + " and IDCard ="
						+ StrUtil.sqlstr(idCard);
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					ResultRecord rd = (ResultRecord) ri.next();
					userName = rd.getString("name");
					ScoreDb sc = new ScoreDb();
					sc.setUserName(userName);
					sc.setPaperId(Integer.parseInt(paperId));
					SimpleDateFormat sdf = new SimpleDateFormat(
							" yyyy-MM-dd HH:mm:ss ");
					Date d = sdf.parse(testtime);
					sc.setEndtime(d);
					sc.setScore(Integer.parseInt(score));
					sc.setIsprj(isPrj);
					sc.setMobile(mobile);
					re = sc.create();
				} else {
					if (msg == "") {
						msg = realName;
					} else {
						msg += "," + realName;
					}
				}

				if (re) {
					// 拿到明年日期
					java.text.Format formatter = new java.text.SimpleDateFormat(
							"yyyy-MM-dd");
					java.util.Date todayDate = new java.util.Date();
					long afterTime = (todayDate.getTime() / 1000) + 60 * 60
							* 24 * 365;
					todayDate.setTime(afterTime * 1000);
					String afterDate = formatter.format(todayDate);
					sql = "update form_table_personbasic set cert_expire = "
							+ StrUtil.sqlstr(afterDate) + " where user_name="
							+ StrUtil.sqlstr(userName);
					System.out.println("sql:" + sql);
					try {
						jt.executeUpdate(sql);
					} catch (SQLException e) {
						e.printStackTrace();
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
