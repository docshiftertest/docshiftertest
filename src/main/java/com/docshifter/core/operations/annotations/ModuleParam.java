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


    String value() default "";
    String defaultValue() default "";
    boolean required() default true;
    boolean supportPlaceholder() default true;
}

