package com.mosc.simo.ptuxiaki3741.util.file.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class JsonToStringTask implements Callable<String> {
    public static final String TAG = "JsonToStringTask";
    private final InputStream inputStream;
    public JsonToStringTask(InputStream inputStream){
        this.inputStream = inputStream;
    }
    @Override
    public String call() {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuilder sb = new StringBuilder();
        String str;
        try{
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
        }catch (Exception e){
            Log.e(TAG, "call: ",e);
        }
        return sb.toString();
    }
}
