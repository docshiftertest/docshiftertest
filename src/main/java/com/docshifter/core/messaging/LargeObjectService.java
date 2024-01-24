package com.docshifter.core.messaging;

import com.docshifter.core.task.FileMetadataDTO;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Service for managing large objects in PostgresSQL.
 */
@Service
@Log4j2
public class LargeObjectService {

    private final DataSource dataSource;

    private AtomicInteger directoryIdCounter;

    public LargeObjectService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Saves a large object (LOB) in the database.
     *
     * @param basePathString the path of the large object or directory
     * @return a list of OIDs of the newly created large objects
     */
    public List<FileMetadataDTO> saveLargeObject(String basePathString) {

        Path basePath = Paths.get(basePathString);
        directoryIdCounter = new AtomicInteger();
        List<FileMetadataDTO> fileMetadataList = new ArrayList<>();

        if (Files.isDirectory(basePath)) {
            Integer dirId = getNextDirectoryId();
            processDirectory(basePath, dirId, fileMetadataList);
        } else {
            processFile(basePath, null, fileMetadataList);
        }

        return fileMetadataList;
    }

    @SneakyThrows
    private void processDirectory(Path directory, Integer parentId, List<FileMetadataDTO> fileMetadataList) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.forEach(path -> {
                if (Files.isDirectory(path) && !path.equals(directory)) {
                    Integer dirId = getNextDirectoryId();
                    processDirectory(path, dirId, fileMetadataList);
                } else if (Files.isRegularFile(path)) {
                    processFile(path, parentId, fileMetadataList);
                }
            });
        }
    }

    private void processFile(Path file, Integer parentId, List<FileMetadataDTO> fileMetadataList) {
        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            String relativePath = file.getParent().relativize(file).toString();
            Long oid = saveFile(inputStream);
            fileMetadataList.add(new FileMetadataDTO(oid, file.toString(), parentId, relativePath));
        } catch (SQLException | IOException e) {
            log.error("Error saving file: {} ", file, e);
        }
    }

    /**
     * Saves a file as a large object (LOB) in the database and stores its metadata.
     *
     * @param inputStream the input stream of the file to save
     * @return the OID of the newly created large object
     * @throws SQLException if a database access error occurs
     * @throws IOException  if an I/O error occurs
     */
    private Long saveFile(InputStream inputStream) throws SQLException, IOException {
        long oid;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PGConnection pgConn = conn.unwrap(PGConnection.class);
                LargeObjectManager lobj = pgConn.getLargeObjectAPI();
                oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);

                try (LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE)) {
                    byte[] buffer = new byte[2048];
                    int s;
                    while ((s = inputStream.read(buffer)) != -1) {
                        obj.write(buffer, 0, s);
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return oid;
    }

    public void processLargeObject(Long lobId, Path destinationPath) throws Exception {
        try (LargeObjectWrapper lobWrapper = readLargeObject(lobId);
             InputStream inputStream = lobWrapper.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationPath)) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }


    /**
     * Reads a large object from the database.
     *
     * @param lobId the OID of the large object
     * @return an InputStream to read the large object
     * @throws SQLException if a database access error occurs
     */
    private LargeObjectWrapper readLargeObject(Long lobId) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false); // Disable auto-commit to manage transaction manually

        PGConnection pgConn = conn.unwrap(PGConnection.class);
        LargeObjectManager lobj = pgConn.getLargeObjectAPI();
        LargeObject obj = lobj.open(lobId, LargeObjectManager.READ);
        InputStream inputStream = obj.getInputStream();

        return new LargeObjectWrapper(inputStream, conn, obj);
    }


    /**
     * Deletes a large object from the database.
     *
     * @param lobId the OID of the large object to delete
     */
    @SneakyThrows
    public void deleteLargeObject(Long lobId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PGConnection pgConn = conn.unwrap(PGConnection.class);
                LargeObjectManager lobj = pgConn.getLargeObjectAPI();
                lobj.unlink(lobId);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public int getNextDirectoryId() {
        return directoryIdCounter.incrementAndGet();
    }
}
