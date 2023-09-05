package com.redmoon.oa.notice;

import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.pvg.Privilege;

;

public class NoticeValidator implements IFormValidator {

	@Override
	public String getExtraData() {
		return null;
	}

	@Override
	public boolean isUsed() {
		return true;
	}

	@Override
	public void onActionFinished(HttpServletRequest request, int flowId,
			FileUpload fu) {
		FormDb fd = new FormDb("tzgg");
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(flowId, fd);
		if (fdao != null && fdao.isLoaded()) {
			String isAll = StrUtil.getNullStr(fdao.getFieldValue("is_toAll"));
			if (isAll.equals("")) {
				isAll = StrUtil.getNullStr(fu.getFieldValue("is_toAll"));
				if (isAll.equals("")) {
					Privilege priv = new Privilege();
					if (priv.isUserPrivValid(request, "notice.dept")) {
						isAll = "1";
					} else if (priv.isUserPrivValid(request, "notice")) {
						isAll = "2";
					}
				}
				if (!isAll.equals("")) {
					fdao.setFieldValue("is_toAll", isAll);
					try {
						fdao.save();
					} catch (ErrMsgException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					}
				}
			}
		}
	}

	@Override
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb arg1)
			throws ErrMsgException {
		NoticeDb nDb = getNoticeDbByFlowId(wf);
		NoticeAttachmentDb naDb;
		long noticeId = 0;
		try {
			boolean res = nDb.createNoticeForFlow();
			if (res) {
				NoticeMgr nmr = new NoticeMgr();
				// NoticeDeptDb nddb = getNoticeDeptDb(nDb);
				nmr.creatNoticeReplayForFlow(nDb, wf.getId());
				// nddb.create();
			}
			noticeId = nDb.getId();
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = null;
			ResultRecord rd = null;
			String sql = "select * from flow_document_attach where doc_id = "
					+ getDocId(wf.getId());
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				naDb = new NoticeAttachmentDb();
				rd = (ResultRecord) ri.next();
				naDb.setNoticeId(noticeId);
				naDb.setName(rd.getString("name"));
				naDb.setDiskName(rd.getString("diskname"));
				naDb.setVisualPath(rd.getString("visualpath"));
				naDb.setSize(rd.getLong("file_size"));
				naDb.create();
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}

	}

	@Override
	public void setExtraData(String arg0) {
	}

	@Override
	public void setIsUsed(boolean arg0) {
	}

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu,
			int flowId, Vector fields) throws ErrMsgException {
		String beginDate = StrUtil.getNullStr(fu.getFieldValue("beginDate"));
		if (beginDate.equals("")) {
			throw new ErrMsgException("请选择开始日期");
		}
		String endDate = StrUtil.getNullStr(fu.getFieldValue("endDate"));
		if (endDate.equals("")) {
			throw new ErrMsgException("请选择结束日期");
		}
		if (DateUtil.parse(beginDate, "yyyy-MM-dd").after(
				DateUtil.parse(endDate, "yyyy-MM-dd"))) {
			throw new ErrMsgException("开始日期必须早于结束日期！");
		}
		return true;
	}

	public NoticeDb getNoticeDbByFlowId(WorkflowDb wf) {
		String userName = wf.getUserName();
		int flowId = wf.getId();
		NoticeDb ndb = null;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		String sql = "select * from ft_tzgg where flowId = " + flowId;

		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ndb = new NoticeDb();
				rd = (ResultRecord) ri.next();
				ndb.setTitle(rd.getString("title"));
				ndb.setContent(rd.getString("content"));
				ndb.setUserName(userName);
				ndb.setUnitCode(rd.getString("unit_code"));
				ndb.setCreateDate(new Date());
				String isShow = rd.getString("isShow");
				if (isShow != null && !"".equals(isShow))
					ndb.setIsShow(Integer.parseInt(isShow));
				else
					ndb.setIsShow(0);
				// ndb.setBeginDate(rd.getDate("beginDate"));\
				String date = StrUtil.getNullStr(rd.getString("beginDate"));
				ndb.setBeginDate(date.equals("") ? new Date() : DateUtil.parse(
						date, "yyyy-MM-dd"));
				// ndb.setEndDate(rd.getDate("endDate"));
				date = StrUtil.getNullStr(rd.getString("endDate"));
				ndb.setEndDate(date.equals("") ? DateUtil
						.addDate(new Date(), 3) : DateUtil.parse(date,
						"yyyy-MM-dd"));
				ndb.setColor(rd.getString("t_color"));
				// ndb.setBold(rd.getBoolean("isBold"));
				ndb.setLevel("".equals(rd.getString("level")) ? 0 : rd
						.getInt("level"));
				// ndb.setBold(rd.getInt("level")==0?false:true);
				String isAll = rd.getString("is_toAll");
				ndb.setIsall(StrUtil.toInt(isAll, 0));
				Privilege priv = new Privilege();
				if (priv.isUserPrivValid(userName, "notice.dept")) {
					ndb.setIsDeptNotice(1);
				} else if (priv.isUserPrivValid(userName, "notice")) {
					ndb.setIsDeptNotice(0);
				}

				ndb.setFlowId(flowId);
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return ndb;
	}

	public int getDocId(int flowId) {
		int docId = 0;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		String sql = "select doc_id from flow where id = " + flowId;

		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				docId = rd.getInt(1);
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return docId;
	}

	public NoticeDeptDb getNoticeDeptDb(NoticeDb noticeDb) {
		NoticeDeptDb ndb = new NoticeDeptDb();
		long noticeId = noticeDb.getId();
		String deptCode = noticeDb.getUnitCode();
		if (null == deptCode || "".equals(deptCode)) {
			deptCode = "root";
		}
		ndb.setNoticeId(noticeId);
		ndb.setDeptCode(deptCode);
		return ndb;
	}
}
