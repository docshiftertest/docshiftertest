package com.docbyte.utils;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import com.docshifter.core.utils.ffmpeg.FfmpegUtils;

public class FfmpegUtilsTest {
	@Ignore("ffmpeg is unlikely to be on Team City, so Ignore this test for now")
	@Test
	public void basicFFmpegTest() {
		Map<String, Object> results = FfmpegUtils.executeInquiry("target/test-classes/3gp.3gp", "ffmpeg");
		for (String key : results.keySet()) {
			System.out.println(key + "=" + results.get(key).toString());
		}
	}
}
