package com.redmoon.clouddisk.socketServer;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import com.redmoon.clouddisk.tools.ByteConvertUtil;
import com.redmoon.clouddisk.tools.ToolsUtil;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ZipUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;

import com.redmoon.clouddisk.db.*;
import com.redmoon.clouddisk.bean.*;
import com.redmoon.oa.Config;
import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.Leaf;
import com.redmoon.oa.netdisk.RoleTemplateMgr;
import com.redmoon.oa.netdisk.SideBarMgr;
import com.redmoon.oa.pvg.RoleDb;

/**
 * @author 古月圣
 * 
 */
public class ServerWorker implements Runnable {

	public final static byte VALID_CODE = 0x7e; // 校验码
	public final static byte FILE_UPDATE = 0x00; // client update
	public final static byte FILE_COMMIT = 0x01; // client commit
	public final static byte FILE_SYNC = 0x02; // 同步
	public final static byte FILE_SHARE = 0x03; // 分享
	public final static byte FILE_HISTORY = 0x04; // 历史记录
	public final static byte FILE_URL = 0x05; // 文件外链
	public final static byte FILE_DOWNLOAD = 0x06; // 文件下载
	public final static byte FILE_RESTORE = 0x07; // 文件恢复
	public final static byte FILE_SHARED = 0x08; // 被分享文件
	public final static byte FILE_DELETE = 0x09; // 文件删除
	public final static byte FILE_LOG = 0x0a; // 文件日志
	public final static byte FILE_ALL = 0x0b; // 所有文件
	public final static byte FILE_RECEIVED = 0x0c; // 是否接收
	public final static byte FILE_DELETED = 0x0d; // 找回误删文件
	public final static byte FILE_RENAME = 0x0e; // 文件充满

	// 分包校验
	public final static byte FILE_ASK = 0x13; // 事务请求
	public final static byte FILE_INFO = 0x14; // 文件基本信息
	public final static byte FILE_SUB = 0x15; // 文件包发送
	public final static byte FILE_ENDED = 0x16; // 分包发送结束
	public final static byte FILE_RESEND = 0x1a; // 分包发送结束

	public final static byte FILE_ASK_R = 0x1b; // 事务请求
	public final static byte FILE_INFO_R = 0x1c; // 文件基本信息
	public final static byte FILE_SUB_R = 0x1d; // 文件包发送
	public final static byte FILE_ENDED_R = 0x1e; // 分包发送结束
	public final static byte FILE_RESEND_R = 0x1f; // 分包发送结束

	public final static byte FILE_SEND = 0x10; // client send
	public final static byte FILE_RECEIVE = 0x11; // client receive
	public final static byte FILE_STATUS = 0x12; // 服务器端文件状态

	public final static byte DIR_DELETE = 0x20; // 文件夹删除
	public final static byte DIR_RENAME = 0x21; // 文件夹重命名

	public final static byte SHARE_STATUS = 0x2a; // 协作文件状态
	public final static byte SHARE_DELETE = 0x2b; // 删除协作文件
	public final static byte SHARE_LOG = 0x2c; // 协作动态
	public final static byte SHARE_RELEASE = 0x2d; // 协作解散
	public final static byte SHARE_DIR_DELETE = 0x2e; // 删除协作文件夹
	public final static byte SHARE_RENAME = 0x2f; // 重命名协作文件
	public final static byte SHARE_DIR_RENAME = 0x29; // 重命名协作文件夹

	public final static byte CHECK_USER = 0x30; // 检查用户
	public final static byte DEPT_USER = 0x31; // 部门用户
	public final static byte USER_LOGIN = 0x32; // 用户登录
	public final static byte CHECK_STATUS = 0x33; // 检查登录状态
	public final static byte GET_KEY = 0x34; // 获取密钥
	public final static byte CHECK_SYNCED = 0x35; // 检查同步
	public final static byte GET_URL = 0x36; // 检查IP
	public final static byte GET_SSO = 0x37; // 获取同步密钥
	public final static byte BACK_UP = 0x38; // 备份
	public final static byte ROLE_TEMPLATE = 0x39; // 角色模板
	public final static byte GET_DIRS = 0x3a; // 获取文件夹
	public final static byte INIT_ROLE = 0x3b; // 初始化角色模板
	public final static byte SET_DIRS = 0x3c; // 提交文件夹

	public final static byte FINISHED = 0x40;

	public final static byte CHECK_VERSION = (byte) 0xee;
	public final static byte ONLINE_UPDATE = (byte) 0xef;
	public final static byte CHECK_VERSION64 = (byte) 0xec;
	public final static byte ONLINE_UPDATE64 = (byte) 0xed;
	public final static byte GET_CHARSET = (byte) 0xeb;
	public final static byte GET_TOTALSPACE = (byte) 0xea;
	public final static byte GET_ISHTML = (byte) 0xe9;
	public final static byte GET_ISVIP = (byte) 0xe8;
	public final static byte SET_SPEED = (byte) 0xe7;
	public final static byte CHECK_SIDEBAR = (byte) 0xe6;

	public final static byte USER_ERROR = 0x00;
	public final static byte USER_SUCCEED = 0x01;
	public final static byte USER_NONE = 0x02;

	public final static int TIME_LENGTH = 19;
	public final static int MAX_VER_COUNT = 100;
	public final static int MAX_SEND_BUF_LEN = 40000;
	public final static int MAX_RECV_BUF_LEN = 40000;
	public final static int MAX_CLIENT_BUF_LEN = 40000;

	public final static char FIRST_SEPT = 0x27;
	public final static char SECOND_SEPT = 0x22;
	public final static char LAST_SEPT = 0x3b;

	public final static int SHARED_FILE = 1;
	public final static int NOT_SHARED_FILE = 0;

	public final static int EDIT_FILE = 1;

	public final static String ROOT = "root";
	public final static String SHARE_FILE = "/共享文件/";

	private Socket theSocket = null;
	private DataInputStream inputStream = null;
	public String filePath = null;
	public final static int MD5_LENGTH = 32;

	private final static int TYPE_SAME_LENGTH = 1;
	private final static int TYPE_DIFF_LENGTH = 0;

	private static boolean OPEN_RESUME_BROKEN = true;
	private static HashMap<String, Long> speedMap = new HashMap<String, Long>();
	private static HashMap<String, Integer> threadCountMap = new HashMap<String, Integer>();

	/**
	 * @param theSocket
	 */
	public ServerWorker(Socket theSocket) {
		Config cfg = new Config();
		filePath = CloudDiskThread.OA_REALPATH + cfg.get("file_netdisk") + "/";
		this.theSocket = theSocket;
	}

	/**
	 * @param flag
	 */
	public void sendMsg(byte flag) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(flag);
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @Description:
	 * @param flag
	 * @param id
	 */
	public void sendMsg(byte status, int id) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(status);
			out.write(ByteConvertUtil.InttoByteArray(id));
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @param msg
	 */
	public void sendMsg(String msg) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(USER_SUCCEED);
			out.write(ByteConvertUtil.ShorttoByteArray((short) msg.trim()
					.getBytes().length));
			out.write(msg.trim().getBytes());
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @param msg
	 */
	public void sendMsg(byte[] msg) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(ByteConvertUtil.InttoByteArray(msg.length));
			// out.write(msg);
			int len = 0;
			while (len < msg.length) {
				int curlen = msg.length - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
						: (int) msg.length - len;
				out.write(msg, len, curlen);
				len += curlen;
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @param i
	 */
	public void sendMsg(int i) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(USER_SUCCEED);
			out.write(ByteConvertUtil.InttoByteArray(i));
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @Description:
	 * @param l
	 */
	public void sendMsg(long l) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(USER_SUCCEED);
			out.write(ByteConvertUtil.LongtoByteArray(l));
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @param userBean
	 */
	public void sendMsg(UserBean userBean) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(ByteConvertUtil.ShorttoByteArray((short) userBean
					.getName().trim().getBytes().length));
			out.write(userBean.getName().getBytes());
			out.write(ByteConvertUtil.ShorttoByteArray((short) userBean
					.getRealName().trim().getBytes().length));
			out.write(userBean.getRealName().getBytes());
			out.write(ByteConvertUtil.ShorttoByteArray((short) userBean
					.getDeptName().trim().getBytes().length));
			out.write(userBean.getDeptName().getBytes());
			out.write(ByteConvertUtil.ShorttoByteArray(userBean.getGender()));
			out.write(ByteConvertUtil.LongtoByteArray(userBean.getDiskSpace()));
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	public void sendMsg(File file) {
		DataOutputStream out = null;
		DataInputStream in = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			in = new DataInputStream(bis);
			long len = 0;

			byte[] length = ByteConvertUtil.LongtoByteArray(file.length());
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(USER_SUCCEED);
			out.write(length);

			while (len < file.length()) {
				byte[] buf = new byte[file.length() - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
						: (int) (file.length() - len)];
				len += in.read(buf);
				out.write(buf);
			}

			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
		}
	}

	/**
	 * @param msg
	 * @param type
	 */
	public void sendMsg(ArrayList<String> msg, int type) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			int total = msg == null ? 0 : msg.size();
			out.write(ByteConvertUtil.InttoByteArray(total));

