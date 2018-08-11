/*
 * This script should be placed on an Arduino attached to an HM-10
 * The LED turning on/off when Serial.available() > 0 shows that the Android correctly 
 * speaks to the Arduino through the HM-10 BLE-to-Serial chip.
 * The Serial.println, if displayed on the Android, shows that the Android correctly 
 * listens to the Arduino through the HM-10 BLE-to-Serial chip.
 * This is useful for debugging purposes. 
 **/


int count = 0; // I use a counter to ensure every message is unique. 
// Really, it's peace of mind that I am not reading a hard-coded string on Android. 

void setup() 
{ 
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  // I use LED_BUILTIN because it is a lot more noticable than the flicker 
  // of the tx/rx builtin LEDs (at least on Arduino Nano) 

}
  
void loop() 
{
  if (Serial.available()> 0) 
  // To keep this simple, I use Serial.available() > 0 to respond after every byte of incoming data.
  // Serial communication can become complex, fast.
  // The Android should only send over single characters, 0-9, a-z, A-Z.
  // Giving a string, i.e. "Hello World" will increment the counter multiple times. 
  {
//    char a = Serial.read().ToString();
    count++;
    Serial.println("Received: " + ". Counter: " + count + ".");
    if (count % 2 == 1) 
    {
        digitalWrite(LED_BUILTIN, HIGH); // if count = odd (as in first communication), turn builtin LED on
    }
    else
    {
        digitalWrite(LED_BUILTIN, HIGH); // if count = even, turn builtin LED off
    }

  }
}





