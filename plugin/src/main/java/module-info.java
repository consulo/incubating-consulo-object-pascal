/**
 * @author VISTALL
 * @since 2025-04-25
 */
module com.siberika.idea.pascal {
    requires consulo.ide.api;

    requires org.apache.commons.codec;

    // TODO remove in future
    requires java.desktop;

    opens com.siberika.idea.pascal.lang.folding to consulo.util.xml.serializer;
}