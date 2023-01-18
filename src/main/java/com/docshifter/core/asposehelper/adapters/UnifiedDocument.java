package com.docshifter.core.asposehelper.adapters;

import java.io.Closeable;
import java.util.stream.Stream;

public interface UnifiedDocument extends Closeable {
	Stream<UnifiedPage> getPages();
}
