#!/bin/bash

if ! command -v trans 2>&1 >/dev/null
then
    echo "'trans' does not exist. Please install with 'brew install translate-shell'"
    exit 1
fi

if ! command -v jq 2>&1 >/dev/null
then
    echo "'jq' does not exist. Please install with 'brew install jq'"
    exit 1
fi

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <keyname> <english>"
    echo "Example: $0 'helloWorld' 'Hello, World!'"
    exit 1
fi

KEYNAME=$1
ENGLISH=$2


LANGUAGES[0]="de"
LANGUAGES[1]="es"
LANGUAGES[2]="fr"
LANGUAGES[3]="hi"
LANGUAGES[4]="it"
LANGUAGES[5]="ja"
LANGUAGES[6]="pl"
LANGUAGES[7]="pt"
LANGUAGES[8]="ru"
LANGUAGES[9]="tr"
LANGUAGES[10]="zh"

LANGUAGE_INPUT=$(IFS=+ ; echo "${LANGUAGES[*]}")

echo "Translating $ENGLISH to $LANGUAGE_INPUT"

TRANSLATIONS_SDTOUT=$(trans -brief :"${LANGUAGE_INPUT}" "$ENGLISH")

echo "Translations: $TRANSLATIONS_SDTOUT"

IFS=$'\n' read -rd '' -a TRANSLATIONS <<< "$TRANSLATIONS_SDTOUT"

add_translation () {
      LANGUAGE=$1
      KEYNAME=$2
      TRANSLATION=$3
      MACHINE_TRANSLATED=$4

      echo "Adding translation for ${LANGUAGE}"


      NEW_LINE="{ \"keyName\": \"$KEYNAME\", \"language\": \"${LANGUAGE}\", \"text\": \"${TRANSLATION}\" }"
      if [ "$MACHINE_TRANSLATED" = 'true' ]; then
          NEW_LINE="{ \"keyName\": \"$KEYNAME\", \"language\": \"${LANGUAGE}\", \"text\": \"${TRANSLATION}\", \"machineTranslated\": true }"
      fi

      echo "$NEW_LINE"

      LANGUAGEPATH="populate/src/main/resources/seed/i18n/${LANGUAGE}/languageTexts.json"

      # Add the new line to the file
      jq ". += [$NEW_LINE]" < "$LANGUAGEPATH"  > tmp.json && mv tmp.json "$LANGUAGEPATH"
}


for i in "${!LANGUAGES[@]}"; do
    if [ -z "${LANGUAGES[$i]}" ]; then
        continue
    fi

    add_translation "${LANGUAGES[$i]}" "$KEYNAME" "${TRANSLATIONS[$i]}" true
done

add_translation "en" "$KEYNAME" "$ENGLISH" false
add_translation "dev" "$KEYNAME" "dev_$ENGLISH" false
