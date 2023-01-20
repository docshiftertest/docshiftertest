package com.docshifter.core.asposehelper.adapters;

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

import java.awt.*;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WordDocumentAdapter implements UnifiedDocument {

	private final Document adaptee;
	private final LayoutCollector layoutCollector;
	private boolean dirty = true;

	public WordDocumentAdapter(Path path) throws Exception {
		this(path.toString());
	}

	public WordDocumentAdapter(String path) throws Exception {
		adaptee = new Document(path);
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

	private void updatePageLayout() {
		if (!dirty) {
			return;
		}
		try {
			adaptee.updatePageLayout();
		} catch (Exception ex) {
			throw new WordProcessingException("Could not update page layout.", ex);
		}
		dirty = false;
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
				do {
					stragglers.add(currentNode);
					currentNode = currentNode.getNextSibling();
				} while (currentNode != null);
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
			currentResult = null;
			throw new UnsupportedOperationException("Not implemented yet.");
			// TODO
			//currentResult.remove();
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
			Node currNode = page.firstNode();
			do {
				currNode = currNode.getParentNode();
			} while (currNode.getNodeType() != NodeType.SECTION);
			adapteeSection = (Section) currNode;
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
			updatePageLayout();
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
						.collect(Collectors.groupingBy(straggler -> {
							Node current = straggler;
							do {
								current = current.getParentNode();
							} while (straggler.getNodeType() != NodeType.PARAGRAPH);
							return (Paragraph) current;
						})).entrySet().stream()
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
						.collect(Collectors.groupingBy(straggler -> {
							Node current = straggler;
							do {
								current = current.getParentNode();
							} while (straggler.getNodeType() != NodeType.PARAGRAPH);
							return (Paragraph) current;
						})).entrySet().stream()
						.map(entry -> new RichTextParagraphAdapter(entry.getKey(), entry.getValue().stream()));
			}

			return Stream.of(preamble, body, appendix)
					.flatMap(Function.identity());
		}

		@Override
		public Stream<InputStream> getImages() {
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
					.map(s -> {
						try {
							return s.getImageData().toStream();
						} catch (Exception ex) {
							throw new WordProcessingException("Cannot convert image data to stream.", ex);
						}
					});
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
	}

	public class RichTextParagraphAdapter extends AbstractRichTextParagraphAdapter<Paragraph> {
		private final Stream<Run> actualRuns;

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
			return switch (alignment) {
				case ParagraphAlignment.LEFT -> Alignment.LEFT;
				case ParagraphAlignment.CENTER -> Alignment.CENTER;
				case ParagraphAlignment.RIGHT -> Alignment.RIGHT;
				case ParagraphAlignment.JUSTIFY -> Alignment.JUSTIFIED;
				// TODO: test distributed
				case ParagraphAlignment.DISTRIBUTED -> Alignment.FULLY_JUSTIFIED;
				case ParagraphAlignment.ARABIC_LOW_KASHIDA -> Alignment.ARABIC_LOW_KISHIDA;
				case ParagraphAlignment.ARABIC_MEDIUM_KASHIDA -> Alignment.ARABIC_MEDIUM_KISHIDA;
				case ParagraphAlignment.ARABIC_HIGH_KASHIDA -> Alignment.ARABIC_HIGH_KISHIDA;
				case ParagraphAlignment.THAI_DISTRIBUTED -> Alignment.THAI_DISTRIBUTED;
				case ParagraphAlignment.MATH_ELEMENT_CENTER_AS_GROUP -> Alignment.MATH_ELEMENT_CENTER_AS_GROUP;
				default -> throw new IllegalStateException("Unknown alignment flag found: "
						+ ParagraphAlignment.getName(alignment) + " (" + alignment + ")");
			};
		}

		@Override
		public Stream<Segment> getSegments() {
			return actualRuns.map(SegmentAdapter::new);
		}
	}

	public static class SegmentAdapter extends AbstractSegmentAdapter<Run> {
		public SegmentAdapter(Run run) {
			super(run);
		}

		@Override
		public String getContent() {
			return adaptee.getText();
		}

		@Override
		public boolean isBold() {
			return adaptee.getFont().getBold();
		}

		@Override
		public boolean isItalic() {
			return adaptee.getFont().getItalic();
		}

		@Override
		public boolean isUnderline() {
			return adaptee.getFont().getUnderline() != Underline.NONE;
		}

		@Override
		public boolean isStrikethrough() {
			return adaptee.getFont().getStrikeThrough();
		}

		@Override
		public boolean isInvisibleRendering() {
			return false;
		}

		@Override
		public Color getForegroundColor() {
			return adaptee.getFont().getColor();
		}

		@Override
		public Color getBackgroundColor() {
			return adaptee.getFont().getHighlightColor();
		}

		@Override
		public Color getUnderlineColor() {
			return adaptee.getFont().getUnderlineColor();
		}

		@Override
		public double getFontSize() {
			return adaptee.getFont().getSize();
		}

		@Override
		public String getFontName() {
			return adaptee.getFont().getName();
		}
	}
}
