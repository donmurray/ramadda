/**
* Copyright (c) 2008-2015 Geode Systems LLC
* This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
* ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
*/

package org.ramadda.data.record;


import java.io.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Holds a list of visitors. Visits them in turn
 *
 */
public class RecordVisitorGroup extends RecordVisitor {

    /** list of visitors */
    private List<RecordVisitor> visitors = new ArrayList<RecordVisitor>();

    /** record count */
    private int count = 0;

    /**
     * ctor
     */
    public RecordVisitorGroup() {}

    /**
     * ctor
     *
     * @param visitors List of visitors
     */
    public RecordVisitorGroup(List<RecordVisitor> visitors) {
        this.visitors.addAll(visitors);
    }

    /**
     * add a new visitor
     *
     * @param visitor new visitor
     */
    public void addVisitor(RecordVisitor visitor) {
        visitors.add(visitor);
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     * @param record _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public boolean visitRecord(RecordFile file, VisitInfo visitInfo,
                               Record record)
            throws Exception {
        count++;
        for (RecordVisitor visitor : visitors) {
            boolean visitorOK = visitor.visitRecord(file, visitInfo, record);
            if ( !visitorOK) {
                return false;
            }
        }

        return true;
    }

    /**
     * _more_
     *
     * @param file _more_
     * @param visitInfo _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void finished(RecordFile file, VisitInfo visitInfo)
            throws Exception {
        super.finished(file, visitInfo);
        for (RecordVisitor visitor : visitors) {
            visitor.finished(file, visitInfo);
        }
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     */
    @Override
    public void close(VisitInfo visitInfo) {
        super.close(visitInfo);
        for (RecordVisitor visitor : visitors) {
            visitor.close(visitInfo);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getCount() {
        return count;
    }

}
