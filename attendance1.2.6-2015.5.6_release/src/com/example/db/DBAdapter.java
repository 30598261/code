package com.example.db;

import com.ebanswers.object.Person;
import com.ebanswers.object.PersonEx;

import android.database.DatabaseUtils.InsertHelper;  
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author Wuyexiong
 */
public class DBAdapter
{
	public static String TAG="DBAdapter";
    public static final String DB_ACTION="db_action";//LogCat
    private Cursor resul1;
    private static final String DB_NAME="/data/data/com.ebanswers.attendance/databases/data.db";//数据库名
    private static final String DB_TABLE_PERSON="Person";//数据库表名      用户管理
    private static final String DB_TABLE_ATTENDANCE="Attendance";//数据库表名        考勤
    private static final String DB_TABLE_DEPARTMENT="Department";//数据库表名     部门管理
    private static final String DB_TABLE_SORT_7DAYS="Sort7Days";
    private static final String DB_TABLE_SORT_30DAYS="Sort30Days";
    private static final String DB_TABLE_SORT_90DAYS="Sort90Days";
    private static final String DB_TABLE_SORT_365DAYS="Sort365Days";
    private static final String DB_TABLE_SORT_ALLDAYS="SortAllDays";
    private static final String KEY_SORT_LEN="timeLen";
    private static final int    DB_VERSION=2;//数据库版本号
    
 
    //Person 表
    public static final String KEY_PERSON_AUTOID = "autoID";
    public static final String KEY_PERSON_ID = "id";  //表属性    指纹id号
    public static final String KEY_PERSON_NAME = "name";//表属性          名字
    public static final String KEY_PERSON_GENDER  = "Gender";//表属性                          性别
    public static final String KEY_PERSON_DEPARTMENT= "Department";//表属性        部门
    public static final String KEY_PERSON_ADMIN= "Admin";//表属性        权限
    
    //Person表扩充
    public static final String KEY_PERSON_BIRTHDAY= "birthday";//表属性        生日
    public static final String KEY_PERSON_BIRTHDAY_SWITCH="birthdaySwitch";// 生日是否提示
    public static final String KEY_PERSON_NUM = "Num"; //班次号
    public static final String KEY_PERSON_ATTENDANCETIMES = "attendanceTimes"; //班次号
    public static final String KEY_PERSON_MSG = "msg"; //班次号
    public static final String KEY_PERSON_TEL = "tel"; //班次号
    public static final String KEY_PERSON_MALE = "male"; //班次号
    
    //Department表
    public static final String KEY_DEPARTMENT_ID = "autoID";  //表属性ID
    public static final String KEY_DEPARTMENT_DEPARTID = "departID";  //表属性ID
    public static final String KEY_DEPARTMENT_DEPARTNAME = "departName";  //表属性ID
    public static final String KEY_DEPARTMENT_DEPARTMANAGE = "departManage";  //表属性ID
    public static final String KEY_DEPARTMENT_DEPARTINTRO = "DepartIntro";  //表属性ID
   
    
    //Attendance表
    public static final String KEY_ATTENDANCE_AUTOID = "autoID";  //表属性ID
    public static final String KEY_ATTENDANCE_ID = "id";  //表属性ID
    public static final String KEY_ATTENDANCE_TIME= "IO_Time";  //表属性ID
    public static final String KEY_ATTENDANCE_DATE= "IO_Date";  //表属性ID
    public static final String KEY_ATTENDANCE_DATETIME= "IO_DateTime";  //表属性ID
    public static final String KEY_ATTENDANCE_YEAR= "IO_Year";  //表属性ID
    public static final String KEY_ATTENDANCE_MONTH= "IO_Month";  //表属性ID
    public static final String KEY_ATTENDANCE_DAY= "IO_Day";  //表属性ID
    public static final String KEY_ATTENDANCE_HOUR= "IO_Hour";  //表属性ID
    public static final String KEY_ATTENDANCE_MINUTE= "IO_Minute";  //表属性ID
    public static final String KEY_ATTENDANCE_SECOND= "IO_Second";  //表属性ID

    
   
