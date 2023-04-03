package com.docshifter.core.asposehelper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Gets the version of an aspose lib both according to the version specified in the
 * pom and the actual implemented version (thanks to Pieter)
 * Mainly useful for debugging
 * @author jules
 *
 */
public class AsposeVersionUtil {

	private static final Name IMPLEMENTATION_VERSION = new Name("Implementation-Version");
	private static final Map<String, Class<?>> asposeMappings = new HashMap<>();
	static {
		asposeMappings.put("email", com.aspose.email.MailMessage.class);
		asposeMappings.put("slides", com.aspose.slides.Presentation.class);
		asposeMappings.put("cells", com.aspose.cells.Workbook.class);
		asposeMappings.put("words", com.aspose.words.Document.class);
		asposeMappings.put("pdf", com.aspose.pdf.Document.class);
		asposeMappings.put("imaging", com.aspose.imaging.Image.class);
		asposeMappings.put("tasks", com.aspose.tasks.Task.class);
		//asposeMappings.put("ocr", com.aspose.ocr.AsposeOCR.class);
		asposeMappings.put("diagram", com.aspose.diagram.Diagram.class);
		asposeMappings.put("cad", com.aspose.cad.Image.class);
		asposeMappings.put("html", com.aspose.html.HTMLDocument.class);
		asposeMappings.put("barcode", com.aspose.barcode.barcoderecognition.BarCodeReader.class);
	}

	/**
	 * Gets both the version according to the Pom.xml and the actual running version
	 * of the given aspose library
	 * @param className An aspose library name (e.g. words, pdf, slides, ...)
	 * @return String Pom version and actual version of the given aspose lib
	 */
	public static String getImplementationVersions(String className) {
		return "[" +
				className +
				"] " +
				"Pom version: " +
				getVersionFromPom(className) +
				"  Actual version: " +
				getActualVersion(className);
	}

	/**
	 * Gets the aspose library version from the Pom
	 * @param className the aspose library (e.g. words, slides, pdf, ocr...)
	 * @return String the property set in the pom 
	 *     (for e.g. ${aspose.words.version}, ${aspose.pdf.version}, ...)
	 *
	 * N.B. This depends on having the aspose_versions.properties file present, which is created
	 *      when yuo do a maven build, otherwise you get a slightly odd null pointer message and
	 *      Unknown for the pom version!
	 */
	public static String getVersionFromPom(String className) {
		return getProperty("aspose_versions.properties", "aspose." + className.toLowerCase() + ".version");
	}

	/**
	 * Gets the actual running version of an aspose library
	 * @param className An aspose library name (e.g. words, pdf, slides, ...)
	 * @return The version of the aspose library
	 */
	public static String getActualVersion(String className) {
		Class<?> clazz = getAsposeClass(className);
		return getActualVersion(clazz, className);
	}

	/**
	 * Returns a collection of all supported aspose features.
	 * @return
	 */
	public static Set<String> getSupportedFeatures() {
		return asposeMappings.keySet();
	}

	/**
	 * Convenience method to get the relevant class given an aspose
	 * library name (e.g. words, pdf, slides, ...)
	 * @param className An aspose library name (e.g. words, pdf, slides, ...)
	 * @return A Class e.g. com.aspose.email.MailMessage.class
	 */
	private static Class<?> getAsposeClass(String className) {
		String lowerClassName = className.toLowerCase();
		if (!asposeMappings.containsKey(lowerClassName)) {
			throw new IllegalArgumentException(className + " is not supported");
		}
		return asposeMappings.get(lowerClassName);
	}

	/**
	 * Gets the actual running version of an aspose library
	 * @param clazz An aspose Class
	 * @return String the version actually running or Unknown: and the text of an exception
	 */
	private static String getActualVersion(Class<?> clazz, String libName) {
		String result = "Unknown: ";
		URL urlFromDomain = clazz.getProtectionDomain().getCodeSource().getLocation();
		Attributes attributes = null;
		try (JarFile jar = new JarFile(urlFromDomain.toURI().getPath())) {
			attributes = jar.getManifest().getMainAttributes();
			result = String.valueOf(attributes.getOrDefault(IMPLEMENTATION_VERSION, "undefined"));
			if (result.equals("undefined")) {
				result = getActualVersionAlternative(jar, libName);
			}
		}
		catch (Exception exc) {
			result += exc;
		}
		return result;
	}

	private static String getActualVersionAlternative(JarFile jar, String libName) throws IOException {
		String result = "undefined";
		JarEntry entry = jar.getJarEntry("META-INF/maven/com.aspose/aspose-"
				+ libName + "/pom.properties");
		if (entry != null) {
			String content = IOUtils.toString(jar.getInputStream(entry), StandardCharsets.UTF_8);
			if (StringUtils.isNotBlank(content)) {
				Properties props = new Properties();
				props.load(new StringReader(content));
				if (props.containsKey("version")) {
					result = (String) props.get("version");
				}
			}
		}
		return result;
	}

		/**
         * Gets a property from the given property file
         * @param propertyFile the name of the property file, including .properties suffix
         * @param propertyName the property you're looking for
         * @return String either the property or Unknown: and the text of an exception
         */
	public static String getProperty(String propertyFile, String propertyName) {
		String result = "Unknown: ";
		try (InputStream is = ClassLoader.getSystemResourceAsStream(propertyFile)) {
			Properties p = new Properties();
			p.load(is);
			result = p.getProperty(propertyName);
		}
		catch (Exception exc) {
			result += exc;
		}
		return result;
	}
}
