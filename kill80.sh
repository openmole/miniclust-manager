#!/bin/sh

# Trouver les PID des processus Java contenant "miniclust"
pids=$(jcmd | grep -i miniclust | awk '{print $1}')

if [ -z "$pids" ]; then
  echo "Aucun processus contenant 'miniclust' trouvé."
  exit 0
fi

echo "Processus trouvés : $pids"
echo "Arrêt en cours..."

for pid in $pids; do
  echo "Killing PID $pid"
  kill -9 "$pid"
done
