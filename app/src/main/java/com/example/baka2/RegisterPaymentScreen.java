package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;
import com.example.baka2.Tools.ObjectWrapperForBinder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class RegisterPaymentScreen extends AppCompatActivity {

    private static final String TAG = "RegisterPaymentScreen";
    private Button registerPaymentBackButton;
    private Button registerPaymentSkipButton;
    private Button registerPaymentCompleteButton;
    ClientData clientDataP = new ClientData();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_payment_main);

        clientDataP = (ClientData) ((ObjectWrapperForBinder)getIntent().getExtras().getBinder("ClientData")).getData();

        registerPaymentBackButton = findViewById(R.id.registerPayBackBtn);
        if(clientDataP.getId() == -1)
            registerPaymentBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openRegisterPage();
                }
            });
        else
            registerPaymentBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditPage();
                }
            });

        registerPaymentCompleteButton = findViewById(R.id.registerPaySaveBtn);



        registerPaymentCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(clientDataP.getId() == -1)
                    openMainPage();
                    else
                    editPaymentCard();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        registerPaymentSkipButton = findViewById(R.id.registerPaySkipBtn);
        if(clientDataP.getId() != -1)
        {
            registerPaymentSkipButton.setVisibility(View.GONE);
            if(clientDataP.getCard_number() != null)
                registerPaymentCompleteButton.setText("Keisti kortelę");
            else
                registerPaymentCompleteButton.setText("Pridėti kortelę");
        }
        registerPaymentSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openMainPageSkipPayment();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void editPaymentCard() throws ExecutionException, InterruptedException {
        if(paymentInfoValidation())
        {
            String response = new editHTTP().execute(Global.urlReturn("edit_user_payment.php"), null).get();
            System.out.println(response);
            if(response.equals("OK"))
            {
                Global.clientDataGlobal = clientDataP;
            }
        }
    }
    public  void opendEditPageSuccess() {
        Intent editPaymentIntent = new Intent(this, EditClientData.class);
        startActivity(editPaymentIntent);
    }
    public void openEditPage(){
        Intent editIntent = new Intent(this, EditClientData.class);
        startActivity(editIntent);
    }

    public void openRegisterPage(){
        Intent paymentRegisterIntent = new Intent(this, RegisterScreen.class);
        startActivity(paymentRegisterIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openMainPage() throws ExecutionException, InterruptedException {
        if(paymentInfoValidation()) {
            String response = new RegisterHTTP().execute(Global.urlReturn("register.php"), null).get();
            System.out.println(response);
            if(response.equals("OK"))
            {
                Intent registerPaymentIntent = new Intent(this, HomeScreen.class);
                Global.clientDataGlobal = clientDataP;
                startActivity(registerPaymentIntent);
            }
        }
    }

    public boolean paymentInfoValidation()
    {
        int falseCount = 0;

        final EditText paymentNameEdit = findViewById(R.id.registerPayNameInput);
        final String paymentName = paymentNameEdit.getText().toString();

        final EditText paymentAddressEdit = findViewById(R.id.registerPayAddressInput);
        final String paymentAddress = paymentAddressEdit.getText().toString();

        final EditText cardNumberEdit = findViewById(R.id.registerPayCardInput);
        final String cardNumber = cardNumberEdit.getText().toString();
        final TextView cardNumberText = findViewById(R.id.registerPayCardText);

        final EditText cardExpiryEdit = findViewById(R.id.registerPayValidInput);
        final String cardExpiry = cardExpiryEdit.getText().toString();

        final EditText ccvEdit = findViewById(R.id.registerPayCcvInput);
        final String ccv = ccvEdit.getText().toString();
        final TextView ccvText = findViewById(R.id.registerPayCcvText);

        if(cardNumber.length() != 16)
        {
            falseCount++;
            cardNumberText.setTextColor(Color.RED);
        }
        if(ccv.length() != 3)
        {
            falseCount++;
            ccvText.setTextColor(Color.RED);
        }
        if(falseCount != 0)
        {
            return false;
        }
        else
        {
            try {
                clientDataP.setPayment_full_name(paymentName);
                clientDataP.setPayment_address(paymentAddress);
                clientDataP.setCard_number(cardNumber);
                clientDataP.setExpiry_date(cardExpiry);
                clientDataP.setCcv(ccv);
            }
            catch (NumberFormatException e) {
                System.out.println(e);
            }
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openMainPageSkipPayment() throws ExecutionException, InterruptedException {
        String response = new RegisterHTTP().execute(Global.urlReturn("register.php"), null).get();
        if(response.equals("OK")) {
            Global.clientDataGlobal = clientDataP;
            Intent registerPaymentSkipIntent = new Intent(this, HomeScreen.class);
            //final Bundle bundle = new Bundle();
            //bundle.putBinder("ClientData", new ObjectWrapperForBinder(clientDataP));
            //registerPaymentSkipIntent.putExtras(bundle);
            startActivity(registerPaymentSkipIntent);
        }
    }

    public class RegisterHTTP extends AsyncTask<String, String, String> {
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        ProgressDialog actionProgressDialog = new ProgressDialog(RegisterPaymentScreen.this);
        StringBuffer response = new StringBuffer();
        String finishStatus = "";
        int counter = 1;

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Tikrinami duomenys...");
            actionProgressDialog.setCancelable(false);
            actionProgressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
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
                        "\"full_name\": \"" + clientDataP.getEmail() + "\", " +
                        "\"password\": \"" + clientDataP.getPassword() + "\", " +
                        "\"birth_date\": \"" + clientDataP.getBirth_date() + "\", " +
                        "\"phone_number\": \"" + clientDataP.getPhone_number() + "\", " +
                        "\"email\": \"" + clientDataP.getEmail() + "\", " +
                        "\"payment_full_name\": \"" + clientDataP.getPayment_full_name() + "\", " +
                        "\"payment_address\": \"" + clientDataP.getPayment_address() + "\", " +
                        "\"card_number\": \"" + clientDataP.getCard_number() + "\", " +
                        "\"expiry_date\": \"" + clientDataP.getExpiry_date() + "\", " +
                        "\"ccv\": \"" + clientDataP.getCcv() + "\"}";

                //Surasom viska i bufferi
                try (OutputStream oStream = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    oStream.write(input, 0, input.length);
                }
                System.out.println("Atejom");
                //Skaitom response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                }

                //Tikrinam response status
                Log.e(TAG, "response: " + response.toString());
                JSONObject jArray = new JSONObject(response.toString());
                if(jArray.getString("status").equals("OK"))
                {
                    clientDataP.setId(Integer.parseInt(jArray.getString("user_id")));
                    publishProgress("OK",jArray.getString("user_id"));
                    finishStatus = "OK";
                }
                else if(jArray.getString("status").equals("ERROR"))
                {
                    publishProgress("ERROR", jArray.getString("reason"));
                    finishStatus = "ERROR";
                    //if (jArray.getString("reason").equals("used_email")) {
                        //openDialog("Registracija nepavyko", "Toks El. Paštas jau užimtas");
                    //} else {
                        //openDialog("Registracija nepavyko", "Toks telefono numeris jau užimtas");
                    //}
                }
                urlConnection.disconnect();
               // Log.e(TAG, "response: " + response.toString());
                publishProgress();
            } catch (Exception e) {
                Log.e(TAG, "KLAIDA! " + e.toString());
            }
            return finishStatus;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //tikrinam response status ir pakeiciam return

            if (counter == 1)
            {
                if (values[0].equals("OK")) {
                    counter -= 1;

                } else {
                    counter -= 1;

                    if (values[1].equals("used_email")) {
                        openDialog("Registracija nepavyko", "Toks El. Paštas jau užimtas");
                    } else {
                        openDialog("Registracija nepavyko", "Toks telefono numeris jau užimtas");
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            actionProgressDialog.dismiss();
            }
        }
    public class editHTTP extends AsyncTask<String, String, String> {
        ProgressDialog actionProgressDialog = new ProgressDialog(RegisterPaymentScreen.this);
        StringBuffer response = new StringBuffer();
        String finishStatus = "";
        int counter = 1;

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Tikrinami duomenys...");
            actionProgressDialog.setCancelable(false);
            actionProgressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
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

                //Surasom viska i bufferi
                try (OutputStream oStream = urlConnection.getOutputStream()) {
                    String jsonInputString = "{" +
                            "\"id\": \"" + clientDataP.getId() + "\", " +
                            "\"payment_full_name\": \"" + clientDataP.getPayment_full_name() + "\", " +
                            "\"payment_address\": \"" + clientDataP.getPayment_address() + "\", " +
                            "\"card_number\": \"" + clientDataP.getCard_number() + "\", " +
                            "\"expiry_date\": \"" + clientDataP.getExpiry_date() + "\", " +
                            "\"ccv\": \"" + clientDataP.getCcv() + "\"}";
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    oStream.write(input, 0, input.length);
                }
                System.out.println("Atejom");
                //Skaitom response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                }
                //Tikrinam response status
                Log.e(TAG, "response: " + response.toString());
                JSONObject jArray = new JSONObject(response.toString());
                if(jArray.getString("status").equals("OK"))
                {
                    publishProgress("OK");
                    finishStatus = "OK";
                    Global.clientDataGlobal = clientDataP;
                }
                else if(jArray.getString("status").equals("ERROR"))
                {
                    publishProgress("ERROR", jArray.getString("reason"));
                    finishStatus = "ERROR";
                }

                urlConnection.disconnect();
                publishProgress();

            } catch (Exception e) {
                Log.e(TAG, "KLAIDA! " + e.toString());
            }
            return finishStatus;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //tikrinam response status ir pakeiciam return

            if (counter == 1)
            {
                if (values[0].equals("OK")) {
                    counter -= 1;
                    openDialog("Kortelė sėkmingai pridėta", "");
                } else {
                    counter -= 1;
                    if (values[1].equals("used_card")) {
                        openDialog("Kortelės pridėti nepavyko", "Kortelė tokiu kodu jau egzistuoja mūsų sistemoje");
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            actionProgressDialog.dismiss();
        }
    }

        public void openDialog(String title, final String message) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(title);
            dialog.setCancelable(true);
            dialog.setMessage(message);
            dialog.setPositiveButton("Gerai", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(message.equals(""))
                        opendEditPageSuccess();
                }
            });
            dialog.show();
        }
    }


