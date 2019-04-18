package com.redmoon.oa.worklog;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;
import org.springframework.web.util.HtmlUtils;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.prj.PrjMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.worklog.dao.MyWorkManageDao;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogAttach;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

/**
 * 工作汇报action
 * 
 * @author jfy
 * @date Jul 9, 2015
 */
public class MyWorkManageAction extends ActionSupport {
	private Logger logger = Logger
			.getLogger(MyWorkManageAction.class.getName());

	private int apraiseType;

	/**
	 * @return the apraiseType
	 */
	public int getApraiseType() {
		return apraiseType;
	}

	/**
	 * @param apraiseType
	 *            the apraiseType to set
	 */
	public void setApraiseType(int apraiseType) {
		this.apraiseType = apraiseType;
	}

	// 日报
	public static final String TYPE_DAY = "0";
	// 周报
	public static final String TYPE_WEEK = "1";
	// 月报
	public static final String TYPE_MONTH = "2";
	// 开始时间
	private static final String BEGIN_TIME = " 00:00:00";
	// 结束时间
	private static final String END_TIME = " 23:59:59";
	// 月报显示格式
	private static final String MONTH_SHOW_FORMAT = "yyyy年MM月";
	// 返回数据
	private List<WorkLog> list = new ArrayList<WorkLog>();
	// 汇报类型
	private String logType = "0";
	// 时间
	private String dateCond;
	// 内容条件
	private String contentCond;
	// 今日汇报是否编写
	private String isPreparedTodys;
	// 超出几天内可补写
	private int dayLimit = Config.getInstance().getIntProperty("dayLimit");
	// json调用信息
	private String message;
	// 内容
	private String content;
	// 新增信息
	private WorkLog workLog;
	// 所有文件信息
	private File[] file;
	// 文件名
	private String[] fileFileName;
	// 文件路径
	private String[] filePath;
	// 主表ID
	private int workLogId;
	// 附件ID
	private int attachId;
	// 前一天或后一天 0：前一天 1：后一天
	private int beforeOrAfter;
	// 评论内容
	private String reviewContent;
	// 评论返回信息
	private WorkLogExpand reWle;
	// 最后一条记录的开始时间
	private String lastBeginTime;
	// 当前页
	private int curPage = 0;
	// 每页显示数量
	private int pageSize = 5;
	// 时间区间
	private String dateArea;
	// 是否当前周
	private String isLast;
	// 根据用户查询
	private String userName;
	// 查看时显示标题
	private String pageTitle;

	private String formCode;

	private int prjId;

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public int getPrjId() {
		return prjId;
	}

	public void setPrjId(int prjId) {
		this.prjId = prjId;
	}

	public String getDateCond() {
		return dateCond;
	}

	public void setDateCond(String dateCond) {
		this.dateCond = dateCond;
	}

	public String getContentCond() {
		return contentCond;
	}

	public void setContentCond(String contentCond) {
		this.contentCond = contentCond;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public List<WorkLog> getList() {
		return list;
	}

	public String getIsPreparedTodys() {
		return isPreparedTodys;
	}

	public void setIsPreparedTodys(String isPreparedTodys) {
		this.isPreparedTodys = isPreparedTodys;
	}

	private void setDayLimit(int dayLimit) {
		this.dayLimit = dayLimit;
	}

	public int getDayLimit() {
		return dayLimit;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public WorkLog getWorkLog() {
		return workLog;
	}

	private void setWorkLog(WorkLog workLog) {
		this.workLog = workLog;
	}

	public File[] getFile() {
		return file;
	}

	public void setFile(File[] file) {
		this.file = file;
	}

	public String[] getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String[] fileFileName) {
		this.fileFileName = fileFileName;
	}

	public String[] getFilePath() {
		return filePath;
	}

	public void setFilePath(String[] filePath) {
		this.filePath = filePath;
	}

	public int getWorkLogId() {
		return workLogId;
	}

	public void setWorkLogId(int workLogId) {
		this.workLogId = workLogId;
	}

	public int getAttachId() {
		return attachId;
	}

	public void setAttachId(int attachId) {
		this.attachId = attachId;
	}

	public int getBeforeOrAfter() {
		return beforeOrAfter;
	}

	public void setBeforeOrAfter(int beforeOrAfter) {
		this.beforeOrAfter = beforeOrAfter;
	}

	public String getReviewContent() {
		return reviewContent;
	}

	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}

	public WorkLogExpand getReWle() {
		return reWle;
	}

	public void setReWle(WorkLogExpand reWle) {
		this.reWle = reWle;
	}

	public String getLastBeginTime() {
		return lastBeginTime;
	}

	public void setLastBeginTime(String lastBeginTime) {
		this.lastBeginTime = lastBeginTime;
	}

	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getDateArea() {
		return dateArea;
	}

	public void setDateArea(String dateArea) {
		this.dateArea = dateArea;
	}

	public String getIsLast() {
		return isLast;
	}

