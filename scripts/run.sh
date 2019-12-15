#/bin/bash

#set -x

SCRIPT_HOME=$(dirname $0)

if [ $# -ne 1 ]
then
   echo "One argument expected: <deployment-name>-<process-name>[-<language-name>[-{win32|o64}]], it must be a compiled executable in dab-bin directory"
   echo "Deployments and processes names are specified in dab.xml"
   echo "Language must be omitted for Java"
   exit 1
fi

PROCESS_PATH=$1
EXEC=(${PROCESS_PATH//-/ })
ARGC=${#EXEC[@]}

echo "Lancement de $PROCESS_PATH..."
 
DEPLOYMENT=${EXEC[0]}
if [[ ! "$DEPLOYMENT" =~ ^(isolated|mixed)$ ]]; then
   echo "Deployment must be in {isolated, mixed}, it's actually set to '$DEPLOYMENT'"
   exit 1
fi

PROCESS=${EXEC[1]}
if [[ ! "$PROCESS" =~ ^(sc|ihm1|ihm2|udt1|udt2|dab)$ ]]; then
   echo "Process must be in {sc, ihm1, ihm2, udt1, udt2, dab}, it's actually set to '$PROCESS'"
   exit 1
fi

PROCESS="${DEPLOYMENT}-${PROCESS}"

if (( ARGC == 2 )) ; then
   IMPL=java
else
   IMPL=${EXEC[2]}
   if (( ARGC == 3 )) ; then
      OS=linux
   else
      OS=${EXEC[3]}
   fi
fi

if [[ ! "$IMPL" =~ ^(c|cpp|java)$ ]]; then
   echo "Language must be in {c, cpp, java}, it's actually set to '$IMPL'"
   exit 1
fi

if [ "$IMPL" == "java" ]
then
   case $PROCESS in
      isolated-ihm1) xterm -title "IHM-1"     -geometry 100x30+-9+211 -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      isolated-ihm2) xterm -title "IHM-2"     -geometry 100x30--9+211 -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      isolated-udt1) xterm -title "UDT-1"     -geometry 100x30+-9-16  -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      isolated-udt2) xterm -title "UDT-2"     -geometry 100x30--9-16  -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      isolated-sc)   xterm -title "Banque"    -geometry 100x30-649-0  -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      mixed-sc)      xterm -title "Banque"    -geometry 100x30-649-0  -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      mixed-dab)     xterm -title "IHMs+UDTs" -geometry 100x30+-9-0   -hold -e ${SCRIPT_HOME}/run-java.sh $PROCESS & ;;
      *) echo "Unexpected deployment and process: '$PROCESS'" ; exit 1 ;;
   esac
else
   if [[ ! "$OS" =~ ^(linux|win32|o64)$ ]]; then
      echo "OS must be in {linux, win32, o64}, it's actually set to '$OS'"
      exit 1
   fi
   DistributeurLocation=45x30+0
   ControleurLocation=227x30+276 
   BanqueLocation=45x30-0
   case $PROCESS in
      isolated-ihm1) xterm -title "DAB-1 $IMPL"  -geometry ${DistributeurLocation}-16  -hold -e ${SCRIPT_HOME}/$OS.sh $PROCESS_PATH & ;;
      isolated-ihm2) xterm -title "DAB-2 $IMPL"  -geometry ${DistributeurLocation}-200 -hold -e ${SCRIPT_HOME}/$OS.sh $PROCESS_PATH & ;;
      isolated-udt1) xterm -title "UDT-1 $IMPL"  -geometry ${ControleurLocation}-16    -hold -e ${SCRIPT_HOME}/$OS.sh $PROCESS_PATH & ;;
      isolated-udt2) xterm -title "UDT-2 $IMPL"  -geometry ${ControleurLocation}-200   -hold -e ${SCRIPT_HOME}/$OS.sh $PROCESS_PATH & ;;
      isolated-sc)   xterm -title "Banque $IMPL" -geometry ${BanqueLocation}-16        -hold -e ${SCRIPT_HOME}/$OS.sh $PROCESS_PATH & ;;
      *) echo "Unexpected deployment and process: '$PROCESS'" ; exit 1 ;;
   esac
fi
