/*
 * 
 */
package com.ocean927.memory.client;

import com.ocean927.memory.impl.MemoryManagerException;

// TODO: Auto-generated Javadoc
/**
 * The Interface MemoryStats.
 */
public interface MemoryStats {
	
	/**
	 * Prints memory page status.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void print() throws MemoryManagerException;

	/**
	 * Gets the pages in this block by reading the header.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return the pages in this block
	 */
	public int getPagesInThisBlock(long addressOfFreeBlock);

	/**
	 * Gets the page size.
	 *
	 * @return the page size in bytes
	 */
	public int getPageSize();
	
	/**
	 * Gets the memory allocated.
	 *
	 * @return the memory allocated in bytes.
	 */
	public long getMemoryAllocated();
}