	public void setIsLast(String isLast) {
		this.isLast = isLast;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 * 初始化页面
	 * 
	 * @return
	 */
	public String init() {
		Privilege priv = new Privilege();
		HttpServletRequest request = ServletActionContext.getRequest();
		if (!priv.isUserLogin(request)) {
			return "nologin";
		}
		Date now = new Date();
		String endDate = null;
		String beginDate = null;
		try {
			if (dateCond == null || dateCond.equals("")) {
				endDate = DateUtil.format(now, DateUtil.DATE_FORMAT) + END_TIME;
				setDateCond(DateUtil.format(now, "yyyy/MM/dd"));
			}
			if (logType.equals(TYPE_DAY)) {// 日报
				MyWorkManageServices mwms = MyWorkManageServicesImpl
						.getInstance();
				WorkLog wl = new WorkLog();
				wl.setBeginDate(beginDate);
				wl.setEndDate(endDate);
				if (userName == null || userName.equals("")) {
					wl.setUserName(priv.getUser(request));
				} else {
					wl.setUserName(userName);
				}
				wl.setType(TYPE_DAY);
				/*
				 * String code = ParamUtil.get(request, "code"); int prjId =
				 * ParamUtil.getInt(request, "id", -1); wl.setPrjId(prjId);
				 * wl.setFormCode(code);
				 */

				beginDate = getBeforeInfos(endDate, beginDate, mwms, wl);
				setIsPreparedTodys(String.valueOf(mwms.isPrepared(DateUtil
						.parse(endDate, DateUtil.DATE_TIME_FORMAT), Integer
						.valueOf(TYPE_DAY))));
				setLastBeginTime(beginDate);
				setMessage("初始化成功");
			}
		} catch (Exception e) {
			logger.error("init error:" + e.getMessage());
			setMessage("初始化失败");
		}
		return SUCCESS;
	}

	/**
	 * 查询个人汇报信息
	 * 
	 * @return
	 */
	public String queryMyWork() {
		// 查询类型 0：前一天后一天 1：按照内容查询
		int queryType = 0;
		Privilege priv = new Privilege();
		HttpServletRequest request = ServletActionContext.getRequest();
		if (!priv.isUserLogin(request)) {
			return "nologin";
		}
		Date now = new Date();
		String endDate = null;
		String beginDate = null;
		MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
		try {
			if (logType.equals(TYPE_DAY)) {// 日报
				if (dateCond == null || dateCond.equals("")) {
					endDate = DateUtil.format(now, DateUtil.DATE_FORMAT)
							+ END_TIME;
					setDateCond(DateUtil.format(now, "yyyy/MM/dd"));
				} else {
					Date conDate = DateUtil.parse(dateCond,
							DateUtil.DATE_FORMAT);
					// 判断是否在当前时间之前
					if (beforeOrAfter == 0) { // 前一天
						Date beforeWorkDay = addWorkDay(dateCond, -1);
						endDate = DateUtil.format(beforeWorkDay,
								DateUtil.DATE_FORMAT)
								+ END_TIME;
						this.setDateCond(DateUtil.format(beforeWorkDay,
								"yyyy/MM/dd"));
						queryType = 0;// 非内容查询
					} else if (beforeOrAfter == 1) {// 后一天
						Date afterWorkDay = addWorkDay(dateCond, 1);
						if (conDate.before(DateUtil.parse(DateUtil.format(now,
								DateUtil.DATE_FORMAT), DateUtil.DATE_FORMAT))) {
							endDate = DateUtil.format(afterWorkDay,
									DateUtil.DATE_FORMAT)
									+ END_TIME;
							setDateCond(DateUtil.format(afterWorkDay,
									"yyyy/MM/dd"));
						} else {
							endDate = DateUtil
									.format(now, DateUtil.DATE_FORMAT)
									+ END_TIME;
							setDateCond(DateUtil.format(now, "yyyy/MM/dd"));
						}
						queryType = 0;// 非内容查询
					} else if (beforeOrAfter == 2) {
						endDate = DateUtil.format(DateUtil.parse(dateCond,
								"yyyy/MM/dd"), DateUtil.DATE_FORMAT)
								+ END_TIME;
						setDateCond(dateCond);
						queryType = 0;// 非内容查询
					} else {// 按照内容查询
						endDate = DateUtil
								.format(conDate, DateUtil.DATE_FORMAT)
								+ END_TIME;
						setDateCond(DateUtil.format(conDate, "yyyy/MM/dd"));
						queryType = 1;// 内容查询
					}
				}

				WorkLog wl = new WorkLog();
				if (userName == null || userName.equals("")) {
					wl.setUserName(priv.getUser(request));
				} else {
					wl.setUserName(userName);
					UserDb ud = new UserDb(userName);
					setPageTitle(ud.getRealName());
				}
				wl.setType(TYPE_DAY);

				String code = ParamUtil.get(request, "code");
				int prjId = ParamUtil.getInt(request, "id", 0);
				boolean isPrjOrTask = !"".equals(code) && prjId != 0;
				wl.setPrjId(prjId);
				wl.setFormCode(code);

				if (!isPrjOrTask && queryType == 0) {
					// 设置当前编辑状态
					Date queryDate = DateUtil.parse(endDate,
							DateUtil.DATE_TIME_FORMAT);

					if (DateUtil.datediff(now, queryDate) > dayLimit) {
						setIsPreparedTodys("true");
					} else {
						setIsPreparedTodys(String.valueOf(mwms.isPrepared(
								queryDate, Integer.valueOf(TYPE_DAY))));
					}
					beginDate = getBeforeInfos(endDate, beginDate, mwms, wl);
				} else {
					java.util.Date curDate = new java.util.Date();
					// 检查当天项目或者任务的负责人有没有写日报，如果已写，则创建关联
					PrjMgr pm = new PrjMgr();
					if (isPrjOrTask) {
						String prjOrTaskUserName;
						if (code.equals(com.redmoon.oa.prj.PrjConfig.CODE_PRJ)) {
							prjOrTaskUserName = pm.getPrjManager(prjId);
						} else {
							prjOrTaskUserName = pm.getTaskManager(prjId);
						}

						MyWorkManageDao mm = MyWorkManageDao.getInstance();
						long curWorkLogId = mm
								.getWorkLogId(prjOrTaskUserName,
										DateUtil.format(curDate,
												"yyyy-MM-dd 00:00:00"), logType);
						if (curWorkLogId != -1) {
							// 如存在当天的日报则关联
							pm.relateWithPrjOrTask(curWorkLogId, code, prjId);
						}
					}

					wl.setCondContent(contentCond);
					wl.setCurPage(curPage + 1);
					wl.setPageSize(pageSize);
					wl.setEndDate(null);
					wl.setBeginDate(null);
					list = mwms.queryWorkLogInfos(wl);
					setCurPage(curPage + 1);
					setIsPreparedTodys("true");

					boolean canWrite = false;
					WorkLog tempLog = new WorkLog();
					if (list.size() > 0) {
						tempLog = list.get(0);
						if (tempLog.getMyDate().equals("今天")) {
							canWrite = false;
						} else if (tempLog.getMyDate().equals("昨天")) {
							canWrite = true;
						} else if (curDate.after(DateUtil.parse(tempLog.getMyDate(),
								"yyyy年MM月dd日"))) {
							canWrite = true;
						}
					} else {
						canWrite = true;
					}
					if (canWrite) {
						tempLog = new WorkLog();
						tempLog.setContent("暂未填写");
						tempLog.setMyDate(DateUtil.parseDate(DateUtil.format(curDate,
								DateUtil.DATE_TIME_FORMAT)));

						tempLog.setId(Integer.valueOf(DateUtil.format(curDate,
								"yyyyMMdd")));
						list.add(0, tempLog);
					}
				}

				setDayLimit(Config.getInstance().getIntProperty("dayLimit"));
				setLastBeginTime(beginDate);
				this.setMessage("查询成功");

			} else if (logType.equals(TYPE_WEEK)) {// 周报
				if (dateCond == null || dateCond.equals("")) {
					setDateCond(String.valueOf(DateUtil.getWeekOfYear(now)));
					beginDate = DateUtil.format(
							DateUtil.getFirstDayOfWeek(now),
							DateUtil.DATE_FORMAT);
					endDate = DateUtil.format(DateUtil.getLastDayOfWeek(now),
							DateUtil.DATE_FORMAT);
					setDateArea(beginDate + "至" + endDate);
					setIsLast("true");
				} else {
					if (beforeOrAfter == 0) { // 前一周
						int year = Integer.valueOf(dateArea.split("至")[1]
								.split("-")[0]);
						int beforeWeek = Integer.valueOf(dateCond) - 1;
						if (beforeWeek < 1) {// 跨年，则取前年的最后一周
							beforeWeek = DateUtil.getMaxWeekNumOfYear(year);
							beginDate = DateUtil.format(DateUtil
									.getFirstDayOfWeek(year - 1, beforeWeek),
									DateUtil.DATE_FORMAT);
							endDate = DateUtil.format(DateUtil
									.getLastDayOfWeek(year - 1, beforeWeek),
									DateUtil.DATE_FORMAT);
						} else {
							beginDate = DateUtil.format(DateUtil
									.getFirstDayOfWeek(year, beforeWeek),
									DateUtil.DATE_FORMAT);
							endDate = DateUtil.format(DateUtil
									.getLastDayOfWeek(year, beforeWeek),
									DateUtil.DATE_FORMAT);
						}
						setDateArea(beginDate + "至" + endDate);
						setDateCond(String.valueOf(beforeWeek));
						queryType = 0;// 非内容查询
					} else if (beforeOrAfter == 1) {// 后一周
						int year = Integer.valueOf(dateArea.split("至")[0]
								.split("-")[0]);
						int afterWeek = Integer.valueOf(dateCond) + 1;
						int nowYear = DateUtil.getYear(now);
						if (year < nowYear) { // 小于当年年份
							if (afterWeek > DateUtil.getMaxWeekNumOfYear(year)) {// 超过既定年最大周数，则取后一年第一周
								Date lastWeekDay = DateUtil.getLastDayOfWeek(
										year, Integer.valueOf(dateCond));
								if (now.before(lastWeekDay)) {// 当前时间小于查询周周末时间
									afterWeek = DateUtil.getWeekOfYear(now);
									beginDate = DateUtil.format(
											DateUtil.getFirstDayOfWeek(year,
													afterWeek),
											DateUtil.DATE_FORMAT);
									endDate = DateUtil.format(DateUtil
											.getLastDayOfWeek(year, afterWeek),
											DateUtil.DATE_FORMAT);
									setIsLast("true");
								} else {
									afterWeek = 1;
									beginDate = DateUtil.format(DateUtil
											.getFirstDayOfWeek(year + 1,
													afterWeek),
											DateUtil.DATE_FORMAT);
									endDate = DateUtil.format(DateUtil
											.getLastDayOfWeek(year + 1,
													afterWeek),
											DateUtil.DATE_FORMAT);
									setIsLast("false");
								}
							}
						} else if (year == nowYear) {// 同一年
							int nowWeek = DateUtil.getWeekOfYear(now);
							if (afterWeek >= nowWeek) {
								afterWeek = nowWeek;
								beginDate = DateUtil.format(DateUtil
										.getFirstDayOfWeek(now),
										DateUtil.DATE_FORMAT);
								endDate = DateUtil.format(DateUtil
										.getLastDayOfWeek(now),
										DateUtil.DATE_FORMAT);
								setIsLast("true");
							} else {
								beginDate = DateUtil.format(DateUtil
										.getFirstDayOfWeek(year, afterWeek),
										DateUtil.DATE_FORMAT);
								endDate = DateUtil.format(DateUtil
										.getLastDayOfWeek(year, afterWeek),
										DateUtil.DATE_FORMAT);
								setIsLast("false");
							}

						} else {// 否则取当前周
							afterWeek = DateUtil.getWeekOfYear(now);
							beginDate = DateUtil.format(DateUtil
									.getFirstDayOfWeek(now),
									DateUtil.DATE_FORMAT);
							endDate = DateUtil.format(DateUtil
									.getLastDayOfWeek(now),
									DateUtil.DATE_FORMAT);
							setIsLast("true");
						}
						setDateArea(beginDate + "至" + endDate);
						setDateCond(String.valueOf(afterWeek));
						queryType = 0;// 非内容查询
					} else {// 按照内容查询
						queryType = 1;// 内容查询
					}
				}
				if (queryType == 0) {// 非内容查询
					int beginYear = Integer.valueOf(dateArea.split("至")[0]
							.split("-")[0]);
					int beginWeek = Integer.valueOf(dateCond);
					if (lastBeginTime != null && !"".equals(lastBeginTime)) {
						String[] lastBeingTimes = lastBeginTime.split("-");
						if (lastBeingTimes.length > 1) {
							beginYear = Integer.valueOf(lastBeingTimes[0]);
							beginWeek = Integer.valueOf(lastBeingTimes[1]) - 1;// 最后一条记录的前一周
						}
					}
					List<WorkLog> tempList = null;
					WorkLog wl = new WorkLog();
					// WorkLogForModuleMgr wmfm = new WorkLogForModuleMgr();
					String code = ParamUtil.get(request, "code");
					int prjId = ParamUtil.getInt(request, "id", -1);
					if (code != null && !"".equals(code) && prjId != -1) {
						wl.setUserName(userName);
						wl.setType(logType);
						wl.setPrjId(prjId);
						wl.setFormCode(code);
						tempList = mwms.queryWeekLogInfos(wl);
						boolean canWrite = false;
						WorkLog tempWl = new WorkLog();
						if (tempList != null && tempList.size() > 0) {
							for (int i = 0; i < tempList.size(); i++) {
								tempWl = tempList.get(i);
								setShowWeekByDate(now, tempWl);
								// list.addAll(tempList);
								list.add(tempWl);
							}

							tempWl = list.get(0);
							if (beginWeek > tempWl.getLogItem()) {
								canWrite = true;
							}
						} else {
							canWrite = true;
						}

						// 如果无记录，或者记录的最后一条在当前周之前
						if (canWrite) {
							tempWl = new WorkLog();
							tempWl.setType(wl.getType());
							tempWl.setLogItem(beginWeek);
							tempWl.setLogYear(beginYear);
							setShowWeekByDate(now, tempWl);
							tempWl.setId(Integer.valueOf(String
									.valueOf(beginYear)
									+ String.valueOf(beginWeek)));
							tempWl.setContent("暂未填写");
							list.add(0, tempWl);
						}
					} else {
						wl.setPageSize(0);
						wl.setFormCode("");
						List<String> weekList = getBeforeWeeks(beginYear + "-"
								+ beginWeek, 5);
						int tempWeekNum = 0;
						int tempWeekYear = 0;
						for (String tempWeek : weekList) {

							String[] yearAndWeeks = tempWeek.split("-");
							if (yearAndWeeks.length > 1) {
								tempWeekNum = Integer.valueOf(tempWeek
										.split("-")[1]);
								tempWeekYear = Integer.valueOf(tempWeek
										.split("-")[0]);
								wl.setUserName(priv.getUser(request));

								wl.setLogItem(tempWeekNum);
								wl.setLogYear(tempWeekYear);
								wl.setType(logType);
								tempList = mwms.queryWeekLogInfos(wl);
								WorkLog tempWl = new WorkLog();
								if (tempList.size() > 0) {
									tempWl = tempList.get(0);
									setShowWeekByDate(now, tempWl);
									list.addAll(tempList);
								} else {
									tempWl.setLogItem(tempWeekNum);
									tempWl.setLogYear(tempWeekYear);
									setShowWeekByDate(now, tempWl);
									tempWl.setId(Integer.valueOf(String
											.valueOf(tempWeekYear)
											+ String.valueOf(tempWeekNum)));
									tempWl.setContent("暂未填写");
									list.add(tempWl);
								}

							}
							setLastBeginTime(tempWeek);
						}
					}
				} else {
					WorkLog wl = new WorkLog();
					// WorkLogForModuleMgr wmfm = new WorkLogForModuleMgr();
					String formCode = ParamUtil.get(request, "code");
					int prjId = ParamUtil.getInt(request, "id", 0);
					List<WorkLog> tempList = null;
					if ((formCode != null && !"".equals(formCode))
							&& prjId != 0) {
						wl.setType(logType);
						wl.setPrjId(prjId);
						wl.setFormCode(formCode);
						wl.setCondContent(contentCond);
						tempList = mwms.queryWeekLogInfos(wl);
						WorkLog tempWl = new WorkLog();
						if (tempList != null && tempList.size() > 0) {
							for (int i = 0; i < tempList.size(); i++) {
								tempWl = tempList.get(i);
								setShowWeekByDate(now, tempWl);
								// list.addAll(tempList);
								list.add(tempWl);
							}
						}
					} else {
						wl.setCurPage(curPage + 1);
						wl.setType(TYPE_WEEK);
						wl.setUserName(priv.getUser(request));

						wl.setPageSize(pageSize);
						wl.setCondContent(contentCond);
						wl.setBeginDate(null);
						list = mwms.queryWorkLogInfos(wl);
						for (WorkLog tempWl : list) {
							setShowWeekByDate(now, tempWl);
						}
						setCurPage(curPage + 1);
					}
				}
				setDayLimit(Config.getInstance().getIntProperty("weekLimit"));
				setMessage("查询成功");
			} else if (logType.equals(TYPE_MONTH)) {// 月报
				setDayLimit(Config.getInstance().getIntProperty("monthLimit"));
				if (dateCond == null || dateCond.equals("")) {
					setDateCond(DateUtil.format(now, MONTH_SHOW_FORMAT));
					setIsLast("true");
				} else {
					Date editDate = DateUtil.parse(dateCond, "yyyy年MM月");
					if (beforeOrAfter == 0) { // 前一月
						Date beforeDate = DateUtil.addMonthDate(editDate, -1);
						setDateCond(DateUtil.format(beforeDate,
								MONTH_SHOW_FORMAT));
						queryType = 0;// 非内容查询
					} else if (beforeOrAfter == 1) {// 后一月
						Date afterDate = DateUtil.addMonthDate(editDate, 1);
						if (afterDate.after(now)) {
							afterDate = now;
							setIsLast("true");
						} else {
							setIsLast("false");
						}
						setDateCond(DateUtil.format(afterDate,
								MONTH_SHOW_FORMAT));
						queryType = 0;// 非内容查询
					} else {// 按照内容查询
						queryType = 1;// 内容查询
					}
				}
				if (queryType == 0) {// 非内容查询
					List<WorkLog> tempList = null;
					WorkLog wl = new WorkLog();
					// WorkLogForModuleMgr wmfm = new WorkLogForModuleMgr();
					String formCode = ParamUtil.get(request, "code");
					int prjId = ParamUtil.getInt(request, "id", 0);

					if (lastBeginTime == null || "".equals(lastBeginTime)) {// lastBeginTime为空，则设置lastBeginTime为dateCond
						lastBeginTime = DateUtil.format(DateUtil.parse(
								dateCond, MONTH_SHOW_FORMAT), "yyyyMM");
					}

					if ((formCode != null && !"".equals(formCode))
							&& prjId != 0) {
						wl.setType(logType);
						wl.setPrjId(prjId);
						wl.setFormCode(formCode);
						tempList = mwms.queryWeekLogInfos(wl);
						WorkLog tempWl = new WorkLog();

						java.util.Date curDay = new java.util.Date();
						int tempMonthNum = DateUtil.getMonth(curDay) + 1;
						int tempMonthYear = DateUtil.getYear(curDay);
						boolean canWrite = false;
						if (tempList != null && tempList.size() > 0) {
							for (int i = 0; i < tempList.size(); i++) {
								tempWl = tempList.get(i);
								// setShowWeekByDate(now, tempWl);
								// list.addAll(tempList);
								tempWl.setShowTitle(tempWl.getLogYear() + "年"
										+ tempWl.getLogItem() + "月");
								list.add(tempWl);
							}

							tempWl = list.get(0);
							if ((tempMonthYear == tempWl.getLogYear() && tempMonthNum > tempWl
									.getLogItem())
									|| (tempMonthYear > tempWl.getLogYear())) {
								canWrite = true;
							}
						} else {
							canWrite = true;
						}

						// 如果无记录，或者记录的最后一条在当前月之前
						if (canWrite) {
							tempWl = new WorkLog();
							tempWl.setType(wl.getType());

							String tempId = String.valueOf(tempMonthYear)
									+ String.valueOf(tempMonthNum);

							tempWl.setLogItem(tempMonthNum);
							tempWl.setLogYear(tempMonthYear);
							tempWl.setShowTitle(DateUtil.format(curDay,
									MONTH_SHOW_FORMAT));
							tempWl.setId(Integer.valueOf(tempId));

							tempWl.setContent("暂未填写");
							list.add(0, tempWl);
						}
					} else {
						wl.setPageSize(0);
						wl.setFormCode("");

						List<Date> MonthDateList = getBeforeMonths(DateUtil
								.parse(lastBeginTime, "yyyyMM"), 5);
						int tempMonthNum = 0;
						int tempMonthYear = 0;
						for (Date temp : MonthDateList) {

							tempMonthNum = DateUtil.getMonth(temp) + 1;
							tempMonthYear = DateUtil.getYear(temp);
							String tempId = String.valueOf(tempMonthYear)
									+ String.valueOf(tempMonthNum);
							wl.setUserName(priv.getUser(request));

							wl.setLogItem(tempMonthNum);
							wl.setLogYear(tempMonthYear);
							wl.setType(TYPE_MONTH);
							tempList = mwms.queryWeekLogInfos(wl);
							WorkLog tempWl = new WorkLog();
							if (tempList.size() > 0) {
								tempWl = tempList.get(0);
								tempWl.setShowTitle(DateUtil.format(temp,
										MONTH_SHOW_FORMAT));
								list.addAll(tempList);
							} else {
								tempWl.setLogItem(tempMonthNum);
								tempWl.setLogYear(tempMonthYear);
								tempWl.setShowTitle(DateUtil.format(temp,
										MONTH_SHOW_FORMAT));
								tempWl.setId(Integer.valueOf(tempId));
								tempWl.setContent("暂未填写");
								list.add(tempWl);
							}
							setLastBeginTime(DateUtil.format(DateUtil.addMonth(
									temp, -1), "yyyyMM"));
						}
					}
				} else {
					WorkLog wl = new WorkLog();
					// WorkLogForModuleMgr wmfm = new WorkLogForModuleMgr();
					String formCode = ParamUtil.get(request, "code");
					int prjId = ParamUtil.getInt(request, "id", 0);
					List<WorkLog> tempList = null;
					if ((formCode != null && !"".equals(formCode))
							&& prjId != 0) {
						wl.setType(logType);
						wl.setPrjId(prjId);
						wl.setFormCode(formCode);
						wl.setCondContent(contentCond);
						tempList = mwms.queryWeekLogInfos(wl);
						WorkLog tempWl = new WorkLog();
						if (tempList != null && tempList.size() > 0) {
							for (int i = 0; i < tempList.size(); i++) {
								tempWl = tempList.get(i);
								Calendar calendar = Calendar.getInstance();
								calendar.set(Calendar.MONTH, tempWl
										.getLogItem() - 1);
								calendar
										.set(Calendar.YEAR, tempWl.getLogYear());
								String showTitle = DateUtil.format(calendar
										.getTime(), MONTH_SHOW_FORMAT);
								tempWl.setShowTitle(showTitle);
								// tempWl.setShowTitle(DateUtil.format(temp,
								// MONTH_SHOW_FORMAT));
								list.addAll(tempList);
							}
						}
					} else {
						wl.setCurPage(curPage + 1);
						wl.setType(TYPE_MONTH);
						wl.setUserName(priv.getUser(request));

						wl.setPageSize(pageSize);
						wl.setCondContent(contentCond);
						wl.setBeginDate(null);
						list = mwms.queryWorkLogInfos(wl);
						for (WorkLog tempWl : list) {
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.MONTH,
									tempWl.getLogItem() - 1);
							calendar.set(Calendar.YEAR, tempWl.getLogYear());
							String showTitle = DateUtil.format(calendar
									.getTime(), MONTH_SHOW_FORMAT);
							tempWl.setShowTitle(showTitle);
							// setShowWeekByDate(now , tempWl);
						}
						setCurPage(curPage + 1);
					}
				}
				setMessage("查询成功");
			}
		} catch (Exception e) {
			logger.error("类型:" + logType + " queryMyWork is error:"
					+ e.getMessage());
			setMessage("查询失败");
		}
		return SUCCESS;
	}

