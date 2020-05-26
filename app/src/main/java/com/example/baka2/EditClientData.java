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

public class EditClientData extends AppCompatActivity {

    private static final String TAG = "EditClientData";
    ClientData clientData = new ClientData();
    ClientData editedData = new ClientData();
    Button backBtn;
    Button saveBtn;
    Button paymentBtn;
    Button removePaymentBtn;
    EditText fullNameEdit;
    EditText emailEdit;
    EditText numberEdit;
    EditText dateEdit;
    EditText passwordEdit;
    EditText passwordREdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_client_data_main);

        clientData = Global.clientDataGlobal;
        fullNameEdit = findViewById(R.id.editNameInput);
        emailEdit = findViewById(R.id.editElInput);
        numberEdit = findViewById(R.id.editPhoneInput);
        dateEdit = findViewById(R.id.editDateInput);
        passwordEdit  = findViewById(R.id.editPswInput);
        passwordREdit  = findViewById(R.id.editPswRepeatInput);
        editedData.setId(clientData.getId());
        editedData.setFull_name(fullNameEdit.getText().toString());
        editedData.setEmail(emailEdit.getText().toString());
        editedData.setBirth_date(dateEdit.getText().toString());
        editedData.setPhone_number(numberEdit.getText().toString());
        editedData.setPassword(passwordEdit.getText().toString());
        setHints();



        backBtn = findViewById(R.id.editBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainPage();
            }
        });

        saveBtn = findViewById(R.id.editSaveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateDetails();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        paymentBtn = findViewById(R.id.editPaymentBtn);
        removePaymentBtn = findViewById(R.id.editRemovePaymentBtn);

        if(clientData.getCard_number() == null)
        {
            removePaymentBtn.setVisibility(View.GONE);
            paymentBtn.setText("Pridėti apmokėjimo kortelę");
        }

        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                openAddPaymentPage();
            }
        });
        removePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                try {
                    removePaymentCard();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void onBackPressed(){
            Intent openMain = new Intent(this, HomeScreen.class);
            startActivity(openMain);
    }

    public void openMainPage()
    {
        Intent openMain = new Intent(this, HomeScreen.class);
        startActivity(openMain);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openAddPaymentPage()
    {
        Intent openAddPaymentIntent = new Intent(this, RegisterPaymentScreen.class);
        final Bundle bundle = new Bundle();
        bundle.putBinder("ClientData", new ObjectWrapperForBinder(clientData));
        openAddPaymentIntent.putExtras(bundle);
        startActivity(openAddPaymentIntent);
    }
    public void removePaymentCard() throws ExecutionException, InterruptedException {
        new removeCardHTTP().execute(Global.urlReturn("edit_user_payment.php"), null).get();
    }
    public void setHints()
    {
        fullNameEdit.setText(clientData.getFull_name());
        emailEdit.setText(clientData.getEmail());
        dateEdit.setText(clientData.getBirth_date());
        numberEdit.setText(clientData.getPhone_number());
        passwordEdit.setText(clientData.getPassword());
        passwordREdit.setText(clientData.getPassword());
    }
    public boolean validateDetails()
    {
        final String psw = passwordEdit.getText().toString();
        final String psw2 = passwordREdit.getText().toString();
        final TextView pswText = findViewById(R.id.editPswText);
        final TextView psw2Text = findViewById(R.id.editPswRepeatText);

        final String email = emailEdit.getText().toString();
        final TextView emailText =  findViewById(R.id.editElText);
        int falseCount = 0;
        if(!psw.equals(psw2) || psw.length() < 1 || psw2.length() < 1) {
            pswText.setTextColor(Color.RED);
            psw2Text.setTextColor(Color.RED);
            falseCount++;
        }
        if(!Global.isValidEmail(email))
        {
            emailText.setTextColor(Color.RED);
            falseCount++;
        }
        if(falseCount != 0)
            return false;
        else{
            editedData.setId(clientData.getId());
            editedData.setFull_name(fullNameEdit.getText().toString());
            editedData.setEmail(email);
            editedData.setBirth_date(dateEdit.getText().toString());
            editedData.setPhone_number(numberEdit.getText().toString());
            editedData.setPassword(psw);
            editedData.setPayment_full_name(clientData.getPayment_full_name());
            editedData.setPayment_address(clientData.getPayment_address());
            editedData.setCard_number(clientData.getCard_number());
            editedData.setExpiry_date(clientData.getExpiry_date());
            editedData.setCcv(clientData.getCcv());
            return true;
        }
    }
    public void updateDetails() throws ExecutionException, InterruptedException {
        if(validateDetails())
        {
            String response = new editHTTP().execute(Global.urlReturn("edit_user.php"), null).get();

        }
    }

    public class editHTTP extends AsyncTask<String, String, String> {
        ProgressDialog actionProgressDialog = new ProgressDialog(EditClientData.this);
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
                            "\"id\": \"" + editedData.getId() + "\", " +
                            "\"full_name\": \"" + editedData.getEmail() + "\", " +
                            "\"password\": \"" + editedData.getPassword() + "\", " +
                            "\"birth_date\": \"" + editedData.getBirth_date() + "\", " +
                            "\"phone_number\": \"" + editedData.getPhone_number() + "\", " +
                            "\"email\": \"" + editedData.getEmail() + "\"}";
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
                    Global.clientDataGlobal = editedData;
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
                    openDialog("Paskyra sėkmingai atnaujinta", "");
                } else {
                    counter -= 1;

                    if (values[1].equals("used_email")) {
                        openDialog("Duomenų atnaujinti nepavyko", "Toks El. Paštas jau užimtas");
                    } else {
                        openDialog("Duomenų atnaujinti nepavyko", "Toks telefono numeris jau užimtas");
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            actionProgressDialog.dismiss();
        }
    }

    public class removeCardHTTP extends AsyncTask<String, String, String> {
        ProgressDialog actionProgressDialog = new ProgressDialog(EditClientData.this);
        StringBuffer response = new StringBuffer();
        String finishStatus = "";
        int counter = 1;

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Naikinama...");
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
                            "\"id\": \"" + editedData.getId() + "\", " +
                            "\"payment_full_name\": \"\", " +
                            "\"payment_address\": \"\", " +
                            "\"card_number\": \"\", " +
                            "\"expiry_date\": \"\", " +
                            "\"ccv\": \"\"}";
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
                    editedData.setPayment_full_name(null);
                    editedData.setPayment_address(null);
                    editedData.setCard_number(null);
                    editedData.setExpiry_date(null);
                    editedData.setCcv(null);
                    Global.clientDataGlobal = editedData;
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
                    openDialog("Kortelė sėkmingai panaikinta", "");
                } /*else {
                    counter -= 1;

                    if (values[1].equals("used_email")) {
                        openDialog("Duomenų atnaujinti nepavyko", "Toks El. Paštas jau užimtas");
                    } else {
                        openDialog("Duomenų atnaujinti nepavyko", "Toks telefono numeris jau užimtas");
                    }
                }*/
            }
        }

        @Override
        protected void onPostExecute(String result) {
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



