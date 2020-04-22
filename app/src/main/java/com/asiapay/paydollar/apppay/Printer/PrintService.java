package com.asiapay.paydollar.apppay.Printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.ecity.android.tinypinyin.Pinyin;
import com.asiapay.paydollar.apppay.Constants;
import com.asiapay.paydollar.apppay.R;
import com.asiapay.paydollar.apppay.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class PrintService {

    private Context context = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static OutputStream outputStream = null;
    private OutputStreamWriter mWriter = null;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnection = false;
    private final static int WIDTH_PIXEL = 384;
    public final static int IMAGE_SIZE = 360;


    public PrintService(Context context, String deviceAddress) {
        super();
        this.context = context;
        Log.d("OTTO", "Context:3::" + this.context + ",," + context);
        if (!"".equals(deviceAddress)) {
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
                    Log.d("OTTO", "关闭适配器！");
                    this.bluetoothAdapter.isDiscovering();
                }
            } catch (Exception e) {
                Log.d("OTTO", "connect() 连接失败！----" + e);
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

    /**
     * 断开蓝牙设备连接
     */
    public static void disconnect() {
        Log.d("OTTO", "断开蓝牙设备连接");
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送数据
     */
    public void send(Context context, Map<String, String> info, Map<String, String> product, Bitmap bitmap) {
        if (this.isConnection) {
            Log.d("OTTO", "开始打印:" + "\n" + info + "\n" + product);
            try {
                Log.d("OTTO", "pMethod:" + product.get("PayMethod").toString() + ",pType:" + product.get("PayStatus").toString() + ",currencyname:" + product.get("CurrCode").toString());

                printLine();
                printAlignment(1);
                printBitmap(bitmap);

                printAlignment(1);
                printText("***" + info.get("Title") + "***");
                printLine();

                printLargeText(info.get("MerchantName"));
                printLine();

                printTwoColumn(info.get("PaymentType"), product.get("PayType"));
                printLine();

                printTwoColumn(info.get("PayMethod"), product.get("PayMethod"));
                printLine();

                printTwoColumn(info.get("PayStatus"), product.get("PayStatus"));
                printLine();

                printTwoColumn(info.get("Operator"), product.get("OperatorNumber"));
                printLine();

                printLine();

                printTwoColumn(info.get("TransactionDate"), product.get("TransactionDate"));
                printLine();

                printTwoColumn(info.get("CardNo"), product.get("CardNo"));
                printLine();

                printTwoColumn(info.get("MerchantRef"), product.get("MerchantRef"));
                printLine();

                printTwoColumn(info.get("PaymentRef"), product.get("PaymentRef"));
                printLine();

                printTwoColumn(info.get("TotalAmount"), product.get("CurrCode") + " " + product.get("Amount"));

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
                            printLine();
                            printTwoColumn(info.get("Surcharge"), product.get("CurrCode") + " " + product.get("Surcharge"));
                            printLine();
                            printTwoColumn(info.get("MerRequestAmt"), product.get("CurrCode") + " " + product.get("MerRequestAmt"));
                        } else {
                            Log.d("OTTO", "PrintService surcharge else");
                        }
                    } else {
                        Log.d("OTTO", "PrintService else");
                    }
                }

                printLine();

                printLine();
                printText(makelinefeed(info.get("note"), info.get("isCN")));
                printLine();
                printDashLine();
                printLine();

                printAlignment(1);
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                printText(date.format(new Date()));
                printLine(2);

                printAlignment(1);
                if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_customerCopy))) {
                    printText("=== " + context.getString(R.string.print_customerCopy) + " ===");
                } else if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_merchantCopy))) {
                    printText("=== " + context.getString(R.string.print_merchantCopy) + " ===");
                }
                printLine(4);

            } catch (Exception e) {
                Log.d("OTTO", "开始打印:" + e);
                Toast.makeText(this.context, R.string.bluetooth_send_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendAlipayTHreceipt(Context context, Map<String, String> info, Map<String, String> product, Bitmap bitmap) {
        if (this.isConnection) {
            Log.d("OTTO", "开始打印:" + "\n" + info + "\n" + product);
            try {

                printLine();
                printAlignment(1);
                printBitmap(bitmap);

                printLargeText(info.get("MerchantName"));

                printLine();
                printDashLine();
                printLine();

                printAlignment(0);
                printText(info.get("TID") + " " + product.get("TID"));
                printLine();

                printText(info.get("merId") + " " + product.get("merId"));
                printLine();

                printTwoColumn(info.get("batchNo") + " " + product.get("batchNo"), info.get("host") + " " + product.get("host"));
                printLine();

                printTwoColumn(info.get("traceNo") + " " + product.get("traceNo"), info.get("stan") + " " + product.get("stan"));
                printLine();

                printDoubleDashLine();
                printLine();

                printText(product.get("walletCard"));
                printLine();

                printText(makelinefeed(info.get("walletCard"), info.get("isCN")));
                printLine();

                printTwoColumn(product.get("txnDate"), product.get("txnTime"));
                printLine();

                printTwoColumn(info.get("apprCode"), product.get("apprCode"));
                printLine();

                printTwoColumn(info.get("refNo"), product.get("refNo"));
                printLine();

                printLargerText(info.get("walletSale"));
                printLine();

                printTwoColumn(info.get("total"), product.get("currCode") + " " + product.get("amount"));

                printLine(3);

                printText(product.get("payMethod"));
                printLine();

                printLine();
                printAlignment(1);
                printText(makelinefeed(info.get("note"), info.get("isCN")));
                printLine();

                printAlignment(1);
                printText("***NO REFUND***");
                printLine();

                printAlignment(1);
                if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_customerCopy))) {
                    printText("--- " + context.getString(R.string.print_customerCopy) + " ---");
                } else if (info.get("copy").equalsIgnoreCase(context.getString(R.string.print_merchantCopy))) {
                    printText("--- " + context.getString(R.string.print_merchantCopy) + " ---");
                }
                printLine(4);

            } catch (Exception e) {
                Log.d("OTTO", "开始打印:" + e);
                Toast.makeText(this.context, R.string.bluetooth_send_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
        }
    }


    //===KM LIEW summry report printing feature 30/5/2018===
    public void printSummaryReport(Context context, Map<String, String> label, Map<String, String> value) {
        if (this.isConnection) {
            Log.d("KM LIEW", "Start Printing:" + "\n" + label + "\n" + value);
            try {

                printLine();

                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                printAlignment(1);
                printLargeText(label.get("Title"));
                printLine();

                printText(value.get("MerName"));
                printLine();

                printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
                printLine();
                printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));

                printLine();
                printText(date.format(new Date()));

                printLine();
                printDashLine();
                printAlignment(1);

                printText(label.get("Header"));
                printLine();
                printTwoColumn(label.get("GrandTotalTrxLabel"), value.get("currCode") + " " + value.get("GrandTotalAmnt"));
                printLine();
                printTwoColumn(label.get("GrandTotalRefundLabel"), value.get("currCode") + " " + value.get("GrandTotalRefundAmnt"));
                printLine();
                printTwoColumn(label.get("GrandTotalVoidLabel"), value.get("currCode") + " " + value.get("GrandTotalVoidAmnt"));

                printLine();
                printDashLine();
                printAlignment(1);

                if (value.get("GrandTotalAmnt").equals("0.00") && value.get("GrandTotalRefundAmnt").equals("0.00") && value.get("GrandTotalVoidAmnt").equals("0.00")) {
                    printText(label.get("NoTxnFound"));
                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxALIPAYCNOFFL").equals("0")) {
                    printText(label.get("ALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYCNOFFL"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxALIPAYHKOFFL").equals("0")) {
                    printText(label.get("ALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYHKOFFL"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxALIPAYOFFL").equals("0")) {
                    printText(label.get("ALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntALIPAYOFFL"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxWECHATOFFL").equals("0")) {
                    printText(label.get("WECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxWECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntWECHATOFFL"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxWECHATONL").equals("0")) {
                    printText(label.get("WECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundTrxLabel"), value.get("TotalRefundTrxWECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalRefundAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntWECHATONL"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxVISA").equals("0")) {
                    printText(label.get("VISA"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxVISA"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntVISA"));
                    printLine();
                    printTwoColumn(label.get("TotalVoidTrxLabel"), value.get("TotalRefundTrxVISA"));
                    printLine();
                    printTwoColumn(label.get("TotalVoidAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntVISA"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                if (!value.get("TotalTrxMASTER").equals("0")) {
                    printText(label.get("MASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxMASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntMASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalVoidTrxLabel"), value.get("TotalRefundTrxMASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalVoidAmntLabel"), value.get("currCode") + " " + value.get("TotalRefundAmntMASTER"));

                    printLine();
                    printDashLine();
                    printAlignment(1);
                }

                printLine();
                printAlignment(1);
                printText("===" + label.get("Footer") + "===");
                printLine(4);
            } catch (Exception e) {
                Log.d("KM LIEW", "Start Printing:" + e);
                Toast.makeText(this.context, R.string.bluetooth_send_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();
        }
    }


    public void printTransactionReport(Context context, Map<String, String> label, Map<String, String> value,
                                       ArrayList<Record> ALIPAYHKOFFLRecordsArray, ArrayList<Record> ALIPAYCNOFFLRecordsArray, ArrayList<Record> ALIPAYOFFLRecordsArray,
                                       ArrayList<Record> WECHATOFFLRecordsArray, ArrayList<Record> WECHATONLRecordsArray, ArrayList<Record> OEPAYOFFLRecordsArray,
                                       ArrayList<Record> BOOSTOFFLRecordsArray, ArrayList<Record> visaRecordsArray, ArrayList<Record> masterRecordsArray) {
        if (this.isConnection) {

            Log.d("KM LIEW", "Start Printing:" + "\n" + label + "\n" + value);
            try {
                printLine();

                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                printAlignment(1);
                printLargeText(label.get("Title"));
                printLine();

                printText(value.get("MerName"));
                printLine();

                printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
                printLine();
                printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));

                printLine();
                printText(date.format(new Date()));

                printLine();
                printDashLine();


                if (value.get("TotalTrxALIPAYCNOFFL").equals("0") && value.get("TotalTrxALIPAYHKOFFL").equals("0") && value.get("TotalTrxALIPAYOFFL").equals("0")
                        && value.get("TotalTrxMASTER").equals("0") && value.get("TotalTrxVISA").equals("0") && value.get("TotalTrxWECHATOFFL").equals("0")
                        && value.get("TotalTrxWECHATONL").equals("0") && value.get("TotalTrxOEPAYOFFL").equals("0") && value.get("TotalTrxBOOSTOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("NoTxnFound"));
                    printLine();
                    printDashLine();
                }


                if (!value.get("TotalTrxALIPAYCNOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYCNOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYCNOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxALIPAYHKOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYHKOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYHKOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxALIPAYOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntALIPAYOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxMASTER").equals("0")) {
                    printAlignment(1);
                    printText(label.get("MASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxMASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntMASTER"));
                    printLine();
                    printDashLine();

                    for (Record record : masterRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxVISA").equals("0")) {
                    printAlignment(1);
                    printText(label.get("VISA"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxVISA"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntVISA"));
                    printLine();
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxWECHATOFFL").equals("0")) {

                    printAlignment(1);
                    printText(label.get("WECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : WECHATOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxWECHATONL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("WECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxWECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntWECHATONL"));
                    printLine();
                    printDashLine();

                    for (Record record : WECHATONLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxOEPAYOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("OEPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxOEPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntOEPAYOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : OEPAYOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalTrxBOOSTOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("BOOSTOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalTrxBOOSTOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("TotalAmntBOOSTOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : BOOSTOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                printLine();
                printAlignment(1);
                printText("===" + label.get("Footer") + "===");
                printLine(4);
            } catch (Exception e) {
                Log.d("KM LIEW", "Start Printing:" + e);
                Toast.makeText(this.context, R.string.bluetooth_send_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();

        }
    }


    public void printRefundReport(Context context, Map<String, String> label, Map<String, String> value,
                                  ArrayList<Record> ALIPAYHKOFFLRecordsArray, ArrayList<Record> ALIPAYCNOFFLRecordsArray, ArrayList<Record> ALIPAYOFFLRecordsArray,
                                  ArrayList<Record> WECHATOFFLRecordsArray, ArrayList<Record> WECHATONLRecordsArray, ArrayList<Record> visaRecordsArray, ArrayList<Record> masterRecordsArray) {
        if (this.isConnection) {
            Log.d("KM LIEW", "Start Printing:" + "\n" + label + "\n" + value);
            try {

                printLine();
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                printAlignment(1);
                printLargeText(label.get("Title"));
                printLine();

                printText(value.get("MerName"));
                printLine();

                printTwoColumn(label.get("FromDateLabel"), value.get("From_date"));
                printLine();
                printTwoColumn(label.get("ToDateLabel"), value.get("To_date"));

                printLine();
                printText(date.format(new Date()));

                printLine();
                printDashLine();


                if(value.get("TotalRefundALIPAYCNOFFL").equals("0") && value.get("TotalRefundALIPAYHKOFFL").equals("0") && value.get("TotalRefundALIPAYOFFL").equals("0")
                        && value.get("TotalRefundMASTER").equals("0") && value.get("TotalRefundVISA").equals("0") && value.get("TotalRefundWECHATOFFL").equals("0")
                        && value.get("TotalRefundWECHATONL").equals("0")){
                    printAlignment(1);
                    printText(label.get("NoTxnFound"));
                    printLine();
                    printDashLine();
                }

                if (!value.get("TotalRefundALIPAYCNOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYCNOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYCNOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYCNOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundALIPAYHKOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYHKOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYHKOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYHKOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundALIPAYOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("ALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundALIPAYOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntALIPAYOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : ALIPAYOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundMASTER").equals("0")) {
                    printAlignment(1);
                    printText(label.get("MASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundMASTER"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntMASTER"));
                    printLine();
                    printDashLine();

                    for (Record record : masterRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundVISA").equals("0")) {
                    printAlignment(1);
                    printText(label.get("VISA"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundVISA"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntVISA"));
                    printLine();
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundWECHATOFFL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("WECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundWECHATOFFL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntWECHATOFFL"));
                    printLine();
                    printDashLine();

                    for (Record record : WECHATOFFLRecordsArray) {
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
                        printLine();
                    }
                }

                if (!value.get("TotalRefundWECHATONL").equals("0")) {
                    printAlignment(1);
                    printText(label.get("WECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalTrxLabel"), value.get("TotalRefundWECHATONL"));
                    printLine();
                    printTwoColumn(label.get("TotalAmntLabel"), value.get("currCode") + " " + value.get("RefundAmntWECHATONL"));
                    printLine();
                    printDashLine();

                    for (Record record : WECHATONLRecordsArray) {
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
                        printLine();
                    }
                }
                printLine();
                printAlignment(1);
                printText("===" + label.get("Footer") + "===");
                printLine(4);
            } catch (Exception e) {
                Log.d("KM LIEW", "Start Printing:" + e);
                Toast.makeText(this.context, R.string.bluetooth_send_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.context, R.string.bluetooth_connect_fail, Toast.LENGTH_SHORT).show();

        }
    }


    //--------------打印排版-----------------

    /**
     * return length 需要打印的空行数
     */
    public static String makelinefeed(String s, String isCN) {
        String result = "";
        if ("T".equals(isCN)) {
            result = makelinefeedCN(s);
        } else {
            result = makelinefeedEN(s);
        }
        return result;
    }

    public static String makelinefeedEN(String s) {
        String result = "";

        String[] str = s.split(" ");
        int len = 0;
        String res_str = "";

        for (int i = 0; i < str.length; i++) {
            if ((len + str[i].length()) > 32) {
                res_str = res_str + "\n" + str[i] + " ";
                len = str[i].length() + 1;
            } else {
                if ((len + str[i].length() + 1) >= 32) {
                    res_str = res_str + str[i];
                    len = len + str[i].length();
                } else {
                    res_str = res_str + str[i] + " ";
                    len = len + str[i].length() + 1;
                }
            }
        }
        result = res_str;
        result = result.replace(", ", ",");
        result = result.replace(". ", ".");
        return result;
    }

    public static String makelinefeedCN(String s) {
        String result = "";
        result = s;
        return result;
    }

    //---------------------------printer layout things--------------------------------//
    private void printDashLine() throws IOException {
        printText("--------------------------------");
    }

    private void printDoubleDashLine() throws IOException {
        printText("================================");
    }

    private void printLine(int lineNum) throws IOException {
        for (int i = 0; i < lineNum; i++) {
            mWriter.write("\n");
            mWriter.flush();
        }
    }

    private void printAlignment(int alignment) throws IOException {
        mWriter.write(0x1b);
        mWriter.write(0x61);
        mWriter.write(alignment);
    }

    private void printLine() throws IOException {
        printLine(1);
    }

    private void printText(String text) throws IOException {
        mWriter.write(text);
        mWriter.flush();
    }

    private void printLargeText(String text) throws IOException {
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(12);
        mWriter.write(text);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(0);
        mWriter.flush();
    }

    private void printLargerText(String text) throws IOException {
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(22);
        mWriter.write(text);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(0);
        mWriter.flush();
    }

    private void printTwoColumn(String title, String content) throws IOException {

        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp;
        Log.d("123123", new String(new String(byteBuffer)));
        tmp = getEncoding(title);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;
        Log.d("123123", new String(new String(byteBuffer)));
        tmp = setLocation(getOffset(content));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;
        Log.d("123123", new String(new String(byteBuffer)));
        tmp = getEncoding(content);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);

        Log.d("123123", new String(new String(byteBuffer)));
        print(byteBuffer);
    }

    private void printLargeTwoColumn(String title, String content) throws IOException {
        mWriter.write(0x1b);
        mWriter.write(0x61);
        mWriter.write(0);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(22);
        mWriter.write(title);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(0);
        mWriter.flush();
        mWriter.write(0x1b);
        mWriter.write(0x61);
        mWriter.write(2);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(22);
        mWriter.write(content);
        mWriter.write(0x1b);
        mWriter.write(0x21);
        mWriter.write(0);
        mWriter.flush();
    }

    private byte[] getEncoding(String stText) throws IOException {
        byte[] returnText = stText.getBytes("GBK"); // 必须放在try内才可以
        return returnText;
    }

    private void print(byte[] bs) throws IOException {
        outputStream.write(bs);
    }

    private byte[] setLocation(int offset) throws IOException {
        byte[] bs = new byte[4];
        bs[0] = 0x1B;
        bs[1] = 0x24;
        bs[2] = (byte) (offset % 256);
        bs[3] = (byte) (offset / 256);
        return bs;
    }

    private int getOffset(String str) {
        return WIDTH_PIXEL - getStringPixLength(str);
    }

    private int getStringPixLength(String str) {
        int pixLength = 0;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (Pinyin.isChinese(c)) {
                pixLength += 24;
            } else {
                pixLength += 12;
            }
        }
        return pixLength;
    }
    //---------------------------printer layout things--------------------------------//

    public void printBitmap(Bitmap bmp) throws IOException {
        bmp = compressPic(bmp);
        byte[] bmpByteArray = draw2PxPoint(bmp);
        printRawBytes(bmpByteArray);
    }

    public void printRawBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    /*************************************************************************
     * 假设一个360*360的图片，分辨率设为24, 共分15行打印 每一行,是一个 360 * 24 的点阵,y轴有24个点,存储在3个byte里面。
     * 即每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    private byte[] draw2PxPoint(Bitmap bmp) {
        //先设置一个足够大的size，最后在用数组拷贝复制到一个精确大小的byte数组中
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] tmp = new byte[size];
        int k = 0;
        // 设置行距为0
        tmp[k++] = 0x1B;
        tmp[k++] = 0x33;
        tmp[k++] = 0x00;
        // 居中打印
        tmp[k++] = 0x1B;
        tmp[k++] = 0x61;
        tmp[k++] = 1;
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            tmp[k++] = 0x1B;
            tmp[k++] = 0x2A;// 0x1B 2A 表示图片打印指令
            tmp[k++] = 33; // m=33时，选择24点密度打印
            tmp[k++] = (byte) (bmp.getWidth() % 256); // nL
            tmp[k++] = (byte) (bmp.getWidth() / 256); // nH
            for (int i = 0; i < bmp.getWidth(); i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        tmp[k] += tmp[k] + b;
                    }
                    k++;
                }
            }
            tmp[k++] = 10;// 换行
        }
        // 恢复默认行距
        tmp[k++] = 0x1B;
        tmp[k++] = 0x32;

        byte[] result = new byte[k];
        System.arraycopy(tmp, 0, result, 0, k);
        return result;
    }

    /**
     * 图片二值化，黑色是1，白色是0
     *
     * @param x   横坐标
     * @param y   纵坐标
     * @param bit 位图
     * @return
     */
    private byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    /**
     * 图片灰度的转化
     */
    private int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

    /**
     * 对图片进行压缩（去除透明度）
     *
     * @param bitmapOrg
     */
    private Bitmap compressPic(Bitmap bitmapOrg) {
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = IMAGE_SIZE;
        int newHeight = IMAGE_SIZE / 2;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        return targetBmp;
    }
}
