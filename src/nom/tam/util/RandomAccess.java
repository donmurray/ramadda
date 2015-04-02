/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/
package nom.tam.util;


/**
 * These packages define the methods which indicate that
 *  an i/o stream may be accessed in arbitrary order.
 *  The method signatures are taken from RandomAccessFile
 *  though that class does not implement this interface.
 */
public interface RandomAccess extends ArrayDataInput {

    /**
     * Move to a specified location in the stream. 
     *
     * @param offsetFromStart _more_
     *
     * @throws java.io.IOException _more_
     */
    public void seek(long offsetFromStart) throws java.io.IOException;

    /**
     * Get the current position in the stream 
     *
     * @return _more_
     */
    public long getFilePointer();
}
