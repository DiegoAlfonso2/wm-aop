package com.xlcatlin.wm.interceptor.bdd;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.wm.data.IData;
import com.xlcatlin.wm.aop.InterceptPoint;
import com.xlcatlin.wm.aop.chainprocessor.AOPChainProcessor;
import com.xlcatlin.wm.aop.chainprocessor.Interceptor;
import com.xlcatlin.wm.aop.matcher.AlwaysTrueMatcher;
import com.xlcatlin.wm.aop.matcher.FlowPositionMatcher;
import com.xlcatlin.wm.aop.matcher.Matcher;
import com.xlcatlin.wm.aop.matcher.jexl.JexlWrappingMatcher;
import com.xlcatlin.wm.aop.pointcut.ServicePipelinePointCut;
import com.xlcatlin.wm.interceptor.xsd.bdd.Advice;
import com.xlcatlin.wm.interceptor.xsd.bdd.Service;
import com.xlcatlin.wm.interceptor.xsd.bdd.When;

/**
 * Pay attention to the Advice class - one from the parse, one from aop
 */
public class BddParser {

	class AssertionHolder {
		String whenCondition;
		String whenid;
		String assertionid;
	}

	public void parse(InputStream bddstream) throws JAXBException, IOException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Advice.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Advice xmlAdvice = (Advice) jaxbUnmarshaller.unmarshal(bddstream);
		processAdvice(xmlAdvice);
	}

	private void processAdvice(Advice xmlAdvice) {
		Interceptor interceptor = new WhenProcessor(xmlAdvice, true);
		com.xlcatlin.wm.aop.Advice advice = new com.xlcatlin.wm.aop.Advice(xmlAdvice.getId(), getJoinPoint(xmlAdvice), interceptor);

		System.out.println("Registering advice: " + advice.getId());
		AOPChainProcessor.getInstance().registerAdvice(advice);
	}

	private InterceptPoint getInterceptPoint(Advice xmlAdvice) {
		InterceptPoint interceptPoint = InterceptPoint.valueOf(xmlAdvice.getGiven().getService().getIntercepted().toUpperCase());
		System.out.println("Creating intercept point: " + interceptPoint);
		return interceptPoint;
	}

	private ServicePipelinePointCut getJoinPoint(Advice xmlAdvice) {
		Service service = xmlAdvice.getGiven().getService();
		When when = xmlAdvice.getGiven().getWhen();

		FlowPositionMatcher flowPositionMatcher = new FlowPositionMatcher(service.getValue() + '_' + service.getIntercepted(), service.getValue());
		System.out.println("Created flow position matcher: " + flowPositionMatcher);

		Matcher<? super IData> pipelineMatcher = getMatcher(when.getCondition(), when.getId());

		ServicePipelinePointCut joinPoint = new ServicePipelinePointCut(flowPositionMatcher, pipelineMatcher, getInterceptPoint(xmlAdvice));
		return joinPoint;
	}

	private Matcher<? super IData> getMatcher(String condition, String id) {
		Matcher<? super IData> pipelineMatcher;
		if (condition != null && condition.length() > 0) {
			pipelineMatcher = new JexlWrappingMatcher<IData>(id, condition);
		} else {
			pipelineMatcher = new AlwaysTrueMatcher();
		}
		System.out.println("Created pipeline matcher: " + pipelineMatcher);
		return pipelineMatcher;
	}
}
