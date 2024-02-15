package com.docshifter.core.metrics.samples;

public interface FilePageCountDistributionSample {
   String getExtension();
   String getInputExtensions();
   long getCount();
}
