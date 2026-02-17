/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.EndpointStatistics;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.EnumTypeHandler;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.NeatPulseCommand;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.NeatPulseConstant;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.NeatPulseModel;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceInfo;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceModel;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceSensor;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceSettings;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.CallStatusEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.ColorCorrectionEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.ControllerModeEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.DateFormatEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.FontSizeEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.LanguageEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.PrimaryModeEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.ScreenStandbyEnum;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.metric.TimeZoneEnum;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * NeatPulseCommunicator
 * Supported features are:
 * Monitoring Aggregator Device:
 *  <ul>
 *  <li> - NumberOfDevices</li>
 *  <li> - NumberOfPulseRooms</li>
 *  <li> - TimeOfPollingCycle</li>
 *  <ul>
 *
 * General Info Aggregated Device:
 * <ul>
 * <li> - Connected</li>
 * <li> - ConnectionTime</li>
 * <li> - ControllerMode</li>
 * <li> - deviceId</li>
 * <li> - deviceName</li>
 * <li> - deviceOnline</li>
 * <li> - FirmwareCurrentVersion</li>
 * <li> - FirmwareUpdateAvailable</li>
 * <li> - InCallStatus</li>
 * <li> - LocalIPAddress</li>
 * <li> - OTAChannel</li>
 * <li> - PrimaryMode</li>
 * <li> - PulseRoomName</li>
 * <li> - Reboot</li>
 * <li> - Serial</li>
 * </ul>
 *
 * Accessibility Group:
 * <ul>
 * <li> - ColorCorrection</li>
 * <li> - FontSize</li>
 * <li> - HighContrastMode</li>
 * <li> - ScreenReader</li>
 * </ul>
 *
 * AudioAndVideo Group:
 * <ul>
 * <li> - USBAudio</li>
 * </ul>
 *
 * Display Group:
 * <ul>
 * <li> - Appearance</li>
 * <li> - AutoWakeup</li>
 * <li> - DisplayPreference</li>
 * <li> - HDMICECControl</li>
 * <li> - KeepScreenOn</li>
 * <li> - ScreenBrightness(%)</li>
 * <li> - ScreenBrightnessCurrentValue(%)</li>
 * <li> - ScreenStandby</li>
 * </ul>
 *
 *
 * Sensor Information Group:
 * <ul>
 * <li> - CO2(ppm)</li>
 * <li> - Humidity(%)</li>
 * <li> - Illumination(lux)</li>
 * <li> - PeopleCount</li>
 * <li> - Temperature(C)</li>
 * <li> - Timestamp(GMT)</li>
 * <li> - VOC(ppb)</li>
 * <li> - VOCIndex</li>
 * </ul>
 *
 *
 * System Group:
 * <ul>
 * <li> - Bluetooth</li>
 * <li> - BYODMode</li>
 * </ul>
 *
 * TimeAndLanguage Group:
 * <ul>
 * <li> - 24HourTime</li>
 * <li> - DateFormat</li>
 * <li> - Language</li>
 * <li> - NTPServer</li>
 * <li> - TimeZone</li>
 *
 * </ul>
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 05/06/2024
 * @since 1.0.0
 */
public class NeatPulseCommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {
	/**
	 * Process that is running constantly and triggers collecting data from NeatPulse SE API endpoints, based on the given timeouts and thresholds.
	 *
	 * @author Harry
	 * @since 1.0.0
	 */
	class NeatPulseDataLoader implements Runnable {
		private volatile boolean inProgress;
		/**
		 * Current monitoring cycle interval - amount of time that passes between 2 consecutive getMultipleStatistics calls
		 * 60000ms by default
		 * */
		private final long systemMonitoringCycleInterval = 60000L;

		public NeatPulseDataLoader() {
			inProgress = true;
		}

		@Override
		public void run() {
			loop:
			while (inProgress) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					logger.info("Ignore for now");
				}

				if (!inProgress) {
					break loop;
				}

				// next line will determine whether Neat Pulse monitoring was paused
				updateAggregatorStatus();
				if (devicePaused) {
					continue loop;
				}

				while (nextDevicesCollectionIterationTimestamp > System.currentTimeMillis()) {
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						logger.info("Ignore for now");
					}
				}

				if (!inProgress) {
					break loop;
				}

