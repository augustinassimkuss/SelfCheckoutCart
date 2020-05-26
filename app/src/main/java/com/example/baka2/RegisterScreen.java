package com.example.baka2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baka2.Tools.ClientData;
import com.example.baka2.Tools.Global;
import com.example.baka2.Tools.ObjectWrapperForBinder;

public class RegisterScreen extends AppCompatActivity {

    private Button registerBackButton;
    private Button registerPaymentButton;
    ClientData registeringData = new ClientData();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main);

        registerBackButton = findViewById(R.id.registerPayBackBtn);
        registerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainPage();
            }
        });

        registerPaymentButton = findViewById(R.id.registerContinueBtn);
        registerPaymentButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                openPaymentPage();
            }
        });
    }

    public void openMainPage(){
        Intent registerBackIntent = new Intent(this, MainActivity.class);
        startActivity(registerBackIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openPaymentPage()
    {
        if (registrationValidation()) {
            Intent registerPaymentIntent = new Intent(this, RegisterPaymentScreen.class);
            final Bundle bundle = new Bundle();
            bundle.putBinder("ClientData", new ObjectWrapperForBinder(registeringData));
            registerPaymentIntent.putExtras(bundle);
            startActivity(registerPaymentIntent);
        }

    }
    public boolean registrationValidation()
    {
        int falseCount = 0;
        final EditText pswEdit = findViewById(R.id.registerPswInput);
        final String psw = pswEdit.getText().toString();
        final TextView pswText = findViewById(R.id.registerPswText);

        final EditText psw2Edit = findViewById(R.id.registerPswRepeatInput);
        final String psw2 = psw2Edit.getText().toString();
        final TextView psw2Text = findViewById(R.id.registerPswRepeatText);

        final EditText emailEdit = findViewById(R.id.registerElInput);
        final String email = emailEdit.getText().toString();
        final TextView emailText = findViewById(R.id.registerElText);

        final EditText fullNameEdit = findViewById(R.id.registerNameInput);
        final String fullName = fullNameEdit.getText().toString();

        final EditText birthDateEdit = findViewById(R.id.registerDateInput);
        final String birthDate = birthDateEdit.getText().toString();

        final EditText phoneEdite = findViewById(R.id.registerPhoneInput);
        final String phone = phoneEdite.getText().toString();

        if(!psw.equals(psw2) || psw.length() < 1 || psw2.length() < 1) {
            pswText.setTextColor(Color.RED);
            psw2Text.setTextColor(Color.RED);
            falseCount++;
        }
        if(!Global.isValidEmail(email))
        {
            emailText.setTextColor(Color.RED);
            falseCount++;
        }
        if(falseCount != 0)
            return false;
        else
        {
            registeringData.setFull_name(fullName);
            registeringData.setPhone_number(phone);
            registeringData.setEmail(email);
            registeringData.setPassword(psw);
            registeringData.setBirth_date(birthDate);
            return true;
        }
    }
}