#!/bin/bash -e

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

MyBase=$(pwd)
ProjectBaseDir=$1 #this must be an absolute path
ArtId=$2
Tag=$3

####Move to project directory####
cd ${ProjectBaseDir}

CR=$'\n'

#Add Implementation Vendor
prop=$prop'Implementation-Vendor: '
tmp='The Apache Software Foundation'$CR
prop=$prop$tmp

#Add GroupId : ArtifactId
prop=$prop'GroupId-ArtifactId: '
tmp='org.apache.datasketches:'$ArtId$CR
prop=$prop$tmp

# Add Branch
prop=$prop'Git-Branch: '
tmp=''$(git rev-parse --abbrev-ref HEAD)''$CR
prop=$prop$tmp

#Add commit-id
prop=$prop'Git-Commit-Id-Full: '
ID=$(git rev-parse HEAD)
tmp=''$ID''$CR
prop=$prop$tmp

#Add timestamp
prop=$prop'Git-Commit-Time: '
tmp=''$(git show --no-patch --no-notes --pretty='%cI' $ID)''$CR
prop=$prop$tmp

#Add user email
prop=$prop'Git-Commit-User-Email: '
tmp=''$(git show --no-patch --no-notes --pretty='%ce' $ID)''$CR
prop=$prop$tmp

#Add Tag
prop=$prop'Git-Commit-Tag: '
tmp=''$Tag''$CR
prop=$prop$tmp

echo "$prop"
