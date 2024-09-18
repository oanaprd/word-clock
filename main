#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include "RTClib.h"
#include <ArduinoBLE.h>

#define NUM_LEDS_LINE1 13
#define NUM_LEDS_LINE2 65
#define NUM_LEDS_LINE3 52
#define NUM_LEDS_LINE4 4

#define LED_PIN1 5
#define LED_PIN2 4
#define LED_PIN3 3
#define LED_PIN4 2
#define PHOTORESISTOR_PIN A0

#define BUTTON_PIN_HOUR 7
#define BUTTON_PIN_MINUTE 8
#define BUTTON_PIN_COLOR 9
#define BUTTON_PIN_BRIGHTNESS 10
#define BUTTON_PIN_RESET 11

#define LED_HOUR_BUTTON 0
#define LED_MINUTE_BUTTON 1
#define LED_COLOR_BUTTON 2
#define LED_BRIGHTNESS_BUTTON 3

#define MAX_BRIGHTNESS 255

Adafruit_NeoPixel strip1 = Adafruit_NeoPixel(NUM_LEDS_LINE1, LED_PIN1, NEO_GRBW + NEO_KHZ800);
Adafruit_NeoPixel strip2 = Adafruit_NeoPixel(NUM_LEDS_LINE2, LED_PIN2, NEO_GRBW + NEO_KHZ800);
Adafruit_NeoPixel strip3 = Adafruit_NeoPixel(NUM_LEDS_LINE3, LED_PIN3, NEO_GRBW + NEO_KHZ800);
Adafruit_NeoPixel strip4 = Adafruit_NeoPixel(NUM_LEDS_LINE4, LED_PIN4, NEO_GRBW + NEO_KHZ800);
RTC_DS1307 rtc;

int brightnessLevels[] = {50, 100, 150, 200, 255};
int currentBrightnessIndex = 4;
int currentColorIndex = 0;
uint8_t currentColorR = 255;
uint8_t currentColorG = 0;
uint8_t currentColorB = 0;
uint32_t currentColor = 0;
int brightness = 50;

bool autoBrightness = true;

const uint8_t AM_LEDS[] = {9, 10};
const uint8_t PM_LEDS[] = {11, 12};

