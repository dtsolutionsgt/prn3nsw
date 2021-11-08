package com.dts.prn3nsw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout rel1;

    private  ArrayList<clsComanda> items= new ArrayList<clsComanda>();
    private clsComanda item;

    private Socketmanager mSockManager;
    private Runnable mUpdate;
    private Handler mHandler;

    private String err;
    private int errcnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grantPermissions();
    }

    private void startApplication() {
        try {

            rel1 = findViewById(R.id.relProgress);

            mSockManager=new Socketmanager(MainActivity.this);

            Handler mtimer = new Handler();
            Runnable mrunner=new Runnable() {
                @Override
                public void run() {
                    procesaArchivos();
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

    //endregion

    //region Main

    private void procesaArchivos() {
        String fname,sn="",path;
        ArrayList<String> names= new ArrayList<String>();

        path = Environment.getExternalStorageDirectory().toString();
        items.clear();

        try {
            File directory = new File(path);
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                fname=files[i].getName();
                if (fname.indexOf("comanda")==0) {
                    if (fname.indexOf(".txt")>=0) {
                         item=new clsComanda(MainActivity.this);
                         if (item.cargar(fname)) items.add(item);
                    }
                }
            }

            if (items.size()>0) {
                processPrint();
            } else {
                finish();
                //msgclose("No existen documentos pendientes de impresión");
            }
        } catch (Exception e) {
            toastlong(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void processPrint() {
        String ss;

        try {

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
                ss="Ocurrio un error :\n"+err+"\nSin impresion : "+errcnt+" / "+items.size();
                msgclose(ss);
            }

        } catch (Exception e) {
            msgclose(e.getMessage());
        }

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

            dialog.setTitle("Impresion 3nStar");
            dialog.setMessage(msg);
            dialog.setCancelable(false);

            dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
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

}