				long startCycle = System.currentTimeMillis();
				if (logger.isDebugEnabled()) {
					logger.debug("Fetching other than aggregated device list");
				}

				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Fetching devices details");
					}
						populateDeviceDetails();
				} catch (Exception e) {
					logger.error("Error occurred during device list retrieval: " + e.getMessage(), e);
				}

				nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + (getMonitoringRate() * systemMonitoringCycleInterval);
				lastMonitoringCycleDuration =  Math.max((System.currentTimeMillis() - startCycle) / 1000, 1L);
				if (logger.isDebugEnabled()) {
					logger.debug("Finished collecting devices statistics cycle at " + new Date() + ", total duration: " + lastMonitoringCycleDuration);
				}

					updateValidRetrieveStatisticsTimestamp();
			}
			// Finished collecting
		}

		/**
		 * Triggers main loop to stop
		 */
		public void stop() {
			inProgress = false;
		}
	}

	/**
	 * Get list devices sensor information
	 */
	private void retrieveDeviceSensorInformation() {
		try {
			JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.LIST_DEVICE_SENSOR, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.DATA)) {
				JsonNode dataArray = response.get(NeatPulseConstant.DATA);
				mapOfDeviceIdAndDeviceSensor.clear();
				if (dataArray != null && dataArray.isArray()) {
					for (JsonNode dataNode : dataArray) {
						String id = dataNode.get("id").asText();
						JsonNode endpointData = dataNode.get("endpointData");
						if (endpointData != null && endpointData.has("data")) {
							JsonNode sensorData = endpointData.get("data");
							mapOfDeviceIdAndDeviceSensor.put(id, sensorData);
						}
					}
				}
			}
		} catch (CommandFailureException ex) {
			// Device not support the sensor command
			logger.warn("Device not support the sensor command");
		} catch (Exception e) {
			logger.error("Error when retrieve room sensor information", e);
		}
	}

	/**
	 * Get room sensor information by id
	 *
	 * @param id is id of device
	 */
	private void retrieveRoomSensorInformation(String id) throws Exception {
		synchronized (deviceList) {
			String roomId = deviceList.get(id);
			if (StringUtils.isNullOrEmpty(roomId) || mapRoomIdAndDeviceSensor.containsKey(roomId)) {
				return;
			}
            JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.ROOM_SENSOR, this.getLogin(), roomId), JsonNode.class);
            if (response != null && response.has(NeatPulseConstant.ROOM_DATA)) {
                JsonNode dataNode = response.get(NeatPulseConstant.ROOM_DATA);
                if (dataNode != null && dataNode.has("data")) {
                    JsonNode sensorData = dataNode.get("data");
                    mapRoomIdAndDeviceSensor.put(roomId, sensorData);
                }
            }
		}
	}


	/**
	 * Indicates whether a device is considered as paused.
	 * True by default so if the system is rebooted and the actual value is lost -> the device won't start stats
	 * collection unless the {@link NeatPulseCommunicator#retrieveMultipleStatistics()} method is called which will change it
	 * to a correct value
	 */
	private volatile boolean devicePaused = true;

	/**
	 * We don't want the statistics to be collected constantly, because if there's not a big list of devices -
	 * new devices' statistics loop will be launched before the next monitoring iteration. To avoid that -
	 * this variable stores a timestamp which validates it, so when the devices' statistics is done collecting, variable
	 * is set to currentTime + 30s, at the same time, calling {@link #retrieveMultipleStatistics()} and updating the
	 */
	private long nextDevicesCollectionIterationTimestamp;

	/**
	 * How much time last monitoring cycle took to finish
	 */
	private long lastMonitoringCycleDuration;

	/**
	 * Adapter metadata properties - adapter version and build date
	 */
	private Properties adapterProperties;

	/**
	 * Device adapter instantiation timestamp.
	 */
	private long adapterInitializationTimestamp;

	/**
	 * This parameter holds timestamp of when we need to stop performing API calls
	 * It used when device stop retrieving statistic. Updated each time of called #retrieveMultipleStatistics
	 */
	private volatile long validRetrieveStatisticsTimestamp = 0;

	/**
	 * Aggregator inactivity timeout. If the {@link NeatPulseCommunicator#retrieveMultipleStatistics()}  method is not
	 * called during this period of time - device is considered to be paused, thus the Cloud API
	 * is not supposed to be called
	 */
	private static final long retrieveStatisticsTimeOut = 8 * 60 * 1000;

	/**
	 * Update the status of the device.
	 * The device is considered as paused if did not receive any retrieveMultipleStatistics()
	 * calls during {@link NeatPulseCommunicator}
	 */
	private synchronized void updateAggregatorStatus() {
		devicePaused = validRetrieveStatisticsTimestamp < System.currentTimeMillis();
	}

	/**
	 * Uptime time stamp to valid one
	 */
	private synchronized void updateValidRetrieveStatisticsTimestamp() {
		validRetrieveStatisticsTimestamp = System.currentTimeMillis() + retrieveStatisticsTimeOut;
		updateAggregatorStatus();
	}

	/**
	 * A mapper for reading and writing JSON using Jackson library.
	 * ObjectMapper provides functionality for converting between Java objects and JSON.
	 * It can be used to serialize objects to JSON format, and deserialize JSON data to objects.
	 */
	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Executor that runs all the async operations, that is posting and
	 */
	private ExecutorService executorService;

	/**
	 * A private field that represents an instance of the NeatPulseLoader class, which is responsible for loading device data for Neat Pulse
	 */
	private NeatPulseDataLoader deviceDataLoader;

	/**
	 * A private final ReentrantLock instance used to provide exclusive access to a shared resource
	 * that can be accessed by multiple threads concurrently. This lock allows multiple reentrant
	 * locks on the same shared resource by the same thread.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Private variable representing the local extended statistics.
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * List of aggregated device
	 */
	private List<AggregatedDevice> aggregatedDeviceList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Cached data
	 */
	private Map<String, Map<String, String>> cachedMonitoringDevice = Collections.synchronizedMap(new HashMap<>());

	/**
	 * list of all devices
	 */
	private final Map<String, String> deviceList = Collections.synchronizedMap(new HashMap<>());

	/**
	 * store device id and list device sensor information
	 */
	private Map<String, JsonNode> mapOfDeviceIdAndDeviceSensor = new HashMap<>();

	/**
	 * store room id and list device sensor information by room
	 */
	private Map<String, JsonNode> mapRoomIdAndDeviceSensor = new HashMap<>();


	/**
	 * list of room id and room name
	 */
	private Map<String, String> mapOfRoomIdAndRoomName = new HashMap<>();

	/**
	 * number of rooms
	 */
	private int countRoom = 0;

	/**
	 * number of threads
	 */
	private Integer numberThreads;

	/**
	 * Configurable property for filter room name
	 */
	private Set<String> filterByPulseRoomName = new HashSet<>();

	/**
	 * Configurable property for filter excluding room name
	 */
	private Set<String> filterByExcludingPulseRoomName = new HashSet<>();

	/**
	 * Configurable property for historical properties, comma separated values kept as set locally
	 */
	private Set<String> historicalProperties = new HashSet<>();

	/**
	 * Retrieves {@link #historicalProperties}
	 *
	 * @return value of {@link #historicalProperties}
	 */
	public String getHistoricalProperties() {
		return String.join(",", this.historicalProperties);
	}

	/**
	 * Sets {@link #historicalProperties} value
	 *
	 * @param historicalProperties new value of {@link #historicalProperties}
	 */
	public void setHistoricalProperties(String historicalProperties) {
		this.historicalProperties.clear();
		Arrays.asList(historicalProperties.split(",")).forEach(propertyName -> {
			this.historicalProperties.add(propertyName.trim());
		});
	}

	/**
	 *
	 */
	private String baseUri = "";

	/**
	 * Retrieves {@link #baseUri}
	 *
	 * @return value of {@link #baseUri}
	 */
	@Override
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * Sets {@link #baseUri} value
	 *
	 * @param baseUri new value of {@link #baseUri}
	 */
	@Override
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	/**
	 * Retrieves {@link #numberThreads}
	 *
	 * @return value of {@link #numberThreads}
	 */
	public Integer getNumberThreads() {
		return numberThreads;
	}

	/**
	 * Sets {@link #numberThreads} value
	 *
	 * @param numberThreads new value of {@link #numberThreads}
	 */
	public void setNumberThreads(Integer numberThreads) {
		this.numberThreads = numberThreads;
	}

	/**
	 * Retrieves {@link #filterByPulseRoomName}
	 *
	 * @return value of {@link #filterByPulseRoomName}
	 */
	public String getFilterByPulseRoomName() {
		return String.join(",", this.filterByPulseRoomName);
	}

	/**
	 * Sets {@link #filterByPulseRoomName} value
	 *
	 * @param filterByPulseRoomName new value of {@link #filterByPulseRoomName}
	 */
	public void setFilterByPulseRoomName(String filterByPulseRoomName) {
		this.filterByPulseRoomName.clear();
		Arrays.asList(filterByPulseRoomName.split(",")).forEach(propertyName -> {
			if (StringUtils.isNotNullOrEmpty(propertyName)) {
				this.filterByPulseRoomName.add(propertyName.trim());
			}
		});
	}

	/**
	 * Retrieves {@link #filterByExcludingPulseRoomName}
	 *
	 * @return value of {@link #filterByExcludingPulseRoomName}
	 */
	public String getFilterByExcludingPulseRoomName() {
		return String.join(",", this.filterByExcludingPulseRoomName);
	}

	/**
	 * Sets {@link #filterByExcludingPulseRoomName} value
	 *
	 * @param filterByExcludingPulseRoomName new value of {@link #filterByExcludingPulseRoomName}
	 */
	public void setFilterByExcludingPulseRoomName(String filterByExcludingPulseRoomName) {
		this.filterByExcludingPulseRoomName.clear();
		Arrays.asList(filterByExcludingPulseRoomName.split(",")).forEach(propertyName -> {
			if (StringUtils.isNotNullOrEmpty(propertyName)) {
				this.filterByExcludingPulseRoomName.add(propertyName.trim());
			}
		});
	}

	/**
	 * Constructs a new instance of NeatPulseCommunicator.
	 *
	 * @throws IOException If an I/O error occurs while loading the properties mapping YAML file.
	 */
	public NeatPulseCommunicator() throws IOException {
		adapterProperties = new Properties();
		adapterProperties.load(getClass().getResourceAsStream("/version.properties"));
		this.setTrustAllCertificates(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			if (StringUtils.isNullOrEmpty(this.getLogin())) {
				throw new ResourceNotReachableException("Please check Organization Id in Username field");
			}
			Map<String, String> statistics = new HashMap<>();
			Map<String, String> dynamicStatistics = new HashMap<>();
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			retrieveMetadata(statistics, dynamicStatistics);
			retrieveSystemInfo();
			retrieveRoomInfo();
			retrieveDeviceSensorInformation();
			filterRoomName();
			populateSystemInfo(statistics);
			extendedStatistics.setStatistics(statistics);
			extendedStatistics.setDynamicStatistics(dynamicStatistics);
			localExtendedStatistics = extendedStatistics;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Filter device by pulse room name
	 */
	private void filterRoomName() {
		if (!filterByExcludingPulseRoomName.isEmpty()) {
			List<String> deviceIds = mapOfRoomIdAndRoomName.entrySet().stream()
					.filter(entry -> filterByExcludingPulseRoomName.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
			if (!deviceIds.isEmpty()) {
				mapOfRoomIdAndRoomName.entrySet().removeIf(entry -> deviceIds.contains(entry.getKey()));
			}
		}
		if (!filterByPulseRoomName.isEmpty()) {
			List<String> deviceId = mapOfRoomIdAndRoomName.entrySet().stream()
					.filter(entry -> filterByPulseRoomName.contains(entry.getValue()))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
			if (deviceId.isEmpty()) {
				mapOfRoomIdAndRoomName.clear();
			} else {
				mapOfRoomIdAndRoomName.entrySet().removeIf(entry -> !deviceId.contains(entry.getKey()));
			}
		}
		deviceList.entrySet().removeIf(entry -> !mapOfRoomIdAndRoomName.keySet().contains(entry.getValue()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		reentrantLock.lock();
		try {
			String property = controllableProperty.getProperty();
			String deviceId = controllableProperty.getDeviceId();
			String value = String.valueOf(controllableProperty.getValue());

			String[] propertyList = property.split(NeatPulseConstant.HASH);
			String propertyName = property;
			if (property.contains(NeatPulseConstant.HASH)) {
				propertyName = propertyList[1];
			}
			Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
			if (aggregatedDevice.isPresent()) {
				DeviceSettings item = DeviceSettings.getByDefaultName(propertyName);
				switch (item) {
					case AUTO_WAKEUP:
					case KEEP_SCREEN_ON:
					case HDMI_CEC_CONTROL:
					case BLUETOOTH:
					case BYOD_MODE:
					case HOUR_TIME:
					case HIGH_CONTRAST_MODE:
					case SCREEN_READER:
					case USB_AUDIO:
					case NIGHT_MODE:
					case DISPLAY_PREFERENCE:
						boolean status = "1".equalsIgnoreCase(value);
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), status);
						updateCacheValue(deviceId, property, String.valueOf(status));
						break;
					case NTP_SERVER:
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), value);
						updateCacheValue(deviceId, property, value);
						break;
					case SCREEN_BRIGHTNESS:
						float percentValue = Float.parseFloat(value) / 100;
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), percentValue);
						updateCacheValue(deviceId, property, String.valueOf(percentValue));
						break;
					case SCREEN_STANDBY:
						String bodyValue = EnumTypeHandler.getValueByName(ScreenStandbyEnum.class, value);
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), Long.parseLong(bodyValue));
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case DATE_FORMAT:
						bodyValue = EnumTypeHandler.getValueByName(DateFormatEnum.class, value);
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), bodyValue);
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case LANGUAGE:
						bodyValue = EnumTypeHandler.getValueByName(LanguageEnum.class, value);
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), bodyValue);
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case COLOR_CORRECTION:
						bodyValue = EnumTypeHandler.getValueByName(ColorCorrectionEnum.class, value);
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), bodyValue);
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case TIME_ZONE:
						bodyValue = value.replace(" ", "_");
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), bodyValue);
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case FONT_SIZE:
						bodyValue = value.toLowerCase();
						sendCommandToControlDevice(deviceId, propertyName, item.getValue(), bodyValue);
						updateCacheValue(deviceId, property, bodyValue);
						break;
					case REBOOT:
						controlRebootDevice(deviceId);
						break;
					default:
						if (logger.isWarnEnabled()) {
							logger.warn(String.format("Unable to execute %s command on device %s: Not Supported", property, deviceId));
						}
						break;
				}
			} else {
				throw new IllegalArgumentException(String.format("Unable to control property: %s as the device does not exist.", property));
			}
		} finally {
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : controllableProperties) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
		if (StringUtils.isNullOrEmpty(this.getLogin())) {
			throw new ResourceNotReachableException("Please check Organization Id in Username field");
		}
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(1);
			executorService.submit(deviceDataLoader = new NeatPulseDataLoader());
		}
		if (validRetrieveStatisticsTimestamp == 0){
			validRetrieveStatisticsTimestamp = System.currentTimeMillis() + retrieveStatisticsTimeOut;
		}
		if (cachedMonitoringDevice.isEmpty()) {
			return Collections.emptyList();
		}
		return cloneAndPopulateAggregatedDeviceList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return retrieveMultipleStatistics().stream().filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId())).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 * set API Key into Header of Request
	 */
	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) {
		headers.setBearerAuth(this.getPassword());
		return headers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void authenticate() throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		adapterInitializationTimestamp = System.currentTimeMillis();
		executorService = Executors.newFixedThreadPool(1);
		executorService.submit(deviceDataLoader = new NeatPulseDataLoader());
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal destroy is called.");
		}
		if (deviceDataLoader != null) {
			deviceDataLoader.stop();
			deviceDataLoader = null;
		}
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics.getStatistics().clear();
			localExtendedStatistics.getControllableProperties().clear();
		}
		nextDevicesCollectionIterationTimestamp = 0;
		validRetrieveStatisticsTimestamp = 0;
		aggregatedDeviceList.clear();
		cachedMonitoringDevice.clear();
		deviceList.clear();
		historicalProperties.clear();
		filterByExcludingPulseRoomName.clear();
		filterByPulseRoomName.clear();
		mapRoomIdAndDeviceSensor.clear();
		mapOfRoomIdAndRoomName.clear();
		mapOfDeviceIdAndDeviceSensor.clear();
		super.internalDestroy();
	}

	/**
	 * Retrieves metadata information and updates the provided statistics and dynamic map.
	 *
	 * @param stats the map where statistics will be stored
	 * @param dynamicStatistics the map where dynamic statistics will be stored
	 */
	private void retrieveMetadata(Map<String, String> stats, Map<String, String> dynamicStatistics) {
		try {
			dynamicStatistics.put(NeatPulseConstant.MONITORING_CYCLE_DURATION, String.valueOf(lastMonitoringCycleDuration));
			stats.put(NeatPulseConstant.ADAPTER_VERSION,
					getDefaultValueForNullData(adapterProperties.getProperty("aggregator.version")));
			stats.put(NeatPulseConstant.ADAPTER_BUILD_DATE,
					getDefaultValueForNullData(adapterProperties.getProperty("aggregator.build.date")));
			long adapterUptime = System.currentTimeMillis() - adapterInitializationTimestamp;

			stats.put(NeatPulseConstant.ADAPTER_UPTIME_MIN, String.valueOf(adapterUptime / (1000 * 60)));
			stats.put(NeatPulseConstant.ADAPTER_UPTIME, normalizeUptime(adapterUptime / 1000));
			stats.put(NeatPulseConstant.SYSTEM_MONITORING_CYCLE, String.valueOf(getMonitoringRate()));
			dynamicStatistics.put(NeatPulseConstant.MONITORED_DEVICES_TOTAL, String.valueOf(deviceList.size()));
		} catch (Exception e) {
			logger.error("Failed to populate metadata information", e);
		}
	}

	/**
	 * Sends a command to control a device with the specified parameters.
	 *
	 * @param deviceId The ID of the device to control.
	 * @param name The name of the device.
	 * @param fieldName The name of the field to control.
	 * @param value The value to set for the specified field.
	 */
	private void sendCommandToControlDevice(String deviceId, String name, String fieldName, Object value) {
		try {
			String command = String.format(baseUri + "/" + NeatPulseCommand.CONTROL_DEVICE, this.getLogin(), deviceId);
			Map<String, Object> bodyJson = new HashMap<>();
			bodyJson.put(fieldName, value);
			JsonNode response = this.doPost(command, bodyJson, JsonNode.class);
			if (response == null || !response.has(NeatPulseConstant.CONFIG) || !response.get(NeatPulseConstant.CONFIG).has(fieldName) || !String.valueOf(value)
					.equalsIgnoreCase(response.get(NeatPulseConstant.CONFIG).get(fieldName).asText())) {
				throw new IllegalArgumentException("The response is incorrect");
			}
		} catch (CommandFailureException ex) {
			if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
				throw new IllegalArgumentException(String.format("Can't control %s with value is %s. You've exceeded the maximum number of allowed requests. Please try again after a few minutes.", name, value));
			} else {
				throw new IllegalArgumentException(String.format("Failed to apply config: attempted to override profile settings: the following fields contain conflicts: [%s]", name));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Can't control %s with value is %s. %s", name, value, e.getMessage()));
		}
	}

	/**
	 * Controls the reboot of the specified device.
	 *
	 * @param deviceId the ID of the device to reboot
	 * @throws IllegalArgumentException if the response is empty or if the status code indicates an error,
	 * or if an exception occurs during the process
	 */
	private void controlRebootDevice(String deviceId) {
		try {
			String command = String.format(baseUri + "/" + NeatPulseCommand.REBOOT_DEVICE, this.getLogin(), deviceId);
			Map<String, String> data = new HashMap<>();
			JsonNode response = this.doPost(command, data, JsonNode.class);
			if (response == null) {
				throw new IllegalArgumentException("The response is empty");
			}
			if (response.has(NeatPulseConstant.MESSAGE) && response.has(NeatPulseConstant.STATUS) && !"200".equals(response.get(NeatPulseConstant.STATUS).asText())) {
				throw new IllegalArgumentException(response.get(NeatPulseConstant.MESSAGE).asText());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't control Reboot. " + e.getMessage());
		}
	}

	/**
	 * Retrieves system information by sending a request to the NeatPulse API.
	 * This method populates the deviceList with the IDs of the available devices.
	 *
	 * @throws FailedLoginException If there's an issue with the login credentials. This could happen if the password is incorrect.
	 * @throws ResourceNotReachableException If there's an error reaching the NeatPulse API or retrieving system information.
	 */
	private void retrieveSystemInfo() throws Exception {
		try {
			JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.ALL_DEVICE_ID_COMMAND, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ENDPOINTS) && response.get(NeatPulseConstant.ENDPOINTS).isArray()) {
				deviceList.clear();
				JsonNode jsonNode = response.get(NeatPulseConstant.ENDPOINTS);
				if (jsonNode != null) {
					for (JsonNode node : jsonNode) {
						if (node.get(NeatPulseConstant.ID) != null && node.get("roomId") != null) {
							deviceList.put(node.get(NeatPulseConstant.ID).asText(), node.get("roomId").asText());
						}
					}
				}
			}
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Error when the login. Please check the password");
		} catch (CommandFailureException ex1) {
			logger.error("Error when retrieve system information", ex1);
		} catch (Exception ex) {
			logger.error(String.format("Error when retrieve system information. %s", ex.getMessage()));
		}
	}

	/**
	 * Retrieves room information by sending a request to the NeatPulse API.
	 * This method updates the countRoom variable with the number of available rooms.
	 *
	 * @throws ResourceNotReachableException If there's an error reaching the NeatPulse API or retrieving room information.
	 */
	private void retrieveRoomInfo() {
		try {
			JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.ALL_ROOM_COMMAND, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ROOMS) && response.get(NeatPulseConstant.ROOMS).isArray()) {
				mapOfRoomIdAndRoomName.clear();
				countRoom = response.get(NeatPulseConstant.ROOMS).size();
				JsonNode itemValueNode = response.get(NeatPulseConstant.ROOMS);
				for (JsonNode item : itemValueNode) {
					mapOfRoomIdAndRoomName.put(item.get("id").asText(), item.get("name").asText());
				}
			}
		} catch (Exception ex) {
			logger.error(String.format("Error when retrieve room information. %s", ex.getMessage()));
		}
	}

	/**
	 * Populates system information into the provided stats map.
	 * This method adds the number of devices and the number of console rooms to the stats map.
	 *
	 * @param stats The map to populate with system information.
	 */
	private void populateSystemInfo(Map<String, String> stats) {
		stats.put("NumberOfPulseRooms", String.valueOf(countRoom));
	}

	/**
	 * Populates device details using multiple threads.
	 * Retrieves aggregated data for each device in the device list concurrently.
	 */
	private void populateDeviceDetails() {
		int numberOfThreads = getDefaultNumberOfThread();
		ExecutorService executorServiceForRetrieveAggregatedData = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<?>> futures = new ArrayList<>();

		synchronized (deviceList) {
			filterRoomName();
			List<Map.Entry<String, String>> entries = new ArrayList<>(deviceList.entrySet());
			for (int i = 0; i < deviceList.size(); i++) {
				int index = i;
				Future<?> future = executorServiceForRetrieveAggregatedData.submit(
						() -> processDeviceId(entries.get(index).getKey())
				);
				futures.add(future);
			}
		}
		waitForFutures(futures, executorServiceForRetrieveAggregatedData);
		executorServiceForRetrieveAggregatedData.shutdown();
	}

	/**
	 * Waits for the completion of all futures in the provided list and then shuts down the executor service.
	 *
	 * @param futures The list of Future objects representing asynchronous tasks.
	 * @param executorService The ExecutorService to be shut down.
	 */
	private void waitForFutures(List<Future<?>> futures, ExecutorService executorService) {
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				logger.error("An exception occurred while waiting for a future to complete.", e);
			}
		}
		executorService.shutdown();
	}

	/**
	 * Executes an operation with retry and exponential backoff on HTTP 429 errors.
	 *
	 * Stops retrying on 404. Logs and exits on other errors.
	 *
	 * @param operation       The operation to execute.
	 * @param commandName     Name of the command (for logging).
	 * @param deviceId        ID of the target device.
	 * @param maxRetries      Maximum number of retry attempts.
	 * @param initialWaitTime Initial wait time in milliseconds before retrying.
	 */
	private void retryWithBackoff(Callable<Void> operation,String commandName, String deviceId , int maxRetries, long initialWaitTime) {
		int retryCount = 0;
		long waitTime = initialWaitTime;

		while (retryCount < maxRetries) {
			try {
                operation.call();
				return;
			} catch (CommandFailureException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                    logger.warn(String.format("Device %s not support the room sensor command", deviceId));
                    break;
                } else if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
					retryCount++;
					if (retryCount < 3) {
						logger.info(String.format("Device %s with %s is Too many requests, retrying... Attempt %s", deviceId, commandName, retryCount));
						try {
							Thread.sleep(waitTime);
						} catch (InterruptedException ie) {
							logger.error("Thread interrupted during retry", ie);
							Thread.currentThread().interrupt();
							return;
						}
						waitTime *= 2; // Exponential backoff
					} else {
						logger.info(String.format("Max retries reached. Skipping %s request for device: %s", commandName, deviceId));
						break;
					}
				} else {
					logger.error(String.format("Error when retrieving %s by id %s", commandName, deviceId), ex);
					break;
				}
			} catch (Exception e) {
				logger.error(String.format("Error when retrieving %s by id %s", commandName, deviceId), e);
				break;
			}
		}
	}

	/**
	 * Processes the specified device by retrieving its information, sensor data, and settings.
	 *
	 * @param deviceId The ID of the device to be processed.
	 */
	private void processDeviceId(String deviceId) {
		try {
            retryWithBackoff(() -> {
                retrieveRoomSensorInformation(deviceId);
                return null;
            },"Room Sensor Information", deviceId, 3, 1000L);
			Thread.sleep(1000);

            retryWithBackoff(() -> {
                retrieveDeviceInfo(deviceId);
                return null;
            },"Device Information", deviceId, 3, 1000L);
            Thread.sleep(1000);

            retryWithBackoff(() -> {
                retrieveDeviceSettings(deviceId);
                return null;
            },"Device Settings", deviceId, 3, 1000L);
            Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("An exception occurred while processing the device.", e);
		}
	}

	/**
	 * Retrieves device information for the specified device ID.
	 *
	 * @param deviceId The ID of the device.
	 */
	private void retrieveDeviceInfo(String deviceId) throws Exception {
        JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.GET_DEVICE_INFO_COMMAND, this.getLogin(), deviceId), JsonNode.class);
        if (response != null) {
            Map<String, String> mappingValue = new HashMap<>();
            for (DeviceInfo item : DeviceInfo.values()) {
                if (!NeatPulseConstant.EMPTY.equals(item.getValue())) {
                    String value = NeatPulseConstant.EMPTY;
                    JsonNode itemValueNode = response.get(item.getValue());
                    if (itemValueNode != null) {
                        value = itemValueNode.isArray() ? itemValueNode.toString() : itemValueNode.asText();
                    }
                    mappingValue.put(item.getPropertyName(), value);
                }
            }
            putMapIntoCachedData(deviceId, mappingValue);
        }
	}

	/**
	 * Retrieves settings information for a device associated with a specific organization and device ID.
	 *
	 * @param deviceId The ID of the device to retrieve settings for.
	 */
	private void retrieveDeviceSettings(String deviceId) throws Exception  {
        JsonNode response = this.doGet(String.format(baseUri + "/" + NeatPulseCommand.GET_DEVICE_SETTINGS_COMMAND, this.getLogin(), deviceId), JsonNode.class);
        if (response != null) {
            Map<String, String> mappingValue = new HashMap<>();
            for (DeviceSettings item : DeviceSettings.values()) {
                String value = NeatPulseConstant.EMPTY;
                if (response.has(item.getValue())) {
                    value = response.get(item.getValue()).asText();
                }
                mappingValue.put(item.getGroup() + NeatPulseConstant.HASH + item.getPropertyName(), value);
            }
            putMapIntoCachedData(deviceId, mappingValue);
        }
	}

	/**
	 * Clones the cached monitoring device list and populates the aggregated device list.
	 *
	 * @return The populated aggregated device list.
	 */
	private List<AggregatedDevice> cloneAndPopulateAggregatedDeviceList() {
		synchronized (aggregatedDeviceList) {
			cachedMonitoringDevice.forEach((key, value) -> {
				Optional<AggregatedDevice> optionalDevice = aggregatedDeviceList.stream().filter(device -> device.getDeviceId().equals(key)).findFirst();
				AggregatedDevice aggregatedDevice = optionalDevice.orElse(new AggregatedDevice());
				Map<String, String> cachedData = cachedMonitoringDevice.get(key);
				String modelCode = cachedData.get(DeviceInfo.MODEL.getPropertyName());
				String modelName = NeatPulseModel.getNameByValue(modelCode);
				String roomName = cachedData.get(DeviceInfo.ROOM_NAME.getPropertyName());
				String deviceStatus = cachedData.get(DeviceInfo.CONNECTED.getPropertyName());
				aggregatedDevice.setDeviceId(key);
				aggregatedDevice.setDeviceOnline(false);
				if (!"Unknown".equals(modelName)) {
					aggregatedDevice.setDeviceModel(modelName);
					if (roomName != null) {
						aggregatedDevice.setDeviceName(modelName + " (" + roomName + ")");
					}
				} else {
					aggregatedDevice.setDeviceName(cachedData.get(DeviceInfo.SERIAL.getPropertyName()));
				}
				if (deviceStatus != null) {
					aggregatedDevice.setDeviceOnline(NeatPulseConstant.TRUE.equalsIgnoreCase(deviceStatus));
				}
				Map<String, String> stats = new HashMap<>();
				Map<String, String> dynamicStats = new HashMap<>();
				List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
				String inCallStatus = getDefaultValueForNullData(cachedData.get(DeviceInfo.IN_CALL_STATUS.getPropertyName()));
				//InCallStatus: NONE, ZOOM, TEAMS
				setInCall(aggregatedDevice, !NeatPulseConstant.NONE.equalsIgnoreCase(inCallStatus));
				populateMonitorProperties(cachedData, stats, dynamicStats, advancedControllableProperties, modelCode, aggregatedDevice.getDeviceId());
				if (!NeatPulseModel.NEAT_PAD.getValue().equalsIgnoreCase(modelCode) && !NeatPulseModel.NEAT_CENTER.getValue().equalsIgnoreCase(modelCode)) {
					populateRoomSensor(aggregatedDevice.getDeviceId(), stats, dynamicStats);
				}
				aggregatedDevice.setProperties(stats);
				aggregatedDevice.setDynamicStatistics(dynamicStats);
				aggregatedDevice.setControllableProperties(advancedControllableProperties);
				addOrUpdateAggregatedDevice(aggregatedDevice);
			});
		}
		return aggregatedDeviceList.stream().sorted(Comparator.comparing(item -> item.getProperties().get(DeviceInfo.ROOM_NAME.getPropertyName()))).collect(Collectors.toList());
	}

	/**
	 * Adds or updates the aggregated device in the aggregated device list.
	 *
	 * @param aggregatedDevice The aggregated device to be added or updated.
	 */
	private void addOrUpdateAggregatedDevice(AggregatedDevice aggregatedDevice) {
		boolean isExist = aggregatedDeviceList.stream().anyMatch(dev -> dev.getDeviceId().equals(aggregatedDevice.getDeviceId()));
		if (isExist) {
			aggregatedDeviceList.removeIf(dev -> dev.getDeviceId().equals(aggregatedDevice.getDeviceId()));
		}
		aggregatedDeviceList.add(aggregatedDevice);
	}

	/**
	 * Set zoom room in call status
	 *
	 * @param device device to change inCall status for
	 * @param inCall whether the device is in call or not
	 */
	private void setInCall(AggregatedDevice device, boolean inCall) {
		List<Statistics> statistics = device.getMonitoredStatistics();
		if (inCall) {
			if (statistics == null) {
				statistics = new ArrayList<>();
				device.setMonitoredStatistics(statistics);
			}
			boolean deviceHasEndpointStatistics = false;
			for (Statistics statsEntry : statistics) {
				if (statsEntry instanceof EndpointStatistics) {
					deviceHasEndpointStatistics = true;
					((EndpointStatistics) statsEntry).setInCall(true);
				}
			}
			if (!deviceHasEndpointStatistics) {
				EndpointStatistics endpointStatistics = new EndpointStatistics();
				endpointStatistics.setInCall(true);
				statistics.add(endpointStatistics);
			}
		} else {
			if (statistics != null) {
				for (Statistics statsEntry : statistics) {
					if (statsEntry instanceof EndpointStatistics) {
						((EndpointStatistics) statsEntry).setInCall(false);
					}
				}
			}
		}
	}

	/**
	 * Populates various monitoring properties related to the device, including device information,
	 * sensor data, and device settings.
	 *
	 * @param cached A map containing cached device data.
	 * @param stats A map storing general device statistics.
	 * @param dynamicStats A map holding dynamic statistics, typically real-time or frequently updated data.
	 * @param advancedControllableProperties A list of advanced controllable properties used for device interaction.
	 * @param modelCode The model code of the device.
	 * @param deviceId The unique identifier of the device.
	 */
	private void populateMonitorProperties(Map<String, String> cached, Map<String, String> stats, Map<String, String> dynamicStats, List<AdvancedControllableProperty> advancedControllableProperties,
			String modelCode, String deviceId) {
		populateDeviceInfo(cached, stats);
		populateDeviceSensor(stats, dynamicStats, modelCode, deviceId);
		populateDeviceSettings(cached, stats, advancedControllableProperties);
	}

	/**
	 * Populates monitor properties including room sensor data,
	 *
	 * @param deviceId The id of device
	 * @param stats The map to store monitor properties.
	 * @param dynamicStats The map to store historical properties.
	 */
	private void populateRoomSensor(String deviceId, Map<String, String> stats, Map<String, String> dynamicStats) {
		try {
			String roomId = deviceList.get(deviceId);
			JsonNode roomSensorInformation = mapRoomIdAndDeviceSensor.get(roomId);
			if (roomSensorInformation != null && roomSensorInformation.isArray()) {
				int index = 0;
				for (JsonNode node : roomSensorInformation) {
					index++;
					String group = NeatPulseConstant.ROOM_DEVICE_SENSOR + index + NeatPulseConstant.HASH;
					if (roomSensorInformation.size() == 1) {
						group = NeatPulseConstant.ROOM_DEVICE_SENSOR + NeatPulseConstant.HASH;
					}
					for (DeviceSensor sensor : DeviceSensor.values()) {
						if (node.has(sensor.getValue())) {
							String name = group + sensor.getPropertyName();
							String value = getDefaultValueForNullData(node.get(sensor.getValue()).asText());
							switch (sensor) {
								case TEMPERATURE:
								case HUMIDITY:
								case ILLUMINATION:
									String temperatureValue = roundDoubleValue(value);
									populateHistoricalProperties(stats, dynamicStats, sensor, temperatureValue, name);
									break;
								case CO2:
								case PEOPLE_COUNT:
								case VOC:
								case VOC_INDEX:
									populateHistoricalProperties(stats, dynamicStats, sensor, value, name);
									break;
								case TIMESTAMP:
									stats.put(name, convertTimestampToFormattedDate(value));
									break;
								default:
									stats.put(name, value);
									break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while populate Room sensor Info", e);
		}
	}

	/**
	 * Categorizes and stores sensor properties in either `stats` or `dynamicStats`
	 * based on historical properties.
	 *
	 * @param stats        Map for standard sensor properties.
	 * @param dynamicStats Map for historical sensor properties.
	 * @param sensor       The sensor being evaluated.
	 * @param value        The property value.
	 * @param name         The key for storing the property.
	 */
	private void populateHistoricalProperties(Map<String, String> stats, Map<String, String> dynamicStats, DeviceSensor sensor, String value, String name) {
		boolean propertyListed = false;
		if (!historicalProperties.isEmpty()) {
			propertyListed = historicalProperties.contains(sensor.getPropertyName()) || historicalProperties.contains(NeatPulseConstant.ROOM_DEVICE_SENSOR + "#" + sensor.getPropertyName());
		}
		if (propertyListed && !NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
			dynamicStats.put(name, value);
		} else {
			stats.put(name, value);
		}
	}

	/**
	 * Populates device information into the stats map.
	 *
	 * @param cached The cached data containing device information.
	 * @param stats The map to store device information.
	 */
	private void populateDeviceInfo(Map<String, String> cached, Map<String, String> stats) {
		for (DeviceInfo item : DeviceInfo.values()) {
			String propertyName = item.getPropertyName();
			String value = getDefaultValueForNullData(cached.get(propertyName));
			switch (item) {
				case MODEL:
					break;
				case CONNECTION_TIME:
					stats.put(propertyName, convertDateTimeFormat(value));
					break;
				case FIRMWARE_UPDATE_VERSION:
					String currentVersion = getDefaultValueForNullData(cached.get(DeviceInfo.FIRMWARE_CURRENT_VERSION.getPropertyName()));
					if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
						stats.put(propertyName, value);
					} else {
						String updateAvailable = NeatPulseConstant.FALSE;
						if (!value.equalsIgnoreCase(currentVersion)) {
							stats.put(propertyName, value);
							updateAvailable = NeatPulseConstant.TRUE;
						}
						stats.put("FirmwareUpdateAvailable", updateAvailable);
					}
					break;
				case CONNECTED:
					stats.put(propertyName, value);
					break;
				case PRIMARY_MODE:
					stats.put(propertyName, EnumTypeHandler.getValueByName(PrimaryModeEnum.class, value));
					break;
				case CONTROLLER_MODE:
					stats.put(propertyName, EnumTypeHandler.getValueByName(ControllerModeEnum.class, value));
					break;
				case IN_CALL_STATUS:
					stats.put(propertyName, EnumTypeHandler.getValueByName(CallStatusEnum.class, value));
					break;
				default:
					stats.put(propertyName, uppercaseFirstCharacter(value));
					break;
			}
		}
	}

	/**
	 * Populates sensor data for a given device by retrieving sensor information from the stored device sensor map.
	 * It categorizes sensor data based on the supported sensors of the device model and formats the values accordingly.
	 *
	 * @param stats A map containing general device statistics, where sensor data will be stored.
	 * @param dynamicStats A map holding real-time or frequently updated sensor values.
	 * @param modelCode The model code of the device, used to determine supported sensors.
	 * @param deviceId The unique identifier of the device, used to fetch sensor data.
	 */
	private void populateDeviceSensor(Map<String, String> stats, Map<String, String> dynamicStats, String modelCode, String deviceId) {
		try {
			JsonNode sensorJson = mapOfDeviceIdAndDeviceSensor.get(deviceId);
			if (sensorJson != null && sensorJson.isArray()) {
				int index = 0;
				for (JsonNode node : sensorJson) {
					index++;
					String group = NeatPulseConstant.SENSOR_INFORMATION + index + NeatPulseConstant.HASH;
					if (sensorJson.size() == 1) {
						group = NeatPulseConstant.SENSOR_INFORMATION + NeatPulseConstant.HASH;
					}

					DeviceModel model = DeviceModel.getByDefaultName(modelCode);
					if (model == null) {
						continue;
					}
					for (DeviceSensor sensor : model.getSupportedSensors()) {
						if (node.has(sensor.getValue())) {
							String name = group + sensor.getPropertyName();
							String value = getDefaultValueForNullData(node.get(sensor.getValue()).asText());
							switch (sensor) {
								case TEMPERATURE:
									if (NeatPulseModel.NEAT_PAD.getValue().equalsIgnoreCase(modelCode)) {
										populateHistoricalProperties(stats, dynamicStats, sensor, roundDoubleValue(value), name);
									} else {
										stats.put(name, roundDoubleValue(value));
									}
									break;
								case HUMIDITY:
								case ILLUMINATION:
									stats.put(name, roundDoubleValue(value));
									break;
								case CO2:
								case PEOPLE_COUNT:
								case VOC:
								case VOC_INDEX:
									stats.put(name, value);
									break;
								case TIMESTAMP:
									stats.put(name, convertTimestampToFormattedDate(value));
									break;
								default:
									stats.put(name, value);
									break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while populate Sensor Info", e);
		}
	}

	/**
	 * Populates device settings information into the specified {@code stats} map based on the cached data.
	 * This method retrieves device settings from the cached data and updates the {@code stats} map with the corresponding settings properties.
	 *
	 * @param cached The cached data containing device settings information.
	 * @param stats The map to populate with the extracted device settings information.
	 */
	private void populateDeviceSettings(Map<String, String> cached, Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String model = NeatPulseModel.getNameByValue(getDefaultValueForNullData(cached.get(DeviceInfo.MODEL.getPropertyName())));
		for (DeviceSettings item : DeviceSettings.values()) {
			String propertyName = item.getGroup() + NeatPulseConstant.HASH + item.getPropertyName();
			String value = getDefaultValueForNullData(cached.get(propertyName));
			switch (item) {
				case REBOOT:
					addAdvancedControlProperties(advancedControllableProperties, stats, createButton("Reboot", "Reboot", "Rebooting", 150000), "N/A");
					break;
				case SCREEN_BRIGHTNESS:
					if (NeatPulseConstant.NONE.equals(value)) {
						stats.put(propertyName, value);
					} else {
						float percentValue = Float.parseFloat(value) * 100;
						addAdvancedControlProperties(advancedControllableProperties, stats, createSlider(stats, propertyName, "0", "100", 0f, 100f, percentValue), String.valueOf((int) percentValue));
						stats.put("Display#ScreenBrightnessCurrentValue(%)", String.valueOf((int) percentValue));
					}
					break;
				case SCREEN_STANDBY:
					if (!NeatPulseModel.NEAT_PAD.getName().equals(model)) {
						String enumName = EnumTypeHandler.getNameByValue(ScreenStandbyEnum.class, value);
						if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
							addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, EnumTypeHandler.getEnumNames(ScreenStandbyEnum.class), enumName), enumName);
						} else {
							stats.put(propertyName, NeatPulseConstant.NONE);
						}
					}
					break;
				case DATE_FORMAT:
					String enumName = EnumTypeHandler.getNameByValue(DateFormatEnum.class, value);
					if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, EnumTypeHandler.getEnumNames(DateFormatEnum.class), enumName), enumName);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case LANGUAGE:
					enumName = EnumTypeHandler.getNameByValue(LanguageEnum.class, value);
					if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, EnumTypeHandler.getEnumNames(LanguageEnum.class), enumName), enumName);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case TIME_ZONE:
					String[] possibleValues = EnumTypeHandler.getEnumNames(TimeZoneEnum.class);
					value = value.replace("_", " ");
					if (Arrays.asList(possibleValues).contains(value)) {
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, possibleValues, value), value);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case FONT_SIZE:
					possibleValues = EnumTypeHandler.getEnumNames(FontSizeEnum.class);
					value = uppercaseFirstCharacter(value);
					if (Arrays.asList(possibleValues).contains(value)) {
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, possibleValues, value), value);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case COLOR_CORRECTION:
					enumName = EnumTypeHandler.getNameByValue(ColorCorrectionEnum.class, value);
					if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
						addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(propertyName, EnumTypeHandler.getEnumNames(ColorCorrectionEnum.class), enumName), enumName);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case KEEP_SCREEN_ON:
				case BLUETOOTH:
				case BYOD_MODE:
				case HOUR_TIME:
				case HIGH_CONTRAST_MODE:
				case SCREEN_READER:
				case USB_AUDIO:
					if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
						stats.put(propertyName, value);
					} else {
						int status = NeatPulseConstant.TRUE.equalsIgnoreCase(value) ? 1 : 0;
						addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, status, NeatPulseConstant.OFF, NeatPulseConstant.ON), String.valueOf(status));
					}
					break;
				case HDMI_CEC_CONTROL:
				case AUTO_WAKEUP:
					if (!NeatPulseModel.NEAT_PAD.getName().equals(model)) {
						if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
							stats.put(propertyName, value);
						} else {
							int status = NeatPulseConstant.TRUE.equalsIgnoreCase(value) ? 1 : 0;
							addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, status, NeatPulseConstant.OFF, NeatPulseConstant.ON), String.valueOf(status));
						}
					}
					break;
				case NIGHT_MODE:
					if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
						stats.put(propertyName, value);
					} else {
						int status = NeatPulseConstant.TRUE.equalsIgnoreCase(value) ? 1 : 0;
						addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, status, "Light Mode", "Dark Mode"), String.valueOf(status));
					}
					break;
				case DISPLAY_PREFERENCE:
					if (!NeatPulseModel.NEAT_PAD.getName().equals(model)) {
						if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
							stats.put(propertyName, value);
						} else {
							int status = NeatPulseConstant.TRUE.equalsIgnoreCase(value) ? 1 : 0;
							addAdvancedControlProperties(advancedControllableProperties, stats, createSwitch(propertyName, status, "Higher Resolution", "Lower Latency"), String.valueOf(status));
						}
					}
					break;
				case NTP_SERVER:
					addAdvancedControlProperties(advancedControllableProperties, stats, createText(propertyName, value), value);
					break;
				default:
					stats.put(propertyName, uppercaseFirstCharacter(value));
					break;
			}
		}
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : NeatPulseConstant.NONE;
	}

	/**
	 * capitalize the first character of the string
	 *
	 * @param input input string
	 * @return string after fix
	 */
	private String uppercaseFirstCharacter(String input) {
		char firstChar = input.charAt(0);
		return Character.toUpperCase(firstChar) + input.substring(1);
	}

	/**
	 * Rounds a double value to the nearest long integer.
	 *
	 * @param value the string representation of the double value to be rounded
	 * @return the rounded long value as a string, or the original value if it is "NONE" or cannot be parsed as a double
	 */
	private String roundDoubleValue(String value) {
		if (NeatPulseConstant.NONE.equalsIgnoreCase(value)) {
			return value;
		} else {
			try {
				double doubleNumber = Double.parseDouble(value);
				return String.valueOf(Math.round(doubleNumber));
			} catch (NumberFormatException e) {
				return NeatPulseConstant.NONE;
			}
		}
	}

	/**
	 * Converts the given timestamp value to a formatted date string.
	 * If the input value is {@link NeatPulseConstant#NONE}, it returns the same value.
	 *
	 * @param input The timestamp value to convert.
	 * @return The formatted date string.
	 */
	private String convertTimestampToFormattedDate(String input) {
		if (NeatPulseConstant.NONE.equals(input)) {
			return input;
		}
		try {
			long timestamp = Long.parseLong(input);
			Date date = new Date(timestamp * 1000);
			SimpleDateFormat formatter = new SimpleDateFormat(NeatPulseConstant.TARGET_FORMAT_DATETIME);
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			return formatter.format(date);
		} catch (Exception e) {
			logger.error(String.format("Error when convert Timestamp To Formatted Date with value %s", input), e);
			return NeatPulseConstant.NONE;
		}
	}

	/**
	 * Converts a date-time string from the default format to the target format with GMT timezone.
	 *
	 * @param inputDateTime The input date-time string in the default format.
	 * @return The date-time string after conversion to the target format with GMT timezone.
	 * Returns {@link NeatPulseConstant#NONE} if there is an error during conversion.
	 * @throws Exception If there is an error parsing the input date-time string.
	 */
	private String convertDateTimeFormat(String inputDateTime) {
		if (NeatPulseConstant.NONE.equals(inputDateTime)) {
			return inputDateTime;
		}
		try {
			Instant instant = Instant.parse(inputDateTime);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(NeatPulseConstant.TARGET_FORMAT_DATETIME).withZone(ZoneId.of("GMT"));
			return formatter.format(instant);
		} catch (Exception e) {
			logger.warn(String.format("Can't convert the date time with value %s", inputDateTime), e);
			return NeatPulseConstant.NONE;
		}
	}

	/**
	 * Gets the default number of threads based on the provided input or a default constant value.
	 *
	 * @return The default number of threads.
	 */
	private int getDefaultNumberOfThread() {
		int result;
		try {
			if (numberThreads == null || numberThreads <= 0 || numberThreads >= NeatPulseConstant.DEFAULT_NUMBER_THREAD) {
				result = NeatPulseConstant.DEFAULT_NUMBER_THREAD;
			} else {
				result = numberThreads;
			}
		} catch (Exception e) {
			result = NeatPulseConstant.DEFAULT_NUMBER_THREAD;
		}
		return result;
	}

	/**
	 * Puts the provided mapping values into the cached monitoring data for the specified device ID.
	 *
	 * @param deviceId The ID of the device.
	 * @param mappingValue The mapping values to be added.
	 */
	private void putMapIntoCachedData(String deviceId, Map<String, String> mappingValue) {
		synchronized (cachedMonitoringDevice) {
			Map<String, String> map = new HashMap<>();
			if (cachedMonitoringDevice.get(deviceId) != null) {
				map = cachedMonitoringDevice.get(deviceId);
			}
			map.putAll(mappingValue);
			cachedMonitoringDevice.put(deviceId, map);
		}
	}

	/**
	 * Updates the cache value for a specified property in the aggregated device list.
	 *
	 * @param deviceId The ID of the device whose cache value needs to be updated.
	 * @param name The name of the property to be updated.
	 * @param value The new value to set for the property.
	 */
	private void updateCacheValue(String deviceId, String name, String value) {
		cachedMonitoringDevice.computeIfAbsent(deviceId, k -> new HashMap<>()).put(name, value);
		String roomName = cachedMonitoringDevice.get(deviceId).get(DeviceInfo.ROOM_NAME.getPropertyName());
		for (String id : deviceList.keySet()) {
			if (cachedMonitoringDevice.get(id) != null && roomName.equals(cachedMonitoringDevice.get(id).get(DeviceInfo.ROOM_NAME.getPropertyName()))) {
				cachedMonitoringDevice.computeIfAbsent(id, k -> new HashMap<>()).put(name, value);
			}
		}
	}

	/**
	 * Create a button.
	 *
	 * @param name name of the button
	 * @param label label of the button
	 * @param labelPressed label of the button after pressing it
	 * @param gracePeriod grace period of button
	 * @return This returns the instance of {@link AdvancedControllableProperty} type Button.
	 */
	private AdvancedControllableProperty createButton(String name, String label, String labelPressed, long gracePeriod) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(gracePeriod);
		return new AdvancedControllableProperty(name, new Date(), button, NeatPulseConstant.EMPTY);
	}

	/**
	 * Create switch is control property for metric
	 *
	 * @param name the name of property
	 * @param status initial status (0|1)
	 * @return AdvancedControllableProperty switch instance
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
		toggle.setLabelOff(labelOff);
		toggle.setLabelOn(labelOn);

		AdvancedControllableProperty advancedControllableProperty = new AdvancedControllableProperty();
		advancedControllableProperty.setName(name);
		advancedControllableProperty.setValue(status);
		advancedControllableProperty.setType(toggle);
		advancedControllableProperty.setTimestamp(new Date());

		return advancedControllableProperty;
	}

	/***
	 * Create dropdown advanced controllable property
	 *
	 * @param name the name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty dropdown instance
	 */
	private AdvancedControllableProperty createDropdown(String name, String[] values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values);
		dropDown.setLabels(values);

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}

	/***
	 * Create AdvancedControllableProperty slider instance
	 *
	 * @param stats extended statistics
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty slider instance
	 */
	private AdvancedControllableProperty createSlider(Map<String, String> stats, String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		stats.put(name, initialValue.toString());
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}

	/**
	 * Create text is control property for metric
	 *
	 * @param name the name of the property
	 * @param stringValue character string
	 * @return AdvancedControllableProperty Text instance
	 */
	private AdvancedControllableProperty createText(String name, String stringValue) {
		AdvancedControllableProperty.Text text = new AdvancedControllableProperty.Text();
		return new AdvancedControllableProperty(name, new Date(), text, stringValue);
	}

	/**
	 * Add addAdvancedControlProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @throws IllegalStateException when exception occur
	 */
	private void addAdvancedControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property, String value) {
		if (property != null) {
			advancedControllableProperties.removeIf(controllableProperty -> controllableProperty.getName().equals(property.getName()));

			String propertyValue = StringUtils.isNotNullOrEmpty(value) ? value : NeatPulseConstant.EMPTY;
			stats.put(property.getName(), propertyValue);

			advancedControllableProperties.add(property);
		}
	}

	/**
	 * Uptime is received in seconds, need to normalize it and make it human-readable, like
	 * 1 day 5 hour 12 minute 55 minute
	 * Incoming parameter is may have a decimal point, so in order to safely process this - it's rounded first.
	 * We don't need to add a segment of time if it's 0.
	 *
	 * @param uptimeSeconds value in seconds
	 * @return string value of format 'x d x hr x min x sec'
	 */
	public static String normalizeUptime(long uptimeSeconds) {
		StringBuilder normalizedUptime = new StringBuilder();

		long seconds = uptimeSeconds % 60;
		long minutes = uptimeSeconds % 3600 / 60;
		long hours = uptimeSeconds % 86400 / 3600;
		long days = uptimeSeconds / 86400;

		if (days > 0) {
			normalizedUptime.append(days).append(" d ");
		}
		if (hours > 0) {
			normalizedUptime.append(hours).append(" hr ");
		}
		if (minutes > 0) {
			normalizedUptime.append(minutes).append(" min ");
		}
		if (seconds > 0) {
			normalizedUptime.append(seconds).append(" sec");
		}
		return normalizedUptime.toString().trim();
	}
}
