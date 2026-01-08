package com.greenhouse.discovery.config;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class EurekaServerInitConfig {

    @Autowired(required = false)
    private ApplicationInfoManager applicationInfoManager;

    @PostConstruct
    public void init() {
        if (applicationInfoManager != null) {
            // Set instance status to UP
            applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        }
    }
}
