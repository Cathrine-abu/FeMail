// Defines DEBUG_LOG macro to log debug messages when compiled with -DDEBUG
#pragma once
#ifdef DEBUG
    #define DEBUG_LOG(x) std::cout << x << std::endl
#else
    #define DEBUG_LOG(x)
#endif