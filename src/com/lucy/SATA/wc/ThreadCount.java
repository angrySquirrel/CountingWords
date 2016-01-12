package com.lucy.SATA.wc;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
public class ThreadCount implements Runnable {

	private String buf;
	private ConcurrentHashMap<String, AtomicLong> wc;	
	private final static String DELIMS = " \t\n\r{}()[]:;.,/!@#$%^&|*";

	public ThreadCount(String buf, ConcurrentHashMap<String,AtomicLong> wc){
		setPara(buf,wc);
	}

	/**
	 * ���ڲ���������
	 * @param buf
	 * @param wc
	 */
	public void setPara(String buf, ConcurrentHashMap<String,AtomicLong> wc){
		this.wc = wc;
		this.buf = buf;
	}

	/**
	 * ���ڲ���������
	 */
	@Override
	public String toString(){
		List<Map.Entry<String,AtomicLong>> list = new ArrayList<Map.Entry<String,AtomicLong>>(wc.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,AtomicLong>>() {
			// in ascending order
			public int compare(Entry<String, AtomicLong> o1,
					Entry<String, AtomicLong> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}	            
		});
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,AtomicLong> mapping:list){ 
			sb.append(mapping.getKey()+":"+mapping.getValue()+";"); 
		} 
		return sb.toString();		
	}		




	@Override
	/**
	 * ����һ�����ݿ���ַ��������´�Ƶͳ�� 	 
	 */	 
	public void run() {

		StringTokenizer st = new StringTokenizer(buf,DELIMS);
		while(st.hasMoreTokens()){
			String token = st.nextToken();
			addFrequency(token);
		}
	}


	private void addFrequency(String word){
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
