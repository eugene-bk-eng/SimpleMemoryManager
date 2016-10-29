/*
 * 
 */
package com.ocean927.memory.client;

import com.ocean927.memory.impl.MemoryManagerException;


// TODO: Auto-generated Javadoc
/**
 * The Interface MemoryManagement.
 */
public interface MemoryManagement {

	/**
	 * Set up memory and page size.
	 *
	 * @param memorySizeBytes the memory size bytes
	 * @param pageSizeParam the page size param
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void setup(long memorySizeBytes, int pageSizeParam) throws MemoryManagerException;
	
	/**
	 * Set up memory using default page 1K.
	 *
	 * @param memorySizeBytes the new up
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void setup(String memorySizeBytes) throws MemoryManagerException;
	
	/**
	 * Set up memory and page size, passing string arguments.
	 * "512 mb", "4 kb"
	 *
	 * @param memorySizeBytes the memory size bytes
	 * @param pageSizeParam the page size param
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void setup(String memorySizeBytes, String pageSizeParam) throws MemoryManagerException;

	
	/**
	 * Allocate block of memory. Similar to C malloc.
	 *
	 * @param requestedLength the requested length
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	public long allocate(long requestedLength) throws MemoryManagerException;	
	
	/**
	 * Deallocate block of memory back to the common pool.
	 *
	 * @param address the address
	 * @return the int
	 * @throws MemoryManagerException the memory manager exception
	 */
	public int deallocate(long address) throws MemoryManagerException;
	
	/**
	 * Defragment scans entire memory and combines adjacent 
	 * free blocks into bigger blocks. It helps to service 
	 * large allocation requests.
	 * 
	 * Note, the method can move used memory block to a 
	 * different location even though it might create 
	 * larger contiguous free space.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void defragment() throws MemoryManagerException;
	
	/**
	 * Free memory by telling the underlining 
	 * implementation to give it up back to OS.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void freeMemory() throws MemoryManagerException;
}
