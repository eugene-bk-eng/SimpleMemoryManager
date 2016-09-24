/*
 * An off-heap memory manager implementing a buddy-memory model 
 * based on blocks of 2^k [0..N] byte size.
 * 
 * It uses Java unsafe to allocate blocks off JVM heap. 
 * Blocks have to be freed manually.
 * 
 * Because it is off JVM heap, allocations can exceed 2^31-1 bytes
 * and go up to 2^63-1 bytes.
 * 
 */

package com.ocean927.memory.impl;

import java.lang.reflect.Field;

import com.ocean927.memory.utils.ByteUtils;
import com.ocean927.memory.utils.Formatter;

/**
 * The Class OffHeapMemoryMgrImpl.
 */
public class OffHeapMemoryMgrImpl extends AbstractMemoryManagerAlgorithm {
	
	/**
	 * Instantiates a new off heap memory mgr impl.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public OffHeapMemoryMgrImpl() throws MemoryManagerException{	
		unsafe=getUnsafe();		
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#setup(long, int)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	/** */
	public void setup(long memorySizeBytes, int pageSizeParam) throws MemoryManagerException {
		N=memorySizeBytes;
		logger.info(ByteUtils.getClassName() + "." + ByteUtils.getMethodName() + ": " + memorySizeBytes + " bytes, " + Formatter.rDbl((double)memorySizeBytes/(1024*1024), 2) + " MB., " + Formatter.rDbl((double)memorySizeBytes/(1024*1024*1024), 2) + " GB.");
		if( memoryOffHeapAddres>=0 ) { throw new MemoryManagerException("Memory address must be free before allocating " + memoryOffHeapAddres); }
		memoryOffHeapAddres=unsafe.allocateMemory(N); 	
		if( ByteUtils.ispowerof2long(N)==false ) {
		 throw new MemoryManagerException("Memory size " + N + " must be power of 2" );
		}
		pageSize=pageSizeParam; 
		if( ByteUtils.ispowerof2(pageSize)==false ) {
			throw new MemoryManagerException("Page size " + pageSize + " must be power of 2" );
		}
		pages=getMaxPages();		//l("Memory " + Uf.fl(N) + ", pages " + pages + " of size " + pageSize + " bytes." );		
		free_list = new Object[ByteUtils.log2_v2(pages)+1];
		pagesPerBlock = new int[ByteUtils.log2_v2(pages)+1];
		for (int i=0; i<free_list.length; i++) {
			int pgsPerBlock=(int)Math.pow(2,i);			
			int blocksOfThisSize=(int) (N/((long)pgsPerBlock*(long)pageSize));			
			free_list[i]=new long[blocksOfThisSize+1]; // first element is a counter
			long free_list_of_size_2i[]=(long[])free_list[i];
			for (int j = 1; j < free_list_of_size_2i.length; j++) { 
				free_list_of_size_2i[j]=-1; 
			}
			pagesPerBlock[i]=pgsPerBlock;
			//l("list " + (i+1) + ", pages per block " + pagesPerBlock + " and actuals " + blocksOfThisSize + " blocks."  );
		}	
		//
		placeOnFreeList(0, getMaxPages() );
 	}
	
