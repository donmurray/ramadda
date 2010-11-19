/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.repository;


import ucar.unidata.repository.auth.*;


import ucar.unidata.repository.metadata.*;
import ucar.unidata.repository.type.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.xml.XmlEncoder;

import java.io.File;

import java.util.ArrayList;
import java.awt.geom.Rectangle2D;


import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * Class Entry _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class Entry extends Entity {

    /** _more_ */
    public static final String IDDELIMITER = ":";

    /** _more_ */
    public static final String PATHDELIMITER = "/";

    /** _more_ */
    public static final double NONGEO = -999999;

    /** _more_ */
    Object[] values;

    /** _more_ */
    private Resource resource = new Resource();

    /** _more_ */
    private String dataType;

    /** _more_ */
    private TypeHandler typeHandler;

    /** _more_ */
    private long startDate;

    /** _more_ */
    private long endDate;

    /** _more_ */
    private double south = NONGEO;

    /** _more_ */
    private double north = NONGEO;

    /** _more_ */
    private double east = NONGEO;

    /** _more_ */
    private double west = NONGEO;

    private double altitudeBottom = NONGEO;

    private double altitudeTop = NONGEO;

    /** _more_ */
    private boolean isLocalFile = false;

    /** _more_ */
    private boolean isRemoteEntry = false;

    /** _more_ */
    private String remoteServer;

    /** _more_ */
    private String icon;

    /** _more_          */
    private Hashtable properties;

    /** _more_          */
    private String propertiesString;

    private Hashtable transientProperties = new Hashtable();


    /** _more_          */
    private static XmlEncoder xmlEncoder = new XmlEncoder();




    /**
     * _more_
     */
    public Entry() {}


    /**
     * _more_
     *
     * @param id _more_
     */
    public Entry(String id) {
        setId(id);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createGeneratedEntry(Request request, String id) {
        return null;
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param typeHandler _more_
     */
    public Entry(String id, TypeHandler typeHandler) {
        this(id);
        this.typeHandler = typeHandler;
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws CloneNotSupportedException _more_
     */
    public Object clone() throws CloneNotSupportedException {
        Entry that = (Entry) super.clone();
        return that;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFullName() {
        if (getParentEntry() != null) {
            return getParentEntry().getFullName() + "/" + getName();
        }
        return getName();
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public File getFile() {
        return getTypeHandler().getFileForEntry(this);
    }



    /**
     *  _more_
     *
     *  @param template _more_
     */
    public void initWith(Entry template) {
        setName(template.getName());
        setDescription(template.getDescription());
        if (template.getMetadata() != null) {
            List<Metadata> thisMetadata = new ArrayList<Metadata>();
            for (Metadata metadata : template.getMetadata()) {
                metadata.setEntryId(getId());
                thisMetadata.add(metadata);
            }
            setMetadata(thisMetadata);
        }
        setCreateDate(template.getCreateDate());
        setChangeDate(template.getChangeDate());
        setStartDate(template.getStartDate());
        setEndDate(template.getEndDate());
        setNorth(template.getNorth());
        setSouth(template.getSouth());
        setEast(template.getEast());
        setWest(template.getWest());
    }

    public Rectangle2D.Double getBounds() {
	return  new Rectangle2D.Double(west, south,
				       east - west, north - south);
    }


    public void setBounds(Rectangle2D.Double rect) {
	west = rect.getX();
	south = rect.getY();
	east =  west+rect.getWidth();
	north =  south+rect.getHeight();
    }




    /**
     * _more_
     *
     * @param name _more_
     * @param description _more_
     * @param group _more_
     * @param user _more_
     * @param resource _more_
     * @param dataType _more_
     * @param createDate _more_
     * @param startDate _more_
     * @param endDate _more_
     * @param values _more_
     */
    public void initEntry(String name, String description, Group parentEntry,
                          User user, Resource resource, String dataType,
                          long createDate, long changeDate, long startDate, long endDate,
                          Object[] values) {
        super.init(name, description, parentEntry, user, createDate,changeDate);
        this.resource = resource;
        this.dataType = dataType;
        if ((dataType == null) || (dataType.length() == 0)) {
            this.dataType = typeHandler.getDefaultDataType();
        }
        if (this.dataType == null) {
            this.dataType = "";
        }
        this.startDate = startDate;
        this.endDate   = endDate;
        this.values    = values;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFile() {
        return (resource != null) && resource.isFile();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getInsertSql() {
        return null;
    }

    /**
     * Set the resource property.
     *
     * @param value The new value for resource
     */
    public void setResource(Resource value) {
        resource = value;
    }

    /**
     * Get the resource property.
     *
     * @return The resource
     */
    public Resource getResource() {
        return resource;
    }




    /**
     * _more_
     *
     * @param value _more_
     */
    public void setDate(long value) {
        super.setCreateDate(value);
        super.setChangeDate(value);
        setStartDate(value);
        setEndDate(value);
    }




    /**
     * Set the StartDate property.
     *
     * @param value The new value for StartDate
     */
    public void setStartDate(long value) {
        startDate = value;
    }

    /**
     * Get the StartDate property.
     *
     * @return The StartDate
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * Set the EndDate property.
     *
     * @param value The new value for EndDate
     */
    public void setEndDate(long value) {
        endDate = value;
    }

    /**
     * Get the EndDate property.
     *
     * @return The EndDate
     */
    public long getEndDate() {
        return endDate;
    }


    public boolean isTopEntry() {
        return isGroup() && (getParentEntryId() == null);
    }


    /**
     * _more_
     *
     * @return _more_
     * @deprecated use isTopEntry
     */
    public boolean isTopGroup() {
        return isTopEntry();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isGroup() {
        return this instanceof Group;
    }


    /**
     * Set the Type property.
     *
     * @param value The new value for Type
     */
    public void setTypeHandler(TypeHandler value) {
        typeHandler = value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getType() {
        return typeHandler.getType();
    }


    /**
     * Get the Type property.
     *
     * @return The Type
     */
    public TypeHandler getTypeHandler() {
        return typeHandler;
    }



    /**
     * Set the Values property.
     *
     * @param value The new value for Values
     */
    public void setValues(Object[] value) {
        values = value;
    }

    /**
     * Get the Values property.
     *
     * @return The Values
     */
    public Object[] getValues() {
        return values;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasLocationDefined() {
        if ((south != NONGEO) && (east != NONGEO) && !hasAreaDefined()) {
            return true;
        }
        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getLocation() {
        return new double[] { south, east };
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public double[] getCenter() {
        return new double[] { south + (north - south) / 2,
                              east + (west - east) / 2 };
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasAreaDefined() {
        if ((south != NONGEO) && (east != NONGEO) && (north != NONGEO)
                && (west != NONGEO)) {
            return true;
        }
        return false;
    }

    /**
     * _more_
     */
    public void trimAreaResolution() {
        double diff = (south - north);
        if (Math.abs(diff) > 1) {
            south = ((int) (south * 1000)) / 1000.0;
            north = ((int) (north * 1000)) / 1000.0;
        }
        diff = (east - west);
        if (Math.abs(diff) > 1) {
            east = ((int) (east * 1000)) / 1000.0;
            west = ((int) (west * 1000)) / 1000.0;
        }

    }

    /**
     * _more_
     */
    public void clearArea() {
        south = north = east = west = NONGEO;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        String label = super.getLabel();
        if (label.length() > 0) {
            return label;
        }
        return getTypeHandler().getLabel() + ": " + new Date(startDate);
    }

    public void setLocation(Entry that) {
        this.north = that.north;
        this.south = that.south;
        this.east = that.east;
        this.west = that.west;
        this.altitudeTop = that.altitudeTop;
        this.altitudeBottom = that.altitudeBottom;
    }


    public void setLocation(double lat, double lon, double alt) {
        this.north = lat;
        this.south = lat;
        this.east = lon;
        this.west = lon;
        this.altitudeTop = alt;
        this.altitudeBottom = alt;
    }


    /**
     * Set the South property.
     *
     * @param value The new value for South
     */
    public void setSouth(double value) {
        south = value;
    }

    /**
     * Get the South property.
     *
     * @return The South
     */
    public double getSouth() {
        return ((south == south)
                ? south
                : NONGEO);
    }

    /**
     * Set the North property.
     *
     * @param value The new value for North
     */
    public void setNorth(double value) {
        north = value;
    }

    /**
     * Get the North property.
     *
     * @return The North
     */
    public double getNorth() {
        return ((north == north)
                ? north
                : NONGEO);
    }

    /**
       Set the AltitudeTop property.

       @param value The new value for AltitudeTop
    **/
    public void setAltitudeTop (double value) {
	altitudeTop = value;
    }

    /**
       Get the AltitudeTop property.

       @return The AltitudeTop
    **/
    public double getAltitudeTop () {
	return altitudeTop;
    }

    /**
       Set the AltitudeBottom property.

       @param value The new value for AltitudeBottom
    **/
    public void setAltitudeBottom (double value) {
	altitudeBottom = value;
    }

    /**
       Get the AltitudeBottom property.

       @return The AltitudeBottom
    **/
    public double getAltitudeBottom () {
	return altitudeBottom;
    }

    public double getAltitude () {
	return altitudeTop;
    }


    public boolean hasAltitudeTop() {
        return (altitudeTop == altitudeTop) && (altitudeTop != NONGEO);
    }


    public boolean hasAltitudeBottom() {
        return (altitudeBottom == altitudeBottom) && (altitudeBottom != NONGEO);
    }


    public boolean hasAltitude() {
        return hasAltitudeTop() && hasAltitudeBottom() && (altitudeBottom== altitudeTop);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasNorth() {
        return (north == north) && (north != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasSouth() {
        return (south == south) && (south != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasEast() {
        return (east == east) && (east != NONGEO);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasWest() {
        return (west == west) && (west != NONGEO);
    }


    /**
     * Set the East property.
     *
     * @param value The new value for East
     */
    public void setEast(double value) {
        east = value;
    }

    /**
     * Get the East property.
     *
     * @return The East
     */
    public double getEast() {
        return ((east == east)
                ? east
                : NONGEO);
    }

    public double[][] getLatLonBounds() {
        return new double[][] {
            {north,north,south,south,north},
            {west,east,east,west,west}
        };
    }

    /**
     * Set the West property.
     *
     * @param value The new value for West
     */
    public void setWest(double value) {
        west = value;
    }

    /**
     * Get the West property.
     *
     * @return The West
     */
    public double getWest() {
        return ((west == west)
                ? west
                : NONGEO);
    }



    /**
     * Set the DataType property.
     *
     * @param value The new value for DataType
     */
    public void setDataType(String value) {
        dataType = value;
    }

    /**
     * Get the DataType property.
     *
     * @return The DataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Set the IsLocalFile property.
     *
     * @param value The new value for IsLocalFile
     */
    public void setIsLocalFile(boolean value) {
        isLocalFile = value;
    }

    /**
     * Get the IsLocalFile property.
     *
     * @return The IsLocalFile
     */
    public boolean getIsLocalFile() {
        return isLocalFile;
    }

    /**
     *  Set the Icon property.
     *
     *  @param value The new value for Icon
     */
    public void setIcon(String value) {
        icon = value;
    }

    /**
     *  Get the Icon property.
     *
     *  @return The Icon
     */
    public String getIcon() {
        return icon;
    }


    /**
     * Set the IsRemoteEntry property.
     *
     * @param value The new value for IsRemoteEntry
     */
    public void setIsRemoteEntry(boolean value) {
        isRemoteEntry = value;
    }

    /**
     * Get the IsRemoteEntry property.
     *
     * @return The IsRemoteEntry
     */
    public boolean getIsRemoteEntry() {
        return isRemoteEntry;
    }

    /**
     * Set the RemoteServer property.
     *
     * @param value The new value for RemoteServer
     */
    public void setRemoteServer(String value) {
        remoteServer = value;
    }

    /**
     * Get the RemoteServer property.
     *
     * @return The RemoteServer
     */
    public String getRemoteServer() {
        return remoteServer;
    }

    public Object getTransientProperty(Object key) {
        return transientProperties.get(key);
    }

    public void putTransientProperty(Object key, Object value) {
        transientProperties.put(key, value);
    }

    /**
     *  Set the Properties property.
     *
     *
     * @param key _more_
     *  @param value The new value for Properties
     *
     * @throws Exception _more_
     */
    public void putProperty(String key, Object value) throws Exception {
        getProperties(true).put(key, value);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Object getProperty(String key) throws Exception {
        Hashtable properties = getProperties();
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }


    /**
     *  Get the Properties property.
     *
     *  @return The Properties
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties() throws Exception {
        return getProperties(false);
    }


    /**
     * _more_
     *
     * @param force _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Hashtable getProperties(boolean force) throws Exception {
        if (properties == null) {
            if (propertiesString != null) {
                properties =
                    (Hashtable) xmlEncoder.toObject(propertiesString);
                propertiesString = null;
            }
            if ((properties == null) && force) {
                properties = new Hashtable();
            }
        }
        return this.properties;
    }


    /**
     *  Get the PropertiesString property.
     *
     *  @return The PropertiesString
     *
     * @throws Exception _more_
     */
    public String getPropertiesString() throws Exception {
        if (properties != null) {
            return xmlEncoder.toXml(properties);
        }
        return null;
    }



}
