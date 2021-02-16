package com.docshifter.core.asposehelper.utils.image;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ImageUtilsTest {
	@ParameterizedTest
	@MethodSource("colorParams")
	public void getColor_fix(String colorStr, Color expectedColor) {
		Color color = ImageUtils.getColor(colorStr);
		assertEquals(expectedColor, color);
	}

	@ParameterizedTest
	@MethodSource("nonExistingColorParams")
	public void getColor_throws(String colorStr) {
		assertThrows(NumberFormatException.class, () -> ImageUtils.getColor(colorStr));
	}

	@ParameterizedTest
	@MethodSource("nonExistingColorParams")
	public void getColor_withFallback(String colorStr) {
		Color color = ImageUtils.getColor(colorStr, Color.RED);
		assertEquals(Color.RED, color);
	}

	@ParameterizedTest
	@MethodSource("nonExistingColorParams")
	public void getColorOrBlack_fallsback(String colorStr) {
		Color color = ImageUtils.getColorOrBlack(colorStr);
		assertEquals(Color.BLACK, color);
	}

	private static Stream<Arguments> colorParams() {
		return Stream.of(
				arguments("Red", new Color(255, 0, 0)),
				arguments("red", new Color(255, 0, 0)),
				arguments("ReD", new Color(255, 0, 0)),
				arguments("RED", new Color(255, 0, 0)),
				arguments("orAngE", new Color(255, 200, 0)),
				arguments("0000ff", new Color(0, 0, 255, 255)),
				arguments("#00FF00", new Color(0, 255, 0)),
				arguments("#EE00FF00", new Color(0, 255, 0, 238)),
				arguments("#DeADbEEf", new Color(173, 190, 239,222)),
				arguments("DEAdBe", new Color(222, 173, 190,255)),
				arguments("DEADBEEF", new Color(173, 190, 239,222)),
				arguments("0xCAFEBABE", new Color(254, 186, 190,202)),
				arguments("#ffffFFFF", new Color(255, 255, 255, 255)),
				arguments("#00000000", new Color(0, 0, 0, 0))
		);
	}

	private static Stream<String> nonExistingColorParams() {
		return Stream.of(null, "", "    ", "I do not exist", "R ed", "#DEADBEEZ", "#DEADBEEF1", "#", "0x", "#000000001",
				"DEADBEEF1", "0xCAFEBABE1");
	}
}