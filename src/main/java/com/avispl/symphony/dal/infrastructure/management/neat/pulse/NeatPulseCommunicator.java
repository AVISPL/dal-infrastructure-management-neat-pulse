/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.neat.pulse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.NeatPulseCommand;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.NeatPulseConstant;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.PingMode;
import com.avispl.symphony.dal.infrastructure.management.neat.pulse.common.information.DeviceInfo;
import com.avispl.symphony.dal.util.StringUtils;


public class NeatPulseCommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {
	/**
	 * Process that is running constantly and triggers collecting data from Nureva Console SE API endpoints, based on the given timeouts and thresholds.
	 *
	 * @author Harry
	 * @since 1.0.0
	 */
	class NurevaConsoleDataLoader implements Runnable {
		private volatile boolean inProgress;
		private volatile boolean flag = false;

		public NurevaConsoleDataLoader() {
			inProgress = true;
		}

		@Override
		public void run() {
			loop:
			while (inProgress) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// Ignore for now
				}

				if (!inProgress) {
					break loop;
				}

				// next line will determine whether Nureva Console monitoring was paused
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
						//
					}
				}

				if (!inProgress) {
					break loop;
				}
				if (flag) {
					nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + 60000;
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
	 * Update the status of the device.
	 * The device is considered as paused if did not receive any retrieveMultipleStatistics()
	 * calls during {@link NeatPulseCommunicator}
	 */
	private synchronized void updateAggregatorStatus() {
		devicePaused = validRetrieveStatisticsTimestamp < System.currentTimeMillis();
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
	 * A private field that represents an instance of the NurevaConsoleLoader class, which is responsible for loading device data for Nureva Console
	 */
	private NurevaConsoleDataLoader deviceDataLoader;

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
	 * number of threads
	 */
	private Integer numberThreads;

	/**
	 * start index
	 */
	private int startIndex = NeatPulseConstant.START_INDEX;

	/**
	 * end index
	 */
	private int endIndex = NeatPulseConstant.NUMBER_DEVICE_IN_INTERVAL;

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
	 * Constructs a new instance of NurevaConsoleCommunicator.
	 *
	 * @throws IOException If an I/O error occurs while loading the properties mapping YAML file.
	 */
	public NeatPulseCommunicator() throws IOException {
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
			retrieveSystemInfo();
			retrieveRoomInfo();
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
		return null;
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
		executorService.submit(deviceDataLoader = new NurevaConsoleDataLoader());
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
		super.internalDestroy();
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
			deviceList.clear();
			JsonNode response = this.doGet(String.format(NeatPulseCommand.ALL_DEVICE_ID_COMMAND, this.getLogin()), JsonNode.class);
			if (response != null && response.has(NeatPulseConstant.ENDPOINTS) && response.get(NeatPulseConstant.ENDPOINTS).isArray()) {
				for (JsonNode node : response.get(NeatPulseConstant.ENDPOINTS)) {
					deviceList.add(node.get(NeatPulseConstant.ID).asText());
				}
			}
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Error when the login. Please check the password");
		} catch (Exception ex) {
			throw new ResourceNotReachableException(String.format("Error when retrieve system information. %s", ex.getMessage()), ex);
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
			throw new ResourceNotReachableException(String.format("Error when retrieve room information. %s", ex.getMessage()), ex);
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
		stats.put("NumberOfConsoleRooms", String.valueOf(countRoom));
	}

	/**
	 * Populates device details using multiple threads.
	 * Retrieves aggregated data for each device in the device list concurrently.
	 */
	private void populateDeviceDetails() {
		int numberOfThreads = getDefaultNumberOfThread();
		ExecutorService executorServiceForRetrieveAggregatedData = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<?>> futures = new ArrayList<>();

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
			endIndex = NeatPulseConstant.NUMBER_DEVICE_IN_INTERVAL;
		} else {
			startIndex = endIndex;
			endIndex += NeatPulseConstant.NUMBER_DEVICE_IN_INTERVAL;
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
}
