package com.example.baka2;

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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class LoginScreen extends AppCompatActivity implements Serializable {
    private static final String TAG = "LoginScreen";
    private Button loginBackBtn;
    private Button loginHomeBtn;
    String[][] itemLists;
    //static String url = "http://192.168.149.145:80/baka/login.php";
    static String domainUrl = "https://computerizedselfcheckoutcart.tk/requests/app/login.php";
    static ClientData clientDatat = new ClientData();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        loginBackBtn = findViewById(R.id.loginBackBtn);
        loginBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openMainPage();
            }
        });

        loginHomeBtn = findViewById(R.id.loginLoginBtn);
        loginHomeBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            public void onClick(View v) {
                try {
                    openHomePage();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void openMainPage() {
        Intent loginBackIntent = new Intent(this, MainActivity.class);
        startActivity(loginBackIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openHomePage() throws ExecutionException, InterruptedException {
        if(loginValidation()) {
            //Jei tinkami duomenys, sudedam viska i Bundle ir perduodam kitam activity
            Intent loginHomeIntent = new Intent(this, HomeScreen.class);
            //final Bundle bundle = new Bundle();
            //bundle.putBinder("ClientData", new ObjectWrapperForBinder(clientDatat));
            //loginHomeIntent.putExtras(bundle);
            startActivity(loginHomeIntent);
        }
    }

    public boolean loginValidation() throws ExecutionException, InterruptedException {
        final EditText emailEdit = findViewById(R.id.loginElInput);
        final TextView emailText = findViewById(R.id.loginElText);
        final String email = emailEdit.getText().toString();
        final EditText pswEdit = findViewById(R.id.loginPswInput);
        final TextView pswText = findViewById(R.id.loginPswText);
        final String psw = pswEdit.getText().toString();

        if (psw.length()< 1)
        {
            pswText.setTextColor(Color.RED);
        }
        if (!Global.isValidEmail(email)){
            emailText.setTextColor(Color.RED);
        }
        if (Global.isValidEmail(email) && psw.length() > 0) {
            String response = new LoginHTTP().execute(domainUrl, email, psw, null).get();
            if(response.equals("OK")) {
                Global.clientDataGlobal = clientDatat;

                return true;
            }
        }
        return false;
    }



    public class LoginHTTP extends AsyncTask<String, String, String> {
        public LoginHTTP() {
            //set context variables if required
        }
        AlertDialog alertDialog;
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(LoginScreen.this);
        StringBuffer response = new StringBuffer();
        int progressCounter = 1;

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
            String email = params[1];
            String password = params[2];//data to post
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
                        "\"email\": \"" + email + "\", " +
                        "\"password\": \"" + password + "\"}";

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
                JSONObject jsonObject = new JSONObject(response.toString());
                Log.e(TAG, "response: " + response.toString());
                if (jsonObject.getString("status").equals("OK")) {
                    JSONObject jsonObjectUser = jsonObject.getJSONObject("user_id");
                    clientDatat.setFull_name(jsonObjectUser.getString("full_name"));
                    clientDatat.setId(jsonObjectUser.getInt("id"));
                    clientDatat.setPassword(jsonObjectUser.getString("password"));
                    clientDatat.setEmail(jsonObjectUser.getString("email"));
                    clientDatat.setPhone_number(jsonObjectUser.getString("phone_number"));
                    clientDatat.setBirth_date(jsonObjectUser.getString("birth_date"));
                    clientDatat.setPassword(jsonObjectUser.getString("password"));
                    if(jsonObjectUser.getString("card_number") != null) {
                        clientDatat.setPayment_full_name(jsonObjectUser.getString("payment_full_name"));
                        clientDatat.setPayment_address(jsonObjectUser.getString("payment_address"));
                        clientDatat.setCard_number(jsonObjectUser.getString("card_number"));
                        clientDatat.setExpiry_date(jsonObjectUser.getString("expiry_date"));
                        clientDatat.setCcv(jsonObjectUser.getString("ccv"));
                    }
                    publishProgress("OK");
                    finalResult = "OK";
                } else{
                    publishProgress("ERROR");
                    finalResult = "ERROR";
                }

                //Surasom data

                urlConnection.disconnect();
                //publishProgress();
            } catch (Exception e) {
                Log.e(TAG, "KLAIDA! " + e.toString());
            }

            return finalResult;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            if(progressCounter == 1) {
                if (values[0].equals("OK")) {
                    progressCounter -= 1;
                } else {
                    progressCounter -= 1;
                    actionProgressDialog.dismiss();
                    openDialog("Nepavyko prisijungti", "Įvestas el. paštas arba slaptažodis neteisingi \nPatikrinkite įvestus duomenis");
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operatio
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
