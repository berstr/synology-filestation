package com.bstransky.synology.filestation.helper;

public class SynologyErrorMessage {

    public static String error_message(Integer code) {
        String result = "";

        switch (code) {
            case 408:
                result = "No such file or directory";
                break;
            case 418:
                result = "Illegal name or path";
                break;
            case 1002:
                result = "An error occurred at the destination. More information in the <errors> object";
                break;
            default:
                result = "synology error code: " + code.toString();
                break;
        }

        return result;
    }
}
