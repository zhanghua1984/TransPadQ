package com.fone.player;

import android.util.Log;

public class L {
	
	// !!! NOTE !!!
	// TODO: set it true when build for release version
	public final static boolean mode_for_release = false;
	public final static String TAG = "LiveChannel";
	public final static boolean normal_server_address = true;
	public final static boolean server_switch = true;
	
	public static void v(String tag, String msg)
	{
		if(!mode_for_release)
			Log.v(tag, msg);
	}
    public static void v(String tag, String type, String msg)
    {
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%s", tag, type, msg);
    		Log.v(TAG, des);
        }
    }
    public static void v(String tag, String type, String msg1, String msg2)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%s%s", tag, type, msg1, msg2);
    		Log.v(TAG, des);
    	}
    }
    public static void v(String tag, String type, String msg1, String msg2, String msg3, String msg4)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%s%s%s%s", tag, type, msg1, msg2, msg3, msg4);
    		Log.v(TAG, des);
    	}
    }
    public static void v(String tag, String type, String msg1, int msg){
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%s%d", tag, type, msg1, msg);
    		Log.v(TAG, des);
        }
    }
    public static void v(String tag, String type, Object msg1, Object msg2, Object msg3, Object msg4){
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%s%s%s%s", tag, type, String.valueOf(msg1), String.valueOf(msg2), String.valueOf(msg3), String.valueOf(msg4));
    		Log.v(TAG, des);
        }
    }
    public static void v(String tag, String type, Object msg1, Object msg2){
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%s%s", tag, type, String.valueOf(msg1), String.valueOf(msg2));
    		Log.v(TAG, des);
        }
    }
    public static void v(String tag, String type, int msg)
    {
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%d", tag, type, msg);
    		Log.v(TAG, des);
        }
    }
    
    public static void v(String tag, String type, boolean msg)
    {
        if(!mode_for_release)
        {
            String des = String.format("[%s][%s]%s", tag, type, msg ? "true" : "false");
            Log.v(TAG, des);
        }
    }
    public static void i(String tag, String type, String msg)
    {
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%s", tag, type, msg);
    		Log.i(TAG, des);
        }
    }
    
    public static void i(String tag, String type, String msg1, String msg2)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%s%s", tag, type, msg1, msg2);
    		Log.i(TAG, des);
    	}
    }
    
    public static void i(String tag, String type, int msg)
    {
    	if(!mode_for_release)
        {
    		String des = String.format("[%s][%s]%d", tag, type, msg);
    		Log.v(TAG, des);
        }
    }
    
    public static void i(String tag, String type, boolean msg)
    {
        if(!mode_for_release)
        {
            String des = String.format("[%s][%s]%s", tag, type, msg ? "true" : "false");
            Log.v(TAG, des);
        }
    }
    
    public static void i(String tag, String msg)
    {
    	if(!mode_for_release)
        {
    		String des = String.format("[%s]%s", tag, msg);
    		Log.v(TAG, des);
        }
    }
    
    public static void e(String tag, String type, String msg)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%s", tag, type, msg);
    		Log.e(TAG, des);
    	}
    }
    public static void e(String tag, String type, int msg)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%d", tag, type, msg);
    		Log.e(TAG, des);
    	}
    }
    public static void e(String tag, String type, boolean msg)
    {
    	if(!mode_for_release)
    	{
    		String des = String.format("[%s][%s]%d", tag, type, msg ? "true" : "false");
    		Log.e(TAG, des);
    	}
    }
}
