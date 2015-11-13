#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Author: Free Soaring
# @Date:   2015-09-14 20:08:30
# @Email:  twofly0313@gmail.com
# @Last Modified by:   Free Soaring
# @Last Modified time: 2015-11-03 10:09:15

import socket
import select
import json
import time
import sys
import os
from threading import Thread


FILEPATH = 'C:\\Users\\anyang\\Desktop\\PyClient\\PyClient\\Files\\'
FILENAME = ''

class TCPClient:

    def __init__(self, host, port):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((host, port))
        self.input = self.socket.makefile('rb', 0)
        self.output = self.socket.makefile('wb', 0)
        self.run()

    def Send(self, message):
        """
        Send message to server.
        """
        if message != "":
            if message[-1] != '\n':
                message += '\r\n'
                self.output.write(bytes(message, 'utf-8'))

    def run(self):
        """
        Start a separate thread to gather the input from the
        keyboard even as we wait for message to come over the
        network. 
        This makes it possible for the user to simultaneously
        send and receive chat text.
        """

        propagateStandardInput = PropagateStandardInput(self.output)
        propagateStandardInput.start()

        # Read from the network and print everything received to standard
        # output. Once data stops coming in from the network, it means
        # we've disconnected.
        inputText = True
        while inputText:
            try:
                inputText = self.input.readline().decode('utf-8')
            except socket.error:
                inputText = False
                propagateStandardInput.done = True
                print('Socket error!')
                pass
            if inputText:
                print(inputText.strip())
                if 'ready' == inputText.strip():
                    # filepath = 'D:\\Program Files\\Python34\\WorkStation\\PyClient\\Files\\'
                    # filename = 'stay.mp3'
                    filepath = FILEPATH
                    filename = FILENAME
                    print(filepath + filename)
                    self._sendfile(filepath + filename)

        propagateStandardInput.done = True

    def _sendfile(self, filepath):
        """发送文件"""
        """
        filepath : 文件完整路径
        """

        print('Send file!')
        f = open(filepath, 'rb')
        while True:
            data = f.read(4096)
            if not data:
                break
            self.socket.send(data)
        f.close()
        # self.socket.send(bytes('EOF', 'utf-8'))
        print('Send file end!')




class PropagateStandardInput(Thread):

    """
    A class that mirrors standard input to the chat server
    until it's told to stop.
    """

    def __init__(self, output):
        """Make this thread a daemon thread, so that if the Python
        interpreter needs to quit it won't be held up waiting for
        this thread to die."""
        Thread.__init__(self)
        self.setDaemon(True)
        self.output = output
        self.done = False

    def run(self):
        """Echo standard input to the chat server until told to stop."""

        # 该处可添加采集数据方法
        # 根据数据类型格式化不同的json数据发送给服务器
        while not self.done:

            # 模拟发送采集数据----------------------start
            
            inputText = ""

            data = DataType(13, 13)
            # 与服务端数据格式对应
            '''
            strdata = {'sensorID': data.sensorID, 'value': data.value, 'datetime': data.datetime}

            inputText = json.dumps(strdata)

            if inputText == '#':
                self.done = True
                break
            if inputText:
                netdata = 'client' + '#' + 'upload' + '#' + 'uploadtemperature' + '#' + str(inputText)
                self.output.write(bytes(netdata + '\r\n', 'utf-8'))
            '''
            # 模拟发送采集数据----------------------end

            # ------------------------------------------

            # 模拟APP请求数据----------------------start
            '''
            message = "{0}#{1}#{2}#{3}".format('app', 'get', 'gettemperature', 'None')
            print(message)
            if message:
                self.output.write(bytes(message + '\r\n', 'utf-8'))
            '''
            # 模拟APP请求数据-----------------------end

            # 模拟APP请求数据----------------------start

            while not self.done:
                inputText = sys.stdin.readline().strip()
                if inputText == '#':
                    self.done = True
                if inputText:
                    global FILENAME
                    FILENAME = inputText.strip()
                    filesize = os.path.getsize(FILEPATH + FILENAME)
                    print(FILEPATH + FILENAME)
                    print(str(filesize))
                    filename, filetype = inputText.split('.')
                    strdata = {'filename': filename, 'filetype': filetype, 'filesize': filesize}
                    data = json.dumps(strdata)
                    netdata = 'client' + '#' + 'upload' + '#' + 'uploadfile' + '#' + str(data)
                    self.output.write(bytes(netdata + '\r\n', 'utf-8'))

            # 模拟APP请求数据-----------------------end

            # 休眠5秒
            # time.sleep(600)



class PyClient(TCPClient):

    def __init__(self):
        """In a tight loop, see whether the user has entered any input
        or whether there's any from the network. Keep dong this until
        the network connection returns EOF."""
        socketClosed = False
        while not socketClosed:
            toRead, ignoe, ignoe = select.select([self.input], [], [])

            # We're not disconnected yet.
            for input in toRead:
                if input == self.input:
                    inputText = self.input.readline()
                    if inputText:
                        print(inputText.strip())
                    else:
                        # The attempt to read failed. The socket is closed.
                        socketClosed = True


class DataType:

    """
    采集数据的类型
    """
    sensorID = 0
    value = 0.0
    datetime = ''

    def __init__(self, sensorID, value):
        self.sensorID = sensorID
        self.value = value
        self.datetime = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())


if __name__ == '__main__':
    PyClient(TCPClient('10.50.62.65', 12000))
