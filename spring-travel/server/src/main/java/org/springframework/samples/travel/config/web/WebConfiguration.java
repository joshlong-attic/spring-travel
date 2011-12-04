package org.springframework.samples.travel.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.js.ajax.AjaxUrlBasedViewResolver;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.samples.travel.domain.*;
import org.springframework.samples.travel.web.BookingFlowHandler;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;
import org.springframework.webflow.mvc.view.FlowAjaxTilesView;
import org.springframework.webflow.security.SecurityFlowExecutionListener;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Sets up all artifacts related to the web
 */
@Configuration
@EnableWebMvc
//@Import(RestConfiguration.class)
@ComponentScan({"org.springframework.samples.travel.rest", "org.springframework.samples.travel.web"})
public class WebConfiguration extends WebMvcConfigurerAdapter {

    private Class[] jaxbClasses = {Hotels.class, Bookings.class, Amenity.class, Booking.class, User.class, Hotel.class};

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // json support
        MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter = new MappingJacksonHttpMessageConverter();
        mappingJacksonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        converters.add(mappingJacksonHttpMessageConverter);

        // jaxb support
        MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter(this.marshaller());
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_XML));
        converters.add(converter);
    }

    @Bean
    public Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(this.jaxbClasses);
        return marshaller;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/users/login");
        registry.addViewController("/users/logout");
        registry.addViewController("/users/logoutSuccess");
        registry.addViewController("/").setViewName("home");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }


    @Inject
    private FlowExecutor flowExecutor;

    @Inject
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Bean(name = "hotels/booking")
    public BookingFlowHandler bookingFlowHandler() {
        return new BookingFlowHandler();
    }

    @Bean
    public AjaxUrlBasedViewResolver ajaxUrlBasedViewResolver() {
        AjaxUrlBasedViewResolver aubvr = new AjaxUrlBasedViewResolver();
        aubvr.setViewClass(FlowAjaxTilesView.class);
        return aubvr;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[]{"/WEB-INF*//**//*tiles.xml"});
        return tilesConfigurer;
    }

    @Bean
    public FlowHandlerMapping mapping() {
        FlowHandlerMapping flowHandlerMapping = new FlowHandlerMapping();
        flowHandlerMapping.setOrder(-1);
        flowHandlerMapping.setFlowRegistry(flowDefinitionRegistry);
        return flowHandlerMapping;
    }

    @Bean
    public FlowHandlerAdapter flowHandlerAdapter() {
        FlowHandlerAdapter fha = new FlowHandlerAdapter();
        fha.setFlowExecutor(flowExecutor);
        return fha;
    }

    @Bean
    public MvcViewFactoryCreator viewFactoryCreator() {
        MvcViewFactoryCreator mvcViewFactoryCreator = new MvcViewFactoryCreator();
        mvcViewFactoryCreator.setViewResolvers(Arrays.asList(ajaxUrlBasedViewResolver()));
        return mvcViewFactoryCreator;
    }


    @Bean
    public SecurityFlowExecutionListener securityFlowExecutionListener() {
        return new SecurityFlowExecutionListener();
    }
}
