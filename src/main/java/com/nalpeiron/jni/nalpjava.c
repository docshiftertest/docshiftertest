//
// nsajava.c
//
// Created Jan 19 2013
// R. D. Ramey
//
// nsajava.c : Defines the exported functions for the DLL application.
//

#if ! defined (LINUX) && ! defined (DARWIN) && ! defined (ANDNDK)
#include <Windows.h>
#else
#include <stdio.h>
#include <dlfcn.h>
#include <string.h>
#if ! defined (ANDNDK)
#include <jni_md.h>
#endif
#endif

#include <stdint.h>

#include "josdefs.h"
#include "osjava.h"
#include "nalpjava.h"
#include "nalpjavafp.h"
#include "nsajavafp.h"
#include "nalpJavaReturns.h"

//***********************************************
//*
//* Generic routines available to NSA and NSL
//*
//***********************************************

//Open and initilize library
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NALP_NalpLibOpen(
JNIEnv		*env,
jobject 	jobj,
jbyteArray	jFilename,
jbyteArray	jxmlParams,
jlongArray	libHandle
)
{
	NalpLibOpen_t		NalpLibOpen_ptr;
	void				*nalpLib;
	jboolean			isCopy;
	jbyte				*filename;
	jbyte				*xmlParams;
	int					retVal;
	int					tmpLen;


	filename = (*env)->GetByteArrayElements(env, jFilename, &isCopy);

	//	Load the nalpLib DLL
	nalpLib = NalpLibLoad((char *)filename);

	(*env)->ReleaseByteArrayElements(env,
				jFilename, filename, JNI_ABORT);

	//	If that didn't work, return an error
	if (nalpLib == NULL)
	{
//		__android_log_print(ANDROID_LOG_INFO, "Nalp",
//			"libopen failed with %s", dlerror());

		return NALPJAVA_LIBLOAD_ERROR;
	}

	NalpLibOpen_ptr = (NalpLibOpen_t)NalpGetSym(nalpLib, "NalpLibOpen");

	if (NalpLibOpen_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	xmlParams = (*env)->GetByteArrayElements(env, jxmlParams, &isCopy);

	retVal = NalpLibOpen_ptr((char *)xmlParams);

	(*env)->ReleaseByteArrayElements(env,
				jxmlParams, xmlParams, JNI_ABORT);

	if (retVal < 0)
	{
		return retVal;
	}

	//Set the libHandle
	(*env)->SetLongArrayRegion(env, libHandle, 0, 1, (jlong *)(&nalpLib));

	return NALPJAVA_OK;
}


//Shutdown and close library
JNIEXPORT jint JNICALL
Java_com_nalpeiron_nalplibrary_NALP_NalpLibClose(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle
)
{
	NalpLibClose_t	NalpLibClose_ptr;
	void *		nalpLib;
	jboolean		isCopy;
	int				retVal;


	if (jLibHandle == 0)
	{
		return NALPJAVA_LIBHANDLE_ERROR;
	}

	nalpLib = (void *)(intptr_t)jLibHandle;

	NalpLibClose_ptr = (NalpLibClose_t)
				NalpGetSym(nalpLib, "NalpLibClose");

	if (NalpLibClose_ptr == NULL)
	{
		return NALPJAVA_SYMLOAD_ERROR;
	}

	retVal = NalpLibClose_ptr();

	//	Unload the nalpLib DLL
	NalpLibUnload(nalpLib);

	jLibHandle = (jlong)0;

	//	Return the value
	return NALPJAVA_OK;
}


JNIEXPORT jstring JNICALL
Java_com_nalpeiron_nalplibrary_NALP_NalpGetErrorMsg(
JNIEnv		*env,
jobject		jobj,
jlong		jLibHandle,
jint		jnalpErrNo
)
{
	NalpGetErrorMsg_t	NalpGetErrorMsg_ptr;
	NSAFree_t			NSAFree_ptr;
	void *				nalpLib;
	jboolean			isCopy;
	jbyte				*shaErrMsg;
	jstring				jerrMsg;
	int					retVal;


	if (jLibHandle == 0)
	{
		jerrMsg = (*env)->NewStringUTF(env, "Library Load error\n");
		return jerrMsg;
	}

	nalpLib = (void *)(intptr_t)jLibHandle;

	NalpGetErrorMsg_ptr = (NalpGetErrorMsg_t)
			NalpGetSym(nalpLib, "NalpGetErrorMsg");

	if (NalpGetErrorMsg_ptr == NULL)
	{
		jerrMsg = (*env)->NewStringUTF(env, "Symbol Load error\n");
		return jerrMsg;
	}

	retVal = NalpGetErrorMsg_ptr((int)jnalpErrNo, (char **)&shaErrMsg);

	NSAFree_ptr = (NSAFree_t)
			NalpGetSym(nalpLib, "NSAFree");

	//if getstats failed free the mem and return
	if (retVal < 0)
	{
		if ((shaErrMsg != NULL) && (NSAFree_ptr != NULL))
		{
			NSAFree_ptr(shaErrMsg);
		}

		jerrMsg = (*env)->NewStringUTF(env, "Error\n");
		return jerrMsg;
	}

	jerrMsg = (*env)->NewStringUTF(env, (char *)shaErrMsg);

	NSAFree_ptr(shaErrMsg);

    return jerrMsg;
}

#if defined (ANDNDK)

void *
NalpGetSym(
LIBHANDLE	nalpLib,
const char	*routine
)
{
	return (void *)dlsym(nalpLib, routine);
}

#endif


/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
