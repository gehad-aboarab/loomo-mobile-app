# Loomo Mobile Application
This system aims to achieve semi-autonomous robot movements by using BLE beacons to localize the user, and visual odometry, using a grid-based map, to localize the robot within its surroundings. A user within the indoor environment can request the robot’s assistance, whose location would be determined by collecting signals received from the surrounding beacons. Upon construction of the grid-based map, the robot in turn pinpoints and localizes the user on the map and navigates towards their location. The user can further request guidance to certain destinations defined within the map, and the robot similarly builds a route to that destination and navigate through it with additional obstacle detection and avoidance techniques.

The application running on the Loomo is an Android application that handles the robot's decisions such as routing, navigation, speech, recognition and constructing the array that contains the map. This application communicates with a server built using Node.js that handles communication between the sub-systems. All communications is carried out over Message Queuing Telemetry Transport (MQTT) protocol. The server also communicates with an Android mobile application that allows the user to request assistance. Additionally, a Raspberry Pi is used to connect external ultrasonic sensors to the robot to detect more obstacles along its route. The Raspberry Pi also communicates with the server and the server sends the instructions to the robot accordingly.

## Important
This application is only meaningful when working with Segway's robot Loomo. 
It is important to note that this application works hand in hand with other sister applications. This project was customized to work in a specific indoor environment, therefore the local database created contains map dimensions of that environment only.

For the system to work within the specified environment:
1. The server (https://github.com/hussu97/loomo-mqtt-publisher) must be running.
2. An MQTT service must be running and the application code as well as the Loomo application code must be modified accordingly to allow communication with the MQTT broker.
3. The Loomo application (https://github.com/gehad-aboarab/loomo-robot-app) must be installed on the robot.
4. This application must be installed on an Android device.
5. Loomo must be at the home station (coordinates specified in the database).

Documentation of the whole system is found at: https://drive.google.com/drive/folders/1TvFIsy3bOis4yCOtvILN86UbV3eCbNdx
