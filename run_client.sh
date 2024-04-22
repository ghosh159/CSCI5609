#!/bin/bash

PORT=1099

# Start the RMI registry
rmiregistry $PORT &
echo "RMI registry started"

# Wait for the RMI registry to start
sleep 5

# Start node-0 to initialize the Chord ring
java ChordNode 0 &
echo "Node 0 started"

# Wait for node-0 to start
sleep 5

# Start the other 7 nodes
for ((i=1; i<8; i++)); do
    java ChordNode $i &
    echo "Node $i started"
    sleep 1
done

# Wait for all nodes to start
sleep 5

# Join all the nodes to the Chord ring
java JoinNodes 8
echo "All nodes joined the Chord ring"

# Wait for the nodes to stabilize
sleep 5

# Run the Client
echo "Client..."
java Client //localhost:$PORT/node_0
echo "Client script completed"

# Wait for the test script to finish
sleep 5

#Shutting down nodes
for ((i=0; i<8; i++)); do
    java -classpath . ShutdownNode //localhost:$PORT/node_$i
done

echo "All nodes shutdown"

fuser -k $PORT/tcp