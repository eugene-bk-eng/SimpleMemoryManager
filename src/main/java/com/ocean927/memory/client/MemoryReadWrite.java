/*
 * 
 */
package com.ocean927.memory.client;

import com.ocean927.memory.impl.MemoryManagerException;

// TODO: Auto-generated Javadoc
/**
 * The Interface MemoryReadWrite.
 */
public interface MemoryReadWrite {


	/**
	 * Write long to byte array.
	 *
	 * @param value the value
	 * @param start the start
	 * @param index the index
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeLongToByteArray(long value, long start, long index) throws MemoryManagerException;
	
	/**
	 * Write int to byte array.
	 *
	 * @param value the value
	 * @param start the start
	 * @param index the index
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeIntToByteArray(int value, long start, long index) throws MemoryManagerException;
	
	/**
	 * Write double to byte array.
	 *
	 * @param value the value
	 * @param start the start
	 * @param index the index
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeDoubleToByteArray(double value, long start, long index) throws MemoryManagerException;
	
	/**
	 * Write byte to byte array.
	 *
	 * @param value the value
	 * @param start the start
	 * @param index the index
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeByteToByteArray(byte value, long start, long index) throws MemoryManagerException;
	
	/**
	 * Write char to byte array.
	 *
	 * @param value the value
	 * @param start the start
	 * @param index the index
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void writeCharToByteArray(char value, long start, long index) throws MemoryManagerException;

	
	/**
	 * Read long from byte array.
	 *
	 * @param start the start
	 * @param index the index
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	public long readLongFromByteArray(long start, long index) throws MemoryManagerException;
	
	/**
	 * Read int from byte array.
	 *
	 * @param start the start
	 * @param index the index
	 * @return the int
	 * @throws MemoryManagerException the memory manager exception
	 */
	public int  readIntFromByteArray(long start, long index) throws MemoryManagerException;
	
	/**
	 * Read double from byte array.
	 *
	 * @param start the start
	 * @param index the index
	 * @return the double
	 * @throws MemoryManagerException the memory manager exception
	 */
	public double  readDoubleFromByteArray(long start, long index) throws MemoryManagerException;
	
	/**
	 * Read byte from byte array.
	 *
	 * @param start the start
	 * @param index the index
	 * @return the byte
	 * @throws MemoryManagerException the memory manager exception
	 */
	public byte  readByteFromByteArray(long start, long index) throws MemoryManagerException;
	
	/**
	 * Read char from byte array.
	 *
	 * @param start the start
	 * @param index the index
	 * @return the char
	 * @throws MemoryManagerException the memory manager exception
	 */
	public char  readCharFromByteArray(long start, long index) throws MemoryManagerException;
	
}