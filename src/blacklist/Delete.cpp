#include "Delete.h"
#include "App.h"
#include "FileHandling.h"
#include <sys/socket.h>  // For send()
#include <string>

// Default destructor
Delete::~Delete() = default;

// Executes the delete command using the app context
std::string Delete::execute(App& app, std::string url) {
    // Retrieve the current app settings and file handler
    AppSettings& settings = app.get_settings();
    FileHandling& fileHandler = app.get_file_handler();

    // Call the delete function and return the result message
    return fileHandler.Delete(url, true);
}
