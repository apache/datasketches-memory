/*
Version 4 Public API. -- One Impl.  AutoCloseable via Handlers.
public abstract class Memory {
  public static Memory wrap(final ByteBuffer byteBuf)
  public static ResourceHandler map(final File file)
  public static ResourceHandler map(final File file, final long fileOffset, final long capacity)
  public abstract Memory region(long offsetBytes, long capacityBytes)
  public static Memory wrap(final prim-type[] arr)
  public abstract void copyTo(long srcOffsetBytes, WritableMemory destination, long dstOffsetBytes,
      long lengthBytes)
  public abstract getXXX(offset) methods
  ... plus other read misc, compareTo

}

public abstract class WritableMemory {
  public static WritableMemory wrap(final ByteBuffer byteBuf)
  public static WritableResourceHandler map(final File file)
  public static WritableResourceHandler map(final File file, final long fileOffset, final long capacity)
  public static WritableResourceHandler allocateDirect(final long capacityBytes)
  public static WritableResourceHandler allocateDirect(final long capacityBytes, final MemoryRequest memReq)
  public abstract WritableMemory region(long offsetBytes, long capacityBytes)
  public abstract Memory asReadOnly();
  public static WritableMemory allocate(final int capacityBytes)
  public static WritableMemory wrap(final prim-type[] arr)
  public abstract void copyTo(long srcOffsetBytes, WritableMemory destination, long dstOffsetBytes,
      long lengthBytes);
  //public abstract getXXX(offset) methods
  //... plus other read misc
  public abstract void putXXX(long offsetBytes, prim-type value)
  ... plus other write misc
  public abstract MemoryRequest getMemoryRequest()

}

public interface ResourceHandler extends AutoCloseable {
  Memory get()
  void close()
  void load()        //only for memory-mapped-files
  boolean isLoaded() //only for memory-mapped-files
}

public interface WritableResourceHandler extends AutoCloseable {
  WritableMemory get()
  void close()
  void load()        //only for memory-mapped-files
  boolean isLoaded() //only for memory-mapped-files
  void force()       //only for memory-mapped-files
}

public abstract Buffer { //Not implemented
}

public abstract WritableBuffer { //Not implemented
}

*/
package com.yahoo.memory4;
