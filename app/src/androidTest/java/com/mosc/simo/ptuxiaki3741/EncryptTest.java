package com.mosc.simo.ptuxiaki3741;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class EncryptTest {
    public static final String TAG = "EncryptTestTag";
    private String stringToEncrypt;

    @Before
    public void init() {
        stringToEncrypt = "If you can read this, its not encrypted.";
    }

    @Test
    public void encryptTimes(){
        String result, result2;
        long start,totalTime;

        List<Long> chachaTimes = new ArrayList<>();
        List<Long> blowfishTimes = new ArrayList<>();
        for(int i = 0;i<5000;i++){
            try{
                start = System.currentTimeMillis();
                result = EncryptUtil.encryptString(stringToEncrypt,true);
                result2 = EncryptUtil.decryptString(result,true);
                totalTime = System.currentTimeMillis()-start;
                chachaTimes.add(totalTime);
                assertNotEquals(result,stringToEncrypt);
                assertEquals(result2,stringToEncrypt);
            }catch (Exception e){
                Log.e(TAG, "encryptTimes:",e);
            }

            try{
                start = System.currentTimeMillis();
                result = EncryptUtil.encryptString(stringToEncrypt,false);
                result2 = EncryptUtil.decryptString(result,false);
                totalTime = System.currentTimeMillis()-start;
                blowfishTimes.add(totalTime);
                assertNotEquals(result,stringToEncrypt);
                assertEquals(result2,stringToEncrypt);
            }catch (Exception e){
                Log.e(TAG, "encryptTimes:",e);
            }
        }

        long sum=0;
        float avg;
        for(Long time:chachaTimes){
            sum = sum + time;
        }
        avg = (float)sum/chachaTimes.size();
        sum=0;
        for(Long time:blowfishTimes){
            sum = sum + time;
        }

        Log.d(TAG, "chacha20 encrypt decrypt: "+avg+"ms");
        avg = (float)sum/blowfishTimes.size();
        Log.d(TAG, "blowfishTimes encrypt decrypt: "+avg+"ms");
    }

}

