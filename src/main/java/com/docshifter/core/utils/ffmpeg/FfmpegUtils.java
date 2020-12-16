package com.docshifter.core.utils.ffmpeg;

import com.docshifter.core.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FfmpegUtils {

	//static public String ffmpegLocation = "C:/ffmpeg/bin/"; <-- really?
    static public Map<String,Object> executeInquiry( String filename, String ffmpegPath)
    {
    	Logger.debug("attempting to get the duration of the video", null);
        Map<String, Object> fieldMap = new HashMap<String,Object>();

        try
        {
            // Build the command line
            StringBuilder sb = new StringBuilder();
            sb.append( ffmpegPath );
            sb.append( " -i " );
            sb.append( filename );

            // Execute the command
            Process proc = Runtime.getRuntime().exec( sb.toString() );

            // Parse the  stderr stream
            List<String> outLines = getOutputLines(proc.getErrorStream());
            if (outLines.size() > 0) {
            	fieldMap = getFieldMap(outLines);
            }
            else {
            	Logger.warn("Did not get stderr as expected, trying stdout", null);
            	outLines = getOutputLines(proc.getInputStream());
            }
        	fieldMap = getFieldMap(outLines);
            proc.destroy();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return fieldMap;
    }
    
    /**
     * Gets the output (stdout or stderr) as a List of Strings
     * @param strm An input stream (normally stdout or stderr of a command)
     * @return List<String> The lines of (error) output
     * @throws IOException
     * TODO: Could probably be factored out to a more general class?
     */
    public static List<String> getOutputLines(InputStream strm) throws IOException {
    	BufferedReader output = new BufferedReader( new InputStreamReader( strm ) );
    	List<String> outLines = new ArrayList<>();
        Logger.debug("starting loop", null);
        String line = output.readLine();
        while( line != null) {
            // Handle the line
            outLines.add(line);
            // Read the next line
            line = output.readLine();
        }
    	return outLines;
    }

    /**
     * Parse the output lines to get a duration and bitrate returned by the ffmpeg command
     * @param outLines A List of Strings of the output to look through
     * @return Map of String to Object containing duration and bitrate
     * @throws IOException
     */
    private static Map<String, Object> getFieldMap(List<String> outLines) {
    	Map<String, Object> fieldMap = new HashMap<>();
        Logger.debug("starting loop", null);
        for( String line : outLines)
        {
            // Handle the line
            if( line.startsWith( "FFmpeg version" ) )
            {
                // Handle the version line:
                //    FFmpeg version 0.6.2-4:0.6.2-1ubuntu1, Copyright (c) 2000-2010 the Libav developers
                String version = line.substring( 15, line.indexOf( ", Copyright", 16  ) );
                fieldMap.put( "version", version );
            }
            else if( line.indexOf( "Duration:" ) != -1 )
            {
                // Handle Duration line:
                //    Duration: 00:42:53.59, start: 0.000000, bitrate: 1136 kb/s
                String duration = line.substring( line.indexOf( "Duration: " ) + 10, line.indexOf( ", start:" ) );
                fieldMap.put("duration", getSeconds(duration));
            
                String bitrate = line.substring( line.indexOf( "bitrate: " ) + 9 );
                fieldMap.put( "bitrate", bitrate );
                Logger.debug("breaking loop, found duration: " + fieldMap.get("duration"), null);
                break;
            }
        }
    	return fieldMap;
    }

	private static Double getSeconds(String duration) {
		String[] split = duration.split(":");
		Double[] doubles = new Double[3];
		for(int i = 0; i < 3; i++)
		{
			doubles[i] = Double.parseDouble(split[i]);
		}
		doubles[1] += doubles[0] * 60;
		doubles[2] += doubles[1] * 60;
		return doubles[2];
	}
}
