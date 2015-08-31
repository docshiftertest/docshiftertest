package com.docbyte.docshifter.messaging.queue.receiver;

import com.docbyte.docshifter.messaging.IDocShifterController;


public class JMSReceiver extends AbstractJMSReceiver implements IMessageReceiver {
	boolean initDone=false;
	
	private static IMessageReceiver instance = null;
	
	private synchronized static void createInstance (IDocShifterController docShifterController) {
        if (instance == null){ 
        	instance = new JMSReceiver(docShifterController);
        }
    }
	 
    public static IMessageReceiver getInstance (IDocShifterController docShifterController) {
        if (instance == null){
        	createInstance (docShifterController);
        }
        return instance;
    }
	
	private JMSReceiver(IDocShifterController docShifterController) {
		if (docShifterController==null)
			throw new IllegalArgumentException("controller can not be null");
		setController(docShifterController);
		setQueueNameSuffix("printServiceReceiver");
	}
	
	public JMSReceiver(IDocShifterController docShifterController, String queueNameSuffix) {
		if (docShifterController==null)
			throw new IllegalArgumentException("controller can not be null");
		setController(docShifterController);
		if (queueNameSuffix == null)
			setQueueNameSuffix("");
		else
			setQueueNameSuffix(queueNameSuffix);
	}
	
}
