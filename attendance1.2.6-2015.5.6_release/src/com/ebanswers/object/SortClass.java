package com.ebanswers.object;

import java.util.Comparator;

public class SortClass implements Comparator {
	 @Override  
	    public int compare(Object lhs, Object rhs) {  
	        SortAttendanceClass a = (SortAttendanceClass) lhs;  
	        SortAttendanceClass b = (SortAttendanceClass) rhs;  
	  
	        return (int) b.timedaysLen - (int)a.timedaysLen;  
	    }  
}
