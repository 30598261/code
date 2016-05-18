package com.ebanswers.object;

public class PersonEx extends Person {
	public int birthdaySwitch;  //�Ƿ���ʾ���տ���
	public int attendance_times; //���ڰ��
	public String msg; //���տ�����ʾ��Ϣ
	public String tel ; //�绰����
	public String Num; //����
	public String male; //�Ա�
	public String Birthday; //����
	
	
	public String toString()
	{
		return super.toString()+" birthdaySwitch : "+this.birthdaySwitch+","
		  +"attendance_times : "+this.attendance_times+","
		  +"Num : "+this.Num+","
		  +"birthday : "+this.Birthday+","
		  +"tel : "+this.tel+","
		  +"male: "+this.male+","
		  +"msg : "+this.msg;
	}
}
