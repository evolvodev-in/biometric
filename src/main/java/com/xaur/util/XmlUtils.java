package com.xaur.util;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;

@Slf4j
public class XmlUtils {

    public static Document parseXml(String xml) throws DocumentException {
        return DocumentHelper.parseText(xml);
    }

    public static String elementText(Element parent, String elementName) {
        Element element = parent.element(elementName);
        return element != null ? element.getTextTrim() : null;
    }

    public static Integer elementInteger(Element parent, String elementName) {
        String text = elementText(parent, elementName);
        if (text != null && !text.isEmpty()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                log.error("Failed to parse integer from element: {}", elementName, e);
            }
        }
        return null;
    }

    public static Boolean elementBoolean(Element parent, String elementName) {
        String text = elementText(parent, elementName);
        if (text != null) {
            return "Yes".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text) || "1".equals(text);
        }
        return null;
    }

    public static String formatXml(Document document) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            StringWriter sw = new StringWriter();
            XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
            return sw.toString();
        } catch (IOException e) {
            log.error("Failed to format XML", e);
            return document.asXML();
        }
    }
}