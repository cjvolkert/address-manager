package com.sap.cloud.s4hana.examples.addressmgr.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sap.cloud.sdk.cloudplatform.cache.CacheKey;
import com.sap.cloud.sdk.s4hana.datamodel.odata.helper.Order;
import org.slf4j.Logger;

import java.util.List;

import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.s4hana.connectivity.CachingErpCommand;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessPartnerService;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class GetAllBusinessPartnersCommand extends CachingErpCommand<List<BusinessPartner>> {

    private static final Logger logger = CloudLoggerFactory.getLogger(GetAllBusinessPartnersCommand.class);

    private static final String CATEGORY_PERSON = "1";

    private static final Cache<CacheKey, List<BusinessPartner>> cache
            = CacheBuilder.newBuilder()
                    .maximumSize(50)
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build();

    private final BusinessPartnerService service;

    @Override
    protected Cache<CacheKey, List<BusinessPartner>> getCache() {
        return cache;
    }

    @Override
    protected CacheKey getCommandCacheKey() {
        return CacheKey.ofTenantIsolation();
    }

    @Override
    protected List<BusinessPartner> getFallback() {
        return Collections.emptyList();
    }

    public GetAllBusinessPartnersCommand(final BusinessPartnerService service) {
        super(GetAllBusinessPartnersCommand.class);
        this.service = service;
    }

    @Override
    protected List<BusinessPartner> runCacheable() throws Exception {
        return service.getAllBusinessPartner()//
                .select(BusinessPartner.BUSINESS_PARTNER, BusinessPartner.LAST_NAME, BusinessPartner.FIRST_NAME)
                .filter(BusinessPartner.BUSINESS_PARTNER_CATEGORY.eq(CATEGORY_PERSON))
                .orderBy(BusinessPartner.LAST_NAME, Order.DESC)
                .execute();
    }
}
