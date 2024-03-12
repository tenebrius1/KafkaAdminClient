#!/bin/bash

kubectl create namespace kafka
kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
kubectl set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES="+UseKRaft" -n kafka
