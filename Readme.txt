1. Run FileServer.java in one session of Putty which is connected to 10.234.136.55
2. Run an another instance of Putty and run FileClient.java which gives us to login and exit
3. Choose login with username: saikrishna, Password: saikrishna.
4. Once the connection is established, enter the file name which we want to send.
5. Once the file is transfered it send out creates an copy of the file in the same folder.

Simulating failure:
If FileServer.java in the while loop change the parameter from "N" to "Y" to simulate failure.