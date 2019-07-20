package com.dns.buggyinstaller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import life.sabujak.roundedbutton.RoundedButton;

public class MainActivity extends AppCompatActivity {

    String receivedData = "";
    int downloadIdOne;
     String URL1 = "https://github.com/dineshshetty/FridaLoader/releases/download/v1/FridaLoader.apk";
     String apkName = "Game.apk";
    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        RoundedButton clickButton = (RoundedButton) findViewById(R.id.installButton);
        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doSDCardStuff();
            }
        });
    }

    private void doSDCardStuff() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            if (checkPermission()) {
                String path = Environment.getExternalStorageDirectory().toString();
                performDownloadAPK(path);

            } else {
                requestPermission(); // Code for permission
                doSDCardStuff();
            }
        }
    }

        private void performDownloadAPK(final String path) {
            System.out.println("EXTERNAL PATH = "+path);

        downloadIdOne = PRDownloader.download(URL1, path, apkName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        System.out.println("onStartOrResume entered");
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        downloadIdOne = 0;
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        System.out.println("Download completed. Installation will start in 10 seconds!");
                        final String apkPath = path+"/"+apkName;
                        System.out.println("apkPath = "+apkPath);
                        final ProgressDialog TempDialog;
                        CountDownTimer CDT;
                        final int[] i = {10};

                        TempDialog = new ProgressDialog(MainActivity.this);
                        TempDialog.setMessage("Installation starts in...");
                        TempDialog.setCancelable(false);
                        TempDialog.setProgress(i[0]);
                        TempDialog.show();

                        CDT = new CountDownTimer(10000, 1000)
                        {
                            public void onTick(long millisUntilFinished)
                            {
                                TempDialog.setMessage("Installation starts in " + i[0] + " sec");
                                i[0]--;
                            }

                            public void onFinish()
                            {
                                TempDialog.dismiss();
                                String status = executeCommand("su 0 pm install -r "+apkPath, false);
                                if (status.contains("Success")) {
                                    StyleableToast.makeText(MainActivity.this, "Successfully installed APK", Toast.LENGTH_LONG, R.style.green).show();
                                }else{
                                    StyleableToast.makeText(MainActivity.this, "Error installing APK", Toast.LENGTH_LONG, R.style.red).show();
                                }
                            }
                        }.start();
                    }

                    @Override
                    public void onError(Error error) {
                        System.out.println(error.getConnectionException());
                        Toast.makeText(getApplicationContext(), "Something Went Wrong" + " " + "1", Toast.LENGTH_SHORT).show();
                        downloadIdOne = 0;
                    }
                });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            StyleableToast.makeText(MainActivity.this, "Allow External Storage permissions in App Settings", Toast.LENGTH_LONG, R.style.red).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
            break;
        }
    }

        private String executeCommand(String command, boolean standardOutExclude) {
            int readData;
            char[] buffer;
            buffer = new char[4096];
            StringBuilder outputData;
            BufferedReader reader;
            try {
                Process process = Runtime.getRuntime().exec(command);
                if (standardOutExclude) {
                    return "";
                }
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                outputData = new StringBuilder();
                while ((readData = reader.read(buffer)) > 0)
                {
                    outputData.append(buffer, 0, readData);
                }
                reader.close();
                process.waitFor();
                return outputData.toString();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }


