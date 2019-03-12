# RA_DistributedMutualExclusion
Implementation of Ricart-Agrawala algorithm for distributed mutual exclusion, with optimization proposed by Roucairol and Carvalho, in a client-server mode.

Java programming language. Thread and/or socket programming and its APIs.

* Three servers in the system, numbered from zero to two.
* Five clients in the system, numbered from zero to four.
* File is replicated on all the server.
* A client can perform a READ or WRITE.
*  READ/WRITE on a file can be performed by only one client at a time. Different clients are allowedto concurrently perform a READ/WRITE on different files.
* Implementation of Ricart-Agrawala algorithm for distributed mu-tual exclusion, with the optimization proposed by Roucairol and Carvalho, so that no READ/WRITE violation could occur. The operations on files can be seen as critical section executions.
* A server supports ENQUIRY, READ, WRITE.
* ENQUIRY: A request from a client for information about the list of hosted files.
* READ: A request to read last line from a given file.
* WRITE: string <clientid, timestamp>. Timestamp is the value of the clients, local clock when the WRITE request is generated
* Assumption 1 : Set of file does not change during the program’s execution.
* Assumption 2 : No server failureoccurs during the execution of the program.
