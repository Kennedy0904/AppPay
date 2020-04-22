package com.asiapay.paydollar.apppay;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CardPayment_ManualKeyIn extends AppCompatActivity {

    private Button btnConfirm;
    private TextView txtStep2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment__manual_key_in);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CardPayment_ManualKeyIn.this, CardPayment_3.class);
                startActivity(intent);
            }

        });

        txtStep2 = (TextView) findViewById(R.id.progress_card_text_2);
        txtStep2.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        Intent intent = new Intent(CardPayment_ManualKeyIn.this, MainActivity.class);
        startActivity(intent);
        return true;
    }
}
