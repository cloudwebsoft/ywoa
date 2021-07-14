package com.redmoon.oa.netdisk;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.Privilege;

public class PublicAttachmentMgr {

	public boolean upload(ServletContext application, HttpServletRequest request)
			throws ErrMsgException {
		CMSMultiFileUploadBean mfu = new CMSMultiFileUploadBean();
		mfu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null)
			mfu.setValidExtname(ext);

		int ret = 0;
		try {
			ret = mfu.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				throw new ErrMsgException(mfu.getErrMessage());
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass().getName()).error(
					"upload:" + e.getMessage());
		}

		if (ret == FileUpload.RET_SUCCESS) {
			String dirCode = mfu.getFieldValue("dirCode");
			PublicLeafPriv lp = new PublicLeafPriv(dirCode);
			Privilege privilege = new Privilege();
			if (!lp.canUserAppend(privilege.getUser(request))) {
				throw new ErrMsgException(SkinUtil.LoadString(request,
						"pvg_invalid"));
			}
			
			String userName = privilege.getUser(request);

			PublicLeaf lf = new PublicLeaf();
			lf = lf.getLeaf(dirCode);
			
			if (lf==null)
				throw new ErrMsgException("目录不存在!");

			String visualPath = StrUtil.getNullString(mfu
					.getFieldValue("filepath"));

			String FilePath = cfg.get("file_netdisk_public");
			if (!visualPath.equals(""))
				FilePath += "/" + visualPath;

			String attSavePath = Global.getRealPath() + FilePath + "/";

			mfu.setSavePath(attSavePath); // 取得目录

			LogUtil.getLog(getClass()).info(
					"attSavePath=" + attSavePath);

			File f = new File(attSavePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			// 检查上传的文件大小有没有超出磁盘空间
			Vector attachs = mfu.getFiles();
			if (attachs.size()==0)
				attachs = mfu.getAttachments();

			// 检查是不是按目录上传
			String upDirCode = "";
			String uploadDirName = StrUtil.getNullStr(mfu.getFieldValue("uploadDirName"));
			if (!uploadDirName.equals("")) {
				// 取得uploadDirName中的目录名称，检测是否已存在，如不存在，则创建该目录
				int index = uploadDirName.lastIndexOf("\\");
				String upDirName = uploadDirName.substring(index + 1);
				// 检查是否存在同名目录
				boolean isExist = false;
				Iterator irlf = lf.getChildren().iterator();
				while (irlf.hasNext()) {
					PublicLeaf child = (PublicLeaf) irlf.next();
					if (child.getName().equals(upDirName)) {
						upDirCode = child.getCode();
						isExist = true;
						break;
					}
				}
				// 如果不存在同名目录，则创建
				if (!isExist) {
					// 创建uploadDirName节点及物理目录
					PublicLeaf lfCh = new PublicLeaf();
					lfCh.setName(upDirName);
					upDirCode = Leaf.getAutoCode();
					lfCh.setCode(upDirCode);
					lfCh.setParentCode(lf.getCode());
					lfCh.setDescription(lf.getCode());
					lfCh.setType(PublicLeaf.TYPE_LIST);
					lf.AddChild(lfCh);
					String savePath = Global.getRealPath()
							+ cfg.get("file_netdisk_public") + "/"
							+ lfCh.getFilePath() + "/";
					// 检查物理目录是否存在，如果不存在，则创建
					// System.out.println(getClass() + savePath +
					// " cfg.get(\"file_netdisk\")=" +
					// cfg.get("file_netdisk"));
					f = new File(savePath);
					if (!f.isDirectory()) {
						f.mkdirs();
					}
				}
			}

			LogUtil.getLog(getClass()).info(
					"uploadDirName=" + uploadDirName + " att size="
							+ attachs.size() + " file size=" + mfu.getFiles().size());

			Iterator ir = attachs.iterator();
			while (ir.hasNext()) {
				FileInfo fi = (FileInfo) ir.next();

				String myVisualPath = visualPath;
				String savePath = mfu.getSavePath();
				String curDirCode = dirCode;

				LogUtil.getLog(getClass()).info(
						"fi.clientPath=" + fi.clientPath + " uploadDirName="
								+ uploadDirName + " savePath=" + savePath);

				if (!uploadDirName.equals("")) {
					// 为该文件在数据库及磁盘创建相应目录					
					// 检查目录是否包含于客户端路径中
					int p = fi.clientPath.indexOf(uploadDirName);
					if (p != -1) {
						// 在循环中，lf的child_count会变化，缓存会被刷新，因此在这里要重新获取
						lf = lf.getLeaf(upDirCode);
						
						curDirCode = lf.getCode();

						myVisualPath = lf.getFilePath();
						savePath = Global.getRealPath()
								+ cfg.get("file_netdisk_public") + "/"
								+ lf.getFilePath() + "/";

						// 取得upoadDirName后的路径
						String path = fi.clientPath.substring(p
								+ uploadDirName.length() + 1);
						
						// 检查path在树形结构上是否已存在，如果不存在，则创建目录节点，并创建物理目录
						String[] ary = path.split("\\\\");				

						PublicLeaf plf = lf;
						PublicLeaf lfCh = null;
						
						LogUtil.getLog(getClass()).info(
								"path=" + path + " ary.len=" + ary.length + " plf.getName=" + plf.getName());		
						
						// 数组中最后一位是文件名，因此不用处理
						for (int i = 0; i < ary.length - 1; i++) {
							// 检查在孩子节点中是否存在
							boolean isFound = false;
							Iterator irLf = plf.getChildren().iterator();
							while (irLf.hasNext()) {
								PublicLeaf lf2 = (PublicLeaf) irLf.next();
								LogUtil.getLog(getClass()).info(
										"lf2.getName()=" + lf2.getName()
												+ " ary[i]=" + ary[i]);
								if (lf2.getName().equals(ary[i])) {
									isFound = true;
									lfCh = lf2;
									plf = lf2;
									break;
								}
							}

							LogUtil.getLog(getClass()).info(
									"isFound=" + isFound);

							if (!isFound) {
								// 创建节点及物理目录
								lfCh = new PublicLeaf();
								lfCh.setName(ary[i]);
								lfCh.setCode(Leaf.getAutoCode());
								lfCh.setParentCode(plf.getCode());
								lfCh.setDescription(ary[i]);
								lfCh.setType(PublicLeaf.TYPE_LIST);

								plf.AddChild(lfCh);

								lfCh = lfCh.getLeaf(lfCh.getCode());

								plf = lfCh;
							}
							curDirCode = lfCh.getCode();
						}

						if (lfCh != null) {
							myVisualPath = lfCh.getFilePath();
							savePath = Global.getRealPath()
									+ cfg.get("file_netdisk_public") + "/"
									+ lfCh.getFilePath() + "/";
							
							LogUtil.getLog(getClass()).info("savePath=" + savePath + " curDirCode=" + curDirCode);							
							// 检查物理目录是否存在，如果不存在，则创建
							f = new File(savePath);
							if (!f.isDirectory()) {
								f.mkdirs();
							}
						}
					}
				}

				// LogUtil.getLog(getClass()).info("fi.getName()=" +
				// fi.getName() + " myDocId=" + myDocId + " docId=" + docId
				// + " savePath=" + savePath);

				fi.write(savePath, "");

				// 检查该目录下,是否已有同名文件,如果有,则不入库
				PublicAttachment att = new PublicAttachment();
				if (!att.isExist(fi.getName(), curDirCode)) {
					LogUtil.getLog(getClass()).info(
							"OK, File " + fi.getName() + " is not exist");

					att.setName(fi.getName());
					att.setDiskName(fi.getName());
					att.setVisualPath(myVisualPath);
					att.setSize(fi.getSize());
					att.setExt(fi.getExt());
					att.setUserName(userName);
					att.setPublicDir(curDirCode);
					att.create();
				}
			}
			return true;
		} else
			return false;
	}
	
	public boolean commonUpload(ServletContext application, HttpServletRequest request)
	throws ErrMsgException {
		boolean flag = true;
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data") == -1) {
			throw new IllegalStateException(
					"The content type of request is not multipart/form-data");
		}
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		FileUpload fu = new FileUpload();
		String[] extAry = exts.split(",");
		fu.setValidExtname(extAry);
		fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
		int ret = -1;
		try {
			ret = fu.doUpload(application, request);
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		}
		
		flag  = writeFile(request, fu);
		return flag;
	}
	
	public boolean writeFile(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
		boolean flag = true;
		String dirCode = ParamUtil.get(request,"dirCode");
		PublicLeaf pl = new PublicLeaf(dirCode);
		if( pl!=null && pl.isLoaded()){
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String myVisualPath = pl.getFilePath();
			String savePath = Global.getRealPath()
					+ cfg.get("file_netdisk_public") + "/"
					+ myVisualPath + "/";
			//如果没有物理文件夹创建
			File f = new File(savePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			Privilege privilege = new Privilege();
	       if (fu.getRet() == FileUpload.RET_SUCCESS) {
	            Vector v = fu.getFiles();
	            Iterator ir = v.iterator();
	            // 置路径
	            fu.setSavePath(savePath);
	            if (ir.hasNext()) {
	                FileInfo fi = (FileInfo) ir.next();
	                // 使用随机名称写入磁盘
	                fi.write(fu.getSavePath(), false);
	                PublicAttachment att = new PublicAttachment();
					if (!att.isExist(fi.getName(), dirCode)) {
						LogUtil.getLog(getClass()).info(
								"OK, File " + fi.getName() + " is not exist");

						att.setName(fi.getName());
						att.setDiskName(fi.getName());
						att.setVisualPath(myVisualPath);
						att.setSize(fi.getSize());
						att.setExt(fi.getExt());
						att.setUserName(privilege.getUser(request));
						att.setPublicDir(dirCode);
						flag &= att.create();
					}
	            }
	            
	        }else{
	        	flag = false;
	        }
			
		}else{
			flag = false;
		}

        return flag;
    }
    /**
     * 发布个人网络硬盘中的文件
     * @param netdiskAttId
     * @return
     */
    public boolean share(HttpServletRequest request, String publicDir, int netdiskAttId, boolean isLink) throws ErrMsgException {
    	Privilege privilege = new Privilege();
		PublicLeafPriv plp = new PublicLeafPriv(publicDir);
		if (plp.canUserAppend(privilege.getUser(request)) || plp.canUserManage(privilege.getUser(request)))
			;
		else
			throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
			PublicAttachment patt = new PublicAttachment();
			Attachment att = new Attachment();
	    	att = att.getAttachment(netdiskAttId);
    	if (isLink){
    		if (patt.isAttLinkShareExist(publicDir, netdiskAttId))
    			throw new ErrMsgException("文件已被发布!");
    		String newName = changeSameName(att.getName(),publicDir);
	    	patt.setAttId(netdiskAttId);
	    	patt.setUserName(att.getUserName());
	    	patt.setSize(att.getSize());
	    	patt.setExt(att.getExt());
	    	patt.setPublicDir(publicDir);
			patt.setName(newName);
			patt.setDiskName(att.getDiskName());
			patt.setVisualPath(att.getVisualPath());
    	}else {
    		PublicLeaf publf = new PublicLeaf();
    		publf = publf.getLeaf(publicDir);
    		
    		//先拷贝
    		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();    		
			String file_netdisk = cfg.get("file_netdisk");
			String file_netdisk_public = cfg.get("file_netdisk_public");
    		String src = Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName();
    		String dest = Global.getRealPath() + file_netdisk_public + "/" + publf.getFilePath() + "/" + att.getDiskName();
    		String newName = changeSameName(att.getName(),publicDir);
    		//重命名
    		if(!newName.equals(att.getName())){
    			FileUtil.CopyFile(src, Global.getRealPath() + file_netdisk_public + "/" + publf.getFilePath() + "/"+newName);
        		patt.setDiskName(newName);
        		patt.setName(newName);
    		}else{
    			FileUtil.CopyFile(src, dest);
        		patt.setDiskName(att.getDiskName());
        		patt.setName(att.getDiskName());
    		}
	    	patt.setPublicDir(publicDir);
    		patt.setVisualPath(publf.getFilePath());
	    	patt.setUserName(att.getUserName());
	    	patt.setSize(att.getSize());
	    	patt.setExt(att.getExt());		
    	}
    	return patt.create();
    }
    /**
     * copy 同名文件时 文件名+1
     * @param name
     * @param publicDir
     * @return
     */
    public String changeSameName(String name,String publicDir){
    	//copy附件，可以拷贝多次
		String sql = "select id from netdisk_public_attach where public_dir="+StrUtil.sqlstr(publicDir)+" order by create_date desc";
		PublicAttachment publicAtt = new PublicAttachment();
    	Vector attVec = publicAtt.list(sql);
		String newChName = name;
		Iterator ir;
		int k = 1;
		while (true) {
			ir = attVec.iterator();
			boolean isFound = false;
			while (ir.hasNext()) {
				PublicAttachment pa = (PublicAttachment) ir.next();
				if (pa.getName().equals(newChName)) {
					isFound = true;
					int index = name.lastIndexOf(".");
					String ext = name.substring(name.lastIndexOf(".") + 1);
					newChName = name.substring(0,index) + "(" + k + ")"+"."+ext;
					k++;
				}
			}
			if (!isFound)
				break;
		}
		return newChName;
    }
    
    public boolean uploadOffice(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        FileUpload fu = new FileUpload();
        fu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
        String[] ext = StrUtil.split(exts, ",");
        if (ext!=null)
            fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage());
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("uploadOffice:" + e.getMessage());
        }

        if (ret == FileUpload.RET_SUCCESS) {
            String strId = fu.getFieldValue("id");

            if (!StrUtil.isNumeric(strId))
                throw new ErrMsgException("id 必须为数字");

            PublicAttachment att = new PublicAttachment();
            att = att.getPublicAttachment(StrUtil.toInt(strId));
            PublicLeafPriv lp = new PublicLeafPriv(att.getPublicDir());
            Privilege privilege = new Privilege();
            if (!lp.canUserModify(privilege.getUser(request))) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "pvg_invalid"));
            }
            // 目前并不允许直接修改链接的文件,在netdisk_public_attach_list.jsp中不允许编辑
            if (att.getAttId()!=0) {
            	Attachment at = new Attachment();
            	at = at.getAttachment(att.getAttId());
	            String file_netdisk = cfg.get("file_netdisk");
	            com.redmoon.kit.util.FileInfo fi = (com.redmoon.kit.util.FileInfo)(fu.getFiles().get(0));
	            return fi.write(Global.getRealPath() + file_netdisk + "/" + at.getVisualPath() + "/", at.getDiskName());	            
            }
            else {
	            String file_netdisk_public = cfg.get("file_netdisk_public");
	            com.redmoon.kit.util.FileInfo fi = (com.redmoon.kit.util.FileInfo)(fu.getFiles().get(0));
	            return fi.write(Global.getRealPath() + file_netdisk_public + "/" + att.getVisualPath() + "/", att.getDiskName());
            }
        }
        else
            return false;
    }
    /**
     * 附件列表
     * @param dirCode
     * @param request
     * @return
     */
    public Vector getAttachmentList(String dirCode,HttpServletRequest request){
    	
    	String sql = "select id from netdisk_public_attach where 1=1";
    
    	String op = ParamUtil.get(request, "op");
    	String nameStr = ParamUtil.get(request, "select_content");
    	String select_file = ParamUtil.get(request, "select_file");
    	String select_sort = ParamUtil.get(request,"select_sort");
    	
    	
    	if( !op.trim().equals("")){
    		if(op.equals("search")){
    			if(select_sort.equals("select_one")) {
    				sql += " and name like " + StrUtil.sqlstr("%" + nameStr + "%");
    			}
    			if(select_file.equals("select_file")){
    				com.redmoon.clouddisk.Config rcfg = com.redmoon.clouddisk.Config.getInstance();
    				int which = ParamUtil.getInt(request,"select_which",1);
    				String extType = rcfg.getProperty("exttype_" + which);
    				String[] extArr = extType.split(",");
    				StringBuffer sb = new StringBuffer();
    				if(extArr != null && extArr.length >0) {
    					for (String ext : extArr) {
    						sb.append(StrUtil.sqlstr(ext)).append(",");
    					}
    					String sbExt = sb.substring(0,sb.lastIndexOf(","));
    					sql += " and  ext in("+sbExt+")";
    				}
    			}
    		
        	}
    		
    	}else{
    		if( dirCode != null && !dirCode.trim().equals("")){
        		sql += " and public_dir=" + StrUtil.sqlstr(dirCode);
        	}
    	}
    	sql += " order by create_date desc";
    	PublicAttachment publicAtt = new PublicAttachment();
    	Vector attVec = publicAtt.list(sql);
    	return attVec;
    }
   
    /**
     * 得到预览图的大路径
     * @param attId
     * @return
     * @throws JSONException 
     * @throws IOException 
     */
	public JSONObject getImgSrc(PublicAttachment att) throws JSONException, IOException{
		//公共共享附件预览
		String imgSrc = "";
		String downloadUrl = "";
		Config config = new Config();
		if(att.getAttId()==0){
			imgSrc = Global.getRealPath()+ "/" + config.get("file_netdisk_public")
			+ "/"+att.getVisualPath()+"/"+att.getName();
			downloadUrl = "netdisk_public_downloadfile.jsp?id="+att.getId();
		}else{
			//只复制链接的情况下
			Attachment attach = new Attachment(att.getAttId());
			imgSrc = Global.getRealPath()+ "/" + config.get("file_netdisk")
			+ "/"+attach.getVisualPath()+"/"+attach.getDiskName();
			downloadUrl = "getfile.jsp?op=1&id="+attach.getId();
		}
		File input = new File(imgSrc);
		JSONObject jsonObject = new JSONObject();
		try {
			BufferedImage image = ImageIO.read(input);
			if (image == null) {
				jsonObject.put("ret",0);
				jsonObject.put("msg", "图片不存在！");
			} else {
				int w = image.getWidth();
				int h = image.getHeight();
				jsonObject.put("ret", 1);
				jsonObject.put("width", w);
				jsonObject.put("height", h);
				jsonObject.put("downloadUrl",downloadUrl);
				jsonObject.put("alt",att.getName());
			}
		}catch (JSONException e) {
			// TODO: handle exception
			Logger.getLogger(PublicAttachmentMgr.class).error("getImgSrc:"+e.getMessage());
			throw new JSONException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(PublicAttachmentMgr.class).error("getImgSrc IOException:"+e.getMessage());
			throw new IOException(e.getMessage());
		}
		return jsonObject;
	}
	
	// 预览图片左右翻(public)
	public int showNextImg(int attId, String arrow) {
		PublicAttachment att = new PublicAttachment(attId);
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		String exts = cfg.getProperty("exttype_1");
		String[] ss = exts.split(",");
		String newExtFirst = "";
		String newExtElse = "";
		int temp = 0;
		int temp2 = 0;
		for (int i = 0; i < ss.length; i++) {
			if (i == 0) {
				newExtFirst = "'" + ss[i] + "'";
			} else {
				newExtElse += ",'" + ss[i] + "'";
			}
		}
		String sql = "select id from netdisk_public_attach where public_dir = "
				+ StrUtil.sqlstr(att.getPublicDir())
				+ " and ext in ("
				+ newExtFirst + newExtElse + ")";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.getRows() == 1) {
				return attId;
			}
			int index = 0;
			boolean findDirect = false;
			boolean isNextId = false;
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				if (arrow.equals("left")) {
					// id与当前预览id一致时
					if (attId == id) {
						if (index == 0) {
							// 当前图片为第一张时，返回最后一张图片的id
							findDirect = true;
							continue;
						} else {
							// 其他情况返回前一张图片的id
							return temp;
						}
					}
					// 当前id为第一个时，直接去找最后一张图片的id
					if (findDirect) {
						if (!ri.hasNext()) {
							return id;
						}
					} else {
						temp = id;
						index++;
					}
				} else {
					// id与当前预览id一致时
					if (attId == id) {
						if (!ri.hasNext()) {
							// 当前图片为最后一张图片时，返回第一张图片的id
							return temp;
						} else {
							// 其他情况返回后一张图片的id
							isNextId = true;
							continue;
						}
					}
					if (isNextId) {
						return id;
					} else {
						// 记录第一张图片的id
						if (index++ == 0) {
							temp = id;
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.getLogger(PublicAttachmentMgr.class).error("showLeftImg:" + e.getMessage());
		} finally {
			jt.close();
		}
		return temp2;
	}
	
	/**
	 * 文本预览
	 * @param att
	 * @return
	 */
	public String getTxtInfo(PublicAttachment att){
		Config configPath = new Config();
		String txtPath = "";
		String txtResult = "";
		try{
			if(att.getAttId()==0){
				txtPath = Global.getRealPath() + configPath.get("file_netdisk_public")+ "/"+att.getVisualPath()+"/"+att.getName();
			}else{
				//非链接情况
				Attachment attach = new Attachment(att.getAttId());
				txtPath = Global.getRealPath() + configPath.get("file_netdisk")+ "/"+attach.getVisualPath()+"/"+attach.getDiskName();
			}
			txtResult = UtilTools.getTextFromTxt(txtPath);
			return txtResult;
		}catch(Exception e){
			LogUtil.getLog(getClass().getName()).error(
					":" + e.getMessage());
		}
		return txtResult;
	}
	
	/**
	 * 重命名 公共文件夹附件
	 * @param attName
	 * @param attId
	 * @return
	 * @throws JSONException 
	 */
	public JSONObject changePublicAttName(int id,String attName) throws JSONException{
		JSONObject jsonObj = new JSONObject();
		String ext = attName.substring(attName.lastIndexOf(".") + 1);
		PublicAttachment publicAtt = new PublicAttachment(id);
		if(isExistAttInDir(attName, publicAtt.getPublicDir())){
			publicAtt.setName(attName);
			String diskName = "";
			if(publicAtt.getAttId() == 0){
				diskName = publicAtt.getDiskName();
				publicAtt.setDiskName(attName);
			}
			publicAtt.setExt(ext);
			boolean flag = true;
			flag = publicAtt.save();
			//不是分享链接的情况下
			if(publicAtt.getAttId() == 0){
				String visualPath = publicAtt.getVisualPath();
				if(flag){
					Config config = new Config();
					String filePath = Global.getRealPath()
							+ config.get("file_netdisk_public") + "/";
					String path = "";
					if(visualPath!=null && !visualPath.trim().equals("")){
						path = filePath+"/"+visualPath+"/";
					}else{
						path = filePath + "/";
					}
					File file = new File(path+diskName);
					if (file.exists()) {
						flag = file.renameTo(new File(path + "/"
								+ attName));
						if (file.exists()) {
							flag = file.delete();
						}
					}
				}
			}
			try {
					if(flag){
						jsonObj.put("result",1);
						jsonObj.put("imgSrc",Attachment.getIcon(ext));
						jsonObj.put("extType",UtilTools.getConfigType(ext));
					}else{
						jsonObj.put("result",-1);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(getClass().getName()).error(
							":" + e.getMessage());
					throw new JSONException(e.getMessage());
				}
		
		}else{
			jsonObj.put("result",0);//同名文件
		}
		return jsonObj;
		
	}
	/**
	 * 移动公共共享附件
	 * @param attId
	 * @param publicDir
	 * @return
	 * @throws JSONException
	 */
	public JSONObject movePublicAtt(int attId,String publicDirCode) throws JSONException{
		JSONObject jsonObj = new JSONObject();
		boolean flag = false;
		try {
			PublicAttachment pa = new PublicAttachment(attId);
			if(isExistAttInDir(pa.getName(),publicDirCode)){
				//copy附件
				if(pa.getAttId()==0){
					String oldVisualPath = pa.getVisualPath();//被拷贝文件夹 
					PublicLeaf pl = new PublicLeaf(publicDirCode);
					String visualPath = pl.getFilePath();//拷贝文件夹
					pa.setPublicDir(publicDirCode);
					pa.setVisualPath(visualPath);
					flag = pa.save();
					if(flag){
						jsonObj.put("result",true);
						Config config = new Config();
						String filePath = Global.getRealPath()
								+ config.get("file_netdisk_public");
						String oldPath = "";
						String newPath = "";
						if(oldVisualPath != "" && !oldVisualPath.trim().equals("")){
							oldPath = filePath+"/"+oldVisualPath+"/"+pa.getDiskName();
						}else{
							oldPath = filePath+"/"+pa.getDiskName();
						}
						if(visualPath != "" && !visualPath.trim().equals("")){
							newPath = filePath+"/"+visualPath+"/"+pa.getDiskName();
						}else{
							newPath = filePath+"/"+pa.getDiskName();
						}
						flag = FileUtil.CopyFile(oldPath,newPath);
						if(flag){
							File file = new File(oldPath);
							if(file.exists()){
								flag = file.delete();
							}
							if(flag){
								jsonObj.put("result",1);
							}else{
								jsonObj.put("result",-1);
							}
							
						}else{
							jsonObj.put("result",-1);
						}
					}else{
						jsonObj.put("result",-1);
					}
					
				}else{//发布链接
					pa.setPublicDir(publicDirCode);
					flag = pa.save();
					if(flag){
						jsonObj.put("result",1);
					}else{
						jsonObj.put("result",-1);
					}
					
				}
			}else{
				jsonObj.put("result",0);//存在同名文件
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass().getName()).error(
					":" + e.getMessage());
			throw new JSONException(e.getMessage());
		} 
		return jsonObj;
		
	}
	/**
	 * 判断某个文件夹下 是否有附件
	 * @param attName
	 * @param dirCode
	 * @return
	 */
	public boolean isExistAttInDir(String attName,String dirCode){
		boolean flag = true;
		String sql = "SELECT count(id) FROM netdisk_public_attach where name="+StrUtil.sqlstr(attName)+" and public_dir="+StrUtil.sqlstr(dirCode);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord record = null;
		try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				record = (ResultRecord)ri.next();
				int count = record.getInt(1);
				if(count>0){
					flag = false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
}
