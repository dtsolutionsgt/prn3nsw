package com.dts.prn3nsusb;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class clsComanda {

    public String tipo,nombre,IP;
    public ArrayList<String> lines= new ArrayList<String>();
    private Context cont;
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
    //endregion
}