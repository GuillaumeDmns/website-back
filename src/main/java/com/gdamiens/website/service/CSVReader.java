package com.gdamiens.website.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CSVReader<T> {

    private final Class<T> type;

    private final RestTemplate restTemplate;

    public CSVReader(Class<T> type) {
        this.type = type;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, List<T>> readFromUrl(String url, Function<T, String> groupingByGetter) {
        return restTemplate.execute(url, HttpMethod.GET, null, clientHttpResponse -> {
            InputStreamReader reader = new InputStreamReader(clientHttpResponse.getBody());
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withType(this.type)
                .withSeparator(';')
                .withIgnoreLeadingWhiteSpace(true)
                .withSkipLines(1)
                .build();
            return csvToBean.stream().collect(Collectors.groupingBy(groupingByGetter));
        });
    }

    public List<T> readFromUrl(String url) {
        return restTemplate.execute(url, HttpMethod.GET, null, clientHttpResponse -> {
            InputStreamReader reader = new InputStreamReader(clientHttpResponse.getBody());
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withType(this.type)
                .withSeparator(';')
                .withIgnoreLeadingWhiteSpace(true)
                .withSkipLines(1)
                .build();
            return csvToBean.stream().collect(Collectors.toList());
        });
    }
}
