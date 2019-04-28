#!/bin/bash
##
## remove the old rspace and start node as validator in a fresh state
## usage ./startValidator.sh 
##

rm -rf ~/.rnode/rspace ~/.rnode/blockstore ~/.rnode/dagstorage ~/.rnode/tmp
rnode run -s \
      --required-sigs 0 \
      --thread-pool-size 5  \
      --validator-private-key \
      ee7a42c736d4da8395e0727c648e05519c7f4cf4d917f56f9249612353426b8f
 


