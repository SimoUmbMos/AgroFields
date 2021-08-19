package com.mosc.simo.ptuxiaki3741;


import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;


@RunWith(AndroidJUnit4.class)
public class EncryptTest {
    public static final String TAG = "EncryptTestTag";
    private String stringToEncrypt;

    @Before
    public void init() {
        stringToEncrypt = "You can read this";
    }

    @Test
    public void encryptTimes(){
        String result, result2;
        long start,totalTime;

        try{
            start = System.currentTimeMillis();
            result = EncryptUtil.encryptString(stringToEncrypt);
            totalTime = System.currentTimeMillis()-start;
            Log.d(TAG, "time to encrypt: "+totalTime+"ms");
            Log.d(TAG, "result of encrypt: "+result);
            if(result != null){
                start = System.currentTimeMillis();
                result2 = EncryptUtil.decryptString(result);
                totalTime = System.currentTimeMillis()-start;
                Log.d(TAG, "time to decrypt: "+totalTime+"ms");
                Log.d(TAG, "result of decrypt: "+result2);
            }
        }catch (Exception e){
            Log.e(TAG, "encryptTimes:",e);
        }
    }

}

