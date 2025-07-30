#!/usr/bin/bash                             
                                            
if [ -z $1 ]                                
then                                        
  BACKUP=hr_$(printf '%(%Y%m%d)T' -1).bak 
else                                        
  BACKUP=$1                               
fi                                          
echo "Restoring $BACKUP with full DB recreation..."
echo "Droping database HR"
docker exec -i hreasypg sh -c 'dropdb --force -U hr hr'

echo "Creating empty database HR"
docker exec -i hreasypg sh -c 'createdb -U hr hr'

echo "Restore dump"
docker exec -i hreasypg sh -c 'psql hr -U hr' < $BACKUP
