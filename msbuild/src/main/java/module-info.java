/**
 * @author VISTALL
 * @since 2026-10-07
 */
module consulo.object.pascal.msbuild {
    requires consulo.object.pascal;
    requires consulo.msbuild.api;
    requires consulo.annotation;
    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.component.api;
    requires consulo.disposer.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.creation.api;
    requires consulo.module.ui.api;
    requires consulo.platform.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.util.dataholder;
    requires consulo.util.lang;
    requires consulo.virtual.file.system.api;
    requires org.jdom;

    exports consulo.object.pascal.msbuild;
    exports consulo.object.pascal.msbuild.module;
}
