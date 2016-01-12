package com.lucy.wc;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@RunWith(Parameterized.class)
public class ThreadCountTest {
	private String expectResult;
	private String input;
	private ConcurrentHashMap<String, AtomicLong> wc;
	private ThreadCount tc;
	
	
	public ThreadCountTest(String inputBuf, String expectResult){
		this.expectResult = expectResult;
		this.input = inputBuf;
		wc = new ConcurrentHashMap<String, AtomicLong>();
		tc = new ThreadCount(inputBuf,wc);		
	}	
	
	
	@Parameters
	public static Collection data(){
			
		Object[][] data = new Object[][]{
			{"hello world, world hello,","hello:2;world:2;"},
			{"免费,托管  Google, #@!#Google@!","Google:2;免费:1;托管:1;"}		
		};
		return Arrays.asList(data);
	}
	
	@Test
	public void testThreadCount(){
		tc.run();//use current thread to test, no need to start another thread
		System.out.println("Parameter input  :"+input);
		System.out.println("Parameter output :"+expectResult);
		System.out.println("Program output   :"+tc.toString());
		System.out.println();
		assertEquals(expectResult,tc.toString());
	}
	
	
}
