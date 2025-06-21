#include "UrlHandler.h"
#include "index_handler.h"
#include "Gui.h"
#include "../Debug_log.h"
#include <iostream>
#include <vector>
#include <string>
#include <functional> // for std::hash
#include <cstdarg> // for va_start, va_end
#include "FileHandling.h"
#include "App.h"
#include <sys/socket.h>  // for send()
#include <pthread.h>

// Default destructor
UrlHandler::~UrlHandler() = default;

// Executes the URL check using the current App state (AppSettings, FileHandling, GUI)
std::string UrlHandler::execute(App& app, std::string url) {
    AppSettings& settings = app.get_settings();          // Get settings reference from App
    FileHandling& fileHandler = app.get_file_handler();  // Get shared FileHandler
    Gui& gui = app.get_gui();                            // Get GUI reference
    int client_fd = settings.get_client_socket_fd();

    // Task 2: Check if the URL is in the Bloom Filter
    bool answer = checkUrl(settings.get_bit_array(), url, settings.get_hash_function_codes(), settings.bitArrayLock);

    if (answer) {
        bool isFalsePositive = fileHandler.checkFalsePositive(url);
        return "200 OK\n\ntrue " + std::string(isFalsePositive ? "false" : "true");
    } else {
        return "200 OK\n\nfalse";
    }
}



// Applies a hash function multiple times to the input string
size_t UrlHandler::repeatedHash(const std::string& input, int times) {
    std::hash<std::string> hasher;
    std::string toHash = input;
    size_t hashValue = hasher(toHash);
    for (int i = 1; i < times; ++i) {
        hashValue = hasher(std::to_string(hashValue));
    }
    return hashValue;
}

// Checks if a URL is in the Bloom filter and updates the filter accordingly
bool UrlHandler::checkUrl(std::vector<bool>& bitArray, const std::string& url, std::vector<int> hash_function, pthread_mutex_t bitArrayLock) {
    std::vector<size_t> indexes;

    // For each hash function   
    for (int hashDepth : hash_function) {
        size_t hashValue = repeatedHash(url, hashDepth);
        size_t index = hashValue % bitArray.size();
        indexes.push_back(index);
    }

    return changeAndCheckIndex(bitArray, indexes, bitArrayLock);
}