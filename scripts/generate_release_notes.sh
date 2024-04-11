#!/bin/sh

# Check what versions demo and production are currently running
demoResponse=$(curl -s 'https://admin-d2p.ddp-dev.envs.broadinstitute.org/version')
demoGitTag=$(echo $demoResponse | jq -r '.gitTag')

prodResponse=$(curl -s 'https://juniper.terra.bio/version')
prodGitTag=$(echo $prodResponse | jq -r '.gitTag')

echo "Demo is currently running git tag \033[32m$demoGitTag\033[0m"
echo "Production is currently running git tag \033[32m$prodGitTag\033[0m"
echo "Generating release notes...\n"

# Pull the differing commits between the demo and production tags
# git log JSON formatter adapted from https://gist.github.com/textarcana/1306223
commits=$(git log $demoGitTag..$prodGitTag \
    --pretty=format:'{%n "message": "%s"%n},' \
    $@ | \
    perl -pe 'BEGIN{print "["}; END{print "]\n"}' | \
    perl -pe 's/},]/}]/')

messages=$(echo $commits | jq -r '.[].message')

# Generate the release notes markdown. This can be pasted directly into a Jira ticket
while IFS= read -r message; do
  if echo "$message" | grep -q -E 'JN-\d+'; then
    ticket=$(echo "$message" | grep -o -E 'JN-\d+')
    message=$(echo "$message" | tr -d '[]')
    echo "[${message}](https://broadworkbench.atlassian.net/browse/$ticket)"
  fi
done <<< "$messages"
