#!/bin/bash

one=$1

GREEN='\033[0;32m'
RED='\033[0;31m'
ORANGE='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BUILD_PATH="bin/"
LIBRARY_PATH="lib/"
TOMCAT_PATH="$HOME/Documents/S2/Programmation/Tahina/Autre/apache-tomcat-10.1.28/"
PATH_FRAMEWORK="$HOME/Documents/S5/FrameWork/Sprint/TestFrameWork"
LIB_PATH_TEST_FRAMWORK="/lib"
LIB_PATH_SERVER="lib/"
JAR_NAME="mh-framework"

if [ "$one" = -1 ]; then
    echo -e "$RED\t...Fermeture du Tomcat$NC\n"
    "${TOMCAT_PATH}/bin/shutdown.sh" ; echo ""
    exit 1
fi

echo -e "$ORANGE\t...Lancement du Script\n$NC"

echo -e "$BLUE...compilation du code java$NC\n"

rm -rf "$BUILD_PATH"*

find . -name "*.java" > source.txt

if javac -d  "$BUILD_PATH/" -cp "$LIBRARY_PATH*" @source.txt; then
    echo -e "${GREEN}\tCompilation reussie${NC}\n"
else
    echo -e "${RED}\tCompilation Failed${NC}\n"
    exit 1;
fi

rm source.txt


echo -e "${BLUE}...Creation du fichier Jar${NC}\n"

cd "$BUILD_PATH" || exit 1

if jar -cvf "../$JAR_NAME.jar" ./*
    echo "" ; then
    echo -e "${GREEN}\Creation de jar Reussie${NC}\n"
else
    echo -e "${RED}\tErreur de lors du création du jar${NC}\n"
    exit 1;
fi

cd ..

echo -e "${BLUE}...Copie de jar sur le serveur Tomcat${NC}\n"

if cp -f "$JAR_NAME.jar" "$TOMCAT_PATH$LIB_PATH_SERVER" ; then
    echo -e "${GREEN}\tCopie Reussie${NC}\n"
else
    echo -e "${RED}Copie Failed${NC}\n"
    exit 1;
fi

echo -e "${BLUE}...Copie de jar sur le Test FrameWork  Tomcat${NC}\n"

if mv -f "$JAR_NAME.jar" "$PATH_FRAMEWORK$LIB_PATH_TEST_FRAMWORK" ; then
    echo -e "${GREEN}\tCopie Reussie${NC}\n"
else
    echo -e "${RED}Copie Failed${NC}\n"
    exit 1;
fi

if pgrep -f tomcat > /dev/null; then
    if [ -n "$one" ]; then
        if [ "$one" = 0 ]; then 
            echo -e "$RED\t...Fermeture du Tomcat$NC\n"
            "${TOMCAT_PATH}/bin/shutdown.sh" ; echo ""
        else
            echo -e "${RED}Paramatre introuvable${NC}\n"
        fi 
    fi
else
    echo -e "$RED\t...Lancement du Tomcat\n$NC"
    "${TOMCAT_PATH}/bin/startup.sh" ; echo ""
fi

echo -e "$ORANGE\t...Fin du script$NC"