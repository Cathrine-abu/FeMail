#include <iostream>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <map>
#include <sstream>
#include <thread>
#include "ICommand.h"
#include "UrlHandler.h"
#include "FileHandling.h"
#include "Delete.h"

const int MESSAGE_BYTES = 4096;

using namespace std;

int create_socket(int server_port) {
    // Creates a TCP socket using IPv4 and get the socket 'id' to sock
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("error creating socket");
    }

    // Initializes the sockaddr_in structure
    struct sockaddr_in sin;
    memset(&sin, 0, sizeof(sin));
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = INADDR_ANY;
    sin.sin_port = htons(server_port);

    // Binds the socket so it can receive messages
    if (bind(sock, (struct sockaddr *) &sin, sizeof(sin)) < 0) {
        perror("error binding socket");
    }

    // Puts the socket into listening mode
    if (listen(sock, 5) < 0) {
        perror("error listening to a socket");
    }

    return sock;
}

int connect_client(int sock) {
    // Wait for a client to connect
    struct sockaddr_in client_sin;
    unsigned int addr_len = sizeof(client_sin);
    int client_sock = accept(sock,  (struct sockaddr *) &client_sin,  &addr_len);
    if (client_sock < 0) {
        perror("error accepting client");
    }
    return client_sock;
}

int receive_message(int client_sock, char *buffer, int buffer_size) {
    // Receives a message from the client
    int read_bytes = recv(client_sock, buffer, buffer_size, 0);
    if (read_bytes <= 0) {
        // Client disconnected / error
        if (read_bytes < 0) {
            perror("error receiving from client");
        }
        return -1;
    }
    buffer[read_bytes] = '\0';  // null-terminate for safety
    return read_bytes;
}

void handle_client(int client_sock, App& app) {
    // Get message from the client
    char buffer[MESSAGE_BYTES];
    int bytes_received = receive_message(client_sock, buffer, sizeof(buffer) - 1);
    if (bytes_received <= 0) {
        close(client_sock);
    };
    string response = app.run_line(buffer);

    // Sends a response to the client
    int sent_bytes = send(client_sock, response.c_str(), response.size(), 0);
    if (sent_bytes < 0) {
        close(client_sock);
    }
    // Close client socket
    close(client_sock);
}

int main(int argc, char *argv[]) {
    // Get port from the user
    if (argc < 2) {
        return 1;
    }
    int server_port = atoi(argv[1]);
    int sock = create_socket(server_port);
    
    // Create commands map
    map<string, ICommand*> commands;
    ICommand* urlHandler = new UrlHandler();
    ICommand* fileHandler = new FileHandling();
    ICommand* deleteCCommand = new Delete();
    commands["POST"] = fileHandler;
    commands["GET"] = urlHandler;
    commands["DELETE"] = deleteCCommand;


    // Initialize client program params
    std::string initial_params = std::to_string(atoi(argv[2])) + " " + argv[3];
    App app(commands);
    if (!app.Initializations(initial_params)) {
        return 1;
    }

    // Accept multiple clients
    while (true) {
        int client_sock = connect_client(sock);
        if (client_sock < 0) continue;
        thread client_thread(handle_client, client_sock, std::ref(app));
        client_thread.detach(); // Run independently
    }

    // Ends the program
    return 0;
}