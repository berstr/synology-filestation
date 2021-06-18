package com.bstransky.synology.filestation.helper;

import com.bstransky.synology.filestation.helper.SynologyErrorMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FileInfo {

    private static Logger logger = LogManager.getLogger();

    public static JsonObject file_info(String sid, String path) throws IOException {

        JsonObject result = new JsonObject();

        String hostname = System.getenv("SYNOLOGY_HOST");
        if (hostname == null) {
            hostname = "localhost";
        }

        // GET /webapi/entry.cgi?api=SYNO.FileStation.List&version=2&method=getinfo&additional=["real_path", "size,owner","time,perm,"type"]&path=["/video/1","/video/2.txt"]
        HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/entry.cgi").newBuilder()
                .addQueryParameter("api", "SYNO.FileStation.List")
                .addQueryParameter("version", "2")
                .addQueryParameter("method", "getinfo")
                .addQueryParameter("path", "[\"" + path + "\"]")
                .addQueryParameter("additional", "[\"real_path\", \"size\",\"owner\",\"time\",\"perm\",\"type\"]")
                .addQueryParameter("_sid", sid).build();

        logger.info("file_info() -- call synology uri: {}...", url.toString().substring(0, 140));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        Integer http_status_code = response.code();

        logger.info("file_info() -- synology - HTTP response code: {}", http_status_code);

        if (http_status_code != 200) {
            result.addProperty("result", "HTTP error - status code: " + http_status_code.toString());
            result.addProperty("http_status_code", http_status_code);
        }
        else {

            // when the path is a valid file:  {"data":{"files":[{"additional":{"owner":{"gid":100,"group":"users","uid":1026,"user":"berndcarolin"},
            //                                  "perm":{"acl":{"append":true,"del":true,"exec":true,"read":true,"write":true},"is_acl_mode":false,"posix":666},
            //                                  "real_path":"/volume1/music/inbox/tmp/ABC.txt","size":8,"time":{"atime":1622210037,"crtime":1622210021,"ctime":1622210032,"mtime":1622210021},"type":"TXT"},
            //                                  "isdir":false,"name":"ABC.txt","path":"/music/inbox/tmp/ABC.txt"}]},"success":true}
            // when path is a valid folder:   {"data":{"files":[{"additional":{"owner":{"gid":100,"group":"users","uid":1026,"user":"berndcarolin"},
            //                                  "perm":{"acl":{"append":true,"del":true,"exec":true,"read":true,"write":true},"is_acl_mode":false,"posix":777},
            //                                  "real_path":"/volume1/music/inbox/tmp","size":44,"time":{"atime":1622210038,"crtime":1622142847,"ctime":1622210037,"mtime":1622142847},
            //                                  "type":""},"isdir":true,"name":"tmp","path":"/music/inbox/tmp"}]},"success":true}
            // if path is not valid:            {"data":{"files":[{"code":418,"path":"/music/inbox/tmp/"}]},"success":true}
            // if path is an non existing file or folder:  {"data":{"files":[{"code":408,"path":"/music/inbox/tmp/ABCx.txt"}]},"success":true}

            String response_body = response.body().string();
            logger.info("file_info() -- filestation response body - {}", response_body);

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

        logger.info("file_info() -- response - {}", result.toString());

        return result;
    }
}
