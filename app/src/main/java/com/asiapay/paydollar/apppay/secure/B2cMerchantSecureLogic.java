package com.asiapay.paydollar.apppay.secure;


public class B2cMerchantSecureLogic  {

	public boolean isSecureMerchant(int masterMerId, int merchantId) {
		return false;
	}


	public String loadExpectedSecureHash(int masterMerId,
                                         String merchantId, String amount, String orderRef, String currCode, String payType, String secureHash) {

		// ROWE ADD 2010-10-22 for MASTER Merchant ID Secure Hash Begin 
		String masterMerchantId = String.valueOf(masterMerId);
//		System.out.println("masterMerchantId=" + masterMerchantId);
		boolean isMasterMerchant = false;

		int masterMerchantIdInteger = -1;

		try {
			masterMerchantIdInteger = Integer.parseInt(masterMerchantId);

		} catch (Throwable e) {

		}

		if (masterMerchantId == null
				|| "".equalsIgnoreCase(masterMerchantId.trim())
				|| masterMerchantIdInteger < 1) {
			isMasterMerchant = false;
		} else {
			isMasterMerchant = true;
		}
//		System.out.println("This Merchant is  a Master Merchant:[" + isMasterMerchant
//				+ "]");
		if (isMasterMerchant) {
			merchantId = masterMerchantId;
//			System.out.println("Set merchantId=masterMerchantId:[" + merchantId + "]");

		}
		// ROWE ADD 2010-10-22 for MASTER Merchant ID Secure Hash END 

		int merchantIdInteger = 0;
		try {
			merchantIdInteger = Integer.parseInt(merchantId);

		} catch (Throwable e) {
//			System.out.println(e.getMessage()+ e);
//			System.out.println("merchantId[" + merchantId
//					+ "] is not a number format data!");
			return "";
		}

		if (secureHash == null) {
//			System.out.println("The merchant[" + merchantId
//					+ "] has not a secureHashSecret data!");
			return "";
		}

		StringBuffer expectedSecureHashBuffer = new StringBuffer();
		PaydollarSecure paydollarSecure = PaydollarSecureFactory
				.getPaydollarSecure("SHA");
			try {
				secureHash = paydollarSecure.generatePaymentSecureHash(
						merchantId, orderRef, currCode, amount, payType,
						secureHash);

			} catch (PaydollarSecureException e) {

			}
			return secureHash.toString();
	}



}
