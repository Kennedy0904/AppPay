package com.asiapay.paydollar.apppay.Printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.asiapay.paydollar.apppay.Printer.PAX.PrintServicePAX;
import com.asiapay.paydollar.apppay.Record;

import java.util.ArrayList;
import java.util.Map;

public class PrintUtil {

    private Context context = null;
    private PrintService printService = null;
    private String deviceAddress = null;
    private String deviceName = null;
    private Map<String,String> info = null;
    private Map<String,String> product = null;
    private Bitmap bitmap;
    private ArrayList<Record> ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray,  ALIPAYOFFLRecordsArray, WECHATOFFLRecordsArray, WECHATONLRecordsArray, OEPAYOFFLRecordsArray, BOOSTOFFLRecordsArray, visaRecordsArray, masterRecordsArray;
    private PrintServicePAX printServicePAX = null;
    String deviceMan = android.os.Build.MANUFACTURER;

    public PrintUtil(Context context, String deviceAddress, String deviceName, Map<String,String> info, Map<String,String> product, Bitmap bitmap) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.printService = new PrintService(this.context, this.deviceAddress);
        this.info = info;
        this.product = product;
        this.bitmap = bitmap;
        this.printServicePAX = new PrintServicePAX(this.context, this.deviceAddress);
    }

    public PrintUtil(Context context, String deviceAddress, String deviceName, Map<String,String> info, Map<String,String> product,
                     ArrayList<Record> ALIPAYHKOFFLRecordsArray, ArrayList<Record> ALIPAYCNOFFLRecordsArray, ArrayList<Record> ALIPAYOFFLRecordsArray,
                     ArrayList<Record> WECHATOFFLRecordsArray, ArrayList<Record> WECHATONLRecordsArray, ArrayList<Record> OEPAYOFFLRecordsArray, ArrayList<Record> BOOSTOFFLRecordsArray, ArrayList<Record> visaRecordsArray, ArrayList<Record> masterRecordsArray) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.printService = new PrintService(this.context, this.deviceAddress);
        this.info = info;
        this.product = product;
        this.ALIPAYHKOFFLRecordsArray = ALIPAYHKOFFLRecordsArray;
        this.ALIPAYCNOFFLRecordsArray = ALIPAYCNOFFLRecordsArray;
        this.ALIPAYOFFLRecordsArray = ALIPAYOFFLRecordsArray;
        this.WECHATOFFLRecordsArray = WECHATOFFLRecordsArray;
        this.WECHATONLRecordsArray = WECHATONLRecordsArray;
        this.OEPAYOFFLRecordsArray = OEPAYOFFLRecordsArray;
        this.BOOSTOFFLRecordsArray = BOOSTOFFLRecordsArray;
        this.visaRecordsArray = visaRecordsArray;
        this.masterRecordsArray = masterRecordsArray;
        this.printServicePAX = new PrintServicePAX(this.context, this.deviceAddress);
    }
    private void initView() {
        // 一上来就先连接蓝牙设备

        boolean flag = false;

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            flag = this.printService.connect();
        }else if(deviceMan.equalsIgnoreCase("PAX")){
            flag = this.printServicePAX.connect();
        }

        if (!flag) {
            // 连接失败
            Log.d("OTTO","连接失败");
        } else {
            // 连接成功
            Log.d("OTTO","连接成功");
        }
    }

    public void sends(){
        initView();

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            this.printService.send(context,info,product,bitmap);
        }else if(deviceMan.equalsIgnoreCase("PAX")){
            this.printServicePAX.sendPAX(context, info, product, bitmap);
        }
    }

    public void sendAlipayTH(){
        initView();

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            this.printService.sendAlipayTHreceipt(context,info,product,bitmap);
        }else if(deviceMan.equalsIgnoreCase("PAX")){
            this.printServicePAX.sendPAX(context, info, product, bitmap);
        }
    }

    //===KM LIEW summry report printing feature 30/5/2018===
    public void sendSummaryReport(){
        initView();

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            this.printService.printSummaryReport(context,info,product);
        }else {
            this.printServicePAX.sendSummaryReportPAX(context, info, product);
        }

    }
    //===================================//


    public void sendTransactionReport(){
        initView();

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            this.printService.printTransactionReport(context, info, product, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, OEPAYOFFLRecordsArray, BOOSTOFFLRecordsArray, visaRecordsArray, masterRecordsArray);
        }else {
            this.printServicePAX.sendTransactionReportPAX(context, info, product, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, OEPAYOFFLRecordsArray, BOOSTOFFLRecordsArray, visaRecordsArray, masterRecordsArray);
        }
    }

    public void sendRefundReport(){
        initView();

        if(deviceMan.equalsIgnoreCase("SUNMI")){
            this.printService.printRefundReport(context,info,product, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, visaRecordsArray, masterRecordsArray);
        }else {
            this.printServicePAX.sendRefundReportPAX(context,info,product, ALIPAYHKOFFLRecordsArray, ALIPAYCNOFFLRecordsArray, ALIPAYOFFLRecordsArray,
                    WECHATOFFLRecordsArray, WECHATONLRecordsArray, visaRecordsArray, masterRecordsArray);
        }
    }
}
