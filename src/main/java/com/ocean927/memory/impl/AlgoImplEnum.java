package com.ocean927.memory.impl;

public enum AlgoImplEnum {
	ON_HEAP("ON_HEAP_JVM"),
	OFF_HEAP("OFF_HEAP_JVM");
	
	String name;
	
	private AlgoImplEnum(String name) {
		this.name=name;
	}
}
