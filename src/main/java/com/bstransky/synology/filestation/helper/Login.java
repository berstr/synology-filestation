package com.bstransky.synology.filestation.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Date;
import java.io.IOException;




public class Login {

    private static long LoginRefresh = 10; // number of minutes, if login sid is older, then a new sid is requested from Synology diskstation device
    private static final Logger logger = LogManager.getLogger();
    private static JsonObject loginCredentials = null;

    public static String getSid() throws IOException {
        String result = null;
        if (Login.loginCredentials == null) {
            logger.info("getSid() - login ID is null - refresh synology login");
            Login.login(2);
        }
        else {
            long time = Login.loginCredentials.get("time").getAsLong();
            long currentTime = new Date().getTime();
            long login_sid_age = ((currentTime - time) / 1000 / 60);
            if (login_sid_age > Login.LoginRefresh) {
                logger.info("getSid() - refresh synology login - current sid age in minutes: {}", login_sid_age);
                Login.login(2);
            }
        }
        result = Login.loginCredentials.get("sid").getAsString();
        return result;
    }

    public static JsonObject login(int sleep_time)  {
        JsonObject result = null;
        do {
            try {
                result = Login.login_synology();
                if (result.get("result").getAsString().equals("ok")) {
                    logger.info("Login.login() - Login success - response: {}...", result.get("result").toString());
                    break;
                } else {
                    logger.error("Login.login() - Login failure - response: {}", result.get("result").toString());
                }
            } catch (IOException err) {
                logger.error("Login.login() - Login error: {}", err.toString());
            }
            try {
                if (sleep_time < 60) {
                    sleep_time = sleep_time * 2;
                }
                logger.info("Login.login() - sleep for {} seconds ...", sleep_time);
                Thread.sleep(sleep_time * 1000);
                // TimeUnit.SECONDS.sleep(sleep_time);
            } catch (InterruptedException ie) {
                logger.info("Login.login() - INTERRUPTED - sleep for {} seconds ... excption: {}", sleep_time, ie.toString());
                //Thread.currentThread().interrupt();
            }
        } while (true);

        result = Login.loginCredentials;
        return result;
    }



    private static JsonObject login_synology() throws IOException {

        JsonObject result = new JsonObject();
        Login.loginCredentials = new JsonObject();

        logger.info("login() -- START");

        final String username = System.getenv("SYNOLOGY_USERNAME");
        final String password = System.getenv("SYNOLOGY_PASSWORD");
        final String hostname = System.getenv("SYNOLOGY_HOST");

        if (username == null) {
            result.addProperty("result", "synology username is not defined");
            logger.info("login() -- response - {}", result.toString());
            return result;
        }
        if (password == null ) {
            result.addProperty("result", "synology password is not defined");
            logger.info("login() -- response - {}", result.toString());
            return result;
        }
        if (hostname == null ) {
            result.addProperty("result", "synology host is not defined");
            logger.info("login() -- response - {}", result.toString());
            return result;
        }

            HttpUrl url = HttpUrl.parse("http://" + hostname + ":5000/webapi/auth.cgi").newBuilder()
                    .addQueryParameter("api", "SYNO.API.Auth")
                    .addQueryParameter("version", "3")
                    .addQueryParameter("method", "login")
                    .addQueryParameter("account", username)
                    .addQueryParameter("passwd", password)
                    .addQueryParameter("session", "FileStation")
                    .addQueryParameter("format", "sid")
                    .build();
            logger.info("login() -- call synology uri: {}...", url.toString().substring(0,50));

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);

            Response response = call.execute();
            logger.info("login() -- synology - HTTP response code: {}",response.code());

            // body:   "{'data':{'sid':'Ly8OVgqCItHOWV6zpbI55wvQYs1l23mM6l5d_nKib0UGxgA-zy6ezRuIwE2mBTviNe_bwg4XiexodtVWUQOJo8'},'success':true}";
            String response_body = response.body().string();

            logger.info("login() -- filestation response - {}", response_body.toString().substring(0,30));

            JsonObject parser = JsonParser.parseString(response_body).getAsJsonObject();
            Boolean success = parser.get("success").getAsBoolean();
            JsonObject data = parser.get("data").getAsJsonObject();


            if (success == Boolean.TRUE) {
                String sid = data.get("sid").getAsString();
                Login.loginCredentials.addProperty("sid", sid);
                result.addProperty("result", "ok");
            } else {
                logger.info("login() -- error - synology response: {}", parser.toString());
                result.addProperty("result", "login error");
            }

        long currentTime = new Date().getTime();
        Login.loginCredentials.addProperty("time", currentTime);

        logger.info("login() -- loginCredentials - time: {} - sid: {}...",loginCredentials.get("time").getAsLong(), loginCredentials.get("sid").getAsString().substring(0,20));

        logger.info("login() -- response - {}", result.toString());

        return result;

    }

}
