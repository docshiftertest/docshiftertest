package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.Node;

public interface INodeDAO
{
	public Node get(Long id);
	public List<Node> get();
	
	public Node insert(Node node) throws Exception;
	public Node update(Node node);
	
	public void delete(Node node);

}
