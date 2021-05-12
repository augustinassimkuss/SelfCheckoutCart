package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;
import com.example.baka2.Tools.ObjectWrapperForBinder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class HomeScreen extends AppCompatActivity {
    private static final String TAG = "Home Screen";
    private int backButtonCount = 0;
    ClientData clientDataH = new ClientData();
    String [][] items;
    private Button startShop;
    private Button shoppingList;
    private Button editDataBtn;
    private Button logoutBtn;
    private Button showOrdersBtn;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);
        //clientDataH = (ClientData) ((ObjectWrapperForBinder)getIntent().getExtras().getBinder("ClientData")).getData();
        clientDataH = Global.clientDataGlobal;

        final TextView homeName = findViewById(R.id.homeName);
        final TextView homeEmail = findViewById(R.id.homeEmail);
        final TextView homePhone = findViewById(R.id.homePhone);
        final TextView homePayment = findViewById(R.id.homePayment);
        homeName.append(clientDataH.getFull_name());
        homeEmail.append(clientDataH.getEmail());
        homePhone.append(clientDataH.getPhone_number());
        homePayment.append(paymentMethod());
        System.out.println(clientDataH.getId());
        try {
            new GetItemsHTTP().execute(Global.urlReturn("get_items.php"), null).get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Global.items = items;
        //homeName.append(" ID: " + clientDataH.getId());

        startShop = findViewById(R.id.homeStartBtn);
        startShop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openConnectPage();
            }
        });

        shoppingList = findViewById(R.id.homeListsBtn);
        shoppingList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openShoppingListsPage();
            }
        });

        editDataBtn = findViewById(R.id.homeEditBtn);
        editDataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openEditDataPage();
            }
        });

        logoutBtn = findViewById(R.id.homeLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               openMainActivity();
            }
        });

        showOrdersBtn = findViewById(R.id.homeOrders);
        showOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrdersPage();
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        if(backButtonCount >= 1)
        {
            backButtonCount = 0;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Jei norite išeiti- paspauskite dar kartą.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openConnectPage()
    {
        Intent homeStartIntent = new Intent(this, Connect.class);
        final Bundle bundle = new Bundle();
        bundle.putBinder("ClientData", new ObjectWrapperForBinder(clientDataH));
        homeStartIntent.putExtras(bundle);
        startActivity(homeStartIntent);
    }

    public void openShoppingListsPage()
    {
        Intent shoppingListsIntent = new Intent(this, ShoppingLists.class);
        startActivity(shoppingListsIntent);
    }

    public String paymentMethod()
    {
        String cardNumber = clientDataH.getCard_number();
        System.out.println("Korta"+cardNumber);
        String result = "";
        if(cardNumber != null)
        {
            if (cardNumber.length() > 4)
            {
                result = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
            }
            else
            {
                result = "**** **** **** "+ cardNumber;
            }
        }
        else {
            return "Banko kortelė nepridėta";
        }
        return result;
    }

    public void openEditDataPage()
    {
        Intent editPageIntent = new Intent(this, EditClientData.class);
        startActivity(editPageIntent);
    }

    public void openMainActivity()
    {
        Global.clientDataGlobal = null;
        Intent openMainActivityIntent = new Intent(this, MainActivity.class);
        System.out.println(Global.clientDataGlobal);
        startActivity(openMainActivityIntent);
    }

    public void openOrdersPage(){
        Intent ordersIntent = new Intent(this, OrdersLists.class);
        ordersIntent.putExtra("order_id", -1);
        startActivity(ordersIntent);
    }

    public class GetItemsHTTP extends AsyncTask<String, String, String> {
        public GetItemsHTTP() {
        }

        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(HomeScreen.this);
        StringBuffer response = new StringBuffer();
        int progressCounter = 1;

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Gaunamos prekės...");
            actionProgressDialog.setCancelable(true);
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
                        "\"user_id\": \"0\"}";

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

                if (jsonObject.getString("status").equals("OK")) {
                    JSONArray shoppingListsArray = jsonObject.getJSONArray("items");
                    Log.e(TAG, "array: " + shoppingListsArray);
                    items = new String[shoppingListsArray.length()][3];
                    for(int i = 0; i < shoppingListsArray.length(); i ++)
                    {
                        JSONObject shoppingItemObject = shoppingListsArray.getJSONObject(i);
                        items[i][0] = shoppingItemObject.getString("id");
                        items[i][1] = shoppingItemObject.getString("name");
                        items[i][2] = shoppingItemObject.getString("price");
                    }
                    publishProgress("OK");
                    finalResult = "OK";
                } else {
                    publishProgress("ERROR");
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
            if (progressCounter == 1) {
                if (values[0].equals("OK")) {
                    progressCounter -= 1;
                } else {
                    progressCounter -= 1;
                    actionProgressDialog.dismiss();
                    //penDialog("Nepavyko prisijungti", "Įvestas el. paštas arba slaptažodis neteisingi \nPatikrinkite įvestus duomenis");
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operatio
            actionProgressDialog.dismiss();
        }
    }
}
