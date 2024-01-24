package com.docshifter.core.dctm;

import com.docshifter.core.utils.dctm.DctmSessionUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PasswordDecryptTest {
	
	@Disabled
	@Test
	public void decryptPassTest() throws Exception {
		String pass  = "DM_ENCR_TEXT_V2=AAAAEEjpAYpiNdj6nz14Uu4bFESUyf6m0VWlc79udYtsJAbN3gFiDyTvymGzr85DhIY388y08s1ioDoYOK3xngUjeRnsZzC2i8asEpf2cei2owqI";
		String decrypted = DctmSessionUtils.getInstance().getClient().decryptText(pass , "");
		System.out.print(decrypted);
	}
}
