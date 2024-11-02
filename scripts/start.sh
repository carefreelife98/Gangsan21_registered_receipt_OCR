#!/usr/bin/env bash

PROJECT_ROOT="/home/ec2-user/app"
JAR_FILE="$PROJECT_ROOT/gangsan21-registered-receipt.jar"

# EC2 힙 메모리 확인 결과 최대 530m 정도 사용 가능.
# 확인법:
# java -XX:+PrintFlagsFinal -version 2>&1 | grep -i -E 'heapsize|permsize|version'
# free -h
JAVA_OPTS="-Xmx512m"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)
if [ "$(lsof -t -i:11000)" ]; then
  kill "$(lsof -t -i:11000)"
fi

chmod +x $JAR_FILE

# build 파일 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사" >> $DEPLOY_LOG
cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE



# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java "$JAVA_OPTS" -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG
