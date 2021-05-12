package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.baka2.Tools.Global;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class OrdersLists extends AppCompatActivity {
    private static final String TAG = "Shopping Lists";
    ListView listView;
    ImageButton deleteButton;
    Button backButton;
    int[] orderId;
    String[][] orderLists;
    int[][] orderListsCount;
    double[][] orderListsDiscount;
    double[][] orderListsPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_list_main);
        listView = findViewById(R.id.ordersListView);
        backButton = findViewById(R.id.ordersListBackBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainPage();
            }
        });

        boolean afterShopping = false;
        boolean ifListsNotNull = false;
        int receivedId;
        //If page is opened after shopping- get received ID and boolean for true
        Intent intentG = getIntent();
        receivedId = intentG.getIntExtra("order_id", -1);
        //Bundle a = this.getIntent().getExtras();
        //assert a != null;
        //receivedId = a.getInt("order_id");
        //System.out.println(a.getInt("order_id"));
        System.out.println(receivedId);
        if(receivedId!= -1)
            afterShopping=true;

        //If client has any previous orders - list them
        try {
            ifListsNotNull = LoadLists();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //If client has any orders- set row views for every order
        if(ifListsNotNull)
            //If page is not opened after shopping
            if(!afterShopping){
                CustomAdapter customAdapter = new CustomAdapter();
                listView.setAdapter(customAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        Intent intent = new Intent(getApplicationContext(), OrderListsData.class);
                        Bundle b = new Bundle();
                        b.putInt("id", orderId[i]);
                        b.putStringArray("items", orderLists[i]);
                        b.putIntArray("items_count", orderListsCount[i]);
                        b.putDoubleArray("items_discount", orderListsDiscount[i]);
                        System.out.println(orderLists[i]);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });
            }
            //If page is opened after shopping
            else{
                for(int i = 0; i < orderLists.length; i++)
                {
                    if(orderId[i] == receivedId)
                    {
                        Intent intent = new Intent(getApplicationContext(), OrderListsData.class);
                        Bundle b = new Bundle();
                        b.putInt("id", orderId[i]);
                        b.putStringArray("items", orderLists[i]);
                        b.putIntArray("items_count", orderListsCount[i]);
                        b.putDoubleArray("items_discount", orderListsDiscount[i]);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
            }
    }
    public void openMainPage(){
        Intent mainPageIntent = new Intent(this, HomeScreen.class);
        startActivity(mainPageIntent);
    }
    public boolean LoadLists() throws ExecutionException, InterruptedException {
        String response = new GetOrdersHTTP().execute(Global.urlReturn("get_orders_lists.php"), String.valueOf(Global.clientDataGlobal.getId()), null).get();
        if(response.equals("OK"))
            return true;
        else
            return false;
    }
    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return orderLists.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View view1 = getLayoutInflater().inflate(R.layout.orders_list_row,null);
            TextView name = view1.findViewById(R.id.ordersListName);
            TextView total_sum = view1.findViewById(R.id.ordersListPrice);

            name.setText(orderLists[i][0]);
            total_sum.setText(orderLists[i][1] + "€");
            //deleteButton = findViewById(R.id.shoppingListDeleteButton);
            /*deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog("Ar tikrai norite ištrinti šį sąrašą?", "DĖMESIO! Sąrašas taps neatsatomu");
                }
            });*/
            return view1;
        }
    }
    public class GetOrdersHTTP extends AsyncTask<String, String, String> {
        public GetOrdersHTTP() {
        }
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(OrdersLists.this);
        StringBuffer response = new StringBuffer();
        int progressCounter = 1;

        @Override
        protected void onPreExecute() {
            actionProgressDialog.setMessage("Gaunami sąrašai...");
            actionProgressDialog.setCancelable(true);
            actionProgressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String user_id = params[1];
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
                        "\"user_id\": \"" + user_id + "\"}";

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
                    JSONObject shoppingListsObject = jsonObject.getJSONObject("order_lists");
                    JSONObject shoppingListsObjectLists = new JSONObject();
                    JSONObject shoppingListsObjectItems = new JSONObject();
                    JSONObject shoppingListsObjectDiscount = new JSONObject();
                    JSONObject shoppingListsObjectPrice = new JSONObject();
                    Log.e(TAG, "Order lists " + shoppingListsObject);
                    orderLists =  new String[shoppingListsObject.length()][20];
                    orderListsCount = new int[shoppingListsObject.length()][20];
                    orderId = new int[shoppingListsObject.length()];
                    orderListsDiscount = new double[shoppingListsObject.length()][20];
                    orderListsPrice = new double[shoppingListsObject.length()][20];
                    Iterator<?> keysObject = shoppingListsObject.keys();
                    for (int i = 0; i < shoppingListsObject.length(); i++)
                    {
                        String keyObject = (String) keysObject.next();
                        shoppingListsObjectLists = shoppingListsObject.getJSONObject(keyObject);
                        Log.e(TAG, "array: " + shoppingListsObjectLists);

                        orderId[i] = shoppingListsObjectLists.getInt("id");
                        orderLists[i][0] = shoppingListsObjectLists.getString("date_created");
                        orderLists[i][1] = shoppingListsObjectLists.getString("total_sum");

                        if(shoppingListsObjectLists.length() > 0) {
                            shoppingListsObjectItems = shoppingListsObjectLists.getJSONObject("items");
                            Log.e(TAG, "items: " + shoppingListsObjectItems);

                            Iterator<?> keys = shoppingListsObjectItems.keys();
                            for (int j = 0; j < shoppingListsObjectItems.length(); j++) {
                                String key = (String) keys.next();
                                orderLists[i][j + 2] = key;
                                orderListsCount[i][j + 2] = Integer.parseInt(shoppingListsObjectItems.get(key).toString());
                            }

                            shoppingListsObjectDiscount = shoppingListsObjectLists.getJSONObject("discount");
                            Log.e(TAG, "discount: " + shoppingListsObjectDiscount);
                            Iterator<?> keysDiscount = shoppingListsObjectDiscount.keys();
                            for (int j = 0; j < shoppingListsObjectDiscount.length(); j++) {
                                String key = (String) keysDiscount.next();
                                if(!shoppingListsObjectDiscount.get(key).equals(null))
                                     orderListsDiscount[i][j+2] = Double.parseDouble((String)shoppingListsObjectDiscount.get(key));
                                else
                                    orderListsDiscount[i][j+2] = 0;
                            }
                            shoppingListsObjectPrice = shoppingListsObjectLists.getJSONObject("discount");
                            Log.e(TAG, "prices: " + shoppingListsObjectPrice);
                            Iterator<?> keysPrice = shoppingListsObjectPrice.keys();
                            for (int j = 0; j < shoppingListsObjectPrice.length(); j++) {
                                String key = (String) keysPrice.next();
                                if(!shoppingListsObjectPrice.get(key).equals(null))
                                    orderListsPrice[i][j+2] = Double.parseDouble((String)shoppingListsObjectPrice.get(key));
                                else
                                    orderListsPrice[i][j+2] = 0;
                            }




                            Log.e(TAG, "Shopping items " + shoppingListsObjectItems);
                        }
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
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operatio
            actionProgressDialog.dismiss();
        }
    }
    public void openDialog(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setCancelable(true);
        dialog.setMessage(message);
        dialog.setPositiveButton("Trinti", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Atšaukti", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
