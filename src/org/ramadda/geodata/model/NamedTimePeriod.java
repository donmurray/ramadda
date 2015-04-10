/*
 * Copyright (c) 2008-2015 Geode Systems LLC
 * This Software is licensed under the Geode Systems RAMADDA License available in the source distribution in the file 
 * ramadda_license.txt. The above copyright notice shall be included in all copies or substantial portions of the Software.
 */


package org.ramadda.geodata.model;

public class NamedTimePeriod {
    
    private String id;
    private String name;
    private String group;
    private int startMonth;
    private int endMonth;
    private String years;

    public NamedTimePeriod(String id, String name, String group, int startMonth, int endMonth, String years) {
        this.id = id;
        this.name = name;
        this.group = group;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.years = years;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getGroup() {
        return group;
    }
    
    public int getStartMonth() {
        return startMonth;
    }
    
    public int getEndMonth() {
        return endMonth;
    }
    
    public String getYears() {
        return years;
    }
    /**
     * Is this in the group?
     *
     * @param group group
     *
     * @return true if in group or group is null
     */
    public boolean isGroup(String group) {
        if (group == null || group.equals("all")) {
            return true;
        }

        return this.group.equals(group);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(";");
        sb.append(name);
        sb.append(";");
        sb.append(startMonth);
        sb.append(";");
        sb.append(endMonth);
        sb.append(";");
        sb.append(years);
        return sb.toString();
    }
}
