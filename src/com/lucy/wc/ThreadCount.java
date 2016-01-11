package com.lucy.wc;
import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
public class ThreadCount implements Runnable {

	private final String buf;
	private final ConcurrentHashMap<String, AtomicLong> wc;	
	private final static String DELIMS = " {}()[]:;.!@#$%^&|*\t\n";
		
	public ThreadCount(String buf, ConcurrentHashMap<String,AtomicLong> wc){
		this.wc = wc;
		this.buf = buf;
	}
	
	
	@Override
	/**
	 * 读入一个数据块的字符串，更新词频统计 	 
	 */	 
	public void run() {
		
		StringTokenizer st = new StringTokenizer(buf,DELIMS);
		while(st.hasMoreTokens()){
			String token = st.nextToken();
			updateCount(token);
		}
	}

	
	private void updateCount(String word){
		AtomicLong count = wc.get(word);
		if(count == null){
			AtomicLong newNumber = new AtomicLong(0);
			count = wc.putIfAbsent(word,newNumber);
			if(count == null){
				count = newNumber;
			}
		}
		count.incrementAndGet();		
	}



	
	
}
