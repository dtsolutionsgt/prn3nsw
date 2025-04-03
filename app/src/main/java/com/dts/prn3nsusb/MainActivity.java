package com.dts.prn3nsusb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout rel1;

    private ArrayList<clsComanda> items= new ArrayList<clsComanda>();
    private clsComanda item;

    private UsbAdmin mUsbAdmin=null;

    private ArrayList<String> lines= new ArrayList<String>();

    private String ps;
    private int connint;
    private boolean connected;

    private byte SendCut[]={0x0a,0x0a,0x1d,0x56,0x01};
    private byte SendCash[]={0x1b,0x70,0x00,0x1e,(byte)0xff,0x00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grantPermissions();
    }

    private void startApplication() {
        try {

            rel1 = findViewById(R.id.relProgress);

            mUsbAdmin=new UsbAdmin(this);

            Handler mtimer = new Handler();
            Runnable mrunner= () -> intentaConexion();
            mtimer.postDelayed(mrunner,500);

        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . " +e.getMessage());
        }
    }


    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doUSB(View view) {
        openUSB();
    }

    public void doTest(View view) {
        intentaConexion();
    }

    //endregion

    //region Conexion

    private void intentaConexion() {
        rel1.setVisibility(View.VISIBLE);
        connint=0;
        connected=false;
        connectaUSB();
    }

    private void connectaUSB() {
        try {
            Handler mtimer = new Handler();
            Runnable mrunner= () -> {
                if (connint<10) {
                    validaConexion();
                    if (connected) {
                        imprimeDocumento();
                    } else {
                        connectaUSB();
                    }
                } else {
                    sinConexion();
                }
            };
            mtimer.postDelayed(mrunner,500);
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void validaConexion() {
        connected=conTest();
        connint++;
    }

    private void sinConexion() {
        rel1.setVisibility(View.INVISIBLE);
        msgclose("Sin conexión a la impresora");
    }

    private void imprimeDocumento() {
        try {
            if (cargaArchivo()) {
                ps+="\n\n\n\n"+((char) SendCut[0])+((char) SendCut[1])+((char) SendCut[2])+((char) SendCut[3])+((char) SendCut[4]);
                PrintfData(ps.getBytes("GBK"));
                PrintfData(SendCash);
                closeSession();
            } else {
                msgclose("Archivo de impresión no existe.");
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Main

    private void closeSession() {
        try {
            mUsbAdmin.Closeusb();
            Handler mtimer = new Handler();
            Runnable mrunner=new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };
            mtimer.postDelayed(mrunner,300);

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region USB

    public boolean conTest() {
        mUsbAdmin.Openusb();
        if (!mUsbAdmin.GetUsbStatus()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean PrintfData(byte[]data) {
        if (!mUsbAdmin.sendCommand(data)) return false; else return true;
    }

    //endregion

    //region Aux

    private void toast(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void toastlong(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    //endregion

    //region Dialogs

    private void msgbox(String msg) {
        try {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Impresion 3nStar");
            dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.setNeutralButton("OK", (dialog1, which) -> {});
            dialog.show();

        } catch (Exception ex) {
            toast(ex.getMessage());
        }
    }

    private void msgclose(String msg) {
        try {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Impresion 3nStar USB");
            dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.setNeutralButton("OK", (dialog1, which) -> closeSession());
            dialog.show();

        } catch (Exception ex) {
            toast(ex.getMessage());
        }
    }

    //endregion

    //region Permission

        private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 20) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . " +e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startApplication();
            } else super.finish();
        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Activity Events


    //endregion

    private void openUSB() {
        String ss;

        try {
                if (conTest()) {
                    toast("USB Opened");
                ss="To koukas\n ty\n chytroline!\n\n";
                PrintfData(ss.getBytes("GBK"));

                Handler mtimer = new Handler();
                Runnable mrunner= () -> PrintfData(SendCut);
                mtimer.postDelayed(mrunner,200);

            } else {
                toast("Not open");
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    boolean cargaArchivo() {
        BufferedReader br=null;
        FileReader fr;
        String line;

        lines.clear();

        try {
            String filename=Environment.getExternalStorageDirectory().toString()+"/print.txt";
            File file = new File(filename);

            fr=new FileReader(file);
            br = new BufferedReader(fr);

            lines.clear();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            fr.close();
            br.close();

            ps="";
            for (int i = 0; i <lines.size(); i++) {
                ps+=lines.get(i)+"\n";
            }

            return true;
        } catch (Exception e) {
            try {
                br.close();
            } catch (IOException ee) {}
            return false;
        }
    }
}