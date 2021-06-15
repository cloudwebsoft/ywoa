package com.redmoon.oa.netdisk;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

public class SideBarMgr {
	public SideBarMgr() {
		super();
	}

	transient Logger logger = Logger.getLogger(SideBarMgr.class.getName());

	public SideBarDb getSideBarDb(long id) {
		SideBarDb sbd = new SideBarDb();
		long theID = (long) id;
		return sbd.getSideBarDb(theID);
	}

	/**
	 * 找出图片中所有未隐藏的
	 * 
	 * @param userName
	 * @return
	 */
	public Vector<Integer> getTheId(String userName) {
		Vector<Integer> v = new Vector<Integer>();
		String sql = "select id from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName) + " and type=1 and is_show = 1";
		JdbcTemplate jt = new JdbcTemplate();
		int id = 0;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				id = rr.getInt(1);
				v.add(id);
			}
		} catch (SQLException e) {
			logger.error("getTheId:" + e.getMessage());
		} finally {
			jt.close();
		}
		return v;
	}

	/**
	 * 找出已经被隐藏的非图片区域
	 */
	public int getWhichHidden(String userName) {
		String sql = "select position from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName) + " and type=0 and is_show = 0";
		JdbcTemplate jt = new JdbcTemplate();
		int is_show = 1;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				is_show = rr.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("getTheId:" + e.getMessage());
		} finally {
			jt.close();
		}
		return is_show;
	}

	/**
	 * public boolean update(long id, String code, String up_div, String
	 * notice_topic, int is_show, String first_p, String first_a, int
	 * first_open, String second_p, String second_a, int second_open, String
	 * third_p, String third_a, int third_open, String fourth_p, String
	 * fourth_a, int fourth_open, String fifth_p, String fifth_a, int
	 * fifth_open, String sixth_p, String sixth_a, int sixth_open, String
	 * seventh_p, String seventh_a, int seventh_open, String eighth_p, String
	 * eighth_a, int eighth_open, String ninth_p, String ninth_a, int
	 * ninth_open, String tenth_p, String tenth_a, int tenth_open, String
	 * eleventh_p, String eleventh_a, int eleventh_open, String twelfth_p,
	 * String twelfth_a, int twelfth_open) throws ResKeyException,
	 * ErrMsgException, SQLException { boolean re = false; JdbcTemplate jt = new
	 * JdbcTemplate(); SideBarDb sbDb = new SideBarDb(); sbDb =
	 * sbDb.getSideBarDb(id); try { re = sbDb.save(jt, new Object[] {up_div,
	 * notice_topic, is_show, first_p, first_a, first_open, second_p, second_a,
	 * second_open, third_p, third_a, third_open, fourth_p, fourth_a,
	 * fourth_open, fifth_p, fifth_a, fifth_open, sixth_p, sixth_a, sixth_open,
	 * seventh_p, seventh_a, seventh_open, eighth_p, eighth_a, eighth_open,
	 * ninth_p, ninth_a, ninth_open, tenth_p, tenth_a, tenth_open, eleventh_p,
	 * eleventh_a, eleventh_open, twelfth_p, twelfth_a, twelfth_open,id }); }
	 * catch (SQLException e) { logger.error("update: " + e.getMessage()); throw
	 * new ErrMsgException(e.getMessage()); } finally{ jt.close(); } return re;
	 * }
	 */

	/**
	 * 更该图片数据
	 */
	public boolean updateArray(String userName, int isShow, int position,
			String title, String picture, String href, int type, int custom,
			int flag) throws ResKeyException, ErrMsgException, SQLException {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			String sql = "update netdisk_sidebar_setup set is_show=" + isShow
					+ ",title=" + StrUtil.sqlstr(title) + ",picture="
					+ StrUtil.sqlstr(picture) + ",href=" + StrUtil.sqlstr(href)
					+ ",type=" + type + ",custom=" + custom + ",mod_flag="
					+ flag + " where user_name =" + StrUtil.sqlstr(userName)
					+ " and position=" + position;
			re = jt.executeUpdate(sql) >= 1 ? true : false;
		} catch (SQLException e) {
			logger.error("updateArray: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}

	/**
	 * 更改sidebar的属性
	 */
	public boolean updateUpDiv(String userName, int isShow, int position,
			String title, int type, int mod_flag) throws ResKeyException,
			ErrMsgException, SQLException {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "";
		try {
			if (isShow == 0) {
				sql = "update netdisk_sidebar_setup set is_show=" + isShow
						+ ",mod_flag=" + mod_flag + " where user_name = "
						+ StrUtil.sqlstr(userName) + " and type=" + type;
				re = jt.executeUpdate(sql) >= 1 ? true : false;
			} else {
				sql = "update netdisk_sidebar_setup set is_show=" + isShow
						+ ",mod_flag=" + mod_flag + " where user_name = "
						+ StrUtil.sqlstr(userName) + " and position= "
						+ position;
				re = jt.executeUpdate(sql) >= 1 ? true : false;
				if (position == 0) {
					sql = "update netdisk_sidebar_setup set is_show=0,mod_flag="
							+ mod_flag
							+ " where user_name = "
							+ StrUtil.sqlstr(userName) + " and position=999";
				} else {
					sql = "update netdisk_sidebar_setup set is_show=0,mod_flag="
							+ mod_flag
							+ " where user_name = "
							+ StrUtil.sqlstr(userName) + " and position=0";
				}
				if (re) {
					re = jt.executeUpdate(sql) >= 1 ? true : false;
				}
			}
		} catch (SQLException e) {
			logger.error("updateArray: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}

	/**
	 * 更改显示页面的主题
	 */
	public boolean updateNoticeTopic(String userName, int position,
			String title, int mod_flag) throws ResKeyException,
			ErrMsgException, SQLException {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			String sql = "update netdisk_sidebar_setup set title="
					+ StrUtil.sqlstr(title) + ",mod_flag=" + mod_flag
					+ " where user_name = " + StrUtil.sqlstr(userName)
					+ " and position=" + position;
			re = jt.executeUpdate(sql) >= 1 ? true : false;
		} catch (SQLException e) {
			logger.error("updateNoticeTopic: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}

	/**
	 * 初始化个人数据
	 */
	public boolean initialization(String userName) throws ResKeyException,
			ErrMsgException, SQLException {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName);
		ResultIterator ri = jt.executeQuery(sql);
		boolean re = false;
		if (ri.hasNext()) {
			return true;
		}
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		String cfg_imgs = cfg.getProperty("init_sidebar_imgs");
		String[] imgs = cfg_imgs.split(",");
		String cfg_titles = cfg.getProperty("init_sidebar_titles");
		String[] imgsTitle = cfg_titles.split(",");
		String cfg_hrefs = cfg.getProperty("init_sidebar_hrefs");
		String[] hrefs = cfg_hrefs.split(",");
		SideBarDb sbDb = new SideBarDb();
		try {
			for (int i = 0; i < imgs.length; i++) {
				re = sbDb.create(jt,
						new Object[] { userName, SideBarDb.IS_SHOW, i + 1,
								imgsTitle[i], imgs[i], hrefs[i],
								SideBarDb.TYPE_PICTURE, SideBarDb.NOT_CUSTOM });
			}
			if (re) {
				re = sbDb.create(jt, new Object[] { userName,
						SideBarDb.IS_SHOW, SideBarDb.UP_NOTICE, "", "", "",
						SideBarDb.TYPE_NOTICE, SideBarDb.NOT_CUSTOM });
			}
			if (re) {
				re = sbDb.create(jt, new Object[] { userName,
						SideBarDb.NOT_SHOW, SideBarDb.UP_BUTTON, "", "", "",
						SideBarDb.TYPE_NOTICE, SideBarDb.NOT_CUSTOM });
			}
			if (re) {
				re = sbDb.create(jt, new Object[] { userName,
						SideBarDb.IS_SHOW, SideBarDb.NOTICE_TOPIC,
						"flowNotice,msgNotice", "", "",
						SideBarDb.TYPE_NOTICE_TOPIC, SideBarDb.NOT_CUSTOM });
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("update: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}

	/**
	 * 恢复默认数据
	 */
	public boolean recover(String userName) throws ResKeyException,
			ErrMsgException, SQLException {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "delete from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName);
		boolean re = false;
		re = jt.executeUpdate(sql) >= 0 ? true : false;
		if (re) {
			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			String cfg_imgs = cfg.getProperty("init_sidebar_imgs");
			String[] imgs = cfg_imgs.split(",");
			String cfg_titles = cfg.getProperty("init_sidebar_titles");
			String[] imgsTitle = cfg_titles.split(",");
			String cfg_hrefs = cfg.getProperty("init_sidebar_hrefs");
			String[] hrefs = cfg_hrefs.split(",");
			SideBarDb sbDb = new SideBarDb();
			try {
				for (int i = 0; i < imgs.length; i++) {
					re = sbDb.create(jt, new Object[] { userName,
							SideBarDb.IS_SHOW, i + 1, imgsTitle[i], imgs[i],
							hrefs[i], SideBarDb.TYPE_PICTURE,
							SideBarDb.NOT_CUSTOM });
				}
				if (re) {
					re = sbDb.create(jt, new Object[] { userName,
							SideBarDb.IS_SHOW, SideBarDb.UP_NOTICE, "", "", "",
							SideBarDb.TYPE_NOTICE, SideBarDb.NOT_CUSTOM });
				}
				if (re) {
					re = sbDb.create(jt, new Object[] { userName,
							SideBarDb.NOT_SHOW, SideBarDb.UP_BUTTON, "", "",
							"", SideBarDb.TYPE_NOTICE, SideBarDb.NOT_CUSTOM });
				}
				if (re) {
					re = sbDb
							.create(jt, new Object[] { userName,
									SideBarDb.IS_SHOW, SideBarDb.NOTICE_TOPIC,
									"flowNotice,msgNotice", "", "",
									SideBarDb.TYPE_NOTICE_TOPIC,
									SideBarDb.NOT_CUSTOM });
				}
				String folderPath = Global.getAppPath()
						+ "netdisk/images/appImages/" + userName;
				File f = new File(folderPath);
				if (f.exists()) {
					FileUtil.del(folderPath);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("update: " + e.getMessage());
				throw new ErrMsgException(e.getMessage());
			} finally {
				jt.close();
				RMCache rm = RMCache.getInstance();
				try {
					rm.clear();
				} catch (CacheException e) {
					logger.error("update: " + e.getMessage());
				}
			}
		} else {
			return false;
		}
		return re;
	}

	/**
	 * @Description: 获取主题字符串
	 * @param userName
	 * @return
	 */
	public String getTopicString(String userName) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select title from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName)
				+ " and type="
				+ SideBarDb.TYPE_NOTICE_TOPIC;
		String topic = "";
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				topic = rr.getString(1);
			}
		} catch (Exception e) {
			logger.error("getTopicString: " + e.getMessage());
		} finally {
			jt.close();
		}
		return topic;
	}

	/**
	 * @Description: 侧边栏的图片属性
	 * @param user_name
	 * @return
	 */
	public List<SideBarBean> querySideBar(String user_name) {
		List<SideBarBean> sideBarLogList = new ArrayList<SideBarBean>();
		String sql = "select id,user_name,is_show,position,title,picture,href,type,custom,mod_flag from netdisk_sidebar_setup where user_name ="
				+ StrUtil.sqlstr(user_name);
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int id = record.getInt("id");
				String userName = record.getString("user_name");
				int isShow = record.getInt("is_show");
				int position = record.getInt("position");
				String title = record.getString("title");
				String picture = record.getString("picture");
				String href = record.getString("href");
				int type = record.getInt("type");
				int custom = record.getInt("custom");
				int mod_flag = record.getInt("mod_flag");
				String authKey = userName + "|"
						+ DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
				// com.redmoon.oa.sso.Config cfg = new
				// com.redmoon.oa.sso.Config();
				// authKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg
				// .get("key"), authKey);
				authKey = cn.js.fan.security.ThreeDesUtil
						.encrypt2hex(
								com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY,
								authKey);
				SideBarBean sideBarBean = new SideBarBean(id, userName, isShow,
						position, title, picture, href, type, custom, authKey,
						mod_flag);
				sideBarLogList.add(sideBarBean);
			}
		} catch (SQLException e) {
			logger.error("querySideBar:" + e.getMessage());
		}
		return sideBarLogList;
	}

	/**
	 * sidebar_icon.jsp中选出长宽相等的图片
	 * 
	 * @param userName
	 * @return
	 */
	public ArrayList<Integer> getSidebarIcon(String userName) {
		ResultIterator ri = null;
		ArrayList<Integer> list = new ArrayList<Integer>();
		com.redmoon.clouddisk.Config cloudcfg = com.redmoon.clouddisk.Config
				.getInstance();
		String extType = cloudcfg.getProperty("exttype_1");
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = cfg.get("file_netdisk");
		String[] exts = extType.split(",");
		StringBuilder sb = new StringBuilder();
		for (String ext : exts) {
			sb.append(StrUtil.sqlstr(ext)).append(",");
		}
		if (sb.toString().endsWith(",")) {
			sb.deleteCharAt(sb.toString().length() - 1);
		}
		String sql = "select id from netdisk_document_attach where user_name="
				+ StrUtil.sqlstr(userName) + " and is_deleted = 0 and ext in"
				+ (sb.toString().equals("") ? "" : "(" + sb.toString() + ")")
				+ " order by version_date desc";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				Attachment att = new Attachment(id);
				File file = new File(Global.getRealPath() + file_netdisk + "/"
						+ att.getVisualPath() + "/" + att.getName());
				Image src = javax.imageio.ImageIO.read(file); // 构造Image对象
				int width = src.getWidth(null); // 得到源图宽
				int height = src.getHeight(null); // 得到源图长
				if (width == height) {
					list.add(id);
				}
			}
		} catch (Exception e) {
			logger.error("getSidebarIcon:" + e.getMessage());
		}
		return list;
	}

	/**
	 * 添加自定义图片 先判定是否存在个人自定义图片文件夹，再添加图片。
	 */
	public String diyImg(String userName, int fileId) {
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk");
		File f = new File(Global.getAppPath() + "/"
				+ "netdisk/images/appImages/" + userName);
		if (!f.exists()) {
			f.mkdirs();
		}
		Attachment att = new Attachment(fileId);
		String fileName = att.getName();
		//String name = fileName.substring(0, fileName.lastIndexOf("."));
		// System.out.println(name);
		String name = DateUtil.format(new Date(), "yyyyMMddHHmmssS");
		String name_default = name + "_default.png";
		File file = new File(filePath + "/" + att.getVisualPath() + "/"
				+ fileName);
		Image src = null;
		try {
			src = javax.imageio.ImageIO.read(file); // 构造Image对象
			BufferedImage tag = new BufferedImage(36, 36,
					BufferedImage.TYPE_INT_RGB);
			// String src = Global.getRealPath()+ "/" +
			// "netdisk/images/appImages/" + userName;
			tag.getGraphics().drawImage(src, 0, 0, 36, 36, null); // 绘制缩小后的图
			FileOutputStream out = new FileOutputStream(Global.getAppPath()
					+ "/" + "netdisk/images/appImages/" + userName + "/"
					+ name_default); // 输出到文件流
			/*
			 * JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			 * encoder.encode(tag); // JPEG编码 out.close();
			 * 
			 * out = new FileOutputStream(Global.getAppPath() + "/" +
			 * "netdisk/images/appImages/" + userName + "/" + name_move);
			 * encoder = JPEGCodec.createJPEGEncoder(out); encoder.encode(tag);
			 * out.close();
			 * 
			 * out = new FileOutputStream(Global.getAppPath() + "/" +
			 * "netdisk/images/appImages/" + userName + "/" + name_click);
			 * encoder = JPEGCodec.createJPEGEncoder(out); encoder.encode(tag);
			 */

			// default
			javax.imageio.ImageIO.write(tag, "png", out);
			out.close();

			// move
			out = new FileOutputStream(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/" + userName + "/" + name
					+ "_move.png");
			javax.imageio.ImageIO.write(tag, "png", out);
			out.close();

			// click
			out = new FileOutputStream(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/" + userName + "/" + name
					+ "_click.png");
			javax.imageio.ImageIO.write(tag, "png", out);
			out.close();
		} catch (Exception e) {
			logger.error("diyImg:" + e.getMessage());
		}
		// FileUtil.CopyFile(filePath+ "/" + att.getVisualPath() + "/" +
		// fileName , Global.getRealPath()+ "/" + "netdisk/images/appImages/" +
		// userName +"/"+ name_default);
		// FileUtil.CopyFile(filePath+ "/" + att.getVisualPath() + "/" +
		// fileName , Global.getRealPath()+ "/" + "netdisk/images/appImages/" +
		// userName +"/"+ name_move);
		// FileUtil.CopyFile(filePath+ "/" + att.getVisualPath() + "/" +
		// fileName , Global.getRealPath()+ "/" + "netdisk/images/appImages/" +
		// userName +"/"+ name_click);
		return name_default;
	}

	/**
	 * 清理冗余图片时，将_default,_move,_click图片类型转换清理
	 */
	public String imgTypeTurn(String srcString, String typeSrc,
			String typePorpose) {
		int z = srcString.lastIndexOf(".");
		String picB = srcString.substring(0, z); // 前缀文件名
		String ext = srcString.substring(z + 1); // 后缀类型
		int i = picB.lastIndexOf(typeSrc); // 将前缀的字段分开，从末尾开始
		String pic1 = picB.substring(0, i); // 取前缀的前端字符串
		srcString = pic1 + typePorpose + "." + ext; // 拼接需要的字符串
		return srcString;
	}

	/**
	 * 将个人自定义图片文件夹进行冗余清理（所有用户）
	 * 如果重复定义同一按钮的自定义图片，则之前的图片会产生冗余图片，这里用于清理。(管理员使用该方法，清理所有用户)
	 */
	public boolean cleanDiyImg() {
		boolean re = false;
		String sql = "SELECT user_name FROM netdisk_sidebar_setup group by user_name";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		File file = new File(Global.getAppPath() + "/"
				+ "netdisk/images/appImages/temporary");
		try {
			FileUtil.del(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/temporary");
			file.mkdirs();

			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				String userName = rr.getString(1);
				if(userName == null || userName.equals("")){
					continue;
				}
				File f1 = new File(Global.getAppPath() + "/"
						+ "netdisk/images/appImages/" + userName);
				File f2 = new File(Global.getAppPath() + "/"
						+ "netdisk/images/appImages/temporary/" + userName);
				f2.mkdirs();
				sql = "select picture from netdisk_sidebar_setup where user_name = "
						+ StrUtil.sqlstr(userName)
						+ " and position in (1,2,3,4,5,6,7,8,9,10,11,12)";
				ResultIterator ri1 = null;
				ri1 = jt.executeQuery(sql);
				while (ri1.hasNext()) {
					ResultRecord rr1 = (ResultRecord) ri1.next();
					String pic = rr1.getString(1);
					if (pic.startsWith(userName)) {
						String path1 = Global.getAppPath()
								+ "netdisk/images/appImages/" + pic;
						String path1Dec = Global.getAppPath()
								+ "netdisk/images/appImages/temporary/" + pic;
						FileUtil.CopyFile(path1, path1Dec);

						// pic = pic.replace("_default", "_move");
						pic = imgTypeTurn(pic, "_default", "_move");
						String path2 = Global.getAppPath()
								+ "netdisk/images/appImages/" + pic;
						String path2Dec = Global.getAppPath()
								+ "netdisk/images/appImages/temporary/" + pic;
						FileUtil.CopyFile(path2, path2Dec);

						pic = imgTypeTurn(pic, "_move", "_click");
						String path3 = Global.getAppPath()
								+ "netdisk/images/appImages/" + pic;
						String path3Dec = Global.getAppPath()
								+ "netdisk/images/appImages/temporary/" + pic;
						FileUtil.CopyFile(path3, path3Dec);
					}
				}

				FileUtil.del(Global.getAppPath() + "netdisk/images/appImages/"
						+ userName);
				UtilTools.copyDir(f2, f1);
			}
			FileUtil.del(Global.getAppPath()
					+ "netdisk/images/appImages/temporary");
			re = true;
		} catch (Exception e) {
			logger.error("cleanDiyImg:" + e.getMessage());
		}
		return re;
	}

	/**
	 * 将个人自定义图片文件夹进行冗余清理（个别用户）
	 * 如果重复定义同一按钮的自定义图片，则之前的图片会产生冗余图片，这里用于清理。
	 */
	public boolean cleanDiyImg(String userName) {
		boolean re = false;
		if( userName==null || userName.equals("")){
			return false;
		}
		String sql = "select picture from netdisk_sidebar_setup where user_name = "
				+ StrUtil.sqlstr(userName)
				+ " and position in (1,2,3,4,5,6,7,8,9,10,11,12)";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		File file = new File(Global.getAppPath() + "/"
				+ "netdisk/images/appImages/temporary");
		try {
			FileUtil.del(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/temporary");
			file.mkdirs();
			File f1 = new File(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/" + userName);
			File f2 = new File(Global.getAppPath() + "/"
					+ "netdisk/images/appImages/temporary/" + userName);
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				f2.mkdirs();
				String pic = rr.getString(1);
				if (pic.startsWith(userName)) {
					String path1 = Global.getAppPath()
							+ "netdisk/images/appImages/" + pic;
					String path1Dec = Global.getAppPath()
							+ "netdisk/images/appImages/temporary/" + pic;
					FileUtil.CopyFile(path1, path1Dec);

					// pic = pic.replace("_default", "_move");
					pic = imgTypeTurn(pic, "_default", "_move");
					String path2 = Global.getAppPath()
							+ "netdisk/images/appImages/" + pic;
					String path2Dec = Global.getAppPath()
							+ "netdisk/images/appImages/temporary/" + pic;
					FileUtil.CopyFile(path2, path2Dec);

					pic = imgTypeTurn(pic, "_move", "_click");
					String path3 = Global.getAppPath()
							+ "netdisk/images/appImages/" + pic;
					String path3Dec = Global.getAppPath()
							+ "netdisk/images/appImages/temporary/" + pic;
					FileUtil.CopyFile(path3, path3Dec);
				}
			}
			
			FileUtil.del(Global.getAppPath() + "netdisk/images/appImages/"
					+ userName);
			UtilTools.copyDir(f2, f1);

			FileUtil.del(Global.getAppPath()
					+ "netdisk/images/appImages/temporary");
			re = true;
		} catch (Exception e) {
			logger.error("cleanDiyImg:" + e.getMessage());
		}
		return re;
	}

	/**
	 * 如果修改图片 flag=1 如果修改其他 flag=2 如果都修改 flag=1 如果未修改 flag=0
	 */
	public int getFlag(String userName, int is_show, int position,
			String title, String picture, String href, int custom) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select is_show,title,picture,href,custom from netdisk_sidebar_setup where position="
				+ position;
		int flag = 0;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			if (ri.hasNext()) {
				rr = (ResultRecord) ri.next();
				String pic_old = rr.getString(3);
				if (!pic_old.equals(picture)) {
					flag = 1;
				} else {
					int show_old = rr.getInt(1);
					String title_old = rr.getString(2);
					String href_old = rr.getString(4);
					int custom_old = rr.getInt(5);
					if ((show_old != is_show) || (!title_old.equals(title))
							|| (!href_old.equals(href))
							|| (custom_old != custom)) {
						flag = 2;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("getFlag:" + e.getMessage());
		}
		return flag;
	}

	/**
	 * 根据username获取mod_flag =1 的所有图片路径
	 */
	public ArrayList<String> getPicture(String userName, boolean isAll) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select picture from netdisk_sidebar_setup where type=1 and user_name = "
				+ StrUtil.sqlstr(userName) + (isAll ? "" : " and mod_flag = 1");
		ArrayList<String> list = new ArrayList<String>();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			while (ri.hasNext()) {
				rr = (ResultRecord) ri.next();
				String pic = rr.getString(1);
				list.add(pic);

				int pos = pic.lastIndexOf("_default.");
				if (pos != -1) {
					String name = pic.substring(0, pos);
					String ext = pic.substring(pos + "_default.".length());

					String temp = name + "_move." + ext;
					list.add(temp);

					temp = name + "_click." + ext;
					list.add(temp);
				}
			}
		} catch (Exception e) {
			logger.error("getPicture:" + e.getMessage());
		}
		return list;
	}

	/**
	 * 返回侧边栏修改情况 返回0 无修改 返回1 有图片修改 返回2 有修改，但无图片修改
	 */
	public int getSideBarEdit(String userName) {
		JdbcTemplate jt = new JdbcTemplate();
		int flag = 0;
		int temp = 0;
		String sql = "select mod_flag from netdisk_sidebar_setup where user_name="
				+ StrUtil.sqlstr(userName);
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			while (ri.hasNext()) {
				rr = (ResultRecord) ri.next();
				int i = rr.getInt(1);
				if (i == 1) {
					flag = 1;
					return flag;
				} else if (i == 2) {
					temp = 1;
				}
			}
			if (temp == 1) {
				flag = 2;
			}
		} catch (Exception e) {
			logger.error("getSideBarEdit:" + e.getMessage());
		}
		return flag;
	}

	/**
	 * 操作后将mod_flag变回0
	 */
	public boolean returnFlag(String userName) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "update netdisk_sidebar_setup set mod_flag = 0 where user_name = "
				+ StrUtil.sqlstr(userName);
		boolean re = false;
		try {
			re = jt.executeUpdate(sql) >= 0 ? true : false;
		} catch (Exception e) {
			logger.error("getFlag:" + e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}
	
	/**
	 * @Description: 将所有为0的mod_flag设为2,用于服务端重启后,key更新后的本地html的同步
	 * @return
	 */
	public boolean syncAllFlag() {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "update netdisk_sidebar_setup set mod_flag = 2 where mod_flag = 0";
		boolean re = false;
		try {
			re = jt.executeUpdate(sql) >= 0 ? true : false;
		} catch (Exception e) {
			logger.error("getFlag:" + e.getMessage());
		} finally {
			jt.close();
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("update: " + e.getMessage());
			}
		}
		return re;
	}
}
