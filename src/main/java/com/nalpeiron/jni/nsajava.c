//
// nsajava.c
//
// Created July 19 2013
// R. D. Ramey
//
// nsajava.c : Defines the exported functions for the NSA Library
//

#if ! defined (LINUX) && ! defined (DARWIN) && ! defined (ANDNDK)
//#include <windows.h>
#else
#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#if ! defined (ANDNDK)
#include <jni_md.h>
#endif
#endif

#if defined (ANDNDK)
#include <android/log.h>
#endif

#include <stdint.h>

#include "josdefs.h"
#include "osjava.h"
#include "nsajavafp.h"
#include "nsajava.h"
#include "nalpjavafp.h"
#include "nalpjava.h"
#include "jhelpers.h"
#include "nalpJavaReturns.h"



//***********************************************
//*
//* NSA (Anaylitic) Routines
//*
//***********************************************

//returns "error" on error and version string otherwise
// return int is error < 0 and strlen > 0
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAGetVersion(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret
)
{
	NSAGetVersion_t		NSAGetVersion_ptr;
	NSAFree_t			NSAFree_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	char				*nsaVer;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nsaVer = NULL;

	NSAGetVersion_ptr = (NSAGetVersion_t)NalpGetSym(nalpLib, "NSAGetVersion");

	if (NSAGetVersion_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSAGetVersion_ptr(&nsaVer);

	NSAFree_ptr = (NSAFree_t)NalpGetSym(nalpLib, "NSAFree");

	if (NSAFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nsaVer != NULL)
		{
			NSAFree_ptr(nsaVer);
		}

		return retVal;
	}

	if (nsaVer == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nsaVer);

	NSAFree_ptr(nsaVer);

    return retVal;
}


//returns "error" on error and hostname string otherwise
// return int is error < 0 and strlen > 0
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAGetHostName(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret
)
{
	NSAGetHostName_t		NSAGetHostName_ptr;
	NSAFree_t				NSAFree_ptr;
	LIBHANDLE				nalpLib;
	int						retVal;
	char					*nsaHostname;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nsaHostname = NULL;

	NSAGetHostName_ptr = (NSAGetHostName_t)NalpGetSym(nalpLib, "NSAGetHostName");

	if (NSAGetHostName_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSAGetHostName_ptr(&nsaHostname);

	NSAFree_ptr = (NSAFree_t)NalpGetSym(nalpLib, "NSAFree");

	if (NSAFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	if (retVal < 0)
	{
		if (nsaHostname != NULL)
		{
			NSAFree_ptr(nsaHostname);
		}

		return retVal;
	}

	if (nsaHostname == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nsaHostname);

	NSAFree_ptr(nsaHostname);

    return retVal;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSALogin(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSALogin_t			NSALogin_ptr;
	LIBHANDLE			nalpLib;
	long int			transID;
	char				*username;
	char				*nsaClientData;
	int					retVal;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSALogin_ptr = (NSALogin_t)NalpGetSym(nalpLib, "NSALogin");

	if (NSALogin_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	(*env)->
		GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSALogin_ptr(username, nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env,
				jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSALogin_ptr(username, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSALogout(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSALogout_t			NSALogout_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	long int			transID;
	char				*username;
	char				*nsaClientData;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSALogout_ptr = (NSALogout_t)NalpGetSym(nalpLib, "NSALogout");

	if (NSALogout_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	(*env)->
		GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
	
		retVal = NSALogout_ptr(username, nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env,
				jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSALogout_ptr(username, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAApStart(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSAApStart_t		NSAApStart_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	long int			transID;
	char				*nsaClientData;
	char				*username;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAApStart_ptr = (NSAApStart_t)NalpGetSym(nalpLib, "NSAAppStart");

	if (NSAApStart_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	(*env)->GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSAApStart_ptr(username, 
				nsaClientData, (unsigned int *)&transID);

		(*env)->SetLongArrayRegion(env,
				jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSAApStart_ptr(username, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAApStop(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSAApStop_t			NSAApStop_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	char				*nsaClientData;
	char				*username;
	long int			transID;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAApStop_ptr = (NSAApStop_t)NalpGetSym(nalpLib, "NSAAppStop");

	if (NSAApStop_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	(*env)->GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSAApStop_ptr(username,
					nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env,
				jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSAApStop_ptr(username, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAFeatureStart(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jFeatureCode,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSAFeatureStart_t		NSAFeatureStart_ptr;
	LIBHANDLE				nalpLib;
	int						retVal;
	char					*username;
	char					*featureCode;
	char					*nsaClientData;
	long int				transID;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAFeatureStart_ptr = (NSAFeatureStart_t)NalpGetSym(nalpLib, "NSAFeatureStart");

	if (NSAFeatureStart_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jFeatureCode, &featureCode);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, nsaClientData);
		return retVal;
	}

	(*env)->
		GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSAFeatureStart_ptr(username, featureCode,
				nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSAFeatureStart_ptr(username,
					featureCode, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);
	freeStrfromByteArray(nalpLib, featureCode);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAFeatureStop(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jFeatureCode,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSAFeatureStop_t		NSAFeatureStop_ptr;
	LIBHANDLE				nalpLib;
	char					*username;
	char					*featureCode;
	char					*nsaClientData;
	long int				transID;
	int						retVal;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAFeatureStop_ptr = (NSAFeatureStop_t)NalpGetSym(nalpLib, "NSAFeatureStop");

	if (NSAFeatureStop_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jFeatureCode, &featureCode);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, nsaClientData);
		return retVal;
	}

	(*env)->
		GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSAFeatureStop_ptr(username, featureCode,
				nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSAFeatureStop_ptr(username, featureCode, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);
	freeStrfromByteArray(nalpLib, featureCode);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAException(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jExceptionCode,
jbyteArray	jDescription,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSAException_t		NSAException_ptr;
	LIBHANDLE			nalpLib;
	char				*username;
	char				*exceptionCode;
	char				*description;
	char				*nsaClientData;
	long int			transID;
	int					retVal;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAException_ptr = (NSAException_t)NalpGetSym(nalpLib, "NSAException");

	if (NSAException_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jExceptionCode, &exceptionCode);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, nsaClientData);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jDescription, &description);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, nsaClientData);
		freeStrfromByteArray(nalpLib, exceptionCode);
		return retVal;
	}

	(*env)->GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSAException_ptr(username, exceptionCode,
				description, nsaClientData, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSAException_ptr(username, exceptionCode,
				description, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, nsaClientData);
	freeStrfromByteArray(nalpLib, exceptionCode);
	freeStrfromByteArray(nalpLib, description);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSASysInfo(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jbyteArray	jApplang,
jbyteArray	jVersion,
jbyteArray	jEdition,
jbyteArray	jBuild,
jbyteArray	jLicenseStat,
jbyteArray	jnsaClientData,
jlongArray	jTransID
)
{
	NSASysInfo_t		NSASysInfo_ptr;
	LIBHANDLE			nalpLib;
	char				*username;
	char				*applang;
	char				*version;
	char				*edition;
	char				*build;
	char				*licenseStat;
	char				*nsaClientData;
	long int			transID;
	int					retVal;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSASysInfo_ptr = (NSASysInfo_t)NalpGetSym(nalpLib, "NSASysInfo");

	if (NSASysInfo_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jApplang, &applang);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jVersion, &version);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, applang);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jEdition, &edition);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, applang);
		freeStrfromByteArray(nalpLib, version);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jBuild, &build);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, applang);
		freeStrfromByteArray(nalpLib, version);
		freeStrfromByteArray(nalpLib, edition);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jLicenseStat, &licenseStat);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, applang);
		freeStrfromByteArray(nalpLib, version);
		freeStrfromByteArray(nalpLib, edition);
		freeStrfromByteArray(nalpLib, build);
		return retVal;
	}

	retVal = getByteArraytoStr(env, jnsaClientData, &nsaClientData);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, username);
		freeStrfromByteArray(nalpLib, applang);
		freeStrfromByteArray(nalpLib, version);
		freeStrfromByteArray(nalpLib, edition);
		freeStrfromByteArray(nalpLib, build);
		freeStrfromByteArray(nalpLib, licenseStat);
		return retVal;
	}

	(*env)->GetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSASysInfo_ptr(username, applang, version,
				edition, build, licenseStat, nsaClientData,
				(unsigned int *)(&transID));

		(*env)->
			SetLongArrayRegion(env, jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSASysInfo_ptr(username, applang, version,
				edition, build, licenseStat, nsaClientData, NULL);
	}

	freeStrfromByteArray(nalpLib, username);
	freeStrfromByteArray(nalpLib, applang);
	freeStrfromByteArray(nalpLib, version);
	freeStrfromByteArray(nalpLib, edition);
	freeStrfromByteArray(nalpLib, build);
	freeStrfromByteArray(nalpLib, licenseStat);
	freeStrfromByteArray(nalpLib, nsaClientData);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSASendCache(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jUsername,
jlongArray	jTransID
)
{
	NSASendCache_t		NSASendCache_ptr;
	LIBHANDLE			nalpLib;
	char				*username;
	jlong 				tempTID;
	long int			transID;
	int					retVal;


	transID = 0;

	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSASendCache_ptr = (NSASendCache_t)NalpGetSym(nalpLib, "NSASendCache");

	if (NSASendCache_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jUsername, &username);

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->GetLongArrayRegion(env, jTransID, 0, 1, &tempTID);
	transID = (long int)tempTID;

	//	If it loaded, call it
	if (transID != -1)
	{
		retVal = NSASendCache_ptr(username, (unsigned int *)(&transID));

		(*env)->SetLongArrayRegion(env,
				jTransID, 0, 1, (jlong *)(&transID));
	}
	else
	{
		retVal = NSASendCache_ptr(username, NULL);
	}

	freeStrfromByteArray(nalpLib, username);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAGetLocation(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle
)
{
	NSAGetLocation_t	NSAGetLocation_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAGetLocation_ptr = (NSAGetLocation_t)
			NalpGetSym(nalpLib, "NSAGetLocation");

	if (NSAGetLocation_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//	If it loaded, call it
	retVal = NSAGetLocation_ptr();

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAGetPrivacy(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle
)
{
	NSAGetPrivacy_t		NSAGetPrivacy_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAGetPrivacy_ptr = (NSAGetPrivacy_t)
			NalpGetSym(nalpLib, "NSAGetPrivacy");

	if (NSAGetPrivacy_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//	If it loaded, call it
	retVal = NSAGetPrivacy_ptr();

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSASetPrivacy(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jint		jPrivacy
)
{
	NSASetPrivacy_t		NSASetPrivacy_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSASetPrivacy_ptr = (NSASetPrivacy_t)
			NalpGetSym(nalpLib, "NSASetPrivacy");

	if (NSASetPrivacy_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//	If it loaded, call it
	retVal = NSASetPrivacy_ptr(jPrivacy);

	if (retVal < 0)
	{
		return retVal;
	}

	return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSA_NSAGetStats(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jnsaStats
)
{
	NSAGetStats_t		NSAGetStats_ptr;
	NSAFree_t			NSAFree_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	char				*nsaStats;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSAGetStats_ptr = (NSAGetStats_t)
			NalpGetSym(nalpLib, "NSAGetStats");

	if (NSAGetStats_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSAGetStats_ptr(&nsaStats);

	NSAFree_ptr = (NSAFree_t)NalpGetSym(nalpLib, "NSAFree");

	if (NSAFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nsaStats != NULL)
		{
			NSAFree_ptr(nsaStats);
		}

		return retVal;
	}

	retVal = setByteArrayfromStr(env, jnsaStats, nsaStats);

	NSAFree_ptr(nsaStats);

	return retVal;
}

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
