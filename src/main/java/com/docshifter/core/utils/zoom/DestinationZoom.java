package com.docshifter.core.utils.zoom;

import com.aspose.pdf.AnnotationType;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.LinkAnnotation;
import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Page;
import com.docshifter.core.asposehelper.utils.pdf.AnnotationUtils;
import com.docshifter.core.asposehelper.utils.pdf.ExplicitDestinationTransformer;
import com.docshifter.core.asposehelper.utils.pdf.OutlineUtils;
import com.docshifter.core.exceptions.InvalidConfigException;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Special PDF zoom presets and an extra field to signal that zoom values should not be changed.
 */
public enum DestinationZoom {

    /**
     * Don't change the zoom preset/value at all. Note: this just means the explicit destinations stored in the PDF
     * document should be kept as is! It does not mean "inherit zoom" as that is not even a preset at all but simply
     * a custom zoom value that is set to "0".
     */
    KEEP_AS_IS,

    /**
     * Fit the entire page inside the view.
     */
    FIT_PAGE,

    /**
     * Fit the entire width of the page inside the view.
     */
    FIT_WIDTH,

    /**
     * Fit what's visible on the page (a.k.a. the bounding box) inside the view.
     */
    FIT_VISIBLE;

    /**
     * Gets the function according to the zoomLevel provided
     * @param zoomLevel the zoom level choice
     * @return the function using as argument an {@link ExplicitDestinationTransformer} and as result an {@link ExplicitDestination}
     * @throws InvalidConfigException throws it if it is not possible to convert the value provided
     */
    @Nullable
    public static Function<ExplicitDestinationTransformer, ExplicitDestination> getFunction(String zoomLevel) throws InvalidConfigException {
        Function<ExplicitDestinationTransformer, ExplicitDestination> operation;
        DestinationZoom presetZoom = EnumUtils.getEnumIgnoreCase(DestinationZoom.class, zoomLevel);
        if (presetZoom == null) {
            try {
                double customZoom = Double.parseDouble(zoomLevel);
                operation = transformer -> transformer.toCustomZoom(customZoom);
            }
            catch (Exception exception) {
                throw new InvalidConfigException("Tried to interpret value " + zoomLevel + " as custom zoom factor for " +
                        "forceDestinationZoom but parsing of this value failed.", exception);
            }
        }
        else {
            operation = switch (presetZoom) {
                case FIT_PAGE -> ExplicitDestinationTransformer::toFit;
                case FIT_WIDTH -> ExplicitDestinationTransformer::toFitH;
                case FIT_VISIBLE -> ExplicitDestinationTransformer::toFitBH;
                default -> null;
            };
        }

        return operation;
    }

    /**
     * Updates the destination of the bookmarks
     * @param outlines the {@link OutlineItemCollection} to iterate and update
     * @param operation the function using as argument an {@link ExplicitDestinationTransformer} and as result an {@link ExplicitDestination}
     */
    public static void updateOutlineDestinations(Iterable<OutlineItemCollection> outlines,
                                                  Function<ExplicitDestinationTransformer, ExplicitDestination> operation) {

        for (OutlineItemCollection oic : outlines) {

            ExplicitDestination dest = OutlineUtils.extractExplicitDestinationHard(oic);

            if (dest != null) {
                dest = operation.apply(ExplicitDestinationTransformer.create(dest));
                oic.setDestination(dest);
            }

            updateOutlineDestinations(oic, operation);
        }
    }

    /**
     * Updates the destination of the annotation links
     * @param doc the {@link IDocument} to iterate and update
     * @param operation the function using as argument an {@link ExplicitDestinationTransformer} and as result an {@link ExplicitDestination}
     */
    public static void updateAnnotationLink(IDocument doc, Function<ExplicitDestinationTransformer, ExplicitDestination> operation) {

        for (Page page : doc.getPages()) {
            StreamSupport.stream(page.getAnnotations().spliterator(), false)
                    .filter(annot -> annot.getAnnotationType() == AnnotationType.Link)
                    .map(LinkAnnotation.class::cast)
                    .forEach(annot -> {

                        ExplicitDestination dest = AnnotationUtils.extractExplicitDestinationHard(annot);

                        if (dest != null) {
                            dest = operation.apply(ExplicitDestinationTransformer.create(dest));
                            annot.setDestination(dest);
                        }
                    });
        }
    }

}
