package com.bstransky.synology.filestation.helper;

// import com.bstransky.synology.filestation.helper.SynologyErrorMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileRename {


    private static Logger logger = LogManager.getLogger();

    public static JsonObject file_rename(String sid, String path, String name) throws IOException {

        JsonObject result = new JsonObject();

        final String hostname = System.getenv("SYNOLOGY_HOST");
        if (hostname == null ) {
            result.addProperty("result", "synology host is not defined");
            logger.info("file_rename() -- response - {}", result.toString());
            return result;
        }

        // path=["/photo/00-INBOX/09 - Bernd/00-test/IMG_5543xxx.JPG"]&name=["IMG_5543zzz.JPG"]&api=SYNO.FileStation.Rename&method=rename&version=2
        HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/entry.cgi").newBuilder()
                .addQueryParameter("api", "SYNO.FileStation.Rename")
                .addQueryParameter("version", "2")
                .addQueryParameter("method", "rename")
                .addQueryParameter("path", "[\"" + path + "\"]")
                .addQueryParameter("name", "[\"" + name + "\"]")
                .addQueryParameter("_sid", sid).build();

        logger.info("file_rename() -- call synology uri: {}...", url.toString().substring(0, 140));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        Integer http_status_code = response.code();

        logger.info("file_rename() -- synology - HTTP response code: {}", http_status_code);

        if (http_status_code != 200) {
            result.addProperty("result", "HTTP error - status code: " + http_status_code.toString());
            result.addProperty("http_status_code", http_status_code);
        }
        else {
            // file does not exist:             {"result":"synology filestation error","synology":{"error":{"code":1200,"errors":[{"code":408,"path":"/photo/00-INBOX/09 - Bernd/00-test/IMG_5543.JPG"}]},"success":false}}

            String response_body = response.body().string();
            logger.info("file_rename() -- filestation response body - {}", response_body);

            JsonObject parser = JsonParser.parseString(response_body).getAsJsonObject();
            Boolean success = parser.get("success").getAsBoolean();

            if (success == Boolean.TRUE) {
                JsonObject data = parser.get("data").getAsJsonObject();
                JsonArray files = data.get("files").getAsJsonArray();
                if (files.get(0).getAsJsonObject().has("code")) {
                    Integer code = files.get(0).getAsJsonObject().get("code").getAsInt();
                    String error_message = SynologyErrorMessage.error_message(code);
                    result.addProperty("result", "synology filesystem error: [" + code.toString() + "] " + error_message);
                } else {
                    result.addProperty("result", "ok");
                }
            } else {
                    result.addProperty("result", "synology filestation error");
            }

            result.add("synology", parser);
        }

        logger.info("file_rename() -- response - {}", result.toString());

        return result;
    }
}
