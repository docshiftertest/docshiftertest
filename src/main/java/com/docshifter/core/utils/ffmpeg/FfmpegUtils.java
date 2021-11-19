package com.docshifter.core.utils.ffmpeg;

import com.docshifter.core.utils.CLIUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.NoClass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class FfmpegUtils {

    public static final String CODEC_TYPE_VIDEO = "video";
    public static final String CODEC_TYPE_AUDIO = "audio";

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
     * FfprobeResult is List of video and audio streams in a media file, followed by some information about the format of the file containing the streams
     */
    public static class FfprobeResult {
        List<FfprobeStream> streams;
        Format format;
    }

    @Getter
    @Setter
    /**
     * Format information provided by ffprobe
     */
    public static class Format {
        private String filename;
        private Integer nbStreams;
        private Integer nbPrograms;
        private String formatName;
        private String formatLongName;
        private Float startTime;
        private Float duration;
        private Long size;
        private Integer bitRate;
        private Integer probeScore;
        private Map<String, String> tags;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "codec_type", defaultImpl = NoClass.class)
    @JsonSubTypes({
            // Create a VideoStream if the codecType is 'video'
            @JsonSubTypes.Type(value = FfprobeStream.VideoStream.class, name = CODEC_TYPE_VIDEO),
            // Create an AudioStream if the codecType is 'audio'
            @JsonSubTypes.Type(value = FfprobeStream.AudioStream.class, name = CODEC_TYPE_AUDIO)
    })

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @ToString
    public abstract static class FfprobeStream {
        private Integer index;
        private String codecName;
        private String codecLongName;
        protected String codecType;
        private String codecTagString;
        private String codecTag;
        private Integer bitRate;
        private Integer nbFrames;
        private Integer startPts;
        private Float startTime;
        private String timeBase;
        private String rFrameRate;
        private String avgFrameRate;
        private Disposition disposition;

        public abstract String getCodecType();

        @Getter
        @Setter
        @ToString(callSuper = true)
        public static class VideoStream extends FfprobeStream {
            private Integer width;
            private Integer height;
            private Integer codedWidth;
            private Integer codedHeight;
            private Integer closedCaptions;
            private Integer filmGrain;
            private Integer hasBFrames;
            private String sampleAspectRatio;
            private String displayAspectRatio;
            private String pixFmt;
            private Integer level;
            private Integer refs;
            private Long durationTs;
            private Float duration;

            @Override
            public String getCodecType() {
                return CODEC_TYPE_VIDEO;
            }
        }

        @Getter
        @Setter
        @ToString(callSuper = true)
        public static class AudioStream extends FfprobeStream {
            private String sampleFmt;
            private String sampleRate;
            private Integer channels;
            private Integer bitsPerSample;

            @Override
            public String getCodecType() {
                return CODEC_TYPE_AUDIO;
            }
        }

        @Getter
        @Setter
        @ToString
        /**
         * Disposition is available for Audio and Video Streams and is the same, so doesn't need subclassing
         */
        public static class Disposition {
            // default is a reserved word
            @JsonProperty("default")
            private Integer defaultInt;
            private Integer dub;
            private Integer original;
            private Integer comment;
            private Integer lyrics;
            private Integer karaoke;
            private Integer forced;
            private Integer hearingImpaired;
            private Integer visualImpaired;
            private Integer cleanEffects;
            private Integer attachedPic;
            private Integer timedThumbnails;
            private Integer captions;
            private Integer descriptions;
            private Integer metadata;
            private Integer dependent;
            private Integer stillImage;

        }
    }

    /**
     * Uses ffprobe to probe a media file and get information about the audio and video streams in the file and the format information
     * May return null if we can't get the output from the command, or the JSON can't be parsed for some reason
     * @param ffprobePath Needs the full path the executable ffprobe (can just be ffprobe if you want to use the system-installed ffprobe)
     * @param filename String representing the path to the media file to be probed
     * @return An FfprobeResult object, or null
     */
    public static FfprobeResult probeMediaFile(String ffprobePath, String filename) {
        log.debug("Attempting to probe the media");
        String outputString;
        // Build the command line
        // We need to use a (List that we convert to a) String[] for the command options as files with spaces in the name don't work otherwise
        List<String> options = new ArrayList<>();
        options.add(ffprobePath);
        options.add("-v");
        options.add("quiet");
        options.add("-print_format");
        options.add("json");
        options.add("-show_format");
        options.add("-show_streams");
        options.add(filename);
        Process proc = null;
        try {
            // Execute the command
            proc = Runtime.getRuntime().exec(options.toArray(new String[0]));
            CLIUtils.ProcessResult procResult = CLIUtils.getResultForProcess(proc);
            if (procResult.getExitCode() != 0) {
                log.error("Exit code of ffprobe was not 0, it was {}! Probing likely failed?", procResult.getExitCode());
            }
            outputString = procResult.getStdout();
            if (StringUtils.isBlank(outputString)) {
                log.warn("Did not get stdout as expected, trying stderr to see if some error was reported");
                outputString = procResult.getStderr();
                if (StringUtils.isNotBlank(outputString)) {
                    if (procResult.getExitCode() == 0) {
                        log.debug("Got stderr:{}", System.lineSeparator() + outputString);
                    } else {
                        log.error("Got stderr:{}", System.lineSeparator() + outputString);
                        return null;
                    }
                }
                else {
                    log.error("Did not get stdout, and stderr was empty too! Giving up and returning a null result...");
                    return null;
                }
            } else {
                log.debug("Got stdout:{}", System.lineSeparator() + outputString);
            }
        }
        catch (IOException | InterruptedException ioe) {
            log.error("We caught an IOException, so returning null!", ioe);
            return null;
        }
        finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return parseFfprobeResult(outputString);
    }

    public static Float getDuration(String ffprobePath, String filename) {
        FfprobeResult result = probeMediaFile(ffprobePath, filename);
        if (result == null) {
            log.warn("Couldn't probe the media file: {} with ffprobe path set to: {}, look for previous errors?",
                    filename, ffprobePath);
            return 0.0F;
        }
        Format format = result.getFormat();
        if (format == null) {
            log.warn("Didn't get format information for the media file: {} with ffprobe path set to: {}, look for previous errors?",
                    filename, ffprobePath);
            return 0.0F;
        }
        else {
            return format.getDuration();
        }
    }

    /**
     * Convenience method that converts the JSON-formatted String output of ffprobe to an FfprobeResult object
     * @param ffprobeResult String of JSON returned by ffprobe with -show_format and -show_streams flags set
     * @return FfprobeResult object or null if something goes wrong
     */
    private static FfprobeResult parseFfprobeResult(String ffprobeResult) {
        ObjectMapper mapper = new ObjectMapper();
        // ffprobe outputs Property Naming Strategy.SNAKE_CASE, specify it as such
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        FfprobeResult result = null;
        try {
            result = mapper.readValue(ffprobeResult, FfprobeResult.class);
        }
        catch (JsonMappingException jammy) {
            log.warn("Got a JSON mapping exception for String [{}]", ffprobeResult, jammy);
        }
        catch (JsonProcessingException jippy) {
            log.warn("Got a JSON processing exception for String [{}]", ffprobeResult, jippy);
        }
        return result;
    }

    /**
     * Turns a duration like 1:52:16 into seconds
     * First we add 1 * 60 to 52 -> 112
     * Then add 112 * 60 to 16 -> 6736 seconds in 1 hour 52 minutes and 16 seconds
     * @param duration Colon-delimited String representing hours, minutes, seconds
     * @return The number of seconds in those hour(s), minute(s), and second(s)
     */
	public static Double getSeconds(String duration) {
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
