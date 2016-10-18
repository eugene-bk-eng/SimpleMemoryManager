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
	 * Example:
	 * 
	 * impl.setup((long)1024*1024*1024l,1024); // 1GB in 1K pages
	 * 
	 * MEMORY LAYOUT: 1,073,741,824 bytes in 1048576 pages of 1024 bytes/page.
	 * Free Pages: 1048576, Used Pages: 0
	 * Free Bytes: 1073741824, 100.0 %, Used Bytes: 0, 0.0 %
	 * BLOCK,        ADDRESS,          PAGES,      BLOCK_LEN,         STATUS
	 * 		1,              0,        1048576,     1073741824,          free
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
	 * Gets the memory size allocated in bytes.
	 *
	 * @return the memory allocated in bytes.
	 */
	public long getMemoryAllocated();
}
