package com.docshifter.core.operations;

import com.docshifter.core.exceptions.ConfigFileNotFoundException;
import com.docshifter.core.exceptions.InputCorruptException;
import com.docshifter.core.exceptions.InvalidConfigException;
import com.docshifter.core.logging.appenders.TaskMessageAppender;
import com.docshifter.core.task.TaskStatus;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.asposehelper.LicenseHelper;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.task.Task;
import com.docshifter.core.operations.annotations.ModuleParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
        DirectoryHandling directoryHandling;
        if (!Files.isDirectory(operationParams.getSourcePath()) || (directoryHandling = getDirectoryHandling()) == DirectoryHandling.AS_IS) {
            try {
                return execute();
            } catch (InvalidConfigException | ConfigFileNotFoundException ex) {
                log.error("The module indicated an invalid configuration", ex);
                operationParams.setSuccess(TaskStatus.BAD_CONFIG);
                return operationParams;
            } catch (InputCorruptException ex) {
                log.error("The module indicated bad input", ex);
                operationParams.setSuccess(TaskStatus.BAD_INPUT);
                return operationParams;
            } catch (Exception ex) {
                log.error("The module indicated a failure", ex);
                operationParams.setSuccess(TaskStatus.FAILURE);
                return operationParams;
            } finally {
                cleanup();
            }
        } else {
            Map<Path, List<Path>> groupedPaths;
            try (Stream<Path> stream = Files.walk(operationParams.getSourcePath())) {
                groupedPaths = stream.filter(Files::isRegularFile)
                        .collect(Collectors.groupingBy(path -> operationParams.getSourcePath().relativize(path.getParent())));
            } catch (Exception ex) {
                log.error("Error while walking source path of operation: {}", operationParams.getSourcePath(), ex);
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
            AtomicReference<OperationParams> mergedResult = new AtomicReference<>(operationParams);
            String thisOperation = getClass().getName();
            OperationParams result = handleResult(groupedPathStream.map(groupedPath -> {
                        Stream<Path> fileStream = directoryHandling == DirectoryHandling.PARALLEL_FOREACH ?
                                groupedPath.getValue().parallelStream() : groupedPath.getValue().stream();
                        return handleResult(fileStream.map(path -> {
                            if (directoryHandling == DirectoryHandling.PARALLEL_FOREACH) {
                                TaskMessageAppender.registerCurrentThread(task);
                            }
                            OperationParams fileOperationParams = (OperationParams) operationParams.clone();
                            fileOperationParams.setSourcePath(path);
                            OperationParams res;
                            try {
                                res = getOperation(thisOperation).execute(task, fileOperationParams, failureLevel);
                                if (res.isSuccess()) {
                                    Files.move(res.getResultPath(), folder.resolve(groupedPath.getKey()).resolve(res.getResultPath().getFileName()));
                                }
                            } catch (Exception ex) {
                                return mergedResult.updateAndGet(curr -> curr.merge(TaskStatus.FAILURE));
                            }
                            return mergedResult.updateAndGet(curr -> curr.merge(res));
                        }), mergedResult, failureLevel != FailureLevel.FILE);
                    }), mergedResult, failureLevel != FailureLevel.FILE && failureLevel != FailureLevel.GROUP);
            if (result.isSuccess()) {
                result.setResultPath(folder);
            }
            return result;
        }
    }

    private OperationParams handleResult(Stream<OperationParams> resultStream,
                                         AtomicReference<OperationParams> mergedResult, boolean shortCircuitOnFailure) {
        if (shortCircuitOnFailure) {
            // TODO: interrupt running threads when we short-circuit?
            return resultStream.filter(res -> !res.isSuccess())
                    .findAny()
                    .orElse(mergedResult.get());
        }
        return resultStream.reduce((first, second) -> second).orElse(mergedResult.get());
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

        if (operation instanceof AbstractOperation) {
            return (AbstractOperation) operation;
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
}
