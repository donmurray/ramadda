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

package org.ramadda.geodata.ogc;



/**
 */

public class WmsUtils {

    /** _more_ */
    public static final String ARG_LAYERS = "layers";

    /** _more_ */
    public static final String ARG_VERSION = "version";

    /** _more_ */
    public static final String ARG_REQUEST = "request";

    /** _more_ */
    public static final String ARG_CRS = "CRS";

    /** _more_ */
    public static final String ARG_SRS = "SRS";

    /** _more_ */
    public static final String ARG_BBOX = "BBOX";

    /** _more_ */
    public static final String ARG_FORMAT = "FORMAT";

    /** _more_ */
    public static final String ARG_WIDTH = "width";

    /** _more_ */
    public static final String ARG_HEIGHT = "height";

    /** _more_ */
    public static final String REQUEST_GETMAP = "GetMap";

    /** _more_ */
    public static final String REQUEST_GETCAPABILITIES = "GetCapabilities";

    /** _more_ */
    public static final String TAG_WMS_CAPABILITIES = "WMS_Capabilities";

    /** _more_ */
    public static final String TAG_SERVICE = "Service";

    /** _more_ */
    public static final String TAG_NAME = "Name";

    /** _more_ */
    public static final String TAG_TITLE = "Title";

    /** _more_ */
    public static final String TAG_ABSTRACT = "Abstract";

    /** _more_ */
    public static final String TAG_KEYWORDLIST = "KeywordList";

    /** _more_ */
    public static final String TAG_KEYWORD = "Keyword";

    /** _more_ */
    public static final String TAG_ONLINERESOURCE = "OnlineResource";

    /** _more_ */
    public static final String TAG_CONTACTINFORMATION = "ContactInformation";

    /** _more_ */
    public static final String TAG_CONTACTPERSONPRIMARY =
        "ContactPersonPrimary";

    /** _more_ */
    public static final String TAG_CONTACTPERSON = "ContactPerson";

    /** _more_ */
    public static final String TAG_CONTACTORGANIZATION =
        "ContactOrganization";

    /** _more_ */
    public static final String TAG_CONTACTPOSITION = "ContactPosition";

    /** _more_ */
    public static final String TAG_CONTACTADDRESS = "ContactAddress";

    /** _more_ */
    public static final String TAG_ADDRESSTYPE = "AddressType";

    /** _more_ */
    public static final String TAG_ADDRESS = "Address";

    /** _more_ */
    public static final String TAG_CITY = "City";

    /** _more_ */
    public static final String TAG_STATEORPROVINCE = "StateOrProvince";

    /** _more_ */
    public static final String TAG_POSTCODE = "PostCode";

    /** _more_ */
    public static final String TAG_COUNTRY = "Country";

    /** _more_ */
    public static final String TAG_CONTACTVOICETELEPHONE =
        "ContactVoiceTelephone";

    /** _more_ */
    public static final String TAG_CONTACTELECTRONICMAILADDRESS =
        "ContactElectronicMailAddress";

    /** _more_ */
    public static final String TAG_FEES = "Fees";

    /** _more_ */
    public static final String TAG_ACCESSCONSTRAINTS = "AccessConstraints";

    /** _more_ */
    public static final String TAG_LAYERLIMIT = "LayerLimit";

    /** _more_ */
    public static final String TAG_MAXWIDTH = "MaxWidth";

    /** _more_ */
    public static final String TAG_MAXHEIGHT = "MaxHeight";

    /** _more_ */
    public static final String TAG_CAPABILITY = "Capability";

    /** _more_ */
    public static final String TAG_REQUEST = "Request";

    /** _more_ */
    public static final String TAG_GETCAPABILITIES = "GetCapabilities";

    /** _more_ */
    public static final String TAG_FORMAT = "Format";

    /** _more_ */
    public static final String TAG_DCPTYPE = "DCPType";

    /** _more_ */
    public static final String TAG_HTTP = "HTTP";

    /** _more_ */
    public static final String TAG_GET = "Get";

    /** _more_ */
    public static final String TAG_GETMAP = "GetMap";

    /** _more_ */
    public static final String TAG_EXCEPTION = "Exception";

    /** _more_ */
    public static final String TAG_LAYER = "Layer";

    /** _more_ */
    public static final String TAG_CRS = "CRS";

    /** _more_ */
    public static final String TAG_SRS = "SRS";

    /** _more_ */
    public static final String TAG_EX_GEOGRAPHICBOUNDINGBOX =
        "EX_GeographicBoundingBox";

    /** _more_ */
    public static final String TAG_WESTBOUNDLONGITUDE = "westBoundLongitude";

    /** _more_ */
    public static final String TAG_EASTBOUNDLONGITUDE = "eastBoundLongitude";

    /** _more_ */
    public static final String TAG_SOUTHBOUNDLATITUDE = "southBoundLatitude";

    /** _more_ */
    public static final String TAG_NORTHBOUNDLATITUDE = "northBoundLatitude";

    /** _more_ */
    public static final String TAG_BOUNDINGBOX = "BoundingBox";

    /** _more_ */
    public static final String TAG_ATTRIBUTION = "Attribution";

    /** _more_ */
    public static final String TAG_DIMENSION = "Dimension";

    /** _more_ */
    public static final String ATTR_VERSION = "version";

    /** _more_ */
    public static final String ATTR_OPAQUE = "opaque";

    /** _more_ */
    public static final String ATTR_CRS = "CRS";

    /** _more_ */
    public static final String ATTR_MAXX = "maxx";

    /** _more_ */
    public static final String ATTR_MAXY = "maxy";

    /** _more_ */
    public static final String ATTR_MINX = "minx";

    /** _more_ */
    public static final String ATTR_MINY = "miny";

    /** _more_ */
    public static final String ATTR_RESX = "resx";

    /** _more_ */
    public static final String ATTR_RESY = "resy";

    /** _more_ */
    public static final String ATTR_DEFAULT = "default";

    /** _more_ */
    public static final String ATTR_MULTIPLEVALUES = "multipleValues";

    /** _more_ */
    public static final String ATTR_NAME = "name";

    /** _more_ */
    public static final String ATTR_NEARESTVALUE = "nearestValue";

    /** _more_ */
    public static final String ATTR_UNITS = "units";

    /** _more_ */
    public static final String ATTR_UNITSYMBOL = "unitSymbol";





}
