package com.ebanswers.object;

import android.util.Log;

public class Person
{
    public int id = -1 ;
    public int autoID = -1;
    public String name;
    public int Gender =-1;
    public int Admin =-1;
    public String Department;
   
    
    @Override
    public String toString()
    {
        String result ="���: "+this.autoID+"," 
        +"ID : "+this.id+","
        +"����:"+this.name+","
        +"�Ա�:"+this.Gender+","
        +"Ȩ��:"+this.Admin+","
        +"����:"+this.Department;
        return result;
    }
}