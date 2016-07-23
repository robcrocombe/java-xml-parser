package com.robcrocombe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser
{
	private List<Rule> rules;
	private int maxDepth = 0;

	/**
	 * Create a new parser that uses the given {@link Rule}s when parsing any
	 * XML content.
	 *
	 * @param rules
	 * 		The rules applied to any parsed content.
	 */
	public XMLParser(Rule... rules)
	{
		this.rules = Arrays.asList(rules);

		for (Rule rule : rules)
		{
			if (rule.depth > maxDepth)
			{
				maxDepth = rule.depth;
			}
		}
	}

	/**
	 * Returns the attribute's String value from the given Node.
	 *
	 * @param node
	 * 		An XML Node with attributes.
	 * @param name
	 * 		The name of the attribute to return the value of.
	 * @return
	 * 		The String value of the attribute.
	 */
	public static String getAttributeValue(Node node, String name)
	{
		NamedNodeMap map = node.getAttributes();
		Node item = map.getNamedItem(name);
		if (item == null) return null;

		String value = item.getNodeValue();
		if (value == null || value.isEmpty()) return null;

		return value;
	}

	/**
	 * Start parsing the XML and execute the rules.
	 *
	 * @param source
	 * 		An InputStream representing the XML source.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void parse(InputStream source) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(source);

		int level = -1;
		parse(doc.getFirstChild(), level);
	}

	private void parse(Node node, int level)
	{
		level++;
		if (level > maxDepth)
		{
			// No need to go further if no rules will be executed.
			return;
		}

		if (node.getNodeType() == Node.COMMENT_NODE)
		{
			for (Rule rule : rules)
			{
				if (rule.type == Type.COMMENT && rule.depth == level)
				{
					rule.handleComment(node, node.getTextContent());
				}
			}
		}
		else
		{
			for (Rule rule : rules)
			{
				if (rule.type == Type.ELEMENT && rule.depth == level
						&& rule.name.equals(node.getNodeName()))
				{
					rule.handleElement(node, node.getAttributes());
				}
				else if (rule.type == Type.CONTENT && rule.depth == level
						&& rule.name.equals(node.getNodeName()))
				{
					rule.handleContent(node, node.getTextContent());
				}
			}
		}

		// Recurse through each child node
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i)
		{
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE ||
					currentNode.getNodeType() == Node.COMMENT_NODE)
			{
				parse(currentNode, level);
			}
		}
	}

	/**
	 * The type of {@link Rule}.
	 */
	public static enum Type
	{
		/**
		 * A regular node with attributes.
		 */
		ELEMENT,
		/**
		 * The text between two tags.
		 */
		CONTENT,
		/**
		 * An XML comment.
		 */
		COMMENT
	}

	/**
	 * A class for executing code to act on certain XML elements.
	 */
	public static class Rule
	{
		public Type type;
		public int depth;
		public String name;

		/**
		 * The constructor for {@link Type#COMMENT} only.
		 *
		 * @param type
		 * 		The {@link Type} of rule this is for.
		 * @param depth
		 * 		The depth this XML element will appear at. Zero is the root element.
		 * @throws IllegalArgumentException
		 */
		public Rule(Type type, int depth) throws IllegalArgumentException
		{
			if (type != Type.COMMENT)
			{
				throw new IllegalArgumentException(
						"This constructor can only be used with Type.COMMENT");
			}

			this.type = type;
			this.depth = depth;
		}
		/**
		 * /**
		 * The constructor for {@link Type#ELEMENT} and {@link Type#CONTENT} only.
		 *
		 * @param type
		 * 		The {@link Type} of rule this is for.
		 * @param depth
		 * 		The depth this XML element will appear at. Zero is the root element.
		 * @param name
		 * 		The name of the XML element to search for.
		 * @throws IllegalArgumentException
		 */
		public Rule(Type type, int depth, String name) throws IllegalArgumentException
		{
			if (type == Type.COMMENT)
			{
				throw new IllegalArgumentException(
						"This constructor cannot be used with Type.COMMENT");
			}

			this.type = type;
			this.depth = depth;
			this.name = name;
		}

		/**
		 * The event method for handling {@link Type#ELEMENT}s.
		 * @param node
		 * 		The XML node at this location.
		 * @param attributes
		 * 		The element's associated attributes.
		 */
		public void handleElement(Node node, NamedNodeMap attributes)
		{ };

		/**
		 * The event method for handling {@link Type#CONTENT}s.
		 * @param node
		 * 		The XML node at this location.
		 * @param value
		 * 		The String value.
		 */
		public void handleContent(Node node, String value)
		{ };

		/**
		 * The event method for handling {@link Type#COMMENT}s.
		 * @param node
		 * 		The XML node at this location.
		 * @param value
		 * 		The String value of the comment.
		 */
		public void handleComment(Node node, String value)
		{ };
	}
}
