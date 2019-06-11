#!/bin/bash
##
## remove the old rspace and start node as validator in a fresh state
## usage ./startValidator.sh 
##
echo $VALIDATOR_KEY
rm -rf ~/.rnode/rspace ~/.rnode/blockstore ~/.rnode/dagstorage ~/.rnode/tmp
rnode run -s \
      --required-sigs 0 \
      --thread-pool-size 5  \
      --store-type v2 \
      --validator-private-key \
      $VALIDATOR_KEY
