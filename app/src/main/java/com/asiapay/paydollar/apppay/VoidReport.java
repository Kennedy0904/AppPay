package com.asiapay.paydollar.apppay;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asiapay.paydollar.apppay.Printer.PAX.GetObj;
import com.asiapay.paydollar.apppay.Printer.PrintUtil;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import eu.erikw.PullToRefreshListView;

public class VoidReport extends AppCompatActivity implements VoidReportInterface {

    private VoidReportAdapter adapter;
    String lang;
    private PullToRefreshListView lvCommonListView;
    String date1;
    String date2;

    Spinner spinner;
    String filterStatus;
    String filterRef;
    String refno;
    String merchantId;

    String userID;
    String currency = "";

    int pageNo = Constants.CURRENT_PAGE_NO;

    String[] b;
    String[] c;
    String[] ref;

    RelativeLayout voidFooter;
    Button printVoid;
    ProgressDialog progressDialog;
    TextView tvnone;
    LinearLayout linearResult;
    RelativeLayout loadingPanel;
    ProgressBar pageLoading;
    ArrayList<Record> record_data = new ArrayList<Record>();

    private String user;
    private String password;
    private String merName;

    //for print function
    String printeraddress = "";
    String printername = "";
    PrintUtil printUtil = null;
    Map<String, String> report_value = null;
    Map<String, String> label = null;
    GetObj getObj = null;

    //label
    String label_print_at = "";
    String label_from = "";
    String label_to = "";
    String label_merName = "";
    String label_title = "";
    String label_header = "";
    String label_footer = "";

    String label_ALIPAYHKOFFL = "";
    String label_ALIPAYCNOFFL = "";
    String label_ALIPAYOFFL = "";
    String label_BOOSTOFFL = "";
    String label_GCASHOFFL = "";
    String label_GRABPAYOFFL = "";
    String label_master = "";
    String label_OEPAYOFFL = "";
    String label_PROMPTPAYOFFL = "";
    String label_UNIONPAYOFFL = "";
    String label_visa = "";
    String label_WECHATOFFL = "";
    String label_WECHATHKOFFL = "";
    String label_WECHATONL = "";

    String label_total_void = "";
    String label_total_voidAmnt = "";

    String label_trx_date = "";
    String label_amt = "";
    String label_merRef = "";
    String label_paymentRef = "";
    String label_qr_number = "";

    //ALIPAYHKOFFL
    int ALIPAYHKOFFLTotalVoid = 0;
    double ALIPAYHKOFFLVoidAmnt = 0;
    ArrayList<Record> ALIPAYHKOFFLvoidArray;

    //ALIPAYCNOFFL
    int ALIPAYCNOFFLTotalVoid = 0;
    double ALIPAYCNOFFLVoidAmnt = 0;
    ArrayList<Record> ALIPAYCNOFFLvoidArray;

    //ALIPAYOFFL
    int ALIPAYOFFLTotalVoid = 0;
    double ALIPAYOFFLVoidAmnt = 0;
    ArrayList<Record> ALIPAYOFFLvoidArray;

    //BOOSTOFFL
    int BOOSTOFFLTotalVoid = 0;
    double BOOSTOFFLVoidAmnt = 0;
    ArrayList<Record> BOOSTOFFLvoidArray;

    //GCASHOFFL
    int GCASHOFFLTotalVoid = 0;
    double GCASHOFFLVoidAmnt = 0;
    ArrayList<Record> GCASHOFFLvoidArray;

    //GRABPAYOFFL
    int GRABPAYOFFLTotalVoid = 0;
    double GRABPAYOFFLVoidAmnt = 0;
    ArrayList<Record> GRABPAYOFFLvoidArray;

    //MASTER
    int MasterTotalVoid = 0;
    double masterVoidAmnt = 0;
    ArrayList<Record> mastervoidArray;

    //OEPAYOFFL
    int OEPAYOFFLTotalVoid = 0;
    double OEPAYOFFLVoidAmnt = 0;
    ArrayList<Record> OEPAYOFFLvoidArray;

