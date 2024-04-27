package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

public enum ColorCorrectionEnum {
	DISABLED("Disabled", "disabled"),
	DEUTERA("Deuteranomaly (red-green)", "deuteranomaly"),
	PROTA("Protanomaly (red-green)", "protanomaly"),
	TRITA("Tritanomaly (blue-yellow)", "tritanomaly"),
	;
	private final String name;
	private final String value;


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
