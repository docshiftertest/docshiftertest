//
// josdefs.h
//
// Created Jan 19 2013
// R. D. Ramey
//

#ifndef _JOSDEFS_H_
#define _JOSDEFS_H_

#if defined (LINUX) || (ANDNDK)
#define		NALPJNIAPI
typedef char	*LPSTR;
#elif defined (DARWIN)
#define		NALPJNIAPI
typedef char	*LPSTR;
#else
#define		NALPJNIAPI		__cdecl
#endif

#endif // _JOSDEFS_H_
