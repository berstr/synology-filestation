package com.bstransky.synology.filestation.controller;

import com.bstransky.synology.filestation.helper.*;

//import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import okhttp3.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@RestController
public class FilestationController {

    private static Logger logger = LogManager.getLogger();



    @GetMapping({"/file/move"})
    public String file_move(@RequestParam String source,@RequestParam String destination, @RequestParam String overwrite) throws IOException {

        JsonObject result = new JsonObject();

        logger.info("GET /file/move");
        logger.info("GET /file/move - source: {}", source);
        logger.info("GET /file/move - destination: {}", destination);
        logger.info("GET /file/move - overwrite: {}", overwrite);
        Boolean overwrite_ = overwrite.toLowerCase(Locale.ROOT).equals("true");
        Integer wait = 1; // number of seconds to sleep before checking the corresponding synology move task

        result = FileMove.file_move(Login.getSid(),source,destination,overwrite_, wait);

        logger.info("GET /file/move -- filestation response - {}", result.toString());

        return result.toString();
    }

    @GetMapping({"/file/delete"})
    public String file_delete(@RequestParam String source) throws IOException {

        JsonObject result = new JsonObject();

        logger.info("GET /file/delete");
        logger.info("GET /file/delete - source: {}", source);
        Integer wait = 1; // number of seconds to sleep before checking the corresponding synology move task

        result = FileDelete.file_delete(Login.getSid(),source, wait);

        logger.info("GET /file/delete -- filestation response - {}", result.toString());

        return result.toString();
    }

    @GetMapping({"/file/info"})
    public String file_info(@RequestParam String path) throws IOException {

        JsonObject result = null;

        logger.info("GET /file/info - path: {}", path);

        result = FileInfo.file_info(Login.getSid(),path);

        logger.info("GET /file/info -- response - {}", result.toString());

        return result.toString();
    }

    @GetMapping({"/file/rename"})
    public String file_info(@RequestParam String path, @RequestParam String name) throws IOException {

        JsonObject result = null;

        logger.info("GET /file/rename - path: {} - name: {}", path,name);

        result = FileRename.file_rename(Login.getSid(),path,name);

        logger.info("GET /file/rename -- response - {}", result.toString());

        return result.toString();
    }


    @GetMapping({"/folder/list"})
    public String list(@RequestParam String path) throws IOException {

        JsonObject result = null;

        logger.info("GET /folder/list - path: {}", path);

        result = FolderList.folder_list(Login.getSid(),path);

        logger.info("GET /folder/list - response - {}", result.toString());

        return result.toString();
    }

}
