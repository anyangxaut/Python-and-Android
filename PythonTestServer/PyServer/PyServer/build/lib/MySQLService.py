#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Author: Free Soaring
# @Date:   2015-09-14 18:47:30
# @Email:  twofly0313@gmail.com
# @Last Modified by:   Free Soaring
# @Last Modified time: 2015-09-15 11:12:40

import pymysql
import datetime


"""
Module for MySQLService.
Import this module and then call:
SaveTemperature(), GetTemperature().
"""

__all__ = ['MySQLService', 'SaveTemperature', 'GetTemperature']

# Helper functions:
# Here write your functions, and add the name to __all__
# After modified the file, you must install this module again

def SaveTemperature():
    mss = MySQLService(host="202.200.119.158", user="root", pwd = "root", 
        db = "test")

    sqlcmd = "insert into sensordata (sensorID, value, datetime) values(666, 888, '"+ now + "')"
    print(sqlcmd)
    mss.ExecNonQuery(sqlcmd)


def GetTemperature():
    mss = __GetService()
    resList = mss.ExecQuery("select * from sensordata")

    for data in resList:
        print("ID:" + str(data[0]) + " Sensor:" + str(data[1]) 
            + " Value:" + str(data[2]) + " Time:" + str(data[3]))


def __GetService():
    mss = MySQLService(host="202.200.119.158", user="root", pwd = "root", 
        db = "test")
    return mss


class MySQLService:

    """
    MySQLService MySQL服务类
    对进行pymssql了简单封装
    """

    def __init__(self, host, user, pwd, db):
        self.host = host
        self.user = user
        self.pwd = pwd
        self.db = db

    def __GetConnect(self):
        """
        Get the contention info
        Return: conn.cursor()
        """
        if not self.db:
            raise(NameError, 'No database setting inform.')
        self.conn = pymysql.connect(host = self.host, user = self.user, 
            password = self.pwd, database = self.db, charset = "utf8")

        cur = self.conn.cursor()
        if not cur:
            raise(NameError, 'Faild to connect with the database.')
        else:
            return cur

    def ExecQuery(self, sql):
        """
        执行查询语句
        返回一个包含tuple的list，list的元素是记录行，tuple的元素是每行记录的字段

        调用示例：
            ms = MySQLService(host="localhost",user="sa",pwd="123456",db="PythonWeiboStatistics")
            resList = ms.ExecQuery("SELECT id,NickName FROM WeiBoUser")
            for (id,NickName) in resList:
                print str(id),NickName
        """
        cur = self.__GetConnect()
        cur.execute(sql)
        resList = cur.fetchall()

        # Now done and close the connection
        self.conn.close()
        return resList

    def ExecNonQuery(self, sql):
        """
        执行非查询语句

        调用示例：
            cur = self.__GetConnect()
            cur.execute(sql)
            self.conn.commit()
            self.conn.close()
        """
        cur = self.__GetConnect()
        cur.execute(sql)
        # Must commit the mission
        self.conn.commit()
        self.conn.close()


def MySQLServiceMain():
    mss = MySQLService(host="202.200.119.158", user="root", pwd = "root", 
        db = "test")
    resList = mss.ExecQuery("select * from sensordata")

    for data in resList:
        print("ID:" + str(data[0]) + " Sensor:" + str(data[1]) 
            + " Value:" + str(data[2]) + " Time:" + str(data[3]))

    now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(now)

    """
    sqlcmd = "insert into sensordata (sensorID, value, datetime) values(666, 888, '"+ now + "')"
    print(sqlcmd)
    mss.ExecNonQuery(sqlcmd)

    resList2 = mss.ExecQuery("select * from sensordata")
    for data in resList2:
        print("ID:" + str(data[0]) + " Sensor:" + str(data[1]) 
            + " Value:" + str(data[2]) + " Time:" + str(data[3]))
    """


if __name__ == '__main__':
    MySQLServiceMain()
