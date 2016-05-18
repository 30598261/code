package com.example.util;

import java.util.LinkedList;
import java.util.Queue;

public class MsgQueue {
	public static Queue<String> queue = new LinkedList<String>(); 
	
	public static boolean Add(String msg)
	{
		return queue.offer(msg);
	}
    
    public static int Size()
    {
    	// System.out.println(queue.size());  
    	return queue.size();
    }
   
    public static String GetFront()
    {
    	return queue.poll();
    }  
}
