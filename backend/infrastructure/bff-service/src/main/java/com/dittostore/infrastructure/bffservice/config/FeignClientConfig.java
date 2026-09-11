package com.dittostore.infrastructure.bffservice.config;

import feign.Client;
import feign.okhttp.OkHttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class FeignClientConfig {

    @Bean
    @Primary
    public Client feignClient(LoadBalancerClient loadBalancerClient,
                               LoadBalancerClientFactory loadBalancerClientFactory,
                               List<LoadBalancerFeignRequestTransformer> transformers) {
        Client delegate = new OkHttpClient();
        return new FeignBlockingLoadBalancerClient(delegate, loadBalancerClient,
                loadBalancerClientFactory, transformers);
    }
}