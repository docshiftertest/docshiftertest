package com.docshifter.core.operations;

import com.docshifter.core.exceptions.ConfigFileNotFoundException;
import com.docshifter.core.exceptions.InputCorruptException;
import com.docshifter.core.exceptions.InputRejectedException;
import com.docshifter.core.exceptions.InvalidConfigException;
import com.docshifter.core.exceptions.UnsupportedInputFormatException;
import com.docshifter.core.logging.appenders.TaskMessageAppender;
import com.docshifter.core.task.TaskStatus;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.asposehelper.LicenseHelper;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.task.Task;
import com.docshifter.core.operations.annotations.ModuleParam;
import com.docshifter.core.utils.TaskDataKey;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by samnang.nop on 5/09/2016.
 */

@Log4j2
public abstract class AbstractOperation extends ModuleOperation {

    private final static String TEMP_DIR = System.getProperty("java.io.tmpdir");

	@ModuleParam(required=false)
	protected String tempFolder;

    private Path rootSourcePath;

    protected Map<String, Object> moduleData = new HashMap<>();

    protected String operation = "Abstract Operation";

    public String getModuleId() {
        return "CSTM";
    }

    public Map<String, Object> getModuleData(){
        return moduleData;
    }

    //Use this execute-method for transformation directly from workflow configuration
    public final OperationParams execute(Task task, ModuleWrapper moduleWrapper, OperationParams operationParams,
                                         FailureLevel failureLevel) {
        this.moduleWrapper = moduleWrapper;
        return execute(task, operationParams, failureLevel);
    }

    //Use these execute-methods for transformation called within a transformation
    //Use the setters to fill up the parameters
    public final OperationParams execute (Task task, OperationParams operationParams) {
        return execute(task, operationParams, FailureLevel.FILE);
    }

