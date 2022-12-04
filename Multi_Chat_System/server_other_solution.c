#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <string.h>
#include <netdb.h>
#include <netinet/in.h>
#include <math.h>
#include <time.h>
#include <arpa/inet.h>
#include <signal.h>

#define PORT 10140
#define MAXCLIENTS 5
#define NAME_MAX 64

int csock[MAXCLIENTS];
char usrname[MAXCLIENTS][NAME_MAX];
struct sockaddr_in clt[MAXCLIENTS];
int k = 0;
char rbuf[1024];
long readLen = 0;
char wbuf[1024];
int flag = 0;
int pid;

int max(int a, int b){
    return a >= b ? a:b;
}

int findSockbyName(char *name){
    for(int i = 0; i < k; i++){
        if(strcmp(name, usrname[i]) == 0) return csock[i];
    }
    return -1;
}

void handler(){
    int hpid;
    if( (hpid = fork()) < 0){
        perror("handler fork");
        exit(1);
    }
    if(hpid == 0){
        char hbuf[] = "The server will be shut down in [ ]s."; //len 34
        for(int i = 10; i > 0; i--){
            hbuf[33] = '0' + i - 1;
            for(int j = 0; j < k; j++){
                write(csock[j], hbuf, strlen(hbuf));
            }
            sleep(1);
        }
        //to parent process
        kill(getppid(), SIGALRM);
        exit(0);
    }
    else{
        //to prevent child process becoming zombie
        signal(SIGCHLD,SIG_IGN);
    }
}

void finish(){
    flag = 1;
}

void addBuf(int index, int n, int start){
    time_t rawtime;
    struct tm *timeinfo;
    //name
    wbuf[index++] = '[';
    strncpy(&wbuf[index], usrname[n], strlen(usrname[n]));
    index += strlen(usrname[n]);
    wbuf[index++] = ']';
    wbuf[index++] = ' ';

    //time
    wbuf[index++] = 'T';
    wbuf[index++] = 'i';
    wbuf[index++] = 'm';
    wbuf[index++] = 'e';
    wbuf[index++] = ':';
    wbuf[index++] = ' ';

    time(&rawtime);
    timeinfo = localtime(&rawtime);
    char timestr[64];
    strcpy(timestr, asctime(timeinfo));
    strncpy(&wbuf[index], timestr, strlen(timestr));
    index += strlen(timestr);
    //ip
    wbuf[index-1] = ' ';
    wbuf[index++] = 'I';
    wbuf[index++] = 'P';
    wbuf[index++] = ':';
    wbuf[index++] = ' ';

    char addrstr[INET_ADDRSTRLEN];                            
    inet_ntop(AF_INET, &(clt[n].sin_addr), addrstr, INET_ADDRSTRLEN);
    strncpy(&wbuf[index], addrstr, strlen(addrstr));
    index += strlen(addrstr);

    wbuf[index++] = ':';
    wbuf[index++] = '\n';

    //message
    strncpy(&wbuf[index], &rbuf[start], readLen);
    index += readLen;
    wbuf[index] = '\0';
}

