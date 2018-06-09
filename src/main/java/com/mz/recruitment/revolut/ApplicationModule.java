package com.mz.recruitment.revolut;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mz.recruitment.revolut.server.ServerModule;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationModule.class);
    public static final String PROPERTIES_FILE_NAME = "application.properties";

    @Override
    protected void configure() {
        install(new ServerModule());
        Names.bindProperties(binder(), readProperties());
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            InputStream propertiesStream = ApplicationModule.class.getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILE_NAME);
            properties.load(propertiesStream);
            LOGGER.info("App starting with properties: {}", properties.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
