package com.asiapay.paydollar.apppay.secure;

public interface PaymentSecure {
	
	public boolean verifyPaymentSecureHash(String merchantId,
                                           String merchantReferenceNumber, String currencyCode, String amount,
                                           String paymentType, String secureHashSecret,
                                           String secureHash);



	public String generatePaymentDatafeed(String src, String prc, String successCode,
                                          String merchantReferenceNumber, String payDollarReferenceNumber,
                                          String currencyCode, String amount,
                                          String payerAuthenticationStatus, String secureHashSecret)
	throws PaydollarSecureException;
	
	
	public String generateSecureHashSecret() throws PaydollarSecureException;

}
