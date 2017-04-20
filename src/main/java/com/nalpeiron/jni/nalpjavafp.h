//
// nalpjavafp.h
//
// Created July 19 2013
// R. D. Ramey
//
// nalpjavafp.h : Function pointer typedefs for NSA/NSL libraries
//

#ifndef __NALPJAVAFP_H__
#define __NALPJAVAFP_H__

#include "josdefs.h"

//***********************************************
//*
//* Generic routines available to NSA and NSL
//*
//***********************************************
//
//Opens and initializes the library
typedef int (NALPJNIAPI *NalpLibOpen_t)(const char *xmlParms);

//Shutdown and close library
typedef int (NALPJNIAPI *NalpLibClose_t)();

//Get a descriptive error message associated with a negatvie
// errno.
typedef int (NALPJNIAPI *NalpGetErrorMsg_t)(int nalpErrNo, char **errMsg);


#endif //__NALPJAVAFP_H__

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */

