package com.engrave.pkuscheduler.Models.ScheduleJsonModel;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.engrave.pkuscheduler.Utils.PkuHelper.ApiRepository;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.engrave.pkuscheduler.Utils.StringUtils.convertStreamToString;
import static com.engrave.pkuscheduler.Utils.StringUtils.getUnicodeEscaped;

public final class ScheduleRootObject{
    public final static String storagePath ="ScheduleRootObjectCache.json";
    public int code;
    public String msg;
    public String uid;
    public String user_token;
    public Coursetable[] courseTable;
    public Coursetableroom[] courseTableRoom;
    public static ScheduleRootObject getInstance(String helperToken, Context context){
        ScheduleRootObject scheduleRootObject = null;
        boolean isStorageValid = true;
        try{
            scheduleRootObject = getInstanceFromStorage(context);
        } catch (IOException e) {
            isStorageValid=false;
            //TODO:alert
        }
        if(!isStorageValid){
            try{
                scheduleRootObject = getInstanceFromWebApi(helperToken,context);
            } catch (Exception e) {
                //TODO:alert
            }

        }
        return scheduleRootObject;
    }

    //
    public static ScheduleRootObject getInstanceFromWebApi(String helperToken,Context context) throws Exception {
        ScheduleRootObject scheduleRootObject=null;
        if(helperToken==null)
            throw new AssertionError();
        try{
            HttpURLConnection conn = null;
            URL url = new URL(ApiRepository.getPKUHelperScheduleUrl(helperToken));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            String jsonResponse = convertStreamToString(conn.getInputStream());
            scheduleRootObject = JSON.parseObject(getUnicodeEscaped(jsonResponse), ScheduleRootObject.class);
            if(scheduleRootObject!=null)
                saveInstance(scheduleRootObject,context);
            return scheduleRootObject;
        } catch (Exception e) {
            throw new Exception();
        }
    }

    //cache
    public static ScheduleRootObject getInstanceFromStorage(Context context) throws IOException{

        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        ScheduleRootObject scheduleRootObject = null;
        try {
            fileInputStream = context.openFileInput(storagePath);
            StringBuilder builder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            scheduleRootObject = JSON.parseObject(builder.toString(),ScheduleRootObject.class);

        } catch (FileNotFoundException fnfe) {
            throw new IOException();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }

        }
        return scheduleRootObject;
    }

    public static void saveInstance(ScheduleRootObject scheduleRootObject, Context context) throws JSONException, IOException {
        FileOutputStream fileOutputStream;
        OutputStreamWriter outputStreamWriter;
        fileOutputStream = context.openFileOutput(storagePath, Context.MODE_PRIVATE);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        outputStreamWriter.write(JSON.toJSONString(scheduleRootObject));
        outputStreamWriter.close();
        fileOutputStream.close();
    }

}
