package com.example.baka2;

import android.content.Intent;
import android.os.Bundle;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ShoppingInProgress extends AppCompatActivity {
    ClientData clientData = new ClientData();
    Button payBtn;
    Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_in_progress);
        clientData = Global.clientDataGlobal;
        String cart_id = getIntent().getStringExtra("cart_id");
        String session_id = getIntent().getStringExtra("session_id");

        final TextView sessionId = findViewById(R.id.shoppingSessionId);
        sessionId.append(session_id);
        final TextView cartId = findViewById(R.id.shoppingCartId);
        cartId.append(cart_id);

        payBtn = findViewById(R.id.shoppingPayBtn);
        if (clientData.getCard_number() == null) {
            payBtn.setEnabled(false);
            final TextView shoppingText = findViewById(R.id.shoppingCheckText);
            shoppingText.setText("Nepridėtas apmokėjimo metodas.\n Už šį apsipirkimą galima atsiskaityti tik kasoje");
        }

        backBtn = findViewById(R.id.shoppingBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomePage();
            }
        });
    }

    private void openHomePage() {
        Intent shoppingBackIntent = new Intent(this, HomeScreen.class);
        startActivity(shoppingBackIntent);
    }
}