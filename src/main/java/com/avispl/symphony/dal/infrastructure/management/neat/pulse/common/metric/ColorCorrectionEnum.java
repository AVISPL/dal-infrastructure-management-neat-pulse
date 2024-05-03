/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * Enum representing different types of color correction
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 5/2/2024
 * @since 1.0.0
 */
public enum ColorCorrectionEnum {
	DISABLED("Disabled", "disabled"),
	DEUTERA("Deuteranomaly (red-green)", "deuteranomaly"),
	PROTA("Protanomaly (red-green)", "protanomaly"),
	TRITA("Tritanomaly (blue-yellow)", "tritanomaly"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructor for ColorCorrectionEnum.
	 *
	 * @param name  The name representing the call status.
	 * @param value The description of the call status.
	 */
	ColorCorrectionEnum(String name, String value) {
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
