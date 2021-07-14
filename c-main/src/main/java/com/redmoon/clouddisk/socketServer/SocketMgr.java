package com.redmoon.clouddisk.socketServer;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.*;
import com.redmoon.clouddisk.db.*;
import com.redmoon.clouddisk.tools.ByteConvertUtil;
import com.redmoon.clouddisk.tools.ToolsUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;

/**
 * @author 古月圣
 * 
 */
public class SocketMgr {

	private NetDiskBean netDiskBean;
	private AttachmentBean attachmentBean;
	private DirectoryBean directoryBean;
	private DocumentBean documentBean;
	private UserBean userBean;
	private KeyBean keyBean;
	private VersionBean versionBean;
	private CooperateBean cooperateBean;
	private SideBean sideBean;

	private String filePath = null;

	public final static String MYDISK = "我的网盘";
	private final static byte FILE_DOCUMENT = 0x00;

	public SocketMgr() {
		Config cfg = new Config();
		filePath = CloudDiskThread.OA_REALPATH + cfg.get("file_netdisk")
				+ File.separator;
		netDiskBean = new NetDiskBean();
		attachmentBean = new AttachmentBean();
		directoryBean = new DirectoryBean();
		documentBean = new DocumentBean();
		userBean = new UserBean();
		keyBean = new KeyBean();
		versionBean = new VersionBean();
		cooperateBean = new CooperateBean();
		sideBean = new SideBean();
	}

	public NetDiskBean getNetDiskBean() {
		return netDiskBean;
	}

	public void setNetDiskBean(NetDiskBean netDiskBean) {
		this.netDiskBean = netDiskBean;
	}

	public AttachmentBean getAttachmentBean() {
		return attachmentBean;
	}

	public void setAttachmentBean(AttachmentBean attachmentBean) {
		this.attachmentBean = attachmentBean;
	}

	public DirectoryBean getDirectoryBean() {
		return directoryBean;
	}

	public void setDirectoryBean(DirectoryBean directoryBean) {
		this.directoryBean = directoryBean;
	}

	public DocumentBean getDocumentBean() {
		return documentBean;
	}

