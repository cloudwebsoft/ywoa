package cn.js.fan.util.net;

import com.cloudwebsoft.framework.util.LogUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TrackBack {
    String title = "";
    String referer = "";

    public TrackBack() {
        
    }

    public String getReferer(HttpServletRequest request) {
        referer = request.getHeader("referer");
        return referer;
   }

    public String getTitle() {
        return title;
    }

    public boolean Track() {
        return Track(referer);
    }

    public boolean Track(String referUrl) {
        try {
            URL url = new URL(referUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            LogUtil.getLog(getClass()).error("encode:"+huc.getContentEncoding()+" type:"+huc.getContentType());
            String type = huc.getContentType();
            String charset = "utf-8";
            int index = type.indexOf("charset=");
            if (index!=-1) {
                index = index + 8;
                charset = type.substring(index).trim();
            }
            else
                charset = "gb2312";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(huc.getInputStream(), charset));
            String line = reader.readLine();
            String t1 = "<title>";
            String t2 = "</title>";
            int n1 = 0;
            int n2 = 0;
            while (line != null) {
                n1 = line.indexOf(t1);
                n2 = line.indexOf(t2);
                if (n1 != -1) {
                    if (n2 > 0) {
                        title = line.substring(n1 + 7, n2);
                    } else {
                        title = line.substring(n1 + 7);
                    }
                    break;
                }
                line = reader.readLine();
            }
            reader.close();
            huc.disconnect();
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("Track: " + e.getMessage());
        }
        return true;
    }

}
