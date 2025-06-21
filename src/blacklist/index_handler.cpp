#include "index_handler.h"
#include "../Debug_log.h"
#include <iostream>
#include <vector>
#include <string>
#include <functional> // for std::hash
#include <cstdarg> // for va_start, va_end
#include <pthread.h>

// Change the array bites according to the given indexes
// Returns true if all given indexes in bitArray are true, otherwise false
bool changeAndCheckIndex(std::vector<bool>& bitArray, std::vector<size_t> indexes, pthread_mutex_t bitArrayLock) {
    bool isInBlackList = true;
    pthread_mutex_lock(&bitArrayLock); // Acquire lock
    for (size_t index : indexes) {
        // Check index
        if (!bitArray[index]) {
            isInBlackList = false;
        }
        // Set index bit to 1
        bitArray[index] = true;
    }
    pthread_mutex_unlock(&bitArrayLock); // Release lock
    return isInBlackList;
}
