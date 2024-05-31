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
	 * Test case to verify the retrieval of aggregator info.
	 */
	@Test
	void testGetAggregatorInfo() throws Exception {
		extendedStatistic = (ExtendedStatistics) neatPulseCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals("47", statistics.get("NumberOfDevices"));
		Assert.assertEquals("34", statistics.get("NumberOfPulseRooms"));
		Assert.assertEquals("10", statistics.get("DevicePollingInterval(minutes)"));
	}

	/**
	 * Test case to verify the aggregated device info.
	 */
	@Test
	void testGetMultipleStatisticsWithTimeOfPollingCycle() throws Exception {
		neatPulseCommunicator.setDevicePollingInterval(2);
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(11, aggregatedDeviceList.size());
	}

	/**
	 * Test case to verify the aggregated device info.
	 */
	@Test
	void testGetMultipleStatistics() throws Exception {
		neatPulseCommunicator.setDevicePollingInterval(12);
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(47, aggregatedDeviceList.size());
	}

	/**
	 * Test case to verify the aggregated device info.
	 */
	@Test
	void testGetMultipleStatisticsWithHistoricalProperties() throws Exception {
		neatPulseCommunicator.setDevicePollingInterval(2);
		neatPulseCommunicator.setHistoricalProperties("Temperature(C)");
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(47, aggregatedDeviceList.size());
	}

	/**
	 * Test case to verify the aggregated information.
	 */
	@Test
	void testAggregatedInformation() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("true", stats.get("Connected"));
			Assert.assertEquals("May 3, 2024, 5:04 AM", stats.get("ConnectionTime"));
			Assert.assertEquals("Meeting room controller", stats.get("ControllerMode"));
			Assert.assertEquals("NFA1.20240312.0503", stats.get("FirmwareCurrentVersion"));
			Assert.assertEquals("Not in a call", stats.get("InCallStatus"));
			Assert.assertEquals("10.10.2.11", stats.get("LocalIPAddress"));
			Assert.assertEquals("stable", stats.get("OTAChannel"));
			Assert.assertEquals("Microsoft Teams", stats.get("PrimaryMode"));
			Assert.assertEquals("Symphony Lab", stats.get("PulseRoomName"));
			Assert.assertEquals("NA12225002340", stats.get("Serial"));
		}
	}

	/**
	 * Test case to verify the sensor information.
	 */
	@Test
	void testAggregatedSensorInformation() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();
			Assert.assertEquals("420", stats.get("SensorInformation#CO2(ppm)"));
			Assert.assertEquals("36", stats.get("SensorInformation#Humidity(%)"));
			Assert.assertEquals("0", stats.get("SensorInformation#Illumination(lux)"));
			Assert.assertEquals("0", stats.get("SensorInformation#PeopleCount"));
			Assert.assertEquals("22", stats.get("SensorInformation#Temperature(C)"));
			Assert.assertEquals("May 6, 2024, 1:55 AM", stats.get("SensorInformation#Timestamp(GMT)"));
			Assert.assertEquals("502", stats.get("SensorInformation#VOC(ppb)"));
			Assert.assertEquals("0", stats.get("SensorInformation#VOCIndex"));
		}
	}

	/**
	 * Test method to verify the Appearance control feature.
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testAppearanceControl() throws Exception {
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the brightness control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the screen standby control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the date format control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the time zone control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the language control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
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

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the Bluetooth control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testBluetoothControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "System#Bluetooth";
		String value = "1";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the BYOD mode control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testBYODModeControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "System#BYODMode";
		String value = "0";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the font size control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testFontSizeControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Accessibility#FontSize";
		String value = "Default";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}

	/**
	 * Test method to verify the color correction control feature.
	 *
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testColorCorrectionControl() throws Exception {
		neatPulseCommunicator.getMultipleStatistics();
		neatPulseCommunicator.retrieveMultipleStatistics();
		Thread.sleep(20000);
		neatPulseCommunicator.retrieveMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String property = "Accessibility#ColorCorrection";
		String value = "Protanomaly (red-green)";
		String deviceId = "58fdaf7d-beb6-4d5c-ad35-aa28e84e4358";
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		controllableProperty.setDeviceId(deviceId);
		neatPulseCommunicator.controlProperty(controllableProperty);

		List<AggregatedDevice> aggregatedDeviceList = neatPulseCommunicator.retrieveMultipleStatistics();
		Optional<AdvancedControllableProperty> advancedControllableProperty = aggregatedDeviceList.get(1).getControllableProperties().stream().filter(item ->
				property.equals(item.getName())).findFirst();
		Assert.assertEquals(value, advancedControllableProperty.get().getValue());
	}
}