	/**
	 * 获取前n个月(包含date所在月)
	 * 
	 * @param date
	 * @return
	 */
	private List<Date> getBeforeMonths(Date date, int n) {
		List<Date> list = new ArrayList<Date>();
		for (int i = 0; i < n; i++) {
			list.add(DateUtil.addMonthDate(date, -i));
		}
		return list;
	}

	/**
	 * 根据传入信息及当前时间，设置showWeek
	 * 
	 * @param nowDate
	 * @param tempWl
	 */
	private void setShowWeekByDate(Date nowDate, WorkLog tempWl) {
		// 本周转换
		if (tempWl.getLogYear() == DateUtil.getYear(nowDate)
				&& tempWl.getLogItem() == DateUtil.getWeekOfYear(nowDate)) {
			tempWl.setShowTitle("本周（"
					+ DateUtil.format(DateUtil.getFirstDayOfWeek(nowDate),
							DateUtil.DATE_FORMAT)
					+ "至"
					+ DateUtil.format(DateUtil.getLastDayOfWeek(nowDate),
							DateUtil.DATE_FORMAT) + "）");
		} else {
			tempWl.setShowTitle("第"
					+ tempWl.getLogItem()
					+ "周（"
					+ DateUtil.format(DateUtil.getFirstDayOfWeek(tempWl
							.getLogYear(), tempWl.getLogItem()),
							DateUtil.DATE_FORMAT)
					+ "至"
					+ DateUtil.format(DateUtil.getLastDayOfWeek(tempWl
							.getLogYear(), tempWl.getLogItem()),
							DateUtil.DATE_FORMAT) + "）");
		}
	}

