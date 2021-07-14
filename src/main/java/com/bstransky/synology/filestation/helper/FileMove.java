package com.bstransky.synology.filestation.helper;

import com.bstransky.synology.filestation.helper.SynologyErrorMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FileMove {

    private static Logger logger = LogManager.getLogger();

    public static JsonObject file_move(String sid,String source,  String destination,  Boolean overwrite, Integer wait) throws IOException {

        JsonObject result = new JsonObject();
        String api_call = "SYNO.FileStation.CopyMove";

        Boolean remove_src = Boolean.TRUE;

        final String hostname = System.getenv("SYNOLOGY_HOST");
        if (hostname == null ) {
            result.addProperty("result", "synology host is not defined");
            logger.info("file_move() -- response - {}", result.toString());
            return result;
        }

        HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/entry.cgi")
                .newBuilder().addQueryParameter("api", api_call)
                .addQueryParameter("version", "2")
                .addQueryParameter("method", "start")

                .addQueryParameter("path", source)
                .addQueryParameter("dest_folder_path", destination)
                .addQueryParameter("overwrite", overwrite.toString())
                .addQueryParameter("remove_src",Boolean.TRUE.toString())
                .addQueryParameter("_sid", sid).build();

        logger.info("file_move() -- call synology uri: {}...", url.toString().substring(0,50));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        Integer http_status_code = response.code();

        logger.info("file_move() -- synology - HTTP status code: {}",http_status_code.toString());

        if (http_status_code != 200) {
            result.addProperty("result", "HTTP error - status code: " + http_status_code.toString());
            result.addProperty("http_status_code", http_status_code);
        } else {
            //  {"data":{"taskid":"FileStation_60B0E65E1C162712"},"success":true}
            String response_body = response.body().string();
            logger.info("file_move() -- filestation response - {}", response_body);

            JsonObject parser = JsonParser.parseString(response_body).getAsJsonObject();
            Boolean success = parser.get("success").getAsBoolean();

            if (success == Boolean.TRUE) {
                String taskid = parser.get("data").getAsJsonObject().get("taskid").getAsString();
                String status_api_version = "3";
                result = TaskStatus.task_status(sid, taskid, api_call,status_api_version, 1, 0);
            } else {
                // {"error":{"code":1002,"errors":[{"code":408,"path":"/music/inbox/tmp1"}]},"success":false}}
                if (parser.has("error")) {
                    JsonObject error = parser.get("error").getAsJsonObject();
                    Integer code = error.get("code").getAsInt();
                    String error_message = SynologyErrorMessage.error_message(code);
                    result.addProperty("result", "synology filesystem error: [" + code.toString() + "] " + error_message);
                    result.addProperty("error_code", code);
                    result.addProperty("error_message", error_message);
                } else {
                    result.addProperty("result", "error - unknown synology repsonse");
                }
                result.add("synology", parser);
            }
        }

        logger.info("file_move() -- result - {}", result.toString());

        return result;
    }
}
