package com.asiapay.paydollar.apppay.secure;

public class PaydollarSecureFactory {
	public static PaydollarSecure getPaydollarSecure(String secureType) {
		if ("SHA".equals(secureType))
			return new SHAPaydollarSecure();
		else
			return new SHAPaydollarSecure();
	}
}
