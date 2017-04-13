//
// nsljava.c
//
// Created Jan 19 2013
// R. D. Ramey
//
// nsljava.c : Defines the exported functions for the NSL Library
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

#include <stdint.h>

#include "josdefs.h"
#include "osjava.h"
#include "nsljavafp.h"
#include "nsljava.h"
#include "nalpjavafp.h"
#include "nalpjava.h"
#include "jhelpers.h"
#include "nalpJavaReturns.h"


//***********************************************
//*
//* NSL (Licensing) Routines
//*
//***********************************************

//returns 0 on success (valid library).  Anything else is invalid
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLValidateLibrary(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jlong		jcustID,
jlong		jprodID,
jint		joffset
)
{
	NSLValidateLibrary_t		NSLValidateLibrary_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLValidateLibrary_ptr = (NSLValidateLibrary_t)
			NalpGetSym(nalpLib, "NSLValidateLibrary");

	if (NSLValidateLibrary_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLValidateLibrary_ptr((int)jcustID, (int)jprodID);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

    return NALPJAVA_OK;
}


//returns "error" on error and version string otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetVersion(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetVersion_t		NSLGetVersion_ptr;
	NSLFree_t			NSLFree_ptr;
	LIBHANDLE			nalpLib;
	int					retVal;
	char				*nslVer;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslVer = NULL;

	NSLGetVersion_ptr = (NSLGetVersion_t)NalpGetSym(nalpLib, "NSLGetVersion");

	if (NSLGetVersion_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetVersion_ptr(&nslVer);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslVer != NULL)
		{
			NSLFree_ptr(nslVer);
		}

		return retVal;
	}

	if (nslVer == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslVer);

	NSLFree_ptr(nslVer);

	return retVal;
}


//returns "error" on error and computerID string otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetComputerID(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetComputerID_t		NSLGetComputerID_ptr;
	NSLFree_t				NSLFree_ptr;
	LIBHANDLE				nalpLib;
	int						retVal;
	char					*nslCompID;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslCompID = NULL;

	NSLGetComputerID_ptr = (NSLGetComputerID_t)
			NalpGetSym(nalpLib, "NSLGetComputerID");

	if (NSLGetComputerID_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetComputerID_ptr(&nslCompID);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslCompID != NULL)
		{
			NSLFree_ptr(nslCompID);
		}

		return retVal;
	}

	if (nslCompID == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslCompID);

	NSLFree_ptr(nslCompID);

    return retVal;
}

//Checks internet connection
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLTestConnection(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jint		joffset
)
{
	NSLTestConnection_t		        NSLTestConnection_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLTestConnection_ptr = (NSLTestConnection_t)
			NalpGetSym(nalpLib, "NSLTestConnection");

	if (NSLTestConnection_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLTestConnection_ptr();

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}
    return NALPJAVA_OK;
}


//returns "error" on error and hostname string otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetHostName(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetHostName_t		NSLGetHostName_ptr;
	NSLFree_t				NSLFree_ptr;
	LIBHANDLE				nalpLib;
	int						retVal;
	char					*nslHostname;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslHostname = NULL;

	NSLGetHostName_ptr = (NSLGetHostName_t)
			NalpGetSym(nalpLib, "NSLGetHostName");

	if (NSLGetHostName_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetHostName_ptr(&nslHostname);

	//Adjust for security offset
	retVal = retVal - (int)joffset;


	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslHostname != NULL)
		{
			NSLFree_ptr(nslHostname);
		}

		return retVal;
	}

	if (nslHostname == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslHostname);

	NSLFree_ptr(nslHostname);

    return retVal;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLeaseExpDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetLeaseExpDate_t		NSLGetLeaseExpDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*nslExpDate;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslExpDate = NULL;

	NSLGetLeaseExpDate_ptr = (NSLGetLeaseExpDate_t)
			NalpGetSym(nalpLib, "NSLGetLeaseExpDate");

	if (NSLGetLeaseExpDate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetLeaseExpDate_ptr(&nslExpDate);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslExpDate != NULL)
		{
			NSLFree_ptr(nslExpDate);
		}

		return retVal;
	}

	if (nslExpDate == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslExpDate);

	NSLFree_ptr(nslExpDate);

    return retVal;
}


