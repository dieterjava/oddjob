/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.tools;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.util.Clock;

/**
 *
 * @author Rob Gordon.
 */
public class ManualClock implements Clock {

    private static final Logger logger = Logger.getLogger(ManualClock.class);
    
    private Date date;

    public ManualClock(String time) {
    	setDateText(time);
    }
    
    public ManualClock() {
    	
    }
    
    public void setDateText(String time) {
        logger.debug("Setting date [" + time + "]");
        try {
        	date = DateHelper.parseDateTime(time);
        }
        catch (ParseException e) {
        	throw new RuntimeException(e);
        }
    }

    public void setDate(Date date) {
    	this.date = date;
    }
    
    public Date getDate() {
        return date;
    }
    
    @Override
    public String toString() {
    	return getClass().getSimpleName() + " " + date;
    }
}
