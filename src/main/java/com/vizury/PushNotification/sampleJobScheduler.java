package com.vizury.PushNotification;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;



public class sampleJobScheduler implements Job{

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		// TODO Auto-generated method stub
		
	}
	
	public static void init() {
	    //Initialize quartz scheduler
	    try {
	      JobDetail job = JobBuilder.newJob(sampleJobScheduler.class)
	          .withIdentity("sampleCheckCron", "group1").build();
	      
	        //configure the scheduler time
	      Trigger trigger = TriggerBuilder.newTrigger()
	          .withIdentity("sampleCheckCron", "group1")
	          .withSchedule(
	            CronScheduleBuilder.cronSchedule("0 * * * * ?"))
	          .build();
	        
	        //schedule it
	      
	        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
	        scheduler.start();
	        scheduler.scheduleJob(job, trigger);
	    } catch (SchedulerException e) {
	    }	    
	  }

}
