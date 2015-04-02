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
package nom.tam.fits;


/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 *
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */

/**
 * This exception indicates that an error
 * was detected while parsing a FITS header record.
 */
public class BadHeaderException extends FitsException {

    /**
     * _more_
     */
    public BadHeaderException() {
        super();
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public BadHeaderException(String msg) {
        super(msg);
    }
}
