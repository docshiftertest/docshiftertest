package com.docbyte.docshifter.util;

import Aladdin.Hasp;
import Aladdin.HaspStatus;

public class LicensingUtils {
	private static Hasp hasp = null;
	private static String vendorCode = 
	"AzIceaqfA1hX5wS+M8cGnYh5ceevUnOZIzJBbXFD6dgf3tBkb9cvUF/Tkd/iKu2fsg9wAysYKw7RMAsV" + 
	"vIp4KcXle/v1RaXrLVnNBJ2H2DmrbUMOZbQUFXe698qmJsqNpLXRA367xpZ54i8kC5DTXwDhfxWTOZrB" + 
	"rh5sRKHcoVLumztIQjgWh37AzmSd1bLOfUGI0xjAL9zJWO3fRaeB0NS2KlmoKaVT5Y04zZEc06waU2r6" + 
	"AU2Dc4uipJqJmObqKM+tfNKAS0rZr5IudRiC7pUwnmtaHRe5fgSI8M7yvypvm+13Wm4Gwd4VnYiZvSxf" + 
	"8ImN3ZOG9wEzfyMIlH2+rKPUVHI+igsqla0Wd9m7ZUR9vFotj1uYV0OzG7hX0+huN2E/IdgLDjbiapj1" + 
	"e2fKHrMmGFaIvI6xzzJIQJF9GiRZ7+0jNFLKSyzX/K3JAyFrIPObfwM+y+zAgE1sWcZ1YnuBhICyRHBh" + 
	"aJDKIZL8MywrEfB2yF+R3k9wFG1oN48gSLyfrfEKuB/qgNp+BeTruWUk0AwRE9XVMUuRbjpxa4YA67SK" + 
	"unFEgFGgUfHBeHJTivvUl0u4Dki1UKAT973P+nXy2O0u239If/kRpNUVhMg8kpk7s8i6Arp7l/705/bL" + 
	"Cx4kN5hHHSXIqkiG9tHdeNV8VYo5+72hgaCx3/uVoVLmtvxbOIvo120uTJbuLVTvT8KtsOlb3DxwUrwL" + 
	"zaEMoAQAFk6Q9bNipHxfkRQER4kR7IYTMzSoW5mxh3H9O8Ge5BqVeYMEW36q9wnOYfxOLNw6yQMf8f9s" + 
	"JN4KhZty02xm707S7VEfJJ1KNq7b5pP/3RjE0IKtB2gE6vAPRvRLzEohu0m7q1aUp8wAvSiqjZy7FLaT" + 
	"tLEApXYvLvz6PEJdj4TegCZugj7c8bIOEqLXmloZ6EgVnjQ7/ttys7VFITB3mazzFiyQuKf4J6+b/a/Y";
	
	public static boolean login(long featureId) {
		hasp = new Hasp(featureId);
		boolean success = hasp.login(vendorCode);
		
		checkErrors(hasp, "Could not login to the protection key for feature " + featureId);
		
		return success;
	}
	
	public static void defaultLogin() {
		hasp = new Hasp(Hasp.HASP_DEFAULT_FID);
		hasp.login(vendorCode);

		checkErrors(hasp, "Could not login to the default protection key");
	}
	
	public static void logout() {
		if (hasp != null) {
			hasp.logout();
			checkErrors(hasp, "Could not logout");
		} else {
			Logger.error("You need to login first!", null);
		}
	}
	
	private static void checkErrors(Hasp hasp, String message) {
		int status = hasp.getLastError();
		if (status != HaspStatus.HASP_STATUS_OK) {
			Logger.error(message, null);
		}
	}
}
