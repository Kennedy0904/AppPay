package com.asiapay.paydollar.apppay;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CardPayment_EnterPin extends AppCompatActivity {

    private TextView txtStep2;
    private Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment__enter_pin);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        txtStep2 = (TextView) findViewById(R.id.progress_card_text_2);
        txtStep2.setTypeface(null, Typeface.BOLD);

        btnProceed = (Button) findViewById(R.id.btnEnter);
        btnProceed.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CardPayment_EnterPin.this, CardPayment_Failure.class);
                startActivity(intent);
            }

        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        Intent intent = new Intent(CardPayment_EnterPin.this, MainActivity.class);
        startActivity(intent);
        return true;
    }
}
