#pragma once
#include "AppSettings.h"
#include <string>
#include <vector>

// Checks if a URL is valid using regex
bool is_valid_url(const std::string& url);

// Reads the bloom filter configuration line from input
bool read_bloom_filter_config(AppSettings& settings, std::string line);

// Initializes the bit array with all bits set to false (0)
void initialize_bit_array(AppSettings& settings);

// Returns the current bit array (for testing/debugging)
std::vector<bool>& get_bit_array();