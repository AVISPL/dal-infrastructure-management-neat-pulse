/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information;

/**
 * * Enum representing different types of device sensor
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/16/2024
 * @since 1.0.0
 */
public enum DeviceSensor {
	CO2("CO2(ppm)", "co2"),
	HUMIDITY("Humidity(%)", "humidity"),
	ILLUMINATION("Illumination(lux)", "illumination"),
	PEOPLE_COUNT("PeopleCount", "people"),
	TEMPERATURE("Temperature(C)", "temp"),
	VOC("VOC(ppb)", "voc"),
	VOC_INDEX("VOCIndex", "vocIndex"),
	TIMESTAMP("Timestamp(GMT)", "timestamp"),
	;
	private final String propertyName;
	private final String value;

	/**
	 * Constructor for DeviceSensor.
	 *
	 * @param defaultName The name of the device property.
	 * @param value The corresponding value in the device response.
	 */
	DeviceSensor(String defaultName, String value) {
		this.propertyName = defaultName;
		this.value = value;
	}

	/**
	 * Retrieves {@link #propertyName}
	 *
	 * @return value of {@link #propertyName}
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}
}
