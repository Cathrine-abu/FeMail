#pragma once

#include "ICommand.h"
#include "App.h"
class App;

// Delete command class inherits from ICommand and overrides execute
class Delete : public ICommand {
public:
    // Executes the delete command using the application context
    std::string execute(App& app, std::string url) override;

    // Virtual destructor for proper cleanup in case of inheritance
    virtual ~Delete();
};
