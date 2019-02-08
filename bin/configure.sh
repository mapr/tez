#!/usr/bin/env bash
#set -x

# Copyright (c) 2009 & onwards. MapR Tech, Inc., All rights reserved

############################################################################
#
#
# Configure script for Tez
#
##
# This script is normally run by the core configure.sh to setup Tez during
# install. If it is run standalone, need to correctly initialize the
# variables that it normally inherits from the master configure.sh
#
############################################################################

MAPR_HOME="${BASEMAPR:-/opt/mapr}"
MAPR_ENABLE_LOGS="${MAPR_ENABLE_LOGS:-false}"


#
# Globals
#
TEZ_VERSION_FILE="$MAPR_HOME"/tez/tezversion
TEZ_VERSION=$(cat "$TEZ_VERSION_FILE")
TEZ_HOME="$MAPR_HOME"/tez/tez-"$TEZ_VERSION"
TEZ_BIN="$TEZ_HOME"/bin
TEZ_CONF="$TEZ_HOME"/conf
TEZ_SITE="$TEZ_CONF"/tez-site.xml

NOW=$(date "+%Y%m%d_%H%M%S")

#
# Checks if Tez has been already configured
#
is_tez_not_configured_yet(){
if [ -f "$TEZ_HOME/conf/.not_configured_yet" ]; then
  return 0; # 0 = true
else
  return 1;
fi
}

#
# Saves security flag to file "$TEZ_BIN"/isSecure
#
save_security_flag() {
# isSecure is set in server/configure.sh
if [ -n "$isSecure" ]; then
  echo "$isSecure" > "$TEZ_BIN"/isSecure
else
  if isSecurityEnabled 2>/dev/null; then
    echo true > "$TEZ_BIN"/isSecure
  else
    echo false > "$TEZ_BIN"/isSecure
  fi
fi
}

#
# Backup security flag to file "$TEZ_BIN"/isSecure.backup
#
backup_security_flag() {
if [ -f "$TEZ_BIN"/isSecure ] ; then
  cp "$TEZ_BIN"/isSecure "$TEZ_BIN"/isSecure.backup
else
  touch "$TEZ_BIN"/isSecure.backup
fi
}
#
# Returns true if security flag was changed comparing current and previous run of configure.sh.
# E.g. user switches off the security (security ON --> security OFF) or
# user turns on security (security OFF --> security ON) then method returns true
# and false otherwise. This method is used for triggering security related configuration
#
is_security_changed(){
security_backup=$(cat "$TEZ_BIN"/isSecure.backup)
current_security=$(cat "$TEZ_BIN"/isSecure)
if [ "$security_backup" = "$current_security" ] ; then
  return 1; # 1 = false
else
  return 0; # 0 = true
fi
}

#
# Returns boolean 'true' if security is custom.
#
is_custom_security(){
if [ -f "$MAPR_HOME/conf/.customSecure" ]; then
  return 0; # 0 = true
else
  return 1; # 1 = false
fi
}

#
# Checks whether there is a need to configure security.
# We have to configure security if security type was changed and it is not in custom format
# or if Tez was not configured yet.
#
#
# Checks whether there is a need to configure security.
# We have to configure security if security type was changed and it is not in custom format
# or if Tez was not configured yet.
#
security_has_to_be_configured(){
if ( is_security_changed || is_tez_not_configured_yet ) && ! is_custom_security ; then
  return 0; # 0 = true
else
  return 1; # 1 = false
fi
}

configure_security(){
TEZ_SITE="$1"
isSecure="$2"

if security_has_to_be_configured ; then
  . "$TEZ_BIN"/conftool -path "$TEZ_SITE" -security "$isSecure"
fi
}

#
#  Save off current configuration file
#
backup_configuration(){
if [ -d "$TEZ_CONF" ]; then
  cp -pa "$TEZ_CONF" "$TEZ_CONF"."$NOW"
fi
}

#
# Removes file $TEZ_HOME/conf/.not_configured_yet after first run of Tez configure.sh
#
remove_fresh_install_indicator(){
if is_tez_not_configured_yet ; then
  rm -f "$TEZ_HOME/conf/.not_configured_yet"
fi
}

#
# main
#
# typically called from core configure.sh
#

USAGE="Usage: $0 [options]
where options include:
    --secure|-s           configure MapR-SASL security cluster
    --unsecure|-u         configure MapR unsecure cluster
    --customSecure|-c     keep existing security configuration
    --help|-h             print help
    -EC <commonEcoOPts>   clusterwide options like TL, RM, ES
    -R                    Roles only"

if [ $# -gt 0 ]; then
  OPTS=$(getopt -a -o chsuC:R -l EC: -l help -l customSecure -l secure -l unsecure -l R -- "$@")
  if [ $? != 0 ] ; then
      echo -e "${USAGE}"
      return 2 2>/dev/null || exit 2
  fi
  eval set -- "$OPTS"

  while (( "$#" )); do
    case "$1" in
      --EC|-C)
        #Parse Common options
        #Ingore ones we don't care about
        ecOpts=("$2")
        shift 2
        restOpts="$@"
        eval set -- "${ecOpts[*]} --"
        while (( "$#" )); do
          case "$1" in
            --R|-R)
              shift 1;;
            --RM|-RM)
              rm_ip=$2;
              shift 2;;
            --TL|-TL)
              tl_ip=$2;
              shift 2;;
            --) shift
              break;;
              *)
              #echo "Ignoring common option $j"
              shift 1;;
          esac
        done
        shift 2
        eval set -- "$restOpts"
        ;;
      --R|-R)
        shift 1
        ;;
      --secure|-s)
          isSecure="true"
          shift 1;;
      --customSecure|-c)
          if is_tez_not_configured_yet ; then
            # If the file exist and our configure.sh is passed --customSecure, then we need to
            # translate this to doing what we normally do for --secure (best we can do)
            isSecure="true"
          else
            isSecure="custom"
          fi
          shift 1;;
      --unsecure|-u)
          isSecure="false"
          shift 1;;
      --help|-h)
          echo -e "${USAGE}"
          return 2 2>/dev/null || exit 2
          ;;
      --)
          shift 1;;
        *)
          echo "Unknown option $1"
          echo -e "${USAGE}"
          return 2 2>/dev/null || exit 2
          ;;
    esac
  done
fi

backup_configuration

backup_security_flag

save_security_flag

configure_security "$TEZ_SITE" "$isSecure"

remove_fresh_install_indicator
