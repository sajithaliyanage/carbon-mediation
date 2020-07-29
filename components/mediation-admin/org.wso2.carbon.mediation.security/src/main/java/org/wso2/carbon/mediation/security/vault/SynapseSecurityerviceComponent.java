/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.security.vault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultConfigLoader;
import org.wso2.carbon.mediation.security.vault.external.ExternalVaultException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;

@Component(
        name = "mediation.security",
        immediate = true)
public class SynapseSecurityerviceComponent {

    private static Log log = LogFactory.getLog(SynapseSecurityerviceComponent.class);

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    public SynapseSecurityerviceComponent() {

    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Synapse mediation security component activated");
        }
        BundleContext bundleCtx = ctxt.getBundleContext();
        bundleCtx.registerService(MediationSecurityAdminService.class.getName(), new MediationSecurityAdminService(),
                null);
        try {
            SecureVaultLookupHandlerImpl.getDefaultSecurityService();
            ExternalVaultConfigLoader.loadExternalVaultConfigs(secretCallbackHandlerService);
        } catch (RegistryException e) {
            log.error("Error while activating secure vault registry component", e);
        } catch (ExternalVaultException e) {
            log.error("Error while loading external secure vault configurations", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService regService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        SecurityServiceHolder.getInstance().setRegistryService(regService);
    }

    protected void unsetRegistryService(RegistryService regService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        SecurityServiceHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "server.configuration",
            service = org.wso2.carbon.base.api.ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {

        SecurityServiceHolder.getInstance().setServerConfigurationService(serverConfiguration);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {

        SecurityServiceHolder.getInstance().setServerConfigurationService(null);
    }

    @Reference(
            name = "secret.callback.handler.service",
            service = org.wso2.carbon.securevault.SecretCallbackHandlerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService bound to the ESB initialization process");
        }
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService unbound from the ESB environment");
        }
        this.secretCallbackHandlerService = null;
    }
}
