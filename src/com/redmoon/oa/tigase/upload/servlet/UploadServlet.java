package com.redmoon.oa.tigase.upload.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.CloudConfig;
import com.redmoon.oa.tigase.Config;
import com.redmoon.oa.tigase.TigaseUtil;
import com.redmoon.oa.tigase.upload.commons.vo.FileType;
import com.redmoon.oa.tigase.upload.commons.vo.UploadItem;

public class UploadServlet extends TigaseServlet {

	/**
	 * @Description:
	 * @return
	 */
	@Override
	public String execute() {
		JSONObject json = new JSONObject();
		File[] files = getFile1();
		if (files == null || files.length == 0) {
			json.put("resultCode", 1010101);
			json.put("resultMsg", "缺少上传文件");
		} else if (getUserId() == null || getUserId().equals("")
				|| getAccess_token() == null || getAccess_token().equals("")) {
			json.put("resultCode", 1010101);
			json.put("resultMsg", "缺少请求参数");
		} else {
			int year = DateUtil.getYear(new Date());
			int month = DateUtil.getMonth(new Date()) + 1;
			Config cfg = new Config();
			String vpath = cfg.get("fileTigase") + "/" + year + "/" + month
					+ "/";
			String filepath = Global.getRealPath() + vpath;
			File file = new File(filepath);
			if (!file.exists()) {
				file.mkdirs();
			}

			List<UploadItem> images = new ArrayList<UploadItem>();
			List<UploadItem> audios = new ArrayList<UploadItem>();
			List<UploadItem> videos = new ArrayList<UploadItem>();
			List<UploadItem> others = new ArrayList<UploadItem>();
			FileOutputStream out = null;
			UploadItem uploadItem = null;
			int successCount = 0;
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				String oName = getFile1FileName()[i];
				String ext = StrUtil.getFileExt(oName);
				String diskName = FileUpload.getRandName() + "." + ext;
				FileType fileType = TigaseUtil.getFileType(ext);
				try {
					out = new FileOutputStream(filepath + diskName);
					FileInputStream in = new FileInputStream(f);
					byte buffer[] = new byte[1024 * 10];
					int length = 0;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					out.close();
					String urlPath = vpath + diskName;
					CloudConfig cloudConfig = CloudConfig.getInstance();
					String skey = getUserId() + "|" + "OA" + "|"
							+ (new Date()).getTime();
					skey = ThreeDesUtil.encrypt2hex(cloudConfig
							.getProperty("key"), "path=" + urlPath + "&skey="
							+ skey);
					uploadItem = new UploadItem(oName, Global.getFullRootPath()
							+ "/public/tigase_getfile.jsp?skey=" + skey,
							(byte) 1, null);
					successCount++;
				} catch (IOException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					uploadItem = new UploadItem(oName, null, (byte) 0, e
							.getMessage());
				}

				if (fileType == FileType.Image) {
					images.add(uploadItem);
				} else if (fileType == FileType.Video) {
					videos.add(uploadItem);
				} else if (fileType == FileType.Audio) {
					audios.add(uploadItem);
				} else {
					others.add(uploadItem);
				}
			}

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("images", images);
			data.put("audios", audios);
			data.put("videos", videos);
			data.put("others", others);

			json.put("resultCode", 1);
			json.put("data", data);
			json.put("total", files.length);
			json.put("success", successCount);
			json.put("failure", files.length - successCount);
		}

		setResult(json.toString());

		return "SUCCESS";
	}

}
