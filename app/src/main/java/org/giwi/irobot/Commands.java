package org.giwi.irobot;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


/**
 * The type Commands.
 */
class Commands {
    private static final String TAG = Commands.class.getCanonicalName();
    /**
     * Init json object.
     *
     * @param context the context
     * @return the json object
     */
    static JSONObject init(Context context) {
        JSONObject json = new JSONObject();
        try {
            InputStream is = context.getAssets().open("conf.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int r = is.read(buffer);
            Log.d(TAG, "" + r);
            is.close();
            json = new JSONObject(new String(buffer, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    static JSONObject initDictionary(Context context) {
        JSONObject json = new JSONObject();
        try {
            InputStream is = context.getAssets().open("dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int r = is.read(buffer);
            Log.d(TAG, "" + r);
            is.close();
            json = new JSONObject(new String(buffer, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Hex string to byte array byte [ ].
     *
     * @param s the s
     * @return the byte [ ]
     */
    static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
}