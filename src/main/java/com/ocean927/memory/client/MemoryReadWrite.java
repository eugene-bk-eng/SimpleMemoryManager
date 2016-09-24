/*
 * 
 */
package com.ocean927.memory.client;

import com.ocean927.memory.impl.MemoryManagerException;

/**
 * The Interface MemoryReadWrite.
 */
public interface MemoryReadWrite {


	/**
	 * Write long to byte array.
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeLongToByteArray(long value, long offset) throws MemoryManagerException;
	
	/**
	 * Write int to byte array.
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeIntToByteArray(int value, long offset) throws MemoryManagerException;
	
	/**
	 * Write double to byte array.
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeDoubleToByteArray(double value, long offset) throws MemoryManagerException;
	
	/**
	 * Write byte to byte array.
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeByteToByteArray(byte value, long offset) throws MemoryManagerException;
	
	/**
	 * Write char to byte array.
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeCharToByteArray(char value, long offset) throws MemoryManagerException;

	
	/**
	 * Read long from byte array.
	 *
	 * @param offset the offset
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	public long readLongFromByteArray(long offset) throws MemoryManagerException;
	
	/**
	 * Read int from byte array.
	 *
	 * @param offset the offset
	 * @return the int
	 * @throws MemoryManagerException the memory manager exception
	 */
	public int  readIntFromByteArray(long offset) throws MemoryManagerException;
	
	/**
	 * Read double from byte array.
	 *
	 * @param offset the offset
	 * @return the double
	 * @throws MemoryManagerException the memory manager exception
	 */
	public double  readDoubleFromByteArray(long offset) throws MemoryManagerException;
	
	/**
	 * Read byte from byte array.
	 *
	 * @param offset the offset
	 * @return the byte
	 * @throws MemoryManagerException the memory manager exception
	 */
	public byte  readByteFromByteArray(long offset) throws MemoryManagerException;
	
	/**
	 * Read char from byte array.
	 *
	 * @param offset the offset
	 * @return the char
	 * @throws MemoryManagerException the memory manager exception
	 */
	public char  readCharFromByteArray(long offset) throws MemoryManagerException;
	
}