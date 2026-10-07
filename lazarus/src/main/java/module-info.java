/**
 * @author VISTALL
 * @since 2026-10-06
 */
module consulo.object.pascal.lazarus {
    requires consulo.object.pascal;
    requires com.intellij.xml.api;
    requires consulo.annotation;
    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.base.icon.library;
    requires consulo.build.ui.api;
    requires consulo.compiler.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.datacontext.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.external.system.api;
    requires consulo.file.chooser.api;
    requires consulo.ide.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.creation.api;
    requires consulo.module.ui.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.util.collection;
    requires consulo.util.concurrent;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.virtual.file.system.api;
    requires java.xml;
    requires org.jdom;

    exports consulo.object.pascal.lazarus;
    exports consulo.object.pascal.lazarus.module;
    exports consulo.object.pascal.lazarus.module.orderEntry;
    exports consulo.object.pascal.lazarus.importing;
    exports consulo.object.pascal.lazarus.externalSystem;
    exports consulo.object.pascal.lazarus.setting;
    exports consulo.object.pascal.lazarus.ui;

    opens consulo.object.pascal.lazarus.setting to consulo.util.xml.serializer;
}
