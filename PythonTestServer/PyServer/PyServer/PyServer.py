#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Author: Free Soaring
# @Date:   2015-09-14 18:44:59
# @Email:  twofly0313@gmail.com
# @Last Modified by:   Free Soaring
# @Last Modified time: 2015-11-06 23:13:25

import socket
import socketserver
import re
import os
import sys
import json


def getpwd():
    """
    获取当前运行路径
    """
    pwd = sys.path[0]
    if os.path.isfile(pwd):
        pwd = os.path.dirname(pwd)
    return pwd

# 将当前运行路径加入环境变量
path = getpwd()
if path != '':
    sys.path.append(path)
# Add the MySQLService module
import MySQLService
import imp
imp.reload(MySQLService)


class ClientError(Exception):

    """
    An exception throw because the client gave bad input to the server.
    """
    pass


class PyServer(socketserver.ThreadingTCPServer):

    """
    The server class.
    """

    def __init__(self, server_address, RequestHandlerClass):
        """Set up  an initially empty mapping between a user's nickname
        and the file-like object used to send data to that user."""
        socketserver.ThreadingTCPServer.__init__(self, server_address,
                                                 RequestHandlerClass)
        print('PyServer now started...')
        self.users = {}


class RequestHandler(socketserver.StreamRequestHandler):

    """
    Handles a life cycle of a user's connection to the chart
    server: connecting, chatting, running server commands, and
    disconnecting.
    """

    # Regex for a valid nickname.
    NICKNAME = re.compile('^[A-Za-z0-9_-]+$')

    def handle(self):
        """Handles a connection: get the user's nickname, then
        processes input from the user until they quit or drop
        the connection.
        """

        print("Accept client %s : %s ." % (self.client_address[0], self.client_address[1]))
        self.nickname = None
        done = False
        netdata = ""
        try:
            netdata = str((self._readline()).decode('utf-8'))
        except ClientError as error:
            # self.privateMessage(error.args[0])
            print("ClientError")
            done = True
            pass
        except socket.error:
            print("SocketError")
            done = True
            pass

        if "" != netdata:
            datalist = netdata.split('#')
            nickname = datalist[0]
            self.nickname = nickname
            self.saveClients(nickname)
            print('%s has connected to the server.' % nickname)

            # 仅用户第一次使用app连接服务器时发送Welcome
            if "app" == nickname:
                self.privateMessage('Hello %s ,welcome to the PyServer.' % nickname) 
            # self.privateMessage('ready')    #test Li
        else:
            done = True

        # Now they're logged in; let them chart.
        while not done:
            try:
                done = self.processInput()
            except ClientError as error:
                # self.privateMessage(error.args[0])
                done = True
            except socket.error:
                done = True

    def finish(self):
        """Automatically called when handle() is done."""
        if self.nickname:
            # The user successfully connected before disconnecting.
            # Broadcast that they're quitting to everyone else.
            message = '%s ha quit.' % self.nickname
            if hasattr(self, 'partingWords'):
                message = '%s has quit: %s' % (self.nickname, self.partingWords)
                self.broadcast(message, False)

            # Remove the user from the list so we don't keep trying to
            # send them message.
            if self.server.users.get(self.nickname):
                del(self.server.users[self.nickname])
        self.request.shutdown(2)
        self.request.close()

    def processInput(self):
        """Reads a line from the socket input """
        # 接收消息并处理

        """
        预设消息格式：
            user # action # command # param(jsondata)

        user:
            1：client 客户端
            2：app Android Application

        action:
            1: upload 客户端上传数据
            2: get app获取数据
            3: post app获取数据

        command：
            此处需根据操作类型自定义
            如：客户端上传温度数据 command = 'uploadtemperature'
                app 获取温度数据 command = 'gettemperature'

        param(jsondata):
            jsondata: 传输的数据内容
            param: 获取数据时的参数
        """

        done = False
        msg = ''
        try:
            msg = (self._readline()).decode('utf-8')
        except:
            done = True
        if done:
            return done

        user, action, command, jsondata = self._parseCommand(msg)

        if 'client' == user:
            # 处理客户端消息--------------------------------------------------
            # 上传采集的数据，上传文件等--------------------------------------
            if 'upload' == action:
                if 'uploadtemperature' == command:
                    # 客户端上传"温度等"数据至数据库

                    # 与客户端数据格式对应
                    data = json.loads(jsondata)
                    sensorID = data['sensorID']
                    value = data['value']
                    datetime = data['datetime']

                    MySQLService.SaveTemperature(sensorID, value, datetime)
                    self.privateMessage('success!')

                elif 'uploadfile' == command:
                    # 客户端上传文件--->服务器--->app

                    # 发送‘已准备好’状态信息
                    self.privateMessage('ready')

                    data = json.loads(jsondata)
                    filename = data['filename']
                    filetype = data['filetype']
                    filesize = data['filesize']
                    state = self._reciveFile(filename, filetype, filesize)

                    if state is not 'Error':
                        # 将文件路径保存至数据库(略)

                        filemsg = "sendfile#" + filename +'#'+ filetype+'#'+str(filesize)

                        # 将文件的基本信息发送至app等待app请求文件
                        # 发送文件操作将转至 app3
                        self.sendDataTo(message = filemsg, sendTo = "app")
                        print('--->Send file message to app: %s ' % filemsg)
                    else:
                        pass
                else:
                    pass
            else:
                pass

        elif 'app1' == user:
            # 处理App1消息：------------------------------------------------------
            # app 发送命令至 client-----------------------------------------------
            if 'post' == action:
                # 处理post请求,APP1的话这个分支不会到
                if 'postmessage' == command:
                    message = jsondata
                    print('App--->Client: %s' % message)
                    self.sendDataTo(message = message, sendTo = "client")
                else:
                    pass
            else:
                pass

        elif 'app2' == user:
            # 处理App2消息--------------------------------------------------------
            # app 获取数据库数据--------------------------------------------------
            if 'get' == action:
                # 处理get请求
                if 'gettemperature' == command:
                    # 具体操作

                    # 与MySQLService数据格式对应
                    jsondata = MySQLService.GetTemperature()
                    self.privateMessage(jsondata)
                    print('---->Send SQL data to app.')
                else:
                    pass
            else:
                pass

        elif 'app3' == user:
            # 处理App3消息---------------------------------------------------------
            # app 端请求服务器发送文件---------------------------------------------
            if 'get' == action:
                if 'getfile' == command:
                    print('--->App request file : %s ' % jsondata)
                    # jsondata = filename.filetype
                    self.sendDataTo(message = "", sendTo = "app3", isFile = True, fileName = jsondata)
                    pass
                else:
                    pass
                pass
            else:
                pass
        else:
            pass

        return done

    # Below are implementations of the server commands.
    def quitCommand(self, partingWords):
        """Tells the other users that this user has quit, then makes
        sure the handler will close this connection."""
        if partingWords:
            self.partingWords = partingWords
        # Returning True makes sure the user will be disconnected.
        return True

    def saveClients(self, nickname):
        """Save client state."""
        if not nickname:
            raise ClientError('No nickname provided.')
        if not self.NICKNAME.match(nickname):
            raise ClientError('Invalid nickname: %s.' % nickname)
        if not self.server.users.get(nickname, None):
            self.server.users[nickname] = self.wfile
            self.nickname = nickname
            print('%s is saving...' % nickname)

    # Below are helper methods.

    def privateMessage(self, message):
        """Send a private message to this user."""
        self.wfile.write((self._ensureNewline(message)).encode())

    # 原brodcast()函数
    def sendDataTo(self, message, sendTo = "", isFile = False, fileName = "", includeThisUser = False):
        """Sends a message to every connected user, possibly exempting
        the user who's the cause of the message."""

        """
        message = self._ensureNewline(message)
        for user, output in self.server.users.items():
            if includeThisUser or user != self.nickname:
                output.write(bytes(message, 'utf-8'))
        """

        if "client" == sendTo:
            # 发送数据至Client
            print("app---(command)--->|server|--->client：%s" % message)
            for user, output in self.server.users.items():
                if "client" == user:
                    message = self._ensureNewline(message)
                    output.write(bytes(message, 'utf-8'))

        elif "app" == sendTo:
            # 发送文件基本消息至APP端app Sockket
            print("client---(file message)--->|server|--->app：%s" % message)
            for user, output in self.server.users.items():
                if "app" == user:
                    # 发送文本数据
                    message = self._ensureNewline(message)
                    output.write(bytes(message, 'utf-8'))
                else:
                    pass
            pass

        elif "app1" == sendTo:
            # 发送数据至APP端app1 Sockket
            pass

        elif "app2" == sendTo:
            # 发送数据至APP端app2 Sockket
            pass

        elif "app3" == sendTo:
            # 发送文件至APP端app3 Sockket
            print("server--->|file|--->app：%s" % message)
            for user, output in self.server.users.items():
                if "app3" == user:
                    # 发送文件
                    _path = '.\\Files\\'
                    _filePath = _path + fileName
                    print("--->Start send file to app...")
                    try:
                        f = open(_filePath, 'rb')
                        while True:
                            data = f.read(4096)
                            if not data:
                                break
                            output.write(data)
                        f.close()
                        raise
                        print("--->Send file to app success!")
                    except Exception as err:
                        print("--->Send file to app failed!")
                        print(err)
                    pass
                else:
                    pass
            pass

        else:
            # 发送广播数据
            pass



    def _readline(self):
        """Reads a line, removing any whitespace."""
        return self.rfile.readline().strip()

    def _ensureNewline(self, s):
        """Makes sure a string ends in a new line."""
        if s and s[-1] != '\n':
            s += '\r\n'
        return s

    def _parseCommand(self, input):
        """Trying to parse a string as a command to the server. """
        # 消息解析函数

        user, action, command, jsondata = None, None, None, None
        if '#' in input:
            inputlist = input.split('#')
            if 4 == len(inputlist):
                user = inputlist[0]
                action = inputlist[1]
                command = inputlist[2]
                jsondata = inputlist[3]
            else:
                raise ClientError('Invalid command: "%s"' % input)
        else:
            raise ClientError('Invalid command: "%s"' % input)

        return user, action, command, jsondata

    def _reciveFile(self, filename, filetype, filesize):
        """接收文件"""

        """
        filename : 文件名
        filetype : 文件类型
        """
        _path = '.\\Files\\'
        _filePath = _path + filename + '.' + filetype

        print('Receive file!')

        f = open(_filePath, 'wb')
        size = filesize
        print(size)
        while True:
            if 4096 < size:
                data = self.rfile.read(4096)
                # print(data)
                size -= 4096
                f.write(data)
            else:
                data = self.rfile.read(size)
                f.write(data)
                break
        f.close()
        print('Receive File end!')

        return _filePath






if __name__ == '__main__':

    PyServer(('10.50.62.65', 12000), RequestHandler).serve_forever()