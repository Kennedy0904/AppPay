package com.asiapay.paydollar.apppay.secure;

public interface PaydollarSecure {

	public String generatePaymentSecureHash(String merchantId,
                                            String merchantReferenceNumber, String currencyCode, String amount,
                                            String paymentType, String secureHashSecret)
			throws PaydollarSecureException;



	public boolean verifyPaymentDatafeed(String src, String prc, String successCode,
                                         String merchantReferenceNumber, String paydollarReferenceNumber,
                                         String currencyCode, String amount,
                                         String payerAuthenticationStatus, String secureHashSecret,
                                         String secureHash);
	
	public String loadSecureHashSecret() throws NullPointerException;

}
