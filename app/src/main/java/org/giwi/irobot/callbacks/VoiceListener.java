package org.giwi.irobot.callbacks;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import org.giwi.irobot.VoiceHandler;

import java.util.ArrayList;


/**
 * The type Voice listener.
 */
public class VoiceListener implements RecognitionListener {
    private static final String TAG = VoiceListener.class.getCanonicalName();
    private final VoiceHandler handler;

    /**
     * Instantiates a new Voice listener.
     *
     * @param handler the handler
     */
    public VoiceListener(VoiceHandler handler) {
        this.handler = handler;
    }

    public void onReadyForSpeech(Bundle params) {
    }

    public void onBeginningOfSpeech() {
    }

    public void onRmsChanged(float rmsdB) {
    }

    public void onBufferReceived(byte[] buffer) {
    }

    public void onEndOfSpeech() {
        Log.d(TAG, "onEndofSpeech");
    }

    public void onError(int error) {
        Log.v(TAG, "error " + error);
    }

    public void onResults(Bundle results) {
        String str = "";
        Log.v(TAG, "onResults " + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++) {
            Log.v(TAG, "result " + data.get(i));
            str += data.get(i);
        }
        if(data.size() > 0) {
            handler.setVoiceText(data.get(0).toString().toLowerCase());
        }
    }

    public void onPartialResults(Bundle partialResults) {
    }

    public void onEvent(int eventType, Bundle params) {
    }
}