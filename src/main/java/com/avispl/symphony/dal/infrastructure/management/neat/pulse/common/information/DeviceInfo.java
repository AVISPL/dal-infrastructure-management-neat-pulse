/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information;

/**
 * Enum representing different types of device information
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/16/2024
 * @since 1.0.0
 */
public enum DeviceInfo {
	SERIAL("Serial", "serial"),
	CONNECTED("Connected", "connected"),
	FIRMWARE_CURRENT_VERSION("FirmwareCurrentVersion", "firmwareVersion"),
	FIRMWARE_UPDATE_VERSION("FirmwareLatestVersion", "latestVersion"),
	ROOM_NAME("PulseRoomName", "roomName"),
	CONTROLLER_MODE("ControllerMode", "controllerMode"),
	LOCAL_IP_ADDRESS("LocalIPAddress", "localIpAddress"),
	IN_CALL_STATUS("InCallStatus", "inCallStatus"),
	OTA_CHANNEL("OTAChannel", "otaChannel"),
	CONNECTION_TIME("ConnectionTime", "connectionTime"),
	PRIMARY_MODE("PrimaryMode", "primaryMode"),
	;
	private final String propertyName;
	private final String value;

	/**
	 * Constructor for DeviceInfo.
	 *
	 * @param defaultName The name of the device property.
	 * @param value The corresponding value in the device response.
	 */
	DeviceInfo(String defaultName, String value) {
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
