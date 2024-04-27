package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * ScreenStandbyEnum
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/24/2024
 * @since 1.0.0
 */
public enum ScreenStandbyEnum {
	MINUTE_1("1 Minute", "60000"),
	MINUTE_5("5 Minutes", "300000"),
	MINUTE_10("10 Minutes", "600000"),
	MINUTE_20("20 Minutes", "1200000"),
	MINUTE_30("30 Minutes", "1800000"),
	MINUTE_60("60 Minutes", "3600000"),
			;
	private final String name;
	private final String value;


	ScreenStandbyEnum(String name, String value) {
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
