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
        String result ="编号: "+this.autoID+"," 
        +"ID : "+this.id+","
        +"姓名:"+this.name+","
        +"性别:"+this.Gender+","
        +"权限:"+this.Admin+","
        +"部门:"+this.Department;
        return result;
    }
}