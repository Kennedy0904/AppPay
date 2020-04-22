package com.asiapay.paydollar.apppay.secure;

import java.util.Random;

public class SHAPaymentSecure implements PaymentSecure {

	private SHAAlgorithmUtil algorithmUtil;

	public SHAPaymentSecure() {
		algorithmUtil = new SHAAlgorithmUtil();
	}

	public String generatePaymentDatafeed(String src, String prc,
                                          String successCode, String merchantReferenceNumber,
                                          String paydollarReferenceNumber, String currencyCode,
                                          String amount, String payerAuthenticationStatus,
                                          String secureHashSecret) throws PaydollarSecureException {
				return secureHashSecret;

	}

	public boolean verifyPaymentSecureHash(String merchantId,
                                           String merchantReferenceNumber, String currencyCode, String amount,
                                           String paymentType, String secureHashSecret, String secureHash) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(merchantId).append("|").append(merchantReferenceNumber)
				.append("|").append(currencyCode).append("|").append(amount)
				.append("|").append(paymentType).append("|").append(
						secureHashSecret);
		try {

			String verifyData = algorithmUtil.operationAlgorithm(buffer
					.toString());
			System.out.println(buffer.toString());
			System.out.println(secureHash);
			System.out.println(verifyData);
			if (secureHash.equals(verifyData))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		} finally {
			buffer.delete(0, buffer.length());
		}
	}

	public String generateSecureHashSecret() throws PaydollarSecureException {

		Random random = new Random();

		char[] passwordChars = new char[32];
		int randInt;
		for (int i = 0; i < passwordChars.length; i++) {
			do {

				randInt = random.nextInt(122);

			} while (randInt < 48 || (57 < randInt && randInt < 65)
					|| (randInt > 90 && randInt < 97));

			passwordChars[i] = (char) randInt;

		}

		return new String(passwordChars);
		/*try {

			return algorithmUtil.operationAlgorithm(new String(passwordChars));

		} catch (NoSuchAlgorithmException e) {
			throw new PaydollarSecureException(e);
		} catch (UnsupportedEncodingException e) {
			throw new PaydollarSecureException(e);
		}*/  

	}

}
