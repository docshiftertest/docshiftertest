package com.docshifter.core.operations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by samnang.nop on 7/09/2016.
 */
public class OperationParams implements Cloneable {

    private Path sourcePath;
    private Path resultPath = null;
    private Map<String, Object> parameters = new HashMap<>();
    private boolean success = false;

    public OperationParams(Path sourcePath){
        this.sourcePath = sourcePath;
    }

    public OperationParams(Path sourcePath, Path resultPath, Map<String, Object> parameters, boolean success) {
        this.sourcePath = sourcePath;
        this.resultPath = resultPath;
        this.parameters = parameters;
        this.success = success;
    }
    
    public OperationParams(OperationParams operationParams) {
        this.sourcePath = operationParams.sourcePath;
        this.resultPath = operationParams.resultPath;
        this.parameters = (Map<String, Object>) ((HashMap<String, Object>)operationParams.parameters).clone();
        this.success = operationParams.success;
    }
    
    public void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setResultPath(Path resultPath) {
        this.resultPath = resultPath;
    }

    public Path getResultPath() {
        return resultPath;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void addParameter(String name, Object o) {
        parameters.put(name, o);
    }

    public Object getParameter(String name) {return parameters.get(name);}

    public void setParameters(Map parameters){
        this.parameters = parameters;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    

    @Override
    public Object clone() {
        return new OperationParams(this);
    }

    public boolean contains(String parameter) {
    	return this.parameters.containsKey(parameter);
    }

    @Override
    public String toString() {
    	StringBuilder sBuf = new StringBuilder();
    	sBuf.append("OperationParams={");
    	sBuf.append("sourcePath=");
    	sBuf.append(sourcePath);
    	sBuf.append(", resultPath=");
    	sBuf.append(resultPath);
    	sBuf.append(", success=");
    	sBuf.append(success);
    	sBuf.append(", parameters=");
    	if (parameters == null) {
    		sBuf.append("{NULL}");
    	}
    	else {
    		Object value;
    		sBuf.append("{");
    		for (String key: parameters.keySet()) {
    			sBuf.append(key);
    			sBuf.append("=");
    			value = parameters.get(key);
    			if (value == null) {
    				sBuf.append("NULL");
    			}
    			else {
    				sBuf.append(value.toString());
    			}
    			sBuf.append(", ");
    		}
    		if (sBuf.toString().endsWith(", ")) {
    			sBuf.setLength(sBuf.length() - 2);
    		}
    		sBuf.append("}");
    	}
    	sBuf.append("}");
    	return sBuf.toString();
    }
}