    private static final String DB_CREATE_PERSON = "CREATE TABLE if not exists "+DB_TABLE_PERSON
    												+" ("+KEY_PERSON_AUTOID+" integer primary key autoincrement, "
    												+KEY_PERSON_ID+" integer ,"
    												+KEY_PERSON_GENDER+"  integer, "
    												+KEY_PERSON_ADMIN+"  integer, "
    												+KEY_PERSON_NUM+" text not null, "
    												+KEY_PERSON_BIRTHDAY_SWITCH+" integer, "
    												+KEY_PERSON_ATTENDANCETIMES+" integer, "
    												+KEY_PERSON_MALE+" text not null, "
    												+KEY_PERSON_BIRTHDAY+" text not null, "
    												+KEY_PERSON_TEL+" text not null, "
    												+KEY_PERSON_MSG+" text not null, "
    												+KEY_PERSON_DEPARTMENT+" text not null, "
        											+KEY_PERSON_NAME+" text not null);";
    
    private static final String DB_CREATE_DEPARTMENT = "CREATE TABLE if not exists "+DB_TABLE_DEPARTMENT
    											+" ("+KEY_DEPARTMENT_ID+" integer primary key autoincrement, "
    											+KEY_DEPARTMENT_DEPARTID+" integer, "
    											+KEY_DEPARTMENT_DEPARTNAME+" integer, "
    											+KEY_DEPARTMENT_DEPARTINTRO+" integer, "
    											+KEY_DEPARTMENT_DEPARTMANAGE+" text not null);";

    private static final String DB_CREATE_ATTENDANCE = "CREATE TABLE if not exists "+DB_TABLE_ATTENDANCE
    												+" ("+KEY_ATTENDANCE_AUTOID+" integer primary key autoincrement, "
													+KEY_ATTENDANCE_ID+" interger, "
													+KEY_ATTENDANCE_YEAR+" interger, "
													+KEY_ATTENDANCE_MONTH+" interger, "
													+KEY_ATTENDANCE_DAY+" interger, "
													+KEY_ATTENDANCE_HOUR+" interger, "
													+KEY_ATTENDANCE_MINUTE+" interger, "
													+KEY_ATTENDANCE_SECOND+" interger, "
													+KEY_ATTENDANCE_DATE+" text not null, "
													+KEY_ATTENDANCE_TIME+" text not null, "
													+KEY_ATTENDANCE_DATETIME+" datetime DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime')));";
    
    private static final String DB_CREATE_ATTENDANCE_INDEX = "CREATE index  if not exists id_index on Attendance(id);";
    private static final String DB_CREATE_PERSON_INDEX = "CREATE index  if not exists id_index on Person(id);";
       
    private static final String DB_CREATE_SORT_7DAYS_TABLE = "CREATE TABLE if not exists "+DB_TABLE_SORT_7DAYS
   														+" ("+KEY_ATTENDANCE_AUTOID+" integer primary key autoincrement, "
														+KEY_PERSON_NAME+" text not null, "
														+KEY_SORT_LEN+" text not null, "
														+KEY_PERSON_DEPARTMENT+" text not null);";
    private static final String DB_CREATE_SORT_30DAYS_TABLE = "CREATE TABLE if not exists "+DB_TABLE_SORT_30DAYS
    													+" ("+KEY_ATTENDANCE_AUTOID+" integer primary key autoincrement, "
														+KEY_PERSON_NAME+" text not null, "
														+KEY_SORT_LEN+" text not null, "
														+KEY_PERSON_DEPARTMENT+" text not null);";
    private static final String DB_CREATE_SORT_90DAYS_TABLE = "CREATE TABLE if not exists "+DB_TABLE_SORT_90DAYS
														+" ("
														+KEY_PERSON_NAME+" text not null, "
														+KEY_SORT_LEN+" text not null, "
														+KEY_PERSON_DEPARTMENT+" text not null);";
    private static final String DB_CREATE_SORT_365DAYS_TABLE = "CREATE TABLE if not exists "+DB_TABLE_SORT_365DAYS
    													+" ("+KEY_ATTENDANCE_AUTOID+" integer primary key autoincrement, "
														+KEY_PERSON_NAME+" text not null, "
														+KEY_SORT_LEN+" text not null, "
														+KEY_PERSON_DEPARTMENT+" text not null);";
    private static final String DB_CREATE_SORT_ALLDAYS_TABLE = "CREATE TABLE if not exists "+DB_TABLE_SORT_ALLDAYS
														+" ("+KEY_ATTENDANCE_AUTOID+" integer primary key autoincrement,  "
														+KEY_PERSON_NAME+" text not null, "
														+KEY_SORT_LEN+" text not null, "
														+KEY_PERSON_DEPARTMENT+" text not null);";
												 
    
    
