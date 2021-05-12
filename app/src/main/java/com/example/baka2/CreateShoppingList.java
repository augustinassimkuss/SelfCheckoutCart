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
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CreateShoppingList extends AppCompatActivity {
    private static final String TAG = "Create Shopping Lists";
    ListView listView;
    ImageButton deleteButton;
    Button backButton;
    Button createButton;
    List<String> shoppingLists = new ArrayList<>();
    EditText listName;
    Button itemsList;
    List<Integer> shoppingListsCount = new ArrayList<>();
    String[][] passingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_shopping_list_main);
        listView = findViewById(R.id.createShoppinListView);
        backButton = findViewById(R.id.createShoppingListBackBtn);
        listName = findViewById(R.id.createShoppingListNameField);
        itemsList = findViewById(R.id.createShoppingListAddBtn);
        createButton = findViewById(R.id.createShoppingListCreateBtn);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Global.createShoppingListsID.size()>0)
                        registerList();
                    else
                        openDialog("Sąrašas tuščias","");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShoppingListPage();
            }
        });

        itemsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openItemsPage();
            }
        });
        //boolean ifListsNotNull = false;
        shoppingLists = Global.createShoppingLists;
        shoppingListsCount = Global.createShoppingListsCount;
        if(shoppingLists != null) {
            CustomAdapter customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
        }

    }
    public void registerList() throws ExecutionException, InterruptedException {
        passingList = new String[2][Global.createShoppingListsCount.size()];
        for(int i = 0; i < Global.createShoppingListsID.size(); i++) {
            passingList[0][i] = String.valueOf(Global.createShoppingListsID.get(i));
            passingList[1][i] = String.valueOf(Global.createShoppingListsCount.get(i));
        }
        String response = new RegisterListHTTP().execute(Global.urlReturn("add_shopping_list.php"), listName.getText().toString(), String.valueOf(Global.clientDataGlobal.getId()), null).get();
        if(response.equals("OK"))
        {
            openShoppingListPage();
        }
    }
    public void openItemsPage(){
        Intent itemsListIntent = new Intent(this, ItemsList.class);
        startActivity(itemsListIntent);
    }

    public void openShoppingListPage(){
        Intent listsPageIntent = new Intent(this, ShoppingLists.class);
        Global.createShoppingListsCount = new ArrayList<>();
        Global.createShoppingLists =  new ArrayList<>();
        Global.createShoppingListsID =  new ArrayList<>();
        startActivity(listsPageIntent);
    }
    public class RegisterListHTTP extends AsyncTask<String, String, String> {
        public RegisterListHTTP() {
        }
        String finalResult = "";
        ProgressDialog actionProgressDialog = new ProgressDialog(CreateShoppingList.this);
        StringBuffer response = new StringBuffer();
        int progressCounter = 1;

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
            String list_name = params[1];
            String user_id = params[2];
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
                        "\"user_id\": \"" + user_id + "\", " +
                        "\"name\": \"" + list_name + "\", " +
                        "\"items\": ";
                String array = "{";
                for(int i = 0; i < passingList[0].length; i++)
                {
                    array = array +
                            "\""+ passingList[0][i] + "\": \"" + passingList[1][i] + "\" ";
                    if(i < passingList[0].length-1)
                        array = array + ", ";
                }
                array = array + "}";
                jsonInputString = jsonInputString + array + "}";
                Log.e(TAG, "Json created: " + jsonInputString);
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
                if(jsonObject.getString("status").equals("OK"))
                {
                    publishProgress("OK");
                    finalResult = "OK";
                }
                else
                {

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
                    openDialog("Sąrašas sukurtas","");
                } else {
                    progressCounter -= 1;
                    actionProgressDialog.dismiss();
                    openDialog("Sąrašas nesukurtas:", "Pavadinimas užimtas");
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operatio
            actionProgressDialog.dismiss();
        }
    }
    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return shoppingLists.size();
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
            @SuppressLint({"ViewHolder", "InflateParams"}) View view1 = getLayoutInflater().inflate(R.layout.shopping_list_items_row,null);
            TextView name = view1.findViewById(R.id.shoppingListItem);
            TextView price = view1.findViewById(R.id.shoppingListPrice);
            deleteButton = view1.findViewById(R.id.shoppingListItemsDeleteBtn);

            name.setText(shoppingLists.get(i));
            price.setText("x" +shoppingListsCount.get(i));
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog("Ar tikrai norite ištrinti šį sąrašą?", "DĖMESIO! Sąrašas taps neatsatomu");
                }
            });
            return view1;
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

