package com.asiapay.paydollar.apppay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class EnterAmount extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;
    private Button btn7;
    private Button btn8;
    private Button btn9;
    private Button btn0;
    private Button btnClear;
    private Button btnDot;
    private Button btnEnter;

    private TextView step1;
    private TextView step2;
    private TextView step3;
    private TextView step4;

    public TextView amount = null;
    public TextView currency = null;
    public EditText MerchantRef = null;
    public TextView Surcharge_title = null;
    public TextView Surcharge_currCode = null;
    public TextView p_mdr = null;

    String pre_amount = "0";
    String pre_mdr = "0";
    String pre_fixed = "";
    String pre_surcharge = "0";
    String pre_mdr_amount = "0";

    String currCode = "";
    String currCode1 = "";
    String merchantName = "";
    String payType = "";
    String payMethod = "";
    String hideSurcharge = "";
    String paymentOption = "";
    String operatorID = "";

    boolean surC = false; //surcharge calution function
    boolean mdr_less_equal_0 = false; //mdr less than or equal to 0

    Toast numberpadtoast = null;
    boolean firstzero = true;
    boolean firstdot = true;
    boolean dotflag = false;
    boolean ischecktoggle = false;//false:paytype=N true:paytype:H

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_amount);

        SharedPreferences prefsettings = getApplicationContext().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        final String autoMerRef = prefsettings.getString(Constants.pref_MerRef, Constants.default_merchantref);

        //*******************layout init**************************//
        //----merchant textview start----//
        MerchantRef = (EditText) findViewById(R.id.edtMerchantRef);
        amount = (TextView) findViewById(R.id.edtAmount);
        p_mdr = (TextView) findViewById(R.id.p_mdr);
        currency = (TextView) findViewById(R.id.currency);
        Surcharge_currCode = (TextView) findViewById(R.id.Surcharge_currCode);
        Surcharge_title = (TextView) findViewById(R.id.Surcharge_title);
        amount.setText(pre_amount);
        p_mdr.setText("0");
        //----merchant textview end----//

        final Intent paymentIntent = getIntent();
        hideSurcharge = paymentIntent.getStringExtra(Constants.pref_hideSurcharge);
        payMethod = paymentIntent.getStringExtra(Constants.PAYMETHODLIST);
        Log.d("OTTO", "paymentTab hideSurcharge:" + payMethod);
        pre_mdr = paymentIntent.getStringExtra(Constants.pref_Rate);
        pre_mdr = String.valueOf(Double.parseDouble(pre_mdr) / 100);
        mdr_less_equal_0 = (Double.valueOf(pre_mdr) <= 0);
        if (mdr_less_equal_0 || !surC || "T".equals(hideSurcharge)) {
            p_mdr.setVisibility(View.GONE);
            Surcharge_currCode.setVisibility(View.INVISIBLE);
            Surcharge_title.setVisibility(View.INVISIBLE);
        } else {
            p_mdr.setVisibility(View.VISIBLE);
            Surcharge_currCode.setVisibility(View.VISIBLE);
            Surcharge_title.setVisibility(View.VISIBLE);
        }

        pre_fixed = paymentIntent.getStringExtra(Constants.pref_Fixed);
        Log.d("OTTO", "OTTO-----get mdr and mdr:" + pre_mdr + ",fixed" + pre_fixed);
        merchantName = paymentIntent.getStringExtra(Constants.MERNAME);
        currCode1 = paymentIntent.getStringExtra(Constants.CURRCODE);

        currCode = CurrCode.getName(currCode1);
        currency.setText(currCode);
        Surcharge_currCode.setText(currCode);

        paymentOption = paymentIntent.getStringExtra(Constants.PAYMENTOPTION);

        if (ischecktoggle) {
            payType = "H";
        } else {
            payType = "N";
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Define Button
        btn0 = (Button) findViewById(R.id.button0);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        btn5 = (Button) findViewById(R.id.button5);
        btn6 = (Button) findViewById(R.id.button6);
        btn7 = (Button) findViewById(R.id.button7);
        btn8 = (Button) findViewById(R.id.button8);
        btn9 = (Button) findViewById(R.id.button9);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnDot = (Button) findViewById(R.id.buttonDot);
        //Define TexView
        amount = (TextView) findViewById(R.id.edtAmount);
        currency = (TextView) findViewById(R.id.currency);
        step1 = (TextView) findViewById(R.id.progress_step_1);
        step2 = (TextView) findViewById(R.id.progress_step_2);
        step3 = (TextView) findViewById(R.id.progress_step_3);
        step4 = (TextView) findViewById(R.id.progress_step_4);

        //Set Value
        if (autoMerRef.equals(Constants.MERREF_AUTO)) {
            MerchantRef.setText(autoRef());
            MerchantRef.setEnabled(false);
        }
        currency.setText(currCode);

        if (paymentOption.equalsIgnoreCase("SCAN QR PAYMENT")) {
            setTitle(R.string.scan_qr);
            step1.setText(R.string.enter_amount);
            step2.setText(R.string.select_qr_payment);
            step3.setText(R.string.read_qr);
            step4.setText(R.string.payment_result);
        } else if (paymentOption.equalsIgnoreCase("PRESENT QR PAYMENT")) {
            setTitle(R.string.present_qr);
            step1.setText(R.string.enter_amount);
            step2.setText(R.string.select_qr_payment);
            step3.setText(R.string.generate_qr);
            step4.setText(R.string.payment_result);
        } else if (paymentOption.equalsIgnoreCase("CARD PAYMENT")) {
            setTitle(R.string.card_payment);
            step1.setText(R.string.enter_amount);
            step2.setText(R.string.read_card);
            step3.setText(R.string.confirm_payment);
            step4.setText(R.string.payment_result);
        } else if (paymentOption.equalsIgnoreCase("MOBILE PAYMENT")) {
            setTitle(R.string.mobile_payment);
            step1.setText(R.string.enter_amount);
            step2.setText(R.string.select_mobile_payment);
            step3.setText(R.string.tab);
            step4.setText(R.string.payment_result);
        } else if (paymentOption.equalsIgnoreCase("FPS PRESENT QR")) {
            setTitle(R.string.fps_present_qr);
            step1.setText(R.string.enter_amount);
            step2.setText(R.string.generate_qr);
            step3.setText(R.string.payment_result);
            step4.setVisibility(View.GONE);
            findViewById(R.id.circle4).setVisibility(View.GONE);
            findViewById(R.id.line3).setVisibility(View.GONE);
            findViewById(R.id.gap3).setVisibility(View.GONE);
        }

        MerchantRef.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "0";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = true;
                    } else {

                        pre_amount = pre_amount.replaceAll(",", "");

                        if (pre_amount.substring(pre_amount.length() - 1).trim().equals(".")) {
                            pre_amount = pre_amount + "0";
                            DecimalFormat formatter = new DecimalFormat("#,###,###,##0.0#");
                            pre_amount = formatter.format(Double.parseDouble(pre_amount));

                        } else if (pre_amount.substring(pre_amount.length() - 1).trim().equals("0") && pre_amount.contains(".")) {
                            pre_amount = pre_amount + "0";
                            DecimalFormat formatter = new DecimalFormat("#,###,###,##0.00");
                            pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        } else {
                            pre_amount = pre_amount + "0";

                            if (pre_amount.contains(".")) {
                                DecimalFormat formatter = new DecimalFormat("#,###,###,##0.00");
                                pre_amount = formatter.format(Double.parseDouble(pre_amount));
                            } else {
                                DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                                pre_amount = formatter.format(Double.parseDouble(pre_amount));
                            }

                        }

                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "1";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "1";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "2";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "2";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "3";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "3";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "4";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "4";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "5";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "5";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "6";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "6";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "7";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "7";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "8";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "8";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_ten));
                } else {
                    if (firstzero) {
                        pre_amount = "9";
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    } else {
                        pre_amount = pre_amount + "9";
                        pre_amount = pre_amount.replaceAll(",", "");
                        DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
                        pre_amount = formatter.format(Double.parseDouble(pre_amount));
                        amount.setText(pre_amount);
                        pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                        p_mdr.setText(pre_surcharge);
                        pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                        firstdot = false;
                        firstzero = false;
                    }
                }
            }
        });
        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs_checkdigit = checkDigit(pre_amount);
                if (rs_checkdigit == 1) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else if (rs_checkdigit == 2) {
                    showTextToast(getString(R.string.digit_over_back_two));
                } else if (rs_checkdigit == 3) {
                    showTextToast(getString(R.string.digit_over_front_and_back));
                } else {
                    if (!dotflag) {
                        if (firstdot) {
                            pre_amount = "0.";
                            amount.setText(pre_amount);
                            pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                            p_mdr.setText(pre_surcharge);
                            pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                            dotflag = true;
                            firstzero = false;
                        } else {
                            pre_amount = pre_amount + ".";
                            amount.setText(pre_amount);
                            pre_surcharge = getSurcharge(pre_amount, pre_mdr);
                            p_mdr.setText(pre_surcharge);
                            pre_mdr_amount = getTotalAmount(pre_amount, pre_surcharge, pre_fixed);
                            dotflag = true;
                            firstzero = false;
                        }
                    }
                }
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount.setText("0");
                pre_amount = "0";
                dotflag = false;
                firstdot = true;
                firstzero = true;
                if (autoMerRef.equals(Constants.MERREF_AUTO)) {
                    MerchantRef.setText(autoRef());
                    MerchantRef.setEnabled(false);
                } else {
                    MerchantRef.setText("");
                }
            }
        });
        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strAmount = amount.getText().toString().replaceAll(",", "");
                if (strAmount.equals("") || MerchantRef.getText().toString().equals("")) {
                    showTextToast(getString(R.string.please_fill));
                } else if (Double.parseDouble(strAmount) <= 0) {
                    showTextToast(getString(R.string.set_amount));
                } else if ("H".equals(payType)) {
                    showTextToast(getString(R.string.paytype_not_support));
                } else {
                    pre_amount = strAmount;

                    if (paymentOption.equalsIgnoreCase("SCAN QR PAYMENT")) {
                        Intent intent = new Intent(EnterAmount.this, ScanQRPayment_2.class);
                        intent.putExtra(Constants.MERCHANT_REF, MerchantRef.getText().toString());
                        intent.putExtra(Constants.PRE_AMOUNT, pre_amount);
                        intent.putExtra(Constants.PRE_MDR_AMOUNT, pre_mdr_amount);
                        intent.putExtra(Constants.PRE_MDR, pre_mdr);
                        intent.putExtra(Constants.PRE_SURCHARGE, pre_surcharge);
                        intent.putExtra(Constants.CURRCODE, currCode);
                        intent.putExtra(Constants.CURRENCY, currCode1);
                        intent.putExtra(Constants.MERNAME, merchantName);
                        intent.putExtra(Constants.MERID, getIntent().getStringExtra(Constants.MERID));
                        intent.putExtra(Constants.PAYMETHODLIST, payMethod);
                        intent.putExtra(Constants.pref_Rate, pre_mdr);
                        intent.putExtra(Constants.pref_hideSurcharge, hideSurcharge);
                        intent.putExtra(Constants.pref_Fixed, pre_fixed);
                        intent.putExtra(Constants.PAYTYPE, payType);
                        intent.putExtra(Constants.OPERATORID, getIntent().getStringExtra(Constants.OPERATORID));

                        startActivity(intent);
                    } else if (paymentOption.equalsIgnoreCase("PRESENT QR PAYMENT")) {
                        Intent intent = new Intent(EnterAmount.this, PresentQRPayment_2.class);
                        intent.putExtra(Constants.MERCHANT_REF, MerchantRef.getText().toString());
                        intent.putExtra(Constants.PRE_AMOUNT, pre_amount);
                        intent.putExtra(Constants.PRE_MDR_AMOUNT, pre_mdr_amount);
                        intent.putExtra(Constants.PRE_MDR, pre_mdr);
                        intent.putExtra(Constants.PRE_SURCHARGE, pre_surcharge);
                        intent.putExtra(Constants.CURRCODE, currCode);
                        intent.putExtra(Constants.CURRENCY, currCode1);
                        intent.putExtra(Constants.MERNAME, merchantName);
                        intent.putExtra(Constants.MERID, getIntent().getStringExtra(Constants.MERID));
                        intent.putExtra(Constants.PAYMETHODLIST, payMethod);
                        intent.putExtra(Constants.pref_Rate, pre_mdr);
                        intent.putExtra(Constants.pref_hideSurcharge, hideSurcharge);
                        intent.putExtra(Constants.pref_Fixed, pre_fixed);
                        intent.putExtra(Constants.PAYTYPE, payType);
                        intent.putExtra(Constants.OPERATORID, getIntent().getStringExtra(Constants.OPERATORID));

                        startActivity(intent);

                    } else if (paymentOption.equalsIgnoreCase("CARD PAYMENT")) {
                        Intent intent = new Intent(EnterAmount.this, CardPayment_2.class);
                        startActivity(intent);
                    } else if (paymentOption.equalsIgnoreCase("MOBILE PAYMENT")) {
                        Intent intent = new Intent(EnterAmount.this, MobilePayment_2.class);
                        startActivity(intent);
                    } else if (paymentOption.equalsIgnoreCase("FPS PRESENT QR")) {
                        Intent intent = new Intent(EnterAmount.this, PresentQRPayment_3.class);

                        intent.putExtra("amount", pre_amount);
                        intent.putExtra("MerRequestAmt", pre_amount);
                        intent.putExtra("Surcharge", "0");
                        intent.putExtra("Mdr", "0");
                        intent.putExtra("surC", "F");
                        intent.putExtra("currCode", currCode);
                        intent.putExtra("currCode1", currCode1);
                        intent.putExtra("MerchantRef", MerchantRef.getText().toString());
                        intent.putExtra("hideSurcharge", hideSurcharge);
                        intent.putExtra(Constants.MERNAME, merchantName);
                        intent.putExtra(Constants.MERID, getIntent().getStringExtra(Constants.MERID));
                        intent.putExtra("payMethod", "FPS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            }

        });
    }

    private void showTextToast(String msg) {
        if (numberpadtoast == null) {
            numberpadtoast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            numberpadtoast.setText(msg);
        }
        numberpadtoast.show();
    }

    public static int checkDigit(String c_amount) {
        int front = 0;
        int back = 0;

        int findpoint = c_amount.indexOf(".");

        if (findpoint == (-1)) {
            front = c_amount.length();
            back = 0;
        } else {
            front = findpoint;
            back = (c_amount.length() - 1) - c_amount.indexOf(".");
        }

        Log.d("OTTO", " checkDigit c_amount:" + c_amount);
        Log.d("OTTO", " checkDigit front:" + front);
        Log.d("OTTO", " checkDigit back:" + back);

        Log.d("OTTO", "jduge digit start---------");
        if ((front >= 12) && (back >= 2)) {
            Log.d("OTTO", "over Ten-digit before the decimal point  and  over two decimal places");
            return 1;// over ten digit before the decimal point  and  over two decimal places
        } else if (back >= 2) {
            Log.d("OTTO", "over two decimal places");
            return 2;// over two decimal places
        } else if (front >= 12) {
            Log.d("OTTO", "over Ten-digit before the decimal point");
            return 3;// over ten digit before the decimal point
        } else {
            Log.d("OTTO", "0.01<=amount<=9999999999");
            return 0;//  0.01<=amount<=9999999999
        }
    }

    private String autoRef() {
        //=====KM LIEW 25/05/2018====
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.COUNTER, MODE_PRIVATE);
        int curCounter = pref.getInt("refCounter", 0);

        int runningNum = curCounter + 1;
        int length = String.valueOf(curCounter).length();
        String digit = "000000";
        String MerchantRef = digit.substring(0, digit.length() - length) + runningNum;

        return MerchantRef;
        //============================//>
    }

    private String getSurcharge(String amount, String mdr) {
        double double_mdr = 0;
        double_mdr = Double.parseDouble(mdr);

        String return_surcharge = "0.00";
        try {
            String pattern = "#0.00";
            NumberFormat format = new DecimalFormat(pattern);
            String pattern2 = "#0.000";
            NumberFormat format2 = new DecimalFormat(pattern2);
            return_surcharge = format2.format((Double.parseDouble(amount) * double_mdr));
            return_surcharge = format.format(Double.valueOf(return_surcharge));
        } catch (Exception e) {
            Log.d("OTTO", "getSurcharge exception :" + e);
        }
        Log.d("OTTO1", "return_surcharge:" + return_surcharge);
        return return_surcharge;
    }

    private String getTotalAmount(String amt, String surchar, String fix) {
        amt = amt.replaceAll(",", "");
        double double_amt = 0;
        double_amt = Double.parseDouble(amt);
        double double_surcharge = 0;
        double_surcharge = Double.parseDouble(surchar);
        double double_fix = 0;
        double_fix = Double.parseDouble(fix);

        String return_totalamt = "0.00";
        try {
            String pattern = "#0.00";
            NumberFormat format = new DecimalFormat(pattern);
            String pattern2 = "#0.000";
            NumberFormat format2 = new DecimalFormat(pattern2);
            return_totalamt = format2.format((double_amt + double_surcharge + double_fix));
            return_totalamt = format.format(Double.valueOf(return_totalamt));
        } catch (Exception e) {
            Log.d("OTTO", "getSurcharge exception :" + e);
        }
        Log.d("OTTO1", "return_totalamt:" + return_totalamt);
        return return_totalamt;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        amount.setText("0");
        pre_amount = "0";
        dotflag = false;
        firstdot = true;
        firstzero = true;

        SharedPreferences prefsettings = getApplicationContext().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        String autoMerRef = prefsettings.getString(Constants.pref_MerRef, Constants.default_merchantref);
        if (autoMerRef.equals(Constants.MERREF_AUTO)) {
            MerchantRef.setText(autoRef());
            MerchantRef.setEnabled(false);

        } else {
            MerchantRef.setText("");
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
