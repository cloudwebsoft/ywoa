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
		if (isPlatformSrc()) {
			return true;
		} else if (isPlatformGroup()) {
			return true;
		} else if (isPlatformSuper()) {
			return true;
		}
		return versionType.equals(VERSION_PLATFORM);
	}

	public boolean isPlatformSrc() {
		/*if (isTrial()) {
			return false;
		}*/
		if (isPlatformSuper()) {
			return true;
		} else if (isPlatformGroup()) {
			return true;
		}
		/*if (isSrc()) {
			return true;
		}*/
		return versionType.equals(VERSION_PLATFORM_SRC);
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
		SAXBuilder sb = new SAXBuilder();
		try {
			StringReader sr = new StringReader(licenseXMLString);
			Document doc = sb.build(sr);
			sr.close();

			root = doc.getRootElement();

			setId(root.getChild("id").getText());

			userCount = StrUtil.toInt(root.getChild("userCount").getText(), 20);
			expiresDate = DateUtil.parse(
					root.getChild("expiresDate").getText(), "yyyy-MM-dd");
			createDate = DateUtil.parse(
					root.getChild("creationDate").getText(), "yyyy-MM-dd");
			company = root.getChild("company").getText();
			type = root.getChild("type").getText();
			domain = root.getChild("domain").getText();

			setName(root.getChild("name").getText());

			// 2.2版后增加该项
			version = root.getChild("version").getText();
			versionType = root.getChild("versionType").getText();
			// 4.0后增加该项
			if (root.getChild("key") != null) {
				key = root.getChild("key").getText();
			}

			// 20120811增加该项
			if (root.getChild("officeControlKey") != null) {
				officeControlKey = root.getChild("officeControlKey").getText();
			}

			// 20130914增加该项
			modules = new HashMap<String, Boolean>();
			if (root.getChild("modules") != null) {
				String[] ary = StrUtil.split(
						root.getChild("modules").getText(), ",");
				if (ary != null) {
					for (int i = 0; i < ary.length; i++) {
						modules.put(ary[i], true);
					}
				}
			}

			// 20140214添加 fgf，政府版、企业版、综合版
			if (root.getChild("kind") != null) {
				kind = root.getChild("kind").getText();
			}

			// 20140602添加 fgf
			if (root.getChild("isCloudDisk") != null) {
				cloudDisk = root.getChild("isCloudDisk").getText();
			}

			if (root.getChild("enterpriseNum") != null) {
				setEnterpriseNum(root.getChild("enterpriseNum").getText());
			}

			// 20160120 add by 古月圣
			// description: 用于解决方案
			solutions = new HashMap<String, Boolean>();
			if (root.getChild("solutions") != null) {
				isSolutionVer = true;
				String[] ary = StrUtil.split(
						root.getChild("solutions").getText(), ",");
				if (ary != null) {
					for (int i = 0; i < ary.length; i++) {
						solutions.put(ary[i], true);
					}
				}
			}

			if (root.getChild("actionCount") != null) {
				setActionCount(StrUtil.toInt(root.getChild("actionCount").getText()));
			}

			if (root.getChild("flowDesigner") != null) {
				flowDesigner = root.getChild("flowDesigner").getText();
			}
			else {
				flowDesigner = "";
			}

			if (root.getChild("isTrial") != null) {
				trial = root.getChildText("isTrial").equals("true");
			}

			// 20220731
			if (root.getChild("category")!=null) {
				category = root.getChildText("category");
			}
		} catch (JDOMException | IOException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

	/**
	 * 根据公钥对license.dat进行验证并重建XML Document
	 * 
	 * @return boolean
	 */
	public boolean verify() {
		try {
			Resource resource = new ClassPathResource("publickey.dat");
			InputStream inputStream = resource.getInputStream();

			java.io.ObjectInputStream in = new java.io.ObjectInputStream(inputStream);
			PublicKey pubkey = (PublicKey) in.readObject();
			in.close();

			ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
			inputStream = configUtil.getFile("license.dat");
			if (inputStream != null) {
				in = new java.io.ObjectInputStream(inputStream);
				// 取得license.xml
				String info = (String) in.readObject();
				// 取得签名
				byte[] signed = (byte[]) in.readObject();
				in.close();

				java.security.Signature signetcheck = java.security.Signature.getInstance("DSA");
				signetcheck.initVerify(pubkey);
				signetcheck.update(info.getBytes(StandardCharsets.UTF_8));
				if (signetcheck.verify(signed)) {
					toXML(info);
					valid = true;
				} else {
					valid = false;
					LogUtil.getLog(getClass()).error("Cloud Web license is invalid. 请联系官方获取技术支持！");
				}
			}
		} catch (java.lang.Exception e) {
			licenseStr = "（未找到许可证文件，您的配置可能不正确，请参考安装说明运行 "
					+ Global.getFullRootPath() + "/setup/index.jsp）";
			LogUtil.getLog(getClass()).error(e);
		}
		return valid;
	}

	public void validate(HttpServletRequest request) throws ErrMsgException {
		validate();

		String serverName = request.getServerName().toLowerCase();

		if ("localhost".equals(serverName) || "127.0.0.1".equals(serverName)) {
			return;
		}
		if (domain == null || "".equals(domain)) {
			throw new ErrMsgException("许可证域名:" + domain + " 非法!");
		}
		if ("*".equals(domain)) {
			return;
		}
		String[] ary = StrUtil.split(domain.toLowerCase(), ",");
		int len = ary.length;
		for (int i = 0; i < len; i++) {
			if (serverName.indexOf(ary[i]) != -1) {
				return;
			}
		}
		throw new ErrMsgException("许可证中的域名非法!" + licenseStr);
	}

	public void validate() throws ErrMsgException {
		if (!valid) {
			userCount = TEST_VERSTION_USER_COUNT;
			// throw new ErrMsgException("许可证非法!" + licenseStr);
		}

		// 检查时间
		if (DateUtil.compare(new java.util.Date(), expiresDate) == 1) {
			throw new ErrMsgException("系统已到期!" + licenseStr);
		}

		// 检查用户数
		IUserService userService = SpringUtil.getBean(IUserService.class);
		int uCount = userService.getValidUserCount();

		if (uCount > userCount) {
			if (!"".equals(licenseStr)) {
				throw new ErrMsgException("许可证限定用户数不能大于" + userCount + "，" + licenseStr);
			}
			else {
				throw new ErrMsgException("许可证限定用户数不能大于" + userCount);
			}
		}

		// 检查是否含有isCloudDisk
		if (cloudDisk == null) {
			throw new ErrMsgException("许可证中网盘参数设置非法!" + licenseStr);
		}
		
		// 不允许使用老版许可证
		double intVer = StrUtil.toDouble(version, 1.0);
		if (intVer < 3) {
			throw new ErrMsgException("非法使用许可证！");
		}
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
		return modules.containsKey(moduleCode);
	}
	
	/**
	 * 能否使用解决方案
	 * 目前仅用于岗位管理
	 * @param solutionCode  解决方案编码
	 * @return
	 */
	public boolean canUseSolution(String solutionCode) {
		if (isPlatformSuper()) {
			return true;
		}
		boolean re = false;
		re = solutions.containsKey(solutionCode);
		if (!re) {
			if (solutionCode.equals(SOLUTION_PERFORMANCE)) {
				licenseStr = "使用许可中不包含绩效考核解决方案！";
			}
			else if (solutionCode.equals(SOLUTION_SCORE)) {
				licenseStr = "使用许可中不包含品牌积分制解决方案！";
			}
			else if (solutionCode.equals(SOLUTION_BUDGET)) {
				licenseStr = "使用许可中不包含预算解决方案！";
			}		
			else if (solutionCode.equals(SOLUTION_TASK)) {
				licenseStr = "使用许可中不包含任务解决方案！";
			}
			else if (solutionCode.equals(SOLUTION_HR)) {
				licenseStr = "使用许可中不包含HR解决方案！";
			}
			else if (solutionCode.equals(SOLUTION_CRM)) {
				licenseStr = "使用许可中不包含CRM解决方案！";
			}
			else {
				licenseStr = "使用许可中不包含" + solutionCode + "解决方案！";
			}
		}
		return re;
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
		// 如果许可证的version为1.0，则为src型，以导出解决方案，及编辑事件脚本
		boolean re = false;
		if ("1.0".equals(version) || "2.0".equals(version)) {
			if (isOem() || versionType.equals(VERSION_PLATFORM_SRC) || versionType.equals(VERSION_PLATFORM_SUPER)) {
				re = true;
			}
		}
		else if ("3.0".equals(version)) {
			if (type.equals(TYPE_SRC)) {
				re = true;
			}
		}		
		else if ("4.0".equals(version)) {
			if (versionType.equals(VERSION_PLATFORM_SUPER)) {
				re = true;
			} else if (versionType.equals(VERSION_PLATFORM_SRC)) {
				re = true;
			} else if (versionType.equals(VERSION_PLATFORM_GROUP)) {
				re = true;
			} else if (versionType.equals(VERSION_PLATFORM)) {
				re = true;
			}
		}

		/*if (!re) {
			licenseStr = "只有src版许可证才能导出解决方案！";
		}*/
		return re;
	}

	public boolean canExportSolution() {
		boolean re =  isPlatformSrc();
		if (!re) {
			licenseStr = "只有src版许可证才能导出解决方案！";
		}
		return re;
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
    	// 如果是铂金版，则含有所有的解决方案
    	if (isPlatformSuper()) {
			return true;
		}
    	
    	// 积分配置表
    	if ("pointsys".equals(formCode)) {
    		if (!canUseSolution(com.redmoon.oa.kernel.License.SOLUTION_SCORE)) {
    			throw new ErrMsgException(getLicenseStr());
    		}
    	}
    	else if ("score_reported".equals(formCode)) {
    		if (!canUseSolution(com.redmoon.oa.kernel.License.SOLUTION_SCORE)) {
    			throw new ErrMsgException(getLicenseStr());
    		}
    	}
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
