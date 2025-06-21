cmake_minimum_required(VERSION 3.10)
project(FeMailTests)

set(CMAKE_CXX_STANDARD 17)

include(FetchContent)

# Download GoogleTest
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/release-1.11.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# Include the parent src directory for header files
include_directories(..)

# Build a test binary with all test files
add_executable(runTests
  test_input_handler.cpp
  ../blacklist/input_handler.cpp
  test_indexes.cpp
  ../blacklist/index_handler.cpp
  test_url.cpp
  ../blacklist/UrlHandler.cpp
  test_file_handler.cpp
  ../blacklist/FileHandling.cpp
  ../blacklist/App.cpp
  ../blacklist/AppSettings.cpp
  ../blacklist/Gui.cpp
  ../blacklist/Delete.cpp
  test_server.cpp
)

# Link GoogleTest
target_link_libraries(runTests gtest_main)

# Register tests
include(GoogleTest)
gtest_discover_tests(runTests)
