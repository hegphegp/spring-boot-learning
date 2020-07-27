package com.condingfly.config.es;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ElasticConfig {
    // 环境变量注入成数组集合
    @Value("#{'${config.es.hosts}'.split(',')}")
    public List<String> hosts;
    @Value("${config.es.username}")
    public String username;
    @Value("${config.es.password}")
    public String password;

    public HttpHost[] configHosts(List<String> hosts) {
        HttpHost[] httpHosts = new HttpHost[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            //去掉前后空格和内容的空格
            String host = hosts.get(i).trim().replace(" ", "");
            httpHosts[i] = HttpHost.create(host);

        }
        return httpHosts;
    }

    @Bean
    public RestClientBuilder restClientBuilder() {
        RestClientBuilder restClientBuilder=RestClient.builder(configHosts(hosts));
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        restClientBuilder.setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider));
        return restClientBuilder;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }
}