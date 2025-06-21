#include "input_handler.h"
#include "ICommand.h"
#include "AppSettings.h"
#include "../Debug_log.h"
#include "App.h"
#include "Gui.h"
#include "FileHandling.h"
#include <iostream>
#include <string>
#include <memory>
#include <map>
#include <sstream>
#include <stdexcept>
#include "UrlHandler.h"

// Constructor
App::App(std::map<std::string, ICommand*>& commands)
    : commands(std::move(commands)), fileHandler() {}

bool App::Initializations(std::string line) {
    // Load settings and GUI configurations
    settings = get_settings();   // store in member variable
    gui = get_gui();             // store in member variable

    // Initialize Bloom filter from settings
    if (!read_bloom_filter_config(settings, line)) {
        return false;
    }
    try {
        initialize_bit_array(settings);
    } catch (...) {
        return false;
    }

    // Initialize file storage (once)
    fileHandler.ensureFileExists();
    fileHandler.loadUrls(settings.get_bit_array(), settings.get_hash_function_codes());

    DEBUG_LOG("[DEBUG] Initialized App");
    return true;
}

// Handle user line command
std::string App::run_line(std::string line) {
    std::string command;
    if (!line.empty()) {
        try {
            std::istringstream iss(line);
            std::string url;

            if (!(iss >> command >> url)) return ("400 Bad format");
            if (!is_valid_url(url)) return ("400 Invalid url");

            // Look up and execute the corresponding command
            auto it = commands.find(command);
            if (it != commands.end() && it->second) {
                return it->second->execute(*this, url);
            }
        } catch (const std::exception& e) {
            return "500 Internal Server Error";
        }
    }
    return "400 Bad Request";
}

// Getter for AppSettings instance
AppSettings& App::get_settings() {
    return settings;
}

// Getter for GUI instance
Gui& App::get_gui() {
    return gui;
}

// Getter for FileHandling instance
FileHandling& App::get_file_handler() {
    return fileHandler;
}
