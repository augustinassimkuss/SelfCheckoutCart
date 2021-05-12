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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class ShoppingLists extends AppCompatActivity {
    private static final String TAG = "Shopping Lists";
    ListView listView;
    Button backButton;
    Button createButton;
    String[][] shoppingLists;
    int[][] shoppingListsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_shopping_main);
        listView = findViewById(R.id.listView);
        backButton = findViewById(R.id.shoppingListBackBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainPage();
            }
        });
        boolean ifListsNotNull = false;
        try {
             ifListsNotNull = LoadLists();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(ifListsNotNull) {
            CustomAdapter customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    Intent intent = new Intent(getApplicationContext(), ShoppingListData.class);
                    Bundle b = new Bundle();
                    b.putStringArray("items", shoppingLists[i]);
                    b.putIntArray("items_count", shoppingListsCount[i]);
                    System.out.println(shoppingLists[i]);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
        }

        createButton = findViewById(R.id.shoppingListCreateBtn);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateList();
            }
        });

    }

    public void openCreateList(){
        Intent createPageIntent = new Intent(this, CreateShoppingList.class);
        startActivity(createPageIntent);
    }

    public void openMainPage(){
        Intent mainPageIntent = new Intent(this, HomeScreen.class);
        startActivity(mainPageIntent);
    }
    public boolean LoadLists() throws ExecutionException, InterruptedException {
        String response = new GetListsHTTP().execute(Global.urlReturn("get_shopping_lists.php"), String.valueOf(Global.clientDataGlobal.getId()), null).get();
        if(response.equals("OK"))
        {
            return true;
        }
        else
            return false;
    }
    private class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shoppingLists.length;
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
           View view1 = getLayoutInflater().inflate(R.layout.shopping_list_row,null);
            TextView name = view1.findViewById(R.id.shoppingListName);
            ImageButton deleteButton = findViewById(R.id.shoppingListDeleteButton);
            //deleteButton.setVisibility(View.GONE);
            name.setText(shoppingLists[i][0]);
            /*deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog("Ar tikrai norite ištrinti šį sąrašą?", "DĖMESIO! Sąrašas taps neatsatomu");
                }
            });*/
            return view1;
        }


    }
    public class GetListsHTTP extends AsyncTask<String, String, String> {
        public GetListsHTTP() {
        }
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(ShoppingLists.this);
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
                    JSONObject shoppingListsObject = jsonObject.getJSONObject("shopping_lists");
                    JSONObject shoppingListsObjectLists = new JSONObject();
                    JSONObject shoppingListsObjectItems = new JSONObject();
                    Log.e(TAG, "Shopping lists " + shoppingListsObject);
                    shoppingLists =  new String[shoppingListsObject.length()][20];
                    shoppingListsCount = new int[shoppingListsObject.length()][20];
                    Iterator<?> keysId = shoppingListsObject.keys();
                    for (int i = 1; i <= shoppingListsObject.length(); i++)
                    {

                        String keyID = (String) keysId.next();
                        shoppingListsObjectLists = shoppingListsObject.getJSONObject(keyID);
                        Log.e(TAG, "array: " + shoppingListsObjectLists);

                        shoppingLists[i - 1][0] = shoppingListsObjectLists.getString("name");
                        shoppingLists[i - 1][1] = shoppingListsObjectLists.getString("date_created");
                        shoppingListsObjectItems = shoppingListsObjectLists.getJSONObject("items");
                        Iterator<?> keys = shoppingListsObjectItems.keys();
                        for(int j = 0; j < shoppingListsObjectItems.length(); j ++)
                        {
                            String key = (String) keys.next();
                            shoppingLists[i-1][j+2] =  key;
                            shoppingListsCount[i-1][j+2] = Integer.parseInt(shoppingListsObjectItems.get(key).toString());
                        }
                        Log.e(TAG, "Shopping items " + shoppingListsObjectItems);

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
