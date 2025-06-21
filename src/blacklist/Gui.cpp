#include "Gui.h"
#include <iostream>
#include <fstream>

// Constructor
Gui::Gui() {}

// Displays the gotten output message in the gotten output stream
void Gui::displayOutput(std::ostream& out, std::string message) const {
    out << message << std::endl;
}
