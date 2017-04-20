//jhelpers.h
//
// Created Sept. 16 2015
// R D Ramey
//
// jhelpers.h: Functions to aid in moving data between C and Java

#ifndef __JHELPERS_H__
#define __JHELPERS_H__

#if ! defined (ANDNDK)
#include <jni_md.h>
#endif

#include "nsajavafp.h"


int
getByteArraytoStr(
JNIEnv		*env,
jbyteArray	jbArray,
char		**cStr
);

int
setByteArrayfromStr(
JNIEnv		*env,
jbyteArray	jbArray,
char		*cStr
);

int
freeStrfromByteArray(
LIBHANDLE	nalpLib,
char		*cStr
);

#endif // __JHELPERS_H__
