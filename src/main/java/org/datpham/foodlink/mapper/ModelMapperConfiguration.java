package org.datpham.foodlink.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {

    // TODO: Customize mappings if you use ModelMapper.
    @Bean
    public ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}
