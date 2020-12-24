package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.KeyValuePair;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import java.util.List;

/**
 * Created by blazejm on 16.05.2017.
 */
public class WebhookConfigurationItemDto extends AbstractConfigurationItemDto {
    private String url;

    private String body;

    private List<KeyValuePair<String, String>> urlParams;

    private List<KeyValuePair<String, String>> headerParams;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<KeyValuePair<String, String>> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(List<KeyValuePair<String, String>> urlParams) {
        this.urlParams = urlParams;
    }

    public List<KeyValuePair<String, String>> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<KeyValuePair<String, String>> headerParams) {
        this.headerParams = headerParams;
    }

    @Override
    public ConfigurationTypes getType() {
        return ConfigurationTypes.webhook;
    }
}
