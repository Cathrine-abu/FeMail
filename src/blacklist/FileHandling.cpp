#include "FileHandling.h"
#include "UrlHandler.h"
#include <fstream>
#include <filesystem>
#include <iostream>
#include "../Debug_log.h"
#include "App.h"
#include <sys/socket.h>  // for send()
#include <pthread.h>


// Default constructor – sets a default filename
FileHandling::FileHandling() : filename("data/urls.txt") {
    // Init lock
    if (pthread_mutex_init(&urlsLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
}

// Destructor
FileHandling::~FileHandling() {
    pthread_mutex_destroy(&urlsLock);
}
// Executes based on current App state (used for task 1)
std::string FileHandling::execute(App& app, std::string url) {
    AppSettings& settings = app.get_settings();           // Access settings
    FileHandling& fileHandler = app.get_file_handler();   // Access file handler
    int client_fd = settings.get_client_socket_fd();

    // Task 1: Add URL to blacklist
    fileHandler.addUrl(url, settings.get_bit_array(), settings.get_hash_function_codes(), true);
    // Change the corresponding bits
    UrlHandler handler;
    handler.checkUrl(settings.get_bit_array(), url, settings.get_hash_function_codes(), settings.bitArrayLock);

    return "201 Created";

}

// Creates the file if it does not already exist
void FileHandling::ensureFileExists() const {
    // Create the directory if it doesn't exist (e.g., "data")
    std::filesystem::create_directories("data");

    // Only create the file if it doesn't exist already
    if (!std::filesystem::exists(filename)) {
        std::ofstream file(filename);  // Will create empty file
        if (file.is_open()) {
            file.close();
            DEBUG_LOG("[DEBUG] File created: " << filename);
        }
    } else {
        DEBUG_LOG("[DEBUG] File already exists: " << filename);
    }
}

// Loads URLs from the file and adds them to the Bloom Filter and real blacklist
void FileHandling::loadUrls(std::vector<bool>& bitArray, const std::vector<int>& hash_functions) {
    std::ifstream file(filename);
    std::string url;

    while (std::getline(file, url)) {
        addUrl(url, bitArray, hash_functions, false); 
        real_blacklist.insert(url);
    }

    file.close();
}

// Adds a URL to the Bloom Filter and the real blacklist, and appends it to the file
void FileHandling::addUrl(const std::string& url, std::vector<bool>& bitArray, const std::vector<int>& hash_functions, bool writeToFile) {
    std::hash<std::string> hasher;
    pthread_mutex_lock(&urlsLock); // Acquire lock
    for (int func_id : hash_functions) {
        size_t hash_value = hasher(url);
        size_t index = hash_value;
    
        for (int i = 1; i < func_id; ++i) {
            hash_value = hasher(std::to_string(hash_value));
            index = hash_value;
        }
    
        index = index % bitArray.size();
        bitArray[index] = true;
    }
    
    if (real_blacklist.find(url) == real_blacklist.end()) {
        real_blacklist.insert(url);

    // Add the URL to the real blacklist to track actual entries
    real_blacklist.insert(url);
    // This ensures the URL is saved for future runs of the program.
        if (writeToFile) {
            std::ofstream outFile(filename, std::ios::app);
            if (outFile.is_open()) {
                outFile << url << std::endl;
                outFile.close();
            }
        }
    }
    pthread_mutex_unlock(&urlsLock); // Release lock
}


// Checks if a URL is a false positive (present in filter but NOT in the real blacklist)
bool FileHandling::checkFalsePositive(const std::string& url) const {
    // Look for the URL in the real blacklist
    pthread_mutex_lock(&urlsLock); // Acquire lock
    bool result = real_blacklist.find(url) == real_blacklist.end();
    pthread_mutex_unlock(&urlsLock); // Release lock
    return result;
}

// Removes the specified URL from the real blacklist data structure without modifying the Bloom filter
std::string FileHandling::Delete(const std::string& url, bool writeToFile) {
    std::string response;

    pthread_mutex_lock(&urlsLock); // Acquire lock
    if (real_blacklist.find(url) != real_blacklist.end()) {
        real_blacklist.erase(url);
        response = "204 No Content";
    } else {
        response = "404 Not Found";
    }

    if (writeToFile) {
        std::ofstream outfile(filename, std::ios::trunc); // overwrite file
        for (const auto& u : real_blacklist) {
            outfile << u << std::endl;
        }
    }
    pthread_mutex_unlock(&urlsLock); // Release lock

    return response;
}

// Check if the url exists in the blacklist
bool FileHandling::existsInBlacklist(const std::string& url) const {
    pthread_mutex_lock(&urlsLock); // Acquire lock
    bool result = real_blacklist.find(url) != real_blacklist.end();
    pthread_mutex_unlock(&urlsLock); // Release lock
    return result;
}
