/**
 *  SmartThings REST API
 *
 *  Copyright 2017 Brady Holt
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      https://opensource.org/licenses/MIT
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "REST API",
    namespace: "bradyholt.smartthingsrest",
    author: "Julian Werfel",
    description: "SmartThings REST API",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "SmartThings REST API", displayLink: ""]
)

mappings {
  path("/devices") {
    action: [
        GET: "listDevices"
    ]
  }
  path("/device/:id") {
    action: [
        GET: "deviceDetails"
    ]
  }
  path("/device/:id/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValue"
    ]
  }
  path("/devices/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValueForDevices"
    ]
  }
  path("/device/:id/command/:name") {
    action: [
        POST: "deviceCommand"
    ]
  }
}

preferences {
  section() {
    input "devices", "capability.actuator", title: "Devices", multiple: true
    input "sensors", "capability.sensor", title: "Sensors", multiple: true
    input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", multiple: true
    input "presenceSensor", "capability.presenceSensor", title: "Presence", multiple: true
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def initialize() {
}

def listDevices() {
  def resp = []
  devices.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  sensors.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
    temperatures.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  presenceSensor.each {
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName
    ]
  }
  return resp
}

def deviceDetails() {
  def device = getDeviceById(params.id)

  def supportedAttributes = []
  device.supportedAttributes.each {
    supportedAttributes << it.name
  }

  def supportedCommands = []
  device.supportedCommands.each {
    def arguments = []
    it.arguments.each { arg ->
      arguments << "" + arg
    }
    supportedCommands << [
        name: it.name,
        arguments: arguments
    ]
  }

  return [
      id: device.id,
      label: device.label,
      manufacturerName: device.manufacturerName,
      modelName: device.modelName,
      name: device.name,
      displayName: device.displayName,
      supportedAttributes: supportedAttributes,
      supportedCommands: supportedCommands
  ]
}

def deviceGetAttributeValueForDevices() {
  def resp = []

  def args = params.arg
  //log.info("Args: " + args);
    
  def deviceIds = args.split(',');
  //log.info("deviceIds: " + deviceIds);
  def name = params.name
  //log.info("ParamName: " + name);

  deviceIds.each {
    def device = getDeviceById(it);
    if(device != null) {
        def value = device.currentValue(name);
        resp << [
          id: it,
          value: value
        ]
    }
    else {
    	log.warn("Could not find device " + it);
    }

  }

  return resp;
}

def deviceGetAttributeValue() {
  def device = getDeviceById(params.id)
  def name = params.name
  def value = device.currentValue(name);
  return [
      value: value
  ]
}

def deviceCommand() {
  def device = getDeviceById(params.id)
  def name = params.name
  def args = params.arg
  if (args == null) {
    args = []
  } else if (args instanceof String) {
    args = [args]
  }
  log.debug "device command: ${name} ${args}"
  switch(args.size) {
    case 0:
      device."$name"()
      break;
    case 1:
      device."$name"(args[0])
      break;
    case 2:
      device."$name"(args[0], args[1])
      break;
    default:
      throw new Exception("Unhandled number of args")
  }
}

def getDeviceById(id) {
  def device = devices.find { it.id == id }
  if(device == null)
  	device = sensors.find{it.id == id}
  if(device == null)
  	device = temperatures.find{it.id == id}
  if(device == null)
    device = presenceSensor.find{it.id == id}
  return device;
}