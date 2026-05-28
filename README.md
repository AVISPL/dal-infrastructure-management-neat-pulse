# Neat Pulse Integration - Capabilities & Configuration
This document covers Neat Pulse Aggregator Capabilities and Configuration.

Symphony integrates with Neat Pulse to provide comprehensive monitoring and control of Neat devices across an organization.
Main features are: real-time device health monitoring, room sensor data (air quality, occupancy, temperature), firmware tracking, call status, and device settings management.

## Main use cases
- **Monitor** Neat device health, connection status, firmware versions, and call activity
- **Track** individual device details - IP address, serial number, primary mode, controller mode, in-call status
- **Manage** device settings such as display preferences, accessibility options, system settings, and time/language configuration
- **Sense** room environment - CO2, humidity, temperature, ambient light, people count, and VOC levels
- **Inventory** keep Neat devices and their associated Pulse rooms in check

## Prerequisites and where to start
Neat Pulse Aggregator communicates with the Neat Pulse API on behalf of an API token, generated from the Neat Pulse user interface after creating an API integration.

Required credentials are:
- Organization Id -> used as the Username in Symphony device configuration
- API Token -> generated from the Neat Pulse UI, used as the Password in Symphony device configuration

The current Neat Pulse API base URL is: https://api.pulse.neat.no/
Note: The old base URL (https://pulse.neat.no/api) is no longer supported as of July 1, 2025. If the management address pulse.neat.no is used, an adapter property baseUri must be set to "api". If api.pulse.neat.no is used, no additional configuration is needed.

## Neat Pulse Device Configuration and Provisioning
Once the API token is generated in Neat Pulse, use the Organization Id and token for the Symphony device configuration.

Once the Neat Pulse device is created with Monitoring Service -> Advanced Monitoring, HTTPS management protocol must be selected.
- Management Address: api.pulse.neat.no (or pulse.neat.no with baseUri adapter property set to "api")
- Protocol: HTTPs
- Username: Organization Id
- Password: API Token
- Port: 443

When the device is configured, saved and set active, the Neat Pulse Aggregator will start communicating with the Neat Pulse API to retrieve data about registered Neat devices based on the provided configuration.
By default, unprovisioned devices will appear on Aggregated Devices -> Unprovisioned Devices tab.
To provision a device, click the (+) icon, fill in Type, Category (Single Codecs), Manufacturer (Neat), and Model (e.g. Neat Bar), then click Import and confirm.

Devices and available device data can be tuned by adapter configuration properties:

| Property | Description |
|---|---|
| filterByPulseRoomName | Filter devices by Pulse Room name(s). Only devices belonging to specified rooms and their Room Sensor Information will be displayed in Symphony. |
| filterByExcludingPulseRoomName | Exclude specific Pulse Rooms. Only applies when filterByPulseRoomName is blank. | 
| DevicePollingInterval(minutes) | Time in minutes for the adapter to refresh device properties in Symphony. Default 5 min (minimum value) |
| baseUri | Required only when management address is pulse.neat.no |
| historicalProperties | Historical/graphable sensor properties: Temperature(C), CO2eq(ppm), RelativeHumidity(%), AmbientLight(lx), PeopleCount, VOC(ppb), VOCIndex. |

Note: The Neat Pulse API enforces a rate limit of 10 requests per enrolled device per 5 minutes (200 requests per 5 minutes for organizations with fewer than 20 devices), and a maximum of 15 requests/second per integration token. Symphony requires 3 requests per device per polling interval. The minimum polling interval is therefore 5 minutes. If the rate limit is exceeded, Symphony will display an error and an HTTP 429 will be returned.

For detailed information on the aggregator and its configuration, please refer to our knowledgebase -> https://symphony.knowledgeowl.com/help/neat-pulse-technical-breakdown

## Available Monitored Data
Neat Pulse Aggregator monitored data consists of 2 parts: Aggregator extended properties and Aggregated Device extended properties.

Aggregator properties:
- AdapterBuildDate, AdapterUptime, AdapterUptime(min), AdapterVersion, LastMonitoringCycleDuration(s), MonitoredDevicesTotal, MonitoringCycleInterval(min), NumberOfPulseRooms

Aggregated Devices properies (monitoring and control):

| Section | Type | Description |
| --- | --- | --- |
| Connected | Monitor | Indicates whether the device is currently connected to Neat Pulse (TRUE/FALSE). |
| ConnectionTime | Monitor | Time the device connected to (or disconnected from) Neat Pulse. |
| ControllerMode | Monitor | Displays the operating mode for Neat Pads, such as Room Scheduler or Room Controller. |
| DeviceId | Monitor | Unique device identifier. |
| DeviceName | Monitor | Name of the device. |
| DeviceModel | Monitor | Hardware model of the device. |
| DeviceOnline | Monitor | Whether the device is online. |
| Firmware Information | Monitor | Provides firmware details. |
| InCallStatus | Monitor | Indicates whether the device is idle or currently in an active Zoom or Teams call. |
| OTAChannel | Monitor | Over-the-air update channel. |
| PrimaryMode | Monitor | Shows the current operating mode such as Zoom Rooms, Microsoft Teams, App Hub, USB BYOD, or OOB. |
| Reboot | Control | Triggers a device reboot. |
| Serial | Monitor | Device serial number. |
| Accessibility group | Monitor + Control | Manages accessibility-related settings. |
| AudioAndVideo group | Monitor + Control | Manages audio and video related settings. |
| Display group | Monitor + Control | Controls display-related settings such as appearance, brightness, and screen preferences. |
| Sensor Information group | Monitor | Reports environmental sensor metrics. |
| Room Sensor Information group | Monitor | Reports the same environmental sensor metrics at the Pulse Room level. |
| System group | Monitor + Control | Manages system-related settings including Bluetooth and BYOD mode. |
| Time And Language group | Monitor + Control | Manages regional settings such as time zone and language configuration. |

Supported device models and their sensor capabilities:

| Device | Model ID | Model Code | Sensors |
|---|---|---|---|
| Neat Pad | A1 | NF19A1 | Relative Humidity, Temperature, CO2eq, VOC |
| Neat Bar | B1 | NF19B1 | Relative Humidity, Temperature, CO2eq, VOC, Ambient Light, People Count |
| Neat Board | C1 | NF20C1 | Relative Humidity, Temperature, CO2eq, VOC, Ambient Light, People Count |
| Neat Bar Pro | D1 | NF21D1 | Relative Humidity, Temperature, VOC Index, Ambient Light, People Count |
| Neat Bar Gen 2 | E1 | NF22E1 | Ambient Light, People Count |
| Neat Frame | F1 | NF21F1 | Relative Humidity, Temperature, VOC Index, Ambient Light, People Count |
| Neat Board 50 | H1 | NF22H1 | Relative Humidity, Temperature, Ambient Light, People Count |
| Neat Board Pro | K1 | NF23K1 | Relative Humidity, Temperature, VOC Index, Ambient Light, People Count |
| Neat Center | L1 | NF23L1 | People Count |

## Troubleshooting
**Login Error**
- Check that the Organization Id (username) and API Token (password) are correct in the Symphony device configuration
- Verify that the API token is still active and has not expired in the Neat Pulse UI
- Ensure the management address is set to api.pulse.neat.no (or pulse.neat.no with baseUri adapter property = "api")

**API Error / Rate Limit (HTTP 429)**
- Check the API error description in Symphony
- If the rate limit is exceeded, wait up to 5 minutes before retrying - Symphony will display "You have exceeded the maximum rate limit of 10 requests per enrolled device within 5 minutes."
- Consider increasing the DevicePollingInterval(minutes) adapter property to reduce API usage

**Link Error / Ping Timeout**
- Make sure your Cloud Connector can reach api.pulse.neat.no on port 443
- Check the Ping Protocol in the Symphony Neat Pulse Aggregator device configuration
- Try switching between ICMP/TCP modes, as certain protocols may be blocked by proxy settings

**Missing Properties / Devices**
- If sensor or display properties are missing for a device, verify the device model - not all properties are supported on all models (e.g. Neat Pad does not support ScreenStandby, AutoWakeup, DisplayPreference, HDMICECControl, or RoomSensorInformation)
- If devices are not appearing, check the filterByPulseRoomName and filterByExcludingPulseRoomName adapter properties to ensure the desired rooms are not filtered out

If none of the recommended steps help, please enter an SOS ticket at {https://avi-spl.atlassian.net/servicedesk/customer/portals}

## What Melody can do with it:
- Find Neat Pulse Aggregated Devices (Neat Pulse Aggregator as Monitoring Proxy)
- Verify Neat Pulse Aggregator configuration
- Check device online/offline status (Connected, deviceOnline properties)
- Check room sensor readings (temperature, CO2, humidity, air quality, occupancy)

## What Melody cannot do with it:
- Provision the devices
- Generate or rotate API tokens (must be done in the Neat Pulse UI)
- Push firmware updates directly