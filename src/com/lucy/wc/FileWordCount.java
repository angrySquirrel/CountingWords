package com.lucy.wc;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class FileWordCount {

	private final ConcurrentHashMap<String, AtomicLong> wc ;
	private final ExecutorService ec ;
	private final static String DELIMS = " {}()[]:;,.!@#$%^&|*\r\t\n";
	
    public FileWordCount(ExecutorService ec, ConcurrentHashMap<String, AtomicLong> wc) {
    	this.ec = ec;
    	this.wc = wc;    	
    }
	
    
    public void analysisFile(BufferedReader reader,int partitionSize) throws IOException{
    	
		String remains = "";
	
		while(true){
			String res = readChuckFile(reader,partitionSize);
			if(!res.equals("")){
				// ���ݷָ������ָ�,�����һ���ָ�������Ĳ��ֱ�������һ��
				int idx = findDelim(res);
				String current = remains + res.substring(0,idx);
				remains = res.substring(idx);	
				ec.submit(new ThreadCount(current,wc));
			}
			else{ // means it the last part of this file
				if(!remains.equals("")){
					ec.submit(new ThreadCount(remains,wc));					
				}
				break;
			}			
		}	
    }
    
	/**
	 * ���ҵ�ǰ�ַ�����Ч�ָ��������Բ��������ʵ�ͳ��
	 * @param buf
	 * @return
	 */
    private static int findDelim(String buf) {
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
	private static String readChuckFile(BufferedReader reader, int partitionSize) throws IOException{
		StringBuffer fileData = new StringBuffer(partitionSize);
		int numRead = 0;
		while(partitionSize>0){
			//String ���ܴ��м�Ͽ�
			char[] buf = new char[partitionSize];
			numRead = reader.read(buf,0,partitionSize);
			if(numRead == -1)
				break;
			String readData = String.valueOf(buf,0,numRead);
			fileData.append(readData);
			partitionSize-= numRead;
		}
		return fileData.toString();
	}
	
	
	

}
