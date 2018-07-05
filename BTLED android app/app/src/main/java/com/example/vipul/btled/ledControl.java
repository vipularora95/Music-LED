package com.example.vipul.btled;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ledControl extends AppCompatActivity {


    static final String TAG="audio";
    private final static float MEAN_MAX = 16384f;   // Maximum signal value
    private final static int AGC_OFF = MediaRecorder.AudioSource.VOICE_RECOGNITION;

    private int fftBins = 256;
    private int sampleRate = 44100;
    private int updateMs = 10;
    public int div=0,rf=5,gf=25,bf=45,RF,GF,BF,rt=50,gt=50,bt=50,base=50;
    public int RED=0,GREEN=0,BLUE=0,RT,GT,BT,RS,GS,BS;
    //private AnalyzeView graphView;
    private Looper samplingThread;

    private boolean isTesting = false;
    private boolean flag = false;
    private boolean isMeasure = true;

    Button btnOn, btnOff, btnDis;
    SeekBar redf,redt,greenf,greent,bluef,bluet,max;
    String address = null;
    TextView lumn;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        //view of the ledControl layout
        setContentView(R.layout.activity_led_control);
        //call the widgtes
        btnOn = (Button)findViewById(R.id.button2);
        btnOff = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);
        redf= findViewById(R.id.seekBar);
        redt= findViewById(R.id.seekBar2);
        greenf= findViewById(R.id.seekBar3);
        greent= findViewById(R.id.seekBar4);
        bluef= findViewById(R.id.seekBar5);
        bluet= findViewById(R.id.seekBar6);
        max= findViewById(R.id.seekBar7);


        new ConnectBT().execute(); //Call the class to connect

        samplingThread = new Looper();
        samplingThread.start();//method to turn on


        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flag=true;

            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                flag=false;
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        redf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            rf=progress/2;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        greenf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gf=progress/2;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bluef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bf=progress/2;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        redt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rt=(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        greent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gt=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bluet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bt=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        max.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                base=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        samplingThread = new Looper();
//        samplingThread.start();
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle bundle) {
//        super.onRestoreInstanceState(bundle);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        samplingThread.finish();
//    }
//
//    @Override
//    protected void onDestroy() {
//        //getPreferences().unregisterOnSharedPreferenceChangeListener(this);
//        super.onDestroy();
//    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(((rt/10)+(gt/10)+(bt/10)+"|").getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("TO".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> // UI thread
    {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");
        }
        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            if (!ConnectSuccess)
            {
                Log.i("TAG", "Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                Log.i("TAG", "Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    public class Looper extends Thread {
        AudioRecord record;
        int minBytes;
        long baseTimeMs;
        boolean isRunning = true;
        boolean isPaused1 = false;
        // Choose 2 arbitrary test frequencies to verify FFT operation
        DoubleSineGen sineGen1 = new DoubleSineGen(1234.0, sampleRate, MEAN_MAX);
        DoubleSineGen sineGen2 = new DoubleSineGen(3300.0, sampleRate, MEAN_MAX / 4.0);
        double[] tmp = new double[fftBins];

        // Timers
        private int loops = 0;

        public Looper() {
            minBytes = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            minBytes = Math.max(minBytes, fftBins);
            // VOICE_RECOGNITION: use the mic with AGC turned off!
            record =  new AudioRecord(AGC_OFF, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,  minBytes);
            Log.d(TAG, "Buffer size: " + minBytes + " (" + record.getSampleRate() + "=" + sampleRate + ")");
        }

        @Override
        public void run() {
            final double[] fftData = new double[fftBins];
            RealDoubleFFT fft = new RealDoubleFFT(fftBins);
            double scale = MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
            short[] audioSamples = new short[minBytes];
            record.startRecording();

            baseTimeMs = SystemClock.uptimeMillis();
            while(isRunning) {
                loops++;
                baseTimeMs += updateMs;
                int delay = (int) (baseTimeMs - SystemClock.uptimeMillis());
                if (delay < 20) {
                    Log.i(TAG, "wait: " + delay);
                }
                try {
                    Thread.sleep(delay < 10 ? 10 : delay);
                } catch (InterruptedException e) {
                    // Log.i(TAG, "Delay interrupted");
                    continue;
                }

                if (isTesting) {
                    sineGen1.getSamples(fftData);
                    sineGen2.addSamples(fftData);
                } else {
                    record.read(audioSamples, 0, minBytes);
                    shortToDouble(audioSamples, fftData);
                }
                if (isPaused1) {
                    continue;
                }
                fft.ft(fftData);
                convertToDb(fftData, scale);
                update(fftData);

                div=fftData.length-1;
                RF=(div*rf)/100;
                GF=(div*gf)/100;
                BF=(div*bf)/100;
                RED= (int)Math.round(base+fftData[RF]);
                GREEN= (int)Math.round(base+fftData[GF]);
                BLUE= (int)Math.round(base+fftData[BF]);
                RT=RED*rt;
                GT=GREEN*gt;
                BT=BLUE*bt;
                RS=clamp(RT/100);
                GS=clamp(GT/100);
                BS=clamp(BT/100);
            }

            Log.i(TAG, "Releasing Audio");
            record.stop();
            record.release();
            record = null;
        }

        private int clamp(int value) {
            if (value < 0) {
                value = 0;
            } else if (value > 9) {
                value = 9;
            }
            return value;
        }

        private void update(final double[] data) {
            ledControl.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //ledControl.this.recompute(data);
                    ((TextView) findViewById(R.id.red)).setText("R"+RED + "DB  F-" + RF + "  RAW " + RT + "  Send " + RS);
                    ((TextView) findViewById(R.id.green)).setText("G"+GREEN + "DB  F-" + GF + "  RAW " + GT + "  Send " + GS);
                    ((TextView) findViewById(R.id.blue)).setText("B"+ BLUE + "DB  F-" + BF + "  RAW " + BT + "  Send " + BS);

                    if(flag==true) {
                        try {
                            btSocket.getOutputStream().write(("|" + RS + GS + BS).getBytes() );
                            //currentThread().sleep(100);
                        } catch (IOException e) {
                            msg("Error");
                        }
                    }

                }
            });
        }

        public void setPause(boolean pause) {
            this.isPaused1 = pause;
        }

        public void finish() {
            isRunning=false;
            interrupt();
        }
    }


