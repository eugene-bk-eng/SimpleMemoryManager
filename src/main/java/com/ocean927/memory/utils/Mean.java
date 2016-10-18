/*
 * 
 */
package com.ocean927.memory.utils;


/**
 * The Class Mean.
 */
public class Mean {
	
	/** The count. */
	private long count=0;
	
	/** The max. */
	private double average=0, sum=0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
	
	/**
	 * Instantiates a new mean.
	 */
	public Mean() {
		reset();
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		count=0; average=0; sum=0; min = Double.MAX_VALUE; max = Double.MIN_VALUE;
	}
	
	/**
	 * Adds the.
	 *
	 * @param value the value
	 */
	public void add(double value) {		
		count++; 
		sum+=value;
		average = average + ((value-average)/count); 
		if( value<min ) { min=value; }
		if( value>max ) { max=value; }
	}	
	
	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public long getCount() { return count; }
	
	/**
	 *  most accurate, prone to overflow for large sum.
	 *
	 * @return the mean
	 */
	public double getMean() {
		if( count>0 ) {
			return sum/count;
		}else{ 
			return Double.NaN;
		}
	}
	
	/**
	 *  will always work but is off by a small amount due to division rounding.
	 *
	 * @return the average
	 */
	public double getAverage() {
		return average; 
	}
	
	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	public double getMin() { return min; }
	
	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	public double getMax() { return max; }
}