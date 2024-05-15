/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
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
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.PingMode;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceInfo;
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
		private volatile boolean flag = false;

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
				if (logger.isDebugEnabled()) {
					logger.debug("Fetching other than aggregated device list");
				}
				long currentTimestamp = System.currentTimeMillis();
				if (!flag && nextDevicesCollectionIterationTimestamp <= currentTimestamp) {
					populateDeviceDetails();
					flag = true;
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
				if (flag) {
					nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + 60000L * timeOfPollingCycle;
					flag = false;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Finished collecting devices statistics cycle at " + new Date());
				}
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
	 * This parameter holds timestamp of when we need to stop performing API calls
	 * It used when device stop retrieving statistic. Updated each time of called #retrieveMultipleStatistics
	 */
	private volatile long validRetrieveStatisticsTimestamp;

	/**
	 * Aggregator inactivity timeout. If the {@link NeatPulseCommunicator#retrieveMultipleStatistics()}  method is not
	 * called during this period of time - device is considered to be paused, thus the Cloud API
	 * is not supposed to be called
	 */
	private static final long retrieveStatisticsTimeOut = 3 * 60 * 1000;

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
	private List<String> deviceList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * number of rooms
	 */
	private int countRoom;

	/**
	 * time of polling cycle
	 */
	private Integer timeOfPollingCycle;

	/**
	 *
	 */
	private Integer frequentlySystem = 0;

	/**
	 * number of threads
	 */
	private Integer numberThreads;

	/**
	 * start index
	 */
	private Integer startIndex = NeatPulseConstant.START_INDEX;

	/**
	 * end index
	 */
	private Integer endIndex = null;

	/**
	 * number device in interval
	 */
	private Integer numberDeviceInInterval;

	/**
	 * ping mode
	 */
	private PingMode pingMode = PingMode.ICMP;

	/**
	 * Retrieves {@link #pingMode}
	 *
	 * @return value of {@link #pingMode}
	 */
	public String getPingMode() {
		return pingMode.name();
	}

	/**
	 * Sets {@link #pingMode} value
	 *
	 * @param pingMode new value of {@link #pingMode}
	 */
	public void setPingMode(String pingMode) {
		this.pingMode = PingMode.ofString(pingMode);
	}

	/**
	 * Retrieves {@link #timeOfPollingCycle}
	 *
	 * @return value of {@link #timeOfPollingCycle}
	 */
	public Integer getTimeOfPollingCycle() {
		return timeOfPollingCycle;
	}

	/**
	 * Sets {@link #timeOfPollingCycle} value
	 *
	 * @param timeOfPollingCycle new value of {@link #timeOfPollingCycle}
	 */
	public void setTimeOfPollingCycle(Integer timeOfPollingCycle) {
		this.timeOfPollingCycle = timeOfPollingCycle;
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
	 * Constructs a new instance of NeatPulseCommunicator.
	 *
	 * @throws IOException If an I/O error occurs while loading the properties mapping YAML file.
	 */
	public NeatPulseCommunicator() throws IOException {
		if (timeOfPollingCycle == null || timeOfPollingCycle > 15 || timeOfPollingCycle < 1) {
			timeOfPollingCycle = 10;
		}
		this.setTrustAllCertificates(true);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (this.pingMode == PingMode.ICMP) {
			return super.ping();
		} else if (this.pingMode == PingMode.TCP) {
			if (isInitialized()) {
				long pingResultTotal = 0L;

				for (int i = 0; i < this.getPingAttempts(); i++) {
					long startTime = System.currentTimeMillis();

					try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
						puSocketConnection.setSoTimeout(this.getPingTimeout());
						if (puSocketConnection.isConnected()) {
							long pingResult = System.currentTimeMillis() - startTime;
							pingResultTotal += pingResult;
							if (this.logger.isTraceEnabled()) {
								this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
							}
						} else {
							if (this.logger.isDebugEnabled()) {
								this.logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
							}
							return this.getPingTimeout();
						}
					} catch (SocketTimeoutException | ConnectException tex) {
						throw new RuntimeException("Socket connection timed out", tex);
					} catch (UnknownHostException ex) {
						throw new UnknownHostException(String.format("Connection timed out, UNKNOWN host %s", host));
					} catch (Exception e) {
						if (this.logger.isWarnEnabled()) {
							this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
						}
						return this.getPingTimeout();
					}
				}
				return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
			} else {
				throw new IllegalStateException("Cannot use device class without calling init() first");
			}
		} else {
			throw new IllegalArgumentException("Unknown PING Mode: " + pingMode);
		}
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
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			if (frequentlySystem == 0) {
				retrieveSystemInfo();
				retrieveRoomInfo();
			}
			frequentlySystem++;
			if (frequentlySystem >= timeOfPollingCycle / 2) {
				frequentlySystem = 0;
			}
			populateSystemInfo(statistics);
			extendedStatistics.setStatistics(statistics);
			localExtendedStatistics = extendedStatistics;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
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
		nextDevicesCollectionIterationTimestamp = System.currentTimeMillis();
		updateValidRetrieveStatisticsTimestamp();
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
		aggregatedDeviceList.clear();
		cachedMonitoringDevice.clear();
		deviceList.clear();
		startIndex = NeatPulseConstant.START_INDEX;
		endIndex = null;
		numberDeviceInInterval = null;
		super.internalDestroy();
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
			String command = String.format(NeatPulseCommand.CONTROL_DEVICE, this.getLogin(), deviceId);
			Map<String, Object> bodyJson = new HashMap<>();
			bodyJson.put(fieldName, value);
			JsonNode response = this.doPost(command, bodyJson, JsonNode.class);
			if (response == null || !response.has(NeatPulseConstant.CONFIG) || !response.get(NeatPulseConstant.CONFIG).has(fieldName) || !String.valueOf(value)
					.equalsIgnoreCase(response.get(NeatPulseConstant.CONFIG).get(fieldName).asText())) {
				throw new IllegalArgumentException("The response is incorrect");
			}
		} catch (CommandFailureException e) {
			throw new IllegalArgumentException(String.format("Failed to apply config: attempted to override profile settings: the following fields contain conflicts: [%s]", name));
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
			String command = String.format(NeatPulseCommand.REBOOT_DEVICE, this.getLogin(), deviceId);
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
			JsonNode response = this.doGet(String.format(NeatPulseCommand.ALL_DEVICE_ID_COMMAND, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ENDPOINTS) && response.get(NeatPulseConstant.ENDPOINTS).isArray()) {
				deviceList.clear();
				for (JsonNode node : response.get(NeatPulseConstant.ENDPOINTS)) {
					deviceList.add(node.get(NeatPulseConstant.ID).asText());
				}
			}
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Error when the login. Please check the password");
		} catch (CommandFailureException ex1) {
			throw new ResourceNotReachableException("Error when retrieve system information", ex1);
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
			countRoom = 0;
			JsonNode response = this.doGet(String.format(NeatPulseCommand.ALL_ROOM_COMMAND, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ROOMS) && response.get(NeatPulseConstant.ROOMS).isArray()) {
				countRoom = response.get(NeatPulseConstant.ROOMS).size();
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
		stats.put("NumberOfDevices", String.valueOf(deviceList.size()));
		stats.put("NumberOfPulseRooms", String.valueOf(countRoom));
		stats.put("TimeOfPollingCycle", String.valueOf(timeOfPollingCycle));
	}

	/**
	 * Populates device details using multiple threads.
	 * Retrieves aggregated data for each device in the device list concurrently.
	 */
	private void populateDeviceDetails() {
		int numberOfThreads = getDefaultNumberOfThread();
		numberDeviceInInterval = 1000 * timeOfPollingCycle / (3 * 60);
		ExecutorService executorServiceForRetrieveAggregatedData = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<?>> futures = new ArrayList<>();

		if (endIndex == null) {
			endIndex = numberDeviceInInterval;
		}
		if (endIndex > deviceList.size()) {
			endIndex = deviceList.size();
		}
		synchronized (deviceList) {
			for (int i = startIndex; i < endIndex; i++) {
				int index = i;
				Future<?> future = executorServiceForRetrieveAggregatedData.submit(() -> processDeviceId(deviceList.get(index)));
				futures.add(future);
			}
		}
		waitForFutures(futures, executorServiceForRetrieveAggregatedData);
		executorServiceForRetrieveAggregatedData.shutdown();
		if (endIndex == deviceList.size()) {
			startIndex = NeatPulseConstant.START_INDEX;
			endIndex = numberDeviceInInterval;
		} else {
			startIndex = endIndex;
			endIndex += numberDeviceInInterval;
		}
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
	 * Processes the specified device by retrieving its information, sensor data, and settings.
	 *
	 * @param deviceId The ID of the device to be processed.
	 */
	private void processDeviceId(String deviceId) {
		retrieveDeviceInfo(deviceId);
		retrieveDeviceSensor(deviceId);
		retrieveDeviceSettings(deviceId);
	}

	/**
	 * Retrieves device information for the specified device ID.
	 *
	 * @param deviceId The ID of the device.
	 */
	private void retrieveDeviceInfo(String deviceId) {
		try {
			JsonNode response = this.doGet(String.format(NeatPulseCommand.GET_DEVICE_INFO_COMMAND, this.getLogin(), deviceId), JsonNode.class);
			if (response != null) {
				Map<String, String> mappingValue = new HashMap<>();
				for (DeviceInfo item : DeviceInfo.values()) {
					if (!NeatPulseConstant.EMPTY.equals(item.getValue())) {
						String value = NeatPulseConstant.EMPTY;
						JsonNode itemValueNode = response.get(item.getValue());
						if (itemValueNode != null) {
							if (itemValueNode.isArray()) {
								value = itemValueNode.toString();
							} else {
								value = itemValueNode.asText();
							}
						}
						mappingValue.put(item.getPropertyName(), value);
					}
				}
				putMapIntoCachedData(deviceId, mappingValue);
			}
		} catch (Exception e) {
			logger.error(String.format("Error when retrieve device info by id %s", deviceId), e);
		}
	}

	/**
	 * Retrieves settings information for a device associated with a specific organization and device ID.
	 *
	 * @param deviceId The ID of the device to retrieve settings for.
	 */
	private void retrieveDeviceSettings(String deviceId) {
		try {
			JsonNode response = this.doGet(String.format(NeatPulseCommand.GET_DEVICE_SETTINGS_COMMAND, this.getLogin(), deviceId), JsonNode.class);
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
		} catch (Exception e) {
			logger.error(String.format("Error when retrieve device settings by id %s", deviceId), e);
		}
	}

	/**
	 * Retrieves sensor data for the specified device ID.
	 *
	 * @param deviceId The ID of the device.
	 */
	private void retrieveDeviceSensor(String deviceId) {
		try {
			JsonNode response = this.doGet(String.format(NeatPulseCommand.GET_DEVICE_SENSOR_COMMAND, this.getLogin(), deviceId), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ENDPOINT_DATA) && response.get(NeatPulseConstant.ENDPOINT_DATA).has(NeatPulseConstant.DATA)) {
				Map<String, String> mappingValue = new HashMap<>();
				mappingValue.put(NeatPulseConstant.DEVICE_SENSOR, response.get(NeatPulseConstant.ENDPOINT_DATA).get(NeatPulseConstant.DATA).toString());
				putMapIntoCachedData(deviceId, mappingValue);
			}
		} catch (CommandFailureException ex) {
			// Device not support the sensor command
			logger.info(String.format("Device %s not support the sensor command", deviceId));
		} catch (Exception e) {
			logger.error(String.format("Error when retrieve device sensor by id %s", deviceId), e);
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
				AggregatedDevice aggregatedDevice = new AggregatedDevice();
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
				List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
				populateMonitorProperties(cachedData, stats, advancedControllableProperties);
				aggregatedDevice.setProperties(stats);
				aggregatedDevice.setControllableProperties(advancedControllableProperties);
				addOrUpdateAggregatedDevice(aggregatedDevice);
			});
		}
		return aggregatedDeviceList.stream().sorted(Comparator.comparing(item -> item.getProperties().get(DeviceInfo.ROOM_NAME.getPropertyName())))
				.collect(Collectors.toList());
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
	 * Populates monitor properties including device info, device sensor, device settings, and advanced controllable properties.
	 *
	 * @param cached The cached data containing device information.
	 * @param stats The map to store monitor properties.
	 * @param advancedControllableProperties The list to store advanced controllable properties.
	 */
	private void populateMonitorProperties(Map<String, String> cached, Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		populateDeviceInfo(cached, stats);
		populateDeviceSensor(cached, stats);
		populateDeviceSettings(cached, stats, advancedControllableProperties);
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
					stats.put(propertyName, value);
					break;
			}
		}
	}

	/**
	 * Populates device sensor information into the specified {@code stats} map based on the cached data.
	 * This method parses the JSON data from the cached device sensor information and extracts relevant sensor properties.
	 *
	 * @param cached The cached data containing device sensor information.
	 * @param stats The map to populate with the extracted sensor information.
	 */
	private void populateDeviceSensor(Map<String, String> cached, Map<String, String> stats) {
		try {
			String jsonValue = getDefaultValueForNullData(cached.get(NeatPulseConstant.DEVICE_SENSOR));
			if (NeatPulseConstant.NONE.equals(jsonValue)) {
				return;
			}
			JsonNode sensorJson = objectMapper.readTree(jsonValue);
			if (sensorJson.isArray()) {
				int index = 0;
				for (JsonNode node : sensorJson) {
					index++;
					String group = NeatPulseConstant.SENSOR_INFORMATION + index + NeatPulseConstant.HASH;
					if (sensorJson.size() == 1) {
						group = NeatPulseConstant.SENSOR_INFORMATION + NeatPulseConstant.HASH;
					}
					for (DeviceSensor item : DeviceSensor.values()) {
						if (node.has(item.getValue())) {
							String name = group + item.getPropertyName();
							String value = getDefaultValueForNullData(node.get(item.getValue()).asText());
							switch (item) {
								case TEMPERATURE:
								case HUMIDITY:
								case ILLUMINATION:
									stats.put(name, roundDoubleValue(value));
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
					addAdvancedControlProperties(advancedControllableProperties, stats, createButton("Reboot", "Apply", "Applying", 0), NeatPulseConstant.NONE);
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
							addAdvancedControlProperties(advancedControllableProperties, stats,
									createDropdown(propertyName, EnumTypeHandler.getEnumNames(ScreenStandbyEnum.class), enumName), enumName);
						} else {
							stats.put(propertyName, NeatPulseConstant.NONE);
						}
					}
					break;
				case DATE_FORMAT:
					String enumName = EnumTypeHandler.getNameByValue(DateFormatEnum.class, value);
					if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
						addAdvancedControlProperties(advancedControllableProperties, stats,
								createDropdown(propertyName, EnumTypeHandler.getEnumNames(DateFormatEnum.class), enumName), enumName);
					} else {
						stats.put(propertyName, NeatPulseConstant.NONE);
					}
					break;
				case LANGUAGE:
					enumName = EnumTypeHandler.getNameByValue(LanguageEnum.class, value);
					if (!NeatPulseConstant.NONE.equalsIgnoreCase(enumName)) {
						addAdvancedControlProperties(advancedControllableProperties, stats,
								createDropdown(propertyName, EnumTypeHandler.getEnumNames(LanguageEnum.class), enumName), enumName);
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
						addAdvancedControlProperties(advancedControllableProperties, stats,
								createDropdown(propertyName, EnumTypeHandler.getEnumNames(ColorCorrectionEnum.class), enumName), enumName);
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
		for (String id : deviceList) {
			if (roomName.equals(cachedMonitoringDevice.get(id).get(DeviceInfo.ROOM_NAME.getPropertyName()))) {
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
}
