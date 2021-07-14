package com.bstransky.synology.filestation.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bstransky.synology.filestation.helper.SynologyErrorMessage;

import java.io.IOException;

public class FolderList {

    private static Logger logger = LogManager.getLogger();

    public static JsonObject folder_list(String sid, String path) throws IOException {

        JsonObject result = new JsonObject();

        final String hostname = System.getenv("SYNOLOGY_HOST");
        if (hostname == null ) {
            result.addProperty("result", "synology host is not defined");
            logger.info("folder_list() -- response - {}", result.toString());
            return result;
        }

        HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/entry.cgi").newBuilder()
                .addQueryParameter("api", "SYNO.FileStation.List")
                .addQueryParameter("version", "2")
                .addQueryParameter("method", "list")
                .addQueryParameter("folder_path", path)
                .addQueryParameter("_sid", sid).build();
        logger.info("GET /list -- call synology uri: {}...", url.toString().substring(0,50));

        logger.info("folder_list() -- call synology uri: {}...", url.toString().substring(0, 140));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        Integer http_status_code = response.code();

        logger.info("folder_list() -- synology - HTTP response code: {}", http_status_code);

        if (http_status_code != 200) {
            result.addProperty("result", "HTTP error - status code: " + http_status_code.toString());
            result.addProperty("http_status_code", http_status_code);
        }
        else {

            // successfull response:   {"data":{"files":[{"isdir":true,"name":"00 - Metadata","path":"/photo/00-INBOX/00 - Metadata"},...],"offset":0,"total":7},"success":true}
            // folder does not exist:  {"error":{"code":408},"success":false}
            String response_body = response.body().string();
            logger.info("folder_list() -- filestation response body - {}", response_body);

            JsonObject parser = JsonParser.parseString(response_body).getAsJsonObject();
            Boolean success = parser.get("success").getAsBoolean();

            if (success == Boolean.TRUE) {
                result.addProperty("result", "ok");
            } else {
                JsonObject error = parser.get("error").getAsJsonObject();
                Integer code = error.get("code").getAsInt();
                String error_message = SynologyErrorMessage.error_message(code);
                result.addProperty("result", "synology filesystem error: [" + code.toString() + "] " + error_message + " - path: [" + path + "]");
                result.addProperty("path", path);
            }

            result.add("synology", parser);
        }

        logger.info("folder_list() -- response - {}", result.toString());

        return result;
    }
}
