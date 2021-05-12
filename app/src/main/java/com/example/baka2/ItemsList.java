package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ItemsList extends AppCompatActivity {
    String[][] items;
    Button backBtn;
    Button addBtn;
    EditText searchBar;
    CustomAdapter customAdapter;
    private String[][] filteredData;
    List<String> editList = new ArrayList<>(Global.createShoppingLists);
    List<Integer> editListCount = new ArrayList<>(Global.createShoppingListsCount);
    List<Integer> editListId = new ArrayList<>(Global.createShoppingListsID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list_main);
        ListView itemView = findViewById(R.id.itemsListView);
        items = Global.items;
        filteredData = items;

        /*editList = Global.createShoppingLists;
        editListCount = Global.createShoppingListsCount;
        editListId = Global.createShoppingListsID;*/
        backBtn = findViewById(R.id.itemsListBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateListPage();
            }
        });
        searchBar = findViewById(R.id.itemsSearchField);
        customAdapter = new CustomAdapter();
        addBtn = findViewById(R.id.itemsAddBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listToArray();
                openCreateListPage();
            }
        });

        itemView.setAdapter(customAdapter);
        /*searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (ItemsList.this).customAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
    }
    public void openCreateListPage(){
        Intent intent = new Intent(this, CreateShoppingList.class);
        startActivity(intent);
    }
    public void listToArray()
    {

        int j = 0;
        while(j < editList.size())
        {
            if(editListCount.get(j) == 0) {
                editList.remove(j);
                editListCount.remove(j);
                editListId.remove(j);
            }
            else
                j++;
        }
        for(int i = 0; i < editListId.size(); i++)
        {
            if(!Global.createShoppingListsID.contains(editListId.get(i)))
            {
                Global.createShoppingListsID.add(editListId.get(i));
                Global.createShoppingLists.add(editList.get(i));
                Global.createShoppingListsCount.add( editListCount.get(i));
            }
            else if(editListCount.get(i)!= Global.createShoppingListsCount.get(i))
            {
                int add = Global.createShoppingListsCount.get(i);
                Global.createShoppingListsCount.set(i, ++add);
            }
        }
    }
    private class CustomAdapter extends BaseAdapter implements Filterable {

        @Override
        public int getCount() {
            /*int returningCount = 0;
            for(int i = 0; i < filteredData.length; i++)
                if(filteredData[i] != null)
                    returningCount++;*/
            return items.length;
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View view1 = getLayoutInflater().inflate(R.layout.shopping_list_items_row, null);

            //getting view in row_data
            TextView name = view1.findViewById(R.id.shoppingListItem);
            TextView price = view1.findViewById(R.id.shoppingListPrice);
            ImageButton check = view1.findViewById(R.id.shoppingListItemsDeleteBtn);
            check.setVisibility(View.VISIBLE);
            if(!editListId.contains(Integer.parseInt(filteredData[i][0]))) {
                editListCount.add(0);
                editListId.add(Integer.parseInt(filteredData[i][0]));
                editList.add(filteredData[i][1]);
            }
            name.setText(filteredData[i][1]);
            price.setText(filteredData[i][2]);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editListCount.set(i,editListCount.get(i)+1);
                    openDialog(String.valueOf(editListCount.get(i)),editList.get(i));

                }
            });
            return view1;
        }
        @Override
        public Filter getFilter() {
            return new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if(constraint == null || constraint.length()==0)
                    {
                        results.values = items;
                        results.count = items.length;
                    }
                    else
                    {
                        int temp = 0;
                        String[][] filterResultsData = new String[items.length][3];
                        for(int i = 0; i < filterResultsData.length; i++)
                        {
                            if(items[i][1].contains(constraint))
                            {
                                filterResultsData[temp][0] = items[i][0];
                                filterResultsData[temp][1] = items[i][1];
                                filterResultsData[temp][2] = items[i][2];
                                temp++;
                            }

                        }
                        /*for(int j = items.length; j > temp; j--)
                            filterResultsData[j] = null;*/

                        results.values = filterResultsData;
                        results.count = filterResultsData.length;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredData = (String[][])results.values;
                    notifyDataSetChanged();
                }
            };
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
