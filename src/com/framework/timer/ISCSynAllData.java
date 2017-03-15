package com.framework.timer;

import java.util.Date;

import org.apache.log4j.Logger;

public class ISCSynAllData {
	private static Logger log = Logger.getLogger(ISCSynAllData.class);


	public void run() {
		
		System.out.println("run--->"+new Date());
		log.info("run"+new Date());
	
	}

	
	

}
