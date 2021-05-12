package com.example.baka2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baka2.Tools.Global;

public class OrderListsData extends AppCompatActivity {
    ListView items;
    String[] receiveList;
    int[] receiveListCount;
    double[] itemDiscountList;
    int receiveId;
    double[] priceList;
    String[][] itemsArray = Global.items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list_data);
        items = findViewById(R.id.orderListItems);
        Bundle b = this.getIntent().getExtras();
        assert b != null;
        receiveId = b.getInt("id");
        receiveList = b.getStringArray("items");
        receiveListCount = b.getIntArray("items_count");
        itemDiscountList = b.getDoubleArray("items_discount");
        priceList = new double[receiveList.length];
        formatPriceList();
        CustomAdapter customAdapter = new CustomAdapter();

        items.setAdapter(customAdapter);
    }
    @Override
    public void onBackPressed()
    {
        Intent ordersIntent = new Intent(this, OrdersLists.class);
        ordersIntent.putExtra("order_id", -1);
        startActivity(ordersIntent);
    }
    public void formatPriceList ()
    {
        for(int i = 0; i < receiveList.length; i++)
        {
            for(int j = 0; j < itemsArray.length; j++)
            {
                if(receiveList[i]!= null && receiveList[i].equals(itemsArray[j][1]))
                    priceList[i] = (1 - itemDiscountList[i]) * Double.parseDouble(itemsArray[j][2]);
            }
        }
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
            View view1 = getLayoutInflater().inflate(R.layout.order_list_items_row, null);
            //getting view in row_data
            TextView name = view1.findViewById(R.id.orderDataListItem);
            TextView count = view1.findViewById(R.id.orderDataListCount);
            TextView price = view1.findViewById(R.id.orderDataListPrice);

            if(i == 0) {
                name.setText("Apsipirkimo sąrašas: \n" + receiveList[i]);
                price.setText("");
                count.setText("");
            }
            else if ( i == 1) {
                name.setText("");
                price.setText(receiveList[i] + "€");
                count.setText("Suma:");
            }
            else {
                name.setText(receiveList[i]);
                count.setText("x" +receiveListCount[i]);
                price.setText(String.format("%.2f", priceList[i]) + "€/vnt\n" + String.format("%.2f", priceList[i] * receiveListCount[i]) + "€");

            }

            return view1;
        }
    }
}

