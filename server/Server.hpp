//
//  Server.hpp
//  Server
//
//  Created by Jakub  Vaněk on 22/11/2018.
//  Copyright © 2018 Jakub  Vaněk. All rights reserved.
//

#ifndef Server_hpp
#define Server_hpp

#include <stdio.h>
#include <iostream>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include "MessageManager.hpp"

class Server{

public:
    int init(std::string adress, int port);
    void listenConnections();
    static void *checkConnected(void *args);
    static void closeSocket(int socketID);
private:
    int server_sock, return_value;
};

#endif /* Server_hpp */
