package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Annotation;
import com.aspose.pdf.AnnotationType;
import com.aspose.pdf.Document;
import com.aspose.pdf.LinkAnnotation;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageCollection;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.aspose.pdf.TextSearchOptions;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
public class PdfToc {
    private final TextFragment titleFragment;
    private final List<Element> elements;

    private static final Comparator<Rectangle> LOGICAL_RECTANGLE_COMPARATOR =
            Comparator.comparingDouble(Rectangle::getURY).reversed()
                    .thenComparingDouble(Rectangle::getLLX)
                    .thenComparingDouble(Rectangle::getLLY)
                    .thenComparing(Comparator.comparingDouble(Rectangle::getURX).reversed());

    public PdfToc(@Nonnull TextFragment titleFragment, int lineSpaceMargin, int headerMargin, int footerMargin) {
        this.titleFragment = titleFragment;
        Page searchPage = titleFragment.getPage();
        Rectangle searchRect = titleFragment.getRectangle();
        searchRect.setLLX(0);
        searchRect.setURX(searchPage.getRect().getWidth());
        searchRect.setLLY(0);
        TextFragmentAbsorber tfa = new TextFragmentAbsorber(Element.SEARCH_PATTERN, new TextSearchOptions(searchRect));
        List<Element> elements = new ArrayList<>();
        boolean first = true;
        while (true) {
            tfa.visit(searchPage);
            Iterator<TextFragment> tfIt = StreamSupport.stream(tfa.getTextFragments().spliterator(), false)
                    .sorted(Comparator.comparing(TextFragment::getRectangle, LOGICAL_RECTANGLE_COMPARATOR)).iterator();
            double lastYPos = first ? titleFragment.getRectangle().getLLY() : searchPage.getRect().getHeight() - headerMargin;
            Stream<LinkAnnotation> annotStream = StreamSupport.stream(searchPage.getAnnotations().spliterator(), false)
                    .filter(annot -> annot.getAnnotationType() == AnnotationType.Link)
                    .map(LinkAnnotation.class::cast);
            if (first) {
                annotStream = annotStream.filter(annot -> searchRect.isIntersect(annot.getRect()));
            }
            LinkAnnotation[] annots = annotStream.sorted(Comparator.comparing(Annotation::getRect, LOGICAL_RECTANGLE_COMPARATOR))
                    .toArray(LinkAnnotation[]::new);
            int annotIdx = 0;
            while (tfIt.hasNext()) {
                TextFragment curr = tfIt.next();
                if (curr.getRectangle().getURY() < lastYPos - lineSpaceMargin) {
                    break;
                }
                LinkAnnotation currAnnot = null;
                while (annotIdx < annots.length && annots[annotIdx].getRect().getURY() >= curr.getRectangle().getLLY()) {
                    Rectangle annotRect = annots[annotIdx].getRect();
                    annotRect.setLLX(annotRect.getLLX() - 1);
                    annotRect.setLLY(annotRect.getLLY() - 1);
                    annotRect.setURX(annotRect.getURX() + 1);
                    annotRect.setURY(annotRect.getURY() + 1);
                    if (annotRect.isInclude(curr.getRectangle(), 0)) {
                        currAnnot = annots[annotIdx];
                        annotIdx++;
                        break;
                    }
                    annotIdx++;
                }
                elements.add(new Element(curr, currAnnot));
                lastYPos = curr.getRectangle().getLLY();
            }

            if (lastYPos > footerMargin + lineSpaceMargin) {
                break;
            }

            PageCollection pages = searchPage.getDocument().getPages();
            if (searchPage.getNumber() == pages.size()) {
                break;
            }
            searchPage = pages.get_Item(searchPage.getNumber() + 1);
            tfa.reset();

            if (first) {
                tfa = new TextFragmentAbsorber(Element.SEARCH_PATTERN);
                first = false;
            }
        }

        this.elements = Collections.unmodifiableList(elements);
    }

    public static Stream<PdfToc> findAll(@Nonnull Document doc, @Nullable String tocTitleText, int tocDuplicationSearchLimit,
                                         int lineSpaceMargin, int headerMargin, int footerMargin) {
        // We're looking for "Table of content(s)" or a user supplied TOC title if it's any different,
        // all case-insensitive
        String regex = "(?i)Table of contents?";
        if (StringUtils.isNotBlank(tocTitleText) && !tocTitleText.matches(regex)) {
            regex += "|" + Pattern.quote(tocTitleText);
        }
        TextFragmentAbsorber tfa = new TextFragmentAbsorber(Pattern.compile(regex));
        Stream<Page> pageStream = StreamSupport.stream(doc.getPages().spliterator(), false);
        if (tocDuplicationSearchLimit > 0) {
            pageStream = pageStream.limit(tocDuplicationSearchLimit);
        }
        return pageStream.flatMap(page -> {
            page.accept(tfa);
            Spliterator<TextFragment> split = tfa.getTextFragments().spliterator();
            tfa.reset();
            return StreamSupport.stream(split, false);
        }).map(tf -> new PdfToc(tf, lineSpaceMargin, headerMargin, footerMargin));
    }

    public record Element(@Nonnull TextFragment textFragment, @Nullable LinkAnnotation annotation) {
        /**
         * Matches a single TOC element. For example:
         * 1.2.1  My cool chapter....................15
         * Upon matching, the following groups will be available (value for the example above is next to the arrow):
         * Group 1: The heading number -> 1.2.1
         * Group 2: The heading title -> My cool chapter
         * Group 3: The entire separation sequence (between heading title and page number, excluding spaces) -> ....................
         * Group 4: The (first) repetition/separation character that is encountered after the title -> .
         * Group 5: The page number -> 15
         */
        public static final Pattern SEARCH_PATTERN = Pattern.compile("^((?:\\d+\\.?)*)\\s*(.+?)\\s*((.)\\4*)\\s*(\\d+)$");
    }
}
