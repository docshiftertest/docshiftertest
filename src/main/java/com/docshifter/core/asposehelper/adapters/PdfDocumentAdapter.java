package com.docshifter.core.asposehelper.adapters;

import com.aspose.pdf.Document;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.FontStyles;
import com.aspose.pdf.HorizontalAlignment;
import com.aspose.pdf.ImagePlacement;
import com.aspose.pdf.ImagePlacementAbsorber;
import com.aspose.pdf.MarkupSection;
import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Page;
import com.aspose.pdf.ParagraphAbsorber;
import com.aspose.pdf.Point;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextSegment;
import com.aspose.pdf.XImage;
import com.docshifter.core.asposehelper.utils.pdf.ExplicitDestinationTransformer;
import com.docshifter.core.asposehelper.utils.pdf.FlatOutlineIterator;
import com.docshifter.core.asposehelper.utils.pdf.OutlineUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class PdfDocumentAdapter extends AbstractAdapter<Document> implements UnifiedDocument {
	private final Double headerMargin;
	private final Double footerMargin;
	private Map<Integer, Map<OutlineItemCollection, Point>> bookmarkCache = null;
	private final Set<Page> pagesToDelete = new HashSet<>();
	private final Set<TextFragment> fragmentsToDelete = new HashSet<>();
	private final Set<XImage> imagesToDelete = new HashSet<>();
	private final Set<OutlineItemCollection> bookmarksToDelete = new HashSet<>();
	private final Set<TextSegment> segmentsToDelete = new HashSet<>();

	public PdfDocumentAdapter(Path path, double headerMargin, double footerMargin) {
		this(path.toString(), headerMargin, footerMargin);
	}

	public PdfDocumentAdapter(String path, double headerMargin, double footerMargin) {
		super(new Document(path));
		this.headerMargin = headerMargin;
		this.footerMargin = footerMargin;
	}

	@Override
	public Type getType() {
		return Type.PDF;
	}

	@Override
	public Stream<UnifiedPage> getPages() {
		return StreamSupport.stream(adaptee.getPages().spliterator(), false)
				.map(PageAdapter::new);
	}

	@Override
	public int getPageCount() {
		return adaptee.getPages().size();
	}

	private Map<Integer, Map<OutlineItemCollection, Point>> getBookmarkCache() {
		if (bookmarkCache == null) {
			bookmarkCache =
					StreamSupport.stream(
							Spliterators.spliteratorUnknownSize(
									new FlatOutlineIterator(adaptee.getOutlines()),
									Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SORTED), false)
							.map(oic -> new AbstractMap.SimpleImmutableEntry<>(oic,OutlineUtils.extractExplicitDestinationSoft(adaptee, oic)))
							.collect(Collectors.groupingBy(entry -> entry.getValue().getPageNumber(),
									Collectors.toMap(Map.Entry::getKey, entry -> ExplicitDestinationTransformer.create(entry.getValue()).getTopLeft())));
		}
		return bookmarkCache;
	}

	@Override
	public void commitDeletes() {
		for (Iterator<Page> it = pagesToDelete.iterator(); it.hasNext();) {
			adaptee.getPages().delete(it.next().getNumber());
			it.remove();
		}
		for (Iterator<TextFragment> it = fragmentsToDelete.iterator(); it.hasNext();) {
			it.next().setText("");
			it.remove();
		}
		for (Iterator<TextSegment> it = segmentsToDelete.iterator(); it.hasNext();) {
			it.next().setText("");
			it.remove();
		}
		for (Iterator<XImage> it = imagesToDelete.iterator(); it.hasNext();) {
			it.next().delete();
			it.remove();
		}
		if (!bookmarksToDelete.isEmpty()) {
			bookmarkCache = null;
		}
		for (Iterator<OutlineItemCollection> it = bookmarksToDelete.iterator(); it.hasNext();) {
			it.next().delete();
			it.remove();
		}
	}

	@Override
	public void save(String path) {
		adaptee.save(path);
	}

	@Override
	public void close() {
		adaptee.close();
	}

	public class PageAdapter extends AbstractAdapterChild<Page, PdfDocumentAdapter> implements UnifiedPage {
		public PageAdapter(Page page) {
			super(page, PdfDocumentAdapter.this);
		}

		@Override
		public Optional<PageSection> getHeader() {
			if (headerMargin == 0) {
				return Optional.empty();
			}
			return Optional.of(new PageSectionAdapter(adaptee, this, PageSection.Type.HEADER,
					adaptee.getMediaBox().getHeight(), calculateHeaderBoundary(), false));
		}

		@Override
		public Optional<PageSection> getFooter() {
			if (footerMargin == 0) {
				return Optional.empty();
			}
			return Optional.of(new PageSectionAdapter(adaptee, this, PageSection.Type.FOOTER,
					calculateFooterBoundary(), 0, false));
		}

		private double calculateHeaderBoundary() {
			if (headerMargin == null) {
				// TODO
				throw new UnsupportedOperationException("Header auto-detect functionality is not supported (yet) for " +
						"PDFs.");
			}
			// TODO: test landscape
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
		public PageSection getBody() {
			return new PageSectionAdapter(adaptee, this, PageSection.Type.BODY, calculateHeaderBoundary(),
					calculateFooterBoundary(), true);
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

		@Override
		public void markForDeletion() {
			pagesToDelete.add(adaptee);
		}
	}

	public class PageSectionAdapter extends AbstractAdapterChild<Page, PageAdapter> implements UnifiedPage.PageSection {
		private final DoublePredicate boundsPredicate;
		private final Type type;

		public PageSectionAdapter(Page adaptee, PageAdapter parent, Type type, double upperThreshold,
								  double lowerThreshold,
								  boolean inclusiveBounds) {
			super(adaptee, parent);
			this.type = type;
			if (inclusiveBounds) {
				boundsPredicate = upperY -> upperY >= lowerThreshold && upperY <= upperThreshold;
			} else {
				boundsPredicate = upperY -> upperY > lowerThreshold && upperY < upperThreshold;
			}
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Stream<RichTextParagraph> getTextParagraphs() {
			ParagraphAbsorber absorber = new ParagraphAbsorber();
			absorber.visit(adaptee);
			return absorber.getPageMarkups().stream()
					.flatMap(markup -> markup.getSections().stream())
					.filter(section -> boundsPredicate.test(section.getRectangle().getURY()))
					.map(section -> new RichTextParagraphAdapter(section, this));
		}

		@Override
		public Stream<Image> getImages() {
			ImagePlacementAbsorber absorber = new ImagePlacementAbsorber();
			absorber.visit(adaptee);
			return StreamSupport.stream(absorber.getImagePlacements().spliterator(), false)
					.filter(placement -> boundsPredicate.test(placement.getRectangle().getURY()))
					.map(placement -> new ImageAdapter(placement, this));
		}

		@Override
		public Stream<Bookmark> getBookmarks() {
			return getBookmarkCache().getOrDefault(adaptee.getNumber(), new HashMap<>()).entrySet().stream()
					.filter(entry -> boundsPredicate.test(entry.getValue().getY()))
					.map(entry -> new BookmarkAdapter(entry.getKey(), this, entry.getValue()));
		}

		@Override
		public void markForDeletion() {
			getTextParagraphs().forEach(RichTextParagraph::markForDeletion);
			getImages().forEach(Image::markForDeletion);
			getBookmarks().forEach(Bookmark::markForDeletion);
		}

		@Override
		public UnifiedPage getPage() {
			return parent;
		}
	}

	public class ImageAdapter extends AbstractAdapterChild<ImagePlacement, PageSectionAdapter> implements Image {
		public ImageAdapter(ImagePlacement adaptee, PageSectionAdapter parent) {
			super(adaptee, parent);
		}

		@Override
		public InputStream getInputStream() {
			// Doesn't work! (InputStream does not appear to be in a valid format)
			// return adaptee.getImage().toStream();
			return imageToInputStream(adaptee.getImage());
		}

		@Override
		public double getWidth() {
			return adaptee.getRectangle().getWidth();
		}

		@Override
		public double getHeight() {
			return adaptee.getRectangle().getHeight();
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
		public void markForDeletion() {
			imagesToDelete.add(adaptee.getImage());
		}

		@Override
		public Type getType() {
			return Type.IMAGE;
		}
	}

	public class BookmarkAdapter extends AbstractAdapterChild<OutlineItemCollection, PageSectionAdapter> implements Bookmark {
		private final Point topLeftDest;

		public BookmarkAdapter(OutlineItemCollection oic, PageSectionAdapter parent, Point topLeftDest) {
			super(oic, parent);
			this.topLeftDest = topLeftDest;
		}

		@Override
		public String getTitle() {
			return adaptee.getTitle();
		}

		@Override
		public boolean pointsTo(RichTextParagraph paragraph) {
			if (paragraph instanceof RichTextParagraphAdapter paraAdapter) {
				Rectangle destination = paraAdapter.adaptee.getRectangle();
				return destination.contains(topLeftDest);
			}
			throw new IllegalStateException("Only paragraphs within the same document can be analyzed.");
		}

		@Override
		public boolean supportsStyling() {
			return true;
		}

		@Override
		public boolean isBold() {
			return adaptee.getBold();
		}

		@Override
		public void setBold(boolean bold) {
			adaptee.setBold(bold);
		}

		@Override
		public boolean isItalic() {
			return adaptee.getItalic();
		}

		@Override
		public void setItalic(boolean italic) {
			adaptee.setItalic(italic);
		}

		@Override
		public void markForDeletion() {
			bookmarksToDelete.add(adaptee);
		}

		@Override
		public Type getType() {
			return Type.BOOKMARK;
		}
	}

	public class RichTextParagraphAdapter extends AbstractRichTextParagraphAdapter<MarkupSection> {
		private final BiMap<Integer, Alignment> alignmentMap = ImmutableBiMap.<Integer, Alignment>builder()
				.put(HorizontalAlignment.Left.getValue(), Alignment.LEFT)
				.put(HorizontalAlignment.Center.getValue(), Alignment.CENTER)
				.put(HorizontalAlignment.Right.getValue(), Alignment.RIGHT)
				.put(HorizontalAlignment.Justify.getValue(), Alignment.JUSTIFIED)
				.put(HorizontalAlignment.FullJustify.getValue(), Alignment.FULLY_JUSTIFIED)
				.put(HorizontalAlignment.None.getValue(), Alignment.NONE)
				.build();

		public RichTextParagraphAdapter(MarkupSection section, PageSectionAdapter parent) {
			super(section, parent);
		}

		@Override
		public Alignment getHorizontalAlignment() {
			return adaptee.getFragments().stream()
					.map(TextFragment::getHorizontalAlignment)
					.findFirst()
					.map(alignment -> switch (alignment) {
							case Left -> Alignment.LEFT;
							case Center -> Alignment.CENTER;
							case Right -> Alignment.RIGHT;
							case Justify -> Alignment.JUSTIFIED;
							case FullJustify -> Alignment.FULLY_JUSTIFIED;
							case None -> Alignment.NONE;
							default -> throw new IllegalStateException("Unknown alignment flag found: "
									+ alignment + " (" + alignment.getValue() + ")");
					}).orElse(Alignment.NONE);
		}

		@Override
		public void setHorizontalAlignment(Alignment alignment) {
			Integer mapping = alignmentMap.inverse().get(alignment);
			if (mapping == null) {
				throw new UnsupportedOperationException("Cannot convert alignment flag " + alignment + " to one that " +
						"is supported in Word documents.");
			}
			adaptee.getFragments().forEach(tf -> tf.setHorizontalAlignment(HorizontalAlignment.valueOf(mapping)));
		}

		@Override
		public Stream<Segment> getSegments() {
			return adaptee.getFragments().stream()
					.flatMap(tf -> {
						TextSegment[] arr = new TextSegment[tf.getSegments().size()];
						tf.getSegments().copyTo(arr, 0);
						return Arrays.stream(arr)
								.map(seg -> new AbstractMap.SimpleImmutableEntry<>(tf, seg));
					})
							//StreamSupport.stream(tf.getSegments().spliterator(), false)
							//.map(seg -> new AbstractMap.SimpleImmutableEntry<>(tf, seg)))
					.map(entry -> new SegmentAdapter(entry.getValue(), this, entry.getKey()));
		}

		@Override
		public void markForDeletion() {
			fragmentsToDelete.addAll(adaptee.getFragments());
		}
	}

	public class SegmentAdapter extends AbstractSegmentAdapter<TextSegment> {
		private final TextFragment actualParent;
		public SegmentAdapter(TextSegment segment, RichTextParagraphAdapter parent, TextFragment actualParent) {
			super(segment, parent);
			this.actualParent = actualParent;
		}

		@Override
		public String getContent() {
			return adaptee.getText();
		}

		@Override
		public void setContent(String content) {
			adaptee.setText(content);
		}

		@Override
		public boolean isBold() {
			return isStyleSet(FontStyles.Bold);
		}

		@Override
		public void setBold(boolean bold) {
			setStyle(FontStyles.Bold, bold);
		}

		private boolean isStyleSet(int style) {
			return (adaptee.getTextState().getFontStyle() & style) == style;
		}

		private void setStyle(int style, boolean value) {
			int newStyle = adaptee.getTextState().getFontStyle();
			if (value) {
				newStyle |= style;
			} else {
				newStyle &= ~style;
			}
			adaptee.getTextState().setFontStyle(newStyle);
		}

		@Override
		public boolean isItalic() {
			return isStyleSet(FontStyles.Italic);
		}

		@Override
		public void setItalic(boolean italic) {
			setStyle(FontStyles.Italic, italic);
		}

		@Override
		public boolean isUnderline() {
			// TODO: test different underline styles from Word
			return adaptee.getTextState().isUnderline();
		}

		@Override
		public void setUnderline(boolean underline) {
			adaptee.getTextState().setUnderline(underline);
		}

		@Override
		public boolean isStrikethrough() {
			return adaptee.getTextState().getStrikeOut();
		}

		@Override
		public void setStrikethrough(boolean strikethrough) {
			adaptee.getTextState().setStrikeOut(true);
		}

		@Override
		public boolean isInvisibleRendering() {
			return adaptee.getTextState().isInvisible();
		}

		@Override
		public boolean isInvisibleRenderingSupported() {
			return true;
		}

		@Override
		public void setInvisibleRendering(boolean invisibleRendering) {
			adaptee.getTextState().setInvisible(invisibleRendering);
		}

		@Override
		public Color getForegroundColor() {
			return adaptee.getTextState().getForegroundColor().toRgb();
		}

		@Override
		public void setForegroundColor(Color color) {
			adaptee.getTextState().setForegroundColor(com.aspose.pdf.Color.fromRgb(color));
		}

		@Override
		public Color getBackgroundColor() {
			com.aspose.pdf.Color color = adaptee.getTextState().getBackgroundColor();
			return color == null ? null : color.toRgb();
		}

		@Override
		public void setBackgroundColor(Color color) {
			adaptee.getTextState().setBackgroundColor(com.aspose.pdf.Color.fromRgb(color));
		}

		@Override
		public Color getUnderlineColor() {
			// TODO: test conversion with different underline color from Word
			return getForegroundColor();
		}

		@Override
		public void setUnderlineColor(Color color) {
			setForegroundColor(color);
		}

		@Override
		public double getFontSize() {
			return adaptee.getTextState().getFontSize();
		}

		@Override
		public void setFontSize(double fontSize) {
			adaptee.getTextState().setFontSize((float) fontSize);
		}

		@Override
		public String getFontName() {
			return adaptee.getTextState().getFont().getFontName();
		}

		@Override
		public void setFontName(String fontName) {
			adaptee.getTextState().setFont(FontRepository.findFont(fontName));
		}

		@Override
		protected RichTextParagraph.Segment doSplit(String end) {
			TextFragment clonedTf = (TextFragment) actualParent.cloneWithSegments();
			int initialSize = actualParent.getSegments().size();
			int indexOfAdaptee = IntStream.rangeClosed(1, initialSize)
					.filter(i -> actualParent.getSegments().get_Item(i) == adaptee)
					.findFirst().orElseThrow();
			TextSegment cloned = clonedTf.getSegments().get_Item(indexOfAdaptee);
			cloned.setText(end);
			actualParent.getSegments().add(cloned);
			IntStream.generate(() -> indexOfAdaptee + 1)
					.limit(initialSize - indexOfAdaptee)
					.forEach(i -> {
						actualParent.getSegments().delete(i);
						actualParent.getSegments().add(clonedTf.getSegments().get_Item(i));
						clonedTf.getSegments().delete(i);
					});
			if (segmentsToDelete.contains(adaptee)) {
				segmentsToDelete.add(cloned);
			}
			return new SegmentAdapter(cloned, (RichTextParagraphAdapter) parent, actualParent);
		}

		@Override
		public void markForDeletion() {
			segmentsToDelete.add(adaptee);
		}
	}
}
