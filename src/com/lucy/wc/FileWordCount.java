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
				// 根据分隔符来分割,将最后一个分隔符后面的部分保留到下一次
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
	 * 查找当前字符串有效分割符，避免对不完整单词的统计
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
     * 读入一个文件块，返回一个字符串
     * @param reader 
     * @param partitionSize 文件分片读入
     * @return 字符串
     * @throws IOException
     */
	private static String readChuckFile(BufferedReader reader, int partitionSize) throws IOException{
		StringBuffer fileData = new StringBuffer(partitionSize);
		int numRead = 0;
		while(partitionSize>0){
			//String 可能从中间断开
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
