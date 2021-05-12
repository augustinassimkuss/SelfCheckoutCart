package com.example.baka2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ShoppingListData extends AppCompatActivity {
    ListView items;
    String[] receiveList;
    int[] receiveListCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list_data);
        items = findViewById(R.id.listItems);
        Bundle b = this.getIntent().getExtras();
        receiveList = b.getStringArray("items");
        receiveListCount = b.getIntArray("items_count");
        CustomAdapter customAdapter = new CustomAdapter();

        items.setAdapter(customAdapter);
    }

    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int returningCount = 0;
            for(int i = 0; i < receiveList.length; i++)
                if(receiveList[i] != null)
                    returningCount++;
            return returningCount;
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
            View view1 = getLayoutInflater().inflate(R.layout.shopping_list_items_row, null);
                //getting view in row_data
                TextView name = view1.findViewById(R.id.shoppingListItem);
                TextView price = view1.findViewById(R.id.shoppingListPrice);
                ImageButton delete = view1.findViewById(R.id.shoppingListItemsDeleteBtn);

                if(i == 0) {
                    name.setText("Apsipirkimo sąrašas: \n" + receiveList[i]);
                    price.setText("");
                    delete.setVisibility(View.GONE);
                }
                else if ( i == 1) {
                    name.setText("Sukurtas: " + receiveList[i]);
                    price.setText("");
                    delete.setVisibility(View.GONE);
                }
                else {
                    name.setText(receiveList[i]);
                    price.setText("x" +receiveListCount[i]);
                }

            return view1;
        }
    }
}
