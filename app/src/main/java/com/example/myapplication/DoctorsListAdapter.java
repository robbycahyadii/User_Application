package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DoctorsListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Doctor> doctorsList;

    public DoctorsListAdapter(Context context, ArrayList<Doctor> doctorsList) {
        this.context = context;
        this.doctorsList = doctorsList;
    }

    @Override
    public int getCount() {
        return doctorsList.size();
    }

    @Override
    public Object getItem(int position) {
        return doctorsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.doctor_list_item, parent, false);
        }

        Doctor doctor = doctorsList.get(position);

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView specializationTextView = convertView.findViewById(R.id.specializationTextView);
        TextView contactInfoTextView = convertView.findViewById(R.id.contactInfoTextView);
        TextView pengalamanTextView = convertView.findViewById(R.id.pengalamanTextView);

        nameTextView.setText(doctor.getName());
        specializationTextView.setText(doctor.getSpecialization());
        contactInfoTextView.setText(doctor.getContactInfo());
        pengalamanTextView.setText(String.valueOf(doctor.getPengalaman()));

        return convertView;
    }
}
