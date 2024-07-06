package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private ListView chatListView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatListAdapter adapter;
    private ArrayList<ChatMessage> chatMessages;

    private int userId;
    private int doctorId;
    private Button btnPayment;

    private Handler handler = new Handler();
    private Runnable pollingRunnable;

    private int lastQty;
    private String lastMedicine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        btnPayment = findViewById(R.id.btnPayment);
        lastQty = -1; // Nilai default yang tidak mungkin digunakan untuk qty
        lastMedicine = "";

        chatMessages = new ArrayList<>();
        adapter = new ChatListAdapter(this, chatMessages);
        chatListView.setAdapter(adapter);

        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
        doctorId = intent.getIntExtra("doctor_id", -1);

        if (userId == -1 || doctorId == -1) {
            Toast.makeText(this, "User ID or Doctor ID not available", Toast.LENGTH_SHORT).show();
            finish(); // End the activity if IDs are not available
            return;
        }

        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Menampilkan dialog pembayaran
                showPaymentDialog(v);
            }
        });
        /*
        TextView userIdTextView = findViewById(R.id.userIdTextView);
        TextView doctorIdTextView = findViewById(R.id.doctorIdTextView);

        userIdTextView.setText("User ID: " + userId);
        doctorIdTextView.setText("Doctor ID: " + doctorId);
        */
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        fetchChatMessages();

        startPollingForMedicineChanges();
        startPollingForNewMessages(); // Tambahkan ini untuk memulai polling pesan baru
    }

    private void fetchChatMessages() {
        String url = "http://192.168.185.62:3000/getChats?user_id=" + userId + "&doctor_id=" + doctorId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            chatMessages.clear(); // Clear existing messages
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject chatObject = response.getJSONObject(i);
                                String sender = chatObject.getString("sender");
                                String message = chatObject.getString("message");

                                ChatMessage chatMessage = new ChatMessage(sender, message);
                                chatMessages.add(chatMessage);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void giveMoneyToDoctor(int userId, int doctorId, int amount) {
        String url = "http://192.168.185.62:3000/giveMoneyToDoctor";

        // Buat objek JSON untuk dikirim ke server
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);
            jsonBody.put("doctorId", doctorId);
            jsonBody.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Buat request POST untuk mengirim data JSON
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Tambahkan request ke antrian request Volley
        Volley.newRequestQueue(this).add(request);
    }

    private void sendMedicineToUser(int userId, String medicineName, int quantity) {
        String url = "http://192.168.185.62:3000/sendMedicine";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);
            jsonBody.put("medicineName", medicineName);
            jsonBody.put("quantity", quantity);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            showMedicineSentOverlay(medicineName, quantity); // Pass medicineName and quantity
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(request);
    }


    public void showPaymentDialog(View view) {
        // Buat dialog baru
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pembayaran");

        // Tambahkan input field untuk memasukkan jumlah pembayaran
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Tambahkan tombol OK
        builder.setPositiveButton("Bayar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amountStr = input.getText().toString().trim();
                if (!amountStr.isEmpty()) {
                    int amount = Integer.parseInt(amountStr);
                    giveMoneyToDoctor(userId, doctorId, amount); // Panggil fungsi untuk memberi uang ke dokter
                } else {
                    Toast.makeText(ChatActivity.this, "Masukkan jumlah pembayaran!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Tampilkan dialog
        builder.show();
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.185.62:3000/sendMessage";

        JSONObject chatParams = new JSONObject();
        try {
            chatParams.put("user_id", userId);
            chatParams.put("doctor_id", doctorId);
            chatParams.put("message", message);
            chatParams.put("sender", "user"); // Or "doctor" based on who is sending
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        messageEditText.setText(""); // Clear the input field
                        fetchChatMessages(); // Refresh chat messages
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() {
                return chatParams.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Method to show overlay when medicine sent successfully
    private void showMedicineSentOverlay(String medicineName, int quantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View overlayView = getLayoutInflater().inflate(R.layout.medicine_change_overlay, null);
        TextView overlayMessage = overlayView.findViewById(R.id.overlayMessage);
        overlayMessage.setText("Obat yang disarankan adalah " + medicineName + " " + quantity + " buah");
        builder.setView(overlayView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startPollingForNewMessages() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchChatMessages();
                handler.postDelayed(this, 5000); // Poll setiap 5 detik
            }
        }, 1000); // Mulai polling setelah 5 detik
    }

    private void startPollingForMedicineChanges() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                String url = "http://192.168.185.62:3000/getUser?user_id=" + userId;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int currentQty = response.getInt("qty");
                                    String currentMedicine = response.getString("medicine");

                                    // Periksa jika ini polling pertama kali
                                    if (lastQty == -1 && lastMedicine.isEmpty()) {
                                        lastQty = currentQty;
                                        lastMedicine = currentMedicine;
                                    } else {
                                        // Tampilkan overlay hanya jika ada perubahan
                                        if (currentQty != lastQty || !currentMedicine.equals(lastMedicine)) {
                                            lastQty = currentQty;
                                            lastMedicine = currentMedicine;
                                            showMedicineSentOverlay(currentMedicine, currentQty); // Pass medicineName and quantity
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                RequestQueue requestQueue = Volley.newRequestQueue(ChatActivity.this);
                requestQueue.add(request);

                handler.postDelayed(this, 2500); // Poll setiap 5 detik
            }
        };
        handler.post(pollingRunnable);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }

}
