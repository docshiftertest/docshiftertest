package com.docshifter.core.asposehelper.utils.email;

import com.docshifter.core.asposehelper.utils.image.ImageUtils;
import com.aspose.email.*;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.*;
import com.aspose.pdf.facades.EncodingType;
import com.aspose.pdf.facades.FormattedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EmailUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailUtils.class);

	// Somewhat arbitrary limits of width and height for what constitutes a large image... 
	public static final int WIDTH_LIMIT = 1024;
	public static final int HEIGHT_LIMIT = 768;
	
	// Minimum thresholds that define whether an image should be considered thin (e.g. horizontal lines in signature)
	public static final int WIDTH_MIN = 10;
	public static final int HEIGHT_MIN = 10;

	/**
	 * Deal with images that are embedded in the mail
	 * @param mail a MailMessage object
	 * @param pdfMaster The master PDF document that we merge all found images into
	 */
	public static void handleInlineImages(MailMessage mail, Document pdfMaster) {
		mail.getAlternateViews().forEach(altV -> handleInlineImages(altV, pdfMaster));
	}

	/**
	 * Deal with images that are embedded in the mail
	 * @param altV An Alternate View of the mail on which we may have Linked Resources
	 * @param pdfMaster The master PDF document that we merge all found images into
	 */
	private static void handleInlineImages(AlternateView altV, Document pdfMaster) {
		LinkedResourceCollection lrc = altV.getLinkedResources();
		 
		lrc.forEach(lnkR -> 
			{
				String contentType = lnkR.getContentType().toString();
				if (contentType.startsWith("image/")) {
					Document pdfAttachment = convertLinkedImageResource(lnkR);
					if (pdfAttachment != null) {
						// Add the pages of the source document to the target document
						pdfMaster.getPages().add(pdfAttachment.getPages());
						pdfAttachment.close();
					}
				}
			}
		); // end forEach
	}
	
	/**
	 * Given a LinkedResource for an image, convert it to a one-page PDF
	 * @param lnkR A LinkedResource
	 * @return A PDF Document or null if the image is small enough to leave in-situ
	 */
	public static Document convertLinkedImageResource(LinkedResource lnkR) {
		Document pdfInline = new Document();
		String contentType = lnkR.getContentType().toString();
		String contentId = lnkR.getContentId();
		String[] nameAndExtension = determineImageNameAndExtension(contentId, lnkR.getHeaders(), contentType);
		// Remove any blah blah from and including ; onwards so we get a 'pure' 
		// extension (jpeg, gif, png etc.)
		String formatName = contentType.substring(contentType.indexOf("image/") + 6);
		if (formatName.indexOf(";") > -1) {
			formatName = formatName.substring(0, formatName.indexOf(";")).trim();
		}
		try {
			BufferedImage image = ImageIO.read(lnkR.getContentStream());
			if (image.getWidth() <= WIDTH_LIMIT && image.getHeight() <= HEIGHT_LIMIT) {
				return null;
			}
			// TODO: Make this more parameterisable? Current limits are Captiva PixTools limits...
			BufferedImage scaledImage = ImageUtils.getScaledImage(image, 6621, 9363, false);
			Rectangle rect = new Rectangle(0, 0, scaledImage.getWidth(), scaledImage.getHeight());
			pdfInline.getPages().add();
			// For Pages, Items start at 1
			pdfInline.getPages().get_Item(1).setPageSize(rect.getURX(), rect.getURY());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(scaledImage, formatName, bos);
			InputStream bis = new ByteArrayInputStream(bos.toByteArray());
			pdfInline.getPages().get_Item(1).addImage(bis, rect);
			// TODO: Make this more parameterisable?
			String fileName = nameAndExtension[0] + "." + nameAndExtension[1];
			Stamp stamp = createTextStamp(fileName, "yellow", "Times New Roman", "12", "0", "1", "left", "bottom");
			pdfInline.getPages().get_Item(1).addStamp(stamp);
		}
		catch (IOException ioe) {
			logger.error("Hit IOException on content ID: " + contentId 
				+ " when handling inline image with Content Type: ["
				+ contentType + "]. IOException was: " + ioe);
			ioe.printStackTrace();
		}
		catch (Exception exc) {
			logger.error("Hit Exception on content ID: " + contentId 
				+ " when handling inline image with Content Type: ["
				+ contentType + "]. Exception was: " + exc);
			exc.printStackTrace();
		}
		return pdfInline;
	}

	/**
	 * Try our best to get a reasonable name for the image, if nothing else, we'll default to 
	 * the Content ID, otherwise if there's a name on the Content Type we'll use that, 
	 * otherwise we'll try to use filename from the Content-Disposition header
	 * @param contentId Gives a reasonable default
	 * @param headers To search for the Content-Disposition
	 * @param contentType Content Type that may include a name= clause
	 * @return A String[] containing name and extension for the image
	 */
	public static String[] determineImageNameAndExtension(String contentId, HeaderCollection headers, ContentType contentType) {
		return determineImageNameAndExtension(contentId, headers, contentType.toString());
	}

	/**
	 * Try our best to get a reasonable name for the image, if nothing else, we'll default to 
	 * the Content ID, otherwise if there's a name on the Content Type we'll use that, 
	 * otherwise we'll try to use filename from the Content-Disposition header
	 * @param contentId Gives a reasonable default
	 * @param headers To search for the Content-Disposition
	 * @param contentType String indicating the Content Type that may include a name= clause
	 * @return A String[] containing name and extension for the image
	 */
	public static String[] determineImageNameAndExtension(String contentId, HeaderCollection headers, String contentType) {
		String[] result = new String[2];
		// Default the name to the ContenId
		result[0] = contentId;
		// And the extension to empty String
		result[1] = "";
		if (contentType.indexOf("name=") > -1) {
			result[0] = contentType.substring(contentType.indexOf("name=") + 5).trim();
		}
		else {
			StringBuilder foundHeader = new StringBuilder();
			headers.forEach(header -> 
				{
					if (header.startsWith("Content-Disposition") && foundHeader.length() == 0) {
						foundHeader.append(header);
					}
				}
			);
			String contentDisposition = foundHeader.toString();
			if (contentDisposition.indexOf("filename=") > -1) {
				result[0] = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
			}
		}
		while (result[0].startsWith("\"")) {
			result[0] = result[0].substring(1);
		}
		while (result[0].endsWith("\"")) {
			result[0] = result[0].substring(0, result[0].length() - 1);
		}
		int lastDotPos = result[0].lastIndexOf(".");
		if (lastDotPos > -1) {
			result[1] = result[0].substring(lastDotPos + 1);
			result[0] = result[0].substring(0, lastDotPos);
		}
		return result;
	}

	/**
	 * Creates a watermark (or Stamp) that is a piece of text
	 *     (specified by textOrPath)  with font details (name, size, color)
	 * @param textOrPath As above, either some text or path to a file
	 * @param color The color of the foreground (ink) of the font
	 * @param font The name of the font to use
	 * @param fontSize The size of font
	 * @param rotation How many degrees to rotate the text
	 * @param opacity How opaque from 0.1 to 1.0 
	 * @param xPos The position of the text along
	 *             (left or right are also valid params)
	 * @param yPos The position of the text up and down 
	 *             (top or bottom are also valid params)
	 * @return A TextStamp, nicely prepared and filled for you
	 */
	private static Stamp createTextStamp(String textOrPath, String color, String font, String fontSize, String rotation, 
			String opacity, String xPos, String yPos) {
		Stamp stamp;
		String[] lines = textOrPath.split("/n");
		Color formatColor = ImageUtils.getColor(color);
		if (formatColor == null) {
			logger.warn("Color " + color + " not found. Using the default color BLACK");
			formatColor = Color.BLACK;
		}
		FormattedText ft = new FormattedText(lines[0], formatColor, font, EncodingType.Winansi, false, Float.valueOf(fontSize));
		for (int j = 1; j < lines.length; j++) {
			ft.addNewLineText(lines[j]);
		}
		stamp = new TextStamp(ft);
		return alignStamp(stamp, rotation, opacity, xPos, yPos);
	}
	
	/**
	 * Convenience method that does the common stampy stuff of aligning and setting rotation/opacity
	 * Called from createTextStamp and createPdfOverlayStamp
	 * @param stamp The stamp we're setting on the page
	 * @param rotation Angle of rotation
	 * @param opacity How opaque this stamp should be
	 * @param xPos The along position (can also be left or right)
	 * @param yPos The up and down position (can also be top or bottom)
	 * @return The stamp, appropriately placed, rotated, opaqued...
	 */
	private static Stamp alignStamp(Stamp stamp, String rotation, String opacity, String xPos, String yPos) {
		stamp.setRotateAngle(Double.parseDouble(rotation));
		stamp.setOpacity(Double.parseDouble(opacity));
		if (!xPos.equalsIgnoreCase("left") && !xPos.equalsIgnoreCase("right")) {
			stamp.setXIndent(Double.parseDouble(xPos));
		}
		if (!yPos.equalsIgnoreCase("bottom") && !yPos.equalsIgnoreCase("top")) {
			stamp.setYIndent(Double.parseDouble(yPos));
		}
		if (xPos.equalsIgnoreCase("left")) {
			stamp.setHorizontalAlignment(HorizontalAlignment.Left);
			stamp.setXIndent(0);
		} else if (xPos.equalsIgnoreCase("right")) {
			stamp.setHorizontalAlignment(HorizontalAlignment.Right);
			stamp.setXIndent(0);
		}
		if (yPos.equalsIgnoreCase("top")) {
			stamp.setVerticalAlignment(VerticalAlignment.Top);
			stamp.setYIndent(0);
		} else if (yPos.equalsIgnoreCase("bottom")) {
			stamp.setVerticalAlignment(VerticalAlignment.Bottom);
			stamp.setYIndent(0);
		}
		return stamp;
	}
}