	/**
	 * 根据时间获取前面5个工作日的数据
	 * 
	 * @param endDate
	 * @param beginDate
	 * @param mwms
	 * @param wl
	 * @return
	 */
	private String getBeforeInfos(String endDate, String beginDate,
			MyWorkManageServices mwms, WorkLog wl) {
		// HttpServletRequest request = ServletActionContext.getRequest();
		List<WorkLog> tempList = null;
		if (logType.equals(TYPE_DAY)) {
			// 循环遍历每天记录
			List<Date> dateList = mwms.getWorkDaysFromDb(DateUtil.parse(
					endDate, DateUtil.DATE_FORMAT), 5);
			for (Date tempDate : dateList) {
				beginDate = DateUtil.format(tempDate, DateUtil.DATE_FORMAT)
						+ BEGIN_TIME;
				endDate = DateUtil.format(tempDate, DateUtil.DATE_FORMAT)
						+ END_TIME;
				wl.setBeginDate(beginDate);
				wl.setEndDate(endDate);

				tempList = mwms.queryWorkLogInfos(wl);

				if (tempList != null && tempList.size() > 0) {
					list.addAll(tempList);
				} else {
					WorkLog tempLog = new WorkLog();
					tempLog.setContent("暂未填写");
					try {
						tempLog.setMyDate(DateUtil.parseDate(beginDate));
					} catch (ParseException e) {
						this.message = "解析时间出错";
						logger.error("parse time error:" + e.getMessage());
					}
					tempLog.setId(Integer.valueOf(DateUtil.format(tempDate,
							"yyyyMMdd")));
					list.add(tempLog);

				}
			}

		} else if (logType.equals(TYPE_WEEK)) {

		} else if (logType.equals(TYPE_MONTH)) {

		}
		return beginDate;
	}

