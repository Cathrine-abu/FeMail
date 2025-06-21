#pragma once
#include "ICommand.h"
#include "FileHandling.h"
#include "App.h"
#include "Gui.h"
#include "AppSettings.h"
#include <iostream>
#include <string>
#include <memory>
#include <map>

// App class manages a collection of commands and runs the application
class App {
public:
    // Constructor: takes a map of commands
    App(std::map<std::string, ICommand*>& commands);

    // Initialize
    bool Initializations(std::string line);

    // Runs the commands
    std::string run_line(std::string line);

    FileHandling& get_file_handler();

    // Getter
    AppSettings& get_settings();
    Gui& get_gui();

private:
    // Holds the commands (mapping strings to unique command objects)
    std::map<std::string, ICommand*> commands;
    AppSettings settings;
    Gui gui;
    FileHandling fileHandler;
};
