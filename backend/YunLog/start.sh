#!/bin/bash

# 云日记后端启动脚本

echo "=== 云日记后端启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装JDK 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

echo "1. 清理并编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "错误: 编译失败"
    exit 1
fi

echo "2. 启动Spring Boot应用..."
mvn spring-boot:run

echo "应用已启动，访问 http://localhost:8080/api/v1/health 检查状态"