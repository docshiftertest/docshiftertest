package com.docshifter.core.metric;

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
