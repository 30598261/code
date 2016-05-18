package com.ebanswers.object;

public class attendanceTimer{
	public String m_startTime="";
	public String m_endTime="";
	public String m_overTime="";
	public int m_beginHour=0;
	public int m_beginMinute=0;
	public int m_endHour=0;
	public int m_endMinute=0;
	public int m_moreTime1=0;
	public int m_moreTime2=0;
	public int m_noLateTime=0;
	public int m_startTimeHour=0;  
	public int m_startTimeMinute=0;  //
	public int m_endTimeHour=0;
	public int m_endTimeMinute=0;
	private int midTime1, midTime2, midTime3;
	
	public int getMidTime2() {
		return midTime2;
	}

	public void setMidTime2(int midTime2) {
		this.midTime2 = midTime2;
	}

	public int getMidTime3() {
		return midTime3;
	}

	public void setMidTime3(int midTime3) {
		this.midTime3 = midTime3;
	}

	public int getMidTime1() {
		return midTime1;
	}

	public void setMidTime1(int midTime1) {
		this.midTime1 = midTime1;
	}

	void SetTimer(String startTime, String endTime, String overTime, 
			int beginHour, int beginMinute, int endHour, int endMinute, int moreTime1, int moreTime2, int noLateTime)
	{
		m_startTime = startTime;
		m_endTime = endTime;
		m_overTime = overTime;
		m_beginHour = beginHour;
		m_beginMinute = beginMinute;
		m_endHour = endHour;
		m_endMinute = endMinute;
		m_moreTime1 = moreTime1;
		m_moreTime2 = moreTime2;
		m_noLateTime = noLateTime;
	}
	
	//单次考勤判断时间段
	public int IsTimeSegmentOneTime(int hour, int minute)
	{
		int time = hour*60+minute;
		midTime1 = (m_beginHour*60+m_beginMinute+m_endHour*60+m_endMinute)/2;
		 //显示缺勤状态
		if(time>=(m_startTimeHour*60+m_startTimeMinute)
			&& time<=midTime1)
		{
			return 1;  //第一时间段
		}
		else if(time>midTime1
			&& time<=(m_endTimeHour*60+m_endTimeMinute))
		{
			return 2;  //第二时间段
		}
		return 0;
	}
	
	//双班次考勤判断时间段
	public int IsTimeSegment(attendanceTimer g_attendanceTimer2, int hour, int minute)
	{
			midTime1 = (m_beginHour*60+m_beginMinute+m_endHour*60+m_endMinute)/2;
			midTime2 = (m_endHour*60+m_endMinute+m_beginHour*60+m_beginMinute)/2;
			midTime3 = (g_attendanceTimer2.m_beginHour*60+g_attendanceTimer2.m_beginMinute+g_attendanceTimer2.m_endHour*60+g_attendanceTimer2.m_endMinute)/2;
			int time = hour*60+minute;
			if(time>=(m_startTimeHour*60+m_startTimeMinute)
				&& time<=midTime1)
			{
				return 1;  //第一时间段
			}
			else if(time>midTime1
				&& time<=midTime2)
			{
				return 2;  //第二时间段
			}
			else if(time>midTime2
				&& time<=midTime3)
			{
				return 3; //第三时间段
			}
			else if(time>midTime3
				&& time<=(g_attendanceTimer2.m_endTimeHour*60+g_attendanceTimer2.m_endTimeMinute))
			{
				return 4;//第四时间段
			}
			//其他时间段
			return 0;
	}
}
