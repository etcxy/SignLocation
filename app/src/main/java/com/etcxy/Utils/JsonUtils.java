package com.etcxy.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by etcxy@live.cn on 9/26/2017.
 */

public class JsonUtils {
    //   path 请求路径
//   json  封装好的json数据，关于封装json数据的方法，最下面有。

    public static String JsonPost(final String path, final JSONObject json) {
        BufferedReader in = null;
        String result = "";
        OutputStream os = null;
        try {
            URL url = new URL(path);
            // 然后我们使用httpPost的方式把lientKey封装成Json数据的形式传递给服务器
            // 在这里呢我们要封装的时这样的数据
            // 我们把JSON数据转换成String类型使用输出流向服务器写
            String content = String.valueOf(json);
            // 现在呢我们已经封装好了数据,接着呢我们要把封装好的数据传递过去
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            // 设置User-Agent: Fiddler
            conn.setRequestProperty("ser-Agent", "Fiddler");
            // 设置contentType
            conn.setRequestProperty("Content-Type", "application/json");

            os = conn.getOutputStream();
            os.write(content.getBytes());
            os.flush();
            // 定义BufferedReader输入流来读取URL的响应
            // Log.i("-----send", "end");

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            if (conn.getResponseCode() == 200) {
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            }
        } catch (SocketTimeoutException e) {
            // Log.i("错误", "连接时间超时");
            e.printStackTrace();
            return "错误";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "错误";
        } catch (ProtocolException e) {
            e.printStackTrace();
            return "错误";
        } catch (IOException e) {
            e.printStackTrace();
            return "错误";
        } finally {// 使用finally块来关闭输出流、输入流
            try {
                if (os != null) {
                    os.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