    public SQLiteDatabase db=null ;
    private Context xContext ;
    private DBOpenHelper dbOpenHelper = null ;
    public DBAdapter(Context context)
    {
        xContext = context ;
    }
    
    /** 空间不够存储的时候设为只读
     * @throws SQLiteException
     */
    public void open() throws SQLiteException
    {
    	dbOpenHelper = new DBOpenHelper(xContext, DB_NAME, null, DB_VERSION);
	    try
	    {
	    	db = dbOpenHelper.getWritableDatabase();
	    }
	    catch (SQLiteException e)
	    {
	    	db = dbOpenHelper.getReadableDatabase();
	    }
    }
    
    public void close()
    {
        if(db != null)
        {
        	if(resul1!=null)
        	{
        		if (!resul1.isClosed()) {
        			resul1.close();
        		}
        	}
            db.close();
            db = null;
        }
    }
    
    public void createDB()
    {
    	if(dbOpenHelper!=null &&db!=null)
    	{
    	}
    	else
    	{
    		open();    		
    	}
    	dbOpenHelper.onCreate(db);
    }
  
    public void CreateNew()
    {
    	dbOpenHelper.CreateNew(db);
    }
    
    public void SetDepartment()
    {
    	dbOpenHelper.SetDepartment(db);
    }
    
    //表操作
    /**
     * 删除一条数据
     * @param id
     * @return
     */
    public long deleteOneData(String table_name, long id, String key)
    {
        return db.delete(table_name, key+"="+id, null );
    }
    
    public long deleteOneDataName(String table_name, String name, String key)
    {
        return db.delete(table_name, key+"='"+name+"'", null );
    }

    /**
     * 删除所有数据
     * @return
     */
    public long deleteAllData()
    {
    	 xContext.deleteDatabase(DB_NAME);
    	 return 1;
    }
    
    public long deleteTable(String table_name)
    {
        return db.delete(table_name, null, null );
    }
    
    /**
     * 向表中添加一条收藏数据
     * @param person
     * @return
     */
    public long insertPerson(String name, int id, int gender, String department, int admin)
    { 	
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_PERSON_NAME, name);
        newValues.put(KEY_PERSON_ID, id);
        newValues.put(KEY_PERSON_GENDER, gender);
        newValues.put(KEY_PERSON_ADMIN, admin);
        newValues.put(KEY_PERSON_DEPARTMENT, department);
 
