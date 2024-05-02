/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information;

import java.util.Arrays;
import java.util.Optional;

/**
 * DeviceSettings
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 4/16/2024
 * @since 1.0.0
 */
public enum DeviceSettings {
	SCREEN_STANDBY("ScreenStandby", "Display","screenStandby"),
	AUTO_WAKEUP("AutoWakeup","Display", "autoWakeup"),
	NIGHT_MODE("Appearance","Display", "nightMode"),
	SCREEN_BRIGHTNESS("ScreenBrightness(%)","Display", "brightness"),
	HDMI_CEC_CONTROL("HDMICECControl","Display", "hdmiCecControl"),
	KEEP_SCREEN_ON("KeepScreenOn","Display", "screenStayOn"),
	DISPLAY_PREFERENCE("DisplayPreference","Display", "frameRatePreferred"),
	BLUETOOTH("Bluetooth","System", "bluetooth"),
	BYOD_MODE("BYODMode","System", "byodMode"),
	HOUR_TIME("24HourTime","TimeAndLanguage", "time24h"),
	DATE_FORMAT("DateFormat","TimeAndLanguage", "dateFormat"),
	NTP_SERVER("NTPServer","TimeAndLanguage", "ntpServer"),
	TIME_ZONE("TimeZone","TimeAndLanguage", "timezone"),
	LANGUAGE("Language","TimeAndLanguage", "language"),
	USB_AUDIO("USBAudio","AudioAndVideo", "usbAudio"),
	HIGH_CONTRAST_MODE("HighContrastMode","Accessibility", "highContrast"),
	SCREEN_READER("ScreenReader","Accessibility", "screenReader"),
	FONT_SIZE("FontSize","Accessibility", "fontSize"),
	COLOR_CORRECTION("ColorCorrection","Accessibility", "colorCorrection"),
	REBOOT("Reboot","", ""),
	;
	private final String propertyName;
	private final String group;
	private final String value;

	/**
	 * Constructor for DeviceInfo.
	 *
	 * @param defaultName The default name of the property.
	 * @param value The code of the control.
	 */
	DeviceSettings(String defaultName, String group, String value) {
		this.propertyName = defaultName;
		this.group = group;
		this.value = value;
	}

	/**
	 * Retrieves {@link #propertyName}
	 *
	 * @return value of {@link #propertyName}
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Retrieves {@link #group}
	 *
	 * @return value of {@link #group}
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	public static DeviceSettings getByDefaultName(String name) {
		Optional<DeviceSettings> property = Arrays.stream(values()).filter(item -> item.getPropertyName().equalsIgnoreCase(name)).findFirst();
		return property.orElse(null);
	}
}
