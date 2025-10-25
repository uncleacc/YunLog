package io.github.uncleacc.yunlog.config;

import io.github.uncleacc.yunlog.entity.Category;
import io.github.uncleacc.yunlog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据初始化配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final CategoryRepository categoryRepository;
    
    @Bean
    public ApplicationRunner initData() {
        return args -> {
            // 检查是否已有分类数据
            if (categoryRepository.count() == 0) {
                log.info("初始化默认分类数据...");
                
                // 创建默认分类
                Category defaultCategory = new Category();
                defaultCategory.setName("默认分类");
                defaultCategory.setIcon("📝");
                defaultCategory.setColor("#FF9A76");
                defaultCategory.setIsDefault(true);
                categoryRepository.save(defaultCategory);
                
                // 创建其他常用分类
                Category workCategory = new Category();
                workCategory.setName("工作");
                workCategory.setIcon("💼");
                workCategory.setColor("#4CAF50");
                workCategory.setIsDefault(false);
                categoryRepository.save(workCategory);
                
                Category lifeCategory = new Category();
                lifeCategory.setName("生活");
                lifeCategory.setIcon("🏠");
                lifeCategory.setColor("#2196F3");
                lifeCategory.setIsDefault(false);
                categoryRepository.save(lifeCategory);
                
                Category travelCategory = new Category();
                travelCategory.setName("旅行");
                travelCategory.setIcon("✈️");
                travelCategory.setColor("#FF5722");
                travelCategory.setIsDefault(false);
                categoryRepository.save(travelCategory);
                
                log.info("默认分类数据初始化完成");
            } else {
                log.info("分类数据已存在，跳过初始化");
            }
        };
    }
}