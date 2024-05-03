/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * Enum representing different types of primary mode
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 5/2/2024
 * @since 1.0.0
 */
public enum PrimaryModeEnum {
	OOB("oob", "The device is being set up"),
	ZOOM("zoom", "Zoom Rooms"),
	TEAMS("msteams", "Microsoft Teams"),
	AVOS("avos", "App hub"),
	USB("usb", "USB BOYD"),
			;
	private final String name;
	private final String value;

	/**
	 * Constructor for PrimaryModeEnum.
	 *
	 * @param name  The name representing the call status.
	 * @param value The description of the call status.
	 */
	PrimaryModeEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
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