	/**
	 *  sets header, pages in block determine block size (in pages).
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @throws MemoryManagerException the memory manager exception
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	protected void setBlockHeaderUsed(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException {
		int logof2=ByteUtils.log2_v2(pagesInBlock);		
		// my_byte = my_byte | (1 << pos); // sets the bit. set 1
		unsafe.putByte(memoryOffHeapAddres + addressOfFreeBlock,  (byte) ((logof2<<1) | (1)) );
			
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#setBlockHeaderFree(long, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	protected void setBlockHeaderFree(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException {
		int logof2=ByteUtils.log2_v2(pagesInBlock);		
		// my_byte = my_byte & ~(1 << pos); // clear the bit. set 0.
		unsafe.putByte(memoryOffHeapAddres + addressOfFreeBlock, (byte) ((logof2<<1) & (-2)) );		
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#isUsed(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	protected boolean isUsed(long addressOfFreeBlock) {
		//return (unsafe.getByte(memoryOffHeapAddres + addressOfFreeBlock) & (1<<0))==1 ? true: false;
		if( unsafe.getByte(memoryOffHeapAddres + addressOfFreeBlock)==(byte)1 ) {
			return true;
		}else{
			return false;
		}
	}	
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#isFree(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	protected boolean isFree(long addressOfFreeBlock) {
		//return (unsafe.getByte(memoryOffHeapAddres + addressOfFreeBlock) & (1<<0))==0 ? true: false;
		if( unsafe.getByte(memoryOffHeapAddres + addressOfFreeBlock)==(byte)0 ) {
			return true;
		}else{
			return false;
		}
	}	
	
	/**
	 *  get number of pages in the block - 2^0, 2^1, 2^2, 2^3....2^k
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return the pages in this block
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override	
	public int getPagesInThisBlock(long addressOfFreeBlock) {
		int power=(int)unsafe.getByte(memoryOffHeapAddres + addressOfFreeBlock)>>1;
		return powerOf2[power];
	}
		
	/**
	 * Gets the unsafe.
	 *
	 * @return the unsafe
	 * @throws MemoryManagerException the memory manager exception
	 */
	@SuppressWarnings(value = { "restriction" })
	public static sun.misc.Unsafe getUnsafe() throws MemoryManagerException {
	  try {	
		Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
		return unsafe;
	  }catch(Exception e) {
		  throw new MemoryManagerException(e);
	  }
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeLongToByteArray(long, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void writeLongToByteArray(long value, long offset) throws MemoryManagerException {
		unsafe.putLong(memoryOffHeapAddres+offset+HEADER_LENGTH, value);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeIntToByteArray(int, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void writeIntToByteArray(int value, long offset) throws MemoryManagerException {
		unsafe.putInt(memoryOffHeapAddres+offset+HEADER_LENGTH, value);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readLongFromByteArray(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public long readLongFromByteArray(long offset) throws MemoryManagerException {
		return unsafe.getLong(memoryOffHeapAddres+offset+HEADER_LENGTH);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readIntFromByteArray(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public int readIntFromByteArray(long offset) throws MemoryManagerException {
		return unsafe.getInt(memoryOffHeapAddres+offset+HEADER_LENGTH);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeDoubleToByteArray(double, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void writeDoubleToByteArray(double value, long offset) throws MemoryManagerException {
		unsafe.putDouble(memoryOffHeapAddres+offset+HEADER_LENGTH, value);
	}

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeByteToByteArray(byte, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void writeByteToByteArray(byte value, long offset) throws MemoryManagerException {
		unsafe.putByte(memoryOffHeapAddres+offset+HEADER_LENGTH, value);
	}

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readDoubleFromByteArray(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public double readDoubleFromByteArray(long offset) throws MemoryManagerException {
		return unsafe.getDouble(memoryOffHeapAddres+offset+HEADER_LENGTH);
	}

	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readByteFromByteArray(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public byte readByteFromByteArray(long offset) throws MemoryManagerException {
		return unsafe.getByte(memoryOffHeapAddres+offset+HEADER_LENGTH);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeCharToByteArray(char, long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void writeCharToByteArray(char value, long offset) throws MemoryManagerException {
		unsafe.putChar(memoryOffHeapAddres+offset+HEADER_LENGTH, value);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readCharFromByteArray(long)
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public char  readCharFromByteArray(long offset) throws MemoryManagerException {
		return unsafe.getChar(memoryOffHeapAddres+offset+HEADER_LENGTH);
	}
	
	/** The helper array. */
	Object helperArray[] 	= new Object[1];
	
	/**
	 * Gets the address of object.
	 *
	 * @param unsafe the unsafe
	 * @param obj the obj
	 * @return the address of object
	 */
	@SuppressWarnings("restriction")
	public long getAddressOfObject(sun.misc.Unsafe unsafe, Object obj) {		
		helperArray[0] 			= obj;
		long baseOffset 		= unsafe.arrayBaseOffset(Object[].class);
		long addressOfObject	= unsafe.getLong(helperArray, baseOffset);		
		return addressOfObject;
	}
	
	/**
	 * In this implementation, calls into unsafe.freeMemory() to tell OS 
	 * to release memory.
	 * 
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#freeMemory()
	 */
	@SuppressWarnings(value = { "restriction" })
	@Override
	public void freeMemory() {
		if( memoryOffHeapAddres>=0 ) {
			unsafe.freeMemory(memoryOffHeapAddres);
			memoryOffHeapAddres=-1;
		}
	}
	
	/** The memory off heap addres. */
	private long memoryOffHeapAddres=-1;
	
	/** The unsafe. */
	@SuppressWarnings(value = { "restriction" })
	private sun.misc.Unsafe unsafe;	
}