package com.asiapay.paydollar.apppay;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardPayment_2 extends AppCompatActivity {

    private LinearLayout btnManualKeyIn;
    private TextView txtStep2;
    private ImageView insert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment_2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        btnManualKeyIn = (LinearLayout) findViewById(R.id.btnManualKeyIn);
        btnManualKeyIn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CardPayment_2.this, CardPayment_ManualKeyIn.class);
                startActivity(intent);
            }

        });

        txtStep2 = (TextView) findViewById(R.id.progress_card_text_2);
        txtStep2.setTypeface(null, Typeface.BOLD);

        insert = (ImageView) findViewById(R.id.ic_insert);

        insert.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CardPayment_2.this, CardPayment_EnterPin.class);
                startActivity(intent);
            }

        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        Intent intent = new Intent(CardPayment_2.this, MainActivity.class);
        startActivity(intent);
        return true;
    }
}
