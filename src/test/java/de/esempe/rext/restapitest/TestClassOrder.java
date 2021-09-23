package de.esempe.rext.restapitest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestClassOrder
{
	public int value() default Integer.MAX_VALUE;
}
