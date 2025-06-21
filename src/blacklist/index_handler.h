#ifndef INDEX_HANDLER_H
#define INDEX_HANDLER_H

#include "../Debug_log.h"
#include <iostream>
#include <vector>
#include <string>
#include <functional> // for std::hash
#include <cstdarg> // for va_start, va_end
#include <pthread.h>

bool changeAndCheckIndex(std::vector<bool>& bitArray, std::vector<size_t> indexes, pthread_mutex_t bitArrayLock);

#endif