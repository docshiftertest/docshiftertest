package com.docshifter.core.task;

import java.io.Serializable;

public record FileMetadataDTO(Long lobId, String filePath, Integer parentId, String relativePath)
        implements Serializable {
}
