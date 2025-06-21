#include <gtest/gtest.h>
#include "../blacklist/FileHandling.h"
#include <fstream>
#include <cstdio>
#include <filesystem>
#include "../blacklist/App.h"


// Test: constructor compiles and doesn't crash
TEST(FileHandlingUnitTest, ConstructorCompiles) {
    FileHandling fh;
    SUCCEED(); // No crash = pass
}

// Test: ensureFileExists should create file
TEST(FileHandlingUnitTest, EnsureFileExistsCreatesFile) {
    std::string expectedFile = "data/urls.txt";
    std::remove(expectedFile.c_str());  // Ensure it's clean

    FileHandling fh; // Uses default filename
    fh.ensureFileExists();

    std::ifstream file(expectedFile);
    ASSERT_TRUE(file.good());  // Should exist now
    file.close();

    std::remove(expectedFile.c_str());  // Clean up
}


// Test: addUrl should add to real blacklist and file
TEST(FileHandlingUnitTest, AddUrlAddsCorrectly) {
    std::string testFile = "data/urls.txt";
    std::filesystem::create_directories("data");
    std::remove(testFile.c_str());

    FileHandling fh;
    std::vector<bool> bitArray(10, false);
    std::vector<int> hashFuncs = {1, 2};

    fh.addUrl("www.google.com", bitArray, hashFuncs);

    ASSERT_FALSE(fh.checkFalsePositive("www.google.com"));

    std::remove(testFile.c_str());
}

// Test: loadUrls loads all lines to real_blacklist and Bloom filter
TEST(FileHandlingUnitTest, LoadUrlsIntoFilterAndBlacklist) {
    std::string testFile = "data/urls.txt";
    std::filesystem::create_directories("data");
    std::ofstream file(testFile);
    file << "www.test1.com\nwww.test2.com\n";
    file.close();

    FileHandling fh;
    std::vector<bool> bitArray(10, false);
    std::vector<int> hashFuncs = {1, 2};

    fh.loadUrls(bitArray, hashFuncs);

    ASSERT_FALSE(fh.checkFalsePositive("www.test1.com"));
    ASSERT_FALSE(fh.checkFalsePositive("www.test2.com"));

    std::remove(testFile.c_str());
}

// Test: unknown URL should return true (false positive)
TEST(FileHandlingUnitTest, DetectsFalsePositive) {
    std::string testFile = "data/urls.txt";
    std::filesystem::create_directories("data");
    std::ofstream file(testFile);
    file << "www.known.com\n";
    file.close();

    FileHandling fh;
    std::vector<bool> bitArray(10, false);
    std::vector<int> hashFuncs = {1, 2};
    fh.loadUrls(bitArray, hashFuncs);

    ASSERT_TRUE(fh.checkFalsePositive("www.unknown.com"));
    std::remove(testFile.c_str());
}

// Test: execute should add URL when task is 1
TEST(FileHandlingUnitTest, ExecuteTask1AddsUrl) {
    std::map<std::string, ICommand*> commands;
    FileHandling* fh = new FileHandling();
    commands["1"] = fh;

    App app(commands);
    AppSettings& settings = app.get_settings();
    settings.set_bit_array_size(10);
    settings.set_hash_function_codes({1, 2});
    std::vector<bool> bitArray(10, false);
    settings.set_bit_array(bitArray);

    fh->execute(app, "www.example.com");

    FileHandling& fileHandler = app.get_file_handler();
    ASSERT_FALSE(fileHandler.checkFalsePositive("www.example.com"));

    delete fh;
}