package com.redmoon.oa.job;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.util.DateUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.licenceValidate.*;
import com.redmoon.oa.licenceValidate.diskno.DiskNo;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.Config;

/**
 * @Description: 证书验证调度类，短信开通类
 * @author: lichao
 * @Date: 2015-8-28下午03:05:58
 */
public class LicenseManagerJob implements Job {
	public LicenseManagerJob() {
	}

	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		executeJob();
	}

	// 独立出来，可直接调用执行
	public void executeJob() {
		String enterpriseNum = License.getInstance().getEnterpriseNum(); // 企业号
		if (enterpriseNum == null || enterpriseNum.equals("")
				|| enterpriseNum.equals("yimi") || enterpriseNum.equals("OA")
				|| enterpriseNum.equals("ywrj")
				|| enterpriseNum.equals("yimihome")) {
			// System.out.println("检测时间：" + time +"  ,企业号"+enterpriseNum+
			// "为试用版");
			return;
		}
		System.out.println("enterpriseNum:" + enterpriseNum);
		// System.out.println("检测时间：" + time +"  ,企业号"+enterpriseNum+ "为正式版");

		int userCount = getSysUserCount();
		JSONObject jobject = new JSONObject();
		try {
			jobject.put("enterpriseNum", enterpriseNum);
			jobject.put("userCount", userCount);
		} catch (JSONException e2) {
			e2.printStackTrace();
		}

		jobject = DiskNo.GetDiskNoOrMotherboardNo(jobject);

		Config cg = new Config();
		String yimihomeURL = cg.get("yimihome_url");

		// 检查企业号对应硬盘号是否超过三个
		try {
			JSONObject json = HttpClientLoginValidate
					.HttpClientLM(
							yimihomeURL
									+ "/httpClientServer/httpclient_server_license_manager.jsp",
							jobject);

			if (json != null) {
				int ret = json.getInt("ret");
				int expire = json.getInt("expire");

				if (ret == 1) {
					// 20170416 fgf 下列代码似乎无用，因为服务器端根本未返回expire、code、mobile_user_name等
					String endTime = DateUtil.format(DateUtil.add(new Date(),
							expire), "yyyy-MM-dd")
							+ " 23:59:59";
					String code = json.getString("code"); // 开通的短信网关名称
					String userName = json.getString("mobile_user_name"); // 短信网关用户名
					String password = json.getString("mobile_password"); // 短信网关密码
					MessageDb mes = new MessageDb();

					Calendar ca = Calendar.getInstance();
					int hour = ca.get(Calendar.HOUR_OF_DAY);
					if (hour >= 20 && hour < 24) { // 20-24点发送消息
						if (expire == 30) {
							mes.sendSysMsg("admin", "到期提醒",
									"尊敬的客户您好,您所订购的服务套餐将于" + endTime
											+ "到期，给您带来不便敬请谅解，如有疑问详询客服。");
						}
						if (expire > 0 && expire <= 7) {
							mes.sendSysMsg("admin", "到期提醒",
									"尊敬的客户您好,您所订购的服务套餐将于" + endTime
											+ "到期，给您带来不便敬请谅解，如有疑问详询客服。");
						}
					}
					if (!"".equals(code) && code != null) { // 开通对应的短信网关
						com.redmoon.oa.sms.Config smsCfg = new com.redmoon.oa.sms.Config();
						Element root = smsCfg.getRootElement();
						Iterator ir = root.getChildren().iterator();
						while (ir.hasNext()) {
							Element e = (Element) ir.next();
							String c = e.getAttributeValue("code");
							String flag = e.getAttributeValue("isUsed");
							if (c.equals(code) && flag.equals("false")) {
								e.setAttribute("isUsed", "true");
								Element userNameElement = e
										.getChild("user_name");
								if (userNameElement == null) {
									userNameElement = new Element("user_name");
									userNameElement.setText(userName);
								} else {
									userNameElement.setText(userName);
								}
								Element passwordElement = e
										.getChild("password");
								if (passwordElement == null) {
									passwordElement = new Element("password");
									passwordElement.setText(password);
								} else {
									passwordElement.setText(password);
								}
								smsCfg.writemodify();
								break;
							}
						}
					}
				} else {
					LicenseDownload ld = new LicenseDownload();
					ld.download(yimihomeURL
							+ "/httpClientServer/license_download_server.jsp",
							enterpriseNum, "trialVersion");
					try {
						RestartTomcat.restartTomcatServer();// 重启tomcat
					} catch (NullPointerException e) {
						LogUtil.getLog(LicenseManagerJob.class).error(
								e.getMessage());
						e.printStackTrace();
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(LicenseManagerJob.class).error(e.getMessage());
		} catch (Exception e) {
			LogUtil.getLog(LicenseManagerJob.class).error(e.getMessage());
		}
	}

	/**
	 * 
	 * @Description: 获取系统有效的用户数量(isValid=1)
	 * @return 用户数量
	 */
	private int getSysUserCount() {
		UserDb udb = new UserDb();
		udb.setQueryList();
		Vector vec = udb.list(udb.QUERY_LIST);
		return vec.size();
	}
}