package com.docshifter.core.asposehelper.adapters;

import com.aspose.pdf.Document;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.FontStyles;
import com.aspose.pdf.HorizontalAlignment;
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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class PdfDocumentAdapter extends AbstractAdapter<Document> implements UnifiedDocument {
	private final Double headerMargin;
	private final Double footerMargin;
	private Map<Integer, Map<OutlineItemCollection, ExplicitDestination>> bookmarkCache = null;
	private final Set<Page> pagesToDelete = new HashSet<>();
	private final Set<MarkupSection> markupsToClear = new HashSet<>();
	private final Set<XImage> imagesToDelete = new HashSet<>();
	private final Set<OutlineItemCollection> bookmarksToDelete = new HashSet<>();
	private final Map<TextFragment, Set<TextSegment>> segmentsToDelete = new HashMap<>();

	public PdfDocumentAdapter(Path path, double headerMargin, double footerMargin) {
		this(path.toString(), headerMargin, footerMargin);
	}

	public PdfDocumentAdapter(String path, double headerMargin, double footerMargin) {
		super(new Document(path));
		this.headerMargin = headerMargin;
		this.footerMargin = footerMargin;
	}

	@Override
	public Stream<UnifiedPage> getPages() {
		return StreamSupport.stream(adaptee.getPages().spliterator(), false)
				.map(PageAdapter::new);
	}

	private Map<Integer, Map<OutlineItemCollection, ExplicitDestination>> getBookmarkCache() {
		if (bookmarkCache == null) {
			bookmarkCache =
					StreamSupport.stream(
							Spliterators.spliteratorUnknownSize(
									new FlatOutlineIterator(adaptee.getOutlines()),
									Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SORTED), false)
							.map(oic -> new AbstractMap.SimpleImmutableEntry<>(oic, OutlineUtils.extractExplicitDestinationSoft(adaptee, oic)))
							.collect(Collectors.groupingBy(entry -> entry.getValue().getPageNumber(),
									Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}
		return bookmarkCache;
	}

	@Override
	public void commitDeletes() {
		for (Iterator<Page> it = pagesToDelete.iterator(); it.hasNext();) {
			adaptee.getPages().remove(it.next());
			it.remove();
		}
		for (Iterator<MarkupSection> it = markupsToClear.iterator(); it.hasNext();) {
			it.next().getFragments().clear();
			it.remove();
		}
		for (Iterator<Map.Entry<TextFragment, Set<TextSegment>>> it = segmentsToDelete.entrySet().iterator(); it.hasNext();) {
			Map.Entry<TextFragment, Set<TextSegment>> currEntry = it.next();
			TextFragment tf = currEntry.getKey();
			for (TextSegment textSegment : currEntry.getValue()) {
				tf.getSegments().remove(textSegment);
			}
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
	public void close() {
		adaptee.close();
	}

	public class PageAdapter extends AbstractAdapter<Page> implements UnifiedPage {
		public PageAdapter(Page page) {
			super(page);
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
		public Stream<RichTextParagraph> getBodyText() {
			ParagraphAbsorber absorber = new ParagraphAbsorber();
			absorber.visit(adaptee);
			return absorber.getPageMarkups().stream()
					.flatMap(markup -> markup.getSections().stream())
					.filter(section -> section.getRectangle().getLLY() >= calculateFooterBoundary()
							&& section.getRectangle().getURY() <= calculateHeaderBoundary())
					.map(RichTextParagraphAdapter::new);
		}

		@Override
		public Stream<Image> getImages() {
			return StreamSupport.stream(adaptee.getResources().getImages().spliterator(), false)
					.map(ImageAdapter::new);
		}

		@Override
		public Stream<Bookmark> getBookmarks() {
			return getBookmarkCache().get(getNumber()).entrySet().stream()
					.map(entry -> new BookmarkAdapter(entry.getKey(), entry.getValue()));
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

	public class ImageAdapter extends AbstractAdapter<XImage> implements Image {
		public ImageAdapter(XImage adaptee) {
			super(adaptee);
		}

		@Override
		public InputStream getInputStream() {
			return adaptee.toStream();
		}

		@Override
		public void markForDeletion() {
			imagesToDelete.add(adaptee);
		}

		@Override
		public Type getType() {
			return Type.IMAGE;
		}
	}

	public class BookmarkAdapter extends AbstractAdapter<OutlineItemCollection> implements Bookmark {
		private final Point topLeftDest;

		public BookmarkAdapter(OutlineItemCollection oic, ExplicitDestination dest) {
			super(oic);
			this.topLeftDest = ExplicitDestinationTransformer.create(dest).getTopLeft();
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
				.put(HorizontalAlignment.Left, Alignment.LEFT)
				.put(HorizontalAlignment.Center, Alignment.CENTER)
				.put(HorizontalAlignment.Right, Alignment.RIGHT)
				.put(HorizontalAlignment.Justify, Alignment.JUSTIFIED)
				.put(HorizontalAlignment.FullJustify, Alignment.FULLY_JUSTIFIED)
				.put(HorizontalAlignment.None, Alignment.NONE)
				.build();
		public RichTextParagraphAdapter(MarkupSection section) {
			super(section);
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
		public void setHorizontalAlignment(Alignment alignment) {
			Integer mapping = alignmentMap.inverse().get(alignment);
			if (mapping == null) {
				throw new UnsupportedOperationException("Cannot convert alignment flag " + alignment + " to one that " +
						"is supported in Word documents.");
			}
			adaptee.getFragments().forEach(tf -> tf.setHorizontalAlignment(mapping));
		}

		@Override
		public Stream<Segment> getSegments() {
			return adaptee.getFragments().stream()
					.flatMap(tf -> StreamSupport.stream(tf.getSegments().spliterator(), false)
							.map(seg -> new AbstractMap.SimpleImmutableEntry<>(tf, seg)))
					.map(entry -> new SegmentAdapter(entry.getValue(), entry.getKey()));
		}

		@Override
		public void markForDeletion() {
			markupsToClear.add(adaptee);
		}
	}

	public class SegmentAdapter extends AbstractSegmentAdapter<TextSegment> {
		private final TextFragment parent;
		public SegmentAdapter(TextSegment segment, TextFragment parent) {
			super(segment);
			this.parent = parent;
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
			return adaptee.getTextState().getBackgroundColor().toRgb();
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
		public void markForDeletion() {
			segmentsToDelete.compute(parent, (k, v) -> {
				if (v == null) {
					v = new HashSet<>();
				}
				v.add(adaptee);
				return v;
			});
		}
	}
}
