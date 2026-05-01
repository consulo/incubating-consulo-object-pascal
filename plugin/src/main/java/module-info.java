/**
 * @author VISTALL
 * @since 2025-04-25
 */
module com.siberika.idea.pascal {
    requires consulo.annotation;
    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.compiler.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.datacontext.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.execution.api;
    requires consulo.execution.debug.api;
    requires consulo.external.service.api;
    requires consulo.file.chooser.api;
    requires consulo.file.editor.api;
    requires consulo.file.template.api;
    requires consulo.ide.api;
    requires consulo.index.io;
    requires consulo.language.api;
    requires consulo.language.code.style.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.language.editor.api;
    requires consulo.language.editor.refactoring.api;
    requires consulo.language.editor.ui.api;
    requires consulo.language.impl;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;
    requires consulo.navigation.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.project.ui.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.undo.redo.api;
    requires consulo.usage.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.virtual.file.system.api;

    requires org.apache.commons.codec;

    requires com.google.common;

    // TODO remove in future
    requires java.desktop;
    requires forms.rt;

    opens com.siberika.idea.pascal.lang.folding to consulo.util.xml.serializer;
    opens com.siberika.idea.pascal.ide.actions to consulo.component.impl;
    opens com.siberika.idea.pascal.editor.settings to consulo.util.xml.serializer;
}
