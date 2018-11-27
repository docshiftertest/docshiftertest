package com.docshifter.core.config.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
//@Repository
//@RepositoryRestResource
public interface NodeRepository extends JpaRepository<Node, Long> {


	@Query("select n from ChainConfiguration cc inner join cc.rootNode n inner join n.moduleConfiguration mc inner join mc.module m where cc.enabled = true")
	List<Node> getEnabledSenderConfigurations();

	@Query("select n from Node n where n.moduleConfiguration.module.classname = '?1'")
	List<Node> getSendersByClassName(String className);

	//TODO
	@Query("select n from Node n")
	Node getSenderByClassNameAndParamValue(String className, String paramValue);
}