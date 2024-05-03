/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;

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
		Assert.assertEquals(3, statistics.size());
	}

	/**
	 * Test case to verify the warnings of room.
	 */
	@Test
	void testGetMultipleStatistics() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(300000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(47, aggregatedDeviceList.size());
	}

	@Test
	void testDelayRequestsControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Display#Appearance";
		String value = "1";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}

	@Test
	void testBrightnessControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Display#ScreenBrightness(%)";
		String value = "85";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}

	@Test
	void testScreenStandbyControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Display#ScreenStandby";
		String value = "20 Minutes";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}

	@Test
	void testDateFormatControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "TimeAndLanguage#DateFormat";
		String value = "MM-DD-YYYY";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}

	@Test
	void testTimeZoneControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "TimeAndLanguage#TimeZone";
		String value = "Europe/Paris";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}

	@Test
	void testLanguageControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "TimeAndLanguage#Language";
		String value = "French";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);
	}
}
