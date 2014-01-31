package org.ramadda.geodata.model;

import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessInput;
import org.ramadda.data.process.DataProcessOutput;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;

public class CDOTimeSeriesProcess extends CDODataProcess {

    public CDOTimeSeriesProcess(Repository repository) throws Exception {
        super(repository, "CDO_TIMESERIES", "Time Series");
    }
    
    @Override
    public void addToForm(Request request, DataProcessInput input,
            StringBuffer sb) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public DataProcessOutput processRequest(Request request,
            DataProcessInput input) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canHandle(DataProcessInput dpi) {
        // TODO Auto-generated method stub
        return false;
    }

}
