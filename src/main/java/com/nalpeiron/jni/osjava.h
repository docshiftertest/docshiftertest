//
// osjava.h
//
// Created Jan 19 2013
// R. D. Ramey
//
#ifndef _NSAJAVAP_H_
#define _NSAJAVAP_H_

//Android log tag
#define LTAG    "Nalpeiron"

//OS Specfic routines
//
#if defined (LINUX) || defined (DARWIN) || defined (ANDNDK)
#define LIBHANDLE	void*
#define LPTSTR		const char*
#if ! defined (DARWIN)
#define DLLNAME		"./FileMgmt"
#else
#define DLLNAME		"./libFileMgmt.dylib"
#endif	//!DARWIN
#else	//!(LINUX || DARWIN)
#define LIBHANDLE	HINSTANCE
#define uint64_t	unsigned long long
#define DLLNAME		L"./filechck.dll"
#include <Windows.h>
#endif

LIBHANDLE
NalpLibLoad(
const char	*libname
);

void *
NalpGetSym(
LIBHANDLE hFilechck,
const char *routine
);

void
NalpLibUnload(
LIBHANDLE	nalpLib
);

#endif // _NSAJAVAP_H_

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
