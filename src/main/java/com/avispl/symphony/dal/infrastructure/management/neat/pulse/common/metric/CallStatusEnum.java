/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * Enum representing different types of call status
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 5/2/2024
 * @since 1.0.0
 */
public enum CallStatusEnum {
	NONE("NONE", "IDLE"),
	ZOOM("ZOOM", "ACTIVE_ZOOM"),
	TEAMS("TEAMS", "ACTIVE_TEAMS"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructor for CallStatusEnum.
	 *
	 * @param name  The name representing the call status.
	 * @param value The description of the call status.
	 */
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