int main(){
    //execute handler when ctrl+C(SIGINT)
    if(signal(SIGINT, handler) < 0){
        perror("signal1");
        exit(1);
    }
    //execute finish when SIGALRM
    if(signal(SIGALRM, finish) < 0){
        perror("signal2");
        exit(1);
    }

    int pid;
    if((pid = fork()) < 0){
        perror("fork");
        exit(1);
    }
    //child
    if(pid == 0){
        int udpSock;
        int enable = 1;
        struct sockaddr_in udpaddr;

        if( (udpSock = socket(AF_INET, SOCK_DGRAM, 0)) < 0){
            perror("udp socket");
            exit(1);
        }
        bzero(&udpaddr, sizeof(udpaddr));
        udpaddr.sin_addr.s_addr = inet_addr("255.255.255.255");
        udpaddr.sin_port = htons(10141);
        udpaddr.sin_family = AF_INET;
        //for reuse
        setsockopt(udpSock, SOL_SOCKET, SO_BROADCAST, &enable, sizeof(enable));
        while(1){
            sendto(udpSock, "Hello", strlen("Hello"), 0, (struct sockaddr *) &udpaddr, sizeof(udpaddr));
            //send for every one second
            sleep(1);
        }
        exit(0);
    }

    int sock;
    struct sockaddr_in svr;
    int clen = 0;
    int reuse = 1;
    fd_set rfds;
    struct timeval tv;

    if( (sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0){
        perror("socket");
        exit(1);
    }

    if( setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0){
        perror("setsockopt");
        exit(1);
    }

    bzero(&svr, sizeof(svr));
    svr.sin_family = AF_INET;
    svr.sin_port = htons(PORT);
    svr.sin_addr.s_addr = htonl(INADDR_ANY);

    if(bind(sock, (struct sockaddr *) &svr, sizeof(svr)) < 0){
        perror("sock");
        exit(1);
    }

    if(listen(sock, MAXCLIENTS) < 0){
        perror("listen");
        exit(1);
    }

    do{
        //time to finish program
        if(flag){
            for(int i = 0; i < k; i++) close(csock[i]);
            close(sock);
            exit(0);
        }
        FD_ZERO(&rfds);
        FD_SET(sock, &rfds);
        int fdMax = sock;
        for(int i = 0; i < k; i++){
            FD_SET(csock[i], &rfds);
            fdMax = max(sock,csock[i]);
        } 
        
        tv.tv_sec = 0.5;
        tv.tv_usec = 0;

        if(select(fdMax+1, &rfds, NULL, NULL, &tv) > 0){

            //new client 
            if(FD_ISSET(sock, &rfds)){
                int tmpsock;
                struct sockaddr_in tmpclt;
                int tmpclen = sizeof(tmpclt);
                if( (tmpsock = accept(sock, (struct sockaddr *) &tmpclt, &tmpclen)) < 0){
                    perror("tmpsock accept");
                    exit(1);
                }
            
                //reject
                if(k == MAXCLIENTS){ 
                    if(write(tmpsock, "REQUEST REJECTED\n", 17) < 0){
                        perror("write1");
                        exit(1);
                    }
                    close(tmpsock);
                }
                //accept
                else{ 
                    write(tmpsock, "REQUEST ACCEPTED\n", 17);
                    char name[NAME_MAX];
                    int flag;
                    int len;
                    do{
                        flag = 0;
                        if( (len = read(tmpsock, name, sizeof(name))) < 0){
                            perror("read1");
                            exit(1);
                        }
                        //replace '\n' with '\0'
                        name[len-1] = '\0';
                        //check if username already exists
                        for(int i = 0; i < k; i++){
                            if(strcmp(usrname[i],name) == 0){
                                if(write(tmpsock, "USERNAME REJECTED\n", 18) < 0){
                                    perror("write2");
                                    exit(1);
                                }
                                flag = 1;
                                break;
                            }
                        }
                    }while(flag);

                    //register usename and increase k
                    if(!flag){
                        strcpy(usrname[k],name);
                        csock[k] = tmpsock;

                        int cltLen = sizeof(struct sockaddr_in);
                        getpeername(csock[k], (struct sockaddr *) &clt[k], &cltLen);

                        if(write(csock[k], "USERNAME REGISTERED\n", 20) < 0){
                            perror("write3");
                            exit(1);
                        }
                        printf("Username registered: [%s]\n", name);

                        char wbuf[128] = "User [";
                        strcat(wbuf, usrname[k]);
                        strcat(wbuf, "] has entered.");
                        for(int i = 0; i < k; i++){
                            if(write(csock[i], wbuf, strlen(wbuf)) < 0){
                                perror("write4");
                                exit(1);
                            }
                        }
                        k++;
                    }
                }
            }

            //check input from csocks
            for(int i = 0; i < k; i++){
                if(FD_ISSET(csock[i], &rfds)){
                    printf("from [%s]\n", usrname[i]);
                    if( (readLen = read(csock[i], rbuf, sizeof(rbuf))) < 0){
                        perror("read2");
                        exit(1);
                    }
                    rbuf[readLen] = '\0';

                    //quit
                    if(readLen == 0){
                        close(csock[i]);
                        printf("[%s] has quited.\n",usrname[i]);

                        char mbuf[128] = "User [";
                        strcat(mbuf, usrname[i]);
                        strcat(mbuf, "] has quited.");
                        for(int m = 0; m < k; m++){
                            if(m != i){
                                if(write(csock[m], mbuf, strlen(mbuf)) < 0){
                                    perror("write4");
                                    exit(1);
                                }
                            }
                        }
                        for(int j = i; j < k; j++){
                            
                            //among the array
                            if(j+1 < k){
                                bzero(usrname[j],sizeof(usrname[j]));
                                strcpy(usrname[j],usrname[j+1]);
                                csock[j] = csock[j+1];
                            }
                            //last element of array
                            else{
                                bzero(usrname[j],sizeof(usrname[j]));
                                csock[j] = 0;
                            }
                        }
                        k--;
                    }
                    else if(rbuf[0] == '/'){
                        //"/list"
                        if(strlen(rbuf) == 5 && strncmp("/list", rbuf, 5) == 0){
                          char lbuf[1024];
                          int index = 0;
                          //concatenate all usernames
                          for(int j = 0; j < k; j++){
                              lbuf[index++] = '0' + j;
                              lbuf[index++] = ':';
                              lbuf[index++] = ' ';
                              strncpy(&lbuf[index], usrname[j], strlen(usrname[j]));
                              index += strlen(usrname[j]);
                              lbuf[index++] = '\n';
                          }
                          lbuf[index] = '\0';
                          if(write(csock[i], lbuf, strlen(lbuf)) < 0){
                              perror("write5");
                              exit(1);
                          }
                        }
                      //"/send hoge message"
                        else if(strstr(rbuf, "/send ") != NULL){
                            char tmp[strlen(rbuf)];
                            strcpy(tmp, rbuf);
                            strtok(tmp, " ");
                            char *DMName = strtok(NULL, " ");
                            int DMSock = findSockbyName(DMName);
                            if(DMSock == -1){
                                if( write(csock[i], "No such username!", strlen("No such username!")) < 0){
                                    perror("write6");
                                    exit(1);
                                }
                            }
                            else{
                                int cnt = 0;
                                int index = 0;
                                for(int j = 0; j < readLen; j++){
                                    if(rbuf[j] == ' ') cnt++;
                                    if(cnt == 2){
                                        index = j + 1;
                                        printf("index = %d\n",index);
                                        break;
                                    }
                                }

                                bzero(&wbuf, sizeof(wbuf));
                                int len = strlen("(direct message) ");
                                strncpy(wbuf, "(direct message) ", len);
                                addBuf(len, i, index);

                                //no need to add '\0' because strcpy includes it
                                if(write(DMSock, wbuf, strlen(wbuf)) < 0){
                                    perror("write7");
                                    exit(1);
                                }
                            }
                       
                        }
                        //others send error
                        else{
                            if(write(csock[i], "Invalid command!", strlen("Invalid command!")) < 0){
                                perror("write8");
                                exit(1);
                            }
                        }
                    }
                    
                    //string
                    else{
                        for(int j = 0; j < k; j++){
                            bzero(&wbuf, sizeof(wbuf));
                            addBuf(0,i,0);
     
                            if(write(csock[j], wbuf, strlen(wbuf)) < 0){
                                perror("write9");
                                exit(1);
                            }
                        }
                    }
                }
            }
        }
    }while(1);
}
