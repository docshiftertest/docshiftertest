package com.docshifter.core.asposehelper.adapters;

import com.aspose.words.BookmarkEnd;
import com.aspose.words.BookmarkStart;
import com.aspose.words.CompositeNode;
import com.aspose.words.Document;
import com.aspose.words.HeaderFooter;
import com.aspose.words.HeaderFooterType;
import com.aspose.words.LayoutCollector;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.ParagraphAlignment;
import com.aspose.words.Run;
import com.aspose.words.Section;
import com.aspose.words.Shape;
import com.aspose.words.ShapeType;
import com.aspose.words.Underline;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class WordDocumentAdapter extends AbstractAdapter<Document> implements UnifiedDocument {
	private LayoutCollector layoutCollector;
	private final Set<Node> nodesToDelete = new HashSet<>();
	private boolean dirty = true;

	public WordDocumentAdapter(Path path) throws Exception {
		this(path.toString());
	}

	public WordDocumentAdapter(String path) throws Exception {
		super(new Document(path));
	}

	@Override
	public Type getType() {
		return Type.WORD;
	}

	@Override
	public Stream<UnifiedPage> getPages() {
		Iterator<Node> flattenedBodyNodes = StreamSupport.stream(adaptee.getSections().spliterator(), false)
				.map(Section::getBody)
				.flatMap(body -> StreamSupport.stream(body.spliterator(), false))
				.iterator();
		return StreamSupport.stream(Spliterators.spliterator(new PageAdapterIterator(flattenedBodyNodes), getPageCount(), 0), false);
	}

	@Override
	public int getPageCount() {
		updatePageLayout();
		try {
			return adaptee.getPageCount();
		} catch (Exception ex) {
			throw new WordProcessingException("Unable to calculate page count.", ex);
		}
	}

	@Override
	public void commitDeletes() {
		dirty = true;
		for (Iterator<Node> it = nodesToDelete.iterator(); it.hasNext();) {
			it.next().remove();
			it.remove();
		}
	}

	@Override
	public void save(String path) {
		try {
			adaptee.save(path);
		} catch (Exception ex) {
			throw new WordProcessingException("Unable to save Word document", ex);
		}
	}

	private boolean updatePageLayout() {
		if (!dirty) {
			return false;
		}
		// Need to initialize a new collector each time because simply calling updatePageLayout() might retain the
		// old page numbers after deleting a page.
		layoutCollector = new LayoutCollector(adaptee);
		try {
			adaptee.updatePageLayout();
		} catch (Exception ex) {
			throw new WordProcessingException("Could not update page layout.", ex);
		}
		dirty = false;
		return true;
	}

	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void close() {
		if (layoutCollector != null) {
			layoutCollector.setDocument(null);
		}
	}

	public class PageAdapterIterator implements Iterator<PageAdapter> {
		private Node currentNode;
		private final Iterator<Node> it;
		private PageAdapter currentResult;

		public PageAdapterIterator(Iterator<Node> flattenedBodyNodes) {
			updatePageLayout();
			it = flattenedBodyNodes;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null || it.hasNext();
		}

		@Override
		public PageAdapter next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			try {
				doNext();
			} catch (Exception ex) {
				throw new WordProcessingException("Could not get layout information from document.", ex);
			}

			return currentResult;
		}

		private void doNext() throws Exception {
			if (currentNode == null) {
				currentNode = it.next();
			}
			int currentPageNumber = currentResult == null ? 1 : currentResult.getNumber() + 1;
			Map.Entry<Node, List<Node>> preamble = null;
			List<Node> body = new ArrayList<>();
			Map.Entry<Node, List<Node>> appendix = null;

			if (currentNode.getParentNode().getNodeType() != NodeType.BODY) {
				Node rootNode;
				do {
					rootNode = currentNode.getParentNode();
				} while (rootNode.getParentNode().getNodeType() != NodeType.BODY);
				List<Node> stragglers = new ArrayList<>();
				Node nextNode = currentNode;
				do {
					stragglers.add(currentNode);
					currentNode = currentNode.getNextSibling();
				} while (currentNode != null);
				currentNode = nextNode;
				preamble = new AbstractMap.SimpleImmutableEntry<>(rootNode, stragglers);
			}

			boolean collectStragglers = false;
			while (true) {
				if (layoutCollector.getStartPageIndex(currentNode) != currentPageNumber) {
					break;
				}
				if (layoutCollector.getEndPageIndex(currentNode) != currentPageNumber) {
					collectStragglers = true;
					break;
				}
				body.add(currentNode);
				if (!it.hasNext()) {
					currentNode = null;
					break;
				}
				currentNode = it.next();
			}

			if (collectStragglers) {
				Node rootNode = currentNode;
				List<Node> stragglers = new ArrayList<>();
				do {
					currentNode = currentNode.nextPreOrder(rootNode);
				} while (layoutCollector.getEndPageIndex(currentNode) != currentPageNumber);
				do {
					stragglers.add(currentNode);
					currentNode = currentNode.getNextSibling();
				} while (currentNode != null && layoutCollector.getStartPageIndex(currentNode) == currentPageNumber);
				appendix = new AbstractMap.SimpleImmutableEntry<>(rootNode, stragglers);
			}

			currentResult = new PageAdapter(new PageCollection(preamble, body, appendix), currentPageNumber);
		}

		@Override
		public void remove() {
			if (currentResult == null) {
				throw new NoSuchElementException();
			}
			currentResult.markForDeletion();
			commitDeletes();
			currentResult = null;
		}
	}

	public record PageCollection(Map.Entry<Node, List<Node>> preamble,
								 List<Node> body,
								 Map.Entry<Node, List<Node>> appendix) {
		public Stream<Node> combinedNodes() {
			Stream<Node> combined = Stream.empty();
			if (preamble != null) {
				combined = preamble.getValue().stream();
			}
			if (!body.isEmpty()) {
				combined = Stream.concat(combined, body.stream());
			}
			if (appendix != null) {
				combined = Stream.concat(combined, appendix.getValue().stream());
			}
			return combined;
		}

		public Node firstNode() {
			Node firstNode;
			if (preamble != null) {
				firstNode = preamble.getValue().get(0);
			} else if (!body.isEmpty()) {
				firstNode = body.get(0);
			} else {
				firstNode = appendix.getValue().get(0);
			}
			return firstNode;
		}
	}

	public class PageAdapter extends AbstractAdapterChild<PageCollection, WordDocumentAdapter> implements UnifiedPage, UnifiedPage.PageSection {
		private final int number;
		private final Section adapteeSection;
		private static final Map<Class<? extends Node>, Integer> NODE_TYPES_MAP = Map.of(
				Shape.class, NodeType.SHAPE,
				BookmarkStart.class, NodeType.BOOKMARK_START,
				Paragraph.class, NodeType.PARAGRAPH,
				Run.class, NodeType.RUN
		);

		public PageAdapter(PageCollection page, int number) {
			super(page, WordDocumentAdapter.this);
			this.number = number;
			adapteeSection = (Section) page.firstNode().getAncestor(NodeType.SECTION);
		}

		@Override
		public Optional<PageSection> getHeader() {
			int numberInSection = getPageNumberInSection();
			int headerFooterType;
			if (numberInSection == 1) {
				headerFooterType = HeaderFooterType.HEADER_FIRST;
			} else if (number % 2 == 0) {
				headerFooterType = HeaderFooterType.HEADER_EVEN;
			} else {
				headerFooterType = HeaderFooterType.HEADER_PRIMARY;
			}
			HeaderFooter headerFooter = adapteeSection.getHeadersFooters().getByHeaderFooterType(headerFooterType);
			if (headerFooter == null && headerFooterType != HeaderFooterType.HEADER_PRIMARY) {
				headerFooter = adapteeSection.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_PRIMARY);
			}
			if (headerFooter == null) {
				return Optional.empty();
			}
			return Optional.of(new HeaderFooterPageSectionAdapter(headerFooter, this));
		}

		@Override
		public Optional<PageSection> getFooter() {
			int numberInSection = getPageNumberInSection();
			int headerFooterType;
			if (numberInSection == 1) {
				headerFooterType = HeaderFooterType.FOOTER_FIRST;
			} else if (number % 2 == 0) {
				headerFooterType = HeaderFooterType.FOOTER_EVEN;
			} else {
				headerFooterType = HeaderFooterType.FOOTER_PRIMARY;
			}
			HeaderFooter headerFooter = adapteeSection.getHeadersFooters().getByHeaderFooterType(headerFooterType);
			if (headerFooter == null && headerFooterType != HeaderFooterType.FOOTER_PRIMARY) {
				headerFooter = adapteeSection.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_PRIMARY);
			}
			if (headerFooter == null) {
				return Optional.empty();
			}
			return Optional.of(new HeaderFooterPageSectionAdapter(headerFooter, this));
		}

		private int getPageNumberInSection() {
			if (updatePageLayout()) {
				log.debug("Page layout required update. Fetching new page number.");
				int newNumber;
				try {
					newNumber = layoutCollector.getStartPageIndex(adaptee.firstNode());
				} catch (Exception ex) {
					throw new WordProcessingException("Could not get start index of first node on the page.", ex);
				}
				if (newNumber != number) {
					// TODO: how can we deal with this? commitChanges in general instead of only commitDeletes?
					throw new IllegalStateException("Page information is outdated as modifications have been made to " +
							"the document. All pages need to be refetched.");
				}
			}
			int startOfSection;
			try {
				startOfSection = layoutCollector.getStartPageIndex(adapteeSection);
			} catch (Exception ex) {
				throw new WordProcessingException("Could not get start index of section corresponding to page " + number, ex);
			}
			return number - startOfSection + 1;
		}

		@Override
		public PageSection getBody() {
			return this;
		}

		@Override
		public Type getType() {
			return Type.BODY;
		}

		@Override
		public Stream<RichTextParagraph> getTextParagraphs() {
			// TODO: test paragraphs nested in paragraphs
			Stream<RichTextParagraph> preamble = Stream.empty();
			if (adaptee.preamble != null) {
				preamble = adaptee.preamble.getValue().stream()
						.flatMap(node -> findAnywhere(node, Run.class))
						.collect(Collectors.groupingBy(straggler -> (Paragraph) straggler.getAncestor(NodeType.PARAGRAPH)))
						.entrySet().stream()
						.map(entry -> new RichTextParagraphAdapter(entry.getKey(), this, entry.getValue().stream()));
			}

			Stream<RichTextParagraph> body = adaptee.body.stream()
					.flatMap(node -> findAnywhere(node, Paragraph.class))
					.map(p -> new RichTextParagraphAdapter(p, this));

			Stream<RichTextParagraph> appendix = Stream.empty();
			if (adaptee.appendix != null) {
				appendix = adaptee.appendix.getValue().stream()
						.flatMap(node -> findAnywhere(node, Run.class))
						.collect(Collectors.groupingBy(straggler -> (Paragraph) straggler.getAncestor(NodeType.PARAGRAPH)))
						.entrySet().stream()
						.map(entry -> new RichTextParagraphAdapter(entry.getKey(), this, entry.getValue().stream()));
			}

			return Stream.of(preamble, body, appendix)
					.flatMap(Function.identity());
		}

		@Override
		public Stream<Image> getImages() {
			return adaptee.combinedNodes()
					.flatMap(node -> findAnywhere(node, Shape.class))
					.filter(s -> {
						try {
							return s.getShapeType() == ShapeType.IMAGE && s.hasImage();
						} catch (Exception ex) {
							throw new WordProcessingException("Cannot check if shape has an image.", ex);
						}
					})
					.map(s -> new ImageAdapter(s, this));
		}

		@Override
		public Stream<Bookmark> getBookmarks() {
			return adaptee.combinedNodes()
					.flatMap(node -> findAnywhere(node, BookmarkStart.class))
					.map(start -> new BookmarkAdapter(start, this));
		}

		private <T extends Node> Stream<T> findAnywhere(Node node, Class<T> nodeTypeToFind) {
			int nodeTypeAsInt = NODE_TYPES_MAP.get(nodeTypeToFind);
			Stream<? extends Node> stream;
			if (node.getNodeType() == nodeTypeAsInt) {
				stream = Stream.of(node);
			} else if (node instanceof CompositeNode<? extends Node> cn) {
				stream = StreamSupport.stream(
						((NodeCollection<? extends Node>)cn.getChildNodes(nodeTypeAsInt, true)).spliterator(), false);
			} else {
				stream = Stream.empty();
			}
			return stream.map(nodeTypeToFind::cast);
		}

		@Override
		public Color getBackgroundColor() {
			// TODO check header/footer section
			return adaptee.combinedNodes()
					.filter(n -> n.getNodeType() == NodeType.SHAPE)
					.map(Shape.class::cast)
					.filter(s -> s.getShapeType() == ShapeType.RECTANGLE)
					.filter(Shape::getFilled)
					.filter(s -> s.getBounds().getX() == 0 && s.getBounds().getY() == 0
							&& s.getBounds().getWidth() == adapteeSection.getPageSetup().getPageWidth()
							&& s.getBounds().getHeight() == adapteeSection.getPageSetup().getPageHeight())
					.map(Shape::getFillColor)
					.findFirst()
					.orElse(adapteeSection.getDocument().getPageColor());
		}

		@Override
		public int getNumber() {
			return number;
		}

		@Override
		public void markForDeletion() {
			if (adaptee.preamble != null) {
				nodesToDelete.addAll(adaptee.preamble.getValue());
			}
			nodesToDelete.addAll(adaptee.body);
			if (adaptee.appendix != null) {
				nodesToDelete.addAll(adaptee.appendix.getValue());
			}
		}

		@Override
		public UnifiedPage getPage() {
			return this;
		}
	}

	public class HeaderFooterPageSectionAdapter extends AbstractAdapterChild<HeaderFooter, PageAdapter> implements UnifiedPage.PageSection {
		public HeaderFooterPageSectionAdapter(HeaderFooter adaptee, PageAdapter parent) {
			super(adaptee, parent);
		}

		@Override
		public Type getType() {
			return adaptee.isHeader() ? Type.HEADER : Type.FOOTER;
		}

		@Override
		public Stream<RichTextParagraph> getTextParagraphs() {
			return StreamSupport.stream(
							((NodeCollection<? extends Node>)adaptee.getChildNodes(NodeType.PARAGRAPH, true)).spliterator(), false)
					.map(Paragraph.class::cast)
					.map(p -> new RichTextParagraphAdapter(p, this));
		}

		@Override
		public Stream<Image> getImages() {
			return StreamSupport.stream(
							((NodeCollection<? extends Node>)adaptee.getChildNodes(NodeType.SHAPE, true)).spliterator(), false)
					.map(Shape.class::cast)
					.filter(s -> {
						try {
							return s.getShapeType() == ShapeType.IMAGE && s.hasImage();
						} catch (Exception ex) {
							throw new WordProcessingException("Cannot check if shape has an image.", ex);
						}
					})
					.map(s -> new ImageAdapter(s, this));
		}

		@Override
		public Stream<Bookmark> getBookmarks() {
			return StreamSupport.stream(
							((NodeCollection<? extends Node>)adaptee.getChildNodes(NodeType.BOOKMARK_START, true)).spliterator(), false)
					.filter(n -> n.getNodeType() == NodeType.BOOKMARK_START)
					.map(BookmarkStart.class::cast)
					.map(start -> new BookmarkAdapter(start, this));
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}

		@Override
		public UnifiedPage getPage() {
			return parent;
		}
	}

	public class ImageAdapter extends AbstractAdapterChild<Shape, UnifiedPage.PageSection> implements Image {
		public ImageAdapter(Shape adaptee, UnifiedPage.PageSection parent) {
			super(adaptee, parent);
		}

		@Override
		public InputStream getInputStream() {
			try {
				return adaptee.getImageData().toStream();
			} catch (Exception ex) {
				throw new WordProcessingException("Cannot convert image data to stream.", ex);
			}
		}

		@Override
		public double getWidth() {
			return adaptee.getWidth();
		}

		@Override
		public double getHeight() {
			return adaptee.getHeight();
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}

		@Override
		public Type getType() {
			return Type.IMAGE;
		}
	}

	public class BookmarkAdapter implements Bookmark {
		private final BookmarkStart startAdaptee;
		private final BookmarkEnd endAdaptee;
		private final UnifiedPage.PageSection parent;
		private final Paragraph startParentParagraph;
		private final Paragraph endParentParagraph;
		private final Set<Paragraph> embeddedParagraphs;

		public BookmarkAdapter(BookmarkStart start, UnifiedPage.PageSection parent) {
			this.startAdaptee = start;
			this.parent = parent;
			int openBookmarks = 1;
			Node currNode = start;
			Set<Paragraph> embeddedParagraphs = new HashSet<>();
			do {
				currNode = currNode.nextPreOrder(adaptee);
				if (currNode.getNodeType() == NodeType.BOOKMARK_START) {
					openBookmarks++;
				} else if (currNode.getNodeType() == NodeType.BOOKMARK_END) {
					openBookmarks--;
				} else if (currNode.getNodeType() == NodeType.PARAGRAPH) {
					embeddedParagraphs.add((Paragraph) currNode);
				}
			} while (openBookmarks > 0);
			this.endAdaptee = (BookmarkEnd) currNode;
			this.embeddedParagraphs = Collections.unmodifiableSet(embeddedParagraphs);
			this.startParentParagraph = (Paragraph) start.getAncestor(NodeType.PARAGRAPH);
			this.endParentParagraph = (Paragraph) endAdaptee.getAncestor(NodeType.PARAGRAPH);
		}

		@Override
		public String getTitle() {
			return startAdaptee.getName();
		}

		@Override
		public boolean pointsTo(RichTextParagraph paragraph) {
			if (paragraph instanceof RichTextParagraphAdapter paraAdapter) {
				Paragraph destination = paraAdapter.adaptee;
				return destination == startParentParagraph || destination == endParentParagraph || embeddedParagraphs.contains(destination);
			}
			throw new IllegalStateException("Only paragraphs within the same document can be analyzed.");
		}

		@Override
		public boolean supportsStyling() {
			return false;
		}

		@Override
		public boolean isBold() {
			return false;
		}

		@Override
		public void setBold(boolean bold) {
			throw new UnsupportedOperationException("Bookmark styling is not supported for Word documents.");
		}

		@Override
		public boolean isItalic() {
			return false;
		}

		@Override
		public void setItalic(boolean italic) {
			throw new UnsupportedOperationException("Bookmark styling is not supported for Word documents.");
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(startAdaptee);
			nodesToDelete.add(endAdaptee);
		}

		@Override
		public Type getType() {
			return Type.BOOKMARK;
		}

		@Override
		public UnifiedPage.PageSection getParent() {
			return parent;
		}
	}

	public class RichTextParagraphAdapter extends AbstractRichTextParagraphAdapter<Paragraph> {
		private final Stream<Run> actualRuns;
		private final BiMap<Integer, Alignment> alignmentMap = ImmutableBiMap.<Integer, Alignment>builder()
				.put(ParagraphAlignment.LEFT, Alignment.LEFT)
				.put(ParagraphAlignment.CENTER, Alignment.CENTER)
				.put(ParagraphAlignment.RIGHT, Alignment.RIGHT)
				.put(ParagraphAlignment.JUSTIFY, Alignment.JUSTIFIED)
				.put(ParagraphAlignment.DISTRIBUTED, Alignment.FULLY_JUSTIFIED)
				.put(ParagraphAlignment.ARABIC_LOW_KASHIDA, Alignment.ARABIC_LOW_KISHIDA)
				.put(ParagraphAlignment.ARABIC_MEDIUM_KASHIDA, Alignment.ARABIC_MEDIUM_KISHIDA)
				.put(ParagraphAlignment.ARABIC_HIGH_KASHIDA, Alignment.ARABIC_HIGH_KISHIDA)
				.put(ParagraphAlignment.THAI_DISTRIBUTED, Alignment.THAI_DISTRIBUTED)
				.put(ParagraphAlignment.MATH_ELEMENT_CENTER_AS_GROUP, Alignment.MATH_ELEMENT_CENTER_AS_GROUP)
				.build();

		public RichTextParagraphAdapter(Paragraph paragraph, UnifiedPage.PageSection parent) {
			this(paragraph, parent,
					StreamSupport.stream(
							((NodeCollection<? extends Node>)paragraph.getChildNodes(NodeType.RUN, true)).spliterator(), false)
							.map(Run.class::cast));
		}

		public RichTextParagraphAdapter(Paragraph paragraph, UnifiedPage.PageSection parent, Stream<Run> actualRuns) {
			super(paragraph, parent);
			this.actualRuns = actualRuns;
		}

		@Override
		public Alignment getHorizontalAlignment() {
			int alignment = adaptee.getParagraphFormat().getAlignment();
			Alignment mapping = alignmentMap.get(alignment);
			if (mapping == null) {
				throw new IllegalStateException("Unknown alignment flag found: "
						+ ParagraphAlignment.getName(alignment) + " (" + alignment + ")");
			}
			return mapping;
		}

		@Override
		public void setHorizontalAlignment(Alignment alignment) {
			if (alignment == Alignment.NONE) {
				alignment = Alignment.LEFT;
			}
			Integer mapping = alignmentMap.inverse().get(alignment);
			if (mapping == null) {
				throw new UnsupportedOperationException("Cannot convert alignment flag " + alignment + " to one that " +
						"is supported in Word documents.");
			}
			adaptee.getParagraphFormat().setAlignment(mapping);
			dirty = true;
		}

		@Override
		public Stream<Segment> getSegments() {
			return actualRuns.map(run -> new SegmentAdapter(run, this));
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}
	}

	public class SegmentAdapter extends AbstractSegmentAdapter<Run> {
		public SegmentAdapter(Run run, RichTextParagraphAdapter parent) {
			super(run, parent);
		}

		@Override
		public String getContent() {
			return adaptee.getText();
		}

		@Override
		public void setContent(String content) {
			adaptee.setText(content);
			dirty = true;
		}

		@Override
		public boolean isBold() {
			return adaptee.getFont().getBold();
		}

		@Override
		public void setBold(boolean bold) {
			adaptee.getFont().setBold(bold);
			dirty = true;
		}

		@Override
		public boolean isItalic() {
			return adaptee.getFont().getItalic();
		}

		@Override
		public void setItalic(boolean italic) {
			adaptee.getFont().setItalic(italic);
			dirty = true;
		}

		@Override
		public boolean isUnderline() {
			return adaptee.getFont().getUnderline() != Underline.NONE;
		}

		@Override
		public void setUnderline(boolean underline) {
			adaptee.getFont().setUnderline(underline ? Underline.SINGLE : Underline.NONE);
			dirty = true;
		}

		@Override
		public boolean isStrikethrough() {
			return adaptee.getFont().getStrikeThrough();
		}

		@Override
		public void setStrikethrough(boolean strikethrough) {
			adaptee.getFont().setStrikeThrough(strikethrough);
			dirty = true;
		}

		@Override
		public boolean isInvisibleRendering() {
			return false;
		}

		@Override
		public boolean isInvisibleRenderingSupported() {
			return false;
		}

		@Override
		public void setInvisibleRendering(boolean invisibleRendering) {
			throw new UnsupportedOperationException("Word documents don't support text rendering.");
		}

		@Override
		public Color getForegroundColor() {
			return adaptee.getFont().getColor();
		}

		@Override
		public void setForegroundColor(Color color) {
			adaptee.getFont().setColor(color);
			dirty = true;
		}

		@Override
		public Color getBackgroundColor() {
			return adaptee.getFont().getHighlightColor();
		}

		@Override
		public void setBackgroundColor(Color color) {
			adaptee.getFont().setHighlightColor(color);
			dirty = true;
		}

		@Override
		public Color getUnderlineColor() {
			return adaptee.getFont().getUnderlineColor();
		}

		@Override
		public void setUnderlineColor(Color color) {
			adaptee.getFont().setUnderlineColor(color);
			dirty = true;
		}

		@Override
		public double getFontSize() {
			return adaptee.getFont().getSize();
		}

		@Override
		public void setFontSize(double fontSize) {
			adaptee.getFont().setSize(fontSize);
			dirty = true;
		}

		@Override
		public String getFontName() {
			return adaptee.getFont().getName();
		}

		@Override
		public void setFontName(String fontName) {
			adaptee.getFont().setName(fontName);
			dirty = true;
		}

		@Override
		protected RichTextParagraph.Segment doSplit(String end) {
			Run cloned = (Run) adaptee.getParentNode().insertAfter(adaptee.deepClone(true), adaptee);
			cloned.setText(end);
			if (nodesToDelete.contains(adaptee)) {
				nodesToDelete.add(cloned);
			}
			return new SegmentAdapter(cloned, (RichTextParagraphAdapter) parent);
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}
	}
}
