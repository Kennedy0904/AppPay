package com.asiapay.paydollar.apppay;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CardPayment_Success extends AppCompatActivity {

    private Button btnOk;
    private TextView txtStep2, txtStep3, txtStep4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment__success);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CardPayment_Success.this, MainActivity.class);
                startActivity(intent);
            }

        });

        txtStep2 = (TextView) findViewById(R.id.progress_card_text_2);
        txtStep3 = (TextView) findViewById(R.id.progress_card_text_3);
        txtStep4 = (TextView) findViewById(R.id.progress_card_text_4);

        txtStep2.setTypeface(null, Typeface.BOLD);
        txtStep3.setTypeface(null, Typeface.BOLD);
        txtStep4.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        Intent intent = new Intent(CardPayment_Success.this, MainActivity.class);
        startActivity(intent);
        return true;
    }
}
