package com.example.mywalletproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import com.example.mywalletproject.ApiService;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;
    private SharedPreferences sharedPreferences;
    private CardStorage cardStorage;

    // generare uuid la prima initiere si il folosim la inrolare
    private static final String PREFS_NAME = "MyAppPrefs"; // Numele fișierului SharedPreferences
    private static final String UUID_KEY = "unique_device_id"; // Cheia pentru UUID în SharedPreferences00

    //toate apelurile de api folosite
    private static final String BASE_URL = "http://127.0.0.1:5000"; // Folosim 10.0.2.2 pe emulator Android

    private String uniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        uniqueID = getUniqueDeviceID();

        // Afișează UUID-ul în log
        Log.d(TAG, "Device Unique ID: " + uniqueID);

        cardStorage = new CardStorage(this);
        // Inițializare SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //TESTEEEE
        //Log.d(TAG,"Salkuuuuuuut");
        //test creare, citire
        //List<StoreCard> cardList = new ArrayList<>();
        //cardList.add(new StoreCard("LIDL", "123"));
        //cardList.add(new StoreCard("KAUFLAND", "345"));
        //cardStorage.saveCards(cardList);
        //cardStorage.removeCardById("345");
        //Log.d(TAG,"Am sters");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<StoreCard> savedCards = cardStorage.getCards();

        if (savedCards != null && !savedCards.isEmpty()) {
            cardAdapter = new CardAdapter(savedCards, position -> {
                StoreCard selectedCard = savedCards.get(position);
                Toast.makeText(MainActivity.this, "Card selectat: " + selectedCard.getName(), Toast.LENGTH_SHORT).show();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Opțiuni Card")
                        .setMessage("Ce vrei să faci cu cardul " + selectedCard.getName() + "?")
                        .setPositiveButton("Șterge", (dialog, which) -> {
                            // Logica pentru a șterge cardul
                            // removeCard(selectedCard);
                            cardStorage.removeCardById(selectedCard.getCardId());
                            recreate();


                            Toast.makeText(MainActivity.this, "Cardul a fost șters.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Aplică cupon", (dialog, which) -> {
                            // Logica pentru a aplica cuponul
                            //applyCouponToCard(selectedCard);
                            Toast.makeText(MainActivity.this, "Cuponul a fost aplicat.", Toast.LENGTH_SHORT).show();
                        })
                        .setNeutralButton("Anulează", (dialog, which) -> {
                            // Do nothing (închide dialogul)
                        })
                        .show();

            });


            recyclerView.setAdapter(cardAdapter);
        } else {
            Toast.makeText(this, "Nu sunt carduri salvate.", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Lista de carduri salvate:");
        for (StoreCard card : savedCards) {
            Log.d(TAG, "Nume: " + card.getName() + ", ID Card: " + card.getCardId());
        }

        //Adaugare card Icon
        Button button1 = findViewById(R.id.button);

        // Deschidere formulare inrolare card
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                showFormDialog();
            }
        });
    }


    //FEREASATRA INROLARE CARD (+)
    private void showFormDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_form, null);

        Spinner spinner = dialogView.findViewById(R.id.spinner);
        EditText editText = dialogView.findViewById(R.id.editText);
        Button submitButton = dialogView.findViewById(R.id.submit_button);

        //Apel de db pentru extragere optiuni Participanti la Program (Lidl, Kaufland etc.)
        String[] spinnerOptions = {"LIDL", "KAUFLAND"};

        // Creează un ArrayAdapter folosind opțiunile hardcodate
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //Aici am definit fereastra pentru adaugare card
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adaugati Card")
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        // Buton submit fereasta inrolare
        submitButton.setOnClickListener(v -> {

            //Apel de api catre serverul de auth + crare card / Mesaj respingere inrolare
            //saveData(spinner,editText); testul de api


                try {
                    sendPostRequest(spinner, editText, uniqueID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            dialog.dismiss();

            recreate();
        });

        // Arată dialogul
        dialog.show();
    }

    private void saveData(Spinner sp, EditText et) {
        String selectedOption = sp.getSelectedItem().toString();  // Obține selecția din Spinner
        String enteredText = et.getText().toString();            // Obține textul din EditText

        // Exemplar de salvare a unei liste de carduri
        cardStorage.saveCards(selectedOption, enteredText);
        // Afișează un mesaj de confirmare (opțional)
        //Toast.makeText(this, "Datele au fost salvate!", Toast.LENGTH_SHORT).show();
    }


    private String getUniqueDeviceID() {
        // Accesăm SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Verificăm dacă există un UUID salvat
        String uniqueID = sharedPreferences.getString(UUID_KEY, null);

        // Dacă nu există un UUID salvat, îl generăm și îl salvăm
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString(); // Generăm un UUID unic
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(UUID_KEY, uniqueID); // Salvăm UUID-ul
            editor.apply(); // Aplicăm schimbările
        }

        return uniqueID;
    }

    private int sendPostRequest(Spinner sp, EditText et, String UUID) throws Exception {
        SSLTrustAllCertificates.disableSSLValidation();

            // Obține selecția din Spinner și textul din EditText
            String shopName = sp.getSelectedItem().toString();
            String cnp = et.getText().toString();

            // Creăm datele pentru cererea POST într-un format de formular
            String postData = "cnp=" + URLEncoder.encode(cnp, "UTF-8") +
                    "&shop_name=" + URLEncoder.encode(shopName, "UTF-8") +
                    "&adresa_mac=" + URLEncoder.encode(UUID, "UTF-8");

            Log.d(TAG, postData);

            // URL-ul serverului
            String url_in = BASE_URL + "/enroll_card";  // Înlocuiește cu URL-ul corect
            String method_in = "POST"; // Metoda HTTP
            String outputString = ""; // Pentru a stoca răspunsul serverului

            try {
                // Creăm conexiunea HTTP
                URL url = new URL(url_in);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod(method_in);

                // Setăm antetul pentru cererea POST (folosim application/x-www-form-urlencoded)
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                Log.d(TAG,"BUNAAA");
                // Trimitem corpul cererii
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();

                // Obținem codul de răspuns
                int responseCode = conn.getResponseCode();
                Log.d(TAG, String.valueOf(responseCode));
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                    throw new RuntimeException("Failed : HTTP error code : " + responseCode);
                }

                // Citim răspunsul serverului
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    responseBuilder.append(output);
                }
                br.close();
                conn.disconnect();

                // Salvăm răspunsul în outputString
                outputString = responseBuilder.toString();
                Log.d(TAG, "Răspuns server: " + outputString);

                // Dacă răspunsul a fost de succes, returnează 150
                return 150;

            } catch (IOException e) {
                e.printStackTrace();
                return 404; // Returnăm un cod de eroare în caz de eșec
            }


    }

}



