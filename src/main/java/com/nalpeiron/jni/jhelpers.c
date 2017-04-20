//jhelpers.c
//
// Created Sept. 16 2015
// R D Ramey
//
// jhelpers.c: Functions to aid in moving data between C and Java

#include <stdlib.h>
#include <string.h>

#if defined (LINUX) || defined (DARWIN) || defined (ANDNDK)
#include <unistd.h>
#endif

#include <stdint.h>
#include <jni.h>

#if defined (ANDNDK)
#include <android/log.h>
#endif

#include "osjava.h"
#include "nsajavafp.h"
#include "nalpJavaReturns.h"


int
getByteArraytoStr(
JNIEnv		*env,
jbyteArray	jbArray,
char		**cStr
)
{
	int	arLen;


	*cStr = NULL;

	//This returns the array length including the UTF8 strings
	// terminating NULL
	arLen = (*env)->GetArrayLength(env, jbArray);

	//We sometimes need to pass NULL pointers into the Shafer library 
	// (in the case of a trial license, for instance).  The
	// closest you can get with Java is a empty string as the null
	// reference really isn't very well thought out.  So, if we
	// have an empty string, pass in NULL.
	if (arLen <= 1)
	{
		return NALPJAVA_OK;
	}

	*cStr = (char *)malloc(arLen);

	if (*cStr == NULL)
	{
		return NALPJAVA_JNI_MEMORY;
	}

	(*env)->GetByteArrayRegion(env,
			jbArray, 0, arLen, (jbyte *)(*cStr));

	return NALPJAVA_OK;
}


//Returns array len - 1 (string len) on success
int
setByteArrayfromStr(
JNIEnv		*env,
jbyteArray	jbArray,
char		*cStr
)
{
	int	csLen;


	csLen = strlen(cStr);

	//Don't forget the NULL term
	if (csLen + 1 > (*env)->GetArrayLength(env, jbArray))
	{
		return NALPJAVA_JAVA_MEMORY;
	}
		
	//Don't forget the NULL term
	(*env)->SetByteArrayRegion(env,
			jbArray, 0, csLen + 1, (jbyte *)cStr);

	return csLen;
}


int
freeStrfromByteArray(
LIBHANDLE	nalpLib,
char		*cStr
)
{
	if (cStr == NULL)
	{
		return NALPJAVA_OK;
	}

	free(cStr);

	return NALPJAVA_OK;
}

