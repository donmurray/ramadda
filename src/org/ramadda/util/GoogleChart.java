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

package org.ramadda.util;


/**
 */
public class GoogleChart {

    /**
     * _more_
     *
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public static void addChartImport(Appendable sb) throws Exception {
        StringBuilder js = new StringBuilder();
        js.append(
            "if ((typeof ramaddaLoadedGoogleCharts === 'undefined')) {\n");
        js.append("ramaddaLoadedGoogleCharts=true;\n");
        js.append(
            "google.load('visualization', '1.0', {'packages':['corechart']});\n");
        js.append(
            "google.load('visualization', '1.0', {'packages':['motionchart']});\n");

        js.append("}\n");
        sb.append(HtmlUtils.importJS("https://www.google.com/jsapi"));
        sb.append(HtmlUtils.script(js.toString()));
        //        // Set a callback to run when the Google Visualization API is loaded.
        //        google.setOnLoadCallback(drawChart);
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...
     */
    public static class DataTable {

        /**
         * _more_
         *
         * @param sb _more_
         */
        public static void init(StringBuffer sb) {
            sb.append("var data = new google.visualization.DataTable();\n");
        }

        /**
         * _more_
         *
         * @param sb _more_
         * @param type _more_
         * @param name _more_
         */
        public static void addColumn(StringBuffer sb, String type,
                                     String name) {
            //data.addColumn('string', 'Name');
            sb.append(HtmlUtils.call("data.addColumn",
                                     HtmlUtils.squote(type),
                                     HtmlUtils.squote(name)));
        }


    }



}
