package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInGenericContainerCondition;
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
@Conditional(IsInGenericContainerCondition.class)
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class GenericContainerChecker implements IContainerChecker {
	@Override
	public Set<String> checkReplicas(int maxReplicas) {
		return Collections.emptySet();
	}
}
