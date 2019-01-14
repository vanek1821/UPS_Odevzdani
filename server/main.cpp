//
//  main.cpp
//  server
//
//  Created by Jakub  Vaněk on 22/11/2018.
//  Copyright © 2018 Jakub  Vaněk. All rights reserved.
//

#include <iostream>
#include "Server.hpp"

int main(int argc, const char * argv[]) {
    
    std::cout <<"Hello to server!\n";
    
    Server* server = new Server();
    string adress = "127.0.0.1";
    int port = 10000;
    
    if (argc<3){
        cout << "Chybně zadané parametry" << endl;
        cout << "1. parametr - adress" << endl;
        cout << "2. parametr - port" << endl;
        cout << "př.: ./server 127.0.0.1 10000";
        return 0;
    } else {
        adress = argv[1];
        port = stoi(argv[2]);
    }
    
    if(server->init(adress, port) == -1){
        cout << "Chyba inicializace serveru. Zkuste prosím znovu." << endl;
        return 0;
    }
    
    server->listenConnections();
    return 0;
}
