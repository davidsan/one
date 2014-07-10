#! /bin/bash

### Paramètres par défaut
compile=n
suffix=

duration=43200
mail_addr=san@npa.lip6.fr
nb_simu=20
tick=0.1

buffer_size=5M
msg_size=1

###

usage="Usage : 00a <chemin the one> <nb hotes> [options]\n
Options :\n
 -c, --compile\t\tcompile le simulateur\n
 -s, --suffix\t\tchaîne de caractères ajoutée à la fin du dossier\n

 -d, --duration\t\tdurée d'une simulation (en secondes)\n
 -m, --mail_addr\tadresse mail pour la notification du statut des jobs OAR\n
 -n, --nb_simu\t\tnombre de simulations à exécuter\n
 -t, --tick\t\tintervalle entre les évènements (en secondes)\n

 -bs, --buffer_size\ttaille du cache (unité en k, M ou G)\n
 -ms, --msg_size\ttaille d'un message (unité en k, M ou G)\n
"

[[ $# -lt 2 ]] && echo -e $usage >&2 && exit 1

path_the_one=$1
nb_host=$2

# Parse des options
shift 2
while [[ "$1" != "" ]] ; do
    case $1 in
        -c|--compile)        compile=y;        shift 1;;
        -s|--suffix)         suffix=$2;        shift 2;;

        -d|--duration)       duration=$2;      shift 2;;
        -m|--mail_addr)      mail_addr=$2;     shift 2;;
        -n|--nb_simu)        nb_simu=$2;       shift 2;;
        -t|--tick)           tick=$2;          shift 2;;

        -bs|--buffer_size)   buffer_size=$2;   shift 2;;
        -ms|--msg_size)      msg_size=$2;      shift 2;;
        *) break;;
    esac
done

# Récupération du chemin absolu vers le dossier de script
launch_dir=$PWD

script_dir=`dirname "$0"`
cd $script_dir
script_dir=$PWD

# Récupération du chemin absolu vers the one, on va dans son dossier
cd $path_the_one
path_the_one=$PWD

# Ajout des bibliothèques nécessaires
for jar in $path_the_one/lib/*.jar; do
    echo $CLASSPATH | grep -q $jar
    [[ $? -ne 0 ]] && export CLASSPATH=$CLASSPATH:$jar
done

# Recompilation du simulateur si demandé
if [[ $compile == "y" ]] ; then
    echo -n "Compilation du simulateur..."
    seconds=`date +%s`
    oarsub -l "/nodes=1/core=1,walltime=100:0:0" "./compile.bat"
    seconds=`expr \`date +%s\` - $seconds`
    echo " finie en $seconds"s
fi

# Génère le dossier de simulation (création dossier, base.db, settings.txt)
cd $script_dir

mail=
date=`date +"%Y%m%dT%H%M%S"`
echo "Date : $date"
dir_name_root="results"

for i in `seq $nb_simu`; do
    [[ $i -lt 10 ]] && i=0$i
    unique_name="$date"_${nb_host}_$i

    dir_name=$path_the_one/$dir_name_root/$unique_name
    mkdir -p $dir_name

    settings_name=$dir_name/$unique_name.txt

    cat > $settings_name <<EOF
#
# Settings for DangerMovement
#

Scenario.name = $unique_name
Scenario.endTime = $duration
Scenario.updateInterval = $tick
Scenario.nrofHostGroups = 2 
# Attention !
# Si nrofHostsGroups vaut 1, la carte n'est plus affichée
# car plus aucun groupe de noeuds possède un mouvement
# qui utilise sur MapBasedMovement

# Wifi interface (802.11a or 802.11g with a stock antenna)
wifiInterface.type = SimpleBroadcastInterface
# Transmit speed of 54 Mbps = 6912kBps
wifiInterface.transmitSpeed = 6912k
# Range 100 m (330 ft) outdoors
wifiInterface.transmitRange = 100

# No signal interface for testing purpose
noSignalInterface.type= SimpleBroadcastInterface
noSignalInterface.transmitSpeed = 0
noSignalInterface.transmitRange = 0

## Group and movement model specific settings
# Common settings for all groups     
Group.nrofHosts = 0

# Applications
dangerApp.type = DangerApplication
# Message sending interval between two connected hosts
dangerApp.interval = 300
# Message size (bytes)
dangerApp.size = $msg_size

# group1 (pedestrians) specific settings
Group1.nrofHosts = $nb_host
Group1.bufferSize = $buffer_size
Group1.movementModel = DangerMovement
Group1.interface1 = btInterface
Group1.nrofInterfaces = 1
# Walking speeds
Group.speed = 1.5, 1.5
# Probability for the node to walk at the beginning
Group1.walkProb = 0.7
# Minimum time the walking node will walk
Group1.walkTime = 14500
# Probability for the node to be selfwarned
Group1.selfwarnedProb = 0.000001
# Probability to be prewarned
Group1.prewarnedProb = 0.8
# Percentage of nodes at evacuation center 
# required to end the simulation
Group1.maxselfwarnedProb = 0.9
# Probability to choose a random POI
Group1.randomPoi = 0.1
# Message router
Group1.router = EpidemicDeliverableRouter
# Nombre d'applications
Group1.nrofApplications = 1
Group1.application1 = dangerApp

## Message creation parameters
# How many event generators
Events.nrof = 1

# Road accident generator
Events1.class = AccidentGenerator
# Time to wait before the first accident
Events1.delay = 0
# Probability of accident per step of sim
Events1.accidentProb = 1.0
# Number of accidents to generate
Events1.nrofAccidents = 10


## Movement model settings
# seed for movement models' pseudo random number generator 
# (default = 0)
MovementModel.rngSeed = $RANDOM
# World's size for Movement Models
MovementModel.worldSize = 6000, 6000
# How long time to move hosts in the world before real simulation
MovementModel.warmup = 0


## Map based movement -movement model specific settings
MapBasedMovement.nrofMapFiles = 1
MapBasedMovement.mapFile1 = data/usantiago2.wkt


## Points of Interest for the evacuation centers
PointsOfInterestEvac.poiFile = data/usantiago2_pois.wkt


## Reports - all report names have to be valid report classes

# how many reports to load
Report.nrofReports = 2
# length of the warm up period (simulated seconds)
Report.warmup = 0
# default directory of reports (can be overridden per Report with output setting)
Report.reportDir = reports/
# ReportDB update rate (steps)
Report.updateRate = 1000
# Report location
Report.report1 = LocationReportCSV
# Report Time
Report.report2 = TimeReportDB
EOF

    # On n'envoie qu'un mail par rafale de simulations, pour le dernier job
    [ $i -eq $nb_simu ] && mail="--notify mail:$mail_addr"

    oarsub -l "/nodes=1/core=1,walltime=100:0:0" -O $dir_name/log_%jobid%.out -E $dir_name/log_%jobid%.err $mail "./01_simulation.sh $path_the_one $settings_name" &

done

