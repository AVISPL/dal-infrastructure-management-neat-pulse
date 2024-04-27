package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * DateFormatEnum
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/24/2024
 * @since 1.0.0
 */
public enum DateFormatEnum {
	FORMAT_1("MM-DD-YYYY", "MM/dd/yyyy"),
	FORMAT_2("YYYY-MM-DD", "yyyy/MM/dd"),
	FORMAT_3("DD-MM-YYYY", "dd/MM/yyyy"),
	;
	private final String name;
	private final String value;


	DateFormatEnum(String name, String value) {
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
