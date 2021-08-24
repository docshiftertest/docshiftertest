package com.docshifter.core.metrics.Sample;

/**
 * @author Created by Juan Marques on 23/08/2021
 */
public interface FileTypeDistributionSample {

   Long getFileSize();
   Long getOnMessageHit();
   String getFilename();
}
