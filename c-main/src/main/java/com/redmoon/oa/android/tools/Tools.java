package com.redmoon.oa.android.tools;

public class Tools {

	/**
	 * 根据文件类型取得其图标
	 * @param ext
	 * @return
	 */
	public static String getIcon(String ext){
		String _fname = "";
		if("jpe,jpeg,gif,bmp,jpg,png".indexOf(ext) != -1){
			_fname = "png.png";
		}else if("doc,docx,wps".indexOf(ext) != -1){
			_fname = "word.png";
		}else if("xls,xlsx".indexOf(ext) != -1){
			_fname = "excel.png";
		}else if("pdf".indexOf(ext) != -1){
			_fname = "pdf.png";
		}else if("ppt,pptx".indexOf(ext) != -1){
			_fname = "ppt.png";
		}else if("txt".indexOf(ext) != -1){
			_fname = "txt.png";
		}else if("avi,mov,asf,wmv,3gp,mp4,rmvb,flv".indexOf(ext)!= -1){
			_fname = "mp4.png";
		}else if("mp3,wma".indexOf(ext) !=-1){
			_fname = "mp3.png";
		}else if("zip,rar".indexOf(ext) !=-1){
			_fname = "zip.png";
		}else{
			_fname = "other.png";
		}
		return _fname;
	}
}
