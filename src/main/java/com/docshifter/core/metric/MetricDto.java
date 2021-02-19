package com.docshifter.core.metric;

/**
Data transfer object to store metrics
 Currently stores filename and counts, will be expanded with other metrics
 Metrics other than the basic counter should be implemented as a licensable module
 */
public class MetricDto {
    private String filename;
    private int counts;

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }

    public int getCounts() { return this.counts; }

    public void setCounts(int counts) { this.counts = counts; }


}
