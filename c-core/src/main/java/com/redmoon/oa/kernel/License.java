package com.redmoon.oa.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.person.UserDb;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class License {
	static License license = null;
	Element root;

	private static Object initLock = new Object();

	public static final String VERSION_STANDARD = "Standard";

	/**
	 * 曾经作为标准版，改为企业版2023/2/14
	 */
	public static final String VERSION_PLATFORM = "Platform";

	/**
	 * 平台版
	 */
	public static final String VERSION_PLATFORM_SRC = "Platform_src";

	/**
	 * 暂无用，原开发版，将来可作为旗舰版
	 */
	public static final String VERSION_PLATFORM_SUPER = "Platform_super";

	/**
	 * 集团版（同时也是开发版）
	 */
	public static final String VERSION_PLATFORM_GROUP= "Platform_group";

	/**
	 * 未注册时显示为试用版
	 */
	public static final String TYPE_TRIAL = "trial";

	/**
	 * 注册后的版本
	 */
	public static final String TYPE_COMMERICAL = "commercial";

	/**
	 * 付费版，去掉logo、水印等，暂未投入使用
	 */
	public static final String TYPE_BIZ = "biz";

	/**
	 * VIP用户
	 */
	public static final String TYPE_VIP = "vip";

	/**
	 * 源码级，4.0以后已弃用
	 */
	@Deprecated
	public static final String TYPE_SRC = "src";

	/**
	 * OEM级 jfy 20150702
	 */
	public static final String TYPE_OEM = "oem";

	/**
	 * 仅ActiveX版流程设计器
	 */
	public static final String FLOW_DESIGNER_A = "0";
	/**
	 * ActiveX + MyFlow版流程设计器
	 */
	public static final String FLOW_DESIGNER_X = "1";

	String licenseStr = "";

	public String getLicenseStr() {
		return licenseStr;
	}

	public void setLicenseStr(String licenseStr) {
		this.licenseStr = licenseStr;
	}

	boolean valid = false;

	/**
	 * 单位注册码
	 */
	private String key;

	/**
	 * 节点事件脚本
	 */
	public static final String MODULE_ACTION_EVENT_SCRIPT = "actionEventScript";
	/**
	 * 调度脚本
	 */
	public static final String MODULE_SCHEDULE_SCRIPT = "scheduleScript";

	/**
	 * 组合型，含政府及企业
	 */
	public static final String KIND_COMB = "COMB"; // Combination

	/**
	 * 企业版
	 */
	public static final String KIND_COM = "COM";
	/**
	 * 政府版
	 */
	public static final String KIND_GOV = "GOV";

	private Map<String, Boolean> modules;
	/**
	 * 解决方案
	 */
	private Map<String, Boolean> solutions;

	/**
	 * hr解决方案
	 */
	public static final String SOLUTION_HR = "hrSolution";
	/**
	 * crm解决方案
	 */
	public static final String SOLUTION_CRM = "crmSolution";
	/**
	 * 进销存解决方案
	 */
	public static final String SOLUTION_STOCKSALES = "stockSalesSolution";
	/**
	 * 任务解决方案
	 */
	public static final String SOLUTION_TASK = "taskSolution";

	/**
	 * 预算解决方案
	 */
	public static final String SOLUTION_BUDGET = "budgetSolution";

	/**
	 * 绩效考核解决方案
	 */
	public static final String SOLUTION_PERFORMANCE = "performanceSolution";

	/**
	 * 积分制解决方案
	 */
	public static final String SOLUTION_SCORE = "scoreSolution";

	private String id;

	public License() {
	}

	public boolean isStandard() {
		return versionType.equals(VERSION_STANDARD);
	}

	public boolean isPlatform() {
		return true;
	}

	public boolean isPlatformSrc() {
		return true;
	}

	public boolean isPlatformSuper() {
		/*if (isSrc()) {
			return true;
		}*/

		return versionType.equals(VERSION_PLATFORM_SUPER);
	}

	public boolean isCloud() {
		return category.equals(ConstUtil.CATEGORY_CLOUD);
	}

	public boolean isPlatformGroup() {
		return versionType.equals(VERSION_PLATFORM_GROUP);
	}

	public boolean isGov() {
		return kind.equals(KIND_GOV);
	}

	public boolean isCom() {
		return kind.equals(KIND_COM);
	}

	public boolean isComb() {
		return kind.equals(KIND_COMB);
	}

	public boolean isValid() {
		return valid;
	}

	public Date getExpiresDate() {
		return expiresDate;
	}

	public String getCompany() {
		return company;
	}

	public String getType() {
		return type;
	}

	public int getUserCount() {
		return userCount;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getDomain() {
		return domain;
	}

	public String getVersion() {
		return version;
	}

	public String getVersionType() {
		return versionType;
	}

	public String getKey() {
		return key;
	}

	public String getOfficeControlKey() {
		return officeControlKey;
	}

	public static License getInstance() {
		if (license == null) {
			synchronized (initLock) {
				license = new License();
				license.init();
			}
		}
		return license;
	}

	public void init() {
		verify();
	}

	public void toXML(String licenseXMLString) {

	}

	/**
	 * 根据公钥对license.dat进行验证并重建XML Document
	 *
	 * @return boolean
	 */
	public boolean verify() {
		return true;
	}

	public void validate(HttpServletRequest request) throws ErrMsgException {

	}

	public void validate() throws ErrMsgException {
	}

	public void setExpiresDate(Date expiresDate) {
		this.expiresDate = expiresDate;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setOfficeControlKey(String officeControlKey) {
		this.officeControlKey = officeControlKey;
	}

	/**
	 * 能否使用模块
	 * @param moduleCode 模块编码
	 * @return
	 */
	public boolean canUseModule(String moduleCode) {
		return true;
	}

	/**
	 * 能否使用解决方案
	 * 目前仅用于岗位管理
	 * @param solutionCode  解决方案编码
	 * @return
	 */
	public boolean canUseSolution(String solutionCode) {
		return true;
	}

	public String getKind() {
		return kind;
	}

	public void setCloudDisk(String cloudDisk) {
		this.cloudDisk = cloudDisk;
	}

	public boolean isCloudDisk() {
		if (cloudDisk == null) {
			return false;
		}
		return cloudDisk.equals("true");
	}

	public void setEnterpriseNum(String enterpriseNum) {
		this.enterpriseNum = enterpriseNum;
	}

	public String getEnterpriseNum() {
		return enterpriseNum;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isTrial() {
		return trial;
	}

	/**
	 * 判断是否为付费版，去掉水印、logo
	 *
	 * @return
	 */
	public boolean isBiz() {
		return type.equals(TYPE_BIZ);
	}

	public boolean isOem() {
		return type.equals(TYPE_OEM);
	}

	/**
	 * 判断是否为开发版
	 * @return
	 */
	public boolean isSrc() {
		return true;
	}

	public boolean canExportSolution() {
		return true;
	}

	public boolean canImportSolution() {
		boolean re =  isPlatformSrc();
		if (!re) {
			licenseStr = "只有src版许可证才能导出解决方案！";
		}
		return re;
	}

	public boolean isFree() {
		return type.equals(TYPE_COMMERICAL);
	}

	/**
	 * 判断是否为VIP用户
	 *
	 * @return
	 */
	public boolean isVip() {
		return type.equals(TYPE_VIP);
	}

	public static String getFormWatermark() {
		License lic = License.getInstance();
		return "<div class='percent80' style='text-align:right;margin-top:10px'><a href='http://www.cloudwebsoft.com' style='color:#cccccc' target='_blank'>-&nbsp;云网OA" + (lic.isFree() ? "免费版" : "") + "&nbsp;-</a></div>";
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean isSolutionVer() {
		return isSolutionVer;
	}

	public void setSolutionVer(boolean isSolutionVer) {
		this.isSolutionVer = isSolutionVer;
	}

	/**
	 * 检查许可证中对于解决方案是否控制
	 * 在flow.Render及visual.Render中使用
	 * @param formCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean checkSolution(String formCode) throws ErrMsgException {
		return true;
	}

	public static final int TEST_VERSTION_USER_COUNT = 15;

	private Date expiresDate;
	private String company = "无";
	private String type = TYPE_TRIAL;
	private int userCount = TEST_VERSTION_USER_COUNT;
	private Date createDate;
	private String domain = "*";
	private String version = "";
	private String officeControlKey = "";
	private String versionType = "";

	private String kind = "";

	private String cloudDisk;

	private String enterpriseNum;

	private String name;

	private boolean isSolutionVer = false;

	private int actionCount = 10000;

	private boolean trial = false;

	public int getActionCount() {
		return actionCount;
	}

	public void setActionCount(int actionCount) {
		this.actionCount = actionCount;
	}

	private String flowDesigner = "";

	public String getFlowDesigner() {
		return flowDesigner;
	}

	public void setFlowDesigner(String flowDesigner) {
		this.flowDesigner = flowDesigner;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	private String category = ConstUtil.CATEGORY_CLOUD;
}
