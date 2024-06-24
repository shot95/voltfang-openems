package io.openems.backend.timedata.influx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.openems.backend.common.timedata.TimedataEdgeFilter;

public class TimedataEdgeFilterTest {

	private static final String DUMMY_COMP_ID = "comp0";
	private static final String[] TEST_PATTERN_SINGLE = {"100000001"};
	private static final String[] TEST_PATTERN_ELEVEN = {"1011"};
	private static final String[] TEST_PATTERN_TWICE = {"1011", "1012"};
	
	@Test
	public void testHandleEdgeFilterOnce() {
		TimedataEdgeFilter filter = new TimedataEdgeFilter(DUMMY_COMP_ID, TEST_PATTERN_SINGLE, true);
		
		assertTrue(filter._isEdgeIdApplicable("100000001"));
		assertTrue(filter._isEdgeIdApplicable("1000000010"));

		assertFalse(filter._isEdgeIdApplicable("1000000"));
		assertFalse(filter._isEdgeIdApplicable("101100001"));
		assertFalse(filter._isEdgeIdApplicable("100000002"));
	}
	
	@Test
	public void testHandleEdgeFilterEleven() {
		TimedataEdgeFilter filter = new TimedataEdgeFilter(DUMMY_COMP_ID, TEST_PATTERN_ELEVEN, true);
		
		assertTrue(filter._isEdgeIdApplicable("101100001"));
		assertTrue(filter._isEdgeIdApplicable("101111111"));

		assertFalse(filter._isEdgeIdApplicable("101011011"));
		assertFalse(filter._isEdgeIdApplicable("101200001"));
	}

	@Test
	public void testHandleEdgeFilterTwice() {
		TimedataEdgeFilter filter = new TimedataEdgeFilter(DUMMY_COMP_ID, TEST_PATTERN_TWICE, true);
		
		assertTrue(filter._isEdgeIdApplicable("101100001"));
		assertTrue(filter._isEdgeIdApplicable("101211111"));

		assertFalse(filter._isEdgeIdApplicable("10131011"));
		assertFalse(filter._isEdgeIdApplicable("121200001"));
	}

}