const uint8_t HOUR_01[] = {13, 14, 15};
const uint8_t HOUR_02[] = {1, 2, 3, 4};
const uint8_t HOUR_03[] = {5, 6, 7, 8};
const uint8_t HOUR_04[] = {16, 17, 18, 19, 20};
const uint8_t HOUR_05[] = {21, 22, 23, 24, 25};
const uint8_t HOUR_06[] = {9, 10, 11, 12};
const uint8_t HOUR_07[] = {26, 27, 28, 29, 30};
const uint8_t HOUR_08[] = {39, 40, 41};
const uint8_t HOUR_09[] = {31, 32, 33, 34};
const uint8_t HOUR_10[] = {35, 36, 37, 38};
const uint8_t HOUR_11[] = {42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
const uint8_t HOUR_12[] = {52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63};

const uint8_t* HOUR_LEDS[] = {HOUR_12, HOUR_01, HOUR_02, HOUR_03, HOUR_04, HOUR_05, HOUR_06, HOUR_07, HOUR_08, HOUR_09, HOUR_10, HOUR_11};
const uint8_t HOUR_LEDS_COUNT[] = {12, 3, 4, 4, 5, 5, 4, 5, 3, 4, 4, 10};

const uint8_t MINUTE_00[] = {6, 7, 8};
const uint8_t MINUTE_05[] = {4, 5, 26, 27, 28, 29, 30, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_10[] = {4, 5, 9, 10, 11, 12, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_15[] = {4, 5, 21, 22, 41, 42, 43, 44, 45};
const uint8_t MINUTE_20[] = {4, 5, 13, 14, 15, 16, 17, 18, 19, 20, 39, 40, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_25[] = {4, 5, 13, 14, 15, 16, 17, 18, 19, 20, 23, 24, 26, 27, 28, 29, 30, 39, 40, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_30[] = {31, 32, 33, 34, 35, 36, 37, 38};
const uint8_t MINUTE_35[] = {0, 1, 2, 3, 13, 14, 15, 16, 17, 18, 19, 20, 23, 24, 26, 27, 28, 29, 30, 39, 40, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_40[] = {0, 1, 2, 3, 13, 14, 15, 16, 17, 18, 19, 20, 39, 40, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_45[] = {0, 1, 2, 3, 21, 22, 41, 42, 43, 44, 45};
const uint8_t MINUTE_50[] = {0, 1, 2, 3, 9, 10, 11, 12, 46, 47, 48, 49, 50, 51};
const uint8_t MINUTE_55[] = {0, 1, 2, 3, 26, 27, 28, 29, 30, 46, 47, 48, 49, 50, 51};

const uint8_t* MINUTE_LEDS[] = {MINUTE_00, MINUTE_05, MINUTE_10, MINUTE_15, MINUTE_20, MINUTE_25, MINUTE_30, MINUTE_35, MINUTE_40, MINUTE_45, MINUTE_50, MINUTE_55};
const uint8_t MINUTE_LEDS_COUNT[] = {3, 13, 12, 9, 18, 25, 8, 27, 20, 11, 14, 15};

uint32_t colors[] = {strip1.Color(255, 0, 0),   // Red
                     strip1.Color(0, 255, 0),   // Green
                     strip1.Color(0, 0, 255)};  // Blue
int numColors = sizeof(colors) / sizeof(colors[0]);

BLEService clockService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLECharacteristic colorChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLEWrite, 3);
BLECharacteristic brightnessChar("2A06", BLERead | BLEWrite, 2);
BLECharacteristic brightnessSliderChar("19B10002-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite, 2);
BLECharacteristic autoBrightnessChar("19B10003-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite, 1);
BLECharacteristic rainbow("19B10004-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite, 1);

void setup() {
  Serial.begin(9600);

  strip1.begin();
  strip2.begin();
  strip3.begin();
  strip4.begin();

  strip1.show();
  strip2.show();
  strip3.show();

  if (!rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  }
  
  if (!rtc.isrunning()) {
    Serial.println("RTC is not running, setting the time...");
    rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  }
  rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  pinMode(BUTTON_PIN_HOUR, INPUT);
  pinMode(BUTTON_PIN_MINUTE, INPUT);
  pinMode(BUTTON_PIN_COLOR, INPUT);
  pinMode(BUTTON_PIN_BRIGHTNESS, INPUT);
  pinMode(PHOTORESISTOR_PIN, INPUT);
  pinMode(BUTTON_PIN_RESET, INPUT);


  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");
    while (1);
  }
  BLE.setLocalName("WordClock");
  BLE.setAdvertisedService(clockService);
  clockService.addCharacteristic(colorChar);
  clockService.addCharacteristic(brightnessSliderChar);
  clockService.addCharacteristic(autoBrightnessChar);
  BLE.addService(clockService);
  BLE.advertise();

  Serial.println("Bluetooth device active, waiting for connections...");

  autoBrightnessChar.writeValue((uint8_t)autoBrightness);
  strip1.setBrightness(brightness);
  strip2.setBrightness(brightness);
  strip3.setBrightness(brightness);
  strip4.setBrightness(brightness);
}

void loop() {

  DateTime now = rtc.now();
  bool isAM = now.hour() < 12;

  int hour = now.hour();
  int minute = now.minute();

  if (hour == 0) {
    hour = 12;
  } else if (hour > 12) {
    hour -= 12;
    isAM = false;
  } else {
    isAM = true;
  }

  handleButtonPresses(now);

  uint32_t color = strip1.Color(currentColorR, currentColorG, currentColorB);

  strip4.clear();

  handleBLEEvents(now);

  if (autoBrightness) {
      int photoresistorValue = analogRead(PHOTORESISTOR_PIN);
      brightness = map(photoresistorValue, 1, 1023, 255, 50);
  }
  
  setBrightness(brightness);
  strip1.show();
  strip2.show();
  strip3.show();
  strip4.show();

  delay(1000);
}
void setBrightness(int brightness) {
    strip1.setBrightness(brightness);
    strip2.setBrightness(brightness);
    strip3.setBrightness(brightness);
    strip4.setBrightness(brightness);
    strip1.show();
    strip2.show();
    strip3.show();
    strip4.show();
}
void updateColor(uint32_t color, DateTime adjustedTime) {
  int alwaysOnLEDs[] = {0, 1, 2, 3, 5, 6, 7};
  for (int i = 0; i < 7; i++) {
    strip1.setPixelColor(alwaysOnLEDs[i], color);
  }
  
  bool isAM = adjustedTime.hour() < 12;
  const uint8_t* amLEDs = AM_LEDS;
  for (int i = 0; i < 2; i++) {
    strip1.setPixelColor(amLEDs[i], isAM ? color : strip1.Color(0, 0, 0));
  }
  
  const uint8_t* pmLEDs = PM_LEDS;
  for (int i = 0; i < 2; i++) {
    strip1.setPixelColor(pmLEDs[i], !isAM ? color : strip1.Color(0, 0, 0));
  }
  strip1.show();
  
  uint8_t hour = adjustedTime.hour();
  if (hour == 0) {
    hour = 12;
  } else if (hour > 12) {
    hour -= 12;
  }
  const uint8_t* hourLEDs = HOUR_LEDS[hour];
  for (int i = 0; i < HOUR_LEDS_COUNT[hour]; i++) {
    if (hourLEDs[i] < NUM_LEDS_LINE2) {
      strip2.setPixelColor(hourLEDs[i], color);
    }
  }
  strip2.show();

  uint8_t minute = adjustedTime.minute();
  int minuteIndex = (minute + 2) / 5;
  const uint8_t* minuteLEDs = MINUTE_LEDS[minuteIndex];
  for (int i = 0; i < MINUTE_LEDS_COUNT[minuteIndex]; i++) {
    if (minuteLEDs[i] < NUM_LEDS_LINE3) {
      strip3.setPixelColor(minuteLEDs[i], color);
    }
  }
  strip3.show();
  strip4.show();
}

void handleButtonPresses(DateTime &adjustedTime) {
  static bool hourButtonPressed = false;
  static bool minuteButtonPressed = false;
  static bool colorButtonPressed = false;
  static bool brightnessButtonPressed = false;

  int currentHourButtonState = digitalRead(BUTTON_PIN_HOUR);
  int currentMinuteButtonState = digitalRead(BUTTON_PIN_MINUTE);
  int currentColorButtonState = digitalRead(BUTTON_PIN_COLOR);
  int currentBrightnessButtonState = digitalRead(BUTTON_PIN_BRIGHTNESS);

  uint32_t color = strip1.Color(currentColorR, currentColorG, currentColorB);

  if (currentHourButtonState == HIGH) {
    adjustedTime = adjustedTime + TimeSpan(0, 1, 0, 0);
    rtc.adjust(adjustedTime);
    Serial.println("Hour button pressed");
    strip4.setPixelColor(LED_HOUR_BUTTON, color);
    strip4.show();
    hourButtonPressed = true;
  } else if (currentHourButtonState == LOW) {
    strip4.setPixelColor(LED_HOUR_BUTTON, strip4.Color(0, 0, 0));
    strip4.show();
    hourButtonPressed = false;
  }

  if (currentMinuteButtonState == HIGH) {
    adjustedTime = adjustedTime + TimeSpan(0, 0, 5, 0);
    rtc.adjust(adjustedTime);
    Serial.println("Minute button pressed");
    strip4.setPixelColor(LED_MINUTE_BUTTON, color);
    strip4.show();
    minuteButtonPressed = true;
  } else if (currentMinuteButtonState == LOW) {
    strip4.setPixelColor(LED_MINUTE_BUTTON, strip4.Color(0, 0, 0));
    strip4.show();
    minuteButtonPressed = false;
  }

  if (currentColorButtonState == HIGH) {
    currentColorIndex = (currentColorIndex + 1) % 10;
    Serial.println("Color button pressed");
    uint32_t color = getColorFromIndex(currentColorIndex);
    updateColorState((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    updateColor(color, adjustedTime);
    strip4.setPixelColor(LED_COLOR_BUTTON, color);
    strip4.show();
    colorButtonPressed = true;
  } else if (currentColorButtonState == LOW) {
    strip4.setPixelColor(LED_COLOR_BUTTON, strip4.Color(0, 0, 0));
    strip4.show();
    colorButtonPressed = false;
  }

  if (currentBrightnessButtonState == HIGH) {
    currentBrightnessIndex = (currentBrightnessIndex + 1) % 6;
    if (currentBrightnessIndex == 5) {
      autoBrightness = !autoBrightness;
      Serial.println("Brightness button pressed - auto brightness toggled");
    } else {
      autoBrightness = false;
      Serial.println("Brightness button pressed");
    }
    brightness = brightnessLevels[currentBrightnessIndex];
    brightnessChar.writeValue((uint8_t)brightness);
    strip4.setPixelColor(LED_BRIGHTNESS_BUTTON, color);
    strip4.show();
    brightnessButtonPressed = true;
  } else if (currentBrightnessButtonState == LOW) {
    strip4.setPixelColor(LED_BRIGHTNESS_BUTTON, strip4.Color(0, 0, 0));
    strip4.show();
    brightnessButtonPressed = false;
  }

  color = strip1.Color(currentColorR, currentColorG, currentColorB);
  updateLEDs(adjustedTime, color);

  strip1.show();
  strip2.show();
  strip3.show();
  strip4.show();
}

void handleBLEEvents(DateTime &adjustedTime) {
  BLEDevice central = BLE.central();
  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());

    while (central.connected()) {
      if (colorChar.written()) {
        uint8_t rgb[3];
        colorChar.readValue(rgb, 3);
        updateColorState(rgb[0], rgb[1], rgb[2]);
        int colorIndex = getColorIndexFromColor(strip1.Color(rgb[0], rgb[1], rgb[2]));
        currentColorIndex = colorIndex;
        Serial.print("Color updated from app - R: ");
        Serial.print(rgb[0]);
        Serial.print(" G: ");
        Serial.print(rgb[1]);
        Serial.print(" B: ");
        Serial.println(rgb[2]);

        uint32_t color = strip1.Color(rgb[0], rgb[1], rgb[2]);
        updateLEDs(adjustedTime, color);
      }
      if (brightnessSliderChar.written()) {
        uint8_t brightnessSliderValue = brightnessSliderChar.value()[0];
        autoBrightness = false;
        brightness = brightnessSliderValue;
        setBrightness(brightness);
      }
      if (autoBrightnessChar.written()) {
        autoBrightness = autoBrightnessChar.value()[0];
        Serial.print("Auto-brightness updated: ");
        Serial.println(autoBrightness);

        if (autoBrightness) {
          startAutoBrightness();
        } else {
          stopAutoBrightness(); 
          int brightness = brightnessLevels[currentBrightnessIndex];
          setBrightness(brightness);
        }
      }
      if (rainbow.written()) {
        colorSwirl();
      }
      uint32_t currentColor = strip1.Color(currentColorR, currentColorG, currentColorB);
      updateLEDs(adjustedTime, currentColor);

      strip1.show();
      strip2.show();
      strip3.show();
      strip4.show();
    }

    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }

  DateTime now = rtc.now();
  handleButtonPresses(now);

  if (autoBrightness) {
        int photoresistorValue = analogRead(PHOTORESISTOR_PIN);
        brightness = map(photoresistorValue, 1, 1023, 255, 50);
    }
  setBrightness(brightness);

  uint32_t currentColor = strip1.Color(currentColorR, currentColorG, currentColorB);
  updateLEDs(now, currentColor);

  delay(1000);
}

int getColorIndexFromColor(uint32_t color) {
  uint8_t r = (color >> 16) & 0xFF;
  uint8_t g = (color >> 8) & 0xFF;
  uint8_t b = color & 0xFF;

  if (r == 255 && g == 0 && b == 0) {
    return 0;
  } else if (r == 255 && g == 128 && b == 0) {
    return 1;
  } else if (r == 255 && g == 255 && b == 0) {
    return 2;
  } else if (r == 0 && g == 255 && b == 0) {
    return 3;
  } else if (r == 0 && g == 255 && b == 255) {
    return 4;
  } else if (r == 0 && g == 0 && b == 255) {
    return 5;
  } else if (r == 143 && g == 0 && b == 255) {
    return 6;
  } else if (r == 128 && g == 0 && b == 128) {
    return 7;
  } else if (r == 255 && g == 0 && b == 255) {
    return 8;
  } else if (r == 255 && g == 192 && b == 203) {
    return 9;
  } else if (r == 128 && g == 128 && b == 128) {
    return 10;
  } else {
    return -1;
  }
}

uint32_t getColorFromIndex(int colorIndex) {
  switch (colorIndex) {
    case 0: return strip1.Color(255, 0, 0, 0);
    case 1: return strip1.Color(255, 128, 0, 0);
    case 2: return strip1.Color(255, 255, 0, 0);
    case 3: return strip1.Color(0, 255, 0, 0);
    case 4: return strip1.Color(0, 255, 255, 0);
    case 5: return strip1.Color(0, 0, 255, 0);
    case 6: return strip1.Color(143, 0, 255, 0);
    case 7: return strip1.Color(128, 0, 128, 0);
    case 8: return strip1.Color(255, 0, 255, 0);
    case 9: return strip1.Color(255, 192, 203, 0);
    case 10: return strip1.Color(128, 128, 128, 0);
  }
}

void updateHourLEDs(int hour, uint32_t color) {
  strip2.clear();
  if (hour == 0) {
    hour = 12;
  } else if (hour > 12) {
    hour -= 12;
  }
  const uint8_t* hourLEDs = HOUR_LEDS[hour];
  int numLEDs = HOUR_LEDS_COUNT[hour];
  
  for (int i = 0; i < numLEDs; i++) {
    if (hourLEDs[i] < NUM_LEDS_LINE2) {
      strip2.setPixelColor(hourLEDs[i], color);
    }
  }
  
  strip2.show();
}

void updateMinuteLEDs(int minute, uint32_t color) {
  strip3.clear();
  int minuteIndex = (minute + 2) / 5;
  const uint8_t* minuteLEDs = MINUTE_LEDS[minuteIndex];
  int numLEDs = MINUTE_LEDS_COUNT[minuteIndex];
  
  for (int i = 0; i < numLEDs; i++) {
    if (minuteLEDs[i] < NUM_LEDS_LINE3) {
      strip3.setPixelColor(minuteLEDs[i], color);
    }
  }
  strip3.show();
}

void updateLEDs(DateTime adjustedTime, uint32_t color) {
  updateHourLEDs(adjustedTime.hour(), color);
  updateMinuteLEDs(adjustedTime.minute(), color);
  updateColor(color, adjustedTime);

  strip1.show();
  strip2.show();
  strip3.show();
  strip4.show();
}


void updateColorState(uint8_t r, uint8_t g, uint8_t b) {
  currentColorR = r;
  currentColorG = g;
  currentColorB = b;
}
void startAutoBrightness() {
    autoBrightness = true;
}

void stopAutoBrightness() {
    autoBrightness = false; 
}
void colorSwirl() {
  static uint8_t colorIndex = 0;
  uint32_t currentColor, nextColor;

  // Get current color and next color in the sequence
  currentColor = colors[colorIndex];
  colorIndex = (colorIndex + 1) % numColors;
  nextColor = colors[colorIndex];

  // Transition from currentColor to nextColor
  for (int i = 0; i < strip1.numPixels(); i++) {
    uint8_t r1 = (currentColor >> 16) & 0xFF;
    uint8_t g1 = (currentColor >> 8) & 0xFF;
    uint8_t b1 = currentColor & 0xFF;

    uint8_t r2 = (nextColor >> 16) & 0xFF;
    uint8_t g2 = (nextColor >> 8) & 0xFF;
    uint8_t b2 = nextColor & 0xFF;

    // Calculate intermediate colors
    uint8_t r = map(i, 0, strip1.numPixels() - 1, r1, r2);
    uint8_t g = map(i, 0, strip1.numPixels() - 1, g1, g2);
    uint8_t b = map(i, 0, strip1.numPixels() - 1, b1, b2);

    // Set color to NeoPixel strips
    strip1.setPixelColor(i, r, g, b);
    strip2.setPixelColor(i, r, g, b);
    strip3.setPixelColor(i, r, g, b);
    strip4.setPixelColor(i, r, g, b);
  }

  // Show the updated colors on the NeoPixel strips
  strip1.show();
  strip2.show();
  strip3.show();
  strip4.show();

  // Delay between color transitions (adjust as needed)
  delay(50);
}
