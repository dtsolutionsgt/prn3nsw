package com.dts.prn3nsusb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout rel1;

    private ArrayList<clsComanda> items= new ArrayList<clsComanda>();
    private clsComanda item;

    private UsbAdmin mUsbAdmin=null;

    private Runnable mUpdate;
    private Handler mHandler;

    private ArrayList<String> lines= new ArrayList<String>();

    private String err,ss,ps;
    private int errcnt,connint;
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
            Runnable mrunner=new Runnable() {
                @Override
                public void run() {
                    intentaConexion();
                    //procesaArchivo();
                }
            };
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
        //procesaArchivo();
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
            Runnable mrunner=new Runnable() {
                @Override
                public void run() {
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
            ps="123456";

            ps+="\n\n\n\n"+((char) SendCut[0])+((char) SendCut[1])+((char) SendCut[2])+((char) SendCut[3])+((char) SendCut[4]);
            PrintfData(ps.getBytes("GBK"));

            closeSession();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Main

    private void procesaArchivo() {
        String fname,sn="",path;
        ArrayList<String> names= new ArrayList<String>();

        path = Environment.getExternalStorageDirectory().toString();
        items.clear();

        try {
            File directory = new File(path);
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                fname=files[i].getName();
                if (fname.indexOf("print")==0) {
                    if (fname.indexOf(".txt")>=0) {

                        cargaArchivo();

                        /*
                        item=new clsComanda(MainActivity.this);

                        if (item.cargar(fname)) {
                            items.add(item);
                        } else {
                            msgclose("No se puede leer archivo de impresión.");return;
                        }
                        */
                    }
                }
            }

            //if (items.size()>0) {
                processPrint();
            /*
            } else {
                toastlong("No existen documentos pendientes de impresión");
                finish();
            }

             */
        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void processPrint() {
        try {

            if (!conTest()) {
                msgclose("No se puede conectar a la impresora.");return;
            }

            err="";errcnt=0;
            for (int i = 0; i < items.size(); i++) {

                item=items.get(i);

                if (!item.print()) {
                    err=item.error;errcnt++;
                }

                try {
                    Thread.sleep(500);
                } catch (Exception ee) {}
            }

            if (errcnt==0) {
                toast("Impresion completa");finish();
            } else {
                ss="Ocurrio un error :\n"+err;
                msgclose(ss);
            }
        } catch (Exception e) {
            msgclose(e.getMessage());
        }
    }

    private void cargaArchivo() {
        try {
            lines.clear();
            lines.add("1234567890");
            lines.add("1234567890");
            lines.add("1234567890");
            lines.add("");
            lines.add("");
            lines.add("");

            ps="";
            for (int i = 0; i <lines.size(); i++) {
                ps+=lines.get(i)+"\n";
            }

            procesaImpresion();

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void procesaImpresion() {
        toast("print 1");
        if (imprimeArchivo()) {
            finish();
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            toast("print 2");

            if (imprimeArchivo()) {
                finish();
            } else {
                msgclose(err);
            }
        }
    }

    private boolean imprimeArchivo() {
        try {
            if (conTest()) {
                PrintfData(ps.getBytes("GBK"));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            err=e.getMessage();return false;
        }
    }

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

            dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
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

            dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    closeSession();
                }
            });
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

    private void openUSB2() {
        String ss;

        try {
            if (conTest()) {
                toast("USB Opened");

                Handler mtimer = new Handler();
                Runnable mrunner=new Runnable() {
                    @Override
                    public void run() {
                        //PrintfData(SendCut);
                    }
                };
                mtimer.postDelayed(mrunner,200);

            } else {
                toast("Not open");
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void openUSB() {
        String ss;

        try {
                if (conTest()) {
                    toast("USB Opened");
                ss="To koukas\n ty\n chytroline!\n\n";
                PrintfData(ss.getBytes("GBK"));

                Handler mtimer = new Handler();
                Runnable mrunner=new Runnable() {
                    @Override
                    public void run() {
                        PrintfData(SendCut);
                    }
                };
                mtimer.postDelayed(mrunner,200);

            } else {
                toast("Not open");
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }




}