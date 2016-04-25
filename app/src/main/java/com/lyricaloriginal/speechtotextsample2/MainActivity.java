package com.lyricaloriginal.speechtotextsample2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements ISpeechDelegate {

    private static final String TAG = MainActivity.class.getName();

    private TextView mMsgTextView;
    private Button mRecordingBtn;

    private String mRecognitionResults = "";
    private TaskDisplayMsg mTaskDisplayMsg = new TaskDisplayMsg();
    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMsgTextView = (TextView) findViewById(R.id.msg_textview);
        mRecordingBtn = (Button) findViewById(R.id.recording_btn);
        mRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    SpeechToText.sharedInstance().stopRecognition();
                } else {
                    SpeechToText.sharedInstance().recognize();
                }
                v.setEnabled(false);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Pair<String, String> account = loadAccount();
            SpeechToText.sharedInstance().setCredentials(account.first, account.second);
            SpeechToText.sharedInstance().setDelegate(this);
            SpeechToText.sharedInstance().setModel("ja-JP_BroadbandModel");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpeechToText.sharedInstance().stopRecognition();
    }

    @Override
    public void onOpen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsRecording = true;
                mRecordingBtn.setText("Stop Recording");
                mRecordingBtn.setEnabled(true);
                Toast.makeText(MainActivity.this, "onOpen", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onError(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "onError " + s, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        mRecognitionResults = "";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsRecording = false;
                mRecordingBtn.setText("Start Recording");
                mRecordingBtn.setEnabled(true);
                Toast.makeText(MainActivity.this, "onClose", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMessage(final String message) {
        Log.d(TAG, "onMessage, message: " + message);
        try {
            JSONObject jObj = new JSONObject(message);
            if (jObj.has("state")) {
                Log.d(TAG, "Status message: " + jObj.getString("state"));
            } else if (jObj.has("results")) {
                Log.d(TAG, "Results message: ");
                JSONArray jArr = jObj.getJSONArray("results");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject obj = jArr.getJSONObject(i);
                    JSONArray jArr1 = obj.getJSONArray("alternatives");
                    String str = jArr1.getJSONObject(0).getString("transcript");
                    str = str.replaceAll("\\s+", "");    //  日本語に限定しているのでスペース補填
                    String strFormatted = Character.toUpperCase(str.charAt(0)) + str.substring(1);
                    if (obj.getString("final").equals("true")) {
                        String stopMarker = "。";
                        mRecognitionResults += strFormatted + stopMarker;
                        displayResult(mRecognitionResults);
                    } else {
                        displayResult(mRecognitionResults + strFormatted);
                    }
                    break;
                }
            } else {
                displayResult("unexpected data coming from stt server: \n" + message);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
        }
    }

    @Override
    public void onAmplitude(double v, double v1) {
    }

    private void displayResult(final String result) {
        mTaskDisplayMsg.setMsg(result);
        runOnUiThread(mTaskDisplayMsg);
    }

    /**
     * Pair#firstはusername, Pair#secondはpassword
     *
     * @return
     * @throws IOException
     */
    private Pair<String, String> loadAccount() throws IOException {
        Properties properties = new Properties();
        properties.load(getAssets().open("account.txt", MODE_PRIVATE));
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        return new Pair<String, String>(username, password);
    }

    private class TaskDisplayMsg implements Runnable {

        private String mMsg = "";

        void setMsg(String msg) {
            if (msg == null) {
                return;
            }
            mMsg = msg;
        }

        @Override
        public void run() {
            mMsgTextView.setText(mMsg);
        }
    }
}
