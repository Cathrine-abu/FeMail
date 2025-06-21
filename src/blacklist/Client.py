import socket
import sys

# Check if the user entered the IP and the PORT
def main():
    if len(sys.argv) != 3:
        sys.exit(1)

    server_ip = sys.argv[1] 
    server_port = int(sys.argv[2])

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((server_ip, server_port))

    # Infinite loop for user input
    while True:
        try:
            # Read user input
            msg = input()
            msg += '\n' # Add new line to the meassage
            s.send(bytes(msg, 'utf-8'))
            data = s.recv(4096)
            print(data.decode('utf-8'))
        except:
            # If any error happens, just continue without stopping the program
            continue

    s.close()

if __name__ == "__main__":
    main()