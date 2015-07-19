package demo;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.robcrocombe.XMLParser;
import com.robcrocombe.XMLParser.Rule;
import com.robcrocombe.XMLParser.Type;

public class Demo
{
	public static void main(String[] args) throws Exception
	{
		final Generator generator = new Generator();
		generator.properties = new ArrayList<AntProperty>();
		generator.name = "ower" + "/" + "repo";
		
		Rule descAttrRule = new Rule(Type.ELEMENT, 0, "project")
		{
			@Override
			public void handleElement(Node node, NamedNodeMap attr)
			{
				Node descNode = attr.getNamedItem("description");
				
				if (descNode != null)
				{
					generator.description = descNode.getNodeValue();
				}
			}
		};
		
		Rule descElemRule = new Rule(Type.CONTENT, 1, "description")
		{
			@Override
			public void handleContent(Node node, String value)
			{
				if (value != null && !value.isEmpty() && generator.description == null)
				{
					generator.description = value;
				}
			}
		};
		
		Rule propertyRule = new Rule(Type.COMMENT, 1)
		{
			@Override
			public void handleComment(Node node, String value)
			{
			    Node property = node.getNextSibling();
		    	
		    	while (property.getNodeType() != Node.ELEMENT_NODE)
		    	{
		    		property = property.getNextSibling();
		    	}
				
		    	AntProperty p = new AntProperty();
		    	
		    	p.name = XMLParser.getAttributeValue(property, "name");
		    	p.value = XMLParser.getAttributeValue(property, "value");
		    	p.description = XMLParser.getAttributeValue(property, "description");
		    	
		    	generator.properties.add(p);
			}
		};
		
		XMLParser parser = new XMLParser(descAttrRule, descElemRule, propertyRule);
		parser.parse(ClassLoader.getSystemResourceAsStream("demo/build.xml"));
	}
	
	public static class Generator
    {
		public String name;
		public String description;
		public ArrayList<AntProperty> properties;
    }
	
	public static class AntProperty
    {
		public String name;
		public String value;
		public String description;
    }
}