	/**
	 * 新增个人汇报
	 * 
	 * @return
	 */
	public String createMyWork() {
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		try {
			if (logType.equals(TYPE_DAY)) {// 日报
				Date nowDate = new Date();
				Date editDate = DateUtil.parse(dateCond, "yyyy/MM/dd");
				WorkLog wl = null;
				if (DateUtil.datediff(nowDate, editDate) <= dayLimit) {
					wl = new WorkLog();
					wl = createWorkLog(request, privilege, DateUtil.format(
							new Date(), DateUtil.DATE_TIME_FORMAT), wl);
				} else {
					this.message = "超过可补填期限，不能补填！";
				}
				setWorkLog(wl);
				this.message = "新增成功";
			} else if (logType.equals(TYPE_WEEK)) {// 周报
				setDayLimit(Config.getInstance().getIntProperty("dayLimit"));
				Date nowDate = new Date();
				WorkLog wl = new WorkLog();
				if (dateCond == null || dateCond.equals("")) {
					dateCond = DateUtil.getYear(nowDate)
							+ String.valueOf(DateUtil.getWeekOfYear(nowDate));
				}

				int nowWeeksOfYear = DateUtil.getWeekOfYear(nowDate);
				int year = DateUtil.getYear(nowDate);
				int editYear = Integer.valueOf(dateArea.split("至")[0]
						.split("-")[0]);
				int editWeeksOfYear = Integer.valueOf(dateCond);
				// 同一年
				if (year == editYear) {
					if (nowWeeksOfYear - editWeeksOfYear <= dayLimit) {
						wl.setLogItem(editWeeksOfYear);
						wl.setLogYear(editYear);
						setWorkLog(createWorkLog(request, privilege, null, wl));
						this.message = "新增成功";
					} else {
						this.message = "超过可补填期限，不能补填！";
					}
				} else if (year - editYear == 1) {
					// 获取跨年周差
					int betweenWeekNum = nowWeeksOfYear
							+ DateUtil.getMaxWeekNumOfYear(editYear)
							- editWeeksOfYear;
					if (betweenWeekNum <= dayLimit) {
						wl.setLogItem(editWeeksOfYear);
						wl.setLogYear(editYear);
						setWorkLog(createWorkLog(request, privilege, null, wl));
						this.message = "新增成功";
					} else {
						this.message = "超过可补填期限，不能补填！";
					}
				} else {
					this.message = "超过可补填期限，不能补填！";
				}
			} else {// 月报

			}
			this.setFile(null);
			this.setFileFileName(null);
			this.setFilePath(null);
			this.setContent(null);
			this.list = null;

		} catch (Exception ex) {
			logger.error("createMyWork is error:" + ex.getMessage());
			this.message = "新增失败！";
		}
		return SUCCESS;
	}

	/**
	 * 保存个人汇报
	 * 
	 * @param request
	 * @param privilege
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ParseException
	 */
	private WorkLog createWorkLog(HttpServletRequest request,
			Privilege privilege, String myDate, WorkLog wl)
			throws UnsupportedEncodingException, IOException, ParseException {
		boolean flag;
		int id = (int) SequenceManager.nextID(SequenceManager.OA_WORK_LOG);
		MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
		// 保存汇报信息
		wl.setId(id);
		wl.setContent(HtmlUtils.htmlUnescape(URLDecoder
				.decode(content, "utf-8")));
		wl.setUserName(privilege.getUser(request));
		if (myDate == null || myDate.equals("")) {// 为空设置为当前时间
			wl
					.setMyDate(DateUtil.format(new Date(),
							DateUtil.DATE_TIME_FORMAT));
		} else {
			wl.setMyDate(DateUtil.format(DateUtil.parse(myDate + BEGIN_TIME,
					DateUtil.DATE_TIME_FORMAT), DateUtil.DATE_TIME_FORMAT));
		}
		wl.setType(logType);
		flag = mwms.createMyWorkLog(wl);
		if (flag) {
			List<WorkLogAttach> list = new ArrayList<WorkLogAttach>();
			File[] tempFiles = this.getFile();
			if (tempFiles != null && tempFiles.length > 0) {
				FileUpload fu = new FileUpload();
				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
				fu.setValidExtname(cfg.get("netdisk_ext").split(","));
				// 文件路径
				String tmpPath = Global.getRealPath();
				WorkLogAttach workLogAttach = null;
				Calendar cal = Calendar.getInstance();
				// 置保存路径
				String year = "" + (cal.get(Calendar.YEAR));
				String month = "" + (cal.get(Calendar.MONTH) + 1);
				String vpath = "upfile/workLog/" + year + "/" + month + "/";
				String savePath = tmpPath + vpath;
				// 校验文件夹是否存在
				File f = new File(savePath);
				if (!f.isDirectory()) {
					f.mkdirs();
				}
				for (int i = 0; i < tempFiles.length; i++) {
					String name = fileFileName[i];
					boolean isValid = fu.isValidExtname(FileUtil
							.getFileExt(name));
					if (!isValid) {
						this.message = "部分非法文件未上传！";
						break;
					}
					long size = tempFiles[i].length();
					String diskName = FileUpload.getRandName() + "."
							+ FileUtil.getFileExt(name);
					int attId = (int) SequenceManager
							.nextID(SequenceManager.OA_NOTICE_ATTACH);
					// 存储文件
					File targetFile = new File(savePath, diskName);
					FileUtils.copyFile(tempFiles[i], targetFile);
					// 保存附件信息
					workLogAttach = new WorkLogAttach();
					workLogAttach.setId(attId);
					workLogAttach.setDiskName(diskName);
					workLogAttach.setFileSize(size);
					workLogAttach.setName(name);
					workLogAttach.setOrders(i);
					workLogAttach.setVisualPath(vpath);
					workLogAttach.setWorkLogId(id);
					// 保存成功才会计入该日志附件列表中
					if (mwms.createMyWorkAttach(workLogAttach)) {// mwms.createMyWorkAttach(workLogAttach)
						list.add(workLogAttach);
					}
				}
			}
			wl.setWorkLogAttachs(list);
		}
		if (TYPE_WEEK.equals(logType)) {
			setShowWeekByDate(new Date(), wl);
		} else if (TYPE_DAY.equals(logType)) {
			wl.setMyDate(DateUtil.parseDate(wl.getMyDate()));// 转化为多少XX前方式显示
		} else {

		}

		wl
				.setContent(HtmlUtils.htmlEscape(URLDecoder.decode(content,
						"utf-8")));
		return wl;
	}

