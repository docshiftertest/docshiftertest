//
// nsljavafp.h
//
// Created July 19 2013
// R. D. Ramey
//
// NSLJAVAFP.h : Function pointer typedefs for NSL libraries
//

#ifndef __NSLJAVAFP_H__
#define __NSLJAVAFP_H__

#include <stdint.h>

#include "josdefs.h"


//***********************************************
//*
//* NSL (Licensing) Routines
//*
//***********************************************
//
// returns true if DLL matches customer number and last 5 digits of product ID
typedef int (NALPJNIAPI *NSLValidateLibrary_t)(uint32_t custID, uint32_t prodID);

// returns the version number as a 3 digit number i.e. 2.7.0 returns 270
typedef	int	(NALPJNIAPI *NSLGetVersion_t)(char	**version);

// returns computer identification string
typedef int	(NALPJNIAPI *NSLGetComputerID_t)(char **computerID);

//Return the domain name of the activation server
typedef int	(NALPJNIAPI *NSLGetHostName_t)(char **hostName);

//Checks server connectivity
typedef int	(NALPJNIAPI *NSLTestConnection_t)();

//Return number of seconds left in license
typedef int
	(NALPJNIAPI *NSLGetLeaseExpSec_t)(uint32_t *seconds, uint32_t *expEpoch);

//Return date of license expiration
typedef int	(NALPJNIAPI *NSLGetLeaseExpDate_t)(char	**date);

//Return number of seconds left in license
typedef int
	(NALPJNIAPI *NSLGetRefreshExpSec_t)(uint32_t *seconds, uint32_t *expEpoch);

//Return date of license expiration
typedef int	(NALPJNIAPI *NSLGetRefreshExpDate_t)(char	**date);

//Return number of seconds left in license
typedef int
	(NALPJNIAPI *NSLGetSubExpSec_t)(uint32_t *seconds, uint32_t *expEpoch);

//Return date of license expiration
typedef int	(NALPJNIAPI *NSLGetSubExpDate_t)(char	**date);

//Return number of seconds left in license
typedef int
	(NALPJNIAPI *NSLGetMaintExpSec_t)(uint32_t *seconds, uint32_t *expEpoch);

//Return date of license expiration
typedef int	(NALPJNIAPI *NSLGetMaintExpDate_t)(char	**date);

//Return number of seconds left in license
typedef int
	(NALPJNIAPI *NSLGetTrialExpSec_t)(uint32_t *seconds, uint32_t *expEpoch);

//Return date of license expiration
typedef int	(NALPJNIAPI *NSLGetTrialExpDate_t)(char	**date);

// returns the license status (replaces audit License)
typedef int	(NALPJNIAPI *NSLGetLicenseStatus_t)(int32_t *licStatus);

// returns the license type and activation method
typedef int	(NALPJNIAPI *NSLGetLicenseInfo_t)
		(uint32_t *licenseType, uint32_t *actType);

// returns the license status (replaces audit License)
typedef int	(NALPJNIAPI *NSLGetLicenseCode_t)(char **licCode);

//Gets current timestamp from daemon
typedef	int	(NALPJNIAPI *NSLGetTimeStamp_t)(uint64_t *timeStamp);

// Return license status and, if featureName != NULL return featurestatus
typedef int (NALPJNIAPI *NSLGetFeatureStatus_t)
		(const char *featureName, int32_t *featureStatus);

// Checkout a seat for a floating feature
typedef int (NALPJNIAPI *NSLCheckoutFeature_t)(const char *featureName,
		int32_t *featureStatus, const char *licenseNo);

// Return a seat for a floating feature
typedef int (NALPJNIAPI *NSLReturnFeature_t)
		(const char *featureName, const char *licenseNo);

typedef int (NALPJNIAPI *NSLGetPoolStatus_t)
		(const char *poolName, uint32_t *poolAmt, int32_t *poolStatus);

typedef int (NALPJNIAPI *NSLCheckoutPool_t)(const char *poolName,
		uint32_t poolAmt, int32_t *poolStatus, const char *licNo);

typedef int (NALPJNIAPI *NSLReturnPool_t)
		(const char *poolName, uint32_t poolAmt, const char *licNo);

//Gets new/updated license from daemon
typedef	int (NALPJNIAPI *NSLGetLicense_t)(const char *licenseNo,
		int32_t *licenseStatus, const char *xmlRegInfo);

//Gets new/updated license from daemon
typedef	int (NALPJNIAPI *NSLReturnLicense_t)
		(const char *licenseNo, int32_t *licenseStatus);

//Gets new/updated license from daemon
typedef	int	(NALPJNIAPI *NSLImportCertificate_t)
		(const char *licenseNo, int32_t *licenseStatus, char *certContainer);

//Gets new/updated license from daemon
typedef	int (NALPJNIAPI *NSLGetActivationCertReq_t)
		(const char *licenseNo, const char *xmlRegInfo, char **cert);

//Gets new/updated license from daemon
typedef	int (NALPJNIAPI *NSLGetDeactivationCertReq_t)
		(const char *licenseNo, char **cert);

// Return a User Defined Field value
typedef int	(NALPJNIAPI *NSLGetUDFValue_t)
		(const char *UDFName, char **UDFValue);

// Get number of process available to run on client
typedef int (NALPJNIAPI *NSLGetNumbAvailProc_t)
		(uint32_t *maxProc, uint32_t *availProc);

// Gets a license Code from the server (web services call)
typedef int	(NALPJNIAPI *NSLGetNewLicenseCode_t)
		(char *profile, char **licCode);

//Get preset messages from stamped library
typedef int	(NALPJNIAPI *NSLGetPreset_t)(int messNo, char **message);

//Get messages from license
typedef int	(NALPJNIAPI *NSLGetMsgByDate_t)(char **messages);

//Frees memory allocated in NSL library
typedef int	(NALPJNIAPI *NSLFree_t)(void *memptr);

#endif //__NSLJAVAFP_H__

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */

