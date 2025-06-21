#include "ICommand.h"
#include "AppSettings.h"
#include "Gui.h"
#include <iostream>
#include <vector>
#include <string>
#include <functional> // for std::hash
#include <cstdarg> // for va_start, va_end
#include "App.h"
#include <pthread.h>
class App;

// UrlHandler class handles URL checking in a Bloom filter according to hash functions
class UrlHandler : public ICommand {
public:
    // Default destructor
    virtual ~UrlHandler();
    
    // Executes the URL check using the current AppSettings
    std::string execute(App& app, std::string url) override;
    
    // Applies a hash function multiple times to the input string
    size_t repeatedHash(const std::string& input, int times);

    // Checks if a URL is in the Bloom filter and updates the filter accordingly
    bool checkUrl(std::vector<bool>& bitArray, const std::string& url, std::vector<int> hash_function, pthread_mutex_t bitArrayLock);
};
