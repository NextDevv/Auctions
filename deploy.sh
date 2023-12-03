JAR="/home/giovanni/IdeaProjects/Auctions/target/Auctions-0.0.1.jar"
echo 'Building Auctions...'
mvn clean package

echo 'Deploying Auctions to survival-test-servers'

echo 'Uploading to survival...'
scp -P 25010 /home/giovanni/IdeaProjects/Auctions/target/Auctions-0.0.1.jar dev@88.198.53.19:~/survival-test-servers/test-spawn/plugins/

echo 'Uploading to dungeon...'
scp -P 25010 /home/giovanni/IdeaProjects/Auctions/target/Auctions-0.0.1.jar dev@88.198.53.19:~/survival-test-servers/test-dungeon/plugins/


echo 'Restarting survival...'
ssh -p 25010 dev@88.198.53.19 'screen -S test-spawn -X stuff "plugman reload Auctions^M"'
ssh -p 25010 dev@88.198.53.19 'screen -S test-spawn -X stuff "say Auctions reloaded^M"'
echo 'Restarting dungeon...'
ssh -p 25010 dev@88.198.53.19 'screen -S test-dungeon -X stuff "plugman reload Auctions^M"'
ssh -p 25010 dev@88.198.53.19 'screen -S test-dungeon -X stuff "say Auctions reloaded^M"'
