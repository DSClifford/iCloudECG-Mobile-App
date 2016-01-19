package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by DSClifford on 8/8/2015.
 */
public class ECGFileWriter {
    public static void newFile(short[] ecg_toAnalyze, String randomID, String basepath) {
        short[] shorts = ecg_toAnalyze;
        PrintWriter out = null;
        File folder = new File(basepath+"/records");
        boolean success = true;
        if (!folder.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = folder.mkdir();
        }
        Log.d("Basepath",basepath);
        try {
            out = new PrintWriter(new FileOutputStream(basepath+"/records/"+randomID+".txt"));
            for (int anInt : shorts) {
                out.println(String.valueOf(anInt));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(out);

        }
        File file = new File(basepath+"/records");
        double dirSize = dirSize(file);
        Log.e("Storage", "Record Directory Size: "+((dirSize/1024)/1024)+" Mb");
    }

    private static double dirSize(File dir) {

        if (dir.exists()) {
            double result = 0.0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if(fileList[i].isDirectory()) {
                    result += dirSize(fileList [i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    private static void safeClose(PrintWriter out) {
        if (out != null) {
            out.close();
        }

    }
}
