/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * Enum representing different types of font size
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/24/2024
 * @since 1.0.0
 */
public enum FontSizeEnum {
	DEFAULT("Default"),
	SMALL("Small"),
	LARGE("Large"),
	LARGEST("Largest"),
	;
	private final String name;


	FontSizeEnum(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}
}
