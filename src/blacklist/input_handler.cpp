#include "input_handler.h"
#include "../Debug_log.h"
#include "AppSettings.h"
#include <vector>
#include <string>
#include <sstream>
#include <regex>
#include <iostream>
#include <cctype>

// Global bit array representing the bloom filter
static std::vector<bool> bit_array;

// Returns true if the string is a positive integer
static bool is_integer(const std::string& s) {
    return !s.empty() && std::all_of(s.begin(), s.end(), ::isdigit);
}

// Validates a URL using regular expression
bool is_valid_url(const std::string& url) {
    const std::regex url_regex(R"(^(https?:\/\/(www\.)?|www\.)[a-zA-Z0-9\-]+(\.[a-zA-Z]{2,})+[0-9]*$)");
    return std::regex_match(url, url_regex);
    
}

// Reads and validates the bloom filter configuration
bool read_bloom_filter_config(AppSettings & settings, std::string line) {

    std::istringstream iss(line);
    std::string token;
    std::vector<std::string> tokens;
    // The input should be at least 2 integers
    while (iss >> token) tokens.push_back(token);
    if (tokens.size() < 2) return false;

    bool valid = true;
    for (const std::string& t : tokens) {
        if (!is_integer(t)) {
            valid = false;
            break;
        }
    }

    if (!valid) return false;

    settings.set_bit_array_size(std::stoi(tokens[0]));
    for (size_t i = 1; i < tokens.size(); ++i)
        settings.add_hash_function_codes(std::stoi(tokens[i]));
    return true;
}

// Initializes the bit array with all 0 (false) values
void initialize_bit_array(AppSettings & settings) {
    bit_array = std::vector<bool>(settings.get_bit_array_size(), false);
    settings.set_bit_array(bit_array);
    DEBUG_LOG("[DEBUG] Bit array initialized to size: " << settings.get_bit_array_size());
}

// Returns the current state of the bit array
std::vector<bool>& get_bit_array() {
    return bit_array;
}
