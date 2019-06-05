#!/bin/bash
##
## refresh the libraries dependencies
## Assumptions:
## rchain libraries have been published as such:
## `sbt clean rholang/bnfc:clean rholang/bnfc:generate compile publishLocal node/stage`
## Script assumes it is being run from the ./script folder
## usage: cd ./scripts && ./refreshLibs.sh
##
ivy2=~/.ivy2/local/coop.rchain
lib=../core/lib


rm -f \
   $lib/casper_2.12.jar \
   $lib/comm_2.12.jar \
   $lib/crypto_2.12.jar  \
   $lib/models_2.12.jar  \
   $lib/rspace_2.12.jar \
   $lib/rholang_2.12.jar \
   $lib/shared_2.12.jar 

cp $ivy2/casper_2.12/0.1.0-SNAPSHOT/jars/casper_2.12.jar $lib
cp $ivy2/comm_2.12/0.1/jars/comm_2.12.jar ./$lib
cp $ivy2/crypto_2.12/0.1.0-SNAPSHOT/jars/crypto_2.12.jar $lib
cp $ivy2/models_2.12/0.1.0-SNAPSHOT/jars/models_2.12.jar $lib
cp $ivy2/rholang_2.12/0.1.0-SNAPSHOT/jars/rholang_2.12.jar $lib
cp $ivy2/rholang_2.12/0.1.0-SNAPSHOT/jars/rholang_2.12.jar $lib
cp $ivy2/shared_2.12/0.1/jars/shared_2.12.jar $lib

