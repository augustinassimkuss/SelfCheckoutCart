package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baka2.Tools.Global;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class Connect extends AppCompatActivity {

    public String session_id = "";
    public String cart_id = "";
    private static final String TAG = "Connect";
    public Button startShop;
    public Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_main);
        final String user_id = getIntent().getStringExtra("client_id");
        System.out.println("kliento id" + user_id);

        startShop = findViewById(R.id.connectBtn);
        startShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startShopping(Global.clientDataGlobal.getId());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        backButton = findViewById(R.id.connectBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });

    }
    public void goHome()
    {
        Intent goHome = new Intent(this, HomeScreen.class);
        startActivity(goHome);
    }
    public void startShopping(int user_id) throws ExecutionException, InterruptedException {
        final EditText cartCodeEdit = findViewById(R.id.connectInput);
        final String cartCode = cartCodeEdit.getText().toString();
        final TextView cartText = findViewById(R.id.connectText);
        //final TextView cartApprovalText = findViewById(R.id.connectValid);


        String result = new ConnectCartHTTP().execute(Global.urlReturn("register_cart.php"), String.valueOf(user_id), cartCode, null).get();
        System.out.println(result);
        if(result.equals("OK"))
        {
            Intent shoppinIntent = new Intent(this, ShoppingInProgress.class);
            shoppinIntent.putExtra("session_id", session_id);
            shoppinIntent.putExtra("cart_code", cartCode);
            shoppinIntent.putExtra("cart_id", cart_id);
            startActivity(shoppinIntent);
        }
    }

    public class ConnectCartHTTP extends AsyncTask<String, String, String> {
        public ConnectCartHTTP() {
            //set context variables if required
        }
        int progressCounter = 1;
        AlertDialog alertDialog;
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(Connect.this);
        StringBuffer response = new StringBuffer();

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Jungiamasi...");
            actionProgressDialog.setCancelable(true);
            actionProgressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String id = params[1];
            String cart = params[2];
            OutputStream out = null;

            //Jungimasis i URL per HTTP
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                //Kuriam JSON POST requestui
                String jsonInputString = "{" +
                        "\"user_id\": \"" + id + "\", " +
                        "\"cart_number\": \"" + cart + "\"}";

                //Surasom viska i bufferi
                try (OutputStream oStream = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    oStream.write(input, 0, input.length);
                }
                System.out.println("Issiusta");
                //Skaitom response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                }
                Log.e(TAG, "response: " + response.toString());
                JSONObject jsonObject = new JSONObject(response.toString());
                Log.e(TAG, "response: " + response.toString());

                if(jsonObject.getString("status").equals("OK"))
                {
                    session_id = jsonObject.getString("assigned_cart_id");
                    cart_id = jsonObject.getString("cart_id");
                    publishProgress("OK");
                    finalResult = "OK";
                }
                else if(jsonObject.getString("status").equals("no_cart")) {
                    publishProgress("ERROR","no_cart");
                    openDialog("Nepavyko prisijungti:","Toks vežimėlis nėra registruotas");
                    finalResult = "ERROR";
                }
                else
                {
                    publishProgress("ERROR","used_cart");
                    openDialog("Nepavyko prisijungti:","Vežimėlis užimtas");
                    finalResult = "ERROR";
                }
                urlConnection.disconnect();
                //publishProgress();
            } catch (Exception e) {
                Log.e(TAG, "KLAIDA! " + e.toString());
            }

            return finalResult;
        }
        @Override
        protected void onProgressUpdate(String... values) {
           if(progressCounter == 1)
            {
                if(values[0].equals("OK")) {
                    progressCounter -= 1;
                }
                else if(values[1].equals("no_cart"))
                {
                    progressCounter -= 1;
                    openDialog("Nepavyko prisijungti:","Toks vežimėlis nėra registruotas");
                }
                else
                {
                    progressCounter -= 1;
                    openDialog("Nepavyko prisijungti:","Vežimėlis užimtas");
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operatio
            System.out.println(result);
            actionProgressDialog.dismiss();
            }
        }
    public void openDialog(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setCancelable(true);
        dialog.setMessage(message);
        dialog.setPositiveButton("Gerai", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    }

