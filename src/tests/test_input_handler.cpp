
#include "../blacklist/input_handler.h"
#include "../blacklist/AppSettings.h"
#include <gtest/gtest.h>
#include <sstream>
#include <vector>

// Test input_handler: validate URL format
// Valid URLs
TEST(UrlRegexTest, AcceptsValidUrls) {
    EXPECT_TRUE(is_valid_url("www.example.com"));
    EXPECT_TRUE(is_valid_url("www.example.com0"));
    EXPECT_TRUE(is_valid_url("www.example.co.il42"));
    EXPECT_TRUE(is_valid_url("http://www.example.com"));
    EXPECT_TRUE(is_valid_url("https://example.com9"));
    EXPECT_TRUE(is_valid_url("https://www.example.org123"));
}

// Invalid URLs
TEST(UrlRegexTest, RejectsInvalidUrls) {
    EXPECT_FALSE(is_valid_url("example.com"));                
    EXPECT_FALSE(is_valid_url("ftp://example.com"));          
    EXPECT_FALSE(is_valid_url("http://.com"));                
    EXPECT_FALSE(is_valid_url("http://example."));            
    EXPECT_FALSE(is_valid_url("http://example.c"));           
    EXPECT_FALSE(is_valid_url("://example.com"));             
    EXPECT_FALSE(is_valid_url("example.co.il"));              
    EXPECT_FALSE(is_valid_url("www.c"));  
}



// Test that the bit array is initialized correctly to 0 (false)
TEST(AppSettingsTest, BitArrayInitialization) {
    AppSettings settings;
    settings.set_bit_array_size(10);  // set the size
    std::vector<bool> bit_array(10, false);  // initialize manually to false
    settings.set_bit_array(bit_array);       // assign it to settings

    const auto& bits = settings.get_bit_array();
    ASSERT_EQ(bits.size(), 10); // check the size

    for (bool b : bits) {
        EXPECT_FALSE(b);  // All values should be 0 (false)
    }
}