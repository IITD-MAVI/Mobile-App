package com.nipungupta.helloworld;

/**
 * Created by Nipun Gupta on 4/16/2016.
 */
public class MaviApp {

    private static String message;
    private static DisplayActivity displayActivity = new DisplayActivity();

    public static void handleMessage(String jsonMessage) {
        JsonPath jsonPath = new JsonPath(jsonMessage);

        //Face Detection
        int noOfFaces = Integer.parseInt((String) jsonPath.read("$.faceDetectionString.noOfFaces"));
        if(noOfFaces > 0) {
            message = noOfFaces + "faces are detected.\n";
            int noOfRecFaces = jsonPath.read("$.faceDetectionString.nameArray.length()");
            if(noOfRecFaces==0) {
                message += "None of them is recognized";
                displayActivity.displayText(message);
            }
            else {
                message += "Recognized faces are ";
                for(int i=0; i<noOfRecFaces; i++) {
                    message += jsonPath.read("$.faceDetectionString.nameArray["+i+"]") + ", ";
                    displayActivity.displayText(message);
                }
            }
        }


        //Texture Detection
        if(jsonPath.read("$.textureString.pothole").equals("True")) {
            message = "Pothole detected ahead. Be careful.";
            displayActivity.displayText(message);
            displayActivity.vibratePhone(500);
        }
    }

    public static String getTextureFromCode(int code) {
        if(code==0) {
            return "not detected";
        }
        else if(code==1) {
            return "road";
        }
        else if(code==2) {
            return "pavement";
        }
        else if(code==3) {
            return "grass";
        }

        return null;
    }

}
