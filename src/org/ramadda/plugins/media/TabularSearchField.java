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

package org.ramadda.plugins.media;



/**
 */
public class TabularSearchField {

    /** _more_ */
    private String name;

    /** _more_ */
    private String value;


    /**
     * _more_
     *
     * @param name _more_
     */
    public TabularSearchField(String name) {
        this.name = name;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public TabularSearchField(String name, String value) {
        this.name  = name;
        this.value = value;
    }

    public String toString() {
        return "search field: name=" + name +" value=" + value;
    }

    public String getUrlArg() {
        return "search_table_" + name;
    }

    /**
       Set the Name property.

       @param value The new value for Name
    **/
    public void setName (String value) {
	name = value;
    }

    /**
       Get the Name property.

       @return The Name
    **/
    public String getName () {
	return name;
    }

    /**
       Set the Value property.

       @param value The new value for Value
    **/
    public void setValue (String value) {
	value = value;
    }

    /**
       Get the Value property.

       @return The Value
    **/
    public String getValue () {
	return value;
    }


}