	/**
	 * 保存汇报信息
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String saveWorkLog() {
		boolean flag = false;
		// 验证是否登录
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		Date nowDate = new Date();
		try {
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			// 保存汇报信息
			WorkLog wl = mwms.getWorkLogInfoById(workLogId);
			Date editDate = null;
			Date endDate = null;
			if (wl != null) {// 存在则更新
				editDate = wl.getRealDate();
				endDate = OACalendarDb.addWorkDay(editDate, dayLimit);
				if (!endDate.before(nowDate)) {
					flag = true;
				}

				if (flag) {
					String code = ParamUtil.get(request, "code");
					int prjId = ParamUtil.getInt(request, "id", 0);
					int process = ParamUtil.getInt(request, "process", 0);
					wl.setFormCode(code);
					wl.setPrjId(prjId);
					wl.setProcess(process);

					wl.setId(workLogId);
					wl.setContent(HtmlUtils.htmlUnescape(URLDecoder.decode(
							content, "utf-8")));
					flag = mwms.saveMyWorkLog(wl);
					if (flag) {
						File[] tempFiles = this.getFile();
						if (tempFiles != null && tempFiles.length > 0) {
							FileUpload fu = new FileUpload();
							com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
							fu.setValidExtname(cfg.get("netdisk_ext")
									.split(","));
							// 校验文件类型，含有非法类型文件，则直接退出
							boolean extValid = false;
							for (int i = 0; i < tempFiles.length; i++) {
								String fileName = fileFileName[i];
								boolean isValid = fu.isValidExtname(FileUtil
										.getFileExt(fileName));
								if (!isValid) {
									this.message = "含有非法类型文件(" + fileName
											+ ")，无法上传！";
									extValid = true;
									break;
								}
							}
							if (extValid) {
								wl.setContent(HtmlUtils.htmlEscape(URLDecoder
										.decode(content, "utf-8")));
								if (TYPE_WEEK.equals(logType)) {
									setShowWeekByDate(nowDate, wl);
								} else if (TYPE_MONTH.equals(logType)) {
									wl.setShowTitle(DateUtil.format(DateUtil
											.parse(wl.getLogYear() + "-"
													+ wl.getLogItem(),
													"yyyy-MM"),
											MONTH_SHOW_FORMAT));
								}
								UserDb ud = new UserDb(wl.getUserName());
								wl.setUserName(ud.getRealName());
								setWorkLog(wl);
								this.setFile(null);
								this.setFileFileName(null);
								this.setFilePath(null);
								this.setContent(null);
								this.list = null;
								return SUCCESS;
							}
							// 文件路径
							String tmpPath = Global.getRealPath();
							WorkLogAttach workLogAttach = null;
							Calendar cal = Calendar.getInstance();
							// 置保存路径
							String year = "" + (cal.get(cal.YEAR));
							String month = "" + (cal.get(cal.MONTH) + 1);
							String vpath = "upfile/workLog/" + year + "/"
									+ month + "/";
							String savePath = tmpPath + vpath;
							// 校验文件夹是否存在
							File f = new File(savePath);
							if (!f.isDirectory()) {
								f.mkdirs();
							}

							for (int i = 0; i < tempFiles.length; i++) {
								String name = fileFileName[i];
								long size = tempFiles[i].length();
								String diskName = FileUpload.getRandName()
										+ "." + FileUtil.getFileExt(name);
								int attId = (int) SequenceManager
										.nextID(SequenceManager.OA_NOTICE_ATTACH);
								// 存储文件
								File targetFile = new File(savePath, diskName);
								FileUtils.copyFile(tempFiles[i], targetFile);
								// 保存附件信息
								workLogAttach = new WorkLogAttach();
								workLogAttach.setId(attId);
								workLogAttach.setDiskName(diskName);
								workLogAttach.setFileSize(size);
								workLogAttach.setName(name);
								workLogAttach.setOrders(i);
								workLogAttach.setVisualPath(vpath);
								workLogAttach.setWorkLogId(workLogId);
								// 保存附件
								mwms.createMyWorkAttach(workLogAttach);
							}
						}
						// 设置附件信息
						List<WorkLogAttach> attList = new ArrayList<WorkLogAttach>();
						attList = mwms.getWorkLogAttachesByWorkLogId(workLogId);
						wl.setWorkLogAttachs(attList);
					}

					wl.setContent(HtmlUtils.htmlEscape(URLDecoder.decode(
							content, "utf-8")));
					if (TYPE_WEEK.equals(logType)) {
						setShowWeekByDate(nowDate, wl);
					} else if (TYPE_MONTH.equals(logType)) {
						wl.setShowTitle(DateUtil.format(DateUtil.parse(wl
								.getLogYear()
								+ "-" + wl.getLogItem(), "yyyy-MM"),
								MONTH_SHOW_FORMAT));
					}
					UserDb ud = new UserDb(wl.getUserName());
					wl.setUserName(ud.getRealName());
					setWorkLog(wl);

					this.message = "修改成功";
				} else {
					this.message = "超过可修改期限，不能修改！";
				}
			} else {// 无则创建
				editDate = DateUtil
						.parse(String.valueOf(workLogId), "yyyyMMdd");
				wl = new WorkLog();

				String code = ParamUtil.get(request, "code");
				int prjId = ParamUtil.getInt(request, "id", 0);
				int process = ParamUtil.getInt(request, "process", 0);
				
				if (!"".equals(code)) {
					if (!PrjMgr.isPrjOrTaskCanWriteDayWork(code, prjId)) {
						this.message = "任务不在有效期内或已完成！";
						return SUCCESS;
					}
				}
				
				wl.setFormCode(code);
				wl.setPrjId(prjId);
				wl.setProcess(process);

				if (TYPE_DAY.equals(logType)) {
					setDayLimit(Config.getInstance().getIntProperty("dayLimit"));
					MyWorkManageDao mm = MyWorkManageDao.getInstance();
					boolean re = mm
							.isTheDateExit(privilege.getUser(request),
									DateUtil.format(editDate,
											DateUtil.DATE_TIME_FORMAT));
					if (re) {
						this.message = "日报已存在，请刷新再试";
					} else {
						endDate = OACalendarDb.addWorkDay(editDate, dayLimit);
						if (endDate.before(nowDate)) {
							this.message = "超过可补填期限，不能补填！";
						} else {
							setWorkLog(createWorkLog(request, privilege,
									DateUtil.format(editDate,
											DateUtil.DATE_TIME_FORMAT), wl));

							if ("".equals(code)) {
								// 如存在待办中的项目或任务则关联
								PrjMgr pm = new PrjMgr();
								pm.relateWithCurPrjAndTask(workLog.getId(),
										workLog.getUserName());
							}

							this.message = "补填成功";
						}
					}
				} else if (TYPE_WEEK.equals(logType)) {
					setDayLimit(Config.getInstance()
							.getIntProperty("weekLimit"));
					if (dateArea == null || dateArea.equals("")) {
						editDate = nowDate;
					} else {
						editDate = DateUtil.parse(dateArea.split("至")[1],
								"yyyy-MM-dd");
					}
					endDate = OACalendarDb.addWorkDay(editDate, dayLimit);
					if (endDate.before(nowDate)) {
						this.message = "超过可补填期限，不能补填！";
					} else {
						int editYear = Integer.valueOf(dateArea.split("至")[0]
								.split("-")[0]);
						int editWeeksOfYear = Integer.valueOf(dateCond);
						if (dateCond == null || dateCond.equals("")) {
							dateCond = String.valueOf(DateUtil
									.getWeekOfYear(nowDate));
						} else {
							editYear = Integer.valueOf(String
									.valueOf(workLogId).substring(0, 4));
							editWeeksOfYear = Integer.valueOf(String.valueOf(
									workLogId).substring(4));
						}
						wl.setLogItem(editWeeksOfYear);
						wl.setLogYear(editYear);
						setWorkLog(createWorkLog(request, privilege, null, wl));

						if ("".equals(code)) {
							// 如存在待办中的项目或任务则关联
							PrjMgr pm = new PrjMgr();
							pm.relateWithCurPrjAndTask(workLog.getId(), workLog
									.getUserName());
						}

						this.message = "补填成功";
					}

					/*
					 * int nowWeeksOfYear = DateUtil.getWeekOfYear(nowDate); int
					 * year = DateUtil.getYear(nowDate); int editYear =
					 * Integer.valueOf(dateArea.split("至")[0].split("-")[0]);
					 * int editWeeksOfYear = Integer.valueOf(dateCond); if
					 * (dateCond == null || dateCond.equals("")){ dateCond =
					 * String.valueOf(DateUtil.getWeekOfYear(nowDate)); }else{
					 * editYear =
					 * Integer.valueOf(String.valueOf(workLogId).substring(0,
					 * 4)); editWeeksOfYear =
					 * Integer.valueOf(String.valueOf(workLogId).substring(4));
					 * }
					 * 
					 * //同一年 if (year == editYear){ if(nowWeeksOfYear -
					 * editWeeksOfYear <= dayLimit){
					 * wl.setLogItem(editWeeksOfYear); wl.setLogYear(editYear);
					 * setWorkLog(createWorkLog(request, privilege, null, wl));
					 * setMessage("补填成功"); }else{ this.message="超过可补填期限，不能补填！";
					 * } }else if(year - editYear == 1){ //获取跨年周差 int
					 * betweenWeekNum = nowWeeksOfYear +
					 * DateUtil.getMaxWeekNumOfYear(editYear) - editWeeksOfYear;
					 * if(betweenWeekNum <= dayLimit){
					 * wl.setLogItem(editWeeksOfYear); wl.setLogYear(editYear);
					 * setWorkLog(createWorkLog(request, privilege, null, wl));
					 * setMessage("补填成功"); }else{ this.message="超过可补填期限，不能补填！";
					 * } }else{ this.message="超过可补填期限，不能补填！"; }
					 */
				} else {
					setDayLimit(Config.getInstance().getIntProperty(
							"monthLimit"));
					editDate = new Date();

					if (dateArea != null && !dateArea.equals("")) {
						editDate = DateUtil.parse(dateArea, MONTH_SHOW_FORMAT);
					}

					editDate = DateUtil.addMonthDate(editDate, 1);
					endDate = OACalendarDb.addWorkDay(editDate, dayLimit);
					if (endDate.before(nowDate)) {
						this.message = "超过可补填期限，不能补填！";
					} else {
						Date editMonthDate = DateUtil.parse(dateCond,
								MONTH_SHOW_FORMAT);

						if (dateCond == null || dateCond.equals("")) {
							dateCond = DateUtil.format(nowDate,
									MONTH_SHOW_FORMAT);
						} else {
							editMonthDate = DateUtil.parse(String
									.valueOf(workLogId), "yyyyMM");
						}
						wl.setLogItem(DateUtil.getMonth(editMonthDate) + 1);
						wl.setLogYear(DateUtil.getYear(editMonthDate));
						wl.setShowTitle(DateUtil.format(editMonthDate,
								MONTH_SHOW_FORMAT));
						setWorkLog(createWorkLog(request, privilege, null, wl));

						if ("".equals(code)) {
							// 如存在待办中的项目或任务则关联
							PrjMgr pm = new PrjMgr();
							pm.relateWithCurPrjAndTask(workLog.getId(), workLog
									.getUserName());
						}

						this.message = "补填成功";
					}
					/*
					 * Date editMonthDate = DateUtil.parse(dateCond,
					 * MONTH_SHOW_FORMAT);
					 * 
					 * if (dateCond == null || dateCond.equals("")){ dateCond =
					 * DateUtil.format(nowDate, MONTH_SHOW_FORMAT); }else{
					 * editMonthDate = DateUtil.parse(String.valueOf(workLogId),
					 * "yyyyMM"); } //编辑时间+dayLimit早于当前时间，则不可编辑 if
					 * (DateUtil.addMonthDate(editMonthDate,
					 * dayLimit).before(nowDate)){ this.message="超过可补填期限，不能补填！";
					 * }else{ wl.setLogItem(DateUtil.getMonth(editMonthDate) +
					 * 1); wl.setLogYear(DateUtil.getYear(editMonthDate));
					 * wl.setShowTitle(DateUtil.format(editMonthDate,
					 * MONTH_SHOW_FORMAT)); setWorkLog(createWorkLog(request,
					 * privilege, null, wl)); setMessage("补填成功"); }
					 */
				}
			}
			this.setFile(null);
			this.setFileFileName(null);
			this.setFilePath(null);
			this.setContent(null);
			this.list = null;
		} catch (Exception e) {
			logger.error("saveWorkLog is error:" + e.getMessage());
			this.message = "保存失败！";
			e.printStackTrace();
		}

		return SUCCESS;
	}

	/**
	 * 删除附件
	 * 
	 * @return
	 */
	public String delAttach() {
		// 验证是否登录
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		try {
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			if (mwms.delAttach(attachId)) {
				this.message = "删除成功！";
			} else {
				this.message = "删除失败！";

			}
		} catch (Exception e) {
			this.message = "删除失败！";
			logger.error("delAttach is error:" + e.getMessage());
		}

		return SUCCESS;
	}

	/**
	 * 点赞
	 * 
	 * @return
	 */
	public String savePraise() {
		// 验证是否登录
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		try {
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			WorkLog wl = mwms.getWorkLogInfoById(workLogId);
			String nowUser = privilege.getUser(request);
			if (nowUser.equals(wl.getUserName())) {
				this.message = "自己不能给自己点赞！";
			} else {
				if (mwms.savePraise(workLogId)) {
					this.message = "点赞成功！";
				} else {
					this.message = "点赞失败！";

				}
			}
		} catch (Exception e) {
			this.message = "点赞失败！";
			logger.error("savePraise is error:" + e.getMessage());
		}
		return SUCCESS;
	}

	/**
	 * 点赞 或取消点赞
	 * 
	 * @Description:
	 * @return
	 */
	public String praiseWorkLog() {
		boolean flag = false;
		JSONObject jsonObj = new JSONObject();
		try {
			// 验证是否登录
			HttpServletRequest request = ServletActionContext.getRequest();
			Privilege privilege = new Privilege();
			if (!privilege.isUserLogin(request))
				return "nologin";
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			int id = (int) SequenceManager
					.nextID(SequenceManager.OA_WORK_LOG_EXPAND);
			WorkLogExpand wle = new WorkLogExpand();
			wle.setId(id);
			wle.setReview("");
			wle.setUserName(privilege.getUser(request));
			wle.setReviewTime(DateUtil.format(new Date(),
					DateUtil.DATE_TIME_FORMAT));
			wle.setWorkLogId(workLogId);
			flag = mwms.savePraiseOrCancel(wle, apraiseType);
			if (flag) {
				StringBuilder usersSb = new StringBuilder();
				WorkLog wl = mwms.getWorkLogInfoById(workLogId);
				int count = wl.getPraiseCount();
				List<WorkLogExpand> praiseList = wl.getWorkLogPraises();
				if (praiseList != null && praiseList.size() > 0) {
					for (WorkLogExpand wleItm : praiseList) {
						if (usersSb.toString().equals("")) {
							usersSb.append(wleItm.getUserName());
						} else {
							usersSb.append(",").append(wleItm.getUserName());
						}

					}
				}
				jsonObj.put("praiseUsers", usersSb.toString());
				jsonObj.put("praiseCount", count);
				jsonObj.put("res", 0);

			} else {
				jsonObj.put("res", -1);
			}

			this.message = jsonObj.toString();

		} catch (Exception e) {

			logger.error("praiseWorkLog is error:" + e.getMessage());
		}
		return SUCCESS;

	}

	/**
	 * 添加评论
	 * 
	 * @return
	 */
	public String saveReviewExpands() {
		// 验证是否登录
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		try {
			int id = (int) SequenceManager
					.nextID(SequenceManager.OA_WORK_LOG_EXPAND);
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			WorkLogExpand wle = new WorkLogExpand();
			wle.setId(id);
			wle.setReview(URLDecoder.decode(reviewContent, "utf-8"));
			wle.setUserName(privilege.getUser(request));
			wle.setReviewTime(DateUtil.format(new Date(),
					DateUtil.DATE_TIME_FORMAT));
			wle.setWorkLogId(workLogId);
			if (mwms.addReview(wle)) {
				this.message = "评论成功！";
				String dateTime = DateUtil.parseDateTime(wle.getReviewTime());
				wle.setReviewTime(dateTime);
				UserDb ud = new UserDb(wle.getUserName());
				wle.setUserName(ud.getRealName());
				this.setReWle(wle);
			} else {
				this.message = "评论失败！";

			}
		} catch (Exception e) {
			this.message = "评论失败！";
			logger.error("saveReviewExpands is error:" + e.getMessage());
		}
		return SUCCESS;
	}

	/**
	 * 工作督办跳转页面
	 * 
	 * @return
	 */
	public String showWorkLog() {
		Privilege priv = new Privilege();
		HttpServletRequest request = ServletActionContext.getRequest();
		if (!priv.isUserLogin(request)) {
			return "nologin";
		}
		UserDb userDb = new UserDb(userName);
		if (!userDb.isLoaded()) {
			setUserName(ParamUtil.get(request, "userName"));
		}

		WorkLog wl = new WorkLog();
		Date now = new Date();
		String endDate = null;
		String beginDate = null;
		try {
			if (dateCond == null || dateCond.equals("")) {
				endDate = DateUtil.format(now, DateUtil.DATE_FORMAT) + END_TIME;
				setDateCond(DateUtil.format(now, "yyyy/MM/dd"));
			}
			if (logType.equals(TYPE_DAY)) {// 日报
				MyWorkManageServices mwms = MyWorkManageServicesImpl
						.getInstance();
				wl.setBeginDate(beginDate);
				wl.setEndDate(endDate);
				wl.setType(TYPE_DAY);
				wl.setUserName(userName);

				beginDate = getBeforeInfos(endDate, beginDate, mwms, wl);
				setIsPreparedTodys(String.valueOf(mwms.isPrepared(DateUtil
						.parse(endDate, DateUtil.DATE_TIME_FORMAT), Integer
						.valueOf(TYPE_DAY))));
				setLastBeginTime(beginDate);
				setMessage("初始化成功");
				UserDb ud = new UserDb(userName);
				this.setPageTitle(ud.getRealName());
			}
		} catch (Exception e) {
			logger.error("showWorkLog error:" + e.getMessage());
			setMessage("初始化显示失败");
		}
		return SUCCESS;
	}

	/**
	 * 根据id获取汇报信息
	 * 
	 * @return
	 */
	public String getWorkLogById() {
		Privilege priv = new Privilege();
		HttpServletRequest request = ServletActionContext.getRequest();
		if (!priv.isUserLogin(request)) {
			return "nologin";
		}
		try {
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			WorkLog wl = mwms.getWorkLogInfoById(workLogId);
			wl.setMyDate(DateUtil
					.format(wl.getRealDate(), DateUtil.DATE_FORMAT));
			UserDb ud = new UserDb(wl.getUserName());
			if (TYPE_DAY.equals(wl.getType())) {
				setPageTitle(ud.getRealName() + "的日报");
			} else if (TYPE_WEEK.equals(wl.getType())) {
				setPageTitle(ud.getRealName() + "的周报");
			} else {
				setPageTitle(ud.getRealName() + "的月报");
			}
			if (list!=null)
				list.add(wl);
		} catch (Exception ex) {
			logger.error("getWorkLogById error:" + ex.getMessage() + StrUtil.trace(ex));
			ex.printStackTrace();
			setMessage("获取信息失败");
		}

		return SUCCESS;
	}

	/**
	 * 计算当前时间加上工作日
	 * 
	 * @param day
	 * @return
	 */
	private Date addWorkDay(String date, int day) {
		// 当天不计入超时时间
		// 遍历指定的当天其后的expire天，如果是休息日，则不计入，往后顺延
		Date cur = DateUtil.parse(date, DateUtil.DATE_FORMAT);
		OACalendarDb oad = new OACalendarDb();
		Date expireDate = DateUtil.addDate(cur, day);
		oad = (OACalendarDb) oad.getQObjectDb(expireDate);
		if (oad.getInt("date_type") != 0) {
			date = DateUtil.format(expireDate, DateUtil.DATE_FORMAT);
			expireDate = addWorkDay(date, day);
		}
		return expireDate;
	}

	/**
	 * 获取前N周
	 * 
	 * @return
	 */
	private List<String> getBeforeWeeks(String begin, int n) {
		List<String> list = new ArrayList<String>();
		int year = Integer.valueOf(begin.split("-")[0]);
		int weekNum = Integer.valueOf(begin.split("-")[1]);
		if (weekNum == 0) {
			year = year - 1;
			weekNum = DateUtil.getMaxWeekNumOfYear(year);
		}

		for (int i = 0; i < n; i++) {
			int beforeWeekNum = weekNum - i;
			if (beforeWeekNum >= 1) {// 非第一周
				list.add(String.valueOf(year) + "-"
						+ String.valueOf(beforeWeekNum));
			} else {
				int beforeYear = year - 1;
				int maxWeeks = DateUtil.getMaxWeekNumOfYear(beforeYear);
				list.add(String.valueOf(year - 1) + "-"
						+ String.valueOf(maxWeeks + beforeWeekNum));
			}
		}

		return list;
	}

	/**
	 * @Description: 是否超期
	 * @return
	 */
	public String checkCando() {
		// 验证是否登录
		HttpServletRequest request = ServletActionContext.getRequest();
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			return "nologin";
		Config config = Config.getInstance();

		if (!config.getBooleanProperty("canEditPreviousWorklog")) {
			this.message = "不能修改工作汇报！";
			return SUCCESS;
		}
		MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
		// 保存汇报信息
		WorkLog wl = mwms.getWorkLogInfoById(workLogId);

		Date editDate = null;
		Date endDate = null;
		Date nowDate = new Date();
		if (wl == null) {
			if (TYPE_DAY.equals(logType)) {
				editDate = DateUtil
						.parse(String.valueOf(workLogId), "yyyyMMdd");
			} else if (TYPE_WEEK.equals(logType)) {
				setDayLimit(config.getIntProperty("weekLimit"));
				if (dateArea == null || dateArea.equals("")) {
					editDate = nowDate;
				} else {
					editDate = DateUtil.parse(dateArea.split("至")[1],
							"yyyy-MM-dd");
				}
			} else if (TYPE_MONTH.equals(logType)) {
				setDayLimit(config.getIntProperty("monthLimit"));
				editDate = new Date();

				if (dateArea != null && !dateArea.equals("")) {
					editDate = DateUtil.parse(dateArea, MONTH_SHOW_FORMAT);
				}

				editDate = DateUtil.addMonthDate(editDate, 1);

			}

			endDate = OACalendarDb.addWorkDay(editDate, dayLimit);
			if (endDate.before(nowDate)) {
				this.message = "超过可补填期限，不能补填！";
			} else {
				this.message = "成功";
			}
		} else {
			editDate = wl.getRealDate();
			int editLimit = Config.getInstance().getIntProperty("editPreviousWorklogLimit");
			if (editLimit < 0) {
				editLimit = 3;
			}
			endDate = OACalendarDb.addWorkDay(editDate, editLimit);
			if (endDate.before(nowDate)) {
				this.message = "超过可修改期限，不能修改！";
			} else {
				this.message = "成功";
			}
		}

		return SUCCESS;
	}
}
