package com.docshifter.core.messaging;

import lombok.Getter;
import org.postgresql.largeobject.LargeObject;

import java.io.InputStream;
import java.sql.Connection;

public class LargeObjectWrapper implements AutoCloseable {
    @Getter
    private final InputStream inputStream;
    private final Connection connection;
    private final LargeObject largeObject;

    public LargeObjectWrapper(InputStream inputStream, Connection connection, LargeObject largeObject) {
        this.inputStream = inputStream;
        this.connection = connection;
        this.largeObject = largeObject;
    }

    @Override
    public void close() throws Exception {
        largeObject.close();  // Close the large object
        connection.commit();  // Commit the transaction
        connection.setAutoCommit(true);
        connection.close();   // Close the connection
    }
}

