package edu.utk.biodynamics.icloudecg.DatabaseUtils;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by DSClifford on 8/8/2015.
 */

public class ECGRecord implements Serializable {
    private String id;
    private String email;
    private String diagnosis = "";
    private String patientGender = ""; //
    private String patientName = ""; //
    private int patientAge;
    private String str_patientAge = ""; //
    private String qualityGrade;
    private String suspectedMisplacement;
    private String upldDate;
    private String record_notes = ""; //
    private String basePath;
    private double maxHR;
    private double maxBR;

    public String getId() {
        return id;
    }
    public void setId(String string) {
        this.id = string;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getPatientGender() {
        return patientGender;
    }
    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }
    public int getPatientAge() {
        return patientAge;
    }
    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }
    public String getQualityGrade() {
        return qualityGrade;
    }
    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }
    public String getSuspectedMisplacement() {
        return suspectedMisplacement;
    }
    public void setSuspectedMisplacement(String suspectedMisplacement) {
        this.suspectedMisplacement = suspectedMisplacement;
    }
    public String getStr_patientAge() {
        return str_patientAge;
    }
    public void setStr_patientAge(String str_patientAge) {
        this.str_patientAge = str_patientAge;
    }
    public String getupDate() {
        // TODO Auto-generated method stub
        return upldDate;
    }
    public void setupDate(Timestamp tstamp) {
        // TODO Auto-generated method stub
        String upDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tstamp);
        this.upldDate = upDate;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public String getRecord_notes() {

        return record_notes;
    }

    public void setRecord_notes(String record_notes) {
        this.record_notes = record_notes;
    }

    public double getMaxHR() {
        return maxHR;
    }

    public void setMaxHR(double maxHR) {
        this.maxHR = maxHR;
    }

    public double getMaxBR() {
        return maxBR;
    }

    public void setMaxBR(double maxBR) {
        this.maxBR = maxBR;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
