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
public enum TimeZoneEnum {
	MIDWAY("Pacific/Midway"),
	HONOLULU("Pacific/Honolulu"),
	ANCHORAGE("America/Anchorage"),
	LOS_ANGELES("America/Los Angeles"),
	TIJUANA("America/Tijuana"),
	PHOENIX("America/Phoenix"),
	CHIHUAHUA("America/Chihuahua"),
	DENVER("America/Denver"),
	COSTA_RICA("America/Costa Rica"),
	CHICAGO("America/Chicago"),
	MEXICO_CITY("America/Mexico City"),
	REGINA("America/Regina"),
	BOGOTA("America/Bogota"),
	NEW_YORK("America/New York"),
	CARACAS("America/Caracas"),
	BARBADOS("America/Barbados"),
	HALIFAX("America/Halifax"),
	MANAUS("America/Manaus"),
	JOHNS("America/St Johns"),
	SANTIAGO("America/Santiago"),
	RECIFE("America/Recife"),
	SAO_PAULO("America/Sao Paulo"),
	BUENOS_AIRES("America/Argentina/Buenos Aires"),
	GODTHAB("America/Godthab"),
	MONTEVIDEO("America/Montevideo"),
	AZORES("Atlantic/Azores"),
	CAPE_VERDE("Atlantic/Cape Verde"),
	GMT("GMT"),
	LONDON("Europe/London"),
	CASABLANCA("Africa/Casablanca"),
	AMSTERDAM("Europe/Amsterdam"),
	BELGRADE("Europe/Belgrade"),
	BERLIN("Europe/Berlin"),
	OSLO("Europe/Oslo"),
	BRUSSELS("Europe/Brussels"),
	MADRID("Europe/Madrid"),
	PARIS("Europe/Paris"),
	ROME("Europe/Rome"),
	SARAJEVO("Europe/Sarajevo"),
	WARSAW("Europe/Warsaw"),
	BRAZZAVILLE("Africa/Brazzaville"),
	WINDOEK("Africa/Windhoek"),
	AMMAN("Asia/Amman"),
	ATHENS("Europe/Athens"),
	BEIRUT("Asia/Beirut"),
	CAIRO("Africa/Cairo"),
	HELSINKI("Europe/Helsinki"),
	JERUSALEM("Asia/Jerusalem"),
	HARARE("Africa/Harare"),
	ISTANBUL("Africa/Istanbul"),
	MINSK("Europe/Minsk"),
	BAGHDAD("Asia/Baghdad"),
	MOSCOW("Europe/Moscow"),
	KUWAIT("Asia/Kuwait"),
	NAIROBI("Africa/Nairobi"),
	TEHRAN("Asia/Tehran"),
	BAKU("Asia/Baku"),
	TBILISI("Asia/Tbilisi"),
	YEREVAN("Asia/Yerevan"),
	DUBAI("Asia/Dubai"),
	KABUL("Asia/Kabul"),
	KARACHI("Asia/Karachi"),
	ORAL("Asia/Oral"),
	YEKATERINBURG("Asia/Yekaterinburg"),
	KOLKATA("Asia/Kolkata"),
	COLOMBO("Asia/Colombo"),
	KATHMANDU("Asia/Kathmandu"),
	ALMATY("Asia/Almaty"),
	YANGON("Asia/Yangon"),
	KRASNOYARSK("Asia/Krasnoyarsk"),
	BANGKOK("Asia/Bangkok"),
	JAKARTA("Asia/Jakarta"),
	SHANGHAI("Asia/Shanghai"),
	HONG_KONG("Asia/Hong Kong"),
	IRKUTSK("Asia/Irkutsk"),
	KUALA_LUMPUR("Asia/Kuala Lumpur"),
	PERTH("Australia/Perth"),
	TAIPEI("Asia/Taipei"),
	SEOUL("Asia/Seoul"),
	TOKYO("Asia/Tokyo"),
	YAKUTSK("Asia/Yakutsk"),
	DARWIN("Australia/Darwin"),
	BRISBANE("Australia/Brisbane"),
	VLADIVOSTOK("Asia/Vladivostok"),
	GUAM("Pacific/Guam"),
	ADELAIDE("Australia/Adelaide"),
	HOBART("Australia/Hobart"),
	SYDNEY("Australia/Sydney"),
	MAGADAN("Asia/Magadan"),
	NOUMEA("Pacific/Noumea"),
	MAJURO("Pacific/Majuro"),
	AUCKLAND("Pacific/Auckland"),
	FIJI("Pacific/Fiji"),
	TONGATAPU("Pacific/Tongatapu"),
	;
	private final String name;

	/**
	 * Constructor for TimeZoneEnum.
	 *
	 * @param name  The name representing the call status.
	 */
	TimeZoneEnum(String name) {
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
