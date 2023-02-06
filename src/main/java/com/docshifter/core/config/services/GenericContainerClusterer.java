package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInContainerCondition;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * Checks cluster size in a generic container environment (e.g. standalone Docker).
 */
@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Conditional(IsInContainerCondition.class)
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class GenericContainerClusterer implements IContainerClusterer {
	@Override
	public Set<String> listOtherReplicas(String hostname) {
		if (NetworkUtils.getLocalHostName().equals(hostname)) {
			return Collections.emptySet();
		}
		return Collections.singleton(hostname);
	}
}
