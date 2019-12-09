
#include <NTPClient.h>

//#include <ESP8266WiFi.h>

#include <WiFiUdp.h>

#include <EEPROM.h>

#include <ESP8266WiFi.h>


#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"

#include "FirebaseESP8266.h"

//legacy login fuer database

//#define FIREBASE_HOST "https://smide-a9506.firebaseio.com"
#define FIREBASE_HOST "https://swt-e-health.firebaseio.com"


//#define FIREBASE_AUTH "nGYcfaMtft9SY0vTJehr6EqsF92IdYEsOujoyqDM"
#define FIREBASE_AUTH "1OOxcA7NAImF3mKplmX6Be6Hi1wrNy81SFeyulRS"



//wlan mit welchem sich verbunden wird
//#define WIFI_SSID "aNetwork"
#define WIFI_SSID "TC-A46AF"

//#define WIFI_PASSWORD "12345678"
#define WIFI_PASSWORD "Kzkm64Kvhc56"



#define LED 2


//name unter welchem die daten bagelegt werden
#define NAME "device1"



//firebase objekt
FirebaseData firebaseData;


//ntp objekt
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 0);


#define SEALEVELPRESSURE_HPA (1013.25)

//sensor objekt
Adafruit_BME680 bme; // I2C





void setup() {
  pinMode(LED, OUTPUT);


  Serial.begin(115200);
  
  //verbinde mit wlan
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  Serial.print("connected: ");
  Serial.println(WiFi.localIP());


  //starte kommunikation mit sensor
  if (!bme.begin(0x76)) 
  {
    Serial.println("Could not find a valid BME680 sensor, check wiring!");
    while (1);
  }
  Serial.println();
  //setze werte für sensor
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms



  //verbinde mit Firebase
  
  //4. Setup Firebase credential in setup()
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

}


int testNum=0;

void loop() {


  //lese sensor aus
   if (! bme.performReading()){
    Serial.println("Failed to perform reading :(");
    return;
  }

  //hole die aktuelle Zeit
  timeClient.update();
  //Serial.println(timeClient.getEpochTime());
  //sendData("test",testNum+.5,timeClient.getEpochTime());

  //schreibe die gemessenen daten
  sendData("temperature",bme.temperature,timeClient.getEpochTime());
  sendData("pressure",bme.pressure,timeClient.getEpochTime());
  sendData("humidity",bme.humidity,timeClient.getEpochTime());
  sendData("gas",bme.gas_resistance / 1000.0,timeClient.getEpochTime());

  //warte bis zum nächsten durchlauf
  delay(20000);
}


//senden eines datenpunktes
//category, spalte unter welcher abgespeichert wird
//data, der datenpnukt
//time, zeit der aufnahme
void sendData(char* category,double data,int time){
  
  FirebaseJson json;

  String dstr(data);
  
  json.set("time", time);
  json.set("myData",dstr);

  char path[64];
  sprintf(path,"/%s/%s",NAME,category);
  if (Firebase.pushJSON(firebaseData,path, json)) {

    Serial.println(firebaseData.dataPath());

    Serial.println(firebaseData.pushName());

    Serial.println(firebaseData.dataPath() + "/" + firebaseData.pushName());

  } else {
    Serial.println(firebaseData.errorReason());
  }
}



/*
 int val = 0;

  if (Firebase.getInt(firebaseData, "/LEDStatus")) {

    if (firebaseData.dataType() == "int") {
      Serial.println(firebaseData.intData());
      val = firebaseData.intData();
    }

  } else {
    Serial.println(firebaseData.errorReason());
  }

  if (!val) {
    digitalWrite(LED, HIGH);
  } else {
    digitalWrite(LED, LOW);
  }
*/
