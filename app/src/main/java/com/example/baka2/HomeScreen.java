package com.example.baka2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;
import com.example.baka2.Tools.ObjectWrapperForBinder;

public class HomeScreen extends AppCompatActivity {
    private int backButtonCount = 0;
    ClientData clientDataH = new ClientData();
    private Button startShop;
    private Button shoppingList;
    private Button editDataBtn;
    private Button logoutBtn;

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
        String result = "";
        if(cardNumber != null || cardNumber.equals("null"))
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
        else
            return "Banko kortelė nepridėta";
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
}
