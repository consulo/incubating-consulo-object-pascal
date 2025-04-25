/**
 * @author VISTALL
 * @since 2025-04-25
 */
module com.siberika.idea.pascal {
    requires consulo.ide.api;

    requires org.apache.commons.codec;

    requires com.google.common;

    // TODO remove in future
    requires java.desktop;

    opens com.siberika.idea.pascal.lang.folding to consulo.util.xml.serializer;
    opens com.siberika.idea.pascal.ide.actions to consulo.component.impl;
}