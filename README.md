<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Storda

Welcome to the Storda app. This is a simple CorDapp build using Kotlin - Spring Boot - ReactJS - AntDesign UI.

# Tech

This CorDapp uses:

* Corda
* Kotlin
* Spring MVC
* NodeJS
* React
* AntDesign

# Usage

## Running the nodes

In terminal in the main project folder run the following command

    ./gradlew deployNodes

After all the nodes have been deployed start them all with following commands

     ...
    cd build/nodes/PartyA
    java -jar corda.jar
    
    ...
    cd build/nodes/PartyB
    java -jar corda.jar
    
    ...
    cd build/nodes/Notary
    java -jar corda.jar

When started properly, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

### Client

`clients/src/main/kotlin/com/template/Client.kt` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

#### Running the webserver

##### Via the command line

Run the `runServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`.

### Running the front-end

In terminal from the main folder of your project run the following commands:
     
     ...
     cd front-end/app
     npm install
     npm start

#### Interacting with the webserver

The app webpage is served on:

    http://localhost:10030
    