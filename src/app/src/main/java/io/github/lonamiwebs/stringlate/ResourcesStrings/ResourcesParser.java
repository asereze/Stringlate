package io.github.lonamiwebs.stringlate.ResourcesStrings;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;

public class ResourcesParser {
    // We don't use namespaces
    private static final String ns = null;

    public Resources parseFromXml(InputStream in)
            throws XmlPullParserException, IOException {

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return new Resources(readResources(parser));
        } finally {
            try {
                in.close();
            } catch (IOException e) { }
        }
    }

    public boolean parseToXml(Resources resources, Writer out) {
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(out);
            //strings.xml do not have the default start declaration
            //serializer.startDocument("UTF-8", true);
            serializer.startTag(ns, "resources");

            for (ResourcesString rs : resources) {
                if (!rs.hasContent())
                    continue;

                serializer.startTag(ns, "string");
                serializer.attribute(ns, "name", rs.getId());
                if (!rs.isTranslatable())
                    serializer.attribute(ns, "translatable", "false");

                serializer.text(rs.getContent());
                serializer.endTag(ns, "string");
            }
            serializer.endTag(ns, "resources");
            serializer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ArrayList<ResourcesString> readResources(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        ArrayList<ResourcesString> strings = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "resources");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals("string"))
                strings.add(readResourceString(parser));
            else
                skip(parser);
        }
        return strings;
    }

    private ResourcesString readResourceString(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        String id, content;
        boolean translatable = true;

        parser.require(XmlPullParser.START_TAG, ns, "string");

        id = parser.getAttributeValue(null, "name");
        if ("false".equals(parser.getAttributeValue(null, "translatable")))
            translatable = false;
        content = readText(parser);

        parser.require(XmlPullParser.END_TAG, ns, "string");

        return new ResourcesString(id, content, translatable);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            throw new IllegalStateException();

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG: --depth; break;
                case XmlPullParser.START_TAG: ++depth; break;
            }
        }
    }
}
