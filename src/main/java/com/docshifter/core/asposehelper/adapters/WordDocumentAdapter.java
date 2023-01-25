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
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class WordDocumentAdapter implements UnifiedDocument {

	private final Document adaptee;
	private final LayoutCollector layoutCollector;
	private final Set<Node> nodesToDelete = new HashSet<>();
	private boolean dirty = true;

	public WordDocumentAdapter(Path path) {
		this(path.toString());
	}

	public WordDocumentAdapter(String path) {
		try {
			adaptee = new Document(path);
		} catch (Exception ex) {
			throw new WordProcessingException("Unable to load document at " + path, ex);
		}
		layoutCollector = new LayoutCollector(adaptee);
	}

	@Override
	public Stream<UnifiedPage> getPages() {
		int pageCount;
		try {
			pageCount = adaptee.getPageCount();
			dirty = false;
		} catch (Exception ex) {
			throw new WordProcessingException("Unable to calculate page count.", ex);
		}
		Iterator<Node> flattenedBodyNodes = StreamSupport.stream(adaptee.getSections().spliterator(), false)
				.map(Section::getBody)
				.flatMap(body -> StreamSupport.stream(body.spliterator(), false))
				.iterator();
		return StreamSupport.stream(Spliterators.spliterator(new PageAdapterIterator(flattenedBodyNodes), pageCount, 0), false);
	}

	@Override
	public void commitDeletes() {
		dirty = true;
		for (Iterator<Node> it = nodesToDelete.iterator(); it.hasNext();) {
			it.next().remove();
			it.remove();
		}
	}

	private boolean updatePageLayout() {
		if (!dirty) {
			return false;
		}
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
		layoutCollector.setDocument(null);
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
			return it.hasNext();
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

	public class PageAdapter implements UnifiedPage {
		private final PageCollection adaptee;
		private final int number;
		private final Section adapteeSection;

		public PageAdapter(PageCollection page, int number) {
			this.adaptee = page;
			this.number = number;
			adapteeSection = (Section) page.firstNode().getAncestor(NodeType.SECTION);
		}

		@Override
		public Stream<RichTextParagraph> getHeaderText() {
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
				return Stream.empty();
			}
			return StreamSupport.stream(
							((NodeCollection<? extends Node>)headerFooter.getChildNodes(NodeType.PARAGRAPH, true)).spliterator(), false)
					.map(Paragraph.class::cast)
					.map(RichTextParagraphAdapter::new);
		}

		@Override
		public Stream<RichTextParagraph> getFooterText() {
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
				return Stream.empty();
			}
			return StreamSupport.stream(
					((NodeCollection<? extends Node>)headerFooter.getChildNodes(NodeType.PARAGRAPH, true)).spliterator(), false)
					.map(Paragraph.class::cast)
					.map(RichTextParagraphAdapter::new);
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
		public Stream<RichTextParagraph> getBodyText() {
			// TODO: test paragraphs nested in paragraphs
			Stream<RichTextParagraph> preamble = Stream.empty();
			if (adaptee.preamble != null) {
				preamble = adaptee.preamble.getValue().stream()
						.flatMap(node -> {
							if (node.getNodeType() == NodeType.RUN) {
								return Stream.of(node);
							}

							if (node instanceof CompositeNode<? extends Node> cn) {
								return StreamSupport.stream(
										((NodeCollection<? extends Node>)cn.getChildNodes(NodeType.RUN, true)).spliterator(), false);
							}

							return Stream.empty();
						})
						.map(Run.class::cast)
						.collect(Collectors.groupingBy(straggler -> (Paragraph) straggler.getAncestor(NodeType.PARAGRAPH)))
						.entrySet().stream()
						.map(entry -> new RichTextParagraphAdapter(entry.getKey(), entry.getValue().stream()));
			}

			Stream<RichTextParagraph> body = adaptee.body.stream()
					.flatMap(node -> {
						if (node.getNodeType() == NodeType.PARAGRAPH) {
							return Stream.of(node);
						}

						if (node instanceof CompositeNode<? extends Node> cn) {
							return StreamSupport.stream(
									((NodeCollection<? extends Node>)cn.getChildNodes(NodeType.PARAGRAPH, true)).spliterator(), false);
						}

						return Stream.empty();
					}).map(Paragraph.class::cast)
					.map(RichTextParagraphAdapter::new);

			Stream<RichTextParagraph> appendix = Stream.empty();
			if (adaptee.appendix != null) {
				appendix = adaptee.appendix.getValue().stream()
						.flatMap(node -> {
							if (node.getNodeType() == NodeType.RUN) {
								return Stream.of(node);
							}

							if (node instanceof CompositeNode<? extends Node> cn) {
								return StreamSupport.stream(
										((NodeCollection<? extends Node>)cn.getChildNodes(NodeType.RUN, true)).spliterator(), false);
							}

							return Stream.empty();
						})
						.map(Run.class::cast)
						.collect(Collectors.groupingBy(straggler -> (Paragraph) straggler.getAncestor(NodeType.PARAGRAPH)))
						.entrySet().stream()
						.map(entry -> new RichTextParagraphAdapter(entry.getKey(), entry.getValue().stream()));
			}

			return Stream.of(preamble, body, appendix)
					.flatMap(Function.identity());
		}

		@Override
		public Stream<Image> getImages() {
			return adaptee.combinedNodes()
					.filter(n -> n.getNodeType() == NodeType.SHAPE)
					.map(Shape.class::cast)
					.filter(s -> {
						try {
							return s.getShapeType() == ShapeType.IMAGE && s.hasImage();
						} catch (Exception ex) {
							throw new WordProcessingException("Cannot check if shape has an image.", ex);
						}
					})
					.map(ImageAdapter::new);
		}

		@Override
		public Stream<Bookmark> getBookmarks() {
			return adaptee.combinedNodes()
					.filter(n -> n.getNodeType() == NodeType.BOOKMARK_START)
					.map(BookmarkStart.class::cast)
					.map(BookmarkAdapter::new);
		}

		@Override
		public Color getBackgroundColor() {
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
			nodesToDelete.addAll(adaptee.preamble.getValue());
			nodesToDelete.addAll(adaptee.body);
			nodesToDelete.addAll(adaptee.appendix.getValue());
		}
	}

	public class ImageAdapter extends AbstractAdapter<Shape> implements Image {
		public ImageAdapter(Shape adaptee) {
			super(adaptee);
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
		private final Paragraph startParentParagraph;
		private final Paragraph endParentParagraph;
		private final Set<Paragraph> embeddedParagraphs;

		public BookmarkAdapter(BookmarkStart start) {
			this.startAdaptee = start;
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

		public RichTextParagraphAdapter(Paragraph paragraph) {
			this(paragraph,
					StreamSupport.stream(
							((NodeCollection<? extends Node>)paragraph.getChildNodes(NodeType.RUN, true)).spliterator(), false)
							.map(Run.class::cast));
		}

		public RichTextParagraphAdapter(Paragraph paragraph, Stream<Run> actualRuns) {
			super(paragraph);
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
			return actualRuns.map(SegmentAdapter::new);
		}

		@Override
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}
	}

	public class SegmentAdapter extends AbstractSegmentAdapter<Run> {
		public SegmentAdapter(Run run) {
			super(run);
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
			adaptee.getFont().setUnderline(Underline.NONE);
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
		public void markForDeletion() {
			nodesToDelete.add(adaptee);
		}
	}
}
