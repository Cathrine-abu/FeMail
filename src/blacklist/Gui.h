#pragma once
#include "AppSettings.h"
#include <iostream>
#include <ostream>
#include <string>
#include <vector>

// Gui class provides an interface to display the app
class Gui {
public:
    // Constructor
    Gui();

    // Displays the gotten output message in the gotten output stream
    void displayOutput(std::ostream& out, std::string message) const;
};
