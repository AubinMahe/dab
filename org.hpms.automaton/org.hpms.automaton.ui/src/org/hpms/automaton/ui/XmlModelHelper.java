package org.hpms.automaton.ui;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class XmlModelHelper implements Constants {

   private static final XPath xPath = XPathFactory.newInstance().newXPath();

   static NodeList getStates( Element root ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
          "//" + TRANSITION_ELEMENT + "/@" + TRANSITION_SOURCE_ATTR +
         "|//" + TRANSITION_ELEMENT + "/@" + TRANSITION_DEST_ATTR ,
         root, XPathConstants.NODESET );
   }

   public static NodeList getShortcuts( Element root ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
         "//" + SHORTCUT_ELEMENT + "/@" + SHORTCUT_DEST_ATTR,
        root, XPathConstants.NODESET );
   }

   static NodeList getStates( Element root, String state ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
          "//" + TRANSITION_ELEMENT + "[@" + TRANSITION_SOURCE_ATTR + "='" + state + "']/@" + TRANSITION_SOURCE_ATTR +
         "|//" + TRANSITION_ELEMENT + "[@" + TRANSITION_DEST_ATTR   + "='" + state + "']/@" + TRANSITION_DEST_ATTR +
         "|//" + SHORTCUT_ELEMENT   + "[@" + SHORTCUT_DEST_ATTR     + "='" + state + "']/@" + SHORTCUT_DEST_ATTR +
         "|//" + ON_ENTRY_ELEMENT   + "[@" + ON_ENTRY_STATE_ATTR    + "='" + state + "']/@" + ON_ENTRY_STATE_ATTR +
         "|//" + ON_EXIT_ELEMENT    + "[@" + ON_EXIT_STATE_ATTR     + "='" + state + "']/@" + ON_EXIT_STATE_ATTR,
         root, XPathConstants.NODESET );
   }

   static Attr getTransition( Element root, EntityConnectionData ecd ) throws XPathExpressionException {
      if( "*".equals( ecd.source )) {
         return (Attr)xPath.evaluate(
            "//" + SHORTCUT_ELEMENT +
            "[@" + SHORTCUT_DEST_ATTR + "='" + ecd.dest + "']/@" + SHORTCUT_EVENT_ATTR,
           root, XPathConstants.NODE );
      }
      return (Attr)xPath.evaluate(
         "//" + TRANSITION_ELEMENT +
         "[@"     + TRANSITION_SOURCE_ATTR + "='" + ecd.source + "'" +
         " and @" + TRANSITION_DEST_ATTR   + "='" + ecd.dest   + "']/@" + TRANSITION_EVENT_ATTR,
         root, XPathConstants.NODE );
   }

   /**
    * Find the reachable states from a given state.
    * @param from the start state
    * @return a collection of reachable states, a {@link NodeList} of {@link Attr}
    * @throws XPathExpressionException
    */
   static NodeList getTransitionsFrom( Element root, String from ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
         "//" + TRANSITION_ELEMENT +
         "[@" + TRANSITION_SOURCE_ATTR + "='" + from + "']/@" + TRANSITION_DEST_ATTR,
         root, XPathConstants.NODESET );
   }

   static String getOnEntryAction( Element root, String state ) throws XPathExpressionException {
      return (String)xPath.evaluate(
         "//" + ON_ENTRY_ELEMENT +
         "[@" + ON_ENTRY_STATE_ATTR + "='" + state + "']/@" + ON_ENTRY_ACTION_ATTR,
         root, XPathConstants.STRING );
   }

   static String getOnExitAction( Element root, String state ) throws XPathExpressionException {
      return (String)xPath.evaluate(
         "//" + ON_EXIT_ELEMENT +
         "[@" + ON_EXIT_STATE_ATTR + "='" + state + "']/@" + ON_EXIT_ACTION_ATTR,
         root, XPathConstants.STRING );
   }

   static void remaneState( Element root, String selected, String newValue ) throws XPathExpressionException {
      final NodeList attrs = getStates( root, selected );
      for( int i = 0, count = attrs.getLength(); i < count; ++i ) {
         final Attr attr = (Attr)attrs.item( i );
         attr.setValue( newValue );
      }
   }

   static void remaneEvent( Element root, EntityConnectionData ecd, String newValue ) throws XPathExpressionException {
      final Attr event = getTransition( root, ecd );
      event.setValue( newValue );
   }

   static void removeState( Element root, String state ) throws XPathExpressionException {
      final NodeList transitions = (NodeList)xPath.evaluate(
         "//" + TRANSITION_ELEMENT +
         "[@"    + TRANSITION_SOURCE_ATTR + "='" + state + "'" +
         " or @" + TRANSITION_DEST_ATTR   + "='" + state + "']",
         root, XPathConstants.NODESET );
      for( int i = 0, count = transitions.getLength(); i < count; ++i ) {
         final Element tr = (Element)transitions.item( i );
         root.removeChild( tr );
      }
      final NodeList shortcutList = (NodeList)xPath.evaluate(
         "//" + SHORTCUT_ELEMENT +
         "[@"    + SHORTCUT_DEST_ATTR + "='" + state + "']",
         root, XPathConstants.NODESET );
      for( int i = 0, count = shortcutList.getLength(); i < count; ++i ) {
         final Element shortcut = (Element)shortcutList.item( i );
         root.removeChild( shortcut );
      }
      final NodeList onEntryList = (NodeList)xPath.evaluate(
         "//" + ON_ENTRY_ELEMENT +
         "[@"    + ON_ENTRY_STATE_ATTR + "='" + state + "']",
         root, XPathConstants.NODESET );
      for( int i = 0, count = onEntryList.getLength(); i < count; ++i ) {
         final Element onEntry = (Element)onEntryList.item( i );
         root.removeChild( onEntry );
      }
      final NodeList onExitList = (NodeList)xPath.evaluate(
         "//" + ON_EXIT_ELEMENT +
         "[@"    + ON_EXIT_STATE_ATTR + "='" + state + "']",
         root, XPathConstants.NODESET );
      for( int i = 0, count = onExitList.getLength(); i < count; ++i ) {
         final Element onExit = (Element)onExitList.item( i );
         root.removeChild( onExit );
      }
   }

   static Element createTransition( Element root, String from, String event, String futur ) {
      final Document doc = root.getOwnerDocument();
      final Element transition = doc.createElement( TRANSITION_ELEMENT );
      transition.setAttribute( TRANSITION_SOURCE_ATTR, from );
      transition.setAttribute( TRANSITION_EVENT_ATTR , event );
      transition.setAttribute( TRANSITION_DEST_ATTR  , futur );
      doc.appendChild( transition );
      return transition;
   }

   static void removeTransition( Element root, EntityConnectionData ecd ) throws XPathExpressionException {
      root.removeChild( getTransition( root, ecd ));
   }

   static void setOnEntryAction( Element root, String state, String action ) throws XPathExpressionException {
      Element onEntry = (Element)xPath.evaluate(
         "//" + ON_ENTRY_ELEMENT +
         "[@" + ON_ENTRY_STATE_ATTR + "='" + state + "']",
         root, XPathConstants.NODE );
      if( onEntry == null ) {
         onEntry = root.getOwnerDocument().createElement( ON_ENTRY_ELEMENT );
      }
      onEntry.setAttribute( ON_ENTRY_ACTION_ATTR, action );
   }

   static void setOnExitAction( Element root, String state, String action ) throws XPathExpressionException {
      Element onExit = (Element)xPath.evaluate(
         "//" + ON_EXIT_ELEMENT +
         "[@" + ON_EXIT_STATE_ATTR + "='" + state + "']",
         root, XPathConstants.NODE );
      if( onExit == null ) {
         onExit = root.getOwnerDocument().createElement( ON_EXIT_ELEMENT );
      }
      onExit.setAttribute( ON_EXIT_ACTION_ATTR, action );
   }

   private XmlModelHelper() {/**/}
}
