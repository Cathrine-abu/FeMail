#include <gtest/gtest.h>
#include "../blacklist/index_handler.h"
#include <vector>
#include <string>
#include <random>
#include <pthread.h>

// Test indexes: Check if corresponding indexes changed
TEST(IndexTest, ChangedIndexes) {
    std::vector<bool> bitArray(8, false);
    std::vector<size_t> indexes;

    // Random number generator
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 8 - 1);

    // Add random number to indexes
    for (size_t i = 0; i < 4; ++i) {
        indexes.push_back(dis(gen));
    }
    // Lock
    pthread_mutex_t bitArrayLock;
    if (pthread_mutex_init(&bitArrayLock, NULL) != 0) {
        std::cerr << "Mutex init failed\n";
    }
    changeAndCheckIndex(bitArray, indexes, bitArrayLock);
    pthread_mutex_destroy(&bitArrayLock);

    // Check if corresponding indexes are 1
    for (size_t idx : indexes) {
        EXPECT_TRUE(bitArray[idx]);
    }
}

// Test indexes: Check indexes are in correct range
TEST(IndexTest, moduloIndexes) {
    int max_size = 8;
    std::vector<bool> bitArray(max_size, false);
    std::vector<size_t> indexes;
    EXPECT_EQ(bitArray.size(), max_size);

    // Random number generator
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, 1000 - 1);

    // Add random number to indexes
    for (size_t i = 0; i < 30; ++i) {
        size_t index = dis(gen) % bitArray.size();
        indexes.push_back(index);
    }

    // Check if all indexes are in range [0, 7]
    for (size_t i = 0; i < indexes.size(); ++i) {
        ASSERT_TRUE(indexes[i] >= 0 && indexes[i] < max_size);
    }
}
