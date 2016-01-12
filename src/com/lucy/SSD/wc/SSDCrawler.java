package com.lucy.SSD.wc;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;




/**
 * this class deals with SSD hard disk
 * @author Administrator
 */
public class SSDCrawler {
	private final ConcurrentHashMap<String, AtomicLong> wc;
	private final ExecutorService workingPool;
	
	public SSDCrawler(){
		this(1);
	}
	
	public SSDCrawler(int nThreads){
		workingPool = Executors.newFixedThreadPool(nThreads);
		wc = new ConcurrentHashMap<String, AtomicLong>();
	}

	/**
	 * 非递归遍历
	 */
	public void process(String folderPath){
				
		File f = new File(folderPath);
		// 检查有效性
		if(!f.exists()|| !f.isDirectory()){
			System.out.println("input is not a valid directory path");
			System.exit(1);
		}
		LinkedList<File> list = new LinkedList<File>(); //store all the folders
		list.add(f);
		while(!list.isEmpty()){
			File directory = list.removeFirst();
			for(File file : directory.listFiles()){
				if(file.isDirectory()){
					list.addLast(file);
				}else{
					if(file.getName().toLowerCase().endsWith(".txt")){
						workingPool.submit(new WorkingThread(file,wc));
					}
				}
			}	
		}
		
		workingPool.shutdown();
		try{
			workingPool.awaitTermination(20, TimeUnit.MINUTES);
		}catch(InterruptedException e){
			System.out.println("workingPool termination Interrupted");
			System.exit(1);
		}		
		
	}
	
	
	public static void main(String[]args) throws IOException{
		long startTime = System.currentTimeMillis();
		if(args.length<1){
			System.out.println("Usage:<directoryPath> <numOfThreads>");
			System.exit(1);
		}
		SSDCrawler dirCrawler = null;
		if(args.length==1){
			dirCrawler = new SSDCrawler();//调用默认参数
		}else{
			dirCrawler = new SSDCrawler(Integer.valueOf(args[1]));
		}		
		
		dirCrawler.process(args[0]);	
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;	
		dirCrawler.printResults();
		System.out.println("Total time = "+elapsed+" ms");

	}
	
	public void printResults(){

		if(wc.isEmpty())
			return;
		for(Map.Entry<String, AtomicLong> e : wc.entrySet()){
			System.out.println(e.getKey()+": "+e.getValue());
		}	
	}		


}
