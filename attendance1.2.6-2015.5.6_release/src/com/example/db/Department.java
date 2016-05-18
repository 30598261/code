package com.example.db;

public class Department {
 
	public int autoID = -1;
	public int departID = -1;
	public String departName = null;
	public String departManage = null;
	public String departIntro = null;
	
	   @Override
	    public String toString()
	    {
	        String result ="编号: "+this.autoID+"," 
	        +"ID : "+this.departID+","
	        +"姓名:"+this.departName+","
	        +"性别:"+this.departManage+","
	        +"部门:"+this.departIntro;
	        return result;
	    }
}
