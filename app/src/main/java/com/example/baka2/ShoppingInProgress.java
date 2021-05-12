package com.example.baka2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class ShoppingInProgress extends AppCompatActivity {
    private static final String TAG = "Shopping in progress";
    ClientData clientData = new ClientData();
    Button payBtn;
    Button backBtn;
    int order_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_in_progress);
        clientData = Global.clientDataGlobal;
        final String cart_code = getIntent().getStringExtra("cart_code");
        final String cart_id = getIntent().getStringExtra("cart_id");
        final String session_id = getIntent().getStringExtra("session_id");

        final TextView sessionId = findViewById(R.id.shoppingSessionId);
        sessionId.append(session_id);
        final TextView cartId = findViewById(R.id.shoppingCartId);
        cartId.append(cart_code);

        payBtn = findViewById(R.id.shoppingPayBtn);
        if (clientData.getCard_number() == null) {
            payBtn.setEnabled(false);
            final TextView shoppingText = findViewById(R.id.shoppingCheckText);
            shoppingText.setText("Nepridėtas apmokėjimo metodas.\n Už šį apsipirkimą galima atsiskaityti tik kasoje");
        }

        backBtn = findViewById(R.id.shoppingBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomePage();
            }
        });

        payBtn = findViewById(R.id.shoppingPayBtn);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    validatePayment(session_id, cart_id);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void validatePayment(String session_id, String cart_id) throws ExecutionException, InterruptedException {
        String response = new PaymentHTTP().execute(Global.urlReturn("finish_shopping.php"),cart_id,session_id, null).get();
        if(response.equals("OK"))
        {
            openOrderPage();
        }
    }
    private void openOrderPage() {
        Intent intent = new Intent(this, OrdersLists.class);
        intent.putExtra("order_id", order_id);
        startActivity(intent);
    }
    private void openHomePage() {
        Intent shoppingBackIntent = new Intent(this, HomeScreen.class);
        startActivity(shoppingBackIntent);
    }
    public class PaymentHTTP extends AsyncTask<String, String, String> {
        public PaymentHTTP() {
            //set context variables if required
        }
        int progressCounter = 1;
        AlertDialog alertDialog;
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(ShoppingInProgress.this);
        StringBuffer response = new StringBuffer();

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Vykdoma...");
            actionProgressDialog.setCancelable(true);
            actionProgressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String cart_id = params[1];
            String session_id = params[2];
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
                        "\"session_id\": \"" + session_id + "\", " +
                        "\"cart_id\": \"" + cart_id + "\"}";

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
                    publishProgress("OK");
                    order_id = jsonObject.getInt("order_id");
                    finalResult = "OK";
                }
                else
                {
                    publishProgress("ERROR",jsonObject.getString("reason"));
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
                    openDialog("Sėkmingai apmokėta","");
                }
                else if(values[1].equals("not_finished"))
                {
                    progressCounter -= 1;
                    openDialog("Apsipirkimas nebaigtas","Sekite vežimęlio instrukcijas");
                }
                else
                {
                    progressCounter -= 1;
                    openDialog("Laikini trugdžiai:","");
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