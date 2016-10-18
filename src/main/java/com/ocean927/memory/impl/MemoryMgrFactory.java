/*
 * 
 */
package com.ocean927.memory.impl;

import com.ocean927.memory.client.MemoryClientInterface;

/**
 * A factory for creating MemoryMgr objects.
 */
public class MemoryMgrFactory {
	
	/**
	 * Gets the implementation.
	 *
	 * @param arg the arg
	 * @return the impl
	 * @throws MemoryManagerException the memory manager exception
	 */
	public static MemoryClientInterface getImplementation(AlgoImplEnum arg) throws MemoryManagerException {
		switch(arg) {
		  case ON_HEAP: return new OnHeapMemoryMgrImpl();
		  case OFF_HEAP: return new OffHeapMemoryMgrImpl();
		}
		return null;
	}
}
