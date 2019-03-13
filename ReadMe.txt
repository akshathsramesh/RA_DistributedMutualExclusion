  ___    ___    __     ____  ____    ___      __  ____   ___ ______
 // \\  // \\  (( \    || \\ || \\  // \\     || ||     //   | || |
 ||=|| ((   ))  \\     ||_// ||_// ((   ))    || ||==  ((      ||
 || ||  \\_//  \_))    ||    || \\  \\_//  |__|| ||___  \\__   ||


By: Akshath Salekoppal Ramesh - asr150330
----------------------------------------------
	CONFIG FILE
----------------------------------------------

1. ClientAddressAndPorts.txt
2. ServerAddressAndPorts.txt
3. severWorkFolder.txt


----------------------------------------------------
	CONFIG FILE CONTENT - Respective File Number
----------------------------------------------------

1. Contains the ClientID, Client IP and Client Port
- Used for setting up mesh connection among clients.
- 0 Connects with 1,2,3,4; 1 connects with 2,3,4; So on.
- It also defines; given Client ID which port it will run on.

2. Contains ServerID, Server IP and Server Port
- Used by client to create the connection.
- It also defines; given server ID which port it will run on.

3. Contains ServerID and thier workFolder directory.

----------------------------------------------
	Compiling And Running Code
----------------------------------------------

Navigate to src folder of project and use javac *.java command to build/compile the classes.

1. SSH into all machines mentioned in ClientAddressAndPorts.txt and ServerAddressAndPorts.txt.
2. Make sure all workfolders are available on all machines. Mapping and ServerID as defines in serverWorkFolder.txt
3. Get all the clients and servers running.
	Usage Client: java Client <ClientID>
	Usage Server: java Server <ServerID>
4. Once all clients and Servers are up and running.
	a. In client machines terminal : In increasing order of thier client Id - Use Command: SETUP
5. Once all clients are connected.
	a. On each client terminal: use command : SERVER_SETUP - this connects to all servers mentioned on ServerAddressAndPorts.txt.

6. On Client machine terminal where you want to generate request: Use Command: AUTO_REQUEST to generate Read/Write request.

---------------------------------------------------
	Supplemental Command
---------------------------------------------------

1. Client
	- SERVER_SETUP_TEST - Send's WRITE test request to all the server client node is connected to.
	- CONNECTION_DETAIL - will show all the clients the current client is connected. Socket and port information.
	- SHOW_FILES - Result of ENQUIRE command sent to server

--------------------------------------------------
	Working Mandate Requirement
--------------------------------------------------
1. Files should be consistent in all server work folders.
2. If n = number of server nodes; then serverWorkFolder will have n entries. All server work folders are under src of project. Each server folder will have all the files.
3. Files hosted should have single character as thier name. Implemented logic uses hosted files string to pick randon file. If folder as a.txt and b.txt; then hosted server files "ab". a or b is chosen randomly.
4. Order in which the command are executed does matter to correct functioning of system.


