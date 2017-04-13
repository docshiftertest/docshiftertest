//
// osjava.c
//
// Created July 19 2013
// R. D. Ramey
//
// osjava.c : OS Specific routines for library
//

#if ! defined (LINUX) && ! defined (DARWIN)
#include <windows.h>
#else
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <jni_md.h>
#endif

#include <stdio.h>

#include "osjava.h"


//***********************************************
//*
//* OS Specific Routines
//*
//***********************************************

LIBHANDLE
NalpLibLoad(
const char	*libname
)
{
	void	*handle;


#if defined (LINUX) || defined (DARWIN)
	fprintf(stdout, "calling dlopen on %s\n", libname);
	handle = dlopen(libname, RTLD_LOCAL|RTLD_LAZY);
	if (handle == NULL)
	{
		fprintf(stdout, "Error opening library: %s\n", dlerror());
	}

	return handle;
#else
	handle = LoadLibraryA(libname);

	if (handle == NULL)
	{
		status = GetLastError();
		fprintf(stdout, "libname is %s\n", libname);
		fprintf(stdout, "Windows error from LoadLibrary is %d\n", status);
		fflush(stdout);
	}

	return (LIBHANDLE)handle;
#endif
}


void *
NalpGetSym(
LIBHANDLE		nalpLib,
const char		*routine
)
{
#if defined (LINUX) || defined (DARWIN)
	return (void *)dlsym(nalpLib, routine);
#else
	return (void *)GetProcAddress(nalpLib, routine);
#endif
}


void
NalpLibUnload(
LIBHANDLE		nalpLib
)
{
#if defined (LINUX) || defined (DARWIN)
	dlclose(nalpLib);
#else
	FreeLibrary(nalpLib);
#endif
}


/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
