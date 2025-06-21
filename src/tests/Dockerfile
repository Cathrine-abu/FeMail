# Use the official GCC image as the base environment
FROM gcc:latest

# Install CMake for building with CMakeLists.txt
RUN apt-get update && apt-get install -y cmake

# Copy the entire project into the container
COPY . /usr/src/mytest

# Navigate to the tests directory and create a build folder
WORKDIR /usr/src/mytest/tests
RUN mkdir build

# Move into the build directory
WORKDIR /usr/src/mytest/tests/build

# Configure and compile the tests using CMake
RUN cmake .. && make

# Compile the server as a standalone binary (used during integration tests)
RUN g++ -std=c++17 -o server \
    ../../blacklist/Server.cpp \
    ../../blacklist/App.cpp \
    ../../blacklist/AppSettings.cpp \
    ../../blacklist/Gui.cpp \
    ../../blacklist/FileHandling.cpp \
    ../../blacklist/UrlHandler.cpp \
    ../../blacklist/Delete.cpp \
    ../../blacklist/index_handler.cpp \
    ../../blacklist/input_handler.cpp \
    -lpthread

# Default command to run the test suite when the container starts
CMD ["./runTests"]
