/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */

// $Id: StringUtil.java,v 1.53 2007/06/01 17:02:44 jeffmc Exp $


package org.ramadda.util;


import java.util.ArrayList;
import java.util.List;


/**
 */

public class FormInfo {

    /** _more_ */
    List<Constraint> constraints = new ArrayList<Constraint>();

    /**
     * _more_
     */
    public FormInfo() {}



    /**
     * _more_
     *
     * @return _more_
     */
    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * _more_
     *
     * @param js _more_
     *
     * @throws Exception _more_
     */
    public void addJavascriptValidation(Appendable js) throws Exception {
        for (Constraint constraint : constraints) {
            constraint.addJavascriptValidation(js);
        }
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param length _more_
     */
    public void addSizeValidation(String label, String id, int length) {
        constraints.add(new MaxLength(label, id, length));
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     */
    public void addRequiredValidation(String label, String id) {
        constraints.add(new Required(label, id));
    }


    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param min _more_
     */
    public void addMinValidation(String label, String id, double min) {
        constraints.add(new Value(label, id, min, true));
    }

    /**
     * _more_
     *
     * @param label _more_
     * @param id _more_
     * @param max _more_
     */
    public void addMaxValidation(String label, String id, double max) {
        constraints.add(new Value(label, id, max, false));
    }

    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...
     */
    public static class Constraint {

        /** _more_ */
        public String label;

        /** _more_ */
        public String id;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         */
        public Constraint(String label, String id) {
            this.label = label;
            this.id    = id;
        }

        /**
         * _more_
         *
         * @param js _more_
         *
         * @throws Exception _more_
         */
        public void addJavascriptValidation(Appendable js) throws Exception {}

        /**
         * _more_
         *
         * @param js _more_
         * @param message _more_
         */
        public void error(Appendable js, String message) {
            Utils.append(js,
                         HtmlUtils.call("alert", HtmlUtils.squote(message)));
            Utils.append(js, "event.preventDefault();\n");
            Utils.append(js, "return;\n");
        }

    }



    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...
     */
    public static class Value extends Constraint {

        /** _more_ */
        double value;

        /** _more_ */
        boolean min = true;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         * @param value _more_
         * @param min _more_
         */
        public Value(String label, String id, double value, boolean min) {
            super(label, id);
            this.value = value;
            this.min   = min;
        }

        /**
         * _more_
         *
         * @param js _more_
         *
         * @throws Exception _more_
         */
        @Override
        public void addJavascriptValidation(Appendable js) throws Exception {
            Utils.append(js,
                         "if(!GuiUtils.inputValueOk(" + HtmlUtils.squote(id)
                         + "," + value + "," + (min
                    ? "true"
                    : "false") + ")) {\n");
            String message;
            if (min) {
                message = "Error: " + label + " is < " + value;
            } else {
                message = "Error: " + label + " is > " + value;
            }
            error(js, message);
            Utils.append(js, "}\n");
        }



    }


    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...
     */
    public static class MaxLength extends Constraint {

        /** _more_ */
        public int length;

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         * @param length _more_
         */
        public MaxLength(String label, String id, int length) {
            super(label, id);
            this.length = length;
        }

        /**
         * _more_
         *
         * @param js _more_
         *
         * @throws Exception _more_
         */
        @Override
        public void addJavascriptValidation(Appendable js) throws Exception {
            Utils.append(js,
                         "if(!GuiUtils.inputLengthOk(" + HtmlUtils.squote(id)
                         + "," + length + ")) {\n");
            String message = "Error: " + label
                             + " is too long. Max length is " + length;
            error(js, message);
            Utils.append(js, "}\n");
        }


    }



    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Oct 31, '13
     * @author         Enter your name here...
     */
    public static class Required extends Constraint {

        /**
         * _more_
         *
         * @param label _more_
         * @param id _more_
         */
        public Required(String label, String id) {
            super(label, id);
        }

        /**
         * _more_
         *
         * @param js _more_
         *
         * @throws Exception _more_
         */
        @Override
        public void addJavascriptValidation(Appendable js) throws Exception {
            js.append("if(!GuiUtils.inputIsRequired(" + HtmlUtils.squote(id)
                      + ")) {\n");
            String message = "Error: " + label + " is required";
            error(js, message);
            js.append("}\n");
        }


    }

}
