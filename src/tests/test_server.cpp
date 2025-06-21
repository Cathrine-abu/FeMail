#include <gtest/gtest.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <thread>
#include <chrono>
#include <string>
#include <cstdlib>
#include <csignal>
#include <sys/types.h>
#include <sys/wait.h>
#include <iostream>

pid_t server_pid;

// Launch the server process in the background using fork + exec
void run_server_process() {
    server_pid = fork();
    if (server_pid == 0) {
        // Child process replaces itself with the server executable
        execl("./server", "server", "8081", "8", "1", NULL);
        exit(1); // Only reached if exec fails
    }
    // Give the server time to boot before the tests connect
    std::this_thread::sleep_for(std::chrono::seconds(1));
}

// Integration test class for the server
class ServerIntegrationTest : public ::testing::Test {
protected:
    int sock;

    // Setup runs before each test
    virtual void SetUp() override {
        run_server_process(); // Start the server

        // Create a TCP socket
        sock = socket(AF_INET, SOCK_STREAM, 0);
        ASSERT_GE(sock, 0); // Assert socket creation succeeded

        // Setup the server address struct
        sockaddr_in server_addr{};
        server_addr.sin_family = AF_INET;
        server_addr.sin_port = htons(8081);
        inet_pton(AF_INET, "127.0.0.1", &server_addr.sin_addr);

        // Connect to the server
        int status = connect(sock, (sockaddr*)&server_addr, sizeof(server_addr));
        ASSERT_GE(status, 0); // Assert connection succeeded
    }

    // TearDown runs after each test
    virtual void TearDown() override {
        close(sock);                // Close the socket
        kill(server_pid, SIGTERM);  // Stop the server process
        waitpid(server_pid, NULL, 0); // Wait for the child to terminate
    }

    // Helper function to send a command to the server and receive response
    std::string send_command(const std::string& cmd) {
        send(sock, cmd.c_str(), cmd.size(), 0);
        char buffer[4096] = {0};
        recv(sock, buffer, sizeof(buffer), 0);
        return std::string(buffer);
    }
};

// Test that POST command adds a URL successfully
TEST_F(ServerIntegrationTest, PostCommandTest) {
    std::string response = send_command("POST http://test.com\n");
    EXPECT_EQ(response, "201 Created");
}

// Test that GET command returns correct response after posting the URL
TEST_F(ServerIntegrationTest, GetCommandTest) {
    send_command("POST http://a.com\n");
    std::string response = send_command("GET http://a.com\n");

    EXPECT_TRUE(response.find("200 OK\n") != std::string::npos);
    EXPECT_TRUE(response.find("true") != std::string::npos || response.find("false") != std::string::npos);
}

// Test that DELETE command removes a URL correctly
TEST_F(ServerIntegrationTest, DeleteCommandTest) {
    send_command("POST http://delete.com\n");
    std::string response = send_command("DELETE http://delete.com\n");
    EXPECT_EQ(response, "204 No Content");
}

// Test that an invalid command returns a proper error response
TEST_F(ServerIntegrationTest, BadRequestTest) {
    std::string response = send_command("INVALID COMMAND\n");
    EXPECT_EQ(response, "400 Bad Request");
}

// Test DELETE for a URL that was never added (should return 404)
TEST_F(ServerIntegrationTest, DeleteNonexistentUrlReturns404) {
    std::string response = send_command("DELETE http://notfound.com\n");
    EXPECT_EQ(response, "404 Not Found");
}