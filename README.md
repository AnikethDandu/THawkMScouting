# T-Hawk Master Scouting
Repository dedicated to Team 1100's master scouting app

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
