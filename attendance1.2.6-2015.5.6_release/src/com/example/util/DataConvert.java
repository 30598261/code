package com.example.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class DataConvert {

	public static int getUnsignedByte (byte data){      //��data�ֽ�������ת��Ϊ0~255 (0xFF ��BYTE)��
         return data&0x00FF ;
      }
      public static int getUnsignedByte (short data){      //��data�ֽ�������ת��Ϊ0~65535 (0xFFFF �� WORD)��
            return data&0x0FFFF ;
      }       
     public static long getUnsignedIntt (int data){     //��int����ת��Ϊ0~4294967295 (0xFFFFFFFF��DWORD)��
         return data&0x0FFFFFFFF ;
      }
     
     /**
      * ��ӡbyte����
      */ 
    public static void printBytes(byte[] bb) {
      int length = bb.length;
      /*
      for (int i=0; i<length; i++) {
        System.out.print(bb + " ");
      }
      */
    //  System.out.println("");
    } 
    
     public static byte[] getBytes (char[] chars) {
    	   Charset cs = Charset.forName ("UTF-8");
    	   CharBuffer cb = CharBuffer.allocate (chars.length);
    	   cb.put (chars);
    	                 cb.flip ();
    	   ByteBuffer bb = cs.encode (cb);
    	  
    	   return bb.array();
    	         }

     public static char[] getChars (byte[] bytes) {
    	      Charset cs = Charset.forName ("UTF-8");
    	      ByteBuffer bb = ByteBuffer.allocate (bytes.length);
    	      bb.put (bytes);
    	                 bb.flip ();
    	       CharBuffer cb = cs.decode (bb);
    	  
    	   return cb.array();
    	}
     
}