	public void setDocumentBean(DocumentBean documentBean) {
		this.documentBean = documentBean;
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public KeyBean getKeyBean() {
		return keyBean;
	}

	public void setKeyBean(KeyBean keyBean) {
		this.keyBean = keyBean;
	}

	public VersionBean getVersionBean() {
		return versionBean;
	}

	public void setVersionBean(VersionBean versionBean) {
		this.versionBean = versionBean;
	}

	public CooperateBean getCooperateBean() {
		return cooperateBean;
	}

	public void setCooperateBean(CooperateBean cooperateBean) {
		this.cooperateBean = cooperateBean;
	}

	public SideBean getSideBean() {
		return sideBean;
	}

	public void setSideBean(SideBean sideBean) {
		this.sideBean = sideBean;
	}

	/**
	 * @Description: 获取应用的路径
	 * @param inputStream
	 * @return
	 */
	public boolean getURL(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 验证用户是否合法
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean checkUser(DataInputStream inputStream) {
		try {
			byte[] buf;
			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			byte userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int pwdLengh = buf[0];

			buf = new byte[pwdLengh];
			inputStream.readFully(buf, 0, pwdLengh);
			String pwd = ByteConvertUtil.ByteArraytoString(buf);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			userBean.setName(userName);
			UserDb userDb = new UserDb(userBean);
			boolean flag = userDb.getUserPwd();

			if (!flag) {
				return false;
			}

			if (userBean.getPwd().toUpperCase().equals(
					SecurityUtil.MD5(pwd).toUpperCase())) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 取得用户所在的部门
	 * 
	 * @param inputStream
	 * @return
	 */
	public byte[] getUserDept(DataInputStream inputStream) {
		try {
			byte[] buf;
			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			byte userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return null;
			}

			netDiskBean.setUserName(userName);
			String[] path = parseFileName(fileName);

			userBean.setName(userName);
			UserDb userDb = new UserDb(userBean);
			String res = "";
			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			boolean isShowAllDepts = cfg.getBooleanProperty("show_all_depts");
			if (isShowAllDepts) {
				res = userDb.getAllDepts(path[0] + "/" + path[1]);
			} else {
				res = userDb.getUserDept(path[0] + "/" + path[1]);
			}

			if (res != null) {
				return res.getBytes();
			} else {
				return null;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return null;
		}
	}

	/**
	 * 登录，同时取部门
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean userLogin(DataInputStream inputStream) {
		try {
			byte[] buf;
			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			byte userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int pwdLengh = buf[0];

			buf = new byte[pwdLengh];
			inputStream.readFully(buf, 0, pwdLengh);
			String pwd = ByteConvertUtil.ByteArraytoString(buf);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			userBean.setName(userName);
			UserDb userDb = new UserDb(userBean);
			boolean flag = userDb.getUserPwd();

			if (!flag) {
				return false;
			}

			if (userBean.getPwd().toUpperCase().equals(
					SecurityUtil.MD5(pwd).toUpperCase())) {
				return userDb.getDeptName();
			} else {
				userBean.setFailedReason(UserDb.FAILED_PWDERROR);
				return false;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 获取验证密钥，用于超期处理
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean getKey(DataInputStream inputStream) {
		try {
			byte[] buf;
			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			byte userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			keyBean.setUserName(userName);

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 从config_sso中获取同步密钥
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean getSso(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 判断文件在服务器端的状态
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean checkStatus(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);

			// 客户端文件当前版本时间
			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			// 客户端本地数据库版本时间,即上一版本时间
			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setTempDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			// 客户端操作类型
			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			netDiskBean.setAction(ByteConvertUtil.ByteArraytoInt(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 判断协作文件在服务器端的状态
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean checkShareStatus(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);
			cooperateBean.setShareUser(userName);

			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int shareLength = buf[0];

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);
			String shareUser = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(shareUser);
			cooperateBean.setShareUser(shareUser);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			cooperateBean.setVisualPath(path[0]);

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setTempDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			// 客户端操作类型
			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			netDiskBean.setAction(ByteConvertUtil.ByteArraytoInt(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 解析客户端传过来的文件名
	 * 
	 * @param fileName
	 * @return 数组第一位放路径，第二位放文件名
	 */
	public String[] parseFileName(String fileName) {
		int index = fileName.lastIndexOf('\\');
		String extPath;

		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		boolean isCloudDiskRoot = cfg.getBooleanProperty("isCloudDiskRoot");

		if (index == -1) {
			if (isCloudDiskRoot) {
				extPath = netDiskBean.getUserName();
			} else {
				extPath = netDiskBean.getUserName() + "/" + MYDISK;
			}
		} else {
			String str = fileName.substring(0, index);
			str = str.replaceAll("\\\\", "/");

			if (isCloudDiskRoot) {
				extPath = netDiskBean.getUserName() + "/" + str;
			} else {
				extPath = netDiskBean.getUserName() + "/" + MYDISK + "/" + str;
			}
		}

		// 文件名
		String name = fileName.substring(index + 1);

		String[] path = new String[2];
		path[0] = extPath;
		path[1] = name;

		return path;
	}

	/**
	 * 客户端从服务器上获取需要更新文件的列表
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileUpdate(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			String fullPath = filePath + path[0] + "/" + path[1];

			File file = new File(fullPath);
			if (!file.exists()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 客户端提交
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileCommit(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);
			attachmentBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			// String parentCode = userName;
			String parentCode = "-1";
			long id = 0;
			String[] dirs = path[0].split("/", 0);
			for (int i = 0; i < dirs.length; i++) {
				documentBean.setTitle(dirs[i]);
				directoryBean.setName(dirs[i]);
				documentBean.setNick(userName);
				directoryBean.setName(dirs[i]);
				documentBean.setParentCode(parentCode);
				directoryBean.setParentCode(parentCode);
				directoryBean.setRootCode(userName);
				directoryBean.setLayer(i + 1);
				directoryBean.setOrders(1);
				directoryBean.setChildCount(0);

				DirectoryDb directoryDb = new DirectoryDb(directoryBean);
				DocumentDb documentDb = new DocumentDb(documentBean);

				boolean flag = directoryDb.isExist();
				String code = "";
				if (flag) {
					com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
							.getInstance();
					boolean isRoot = cfg.getBooleanProperty("isCloudDiskRoot");
					documentBean.setTitle(isRoot ? "我的网盘" : "我的文档");
					directoryBean.setName(isRoot ? "我的网盘" : "我的文档");
					directoryBean.setDescription("根结点");
					code = directoryBean.getCode();
				} else {
					code = RandomSecquenceCreator.getId(20);
				}
				documentBean.setClass1(code);
				directoryBean.setCode(code);

				if (!documentDb.isExist()) {
					// documentDb.getCurId();
					// id = documentBean.getId();
					id = SequenceManager
							.nextID(SequenceManager.OA_DOCUMENT_NETDISK);
					documentBean.setId(id);
					documentDb = new DocumentDb(documentBean);
					documentDb.create();
				}
				directoryBean.setDocId(documentBean.getId());
				directoryDb = new DirectoryDb(directoryBean);
				if (!flag) {
					directoryDb.getOrders();
					directoryDb.create();
				} else {
					directoryDb.update();
				}
				parentCode = code;
			}

			if (dirs.length == 2) {
				documentBean.setClass1(parentCode);
				DocumentDb documentDb = new DocumentDb(documentBean);
				documentDb.getRootId();
			}
			attachmentBean.setDocId(documentBean.getId());
			netDiskBean.setDocId(documentBean.getId());

			String name = path[1];
			netDiskBean.setName(name);
			attachmentBean.setName(name);
			attachmentBean.setDiskName(name);
			netDiskBean.setVisualPath(path[0]);
			attachmentBean.setVisualPath(path[0]);
			attachmentBean.setFullPath(filePath + path[0] + File.separator
					+ name);
			attachmentBean.setExt(StrUtil.getFileExt(name));

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			String diskName = netDiskBean.getName() + "_"
					+ DateUtil.format(new Date(), "yyyyMMddHHmmss");
			netDiskBean.setDiskName(diskName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 分享
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileShare(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			cooperateBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));
			netDiskBean.setUserName(cooperateBean.getUserName());

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			// netDiskBean.setName(path[1]);
			cooperateBean.setVisualPath(path[0] + "/" + path[1]);

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			// netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
			// .ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int editLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[editLength];
			inputStream.readFully(buf, 0, editLength);
			// netDiskBean.setIsEdit(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int shareLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);
			cooperateBean.setShareUser(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			String fullPath = filePath + cooperateBean.getVisualPath();

			File file = new File(fullPath);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					return false;
				}
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 版本
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileHistory(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 文件下载，用于历史版本的下载
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileDownload(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			byte isDoc = buf[0];

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);

			if (isDoc == FILE_DOCUMENT) {
				buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
				inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
				netDiskBean.setIsCurrent(StrUtil.toInt(ByteConvertUtil
						.ByteArraytoString(buf)));

				if (netDiskBean.getIsCurrent() == 0) {
					buf = new byte[ServerWorker.TIME_LENGTH];
					inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
					netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
							.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));
				}
			} else {
				netDiskBean.setIsDoc(0);
			}

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 历史版本的回退
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileRestore(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);

			buf = new byte[ServerWorker.TIME_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.TIME_LENGTH);
			netDiskBean.setVersionDate(DateUtil.parse(ByteConvertUtil
					.ByteArraytoString(buf), "yyyy-MM-dd HH:mm:ss"));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			String fullPath = filePath + File.separator + path[0]
					+ File.separator + path[1];

			File file = new File(fullPath);
			if (!file.exists()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 被分享的文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileShared(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			cooperateBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileDelete(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);
			attachmentBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 删除夹文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean dirDelete(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);
			attachmentBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 协作删除文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean shareFileDelete(DataInputStream inputStream) {
		try {
			byte[] buf;
			CooperateLogBean cologBean = new CooperateLogBean();

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			attachmentBean.setUserName(userName);
			cologBean.setUserName(userName);

			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int shareLength = buf[0];

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);

			String shareUser = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(shareUser);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			cooperateBean.setVisualPath(path[0]);

			netDiskBean.setDeleteDate(new Date());
			String diskName = netDiskBean.getName()
					+ "_"
					+ DateUtil.format(netDiskBean.getDeleteDate(),
							"yyyyMMddHHmmss");
			netDiskBean.setDiskName(diskName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			CooperateDb cooperateDb = new CooperateDb(cooperateBean);
			// 获取dir_code
			boolean re = cooperateDb.getDirCodeByPath();
			if (re) {
				// 协作日志
				cologBean.setAction(CooperateLogBean.ACTION_DELETE);
				cologBean.setActionDate(netDiskBean.getDeleteDate());
				cologBean.setActionName(netDiskBean.getName());
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(userName);
				CooperateLogDb cooperateLogDb = new CooperateLogDb(cologBean);
				re = cooperateLogDb.create();
			}
			return re;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 协作删除夹文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean shareDirDelete(DataInputStream inputStream) {
		try {
			byte[] buf;
			CooperateLogBean cologBean = new CooperateLogBean();

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			attachmentBean.setUserName(userName);
			cologBean.setUserName(userName);

			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int shareLength = buf[0];

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);

			String shareUser = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(shareUser);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			cooperateBean.setVisualPath(path[0]);

			netDiskBean.setDeleteDate(new Date());
			String diskName = netDiskBean.getName()
					+ "_"
					+ DateUtil.format(netDiskBean.getDeleteDate(),
							"yyyyMMddHHmmss");
			netDiskBean.setDiskName(diskName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			CooperateDb cooperateDb = new CooperateDb(cooperateBean);
			// 获取dir_code
			boolean re = cooperateDb.getDirCodeByPath();
			if (re) {
				// 协作日志
				cologBean.setAction(CooperateLogBean.ACTION_DELETE);
				cologBean.setActionDate(netDiskBean.getDeleteDate());
				cologBean.setActionName(netDiskBean.getName());
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(userName);
				CooperateLogDb cooperateLogDb = new CooperateLogDb(cologBean);
				re = cooperateLogDb.create();
			}
			return re;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 重命名文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileRename(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);
			attachmentBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int newNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[newNameLength];
			inputStream.readFully(buf, 0, newNameLength);
			String newName = ByteConvertUtil.ByteArraytoString(buf);

			path = parseFileName(newName);

			netDiskBean.setDiskName(path[1]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 重命名文件夹
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean dirRename(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);
			attachmentBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int newNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[newNameLength];
			inputStream.readFully(buf, 0, newNameLength);
			String newName = ByteConvertUtil.ByteArraytoString(buf);

			path = parseFileName(newName);

			netDiskBean.setDiskName(path[1]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 协作重命名文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean shareFileRename(DataInputStream inputStream) {
		try {
			byte[] buf;
			CooperateLogBean cologBean = new CooperateLogBean();

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			attachmentBean.setUserName(userName);
			cologBean.setUserName(userName);

			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int shareLength = buf[0];

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);

			String shareUser = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(shareUser);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			cooperateBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int newNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[newNameLength];
			inputStream.readFully(buf, 0, newNameLength);
			String newName = ByteConvertUtil.ByteArraytoString(buf);

			path = parseFileName(newName);

			netDiskBean.setDiskName(path[1]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			CooperateDb cooperateDb = new CooperateDb(cooperateBean);
			// 获取dir_code
			boolean re = cooperateDb.getDirCodeByPath();
			if (re) {
				// 协作日志
				cologBean.setAction(CooperateLogBean.ACTION_DELETE);
				cologBean.setActionDate(netDiskBean.getDeleteDate());
				cologBean.setActionName(netDiskBean.getName());
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(userName);
				CooperateLogDb cooperateLogDb = new CooperateLogDb(cologBean);
				re = cooperateLogDb.create();
			}
			return re;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 协作重命名夹文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean shareDirRename(DataInputStream inputStream) {
		try {
			byte[] buf;
			CooperateLogBean cologBean = new CooperateLogBean();

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			attachmentBean.setUserName(userName);
			cologBean.setUserName(userName);

			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int shareLength = buf[0];

			buf = new byte[shareLength];
			inputStream.readFully(buf, 0, shareLength);

			String shareUser = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(shareUser);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(fileName);

			netDiskBean.setName(path[1]);
			netDiskBean.setVisualPath(path[0]);
			cooperateBean.setVisualPath(path[0]);
			netDiskBean.setDeleteDate(new Date());

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int newNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[newNameLength];
			inputStream.readFully(buf, 0, newNameLength);
			String newName = ByteConvertUtil.ByteArraytoString(buf);

			path = parseFileName(newName);

			netDiskBean.setDiskName(path[1]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			CooperateDb cooperateDb = new CooperateDb(cooperateBean);
			// 获取dir_code
			boolean re = cooperateDb.getDirCodeByPath();
			if (re) {
				// 协作日志
				cologBean.setAction(CooperateLogBean.ACTION_DELETE);
				cologBean.setActionDate(netDiskBean.getDeleteDate());
				cologBean.setActionName(netDiskBean.getName());
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(userName);
				CooperateLogDb cooperateLogDb = new CooperateLogDb(cologBean);
				re = cooperateLogDb.create();
			}
			return re;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 协作动态
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean shareLog(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);
			cooperateBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			boolean isCloudDiskRoot = cfg.getBooleanProperty("isCloudDiskRoot");
			cooperateBean.setVisualPath(userName + "/"
					+ (isCloudDiskRoot ? "" : MYDISK + "/") + fileName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description: 解散协作
	 * @param inputStream
	 * @return
	 */
	public boolean shareRelease(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			String userName = ByteConvertUtil.ByteArraytoString(buf);
			cooperateBean.setUserName(userName);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int fileNameLength = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[fileNameLength];
			inputStream.readFully(buf, 0, fileNameLength);
			String fileName = ByteConvertUtil.ByteArraytoString(buf);

			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			boolean isCloudDiskRoot = cfg.getBooleanProperty("isCloudDiskRoot");
			cooperateBean.setVisualPath(userName + "/"
					+ (isCloudDiskRoot ? "" : MYDISK + "/") + fileName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 获取所有的服务器端文件，用于第一次登录的时候同步
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileAll(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);

			String userName = ByteConvertUtil.ByteArraytoString(buf);
			netDiskBean.setUserName(userName);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 当有分享文件到达时，进行接收、拒绝、取消操作
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileReceived(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			int id = ByteConvertUtil.ByteArraytoInt(buf);
			cooperateBean.setId(id);

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			cooperateBean.setIsRefused(buf[0]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			CooperateDb coDb = new CooperateDb(cooperateBean);
			return coDb.update();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 客户端根据文件ID接收文件
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileReceive(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			int id = ByteConvertUtil.ByteArraytoInt(buf);
			netDiskBean.setId(id);

			/*
			 * buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			 * inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE); int
			 * userLength = buf[0];
			 * 
			 * buf = new byte[userLength]; inputStream.readFully(buf, 0,
			 * userLength);
			 * 
			 * String userName = ByteConvertUtil.ByteArraytoString(buf);
			 * netDiskBean.setUserName(userName);
			 */

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 客户端根据文件ID接收文件以及上次接收的最大包号
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileReceive2(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			int id = ByteConvertUtil.ByteArraytoInt(buf);
			netDiskBean.setId(id);
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			int lastPacket = ByteConvertUtil.ByteArraytoInt(buf);
			netDiskBean.setPackageCount(lastPacket);

			/*
			 * buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			 * inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE); int
			 * userLength = buf[0];
			 * 
			 * buf = new byte[userLength]; inputStream.readFully(buf, 0,
			 * userLength);
			 * 
			 * String userName = ByteConvertUtil.ByteArraytoString(buf);
			 * netDiskBean.setUserName(userName);
			 */

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 客户端发送文件至服务器端（文件本身，非ID）
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean fileSend(DataInputStream inputStream) {
		DataOutputStream dos = null;
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_LONG];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_LONG);
			long fileLength = ByteConvertUtil.ByteArraytoLong(buf);

			attachmentBean.setFileSize(fileLength);
			netDiskBean.setFileSize(fileLength);

			// 似乎netDiskBean的visualPath未初始化 ??????????
			// fileSend方法一定是在fileCommit/checkStatus之后调用的,依然在同一个连接中,netDiskBean在fileComit中已经赋值
			File file = new File(filePath + netDiskBean.getVisualPath()
					+ File.separator);
			if (!file.exists()) {
				file.mkdirs();
			}

			file = new File(filePath + netDiskBean.getVisualPath()
					+ File.separator + netDiskBean.getName());

			file.renameTo(new File(filePath + netDiskBean.getVisualPath()
					+ File.separator + netDiskBean.getDiskName()));

			FileOutputStream fos = new FileOutputStream(filePath
					+ netDiskBean.getVisualPath() + File.separator
					+ netDiskBean.getName());
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			if (bos != null) {
				dos = new DataOutputStream(bos);
			} else {
				return false;
			}
			long len = 0;

			while (len < fileLength) {
				int curlen = (int) (fileLength - len < ServerWorker.MAX_RECV_BUF_LEN ? fileLength
						- len
						: ServerWorker.MAX_RECV_BUF_LEN);
				buf = new byte[curlen];
				inputStream.readFully(buf, 0, curlen);
				dos.write(buf);
				len += curlen;
			}
			bos.close();

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				// AttachmentDb attachmentDb = new AttachmentDb(attachmentBean);
				// if (!attachmentDb.isExist()) {
				// attachmentDb.create();
				// }
				// 只要net_disk进行判断,netdisk_document_attach不做判断

				// attachmentDb.create();
				NetDiskDb netDiskDb = new NetDiskDb(netDiskBean);

				// 更新文件大小
				return netDiskDb.updateFileSize();
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (IOException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
	}

	/**
	 * 检测客户端新版本，判断是否需要更新x86
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean checkVersion(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int verLength = buf[0];

			buf = new byte[verLength];
			inputStream.readFully(buf, 0, verLength);
			versionBean.setFileVersion(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			versionBean.setIsWow64(0);
			VersionDb versionDb = new VersionDb(versionBean);
			if (versionDb.isLastVersion()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 检测客户端新版本，判断是否需要更新x64
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean checkVersion64(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int verLength = buf[0];

			buf = new byte[verLength];
			inputStream.readFully(buf, 0, verLength);
			versionBean.setFileVersion(ByteConvertUtil.ByteArraytoString(buf));

			byte isWow64 = inputStream.readByte();

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			versionBean.setIsWow64(isWow64);
			VersionDb versionDb = new VersionDb(versionBean);
			if (versionDb.isLastVersion()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 在线升级x86
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean onlineUpdate(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			VersionDb versionDb = new VersionDb(versionBean);
			if (!versionDb.getFullPath()) {
				return false;
			}

			File file = new File(versionBean.getFilePath());
			if (!file.exists()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 在线升级x64
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean onlineUpdate64(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			VersionDb versionDb = new VersionDb(versionBean);
			if (!versionDb.getFullPath64()) {
				return false;
			}

			File file = new File(versionBean.getFilePath());
			if (!file.exists()) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description: 角色模板,暂不使用
	 * @param inputStream
	 * @return
	 */
	public boolean roleTemplate(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description: 获取全部个人文件夹
	 * @param inputStream
	 * @return
	 */
	public boolean getDirs(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			directoryBean.setRootCode(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description:
	 * @param inputStream
	 * @return
	 */
	public boolean setDirs(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			directoryBean.setRootCode(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int dirLength = buf[0];

			buf = new byte[dirLength];
			inputStream.readFully(buf, 0, dirLength);
			String dir = ByteConvertUtil.ByteArraytoString(buf);

			String[] path = parseFileName(dir);
			directoryBean.setName(path[0] + "/" + path[1]);

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description: 初始化角色模板
	 * @param inputStream
	 * @return
	 */
	public boolean initRoleTemplate(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			netDiskBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 获取字符集
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean getCharset(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 获取用户网盘空间
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean getDiskSpace(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			userBean.setName(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 获取是否启用侧边栏html
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean getIsSidebarHtml(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			userBean.setName(ByteConvertUtil.ByteArraytoString(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 是否启用客户端的高级功能
	 * 
	 * @Description:
	 * @param inputStream
	 * @return
	 */
	public boolean getIsVip(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 限速
	 * 
	 * @Description:
	 * @param inputStream
	 * @return
	 */
	public boolean getSetSpeed(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			userBean.setName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			userBean.setSpeed(ByteConvertUtil.ByteArraytoInt(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * 校验侧边栏是否需要更新
	 * 
	 * @Description:
	 * @param inputStream
	 * @return
	 */
	public boolean getCheckSidebar(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			sideBean.setUserName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			sideBean.setIsCheck(buf[0]);

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			sideBean.setFlowCount(ByteConvertUtil.ByteArraytoInt(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			sideBean.setMsgCount(ByteConvertUtil.ByteArraytoInt(buf));

			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}

	/**
	 * @Description: 分包校验
	 * @param inputStream
	 * @return
	 */
	public boolean getSub(DataInputStream inputStream) {
		try {
			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			// 报文协议版本,暂未使用
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			// id号
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			netDiskBean.setId(ByteConvertUtil.ByteArraytoInt(buf));

			NetDiskDb netDiskDb = new NetDiskDb(netDiskBean);
			netDiskDb.load();

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			// 用户名标识,暂未使用
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			// 用户名长度
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			int userLength = buf[0];

			buf = new byte[userLength];
			inputStream.readFully(buf, 0, userLength);
			userBean.setName(ByteConvertUtil.ByteArraytoString(buf));

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			// 分包标识
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			// 子包数
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			netDiskBean.setPackageCount(ByteConvertUtil.ByteArraytoInt(buf));

			buf = new byte[ServerWorker.MD5_LENGTH];
			inputStream.readFully(buf, 0, ServerWorker.MD5_LENGTH);
			netDiskBean.setMd5(ByteConvertUtil.ByteArraytoString(buf));

			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return false;
	}

	/**
	 * @Description: 重发包
	 * @param inputStream
	 * @return
	 */
	public boolean getResend(DataInputStream inputStream) {
		DataOutputStream dos = null;
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}

			byte[] buf;

			buf = new byte[ByteConvertUtil.SIZEOF_BYTE];
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_BYTE);
			if (buf[0] != ServerWorker.FILE_RESEND) {
				return false;
			}

			buf = new byte[ByteConvertUtil.SIZEOF_INT];
			// 子包序号
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_INT);
			int subNum = ByteConvertUtil.ByteArraytoInt(buf);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			// 子包标识
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);

			buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			// 数据长度
			inputStream.readFully(buf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int dataLen = ByteConvertUtil.ByteArraytoShort(buf);

			buf = new byte[dataLen];
			inputStream.readFully(buf, 0, dataLen);

			byte[] crcbuf = new byte[ByteConvertUtil.SIZEOF_SHORT];
			// CRC16校验码
			inputStream.readFully(crcbuf, 0, ByteConvertUtil.SIZEOF_SHORT);
			int crcsrc = ByteConvertUtil.ByteArraytoShort(crcbuf);
			int crcdst = ToolsUtil.CRC16(buf, dataLen, crcbuf);

			if (crcsrc != crcdst) {
				LogUtil.getLog(getClass()).info("error package: " + subNum);
			} else {
				NetDiskDb netDiskDb = new NetDiskDb(netDiskBean);
				netDiskDb.load();
				File file = new File(filePath + netDiskBean.getVisualPath()
						+ File.separator);
				if (!file.exists()) {
					file.mkdirs();
				}

				file = new File(filePath + netDiskBean.getVisualPath()
						+ File.separator + netDiskBean.getName());

				file.renameTo(new File(filePath + netDiskBean.getVisualPath()
						+ File.separator + netDiskBean.getDiskName()));

				FileOutputStream fos = new FileOutputStream(filePath
						+ netDiskBean.getVisualPath() + File.separator
						+ netDiskBean.getName());
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				if (bos != null) {
					dos = new DataOutputStream(bos);
				} else {
					return false;
				}
				long len = 0;

				while (len < dataLen) {
					int curlen = (int) (dataLen - len < ServerWorker.MAX_RECV_BUF_LEN ? dataLen
							- len
							: ServerWorker.MAX_RECV_BUF_LEN);
					buf = new byte[curlen];
					inputStream.readFully(buf, 0, curlen);
					dos.write(buf);
					len += curlen;
				}
			}

			validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			}
			return true;
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (IOException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				return false;
			}
		}
	}

	/**
	 * 每执行完一次后，结束握手
	 * 
	 * @param inputStream
	 * @return
	 */
	public boolean finished(DataInputStream inputStream) {
		try {
			byte validcode = inputStream.readByte();
			if (validcode != ServerWorker.VALID_CODE) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return false;
		}
	}
}
