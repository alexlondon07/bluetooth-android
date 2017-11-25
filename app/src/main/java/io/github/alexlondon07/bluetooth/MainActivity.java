package io.github.alexlondon07.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button buttonOn, buttonOf, buttonDiscover;
    private ListView devicesListView;

    //BT Adapter
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> arrayAdapter;

    //Manejadores y Conexiones
    private Handler handler;
    //private ConnectedThread connectedThread;
    private BluetoothSocket bluetoothSocket;


    //Constantes
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTION_STATUS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadView();
    }

    private void loadView() {
        buttonOn = findViewById(R.id.btn_on);
        buttonOf = findViewById(R.id.btn_off);
        buttonDiscover = findViewById(R.id.btn_discover);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devicesListView = findViewById(R.id.listview);
        devicesListView.setAdapter(arrayAdapter);

        requestPermission();

        initHandler();

        validateBlueTDevice();

    }

    private void requestPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {

        handler = new Handler(){

            public void handleMessage(Message message){

                if(message.what == MESSAGE_READ){
                    String messageRead = "";
                   try {
                       messageRead = new String((byte[]) message.obj, "UTF-8");
                   }catch (Exception e){
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                   }
                   Log.e(TAG, messageRead);

                   if(message.what == CONNECTION_STATUS){
                       if(message.arg1 == 1){
                           Log.i(TAG, getString(R.string.conectandoBluetooth) + message.obj);
                       }else{
                           Log.i(TAG, getString(R.string.conexionFallida) + message.obj);
                       }
                   }

                }

            }
        };

    }

    private void validateBlueTDevice() {
        if(bluetoothAdapter == null){
            Toast.makeText(this, R.string.dispositivoNoSoportado, Toast.LENGTH_LONG).show();
        }else {
            buttonOn.setEnabled(true);
            buttonOf.setEnabled(true);
            buttonDiscover.setEnabled(true);
        }
    }


    public void BluetoothON(View view) {

        if(!bluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this, R.string.bluetoothEncedido, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, R.string.bluetoothEncendido, Toast.LENGTH_LONG).show();
        }

    }

    public void BluetoothOFF(View view) {

        bluetoothAdapter.disable();
        Toast.makeText(this, R.string.bluetoothEncendido, Toast.LENGTH_SHORT).show();

    }

    public void BluetoothDiscover(View view) {

        if(bluetoothAdapter.isEnabled()){
            arrayAdapter.clear();
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "Descrubir iniciando...", Toast.LENGTH_SHORT).show();
            registerReceiver(btnReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }else {
            Toast.makeText(this, R.string.bluetoothEncendido, Toast.LENGTH_LONG).show();
        }
    }


    final BroadcastReceiver btnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Agregar devices al adaptador
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };
}
