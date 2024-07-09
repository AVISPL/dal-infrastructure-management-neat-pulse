/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * NeatPulseModel
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 5/7/2024
 * @since 1.0.0
 */
public enum NeatPulseModel {
	NEAT_BAR_BRO("Neat Bar Pro", "NF21D1"),
	NEAT_FRAME("Neat Frame", "NF21F1"),
	NEAT_BAR("Neat Bar", "NF19B1"),
	NEAT_PAD("Neat Pad", "NF19A1"),
	NEAT_BOARD("Neat Board", "NF20C1"),
	NEAT_CENTER("Neat Center", "NF23L1"),
	NEAT_BOARD_50("Neat Board 50", "NF22H1"),
	NEAT_BAR_2("Neat Bar 2", "NF22E1"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructor for NeatPulseModel.
	 *
	 * @param name  The name representing the call status.
	 * @param value The description of the call status.
	 */
	NeatPulseModel(String name, String value) {
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

	/**
	 * Retrieves the name associated with the given value.
	 *
	 * @param value The value to search for.
	 * @return The name corresponding to the given value, or "Unknown" if not found.
	 */
	public static String getNameByValue(String value) {
		Optional<String> result = Stream.of(values()).filter(model -> model.getValue().equals(value))
				.map(NeatPulseModel::getName).findFirst();
		return result.orElse("Unknown");
	}
}
