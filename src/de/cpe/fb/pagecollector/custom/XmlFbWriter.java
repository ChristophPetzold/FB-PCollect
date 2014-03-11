package de.cpe.fb.pagecollector.custom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.cpe.fb.pagecollector.core.FBFact;
import de.cpe.fb.pagecollector.core.IFBWriter;

public class XmlFbWriter implements IFBWriter {

	/*
	 * (non-Javadoc)
	 * @see de.cpe.fb_crawl.IFBWriter#writeFile(java.lang.String, java.util.Collection)
	 */
	@Override
	public void writeFile(String filename, Collection<FBFact> factTable) throws FactoryConfigurationError {
		try {

			File file = new File(filename);

			if (!file.exists())
				file.createNewFile();

			FileOutputStream fos = new FileOutputStream(file);
			Writer writer = new OutputStreamWriter(fos, "UTF-8");

			XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();

			XMLEventWriter eventWriter = xmlFactory.createXMLEventWriter(writer);

			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
			XMLEvent end = eventFactory.createDTD("\n");

			StartDocument startDocument = eventFactory.createStartDocument("utf-8", "1.0");
			eventWriter.add(startDocument);

			// create table open tag
			StartElement configStartElement = eventFactory.createStartElement("", "", "facts");
			eventWriter.add(configStartElement);
			eventWriter.add(end);

			for (FBFact fact : factTable) {

				createNode(eventWriter, "fact", fact);
			}

			// create table end tag
			eventWriter.add(eventFactory.createEndElement("", "", "facts"));
			eventWriter.add(end);
			eventWriter.add(eventFactory.createEndDocument());
			eventWriter.close();

		} catch (IOException | XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void createNode(XMLEventWriter eventWriter, String name, FBFact fact) throws XMLStreamException {

		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");

		// create Start node
		StartElement sElement = eventFactory.createStartElement("", "", name);
		eventWriter.add(tab);
		eventWriter.add(sElement);

		// Write the different nodes
		createNode(eventWriter, "type", fact.getType().name());
		createNode(eventWriter, "user", stripNonValidXMLCharacters(fact.getUser()));
		createNode(eventWriter, "date", fact.getDate());
		createNode(eventWriter, "relevance", fact.isRelevant() ? "ja" : "nein");
		createNode(eventWriter, "indirectRel", fact.isIndirectRelevant() ? "ja" : "nein");
		createNode(eventWriter, "message", stripNonValidXMLCharacters(fact.getMessage()));
		createNode(eventWriter, "link", stripNonValidXMLCharacters(fact.getAttachedLink()));
		createNode(eventWriter, "picture", stripNonValidXMLCharacters(fact.getPictureLink()));

		// create End node
		EndElement eElement = eventFactory.createEndElement("", "", name);
		eventWriter.add(eElement);
		eventWriter.add(end);

	}

	protected String stripNonValidXMLCharacters(String in) {
		StringBuffer out = new StringBuffer(); // Used to hold the output.
		char current; // Used to reference the current character.

		if (in == null || ("".equals(in)))
			return ""; // vacancy test.
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.

			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}

	protected void createNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {

		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");

		// create Start node
		StartElement sElement = eventFactory.createStartElement("", "", name);
		eventWriter.add(tab);
		eventWriter.add(sElement);

		// create Content
		Characters characters = eventFactory.createCharacters(value);
		eventWriter.add(characters);

		// create End node
		EndElement eElement = eventFactory.createEndElement("", "", name);
		eventWriter.add(eElement);
		eventWriter.add(end);

	}

}
