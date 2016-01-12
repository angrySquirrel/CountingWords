package com.lucy.SATA.wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DirectoryCrawler {

	private  ExecutorService workingPool;
	private  ExecutorService readerPool ;
	private  ConcurrentHashMap<String, AtomicLong> wc;

	public DirectoryCrawler(){
		this(1);
	}

	public DirectoryCrawler(int nThreads){
		readerPool  = Executors.newSingleThreadExecutor();
		workingPool = Executors.newFixedThreadPool(nThreads);
		wc = new ConcurrentHashMap<String, AtomicLong>();
	}


	private void process(String folderName) throws IOException{

		readerPool.execute(new Reader(folderName,workingPool, wc));				

		readerPool.shutdown();
		try{
			readerPool.awaitTermination(20, TimeUnit.MINUTES);
		}catch(InterruptedException e){
			System.out.println("ReaderPool termination Interrupted");
			System.exit(1);
		}	
		
		workingPool.shutdown();
		try{
			workingPool.awaitTermination(20, TimeUnit.MINUTES);
		}catch(InterruptedException e){
			System.out.println("workingPool termination Interrupted");
			System.exit(1);
		}	
		
	}	

	public void printResults(){

		if(wc.isEmpty())
			return;
		for(Map.Entry<String, AtomicLong> e : wc.entrySet()){
			System.out.println(e.getKey()+": "+e.getValue());
		}	
	}	

	public static void main(String[]args) throws IOException{
		long startTime = System.currentTimeMillis();
		if(args.length<1){
			System.out.println("Usage:<directoryPath> [<numOfThreads>]");
			System.exit(1);
		}
		DirectoryCrawler dirCrawler = null;
		if(args.length==1){
			dirCrawler = new DirectoryCrawler();//调用默认参数
		}else{
			dirCrawler = new DirectoryCrawler(Integer.valueOf(args[1]));
		}
		
		dirCrawler.process(args[0]);	
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;	
		dirCrawler.printResults();
		System.out.println("Total time = "+elapsed+" ms");

	}



}