    public final OperationParams execute(Task task, OperationParams operationParams, FailureLevel failureLevel) {
        this.task = task;
        this.operationParams = operationParams;
        LicenseHelper.getLicenseHelper();
        if (StringUtils.isBlank(tempFolder)) {
            tempFolder = TEMP_DIR;
        }
        log.info("Executing operation: {}", operation);
        boolean valid = fillInParameters();
        if (!valid) {
            operationParams.setSuccess(TaskStatus.FAILURE);
            return operationParams;
        }

        Path inFilePath = operationParams.getSourcePath();
        if (inFilePath != null && !Files.exists(inFilePath)) {
            log.error("{} does not exist!", inFilePath);
            operationParams.setSuccess(TaskStatus.BAD_INPUT);
            return operationParams;
        }

        DirectoryHandling directoryHandling;
        if (inFilePath == null || !Files.isDirectory(inFilePath) || (directoryHandling = getDirectoryHandling()) == DirectoryHandling.AS_IS) {
            if (rootSourcePath == null) {
                rootSourcePath = inFilePath;
            }
            try {
                return execute();
            } catch (InvalidConfigException ex) {
                log.error("The module indicated an invalid configuration", ex);
                operationParams.setSuccess(TaskStatus.BAD_CONFIG);
                return operationParams;
            } catch (ConfigFileNotFoundException ex) {
                log.error("The module indicated that it could not find a configuration file", ex);
                operationParams.setSuccess(TaskStatus.BAD_CONFIG);
                return operationParams;
            } catch (UnsupportedInputFormatException ex) {
                log.error("The module encountered an input format it cannot handle", ex);
                operationParams.setSuccess(TaskStatus.BAD_INPUT);
                return operationParams;
            } catch (InputRejectedException ex) {
                log.error("The module indicated that an input was rejected (possibly due to configuration)", ex);
                operationParams.setSuccess(TaskStatus.BAD_INPUT);
                return operationParams;
            } catch (InputCorruptException ex) {
                log.error("The module indicated bad/corrupt input", ex);
                operationParams.setSuccess(TaskStatus.BAD_INPUT);
                return operationParams;
            } catch (InterruptedException ex) {
                log.error("The task has reached its configured timeout value", ex);
                operationParams.setSuccess(TaskStatus.TIMED_OUT);
                Thread.currentThread().interrupt();
                return operationParams;
            } catch (TimeoutException ex) {
                log.error("The module performed an operation that timed out", ex);
                operationParams.setSuccess(TaskStatus.TIMED_OUT);
                return operationParams;
            } catch (Exception ex) {
                log.error("The module indicated a failure", ex);
                operationParams.setSuccess(TaskStatus.FAILURE);
                return operationParams;
            } finally {
                rootSourcePath = null;
                cleanup();
            }
        } else {
            // File groups are important to keep track of for the failure level that was set. Furthermore, groups
            // help several modules in differentiating which input is similar to one another or originated from a
            // single ancestor file (but was then split in multiple split files by the Splitter module for example).
            // Here we walk through all files in the directory hierarchy but keep them grouped according to their
            // parent path.
            Map<Path, List<Path>> groupedPaths;
            try (Stream<Path> stream = Files.walk(inFilePath)) {
                groupedPaths = stream.filter(Files::isRegularFile)
                        .collect(Collectors.groupingBy(path -> inFilePath.relativize(path.getParent())));
            } catch (Exception ex) {
                log.error("Error while walking source path of operation: {}", inFilePath, ex);
                operationParams.setSuccess(TaskStatus.FAILURE);
                return operationParams;
            }

            if (groupedPaths.isEmpty()) {
                log.error("Got a directory as input, but it seems to be empty!");
                operationParams.setSuccess(TaskStatus.BAD_INPUT);
                return operationParams;
            }

            Stream<Map.Entry<Path, List<Path>>> groupedPathStream =
                    directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
                            groupedPaths.entrySet().parallelStream() : groupedPaths.entrySet().stream();
            Path folder = task.getWorkFolder().getNewFolderPath();
            WrappedOperationParams mergedResult = WrappedOperationParams.fromOperationParams(operationParams);
            String thisOperation = getClass().getName();
            // Handle the results for all groups
            OperationParams result = handleResult(groupedPathStream.map(groupedPath -> {
                        Stream<Path> fileStream = directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
                                groupedPath.getValue().parallelStream() : groupedPath.getValue().stream();
                        // ...And the results for each file in the group
                        return handleResult(fileStream.map(path -> {
                            if (directoryHandling == DirectoryHandling.PARALLEL_FOREACH) {
                                TaskMessageAppender.registerCurrentThread(task);
                            }
                            OperationParams fileOperationParams = new OperationParams(operationParams);
                            fileOperationParams.setSourcePath(path);
                            OperationParams res = null;
                            try {
                                AbstractOperation op = getOperation(thisOperation);
                                if (op == this) {
                                    throw new IllegalStateException("A module should not be registered as a " +
                                            "singleton scoped bean in order to use automatic directory handling, " +
                                            "otherwise unintended behavior might happen.");
                                }
                                op.rootSourcePath = inFilePath;
                                res = op.execute(task, fileOperationParams, failureLevel);
                                // Never move over the result paths if the current module is of type RELEASE. We
                                // never have any modules following such a module and moving over files might result
                                // in the output being moved to somewhere the user doesn't want it (e.g. in case of
                                // FSExport).
                                if (res.isSuccess()) {

                                    if (moduleWrapper == null
                                            || !moduleWrapper.getType().equalsIgnoreCase("release")) {
                                        Path newGroupedPath = folder.resolve(groupedPath.getKey());
                                        Files.createDirectories(newGroupedPath);
                                        Files.move(res.getResultPath(), newGroupedPath.resolve(res.getResultPath().getFileName()));
                                    }

                                    try {
                                        addOutputFilePath(task, res);
                                    }
                                    catch (Exception exception) {
                                        log.warn("It was not possible to add the file path to the output file path list.", exception);
                                    }
                                }
                            }
                            catch (Exception ex) {
                                log.error("Got an exception while trying to process a nested operation.", ex);
                                if (res == null) {
                                    res = fileOperationParams;
                                } else {
                                    log.error("...But the nested operation did appear to return a result. So we got " +
                                            "an exception while trying to move over the result path?");
                                }
                                res.setSuccess(TaskStatus.FAILURE);
                            }
                            mergedResult.wrap(res);
                            return mergedResult;
                        }), mergedResult, failureLevel.isHigherThan(FailureLevel.FILE)); // No need to process other
                // files if the failure level is higher.
            }), mergedResult, failureLevel.isHigherThan(FailureLevel.GROUP)); // No need to process other

            // groups if the failure level is higher.
            if (result.isSuccess()) {
                result.setResultPath(folder);
            }
            return result;
        }
    }

