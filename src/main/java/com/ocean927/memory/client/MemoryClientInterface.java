/*
 * Memory manager interface.
 * 
 * Memory management - allocate, deallocate, free.
 * 
 * Memory statistics - query stats.
 * 
 * Memory I/O - read/write the block.
 */
package com.ocean927.memory.client;

public interface MemoryClientInterface extends MemoryManagement, MemoryStats, MemoryReadWrite {

}
