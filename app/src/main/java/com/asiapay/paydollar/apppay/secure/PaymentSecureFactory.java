package com.asiapay.paydollar.apppay.secure;

public class PaymentSecureFactory {
	public static PaymentSecure getPaymentSecure(String secureType) {
		if ("SHA".equals(secureType))
			return new SHAPaymentSecure();
		else
			return new SHAPaymentSecure();
	}

}
