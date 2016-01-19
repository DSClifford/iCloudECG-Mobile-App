package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import edu.utk.biodynamics.icloudecg.RequestPackage;

/**
 * Created by DSClifford on 8/8/2015.
 */

public class HttpManager {

    public static void sendFile(String ip, String userName, String pass, String randomID, String basepath) {
        boolean status = false;
        try {
            FTPClient mFtpClient = new FTPClient();
            mFtpClient.setConnectTimeout(10 * 1000);
            mFtpClient.connect(InetAddress.getByName(ip));
            status = mFtpClient.login(userName, pass);
            Log.e("isFTPConnected", String.valueOf(status));
            mFtpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            BufferedInputStream buffIn = null;
            buffIn = new BufferedInputStream(new FileInputStream(basepath + "/records/" + randomID + ".txt"));
            mFtpClient.enterLocalPassiveMode();
            // ProgressInputStream progressInput = new ProgressInputStream(buffIn, progressHandler);
            mFtpClient.cwd("/");
            boolean result = mFtpClient.storeFile("var/lib/tomcat7/webapps/resources/records/" + randomID + ".txt", buffIn);
            mFtpClient.sendSiteCommand("chmod " + "444 " + "var/lib/tomcat7/webapps/resources/records/" + randomID + ".txt");
            Log.e("FileStored", String.valueOf(result));
            buffIn.close();
            mFtpClient.logout();
            mFtpClient.disconnect();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean getFile(String ip, String userName, String pass, String fileID, String basepath) {
        FileOutputStream recFileOut;
        File targetFile = new File(basepath + "/records/" + fileID + "retrieved" + ".txt");

        FTPClient ftpClient = new FTPClient();
        boolean result = false;
        try {
            ftpClient.connect(InetAddress.getByName(ip));
            ftpClient.enterLocalPassiveMode();
            ftpClient.login(userName, pass);

            targetFile.createNewFile();

            ftpClient.cwd("/");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);// Used for video
            recFileOut = new FileOutputStream(targetFile);
            result = ftpClient.retrieveFile("var/lib/tomcat7/webapps/resources/records/" + fileID + ".txt", recFileOut);

            ftpClient.disconnect();
            recFileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String getData(RequestPackage p) {

        BufferedReader reader = null;
        String uri = p.getUri();

        if (p.getMethod().equals("GET")) {
            uri += "?" + p.getEncodedParams();
            Log.e("URI", uri);
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod());

            if (p.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(p.getEncodedParams());
                writer.flush();
                writer.close();
            }

            con.disconnect();
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }

    public static void updateECGDatabase(Context ctx, ECGRecord record) {

        BufferedReader reader = null;

        RequestPackage p = new RequestPackage();
        p.setMethod("GET");
        p.setUri("http://biodynamics.engr.utk.edu/iCloudECGAppBridge/updateECG.php");
        p.setParam("name", record.getPatientName());
        p.setParam("email", record.getEmail());
        p.setParam("gender", record.getPatientGender());
        p.setParam("age",String.valueOf(record.getPatientAge()));
        p.setParam("fid", record.getId());
        p.setParam("maxHR", String.valueOf(record.getMaxHR()));
        p.setParam("maxBR", String.valueOf(record.getMaxBR()));
        p.setParam("recordNotes",record.getRecord_notes());


        String uri = p.getUri();

        if (p.getMethod().equals("GET")) {
            uri += "?" + p.getEncodedParams();
            Log.e("URI", uri);
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod());

            if (p.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(p.getEncodedParams());
                writer.flush();
                writer.close();
            }

            con.disconnect();
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    reader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void initiateAnalysis(String randomID, String email) {

        RequestPackage p = new RequestPackage();
        p.setMethod("GET");
        p.setUri("http://biodynamics.engr.utk.edu:8080/HelloTest");
        p.setParam("param1", "analysis");
        p.setParam("param2", randomID);
        p.setParam("userEmail",email);

        String uri = p.getUri();

        if (p.getMethod().equals("GET")) {
            uri += "?" + p.getEncodedParams();
            Log.e("URI", uri);
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod());
            con.connect();

            if (p.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(p.getEncodedParams());
                writer.flush();
                writer.close();
            }
            Log.d("Response: ", (Integer.toString(con.getResponseCode())));
            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testNotification(String GCMToken) {

        RequestPackage p = new RequestPackage();
        p.setMethod("GET");
        p.setUri("http://biodynamics.engr.utk.edu/iCloudECGAppBridge/testnotification.php");
        p.setParam("id", GCMToken);


        String uri = p.getUri();

        if (p.getMethod().equals("GET")) {
            uri += "?" + p.getEncodedParams();
            Log.e("URI", uri);
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod());
            con.connect();

            if (p.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(p.getEncodedParams());
                writer.flush();
                writer.close();
            }
            Log.d("Response: ", (Integer.toString(con.getResponseCode())));
            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
