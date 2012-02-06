/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/
package org.ramadda.geodata.fits;


import nom.tam.fits.*;


import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.text.SimpleDateFormat;

import java.awt.geom.Rectangle2D;

import java.io.File;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class FitsTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static final String FITS_PROP_INSTRUMENT = "INSTRUME";

    /** _more_          */
    public static final String FITS_PROP_TELESCOPE = "TELESCOP";

    /** _more_          */
    public static final String FITS_PROP_LOCATION = "LOCATION";

    /** _more_          */
    public static final String FITS_PROP_ORIGIN = "ORIGIN";

    /** _more_          */
    public static final String FITS_PROP_OBJECT = "OBJECT";

    /** _more_          */
    public static final String FITS_PROP_OBSERVER = "OBSERVER";

    /** _more_          */
    public static final String FITS_PROP_AUTHOR = "AUTHOR";

    /** _more_          */
    public static final String FITS_PROP_REFERENCE = "REFERENC";

    /** _more_          */
    public static final String FITS_PROP_TYPE_OBS = "TYPE-OBS";

    /** _more_          */
    public static final String FITS_PROP_DATE_OBS = "DATE-OBS";

    /** _more_          */
    public static final String FITS_PROP_TIME_OBS = "TIME-OBS";


    /** _more_          */
    public static final String BAD_PROP_DATE_OBS = "DATE_OBS";

    /** _more_          */
    public static final String BAD_PROP_TIME_OBS = "TIME_OBS";


    /** _more_          */
    public static final String[] FITS_PROPS = { FITS_PROP_ORIGIN, FITS_PROP_TELESCOPE,
                                           FITS_PROP_INSTRUMENT, FITS_PROP_TYPE_OBS, };


    /** _more_          */
    public static final String FITS_PROP_ = "";



    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public FitsTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if (newEntry) {
            initializeNewEntry(entry);
        }
    }

    private static SimpleDateFormat[] SDFS;
    private static SimpleDateFormat STUPIDSDF;

    private static Date[] getDateRange(nom.tam.fits.Header header) {
        if(SDFS==null) {
            STUPIDSDF = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
            STUPIDSDF.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            SimpleDateFormat[] tmp = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ"),
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss"),
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ"),
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"),
                new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.S"),
                new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"),
                new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss"),
                new SimpleDateFormat("dd/MM/yy hh:mm:ss"),
                new SimpleDateFormat("yyyy/MM/dd"),
            };
            for(SimpleDateFormat sdf: tmp) {
                sdf.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            }
            SDFS = tmp;
        }

        Date  fromDate   = null;
        Date toDate = null;
        String dateString= header.getStringValue(FITS_PROP_DATE_OBS);
        if(dateString==null) dateString =  header.getStringValue(BAD_PROP_DATE_OBS);
        String timeString= header.getStringValue(FITS_PROP_TIME_OBS);
        if(timeString==null) timeString =  header.getStringValue(BAD_PROP_TIME_OBS);

        if(dateString==null) {
            return new Date[]{fromDate,toDate};
        }
        boolean stupidFormat = false;

        if(dateString.indexOf("/")>=0 && (dateString.length() ==7 ||
                                          dateString.length() ==8)) {
            stupidFormat = true;
        }

        if(timeString!=null) {
            dateString = dateString+" " + timeString;
        }
        //Check for the dd/MM/yy format
        if(stupidFormat) {
            try {
                synchronized(STUPIDSDF) {
                    fromDate = toDate = STUPIDSDF.parse(dateString);
                }
            } catch(Exception exc) {
            }
        }

        for(int i=0;fromDate== null && i<SDFS.length;i++) {
            SimpleDateFormat sdf =  SDFS[i];
            synchronized(sdf) {
                try {
                    fromDate = toDate = sdf.parse(dateString);
                    break;
                } catch(Exception exc) {
                }
            }
        }



        /*
        if(fromDate!=null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ");
            sdf.setTimeZone(RepositoryUtil.TIMEZONE_DEFAULT);
            System.err.println ("DATE:" + dateString + "\n     " + 
                                sdf.format(fromDate));
            System.err.println ("");
        } else {
            System.err.println ("FAILED:" + dateString);
        }
        */

        return new Date[]{fromDate,toDate};
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        Object[] values = entry.getValues();
        if (values == null) {
            values = new Object[4];
        }
        Fits fits = new Fits(entry.getFile());
        for (int headerIdx = 0; headerIdx < fits.size(); headerIdx++) {
            BasicHDU hdu  = fits.getHDU(headerIdx);
            nom.tam.fits.Header header = hdu.getHeader();

            Date[] dateRange = getDateRange(header);
            if (dateRange != null&& dateRange[0]!=null) {
                entry.setStartDate(dateRange[0].getTime());
                entry.setEndDate(dateRange[1].getTime());
            }
            for (int i = 0; i < FITS_PROPS.length; i++) {
                if (values[i] == null) {
                    String value = header.getStringValue(FITS_PROPS[i]);
                    if (value != null) {
                        values[i] = value.trim();
                    }
                }
            }
        }
        entry.setValues(values);
    }


    /**
     * _more_
     *
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public static void processFits(String file) throws Exception {
        Fits fits = new Fits(file);
        //        System.err.println(file);
        for (int headerIdx = 0; headerIdx < fits.size(); headerIdx++) {
            BasicHDU            hdu     = fits.getHDU(headerIdx);
            nom.tam.fits.Header header  = hdu.getHeader();
            Date[] dateRange = getDateRange(header);
            if(true) return;

            String              hduType = "N/A";
            if (hdu instanceof AsciiTableHDU) {
                hduType = "Ascii Table";
            } else if (hdu instanceof ImageHDU) {
                hduType = "Image";
            } else if (hdu instanceof BinaryTableHDU) {
                hduType = "Binary Table";
            }

            System.out.println("hdu:" + hduType + " -- "
                               + hdu.getObservationDate());
            for (String prop : FITS_PROPS) {
                String value = header.getStringValue(prop);
                if (value != null) {
                    System.out.println("\t" + prop + "=" + value);
                }
            }
            int numCards = header.getNumberOfCards();
            for (int cardIdx = 0; cardIdx < numCards; cardIdx++) {
                String card = header.getCard(cardIdx);
                //                System.out.println("\tcard:" + card);
            }
        }
    }







    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        for (String file : args) {
            //            System.err.println(file);
            processFits(file);
        }

    }


}
