package com.docshifter.core.operations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by samnang.nop on 13/07/2016.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
public @interface ModuleParam {

    enum ConfigFileType {
        NONE,
        JAXB_XML
    }

    String value() default "";
    String defaultValue() default "";
    boolean required() default true;

    /**
     * Whether we support placeholders or not ${something}
     */
    boolean supportPlaceholder() default true;

    /**
     * Whether this module parameter should deserialize to a config file
     */
    ConfigFileType configFile() default ConfigFileType.NONE;

    /**
     * If set, defines the separators used to split a {@link String} input into a simple or multidimensional collection.
     */
    String[] separators() default {};
}

