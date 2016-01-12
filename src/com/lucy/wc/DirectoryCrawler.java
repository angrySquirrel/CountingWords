package com.lucy.wc;

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

	private  ExecutorService pool;
	private  ConcurrentHashMap<String, AtomicLong> wc;

	public DirectoryCrawler(){
		this(1);
	}

	public DirectoryCrawler(int nThreads){

		pool = Executors.newFixedThreadPool(nThreads);
		wc = new ConcurrentHashMap<String, AtomicLong>();
	}

	public void process(String folderName) throws IOException{
		File f = new File(folderName);
		if(!f.exists()|| !f.isDirectory()){
			System.out.println("input is not a valid directory path");
			System.exit(1);
		}		
		crawlAndProcess(f); 		

	}


	private void crawlAndProcess(File directory) throws IOException{
		int partitionSize = 1024;
		for(File file : directory.listFiles()){
			if(file.isDirectory()){
				crawlAndProcess(file);
			}else{
				if(file.getName().endsWith(".txt")){
					FileWordCount fwc = new FileWordCount(pool,wc);
					BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
					fwc.analysisFile(reader, partitionSize);
				}
			}
		}
		pool.shutdown();
		try{
			pool.awaitTermination(20, TimeUnit.MINUTES);
		}catch(InterruptedException e){
			System.out.println("termination Interrupted");
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
		if(args.length<2){
			System.out.println("Usage:<directoryPath> <numOfThreads>");
			System.exit(1);
		}
		DirectoryCrawler dirCrawler = new DirectoryCrawler(Integer.valueOf(args[1]));
		dirCrawler.process(args[0]);	
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;	
		dirCrawler.printResults();
		System.out.println("Total time = "+elapsed+" ms");

	}



}
