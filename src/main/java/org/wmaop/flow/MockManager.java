package org.wmaop.flow;

import java.io.IOException;

import org.wmaop.aop.Advice;
import org.wmaop.aop.InterceptPoint;
import org.wmaop.aop.PointCut;
import org.wmaop.aop.chainprocessor.AOPChainProcessor;
import org.wmaop.aop.chainprocessor.Interceptor;
import org.wmaop.aop.matcher.AlwaysTrueMatcher;
import org.wmaop.aop.matcher.Matcher;
import org.wmaop.aop.pipeline.FlowPosition;
import org.wmaop.aop.pointcut.ServicePipelinePointCut;
import org.wmaop.interceptor.mock.canned.CannedResponseInterceptor;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

public class MockManager extends FlowManager {

	public void registerFixedResponseMock(IData pipeline) throws ServiceException {
		IDataCursor pipelineCursor = pipeline.getCursor();
		String adviceId = IDataUtil.getString(pipelineCursor, "adviceId");
		String interceptPoint = IDataUtil.getString(pipelineCursor, "interceptPoint");
		String idata = IDataUtil.getString(pipelineCursor, "response");
		pipelineCursor.destroy();

		mandatory(pipeline, "{0} must exist when creating a fixed response mock", adviceId, interceptPoint);
		interceptPoint = interceptPoint.toUpperCase();
		oneof("{0} must be either {1}, {2} or {3}", interceptPoint, "BEFORE", "INVOKE", "AFTER");
		InterceptPoint ip = InterceptPoint.valueOf(interceptPoint);

		Matcher<FlowPosition> servicePositionMatcher = null;
		AlwaysTrueMatcher<IData> pipelineMatcher = new AlwaysTrueMatcher<IData>("default");
		PointCut joinPoint = new ServicePipelinePointCut(servicePositionMatcher, pipelineMatcher, ip);
		Interceptor interceptor;
		try {
			interceptor = new CannedResponseInterceptor(idata);
		} catch (IOException e) {
			throw new ServiceException("Unable to parse response IData for " + adviceId);
		}
		Advice advice = new Advice(adviceId, joinPoint, interceptor);
		AOPChainProcessor.getInstance().registerAdvice(advice);
	}
}