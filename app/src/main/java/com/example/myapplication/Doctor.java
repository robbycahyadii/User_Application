package com.example.myapplication;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String contactInfo;
    private int pengalaman;

    public Doctor(int id, String name, String specialization, String contactInfo, int pengalaman) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.contactInfo = contactInfo;
        this.pengalaman = pengalaman;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getContactInfo() {
        return "email: " + contactInfo;
    }

    public String getPengalaman() {
        return "pengalaman: " + pengalaman + " tahun";
    }
    //public int getPengalaman() { return pengalaman; }
}
