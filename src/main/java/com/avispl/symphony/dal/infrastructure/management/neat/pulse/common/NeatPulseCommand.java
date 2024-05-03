/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common;

/**
 * Class containing constants for Neat Pulse API commands.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/16/2024
 * @since 1.0.0
 */
public class NeatPulseCommand {
	public static final String ALL_DEVICE_ID_COMMAND = "api/v1/orgs/%s/endpoints";
	public static final String ALL_ROOM_COMMAND = "api/v1/orgs/%s/rooms";
	public static final String GET_DEVICE_INFO_COMMAND = "api/v1/orgs/%s/endpoints/%s";
	public static final String GET_DEVICE_SENSOR_COMMAND = "api/v1/orgs/%s/endpoints/%s/sensor";
	public static final String GET_DEVICE_SETTINGS_COMMAND = "api/v1/orgs/%s/endpoints/%s/config";
	public static final String CONTROL_DEVICE = "api/v1/orgs/%s/endpoints/%s/config";
}
