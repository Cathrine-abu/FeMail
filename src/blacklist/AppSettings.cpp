#include "AppSettings.h"
#include "../Debug_log.h"
#include <fstream>
#include <iostream>
#include <pthread.h>

// Constructor
AppSettings::AppSettings() {
    // Init lock
    if (pthread_mutex_init(&bitArrayLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
}

// Destructor
AppSettings::~AppSettings() {
    pthread_mutex_destroy(&bitArrayLock);
}

// Getter and Setter for bit array
std::vector<bool>& AppSettings::get_bit_array() {
    return bitArray;
}

void AppSettings::set_bit_array(std::vector<bool>& array) {
    bitArray = array;
}

// Getter and Setter for bit array size
int AppSettings::get_bit_array_size() const {
    return bit_array_size;
}

void AppSettings::set_bit_array_size(int size) {
    bit_array_size = size;
}

// Getter and Setter and Adding for hash functions
std::vector<int> AppSettings::get_hash_function_codes() const {
    return hash_function_codes;
}

void AppSettings::set_hash_function_codes(const std::vector<int>& codes) {
    hash_function_codes = codes;
}

void AppSettings::add_hash_function_codes(int code) {
    hash_function_codes.push_back(code);
}

// Setter for the client socket file descriptor
void AppSettings::set_client_socket_fd(int fd) {
    client_socket_fd = fd;
}

// Getter for the client socket file descriptor
int AppSettings::get_client_socket_fd() const {
    return client_socket_fd;
}
