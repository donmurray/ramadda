/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/
package nom.tam.fits.utilities;


import nom.tam.fits.*;
import nom.tam.util.*;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Apr 2, '15
 * @author         Enter your name here...    
 */
public class FitsCopy {

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {

        String   file = args[0];

        Fits     f    = new Fits(file);
        int      i    = 0;
        BasicHDU h;

        do {
            h = f.readHDU();
            if (h != null) {
                if (i == 0) {
                    System.out.println("\n\nPrimary header:\n");
                } else {
                    System.out.println("\n\nExtension " + i + ":\n");
                }
                i += 1;
                h.info();
            }
        } while (h != null);
        BufferedFile bf = new BufferedFile(args[1], "rw");
        f.write(bf);
        bf.close();

    }
}
