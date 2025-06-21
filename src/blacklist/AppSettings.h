#pragma once
#include <string>
#include <vector>
#include <pthread.h>

// AppSettings class stores and manages application-wide settings
class AppSettings {
public:
    // Constructor - initializes default settings
    AppSettings();
    // Destructor
    ~AppSettings();

    // Getter and setter for the Bloom filter bit array
    std::vector<bool>& get_bit_array();
    void set_bit_array(std::vector<bool>& array);

    // Getter and setter for the size of the Bloom filter bit array
    int get_bit_array_size() const;
    void set_bit_array_size(int size);

    // Getter and setter for the hash function depths used in the Bloom filter
    std::vector<int> get_hash_function_codes() const;
    void set_hash_function_codes(const std::vector<int>& codes);

    // Add a single hash function code to the list
    void add_hash_function_codes(int code);

    // Setter and getter for the client's socket file descriptor
    void set_client_socket_fd(int fd);
    int get_client_socket_fd() const;

    mutable pthread_mutex_t bitArrayLock; // POSIX mutex for thread safety

private:
    std::vector<bool> bitArray;          // The Bloom filter bit array
    int bit_array_size;                  // Size of the Bloom filter
    std::vector<int> hash_function_codes; // List of hash depths for Bloom filter
    int client_socket_fd;                // File descriptor of the connected client
};
