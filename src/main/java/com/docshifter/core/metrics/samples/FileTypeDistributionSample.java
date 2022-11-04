package com.docshifter.core.metrics.samples;

/**
 * @author Created by Juan Marques on 23/08/2021
 */
public interface FileTypeDistributionSample {
   String getExtension();
   long getCount();
}
