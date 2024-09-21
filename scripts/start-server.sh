echo "-----------서버 배포시작----------"
cd /home/ubuntu/server-health
sudo fuser -k -n tcp 8080 || true
nohup java -jar project.jar > ./outlog 2>&1 &
echo "-----------서버 배포 끝-----------"