    //PROMPTPAYOFFL 20190815
    int PROMPTPAYOFFLTotalVoid = 0;
    double PROMPTPAYOFFLVoidAmnt = 0;
    ArrayList<Record> PROMPTPAYOFFLvoidArray;

    //UNIONPAYOFFL 20190917
    int UNIONPAYOFFLTotalVoid = 0;
    double UNIONPAYOFFLVoidAmnt = 0;
    ArrayList<Record> UNIONPAYOFFLvoidArray;

    //VISA
    int VisaTotalVoid = 0;
    double visaVoidAmnt = 0;
    ArrayList<Record> visavoidArray;

    //WECHATOFFL
    int WECHATOFFLTotalVoid = 0;
    double WECHATOFFLVoidAmnt = 0;
    ArrayList<Record> WECHATOFFLvoidArray;

    //WECHATHKOFFL
    int WECHATHKOFFLTotalVoid = 0;
    double WECHATHKOFFLVoidAmnt = 0;
    ArrayList<Record> WECHATHKOFFLvoidArray;

    //WECHATONL
    int WECHATONLTotalVoid = 0;
    double WECHATONLVoidAmnt = 0;
    ArrayList<Record> WECHATONLvoidArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_report);
        setTitle(R.string.void_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefsettings = getApplicationContext().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        //check language
        lang = prefsettings.getString(Constants.pref_Lang, Constants.default_language);
        changeLang(lang);
        //---------for print func---------//
        printeraddress = prefsettings.getString(Constants.printer_address, "");
        printername = prefsettings.getString(Constants.printer_name, "");
        if ("".equals(printeraddress) || printeraddress == null) {
            printeraddress = "";
        }
        if ("".equals(printername) || printername == null) {
            printername = "";
        }
        Log.d("OTTO", "device SETTEXT>>" + printername + ":" + printeraddress);

        //Initialize PAX Printer
        getObj = new GetObj(getApplicationContext());
        //---------for print func---------//

        Intent his_list = getIntent();
        filterStatus = "Voided";
        date1 = his_list.getStringExtra("fromDate");
        date2 = his_list.getStringExtra("toDate");
        refno = his_list.getStringExtra("refno");
        filterRef = his_list.getStringExtra("refFilter");
        merchantId = his_list.getStringExtra(Constants.MERID);

        getApiAccount();

        TextView tv1 = (TextView) findViewById(R.id.txtdate1);
        TextView tv2 = (TextView) findViewById(R.id.txtdate2);
        tv1.setText(date1);
        tv2.setText(date2);

        linearResult = (LinearLayout) findViewById(R.id.linearResult);
        voidFooter = (RelativeLayout) findViewById(R.id.voidFooter);
        lvCommonListView = (PullToRefreshListView) findViewById(R.id.voidlistView);
        lvCommonListView.onRefreshComplete();
        tvnone = (TextView) findViewById(R.id.empty_view);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        pageLoading = (ProgressBar) findViewById(R.id.progressBar2);


        // Printing Function
        /*printRefund = (Button) findViewById(R.id.print_refund);
        printRefund.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                label_title = getString(R.string.void_report);

                label_merName = getString(R.string.merName_label);
                label_from = getString(R.string.from);
                label_to = getString(R.string.to);
                label_print_at = getString(R.string.print_at_label);

                label_footer = getString(R.string.report_end);

                label_ALIPAYHKOFFL = getString(R.string.ALIPAYHKOFFL_label);
                label_ALIPAYCNOFFL = getString(R.string.ALIPAYCNOFFL_label);
                label_ALIPAYOFFL = getString(R.string.ALIPAYOFFL_label);
                label_WECHATOFFL = getString(R.string.WECHATOFFL_label);
                label_WECHATHKOFFL = getString(R.string.WECHATHKOFFL_label);
                label_WECHATONL = getString(R.string.WECHATONL_label);
                label_BOOSTOFFL = getString(R.string.BOOSTOFFL_label);
                label_visa = getString(R.string.visa_label);
                label_master = getString(R.string.master_label);

                label_total_refund = getString(R.string.total_transaction_label);
                label_total_refundAmnt = getString(R.string.total_amount_label);

                label_trx_date = getString(R.string.transaction_date_label);
                label_amt = getString(R.string.amount_label);
                label_merRef = getString(R.string.merchant_ref_label);
                label_paymentRef = getString(R.string.payment_ref_label);
                label_qr_number = getString(R.string.txn_number_label);

                label = new TreeMap<String, String>();
                label.put("Title", label_title);

                //====Merchant Information===//
                label.put("MerNameLabel", label_merName);
                label.put("FromDateLabel", label_from);
                label.put("ToDateLabel", label_to);
                //============================//

                label.put("Header", label_header);
                label.put("Footer", label_footer);

                 label.put("ALIPAYHKOFFL", label_ALIPAYHKOFFL);
                label.put("ALIPAYCNOFFL", label_ALIPAYCNOFFL);
                label.put("ALIPAYOFFL", label_ALIPAYOFFL);
                label.put("BOOSTOFFL", label_BOOSTOFFL);
                label.put("GCASHOFFL", label_GCASHOFFL);
                label.put("GRABPAYOFFL", label_GCASHOFFL);
                label.put("MASTER", label_master);
                label.put("OEPAYOFFL", label_OEPAYOFFL);
                label.put("PROMPTPAYOFFL", label_PROMPTPAYOFFL);
                label.put("UNIONPAYOFFL", label_UNIONPAYOFFL);
                label.put("VISA", label_visa);
                label.put("WECHATOFFL", label_WECHATOFFL);
                label.put("WECHATHKOFFL", label_WECHATHKOFFL);
                label.put("WECHATONL", label_WECHATONL);

                label.put("TotalTrxLabel", label_total_refund);
                label.put("TotalAmntLabel", label_total_refundAmnt);
                label.put("NoTxnFound", getString(R.string.noTxnFound));

                label.put("TransactionDate", label_trx_date);
                label.put("Amount", label_amt);
                label.put("MerRef", label_merRef);
                label.put("PaymentRef", label_paymentRef);
                label.put("QRNumber", label_qr_number);

                report_value = new TreeMap<String, String>();
                report_value.put("MerName", merName);
                report_value.put("From_date", date1);
                report_value.put("To_date", date2);
                report_value.put("currCode", currency);

                report_value.put("TotalRefundALIPAYHKOFFL", Integer.toString(totalRefundALIPAYHKOFFL));
                report_value.put("RefundAmntALIPAYHKOFFL", String.format("%,.2f", ALIPAYHKOFFLRefundAmnt));

                report_value.put("TotalRefundALIPAYCNOFFL", Integer.toString(totalRefundALIPAYCNOFFL));
                report_value.put("RefundAmntALIPAYCNOFFL", String.format("%,.2f", ALIPAYCNOFFLRefundAmnt));

                report_value.put("TotalRefundALIPAYOFFL", Integer.toString(totalRefundALIPAYOFFL));
                report_value.put("RefundAmntALIPAYOFFL", String.format("%,.2f", ALIPAYOFFLRefundAmnt));

                report_value.put("TotalRefundWECHATOFFL", Integer.toString(totalRefundWECHATOFFL));
                report_value.put("RefundAmntWECHATOFFL", String.format("%,.2f", WECHATOFFLRefundAmnt));

                report_value.put("TotalRefundWECHATHKOFFL", Integer.toString(totalRefundWECHATHKOFFL));
                report_value.put("RefundAmntWECHATHKOFFL", String.format("%,.2f", WECHATHKOFFLRefundAmnt));

                report_value.put("TotalRefundWECHATONL", Integer.toString(totalRefundWECHATONL));
                report_value.put("RefundAmntWECHATONL", String.format("%,.2f", WECHATONLRefundAmnt));

                report_value.put("TotalRefundOEPAYOFFL", Integer.toString(totalRefundOEPAYOFFL));
                report_value.put("RefundAmntOEPAYOFFL", String.format("%,.2f", OEPAYOFFLRefundAmnt));

                report_value.put("TotalRefundBOOSTOFFL", Integer.toString(totalRefundBOOSTOFFL));
                report_value.put("RefundAmntBOOSTOFFL", String.format("%,.2f", BOOSTOFFLRefundAmnt));

                report_value.put("TotalRefundVISA", Integer.toString((totalRefundVisa)));
                report_value.put("RefundAmntVISA", String.format("%,.2f", visaRefundAmnt));

                report_value.put("TotalRefundMASTER", Integer.toString((totalRefundMaster)));
                report_value.put("RefundAmntMASTER", String.format("%,.2f", masterRefundAmnt));

                printUtil = new PrintUtil(VoidReport.this, printeraddress, printername, label, report_value, ALIPAYHKOFFLrefundArray, ALIPAYCNOFFLrefundArray, ALIPAYOFFLrefundArray,
                        WECHATOFFLrefundArray, WECHATONLrefundArray, OEPAYOFFLrefundArray, BOOSTOFFLrefundArray, visarefundArray, masterrefundArray);
                printUtil.sendRefundReport();

            }
        });
        */

        lvCommonListView.setTextFilterEnabled(true);
        lvCommonListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                progressDialog = new ProgressDialog(VoidReport.this);
                progressDialog.setCancelable(false);
                String strpageno = Integer.toString(pageNo);
                Constants.CURRENT_PAGE_NO = pageNo;
                LoadXML loadxml = new LoadXML(VoidReport.this, progressDialog, getPrefPayGate());
                loadxml.execute(date1, date2, strpageno);
            }
        });

        tvnone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                loading();
                progressDialog = new ProgressDialog(VoidReport.this);
                String strpageno = Integer.toString(pageNo);
                Constants.CURRENT_PAGE_NO = pageNo;
                LoadXML loadxml = new LoadXML(VoidReport.this, progressDialog, getPrefPayGate());
                loadxml.execute(date1, date2, strpageno);
            }
        });
        loading();
        if (!isOnline()) {

            Handler handlerTimer = new Handler();
            handlerTimer.postDelayed(new Runnable() {
                public void run() {
                    // do something
                    tapToRetry();
                }
            }, 2000);
        } else {
            getData();
        }
    }

    @Override
    public void setVoidTransactions(TreeMap<String, String> report,
                                    ArrayList<Record> ALIPAYHKOFFLrecords,
                                    ArrayList<Record> ALIPAYCNOFFLrecords,
                                    ArrayList<Record> ALIPAYOFFLrecords,
                                    ArrayList<Record> BOOSTOFFLrecords,
                                    ArrayList<Record> GCASHOFFLrecords,
                                    ArrayList<Record> masterRecords,
                                    ArrayList<Record> PROMPTPAYOFFLrecords,
                                    ArrayList<Record> UNIONPAYOFFLrecords,
                                    ArrayList<Record> visaRecords,
                                    ArrayList<Record> WECHATOFFLrecords,
                                    ArrayList<Record> WECHATHKOFFLrecords,
                                    ArrayList<Record> WECHATONLrecords) {
        ALIPAYHKOFFLTotalVoid = Integer.parseInt(report.get("ALIPAYHKOFFLTotalVoid"));
        ALIPAYHKOFFLVoidAmnt = Double.parseDouble(report.get("ALIPAYHKOFFLVoidAmt"));
        ALIPAYCNOFFLTotalVoid = Integer.parseInt(report.get("ALIPAYCNOFFLTotalVoid"));
        ALIPAYCNOFFLVoidAmnt = Double.parseDouble(report.get("ALIPAYCNOFFLVoidAmt"));
        ALIPAYOFFLTotalVoid = Integer.parseInt(report.get("ALIPAYOFFLTotalVoid"));
        ALIPAYOFFLVoidAmnt = Double.parseDouble(report.get("ALIPAYOFFLVoidAmt"));
        BOOSTOFFLTotalVoid = Integer.parseInt(report.get("BOOSTOFFLTotalVoid"));
        BOOSTOFFLVoidAmnt = Double.parseDouble(report.get("BOOSTOFFLVoidAmt"));
        GCASHOFFLTotalVoid = Integer.parseInt(report.get("GCASHOFFLTotalVoid"));
        GCASHOFFLVoidAmnt = Double.parseDouble(report.get("GCASHOFFLVoidAmt"));
        MasterTotalVoid = Integer.parseInt(report.get("MasterTotalVoid"));
        masterVoidAmnt = Double.parseDouble(report.get("MasterVoidAmt"));
        PROMPTPAYOFFLTotalVoid = Integer.parseInt(report.get("PROMPTPAYOFFLTotalVoid"));
        PROMPTPAYOFFLVoidAmnt = Double.parseDouble(report.get("PROMPTPAYOFFLVoidAmt"));
        UNIONPAYOFFLTotalVoid = Integer.parseInt(report.get("UNIONPAYOFFLTotalVoid"));
        UNIONPAYOFFLVoidAmnt = Double.parseDouble(report.get("UNIONPAYOFFLVoidAmt"));
        VisaTotalVoid = Integer.parseInt(report.get("VisaTotalVoid"));
        visaVoidAmnt = Double.parseDouble(report.get("VisaVoidAmt"));
        WECHATOFFLTotalVoid = Integer.parseInt(report.get("WECHATOFFLTotalVoid"));
        WECHATOFFLVoidAmnt = Double.parseDouble(report.get("WECHATOFFLVoidAmt"));
        WECHATHKOFFLTotalVoid = Integer.parseInt(report.get("WECHATHKOFFLTotalVoid"));
        WECHATHKOFFLVoidAmnt = Double.parseDouble(report.get("WECHATHKOFFLVoidAmt"));
        WECHATONLTotalVoid = Integer.parseInt(report.get("WECHATONLTotalVoid"));
        WECHATONLVoidAmnt = Double.parseDouble(report.get("WECHATONLVoidAmt"));

        ALIPAYHKOFFLvoidArray = ALIPAYHKOFFLrecords;
        ALIPAYCNOFFLvoidArray = ALIPAYCNOFFLrecords;
        ALIPAYOFFLvoidArray = ALIPAYOFFLrecords;
        BOOSTOFFLvoidArray = BOOSTOFFLrecords;
        GCASHOFFLvoidArray = GCASHOFFLrecords;
        mastervoidArray = masterRecords;
        PROMPTPAYOFFLvoidArray = PROMPTPAYOFFLrecords;
        UNIONPAYOFFLvoidArray = UNIONPAYOFFLrecords;
        visavoidArray = visaRecords;
        WECHATOFFLvoidArray = WECHATOFFLrecords;
        WECHATHKOFFLvoidArray = WECHATHKOFFLrecords;
        WECHATONLvoidArray = WECHATONLrecords;
    }


    public String getPrefPayGate() {
        SharedPreferences prefsettings = getApplicationContext().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        String prefpaygate = prefsettings.getString(Constants.pref_PayGate, Constants.default_paygate);
        return prefpaygate;
    }

    public void tapToRetry() {
        loadingPanel.setVisibility(View.GONE);
        tvnone.setVisibility(TextView.VISIBLE);
        tvnone.setText(R.string.tap_to_retry);
        tvnone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stubs

                Intent intent = getIntent();
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void getData() {
        // TODO Auto-generated method stub
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        String strpageno = Integer.toString(pageNo);
        LoadXML loadxml = new LoadXML(VoidReport.this, progressDialog, getPrefPayGate());
        loadxml.execute(date1, date2, strpageno);
    }

    public void loadData(String merName, ArrayList<Record> record_data2) {
        resetRes();
        voidFooter.setVisibility(RelativeLayout.GONE);
        adapter = new VoidReportAdapter(this, R.layout.voidreport_item, record_data2, merName, this);
        lvCommonListView.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            VoidReport.this.finish();
            //            TabGroupActivity parentActivity = (TabGroupActivity)getParent();
            //            parentActivity.onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void getApiAccount() {
        //GET API FROM DB
        Intent his_list = getIntent();
        merName = his_list.getStringExtra(Constants.MERNAME);
        //			String user="";
        //			String password="";
        DatabaseHelper db = new DatabaseHelper(VoidReport.this);
        String orgMerchantId = merchantId;
        String encMerchantId = "";
        DesEncrypter encrypt;
        try {
            encrypt = new DesEncrypter(merName);
            encMerchantId = encrypt.encrypt(orgMerchantId);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        String whereargs[] = {encMerchantId};
        String dbuser = db.getSingleData(Constants.DB_TABLE_ID, Constants.DB_API_ID, Constants.DB_MERCHANT_ID, whereargs);
        String dbpassword = db.getSingleData(Constants.DB_TABLE_ID, Constants.DB_API_PW, Constants.DB_MERCHANT_ID, whereargs);
        String dbuserid = db.getSingleData(Constants.DB_TABLE_ID, Constants.DB_USER_ID, Constants.DB_MERCHANT_ID, whereargs);
        db.close();
        if (dbuser != null) {
            try {
                DesEncrypter encrypter = new DesEncrypter(merName);
                user = encrypter.decrypt(dbuser);
                password = encrypter.decrypt(dbpassword);
                userID = encrypter.decrypt(dbuserid);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(VoidReport.this, R.string.apiID_pw_notset, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private class LoadXML extends AsyncTask<String, Void, ArrayList<Record>> {
        private ProgressDialog progressDialog;
        private VoidReport activity;
        private String payGate;

        private String baseUrl = null;
        private String result = "";
        private ArrayList<Record> record_data = new ArrayList<Record>();

        public LoadXML(VoidReport activity, ProgressDialog progressDialog, String payGate) {
            this.activity = activity;
            this.progressDialog = progressDialog;
            this.payGate = payGate;
        }


        @Override
        protected ArrayList<Record> doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String d1 = arg0[0].replace("/", "") + "000000";
            String d2 = arg0[1].replace("/", "") + "235959";

            //getAPIAct

            NameValuePair merchantIdNVPair = new BasicNameValuePair("merchantId", merchantId);
            NameValuePair loginIdNVPair = new BasicNameValuePair("loginId", user);
            NameValuePair passwordNVPair = new BasicNameValuePair("password", password);
            NameValuePair startDateNVPair = new BasicNameValuePair("startDate", d1);
            //        NameValuePair pageNoNVPair = new BasicNameValuePair("pageNo",arg0[2]);
            //        NameValuePair pageRecordsNVPair = new BasicNameValuePair("pageRecords",String.valueOf(perPage));
            NameValuePair endDateNVPair = new BasicNameValuePair("endDate", d2);
            NameValuePair mobileNVPair = new BasicNameValuePair("enableMobile", "T");
            NameValuePair sortOrderNVpair = new BasicNameValuePair("sortOrder", "asc");
            NameValuePair recordStatusNVPair = new BasicNameValuePair("orderStatus", filterStatus);
            NameValuePair operatorIdNVPair = new BasicNameValuePair("operatorId", userID);
            //				NameValuePair channelTypeNVPair = new BasicNameValuePair("channelType","MPOS");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(merchantIdNVPair);
            nameValuePairs.add(loginIdNVPair);
            nameValuePairs.add(passwordNVPair);
            nameValuePairs.add(startDateNVPair);
            nameValuePairs.add(endDateNVPair);
            nameValuePairs.add(mobileNVPair);
            //        nameValuePairs.add(pageNoNVPair);
            //        nameValuePairs.add(pageRecordsNVPair);
            nameValuePairs.add(sortOrderNVpair);
            nameValuePairs.add(operatorIdNVPair);
            nameValuePairs.add(recordStatusNVPair);
            //		        nameValuePairs.add(channelTypeNVPair)  ;

            try {
                baseUrl = PayGate.getURL_genTxtXMLMPOS(payGate);
                URL url = new URL(baseUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                TrustModifier.relaxHostChecking(con);
                con.setReadTimeout(30000);
                con.setConnectTimeout(25000);
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(PayGate.getQuery(nameValuePairs));
                writer.flush();
                writer.close();
                os.close();
                InputStream in = con.getInputStream();
                BufferedReader reader = null;

                try {
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        result = result + line;
                    }
                    try {
                        String text = result;
                        InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
                        XML2Record xml2Record = new XML2Record();
                        Xml.parse(is, Xml.Encoding.UTF_8, xml2Record);
                        List<Record> records = xml2Record.getRecords();
                        int i = 0;
                        Record recordData[] = new Record[records.size()];
                        for (Record record : records) {
                            if (Double.parseDouble(record.getamt()) > 0.0) {
                                currency = record.currency();
                                recordData[i] = new Record(record.PayRef(), record.merref(), record.getOrderdate(), record.currency(), record.getamt(), record.getSurcharge(), record.getMerRequestAmt(), record.remark(), record.orderstatus()
                                        , merName, record.getPayType(), record.getPaymethod(), record.getPayBankId(), record.accountno(), record.getCardHolder(), record.getSettle(), record.getBankId());
                                record_data.add(recordData[i]);
                                Log.d("pMethod", record.getPaymethod());
                                i++;
                            }
                        } //end for loop

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return record_data;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(ArrayList<Record> record_data) {
            if (record_data == null) {
                activity.networkError();
            }
            if (record_data.size() != 0) {

                Collections.sort(record_data, new RecordComparator());

                activity.loadData(merName, record_data);
                lvCommonListView.onRefreshComplete();
            } else {
                if (result.toLowerCase().contains("invalid login") || result.toLowerCase().contains("invalid password")) {
                    if (result.equalsIgnoreCase("invalid login name")) {
                        result = getString(R.string.invalid_login_name);
                    } else if (result.equalsIgnoreCase("invalid password")) {
                        result = getString(R.string.invalid_password);
                    }
                    activity.noResFound(result);
                } else {
                    activity.noResFound(getString(R.string.noTxnFound));
                }
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public class RecordComparator implements Comparator<Record> {

        @Override
        public int compare(Record r1, Record r2) {
            return new CompareToBuilder()
                    .append(r1.getPaymethod(), r2.getPaymethod()).toComparison();
        }
    }

    public void networkError() {
        // TODO Auto-generated method stub
        lvCommonListView.setVisibility(ListView.GONE);
        linearResult.setVisibility(View.GONE);
        loadingPanel.setVisibility(RelativeLayout.GONE);
        tvnone.setVisibility(TextView.VISIBLE);
        tvnone.setText(R.string.network_error);
        voidFooter.setVisibility(RelativeLayout.GONE);
    }

    public void noResFound(String error) {
        // TODO Auto-generated method stub
        lvCommonListView.setVisibility(ListView.GONE);
        linearResult.setVisibility(View.GONE);
        loadingPanel.setVisibility(RelativeLayout.GONE);
        tvnone.setVisibility(TextView.VISIBLE);
        if (!error.equals("")) {
            tvnone.setText(error);
        } else {
            tvnone.setText(R.string.noTxnFound);
        }
        voidFooter.setVisibility(RelativeLayout.GONE);
    }

    public void loading() {

        loadingPanel.setVisibility(View.VISIBLE);
        linearResult.setVisibility(View.GONE);
        lvCommonListView.setVisibility(View.GONE);
        voidFooter.setVisibility(View.GONE);
        tvnone.setVisibility(View.GONE);
    }

    public void resetRes() {
        // TODO Auto-generated method stub
        linearResult.setVisibility(View.VISIBLE);
        lvCommonListView.setVisibility(ListView.VISIBLE);
        loadingPanel.setVisibility(RelativeLayout.GONE);
        tvnone.setVisibility(TextView.GONE);
        voidFooter.setVisibility(View.GONE);
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
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                pageNo = 1;
                String strpageno = Integer.toString(pageNo);
                LoadXML loadxml = new LoadXML(VoidReport.this, progressDialog, getPrefPayGate());
                loadxml.execute(date1, date2, strpageno);
            }

            if (resultCode == RESULT_CANCELED) {
            }
        }
    }
}
