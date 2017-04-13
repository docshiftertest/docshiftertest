//
// nsajavafp.h
//
// Created July 19 2013
// R. D. Ramey
//
// nsajavafp.h : Function pointer typedefs for NSA libraries
//

#ifndef __NSAJAVAFP_H__
#define __NSAJAVAFP_H__

#include <stdint.h>

#include "josdefs.h"

//***********************************************
//*
//* NSA (Anaylitic) Routines
//*
//***********************************************
//
//Validate library against custID and prodID
typedef int (NALPJNIAPI *NSAValidateLibrary_t)(uint32_t custNo, uint32_t prodNo);

//Get library version nalpation
typedef int (NALPJNIAPI *NSAGetVersion_t)(char **version);

//Get computerID
typedef int (NALPJNIAPI *NSAGetComputerID_t)(char **compID);

//Get Hostname for NSA SOAP
typedef int (NALPJNIAPI *NSAGetHostName_t)(char **hostName);

//Application start analytics
typedef int (NALPJNIAPI *NSAApStart_t)
	(const char *username, const char *nsaClientData, uint32_t *transID);

//Application stop analytics
typedef int (NALPJNIAPI *NSAApStop_t)
	(const char *username, const char *nsaClientData, uint32_t *transID);

//Login Analytics
typedef int (NALPJNIAPI *NSALogin_t)
	(const char *username, const char *nsaClientData, uint32_t *transID);

//Logout Analytics
typedef int (NALPJNIAPI *NSALogout_t)
	(const char *username, const char *nsaClientData, uint32_t *transID);

//Feature start analytics
typedef int (NALPJNIAPI *NSAFeatureStart_t)
	(const char *username, const char *featureCode,
	 const char *nsaClientData, uint32_t *transID);

//Feature stop analytics
typedef int (NALPJNIAPI *NSAFeatureStop_t)
	(const char *username, const char *featureCode,
	 const char *nsaClientData, uint32_t *transID);

//Record exception info
typedef int (NALPJNIAPI *NSAException_t)
	(const char *username, const char *exceptionCode,
	 const char *description, const char *nsaClientData, uint32_t *transID);

//Record sytem info
typedef int (NALPJNIAPI *NSASysInfo_t)
	(const char *username, const char *applang, const char *version,
	 const char *edition, const char *build, const char *licenseStat,
	 const char *nsaClientData, uint32_t *transID);

//Send NSA cache file to server
typedef int (NALPJNIAPI *NSASendCache_t)
	(const char *username, uint32_t *transID);

//Set NSA privacy
typedef int (NALPJNIAPI *NSASetPrivacy_t)(unsigned short NSAPriv);

//Get NSA privacy 
typedef int (NALPJNIAPI *NSAGetPrivacy_t)();

//Get NSA usage statistics
typedef int (NALPJNIAPI *NSAGetStats_t)(char **nsaStats);

//Free memory allocated with nsa.
typedef int (NALPJNIAPI *NSAFree_t)(void *memptr);

//NSA get location info
typedef int (NALPJNIAPI *NSAGetLocation_t)();
#endif //__NSAJAVAFP_H__

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */

