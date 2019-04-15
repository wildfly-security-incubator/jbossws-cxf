package org.jboss.wsf.stack.cxf.client.configuration;


import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.security.WildflyClientConfigException;
import org.jboss.wsf.spi.security.WildflyClientSecurityConfigProvider;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;

import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

class WildflyClientConfigHelper {

    static ClientConfig getClientConfig(WildflyClientSecurityConfigProvider wildflyConfigProvider, BindingProvider port) throws WildflyClientConfigException, URISyntaxException {
        setSSLContext(wildflyConfigProvider, port);

        Map<String, String> props = new HashMap<>();
        setUsernameAndPasswordProperties(wildflyConfigProvider, port, props);
        return new ClientConfig(null, null, null, props, null);
    }

    private static void setUsernameAndPasswordProperties(WildflyClientSecurityConfigProvider wildflyConfigProvider, BindingProvider port, Map<String, String> props) throws WildflyClientConfigException, URISyntaxException {
        URI endpointURI = new URI(port.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString());
        String username = wildflyConfigProvider.getUsername(endpointURI);
        String password = wildflyConfigProvider.getPassword(endpointURI);
        if (username != null) {
            props.put(BindingProvider.USERNAME_PROPERTY, username);
        }
        if (password != null) {
            props.put(BindingProvider.PASSWORD_PROPERTY, password);
        }
    }

    private static void setSSLContext(WildflyClientSecurityConfigProvider wildflyConfigProvider, BindingProvider port) throws WildflyClientConfigException, URISyntaxException {
        SSLContext sslContext = wildflyConfigProvider.getSSLContext(new URI(port.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString()));

        //CXF API is required to set the TLS client params (and props.put(JAXWSProperties.SSLSocketFactory) does not work)
        Client client = ClientProxy.getClient(port);
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsParams = new TLSClientParameters();
        if (sslContext != null && sslContext.getSocketFactory() != null) {
//            tlsParams.setSslContext(sslContext);
            tlsParams.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        httpConduit.setTlsClientParameters(tlsParams);
    }
}
