#pragma once
#include "ICommand.h"
#include <string>
#include <unordered_set>
#include <pthread.h>
class App;

// FileHandling class manages reading/writing URLs with Bloom Filter integration
class FileHandling : public ICommand {
public:
    // Constructor
    FileHandling();
    // Destructor
    ~FileHandling();

    // Command interface implementation
    std::string execute(App& app, std::string url) override;

    // Creates the file if it doesn't exist (does not modify class data)
    void ensureFileExists() const;

    // Reads URLs from the file and adds them to the filter and blacklist
    // This modifies the internal state -> NOT const
    void loadUrls(std::vector<bool>& bitArray, const std::vector<int>& hash_functions);

    // Adds a single URL to the Bloom Filter and the real blacklist
    // This modifies internal data -> NOT const
    void addUrl(const std::string& url, std::vector<bool>& bitArray, const std::vector<int>& hash_functions, bool writeToFile = true);

    // Checks if the given URL is a false positive (in filter but not in list)
    bool checkFalsePositive(const std::string& url) const;

    // Removes the specified URL from the real blacklist data structure without modifying the Bloom filter
    std::string Delete(const std::string& url, bool writeToFile);

    bool existsInBlacklist(const std::string& url) const;

private:
    std::string filename;  // path to the file we're handling

    // Internal Bloom Filter represented as a bit vector
    std::vector<bool> bloom_filter;

    // URLs that were actually added (for checking false positives) using set to avoid duplicates
    std::unordered_set<std::string> real_blacklist;

    // Which hash functions to use (e.g., {1, 2})
    std::vector<int> hash_functions;

    // Size of the bloom filter array
    size_t filter_size;

    // Flag indicating whether the blacklist should be written to file after changes
    bool writeToFile = true;

    // POSIX mutex for thread safety
    mutable pthread_mutex_t urlsLock;
};
