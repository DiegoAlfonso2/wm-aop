package org.wmaop.aop.stub;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.wmaop.aop.advice.Advice;
import org.wmaop.aop.advice.AdviceState;
import org.wmaop.aop.assertion.AssertionInterceptor;
import org.wmaop.aop.interceptor.Interceptor;

public class StubLifecycleObserverTest {

	private StubManager stubManager;
	private Advice advice;
	
	@Before
	public void setup() {
		stubManager = mock(StubManager.class);
		advice = mock(Advice.class);
	}
	
	@Test
	public void shouldNotAttemptStubUnregister() {
		StubLifecycleObserver slo = new StubLifecycleObserver(stubManager);
		Interceptor interceptor = mock(Interceptor.class);
		
		when(advice.getInterceptor()).thenReturn(interceptor);
		when(stubManager.hasStub((Advice) any())).thenReturn(false);
		when(advice.getAdviceState()).thenReturn(AdviceState.DISPOSED);
		slo.update(null, advice);
		
		verify(stubManager, times(0)).unregisterStubService(advice);
	}

	@Test
	public void shouldStubUnregisterAssertable() {
		when(stubManager.hasStub((Advice) any())).thenReturn(true);
		StubLifecycleObserver slo = new StubLifecycleObserver(stubManager);
		Interceptor interceptor = mock(AssertionInterceptor.class);
		
		when(advice.getInterceptor()).thenReturn(interceptor);
		when(advice.getAdviceState()).thenReturn(AdviceState.ENABLED);
		slo.update(null, advice);
		when(advice.getAdviceState()).thenReturn(AdviceState.DISPOSED);
		slo.update(null, advice);
		
		verify(stubManager, times(1)).unregisterStubService(advice);
	}
	
	@Test
	public void shouldStubUnregisterBdd() {
		when(stubManager.hasStub((Advice) any())).thenReturn(true);
		StubLifecycleObserver slo = new StubLifecycleObserver(stubManager);
		List<Interceptor> interceptors = new ArrayList<Interceptor>();
		interceptors.add(mock(AssertionInterceptor.class));
		
		when(advice.getAdviceState()).thenReturn(AdviceState.ENABLED);
		slo.update(null, advice);
		when(advice.getAdviceState()).thenReturn(AdviceState.DISPOSED);
		slo.update(null, advice);
		
		verify(stubManager, times(1)).unregisterStubService(advice);
	}
}
