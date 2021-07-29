package com.linkedin.kafka.cruisecontrol.vertx.generator;

import java.lang.annotation.*;

/**
 * @author ckaratza
 * Used to correlate the actual path with vertx routes.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VertxPath {
    String value() default "" ;
}
