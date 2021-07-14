package com.bstransky.synology.filestation.helper;

import com.bstransky.synology.filestation.helper.SynologyErrorMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TaskStatus {

    private static Logger logger = LogManager.getLogger();


    private static JsonObject task_status_request(HttpUrl url, String api_call) throws IOException {

        // MOVE operations - Synology repsonses
        //
        // file to move does not exist:
        //      {"data":{"dest_folder_path":"/music/inbox/tmp",
        //          "errors":[{"code":408,"path":"/music/inbox/ABC.txt"}],
        //          "finished":true,"found_dir_num":0,"found_file_num":0,"found_file_size":0,"path":"","processed_size":0,"processing_path":"",
        //          "progress":1,"status":"FAIL","total":0,"transfer_rate":0},
        //          "success":true}}

        // file already exists in destination, but overwrite = false
        //      {"data":{"dest_folder_path":"/music/inbox/tmp",
        //          "finished":true,"found_dir_num":0,"found_file_num":0,"found_file_size":0,"path":"",
        //          "processed_size":0,"processing_path":"","progress":1,
        //          "skipstatus":{"status":"all"},"status":"PROCESSING","total":0,"transfer_rate":0},
        //      "success":true}}

        // file already exists in destination, overwrite = true
        //      {"data":{"dest_folder_path":"/music/inbox/tmp",
        //          "finished":true,"found_dir_num":0,"found_file_num":0,"found_file_size":0,"path":"",
        //          "processed_size":0,"processing_path":"","progress":1,
        //          "status":"PROCESSING","total":0,"transfer_rate":0},
        //      "success":true}}

        // file does not exists in destination, overwrite = true
        //      {"data":{"dest_folder_path":"/music/inbox/tmp",
        //      "finished":true,"found_dir_num":0,"found_file_num":0,"found_file_size":0,"path":"",
        //      "processed_size":0,"processing_path":"","progress":1,
        //      "status":"PROCESSING","total":0,"transfer_rate":0},
        //      "success":true}}

        // file does not exists in destination, overwrite = false
        //      {"data":{"dest_folder_path":"/music/inbox/tmp",
        //      "finished":true,"found_dir_num":0,"found_file_num":0,"found_file_size":0,"path":"",
        //      "processed_size":0,"processing_path":"","progress":1,
        //      "skipstatus":{"status":"none"},"status":"PROCESSING","total":0,"transfer_rate":0},
        //      "success":true}}

        // destination path does not exist
        //      {"error":{"code":1002,"errors":[{"code":408,"path":"/music/inbox/tmp1"}]},
        //      "success":false}}

        JsonObject result = new JsonObject();

        logger.info("task_status_request() -- url: {}", url.toString().substring(0,url.toString().length()-40));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        Integer http_status_code = response.code();
        logger.info("task_status_request() -- synology - HTTP response code: {}",http_status_code);

        if (http_status_code != 200) {
            result.addProperty("result", "HTTP error - status code: " + http_status_code.toString());
            result.addProperty("http_status_code", http_status_code);
        } else {
            String response_body = response.body().string();
            logger.info("task_status_request() -- synology response body - {}", response_body);

            JsonObject parser = JsonParser.parseString(response_body).getAsJsonObject();
            Boolean success = parser.get("success").getAsBoolean();

            if (success == Boolean.TRUE) {
                JsonObject data = parser.get("data").getAsJsonObject();
                // "data":{..., "errors":[{"code":408,"path":"/music/inbox/ABC.txt"}],...}
                if (data.has("errors")) {
                    JsonArray errors = data.get("errors").getAsJsonArray();
                    Integer code = errors.get(0).getAsJsonObject().get("code").getAsInt();
                    String error_message = SynologyErrorMessage.error_message(code);
                    result.addProperty("result", "synology filesystem error: [" + code.toString() + "] " + error_message);
                    result.addProperty("error_code", code);
                    result.addProperty("error_message", error_message);
                } else if (data.has("skipstatus")) {
                    String skipstatus = data.get("skipstatus").getAsJsonObject().get("status").getAsString();
                    if (skipstatus.equals("none")) {
                        result.addProperty("result", "ok");
                    } else {
                        result.addProperty("result", "destination file exists, not overwritten - synology skipstatus: " + skipstatus);
                        result.addProperty("skipstatus", skipstatus);
                    }
                } else {
                    result.addProperty("result", "ok");
                }
            } else { // success == false
                result.addProperty("result", "synology error, cannot get task status");
                result.addProperty("success", success);
            }
            result.add("synology", parser);
        }
        return result;
    }

    // api_call:    "SYNO.FileStation.CopyMove"
    public static JsonObject task_status(String sid, String taskid,  String api_call, String status_api_version,  Integer wait, Integer retries) throws IOException {

        JsonObject result = new JsonObject();

        final String hostname = System.getenv("SYNOLOGY_HOST");
        if (hostname == null ) {
            result.addProperty("result", "synology host is not defined");
            logger.info("task_status() -- response - {}", result.toString());
            return result;
        }
        logger.info("task_status() - taskid: [{}] - api_call: [{}]", taskid, api_call);

        HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/entry.cgi")
                .newBuilder().addQueryParameter("api", api_call)
                .addQueryParameter("version", status_api_version)
                .addQueryParameter("method", "status")
                .addQueryParameter("taskid",  String.format("\"%s\"",taskid))
                .addQueryParameter("_sid", sid).build();

        try {
           logger.info("task_status() -- START - sleep for {} seconds ...",wait);
           TimeUnit.SECONDS.sleep(wait);
           logger.info("task_status() -- END - sleep for {} seconds ...",wait);
           result = task_status_request(url, api_call);
         } catch (InterruptedException ie) {
            logger.info("task_status() -- INTERRUPTED - sleep for {} seconds ... excption: {}",wait, ie.toString());
            Thread.currentThread().interrupt();
            result.addProperty("result", "wait for task status request was interrupted");
        }

        logger.info("task_status() -- result - {}", result.toString());

        return result;
    }

}
