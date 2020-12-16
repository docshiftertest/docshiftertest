package com.docshifter.core.asposehelper.utils.image;

import com.aspose.pdf.PageSize;
import com.aspose.pdf.Rectangle;
import com.aspose.words.ImageSize;
import com.aspose.words.PaperSize;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.segments.UnknownSegment;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ImageUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);
	
    public static final int COLOR_TYPE_RGB = 1;
    public static final int COLOR_TYPE_CMYK = 2;
    public static final int COLOR_TYPE_YCCK = 3;

    private int colorType = COLOR_TYPE_RGB;
    private boolean hasAdobeMarker = false;


	/**
	 * Scale a BufferedImage to a maximum height and width
	 * @param src A BufferedImage
	 * @param bounds A rectangle specifying the maximum height and width
	 * @return A scaled BufferedImage (getScaledImage from BufferedImage returns an Image 
	 * 			which is not so handy)
	 */
	public static BufferedImage getScaledImage(BufferedImage src, Rectangle bounds, boolean upScale){
	    return getScaledImage(src, 
	    		bounds.getWidth(), bounds.getHeight(), upScale);
	}

	/**
	 * Scale a BufferedImage to a maximum height and width
	 * @param src A BufferedImage
	 * @param bounds A rectangle specifying the maximum height and width
	 * @return A scaled BufferedImage (getScaledImage from BufferedImage returns an Image 
	 * 			which is not so handy)
	 */
	public static BufferedImage getScaledImage(BufferedImage src, ImageSize bounds, boolean upScale){
	    return getScaledImage(src, 
	    		bounds.getWidthPoints(), bounds.getHeightPoints(), upScale);
	}

	/**
	 * Scale a BufferedImage to a maximum height and width. If the width gets scaled we adjust 
	 * the height but then check whether the height is within the bounds...and may adjust 
	 * the width again (but it's only int operations, we don't actually scale until the end!)
	 * @param src A BufferedImage
	 * @param boundWidth The maximum allowed width
	 * @param boundHeight The maximum allowed height
	 * @return A scaled BufferedImage (getScaledImage from BufferedImage returns an Image 
	 * 			which is not so handy)
	 */
	public static BufferedImage getScaledImage(BufferedImage src, 
			double boundWidth, double boundHeight, boolean upScale){
	    Rectangle scaled = scaleToBounds(src.getWidth(), src.getHeight(), boundWidth, boundHeight, upScale);
	    // Don't scale unnecessarily
	    if (!upScale && src.getWidth() <= boundWidth && src.getHeight() <= boundHeight) {
	    	return src;
	    }
	    BufferedImage resizedImg = new BufferedImage((int) scaled.getWidth(), (int) scaled.getHeight(), BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setBackground(Color.WHITE);
	    g2.clearRect(0, 0, (int) scaled.getWidth(), (int) scaled.getHeight());
	    g2.drawImage(src, 0, 0, (int) scaled.getWidth(), (int) scaled.getHeight(), null);
	    g2.dispose();
	    return resizedImg;
	}
	
	public static Rectangle scaleToBounds(Rectangle original, Rectangle bounds, boolean upScale) {
	    return scaleToBounds(original.getWidth(), original.getHeight(), bounds.getWidth(), bounds.getHeight(), upScale);
	}

	public static Rectangle scaleToBounds(Rectangle original, 
			double boundWidth, double boundHeight, boolean upScale) {
	    return scaleToBounds(original.getWidth(), original.getHeight(), boundWidth, boundHeight, upScale);
	}

	public static Rectangle scaleToBounds(double originalWidth, double originalHeight, 
			Rectangle bounds, boolean upScale) {
	    return scaleToBounds(originalWidth, originalHeight, bounds.getWidth(), bounds.getHeight(), upScale);
	}

	public static Rectangle scaleToBounds(ImageSize imageSize, 
			ImageSize bounds, boolean upScale) {
	    return scaleToBounds(imageSize.getWidthPoints(), imageSize.getHeightPoints(), bounds.getWidthPoints(), bounds.getHeightPoints(), upScale);
	}

	public static Rectangle scaleToBounds(ImageSize imageSize, 
			Rectangle bounds, boolean upScale) {
	    return scaleToBounds(imageSize.getWidthPoints(), imageSize.getHeightPoints(), bounds.getWidth(), bounds.getHeight(), upScale);
	}

	public static Rectangle scaleToBounds(ImageSize imageSize, 
			double boundWidth, double boundHeight, boolean upScale) {
	    return scaleToBounds(imageSize.getWidthPoints(), imageSize.getHeightPoints(), boundWidth, boundHeight, upScale);
	}

	public static Rectangle scaleToBounds(double originalWidth, double originalHeight, 
			double boundWidth, double boundHeight, boolean upScale) {
	    double[] scaled = getScaledDims(boundWidth, boundHeight, originalWidth, originalHeight, upScale);
	    return new Rectangle(0, 0, scaled[0], scaled[1]);
	}

	/**
	 * Try to get a Color, first by name and if that doesn't work, then we
	 * presume it's a hex string 
	 * @param nm Name of a Color (yellow, black, blue...) or a hex string defining (alpha)rgb
	 * @return A java awt Color object
	 * @throws NumberFormatException
	 */
	public static Color getColor(String nm) throws NumberFormatException {
		Color result = Color.getColor(nm);
    	if (result == null) {
    		result = decodeWithAlpha(nm);
    	}
    	return result;
	}

    /**
     * 
     * @param nm RGB hex string that may also include Alpha value
     * @return A java awt Color object
     * @throws NumberFormatException
     */
	public static Color decodeWithAlpha(String nm) throws NumberFormatException {
    	Integer intval = Integer.decode(nm);
    	int i = intval.intValue();
    	// Alpha channel is the most significant
    	int alpha = (i >> 24) & 0xFF;
    	// If no alpha bits, default to 255
    	if (alpha == 0) {
    		alpha = 255;
    	}
    	logger.debug("Creating a new colour with alpha: " + alpha);
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF, alpha);
    }

    // Read jpegs with any colourspace you care to mention!
	public BufferedImage readImage(File file) throws IOException, ImageReadException {
        colorType = COLOR_TYPE_RGB;
        hasAdobeMarker = false;

        ImageInputStream stream = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            reader.setInput(stream);

            BufferedImage image;
            ICC_Profile profile = null;
            try {
                image = reader.read(0);
            } catch (IIOException e) {
                colorType = COLOR_TYPE_CMYK;
                checkAdobeMarker(file);
                profile = Imaging.getICCProfile(file);
                WritableRaster raster = (WritableRaster) reader.readRaster(0, null);
                if (colorType == COLOR_TYPE_YCCK)
                    convertYcckToCmyk(raster);
                if (hasAdobeMarker)
                    convertInvertedColors(raster);
                image = convertCmykToRgb(raster, profile);
            }

            return image;
        }

        return null;
    }

    public void checkAdobeMarker(File file) throws IOException, ImageReadException {
        JpegImageParser parser = new JpegImageParser();
        ByteSource byteSource = new ByteSourceFile(file);
        @SuppressWarnings("rawtypes")
        List segments = parser.readSegments(byteSource, new int[] { 0xffee }, true);
        if (segments != null && segments.size() >= 1) {
            UnknownSegment app14Segment = (UnknownSegment) segments.get(0);
            //byte[] data = app14Segment.bytes;
            byte[] data = app14Segment.getSegmentData();
            if (data.length >= 12 && data[0] == 'A' && data[1] == 'd' && data[2] == 'o' && data[3] == 'b' && data[4] == 'e')
            {
                hasAdobeMarker = true;
                //int transform = app14Segment.bytes[11] & 0xff;
                int transform = app14Segment.getSegmentData()[11] & 0xff;
                if (transform == 2)
                    colorType = COLOR_TYPE_YCCK;
            }
        }
    }

    public static void convertYcckToCmyk(WritableRaster raster) {
        int height = raster.getHeight();
        int width = raster.getWidth();
        int stride = width * 4;
        int[] pixelRow = new int[stride];
        for (int h = 0; h < height; h++) {
            raster.getPixels(0, h, width, 1, pixelRow);

            for (int x = 0; x < stride; x += 4) {
                int y = pixelRow[x];
                int cb = pixelRow[x + 1];
                int cr = pixelRow[x + 2];

                int c = (int) (y + 1.402 * cr - 178.956);
                int m = (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984);
                y = (int) (y + 1.772 * cb - 226.316);

                if (c < 0) c = 0; else if (c > 255) c = 255;
                if (m < 0) m = 0; else if (m > 255) m = 255;
                if (y < 0) y = 0; else if (y > 255) y = 255;

                pixelRow[x] = 255 - c;
                pixelRow[x + 1] = 255 - m;
                pixelRow[x + 2] = 255 - y;
            }

            raster.setPixels(0, h, width, 1, pixelRow);
        }
    }

    public static void convertInvertedColors(WritableRaster raster) {
        int height = raster.getHeight();
        int width = raster.getWidth();
        int stride = width * 4;
        int[] pixelRow = new int[stride];
        for (int h = 0; h < height; h++) {
            raster.getPixels(0, h, width, 1, pixelRow);
            for (int x = 0; x < stride; x++)
                pixelRow[x] = 255 - pixelRow[x];
            raster.setPixels(0, h, width, 1, pixelRow);
        }
    }

    public static BufferedImage convertCmykToRgb(Raster cmykRaster, ICC_Profile cmykProfile) throws IOException {
        if (cmykProfile == null)
            cmykProfile = ICC_Profile.getInstance(ImageUtils.class.getResourceAsStream("/ISOcoated_v2_300_eci.icc"));
        if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
            byte[] profileData = cmykProfile.getData(); // Need to clone entire profile, due to a JDK 7 bug

            if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
                intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass); // Header is first

                cmykProfile = ICC_Profile.getInstance(profileData);
            }
        }
        ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
        BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(), cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();
        ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
        ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
        cmykToRgb.filter(cmykRaster, rgbRaster);
        return rgbImage;
    }

    static void intToBigEndian(int value, byte[] array, int index) {
        array[index]   = (byte) (value >> 24);
        array[index+1] = (byte) (value >> 16);
        array[index+2] = (byte) (value >>  8);
        array[index+3] = (byte) (value);
    }

	/**
	 * Given the width and height of the page size we want the output to be, and the current
	 * width and height of the slides in the presentation, return a double[] of the new
	 * dimensions. If upscale is true, a scaling factor greater than 1 may be used 
	 * @param pageWidth Width of the page in pixels for 72DPI (e.g. for A4 Portrait it's 595) 
	 * @param pageHeight Height of the page in pixels for 72DPI (e.g. for A4 Portrait it's 842)
	 * @param slideWidth Current width of the Slides in the Presentation
	 * @param slideHeight Current height of the Slides in the Presentation
	 * @return A double[] of the new dimensions
	 */
	public static double[] getScaledDims(double pageWidth, double pageHeight, 
			double slideWidth, double slideHeight, boolean upScale) {
		double ratioW = pageWidth / slideWidth; 
		double ratioH = pageHeight / slideHeight;

		// A smaller ratio will ensure that the image fits on the page
		double ratio = ratioW < ratioH ? ratioW:ratioH;
	    if ((slideWidth*ratio - pageWidth) > 1) {
	    	logger.warn("Width (" +  slideWidth*ratio 
	    			+ ") will be too large to fit on page (" 
	    			+ pageWidth + ")");
	    }
	    if ((slideHeight*ratio - pageHeight) > 1) {
	    	logger.warn("Height (" +  slideHeight*ratio 
	    			+ ") will be too large to fit on page (" 
	    			+ pageHeight + ")");
	    }
	    // If we would be upscaling (ratio > 1) but upScale NOT requested, set ratio back to 1 (no resizing)
	    if (ratio > 1 && !upScale) {
	    	ratio = 1;
	    }
		return new double[] {slideWidth*ratio, slideHeight*ratio};
	}

	/**
	 * Convert a String representation of a Page Size to an Aspose PDF PageSize object
	 * of the correct Width and Height
	 * @param pageSize String containing e.g. A4, Letter, Legal
	 * @return An Aspose PDF PageSize object of the correct dimensions
	 */
	public static PageSize parsePageSize(String pageSize) {
		switch (pageSize.toUpperCase()) {
			case "A0":
				return PageSize.getA0();
			case "A1":
				return PageSize.getA1();
			case "A2":
				return PageSize.getA2();
			case "A3":
				return PageSize.getA3();
			case "A4":
				return PageSize.getA4();
			case "A5":
				return PageSize.getA5();
			case "A6":
				return PageSize.getA6();
			case "A7":
				return new PageSize(210, 298);
			case "A8":
				return new PageSize(147, 210);
			case "A9":
				return new PageSize(105, 147);
			case "A10":
				return new PageSize(74, 105);
			case "B0":
				return new PageSize(2834, 4008);
			case "B1":
				return new PageSize(2004, 2834);
			case "B2":
				return new PageSize(1417, 2004);
			case "B3":
				return new PageSize(1001, 1417);
			case "B4":
				return new PageSize(709, 1001);
			case "B5":
				return PageSize.getB5();
			case "B6":
				return new PageSize(354, 499);
			case "B7":
				return new PageSize(249, 354);
			case "B8":
				return new PageSize(176, 249);
			case "B9":
				return new PageSize(125, 176);
			case "B10":
				return new PageSize(88, 125);
			case "11X17":
				return PageSize.getP11x17();
			case "LEDGER":
				return PageSize.getPageLedger();
			case "LEGAL":
				return PageSize.getPageLegal();
			case "LETTER":
				return PageSize.getPageLetter();
			default:
				logger.warn("Got a bad or empty page size: [" + pageSize + "]. Defaulting to A4...");
				return PageSize.getA4();
		}
	}

	/**
	 * Convert a String representation of a Paper Size to an Aspose Words PaperSize (int)
	 * @return An Aspose Words PaperSize as an int
	 */
	public static int parsePaperSize(String paperSize) {
		if (StringUtils.isBlank(paperSize)) {
			logger.warn("Got a bad or empty paper size: [" + paperSize + "]. Defaulting to A4...");
			return PaperSize.A4;
		}
		switch (paperSize.toUpperCase()) {
			case "A3":
				return PaperSize.A3;
			case "A4":
				return PaperSize.A4;
			case "A5":
				return PaperSize.A5;
			case "B4":
				return PaperSize.B4;
			case "B5":
				return PaperSize.B5;
			case "ENVELOPE":
			case "ENVELOPE_DL":
			case "ENVELOPE DL":
			case "ENVELOPE-DL":
				return PaperSize.ENVELOPE_DL;
			case "EXECUTIVE":
				return PaperSize.EXECUTIVE;
			case "FOLIO":
				return PaperSize.FOLIO;
			case "LEDGER":
				return PaperSize.LEDGER;
			case "LEGAL":
				return PaperSize.LEGAL;
			case "LETTER":
				return PaperSize.LETTER;
			case "10X14":
				return PaperSize.PAPER_10_X_14;
			case "11X17":
				return PaperSize.PAPER_11_X_17;
			case "QUARTO":
				return PaperSize.QUARTO;
			case "STATEMENT":
				return PaperSize.STATEMENT;
			case "TABLOID":
				return PaperSize.TABLOID;
			default:
				logger.warn("Got a bad or empty paper size: [" + paperSize + "]. Defaulting to A4...");
				return PaperSize.A4;
		}
	}

	public static double[] getSelectedWidthAndHeight(String pageSize, String orientation, int imageWidth, int imageHeight) {
		double[] result = new double[] {-1, -1};
		if (StringUtils.isNotEmpty(pageSize)) {
			PageSize chosenPageSize = ImageUtils.parsePageSize(pageSize);
			boolean landscape = imageWidth > imageHeight;
			if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
				landscape = true;
			}
			else {
				landscape = false;
			}
			if (landscape) {
				// Flip the width and height as it's landscape
				result[0] = chosenPageSize.getHeight();
				result[1] = chosenPageSize.getWidth();
			}
			else {
				result[0] = chosenPageSize.getWidth();
				result[1] = chosenPageSize.getHeight();
			}
		}
		return result;
	}
}
