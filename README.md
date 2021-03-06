# T-Hawk Master Scouting
The T-Hawk Master Scouting app is a native Android app that works in conjunction with the T-Hawk Scouting app to track the preformance of FRC (FIRST robotics competition) robots during a match. This app scans QR codes generated by the T-Hawk Scouting app and uploads them to a specificed AWS DynamoDB table in a mutli-level hierarchy. The app also supports an offline mode: if the device does not have a secure connection to AWS, the app will write the data in each QR code to a JSON file created in a directory located in the device's internal storage. 

## Installation
>The app is native Android

Download [Android Studio](https://www.google.com/search?client=safari&rls=en&q=android+studio&ie=UTF-8&oe=UTF-8) to access the source code
* Download the source code
* Open the source code in Android Studio
* Navigate to Build -> Build Bundle(s) / APK (s) -> Build APK(s)
* Open the APK file on a native Android device to download the app

## Configuration
* Change `ID` in gradle.properties and `PoolId:` in awsconfiguration.json to a Cognito User Pool ID 
* Change both instances of `Region:` in awsconfiguration.json and `REGION` in gradle.properties to the region (ex: us-west-1)
* Change `TABLE_NAME` in gradle.properties to the DynamoDB table name

## Features
* Ability to scan and read data from 6 QR codes
* Can add and remove specific QR codes
* Pushes data to AWS DynamoDB if Internet connection exists
* Writes data to directory in interal storage in JSON format
