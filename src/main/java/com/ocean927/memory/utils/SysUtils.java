/*
 * 
 */
package com.ocean927.memory.utils;

/**
 * The Class SysUtils.
 */
public class SysUtils {

	/**
	 * prevent users instantiating this class.
	 */	
	private SysUtils() {
		
	}
	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	public final static String getClassName() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
	    StackTraceElement e = stacktrace[2];			
		return e.getClassName();  				
	}

	/**
	 * Gets the class path loaded.
	 *
	 * @return the class path loaded
	 */
	public final static String getClassPathLoaded() {
		String classpath = System.getProperty("java.class.path");			
		return classpath;  				
	}	
	
	
	/**
	 * Gets the class name.
	 *
	 * @param c the c
	 * @return the class name
	 */
	public final static String getClassName(Class<?> c) {
		return c.getName(); 				
	}
	
	/**
	 * Gets the class name.
	 *
	 * @param obj the obj
	 * @return the class name
	 */
	public static String getClassName(Object obj) {
	    if( obj!=null ) { 
	    	Class<?> c=obj.getClass();
	    	String name=getClassName(c);
	    	if( name.lastIndexOf(".")>=0 ) {
	    		name=name.substring(name.lastIndexOf(".")+1);
	    	}
	    	return name;
	    }
	    return null;
	}
	
	/**
	 * Gets the method name.
	 *
	 * @return the method name
	 */
	public final static String getMethodName() {	
		StackTraceElement st[] = Thread.currentThread().getStackTrace();
		String methodName = st[2].getMethodName();
		return methodName;
	}
	
	/**
	 * Gets the method name.
	 *
	 * @param thread the thread
	 * @return the method name
	 */
	public final static String getMethodName(Thread thread) {
		StackTraceElement st[] = thread.getStackTrace();
		String methodName = st[2].getMethodName();
		return methodName; 				
	}	
}