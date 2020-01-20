
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
#include "FS.h"
//legacy login fuer database

//#define FIREBASE_HOST "https://smide-a9506.firebaseio.com"
#define FIREBASE_HOST "https://swt-e-health.firebaseio.com"


//#define FIREBASE_AUTH "nGYcfaMtft9SY0vTJehr6EqsF92IdYEsOujoyqDM"
#define FIREBASE_AUTH "1OOxcA7NAImF3mKplmX6Be6Hi1wrNy81SFeyulRS"



//wlan mit welchem sich verbunden wird
#define WIFI_SSID "aNetwork"
//#define WIFI_SSID "TC-A46AF"

#define WIFI_PASSWORD "12345678"
//#define WIFI_PASSWORD "Kzkm64Kvhc56"



#define LED 2


//name unter welchem die daten bagelegt werden
//#define NAME "device1"


char deviceName[64]="unnnamed";


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

  Serial.printf("starting spiffs %i\n", SPIFFS.begin());
  //Serial.printf("formatting spiffs %i\n", SPIFFS.format());



  //verbinde mit wlan
  char password[64] = "pass";
  char ssid[64] = "ssid";
  loadSSID(ssid);
  loadPass(password);
  loadID(deviceName);
  Serial.printf("loaded pass|%s|\n", password);
  Serial.printf("loaded ssid|%s|\n", ssid);
  WiFi.begin(ssid, password);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    readLine(2000);
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


int testNum = 0;

void loop() {


  //lese sensor aus
  if (! bme.performReading()) {
    Serial.println("Failed to perform reading :(");
    return;
  }

  //hole die aktuelle Zeit
  timeClient.update();
  //Serial.println(timeClient.getEpochTime());
  //sendData("test",testNum+.5,timeClient.getEpochTime());

  //schreibe die gemessenen daten
  sendData("temperature", bme.temperature, timeClient.getEpochTime());
  sendData("pressure", bme.pressure, timeClient.getEpochTime());
  sendData("humidity", bme.humidity, timeClient.getEpochTime());
  sendData("gas", 1.0f -bme.gas_resistance / 70000.0, timeClient.getEpochTime());

  //warte bis zum nächsten durchlauf
  //delay(600000);
  readLine(600000);
}


//senden eines datenpunktes
//category, spalte unter welcher abgespeichert wird
//data, der datenpnukt
//time, zeit der aufnahme
void sendData(char* category, double data, int time) {

  FirebaseJson json;

  String dstr(data);

  json.set("time", time);
  json.set("myData", dstr);

  char path[64];
  sprintf(path, "/%s/%s", deviceName, category);
  if (Firebase.pushJSON(firebaseData, path, json)) {

    Serial.println(firebaseData.dataPath());

    Serial.println(firebaseData.pushName());

    Serial.println(firebaseData.dataPath() + "/" + firebaseData.pushName());
    Serial.println(dstr);
  } else {
    Serial.println(firebaseData.errorReason());
  }
}


void loadPass(char* pass) {
  File f = SPIFFS.open("/pass.txt", "r");
  if (!f) {
    Serial.println("file open failed");
  } else {
    int c ;
    char* p = pass;
    while (f.available()) {
      c = f.read();
      //Serial.printf("%c", c);
      *p = c;
      //Serial.printf("%s", *pass);
      p++;
      //delay(1000);
    }
    f.close();
  }
}

void setPass(char* pass) {
  File f = SPIFFS.open("/pass.txt", "w");
  if (!f) {
    Serial.println("file open failed");
  } else {
    f.print(pass);
    f.close();
  }
}

void loadSSID(char* ssid) {
  File f = SPIFFS.open("/ssid.txt", "r");
  if (!f) {
    Serial.println("file open failed");
  } else {
    int c ;
    char* p = ssid;
    while (f.available()) {
      c = f.read();
      //Serial.printf("%c", c);
      *p = c;
      //Serial.printf("%s", *ssid);
      p++;
      //delay(1000);
    }
    f.close();
  }
}


void putSSID(char* ssid) {
  File f = SPIFFS.open("/ssid.txt", "w");
  if (!f) {
    Serial.println("file open failed");
  } else {
    f.print(ssid);
    f.close();
  }
}

void putID(char* id) {
  File f = SPIFFS.open("/id.txt", "w");
  if (!f) {
    Serial.println("file open failed");
  } else {
    f.print(id);
    f.close();
  }
}



void loadID(char* id) {
  File f = SPIFFS.open("/id.txt", "r");
  if (!f) {
    Serial.println("file open failed");
  } else {
    int c ;
    char* p = id;
    while (f.available()) {
      c = f.read();
      //Serial.printf("%c", c);
      *p = c;
      //Serial.printf("%s", *ssid);
      p++;
      //delay(1000);
    }
    f.close();
  }
}

void readLine(int timeout) {
  char command[64];
  command[64] = 0;
  bool stopped = false;
  int startTime = millis();
  int i = 0;
  while (true) {
    if (Serial.available()) {
      char b = Serial.read();
      if (b == 0) {
        yield();
        delay(500);
      } else {
        command[i] = b;
        //Serial.println(b);
        i++;
        if (b == '\n') {
          command[i + 1] = 0;
          break;
        }
      }
    } else {
      yield();
      delay(500);
    }

    if (i >= 64) {
      stopped = true;
      break;
    }
    if (millis() - startTime > timeout) {
      stopped = true;
      break;
    }
  }
  if (!stopped) {
    Serial.printf("Command %s\n", command);
    char * pch = strtok(command, " ");
    Serial.printf("part1 |%s|\n", pch);

    /*while (pch != NULL)
      {
      Serial.printf ("%s\n",pch);
      pch = strtok (NULL, " ");
      }*/

    if (strcmp(pch, "setPass") == 0) {
      pch = strtok (NULL, " \n");
      Serial.println("setting pass");
      if (pch != NULL) {
        Serial.printf("new pass:|%s|\n", (char*)pch);
        setPass(pch);
      }
    }

    if (strcmp(pch, "setSSID") == 0) {
      pch = strtok (NULL, " \n");
      Serial.println("setting ssid");
      if (pch != NULL) {
        Serial.printf("new ssid|%s|\n", (char*)pch);
        putSSID(pch);
      }
    }

    if (strcmp(pch, "setID") == 0) {
      pch = strtok (NULL, " \n");
      Serial.println("setting id");
      if (pch != NULL) {
        Serial.printf("new id|%s|\n", (char*)pch);
        putID(pch);
      }
    }
  }
};
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
