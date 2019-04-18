package com.redmoon.oa.tigase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.redmoon.oa.tigase.upload.commons.vo.FileType;

/**
 * @Description:
 * @author:
 * @Date: 2016-6-14下午04:38:02
 */
public class TigaseUtil {
	public static byte[] getFileBytes(File file) {
		if (!file.exists()) {
			return null;
		}
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			int bytes = (int) file.length();
			byte buffer[] = new byte[bytes];
			bis.read(buffer);
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static FileType getFileType(String ext) {
		Config cfg = new Config();
		String imageFilter = cfg.get("imageFilter");
		String videoFilter = cfg.get("videoFilter");
		String audioFilter = cfg.get("audioFilter");
		
		String[] images = imageFilter.split("\\,");
		if (images != null) {
			for (String filter : images) {
				if (filter.equals(ext)) {
					return FileType.Image;
				}
			}
		}
		
		String[] videos = videoFilter.split("\\,");
		if (videos != null) {
			for (String filter : videos) {
				if (filter.equals(ext)) {
					return FileType.Video;
				}
			}
		}
		
		String[] audios = audioFilter.split("\\,");
		if (audios != null) {
			for (String filter : audios) {
				if (filter.equals(ext)) {
					return FileType.Audio;
				}
			}
		}
		
		return FileType.Other;
	}
}
