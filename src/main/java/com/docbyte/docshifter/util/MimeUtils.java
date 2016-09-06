package com.docbyte.docshifter.util;


import com.senn.magic.collections.ChainableHashMap;

import java.io.File;
import java.util.*;

public class MimeUtils {

	private static final String SWF_MIME_TYPE = "application/x-shockwave-flash";
	private static final String PDF_MIME_TYPE = "application/pdf";
	private static final String PNG_MIME_TYPE = "image/png";

	private static final String UNKNOWN = "unknown";

	private static final MimeTypeMap DICOM_MIME_TYPE = new MimeTypeMap()
															   .add("dcm", "application/dicom");

	private static final MimeTypeMap XML_MIME_TYPES = new MimeTypeMap()
															  .add("xml", "application/xml");

	private static final MimeTypeMap AUDIO_MIME_TYPES = new MimeTypeMap("epa_audio")
																.add("mp3", "audio/mpeg")
																.add("ogg", "audio/ogg")
																.add("aac", "audio/aac");

	private static final MimeTypeMap VIDEO_MIME_TYPES = new MimeTypeMap("epa_video")
																.add("avi", "video/x-msvideo", "video/x-vp6-flv")
																.add("flv", "video/x-vp6-flv")
																.add("mpeg", "video/mpeg", "video/x-vp6-flv")
																.add("mpg", "video/mpeg", "video/x-vp6-flv")
																.add("wmv", "video/x-ms-wmv", "video/x-vp6-flv")
																.add("webm", "video/webm")
																.add("mp4", "video/mp4");

	private static final MimeTypeMap IMAGE_MIME_TYPES = new MimeTypeMap("epa_image")
																.add("bmp", "image/bmp", PNG_MIME_TYPE)
																//TODO: check EPA2->3 convertion						.add("dxf",	"image/vnd.dxf", SWF_MIME_TYPE)
																//TODO: check EPA2->3 convertion						.add("emf",	"image/x-emf", SWF_MIME_TYPE)
																.add("gif", "image/gif", PNG_MIME_TYPE)
																.add("jpeg", "image/jpeg", PNG_MIME_TYPE)
																.add("jpg", "image/jpeg", PNG_MIME_TYPE)
																//TODO: check EPA2->3 convertion						.add("pcd", "image/x-photo-cd", SWF_MIME_TYPE)
																//TODO: check EPA2->3 convertion						.add("pct",	"image/x-pict", SWF_MIME_TYPE)
																//TODO: check EPA2->3 convertion					.add("pgm",	"image/x-portable-graymap",	SWF_MIME_TYPE)
																.add("png", "image/png")
			//TODO: check EPA2->3 convertion						.add("tga",	"application/x-tga", SWF_MIME_TYPE)
			//TODO: check EPA2->3 convertion						.add("wmf",	"image/x-wmf", SWF_MIME_TYPE)
			;

	private static final MimeTypeMap DOCUMENT_MIME_TYPES = new MimeTypeMap("epa_pdf")
																   .add("doc", "application/msword", PDF_MIME_TYPE)
																   .add("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", PDF_MIME_TYPE)
																   .add("eps", "application/postscript", PDF_MIME_TYPE)
																   .add("html", "text/html", PDF_MIME_TYPE)
																   .add("odg", "application/vnd.oasis.opendocument.graphics", PDF_MIME_TYPE)
																   .add("odp", "application/vnd.oasis.opendocument.presentation", PDF_MIME_TYPE)
																   .add("ods", "application/vnd.oasis.opendocument.spreadsheet", PDF_MIME_TYPE)
																   .add("odt", "application/vnd.oasis.opendocument.text", PDF_MIME_TYPE)
																   .add("pdf", "application/pdf", PDF_MIME_TYPE, null)
																   .add("ppt", "application/vnd.ms-powerpoint", PDF_MIME_TYPE)
																   .add("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", PDF_MIME_TYPE)
																   .add("rtf", "text/rtf", PDF_MIME_TYPE)
																   .add("sgf", "application/pdf", PDF_MIME_TYPE) //no mimetype, but will be converted to pdf by docShifter
																   .add("sgv", "application/pdf", PDF_MIME_TYPE) //no mimetype, but will be converted to pdf by docShifter
																   .add("svm", "application/pdf", PDF_MIME_TYPE) //no mimetype, but will be converted to pdf by docShifter
																   .add("sxc", "application/vnd.sun.xml.calc", PDF_MIME_TYPE)
																   .add("sxi", "application/vnd.sun.xml.impress", PDF_MIME_TYPE)
																   .add("sxw", "application/vnd.sun.xml.writer", PDF_MIME_TYPE)
																   .add("txt", "text/plain")
																   .add("xls", "application/vnd.ms-excel", PDF_MIME_TYPE)
																   .add("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", PDF_MIME_TYPE)
																   //.add("xml", "text/xml")
																   .add("swf", SWF_MIME_TYPE, PDF_MIME_TYPE)

