package org.ramadda.data.services;

import java.util.Iterator;
import java.util.List;

import org.ramadda.data.point.text.MultiMonthFile;
import org.ramadda.data.record.RecordFile;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

public class NoaaPsdMonthlyClimateIndexTypeHandler extends PointTypeHandler {


    public NoaaPsdMonthlyClimateIndexTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        super.initializeNewEntry(entry);
        // override to set the missing value from the file/url
        String loc = entry.getResource().getPath();
        Object[] values = entry.getValues();
        /* Values are:
         *    0 - number of points
         *    1 - properties string
         *    2 - missing value
         *    3 - units
         */
        double missingValue = readMissingValue(loc);
        if (!Double.isNaN(missingValue)) {
            values[2] = new Double(missingValue);
            entry.setValues(values);
        }
    }
    
    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    public RecordFile doMakeRecordFile(Entry entry) throws Exception {
        String name = entry.getName();
        String loc = entry.getResource().getPath();
        String description = entry.getDescription();
        if (description == null || description.isEmpty()) {
            description = name;
        }
        Object[] values = entry.getValues();
        /* Values are:
         *    0 - number of points
         *    1 - properties string
         *    2 - missing value
         *    3 - units
         */
        RecordFile myRF = new MultiMonthFile(loc, name, description, (String) values[3], (Double) values[2]);
        return myRF;
    }
    
    private double readMissingValue(String loc) throws Exception {
        double missing = Double.NaN;
        String contents = IOUtil.readContents(loc);
        List<String> lines = StringUtil.split(contents,"\n",true,true);
        Iterator<String> iter = lines.iterator();
        String header = iter.next();
        List<String> years = StringUtil.split(header," ",true,true);
        if (years.size() < 2) {
            throw new Exception("can't find time range in: "+header);
        }
        int startYear = Integer.parseInt(years.get(0));
        int endYear = Integer.parseInt(years.get(1));
        int numYears = endYear-startYear+1;
        for (int i = 0; i < numYears; i++) {
            iter.next();
        }
        // Read in the missing value
        String missingStr = iter.next().trim();
        missing = Misc.parseNumber(missingStr);
        return missing;
    }
    

}
