/*
 * On-heap memory manager implementing a buddy-memory model 
 *  
 * Memory cann't exceed 2^31-1 bytes, the size of byte array.
 * 
 * Memory manager is exposed to clients via an interface.
 * 
 */

package com.ocean927.memory.impl;

import java.nio.ByteBuffer;

import com.ocean927.memory.utils.ByteUtils;
import com.ocean927.memory.utils.Formatter;

/**
 * The Class OnHeapMemoryMgrImpl.
 */
public class OnHeapMemoryMgrImpl extends AbstractMemoryManagerAlgorithm {
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#setup(long, int)
	 */
	@Override
	public void setup(long memorySizeBytes, int pageSizeParam) throws MemoryManagerException {
		N=(int)memorySizeBytes;
		logger.info(ByteUtils.getClassName() + "." + ByteUtils.getMethodName() + ": " + memorySizeBytes + " bytes, " + Formatter.rDbl((double)memorySizeBytes/(1024*1024), 2) + " MB., " + Formatter.rDbl((double)memorySizeBytes/(1024*1024*1024), 2) + " GB.");
		memory=new byte[(int)N]; 	
		if( ByteUtils.ispowerof2((int)N)==false ) {
			 throw new MemoryManagerException("Memory size " + N + " must be power of 2" );
		}
		pageSize=pageSizeParam; 		
		if( ByteUtils.ispowerof2(pageSize)==false ) {
			throw new MemoryManagerException("Page size " + pageSize + " must be power of 2" );
		}
		pages=getMaxPages();				
		free_list = new Object[ByteUtils.log2_v2(pages)+1];
		pagesPerBlock = new int[ByteUtils.log2_v2(pages)+1];
		for (int i=0; i<free_list.length; i++) {
			int pgsPerBlock=(int)Math.pow(2,i);			
			int blocksOfThisSize=((int)N)/(pgsPerBlock*pageSize);			
			free_list[i]=new long[blocksOfThisSize+1]; // +1 because 1st element is a counter
			for (int j = 1; j < ((long[])free_list[i]).length; j++) { ((long[])free_list[i])[j]=-1; }
			pagesPerBlock[i]=pgsPerBlock; // "list " + (i+1) + ", pages per block " + pagesPerBlock + " and actuals " + blocksOfThisSize + " blocks."					
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
	@Override
	protected void setBlockHeaderUsed(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException {
		int logof2=ByteUtils.log2_v2(pagesInBlock);		
		// my_byte = my_byte | (1 << pos); // sets the bit. set 1
		memory[(int)addressOfFreeBlock] = (byte) ((logof2<<1) | (1));			
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#setBlockHeaderFree(long, long)
	 */
	@Override
	protected void setBlockHeaderFree(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException {
		int logof2=ByteUtils.log2_v2(pagesInBlock);		
		// my_byte = my_byte & ~(1 << pos); // clear the bit. set 0.
		memory[(int)addressOfFreeBlock] = (byte) ((logof2<<1) & (-2));		
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#isUsed(long)
	 */
	@Override
	protected boolean isUsed(long addressOfFreeBlock) {
		return (0xff & (memory[(int)addressOfFreeBlock]) & (1<<0))==1 ? true: false;		
	}	
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#isFree(long)
	 */
	@Override
	protected boolean isFree(long addressOfFreeBlock) {
		return (0xff & (memory[(int)addressOfFreeBlock]) & (1<<0))==0 ? true: false;	
	}	
	
	
	/**
	 *  get count of pages in the block - 2^0, 2^1, 2^2, 2^3....2^k
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return the pages in this block
	 */
	@Override
	public int getPagesInThisBlock(long addressOfFreeBlock) {
		int power=(int)memory[(int)addressOfFreeBlock]>>1;
		return powerOf2[power];
	}
		

	/**
	 * In this implementation, sets the underlining array reference to null. 
	 * @see com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm#freeMemory()
	 */
	@Override
	public void freeMemory() throws MemoryManagerException {
		memory=null;
	}
	
	/**
	 *  LIMITATION OF JAVA ON-HEAP SOLUTION THAT WE USE BYTE ARRAY WHICH 
	 * CAN ONLY GO TO 2^31-1 OR ABOUT 1.99GB. LAST BIT IS A SIGN. 
	 * OFFSET MUST BE WITHIN A NON-NEGATIVE INTEGER RANGE AS SPECIFIED
	 *
	 * @param value the value
	 * @param offset the offset
	 * @throws MemoryManagerException the memory manager exception
	 */
	@Override
	public void writeLongToByteArray(long value, long offset) throws MemoryManagerException {
		ByteUtils.writeLongToByteArray2(value, memory, (int)offset);
	}

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeIntToByteArray(int, long)
	 */
	@Override
	public void writeIntToByteArray(int value, long offset) throws MemoryManagerException {
		ByteUtils.writeIntToByteArray(value, memory, (int)offset);
	}
	
	/**
	 *  LIMITATION OF JAVA ON-HEAP SOLUTION THAT WE USE BYTE ARRAY WHICH 
	 * CAN ONLY GO TO 2^31-1 OR ABOUT 1.99GB. LAST BIT IS A SIGN. 
	 * OFFSET MUST BE WITHIN A NON-NEGATIVE INTEGER RANGE AS SPECIFIED
	 *
	 * @param offset the offset
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	@Override
	public long readLongFromByteArray(long offset) throws MemoryManagerException {
		return ByteUtils.readLongFromByteArray(memory, (int)offset);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readIntFromByteArray(long)
	 */
	@Override
	public int readIntFromByteArray(long offset) throws MemoryManagerException {
		return ByteUtils.readIntFromByteArray(memory, (int)offset);
	}	

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeDoubleToByteArray(double, long)
	 */
	@Override
	public void writeDoubleToByteArray(double value, long offset) throws MemoryManagerException {
		ByteBuffer buf=ByteBuffer.wrap(memory, (int)offset, 8); 
		buf.putDouble(value);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readDoubleFromByteArray(long)
	 */
	@Override
	public double readDoubleFromByteArray(long offset) throws MemoryManagerException { 
		ByteBuffer buf=ByteBuffer.wrap(memory);
		return buf.getDouble((int)offset); // you're reading from offset
	}

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeByteToByteArray(byte, long)
	 */
	@Override
	public void writeByteToByteArray(byte value, long offset) throws MemoryManagerException {
		memory[(int)offset]=value;
	}

	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readByteFromByteArray(long)
	 */
	@Override
	public byte readByteFromByteArray(long offset) throws MemoryManagerException {
		return memory[(int)offset];
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#writeCharToByteArray(char, long)
	 */
	@Override
	public void writeCharToByteArray(char value, long offset) throws MemoryManagerException {
		ByteBuffer buf=ByteBuffer.wrap(memory, (int)offset, 2); 
		buf.putChar(value);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#readCharFromByteArray(long)
	 */
	@Override
	public char  readCharFromByteArray(long offset) throws MemoryManagerException {
		ByteBuffer buf=ByteBuffer.wrap(memory);
		return buf.getChar((int)offset); // you're reading from offset
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#copyMemory(long, long, long)
	 */
	public void copyMemory(long srcPos, long destPos, long length) throws MemoryManagerException {
		System.arraycopy(memory, (int)srcPos, memory, (int)destPos, (int)length);
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryReadWrite#copyMemory(byte[], long, long, long)
	 */
	private void copyMemory(byte src[], long srcPos, long destPos, long length) throws MemoryManagerException {
		System.arraycopy(src, (int)srcPos, memory, (int)destPos, (int)length);
	}
	
	/** The memory. */
	private byte memory[];
}