    /**
     * Adds the path to the file if in a release module to the outputFilePath set of files
     *
     * @param task the {@link Task} in use
     * @param res the {@link OperationParams} result
     */
    private void addOutputFilePath(Task task, OperationParams res) {

        boolean isCountAllowed = task.getData().containsKey(TaskDataKey.COUNT_ALLOWED.toString())
                && ((boolean) task.getData().get(TaskDataKey.COUNT_ALLOWED.toString()));

        // We just do it id it is a release module and the COUNT_ALLOWED option is true
        if (moduleWrapper.getType().equalsIgnoreCase("release")
                && isCountAllowed) {

            Map<String, Object> taskData = task.getData();

            if (!taskData.containsKey(TaskDataKey.OUTPUT_FILE_PATH.toString())) {
                taskData.put(TaskDataKey.OUTPUT_FILE_PATH.toString(), new HashSet<String>());
            }

            Set<String> outputFilePathSet = (Set<String>) taskData.get(TaskDataKey.OUTPUT_FILE_PATH.toString());

            log.debug("Adding the file: [{}] to the outputFilePath", res.getSourcePath().toString());

            outputFilePathSet.add(res.getSourcePath().toString());
        }
    }

    /**
     * Collects results of a {@link Stream<OperationParams>} into a single {@link OperationParams}, depending on
     * whether we need an early return (short-circuit) after failure or not.
     * @param resultStream The {@link Stream<OperationParams>} of results to handle.
     * @param mergedResult A
     * @param shortCircuitOnFailure Whether to return early after a failure.
     * @return
     */
    private OperationParams handleResult(Stream<OperationParams> resultStream,
                                         WrappedOperationParams mergedResult, boolean shortCircuitOnFailure) {
        if (shortCircuitOnFailure) {
            // TODO: interrupt running threads when we short-circuit?
            return resultStream.filter(res -> !res.isSuccess())
                    .findAny()
                    .orElse(mergedResult);
        }
        return resultStream.reduce((first, second) -> second).orElse(mergedResult);
    }

    protected abstract OperationParams execute() throws Exception;

    //Always use this method to get the appropriate rendername.
    public String getRenderFilename(Path inFilePath, OperationParams operationParams) {
        String name = FileUtils.getNameWithoutExtension(inFilePath.getFileName().toString());
        String tempName = (String) operationParams.getParameter("renderfilename");
        if (tempName != null) {
            if (!tempName.isEmpty()) {
                name = tempName;
            }
        }
        return name;
    }


    public static AbstractOperation getOperation(String op) throws EmptyOperationException {
    	log.debug("Into getOperation with op: {}", op);
        ModuleOperation operation = ModuleOperation.getModuleOperation(op);
        if (operation == null) {
        	log.warn("Returned operation was NULL for op: {}", op);
        }
        else {
        	log.debug("operation is: {}", operation);
        }

        if (operation instanceof AbstractOperation abstractOperation) {
            return abstractOperation;
        } else {
            log.error("Incorrect operation [{}], please check your configuration", op);
            throw new EmptyOperationException();
        }

    }


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ModuleWrapper getModuleWrapper() {
        return moduleWrapper;
    }

    public void setModuleWrapper(ModuleWrapper moduleWrapper) {
        this.moduleWrapper = moduleWrapper;
    }

    public OperationParams getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(OperationParams operationParams) {
        this.operationParams = operationParams;
    }

    public String toString() {
        return operation;
    }

    public boolean cacheContext() {
        return true;
    }

    /**
     * Returns whether the operation being executed is a nested one (when handling directory input). This can be
     * useful to check if resulting files need to be named in a different way for example.
     * @return
     */
    protected final boolean isNestedOperation() {
        return !Objects.equals(rootSourcePath, operationParams.getSourcePath());
    }

    /**
     * If we're in a nested operation, returns the source path (i.e. a directory) that the main operation received.
     * Otherwise, this always equals the path returned by {@link OperationParams#getSourcePath()} of this instance's
     * {@code operationParams}.
     * @return The source path as received by the main operation.
     */
    protected final Path getRootSourcePath() {
        return rootSourcePath;
    }
}
