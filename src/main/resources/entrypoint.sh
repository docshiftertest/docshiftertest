#!/bin/bash
#set -o errexit
echo "im here"

java_vm_parameters=""


if [ -n "${JAVA_VM_PARAMETERS}" ]; then
  java_vm_parameters=${JAVA_VM_PARAMETERS}
fi

if [ "${DS_Component}" = 'DocShifterConsole' ]; then
  echo "console"
  mkdir -p /opt/DocShifter/data/logs/console/${HOSTNAME}
  ln -s /opt/DocShifter/data/logs/console/${HOSTNAME} /opt/DocShifter/console/logs
  java ${java_vm_parameters} -classpath ".:/opt/DocShifter/console:/opt/DocShifter/console/lib:/opt/DocShifter/console/lib/*:/opt/DocShifter/beans/lib:/opt/DocShifter/beans/lib/*:" com.docshifter.console.DocShifterConsole
elif [ "${DS_Component}" = 'DocShifterReceiver' ]; then
  echo "receiver"  
  echo "Log folder creation"
  mkdir -p /opt/DocShifter/data/logs/receiver/${HOSTNAME}
  ln -s /opt/DocShifter/data/logs/receiver/${HOSTNAME} /opt/DocShifter/receiver/logs
  echo "Adding fonts"
  ln -s /opt/DocShifter/dependencies/fonts /usr/share/fonts
  fc-cache -f -v
  echo "Done adding fonts"
  if [ -n "${DS_LICENSE_CODE}" ]; then
  echo "Setting license code"
    echo ${DS_LICENSE_CODE} > /opt/DocShifterLicensing/DSLicenseCode.txt
  fi
  java ${java_vm_parameters} -Djava.library.path=./opt/DocShifter/beans/lib -classpath ".:/opt/DocShifter/receiver:/opt/DocShifter/receiver/lib:/opt/DocShifter/receiver/lib/*:/opt/DocShifter/beans/lib:/opt/DocShifter/beans/lib/*:/opt/Docshifter/dctm/dctm.jar:/opt/Docshifter/dctm_cfg/:/opt/DocShifter/dependencies/modules/receiver:/opt/DocShifter/dependencies/modules/receiver/*:" com.docshifter.receiver.DocShifterReceiver
elif [ "${DS_Component}" = 'DocShifterSender' ]; then
  echo "sender"
  mkdir -p /opt/DocShifter/data/logs/sender/${HOSTNAME}
  ln -s /opt/DocShifter/data/logs/sender/${HOSTNAME} /opt/DocShifter/sender/logs
  java ${java_vm_parameters} -classpath ".:/opt/DocShifter/sender:/opt/DocShifter/sender/lib:/opt/DocShifter/sender/lib/*:/opt/DocShifter/beans/lib:/opt/DocShifter/beans/lib/*:/opt/DocShifter/dependencies/modules/sender:/opt/DocShifter/dependencies/modules/sender/*:/opt/Docshifter/dctm/dctm.jar:/opt/Docshifter/dctm_cfg/:" com.docshifter.sender.DocShifterSender
else
  exec "$@"
fi