//returns <0 on error otherwise expiration time in epoch secs
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLeaseExpSec(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jSec,
jintArray	jEpoch,
jint		joffset
)
{
	NSLGetLeaseExpSec_t			NSLGetLeaseExpSec_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					expSec;
	uint32_t					expEpoch;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetLeaseExpSec_ptr = (NSLGetLeaseExpSec_t)
			NalpGetSym(nalpLib, "NSLGetLeaseExpSec");

	if (NSLGetLeaseExpSec_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetLeaseExpSec_ptr(&expSec, &expEpoch);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jSec, 0, 1, (jint *)(&expSec));
	(*env)->SetIntArrayRegion(env, jEpoch, 0, 1, (jint *)(&expEpoch));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetRefreshExpDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetRefreshExpDate_t		NSLGetRefreshExpDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*nslExpDate;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslExpDate = NULL;

	NSLGetRefreshExpDate_ptr = (NSLGetRefreshExpDate_t)
			NalpGetSym(nalpLib, "NSLGetRefreshExpDate");

	if (NSLGetRefreshExpDate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetRefreshExpDate_ptr(&nslExpDate);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslExpDate != NULL)
		{
			NSLFree_ptr(nslExpDate);
		}

		return retVal;
	}

	if (nslExpDate == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslExpDate);

	NSLFree_ptr(nslExpDate);

    return retVal;
}


//returns <0 on error otherwise expiration time in epoch secs
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetRefreshExpSec(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jSec,
jintArray	jEpoch,
jint		joffset
)
{
	NSLGetRefreshExpSec_t			NSLGetRefreshExpSec_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					expSec;
	uint32_t					expEpoch;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetRefreshExpSec_ptr = (NSLGetRefreshExpSec_t)
			NalpGetSym(nalpLib, "NSLGetRefreshExpSec");

	if (NSLGetRefreshExpSec_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetRefreshExpSec_ptr(&expSec, &expEpoch);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jSec, 0, 1, (jint *)(&expSec));
	(*env)->SetIntArrayRegion(env, jEpoch, 0, 1, (jint *)(&expEpoch));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetSubExpDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetSubExpDate_t			NSLGetSubExpDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*nslExpDate;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslExpDate = NULL;

	NSLGetSubExpDate_ptr = (NSLGetSubExpDate_t)
			NalpGetSym(nalpLib, "NSLGetSubExpDate");

	if (NSLGetSubExpDate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetSubExpDate_ptr(&nslExpDate);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslExpDate != NULL)
		{
			NSLFree_ptr(nslExpDate);
		}

		return retVal;
	}

	if (nslExpDate == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslExpDate);

	NSLFree_ptr(nslExpDate);

    return retVal;
}


//returns <0 on error otherwise expiration time in epoch secs
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetSubExpSec(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jSec,
jintArray	jEpoch,
jint		joffset
)
{
	NSLGetSubExpSec_t			NSLGetSubExpSec_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					expSec;
	uint32_t					expEpoch;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetSubExpSec_ptr = (NSLGetSubExpSec_t)
			NalpGetSym(nalpLib, "NSLGetSubExpSec");

	if (NSLGetSubExpSec_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetSubExpSec_ptr(&expSec, &expEpoch);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jSec, 0, 1, (jint *)(&expSec));
	(*env)->SetIntArrayRegion(env, jEpoch, 0, 1, (jint *)(&expEpoch));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetMaintExpDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetMaintExpDate_t		NSLGetMaintExpDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*nslExpDate;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslExpDate = NULL;

	NSLGetMaintExpDate_ptr = (NSLGetMaintExpDate_t)
			NalpGetSym(nalpLib, "NSLGetMaintExpDate");

	if (NSLGetMaintExpDate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetMaintExpDate_ptr(&nslExpDate);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslExpDate != NULL)
		{
			NSLFree_ptr(nslExpDate);
		}

		return retVal;
	}

	if (nslExpDate == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslExpDate);

	NSLFree_ptr(nslExpDate);

    return retVal;
}


//returns <0 on error otherwise expiration time in epoch secs
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetMaintExpSec(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jSec,
jintArray	jEpoch,
jint		joffset
)
{
	NSLGetMaintExpSec_t			NSLGetMaintExpSec_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					expSec;
	uint32_t					expEpoch;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetMaintExpSec_ptr = (NSLGetMaintExpSec_t)
			NalpGetSym(nalpLib, "NSLGetMaintExpSec");

	if (NSLGetMaintExpSec_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetMaintExpSec_ptr(&expSec, &expEpoch);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jSec, 0, 1, (jint *)(&expSec));
	(*env)->SetIntArrayRegion(env, jEpoch, 0, 1, (jint *)(&expEpoch));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetTrialExpDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetTrialExpDate_t		NSLGetTrialExpDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*nslExpDate;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	nslExpDate = NULL;

	NSLGetTrialExpDate_ptr = (NSLGetTrialExpDate_t)
			NalpGetSym(nalpLib, "NSLGetTrialExpDate");

	if (NSLGetTrialExpDate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetTrialExpDate_ptr(&nslExpDate);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (nslExpDate != NULL)
		{
			NSLFree_ptr(nslExpDate);
		}

		return retVal;
	}

	if (nslExpDate == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, nslExpDate);

	NSLFree_ptr(nslExpDate);

    return retVal;
}


