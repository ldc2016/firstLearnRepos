package com.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class QuartzManager implements BeanFactoryAware {
	private Logger log = Logger.getLogger(QuartzManager.class);
	private Scheduler scheduler;
	private static BeanFactory beanFactory = null;

	@SuppressWarnings("unused")
	private void reScheduleJob() throws Exception, ParseException {
		// ͨ����ѯ���ݿ���ƻ����������üƻ�����
		// 修改注释1
		System.out.println("reScheduleJob---->"+new Date());
		Wsdoc d= new Wsdoc();
		List<Wsdoc> quartzList = new ArrayList<Wsdoc>();//�������ֶ�������һ��
		Wsdoc tbcq=new Wsdoc();
		tbcq.setTriggername("triggername");
		tbcq.setCronexpression("0/5 * * * * ?");
		tbcq.setJobdetailname("detailname");
		tbcq.setTargetobject("com.framework.timer.ISCSynAllData");
		tbcq.setMethodname("run");
		tbcq.setConcurrent("1");
		tbcq.setState("1");
		tbcq.setReadme("readme");
		tbcq.setIsspringbean("0");
		quartzList.add(tbcq);
		if (quartzList != null && quartzList.size() > 0) {
			for (Wsdoc tbcq1 : quartzList) {
				configQuatrz(tbcq1);
			}
		}
	}

	public boolean configQuatrz(Wsdoc tbcq) {
		boolean result = false;
		try {
			// ����ʱ��ͨ����̬ע���scheduler�õ�trigger
			CronTriggerBean trigger = (CronTriggerBean) scheduler.getTrigger(
					tbcq.getTriggername(), Scheduler.DEFAULT_GROUP);
			// ����ƻ������Ѵ���������޸ķ���
			if (trigger != null) {
				change(tbcq, trigger);
			} else {
				// ����ƻ����񲻴��ڲ������ݿ��������״̬Ϊ����ʱ,�򴴽��ƻ�����
				if (tbcq.getState().equals("1")) {
					this.createCronTriggerBean(tbcq);
				}
			}
			result = true;
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		return result;
	}

	public void change(Wsdoc tbcq, CronTriggerBean trigger)
			throws Exception {
		// �������Ϊ����
		if (tbcq.getState().equals("1")) {
			// �жϴ�DB��ȡ�õ�����ʱ������ڵ�quartz�߳��е�����ʱ���Ƿ����
			// �����ȣ����ʾ�û���û�������趨���ݿ��е�����ʱ�䣬�����������Ҫ����rescheduleJob
			if (!trigger.getCronExpression().equalsIgnoreCase(
					tbcq.getCronexpression())) {
				trigger.setCronExpression(tbcq.getCronexpression());
				scheduler.rescheduleJob(tbcq.getTriggername(),
						Scheduler.DEFAULT_GROUP, trigger);
				log.info(new Date() + ": ����" + tbcq.getTriggername() + "�ƻ�����");
			}
		} else {
			// ������
			scheduler.pauseTrigger(trigger.getName(), trigger.getGroup());// ֹͣ������
			scheduler.unscheduleJob(trigger.getName(), trigger.getGroup());// �Ƴ�������
			scheduler.deleteJob(trigger.getJobName(), trigger.getJobGroup());// ɾ������
			log.info(new Date() + ": ɾ��" + tbcq.getTriggername() + "�ƻ�����");

		}

	}

	/**
	 * ����/��Ӽƻ�����
	 * 
	 * @param tbcq
	 *            �ƻ��������ö���
	 * @throws Exception
	 */
	public void createCronTriggerBean(Wsdoc tbcq) throws Exception {
		// �½�һ������Spring�Ĺ���Job��
		MethodInvokingJobDetailFactoryBean mjdfb = new MethodInvokingJobDetailFactoryBean();
		mjdfb.setName(tbcq.getJobdetailname());// ����Job����
		// ��������������ΪSpring�Ķ����Bean����� getBean����
		if (tbcq.getIsspringbean().equals("1")) {
			mjdfb.setTargetObject(beanFactory.getBean(tbcq.getTargetobject()));// ����������
		} else {
			// ����ֱ��new����
			mjdfb.setTargetObject(Class.forName(tbcq.getTargetobject())
					.newInstance());// ����������
		}

		mjdfb.setTargetMethod(tbcq.getMethodname());// �������񷽷�
		mjdfb.setConcurrent(tbcq.getConcurrent().equals("0") ? false : true); // �����Ƿ񲢷���������
		mjdfb.afterPropertiesSet();// ������Job���ύ���ƻ�������
		// ��Spring�Ĺ���Job��תΪQuartz����Job��
		JobDetail jobDetail = new JobDetail();
		jobDetail = (JobDetail) mjdfb.getObject();
		jobDetail.setName(tbcq.getJobdetailname());
		scheduler.addJob(jobDetail, true); // ��Job��ӵ�������
		// ��һ������Spring��ʱ����
		CronTriggerBean c = new CronTriggerBean();
		c.setCronExpression(tbcq.getCronexpression());// ����ʱ����ʽ
		c.setName(tbcq.getTriggername());// ��������
		c.setJobDetail(jobDetail);// ע��Job
		c.setJobName(tbcq.getJobdetailname());// ����Job����
		scheduler.scheduleJob(c);// ע�뵽������
		scheduler.rescheduleJob(tbcq.getTriggername(), Scheduler.DEFAULT_GROUP,
				c);// ˢ�¹�����
		log.info(new Date() + ": �½�" + tbcq.getTriggername() + "�ƻ�����");
	}



	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/*
	 * public ApplicationContext getApc() { return apc; }
	 * 
	 * public void setApc(ApplicationContext apc) { this.apc = apc; }
	 */
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		this.beanFactory = factory;

	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
   
}
