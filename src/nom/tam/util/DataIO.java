/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

/**
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */
package nom.tam.util;


import java.io.DataInput;
import java.io.DataOutput;


/**
 * This interface combines the DataInput, DataOutput and
 *  RandomAccess interfaces to provide a reference type
 *  which can be used to build BufferedFile in a fashion
 *  that accommodates both the RandomAccessFile and ByteBuffers
 */
public interface DataIO extends DataInput, DataOutput, RandomAccess {}
