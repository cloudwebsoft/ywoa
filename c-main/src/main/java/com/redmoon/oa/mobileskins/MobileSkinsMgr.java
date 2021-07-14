package com.redmoon.oa.mobileskins;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import java.io.File;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.Paginator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ParamConfig;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;

public class MobileSkinsMgr {
	private final static int PAGESIZE = 10;
	private String updateIsUsedStatus = "update mobile_skins set is_used=0";
	private final static String VISUAL_PATH = "upfile/mobile_skins/";
	private final static String FILE_PAHT = Global.getRealPath() + VISUAL_PATH;
	private final static String FORMCODE = "mobile_skins_create";
	/**
	 * 修改上传皮肤 更新文件 disk_name 以及visul_path 更新成功后  删除原来文件
	 * @param application
	 * @param request
	 * @param code
	 * @param is_used
	 * @param isNewFile
	 * @param name
	 * @return
	 */
	public boolean save(ServletContext application, HttpServletRequest request,
			String code, int is_used, int isNewFile, String name) {
		boolean flag = false;
		MobileSkinsDb mobileSkinsDb = new MobileSkinsDb();
		mobileSkinsDb = getMobileSkinsDb(code);
		if (isNewFile == 1) {
			String contentType = request.getContentType();
			if (contentType.indexOf("multipart/form-data") == -1) {
				throw new IllegalStateException(
						"The content type of request is not multipart/form-data");
			}
			FileUpload fu = new FileUpload();
			String[] extAry = { "rar", "zip" };
			fu.setValidExtname(extAry);
			fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
			int ret = -1;
			try {
				ret = fu.doUpload(application, request);
				if (ret == FileUpload.RET_SUCCESS) {
					String disk_name = mobileSkinsDb.writeFile(request, fu,
							FILE_PAHT);
					if (disk_name.equals("")) {
						throw new ErrMsgException("请上传图片！");
					}
					int version = mobileSkinsDb.getInt("version") + 1;
					if(is_used == 1){
						  if(mobileSkinsDb.updateIsUsedStatus(updateIsUsedStatus)){
							  boolean result = saveMobileSkinsDb(name, is_used,
										disk_name, VISUAL_PATH + disk_name, version, code);
								if (result) {
									File file = new File(Global.getRealPath()
											+ mobileSkinsDb.getString("visual_path"));
									if (file.delete()) {//删除原来上传的文件
										flag = true;
									}
								}
						  }
						}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {//如果没有上传文件的话，只需要修改名称后是否选中,版本号 不需要改变
			if(is_used == 1){
				if(mobileSkinsDb.updateIsUsedStatus(updateIsUsedStatus)){
					boolean result = saveMobileSkinsDb(name, is_used,
							mobileSkinsDb.getString("disk_name"), mobileSkinsDb.getString("visual_path"),mobileSkinsDb.getInt("version"), code);
					if(result){
						flag = true;
					}
				}
			}else{
				boolean result = saveMobileSkinsDb(name, is_used,
						mobileSkinsDb.getString("disk_name"), mobileSkinsDb.getString("visual_path"),mobileSkinsDb.getInt("version"), code);
				if(result){
					flag = true;
				}
			}
		}
		return flag;
	}

	/**
	 * 上传皮肤文件
	 * 
	 * @param application
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean create(ServletContext application, HttpServletRequest request)
			throws ErrMsgException {
		String disk_name = "";
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data") == -1) {
			throw new IllegalStateException(
					"The content type of request is not multipart/form-data");
		}
		FileUpload fu = new FileUpload();
		String[] extAry = { "rar", "zip" };
		fu.setValidExtname(extAry);
		fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
		int ret = -1;
		try {
			ret = fu.doUpload(application, request);
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		}
		if (ret != FileUpload.RET_SUCCESS) {
			throw new ErrMsgException(fu.getErrMessage(request));
		}
		boolean re = false;
		MobileSkinsDb skinDb = new MobileSkinsDb();
		ParamConfig pc = new ParamConfig(skinDb.getTable()
				.getFormValidatorFile());
		ParamChecker pck = new ParamChecker(request, fu);
		try {
			disk_name = skinDb.writeFile(request, fu, FILE_PAHT);
			pck.doCheck(pc.getFormRule(FORMCODE)); // "regist"));
			pck.setValue("code", "编号", RandomSecquenceCreator.getId(20));
			pck.setValue("disk_name", "文件名", disk_name);
			pck.setValue("visual_path", "文件存放路径", VISUAL_PATH + disk_name);
			pck.setValue("version", "版本", 1);
			if (disk_name.equals(""))
				throw new ErrMsgException("请上传图片！");
		} catch (CheckErrException e) {
			// 如果onError=exit，则会抛出异常
			throw new ErrMsgException(e.getMessage());
		}
		try {
			boolean flag = false;
			// 最新上传的皮肤为使用中，更新之前上传的皮肤状态为未使用，并保存新皮肤
			// 未使用，占不更新
			Integer is_used = (Integer) pck.getValue("is_used");
			if (is_used.intValue() == 1) {
				flag = skinDb.updateIsUsedStatus(updateIsUsedStatus);
				if (flag) {
					JdbcTemplate jt = new JdbcTemplate();
					re = skinDb.create(jt, pck);

				}
			} else {
				JdbcTemplate jt = new JdbcTemplate();
				re = skinDb.create(jt, pck);
			}
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		return re;
	}

	/**
	 * 手机端 换肤列表
	 * 
	 * @param curPage
	 * @param pageSize
	 * @return
	 */
	public ListResult queryAllSkinsMobilByList(int curPage, int pageSize) {
		MobileSkinsDb skinDb = new MobileSkinsDb();
		String sql = "SELECT code,version,name,is_used FROM  mobile_skins ORDER BY code DESC";
		try {
			ListResult listResult = skinDb.listResult(sql, curPage, pageSize);
			return listResult;
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 删除皮肤 1,采用逻辑删除保留服务器中文件 并保留历史皮肤(优化考虑) 2，目前采用真实删除
	 * 
	 * @param code
	 * @return
	 */
	public boolean deleteMobileSkinsByCode(String code) {
		boolean flag = false;
		MobileSkinsDb mobileSkinDb = new MobileSkinsDb();
		
		mobileSkinDb = mobileSkinDb.getMobileSkinsDb(code);
		String file_path = Global.getRealPath()
				+ mobileSkinDb.getString("visual_path");
		try {
			boolean result = mobileSkinDb.del();
			if (result) {
				File file = new File(file_path);
				flag = file.delete();
			}
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 批量删除多套皮肤
	 * 
	 * @param codes
	 * @return
	 */
	public boolean deleteMobileSkinsByBatch(String codes) {
		String[] idsInfo = StrUtil.split(codes, ",");
		int length = idsInfo.length;
		boolean flag = true;
		if (codes == null || codes.trim().equals("") || length <= 0) {
			flag = false;
			return flag;
		}
		for (String id : idsInfo) {
			boolean flag2 = deleteMobileSkinsByCode(id);
			if (flag2 == false) {
				flag = false;
			}
		}
		return flag;
	}

	/**
	 * 根据code 返回 MobileSkinsDb的QobjectDb对象
	 * 
	 * @param code
	 * @return
	 */
	public MobileSkinsDb getMobileSkinsDb(String code) {
		MobileSkinsDb mobileSkinsDb = new MobileSkinsDb();
		return mobileSkinsDb.getMobileSkinsDb(code);
	}

	/**
	 * 更新换肤
	 * 
	 * @param is_used
	 * @param disk_name
	 * @param visual_path
	 * @param version
	 * @param mobileSkinsDb
	 * @return
	 */
	public boolean saveMobileSkinsDb(String name, int is_used,
			String disk_name, String visual_path, int version, String code) {
		boolean flag = false;
		MobileSkinsDb mobileSkinsDb = new MobileSkinsDb();
		mobileSkinsDb = getMobileSkinsDb(code);
		mobileSkinsDb.set("name", name);
		mobileSkinsDb.set("version", version);
		mobileSkinsDb.set("is_used", String.valueOf(is_used));
		mobileSkinsDb.set("visual_path", visual_path);
		mobileSkinsDb.set("disk_name", disk_name);
		try {
			flag = mobileSkinsDb.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
}
