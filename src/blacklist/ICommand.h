#pragma once
#include <iostream>
#include "../Debug_log.h"
#include "AppSettings.h"
#include "Gui.h"
class App;

/**
 * ICommand is an abstract base class (interface) for all command types.
 * It follows the Command Design Pattern.
 * Any class that inherits from ICommand must implement the execute method.
 */
class ICommand {
public:
/**
     * Executes a command using the application context.
     * @param app: Reference to the main App object, giving access to settings, GUI, and file handling.
     */
virtual std::string execute(App& app, std::string url) = 0;
virtual ~ICommand() = default;


};