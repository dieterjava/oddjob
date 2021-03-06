package org.oddjob.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.oddjob.Resetable;

/**
 * Annotate that a method is to be called during a hard reset.
 * 
 * @see Resetable
 * @see ResetableAdaptorFactory
 * 
 * @author rob
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HardReset {

}
