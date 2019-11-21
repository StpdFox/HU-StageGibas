#!/bin/bash

##############################################################################
# Bash script that makes it simple to:
#  1) create a urcap project in the _parent directory of the working directory_
#  2) and bundles it as a OSGi bundle directly ready for import in Universal 
#     Robots GUI PolyScope
##############################################################################

function getReleaseNumber() {
case $1 in
1)
	myapiversion=1.6.0
	myReleaseBuildNumber=1.6.0
;;
2)
	myapiversion=1.5.0
	myReleaseBuildNumber=1.5.0
;;
3)
	myapiversion=1.4.0
	myReleaseBuildNumber=1.4.0
;;
4)
	myapiversion=1.3.0
	myReleaseBuildNumber=1.3.0
;;
5)
	myapiversion=1.2.56
	myReleaseBuildNumber=1.2.56
;;
6)
	myapiversion=1.1.0
	myReleaseBuildNumber=1.1.0-69
;;
7)
	myapiversion=1.0.0
	myReleaseBuildNumber=1.0.0.30
;;
esac
}

mygroupid="com.yourcompany"
myartifactid="thenewapp"
myapiversion="1.6.0"
myReleaseBuildNumber=1.6.0


shell=""
groups=""
home=""

if [[ -z $1 ]] || [[ "$1" != "-t" ]]; then
	# open fd
	exec 3>&1
	# Store data to $VALUES variable
	VALUES=$(dialog --ok-label "Ok" \
		  --separator "¤" \
		  --backtitle "URCap Project Creator" \
		  --title "Project Configuration" \
		  --form "Create a new project" \
	15 60 0 \
		"GroupId:"     1 1	"$mygroupid" 		1 13 80 0 \
		"ArtifactID:"  2 1	"$myartifactid"  	2 13 80 0 \
		  --menu "API version:" \
	13 60 0 \
		1 "1.6.0 (PolyScope SW 3.9.0/5.3.0 or newer required)" \
		2 "1.5.0 (PolyScope SW 3.8.0/5.2.0 or newer required)" \
		3 "1.4.0 (PolyScope SW 3.7.0/5.1.0 or newer required)" \
		4 "1.3.0 (PolyScope SW 3.6.0/5.0.0 or newer required)" \
		5 "1.2.56 (PolyScope SW 3.5.0 or newer required)" \
		6 "1.1.0 (PolyScope SW 3.4.0 or newer required)" \
		7 "1.0.0 (PolyScope SW 3.3.0 or newer required)" \
	2>&1 1>&3)

	# close fd
	exec 3>&-

	# Inserting blank lines
	echo ""
	echo ""

	if [ -z "$VALUES" ]; then
		echo "Operation cancelled, nothing done..."
		exit 1
	fi


	IFS='¤' read -ra VAL_ARRAY <<< "$VALUES"

	if [ -z "${VAL_ARRAY[2]}" ]; then
		echo "Operation cancelled, nothing done..."
		exit 1
	fi

	mygroupid=${VAL_ARRAY[0]}
	myartifactid=${VAL_ARRAY[1]}
fi

mypackage+=$mygroupid
mypackage+="."
mypackage+="$myartifactid"

getReleaseNumber ${VAL_ARRAY[2]}

echo 'Building project with parameters:'
echo "  package:     $mypackage"
echo "  GroupId:     $mygroupid"
echo "  ArtifactID:  $myartifactid"
echo "  API:         $myapiversion"


mvn archetype:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=com.ur.urcap \
  -DarchetypeArtifactId=archetype \
  -DarchetypeVersion=1.6.1 \
  "-Dpackage=$mypackage.impl" \
  "-DgroupId=$mygroupid" \
  "-DartifactId=$myartifactid" \
  "-Dapiversion=$myapiversion" \
  "-DapiversionRelease=$myReleaseBuildNumber"

mv $myartifactid $mypackage
