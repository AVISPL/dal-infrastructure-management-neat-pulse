/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * CallStatusEnum
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 5/2/2024
 * @since 1.0.0
 */
public enum CallStatusEnum {
	NONE("NONE", "Not in a call"),
	ZOOM("ZOOM", "On a zoom call"),
	TEAMS("TEAMS", "On a teams call"),
	;
	private final String name;
	private final String value;


	CallStatusEnum(String name, String value) {
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
