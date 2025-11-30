#!/bin/bash

# 云日记后端自动部署脚本
# 功能: 编译 -> 上传 -> 重启服务

set -e  # 遇到错误立即退出

# 配置项
SERVER_USER="root"
SERVER_HOST="101.200.84.91"
SERVER_PATH="/root/apps"
JAR_NAME="YunLog-0.0.1-SNAPSHOT.jar"
REMOTE_JAR_PATH="${SERVER_PATH}/${JAR_NAME}"

# 颜色输出
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  云日记后端自动部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 步骤1: 编译项目
echo -e "${GREEN}[步骤 1/4] 开始编译项目...${NC}"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 编译成功${NC}"
else
    echo -e "${RED}✗ 编译失败${NC}"
    exit 1
fi
echo ""

# 步骤2: 检查 JAR 包是否存在
echo -e "${GREEN}[步骤 2/4] 检查 JAR 包...${NC}"
if [ -f "target/${JAR_NAME}" ]; then
    JAR_SIZE=$(ls -lh "target/${JAR_NAME}" | awk '{print $5}')
    echo -e "${GREEN}✓ JAR 包存在: target/${JAR_NAME} (${JAR_SIZE})${NC}"
else
    echo -e "${RED}✗ JAR 包不存在${NC}"
    exit 1
fi
echo ""

# 步骤3: 上传 JAR 包到服务器
echo -e "${GREEN}[步骤 3/4] 上传 JAR 包到服务器...${NC}"
echo "目标: ${SERVER_USER}@${SERVER_HOST}:${SERVER_PATH}"

# 确保服务器目录存在
ssh ${SERVER_USER}@${SERVER_HOST} "mkdir -p ${SERVER_PATH}"

# 上传文件
scp target/${JAR_NAME} ${SERVER_USER}@${SERVER_HOST}:${REMOTE_JAR_PATH}
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 上传成功${NC}"
else
    echo -e "${RED}✗ 上传失败${NC}"
    exit 1
fi
echo ""

# 步骤4: 重启服务器上的应用
echo -e "${GREEN}[步骤 4/4] 重启服务器应用...${NC}"

ssh ${SERVER_USER}@${SERVER_HOST} << 'EOF'
    # 定义颜色
    GREEN='\033[0;32m'
    BLUE='\033[0;34m'
    RED='\033[0;31m'
    NC='\033[0m'
    
    # 配置
    JAR_NAME="YunLog-0.0.1-SNAPSHOT.jar"
    JAR_PATH="/root/apps/${JAR_NAME}"
    LOG_FILE="/root/apps/yunlog.log"
    PID_FILE="/root/apps/yunlog.pid"
    
    echo -e "${BLUE}>>> 在服务器上执行重启操作...${NC}"
    
    # 停止旧进程
    if [ -f "${PID_FILE}" ]; then
        OLD_PID=$(cat ${PID_FILE})
        if ps -p ${OLD_PID} > /dev/null 2>&1; then
            echo -e "${BLUE}>>> 停止旧进程 (PID: ${OLD_PID})...${NC}"
            kill ${OLD_PID}
            sleep 2
            
            # 强制停止
            if ps -p ${OLD_PID} > /dev/null 2>&1; then
                echo -e "${BLUE}>>> 强制停止...${NC}"
                kill -9 ${OLD_PID}
            fi
            echo -e "${GREEN}✓ 旧进程已停止${NC}"
        else
            echo -e "${BLUE}>>> 旧进程不存在，跳过${NC}"
        fi
    fi
    
    # 启动新进程
    echo -e "${BLUE}>>> 启动新进程...${NC}"
    nohup java -jar ${JAR_PATH} --spring.profiles.active=prod > ${LOG_FILE} 2>&1 &
    NEW_PID=$!
    echo ${NEW_PID} > ${PID_FILE}
    
    # 等待启动
    sleep 3
    
    # 检查进程状态
    if ps -p ${NEW_PID} > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 新进程启动成功 (PID: ${NEW_PID})${NC}"
        echo -e "${BLUE}>>> 日志文件: ${LOG_FILE}${NC}"
        echo -e "${BLUE}>>> 查看日志: tail -f ${LOG_FILE}${NC}"
    else
        echo -e "${RED}✗ 新进程启动失败${NC}"
        echo -e "${RED}>>> 最后 20 行日志:${NC}"
        tail -n 20 ${LOG_FILE}
        exit 1
    fi
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  部署完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${BLUE}服务器信息:${NC}"
    echo -e "  地址: http://${SERVER_HOST}:8080"
    echo -e "  日志: ssh ${SERVER_USER}@${SERVER_HOST} 'tail -f /root/apps/yunlog.log'"
    echo ""
else
    echo -e "${RED}✗ 重启失败${NC}"
    exit 1
fi
