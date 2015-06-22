package com.docbyte.docshifter;

import com.docbyte.docshifter.model.dao.NodeDAO;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;

public class dbTest {

	public static Node Root;
	private static NodeDAO ndao=new NodeDAO();
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			Module mRoot=new Module(1, "", "", "root", "input", null, null);
			Module m01=new Module(2, "", "", "c1", "input", null, null);
			Module m11=new Module(3, "", "", "cc1", "input", null, null);
			Module m02=new Module(4, "", "", "c2", "input", null, null);
		
			ModuleConfiguration mcroot=new ModuleConfiguration(1, mRoot, "root", "root", null);
			ModuleConfiguration mcc01=new ModuleConfiguration(2, m01, "child p root", "child p root", null);
			ModuleConfiguration mcc11=new ModuleConfiguration(3, m11, "child p child1", "child p child1", null);
			ModuleConfiguration mcc02=new ModuleConfiguration(4, m02, "child 2 p root", "child 2 p root", null);
			
			Root=new Node(null, mcroot);
			Node n01=new Node(Root, mcc01);
			Node n11=new Node(n01,mcc11);
			Node n02=new Node(Root, mcc02);
			
			Save();
			Get(2);
			
			
	}

	private static void delete() {
		// TODO Auto-generated method stub
		ndao.delete(ndao.get(2));
	}

	private static void Change() {
		// TODO Auto-generated method stub
		ModuleConfiguration mNew=new ModuleConfiguration(5, null, "", "new", null);
		Node n=ndao.get(3);
		n.setModuleConfiguration(mNew);
		ndao.update(n);
	}

	private static void Get(int i) {
		// TODO Auto-generated method stub
		Node n=ndao.get(i);
		System.out.println("got "+n.getModuleConfiguration().getDescription());
		System.out.println("parent:"+n.getParentNode().getModuleConfiguration().getDescription());
		for(Node nc:n.getChildNodes())
		{
			System.out.println("child: " +nc.getId());
		}
		
	}

	private static void Save() {
		// TODO Auto-generated method stub
		try {
			ndao.insert(Root);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
