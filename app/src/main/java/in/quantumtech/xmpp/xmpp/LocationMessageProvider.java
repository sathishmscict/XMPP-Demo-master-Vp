package in.quantumtech.xmpp.xmpp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by dilli on 11/27/2015.
 */
public class LocationMessageProvider extends ExtensionElementProvider {

    @Override
    public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
        UserLocation location = new UserLocation();

        boolean done = false;
        String tag = null;
        while (!done) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tag = parser.getName();
                    break;

                case XmlPullParser.TEXT:
                    if (UserLocation.TAG_NAME_LATITUDE.equals(tag)) {
                        try {
                            location.setLatitude(Double.parseDouble(parser.getText()));
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    } else if (UserLocation.TAG_NAME_LONGITUDE.equals(tag)) {
                        try {
                            location.setLongitude(Double.parseDouble(parser.getText()));
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    } else if (UserLocation.TAG_NAME_NAME.equals(tag)) {
                        location.setName(parser.getText());
                    } else if(UserLocation.TAG_NAME_ADDRESS.equals(tag)) {
                        location.setAddress(parser.getText());
                    }
                    break;

                case XmlPullParser.END_TAG:
                    // Stop parsing when we hit </addr>
                    done = UserLocation.TAG_NAME_ADDRESS.equals(parser.getName());
                    break;
            }
        }

        return location;
    }
}