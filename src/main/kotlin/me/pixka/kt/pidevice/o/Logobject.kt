package me.pixka.kt.pidevice.o

/**
 * ใช้สำหรับส่ง ERROR ออกไปเป็น JSON
 */
class Logobject(var date:String?=null,var time:String?=null,var threadname:String?=null,var errortype:String,
var classname:String?=null,var errormessage:String?=null)