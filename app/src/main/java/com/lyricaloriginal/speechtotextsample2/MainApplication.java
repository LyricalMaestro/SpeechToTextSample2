package com.lyricaloriginal.speechtotextsample2;

import android.app.Application;

import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.WatsonSDK;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;

import java.net.URI;

/**
 * Created by LyricalMaestro on 2016/04/26.
 */
public class MainApplication extends WatsonSDK{

    private static final String URL = "wss://stream.watsonplatform.net/speech-to-text/api";

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechToText.sharedInstance().initWithContext(
                URI.create(URL),
                this.getApplicationContext(),
                new SpeechConfiguration());
    }
}
