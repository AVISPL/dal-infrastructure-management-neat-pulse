/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric;

/**
 * Enum representing different types of time zone
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/24/2024
 * @since 1.0.0
 */
public enum LanguageEnum {
	GERMAN("German", "de"),
	ENGLISH("English (United States)", "en-US"),
	SPANISH("Spanish", "es"),
	FRENCH("French", "fr"),
	ITALIA("Italian", "it"),
	JAPANESE("Japanese", "ja"),
	KOREAN("Korean", "ko"),
	NORWEGIAN("Norwegian", "nb"),
	POLISH("Polish", "pl"),
	PORTUGUESE("Portuguese", "pt"),
	RUSSIAN("Russian", "ru"),
	SWEDISH("Swedish", "sv"),
	TURKISH("Turkish", "tr"),
	SIMPLIFIED_CHINESE("Chinese (Simplified Han)", "zh-Hans"),
	TRADITIONAL_CHINESE("Chinese (Traditional Han)", "zh-Hant"),
			;
	private final String name;
	private final String value;

	/**
	 * Constructor for LanguageEnum.
	 *
	 * @param name  The name representing the call status.
	 * @param value The description of the call status.
	 */
	LanguageEnum(String name, String value) {
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