//    public void recompute(double[] data) {
////        graphView.recompute(data, 1, data.length / 2, showLines);
////        graphView.invalidate();
//        refreshrgb();
//
//    }

    private static double[] shortToDouble(short[] s, double[] d) {
        for (int i = 0; i < d.length; i++) {
            d[i] = s[i];
        }
        return d;
    }

    /**
     * Compute db of bin, where "max" is the reference db
     * @param r Real part
     * @param i complex part
     */
    private static double db2(double r, double i, double maxSquared) {
        return 5.0 * Math.log10((r * r + i * i) / maxSquared);
    }

    /**
     * Convert the fft output to DB
     */

    static double[] convertToDb(double[] data, double maxSquared) {
        data[0] = db2(data[0], 0.0, maxSquared);
        int j = 1;
        for (int i=1; i < data.length - 1; i+=2, j++) {
            data[j] = db2(data[i], data[i+1], maxSquared);
        }
        data[j] = data[0];


        return data;
    }

    /**
     * Verify the supplied audio rates are valid!
     * @param requested
     */
    private static String[] validateAudioRates(String[] requested) {
        ArrayList<String> validated = new ArrayList<String>();
        for (String s : requested) {
            int rate = Integer.parseInt(s);
            if (AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT) != AudioRecord.ERROR_BAD_VALUE) {
                validated.add(s);
            }
        }
        return validated.toArray(new String[0]);
    }
}