//returns <0 on error otherwise expiration time in epoch secs
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetTrialExpSec(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jSec,
jintArray	jEpoch,
jint		joffset
)
{
	NSLGetTrialExpSec_t			NSLGetTrialExpSec_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					expSec;
	uint32_t					expEpoch;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetTrialExpSec_ptr = (NSLGetTrialExpSec_t)
			NalpGetSym(nalpLib, "NSLGetTrialExpSec");

	if (NSLGetTrialExpSec_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetTrialExpSec_ptr(&expSec, &expEpoch);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jSec, 0, 1, (jint *)(&expSec));
	(*env)->SetIntArrayRegion(env, jEpoch, 0, 1, (jint *)(&expEpoch));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLicenseStatus(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jintret,
jint		joffset
)
{
	NSLGetLicenseStatus_t		NSLGetLicenseStatus_ptr;
	LIBHANDLE					nalpLib;
	int							licStat;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetLicenseStatus_ptr = (NSLGetLicenseStatus_t)
			NalpGetSym(nalpLib, "NSLGetLicenseStatus");

	if (NSLGetLicenseStatus_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetLicenseStatus_ptr(&licStat);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&licStat));

    return NALPJAVA_OK;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLicenseInfo(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jLicType,
jintArray	jActType,
jint		joffset
)
{
	NSLGetLicenseInfo_t			NSLGetLicenseInfo_ptr;
	LIBHANDLE					nalpLib;
	uint32_t					licType;
	uint32_t					actType;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetLicenseInfo_ptr = (NSLGetLicenseInfo_t)
			NalpGetSym(nalpLib, "NSLGetLicenseInfo");

	if (NSLGetLicenseInfo_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetLicenseInfo_ptr(&licType, &actType);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jLicType, 0, 1, (jint *)(&licType));
	(*env)->SetIntArrayRegion(env, jActType, 0, 1, (jint *)(&actType));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLicenseCode(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetLicenseCode_t			NSLGetLicenseCode_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*licenseCode;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	licenseCode = NULL;

	NSLGetLicenseCode_ptr = (NSLGetLicenseCode_t)
			NalpGetSym(nalpLib, "NSLGetLicenseCode");

	if (NSLGetLicenseCode_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetLicenseCode_ptr(&licenseCode);

	//Adjust for security offset
	retVal = retVal - (int)joffset;


	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (licenseCode != NULL)
		{
			NSLFree_ptr(licenseCode);
		}

		return retVal;
	}

	if (licenseCode == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, licenseCode);

	NSLFree_ptr(licenseCode);

    return retVal;
}


//returns <0 on error otherwise numeric timestamp (epoch sec)
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetTimeStamp(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jlongArray	jintret,
jint		joffset
)
{
	NSLGetTimeStamp_t		NSLGetTimeStamp_ptr;
	LIBHANDLE				nalpLib;
	//Java doesn't have signed ints but a uint64_t time stamp
	// will fit in a jlong for a while yet.
	uint64_t				timeStamp;
	int						retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetTimeStamp_ptr = (NSLGetTimeStamp_t)
			NalpGetSym(nalpLib, "NSLGetTimeStamp");

	if (NSLGetTimeStamp_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetTimeStamp_ptr(&timeStamp);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetLongArrayRegion(env, jintret, 0, 1, (jlong *)(&timeStamp));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric feature status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetFeatureStatus(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jfeatName,
jintArray	jintret,
jint		joffset
)
{
	NSLGetFeatureStatus_t		NSLGetFeatureStatus_ptr;
	LIBHANDLE					nalpLib;
	char						*featureName;
	int							featStat;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetFeatureStatus_ptr = (NSLGetFeatureStatus_t)
			NalpGetSym(nalpLib, "NSLGetFeatureStatus");

	if (NSLGetFeatureStatus_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jfeatName, &featureName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLGetFeatureStatus_ptr(featureName, &featStat);

	freeStrfromByteArray(nalpLib, featureName);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&featStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric feature status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLCheckoutFeature(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jfeatName,
jbyteArray	jlicenseNo,
jintArray	jintret,
jint		joffset
)
{
	NSLCheckoutFeature_t		NSLCheckoutFeature_ptr;
	LIBHANDLE					nalpLib;
	char						*featureName;
	char						*licenseNo;
	int							featStat;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLCheckoutFeature_ptr = (NSLCheckoutFeature_t)
			NalpGetSym(nalpLib, "NSLCheckoutFeature");

	if (NSLCheckoutFeature_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jfeatName, &featureName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, featureName);
		return retVal;
	}

	retVal = NSLCheckoutFeature_ptr(featureName, &featStat, licenseNo);

	freeStrfromByteArray(nalpLib, featureName);
	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&featStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric feature status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLReturnFeature(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jfeatName,
jbyteArray	jlicenseNo,
jint		joffset
)
{
	NSLReturnFeature_t			NSLReturnFeature_ptr;
	LIBHANDLE					nalpLib;
	char						*featureName;
	char						*licenseNo;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLReturnFeature_ptr = (NSLReturnFeature_t)
			NalpGetSym(nalpLib, "NSLReturnFeature");

	if (NSLReturnFeature_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jfeatName, &featureName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, featureName);
		return retVal;
	}

	retVal = NSLReturnFeature_ptr(featureName, licenseNo);

	freeStrfromByteArray(nalpLib, featureName);
	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric pool status and element amount
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetPoolStatus(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jpoolName,
jintArray	jpoolAmt,
jintArray	jintret,
jint		joffset
)
{
	NSLGetPoolStatus_t			NSLGetPoolStatus_ptr;
	LIBHANDLE					nalpLib;
	char						*poolName;
	int32_t						poolStat;
	uint32_t					poolAmt;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetPoolStatus_ptr = (NSLGetPoolStatus_t)
			NalpGetSym(nalpLib, "NSLGetPoolStatus");

	if (NSLGetPoolStatus_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jpoolName, &poolName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLGetPoolStatus_ptr(poolName, &poolAmt, &poolStat);

	freeStrfromByteArray(nalpLib, poolName);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jpoolAmt, 0, 1, (jint *)(&poolAmt));
	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&poolStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise pool status and element amoutn
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLCheckoutPool(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jpoolName,
jbyteArray	jlicenseNo,
jint		jpoolAmt,
jintArray	jintret,
jint		joffset
)
{
	NSLCheckoutPool_t			NSLCheckoutPool_ptr;
	LIBHANDLE					nalpLib;
	char						*poolName;
	char						*licenseNo;
	int32_t						poolStat;
	uint32_t					poolAmt;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLCheckoutPool_ptr = (NSLCheckoutPool_t)
			NalpGetSym(nalpLib, "NSLCheckoutPool");

	if (NSLCheckoutPool_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jpoolName, &poolName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, poolName);
		return retVal;
	}

	poolAmt = (uint32_t)jpoolAmt;

	retVal = NSLCheckoutPool_ptr(poolName, poolAmt, &poolStat, licenseNo);

	freeStrfromByteArray(nalpLib, poolName);
	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&poolStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise 0
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLReturnPool(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jpoolName,
jbyteArray	jlicenseNo,
jint		jpoolAmt,
jint		joffset
)
{
	NSLReturnPool_t				NSLReturnPool_ptr;
	LIBHANDLE					nalpLib;
	char						*poolName;
	char						*licenseNo;
	uint32_t					poolAmt;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLReturnPool_ptr = (NSLReturnPool_t)
			NalpGetSym(nalpLib, "NSLReturnPool");

	if (NSLReturnPool_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jpoolName, &poolName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, poolName);
		return retVal;
	}

	poolAmt = (uint32_t)jpoolAmt;

	retVal = NSLReturnPool_ptr(poolName, poolAmt, licenseNo);

	freeStrfromByteArray(nalpLib, poolName);
	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetLicense(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jlicenseNo,
jbyteArray	jxmlRegInfo,
jintArray	jintret,
jint		joffset
)
{
	NSLGetLicense_t			NSLGetLicense_ptr;
	LIBHANDLE				nalpLib;
	char					*licenseNo;
	char					*xmlRegInfo;
	int						licStat;
	int						retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetLicense_ptr = (NSLGetLicense_t)
			NalpGetSym(nalpLib, "NSLGetLicense");

	if (NSLGetLicense_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jxmlRegInfo, &xmlRegInfo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, licenseNo);
		return retVal;
	}

	retVal = NSLGetLicense_ptr(licenseNo, &licStat, xmlRegInfo);

	freeStrfromByteArray(nalpLib, licenseNo);
	freeStrfromByteArray(nalpLib, xmlRegInfo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&licStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLReturnLicense(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jlicenseNo,
jintArray	jintret,
jint		joffset
)
{
	NSLReturnLicense_t		NSLReturnLicense_ptr;
	LIBHANDLE				nalpLib;
	char					*licenseNo;
	int						licStat;
	int						retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLReturnLicense_ptr = (NSLReturnLicense_t)
			NalpGetSym(nalpLib, "NSLReturnLicense");

	if (NSLReturnLicense_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLReturnLicense_ptr(licenseNo, &licStat);

	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&licStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLImportCertificate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jcertContainer,
jbyteArray	jlicenseNo,
jintArray	jintret,
jint		joffset
)
{
	NSLImportCertificate_t		NSLImportCertificate_ptr;
	LIBHANDLE					nalpLib;
	char						*licenseNo;
	char						*certCont;
	int							licStat;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLImportCertificate_ptr = (NSLImportCertificate_t)
			NalpGetSym(nalpLib, "NSLImportCertificate");

	if (NSLImportCertificate_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jcertContainer, &certCont);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, certCont);
		return retVal;
	}

	retVal = NSLImportCertificate_ptr(licenseNo, &licStat, certCont);

	freeStrfromByteArray(nalpLib, certCont);
	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&licStat));

    return NALPJAVA_OK;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetActivationCertReq(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jlicenseNo,
jbyteArray	jxmlRegInfo,
jlongArray	jstrret,
jint		joffset
)
{
	NSLGetActivationCertReq_t		NSLGetActivationCertReq_ptr;
	LIBHANDLE						nalpLib;
	NSLFree_t						NSLFree_ptr;
	char							*xmlRegInfo;
	char							*licenseNo;
	char							*cert;
	int								retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	cert = NULL;

	NSLGetActivationCertReq_ptr = (NSLGetActivationCertReq_t)
			NalpGetSym(nalpLib, "NSLGetActivationCertReq");

	if (NSLGetActivationCertReq_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = getByteArraytoStr(env, jxmlRegInfo, &xmlRegInfo);

	if (retVal < 0)
	{
		freeStrfromByteArray(nalpLib, licenseNo);
		return retVal;
	}

	retVal = NSLGetActivationCertReq_ptr(licenseNo, xmlRegInfo, &cert);

	freeStrfromByteArray(nalpLib, licenseNo);
	freeStrfromByteArray(nalpLib, xmlRegInfo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	if (cert == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, cert);

	NSLFree_ptr(cert);

    return retVal;
}


//returns <0 on error otherwise numeric license status
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetDeactivationCertReq(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jlicenseNo,
jlongArray	jstrret,
jint		joffset
)
{
	NSLGetDeactivationCertReq_t		NSLGetDeactivationCertReq_ptr;
	LIBHANDLE						nalpLib;
	NSLFree_t						NSLFree_ptr;
	char							*licenseNo;
	char							*cert;
	int								retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	cert = NULL;

	NSLGetDeactivationCertReq_ptr = (NSLGetDeactivationCertReq_t)
			NalpGetSym(nalpLib, "NSLGetDeactivationCertReq");

	if (NSLGetDeactivationCertReq_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jlicenseNo, &licenseNo);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLGetDeactivationCertReq_ptr(licenseNo, &cert);

	freeStrfromByteArray(nalpLib, licenseNo);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	if (cert == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, cert);

	NSLFree_ptr(cert);

    return retVal;
}


//returns "error" on error and UDFValue otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetUDFValue(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	judfName,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetUDFValue_t			NSLGetUDFValue_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*udfName;
	char						*udfValue;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	udfName = NULL;

	NSLGetUDFValue_ptr = (NSLGetUDFValue_t)
			NalpGetSym(nalpLib, "NSLGetUDFValue");

	if (NSLGetUDFValue_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, judfName, &udfName);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLGetUDFValue_ptr(udfName, &udfValue);

	freeStrfromByteArray(nalpLib, udfName);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (udfValue != NULL)
		{
			NSLFree_ptr(udfValue);
		}

		return retVal;
	}

	if (udfValue == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, udfValue);

	NSLFree_ptr(udfValue);

    return retVal;
}


//returns <0 on error otherwise numeric timestamp (epoch sec)
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetNumbAvailProc(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jmaxProc,
jintArray	javailProc,
jint		joffset
)
{
	NSLGetNumbAvailProc_t		NSLGetNumbAvailProc_ptr;
	LIBHANDLE					nalpLib;
	unsigned int				maxProc;
	unsigned int				availProc;
	int							retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;

	NSLGetNumbAvailProc_ptr = (NSLGetNumbAvailProc_t)
			NalpGetSym(nalpLib, "NSLGetNumbAvailProc");

	if (NSLGetNumbAvailProc_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetNumbAvailProc_ptr(&maxProc, &availProc);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	if (retVal < 0)
	{
		return retVal;
	}

	(*env)->SetIntArrayRegion(env, jmaxProc, 0, 1, (jint *)(&maxProc));

	(*env)->SetIntArrayRegion(env, javailProc, 0, 1, (jint *)(&availProc));

    return NALPJAVA_OK;
}


//returns "error" on error and expiration date otherwise
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetNewLicenseCode(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jbyteArray	jprofile,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetNewLicenseCode_t		NSLGetNewLicenseCode_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*licenseCode;
	char						*profile;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	licenseCode = NULL;
	profile = NULL;

	NSLGetNewLicenseCode_ptr = (NSLGetNewLicenseCode_t)
			NalpGetSym(nalpLib, "NSLGetNewLicenseCode");

	if (NSLGetNewLicenseCode_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = getByteArraytoStr(env, jprofile, &profile);

	if (retVal < 0)
	{
		return retVal;
	}

	retVal = NSLGetNewLicenseCode_ptr(profile, &licenseCode);

	freeStrfromByteArray(nalpLib, profile);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (licenseCode != NULL)
		{
			NSLFree_ptr(licenseCode);
		}

		return retVal;
	}

	if (licenseCode == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, licenseCode);

	NSLFree_ptr(licenseCode);

    return retVal;
}


JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetPreset(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jint		messageNo,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetPreset_t				NSLGetPreset_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	char						*presetMsg;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	presetMsg = NULL;

	NSLGetPreset_ptr = (NSLGetPreset_t)
			NalpGetSym(nalpLib, "NSLGetPreset");

	if (NSLGetPreset_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NSLGetPreset_ptr((int)messageNo, &presetMsg);

	//Adjust for security offset
	retVal = retVal - (int)joffset;


	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if (presetMsg != NULL)
		{
			NSLFree_ptr(presetMsg);
		}

		return retVal;
	}

	if (presetMsg == NULL)
	{
		return NALPJAVA_LIB_MEMORY;
	}

	retVal = setByteArrayfromStr(env, jstrret, presetMsg);

	NSLFree_ptr(presetMsg);

    return retVal;
}


//We need to return a set of strings. Don't know how many or how long
// each might be. Can't use a 2d array as JNI requires a preset size. So
// create an ArrayList and send that back.  Unfortunately, the only way
// to do this is via the return value. So, we'll have to send the return
// status back as a paramter.
JNIEXPORT jobject JNICALL
Java_com_nalpeiron_nalplibrary_NSL_NSLGetMsgByDate(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jintArray	jintret,
jbyteArray	jstrret,
jint		joffset
)
{
	NSLGetMsgByDate_t			NSLGetMsgByDate_ptr;
	NSLFree_t					NSLFree_ptr;
	LIBHANDLE					nalpLib;
	int							retVal;
	int							i;
	char						*messages;
	char						*messPtr;
	jclass						arrayClass;
	jmethodID					midArrayInit;
	jmethodID					midArrayAdd;
	jobject						objArray;
	jboolean					jret;
	jstring						jmess;


	if (jLibHandle == 0)
	{
		retVal = NALPJAVA_LIBHANDLE_ERROR;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	nalpLib = (LIBHANDLE)(intptr_t)jLibHandle;
	messages = NULL;

	NSLGetMsgByDate_ptr = (NSLGetMsgByDate_t)
			NalpGetSym(nalpLib, "NSLGetMsgByDate");

	if (NSLGetMsgByDate_ptr == NULL)
	{
		retVal = NALPJAVA_SYMLOAD_ERROR;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	NSLFree_ptr = (NSLFree_t)NalpGetSym(nalpLib, "NSLFree");

	if (NSLFree_ptr == NULL)
	{
		retVal = NALPJAVA_SYMLOAD_ERROR;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	retVal = NSLGetMsgByDate_ptr(&messages);

	//Adjust for security offset
	retVal = retVal - (int)joffset;

	//if getstats failed free the mem and return
	//if retVal = 0 no messages to be displayed
	if (retVal <= 0)
	{
		if (messages != NULL)
		{
			NSLFree_ptr(messages);
		}

		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	if (messages == NULL)
	{
		retVal = NALPJAVA_LIB_MEMORY;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	arrayClass = (*env)->FindClass(env, "java/util/ArrayList");

	if (arrayClass == NULL)
	{
		retVal = NALPJAVA_JNI_CLASS;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	midArrayInit =  (*env)->GetMethodID(env, arrayClass, "<init>", "()V");

    if (midArrayInit == NULL)
	{
		retVal = NALPJAVA_JNI_METH;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	objArray = (*env)->NewObject(env, arrayClass, midArrayInit);

	if (objArray == NULL)
	{
		retVal = NALPJAVA_JNI_CONTOBJ;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	midArrayAdd = (*env)->GetMethodID(env,
			arrayClass, "add", "(Ljava/lang/Object;)Z");

	if (midArrayAdd == NULL)
	{
		retVal = NALPJAVA_JNI_METH;
		(*env)->SetIntArrayRegion(env, jintret, 0, 1, (jint *)(&retVal));
		return NULL;
	}

	messPtr = messages;

	for (i = 0; i < retVal; i++)
	{
		jmess = (*env)->NewStringUTF(env, messPtr);

		jret = (*env)->CallBooleanMethod(env, objArray, midArrayAdd, jmess);

		(*env)->DeleteLocalRef(env, jmess);

		messPtr = messPtr + strlen(messPtr) + 1;
	}

	retVal = setByteArrayfromStr(env, jstrret, messages);

	NSLFree_ptr(messages);

    return objArray;
}

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