																   .add(UNKNOWN, "application/octet-stream", UNKNOWN);

	private static final MimeTypeMap MIME_TYPES = (MimeTypeMap) (new MimeTypeMap()
																		 .addAll(DICOM_MIME_TYPE))
																		.addAll(DOCUMENT_MIME_TYPES)
																		.addAll(IMAGE_MIME_TYPES)
																		.addAll(AUDIO_MIME_TYPES)
																		.addAll(VIDEO_MIME_TYPES)
																		.addAll(XML_MIME_TYPES);

	/**
	 * Returns the mimetype associated with a file.
	 *
	 * @param file the file to get the mimetype from. this should be a valid file-reference
	 * @return the mimetype of the file
	 */
	public static String getMimeType(File file) {
		try {
			String extension = getExtension(file);

			EPAMimeType epaMimeType = MIME_TYPES.get(extension);
			if (epaMimeType != null)
				return epaMimeType.getOriginalMimeType();
			else
				return getUnknownMimeType();
		} catch (Exception e) {
			//no mimetype whatsoever
			return getUnknownMimeType();
		}
	}

	public static String getExtention(String mime) {

		return MIME_TYPES.getExtention(mime);
	}


	public static boolean isSupportedFiletype(String path) {
		if (path != null && !path.isEmpty()) {
			try {
				String extension = getExtension(new File(path));
				return MIME_TYPES.containsKey(extension);
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Check if the given extension is supported
	 *
	 * @param extension: example: pdf
	 * @return true if filetype is supported otherwise false
	 */
	public static boolean isSupportedFiletypeExtension(String extension) {
		if (extension != null && !extension.isEmpty()) {
			try {
				return MIME_TYPES.containsKey(extension);
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static String getMimeType(String extension) {
		return MIME_TYPES.get(extension).getOriginalMimeType();
	}


	/**
	 * Returns the mimetype that is used in epaViewer for the given mimetype (of the original file).
	 *
	 * @param mimeType the mimetype the original file is in
	 * @return the mimetype of the rendition that should be get from the repository
	 */
	public static String getViewerMimeType(String mimeType) {
		for (EPAMimeType epaMimeType : MIME_TYPES.values())
			if (epaMimeType.getOriginalMimeType().equalsIgnoreCase(mimeType))
				return epaMimeType.getViewerMimeType();
		return MIME_TYPES.getUnknownMimeType().getViewerMimeType();
	}

	/**
	 * Returns the mimetype that is used in epaViewer for the given file.
	 *
	 * @param file the original file
	 * @return the mimetype of the rendition that should be get from the repository
	 */
	public static String getViewerMimeType(File file) {
		return getViewerMimeType(getMimeType(file));
	}

	public static String getRendition(String mimeType) {
		for (EPAMimeType epaMimeType : MIME_TYPES.values())
			if (epaMimeType.getOriginalMimeType().equalsIgnoreCase(mimeType))
				return epaMimeType.getRenditionRequest();
		return null;
	}

	public static List<String> getSupportedFormats() {
		return new LinkedList<String>(MIME_TYPES.keySet());
	}

	public static List<String> getSupportedDocumentFormats() {
		return new LinkedList<String>(DOCUMENT_MIME_TYPES.keySet());
	}

	public static List<String> getSupportedImageFormats() {
		return new LinkedList<String>(IMAGE_MIME_TYPES.keySet());
	}

	public static List<String> getSupportedAudioFormats() {
		return new LinkedList<String>(AUDIO_MIME_TYPES.keySet());
	}

	public static List<String> getSupportedVideoFormats() {
		return new LinkedList<String>(VIDEO_MIME_TYPES.keySet());
	}


	public static List<String> getSupportedMimeTypes() {
		List<String> output = new ArrayList<String>();
		for (EPAMimeType supportedMimeType : MIME_TYPES.values())
			output.add(supportedMimeType.getOriginalMimeType());
		return output;
	}

	public static String getUnknownMimeType() {
		return MIME_TYPES.getUnknownMimeType().getOriginalMimeType();
	}

	private static String getExtension(File file)
			throws Exception {
		String fileName = file.getName();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

		//special case: tar.gz and consorts
		if (fileName.contains(".tar."))
			extension = "tar";

		return extension;
	}


	static class MimeTypeMap extends ChainableHashMap<String, EPAMimeType> {

		private static final long serialVersionUID = -2095193638210906685L;
		private static final String UNKNOWN = "unknown";

		private String defaultRendition;

		MimeTypeMap() {
			this(null);
		}

		MimeTypeMap(String defaultRendition) {
			this.defaultRendition = defaultRendition;
			this.add(UNKNOWN, "application/octet-stream", UNKNOWN, defaultRendition);
		}

		MimeTypeMap add(String extension, String originalMimeType) {
			return this.add(extension, originalMimeType, originalMimeType, defaultRendition);
		}

		MimeTypeMap add(String extension, String originalMimeType, String viewerMimeType) {
			if (viewerMimeType == null) {
				viewerMimeType = originalMimeType;
			}
			return (MimeTypeMap) add(extension, new EPAMimeType(extension, originalMimeType, viewerMimeType, defaultRendition));
		}

		MimeTypeMap add(String extension, String originalMimeType, String viewerMimeType, String rendition) {
			if (viewerMimeType == null) {
				viewerMimeType = originalMimeType;
			}
			return (MimeTypeMap) add(extension, new EPAMimeType(extension, originalMimeType, viewerMimeType, rendition));
		}

		EPAMimeType get(String extension) {
			return super.get(extension);
			/*
			for(EPAMimeType epaMimeType : this)
				if(epaMimeType.getExtension().equalsIgnoreCase(extension))
					return epaMimeType;
			return null;
			*/
		}

		String getExtention(String mimetype) {
			Set<String> keys = this.keySet();

			for (String key : keys) {
				if (this.get(key).originalMimeType.equalsIgnoreCase(mimetype)) {
					return key;
				}
			}

			return null;
		}

		String getRendition(String mimetype) {
			Collection<EPAMimeType> mimetypes = this.values();

			for (EPAMimeType epaMimetype : mimetypes) {
				if (epaMimetype.originalMimeType.equalsIgnoreCase(mimetype)) {
					return epaMimetype.getRenditionRequest();
				}
			}

			return null;
		}

		EPAMimeType getUnknownMimeType() {
			return get(UNKNOWN);
		}
	}

	static class EPAMimeType {

		String extension;
		String originalMimeType;
		String viewerMimeType;
		String renditionRequest;

		EPAMimeType() {
		}

		EPAMimeType(String extension, String originalMimeType) {
			this(extension, originalMimeType, originalMimeType);
		}

		EPAMimeType(String extension, String originalMimeType, String viewerMimeType) {
			this(extension, originalMimeType, viewerMimeType, null);
		}

		EPAMimeType(String extension, String originalMimeType, String viewerMimeType, String renditionRequest) {
			if (viewerMimeType == null) {
				viewerMimeType = originalMimeType;
			}

			setExtension(extension);
			setOriginalMimeType(originalMimeType);
			setViewerMimeType(viewerMimeType);
			setRenditionRequest(renditionRequest);
		}

		String getExtension() {
			return extension;
		}

		void setExtension(String extension) {
			this.extension = extension;
		}

		String getOriginalMimeType() {
			return originalMimeType;
		}

		void setOriginalMimeType(String originalMimeType) {
			this.originalMimeType = originalMimeType;
		}

		String getViewerMimeType() {
			return viewerMimeType;
		}

		void setViewerMimeType(String viewerMimeType) {
			this.viewerMimeType = viewerMimeType;
		}

		String getRenditionRequest() {
			return renditionRequest;
		}

		void setRenditionRequest(String renditionRequest) {
			this.renditionRequest = renditionRequest;
		}

	}

}