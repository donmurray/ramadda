/*
* Copyright 2008-2014 Geode Systems LLC
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

package org.ramadda.geodata.cdmdata;


import org.ramadda.data.process.*;

import org.ramadda.data.process.Service;

import org.ramadda.repository.*;
import org.ramadda.repository.job.JobManager;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.TempDir;


import org.w3c.dom.*;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Jeff McWhirter/ramadda.org
 */
public class NetcdfService extends Service {

    /**
     * ctor
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public NetcdfService(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
            CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param input _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    @Override
   public void addToForm(Request request, ServiceInput input, Appendable sb, String argPrefix,String label)
        throws Exception {



        List<Entry>          entries           = input.getEntries();
        Entry                entry             = ((entries.size() == 0)
                                                  ? null
                                                  : entries.get(0));
        if(entry==null) {
            for(ServiceArg inputArg: getInputs()) {
                if(inputArg.isEntry()) {
                    entry  =  getEntry(request,  argPrefix,  inputArg);
                    break;
                }
            }            
        }


        if(entry!=null) {
            addMetadata(request, input, entry.getResource().getPath());
        }

        super.addToForm(request, input, sb, argPrefix, label);
    }

    private void addMetadata(Request request, ServiceInput input, String path) throws Exception {
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        NetcdfDataset dataset =
            NetcdfDataset.openDataset(path);
            List<Variable>       variables  = dataset.getVariables();
            List<TwoFacedObject> coordNames = new ArrayList<TwoFacedObject>();
            List<TwoFacedObject> varNames   = new ArrayList<TwoFacedObject>();
            for (Variable var : variables) {
                if (var instanceof CoordinateAxis) {
                    coordNames.add(new TwoFacedObject(var.getName(),
                                                      var.getShortName()));
                }
                varNames.add(new TwoFacedObject(var.getName(),
                                                var.getShortName()));
            }

            dataset.close();
            input.putProperty("varNames", varNames);
            input.putProperty("coordNames", coordNames);

    }

}