			for (int i = 0; i < total; i++) {
				int length = msg.get(i).trim().getBytes().length;
				if (type == TYPE_DIFF_LENGTH) {
					out.write(ByteConvertUtil.InttoByteArray(length));
				}

				int len = 0;
				while (len < length) {
					int curlen = length - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
							: length - len;
					out.write(msg.get(i).trim().getBytes(), len, curlen);
					len += curlen;
				}
				// out.write(msg.get(i).trim().getBytes());
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (SocketException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	public void sendMsg(HashMap<String, String> msg) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			int total = msg == null ? 0 : msg.size();
			out.write(ByteConvertUtil.InttoByteArray(total));

			if (msg != null) {
				for (Map.Entry<String, String> entry : msg.entrySet()) {
					String value = entry.getValue();
					int length = value.getBytes().length;
					out.write(ByteConvertUtil.InttoByteArray(length));

					int len = 0;
					while (len < length) {
						int curlen = length - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
								: length - len;
						out.write(value.getBytes(), len, curlen);
						len += curlen;
					}
					// out.write(msg.get(i).trim().getBytes());
				}
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (SocketException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	public void sendMsg(SideBean sideBean) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			switch (sideBean.getModFlag()) {
			case SideDb.MOD_NONE:
				out.write(USER_NONE);
				break;
			case SideDb.MOD_OTHER:
				out.write(USER_SUCCEED);
				out.write(SideDb.MOD_OTHER);
				break;
			case SideDb.MOD_COUNT:
				out.write(USER_SUCCEED);
				out.write(SideDb.MOD_COUNT);
				out.write(ByteConvertUtil.InttoByteArray(sideBean
						.getFlowCount()));
				out.write(ByteConvertUtil
						.InttoByteArray(sideBean.getMsgCount()));
				break;
			case SideDb.MOD_PIC:
				out.write(USER_SUCCEED);
				out.write(SideDb.MOD_PIC);

				// 图片分为_default,_move,_click,以及msg_count.png
				byte total = (byte) (sideBean.getIsCheck() == SideDb.IS_CHECK ? sideBean
						.getList().size() + 1
						: sideBean.getList().size());
				out.write(total);

				total = (byte) (sideBean.getIsCheck() == SideDb.IS_CHECK ? sideBean
						.getList().size() + 1
						: sideBean.getList().size());

				for (byte i = 0; i < total; i++) {
					String path = null;
					File file = null;
					if (sideBean.getIsCheck() == SideDb.IS_CHECK
							&& i == total - 1) {
						path = "netdisk\\images\\msg_count.png";
					} else {
						path = "netdisk\\images\\appImages\\"
								+ sideBean.getList().get(i).replaceAll("/",
										"\\\\");
					}

					file = new File(Global.getAppPath() + path);

					short nameLength = (short) path.getBytes().length;
					out.write(ByteConvertUtil.ShorttoByteArray(nameLength));
					out.write(path.getBytes());

					DataInputStream in = null;
					FileInputStream fis = null;
					BufferedInputStream bis = null;
					try {
						fis = new FileInputStream(file);
						bis = new BufferedInputStream(fis);
						in = new DataInputStream(bis);
						long len = 0;

						out.write(ByteConvertUtil
								.LongtoByteArray(file.length()));

						while (len < file.length()) {
							byte[] buf = new byte[file.length() - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
									: (int) (file.length() - len)];
							len += in.read(buf);
							out.write(buf);
						}

					} catch (IOException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								LogUtil.getLog(getClass()).error(
										StrUtil.trace(e));
							}
						}
						if (bis != null) {
							try {
								bis.close();
							} catch (IOException e) {
								LogUtil.getLog(getClass()).error(
										StrUtil.trace(e));
							}
						}
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								LogUtil.getLog(getClass()).error(
										StrUtil.trace(e));
							}
						}
					}
				}
				out.write(ByteConvertUtil.InttoByteArray(sideBean
						.getFlowCount()));
				out.write(ByteConvertUtil
						.InttoByteArray(sideBean.getMsgCount()));
				break;
			default:
				out.write(USER_NONE);
				break;
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	/**
	 * @param msg
	 * @param type
	 */
	public void packageResend(ArrayList<Integer> msg) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			out.write(USER_ERROR);
			out.write(ByteConvertUtil.InttoByteArray(msg.size()));
			for (int num : msg) {
				out.write(ByteConvertUtil.InttoByteArray(num));
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (SocketException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	public void sendMsg(NetDiskBean netDiskBean) {
		String path = filePath
				+ netDiskBean.getVisualPath()
				+ "/"
				+ (netDiskBean.getDiskName().equals("") ? netDiskBean.getName()
						: netDiskBean.getDiskName());
		DataOutputStream out = null;
		DataInputStream in = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			out = new DataOutputStream(theSocket.getOutputStream());

			File file = new File(path);
			if (!file.exists()) {
				NetDiskDb ndb = new NetDiskDb(netDiskBean);
				ndb.delete();
				out.write(VALID_CODE);
				out.write(USER_NONE);
				out.write(VALID_CODE);
				out.flush();
			} else {
				fis = new FileInputStream(path);
				bis = new BufferedInputStream(fis);
				in = new DataInputStream(bis);

				long len = 0;
				long fileLength = file.length();

				byte[] length = ByteConvertUtil.LongtoByteArray(fileLength);
				out.write(VALID_CODE);
				out.write(USER_SUCCEED);
				String a = DateUtil.format(netDiskBean.getTempDate(),
						"yyyy-MM-dd HH:mm:ss");
				out.write(a.getBytes());
				out.write(length);

				while (len < fileLength) {
					byte[] buf = new byte[fileLength - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
							: (int) (fileLength - len)];
					len += in.read(buf);
					out.write(buf);
				}

				out.write(VALID_CODE);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
		}
	}

	public void sendMsg2(NetDiskBean netDiskBean) {
		String userName = netDiskBean.getUserName();
		int curCount = 1;
		if (threadCountMap.containsKey(userName)) {
			curCount = threadCountMap.get(userName) + 1;
		}
		threadCountMap.put(userName, curCount);

		String path = filePath
				+ netDiskBean.getVisualPath()
				+ "/"
				+ (netDiskBean.getDiskName().equals("") ? netDiskBean.getName()
						: netDiskBean.getDiskName());
		DataOutputStream out = null;
		// 用流控制文件发送,RandomAccessFile比流的读取效率低很多,不适用于文件的正常传输
		DataInputStream in = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		// 用RandomAccessFile做seek控制文件丢包的重发
		RandomAccessFile raf = null;

		try {
			out = new DataOutputStream(theSocket.getOutputStream());

			File file = new File(path);
			if (!file.exists()) {
				NetDiskDb ndb = new NetDiskDb(netDiskBean);
				ndb.delete();
				out.write(VALID_CODE);
				out.write(USER_NONE);
				out.write(VALID_CODE);
				out.flush();
			} else {
				long len = 0;
				long fileLength = file.length();

				int allSubCount = (int) (fileLength % MAX_SEND_BUF_LEN > 0 ? fileLength
						/ MAX_SEND_BUF_LEN + 1
						: fileLength / MAX_SEND_BUF_LEN);

				out.write(VALID_CODE);
				out.write(USER_SUCCEED);
				String a = DateUtil.format(netDiskBean.getTempDate(),
						"yyyy-MM-dd HH:mm:ss");
				out.write(a.getBytes());
				out.write(ByteConvertUtil.LongtoByteArray(fileLength));
				out.write(ByteConvertUtil.InttoByteArray(allSubCount));

				// int num = 0;
				int num = 1;

				long sendedInMaxbuf = 0;

				fis = new FileInputStream(path);
				bis = new BufferedInputStream(fis);
				in = new DataInputStream(bis);
				// raf = new RandomAccessFile(path, "r");

				// int k = 0;
				MessageDigest MD5 = MessageDigest.getInstance("MD5");
				long st = System.nanoTime();

				while (len < fileLength) {
					// 断点续传定位
					if (num < netDiskBean.getPackageCount()) {
						byte[] buf = new byte[MAX_SEND_BUF_LEN];
						in.readFully(buf);
						MD5.update(buf);
						num++;
						len += MAX_SEND_BUF_LEN;
						continue;
					}

					long speed = 0;
					if (speedMap.containsKey(userName)) {
						speed = speedMap.get(userName);
					}
					// 多线程限速
					curCount = threadCountMap.get(userName);
					speed /= curCount;

					// 读取文件顺做MD5更新
					// raf.seek(num * MAX_SEND_BUF_LEN);
					int curlen = fileLength - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
							: (int) (fileLength - len);
					byte[] buf = new byte[curlen];
					// raf.readFully(buf);
					in.readFully(buf);
					MD5.update(buf);
					len += curlen;

					// 丢包测试
					// if (num % 23 == 0 && len < fileLength) {
					// System.out.println("discard packet:" + num
					// + " and count is:" + ++k);
					// num++;
					// continue;
					// }

					// 数据体部分
					// out.write(ByteConvertUtil.InttoByteArray(++num));
					out.write(ByteConvertUtil.InttoByteArray(num++));
					out.write(ByteConvertUtil.InttoByteArray(curlen));

					// 做限速控制
					if (speed == 0) {
						// 不限速
						out.write(buf);
					} else if (speed < MAX_SEND_BUF_LEN) {
						int sendLen = MAX_SEND_BUF_LEN - (int) sendedInMaxbuf;
						while (sendLen > speed) {
							out.write(buf, (int) sendedInMaxbuf, (int) speed);
							sendedInMaxbuf += speed;
							long et = System.nanoTime();
							long spentTime = et - st;
							if (spentTime < 1000000000) {
								long sleepTime = 1000000000 - spentTime;
								Thread.sleep(sleepTime / 1000000,
										(int) (sleepTime % 1000000));
								st = System.nanoTime();
							}
							sendLen = MAX_SEND_BUF_LEN - (int) sendedInMaxbuf;
						}
						if (sendLen > 0) {
							out.write(buf, (int) sendedInMaxbuf, sendLen);
							sendedInMaxbuf = 0;
							long et = System.nanoTime();
							long spentTime = et - st;
							if (spentTime < 1000000000) {
								long sleepTime = 1000000000 - spentTime;
								Thread.sleep(sleepTime / 1000000,
										(int) (sleepTime % 1000000));
								st = System.nanoTime();
							}
						}
					} else if (speed >= MAX_SEND_BUF_LEN) {
						out.write(buf);
						sendedInMaxbuf += curlen > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
								: curlen;
						if (sendedInMaxbuf >= speed) {
							long et = System.nanoTime();
							long spentTime = et - st;
							if (spentTime < 1000000000) {
								long sleepTime = 1000000000 - spentTime;
								Thread.sleep(sleepTime / 1000000,
										(int) (sleepTime % 1000000));
							}
							sendedInMaxbuf = 0;
							st = System.nanoTime();
						}
					}
					byte[] pcrc = new byte[2];
					ToolsUtil.CRC16(buf, curlen, pcrc);
					out.write(pcrc);
				}

				String md5Code = fileLength > 0 ? new String(Hex.encodeHex(MD5
						.digest())).toUpperCase() : "";
				// System.out.println(md5Code);

				// 重发包用RandomAccessFile做,所以必须关闭流
				in.close();
				bis.close();
				fis.close();
				in = null;
				bis = null;
				fis = null;

				// 检测丢包
				while (true) {
					byte byteData = inputStream.readByte();
					if (byteData == VALID_CODE) {
						byteData = inputStream.readByte();
						if (byteData == USER_SUCCEED) {
							byteData = inputStream.readByte();
							break;
						} else {
							byte buf[] = new byte[4];
							inputStream.readFully(buf);
							int count = ByteConvertUtil.ByteArraytoInt(buf);
							ArrayList<Integer> list = new ArrayList<Integer>();
							while (count > 0) {
								inputStream.readFully(buf);
								list.add(ByteConvertUtil.ByteArraytoInt(buf));
								count--;
							}
							byteData = inputStream.readByte();

							raf = new RandomAccessFile(path, "r");
							for (int i : list) {
								raf.seek((long) ((i - 1) * MAX_SEND_BUF_LEN));
								int curlen = i < allSubCount ? MAX_SEND_BUF_LEN
										: (int) (fileLength - (long) ((i - 1) * MAX_SEND_BUF_LEN));
								buf = new byte[curlen];
								raf.readFully(buf);
								out.write(ByteConvertUtil.InttoByteArray(i));
								out.write(ByteConvertUtil
										.InttoByteArray(curlen));
								out.write(buf);
								byte[] pcrc = new byte[2];
								ToolsUtil.CRC16(buf, curlen, pcrc);
								out.write(pcrc);
							}
							raf.close();
							raf = null;
						}
					}
				}

				if (md5Code.equals("")) {
					byte[] b = new byte[MD5_LENGTH];
					for (int i = 0; i < MD5_LENGTH; i++) {
						b[i] = 0x00;
					}
					out.write(b);
				} else {
					out.write(md5Code.getBytes());
				}
				out.write(VALID_CODE);
				out.flush();

				// long et = System.nanoTime();
				//
				// long spt = (et - st) / 1000000;
				// System.out.println(fileLength + "---------" + spt
				// + "----------" + (float) (fileLength / spt));
			}
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (InterruptedException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
			curCount = threadCountMap.get(userName);
			threadCountMap.put(userName, --curCount);
		}
	}

	/**
	 * @param msg
	 * @param type
	 */
	public void sendMsg(ArrayList<File> msg) {
		DataOutputStream out = null;
		DataInputStream in = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;

		try {
			out = new DataOutputStream(theSocket.getOutputStream());
			out.write(VALID_CODE);
			int total = msg == null ? 0 : msg.size();
			out.write(ByteConvertUtil.InttoByteArray(total));

			Config cfg = new Config();
			String rootPath = Global.getRealPath()
					+ cfg.get("file_role_template") + File.separator;

			for (int i = 0; i < total; i++) {
				File file = msg.get(i);
				String filePath = file.getAbsolutePath().substring(
						rootPath.length());

				int index = filePath.indexOf(File.separator);
				String roleCode = filePath.substring(0, index);
				RoleDb roleDb = new RoleDb(roleCode);
				filePath = filePath.replace(roleCode, roleDb.getDesc());
				int nameLength = filePath.getBytes().length;

				out.write(ByteConvertUtil.ShorttoByteArray((short) nameLength));
				out.write(filePath.getBytes());

				byte[] length = ByteConvertUtil.LongtoByteArray(file.length());
				out.write(length);

				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				in = new DataInputStream(bis);

				long len = 0;
				while (len < file.length()) {
					byte[] buf = new byte[file.length() - len > MAX_SEND_BUF_LEN ? MAX_SEND_BUF_LEN
							: (int) (file.length() - len)];
					len += in.read(buf);
					out.write(buf);
				}

				bis.close();
				fis.close();
				in.close();
			}
			out.write(VALID_CODE);
			out.flush();
		} catch (SocketException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}

	// 服务器端的执行入口
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		SocketMgr sm = new SocketMgr();
		// InetAddress inet = theSocket.getInetAddress();
		// String clientAddress = inet.getHostAddress();
		boolean flag = true;
		RandomAccessFile raf = null;
		while (flag) {
			try {
				inputStream = new DataInputStream(new BufferedInputStream(
						theSocket.getInputStream()));

				if (inputStream == null) {
					Thread.sleep(1);
					continue;
				}

				byte validcode = inputStream.readByte();
				if (validcode != VALID_CODE) {
					return;
				}

				byte optcode = inputStream.readByte();
				boolean ret = false;
				int res = 0;

				NetDiskBean netDiskBean = new NetDiskBean();
				NetDiskDb netDiskDb = null;
				KeyBean keyBean = new KeyBean();
				UserBean userBean = null;

				switch (optcode) {
				case GET_URL:
					ret = sm.getURL(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					String oa = Global.getFullRootPath();
					if (oa == null || oa.equals("")) {
						sendMsg(USER_NONE);
					} else {
						sendMsg(oa);
					}
					break;
				case CHECK_USER:
					ret = sm.checkUser(inputStream);
					sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					break;
				case DEPT_USER:
					byte[] temp = sm.getUserDept(inputStream);
					if (temp == null) {
						sendMsg("".getBytes());
					} else {
						sendMsg(temp);
					}
					break;
				case USER_LOGIN:
					ret = sm.userLogin(inputStream);
					if (ret) {
						sendMsg(sm.getUserBean());
					} else {
						sendMsg(sm.getUserBean().getFailedReason());
					}
					break;
				case GET_KEY:
					ret = sm.getKey(inputStream);
					if (ret) {
						keyBean = sm.getKeyBean();
						KeyDb keyDb = new KeyDb(keyBean);
						res = keyDb.getCurrentKey();
						if (res == KeyDb.KEY_CORRECT) {
							sendMsg(keyBean.getKey());
						} else if (res == KeyDb.KEY_OVERDUE) {
							sendMsg(USER_NONE);
						} else {
							sendMsg(USER_ERROR);
						}
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case GET_SSO:
					ret = sm.getSso(inputStream);
					if (ret) {
						// com.redmoon.oa.sso.Config cfg = new
						// com.redmoon.oa.sso.Config();
						// sendMsg(cfg.get("key"));
						sendMsg(CloudDiskThread.OA_KEY);
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case FILE_STATUS:
					ret = sm.checkStatus(inputStream);
					if (ret) {
						netDiskBean = sm.getNetDiskBean();
						netDiskDb = new NetDiskDb(netDiskBean);
						byte status = netDiskDb.checkStatus();

						switch (status) {
						case NetDiskDb.CAN_COMMIT:
							// long oldId = netDiskBean.getId();
							// 先将当期版本更新为历史版本
							ret = netDiskDb.update();
							if (ret) {
								// 服务端的实际文件重命名为文件名_版本日期
								File file = new File(filePath
										+ netDiskBean.getVisualPath()
										+ File.separator
										+ netDiskBean.getName());
								File bakFile = new File(filePath
										+ netDiskBean.getVisualPath()
										+ File.separator
										+ netDiskBean.getDiskName());
								if (bakFile.exists()) {
									bakFile.delete();
								}
								ret = file.renameTo(bakFile);
								if (ret) {
									// 再将客户端即将提交的文件作为当期版本
									ret = netDiskDb.create();
									// if (ret) {
									// // 更新所有的被分享者的share_id为当前版本id
									// ret = netDiskDb.updateSharedFile(oldId);
									// }
									if (ret) {
										// 判断是否为协作文件夹,如果是则写协作日志
										CooperateLogBean cooperateLogBean = new CooperateLogBean();
										cooperateLogBean
												.setAction(CooperateLogBean.ACTION_UPDATE);
										netDiskDb
												.checkCooperating(cooperateLogBean);
									}
								}
							}
							break;
						case NetDiskDb.NOT_EXIST:
						case NetDiskDb.DELETED_ADD:
							ret = netDiskDb.create();
							if (ret) {
								// 判断是否为协作文件夹,如果是则写协作日志
								CooperateLogBean cooperateLogBean = new CooperateLogBean();
								cooperateLogBean
										.setAction(CooperateLogBean.ACTION_UPLOAD);
								netDiskDb.checkCooperating(cooperateLogBean);
							}
							break;
						case NetDiskDb.BROKEN_RESUME:
							status = NetDiskDb.NOT_EXIST;
							ret = true;
							break;
						case NetDiskDb.BROKEN_RESUME_R:
							status = NetDiskDb.CAN_UPDATE;
							ret = true;
							break;
						default:
							break;
						}

						if (!ret) {
							sendMsg(USER_ERROR);
						} else {
							sendMsg(status, (int) netDiskBean.getId());
						}
						break;
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case FILE_UPDATE: // 不再使用
					ret = sm.fileUpdate(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					res = netDiskDb.checkStatus();
					if (res == NetDiskDb.CAN_UPDATE) {
						sendMsg(netDiskBean);
					} else {
						sendMsg(USER_NONE);
					}
					break;
				case FILE_COMMIT: // 不再使用
					ret = sm.fileCommit(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					res = netDiskDb.checkStatus();
					byte sendmsg = USER_ERROR;
					if (res == NetDiskDb.NOT_EXIST) {
						ret = netDiskDb.create();
						// sendmsg = ret ? USER_SUCCEED : USER_ERROR;
						sendMsg((int) netDiskBean.getId());
						break;
					}
					/*
					 * else if (res == NetDiskDb.IS_EDIT) { // 分享者如果修改了文件 ret =
					 * netDiskDb.update(); netDiskDb = new
					 * NetDiskDb(netDiskBean); netDiskDb.getVisualPath(); //
					 * getShareUser取得被分享者
					 * netDiskBean.setUserName(netDiskBean.getShareUser()); //
					 * 更新被分享者的文件状态 ret = netDiskDb.updateSharedFile(); sendmsg =
					 * ret ? USER_SUCCEED : USER_ERROR; } else if (res ==
					 * NetDiskDb.CAN_COMMIT) { if (netDiskBean.getIsShare() !=
					 * SHARED_FILE) { // // 不是别人分享给我的文件,即普通文件 ret =
					 * netDiskDb.update(); if (!ret) { sendmsg = USER_ERROR; }
					 * else { ret = netDiskDb.create(); if (ret) { sendMsg((int)
					 * netDiskBean.getId()); break; } else { sendmsg =
					 * USER_ERROR; } } } else { // 别人分享给我的文件 long oldId =
					 * netDiskBean.getId(); ret = netDiskDb.create(); if (ret) {
					 * sendMsg((int) netDiskBean.getId()); //
					 * 更新被分享者的对应share_id为当前id ret =
					 * netDiskDb.updateSharedFile(oldId);
					 * 
					 * // 如果是别人分享给我的文件,且我可编辑,则同步分享者的文件 if
					 * (netDiskBean.getIsShare() == SHARED_FILE &&
					 * netDiskBean.getIsEdit() == EDIT_FILE) {
					 * 
					 * } } else { sendmsg = USER_ERROR; } } }
					 */
					else {
						sendmsg = USER_NONE;
					}
					sendMsg(sendmsg);
					break;
				case FILE_SHARE:
					ret = sm.fileShare(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					CooperateBean cooperateBean = sm.getCooperateBean();
					CooperateDb cooperateDb = new CooperateDb(cooperateBean);
					cooperateDb.cooperate();
					sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					break;
				case FILE_HISTORY:
					ret = sm.fileHistory(inputStream);
					if (!ret) {
						sendMsg(null, TYPE_SAME_LENGTH);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					ArrayList<String> list = netDiskDb.listHistory();

					sendMsg(list, TYPE_SAME_LENGTH);
					break;
				case FILE_DOWNLOAD:
					ret = sm.fileDownload(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					if (netDiskBean.getIsDoc() == 0) {
						String inPath = filePath + netDiskBean.getVisualPath()
								+ File.separator;

						File file = new File(inPath);
						if (!file.exists()) {
							sendMsg(USER_ERROR);
							break;
						}

						String outPath = filePath + "download" + File.separator
								+ netDiskBean.getUserName() + File.separator;

						file = new File(outPath);
						if (!file.exists()) {
							file.mkdirs();
						}

						String outName = "云盘下载_" + netDiskBean.getName() + "_"
								+ DateUtil.format(new Date(), "yyyyMMddHHmmss");

						ArrayList<String> listZip = netDiskDb.listFilesForZip(
								netDiskBean.getVisualPath(), netDiskBean
										.getName());

						ZipUtil.zip(inPath, listZip, outPath, outName);

						// 参数:isDoc,visualPath
						String param = "isDoc=0&name="
								+ StrUtil.UrlEncode(outName + ".zip");
						sendMsg(param);
					} else {
						String urlName = "";
						if (netDiskBean.getIsCurrent() == 1) {
							if (!netDiskDb.getCurrentId()) {
								sendMsg(USER_ERROR);
								break;
							}
							urlName = netDiskBean.getName();
						} else {
							urlName = netDiskDb.getUrlName();
						}

						if (urlName == null || urlName.equals("")) {
							sendMsg(USER_ERROR);
						} else {
							// 参数:id,文件名
							String param = "id=" + netDiskBean.getId()
									+ "&name=" + StrUtil.UrlEncode(urlName);
							sendMsg(param);
						}
					}
					break;
				case FILE_RESTORE:
					ret = sm.fileRestore(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}

					// 将数据库中当前版本文件置为历史版本文件
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					netDiskDb.getVersionDate();
					netDiskBean.setDiskName(netDiskBean.getName()
							+ "_"
							+ DateUtil.format(netDiskBean.getTempDate(),
									"yyyyMMddHHmmss"));

					ret = netDiskDb.update();

					if (!ret) {
						sendmsg = USER_ERROR;
					} else {
						// 将文件夹中当前版本文件置为历史版本文件
						File file = new File(filePath
								+ netDiskBean.getVisualPath() + File.separator
								+ netDiskBean.getName());
						File newFile = new File(filePath
								+ netDiskBean.getVisualPath() + File.separator
								+ netDiskBean.getDiskName());
						if (newFile.exists()) {
							newFile.delete();
						}
						ret = file.renameTo(newFile);
						if (!ret) {
							sendmsg = USER_ERROR;
						} else {
							// 将数据库中指定的历史版本文件还原为当前版本文件
							String fileDiskName = netDiskDb.getFileName();
							netDiskBean.setDiskName(netDiskBean.getName());
							// 以当前时间作为新版本时间
							Date date = new Date();
							netDiskBean.setVersionDate(date);
							netDiskBean.setTempDate(date);
							netDiskDb.create();
							if (!ret) {
								sendmsg = USER_ERROR;
							} else {
								// 将文件夹中指定的历史版本文件还原为当前版本文件
								ret = FileUtil.CopyFile(filePath
										+ netDiskBean.getVisualPath()
										+ File.separator + fileDiskName,
										filePath + netDiskBean.getVisualPath()
												+ File.separator
												+ netDiskBean.getName());
								if (ret) {
									file = new File(filePath
											+ netDiskBean.getVisualPath()
											+ File.separator
											+ netDiskBean.getName());
									ret = file.setLastModified(date.getTime());
								}
								sendmsg = ret ? USER_SUCCEED : USER_ERROR;
							}
						}
					}
					if (ret) {
						sendMsg(netDiskBean);
					} else {
						sendMsg(sendmsg);
					}
					break;
				case FILE_SHARED:
					ret = sm.fileShared(inputStream);
					if (!ret) {
						sendMsg(null, TYPE_DIFF_LENGTH);
						break;
					}
					cooperateBean = sm.getCooperateBean();
					cooperateDb = new CooperateDb(cooperateBean);
					ArrayList<String> ndblist = cooperateDb.listSharedFile();

					sendMsg(ndblist, TYPE_DIFF_LENGTH);
					break;
				case FILE_DELETE:
					ret = sm.fileDelete(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					res = netDiskDb.checkStatus();
					if (res == NetDiskDb.NOT_EXIST) {
						sendMsg(USER_NONE);
					} else {
						Attachment att = new Attachment((int) netDiskBean
								.getId());
						ret = att.delLogical();
						sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					}
					break;
				case DIR_DELETE: {
					ret = sm.dirDelete(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					DirectoryBean directoryBean = new DirectoryBean();
					DirectoryDb dircetoryDb = new DirectoryDb(directoryBean);
					String dirCode = dircetoryDb.getDirCodeByPath(netDiskBean
							.getVisualPath()
							+ "/" + netDiskBean.getName());

					if (dirCode == null || dirCode.equals("")) {
						sendMsg(USER_NONE);
					}

					Leaf leaf = new Leaf(dirCode);

					if (leaf == null || !leaf.isLoaded()) {
						sendMsg(USER_NONE);
					} else {
						ret = leaf.delFolders(leaf);
						sendMsg(ret ? USER_SUCCEED : USER_NONE);
					}
					break;
				}
				case FILE_RENAME: {
					ret = sm.fileRename(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					String newName = netDiskBean.getDiskName();
					res = netDiskDb.checkStatus();
					if (res == NetDiskDb.NOT_EXIST) {
						sendMsg(USER_NONE);
					} else {
						Attachment att = new Attachment((int) netDiskBean
								.getId());
						ret = att.changeName(newName, (int) netDiskBean
								.getDocId());
						sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					}
					break;
				}
				case DIR_RENAME: {
					ret = sm.dirRename(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					DirectoryBean directoryBean = new DirectoryBean();
					DirectoryDb dircetoryDb = new DirectoryDb(directoryBean);
					String dirCode = dircetoryDb.getDirCodeByPath(netDiskBean
							.getVisualPath()
							+ "/" + netDiskBean.getName());

					if (dirCode == null || dirCode.equals("")) {
						sendMsg(USER_NONE);
					}

					Leaf leaf = new Leaf(dirCode);

					if (leaf == null || !leaf.isLoaded()) {
						sendMsg(USER_NONE);
					} else {
						try {
							ret = leaf.rename(netDiskBean.getDiskName());
						} catch (ErrMsgException e) {
							LogUtil.getLog(getClass()).error(StrUtil.trace(e));
						}
						sendMsg(ret ? USER_SUCCEED : USER_NONE);
					}
					break;
				}
				case FILE_SEND:
					sendMsg(sm.fileSend(inputStream) ? USER_SUCCEED
							: USER_ERROR);
					break;
				case FILE_RECEIVE:
					ret = sm.fileReceive(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					if (netDiskDb.load()) {
						sendMsg(netDiskBean);
						/*
						 * if (netDiskBean.getShareId() > 0) { String newPath =
						 * netDiskBean.getUserName() + SHARE_FILE;
						 * FileUtil.CopyFile(filePath +
						 * netDiskBean.getVisualPath() +
						 * netDiskBean.getBakName(), filePath + newPath +
						 * netDiskBean.getBakName()); }
						 */
					} else {
						sendMsg(USER_NONE);
					}
					break;
				case FILE_SUB_R:
					ret = sm.fileReceive2(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					if (netDiskDb.load()) {
						sendMsg2(netDiskBean);
					} else {
						sendMsg(USER_NONE);
					}
					break;
				case FILE_ALL:
					ret = sm.fileAll(inputStream);
					if (!ret) {
						sendMsg(null, TYPE_DIFF_LENGTH);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					HashMap<String, String> map = netDiskDb.listAll();

					sendMsg(map);
					break;
				case FILE_RECEIVED:
					ret = sm.fileReceived(inputStream);
					sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					break;
				case SHARE_STATUS:
					ret = sm.checkShareStatus(inputStream);
					if (ret) {
						netDiskBean = sm.getNetDiskBean();
						netDiskDb = new NetDiskDb(netDiskBean);
						byte status = netDiskDb.checkStatus();

						cooperateBean = sm.getCooperateBean();
						cooperateDb = new CooperateDb(cooperateBean);

						if (cooperateDb.getDirCodeByPath()) {
							sendMsg(USER_NONE);
							break;
						}

						CooperateLogBean cologBean = new CooperateLogBean();
						cologBean.setActionName(cooperateBean.getVisualPath()
								+ "/" + netDiskBean.getName());
						cologBean.setDirCode(cooperateBean.getDirCode());
						cologBean.setActionDate(new Date());
						cologBean.setUserName(cooperateBean.getUserName());

						CooperateLogDb cooperateLogDb = new CooperateLogDb(
								cologBean);

						switch (status) {
						case NetDiskDb.CAN_COMMIT:
							// 先将当期版本更新为历史版本
							ret = netDiskDb.update();
							if (ret) {
								// 服务端的实际文件重命名为文件名_版本日期
								File file = new File(filePath
										+ netDiskBean.getVisualPath()
										+ File.separator
										+ netDiskBean.getName());
								ret = file.renameTo(new File(filePath
										+ netDiskBean.getVisualPath()
										+ File.separator
										+ netDiskBean.getDiskName()));
								if (ret) {
									// 再将客户端即将提交的文件作为当期版本
									ret = netDiskDb.create();
									if (ret) {
										// 协作日志
										cologBean
												.setAction(CooperateLogBean.ACTION_UPLOAD);
										ret = cooperateLogDb.create();
									}
								}
							}
							break;
						case NetDiskDb.CAN_UPDATE:
							// 协作日志
							cologBean.setAction(CooperateLogBean.ACTION_UPDATE);
							ret = cooperateLogDb.create();
							break;
						case NetDiskDb.NOT_EXIST:
						case NetDiskDb.DELETED_ADD:
							ret = netDiskDb.create();
							break;
						case NetDiskDb.BROKEN_RESUME:
							status = NetDiskDb.NOT_EXIST;
							ret = true;
							break;
						case NetDiskDb.BROKEN_RESUME_R:
							status = NetDiskDb.CAN_UPDATE;
							ret = true;
							break;
						default:
							break;
						}

						if (!ret) {
							sendMsg(USER_ERROR);
						} else {
							sendMsg(status, (int) netDiskBean.getId());
						}
						break;
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case SHARE_DELETE:
					ret = sm.shareFileDelete(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					res = netDiskDb.checkStatus();
					if (res == NetDiskDb.NOT_EXIST) {
						sendMsg(USER_NONE);
					} else {
						ret = netDiskDb.delete();
						if (!ret) {
							sendMsg(USER_ERROR);
						} else {
							File file = new File(filePath
									+ netDiskBean.getVisualPath()
									+ netDiskBean.getName());
							ret = file.renameTo(new File(filePath
									+ netDiskBean.getVisualPath()
									+ netDiskBean.getDiskName()));

							sendMsg(ret ? USER_SUCCEED : USER_ERROR);
						}
					}
					break;
				case SHARE_DIR_DELETE: {
					ret = sm.dirDelete(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					DirectoryBean directoryBean = new DirectoryBean();
					DirectoryDb dircetoryDb = new DirectoryDb(directoryBean);
					String dirCode = dircetoryDb.getDirCodeByPath(netDiskBean
							.getVisualPath()
							+ "/" + netDiskBean.getName());

					if (dirCode == null || dirCode.equals("")) {
						sendMsg(USER_NONE);
					}

					Leaf leaf = new Leaf(dirCode);

					if (leaf == null || !leaf.isLoaded()) {
						sendMsg(USER_NONE);
					} else {
						ret = leaf.delFolders(leaf);
						sendMsg(ret ? USER_SUCCEED : USER_NONE);
					}
					break;
				}
				case SHARE_RENAME: {
					ret = sm.fileRename(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					netDiskDb = new NetDiskDb(netDiskBean);
					String newName = netDiskBean.getDiskName();
					res = netDiskDb.checkStatus();
					if (res == NetDiskDb.NOT_EXIST) {
						sendMsg(USER_NONE);
					} else {
						Attachment att = new Attachment((int) netDiskBean
								.getId());
						ret = att.changeName(newName, (int) netDiskBean
								.getDocId());
					}
					break;
				}
				case SHARE_DIR_RENAME: {
					ret = sm.dirRename(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					DirectoryBean directoryBean = new DirectoryBean();
					DirectoryDb dircetoryDb = new DirectoryDb(directoryBean);
					String dirCode = dircetoryDb.getDirCodeByPath(netDiskBean
							.getVisualPath()
							+ "/" + netDiskBean.getName());

					if (dirCode == null || dirCode.equals("")) {
						sendMsg(USER_NONE);
					}

					Leaf leaf = new Leaf(dirCode);

					if (leaf == null || !leaf.isLoaded()) {
						sendMsg(USER_NONE);
					} else {
						try {
							ret = leaf.rename(netDiskBean.getDiskName());
						} catch (ErrMsgException e) {
							LogUtil.getLog(getClass()).error(StrUtil.trace(e));
							ret = false;
						}
						sendMsg(ret ? USER_SUCCEED : USER_NONE);
					}
					break;
				}
				case SHARE_RELEASE:
					ret = sm.shareRelease(inputStream);
					if (!ret) {
						sendMsg(USER_NONE);
						break;
					}
					cooperateBean = sm.getCooperateBean();
					cooperateDb = new CooperateDb(cooperateBean);
					ret = cooperateDb.release();
					sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					break;
				case SHARE_LOG:
					ret = sm.shareLog(inputStream);
					if (!ret) {
						sendMsg(null, TYPE_DIFF_LENGTH);
						break;
					}
					cooperateBean = sm.getCooperateBean();
					cooperateDb = new CooperateDb(cooperateBean);
					CooperateLogBean cologBean = new CooperateLogBean();
					if (!cooperateBean.getName().equals("")) {
						ret = cooperateDb.getDirCodeByPath();
						cologBean.setDirCode(cooperateBean.getDirCode());
					} else {
						cologBean.setUserName(cooperateBean.getUserName());
					}
					CooperateLogDb cooperateLogDb = new CooperateLogDb(
							cologBean);
					ArrayList<String> coLogList = cooperateLogDb.list();
					sendMsg(coLogList, TYPE_DIFF_LENGTH);
					break;
				case ONLINE_UPDATE:
					ret = sm.onlineUpdate(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					sendMsg(new File(sm.getVersionBean().getFilePath()));
					break;
				case ONLINE_UPDATE64:
					ret = sm.onlineUpdate64(inputStream);
					if (!ret) {
						sendMsg(USER_ERROR);
						break;
					}
					sendMsg(new File(sm.getVersionBean().getFilePath()));
					break;
				case CHECK_VERSION:
					ret = sm.checkVersion(inputStream);
					if (ret) {
						sendMsg(StrUtil.getNullString(sm.getVersionBean()
								.getFileName()));
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case CHECK_VERSION64:
					ret = sm.checkVersion64(inputStream);
					if (ret) {
						sendMsg(StrUtil.getNullString(sm.getVersionBean()
								.getFileName()));
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case ROLE_TEMPLATE:
					ret = sm.roleTemplate(inputStream);
					if (ret) {
						netDiskBean = sm.getNetDiskBean();
						RoleTemplateMgr rtMgr = new RoleTemplateMgr();
						ArrayList<File> rtlist = rtMgr
								.getUserRoleNeedDownload(netDiskBean
										.getUserName());
						sendMsg(rtlist);
					}
					break;
				case GET_DIRS:
					ret = sm.getDirs(inputStream);
					if (ret) {
						DirectoryBean directoryBean = sm.getDirectoryBean();
						DirectoryDb directoryDb = new DirectoryDb(directoryBean);
						sendMsg(directoryDb.getAllDirs().getBytes());
					}
					break;
				case SET_DIRS:
					ret = sm.setDirs(inputStream);
					if (ret) {
						DirectoryBean directoryBean = sm.getDirectoryBean();
						DirectoryDb directoryDb = new DirectoryDb(directoryBean);
						sendMsg(directoryDb.setDir() ? USER_SUCCEED
								: USER_ERROR);
					}
					break;
				case INIT_ROLE:
					ret = sm.initRoleTemplate(inputStream);
					if (ret) {
						netDiskBean = sm.getNetDiskBean();
						RoleTemplateMgr rtMgr = new RoleTemplateMgr();
						ret = rtMgr.copyDirsAndAttToNewUser(netDiskBean
								.getUserName());
					}
					sendMsg(ret ? USER_SUCCEED : USER_ERROR);
					break;
				case GET_CHARSET:
					ret = sm.getCharset(inputStream);
					if (ret) {
						String charset = System.getProperty("sun.jnu.encoding");
						if (charset == null || charset.equals("")) {
							String os = System.getProperty("os.name");
							if (os.toLowerCase().startsWith("linux")) {
								ret = false;
							}
						}
						if (!charset.toLowerCase().startsWith("gb")) {
							ret = false;
						}
					}
					sendMsg(ret ? USER_SUCCEED : USER_NONE);
					break;
				case GET_TOTALSPACE:
					ret = sm.getDiskSpace(inputStream);
					if (ret) {
						userBean = sm.getUserBean();
						com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb(
								userBean.getName());
						if (ud != null && ud.isLoaded()) {
							sendMsg(ud.getDiskSpaceAllowed());
							break;
						}
					}
					sendMsg(USER_NONE);
					break;
				case GET_ISHTML:
					ret = sm.getIsSidebarHtml(inputStream);
					if (ret) {
						com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
								.getInstance();
						if (cfg.getBooleanProperty("is_openSideHTML")) {
							SideBarMgr sbMgr = new SideBarMgr();
							userBean = sm.getUserBean();
							sbMgr.initialization(userBean.getName());
							sendMsg(USER_SUCCEED);
							break;
						}
					}
					sendMsg(USER_NONE);
					break;
				case GET_ISVIP:
					ret = sm.getIsVip(inputStream);
					if (ret) {
						sendMsg(OPEN_RESUME_BROKEN ? USER_SUCCEED : USER_ERROR);
					} else {
						sendMsg(USER_ERROR);
					}
					break;
				case SET_SPEED:
					ret = sm.getSetSpeed(inputStream);
					if (ret) {
						userBean = sm.getUserBean();
						if (userBean.getSpeed() > 0) {
							// 客户端单位KB,转为字节
							speedMap.put(userBean.getName(), userBean
									.getSpeed() * 1024);
						} else {
							speedMap.remove(userBean.getName());
						}
						sendMsg(USER_SUCCEED);
					} else {
						sendMsg(USER_NONE);
					}
					break;
				case CHECK_SIDEBAR:
					ret = sm.getCheckSidebar(inputStream);
					if (ret) {
						SideBean sideBean = sm.getSideBean();
						SideDb sideDb = new SideDb(sideBean);
						sideDb.checkSidebar();
						sendMsg(sideBean);
					} else {
						sendMsg(USER_NONE);
					}
					break;
				case FILE_SUB:
					ret = sm.getSub(inputStream);
					if (!ret) {
						sendMsg(USER_NONE);
						break;
					}
					netDiskBean = sm.getNetDiskBean();
					userBean = sm.getUserBean();
					File file = new File(filePath + netDiskBean.getVisualPath()
							+ File.separator);
					if (!file.exists()) {
						file.mkdirs();
					}

					file = new File(filePath + netDiskBean.getVisualPath()
							+ File.separator + netDiskBean.getName());

					file.renameTo(new File(filePath
							+ netDiskBean.getVisualPath() + File.separator
							+ netDiskBean.getDiskName()));

					raf = new RandomAccessFile(file, "rw");
					// System.out.println("total package: "
					// + netDiskBean.getPackageCount());
					// 记录丢包
					ArrayList<Integer> sublist = new ArrayList<Integer>();
					ResumeBrokenBean rbb = new ResumeBrokenBean();
					rbb.setAttId(netDiskBean.getId());
					rbb.setUserName(userBean.getName());
					rbb.setPackageType(ResumeBrokenDb.TYPE_LAST_PACKAGE);
					ResumeBrokenDb rbd = new ResumeBrokenDb(rbb);

					int i = 0;
					if (OPEN_RESUME_BROKEN) {
						rbd.getLastPackageNo();

						sendMsg(rbb.getPackageNo());

						i = rbb.getPackageNo() == 0 ? 1 : rbb.getPackageNo();
					} else {
						sendMsg(0);
						i = 1;
					}

					// Date st = new Date();
					boolean isBroken = false;
					while (i <= netDiskBean.getPackageCount()) {
						Thread.sleep(1);
						if (OPEN_RESUME_BROKEN) {
							int count = 0;
							long st = System.currentTimeMillis();
							// 如果在网络传输中数据没有完全传递，则方法返回0
							while (count == 0) {
								Thread.sleep(1);
								count = inputStream.available();
								long et = System.currentTimeMillis();
								if (et - st >= 20000) {
									// 中断
									isBroken = true;
									break;
								}
							}
						}

						if (isBroken) {
							break;
						}

						byte[] buf = new byte[ByteConvertUtil.SIZEOF_INT];
						// 子包序号
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_INT);
						int subNum = ByteConvertUtil.ByteArraytoInt(buf);

						// 重复包
						if (subNum < i) {
							continue;
						}

						// 丢包
						while (subNum > i) {
							LogUtil.getLog(getClass()).info(
									"lost package: " + i);
							//System.out.println("lost package: " + i);
							// 记录丢包
							if (OPEN_RESUME_BROKEN) {
								rbb.setPackageNo(i);
								rbb
										.setPackageType(ResumeBrokenDb.TYPE_LOST_PACKAGE);
							}
							rbd.create();
							sublist.add(i++);
							continue;
						}

						buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
						// 子包标识
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_SHORT);

						buf = new byte[ByteConvertUtil.SIZEOF_INT];
						// 数据长度
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_INT);
						int dataLen = ByteConvertUtil.ByteArraytoInt(buf);

						byte[] filebuf = new byte[dataLen];
						inputStream.readFully(filebuf, 0, dataLen);
						raf.seek(MAX_CLIENT_BUF_LEN * (i - 1));
						raf.write(filebuf);

						buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
						// CRC16校验码
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_SHORT);
						int crcsrc = ByteConvertUtil.ByteArraytoShort(buf);
						int crcdst = ToolsUtil.CRC16(filebuf, dataLen, buf);

						if (crcsrc != crcdst) {
							LogUtil.getLog(getClass()).info(
									"error package: " + i);
							// System.out.println("error package: " + i);
							// 记录丢包
							if (OPEN_RESUME_BROKEN) {
								rbb.setPackageNo(i);
								rbb
										.setPackageType(ResumeBrokenDb.TYPE_LOST_PACKAGE);
								rbd.create();
							}
							sublist.add(i++);
						} else {
							// System.out.println("cuuent package: " + i);
							// 记录断点
							if (OPEN_RESUME_BROKEN) {
								rbb.setPackageNo(i);
								rbb
										.setPackageType(ResumeBrokenDb.TYPE_LAST_PACKAGE);
								if (i > 1) {
									rbd.update();
								} else {
									rbd.create();
								}
							}
							i++;
						}
					}

					if (isBroken) {
						raf.close();
						raf = null;
						break;
					}

					validcode = inputStream.readByte();

					if (sublist.size() > 0) {
						packageResend(sublist);
					}

					int j = 0;
					while (sublist.size() > 0) {
						byte[] buf = new byte[ByteConvertUtil.SIZEOF_INT];
						// 子包序号
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_INT);
						int subNum = ByteConvertUtil.ByteArraytoInt(buf);

						// 重复包
						if (subNum < sublist.get(j)) {
							continue;
						}

						// 丢包
						while (subNum > sublist.get(j)) {
							LogUtil.getLog(getClass()).info(
									"lost package: " + sublist.get(j));
							// System.out.println("lost package: "
							// + sublist.get(j));
							j++;
							continue;
						}

						buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
						// 子包标识
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_SHORT);

						buf = new byte[ByteConvertUtil.SIZEOF_INT];
						// 数据长度
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_INT);
						int dataLen = ByteConvertUtil.ByteArraytoInt(buf);

						byte[] filebuf = new byte[dataLen];
						inputStream.readFully(filebuf, 0, dataLen);
						raf.seek((long) (MAX_CLIENT_BUF_LEN * (sublist.get(j) - 1)));
						raf.write(filebuf);

						buf = new byte[ByteConvertUtil.SIZEOF_SHORT];
						// CRC16校验码
						inputStream.readFully(buf, 0,
								ByteConvertUtil.SIZEOF_SHORT);
						int crcsrc = ByteConvertUtil.ByteArraytoShort(buf);
						int crcdst = ToolsUtil.CRC16(filebuf, dataLen, buf);

						if (crcsrc != crcdst) {
							LogUtil.getLog(getClass()).info(
									"error package: " + sublist.get(j));
							// System.out.println("error package: "
							// + sublist.get(j));
							j++;
						} else {
							// System.out.println("cuuent package: "
							// + sublist.get(j));
							sublist.remove(j);
						}
						if (j >= sublist.size()) {
							packageResend(sublist);
							j = 0;
						}
					}

					raf.close();
					raf = null;

					sendMsg(USER_SUCCEED);

					// Date et = new Date();
					// System.out.println("total size: " + file.length()
					// + "	total package: "
					// + netDiskBean.getPackageCount()
					// + "	and spend seconds: "
					// + (et.getTime() - st.getTime()) / 1000);

					String md5 = ToolsUtil.getMD5(file);
					if (!md5.equals(netDiskBean.getMd5())) {
						sendMsg(USER_ERROR);
						break;
					}

					netDiskBean.setFileSize(file.length());
					netDiskDb = new NetDiskDb(netDiskBean);
					netDiskDb.updateFileSize();

					if (OPEN_RESUME_BROKEN) {
						if (rbb.getPackageNo() > 0) {
							// 删除断点续传
							rbd.del();
						}
					}

					sendMsg(USER_SUCCEED);
					break;
				case FINISHED:
					ret = sm.finished(inputStream);
					flag = !ret;
					break;
				default:
					break;
				}
			} catch (SocketException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (EOFException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (IOException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (InterruptedException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (ErrMsgException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (ResKeyException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (SQLException e) {
				flag = false;
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} finally {
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					}
				}
			}
		}
		try {
			theSocket.close();
			inputStream.close();
			// System.out.println(clientAddress + " is disconnected");
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
	}
}
