package org.giwi.irobot;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;
import org.giwi.irobot.callbacks.MyLeScanCallback;
import org.giwi.irobot.callbacks.VoiceListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * The type Main activity.
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BTHandler, VoiceHandler {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    /**
     * The Toolbar.
     */
    @ViewById
    Toolbar toolbar;
    /**
     * The Drawer.
     */
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawer;
    /**
     * The Navigation view.
     */
    @ViewById(R.id.nav_view)
    NavigationView navigationView;
    @ViewById
    Button start_reg;
    @ViewById
    TextView textResults;

    @ViewById
    EditText code;
    @ViewById
    Button sendCmd;
    private static final int REQUEST_ENABLE_BT = 666;
    /**
     * The constant EXTRA_DATA.
     */
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    private BluetoothManager bluetoothManager;
    private BluetoothGattCharacteristic carac;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt gatt;
    private JSONObject commands;
    private JSONObject dictionary;
    private String currentCommand = "halt";

    /**
     * Init.
     */
    @AfterViews
    void init() {
        if (commands == null) {
            commands = Commands.init(this);
        }
        if (dictionary == null) {
            dictionary = Commands.initDictionary(this);
        }
        setSupportActionBar(toolbar);
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(
                new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }
        ).check();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.i(TAG, "start bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Log.i(TAG, "No bluetooth");
        }
        mBluetoothAdapter.startLeScan(new MyLeScanCallback(this));
        initVoiceRecognizer();
    }

    void initVoiceRecognizer() {
        speechRecognizer = getSpeechRecognizer();
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
    }

    void sendCommand(String command) {
        if (carac != null) {
            try {
                JSONObject cmdObj = commands.getJSONObject(command.toUpperCase());
                if (cmdObj != null) {
                    String cmdStr = cmdObj.getString("command");
                    byte[] cmd = Commands.hexStringToByteArray(cmdStr);
                    carac.setValue(cmd);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            if (gatt != null) {
                gatt.writeCharacteristic(carac);
            }
        }
    }

    @Click(R.id.sendCmd)
    void sendCmd() {
        sendCommand(code.getText().toString());
    }

    @Click(R.id.start_reg)
    void startListening(View v) {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
        speechRecognizer.startListening(intent);
    }

    SpeechRecognizer getSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new VoiceListener(this));
        }
        return speechRecognizer;
    }

    @Override
    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.i(TAG, "Action : " + action);
        sendCommand("E1");
        sendCommand("CU");
        //  sendCommand("S10");
        sendBroadcast(intent);
    }

    @Override
    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.i(TAG, "characteristic : " + characteristic.getUuid().toString());
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                    stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setCarac(BluetoothGatt gatt, BluetoothGattCharacteristic carac) {
        this.carac = carac;
        this.gatt = gatt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setVoiceText(String text) {
        try {
            String[] lexems = text.split(" ");
            for (int i = 0; i < lexems.length; i++) {
                String lexem = lexems[i];
                if (dictionary.has(lexem)) {
                    JSONObject cmd = dictionary.getJSONObject(lexem);
                    String cmdStr = cmd.optString("cmd", "");
                    if ("halt".equals(cmdStr)) {
                        BackgroundExecutor.cancelAll("main", true);
                    } else {
                        if (cmd.optBoolean("speed", false)) {
                            if (lexems.length > i + 1 && dictionary.has(lexems[i + 1])) {
                                cmdStr += dictionary.getJSONObject(lexems[i + 1]).getString("val");
                            } else {
                                cmdStr += dictionary.getJSONObject("normal").getString("val");
                            }
                        }
                        if (cmd.optBoolean("number", false)) {
                            if (lexems.length > i + 1 && dictionary.has(lexems[i + 1])) {
                                cmdStr += lexems[i + 1];
                            } else {
                                cmdStr = "E5";
                            }
                        }
                    }
                    //looper(cmdStr);
                    if(!"".equals(cmdStr)) {
                        textResults.setText(cmdStr);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Background(id = "main", serial = "looper")
    void looper(String cmd) {
        if (!"halt".equals(cmd)) {
            sendCommand(cmd);
            SystemClock.sleep(500L);
        }
    }
}
