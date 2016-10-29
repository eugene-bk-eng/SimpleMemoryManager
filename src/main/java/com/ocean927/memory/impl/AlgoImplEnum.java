/*
 * 
 */
package com.ocean927.memory.impl;

// TODO: Auto-generated Javadoc
/**
 * The Enum AlgoImplEnum.
 */
public enum AlgoImplEnum {
	
	/** The on heap. */
	ON_HEAP("ON_HEAP_JVM"),
	
	/** The off heap. */
	OFF_HEAP("OFF_HEAP_JVM");
	
	/** The name. */
	String name;
	
	/**
	 * Instantiates a new algo impl enum.
	 *
	 * @param name the name
	 */
	private AlgoImplEnum(String name) {
		this.name=name;
	}
}
