package com.dts.prn3nsw;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class clsComanda {

    public String tipo,nombre,IP,error;
    public ArrayList<String> lines= new ArrayList<String>();

    private Context cont;
    private Socketmanager mSockManager;

    private String filename;

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
        boolean connected;

        error="";
        ps="\n\n"+nombre+"\n\n";

        try {
            mSockManager=new Socketmanager(cont);

            connected=connect();
            if (!connected) connected=connect();
            if (!connected) connected=connect();
            if (!connected) return false;

            for (int i = 0; i <lines.size(); i++) {
                ps+=lines.get(i)+"\n";
            }

            ps=ps+((char) SendCut[0])+((char) SendCut[1])+((char) SendCut[2])+((char) SendCut[3])+((char) SendCut[4]);

            printData(ps.getBytes("GBK"));

            try {
                File file = new File(filename);
                file.delete();
            } catch (Exception e) {
                error = "No se logro borrar archivo de impresion. La impresion de va a repetir.";return false;
            }

            return true;
        } catch (Exception e) {
            error = e.getMessage();return false;
        }
    }

    //endregion

    //region Private

    public boolean connect() {
        mSockManager.mPort=9100;
        mSockManager.mstrIp=IP;
        mSockManager.threadconnect();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSockManager.getIstate()) {
            return true;
        } else {
            error="No se puede conectar al : "+IP;
            return false;
        }
    }

    public boolean printData(byte[]data) {
        mSockManager.threadconnectwrite(data);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSockManager.getIstate()) {
            return true;
        } else {
            return false;
        }
    }

    //endregion
}
