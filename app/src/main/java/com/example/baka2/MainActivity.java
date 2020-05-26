package com.example.baka2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button mainLoginBtn;
    private Button mainRegisternBtn;
    private int backButtonCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLoginBtn = findViewById(R.id.mainLoginBtn);
        mainLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginPage();
            }
        });

        mainRegisternBtn = findViewById(R.id.mainRegisterBtn);
        mainRegisternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterPage();
            }
        });

    }
    @Override
    public void onBackPressed()
    {
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Jei norite išjungti programėlę- paspauskite dar kartą.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    public void openLoginPage() {
        Intent mainLoginIntent = new Intent(this, LoginScreen.class);
        startActivity(mainLoginIntent);
    }

    public void openRegisterPage() {
        Intent mainRegisterIntent = new Intent(this, RegisterScreen.class);
        startActivity(mainRegisterIntent);
    }
}
