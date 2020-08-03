preferences
{
	input("username",	"text",		title: "Camera username",	description: "Username for web login")
	input("password",	"password",	title: "Camera password",	description: "Password for web login")
	input("url",		"text",		title: "IP or URL of camera",	description: "Do not include http://")
	input("port",		"text",		title: "Port",			description: "Port")
}

metadata {
	definition (name: "Android IP Camera Beta", author: "Alan", namespace: "alt") {
		capability "Image Capture"
		capability "Switch"
		capability "Actuator"
		capability "Battery"
       	capability "Illuminance Measurement"
		capability "Temperature Measurement"
        capability "Motion Sensor"

		command "ledOn"
		command "ledOff"
		command "focusOn"
		command "focusOff"
		command "overlayOn"
		command "overlayOff"
		command "nightVisionOn"
		command "nightVisionOff"
		command "refresh"


	}

	tiles {
		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
			state("default", label: '', action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF")
		}

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
			state("take", label: 'Take Photo', action: "Image Capture.take", icon: "st.camera.take-photo", nextState:"taking")
			state("taking", label: 'Taking...', action: "Image Capture.take", icon: "st.camera.take-photo", backgroundColor: "#79b821")
		}

		standardTile("record", "device.switch", width: 1, height: 1) {
			state("recordOff", label: 'Record Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("recordOn", label: 'Record On', action:"switch.off", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}




		standardTile("led", "device.led", width: 1, height: 1) {
			state("ledOff", label: 'Led Off', action:"ledOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("ledOn", label: 'Led On', action:"ledOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("focus", "device.focus", width: 1, height: 1) {
			state("focusOff", label: 'Focus Off', action:"focusOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("focusOn", label: 'Focus On', action:"focusOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("overlay", "device.overlay", width: 1, height: 1) {
			state("overlayOff", label: 'Overlay Off', action:"overlayOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("overlayOn", label: 'Overlay On', action:"overlayOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}

		standardTile("nightVision", "device.nightVision", width: 1, height: 1) {
			state("nightVisionOff", label: 'Night Vision Off', action:"nightVisionOn", icon:"st.switches.light.off", backgroundColor: "#ffffff")
			state("nightVisionOn", label: 'Night Vision On', action:"nightVisionOff", icon:"st.switches.light.on", backgroundColor: "#79b821")
		}


		valueTile("battery", "device.battery", decoration: "flat", width: 1, height: 1) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        
		valueTile("light", "device.illuminance", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
			state "default", label:'Lux\n${currentValue}'
		}

		valueTile("magnetic", "device.magnetic", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
			state "default", label:'Mag\n${currentValue}'
		}
		valueTile("magy", "device.magy", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
			state "default", label:'Magy\n${currentValue}'
		}
		valueTile("magz", "device.magz", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
			state "default", label:'Magz\n${currentValue}'
		}
		multiAttributeTile(name: "motion", type: "generic", width: 3, height: 2) {
			tileAttribute("device.motionevent", key: "PRIMARY_CONTROL") {
				attributeState "1.0", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#53a7c0"
				attributeState "0", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
			}
            }
		main "camera"
		details(["cameraDetails","camera","take","record","led","focus","overlay","nightVision","battery","light","motion","magnetic","magy","magz"])
	}
}


def parseCameraResponse(def response) {
	if(response.headers.'Content-Type'.contains("image/jpeg")) {
		def imageBytes = response.data

		if(imageBytes) {
			storeImage(getPictureName(), imageBytes)
		}
	} else {
		log.error("${device.label} could not capture an image.")
	}
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	"image" + "_$pictureUuid" + ".jpg"
}

private take() {
	log.info("${device.label} taking photo")

	httpGet("http://${username}:${password}@${url}:${port}/photo_save_only.jpg"){
		httpGet("http://${username}:${password}@${url}:${port}/photo.jpg"){
			response -> log.info("${device.label} image captured")
			parseCameraResponse(response)
		}
	}
}

def on(theSwitch="record") {
	def sUrl
	switch ( theSwitch ) {
		case "led":
			sUrl = "enabletorch"
			break

		case "focus":
			sUrl = "focus"
			break

		case "overlay":
			sUrl = "settings/overlay?set=on"
			break

		case "nightVision":
			sUrl = "settings/night_vision?set=on"
			break

		default:
			sUrl = "/startvideo?force=1"
	}

	httpGet("http://${username}:${password}@${url}:${port}/${sUrl}"){
		response -> log.info("${device.label} ${theSwitch} On")
		sendEvent(name: "${theSwitch}", value: "${theSwitch}On")
	}

}

def off(theSwitch="record") {
	def sUrl
	switch ( theSwitch ) {
		case "led":
			sUrl = "disabletorch"
			break

		case "focus":
			sUrl = "nofocus"
			break

		case "overlay":
			sUrl = "settings/overlay?set=off"
			break

		case "nightVision":
			sUrl = "settings/night_vision?set=off"
			break

		default:
			sUrl = "stopvideo?force=1"
	}

	httpGet("http://${username}:${password}@${url}:${port}/${sUrl}"){
		response -> log.info("${device.label} ${theSwitch} Off")
		sendEvent(name: "${theSwitch}", value: "${theSwitch}Off")
	}

}

def ledOn() { on("led") }

def ledOff() { off("led") }

def focusOn() { on("focus") }

def focusOff() { off("focus") }

def overlayOn() { on("overlay") }

def overlayOff() { off("overlay") }

def nightVisionOn() { on("nightVision") }

def nightVisionOff() { off("nightVision") }

def installed() { runPeriodically(20*60, poll) }

def configure() { poll() }

def poll() { refresh() }

def refresh() { getSensors() }

def cToF(temp) {
	return temp * 1.8 + 32
}

def getSensors() {

	def params = [
		uri: "http://${username}:${password}@${url}:${port}",
		path: "/sensors.json",
		contentType: 'application/json'
	]

	log.debug "Params = ${params}"

	def theSensor
	def theUnit
	def theData
    def n

	try {
		httpGet(params) { 
			response -> log.debug "Start httpGet"
			response.data.each {
				key,value -> theSensor = key
				theUnit = value.unit
				if (value.data[0][1].size() == 1) {
					theData = value.data[0][1].first() 
					if (theSensor == "battery_level") {theSensor = "battery"}
					if (theSensor == "battery_temp") {
						theSensor = "device temperature"
						theUnit = "F"
						theData = cToF(theData as Integer)
                    }
					if (theSensor == "ambient_temp") {
						theSensor = "temperature"
						theUnit = "F"
						theData = cToF(theData as Integer)
                    }

                    if (theSensor == "battery_voltage") {
                    	theSensor = "battery Volt"
                    }
                    

					if (theSensor == "light") {
						theSensor = "illuminance"
						theUnit = "lux"
					}
                    
                    if (theSensor == "motion_event") {
                    	theSensor = "motionevent"
                        		
                    }
		
					log.info "name: ${theSensor}, value: ${theData}, unit: ${theUnit}"
					sendEvent(name:"${theSensor}", value: theData as Integer, unit:"${theUnit}")
    
               } else { theData = value.data[0][1] }
				log.debug "${theSensor}: ${theUnit} ${theData}"
			}
		}
	}
	catch(e) { log.debug "$e" }
}