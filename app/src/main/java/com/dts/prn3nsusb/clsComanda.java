package com.dts.prn3nsusb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class clsComanda {

    public String tipo,nombre,IP,error;
    public ArrayList<String> lines= new ArrayList<String>();

    private Context cont;

    private String filename;

    private UsbAdmin mUsbAdmin=null;

    private byte SendCut[]={0x0a,0x0a,0x1d,0x56,0x01};
    private byte SendCash[]={0x1b,0x70,0x00,0x1e,(byte)0xff,0x00};

    public clsComanda(Context context) {
        cont=context;
    }

    //region Public

    public boolean cargar(String fname) {
        BufferedReader br=null;
        FileReader fr;
        String line;
        int ii=0;

        lines.clear();

        try {
            filename=Environment.getExternalStorageDirectory().toString()+"/"+fname;
            File file = new File(filename);

            fr=new FileReader(file);
            br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {

                if (ii==0) {
                    tipo =line;
                } else if (ii==1) {
                    nombre =line;
                } else if (ii==2) {
                    IP=line;
                } else {
                    lines.add(line);
                }
                ii++;
            }
            fr.close();
            br.close();

            return true;
        } catch (Exception e) {
            try {
                br.close();
            } catch (IOException ee) {}
            return false;
        }
    }

    public boolean print() {
        String ps;

        error="";
        ps="\n\n"+nombre+"\n\n";

        try {
            mUsbAdmin=new UsbAdmin(cont);

            msgbox("cm 1");

            if (!connect()) {
                error ="No se puede conectar a la impresora";return false;
            }

            msgbox("cm 2");


            for (int i = 0; i <lines.size(); i++) {
                ps+=lines.get(i)+"\n";
            }

            msgbox("cm 3\n"+ps);


            //ps=ps+((char) SendCut[0])+((char) SendCut[1])+((char) SendCut[2])+((char) SendCut[3])+((char) SendCut[4]);

            printData(ps.getBytes("GBK"));

            msgbox("cm 4");



            return true;
        } catch (Exception e) {
            error = e.getMessage();return false;
        }
    }

    //endregion

    //region Private

    public boolean connect() {
        mUsbAdmin.Openusb();
        if (!mUsbAdmin.GetUsbStatus())  return false; else return true;
    }

    public boolean printData(byte[]data) {
        if(!mUsbAdmin.sendCommand(data)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        } else {
            return true;
        }
    }

    //endregion

    private void toast(String msg) {
        Toast toast= Toast.makeText(cont,msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void msgbox(String msg) {
        try {

            AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

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


}
