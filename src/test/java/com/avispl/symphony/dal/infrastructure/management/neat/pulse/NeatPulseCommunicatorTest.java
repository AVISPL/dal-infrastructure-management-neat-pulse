/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse;

import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * NeatPulseCommunicatorTest
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/8/2023
 * @since 1.0.0
 */
public class NeatPulseCommunicatorTest {
	private ExtendedStatistics extendedStatistic;
	private NeatPulseCommunicator neatPulseCommunicator;

	@BeforeEach
	void setUp() throws Exception {
		neatPulseCommunicator = new NeatPulseCommunicator();
		neatPulseCommunicator.setHost("");
		neatPulseCommunicator.setLogin("");
		neatPulseCommunicator.setPassword("");
		neatPulseCommunicator.setPort(443);
		neatPulseCommunicator.init();
		neatPulseCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		neatPulseCommunicator.disconnect();
		neatPulseCommunicator.destroy();
	}

	/**
	 * Test case to verify the retrieval of aggregator data.
	 */
	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) neatPulseCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(2, statistics.size());
	}
}
