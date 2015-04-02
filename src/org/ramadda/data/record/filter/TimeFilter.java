/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.data.record.filter;


import org.ramadda.data.record.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Fri, May 21, '10
 * @author         Enter your name here...
 */
public class TimeFilter implements RecordFilter {



    /** _more_ */
    public long minTime = Record.UNDEFINED_TIME;

    /** _more_ */
    public long maxTime = Record.UNDEFINED_TIME;


    /**
     * _more_
     *
     * @param minTime _more_
     * @param maxTime _more_
     */
    public TimeFilter(Date minTime, Date maxTime) {
        if (minTime != null) {
            this.minTime = minTime.getTime();
        }
        if (maxTime != null) {
            this.maxTime = maxTime.getTime();
        }

    }


    /**
     * _more_
     *
     *
     * @param minTime _more_
     * @param maxTime _more_
     */
    public TimeFilter(long minTime, long maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;

    }



    /**
     * _more_
     *
     * @param record _more_
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public boolean isRecordOk(Record record, VisitInfo visitInfo) {
        long time = record.getRecordTime();
        if (time == Record.UNDEFINED_TIME) {
            return false;
        }
        if ((minTime != Record.UNDEFINED_TIME) && (time < minTime)) {
            return false;
        }
        if ((maxTime != Record.UNDEFINED_TIME) && (time > maxTime)) {
            return false;
        }

        return true;

    }

}
