package com.docshifter.core;

import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.docshifter.core.utils.ffmpeg.FfmpegUtils;

public class FfmpegUtilsTest {
	@Disabled("ffmpeg is unlikely to be on Team City, so Ignore this test for now")
	@Test
	public void basicFFmpegTest() {
		//
		//See the video transformation module for some real tests
		//
		//Map<String, Object> results = FfmpegUtils.executeInquiry("target/test-classes/3gp.3gp", "ffmpeg");
		//for (String key : results.keySet()) {
		//	System.out.println(key + "=" + results.get(key).toString());
		//}
	}
}
