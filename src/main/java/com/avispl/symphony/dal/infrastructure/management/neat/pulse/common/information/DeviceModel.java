/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Enum DeviceModel to store list of model of Neat pulse device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 11/25/2024
 * @since 1.0.1
 */
public enum DeviceModel {
	NEAT_PAD("NF19A1", EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.CO2, DeviceSensor.VOC, DeviceSensor.TIMESTAMP)),
	NEAT_BAR("NF19B1", EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.CO2, DeviceSensor.VOC, DeviceSensor.ILLUMINATION,
			DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_BOARD("NF20C1", EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.CO2, DeviceSensor.VOC, DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_BAR_PRO("NF21D1",
			EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.VOC_INDEX, DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_BAR_2("NF22E1", EnumSet.of(DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_FRAME("NF21F1", EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.VOC_INDEX, DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_BOARD_50("NF22H1",
			EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_BOARD_PRO("NF23K1",
			EnumSet.of(DeviceSensor.HUMIDITY, DeviceSensor.TEMPERATURE, DeviceSensor.VOC_INDEX, DeviceSensor.ILLUMINATION, DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP)),
	NEAT_CENTER("NF23L1", EnumSet.of(DeviceSensor.PEOPLE_COUNT, DeviceSensor.TIMESTAMP));

	private final String modelId;
	private final Set<DeviceSensor> supportedSensors;

	/**
	 * Constructor for DeviceModel.
	 *
	 * @param modelId The model id of the device
	 * @param supportedSensors The supportedSensors is Set list device Sensor
	 */
	DeviceModel(String modelId, Set<DeviceSensor> supportedSensors) {
		this.modelId = modelId;
		this.supportedSensors = supportedSensors;
	}

	/**
	 * Retrieves {@link #modelId}
	 *
	 * @return value of {@link #modelId}
	 */
	public String getModelId() {
		return modelId;
	}

	/**
	 * Retrieves {@link #supportedSensors}
	 *
	 * @return value of {@link #supportedSensors}
	 */
	public Set<DeviceSensor> getSupportedSensors() {
		return supportedSensors;
	}

	/**
	 * Retrieves the DeviceModel enum based on its default name.
	 *
	 * @param name The default name of the DeviceSettings enum.
	 * @return The DeviceSettings enum corresponding to the default name, or null if not found.
	 */
	public static DeviceModel getByDefaultName(String name) {
		Optional<DeviceModel> property = Arrays.stream(values()).filter(item -> item.getModelId().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}