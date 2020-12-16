package com.docshifter.core.utils.ffmpeg;

import java.io.IOException;
import java.io.InputStream;

public class StreamReader extends Thread {

	InputStream is;
	public StreamReader(InputStream is)
	{
		this.is = is;
		
	}
	
	public void run()
	{
		int previous = 0;
		while(previous != -1)
		{
			try {
				previous = is.read();
			}
			catch (IOException e) {
			}
		}
		
	}
}
