package com.docbyte.utils.dctm;

import com.docshifter.core.utils.dctm.DctmSessionUtils;
import org.junit.Ignore;
import org.junit.Ignore;
import org.junit.Test;

public class PasswordDecryptTest {
	
	@Ignore
	@Test
	public void decryptPassTest() throws Exception {
		String pass  = "DM_ENCR_TEXT_V2=AAAAEEjpAYpiNdj6nz14Uu4bFESUyf6m0VWlc79udYtsJAbN3gFiDyTvymGzr85DhIY388y08s1ioDoYOK3xngUjeRnsZzC2i8asEpf2cei2owqI";
		String decrypted = DctmSessionUtils.getInstance().getClient().decryptText(pass , "");
		System.out.print(decrypted);
	}
}
