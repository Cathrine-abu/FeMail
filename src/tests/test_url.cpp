#include <gtest/gtest.h>
#include "../blacklist/UrlHandler.h"
#include <vector>
#include <string>
#include <random>

// Test URLs: First URL returns always false
TEST(UrlTest, FirstUrlFalse) {
    std::vector<bool> bitArray(8, false);
    UrlHandler handler;
    // Lock
    pthread_mutex_t bitArrayLock;
    if (pthread_mutex_init(&bitArrayLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
    bool firstCall = handler.checkUrl(bitArray, "www.example.com0", {1, 2}, bitArrayLock);
    pthread_mutex_destroy(&bitArrayLock);
    EXPECT_FALSE(firstCall);
}

// Test URLs: Same URL one after another returns always true
TEST(UrlTest, SameUrlTrue) {
    std::vector<bool> bitArray(8, false);
    UrlHandler handler;
    // Lock
    pthread_mutex_t bitArrayLock;
    if (pthread_mutex_init(&bitArrayLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
    bool firstCall = handler.checkUrl(bitArray, "www.example.com0", {1, 2}, bitArrayLock);
    bool secondCall = handler.checkUrl(bitArray, "www.example.com0", {1, 2}, bitArrayLock);
    pthread_mutex_destroy(&bitArrayLock);
    EXPECT_TRUE(secondCall);
}

// Test URLs: Same indexes few times returns true
TEST(UrlTest, SameIndexTwiceTrue) {
    std::vector<bool> bitArray(8, false);
    UrlHandler handler;
    // Lock
    pthread_mutex_t bitArrayLock;
    if (pthread_mutex_init(&bitArrayLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
    bool firstCall = handler.checkUrl(bitArray, "www.example.com0", {1, 2}, bitArrayLock);
    bool sameIndexCall = handler.checkUrl(bitArray, "www.example.com0", {1, 1}, bitArrayLock);
    pthread_mutex_destroy(&bitArrayLock);
    EXPECT_TRUE(sameIndexCall);
}
