package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView doctorsListView;
    private DoctorsListAdapter adapter;
    private ArrayList<Doctor> doctorsList;
    private Button medicineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doctorsListView = findViewById(R.id.doctorsListView);
        medicineButton = findViewById(R.id.medicineButton);
        doctorsList = new ArrayList<>();
        adapter = new DoctorsListAdapter(this, doctorsList);
        doctorsListView.setAdapter(adapter);

        doctorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Doctor selectedDoctor = doctorsList.get(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("user_id", 1); // Assuming user_id is always 1
                intent.putExtra("doctor_id", selectedDoctor.getId());
                startActivity(intent);
            }
        });

        fetchDoctors();

        medicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchMedicineInfo();
            }
        });
    }

    private void fetchDoctors() {
        String url = "http://192.168.185.62:3000/getDoctors"; // Replace with your server IP

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject doctorObject = response.getJSONObject(i);
                                int id = doctorObject.getInt("id");
                                String name = doctorObject.getString("name");
                                String specialization = doctorObject.getString("specialization");
                                String contactInfo = doctorObject.getString("contact_info");
                                int pengalaman = doctorObject.getInt("pengalaman");

                                Doctor doctor = new Doctor(id, name, specialization, contactInfo, pengalaman);
                                doctorsList.add(doctor);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void fetchMedicineInfo() {
        String url = "http://192.168.185.62:3000/getUser?user_id=1"; // Replace with your server IP and user ID

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String medicine = jsonObject.getString("medicine");
                            int qty = jsonObject.getInt("qty");
                            showMedicineOverlay(medicine, qty);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void showMedicineOverlay(String medicine, int qty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.overlay_medicine_info, null);
        builder.setView(dialogView);

        TextView medicineInfoTextView = dialogView.findViewById(R.id.medicineInfoTextView);
        medicineInfoTextView.setText("Jenis obat yang terakhir kali disarankan adalah " + medicine + " " + qty + " buah.");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
