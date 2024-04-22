### Compile and Clean Code
To clean the directory and remove all logs and .class files, run `make clean`. To compile all classes and produce the .class files, run `make compile`.  

### Run Dictionary Loader Script
To run the dicionary loader script, after compiling all classes, run `./run_dictionary.sh` to run the dicionary loader script. This should output all nodes' finger tables as well as the words that got inserted into each node. All logging information for each node is named as `node_{nodeID}.log`.  

### Run Client Script
To run the client script, after compiling all classes, run `./run_client.sh` to run the client script. The client script will prompt you with three choices (insert, lookup, or exit). After completion, the script will output the words in each node. 

### Status
All functionalities should be working as intended according to the assignment 7 requirements.