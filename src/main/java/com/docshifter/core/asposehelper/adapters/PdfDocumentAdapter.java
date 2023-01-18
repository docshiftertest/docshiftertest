package com.docshifter.core.asposehelper.adapters;

import com.aspose.pdf.Document;
import com.aspose.pdf.FontStyles;
import com.aspose.pdf.HorizontalAlignment;
import com.aspose.pdf.MarkupSection;
import com.aspose.pdf.Page;
import com.aspose.pdf.ParagraphAbsorber;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextSegment;
import com.aspose.pdf.XImage;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class PdfDocumentAdapter implements UnifiedDocument {
	private final Document adaptee;
	private final Double headerMargin;
	private final Double footerMargin;

	public PdfDocumentAdapter(Path path, double headerMargin, double footerMargin) {
		this(path.toString(), headerMargin, footerMargin);
	}

	public PdfDocumentAdapter(String path, double headerMargin, double footerMargin) {
		adaptee = new Document(path);
		this.headerMargin = headerMargin;
		this.footerMargin = footerMargin;
	}

	@Override
	public Stream<UnifiedPage> getPages() {
		return StreamSupport.stream(adaptee.getPages().spliterator(), false)
				.map(PageAdapter::new);
	}

	@Override
	public void close() {
		adaptee.close();
	}

	public class PageAdapter implements UnifiedPage {
		private final Page adaptee;

		public PageAdapter(Page page) {
			adaptee = page;
		}

		@Override
		public Stream<RichTextParagraph> getHeaderText() {
			ParagraphAbsorber absorber = new ParagraphAbsorber();
			absorber.visit(adaptee);
			return absorber.getPageMarkups().stream()
					.flatMap(markup -> markup.getSections().stream())
					.filter(section -> section.getRectangle().getLLY() > calculateHeaderBoundary())
					.map(RichTextParagraphAdapter::new);
		}

		@Override
		public Stream<RichTextParagraph> getFooterText() {
			ParagraphAbsorber absorber = new ParagraphAbsorber();
			absorber.visit(adaptee);
			return absorber.getPageMarkups().stream()
					.flatMap(markup -> markup.getSections().stream())
					.filter(section -> section.getRectangle().getURY() < calculateFooterBoundary())
					.map(RichTextParagraphAdapter::new);
		}

		private double calculateHeaderBoundary() {
			if (headerMargin == null) {
				// TODO
				throw new UnsupportedOperationException("Header auto-detect functionality is not supported (yet) for " +
						"PDFs.");
			}
			return adaptee.getMediaBox().getHeight() - headerMargin;
		}

		private double calculateFooterBoundary() {
			if (footerMargin == null) {
				// TODO
				throw new UnsupportedOperationException("Footer auto-detect functionality is not supported (yet) for " +
						"PDFs.");
			}
			return footerMargin;
		}

		@Override
		public Stream<RichTextParagraph> getMainText() {
			ParagraphAbsorber absorber = new ParagraphAbsorber();
			absorber.visit(adaptee);
			return absorber.getPageMarkups().stream()
					.flatMap(markup -> markup.getSections().stream())
					.filter(section -> section.getRectangle().getLLY() >= calculateFooterBoundary()
							&& section.getRectangle().getURY() <= calculateHeaderBoundary())
					.map(RichTextParagraphAdapter::new);
		}

		@Override
		public Stream<InputStream> getImages() {
			return StreamSupport.stream(adaptee.getResources().getImages().spliterator(), false)
					.map(this::imageToInputStream);
		}

		private InputStream imageToInputStream(XImage img) {
			PipedInputStream pis = new PipedInputStream();
			try {
				PipedOutputStream pos = new PipedOutputStream(pis);
				new Thread(() -> {
					try {
						img.save(pos);
					} catch (Exception ex) {
						log.error("Unable to save image to piped output stream.", ex);
					} finally {
						try {
							pos.close();
						} catch (Exception ex) {
							log.error("Ran into an exception while trying to close piped stream during saving of "
									+ "image", ex);
						}
					}
				}).start();
				return pis;
			} catch (IOException ioe) {
				throw new PdfProcessingException("Could not create a piped output stream.", ioe);
			}
		}

		@Override
		public Color getBackgroundColor() {
			// TODO convert shape method to PDF (Word + Aspose) and check for that as well?
			return adaptee.getBackground();
		}

		@Override
		public int getNumber() {
			return adaptee.getNumber();
		}
	}

	public static class RichTextParagraphAdapter extends AbstractRichTextParagraph {

		private final MarkupSection adaptee;

		public RichTextParagraphAdapter(MarkupSection section) {
			adaptee = section;
		}

		@Override
		public Alignment getHorizontalAlignment() {
			return adaptee.getFragments().stream()
					.map(TextFragment::getHorizontalAlignment)
					.findFirst()
					.map(alignment -> switch (alignment) {
						case HorizontalAlignment.Left -> Alignment.LEFT;
						case HorizontalAlignment.Center -> Alignment.CENTER;
						case HorizontalAlignment.Right -> Alignment.RIGHT;
						case HorizontalAlignment.Justify -> Alignment.JUSTIFIED;
						case HorizontalAlignment.FullJustify -> Alignment.FULLY_JUSTIFIED;
						case HorizontalAlignment.None -> Alignment.NONE;
						default -> throw new IllegalStateException("Unknown alignment flag found: "
								+ HorizontalAlignment.getName(HorizontalAlignment.class, alignment) + " (" + alignment + ")");
					}).orElse(Alignment.NONE);
		}

		@Override
		public Stream<Segment> getSegments() {
			return adaptee.getFragments().stream()
					.flatMap(tf -> StreamSupport.stream(tf.getSegments().spliterator(), false))
					.map(SegmentAdapter::new);
		}
	}

	public static class SegmentAdapter extends AbstractSegment {

		private final TextSegment adaptee;

		public SegmentAdapter(TextSegment segment) {
			adaptee = segment;
		}

		@Override
		public String getContent() {
			return adaptee.getText();
		}

		@Override
		public boolean isBold() {
			return (adaptee.getTextState().getFontStyle() & FontStyles.Bold) == FontStyles.Bold;
		}

		@Override
		public boolean isItalic() {
			return (adaptee.getTextState().getFontStyle() & FontStyles.Italic) == FontStyles.Italic;
		}

		@Override
		public boolean isUnderline() {
			// TODO: test different underline styles from Word
			return adaptee.getTextState().isUnderline();
		}

		@Override
		public boolean isStrikethrough() {
			return adaptee.getTextState().getStrikeOut();
		}

		@Override
		public boolean isInvisibleRendering() {
			return adaptee.getTextState().isInvisible();
		}

		@Override
		public Color getForegroundColor() {
			return adaptee.getTextState().getForegroundColor().toRgb();
		}

		@Override
		public Color getBackgroundColor() {
			return adaptee.getTextState().getBackgroundColor().toRgb();
		}

		@Override
		public Color getUnderlineColor() {
			// TODO: test conversion with different underline color from Word
			return getForegroundColor();
		}

		@Override
		public double getFontSize() {
			return adaptee.getTextState().getFontSize();
		}

		@Override
		public String getFontName() {
			return adaptee.getTextState().getFont().getFontName();
		}
	}
}
