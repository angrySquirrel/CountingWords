package com.lucy.SSD.wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;


public class WorkingThread implements Runnable{

	private final File file;
	private String buf;
		
	private final ConcurrentHashMap<String, AtomicLong> wc;
	private final static String DELIMS = " \t\n\r{}()[]:;.,/!@#$%^&|*";
	
	public WorkingThread(File file,ConcurrentHashMap<String, AtomicLong> wc){
		this.file = file;
		this.wc = wc;
	}

	
	/**
	 * ���ҵ�ǰ�ַ�����Ч�ָ��������Բ��������ʵ�ͳ��
	 * @param buf
	 * @return
	 */
	private static int findLastDelim(String buf) {
		for (int i = buf.length() - 1; i>=0; i--) {
			for (int j = 0; j < DELIMS.length(); j++) {
				char d = DELIMS.charAt(j);
				if (d == buf.charAt(i)) return i;
			}
		}
		return 0;
	} 



	/**
	 * ����һ���ļ��飬����һ���ַ���
	 * @param reader 
	 * @param partitionSize �ļ���Ƭ����
	 * @return �ַ���
	 * @throws IOException
	 */
	private static String readBlockFile(BufferedReader reader) throws IOException{

		int blockSize = 512;		
		StringBuffer blockData = new StringBuffer(blockSize);
		char[] buf = new char[blockSize];
		int numRead = 0;// ÿ�ζ����characters����Ŀ
		int totalRead = 0;//��ǰ�ܹ������characters��Ŀ
		while((numRead = reader.read(buf,0,blockSize))!=-1){

			String readData = String.valueOf(buf,0,numRead);
			blockData.append(readData);			
			buf = new char[blockSize];		
			totalRead = totalRead + numRead;
			if(totalRead>= blockSize)
				break;
		}
		//���ʿ��ܴ��м�Ͽ�,���Ժ�����Ҫ����
		return blockData.toString();

	}	
	
	
	@Override
	public void run(){
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file.getPath()));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
		String remains="";		
		while(true){
			String result = "";
			try {
				result = readBlockFile(reader);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!result.equals("")){
				// ���ݷָ������ָ�,�����һ���ָ�������Ĳ��ֱ�������һ��
				int idx = findLastDelim(result);
				String current = remains + result.substring(0,idx);//������һ�εĲ���
				remains = result.substring(idx);	
				bufCount(current);				
			}
			else{ // means it the last part of this file
				if(!remains.equals("")){
					bufCount(remains);	
				}
				break;
			}		
		}		
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	private void bufCount(String buf){
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
