package cn.js.fan.util.file;

import java.io.*;
import java.util.zip.*;
import cn.js.fan.util.*;

public class Compress {
  public static void gzipFile(String from, String to) throws IOException {
    FileInputStream in = new FileInputStream(from);
    ZipEntry entry = new ZipEntry(from);
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(to));
    out.putNextEntry(entry);
    byte[] buffer = new byte[4096];
    int bytes_read;
    while ( (bytes_read = in.read(buffer)) != -1)
      out.write(buffer, 0, bytes_read);
    in.close();
    out.close();
  }

  public static void zipDirectory(String dir, String zipfile) throws
      IOException, IllegalArgumentException {
	  ZipOutputStream out = null;
	  try{
		  out = new ZipOutputStream(new FileOutputStream(zipfile));
		  zipDir(out, dir);
	  }catch(Exception ex){
		  
	  }finally{
		  if (out != null){
			  out.close();
		  }
	  }
  }

  public static void zipDir(ZipOutputStream out, String dir) throws IOException,
      IllegalArgumentException {
    File d = new File(dir);
    if (!d.isDirectory()) {
      byte[] buffer = new byte[4096];
      int bytes_read;
      FileInputStream in = new FileInputStream(d);
      ZipEntry entry = new ZipEntry(StrUtil.UTF8ToUnicode(dir));//dir);//中文转换有问题
      out.putNextEntry(entry);
      while ( (bytes_read = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytes_read);
      }
      in.close();
      return;
    }

    String[] entries = d.list();
    String path = d.getPath();
    for (int i = 0; i < entries.length; i++) {
      zipDir(out, path + "/" + entries[i]);
    }
  }
/*
  public static class Test {
    public static void main(String args[]) throws IOException {
      if ( (args.length != 1) && (args.length != 2)) { // check arguments
        System.err.println("Usage: java Compress$Test <from> [<to>]");
        System.exit(0);
      }
      String from = args[0], to;
      File f = new File(from);
      boolean directory = f.isDirectory(); // Is it a file or directory?
      if (args.length == 2)
        to = args[1];
      else { // If destination not specified
        if (directory)
          to = from + ".zip"; //   use a .zip suffix
        else
          to = from + ".gz"; //   or a .gz suffix
      }

      if ( (new File(to)).exists()) { // Make sure not to overwrite
        System.err.println("Compress: won't overwrite existing file: " +
                           to);
        System.exit(0);
      }

      if (directory)
        Compress.zipDirectory(from, to);
      else
        Compress.gzipFile(from, to);
    }
  }
*/
}
