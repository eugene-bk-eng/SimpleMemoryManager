/*
 * 
 */
package com.ocean927.memory.utils;

/**
 * The Class Profiler.
 */
public class Profiler {
	
	/**
	 * Reset.
	 */
	public final void reset() { l_beg_nanos=0; l_end_nanos=0; }
	
	/**
	 * B.
	 */
	public final void b() {		
		l_beg_nanos = System.nanoTime(); 
	}
	
	/**
	 * E.
	 */
	public final void e() { l_end_nanos = System.nanoTime(); }
	
	/**
	 * Gets the time nanoseconds.
	 *
	 * @return the time nanoseconds
	 */
	public final long 		getTimeNanoseconds() { return l_end_nanos - l_beg_nanos; }
	
	/**
	 * Gets the time as microseconds.
	 *
	 * @return the time as microseconds
	 */
	public final long 		getTimeAsMicroseconds() {
		return (long) ((double)(l_end_nanos - l_beg_nanos)/(double)(1000)); 
	}	
	
	/**
	 * Gets the time as milliseconds.
	 *
	 * @return the time as milliseconds
	 */
	public final long 		getTimeAsMilliseconds() {
		return (long) ((double)(l_end_nanos - l_beg_nanos)/(double)(1000000)); 
	}

	/**
	 * Report timing mini.
	 *
	 * @return the string
	 */
	public final String reportTimingMini() {
		long nanos = getTimeNanoseconds();
		String elapsed = Formatter.getDiffNanoseconds(nanos);		
		String str = "elapsed:=" + elapsed;
		return str;			
	}

	/** The l beg nanos. */
	private 			long 					l_beg_nanos 						= 0;
	
	/** The l end nanos. */
	private 			long 					l_end_nanos 						= 0;	
}