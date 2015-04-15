package com.docbyte.docshifter.config.test;

import com.docbyte.docshifter.model.vo.Node;

public interface NodeCallable {

	public void call(Node n);
	
	public void enteringChildNodes();

	public void exitingChildNodes();
	
}
