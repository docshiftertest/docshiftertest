package com.docshifter.core.operations;

import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.asposehelper.LicenseHelper;
import com.docshifter.core.config.wrapper.ModuleWrapper;
import com.docshifter.core.exceptions.EmptyOperationException;
import com.docshifter.core.task.Task;
import com.docshifter.core.operations.annotations.ModuleParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
    public OperationParams execute(Task task, ModuleWrapper moduleWrapper, OperationParams operationParams) {
        this.task = task;
        this.moduleWrapper = moduleWrapper;
        this.operationParams = operationParams;
        boolean valid = fillInParameters();
        LicenseHelper.getLicenseHelper();
		if (StringUtils.isBlank(tempFolder)) {
			tempFolder = TEMP_DIR;
		}
        log.info("Executing operation: {}", operation);
        if (valid) {
            return execute();
        }
        operationParams.setSuccess(false);
        return operationParams;
    }

    //Use this execute-method for transformation called within a transformation
    //Use the setters to fill up the parameters
    public OperationParams execute(Task task, OperationParams operationParams) {
        this.task = task;
        this.operationParams = operationParams;
        LicenseHelper.getLicenseHelper();
        log.info("Executing operation: {}", operation);
        boolean valid = fillInParameters();
        if (valid) {
            return execute();
        }
        operationParams.setSuccess(false);
        return operationParams;
    }

    public abstract OperationParams execute();

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