        return db.insert(DB_TABLE_PERSON, null, newValues);
    }
    
    public long insertPersonEx(String name, int id, int gender, String department, int admin)
    { 	
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_PERSON_NAME, name);
        newValues.put(KEY_PERSON_ID, id);
        newValues.put(KEY_PERSON_GENDER, gender);
        newValues.put(KEY_PERSON_ADMIN, admin);
        newValues.put(KEY_PERSON_DEPARTMENT, department);
        newValues.put(KEY_PERSON_MALE, "");    
        newValues.put(KEY_PERSON_BIRTHDAY, "");
        newValues.put(KEY_PERSON_TEL, "");
        newValues.put(KEY_PERSON_NUM, "");
        newValues.put(KEY_PERSON_MSG, "生日快乐！");
        newValues.put(KEY_PERSON_BIRTHDAY_SWITCH, 1);
        
        return db.insert(DB_TABLE_PERSON, null, newValues);
    }
    

    /**x
     * 根据id查询数据的代码
     * @param id
     * @return
     */
    public Person[] queryPersonOneData(long id)
    {	  	
        Cursor result = db.query(DB_TABLE_PERSON, new String[] { KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
        		KEY_PERSON_DEPARTMENT,KEY_PERSON_ADMIN}, KEY_PERSON_ID+"="+id, null, null, null, null, null);
                
        return ConvertToPerson(result) ;
    }
    /**
     * 查询全部数据的代码
     * @return
     */
    public Person[] queryPersonAllData()
    {
        Cursor result = db.query(DB_TABLE_PERSON, new String[] {KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
        		KEY_PERSON_DEPARTMENT, KEY_PERSON_ADMIN}, null, null, null, null, null, null);
        return ConvertToPerson(result);
    }
    
    public PersonEx[] queryPersonExAllData()
    {        
        Cursor result = db.query(DB_TABLE_PERSON, new String[] { KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
        		KEY_PERSON_DEPARTMENT,KEY_PERSON_ADMIN, KEY_PERSON_NUM, KEY_PERSON_MALE, KEY_PERSON_ATTENDANCETIMES, KEY_PERSON_MSG, KEY_PERSON_BIRTHDAY_SWITCH,
        		KEY_PERSON_BIRTHDAY,KEY_PERSON_TEL}, null, null, null, null, null, null);
        return ConvertToPersonEx(result);
    }
    
    
    public long updatePersonOneData(long id ,Person people)
    {
    	
        ContentValues newValues = new ContentValues();
        
        newValues.put(KEY_PERSON_ID, people.id);
        newValues.put(KEY_PERSON_NAME, people.name);
        newValues.put(KEY_PERSON_GENDER, people.Gender);
        newValues.put(KEY_PERSON_ADMIN, people.Admin);
        newValues.put(KEY_PERSON_DEPARTMENT, people.Department);
        
        return db.update(DB_TABLE_PERSON, newValues, KEY_PERSON_ID+"="+id, null);
    }
    
    public long updatePersonExData(long id ,PersonEx people)
    {
    	
        ContentValues newValues = new ContentValues();
        
        newValues.put(KEY_PERSON_NAME, people.name);
        newValues.put(KEY_PERSON_GENDER, people.Gender);
        newValues.put(KEY_PERSON_DEPARTMENT, people.Department);
        newValues.put(KEY_PERSON_NUM, people.Num);
        newValues.put(KEY_PERSON_ATTENDANCETIMES, people.attendance_times);
        newValues.put(KEY_PERSON_MALE, people.male);
        newValues.put(KEY_PERSON_MSG, people.msg);
        newValues.put(KEY_PERSON_BIRTHDAY_SWITCH, people.birthdaySwitch);
        newValues.put(KEY_PERSON_BIRTHDAY, people.Birthday);
        newValues.put(KEY_PERSON_TEL, people.tel);
        
      //  Log.e(TAG, "id:"+people.id+" name:"+people.name+" Gender:"+people.Gender+" department:"+people.Department+" num :"+people.Num+
      //  		" attendancetimes:"+people.attendance_times+" male:"+people.male+" msg:"+people.msg+" switch:"+people.birthdaySwitch+" birth:"+people.Birthday);
        return db.update(DB_TABLE_PERSON, newValues, "name=?", new String[]{people.name});
    }
    
    
    public PersonEx[] queryPersonExOneData(long id)
    {	  	
        Cursor result = db.query(DB_TABLE_PERSON, new String[] { KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
        		KEY_PERSON_DEPARTMENT,KEY_PERSON_ADMIN, KEY_PERSON_NUM, KEY_PERSON_MALE, KEY_PERSON_ATTENDANCETIMES, KEY_PERSON_MSG, KEY_PERSON_BIRTHDAY_SWITCH,
        		KEY_PERSON_BIRTHDAY,KEY_PERSON_TEL}, KEY_PERSON_ID+"="+id, null, null, null, null, null);
                
        return ConvertToPersonEx(result) ;
    }
    
    public long updateDepartment(String name, String department)
    {
        ContentValues newValues = new ContentValues();
        
        newValues.put(KEY_PERSON_DEPARTMENT, department);
        return db.update(DB_TABLE_PERSON, newValues, KEY_PERSON_NAME+"="+name, null);
    }
    
    public long updateAttendanceOneData(long id , String name)
    {
    	
        ContentValues newValues = new ContentValues();
        newValues.put("NAME", name);
        return db.update("Attendance", newValues, KEY_PERSON_ID+"="+id, null);
    }
    
    private Person[] ConvertToPerson(Cursor cursor)
    {
        int resultCounts = cursor.getCount();
        if(resultCounts == 0 || !cursor.moveToFirst())
        {
            return null ;
        }
        Person[] m_Person = new Person[resultCounts];
        for (int i = 0; i < resultCounts; i++)
        {
        	m_Person[i] = new Person();
        	m_Person[i].autoID = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_AUTOID));
        	m_Person[i].id = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_ID));
        	m_Person[i].Admin = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_ADMIN));
        	m_Person[i].name = cursor.getString(cursor.getColumnIndex(KEY_PERSON_NAME));
        	m_Person[i].Gender  = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_GENDER));
        	m_Person[i].Department = cursor.getString(cursor.getColumnIndex(KEY_PERSON_DEPARTMENT));
        //    Log.i(DB_ACTION, "Person "+i+m_Person[i].toString());
            cursor.moveToNext();
        }
        return m_Person;
    }
    
    
    private PersonEx[] ConvertToPersonEx(Cursor cursor)
    {
        int resultCounts = cursor.getCount();
        if(resultCounts == 0 || !cursor.moveToFirst())
        {
            return null ;
        }
        PersonEx[] m_PersonEx = new PersonEx[resultCounts];
        for (int i = 0; i < resultCounts; i++)
        {
        	m_PersonEx[i] = new PersonEx();
        	m_PersonEx[i].autoID = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_AUTOID));
        	m_PersonEx[i].id = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_ID));
        	m_PersonEx[i].Admin = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_ADMIN));
        	m_PersonEx[i].name = cursor.getString(cursor.getColumnIndex(KEY_PERSON_NAME));
        	m_PersonEx[i].Gender  = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_GENDER));
        	m_PersonEx[i].Department = cursor.getString(cursor.getColumnIndex(KEY_PERSON_DEPARTMENT));
        	m_PersonEx[i].male = cursor.getString(cursor.getColumnIndex(KEY_PERSON_MALE));
        	m_PersonEx[i].Birthday = cursor.getString(cursor.getColumnIndex(KEY_PERSON_BIRTHDAY));
        	m_PersonEx[i].msg = cursor.getString(cursor.getColumnIndex(KEY_PERSON_MSG));
        	m_PersonEx[i].Num = cursor.getString(cursor.getColumnIndex(KEY_PERSON_NUM));
        	m_PersonEx[i].birthdaySwitch = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_BIRTHDAY_SWITCH));
        	m_PersonEx[i].attendance_times = cursor.getInt(cursor.getColumnIndex(KEY_PERSON_ATTENDANCETIMES));
        	m_PersonEx[i].tel = cursor.getString(cursor.getColumnIndex(KEY_PERSON_TEL));
        //    Log.i(DB_ACTION, "Person "+i+m_PersonEx[i].toString());
            cursor.moveToNext();
        }
        return m_PersonEx;
    }
       
    //department表
    private Department[] ConvertToDepartment(Cursor cursor)
    {
        int resultCounts = cursor.getCount();
        if(resultCounts == 0 || !cursor.moveToFirst())
        {
            return null ;
        }
        Department[] departMent = new Department[resultCounts];
        Log.i(DB_ACTION, "department len:"+departMent.length);
        for (int i = 0; i < resultCounts; i++)
        {
           	departMent[i] = new Department();
        	departMent[i].autoID = cursor.getInt(cursor.getColumnIndex(KEY_DEPARTMENT_ID));
        	departMent[i].departID = cursor.getInt(cursor.getColumnIndex(KEY_DEPARTMENT_DEPARTID));
        	departMent[i].departName = cursor.getString(cursor.getColumnIndex(KEY_DEPARTMENT_DEPARTNAME));
        	departMent[i].departManage = cursor.getString(cursor.getColumnIndex(KEY_DEPARTMENT_DEPARTMANAGE));
	        departMent[i].departIntro = cursor.getString(cursor.getColumnIndex(KEY_DEPARTMENT_DEPARTINTRO));
        	
            Log.i(DB_ACTION, "Department "+i+departMent[i].toString());
            cursor.moveToNext();
        }
        return departMent;
    }
    
    
    /**
     * 静态Helper类，用于建立、更新和打开数据库
     */
    private static class DBOpenHelper extends SQLiteOpenHelper
    {
        /*
         * 手动建库代码
         * */
        public DBOpenHelper(Context context, String name,
                CursorFactory factory, int version)
        {
            super(context, name, factory, version);
           // Log.e("sqlite","version : "+version);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {  //建立数据库中的表 
            db.execSQL(DB_CREATE_PERSON);
            db.execSQL(DB_CREATE_PERSON_INDEX);
        	db.execSQL(DB_CREATE_DEPARTMENT);
        	db.execSQL(DB_CREATE_ATTENDANCE);
        	db.execSQL(DB_CREATE_ATTENDANCE_INDEX);
			/*
        	db.execSQL(DB_CREATE_SORT_7DAYS_TABLE);
        	db.execSQL(DB_CREATE_SORT_30DAYS_TABLE);
        	db.execSQL(DB_CREATE_SORT_90DAYS_TABLE);
        	db.execSQL(DB_CREATE_SORT_365DAYS_TABLE);
        	db.execSQL(DB_CREATE_SORT_ALLDAYS_TABLE);
			*/
        }
        
        public boolean SetDepartment(SQLiteDatabase db)
        {		
			Cursor result = db.query(DB_TABLE_PERSON, new String[] {KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
			       KEY_PERSON_DEPARTMENT, KEY_PERSON_ADMIN}, null, null, null, null, null, null);
			int resultCounts = result.getCount();
			if(resultCounts == 0 || !result.moveToFirst())
			{
				return  true;
			}
			Person[] m_Person = new Person[resultCounts];
			for (int i = 0; i < resultCounts; i++)
			{
				m_Person[i] = new Person();
			    m_Person[i].id = result.getInt(result.getColumnIndex(KEY_PERSON_ID));
			    m_Person[i].name = result.getString(result.getColumnIndex(KEY_PERSON_NAME));
			    m_Person[i].Department = result.getString(result.getColumnIndex(KEY_PERSON_DEPARTMENT));
			    result.moveToNext();
			}	
			for(int j = 0; j<m_Person.length; j++)
			{
			    if(m_Person[j].name.equals(m_Person[j].Department)==true)
			    {
			    	ContentValues newValuesDepartment = new ContentValues();
			    	newValuesDepartment.put(KEY_PERSON_DEPARTMENT, "");
			    	db.update("Person", newValuesDepartment, KEY_ATTENDANCE_ID+"="+m_Person[j].id, null);
			    }
			}
			return false;
        }
        
        public boolean CreateNew(SQLiteDatabase db)
        {   
        	if(checkColumnExists2(db, "ATTENDANCE", "NAME")==true)
        	{
        		return true;
        	}
        	else
        	{
        		try
        		{	
        			db.execSQL("ALTER TABLE Attendance ADD NAME CHAR(20);");		
					Cursor result = db.query(DB_TABLE_PERSON, new String[] {KEY_PERSON_AUTOID, KEY_PERSON_ID, KEY_PERSON_NAME, KEY_PERSON_GENDER, 
					       KEY_PERSON_DEPARTMENT, KEY_PERSON_ADMIN}, null, null, null, null, null, null);
					int resultCounts = result.getCount();
					if(resultCounts == 0 || !result.moveToFirst())
					{
						return  true;
					}
					Person[] m_Person = new Person[resultCounts];
					for (int i = 0; i < resultCounts; i++)
					{
						m_Person[i] = new Person();
					    m_Person[i].id = result.getInt(result.getColumnIndex(KEY_PERSON_ID));
					    m_Person[i].name = result.getString(result.getColumnIndex(KEY_PERSON_NAME));
					    result.moveToNext();
					}	
					for(int j = 0; j<m_Person.length; j++)
					{
					    ContentValues newValues = new ContentValues();
					    newValues.put("NAME", m_Person[j].name);
					    db.update("Attendance", newValues, KEY_ATTENDANCE_ID+"="+m_Person[j].id, null);
					    if(m_Person[j].name.equals(m_Person[j].Department)==true)
					    {
					    	ContentValues newValuesDepartment = new ContentValues();
					    	newValuesDepartment.put(KEY_PERSON_DEPARTMENT, "");
					    	db.update("Person", newValues, KEY_ATTENDANCE_ID+"="+m_Person[j].id, null);
					    }
					}
        		}
        		catch (SQLiteException e)
        		{
	        		return false;
        		}
			}
        	return true;
        }
        
        private boolean checkColumnExists2(SQLiteDatabase db, String tableName, String columnName) 
        {
        	Cursor cursor = null;
        	try
        	{
        		cursor = db.rawQuery( "select NAME  from Attendance ", null);
        		int columnId = cursor.getColumnIndex("NAME");
            	if(columnId == -1){ // 不存在这个字段
            		return false;
            	}else {
            		return true;
            	}
        	}
        	catch (SQLiteException e)
        	{   //不存在这个字段
        		System.out.println("### 不存在这个字段");
        	}
        	return false;
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion)
        {
            //函数在数据库需要升级时被调用，
            //一般用来删除旧的数据库表，
            //并将数据转移到新版本的数据库表中
        
        	if(newVersion == 2)
        	{
	        //	 Log.i(DB_ACTION, "Upgrade  versionss111  : "+newVersion);
	        	 _db.execSQL("ALTER TABLE Person ADD birthdaySwitch integer;");	
	        	 _db.execSQL("ALTER TABLE Person ADD attendanceTimes integer;");
	        	 _db.execSQL("ALTER TABLE Person ADD Num text ;");
	        	 _db.execSQL("ALTER TABLE Person ADD male text ;");
	        	 _db.execSQL("ALTER TABLE Person ADD birthday text  ;");
	        	 _db.execSQL("ALTER TABLE Person ADD tel text ;");
	        	 _db.execSQL("ALTER TABLE Person ADD msg text;");
        	}
        }
    }
    
   
    /**
     * 向表中添加一条信息
     * @param people
     * @return
     */
    public long insertDepartment(String fileName)
    { 	
    	ContentValues newValues = new ContentValues();
        newValues.put(KEY_DEPARTMENT_ID, fileName);
        newValues.put(KEY_DEPARTMENT_DEPARTID, fileName);
        newValues.put(KEY_DEPARTMENT_DEPARTNAME, fileName);
        newValues.put(KEY_DEPARTMENT_DEPARTMANAGE, fileName);
        newValues.put(KEY_DEPARTMENT_DEPARTINTRO, fileName);
   
        return db.insert(DB_TABLE_DEPARTMENT, null, newValues);
    }
    
    /**
     * 查询全部数据的代码
     * @return
     */
    public Department[] queryAllDataDepartment()
    {
    	resul1 = db.query(DB_TABLE_PERSON, new String[] {KEY_DEPARTMENT_ID, KEY_DEPARTMENT_DEPARTID, KEY_DEPARTMENT_DEPARTNAME, KEY_DEPARTMENT_DEPARTMANAGE, 
    			KEY_DEPARTMENT_DEPARTINTRO}, null, null, null, null, null, null);
        return ConvertToDepartment(resul1);
    }
    
    /**
     * 根据name查询数据的代码
     * @param id
     * @return
     */
    public Department[] queryOneDataNameDepartment(String name)
    {
    	resul1 = db.query(DB_TABLE_PERSON, new String[] {KEY_DEPARTMENT_ID, KEY_DEPARTMENT_DEPARTNAME, KEY_DEPARTMENT_DEPARTMANAGE, 
    			KEY_DEPARTMENT_DEPARTINTRO}, KEY_DEPARTMENT_DEPARTNAME+"="+name, null, null, null, null);
        return ConvertToDepartment(resul1) ;
    }
    
    
    //Attendance表     考勤记录表
    private Attendance[] ConvertToAttendance(Cursor cursor)
    {
        int resultCounts = cursor.getCount();
        if(resultCounts == 0 || !cursor.moveToFirst())
        {
            return null ;
        }
        Attendance[] attenDance = new Attendance[resultCounts];
        for (int i = 0; i < resultCounts; i++)
        {   
        	attenDance[i] = new Attendance();
        	
        	attenDance[i].autoID = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_AUTOID));
        	attenDance[i].ID = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_ID));
        	attenDance[i].year = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_YEAR));
        	attenDance[i].month = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_MONTH));
        	attenDance[i].day = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_DAY));
        	attenDance[i].hour = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_HOUR));
        	attenDance[i].minute = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_MINUTE));
        	attenDance[i].second = cursor.getInt(cursor.getColumnIndex(KEY_ATTENDANCE_SECOND));
        	attenDance[i].date = cursor.getString(cursor.getColumnIndex(KEY_ATTENDANCE_DATE));
        	attenDance[i].time = cursor.getString(cursor.getColumnIndex(KEY_ATTENDANCE_TIME));
        	attenDance[i].dateTime = cursor.getString(cursor.getColumnIndex(KEY_ATTENDANCE_DATETIME));
        	attenDance[i].NAME = cursor.getString(cursor.getColumnIndex("NAME"));
        	
        //   Log.i(DB_ACTION, "Attendacne "+i+attenDance[i].toString());
            cursor.moveToNext();
        }
        return attenDance;
    }
    
  
  
    /**
     * 向表中添加一条信息
     * @param attendance table
     */
    public long insertAttendance(int id, int year, int month, int day, int hour, 
    		int minute, int second,  String date, String time, String dateTime, String Name)
    { 	
    	ContentValues newValues = new ContentValues();
    	newValues.put(KEY_ATTENDANCE_ID, id);
    	newValues.put(KEY_ATTENDANCE_YEAR, year);
    	newValues.put(KEY_ATTENDANCE_MONTH, month);
    	newValues.put(KEY_ATTENDANCE_DAY, day);
    	newValues.put(KEY_ATTENDANCE_HOUR, hour);
    	newValues.put(KEY_ATTENDANCE_MINUTE, minute);
    	newValues.put(KEY_ATTENDANCE_SECOND, second);
    	newValues.put(KEY_ATTENDANCE_DATE, date);
    	newValues.put(KEY_ATTENDANCE_TIME, time);
    	newValues.put(KEY_ATTENDANCE_DATETIME, dateTime);
    	newValues.put("NAME", Name);
        return db.insert(DB_TABLE_ATTENDANCE, null, newValues);
    }
    
    /**
     * 查询全部数据的代码  attendance table
     */
    public Attendance[] queryAllDataAttendance()
    {
    	resul1 = db.query(DB_TABLE_ATTENDANCE, new String[] {KEY_ATTENDANCE_AUTOID, KEY_ATTENDANCE_ID, 
    			KEY_ATTENDANCE_YEAR, KEY_ATTENDANCE_MONTH, KEY_ATTENDANCE_DAY, KEY_ATTENDANCE_HOUR, 
    			KEY_ATTENDANCE_MINUTE, KEY_ATTENDANCE_SECOND, KEY_ATTENDANCE_DATE, KEY_ATTENDANCE_TIME, KEY_ATTENDANCE_DATETIME}, 
    			null, null, null, null, null);
        return ConvertToAttendance(resul1);
    }
    
    /**
     * 根据id查询数据的代码  attendance table
     */
    public Attendance[] queryOneDataNameAttendance(int id)
    {
    	resul1 = db.query(DB_TABLE_ATTENDANCE, new String[] {KEY_ATTENDANCE_AUTOID, KEY_ATTENDANCE_ID, 
    			KEY_ATTENDANCE_YEAR, KEY_ATTENDANCE_MONTH, KEY_ATTENDANCE_DAY, KEY_ATTENDANCE_HOUR, 
    			KEY_ATTENDANCE_MINUTE, KEY_ATTENDANCE_SECOND, KEY_ATTENDANCE_DATE, KEY_ATTENDANCE_TIME,KEY_ATTENDANCE_DATETIME}, 
    			KEY_ATTENDANCE_ID+"="+id, null,  null, null,null);
        return ConvertToAttendance(resul1) ;
    }
    
    public Long getCount(String str) {
    	  Cursor cursor = db.rawQuery(str, null);
    	  cursor.moveToFirst();
    	  Long count = cursor.getLong(0);
    	  cursor.close();
    	  return count;
    	 }
    
    public Person[] getQuery(String str)
    {
      Cursor cursor = db.rawQuery(str, null);;  
  	  return ConvertToPerson(cursor);
    }
    
    public Attendance[] getAttendanceQuery(String str)
    {
      Cursor cursor1 = db.rawQuery(str, null);;  
     // System.out.println("#### count : "+cursor.getLong(0));
  	  return ConvertToAttendance(cursor1);
    }
    
    public int exec(String str)
    {
    	db.execSQL(str);
		return 0;
    }
 }
