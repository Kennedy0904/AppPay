package com.asiapay.paydollar.apppay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.asiapay.paydollar.apppay.Printer.BluetoothSelect;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private final static String TABLE_ID = "Settings";
    private final static String MERCHANT_ID = "merID";

    private HashMap<String, String> map = null;
    ProgressDialog progressDialog;
    DatabaseHelper db;
    AlertDialog.Builder dialog;
    AlertDialog alertDialog;

    String merchantId;
    String payMethod = "";

    Boolean setApiId = false;
    Boolean setApiPw = false;

    Boolean setSettingPW = false;

    Context context = null;
    TextView connectedDevice;

    public SettingsFragment() {
        // Required empty public constructor
    }
    //---------for printer---------//
    String printeraddress = "";
    String printername = "";
    //---------for printer---------//

    int dayTrxChecking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.settings));

        //--Edited 25/07/18 by KJ--//

        LinearLayout setting_merRef = (LinearLayout) rootView.findViewById(R.id.setting_merRef);
        LinearLayout setting_printMode = (LinearLayout) rootView.findViewById(R.id.setting_printingMode);
        LinearLayout setting_apiID = (LinearLayout) rootView.findViewById(R.id.setting_apiID);
        LinearLayout setting_apiPassword = (LinearLayout) rootView.findViewById(R.id.setting_apiPassword);
        LinearLayout refundSameDay = (LinearLayout) rootView.findViewById(R.id.refund_same_day);
        LinearLayout refundPwProtect = (LinearLayout) rootView.findViewById(R.id.refund_pw_protect);
        LinearLayout settingModulePW = (LinearLayout) rootView.findViewById(R.id.setting_pw);
        LinearLayout setting_paymentTimeout = (LinearLayout) rootView.findViewById(R.id.setting_paymentTimeout);
        LinearLayout setting_printer = (LinearLayout) rootView.findViewById(R.id.setting_printer);
        LinearLayout emailSettingControl = (LinearLayout) rootView.findViewById(R.id.email_setting_control);
        LinearLayout emailSetting = (LinearLayout) rootView.findViewById(R.id.emailSetting);
        LinearLayout list6 = (LinearLayout) rootView.findViewById(R.id.setting_email);
        LinearLayout auto_update = (LinearLayout) rootView.findViewById(R.id.updateSettings);
        LinearLayout voidPwProtect = (LinearLayout) rootView.findViewById(R.id.void_pw_protect);
        LinearLayout transaction_checking = (LinearLayout) rootView.findViewById(R.id.transaction_history_checking);
        //--done Edited 25/07/18 by KJ--//


        //initialize settings
        final String[] merchantRef = {getResources().getString(R.string.auto), getResources().getString(R.string.manual)};
        final String[] printingMode = {getResources().getString(R.string.mode1), getResources().getString(R.string.mode2), getResources().getString(R.string.mode3)};
        final String[] language = {getResources().getString(R.string.english), getResources().getString(R.string.trad_chi), getResources().getString(R.string.simp_chi), getResources().getString(R.string.thai)};

        Bundle bundle = getArguments();
        final String orgMerchantId = bundle.getString(Constants.MERID);
        final String merName = bundle.getString(Constants.MERNAME);

        //--Edited 25/07/18 by KJ--//
        payMethod = bundle.getString(Constants.PAYMETHODLIST);
        //--done Edited 25/07/18 by KJ--//

        System.out.println("PayMethod:---------------------" + payMethod);

        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                Constants.SETTINGS, MODE_PRIVATE);

        String settingMode = pref.getString(Constants.pref_setting_mode, Constants.default_setting_mode);

        if (settingMode.equalsIgnoreCase("mode1")) {
            //            setting_apiID.setVisibility(View.VISIBLE);
            //            setting_apiPassword.setVisibility(View.VISIBLE);
            //            merchant_password.setVisibility(View.VISIBLE);
            setting_merRef.setVisibility(View.VISIBLE);
            setting_paymentTimeout.setVisibility(View.VISIBLE);
            //            setting_printer.setVisibility(View.VISIBLE);
            //            setting_printMode.setVisibility(View.VISIBLE);
            refundPwProtect.setVisibility(View.VISIBLE);
            refundSameDay.setVisibility(View.VISIBLE);
            voidPwProtect.setVisibility(View.VISIBLE);
            settingModulePW.setVisibility(View.VISIBLE);
            //            email_setting_control.setVisibility(View.VISIBLE);
            transaction_checking.setVisibility(View.VISIBLE);

            //            auto_update.setVisibility(View.VISIBLE);
        } else if (settingMode.equalsIgnoreCase("mode2")) {
            setting_merRef.setVisibility(View.VISIBLE);
            setting_paymentTimeout.setVisibility(View.VISIBLE);
            //            setting_printer.setVisibility(View.VISIBLE);
            //            setting_printMode.setVisibility(View.VISIBLE);
            refundPwProtect.setVisibility(View.VISIBLE);
            refundSameDay.setVisibility(View.VISIBLE);
            //            merchant_password.setVisibility(View.VISIBLE);
            voidPwProtect.setVisibility(View.VISIBLE);
            settingModulePW.setVisibility(View.VISIBLE);
            //            email_setting_control.setVisibility(View.VISIBLE);
            transaction_checking.setVisibility(View.VISIBLE);
        }

        //check language
        String lang = pref.getString(Constants.pref_Lang, "");
        changeLang(lang);


        // Get Settings Values in SharedPreference and Database and Display on TextViews
        TextView disLang = (TextView) rootView.findViewById(R.id.language);
        String strLang = "";
        if (pref.getString(Constants.pref_Lang, Constants.default_language).equalsIgnoreCase(Constants.LANG_ENG)) {
            strLang = getString(R.string.english);
        } else if (pref.getString(Constants.pref_Lang, Constants.default_language).equalsIgnoreCase(Constants.LANG_SIMP)) {
            strLang = getString(R.string.simp_chi);
        } else if (pref.getString(Constants.pref_Lang, Constants.default_language).equalsIgnoreCase(Constants.LANG_TRAD)) {
            strLang = getString(R.string.trad_chi);
        } else if (pref.getString(Constants.pref_Lang, Constants.default_language).equalsIgnoreCase(Constants.LANG_THAI)) {
            strLang = getString(R.string.thai);
        }
        disLang.setText(strLang);


        final TextView merRef = (TextView) rootView.findViewById(R.id.merRef);
        String strMerRef;
        if (pref.getString(Constants.pref_MerRef, Constants.default_merchantref).equalsIgnoreCase(Constants.MERREF_AUTO)) {
            strMerRef = getString(R.string.auto);
        } else {
            strMerRef = getString(R.string.manual);
        }
        merRef.setText(strMerRef);

        TextView printMode = (TextView) rootView.findViewById(R.id.receiptPrinting);
        String strPrintMode = "";
        if (pref.getString(Constants.pref_PrintMode, Constants.default_printmode).equalsIgnoreCase(Constants.PRINT_MODE1)) {
            strPrintMode = getString(R.string.mode1);
        } else if (pref.getString(Constants.pref_PrintMode, Constants.default_printmode).equalsIgnoreCase(Constants.PRINT_MODE2)) {
            strPrintMode = getString(R.string.mode2);
        } else if (pref.getString(Constants.pref_PrintMode, Constants.default_printmode).equalsIgnoreCase(Constants.PRINT_MODE3)) {
            strPrintMode = getString(R.string.mode3);
        }
        printMode.setText(strPrintMode);

        TextView tv6 = (TextView) rootView.findViewById(R.id.same_day_refund_enable);
        TextView tv7 = (TextView) rootView.findViewById(R.id.pw_protect_enable);
        TextView void_protect_enable = (TextView) rootView.findViewById(R.id.void_protect_enable);

        Switch enableSameDayRefund = (Switch) rootView.findViewById(R.id.enableSameDayRefund);
        Switch enablePwProtection = (Switch) rootView.findViewById(R.id.enablePwProtection);
        Switch enableVoidPwProtection = (Switch) rootView.findViewById(R.id.enableVoidPwProtection);

        String sameDayRefundOnOff;
        String pwProtectOnOff;

        if (!settingMode.equalsIgnoreCase("mode3")) {
            if (pref.getString(Constants.pref_same_day_refund, Constants.default_same_day_refund).equalsIgnoreCase("ON")) {
                sameDayRefundOnOff = getString(R.string.on);
            } else {
                sameDayRefundOnOff = getString(R.string.off);
            }
            tv6.setText(sameDayRefundOnOff);

            if (getString(R.string.off).equalsIgnoreCase(sameDayRefundOnOff)) {
                enableSameDayRefund.setChecked(false);
            } else {
                enableSameDayRefund.setChecked(true);
            }
        }

        if (!settingMode.equalsIgnoreCase("mode3")) {
            if (pref.getString(Constants.pref_refund_pw_protection, Constants.default_refund_pw_protection).equalsIgnoreCase("ON")) {
                pwProtectOnOff = getString(R.string.on);
            } else {
                pwProtectOnOff = getString(R.string.off);
            }
            tv7.setText(pwProtectOnOff);

            if (getString(R.string.off).equalsIgnoreCase(pwProtectOnOff)) {
                enablePwProtection.setChecked(false);
            } else {
                enablePwProtection.setChecked(true);
            }
        }

        if(!settingMode.equalsIgnoreCase("mode3")){
            if (pref.getString(Constants.pref_void_pw_protection, Constants.default_void_pw_protection).equalsIgnoreCase("ON")) {
                pwProtectOnOff = getString(R.string.on);
            } else {
                pwProtectOnOff = getString(R.string.off);
            }
            void_protect_enable.setText(pwProtectOnOff);

            if (getString(R.string.off).equalsIgnoreCase(pwProtectOnOff)) {
                enableVoidPwProtection.setChecked(false);
            } else {
                enableVoidPwProtection.setChecked(true);
            }
        }

        // Control Stationery Order
        /*TextView tv5 = (TextView) rootView.findViewById(R.id.onOff);
        Switch swOnOff = (Switch) rootView.findViewById(R.id.orderControl);
        String seccode;
        if (!settingMode.equalsIgnoreCase("mode3")) {
            if (pref.getString(Constants.pref_order_control, Constants.default_order_control).equalsIgnoreCase("ON")) {
                seccode = getString(R.string.on);
                emailSetting.setVisibility(View.VISIBLE);
            } else {
                seccode = getString(R.string.off);
                emailSetting.setVisibility(View.GONE);
            }
            tv5.setText(seccode);

            if (getString(R.string.off).equalsIgnoreCase(seccode)) {
                swOnOff.setChecked(false);
            } else {
                swOnOff.setChecked(true);
            }
        }*/

        TextView supportEmail = (TextView) rootView.findViewById(R.id.supportEmail);
        String orderEmail = pref.getString(Constants.pref_SupEmail, Constants.default_supportEmail);
        if (orderEmail.equalsIgnoreCase("")) {
            supportEmail.setText(orderEmail);
        } else {
            supportEmail.setText(orderEmail);
        }

        DesEncrypter encrypt;
        try {
            encrypt = new DesEncrypter(merName);
            merchantId = encrypt.encrypt(orgMerchantId);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        String whereargs[] = {merchantId};

        DatabaseHelper db = new DatabaseHelper(getActivity());

        final TextView apiIDtxt = (TextView) rootView.findViewById(R.id.apiIDtxt);
        String apiID = db.getSingleData(Constants.DB_TABLE_ID, Constants.DB_API_ID, Constants.DB_MERCHANT_ID, whereargs);

        System.out.println("---------------" + apiID);
        if (apiID != null) {
            String decApiID = "";
            try {
                DesEncrypter encrypter = new DesEncrypter(merName);
                decApiID = encrypter.decrypt(apiID);
                System.out.println("---------------" + decApiID);
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String proID = decApiID.replaceAll(".", "*");
            apiIDtxt.setText(proID);
            setApiId = true;
        } else {
            apiIDtxt.setText(getString(R.string.not_set));
            setApiId = false;
        }

        final TextView apiPasswordtxt = (TextView) rootView.findViewById(R.id.apiPasswordtxt);
        String apiPW = db.getSingleData(Constants.DB_TABLE_ID, Constants.DB_API_PW, Constants.DB_MERCHANT_ID, whereargs);
        if (apiPW != null) {
            String decApiPW = "";
            try {
                DesEncrypter encrypter = new DesEncrypter(merName);
                decApiPW = encrypter.decrypt(apiPW);
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String propw = decApiPW.replaceAll(".", "*");
            apiPasswordtxt.setText(propw);
            setApiPw = true;
        } else {
            apiPasswordtxt.setText(getString(R.string.not_set));
            setApiPw = false;
        }

        final TextView settingPWtext = (TextView) rootView.findViewById(R.id.settingPwText);
        String settingPW = pref.getString(Constants.pref_Mer_SettingPW, Constants.default_settingPW);
        if (settingPW != null) {

            String propw = "";
            for (int i = 0; i < settingPW.length(); i++) {
                propw = propw + "*";
            }

            if (settingPW.equals("Admin8188")) {
                settingPWtext.setText(R.string.system_default);
            } else {
                settingPWtext.setText(propw);
            }

            setSettingPW = true;
        } else {
            settingPWtext.setText(getString(R.string.not_set));
            setSettingPW = false;
        }

        TextView mpos_version = (TextView) rootView.findViewById(R.id.mpos_version);
        mpos_version.setText(Constants.current_VersionCode);

        //Added on 20190923
        final TextView historyDays  = (TextView) rootView.findViewById(R.id.historyDays);
        int day = pref.getInt(Constants.pref_transaction_checking, Constants.default_transaction_checking);
        historyDays.setText(String.valueOf(day) + " " +getString(R.string.day));

        //When Click Actions
        LinearLayout setting_language = (LinearLayout) rootView.findViewById(R.id.setting_language);
        setting_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.select_language);
                dialog.setItems(language, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int which) {
                        // TODO Auto-generated method stub
                        String prefLang;
                        switch (which) {
                            case 0:
                                prefLang = Constants.LANG_ENG;
                                break;
                            case 1:
                                prefLang = Constants.LANG_TRAD;
                                break;
                            case 2:
                                prefLang = Constants.LANG_SIMP;
                                break;
                            case 3:
                                prefLang = Constants.LANG_THAI;
                                break;
                            default:
                                prefLang = Constants.LANG_ENG;
                                break;
                        }
                        TextView disLang = (TextView) rootView.findViewById(R.id.language);
                        disLang.setText(language[which]);
                        SharedPreferences pref = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(Constants.pref_Lang, prefLang);
                        edit.commit();
                        changeLang(prefLang);
                        restartActivity();
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        setting_merRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.select_merchant_ref);
                dialog.setItems(merchantRef, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int which) {
                        // TODO Auto-generated method stub
                        String mref = "";
                        switch (which) {
                            case 0:
                                mref = Constants.MERREF_AUTO;
                                break;
                            case 1:
                                mref = Constants.MERREF_MANUAL;
                                break;
                            default:
                        }
                        TextView merRef = (TextView) rootView.findViewById(R.id.merRef);
                        merRef.setText(merchantRef[which]);
                        SharedPreferences pref = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(Constants.pref_MerRef, mref);
                        edit.commit();
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        setting_printMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.select_printing_mode);
                dialog.setItems(printingMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int which) {
                        // TODO Auto-generated method stub
                        String printmode = "";
                        switch (which) {
                            case 0:
                                printmode = Constants.PRINT_MODE1;
                                break;
                            case 1:
                                printmode = Constants.PRINT_MODE2;
                                break;
                            case 2:
                                printmode = Constants.PRINT_MODE3;
                                break;
                            default:
                        }
                        TextView printMode = (TextView) rootView.findViewById(R.id.receiptPrinting);
                        printMode.setText(printingMode[which]);
                        SharedPreferences pref = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(Constants.pref_PrintMode, printmode);
                        edit.commit();
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        setting_apiID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.input_apiID);

                final EditText inputApiId = new EditText(getActivity());
                if (setApiId) {
                    inputApiId.setHint(getString(R.string.unchanged));
                }

                dialog.setView(inputApiId);
                dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = inputApiId.getText().toString().trim();
                        if (!"".equals(value)) {
                            String proV = value.replaceAll(".", "*");
                            apiIDtxt.setText(proV);

                            String encrypted = "";
                            try {
                                DesEncrypter encrypter = new DesEncrypter(merName);
                                encrypted = encrypter.encrypt(value);
                            } catch (NoSuchAlgorithmException e) {
                                // TODO Auto-generated catch block
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                            }

                            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                            dbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("apiID", encrypted);
                            System.out.println("-------------" + values);
                            String whereClause = MERCHANT_ID + "=?";
                            String whereArgs[] = {merchantId};
                            dbHelper.update(TABLE_ID, values, whereClause, whereArgs);
                            dbHelper.close();
                            setApiId = true;
                        }

                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        setting_apiPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.input_apiPW);
                //EditText
                final EditText inputApiPassword = new EditText(getActivity());
                inputApiPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                if (setApiPw) {
                    inputApiPassword.setHint(getString(R.string.unchanged));
                }
                dialog.setView(inputApiPassword);
                dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = inputApiPassword.getText().toString().trim();
                        if (!"".equals(value)) {
                            String provalue = value.replaceAll(".", "*");
                            apiPasswordtxt.setText(provalue);
                            String encrypted = "";
                            try {
                                DesEncrypter encrypter = new DesEncrypter(merName);
                                encrypted = encrypter.encrypt(value);
                            } catch (NoSuchAlgorithmException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                            dbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("apipw", encrypted);
                            String whereClause = MERCHANT_ID + "=?";
                            String whereArgs[] = {merchantId};
                            dbHelper.update(TABLE_ID, values, whereClause, whereArgs);
                            dbHelper.close();
                            setApiPw = true;
                        } else {
                        }
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();

            }
        });

        //--Edited 25/07/18 by KJ--//
        setting_paymentTimeout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                final Intent timeout = new Intent(getActivity(), PaymentTimeoutSetting.class);
                timeout.putExtra(Constants.MERID, getArguments().getString(Constants.MERID));
                timeout.putExtra(Constants.MERNAME, getArguments().getString(Constants.MERNAME));
                timeout.putExtra(Constants.PAYMETHODLIST, payMethod);
                startActivity(timeout);
            }
        });
        //--done Edited 25/07/18 by KJ--//

        auto_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCancelable(false);

                System.out.println("click-------------------------------------------");
                MerchantSettingsInfo settingInfo = new MerchantSettingsInfo(SettingsFragment.this, progressDialog, getPrefPayGate());
                settingInfo.execute(orgMerchantId, merName);
            }
        });

        settingModulePW.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.input_settingPW);
                //EditText
                final EditText inputSettingPassword = new EditText(getActivity());
                inputSettingPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                if (setSettingPW) {
                    inputSettingPassword.setHint(getString(R.string.unchanged));
                }
                dialog.setView(inputSettingPassword);
                dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = inputSettingPassword.getText().toString().trim();
                        if (!"".equals(value)) {
                            String provalue = value.replaceAll(".", "*");
                            settingPWtext.setText(provalue);
                            String encrypted = "";
                            try {
                                DesEncrypter encrypter = new DesEncrypter(merName);
                                encrypted = encrypter.encrypt(value);
                            } catch (NoSuchAlgorithmException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            //edit shared pref data
                            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                                    Constants.SETTINGS, MODE_PRIVATE);
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString(Constants.pref_Mer_SettingPW, value);
                            edit.commit();
                        } else {
                        }
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();

            }
        });

        connectedDevice = (TextView) rootView.findViewById(R.id.connectedDevice);

        printeraddress = pref.getString(Constants.printer_address, "");
        printername = pref.getString(Constants.printer_name, "");
        if ("".equals(printeraddress) || printeraddress == null) {
            printeraddress = "";
        }
        if ("".equals(printername) || printername == null) {
            printername = "";
        }
        connectedDevice.setText(printername);

        setting_printer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), BluetoothSelect.class);
                startActivity(intent);
            }
        });

        list6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.input_order_Email);
                //EditText
                final EditText inputSupportEmail = new EditText(getActivity());
                dialog.setView(inputSupportEmail);
                dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        TextView tv1 = (TextView) rootView.findViewById(R.id.supportEmail);
                        String value = inputSupportEmail.getText().toString();
                        if ("".equals(value)) {
                            tv1.setText(getString(R.string.not_set));
                        } else {
                            tv1.setText(value);
                        }
                        //edit shared pref data
                        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                                Constants.SETTINGS, MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(Constants.pref_SupEmail, value);
                        edit.commit();
                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        Switch onOff = (Switch) rootView.findViewById(R.id.orderControl);
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tvOnOff = (TextView) rootView.findViewById(R.id.onOff);
                SharedPreferences prefsettings = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                String stat = (prefsettings.getString(Constants.pref_order_control, Constants.default_order_control));
                String newstat;
                String rnewstat;

                final LinearLayout emailSetting = (LinearLayout) rootView.findViewById(R.id.emailSetting);


                if (isChecked) {
                    newstat = "ON";
                    rnewstat = getString(R.string.on);
                    emailSetting.setVisibility(View.VISIBLE);

                    final ScrollView s = (ScrollView) getActivity().findViewById(R.id.scroll1);
                    s.post(new Runnable() {
                        public void run() {
                            s.smoothScrollTo(0, s.getBottom());
                        }
                    });

                } else {
                    newstat = "OFF";
                    rnewstat = getString(R.string.off);
                    emailSetting.setVisibility(View.GONE);
                }

                tvOnOff.setText(rnewstat);
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString(Constants.pref_order_control, newstat);
                edit.commit();
            }
        });

        enableSameDayRefund.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tvEnableSameDayRefund = (TextView) rootView.findViewById(R.id.same_day_refund_enable);
                SharedPreferences prefsettings = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                String stat = (prefsettings.getString(Constants.pref_same_day_refund, Constants.default_same_day_refund));
                String newstat;
                String rnewstat;

                if (isChecked) {
                    newstat = "ON";
                    rnewstat = getString(R.string.on);
                } else {
                    newstat = "OFF";
                    rnewstat = getString(R.string.off);
                }

                tvEnableSameDayRefund.setText(rnewstat);
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString(Constants.pref_same_day_refund, newstat);
                edit.commit();
            }
        });

        enablePwProtection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tvEnablePwProtect = (TextView) rootView.findViewById(R.id.pw_protect_enable);
                SharedPreferences prefsettings = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                String stat = (prefsettings.getString(Constants.pref_refund_pw_protection, Constants.default_refund_pw_protection));
                String newstat;
                String rnewstat;

                if (isChecked) {
                    newstat = "ON";
                    rnewstat = getString(R.string.on);
                } else {
                    newstat = "OFF";
                    rnewstat = getString(R.string.off);
                }

                tvEnablePwProtect.setText(rnewstat);
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString(Constants.pref_refund_pw_protection, newstat);
                edit.commit();
            }
        });

        enableVoidPwProtection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tvVoidEnablePwProtect = (TextView) rootView.findViewById(R.id.void_protect_enable);
                SharedPreferences prefsettings = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                String stat = (prefsettings.getString(Constants.pref_void_pw_protection, Constants.default_void_pw_protection));
                String newstat;
                String rnewstat;

                if (isChecked) {
                    newstat = "ON";
                    rnewstat = getString(R.string.on);
                } else {
                    newstat = "OFF";
                    rnewstat = getString(R.string.off);
                }

                tvVoidEnablePwProtect.setText(rnewstat);
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                        Constants.SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString(Constants.pref_void_pw_protection, newstat);
                edit.commit();
            }
        });

        //Added on 20190923
        transaction_checking = (LinearLayout) rootView.findViewById(R.id.transaction_history_checking);
        transaction_checking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
                View view = mLayoutInflater.inflate(R.layout.dialogbox_transaction_checking, null);

                Button addDay  = (Button) view.findViewById(R.id.addDayHistory);
                Button minusDay = (Button) view.findViewById(R.id.minusDayHistory);
                final TextView days =(TextView) view.findViewById(R.id.dayChecking);

                final SharedPreferences pref = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
                dayTrxChecking = pref.getInt(Constants.pref_transaction_checking, Constants.default_transaction_checking);
                days.setText(String.valueOf(dayTrxChecking));

                addDay.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (dayTrxChecking < Constants.max_transaction_checking) {
                            dayTrxChecking = dayTrxChecking + 1;
                        }

                        days.setText(String.valueOf(dayTrxChecking));
                    }
                });

                minusDay.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (dayTrxChecking > Constants.default_transaction_checking) {
                            dayTrxChecking = dayTrxChecking - 1;
                        }

                        days.setText(String.valueOf(dayTrxChecking));
                    }
                });

                addDay.setOnTouchListener(new View.OnTouchListener() {

                    private Handler mHandler;

                    @Override public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (mHandler != null) return true;
                                mHandler = new Handler();
                                mHandler.postDelayed(mAction, 150);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mHandler == null) return true;
                                mHandler.removeCallbacks(mAction);
                                mHandler = null;
                                break;
                        }
                        return false;
                    }

                    Runnable mAction = new Runnable() {
                        @Override public void run() {
                            if (dayTrxChecking < Constants.max_transaction_checking) {
                                dayTrxChecking = dayTrxChecking + 1;
                            }

                            days.setText(String.valueOf(dayTrxChecking));
                            mHandler.postDelayed(this, 150);
                        }
                    };
                });

                minusDay.setOnTouchListener(new View.OnTouchListener() {

                    private Handler mHandler;

                    @Override public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (mHandler != null) return true;
                                mHandler = new Handler();
                                mHandler.postDelayed(mAction, 150);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mHandler == null) return true;
                                mHandler.removeCallbacks(mAction);
                                mHandler = null;
                                break;
                        }
                        return false;
                    }

                    Runnable mAction = new Runnable() {
                        @Override public void run() {
                            if (dayTrxChecking > Constants.default_transaction_checking) {
                                dayTrxChecking = dayTrxChecking - 1;
                            }

                            days.setText(String.valueOf(dayTrxChecking));
                            mHandler.postDelayed(this, 150);
                        }
                    };
                });

                dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.select_transaction_days);
                dialog.setView(view);
                dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences.Editor edit = pref.edit();
                        edit.putInt(Constants.pref_transaction_checking, Integer.valueOf(days.getText().toString()));
                        edit.commit();

                        historyDays.setText(days.getText().toString() + " " + getString(R.string.day));

                    }
                });
                alertDialog = dialog.create();
                alertDialog.show();

                //                dialog.setItems(transaction_days, new DialogInterface.OnClickListener() {
                //                    @Override
                //                    public void onClick(DialogInterface arg0, int which) {
                //                        // TODO Auto-generated method stub
                //
                //                        TextView historyDays = (TextView) rootView.findViewById(R.id.historyDays);
                //                        historyDays.setText(transaction_days[which]);
                //                        SharedPreferences pref = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
                //                        SharedPreferences.Editor edit = pref.edit();
                //                        edit.putInt(Constants.pref_transaction_checking, which + 1);
                //                        edit.commit();
                //                    }
                //                });
                //                alertDialog = dialog.create();
                //                alertDialog.show();
            }
        });

        return rootView;
    }

    public void changeLang(String lang) {
        Locale myLocale = null;
        if (lang.equalsIgnoreCase(Constants.LANG_ENG)) {
            myLocale = Locale.ENGLISH;
        } else if (lang.equalsIgnoreCase(Constants.LANG_TRAD)) {
            myLocale = Locale.TRADITIONAL_CHINESE;
        } else if (lang.equalsIgnoreCase(Constants.LANG_SIMP)) {
            myLocale = Locale.SIMPLIFIED_CHINESE;
        } else if (lang.equalsIgnoreCase(Constants.LANG_THAI)) {
            myLocale = new Locale("th", "TH");
        }

        Configuration config = new Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }

    private void restartActivity() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    //--Edited 25/07/18 by KJ--//
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                Constants.SETTINGS, MODE_PRIVATE);

        printername = pref.getString(Constants.printer_name, "");

        if ("".equals(printername) || printername == null) {
            printername = "";
        }
        connectedDevice.setText(printername);
    }
    //--done Edited 25/07/18 by KJ--//

    public String getPrefPayGate() {
        SharedPreferences prefsettings = getActivity().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        String prefpaygate = prefsettings.getString(Constants.pref_PayGate, Constants.default_paygate);
        return prefpaygate;
    }


    public void getMerchantSettingsInfo(String merchantId, String merchantName, String language, String merchantReferenceNo, String printMode,
                                        String settingsPassword, String stationeryOrder, String orderEmail, String alipayTimeOut,
                                        String wechatpayTimeOut) {
        String merid = "";
        String mref = "";
        String printmode = "";
        String prefLang = "";
        if (merchantId == null) {
            dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle(R.string.message);
            dialog.setMessage(R.string.no_update);

            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            alertDialog = dialog.create();
            alertDialog.show();


        } else {
            SharedPreferences prefsettings = getActivity().getApplicationContext().getSharedPreferences(
                    Constants.SETTINGS, MODE_PRIVATE);
            SharedPreferences.Editor edit = prefsettings.edit();

            if (language.equals("eng")) {
                prefLang = Constants.LANG_ENG;
            } else if (language.equals("simp_chi")) {
                prefLang = Constants.LANG_SIMP;
            } else if (language.equals("trad_chi")) {
                prefLang = Constants.LANG_TRAD;
            } else if (language.equals("thai")) {
                prefLang = Constants.LANG_THAI;
            } else {
                prefLang = Constants.LANG_ENG;
            }
            Log.d("12345555", merchantReferenceNo);
            if (merchantReferenceNo.equals("Auto")) {
                mref = Constants.MERREF_AUTO;
            } else {
                mref = Constants.MERREF_MANUAL;
            }

            if (printMode.equals("mode1")) {
                printmode = Constants.PRINT_MODE1;
            } else if (printMode.equals("mode2")) {
                printmode = Constants.PRINT_MODE2;
            } else if (printMode.equals("mode3")) {
                printmode = Constants.PRINT_MODE3;
            }

            edit.putString(Constants.pref_Lang, prefLang);
            edit.putString(Constants.pref_Mer_SettingPW, settingsPassword);
            edit.putString(Constants.pref_PrintMode, printmode);
            edit.putString(Constants.pref_MerRef, mref);

            TextView tv5 = (TextView) getActivity().findViewById(R.id.onOff);
            Switch swOnOff = (Switch) getActivity().findViewById(R.id.orderControl);

            if(stationeryOrder.equals("on")){
                edit.putString(Constants.pref_order_control, "ON");
                tv5.setText(R.string.on);
                swOnOff.setChecked(true);
            } else {
                edit.putString(Constants.pref_order_control, "OFF");
                tv5.setText(R.string.off);
                swOnOff.setChecked(false);
            }

            edit.putString(Constants.pref_SupEmail, orderEmail);

            edit.commit();
            changeLang(prefLang);

            DesEncrypter encrypt;
            try {
                encrypt = new DesEncrypter(merchantName);
                merid = encrypt.encrypt(merchantId);
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Constants.DB_ALIPAY_TIMEOUT, alipayTimeOut);
            values.put(Constants.DB_WECHAT_TIMEOUT, wechatpayTimeOut);
            String whereClause = MERCHANT_ID + "=?";
            String whereArgs[] = {merid};
            dbHelper.upsert(TABLE_ID, values, whereClause, whereArgs);
            dbHelper.close();

            dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle(R.string.message);
            dialog.setMessage(R.string.configuration_updated);

            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    restartActivity();
                }
            });
            alertDialog = dialog.create();
            alertDialog.show();


        }
    }
}
