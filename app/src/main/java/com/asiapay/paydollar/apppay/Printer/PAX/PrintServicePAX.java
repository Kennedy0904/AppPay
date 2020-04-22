package com.asiapay.paydollar.apppay.Printer.PAX;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.asiapay.paydollar.apppay.Constants;
import com.asiapay.paydollar.apppay.R;
import com.asiapay.paydollar.apppay.Record;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class PrintServicePAX {

    public final static int IMAGE_SIZE = 360;
    private Context context = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static OutputStream outputStream = null;
    private OutputStreamWriter mWriter = null;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnection = false;

    public PrintServicePAX(Context context, String deviceAddress) {
        super();
        this.context = context;
        Log.d("OTTO","Context:3::"+this.context+",,"+context);

        if(!"".equals(deviceAddress)) {
            this.device = this.bluetoothAdapter.getRemoteDevice(deviceAddress);
        }
    }

    /**
     * 连接蓝牙设备
     */
    public boolean connect() {
        if (!this.isConnection) {
            try {
                bluetoothSocket = this.device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                mWriter = new OutputStreamWriter(outputStream, "GBK");
                this.isConnection = true;
                if (this.bluetoothAdapter.isDiscovering()) {
                    Log.d("OTTO","关闭适配器！");
                    this.bluetoothAdapter.isDiscovering();
                }
            } catch (Exception e) {
                Log.d("OTTO","connect() 连接失败！----" + e);
                Toast.makeText(this.context, context.getString(R.string.bluetooth_connect_fail), Toast.LENGTH_SHORT).show();
                this.isConnection = false;
                return false;
            }
            Toast.makeText(this.context, this.device.getName() + context.getString(R.string.bluetooth_connect_success), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return true;
        }
    }

    public void sendPAX(final Context context, final Map<String,String> info, final Map<String,String> product, final Bitmap bitmap) {

        if (this.isConnection) {

            printPAX(context, info, product, bitmap);

            if (PrinterFunction.getInstance().getStatus().equalsIgnoreCase("Out of paper ")) {

                new AlertDialog.Builder(context)
                        .setTitle("Out of Paper")
                        .setMessage("Please insert new roll of paper")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String status = PrinterFunction.getInstance().getStatus();

                                if (status.equalsIgnoreCase("Success ")) {
                                    printPAX(context, info, product, bitmap);
                                }else{

                                    new AlertDialog.Builder(context)
                                            .setMessage(R.string.outofpaper_error)
                                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();

            } else {
                Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void printPAX(Context context, Map<String,String> info, Map<String,String> product, Bitmap bitmap){

        PrinterFunction.getInstance().init();

        PrinterFunction.getInstance().printBitmap(compressPic(bitmap));

        spaceSet("0", "10");

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_24,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);
        printCenter("***" + info.get("Title") + "***");
        printNextLine();

        printNextLine();

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_32,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        printText(info.get("MerchantName"));
        printNextLine();

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_24,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        printTwoColumn(info.get("PaymentType"), product.get("PayType"));
        printNextLine();

        printTwoColumn(info.get("PayMethod"), product.get("PayMethod"));
        printNextLine();

        printTwoColumn(info.get("PayStatus"), product.get("PayStatus"));
        printNextLine();

        printTwoColumn(info.get("Operator"), product.get("OperatorNumber"));
        printNextLine();

        printNextLine();

        printTwoColumn(info.get("TransactionDate"), product.get("TransactionDate"));
        printNextLine();

        printTwoColumn(info.get("CardNo"), product.get("CardNo"));
        printNextLine();

        printTwoColumn(info.get("MerchantRef"), product.get("MerchantRef"));
        printNextLine();

        printTwoColumn(info.get("PaymentRef"), product.get("PaymentRef"));
        printNextLine();

        printTwoColumn(info.get("TotalAmount"),  product.get("CurrCode") + " " + product.get("Amount"));
        printNextLine();

        printNextLine();

        SharedPreferences prefsettings = context.getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        String SurCalstat = (prefsettings.getString(Constants.pref_surcharge_calculation, Constants.default_surcharge_calculation));
        SharedPreferences pref_sur = context.getApplicationContext().getSharedPreferences(Constants.SETTINGS, MODE_PRIVATE);
        String mdr = pref_sur.getString(Constants.mer_mdr, "");
        if ((product.get("Surcharge") != null) || (!"".equals(product.get("Surcharge")))) {
            if ("OFF".equals(SurCalstat) || mdr == null || "".equals(mdr)) {
                Log.d("OTTO", "PrintService OFF null empty");
            } else if ("OFF".equals(SurCalstat) || Double.valueOf(mdr) <= 0) {
                Log.d("OTTO", "PrintService OFF or <0");
            } else if ("ON".equals(SurCalstat) && Double.valueOf(mdr) > 0) {
                if ("".equals(product.get("Surcharge")) || product.get("Surcharge") == null) {
                    Log.d("OTTO", "PrintService surcharge null empty");
                } else if (Double.valueOf(product.get("Surcharge")) <= 0) {
                    Log.d("OTTO", "PrintService surcharge <0");
                } else if ((Double.valueOf(product.get("Surcharge")) > 0)) {
                    Log.d("OTTO", "PrintService surcharge >0");
                    printTwoColumn(info.get("Surcharge"), product.get("CurrCode") + " " + product.get("Surcharge"));
                    printNextLine();

                    printTwoColumn(info.get("MerRequestAmt"), product.get("CurrCode") + " " + product.get("MerRequestAmt"));
                    printNextLine();
                } else {
                    Log.d("OTTO", "PrintService surcharge else");
                }
            } else {
                Log.d("OTTO", "PrintService else");
            }
        }

        printText(makelinefeed(info.get("note"), info.get("isCN")));
        printNextLine();

        printDashLine();
        printNextLine();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = date.format(new Date());

        printCenter(now);
        printNextLine();

        String copy = "";
        if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_customerCopy))) {

            copy = "=== " + context.getString(R.string.print_customerCopy) + " ===";

        } else if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_merchantCopy))) {
            copy = "=== " + context.getString(R.string.print_merchantCopy) + " ===";
        }

        printCenter(copy);
        printText("\n\n\n\n\n");

        PrinterFunction.getInstance().start();
    }


    public void sendSummaryReportPAX(final Context context, final Map<String,String> info, final Map<String,String> product) {

        if (this.isConnection) {

            printSummaryReportPAX(context, info, product);

            if (PrinterFunction.getInstance().getStatus().equalsIgnoreCase("Out of paper ")) {

                new AlertDialog.Builder(context)
                        .setTitle("Out of Paper")
                        .setMessage("Please insert new roll of paper")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String status = PrinterFunction.getInstance().getStatus();

                                if (status.equalsIgnoreCase("Success ")) {
                                    printSummaryReportPAX(context, info, product);
                                }else{
                                    new AlertDialog.Builder(context)
                                            .setMessage(R.string.outofpaper_error)
                                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();

            } else {
                Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void printSummaryReportPAX(Context context, Map<String,String> label, Map<String,String>value) {

        PrinterFunction.getInstance().init();

        spaceSet("0", "10");

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_32,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        String title = label.get("Title");
        printCenterHeader(title);
        printNextLine();

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_24,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        printCenter(value.get("MerName"));
        printNextLine();

        printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
        printNextLine();

        printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));
        printNextLine();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        printText(date.format(new Date()));
        printNextLine();
        printDashLine();

        printCenter(label.get("Header"));
        printNextLine();

        printTwoColumn(label.get("GrandTotalTrxLabel"), value.get("currCode")+" "+value.get("GrandTotalAmnt"));
        printNextLine();

        printTwoColumn(label.get("FromDateLabel"), value.get("currCode")+" "+value.get("GrandTotalRefundAmnt"));
        printNextLine();

        printTwoColumn(label.get("FromDateLabel"),value.get("currCode")+" "+value.get("GrandTotalVoidAmnt"));
        printNextLine();
        printDashLine();

        if(value.get("GrandTotalAmnt").equals("0.00") && value.get("GrandTotalRefundAmnt").equals("0.00") && value.get("GrandTotalVoidAmnt").equals("0.00")){
            printCenter(label.get("NoTxnFound"));
            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxALIPAYCNOFFL").equals("0")) {
            printCenter(label.get("ALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYCNOFFL"));

            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxALIPAYHKOFFL").equals("0")) {
            printCenter(label.get("ALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYHKOFFL"));

            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxALIPAYOFFL").equals("0")) {
            printCenter(label.get("ALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYOFFL"));

            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxWECHATOFFL").equals("0")) {
            printCenter(label.get("WECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxWECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntWECHATOFFL"));

            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxWECHATONL").equals("0")){
            printCenter(label.get("WECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode")+" "+value.get("TotalAmntWECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxWECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode")+" "+value.get("TotalRefundAmntWECHATONL"));

            printNextLine();
            printDashLine();

        }

        if(!value.get("TotalTrxVISA").equals("0")){
            printCenter(label.get("VISA"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxVISA"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode")+" "+value.get("TotalAmntVISA"));
            printNextLine();
            printTwoColumn(label.get("TotalVoidTrxLabel"), value.get("TotalRefundTrxVISA"));
            printNextLine();
            printTwoColumn(label.get("TotalVoidAmntLabel"), value.get("currCode")+" "+value.get("TotalRefundAmntVISA"));

            printNextLine();
            printDashLine();
        }

        if(!value.get("TotalTrxMASTER").equals("0")){
            printCenter(label.get("MASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxMASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode")+" "+value.get("TotalAmntMASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalVoidTrxLabel"), value.get("TotalRefundTrxMASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalVoidAmntLabel"), value.get("currCode")+" "+value.get("TotalRefundAmntMASTER"));

            printNextLine();
            printDashLine();
        }

        printNextLine();
        printCenter("==="+label.get("Footer")+"===");

        printText("\n\n\n\n\n");

        PrinterFunction.getInstance().start();

    }


    public void sendTransactionReportPAX(final Context context, final Map<String,String> label, final Map<String,String>value,
                                         final ArrayList<Record> ALIPAYHKOFFLRecordsArray, final ArrayList<Record> ALIPAYCNOFFLRecordsArray, final ArrayList<Record> ALIPAYOFFLRecordsArray,
                                         final ArrayList<Record> WECHATOFFLRecordsArray, final ArrayList<Record> WECHATONLRecordsArray, final ArrayList<Record> OEPAYOFFLRecordsArray,
                                         final ArrayList<Record> BOOSTOFFLRecordsArray, final ArrayList<Record> visaRecordsArray, final ArrayList<Record> masterRecordsArray){
        if (this.isConnection) {

            printTransactionReportPAX(context,label, value, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, OEPAYOFFLRecordsArray, BOOSTOFFLRecordsArray, visaRecordsArray, masterRecordsArray);

            if (PrinterFunction.getInstance().getStatus().equalsIgnoreCase("Out of paper ")) {

                new AlertDialog.Builder(context)
                        .setTitle("Out of Paper")
                        .setMessage("Please insert new roll of paper")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String status = PrinterFunction.getInstance().getStatus();

                                if (status.equalsIgnoreCase("Success ")) {
                                    printTransactionReportPAX(context,label, value, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                                            WECHATOFFLRecordsArray, WECHATONLRecordsArray, OEPAYOFFLRecordsArray, BOOSTOFFLRecordsArray, visaRecordsArray, masterRecordsArray);
                                }else{
                                    new AlertDialog.Builder(context)
                                            .setMessage(R.string.outofpaper_error)
                                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();

            } else {
                Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void printTransactionReportPAX(Context context, Map<String,String> label, Map<String,String>value,
                                          ArrayList<Record> ALIPAYHKOFFLRecordsArray, ArrayList<Record> ALIPAYCNOFFLRecordsArray, ArrayList<Record> ALIPAYOFFLRecordsArray,
                                          ArrayList<Record> WECHATOFFLRecordsArray, ArrayList<Record> WECHATONLRecordsArray, ArrayList<Record> OEPAYOFFLRecordsArray,
                                          ArrayList<Record> BOOSTOFFLRecordsArray, ArrayList<Record> visaRecordsArray, ArrayList<Record> masterRecordsArray)  {

        PrinterFunction.getInstance().init();

        spaceSet("0", "10");

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_32,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        String title = label.get("Title");
        printCenterHeader(title);
        printNextLine();

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_24,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        printCenter(value.get("MerName"));
        printNextLine();

        printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
        printNextLine();

        printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));
        printNextLine();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        printText(date.format(new Date()));
        printNextLine();
        printDashLine();

        if (value.get("TotalTrxALIPAYCNOFFL").equals("0") && value.get("TotalTrxALIPAYHKOFFL").equals("0") && value.get("TotalTrxALIPAYOFFL").equals("0")
                && value.get("TotalTrxMASTER").equals("0") && value.get("TotalTrxVISA").equals("0") && value.get("TotalTrxWECHATOFFL").equals("0")
                && value.get("TotalTrxWECHATONL").equals("0") && value.get("TotalTrxOEPAYOFFL").equals("0") && value.get("TotalTrxBOOSTOFFL").equals("0")) {
            printCenter(label.get("NoTxnFound"));
            printNextLine();
            printDashLine();
        }


        if (!value.get("TotalTrxALIPAYCNOFFL").equals("0")) {
            printCenter(label.get("ALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYCNOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYCNOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);
                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxALIPAYHKOFFL").equals("0")) {
            printCenter(label.get("ALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYHKOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYHKOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxALIPAYOFFL").equals("0")) {
            printCenter(label.get("ALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxMASTER").equals("0")) {
            printCenter(label.get("MASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxMASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntMASTER"));
            printNextLine();
            printDashLine();

            for(Record record: masterRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxVISA").equals("0")) {
            printCenter(label.get("VISA"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxVISA"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntVISA"));
            printNextLine();
            printDashLine();

            for (Record record : visaRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0, 8);
                String timetrx = trxDate.substring(8, 14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5, "/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " " + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxWECHATOFFL").equals("0")) {
            printCenter(label.get("WECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: WECHATOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxWECHATONL").equals("0")) {
            printCenter(label.get("WECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATONL"));
            printNextLine();
            printDashLine();

            for(Record record: WECHATONLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxOEPAYOFFL").equals("0")) {
            printCenter(label.get("OEPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxOEPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntOEPAYOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: OEPAYOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalTrxBOOSTOFFL").equals("0")) {
            printCenter(label.get("BOOSTOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxBOOSTOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntBOOSTOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: BOOSTOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        printNextLine();
        printCenter("===" + label.get("Footer") + "===");
        printText("\n\n\n\n\n");

        PrinterFunction.getInstance().start();
    }


    public void sendRefundReportPAX(final Context context, final Map<String,String> label, final Map<String,String>value,
                                         final ArrayList<Record> ALIPAYHKOFFLRecordsArray, final ArrayList<Record> ALIPAYCNOFFLRecordsArray, final ArrayList<Record> ALIPAYOFFLRecordsArray,
                                         final ArrayList<Record> WECHATOFFLRecordsArray, final ArrayList<Record> WECHATONLRecordsArray, final ArrayList<Record> visaRecordsArray, final ArrayList<Record> masterRecordsArray){
        if (this.isConnection) {

            printRefundReportPAX(context,label, value, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, visaRecordsArray, masterRecordsArray);

            if (PrinterFunction.getInstance().getStatus().equalsIgnoreCase("Out of paper ")) {

                new AlertDialog.Builder(context)
                        .setTitle("Out of Paper")
                        .setMessage("Please insert new roll of paper")
                        .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String status = PrinterFunction.getInstance().getStatus();

                                if (status.equalsIgnoreCase("Success ")) {
                                    printRefundReportPAX(context,label, value, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                                            WECHATOFFLRecordsArray, WECHATONLRecordsArray, visaRecordsArray, masterRecordsArray);
                                }else{
                                    new AlertDialog.Builder(context)
                                            .setMessage(R.string.outofpaper_error)
                                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();

            } else {
                Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void printRefundReportPAX(Context context, Map<String,String> label, Map<String,String>value,
                                     ArrayList<Record> ALIPAYHKOFFLRecordsArray, ArrayList<Record> ALIPAYCNOFFLRecordsArray, ArrayList<Record> ALIPAYOFFLRecordsArray,
                                     ArrayList<Record> WECHATOFFLRecordsArray, ArrayList<Record> WECHATONLRecordsArray, ArrayList<Record> visaRecordsArray, ArrayList<Record> masterRecordsArray)  {

        PrinterFunction.getInstance().init();

        spaceSet("0", "10");

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_32,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        String title = label.get("Title");
        printCenterHeader(title);
        printNextLine();

        PrinterFunction.getInstance().fontSet((EFontTypeAscii) EFontTypeAscii.FONT_16_24,
                (EFontTypeExtCode) EFontTypeExtCode.FONT_16_16);

        printCenter(value.get("MerName"));
        printNextLine();

        printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
        printNextLine();

        printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));
        printNextLine();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        printText(date.format(new Date()));
        printNextLine();
        printDashLine();

        if(value.get("TotalRefundALIPAYCNOFFL").equals("0") && value.get("TotalRefundALIPAYHKOFFL").equals("0") && value.get("TotalRefundALIPAYOFFL").equals("0")
                && value.get("TotalRefundMASTER").equals("0") && value.get("TotalRefundVISA").equals("0") && value.get("TotalRefundWECHATOFFL").equals("0")
                && value.get("TotalRefundWECHATONL").equals("0")){
            printCenter(label.get("NoTxnFound"));
            printNextLine();
            printDashLine();
        }

        if (!value.get("TotalRefundALIPAYCNOFFL").equals("0")) {
            printCenter(label.get("ALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYCNOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYCNOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYCNOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundALIPAYHKOFFL").equals("0")) {
            printCenter(label.get("ALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYHKOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYHKOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYHKOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundALIPAYOFFL").equals("0")) {
            printCenter(label.get("ALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: ALIPAYOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundMASTER").equals("0")) {
            printCenter(label.get("MASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundMASTER"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntMASTER"));
            printNextLine();
            printDashLine();

            for(Record record: masterRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundVISA").equals("0")) {
            printCenter(label.get("VISA"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundVISA"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntVISA"));
            printNextLine();
            printDashLine();

            for(Record record: visaRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundWECHATOFFL").equals("0")) {
            printCenter(label.get("WECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundWECHATOFFL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntWECHATOFFL"));
            printNextLine();
            printDashLine();

            for(Record record: WECHATOFFLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        if (!value.get("TotalRefundWECHATONL").equals("0")) {
            printCenter(label.get("WECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundWECHATONL"));
            printNextLine();
            printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntWECHATONL"));
            printNextLine();
            printDashLine();

            for(Record record: WECHATONLRecordsArray) {
                String trxDate = record.getOrderdate();
                String datetrx = trxDate.substring(0,8);
                String timetrx = trxDate.substring(8,14);
                StringBuilder sbdate = new StringBuilder(datetrx);
                sbdate.insert(2, "/");
                sbdate.insert(5,"/");
                StringBuilder sbtime = new StringBuilder(timetrx);
                sbtime.insert(2, ":");
                sbtime.insert(5, ":");

                String currency = record.currency();
                String amount = record.getamt();
                String merRef = record.merref();
                String paymentRef = record.PayRef();
                String qr = record.cardNo();

                printTwoColumn(label.get("TransactionDate"), sbdate.toString() + " "  + sbtime.toString());
                printTwoColumn(label.get("Amount"), currency + " " + amount);
                printTwoColumn(label.get("MerRef"), merRef);
                printTwoColumn(label.get("PaymentRef"), paymentRef);
                printTwoColumn(label.get("QRNumber"), qr);

                printDashLine();
                printNextLine();
            }
        }

        printNextLine();
        printCenter("===" + label.get("Footer") + "===");
        printText("\n\n\n\n\n");

        PrinterFunction.getInstance().start();

    }

    //---------------------------printer layout things--------------------------------//

    public void printNextLine(){
        PrinterFunction.getInstance().printStr("\n", null);
    }

    public void spaceSet(String wordSpace, String lineSpace){
        PrinterFunction.getInstance().spaceSet(Byte.parseByte(wordSpace),
                Byte.parseByte(lineSpace));
    }

    public void printDashLine(){
        PrinterFunction.getInstance().printStr("--------------------------------", null);
    }

    public void printText(String text){
        PrinterFunction.getInstance().printStr(text, null);
    }

    public void printCenter(String text){
        double dblCenter = (32 - text.length()) / 2;
        String centerFormat = "%" + dblCenter + "s" + "" + "%" + text.length() + "s" + "" + "%" + dblCenter + "s";
        PrinterFunction.getInstance().printStr(String.format(centerFormat, "", text, ""), null);
    }

    public void printCenterHeader(String text){
        double headerCenter = (24 - text.length()) / 2;
        String centerFormat = "%" + headerCenter + "s" + "" + "%" + text.length() + "s" + "" + "%" + headerCenter + "s";
        PrinterFunction.getInstance().printStr(String.format(centerFormat, "", text, ""), null);
    }

    public  void printTwoColumn(String title, String content){
        String titleFormat = "%-" + title.length() + "s";
        String contentFormat = "%" + (32 - title.length()) + "s";
        // compose the complete format information
        String columnFormat = titleFormat + "" + contentFormat;

        PrinterFunction.getInstance().printStr(String.format(columnFormat, title, content), null);
    }


    public static String makelinefeed(String s, String isCN){
        String result = "";
        if("T".equals(isCN)) {
            result = makelinefeedCN(s);
        }else {
            result = makelinefeedEN(s);
        }
        return result;
    }

    public static String makelinefeedEN(String s) {
        String result = "";

        String[] str = s.split(" ");
        int len = 0;
        String res_str = "";

        for(int i=0;i<str.length;i++) {
            if((len + str[i].length())>32) {
                res_str = res_str + "\n" + str[i] + " ";
                len = str[i].length() + 1;
            }else {
                if((len + str[i].length() + 1)>=32){
                    res_str = res_str + str[i];
                    len = len + str[i].length();
                }else{
                    res_str = res_str + str[i] + " ";
                    len = len + str[i].length() + 1;
                }
            }
        }
        result = res_str;
        result = result.replace(", ",",");
        result = result.replace(". ",".");
        return result;
    }
    public static String makelinefeedCN(String s) {
        String result = "";
        result = s;
        return result;
    }

    private Bitmap compressPic(Bitmap bitmapOrg) {
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = IMAGE_SIZE + 18;
        int newHeight = IMAGE_SIZE/2;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        return targetBmp;
    }
}


