#!/bin/bash
#HADOOP VARIABLES START
export JAVA_HOME=/usr/lib/jvm/jdk1.7.0
export HADOOP_HOME=/opt/hadoop/hadoop
export HADOOP_INSTALL=/opt/hadoop/hadoop
export HADOOP_LOG_DIR=${HADOOP_HOME}/logs
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/lib/native
export HADOOP_OPTS="-Djava.library.path=$HADOOP_INSTALL/lib"
export PATH=$PATH:$HADOOP_HADOOP/bin
export PATH=$PATH:$HADOOP_INSTALL/bin
export PATH=$PATH:$HADOOP_INSTALL/sbin
export HADOOP_MAPRED_HOME=$HADOOP_INSTALL
export HADOOP_COMMON_HOME=$HADOOP_INSTALL
export HADOOP_HDFS_HOME=$HADOOP_INSTALL
export HBASE_HOME=/opt/pokus/hbase-0.90.4
export PATH=$PATH:$HBASE_HOME/bin
export YARN_HOME=$HADOOP_INSTALL
export NUTCH_HOME=/opt/pokus/nutch
export PATH=$NUTCH_HOME/runtime/deploy/bin:$JAVA_HOME/bin:$PATH
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/lib/native
export HADOOP_OPTS="-Djava.library.path=$HADOOP_INSTALL/lib"
#HADOOP VARIABLES END

echo "Starting java exec script $1"

if [ "$(id -un)" != "hadoop" ]
then
    echo user $USER
        echo "Starting with different user than hadoop" 
    if [ "$1" == "sudo" ]
    then
        echo >&2 "loop with sudo. End."
    else
        echo "starting exec sudo."
        exec sudo -i -u hadoop /bin/bash /opt/pokus/nutch/java_exec.sh sudo
    fi
    exit
fi

echo "PART_deset"

: <<'END'
lockdir=/var/tmp/nutch_java_exec.lockdir
if ! mkdir "$lockdir" 2>/dev/null
then
    echo >&2 "cannot acquire lock, giving up on $lockdir"
    exit 0
fi
trap 'rm -rf "$lockdir"; exit' INT TERM EXIT  # remove directory when script finishes
END

cd /opt/pokus/nutch
sudo ant &
wait %1
echo "Building Nutch completed"

echo "PART_tricet"

sudo chmod 777 runtime/deploy/apache-nutch-2.3-SNAPSHOT.job
sudo chmod 777 build/apache-nutch-2.3-SNAPSHOT.jar
cp build/apache-nutch-2.3-SNAPSHOT.jar /opt/pokus/nutch/runtime/local/lib/
cp build/apache-nutch-2.3-SNAPSHOT.jar /opt/hadoop/nutch/lib/
cp runtime/deploy/apache-nutch-2.3-SNAPSHOT.job /opt/hadoop/nutch/
wait
echo "Local copies finished"

echo "PART_sedesat"

sudo -i -u hadoop scp -p -B -q /opt/pokus/nutch/build/apache-nutch-2.3-SNAPSHOT.jar hadoop@slave:/opt/hadoop/nutch/lib
sudo -i -u hadoop scp -p -B -q /opt/pokus/nutch/build/apache-nutch-2.3-SNAPSHOT.jar hadoop@slave:/opt/pokus/nutch/runtime/local/lib
sudo -i -u hadoop scp -p -B -q /opt/pokus/nutch/runtime/deploy/apache-nutch-2.3-SNAPSHOT.job hadoop@slave:/opt/hadoop/nutch
wait
echo "Remote copies finished"

echo "PART_petasedmdesat"

cd /opt/hadoop/hadoop
./bin/hadoop dfs -rmr urls
./bin/hadoop dfs -mkdir urls
./bin/hadoop dfs -copyFromLocal /opt/hadoop/nutch/seed/all/rbb_seed.txt /user/hadoop/urls/rbb_seed.txt
./bin/hadoop dfs -copyFromLocal /opt/hadoop/nutch/seed/all/sav_seed.txt /user/hadoop/urls/sav_seed.txt
wait

echo "PART_devade"

echo "Starting java inject script"
echo $USER
cd /opt/hadoop/nutch
echo "PART_sto"
./bin/nutch inject urls -crawlId production_v01
